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

#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <stdio.h>
#include <errno.h>
#include <sys/ioctl.h>

#if defined (__linux__)
#include <linux/types.h>
#include <linux/input.h>
#include <libudev.h>
#include <linux/hidraw.h>
#endif

#if defined (__APPLE__)
#include <CoreFoundation/CoreFoundation.h>
#include <IOKit/hid/IOHIDKeys.h>
#include <IOKit/hid/IOHIDManager.h>
#endif

#include <jni.h>
#include "unix_like_hid.h"

#if defined (__linux__)
/*
 * Find the required information from USB device (parent udev device) of given HID device
 * (child udev device).
 *
 * @return required information string otherwise NULL.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
jstring linux_get_hiddev_info_string(JNIEnv *env, jlong fd, int info_required) {

	jstring info_string = NULL;
	int ret = -1;
	struct hidraw_devinfo info;
	struct stat st;
	struct udev *udev_ctx;
	struct udev_device *udev_device;
	struct udev_device *usb_udev_device;
	const char *sysattr_val;
	int bus = 0;

	/* find transport/bus used by given HID device. */
	errno = 0;
	ret = ioctl(fd, HIDIOCGRAWINFO, &info);
	if(ret < 0) {
		throw_serialcom_exception(env, 1, errno, NULL);
		return NULL;
	}
	if(info.bustype == BUS_USB) {
		bus = 1;
	}else if(info.bustype == BUS_BLUETOOTH) {
		bus = 2;
	}else {
		throw_serialcom_exception(env, 3, 0, E_NOTONSUPPORTEDBUS);
		return NULL;
	}

	/* Using the given file handle, get the major and minor number of device node (dev_t). */
	errno = 0;
	ret = fstat(fd, &st);
	if(ret < 0) {
		throw_serialcom_exception(env, 1, errno, NULL);
		return NULL;
	}

	udev_ctx = udev_new();

	/* Create new udev device, and fill in information from the sys device and the udev database
	 * entry. The device is looked-up by its major/minor number and type. */
	udev_device = udev_device_new_from_devnum(udev_ctx, 'c', st.st_rdev);
	if(udev_device == NULL) {
		udev_unref(udev_ctx);
		throw_serialcom_exception(env, 3, 0, E_CANNOTCREATEUDEVDEV);
		return NULL;
	}

	if(bus == 1) {
		/* Find the next parent device, with a matching subsystem and devtype value, and fill in
		 * information from the sys device and the udev database entry. */
		usb_udev_device = udev_device_get_parent_with_subsystem_devtype(udev_device, "usb", "usb_device");
		if(usb_udev_device == NULL) {
			udev_device_unref(udev_device);
			udev_unref(udev_ctx);
			throw_serialcom_exception(env, 3, 0, E_CANNOTFINDPARENTUSBHID);
			return NULL;
		}

		if(info_required == 1) {
			sysattr_val = udev_device_get_sysattr_value(usb_udev_device, "manufacturer");
			if(sysattr_val != NULL) {
				info_string = (*env)->NewStringUTF(env, sysattr_val);
			}else {
				info_string = (*env)->NewStringUTF(env, "");
			}
			if((info_string == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
				udev_device_unref(udev_device);
				udev_unref(udev_ctx);
				throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
				return NULL;
			}
		}else if(info_required == 2) {
			sysattr_val = udev_device_get_sysattr_value(usb_udev_device, "product");
			if(sysattr_val != NULL) {
				info_string = (*env)->NewStringUTF(env, sysattr_val);
			}else {
				info_string = (*env)->NewStringUTF(env, "");
			}
			if((info_string == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
				udev_device_unref(udev_device);
				udev_unref(udev_ctx);
				throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
				return NULL;
			}
		}else if(info_required == 3) {
			sysattr_val = udev_device_get_sysattr_value(usb_udev_device, "serial");
			if(sysattr_val != NULL) {
				info_string = (*env)->NewStringUTF(env, sysattr_val);
			}else {
				info_string = (*env)->NewStringUTF(env, "");
			}
			if((info_string == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
				udev_device_unref(udev_device);
				udev_unref(udev_ctx);
				throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
				return NULL;
			}
		}else {
		}
	}else if(bus == 2) {

	}else {
	}

	/* clean up */
	udev_device_unref(udev_device);
	udev_unref(udev_ctx);

	return info_string;
}
#endif

#if defined (__APPLE__)
/*
 * Find the required information about USB HID device using API provided in IOHIDxxx.
 *
 * @return required information string otherwise NULL.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
jstring mac_get_hiddev_info_string(JNIEnv *env, jlong fd, int info_required) {
	int bus = 0;
	char charbuffer[128];
	jstring info_string = NULL;
	CFStringRef str_ref;

	/* find transport/bus used by given HID device. */
	str_ref = IOHIDDeviceGetProperty(fd, CFSTR(kIOHIDTransportKey));
	if(str_ref == NULL) {
		(*env)->ThrowNew(env, serialComExpCls, E_COULDNOTTARNSPORT);
		return NULL;
	}
	memset(charbuffer, '\0', sizeof(charbuffer));
	CFStringGetCString(str_ref, charbuffer, sizeof(charbuffer), kCFStringEncodingUTF8);
	CFRelease(str_ref);
	if((strcmp(charbuffer, "USB") == 0) || (strcmp(charbuffer, "usb") == 0)) {
		bus = 1;
	}else if((strcmp(charbuffer, "BLUETOOTH") == 0) || (strcmp(charbuffer, "bluetooth") == 0) || (strcmp(charbuffer, "Bluetooth") == 0)) {
		bus = 2;
	}else {
	}

	if(bus == 1) {
		if(info_required == 1) {
			str_ref = IOHIDDeviceGetProperty(fd, kIOHIDManufacturerKey);
			if(str_ref == NULL) {
				(*env)->ThrowNew(env, serialComExpCls, E_COULDNOTMANUFCTRER);
				return NULL;
			}
			memset(charbuffer, '\0', sizeof(charbuffer));
			CFStringGetCString(str_ref, charbuffer, sizeof(charbuffer), kCFStringEncodingUTF8);
			CFRelease(str_ref);
		}else if(info_required == 2) {
		}else if(info_required == 3) {
		}else {
		}
		info_string = (*env)->NewStringUTF(env, charbuffer);
		if((info_string == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			(*env)->ThrowNew(env, serialComExpCls, E_NEWSTRUTFSTR);
			return NULL;
		}
	}else if(bus ==2) {
	}else {
	}

	return info_string;
}
#endif
