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

#include <stdarg.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <stdio.h>
#include <unistd.h>
#include <fcntl.h>

#if defined (__linux__)
#include <libudev.h>
#endif

#if defined (__APPLE__)
#include <CoreFoundation/CoreFoundation.h>
#include <IOKit/hid/IOHIDKeys.h>
#include <IOKit/hid/IOHIDManager.h>
#endif

#include <jni.h>
#include "unix_like_hid.h"

#if defined (__linux__)
/* Cleans up resources and set exception that will get thrown upon return to java layer. */
jlong linux_clean_throw_exp_usbattropen(JNIEnv *env, int task, const char *expmsg,
		struct udev_device *udev_device, struct udev_enumerate *enumerator, struct udev *udev_ctx) {

	(*env)->ExceptionClear(env);

	/* free memory first, so even if throwing JNI exception fails, this succeeds. */
	if(task == 1) {
		udev_device_unref(udev_device);
		udev_enumerate_unref(enumerator);
		udev_unref(udev_ctx);
	}else if(task == 2) {
		udev_device_unref(udev_device);
		udev_enumerate_unref(enumerator);
		udev_unref(udev_ctx);
	}else {
	}

	jclass serialComExceptionClass = (*env)->FindClass(env, SCOMEXPCLASS);
	if((serialComExceptionClass == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		(*env)->ExceptionClear(env);
		LOGE(E_FINDCLASSSCOMEXPSTR);
		return -1;
	}

	if(task == 1) {
		(*env)->ThrowNew(env, serialComExceptionClass, E_NEWSTRUTFSTR);
	}else {
		(*env)->ThrowNew(env, serialComExceptionClass, expmsg);
	}

	return -1;
}

/*
 * The USB strings are Unicode, UCS2 encoded, but the strings returned from udev_device_get_sysattr_value()
 * are UTF-8 encoded. GetStringUTFChars() returns in modified UTF-8 encoding.
 */
jlong linux_usbattrhid_open(JNIEnv *env, jint usbvid, jint usbpid, jstring usbserialnumber, jint busnum, jint devnum) {
	jlong fd = -1;
	const char* serial_num_to_match = NULL;
	int matching_dev_found = -1;
	struct udev *udev_ctx;
	struct udev_enumerate *enumerator;
	struct udev_list_entry *devices, *dev_list_entry;
	const char *sysattr_val;
	const char *path;
	struct udev_device *udev_device;
	struct udev_device *parent_udev_device;
	const char *device_node;
	char device_file[128];
	char *endptr;

	serial_num_to_match = (*env)->GetStringUTFChars(env, usbserialnumber, NULL);
	if((serial_num_to_match == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_GETSTRUTFCHARSTR);
		return -1;
	}

	udev_ctx = udev_new();
	enumerator = udev_enumerate_new(udev_ctx);
	udev_enumerate_add_match_subsystem(enumerator, "hidraw");
	udev_enumerate_scan_devices(enumerator);
	devices = udev_enumerate_get_list_entry(enumerator);

	udev_list_entry_foreach(dev_list_entry, devices) {
		path = udev_list_entry_get_name(dev_list_entry);
		udev_device = udev_device_new_from_syspath(udev_enumerate_get_udev(enumerator), path);
		if(udev_device == NULL) {
			continue;
		}

		/* save the device file */
		device_node = udev_device_get_devnode(udev_device);
		if(device_node == NULL) {
			return linux_clean_throw_exp_usbattropen(env, 2, E_CANNOTFINDDEVNODE, udev_device, enumerator, udev_ctx);
		}
		memset(device_file, '\0', sizeof(device_file));
		strcpy(device_file, device_node);

		parent_udev_device = udev_device_get_parent_with_subsystem_devtype(udev_device, "usb", "usb_device");
		if(parent_udev_device == NULL) {
			udev_device_unref(udev_device);
			continue;
		}

		/* USB-IF VENDOR ID */
		sysattr_val = udev_device_get_sysattr_value(parent_udev_device, "idVendor");
		if(sysattr_val != NULL) {
			if(usbvid != (0x0000FFFF & (int)strtol(sysattr_val, &endptr, 16))) {
				udev_device_unref(udev_device);
				continue;
			}
		}else {
			udev_device_unref(udev_device);
			continue;
		}

		/* USB PRODUCT ID */
		sysattr_val = udev_device_get_sysattr_value(parent_udev_device, "idProduct");
		if(sysattr_val != NULL) {
			if(usbpid != (0x0000FFFF & (int)strtol(sysattr_val, &endptr, 16))) {
				udev_device_unref(udev_device);
				continue;
			}
		}else {
			udev_device_unref(udev_device);
			continue;
		}

		/* SERIAL NUMBER */
		if(serial_num_to_match != NULL) {
			sysattr_val = udev_device_get_sysattr_value(parent_udev_device, "serial");
			if(sysattr_val != NULL) {
				if(strcasecmp(sysattr_val, serial_num_to_match) != 0) {
					udev_device_unref(udev_device);
					continue;
				}
			}else {
				udev_device_unref(udev_device);
				continue;
			}
		}

		/* BUS NUMBER */
		if(busnum != -1) {
			sysattr_val = udev_device_get_sysattr_value(parent_udev_device, "busnum");
			if(sysattr_val != NULL) {
				if(busnum != (int)strtol(sysattr_val, &endptr, 10)) {
					udev_device_unref(udev_device);
					continue;
				}
			}else {
				udev_device_unref(udev_device);
				continue;
			}
		}

		/* DEVICE NUMBER */
		if(devnum != -1) {
			sysattr_val = udev_device_get_sysattr_value(parent_udev_device, "devnum");
			if(sysattr_val != NULL) {
				if(devnum != (int)strtol(sysattr_val, &endptr, 10)) {
					udev_device_unref(udev_device);
					continue;
				}
			}else {
				udev_device_unref(udev_device);
				continue;
			}
		}

		udev_device_unref(udev_device);
		/* reaching here means matching device is found */
		matching_dev_found = 1;
		break;
	}
	udev_enumerate_unref(enumerator);
	udev_unref(udev_ctx);
	(*env)->ReleaseStringUTFChars(env, usbserialnumber, serial_num_to_match);

	if(matching_dev_found < 0) {
		throw_serialcom_exception(env, 1, ENXIO, NULL);
		return -1;
	}

	errno = 0;
	fd = open(device_file, O_RDWR);
	if(fd < 0) {
		throw_serialcom_exception(env, 1, errno, NULL);
		return -1;
	}
	return fd;
}
#endif
