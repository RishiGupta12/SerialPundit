/***************************************************************************************************
 * Author : Rishi Gupta
 *
 * This file is part of 'serial communication manager' library.
 *
 * The 'serial communication manager' is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * The 'serial communication manager' is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with serial communication manager. If not, see <http://www.gnu.org/licenses/>.
 *
 ***************************************************************************************************/

/* We have tried to follow the philosophy that resources specific to thread should be held by thread
 * and that the thread is responsible for cleaning them before exiting. As appropriate a policy is
 * followed that if listener exist and port is removed, CPU usage does not go very high. */

#include <unistd.h>     	/* UNIX standard function definitions */
#include <stdio.h>
#include <stdlib.h>     	/* Standard ANSI routines             */
#include <string.h>     	/* String function definitions        */
#include <fcntl.h>      	/* File control definitions           */
#include <errno.h>      	/* Error number definitions           */
#include <dirent.h>     	/* Format of directory entries        */
#include <sys/types.h>  	/* Primitive System Data Types        */
#include <sys/stat.h>   	/* Defines the structure of the data  */
#include <pthread.h>		/* POSIX thread definitions	          */
#include <sys/select.h>

#if defined (__linux__)
#include <linux/types.h>
#include <linux/termios.h>  /* POSIX terminal control definitions for Linux (termios2) */
#include <linux/serial.h>
#include <linux/ioctl.h>
#include <sys/eventfd.h>    /* Linux eventfd for event notification. */
#include <sys/epoll.h>      /* epoll feature of Linux	              */
#include <signal.h>
#include <libudev.h>
#include <locale.h>
#include <time.h>
#endif

#if defined (__APPLE__)
#include <termios.h>
#include <paths.h>
#include <sys/ioctl.h>
#include <sysexits.h>
#include <sys/param.h>
#include <sys/event.h>
#include <CoreFoundation/CoreFoundation.h>
#include <IOKit/IOKitLib.h>
#include <IOKit/serial/IOSerialKeys.h>
#include <IOKit/serial/ioss.h>
#include <IOKit/IOBSD.h>
#include <IOKit/IOMessage.h>
#include <IOKit/usb/IOUSBLib.h>
#endif

#if defined (__SunOS)
#include <termios.h>
#include <sys/ioctl.h>
#include <sys/filio.h>
#endif

#include <jni.h>
#include "unix_like_serial_lib.h"

JavaVM *jvm_event;

/* pselect() is used to provide delay whenever required. It returns errno as is and let caller decide
 * what to do if pselect fails. This returns negative value if function fails. The caller must multiply
 * return value by -1 to get actual errno value. */
int serial_delay(unsigned milliSeconds) {

	int ret = 0;
	struct timespec ts;
	ts.tv_sec  = milliSeconds/1000;
	ts.tv_nsec = 0;
	
	errno = 0;
	ret = pselect(1, 0, 0, 0, &ts, 0);
	if(ret < 0) {
		return -1 * errno;
	}
	
	return 0;
}

/* This thread wait for data to be available on fd and enqueues it in data queue managed by java layer.
 * For unrecoverable errors thread would like to exit and try again. */
void *data_looper(void *arg) {
	int i = -1;
	int index = 0;
	int partial_data = -1;
	int error_count = 0;
	ssize_t ret = -1;
	jbyte buffer[2 * 1024];
	jbyte final_buf[1024 * 3]; /* Sufficient enough to deal with consecutive multiple partial reads. */
	jbyteArray dataRead = NULL;
	int data_available = 0;
	void* env1 = NULL;
	JNIEnv* env = NULL;
	jclass SerialComLooper = NULL;
	jmethodID mid = NULL;
	jmethodID mide = NULL;

#if defined (__linux__)
	/* Epoll is used for Linux systems.
	 * ev_port refers to serial port fd and ev_exit refers to fd on which we make an event happen explicitly
	 * so as to signal epoll_wait come out of waiting state. */
	int MAXEVENTS = 4;
	int epfd = 0;
	int evfd = 0;
	struct epoll_event ev_port;
	struct epoll_event ev_exit;
	struct epoll_event event;
	struct epoll_event *events = NULL;
#endif
#if defined (__APPLE__)
	/* Kqueue is used for MAC OS X systems. */
	int kq;
	struct kevent chlist[2]; /* events to monitor */
	struct kevent evlist[2]; /* events that were triggered */
	int pipe1[2];            /* pipe1[0] is reading end, and pipe1[1] is writing end. */
#endif

	pthread_mutex_lock(((struct com_thread_params*) arg)->mutex);

	struct com_thread_params* params = (struct com_thread_params*) arg;
	JavaVM *jvm = (*params).jvm;
	int fd = (*params).fd;
	jobject looper = (*params).looper;

	/* The JNIEnv is valid only in the current thread. So, threads created in C should attach itself to the VM
	 * and obtain a JNI interface pointer. */
	if((*jvm)->AttachCurrentThread(jvm, &env1, NULL) != JNI_OK) {
		((struct com_thread_params*) arg)->data_custom_err_code = E_ATTACHCURRENTTHREAD;
		((struct com_thread_params*) arg)->data_init_done = 2;
		pthread_mutex_unlock(((struct com_thread_params*) arg)->mutex);
		pthread_exit((void *)0);
	}
	env = (JNIEnv*) env1;

	/* Local references are valid for the duration of a native method call.
	   They are freed automatically after the native method returns.
	   Local references are only valid in the thread in which they are created.
	   The native code must not pass local references from one thread to another if required. */
	SerialComLooper = (*env)->GetObjectClass(env, looper);
	if((SerialComLooper == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		(*jvm)->DetachCurrentThread(jvm);
		((struct com_thread_params*) arg)->data_custom_err_code = E_GETOBJECTCLASS;
		((struct com_thread_params*) arg)->data_init_done = 2;
		pthread_mutex_unlock(((struct com_thread_params*) arg)->mutex);
		pthread_exit((void *)0);
	}

	mid = (*env)->GetMethodID(env, SerialComLooper, "insertInDataQueue", "([B)V");
	if((mid == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		(*jvm)->DetachCurrentThread(jvm);
		((struct com_thread_params*) arg)->data_custom_err_code = E_GETMETHODID;
		((struct com_thread_params*) arg)->data_init_done = 2;
		pthread_mutex_unlock(((struct com_thread_params*) arg)->mutex);
		pthread_exit((void *)0);
	}

	mide = (*env)->GetMethodID(env, SerialComLooper, "insertInDataErrorQueue", "(I)V");
	if((mide == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		(*jvm)->DetachCurrentThread(jvm);
		((struct com_thread_params*) arg)->data_custom_err_code = E_GETMETHODID;
		((struct com_thread_params*) arg)->data_init_done = 2;
		pthread_mutex_unlock(((struct com_thread_params*) arg)->mutex);
		pthread_exit((void *)0);
	}

#if defined (__linux__)
	events = calloc(MAXEVENTS, sizeof(event));
	if(events == NULL) {
		(*jvm)->DetachCurrentThread(jvm);
		((struct com_thread_params*) arg)->data_custom_err_code = E_CALLOC;
		((struct com_thread_params*) arg)->data_init_done = 2;
		pthread_mutex_unlock(((struct com_thread_params*) arg)->mutex);
		pthread_exit((void *)0);
	}

	errno = 0;
	evfd  = eventfd(0, 0);
	if(evfd < 0) {
		(*jvm)->DetachCurrentThread(jvm);
		free(events);
		((struct com_thread_params*) arg)->data_standard_err_code = errno;
		((struct com_thread_params*) arg)->data_init_done = 2;
		pthread_mutex_unlock(((struct com_thread_params*) arg)->mutex);
		pthread_exit((void *)0);
	}
	((struct com_thread_params*) arg)->evfd = evfd;  /* Save evfd for cleanup. */

	errno = 0;
	epfd = epoll_create(2);
	if(epfd < 0) {
		(*jvm)->DetachCurrentThread(jvm);
		free(events);
		close(((struct com_thread_params*) arg)->evfd);
		((struct com_thread_params*) arg)->data_standard_err_code = errno;
		((struct com_thread_params*) arg)->data_init_done = 2;
		pthread_mutex_unlock(((struct com_thread_params*) arg)->mutex);
		pthread_exit((void *)0);
	}

	/* Add serial port to epoll wait mechanism. Use level triggered (returned immediately if there is data in read buffer)
	 * epoll mechanism.  */
	ev_port.events = (EPOLLIN | EPOLLPRI | EPOLLERR | EPOLLHUP);
	ev_port.data.fd = fd;
	errno = 0;
	ret = epoll_ctl(epfd, EPOLL_CTL_ADD, fd, &ev_port);
	if(ret < 0) {
		(*jvm)->DetachCurrentThread(jvm);
		free(events);
		close(epfd);
		close(((struct com_thread_params*) arg)->evfd);
		((struct com_thread_params*) arg)->data_standard_err_code = errno;
		((struct com_thread_params*) arg)->data_init_done = 2;
		pthread_mutex_unlock(((struct com_thread_params*) arg)->mutex);
		pthread_exit((void *)0);
	}

	/* add our thread exit signal fd to epoll wait mechanism. */
	ev_exit.events = (EPOLLIN | EPOLLPRI | EPOLLERR | EPOLLHUP);
	ev_exit.data.fd = ((struct com_thread_params*) arg)->evfd;
	errno = 0;
	ret = epoll_ctl(epfd, EPOLL_CTL_ADD, ((struct com_thread_params*) arg)->evfd, &ev_exit);
	if(ret < 0) {
		(*jvm)->DetachCurrentThread(jvm);
		free(events);
		close(epfd);
		close(((struct com_thread_params*) arg)->evfd);
		((struct com_thread_params*) arg)->data_standard_err_code = errno;
		((struct com_thread_params*) arg)->data_init_done = 2;
		pthread_mutex_unlock(((struct com_thread_params*) arg)->mutex);
		pthread_exit((void *)0);
	}
#endif
#if defined (__APPLE__)
	errno = 0;
	ret = pipe(pipe1);
	if(ret < 0) {
		(*jvm)->DetachCurrentThread(jvm);
		((struct com_thread_params*) arg)->data_standard_err_code = errno;
		((struct com_thread_params*) arg)->data_init_done = 2;
		pthread_mutex_unlock(((struct com_thread_params*) arg)->mutex);
		pthread_exit((void *)0);
	}
	((struct com_thread_params*) arg)->evfd = pipe1[1];  /* Save writing end of pipe for exit and cleanup. */

	/* The kqueue() system call creates a new kernel event queue and returns a file descriptor. */
	errno = 0;
	kq = kqueue();
	if(kq < 0) {
		(*jvm)->DetachCurrentThread(jvm);
		close(pipe1[0]);
		close(pipe1[1]);
		((struct com_thread_params*) arg)->data_standard_err_code = errno;
		((struct com_thread_params*) arg)->data_init_done = 2;
		pthread_mutex_unlock(((struct com_thread_params*) arg)->mutex);
		pthread_exit((void *)0);
	}

	/* Initialize what changes to be monitor on which file descriptor. */
	EV_SET(&chlist[0], fd, EVFILT_READ, EV_ADD , 0, 0, NULL);
	EV_SET(&chlist[1], pipe1[0], EVFILT_READ, EV_ADD , 0, 0, NULL);
#endif

	/* indicate success to the caller so it can return success to java layer */
	((struct com_thread_params*) arg)->data_init_done = 0;
	pthread_mutex_unlock(((struct com_thread_params*) arg)->mutex);

	/* This keep looping until listener is unregistered, waiting for data and passing it to java layer. */
	while(1) {
		index = 0;           /* reset */
		partial_data = 0;    /* reset */
		data_available = 0;  /* reset */

#if defined (__linux__)
		errno = 0;
		ret = epoll_wait(epfd, events, MAXEVENTS, -1);

		if(ret <= 0) {
			/* ret < 0 if error occurs, ret = 0 if no fd available for read.
			 * for error (unlikely to happen) just restart looping. */
			continue;
		}
#endif
#if defined (__APPLE__)
		errno = 0;
		ret = kevent(kq, chlist, 2, evlist, 2, NULL);
		if(ret <= 0) {
			/* for error (unlikely to happen) just restart looping. */
			continue;
		}
#endif
#if defined (__linux__)
		if((events[0].data.fd == evfd) || (events[1].data.fd == evfd)) {
			/* check if thread should exit due to un-registration of listener. */
			if(1 == ((struct com_thread_params*) arg)->data_thread_exit) {
				(*jvm)->DetachCurrentThread(jvm);
				close(epfd);
				close(((struct com_thread_params*) arg)->evfd);
				free(events);
				pthread_exit((void *)0);
			}
		}
#endif
#if defined (__APPLE__)
		/* Depending upon how many events has happened, pipe1[0] fd can be at 1st or 2nd
		 * index in evlist array. */
		if((evlist[0].ident == pipe1[0]) || (evlist[1].ident == pipe1[0])) {
			/* check if thread should exit due to un-registration of listener. */
			if(1 == ((struct com_thread_params*) arg)->data_thread_exit) {
				(*jvm)->DetachCurrentThread(jvm);
				close(kq);
				close(pipe1[0]);
				close(pipe1[1]);
				pthread_exit((void *)0);
			}
		}
#endif

#if defined (__linux__)
		if((events[0].events & EPOLLIN) && !(events[0].events & EPOLLERR)) {
#endif
#if defined (__APPLE__)
			if((evlist[0].ident == fd) && !(evlist[0].flags & EV_ERROR)) {
#endif
				/* input event happened, no error occurred, we have data to read on file descriptor. */
				do {

					errno = 0;
					ret = read(fd, buffer, sizeof(buffer));
					if(ret > 0 && errno == 0) {
						/* This indicates we got success and have read data. */
						/* If there is partial data read previously, append this data. */
						if(partial_data == 1) {
							for(i = 0; i < ret; i++) {
								final_buf[index] = buffer[i];
								index++;
							}
							dataRead = (*env)->NewByteArray(env, index);
							(*env)->SetByteArrayRegion(env, dataRead, 0, index, final_buf);
							data_available = 1;
							break;
						}else {
							/* Pass the successful read to java layer straight away. */
							dataRead = (*env)->NewByteArray(env, ret);
							(*env)->SetByteArrayRegion(env, dataRead, 0, ret, buffer);
							data_available = 1;
							break;
						}
					}else if(ret > 0 && errno == EINTR) {
						/* This indicates, there is data to read, however, we got interrupted before we finish reading
						 * all of the available data. So we need to save this partial data and get back to read remaining. */
						for(i = 0; i < ret; i++) {
							final_buf[index] = buffer[i];
							index++;
						}
						partial_data = 1;
						continue;
					}else if(ret < 0) {
						if(errno == EINTR) {
							/* This indicates that we should retry as we are just interrupted by a signal. */
							continue;
						}else {
							/* This indicates, there was data to read but we got an error during read operation, notify application. */
							(*env)->CallVoidMethod(env, looper, mide, errno);
							if((*env)->ExceptionOccurred(env)) {
								(*env)->ExceptionClear(env);
							}
							break;
						}
					}else if(ret == 0) {
						/* Not possible because fd has data as indicated by epoll/kevent. */
						break;
					}else {
					}
				} while(1);

				if(data_available == 1) {
					/* once we have successfully read the data, let us pass this to java layer. */
					(*env)->CallVoidMethod(env, looper, mid, dataRead);
					if((*env)->ExceptionOccurred(env)) {
						(*env)->ExceptionClear(env);
					}
				}

			}else {
#if defined (__linux__)
				if(events[0].events & (EPOLLERR|EPOLLHUP)) {
					error_count++;
					/* minimize JNI transition by setting threshold for when application will be called. */
					if(error_count == 100) {
						(*env)->CallVoidMethod(env, looper, mide, events[0].events);
						if((*env)->ExceptionOccurred(env)) {
							(*env)->ExceptionClear(env);
						}
						error_count = 0; // reset error count
					}
				}
#endif
#if defined (__APPLE__)
				if(evlist[0].flags & EV_ERROR) {
					error_count++;
					/* minimize JNI transition by setting threshold for when application will be called. */
					if(error_count == 100) {
						(*env)->CallVoidMethod(env, looper, mide, evlist[0].data);
						if((*env)->ExceptionOccurred(env)) {
							(*env)->ExceptionClear(env);
						}
						error_count = 0; // reset error count
					}
				}
#endif
#if defined (__SunOS)
				//TODO solaris
#endif
			}
		} /* Go back to loop (while loop) again waiting for the data, available to read. */

	return ((void *)0);
}

/* This handler is invoked whenever application unregisters event listener. */
void event_exit_signal_handler(int signal_number) {
	int ret = -1;
	if(signal_number == SIGUSR1) {
		ret = (*jvm_event)->DetachCurrentThread(jvm_event);
		if(ret != JNI_OK) {
			LOGE(E_DETACHCURTHREAD, "exit signal handler.");
		}
		pthread_exit((void *)0);
	}
}

/* This thread wait for a serial event to occur and enqueues it in event queue managed by java layer. */
/* TIOCMWAIT RETURNS -EIO IF DEVICE FROM USB PORT HAS BEEN REMOVED */
void *event_looper(void *arg) {
	int ret = 0;
	int CTS =  0x01;  // 0000001
	int DSR =  0x02;  // 0000010
	int DCD =  0x04;  // 0000100
	int RI  =  0x08;  // 0001000
	int lines_status = 0;
	int cts,dsr,dcd,ri = 0;
	int event = 0;

	void* env1;
	JNIEnv* env;
	struct com_thread_params* params = (struct com_thread_params*) arg;
	JavaVM *jvm = (*params).jvm;
	jvm_event = (*params).jvm;
	int fd = (*params).fd;
	jobject looper = (*params).looper;
	jclass SerialComLooper = NULL;
	jmethodID mid = NULL;
#if defined (__APPLE__)
	int oldstate = 0;
	int newstate = 0;
#endif

	pthread_mutex_lock(((struct com_thread_params*) arg)->mutex);

	/* The JNIEnv is valid only in the current thread. So, threads created should attach itself to the VM and obtain a JNI interface pointer. */
	if((*jvm)->AttachCurrentThread(jvm, &env1, NULL) != JNI_OK) {
		((struct com_thread_params*) arg)->event_init_done = E_ATTACHCURRENTTHREAD;
		pthread_mutex_unlock(((struct com_thread_params*) arg)->mutex);
		pthread_exit((void *)0);
	}
	env = (JNIEnv*) env1;

	SerialComLooper = (*env)->GetObjectClass(env, looper);
	if((SerialComLooper == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		(*jvm)->DetachCurrentThread(jvm);
		((struct com_thread_params*) arg)->event_custom_err_code = E_GETOBJECTCLASS;
		((struct com_thread_params*) arg)->event_init_done = 2;
		pthread_mutex_unlock(((struct com_thread_params*) arg)->mutex);
		pthread_exit((void *)0);
	}

	mid = (*env)->GetMethodID(env, SerialComLooper, "insertInEventQueue", "(I)V");
	if((mid == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		(*jvm)->DetachCurrentThread(jvm);
		((struct com_thread_params*) arg)->event_custom_err_code = E_GETMETHODID;
		((struct com_thread_params*) arg)->event_init_done = 2;
		pthread_mutex_unlock(((struct com_thread_params*) arg)->mutex);
		pthread_exit((void *)0);
	}

	/* Install signal handler that will be invoked to indicate that the thread should exit. */
	if(signal(SIGUSR1, event_exit_signal_handler) == SIG_ERR) {
		(*jvm)->DetachCurrentThread(jvm);
		((struct com_thread_params*) arg)->event_custom_err_code = E_SIGNALINSTFAIL;
		((struct com_thread_params*) arg)->event_init_done = 2;
		pthread_mutex_unlock(((struct com_thread_params*) arg)->mutex);
		pthread_exit((void *)0);
	}

	/* indicate success to caller so it can return success to java layer */
	((struct com_thread_params*) arg)->event_init_done = 1;
	pthread_mutex_unlock(((struct com_thread_params*) arg)->mutex);

	/* This keep looping until listener is unregistered, waiting for events and passing it to java layer.
	 * This sleep within the kernel until something happens to the MSR register of the tty device. */
	while(1) {
		lines_status = 0;
		cts = 0;
		dsr = 0;
		dcd = 0;
		ri = 0;
		event = 0;

#if defined (__linux__)
		/* When the user removes port on which this thread was calling this ioctl, this thread keep giving
		 * error -5 and keep looping in this ioctl for Linux. */
		errno = 0;
		ret = ioctl(fd, TIOCMIWAIT, TIOCM_DSR | TIOCM_CTS | TIOCM_CD | TIOCM_RNG);
		if(ret < 0) {
			/*if(DBG) fprintf(stderr, "%s%d\n", "NATIVE event_looper() failed in ioctl TIOCMIWAIT with error number : -", errno);
				if(DBG) fflush(stderr);*/
			continue;
		}

#endif
#if defined (__APPLE__)
		usleep(500000); /* 500 milliseconds */
#endif

		/* Something happened on status line so get it. */
		errno = 0;
		ret = ioctl(fd, TIOCMGET, &lines_status);
		if(ret < 0) {
			/* todo consider error reporting like data looper here. data and event error may be mutually exclusive*/
			continue;
		}

		cts = (lines_status & TIOCM_CTS) ? 1 : 0;
		dsr = (lines_status & TIOCM_DSR) ? 1 : 0;
		dcd = (lines_status & TIOCM_CD)  ? 1 : 0;
		ri  = (lines_status & TIOCM_RI)  ? 1 : 0;

		if(cts) {
			event = event | CTS;
		}
		if(dsr) {
			event = event | DSR;
		}
		if(dcd) {
			event = event | DCD;
		}
		if(ri) {
			event = event | RI;
		}

#if defined (__linux__)
		/* Pass this to java layer inserting event in event queue. */
		(*env)->CallVoidMethod(env, looper, mid, event);
		if((*env)->ExceptionOccurred(env)) {
			(*env)->ExceptionClear(env);
		}
#endif
#if defined (__APPLE__)
		newstate = event;
		if(newstate != oldstate) {
			/* Pass this to java layer inserting event in event queue. */
			(*env)->CallVoidMethod(env, looper, mid, event);
			if((*env)->ExceptionOccurred(env)) {
				(*env)->ExceptionClear(env);
			}
			oldstate = newstate;
		}
#endif
	} /* Go back to loop again waiting for event to happen. */

	return ((void *)0);
}

#if defined (__APPLE__)
/* Each thread has exactly one run loop associated with it. This forces run loop to stop running and return
 * control to the function that called CFRunLoopRun i.e. this thread's usb_hot_plug_monitor(). The function
 * CFRunLoopStop() may not be thread safe so we invoke/execute it from worker thread itself to prevent
 * thread deadlock between main and worker thread. */
void mac_indicate_thread_exit(void *info) {
	CFRunLoopStop(CFRunLoopGetCurrent());
}

/* Callback associated with run loop which will be invoked whenever
 * a matching USB device is removed from system. */
void mac_usb_device_removed(void *refCon, io_iterator_t iterator) {
	int usb_vid;
	int usb_pid;
	CFNumberRef num_ref;
	io_service_t usb_dev_obj = 0;
	int USB_DEV_ANY = 0x00;
	int USB_DEV_REMOVED = 0x02;
	JNIEnv* env = ((struct port_info*) refCon)->env;

	if(((struct port_info*) refCon)->empty_iterator_removed != 0) {
		while ((usb_dev_obj = IOIteratorNext(iterator)) != 0) {
			if(((struct port_info*) refCon)->filterVID != USB_DEV_ANY) {
				num_ref = (CFNumberRef) IORegistryEntrySearchCFProperty(usb_dev_obj, kIOServicePlane, CFSTR("idVendor"),
						NULL, kIORegistryIterateRecursively | kIORegistryIterateParents);
				if(num_ref) {
					CFNumberGetValue(num_ref, kCFNumberSInt32Type, &usb_vid);
					CFRelease(num_ref);
					if(((struct port_info*) refCon)->filterVID != usb_vid) {
						IOObjectRelease(usb_dev_obj);
						continue;
					}
				}
			}
			if(((struct port_info*) refCon)->filterPID != USB_DEV_ANY) {
				num_ref = (CFNumberRef) IORegistryEntrySearchCFProperty(usb_dev_obj, kIOServicePlane, CFSTR("idProduct"),
						NULL, kIORegistryIterateRecursively | kIORegistryIterateParents);
				if(num_ref) {
					CFNumberGetValue(num_ref, kCFNumberSInt32Type, &usb_pid);
					CFRelease(num_ref);
					if(((struct port_info*) refCon)->filterPID != usb_pid) {
						IOObjectRelease(usb_dev_obj);
						continue;
					}
				}
			}
			/* Reaching here means both USB VID and PID criteria is met. */
			(*env)->CallVoidMethod(env, ((struct port_info*) refCon)->usbHotPlugEventListener,
					((struct port_info*) refCon)->onHotPlugEventMethodID, USB_DEV_REMOVED);
			if((*env)->ExceptionOccurred(env)) {
				throw_serialcom_exception(env, 3, 0, E_CALLVOIDMETHDSTR);
			}

			IOObjectRelease(usb_dev_obj);
		}
	}else {
		/* empty the iterator for the very first time this function is called */
		while ((usb_dev_obj = IOIteratorNext(iterator)) != 0) {
			IOObjectRelease(usb_dev_obj);
		}
		((struct port_info*) refCon)->empty_iterator_removed = 1;
	}
}

/* Callback associated with run loop which will be invoked whenever
 * a matching USB device is added into system. */
void mac_usb_device_added(void *refCon, io_iterator_t iterator) {
	int usb_vid;
	int usb_pid;
	CFNumberRef num_ref;
	io_service_t usb_dev_obj = 0;
	int USB_DEV_ANY = 0x00;
	int USB_DEV_ADDED = 0x01;
	JNIEnv* env = ((struct port_info*) refCon)->env;

	if(((struct port_info*) refCon)->empty_iterator_added != 0) {
		while ((usb_dev_obj = IOIteratorNext(iterator)) != 0) {
			if(((struct port_info*) refCon)->filterVID != USB_DEV_ANY) {
				num_ref = (CFNumberRef) IORegistryEntrySearchCFProperty(usb_dev_obj, kIOServicePlane, CFSTR("idVendor"),
						NULL, kIORegistryIterateRecursively | kIORegistryIterateParents);
				if(num_ref) {
					CFNumberGetValue(num_ref, kCFNumberSInt32Type, &usb_vid);
					CFRelease(num_ref);
					if(((struct port_info*) refCon)->filterVID != usb_vid) {
						IOObjectRelease(usb_dev_obj);
						continue;
					}
				}
			}
			if(((struct port_info*) refCon)->filterPID != USB_DEV_ANY) {
				num_ref = (CFNumberRef) IORegistryEntrySearchCFProperty(usb_dev_obj, kIOServicePlane, CFSTR("idProduct"),
						NULL, kIORegistryIterateRecursively | kIORegistryIterateParents);
				if(num_ref) {
					CFNumberGetValue(num_ref, kCFNumberSInt32Type, &usb_pid);
					CFRelease(num_ref);
					if(((struct port_info*) refCon)->filterPID != usb_pid) {
						IOObjectRelease(usb_dev_obj);
						continue;
					}
				}
			}
			/* Reaching here means both USB VID and PID criteria is met. */
			(*env)->CallVoidMethod(env, ((struct port_info*) refCon)->usbHotPlugEventListener,
					((struct port_info*) refCon)->onHotPlugEventMethodID, USB_DEV_ADDED);
			if((*env)->ExceptionOccurred(env)) {
				throw_serialcom_exception(env, 3, 0, E_CALLVOIDMETHDSTR);
			}

			IOObjectRelease(usb_dev_obj);
		}
	}else {
		/* empty the iterator for the very first time this function is called */
		while ((usb_dev_obj = IOIteratorNext(iterator)) != 0) {
			IOObjectRelease(usb_dev_obj);
		}
		((struct port_info*) refCon)->empty_iterator_added = 1;
	}
}
#endif

/*
 * This worker thread monitors usb events and notifies application as appropriate. Platform specific facility
 * has been used to terminate thread with any thread dead-lock and ensuring resource clean up.
 *
 * For Linux : libudev is used to monitor events, extract info about events etc. To destroy this thread main thread
 * causes dummy event on evfd which makes select come out of blocking state.
 *
 * For MAC OS X : There is exactly one CFRunLoop for each thread. Prepare this run loop to listen to add and remove
 * notifications from kernel and callbacks gets called as appropriate. Signal and wake up runloop when hot plug listener
 * is un-registered to get out of waiting state.
 */
void *usb_hot_plug_monitor(void *arg) {
	struct port_info* params = (struct port_info*) arg;
	jobject usbHotPlugEventListenerObj = (*params).usbHotPlugEventListener;
	jclass usbHotPlugEventListenerClass = NULL;
	JavaVM *jvm = (*params).jvm;
	JNIEnv* env = NULL;
	void* env1 = NULL;

#if defined (__linux__)
	jmethodID onHotPlugEventMethodID = NULL;
	int filterVID = (*params).filterVID;
	int filterPID = (*params).filterPID;
	int ret = 0;
	int evfd = 0;
	int udev_monitor_fd;
	fd_set fds;
	int maxfd = 0;
	struct udev *udev_ctx = NULL;
	struct udev_device *udev_device;
	struct udev_monitor *udev_monitor;
	const char* udev_action_str;
	char udev_action[128];
	const char* usb_vid_str;
	const char* usb_pid_str;
	int usb_vid = 0;
	int usb_pid = 0;

	/* these 3 must match their value in SerialComManager class. */
	int USB_DEV_ANY = 0x00;
	int USB_DEV_ADDED = 0x01;
	int USB_DEV_REMOVED = 0x02;
#endif
#if defined (__APPLE__)
	kern_return_t kr;
	io_iterator_t added_iterator = 0;
	io_iterator_t removed_iterator = 0;
	CFRunLoopSourceRef usb_run_loop_source;
	CFRunLoopSourceContext exit_source_context;
	/* Use separate dictionary, reusing same may not work.
	 * OS is responsible for releasing dictionary. */
	CFDictionaryRef added_matching_dict = NULL;
	CFDictionaryRef removed_matching_dict = NULL;
#endif
#if defined (__SunOS)
	/* TODO solaris */
#endif

	pthread_mutex_lock(((struct com_thread_params*) arg)->mutex);

	if((*jvm)->AttachCurrentThread(jvm, &env1, NULL) != JNI_OK) {
		((struct port_info*) arg)->custom_err_code = E_ATTACHCURRENTTHREAD;
		((struct port_info*) arg)->init_done = 2;
		pthread_mutex_unlock(((struct com_thread_params*) arg)->mutex);
		pthread_exit((void *)0);
	}
	env =  (JNIEnv*) env1;
#if defined (__APPLE__)
	((struct port_info*) arg)->env = (JNIEnv*) env1;
#endif

	usbHotPlugEventListenerClass = (*env)->GetObjectClass(env, usbHotPlugEventListenerObj);
	if((usbHotPlugEventListenerClass == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		(*jvm)->DetachCurrentThread(jvm);
		((struct port_info*) arg)->custom_err_code = E_GETOBJECTCLASS;
		((struct port_info*) arg)->init_done = 2;
		pthread_mutex_unlock(((struct com_thread_params*) arg)->mutex);
		pthread_exit((void *)0);
	}

#if defined (__linux__)
	onHotPlugEventMethodID = (*env)->GetMethodID(env, usbHotPlugEventListenerClass, "onHotPlugEvent", "(I)V");
	if((onHotPlugEventMethodID == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
#elif defined (__APPLE__)
	((struct port_info*) arg)->onHotPlugEventMethodID = (*env)->GetMethodID(env, usbHotPlugEventListenerClass, "onHotPlugEvent", "(I)V");
	if((((struct port_info*) arg)->onHotPlugEventMethodID == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
#endif
		(*jvm)->DetachCurrentThread(jvm);
		((struct port_info*) arg)->custom_err_code = E_GETMETHODID;
		((struct port_info*) arg)->init_done = 2;
		pthread_mutex_unlock(((struct com_thread_params*) arg)->mutex);
		pthread_exit((void *)0);
	}

#if defined (__linux__)
	errno = 0;
	evfd  = eventfd(0, 0);
	if(evfd < 0) {
		(*jvm)->DetachCurrentThread(jvm);
		((struct port_info*) arg)->standard_err_code = errno;
		((struct port_info*) arg)->init_done = 2;
		pthread_mutex_unlock(((struct com_thread_params*) arg)->mutex);
		pthread_exit((void *)0);
	}
	((struct port_info*) arg)->evfd = evfd;

	/* Create udev library context. Reads the udev configuration file, fills in the default values and return pointer to it. */
	udev_ctx = udev_new();
	if(!udev_ctx) {
		(*jvm)->DetachCurrentThread(jvm);
		((struct port_info*) arg)->custom_err_code = E_UDEVNEW;
		((struct port_info*) arg)->init_done = 2;
		pthread_mutex_unlock(((struct com_thread_params*) arg)->mutex);
		pthread_exit((void *)0);
	}

	/* Create new udev monitor and connect to a specified event source. Applications should usually not connect directly to the "kernel" events,
	 * because the devices might not be usable at that time, before udev has configured them, and created device nodes. Accessing devices at the
	 * same time as udev, might result in unpredictable behavior. The "udev" events are sent out after udev has finished its event processing,
	 * all rules have been processed, and needed device nodes are created. This returns a pointer to the allocated udev monitor. */
	udev_monitor = udev_monitor_new_from_netlink(udev_ctx, "udev");
	if(udev_monitor == NULL) {
		(*jvm)->DetachCurrentThread(jvm);
		((struct port_info*) arg)->custom_err_code = E_UDEVNETLINK;
		((struct port_info*) arg)->init_done = 2;
		udev_unref(udev_ctx);
		pthread_mutex_unlock(((struct com_thread_params*) arg)->mutex);
		pthread_exit((void *)0);
	}

	/* This filter is efficiently executed inside the kernel, and libudev subscribers will usually not be woken up for devices which do not match.
	 * The filter must be installed before the monitor is switched to listening mode. */
	ret = udev_monitor_filter_add_match_subsystem_devtype(udev_monitor, "usb", "usb_device");
	if(ret < 0) {
		(*jvm)->DetachCurrentThread(jvm);
		udev_monitor_unref(udev_monitor);
		udev_unref(udev_ctx);
		((struct port_info*) arg)->standard_err_code = ret;
		((struct port_info*) arg)->init_done = 2;
		pthread_mutex_unlock(((struct com_thread_params*) arg)->mutex);
		pthread_exit((void *)0);
	}

	/* Binds the udev_monitor socket to the event source. */
	ret = udev_monitor_enable_receiving(udev_monitor);
	if(ret < 0) {
		(*jvm)->DetachCurrentThread(jvm);
		udev_monitor_unref(udev_monitor);
		udev_unref(udev_ctx);
		((struct port_info*) arg)->standard_err_code = ret;
		((struct port_info*) arg)->init_done = 2;
		pthread_mutex_unlock(((struct com_thread_params*) arg)->mutex);
		pthread_exit((void *)0);
	}

	/* Retrieve the socket file descriptor associated with the monitor. This fd will get passed to select(). */
	udev_monitor_fd = udev_monitor_get_fd(udev_monitor);
	FD_ZERO(&fds);
	FD_SET(evfd, &fds);
	FD_SET(udev_monitor_fd, &fds);
	if(evfd > udev_monitor_fd) {
		maxfd = evfd;
	}else {
		maxfd = udev_monitor_fd;
	}

	/* tell main thread thread initialization successfully completed */
	((struct port_info*) arg)->init_done = 0;

	while(1) {
		ret = select(maxfd + 1, &fds, NULL, NULL, NULL);

		/* Check if thread should exit. If yes, do clean up and exit. */
		if((ret > 0) && FD_ISSET(evfd, &fds)) {
			if(((struct port_info*) arg)->thread_exit == 1) {
				(*jvm)->DetachCurrentThread(jvm);
				close(((struct port_info*) arg)->evfd);
				udev_monitor_unref(udev_monitor);
				udev_unref(udev_ctx);
				pthread_exit((void *)0);
			}
		}

		/* Check no error occurred, and udev file descriptor indicates event. */
		if((ret > 0) && FD_ISSET(udev_monitor_fd, &fds)) {
			udev_device = udev_monitor_receive_device(udev_monitor);
			if(udev_device) {
				/* This is only valid if the device was received through a monitor. */
				udev_action_str = udev_device_get_action(udev_device);
				if(udev_action == NULL) {
					udev_device_unref(udev_device);
					continue;
				}
				memset(udev_action, '\0', sizeof(udev_action));
				strcpy(udev_action, udev_action_str);

				usb_vid_str = udev_device_get_property_value(udev_device, "ID_VENDOR_ID");
				if(usb_vid_str != NULL) {
					usb_vid = strtoul(usb_vid_str, NULL, 16);
				}

				usb_pid_str = udev_device_get_property_value(udev_device, "ID_MODEL_ID");
				if(usb_pid_str != NULL) {
					usb_pid = strtoul(usb_pid_str, NULL, 16);
				}

				udev_device_unref(udev_device);

				if(filterVID != USB_DEV_ANY) {
					if(filterVID != usb_vid) {
						continue;
					}
				}
				if(filterPID != USB_DEV_ANY) {
					if(filterPID != usb_pid) {
						continue;
					}
				}

				/* reaching here means vip/pid matching criteria is fulfilled, so notify application about it. */
				if(strcmp(udev_action, "add") == 0) {
					(*env)->CallVoidMethod(env, usbHotPlugEventListenerObj, onHotPlugEventMethodID, USB_DEV_ADDED);
					if((*env)->ExceptionOccurred(env)) {
						(*env)->ExceptionClear(env);
					}
				}else if(strcmp(udev_action, "remove") == 0) {
					(*env)->CallVoidMethod(env, usbHotPlugEventListenerObj, onHotPlugEventMethodID, USB_DEV_REMOVED);
					if((*env)->ExceptionOccurred(env)) {
						(*env)->ExceptionClear(env);
					}
				}else {
					/* ignore */
				}
			}
		}
	} /* go back to loop again */

	return ((void *)0);
#endif

#if defined (__APPLE__)
	/* Install custom input source in run loop of worker thread. Run loop execute sources in order of priority so give it more priority than
	 * usb events source. */
	((struct port_info*) arg)->run_loop = CFRunLoopGetCurrent();
	exit_source_context.perform = mac_indicate_thread_exit;
	((struct port_info*) arg)->exit_run_loop_source = CFRunLoopSourceCreate(NULL, 1, &exit_source_context);
	CFRunLoopAddSource(((struct port_info*) arg)->run_loop, ((struct port_info*) arg)->exit_run_loop_source, kCFRunLoopCommonModes);

	/* Create a matching dictionary that will find any USB device.
	 * Interested in instances of class IOUSBDevice and its subclasses.*/
	added_matching_dict = IOServiceMatching("IOUSBDevice");
	if(added_matching_dict == NULL) {
		(*jvm)->DetachCurrentThread(jvm);
		CFRunLoopRemoveSource(((struct port_info*) arg)->run_loop, ((struct port_info*) arg)->exit_run_loop_source, kCFRunLoopCommonModes);
		CFRelease(((struct port_info*) arg)->exit_run_loop_source);
		CFRelease(((struct port_info*) arg)->run_loop);
		((struct port_info*) arg)->init_done = E_IOSRVMATUSBDEV;
		pthread_exit((void *)0);
	}
	removed_matching_dict = IOServiceMatching("IOUSBDevice");
	if(removed_matching_dict == NULL) {
		(*jvm)->DetachCurrentThread(jvm);
		CFRunLoopRemoveSource(((struct port_info*) arg)->run_loop, ((struct port_info*) arg)->exit_run_loop_source, kCFRunLoopCommonModes);
		CFRelease(((struct port_info*) arg)->exit_run_loop_source);
		CFRelease(((struct port_info*) arg)->run_loop);
		((struct port_info*) arg)->custom_err_code = E_IOSRVMATUSBDEVSTR;
		((struct port_info*) arg)->init_done = 2;
		pthread_mutex_unlock(((struct com_thread_params*) arg)->mutex);
		pthread_exit((void *)0);
	}

	/* Create a notification object for receiving IOKit notifications of new devices or state changes. */
	((struct port_info*) arg)->notification_port = IONotificationPortCreate(kIOMasterPortDefault);

	/* CFRunLoopSource to be used to listen for notifications. */
	usb_run_loop_source = IONotificationPortGetRunLoopSource(((struct port_info*) arg)->notification_port);

	/* Adds a CFRunLoopSource object to a run loop mode. */
	CFRunLoopAddSource(((struct port_info*) arg)->run_loop, usb_run_loop_source, kCFRunLoopCommonModes);

	/* Look up registered IOService objects that match a matching dictionary, and install a notification request of new IOServices that match.
	 * It associates the matching dictionary with the notification port (and run loop source), allocates and returns an iterator object.
	 * The kIOFirstMatchNotification is delivered when an IOService has had all matching drivers in the kernel probed and started, but only
	 * once per IOService instance. Some IOService's may be re-registered when their state is changed.*/
	kr = IOServiceAddMatchingNotification(((struct port_info*) arg)->notification_port, kIOFirstMatchNotification, added_matching_dict,
			mac_usb_device_added, ((struct port_info*) arg)->data, &added_iterator);
	if(kr != KERN_SUCCESS) {
		/* handle error */
	}
	kr = IOServiceAddMatchingNotification(((struct port_info*) arg)->notification_port, kIOTerminatedNotification, removed_matching_dict,
			mac_usb_device_removed, ((struct port_info*) arg)->data, &removed_iterator);
	if(kr != KERN_SUCCESS) {
		/* handle error */
	}

	/* Iterate once explicitly to empty iterator. */
	mac_usb_device_added(((struct port_info*) arg)->data, added_iterator);
	mac_usb_device_removed(((struct port_info*) arg)->data, removed_iterator);

	/* notify main thread, initialization successfully completed */
	((struct port_info*) arg)->init_done = 0;

	/* Start the run loop to begin receiving notifications. */
	CFRunLoopRun();

	/* Reaching here means run loop is stopped; thread is asked to exit. Clean up. */
	(*jvm)->DetachCurrentThread(jvm);
	CFRunLoopRemoveSource(((struct port_info*) arg)->run_loop, ((struct port_info*) arg)->exit_run_loop_source, kCFRunLoopCommonModes);
	CFRelease(((struct port_info*) arg)->exit_run_loop_source);
	IONotificationPortDestroy(((struct port_info*) arg)->notification_port); /* will also release usb_run_loop_source */
	CFRunLoopRemoveSource(((struct port_info*) arg)->run_loop, usb_run_loop_source, kCFRunLoopCommonModes);
	IOObjectRelease(added_iterator);
	IOObjectRelease(removed_iterator);
	return ((void *)0);
#endif

#if defined (__SunOS)
#endif
}

