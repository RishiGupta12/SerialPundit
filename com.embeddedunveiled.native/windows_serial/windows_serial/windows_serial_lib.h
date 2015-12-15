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

#ifndef WINDOWS_SERIAL_LIB_H_
#define WINDOWS_SERIAL_LIB_H_

#pragma once
#pragma comment (lib, "Setupapi.lib")
#pragma comment (lib, "cfgmgr32.lib")

#include "stdafx.h"
#include <windows.h>
#include <jni.h>

/* This is the maximum number of threads and hence data listeners instance we support. */
#define MAX_NUM_THREADS 1024

#define CommInBufSize 8192
#define CommOutBufSize 3072

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
#define E_NEWBYTEARRAYSTR "JNI call NewByteArray failed. Probably out of memory !"
#define E_NEWINTARRAYSTR "JNI call NewIntArray failed. Probably out of memory !"
#define E_SETOBJECTARRAYSTR "JNI call SetObjectArrayElement failed. Either index violation or wrong class used !"
#define E_SETBYTEARRREGIONSTR "JNI call SetByteArrayRegion failed !"
#define E_SETINTARRREGIONSTR "JNI call SetIntArrayRegion failed !"
#define E_NEWSTRUTFSTR "JNI call NewStringUTF failed. Probably out of memory !"
#define E_NEWSTRSTR "JNI call NewString failed. Probably out of memory !"
#define E_GETSTRUTFCHARSTR "JNI call GetStringUTFChars failed !"
#define E_GETSTRCHARSTR "JNI call GetStringChars failed !"
#define E_GETBYTEARRELEMTSTR "JNI call GetByteArrayElements failed !"
#define E_GETBYTEARRREGIONSTR "JNI call GetByteArrayRegion failed !"
#define E_NEWGLOBALREFSTR "JNI Call NewGlobalRef failed !"
#define E_DELGLOBALREFSTR "JNI Call DeleteGlobalRef failed !"
#define E_MALLOCSTR "malloc() failed to allocate requested memory !"
#define E_CALLOCSTR "calloc() failed to allocate requested memory !"
#define E_HEAPALLOCSTR "HeapAlloc() failed to allocate requested memory !"
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

#define E_UNBLOCKIO "I/O operation unblocked !"
#define E_NOTFTDIPORT "Given COM port does not exist or may not be a FTDI com port !"

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

/* Structure representing data that is passed to each data looper thread
 * with info corresponding to that file descriptor. */
struct looper_thread_params {
	JavaVM *jvm;
	HANDLE hComm;
	jobject looper;
	HANDLE thread_handle;
	HANDLE wait_event_handles[2];
	int thread_exit;
	CRITICAL_SECTION *csmutex;
	int data_enabled;
	int event_enabled;
	int init_done;
	HANDLE init_done_event_handle;
	int custom_err_code;
	int standard_err_code;
};

struct usb_dev_monitor_info {
	JavaVM *jvm;
	JNIEnv* env;
	HANDLE thread_handle;
	HANDLE wait_event_handle;
	HWND window_handle;
	int thread_exit;
	int usb_vid_to_match;
	int usb_pid_to_match;
	char serial_number_to_match[64];
	jobject usbHotPlugEventListener;
	jmethodID onUSBHotPlugEventMethodID;
	struct usb_dev_monitor_info *info;
	int init_done;
	HANDLE init_done_event_handle;
	int custom_err_code;
	int standard_err_code;
	CRITICAL_SECTION *csmutex;
};

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
jint is_usb_dev_connected(JNIEnv *env, jint usbvid_to_match, jint usbpid_to_match, jstring serial_number);
int get_driver_com_port_usb(JNIEnv *env, const jchar *port_name, TCHAR *driver_name);
int get_driver_com_port_multiportadaptor(JNIEnv *env, const jchar *port_name, TCHAR *driver_name);
jstring find_driver_for_given_com_port(JNIEnv *env, jstring comPortName);
jstring find_address_irq_for_given_com_port(JNIEnv *env, jlong fd);
jobjectArray list_usb_devices(JNIEnv *env, jint vendor_filter);
jobjectArray vcp_node_from_usb_attributes(JNIEnv *env, jint usbvid_to_match, jint usbpid_to_match, jstring serial_num);
jobjectArray get_usbdev_powerinfo(JNIEnv *env, jstring comPortName);
jint get_latency_timer_value(JNIEnv *env, jstring comPortName);
jint set_latency_timer_value(JNIEnv *env, jstring comPortName, jbyte timerValue);
jobjectArray getusb_firmware_version(JNIEnv *env, jint usbvid_to_match, jint usbpid_to_match, jstring serial_number);

jobjectArray list_bt_rfcomm_dev_nodes(JNIEnv *env);

int setupLooperThread(JNIEnv *env, jobject obj, jlong handle, jobject looper_obj_ref, int data_enabled, int event_enabled, int global_index, int new_dtp_index);
unsigned WINAPI event_data_looper(LPVOID lpParam);

LRESULT CALLBACK usb_hotplug_event_handler(HWND window_handle, UINT msg, WPARAM event, LPARAM event_data);
unsigned WINAPI usb_device_hotplug_monitor(LPVOID lpParam);

#endif /* WINDOWS_SERIAL_LIB_H_ */

