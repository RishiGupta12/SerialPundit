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

#if defined (__linux__) || defined (__APPLE__) || defined (__SunOS) || defined(__sun) || defined(__FreeBSD__) || defined(__OpenBSD__) || defined(__NetBSD__) || defined(__hpux__) || defined(__hpux) || defined(_AIX)

#if defined (__APPLE__)
#include <IOKit/IOMessage.h>
#endif


#ifndef UNIX_LIKE_SERIAL_LIB_H_
#define UNIX_LIKE_SERIAL_LIB_H_

#include <pthread.h>		/* POSIX thread definitions	      */

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
		int evfd;
		int thread_exit;
		pthread_t thread_id;
		pthread_attr_t thread_attr;
		pthread_mutex_t *mutex;
		int init_done;
#elif defined (__APPLE__)
		JavaVM *jvm;
		JNIEnv* env;
		const char *port_name;
		int fd;
		int thread_exit;
		jobject port_listener;
		jmethodID mid;
		pthread_t thread_id;
		pthread_mutex_t *mutex;
		struct port_info *data;
		IONotificationPortRef notification_port;
		int tempVal;
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

/* function prototypes */
extern void *data_looper(void *params);
extern void *event_looper(void *params);
extern void *usb_hot_plug_monitor(void *params);
extern int serial_delay(unsigned usecs);
extern int set_error_status(JNIEnv *env, jobject obj, jobject status, int error_number);

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

#endif /* UNIX_LIKE_SERIAL_LIB_H_ */

#endif /* end compiling for Unix-like OS */
