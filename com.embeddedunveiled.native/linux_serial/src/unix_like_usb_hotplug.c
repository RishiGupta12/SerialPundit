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

#include <unistd.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <errno.h>
#include <sys/param.h>

#if defined (__linux__)
#include <sys/eventfd.h>
#include <sys/select.h>
#endif

#if defined (__APPLE__)
#include <sysexits.h>
#include <sys/event.h>
#include <CoreFoundation/CoreFoundation.h>
#include <IOKit/IOKitLib.h>
#include <IOKit/IOBSD.h>
#include <IOKit/IOMessage.h>
#include <IOKit/usb/IOUSBLib.h>
#endif

#if defined (__SunOS)
#endif

#include <jni.h>
#include "unix_like_serial_lib.h"

#if defined (__linux__)
/*
 * This worker thread monitors usb events and notifies application as appropriate. Platform specific facility
 * has been used to terminate thread with any thread dead-lock and ensuring resource clean up.
 *
 * For Linux : libudev is used to monitor events, extract info about events etc. To destroy this thread main thread
 * causes dummy event on evfd which makes select come out of blocking state.
 */
void *usb_device_hotplug_monitor(void *arg) {

	struct usb_dev_monitor_info* ptr = (struct usb_dev_monitor_info*) arg;
	jmethodID onUSBHotPlugEventMethodID = NULL;
	jclass usbHotPlugEventListenerClass = NULL;
	jobject usbHotPlugEventListenerObj = (*ptr).usbHotPlugEventListener;
	JavaVM *jvm = (*ptr).jvm;
	JNIEnv* env = NULL;
	void* env1 = NULL;

	int ret = 0;
	int evfd = 0;
	fd_set fds;
	int udev_monitor_fd;
	struct udev *udev_ctx = NULL;
	struct udev_device *udev_device;
	struct udev_monitor *udev_monitor;
	const char* udev_action_str;
	char udev_action[128];
	const char* usb_vid_str;
	const char* usb_pid_str;
	const char* serial_str;

	/* these 3 must match their value in SerialComManager class. */
	const int USB_DEV_ANY     = 0x00;
	const int USB_DEV_ADDED   = 0x01;
	const int USB_DEV_REMOVED = 0x02;

	pthread_mutex_lock(((struct usb_dev_monitor_info*) arg)->mutex);

	if((*jvm)->AttachCurrentThread(jvm, &env1, NULL) != JNI_OK) {
		((struct usb_dev_monitor_info*) arg)->custom_err_code = E_ATTACHCURRENTTHREAD;
		((struct usb_dev_monitor_info*) arg)->init_done = 2;
		pthread_mutex_unlock(((struct usb_dev_monitor_info*) arg)->mutex);
		pthread_exit((void *)0);
	}
	env =  (JNIEnv*) env1;

	usbHotPlugEventListenerClass = (*env)->GetObjectClass(env, usbHotPlugEventListenerObj);
	if((usbHotPlugEventListenerClass == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		(*jvm)->DetachCurrentThread(jvm);
		((struct usb_dev_monitor_info*) arg)->custom_err_code = E_GETOBJECTCLASS;
		((struct usb_dev_monitor_info*) arg)->init_done = 2;
		pthread_mutex_unlock(((struct usb_dev_monitor_info*) arg)->mutex);
		pthread_exit((void *)0);
	}

	onUSBHotPlugEventMethodID = (*env)->GetMethodID(env, usbHotPlugEventListenerClass, "onUSBHotPlugEvent", "(I)V");
	if((onUSBHotPlugEventMethodID == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		(*jvm)->DetachCurrentThread(jvm);
		((struct usb_dev_monitor_info*) arg)->custom_err_code = E_GETMETHODID;
		((struct usb_dev_monitor_info*) arg)->init_done = 2;
		pthread_mutex_unlock(((struct usb_dev_monitor_info*) arg)->mutex);
		pthread_exit((void *)0);
	}

	errno = 0;
	evfd = eventfd(0, 0);
	if(evfd < 0) {
		(*jvm)->DetachCurrentThread(jvm);
		((struct usb_dev_monitor_info*) arg)->standard_err_code = errno;
		((struct usb_dev_monitor_info*) arg)->init_done = 2;
		pthread_mutex_unlock(((struct usb_dev_monitor_info*) arg)->mutex);
		pthread_exit((void *)0);
	}
	((struct usb_dev_monitor_info*) arg)->evfd = evfd;

	/* Create udev library context. Reads the udev configuration file, fills in the default values and return pointer to it. */
	udev_ctx = udev_new();
	if(!udev_ctx) {
		(*jvm)->DetachCurrentThread(jvm);
		((struct usb_dev_monitor_info*) arg)->custom_err_code = E_UDEVNEW;
		((struct usb_dev_monitor_info*) arg)->init_done = 2;
		pthread_mutex_unlock(((struct usb_dev_monitor_info*) arg)->mutex);
		pthread_exit((void *)0);
	}

	/* Create new udev monitor and connect to a specified event source. Applications should usually not connect directly to the "kernel" events,
	 * because the devices might not be usable at that time, before udev has configured them, and created device nodes. Accessing devices at the
	 * same time as udev, might result in unpredictable behavior. The "udev" events are sent out after udev has finished its event processing,
	 * all rules have been processed, and needed device nodes are created. This returns a pointer to the allocated udev monitor. */
	udev_monitor = udev_monitor_new_from_netlink(udev_ctx, "udev");
	if(udev_monitor == NULL) {
		(*jvm)->DetachCurrentThread(jvm);
		((struct usb_dev_monitor_info*) arg)->custom_err_code = E_UDEVNETLINK;
		((struct usb_dev_monitor_info*) arg)->init_done = 2;
		udev_unref(udev_ctx);
		pthread_mutex_unlock(((struct usb_dev_monitor_info*) arg)->mutex);
		pthread_exit((void *)0);
	}

	/* This filter is efficiently executed inside the kernel, and libudev subscribers will usually not be woken up for devices which do not match.
	 * The filter must be installed before the monitor is switched to listening mode. */
	ret = udev_monitor_filter_add_match_subsystem_devtype(udev_monitor, "usb", "usb_device");
	if(ret < 0) {
		(*jvm)->DetachCurrentThread(jvm);
		udev_monitor_unref(udev_monitor);
		udev_unref(udev_ctx);
		((struct usb_dev_monitor_info*) arg)->standard_err_code = ret;
		((struct usb_dev_monitor_info*) arg)->init_done = 2;
		pthread_mutex_unlock(((struct usb_dev_monitor_info*) arg)->mutex);
		pthread_exit((void *)0);
	}

	/* Binds the udev_monitor socket to the event source. */
	ret = udev_monitor_enable_receiving(udev_monitor);
	if(ret < 0) {
		(*jvm)->DetachCurrentThread(jvm);
		udev_monitor_unref(udev_monitor);
		udev_unref(udev_ctx);
		((struct usb_dev_monitor_info*) arg)->standard_err_code = ret;
		((struct usb_dev_monitor_info*) arg)->init_done = 2;
		pthread_mutex_unlock(((struct usb_dev_monitor_info*) arg)->mutex);
		pthread_exit((void *)0);
	}

	/* Retrieve the socket file descriptor associated with the monitor. This fd will get passed to select(). */
	udev_monitor_fd = udev_monitor_get_fd(udev_monitor);

	/* tell main thread thread initialization successfully completed */
	pthread_cond_signal(&(((struct usb_dev_monitor_info*) arg)->cond_var));
	((struct usb_dev_monitor_info*) arg)->init_done = 0;
	pthread_mutex_unlock(((struct usb_dev_monitor_info*) arg)->mutex);

	while(1) {
		FD_ZERO(&fds);
		FD_SET(evfd, &fds);
		FD_SET(udev_monitor_fd, &fds);

		errno = 0;
		ret = pselect((MAX(evfd, udev_monitor_fd) + 1), &fds, NULL, NULL, NULL, NULL);
		if(ret < 0) {
			LOGEN("pselect() ", "usb_device_hotplug_monitor() failed with error code : %d\n", errno);
		}

		/* Check if thread should exit. If yes, do clean up and exit. */
		if((ret > 0) && FD_ISSET(evfd, &fds)) {
			if(((struct usb_dev_monitor_info*) arg)->thread_exit == 1) {
				(*jvm)->DetachCurrentThread(jvm);
				close(((struct usb_dev_monitor_info*) arg)->evfd);
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

				if(strcmp(udev_action, "add") == 0) {

					/* extract vid and match, proceed to match pid only if vid matches */
					if (ptr->usb_vid_to_match != USB_DEV_ANY) {
						usb_vid_str = udev_device_get_property_value(udev_device, "ID_VENDOR_ID");
						if(usb_vid_str != NULL) {
							if (ptr->usb_vid_to_match != ((int) strtoul(usb_vid_str, NULL, 16))) {
								/* go back to wait for next usb hot plug event */
								continue;
							}
						}else {
							/* should not happen */
							continue;
						}
					}

					/* extract pid and match, proceed to match serial only if pid matches */
					if (ptr->usb_pid_to_match != USB_DEV_ANY) {
						usb_pid_str = udev_device_get_property_value(udev_device, "ID_MODEL_ID");
						if(usb_pid_str != NULL) {
							if (ptr->usb_pid_to_match != ((int) strtoul(usb_pid_str, NULL, 16))) {
								/* go back to wait for next usb hot plug event */
								continue;
							}
						}else {
							/* should not happen */
							continue;
						}
					}

					/* extract serial and match if required by application */
					if (ptr->serial_number_to_match[0] != '\0') {
						serial_str = udev_device_get_property_value(udev_device, "ID_SERIAL_SHORT");
						if(serial_str != NULL) {
							if (strcmp(serial_str, ptr->serial_number_to_match) != 0) {
								/* go back to wait for next usb hot plug event */
								continue;
							}
						}else {
							/* should not happen */
							continue;
						}
					}

					/* reaching here means device matches all criteria, invoke application's usb hot plug listener */
					(*env)->CallVoidMethod(env, usbHotPlugEventListenerObj, onUSBHotPlugEventMethodID, USB_DEV_ADDED);
					if((*env)->ExceptionOccurred(env)) {
						(*env)->ExceptionClear(env);
					}
				}else if(strcmp(udev_action, "remove") == 0) {

					/* extract vid and match, proceed to match pid only if vid matches */
					if (ptr->usb_vid_to_match != USB_DEV_ANY) {
						usb_vid_str = udev_device_get_property_value(udev_device, "ID_VENDOR_ID");
						if(usb_vid_str != NULL) {
							if (ptr->usb_vid_to_match != ((int) strtoul(usb_vid_str, NULL, 16))) {
								/* go back to wait for next usb hot plug event */
								continue;
							}
						}else {
							/* should not happen */
							continue;
						}
					}

					/* extract pid and match, proceed to match serial only if pid matches */
					if (ptr->usb_pid_to_match != USB_DEV_ANY) {
						usb_pid_str = udev_device_get_property_value(udev_device, "ID_MODEL_ID");
						if(usb_pid_str != NULL) {
							if (ptr->usb_pid_to_match != ((int) strtoul(usb_pid_str, NULL, 16))) {
								/* go back to wait for next usb hot plug event */
								continue;
							}
						}else {
							/* should not happen */
							continue;
						}
					}

					/* extract serial and match if required by application */
					if (ptr->serial_number_to_match[0] != '\0') {
						serial_str = udev_device_get_property_value(udev_device, "ID_SERIAL_SHORT");
						if(serial_str != NULL) {
							if (strcmp(serial_str, ptr->serial_number_to_match) != 0) {
								/* go back to wait for next usb hot plug event */
								continue;
							}
						}else {
							/* should not happen */
							continue;
						}
					}

					/* reaching here means device matches all criteria, invoke application listener */
					(*env)->CallVoidMethod(env, usbHotPlugEventListenerObj, onUSBHotPlugEventMethodID, USB_DEV_REMOVED);
					if((*env)->ExceptionOccurred(env)) {
						(*env)->ExceptionClear(env);
					}
				}else {
					/* do nothing */
				}
			}
		}
	} /* go back to wait for next usb hot plug event again */

	return ((void *)0);
}
#endif

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
	JNIEnv* env = ((struct usb_dev_monitor_info*) refCon)->env;
	char charbuffer[64];

	if(((struct usb_dev_monitor_info*) refCon)->empty_iterator_removed != 0) {
		while ((usb_dev_obj = IOIteratorNext(iterator)) != 0) {

			/* extract vid and match, proceed to match pid only if vid matches */
			if(((struct usb_dev_monitor_info*) refCon)->usb_vid_to_match != USB_DEV_ANY) {
				num_ref = (CFNumberRef) IORegistryEntrySearchCFProperty(usb_dev_obj, kIOServicePlane, CFSTR("idVendor"),
						NULL, kIORegistryIterateRecursively | kIORegistryIterateParents);
				if(num_ref) {
					CFNumberGetValue(num_ref, kCFNumberSInt32Type, &usb_vid);
					CFRelease(num_ref);
					if(((struct usb_dev_monitor_info*) refCon)->usb_vid_to_match != usb_vid) {
						IOObjectRelease(usb_dev_obj);
						continue;
					}
				}else {
					continue;
				}
			}

			/* extract pid and match, proceed to match serial only if pid matches */
			if(((struct usb_dev_monitor_info*) refCon)->usb_pid_to_match != USB_DEV_ANY) {
				num_ref = (CFNumberRef) IORegistryEntrySearchCFProperty(usb_dev_obj, kIOServicePlane, CFSTR("idProduct"),
						NULL, kIORegistryIterateRecursively | kIORegistryIterateParents);
				if(num_ref) {
					CFNumberGetValue(num_ref, kCFNumberSInt32Type, &usb_pid);
					CFRelease(num_ref);
					if(((struct usb_dev_monitor_info*) refCon)->usb_pid_to_match != usb_pid) {
						IOObjectRelease(usb_dev_obj);
						continue;
					}
				}else {
					continue;
				}
			}

			/* extract serial and match if required by application */
			if (((struct usb_dev_monitor_info*) refCon)->serial_number_to_match[0] != '\0') {
				str_ref = (CFStringRef) IORegistryEntrySearchCFProperty(usb_dev_obj, kIOServicePlane,
						CFSTR("USB Serial Number"), NULL, kIORegistryIterateRecursively | kIORegistryIterateParents);
				if(str_ref) {
					memset(charbuffer, '\0', sizeof(charbuffer));
					CFStringGetCString(str_ref, charbuffer, sizeof(charbuffer), kCFStringEncodingUTF8);
					CFRelease(str_ref);
					if(strcasecmp(charbuffer, ((struct usb_dev_monitor_info*) refCon)->serial_number_to_match) != 0) {
						IOObjectRelease(usb_dev_obj);
						continue;
					}
				}else {
					IOObjectRelease(usb_dev_obj);
					continue;
				}
			}

			/* Reaching here means both USB VID and PID criteria is met. */
			(*env)->CallVoidMethod(env, ((struct usb_dev_monitor_info*) refCon)->usbHotPlugEventListener,
					((struct usb_dev_monitor_info*) refCon)->onUSBHotPlugEventMethodID, USB_DEV_REMOVED);
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
		((struct usb_dev_monitor_info*) refCon)->empty_iterator_removed = 1;
	}
}

/*
 * Callback associated with run loop which will be invoked whenever a matching USB device is
 * added into the system.
 */
void mac_usb_device_added(void *refCon, io_iterator_t iterator) {
	int usb_vid;
	int usb_pid;
	CFNumberRef num_ref;
	io_service_t usb_dev_obj = 0;
	int USB_DEV_ANY = 0x00;
	int USB_DEV_ADDED = 0x01;
	JNIEnv* env = ((struct usb_dev_monitor_info*) refCon)->env;
	char charbuffer[64];

	if(((struct usb_dev_monitor_info*) refCon)->empty_iterator_added != 0) {
		while ((usb_dev_obj = IOIteratorNext(iterator)) != 0) {

			/* extract vid and match, proceed to match pid only if vid matches */
			if(((struct usb_dev_monitor_info*) refCon)->usb_vid_to_match != USB_DEV_ANY) {
				num_ref = (CFNumberRef) IORegistryEntrySearchCFProperty(usb_dev_obj, kIOServicePlane, CFSTR("idVendor"),
						NULL, kIORegistryIterateRecursively | kIORegistryIterateParents);
				if(num_ref) {
					CFNumberGetValue(num_ref, kCFNumberSInt32Type, &usb_vid);
					CFRelease(num_ref);
					if(((struct usb_dev_monitor_info*) refCon)->usb_vid_to_match != usb_vid) {
						IOObjectRelease(usb_dev_obj);
						continue;
					}
				}
			}

			/* extract pid and match, proceed to match serial only if pid matches */
			if(((struct usb_dev_monitor_info*) refCon)->usb_pid_to_match != USB_DEV_ANY) {
				num_ref = (CFNumberRef) IORegistryEntrySearchCFProperty(usb_dev_obj, kIOServicePlane, CFSTR("idProduct"),
						NULL, kIORegistryIterateRecursively | kIORegistryIterateParents);
				if(num_ref) {
					CFNumberGetValue(num_ref, kCFNumberSInt32Type, &usb_pid);
					CFRelease(num_ref);
					if(((struct usb_dev_monitor_info*) refCon)->usb_pid_to_match != usb_pid) {
						IOObjectRelease(usb_dev_obj);
						continue;
					}
				}
			}

			/* extract serial and match if required by application */
			if (((struct usb_dev_monitor_info*) refCon)->serial_number_to_match[0] != '\0') {
				str_ref = (CFStringRef) IORegistryEntrySearchCFProperty(usb_dev_obj, kIOServicePlane,
						CFSTR("USB Serial Number"), NULL, kIORegistryIterateRecursively | kIORegistryIterateParents);
				if(str_ref) {
					memset(charbuffer, '\0', sizeof(charbuffer));
					CFStringGetCString(str_ref, charbuffer, sizeof(charbuffer), kCFStringEncodingUTF8);
					CFRelease(str_ref);
					if(strcasecmp(charbuffer, ((struct usb_dev_monitor_info*) refCon)->serial_number_to_match) != 0) {
						IOObjectRelease(usb_dev_obj);
						continue;
					}
				}else {
					IOObjectRelease(usb_dev_obj);
					continue;
				}
			}

			/* Reaching here means both USB VID and PID criteria is met. */
			(*env)->CallVoidMethod(env, ((struct usb_dev_monitor_info*) refCon)->usbHotPlugEventListener,
					((struct usb_dev_monitor_info*) refCon)->onUSBHotPlugEventMethodID, USB_DEV_ADDED);
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
		((struct usb_dev_monitor_info*) refCon)->empty_iterator_added = 1;
	}
}


/*
 * This worker thread monitors usb events and notifies application as appropriate. Platform specific facility
 * has been used to terminate thread with any thread dead-lock and ensuring resource clean up.
 *
 * For MAC OS X : There is exactly one CFRunLoop for each thread. Prepare this run loop to listen to add and remove
 * notifications from kernel and callbacks gets called as appropriate. Signal and wake up runloop when hot plug listener
 * is un-registered to get out of waiting state.
 */
void *usb_hot_plug_monitor(void *arg) {

	struct usb_dev_monitor_info* ptr = (struct usb_dev_monitor_info*) arg;
	jmethodID onUSBHotPlugEventMethodID = NULL;
	jclass usbHotPlugEventListenerClass = NULL;
	jobject usbHotPlugEventListenerObj = (*ptr).usbHotPlugEventListener;
	JavaVM *jvm = (*ptr).jvm;
	JNIEnv* env = NULL;
	void* env1 = NULL;

	kern_return_t kr;
	io_iterator_t added_iterator = 0;
	io_iterator_t removed_iterator = 0;
	CFRunLoopSourceRef usb_run_loop_source;
	CFRunLoopSourceContext exit_source_context;

	/* Use separate dictionary, reusing same may not work.
	 * OS is responsible for releasing dictionary. */
	CFDictionaryRef added_matching_dict = NULL;
	CFDictionaryRef removed_matching_dict = NULL;

	pthread_mutex_lock(((struct usb_dev_monitor_info*) arg)->mutex);

	if((*jvm)->AttachCurrentThread(jvm, &env1, NULL) != JNI_OK) {
		((struct usb_dev_monitor_info*) arg)->custom_err_code = E_ATTACHCURRENTTHREAD;
		((struct usb_dev_monitor_info*) arg)->init_done = 2;
		pthread_mutex_unlock(((struct usb_dev_monitor_info*) arg)->mutex);
		pthread_exit((void *)0);
	}
	env =  (JNIEnv*) env1;
	((struct usb_dev_monitor_info*) arg)->env = (JNIEnv*) env1;

	usbHotPlugEventListenerClass = (*env)->GetObjectClass(env, usbHotPlugEventListenerObj);
	if((usbHotPlugEventListenerClass == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		(*jvm)->DetachCurrentThread(jvm);
		((struct usb_dev_monitor_info*) arg)->custom_err_code = E_GETOBJECTCLASS;
		((struct usb_dev_monitor_info*) arg)->init_done = 2;
		pthread_mutex_unlock(((struct usb_dev_monitor_info*) arg)->mutex);
		pthread_exit((void *)0);
	}

	((struct usb_dev_monitor_info*) arg)->onUSBHotPlugEventMethodID = (*env)->GetMethodID(env, usbHotPlugEventListenerClass, "onUSBHotPlugEvent", "(I)V");
	if((((struct usb_dev_monitor_info*) arg)->onUSBHotPlugEventMethodID == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		(*jvm)->DetachCurrentThread(jvm);
		((struct usb_dev_monitor_info*) arg)->custom_err_code = E_GETMETHODID;
		((struct usb_dev_monitor_info*) arg)->init_done = 2;
		pthread_mutex_unlock(((struct usb_dev_monitor_info*) arg)->mutex);
		pthread_exit((void *)0);
	}

	/* Install custom input source in run loop of worker thread. Run loop execute sources in order of priority so give it more priority than
	 * usb events source. */
	((struct usb_dev_monitor_info*) arg)->run_loop = CFRunLoopGetCurrent();
	exit_source_context.perform = mac_indicate_thread_exit;
	((struct usb_dev_monitor_info*) arg)->exit_run_loop_source = CFRunLoopSourceCreate(NULL, 1, &exit_source_context);
	CFRunLoopAddSource(((struct usb_dev_monitor_info*) arg)->run_loop, ((struct usb_dev_monitor_info*) arg)->exit_run_loop_source, kCFRunLoopCommonModes);

	/* Create a matching dictionary that will find any USB device.
	 * Interested in instances of class IOUSBDevice and its subclasses.*/
	added_matching_dict = IOServiceMatching("IOUSBDevice");
	if(added_matching_dict == NULL) {
		(*jvm)->DetachCurrentThread(jvm);
		CFRunLoopRemoveSource(((struct usb_dev_monitor_info*) arg)->run_loop, ((struct usb_dev_monitor_info*) arg)->exit_run_loop_source, kCFRunLoopCommonModes);
		CFRelease(((struct usb_dev_monitor_info*) arg)->exit_run_loop_source);
		CFRelease(((struct usb_dev_monitor_info*) arg)->run_loop);
		((struct usb_dev_monitor_info*) arg)->init_done = E_IOSRVMATUSBDEV;
		pthread_exit((void *)0);
	}

	removed_matching_dict = IOServiceMatching("IOUSBDevice");
	if(removed_matching_dict == NULL) {
		(*jvm)->DetachCurrentThread(jvm);
		CFRunLoopRemoveSource(((struct usb_dev_monitor_info*) arg)->run_loop, ((struct usb_dev_monitor_info*) arg)->exit_run_loop_source, kCFRunLoopCommonModes);
		CFRelease(((struct usb_dev_monitor_info*) arg)->exit_run_loop_source);
		CFRelease(((struct usb_dev_monitor_info*) arg)->run_loop);
		((struct usb_dev_monitor_info*) arg)->custom_err_code = E_IOSRVMATUSBDEVSTR;
		((struct usb_dev_monitor_info*) arg)->init_done = 2;
		pthread_mutex_unlock(((struct usb_dev_monitor_info*) arg)->mutex);
		pthread_exit((void *)0);
	}

	/* Create a notification object for receiving IOKit notifications of new devices or state changes. */
	((struct usb_dev_monitor_info*) arg)->notification_port = IONotificationPortCreate(kIOMasterPortDefault);

	/* CFRunLoopSource to be used to listen for notifications. */
	usb_run_loop_source = IONotificationPortGetRunLoopSource(((struct usb_dev_monitor_info*) arg)->notification_port);

	/* Adds a CFRunLoopSource object to a run loop mode. */
	CFRunLoopAddSource(((struct usb_dev_monitor_info*) arg)->run_loop, usb_run_loop_source, kCFRunLoopCommonModes);

	/* Look up registered IOService objects that match a matching dictionary, and install a notification request of new IOServices that match.
	 * It associates the matching dictionary with the notification port (and run loop source), allocates and returns an iterator object.
	 * The kIOFirstMatchNotification is delivered when an IOService has had all matching drivers in the kernel probed and started, but only
	 * once per IOService instance. Some IOService's may be re-registered when their state is changed.*/
	kr = IOServiceAddMatchingNotification(((struct usb_dev_monitor_info*) arg)->notification_port, kIOFirstMatchNotification, added_matching_dict,
			mac_usb_device_added, ((struct usb_dev_monitor_info*) arg)->data, &added_iterator);
	if(kr != KERN_SUCCESS) {
		/* handle error */
	}
	kr = IOServiceAddMatchingNotification(((struct usb_dev_monitor_info*) arg)->notification_port, kIOTerminatedNotification, removed_matching_dict,
			mac_usb_device_removed, ((struct usb_dev_monitor_info*) arg)->data, &removed_iterator);
	if(kr != KERN_SUCCESS) {
		/* handle error */
	}

	/* Iterate once explicitly to empty iterator. */
	mac_usb_device_added(((struct usb_dev_monitor_info*) arg)->data, added_iterator);
	mac_usb_device_removed(((struct usb_dev_monitor_info*) arg)->data, removed_iterator);

	/* notify main thread, initialization successfully completed */
	((struct usb_dev_monitor_info*) arg)->init_done = 0;
	pthread_mutex_unlock(((struct usb_dev_monitor_info*) arg)->mutex);

	/* Start the run loop to begin receiving notifications. */
	CFRunLoopRun();

	/* Reaching here means run loop is stopped; thread is asked to exit. Clean up. */
	(*jvm)->DetachCurrentThread(jvm);
	CFRunLoopRemoveSource(((struct usb_dev_monitor_info*) arg)->run_loop, ((struct usb_dev_monitor_info*) arg)->exit_run_loop_source, kCFRunLoopCommonModes);
	CFRelease(((struct usb_dev_monitor_info*) arg)->exit_run_loop_source);
	IONotificationPortDestroy(((struct usb_dev_monitor_info*) arg)->notification_port); /* will also release usb_run_loop_source */
	CFRunLoopRemoveSource(((struct usb_dev_monitor_info*) arg)->run_loop, usb_run_loop_source, kCFRunLoopCommonModes);
	IOObjectRelease(added_iterator);
	IOObjectRelease(removed_iterator);
	return ((void *)0);
#endif
