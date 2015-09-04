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

/* It is possible to re-factor some functions or things like that to factor in common function.
 * We have knowingly kept internal dependencies to minimum so as to accommodate future changes.*/

#ifndef UNIX_LIKE_HID_H_
#define UNIX_LIKE_HID_H_

#if defined (__linux__)
#include <libudev.h>
#endif

#if defined (__APPLE__)
#include <CoreFoundation/CoreFoundation.h>
#include <IOKit/hid/IOHIDKeys.h>
#include <IOKit/hid/IOHIDManager.h>
#endif

#include <sys/ioctl.h>
#include <jni.h>

#if defined (__linux__)
/* Hack to work around failing compilation on systems that don't
 * yet populate new version of hidraw.h to userspace. */
#ifndef HIDIOCSFEATURE
#define HIDIOCSFEATURE(len)    _IOC(_IOC_WRITE|_IOC_READ, 'H', 0x06, len)
#define HIDIOCGFEATURE(len)    _IOC(_IOC_WRITE|_IOC_READ, 'H', 0x07, len)
#endif
#endif

/* Constant string defines */
#define SCOMEXPCLASS "com/embeddedunveiled/serial/SerialComException"
#define JAVALSTRING "java/lang/String"
#define E_NEWGLOBALREFSTR "JNI Call NewGlobalRef failed !"
#define E_GETSTRUTFCHARSTR "JNI call GetStringUTFChars failed !"
#define FAILTHOWEXP "JNI call ThrowNew failed to throw exception !"
#define E_FINDCLASSSCOMEXPSTR "Can not find class com/embeddedunveiled/serial/SerialComException. Probably out of memory !"
#define E_GETBYTEARRELEMTSTR "JNI call GetByteArrayElements failed !"
#define E_GETBYTEARRREGIONSTR "JNI call GetByteArrayRegion failed !"
#define E_MALLOCSTR "malloc() failed to allocate requested memory !"
#define E_NEWSTRUTFSTR "JNI call NewStringUTF failed !"
#define E_FINDCLASSSSTRINGSTR "Can not find class java/lang/String. Probably out of memory !"
#define E_NEWOBJECTARRAYSTR "JNI call NewObjectArray failed. Probably out of memory !"
#define E_SETOBJECTARRAYSTR "JNI call SetObjectArrayElement failed. Either index violation or wrong class used !"
#define E_SETBYTEARRAYREGION "JNI call SetByteArrayRegion failed. Probably index out of bound !"

#define E_CALLOCSTR "calloc() failed to allocate requested memory !"
#define E_CANNOTFINDDEVNODE "Failed to find device node for the USB HID interface !"
#define E_CANNOTFINDPARENTUDEV "Could not find parent udev device for the USB HID interface !"
#define E_CANNOTCREATEUDEVDEV "Could not create udev device from major/minor numbers of device node !"
#define E_CANNOTFINDPARENTUSBHID "Could not find parent USB HID device from given file handle !"
#define E_NOTONSUPPORTEDBUS "Given device is not on USB or Bluetooth bus !"
#define E_NOMANUFACTURERVAL "Given device does not seem to have manufacturer value set !"

#define E_COULDNOTTARNSPORT "IOHIDDeviceGetProperty for finding transport for given HID device returned null !"
#define E_COULDNOTMANUFCTRER "IOHIDDeviceGetProperty for finding manufacturer for given HID device returned null !"

/* This holds information for implementing dynamically growing array in C language. */
struct jstrarray_list {
	jstring *base;      /* pointer to an array of pointers to string */
	int index;         /* array element index                       */
	int current_size;  /* size of this array                        */
};

/* global variables */
extern jclass serialComExpCls;

/* function prototypes (declared in reverse order of use) */
extern int LOGE(const char *error_msg);
extern void throw_serialcom_exception(JNIEnv *env, int type, int error_code, const char *);
extern void free_jstrarraylist(struct jstrarray_list *al);
extern void insert_jstrarraylist(struct jstrarray_list *al, jstring element);
extern void init_jstrarraylist(struct jstrarray_list *al, int initial_size);

#if defined (__linux__)
extern jstring linux_clean_throw_exp_usbenumeration(JNIEnv *env, int task, const char *expmsg,
		struct jstrarray_list *list, struct udev_device *udev_device, struct udev_enumerate *enumerator,
		struct udev *udev_ctx);
extern jobjectArray linux_enumerate_usb_hid_devices(JNIEnv *env, jint vendor_filter);
extern jlong linux_clean_throw_exp_usbattropen(JNIEnv *env, int task, const char *expmsg,
		struct udev_device *udev_device, struct udev_enumerate *enumerator, struct udev *udev_ctx);
extern jlong linux_usbattrhid_open(JNIEnv *env, jint usbvid, jint usbpid, jstring usbserialnumber,
		jint busnum, jint devnum);
extern jint linux_send_output_report(JNIEnv *env, jlong fd, jbyte reportID, jbyteArray report, jint length);
extern jint linux_send_feature_report(JNIEnv *env, jlong fd, jbyte reportID, jbyteArray report, jint length);
extern jint linux_get_feature_report(JNIEnv *env, jlong fd, jbyte reportID, jbyteArray report, jint length);
extern jstring linux_get_hiddev_info_string(JNIEnv *env, jlong fd, int info_required);
#endif

#if defined (__APPLE__)
extern jobjectArray mac_enumerate_usb_hid_devices(JNIEnv *env, jint vendor_filter, IOHIDManagerRef mac_hid_mgr);
extern 	jstring mac_clean_throw_exp_usbenumeration(JNIEnv *env, int task, const char *expmsg, CFSetRef hiddev_cfset,
		IOHIDDeviceRef *hiddev_references);
extern jlong mac_usbattrhid_open(JNIEnv *env, jint usbvid, jint usbpid, jstring usbserialnumber, jint locationID);
extern jint mac_send_output_report(JNIEnv *env, jlong fd, jbyte reportID, jbyteArray report, jint length);
extern jint mac_send_feature_report(JNIEnv *env, jlong fd, jbyte reportID, jbyteArray report, jint length);
extern jint mac_get_feature_report(JNIEnv *env, jlong fd, jbyte reportID, jbyteArray report, jint length);
extern jstring mac_get_hiddev_info_string(JNIEnv *env, jlong fd, int info_required);
#endif

extern jint get_report_descriptor_size(JNIEnv *env, jlong fd);

extern jstring get_hiddev_indexed_string(JNIEnv *env, jlong fd, int index);

#endif /* UNIX_LIKE_HID_H_ */
