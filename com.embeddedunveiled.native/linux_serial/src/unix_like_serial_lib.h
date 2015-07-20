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

#ifndef UNIX_LIKE_SERIAL_LIB_H_
#define UNIX_LIKE_SERIAL_LIB_H_

#include <pthread.h>
#if defined (__APPLE__)
#include <CoreFoundation/CoreFoundation.h>
#include <IOKit/IOKitLib.h>
#include <IOKit/serial/IOSerialKeys.h>
#include <IOKit/serial/ioss.h>
#include <IOKit/IOBSD.h>
#include <IOKit/IOMessage.h>
#include <IOKit/usb/IOUSBLib.h>
#endif
#include <jni.h>

/* custom error codes for SCM library */
#define ERROR_OFFSET 350
#define E_GETJVM              (ERROR_OFFSET + 1)
#define E_NEWSTRUTF           (ERROR_OFFSET + 2)
#define E_OPENDIR             (ERROR_OFFSET + 3)  /* Either filename cannot be accessed or cannot malloc() enough memory to hold the whole thing. */
#define E_FINDCLASS           (ERROR_OFFSET + 4)  /* Probably out of memory. */
#define E_NEWOBJECTARRAY      (ERROR_OFFSET + 5)  /* Probably out of memory. */
#define E_GETSTRUTFCHAR       (ERROR_OFFSET + 6)
#define E_NEWGLOBALREF        (ERROR_OFFSET + 7)  /* Probably out of memory. */
#define E_ATTACHCURRENTTHREAD (ERROR_OFFSET + 8)
#define E_GETOBJECTCLASS      (ERROR_OFFSET + 9)
#define E_GETMETHODID         (ERROR_OFFSET + 10) /* Probably out of memory. */
#define E_UDEVNEW             (ERROR_OFFSET + 11) /* Could not create udev context. */
#define E_UDEVNETLINK         (ERROR_OFFSET + 12) /* Could not initialize udev monitor. */
#define E_ENBLPARCHK          (ERROR_OFFSET + 13) /* Enable parity checking in configureComPortData method first. */
#define E_IOSRVMATUSBDEV      (ERROR_OFFSET + 14) /* IOServiceMatching("IOUSBDevice") failed. */

/* Structure representing data that is passed to each data looper thread with info corresponding to that file descriptor. */
struct com_thread_params {
	JavaVM *jvm;
	int fd;
	jobject looper;
	pthread_t data_thread_id;
	pthread_t event_thread_id;
	int evfd;               /* used to get epoll_wait and kevent out of waiting state so that it checks for thread exit condition. */
	int data_thread_exit;   /* set to 1 to indicate that the data thread should exit gracefully. */
	int event_thread_exit;  /* set to 1 to indicate that the event thread should exit gracefully. */
	pthread_mutex_t *mutex; /* protect global shared data from synchronous access */
	int data_init_done;     /* indicates data thread has been successfully initialized or not; 0 is default 1 is success, otherwise error number as set by thread */
	int event_init_done;    /* indicates event thread has been successfully initialized or not; 0 is default 1 is success, otherwise error number as set by thread */
	pthread_attr_t data_thread_attr;
	pthread_attr_t event_thread_attr;
};

/* The port_info structure has platform specific fields based on how thread is created, destroyed and what data need to be passed.*/
	struct port_info {
#if defined (__linux__)
		JavaVM *jvm;
		jobject usbHotPlugEventListener;
		jint filterVID;
		jint filterPID;
		int thread_exit;
		pthread_t thread_id;
		pthread_attr_t thread_attr;
		pthread_mutex_t *mutex;
		int init_done;
		int evfd;
#elif defined (__APPLE__)
		JavaVM *jvm;
		JNIEnv* env;
		jobject usbHotPlugEventListener;
		jmethodID onHotPlugEventMethodID;
		jint filterVID;
		jint filterPID;
		int thread_exit;
		pthread_t thread_id;
		pthread_attr_t thread_attr;
		pthread_mutex_t *mutex;
		int init_done;
		CFRunLoopSourceRef exit_run_loop_source;
		CFRunLoopRef run_loop;
		struct port_info *data;
		IONotificationPortRef notification_port;
		int empty_iterator_added;
		int empty_iterator_removed;
		jmethodID mid;
#elif defined (__SunOS)
#else
#endif
	};

#if defined (__APPLE__)
	/* Structure to hold reference to driver and subscribed notification. */
	struct driver_ref {
		io_service_t service;
		io_object_t notification;
		struct port_info *data;
	};
#endif

/* This holds information for implementing dynamically growing array in C language. */
struct jstrarray_list {
	jstring *base;      /* pointer to an array of pointers to string */
	int index;        /* array element index                       */
	int current_size; /* size of this array                        */
};

/* function prototypes (declared in reverse order of use) */
extern int set_error_status(JNIEnv *env, jobject obj, jobject status, int error_number);
extern void init_jstrarraylist(struct jstrarray_list *al, int initial_size);
extern void insert_jstrarraylist(struct jstrarray_list *al, jstring element);
extern void free_jstrarraylist(struct jstrarray_list *al);
#if defined (__APPLE__)
void mac_indicate_thread_exit(void *info);
void mac_usb_device_added(void *refCon, io_iterator_t iterator);
void mac_usb_device_removed(void *refCon, io_iterator_t iterator);
#endif
extern jobjectArray list_usb_devices(JNIEnv *env, jobject obj, jobject status);
extern jobjectArray vcp_node_from_usb_attributes(JNIEnv *env, jobject obj, int usbvid_to_match, int usbpid_to_match, jstring serial_num, jobject status);
extern int serial_delay(unsigned usecs);
extern void *data_looper(void *params);
extern void *event_looper(void *params);
extern void *usb_hot_plug_monitor(void *params);

#endif /* UNIX_LIKE_SERIAL_LIB_H_ */

