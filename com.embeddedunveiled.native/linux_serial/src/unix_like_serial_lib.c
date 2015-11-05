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
	pthread_cond_signal(&(((struct com_thread_params*) arg)->data_cond_var));
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
	pthread_cond_signal(&(((struct com_thread_params*) arg)->event_cond_var));
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
