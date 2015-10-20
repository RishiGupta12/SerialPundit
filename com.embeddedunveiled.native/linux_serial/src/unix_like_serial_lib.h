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

#if defined (__linux__)
#include <libudev.h>
#endif

#if defined (__APPLE__)
#include <CoreFoundation/CoreFoundation.h>
#include <IOKit/IOKitLib.h>
#include <IOKit/serial/IOSerialKeys.h>
#include <IOKit/serial/ioss.h>
#include <IOKit/IOBSD.h>
#include <IOKit/IOMessage.h>
#include <IOKit/usb/IOUSBLib.h>
#endif

#include <pthread.h>
#include <jni.h>

/* Constant string defines */
#define SCOMEXPCLASS "com/embeddedunveiled/serial/SerialComException"
#define JAVALSTRING "java/lang/String"
#define FAILTHOWEXP "JNI call ThrowNew failed to throw exception !"

#define E_UNKNOWN "Unknown error occurred !"
#define E_ENBLPARCHKSTR "Parity checking in configureComPortData method needs to be enabled first !"
#define E_GETJVMSTR "JNI call GetJavaVM failed !"
#define E_FINDCLASSSCOMEXPSTR "Can not find class com/embeddedunveiled/serial/SerialComException. Probably out of memory !"
#define E_FINDCLASSSSTRINGSTR "Can not find class java/lang/String. Probably out of memory !"
#define E_NEWOBJECTARRAYSTR "JNI call NewObjectArray failed. Probably out of memory !"
#define E_NEWBYTEARRAYSTR "JNI call NewByteArray failed !"
#define E_NEWINTARRAYSTR "JNI call NewIntArray failed !"
#define E_SETOBJECTARRAYSTR "JNI call SetObjectArrayElement failed. Either index violation or wrong class used !"
#define E_SETBYTEARRREGIONSTR "JNI call SetByteArrayRegion failed !"
#define E_SETINTARRREGIONSTR "JNI call SetIntArrayRegion failed !"
#define E_NEWSTRUTFSTR "JNI call NewStringUTF failed !"
#define E_GETSTRUTFCHARSTR "JNI call GetStringUTFChars failed !"
#define E_GETBYTEARRELEMTSTR "JNI call GetByteArrayElements failed !"
#define E_GETBYTEARRREGIONSTR "JNI call GetByteArrayRegion failed !"
#define E_NEWGLOBALREFSTR "JNI Call NewGlobalRef failed !"
#define E_DELGLOBALREFSTR "JNI Call DeleteGlobalRef failed !"
#define E_MALLOCSTR "malloc() failed to allocate requested memory !"
#define E_CALLOCSTR "calloc() failed to allocate requested memory !"
#define E_REALLOCSTR "realloc() failed to allocate requested memory !"
#define E_ATTACHCURRENTTHREADSTR "JNI call AttachCurrentThread failed !"
#define E_GETOBJECTCLASSSTR "JNI call GetObjectClass failed !"
#define E_GETMETHODIDSTR "JNI call GetMethodID failed !"
#define E_DETACHCURTHREAD "JNI call DetachCurrentThread failed !"
#define E_SIGNALINSTFAILSTR "Failed to install signal handler !"
#define E_CALLVOIDMETHDSTR "JNI call CallVoidMethod failed !"
#define E_UDEVNEWSTR "Could not create udev context !"
#define E_UDEVNETLINKSTR "Could not create udev monitor !"
#define E_IOSRVMATUSBDEVSTR "Function call IOServiceMatching('IOUSBDevice') failed !"
#define E_GETDIRCTBUFADDRSTR "JNI call GetDirectBufferAddress failed !"
#define E_VIOVNTINVALIDSTR "The length of data supplied exceeds maximum limit !"
#define E_HCIOPENDEV "Could not open BT HCI device !"
#define E_HCIREADNAME "Could not read local name of BT HCI device !"
#define E_HCIBTADDR "Could not determine address of BT HCI device !"
#define E_CANNOTFINDDEVNODE "Failed to find device node from sysfs path !"
#define E_WRITEZERONOTALLOWED "Write requires at least one byte data !"
#define E_UNBLOCKIO "Byte stream unblocked !"

/* Custom error codes and messages for SCM library */
#define ERROR_OFFSET 15000
#define E_CALLOC              (ERROR_OFFSET + 1)
#define E_ATTACHCURRENTTHREAD (ERROR_OFFSET + 2)
#define E_GETOBJECTCLASS      (ERROR_OFFSET + 3)
#define E_GETMETHODID         (ERROR_OFFSET + 4)
#define E_SIGNALINSTFAIL      (ERROR_OFFSET + 5)
#define E_CALLVOIDMETHD       (ERROR_OFFSET + 6)
#define E_UDEVNEW             (ERROR_OFFSET + 7)
#define E_UDEVNETLINK         (ERROR_OFFSET + 8)
#define E_IOSRVMATUSBDEV      (ERROR_OFFSET + 9)

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
	int data_init_done;     /* indicates data thread has been successfully initialized or not */
	int event_init_done;    /* indicates event thread has been successfully initialized or not */
	int data_custom_err_code;
	int data_standard_err_code;
	int event_custom_err_code;
	int event_standard_err_code;
	pthread_attr_t data_thread_attr;
	pthread_attr_t event_thread_attr;
};

#if defined (__linux__)
/* The port_info structure has platform specific fields based on how thread is created,
 * destroyed and what data need to be passed.*/
struct usb_dev_monitor_info {
	JavaVM *jvm;
	jobject usbHotPlugEventListener;
	int usb_vid_to_match;
	int usb_pid_to_match;
	char serial_number_to_match[64];
	int thread_exit;
	pthread_t thread_id;
	pthread_attr_t thread_attr;
	pthread_mutex_t *mutex;
	int init_done;
	int standard_err_code;
	int custom_err_code;
	int evfd;
};
#endif

#if defined (__APPLE__)
/* Structure to hold reference to driver and subscribed notification. */
struct driver_ref {
	io_service_t service;
	io_object_t notification;
	struct usb_dev_monitor_info *data;
};

struct usb_dev_monitor_info {
	JavaVM *jvm;
	JNIEnv* env;
	jobject usbHotPlugEventListener;
	jmethodID onUSBHotPlugEventMethodID;
	int usb_vid_to_match;
	int usb_pid_to_match;
	char serial_number_to_match[64];
	int thread_exit;
	pthread_t thread_id;
	pthread_attr_t thread_attr;
	pthread_mutex_t *mutex;
	int init_done;
	int standard_err_code;
	int custom_err_code;
	CFRunLoopSourceRef exit_run_loop_source;
	CFRunLoopRef run_loop;
	struct usb_dev_monitor_info *data;
	IONotificationPortRef notification_port;
	int empty_iterator_added;
	int empty_iterator_removed;
	jmethodID mid;
};
#endif

/* This holds information for implementing dynamically growing array in C language. */
struct jstrarray_list {
	jstring *base;      /* pointer to an array of pointers to string */
	int index;         /* array element index                       */
	int current_size;  /* size of this array                        */
};

/* function prototypes (declared in reverse order of use) */
int LOGE(const char *msga, const char *msgb);
int LOGEN(const char *msga, const char *msgb, unsigned int error_num);
void throw_serialcom_exception(JNIEnv *env, int type, int error_code, const char *);
void free_jstrarraylist(struct jstrarray_list *al);
void insert_jstrarraylist(struct jstrarray_list *al, jstring element);
void init_jstrarraylist(struct jstrarray_list *al, int initial_size);
int serial_delay(unsigned ms);

#if defined (__linux__)
jstring linux_listusb_clean_throw_exp(JNIEnv *env, int task, const char *expmsg, struct jstrarray_list *list, struct udev_device *udev_device, struct udev_enumerate *enumerator, struct udev *udev_ctx);
jstring linux_rfcomm_cleanexp(JNIEnv *env, int task, const char *expmsg, struct jstrarray_list *list, struct udev_device *udev_device, struct udev_enumerate *enumerator, struct udev *udev_ctx);
#endif

#if defined (__APPLE__)
jstring mac_listusb_clean_throw_exp(JNIEnv *env, struct jstrarray_list *list, io_service_t usb_dev_obj, io_iterator_t iterator)
void mac_indicate_thread_exit(void *info);
void mac_usb_device_added(void *refCon, io_iterator_t iterator);
void mac_usb_device_removed(void *refCon, io_iterator_t iterator);
#endif

jint is_usb_dev_connected(JNIEnv *env, jint usbvid_to_match, jint usbpid_to_match, jstring serial_number);
jstring find_driver_for_given_com_port(JNIEnv *env, jstring com_port_name);
jstring find_address_irq_for_given_com_port(JNIEnv *env, jlong fd);
jobjectArray list_usb_devices(JNIEnv *env, jint vendor_filter);
jobjectArray list_bt_rfcomm_dev_nodes(JNIEnv *env);
jobjectArray vcp_node_from_usb_attributes(JNIEnv *env, jint usbvid_to_match, jint usbpid_to_match, jstring serial_numer);
jobjectArray get_usbdev_powerinfo(JNIEnv *env, jstring comPortName);

void *data_looper(void *params);
void *event_looper(void *params);
void *usb_device_hotplug_monitor(void *params);

#endif /* UNIX_LIKE_SERIAL_LIB_H_ */
