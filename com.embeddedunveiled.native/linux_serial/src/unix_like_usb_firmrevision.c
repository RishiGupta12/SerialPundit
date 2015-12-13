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

#if defined (__linux__)
#include <libudev.h>
#endif

#if defined (__APPLE__)
#include <CoreFoundation/CoreFoundation.h>
#include <IOKit/IOKitLib.h>
#include <IOKit/usb/IOUSBLib.h>
#endif

#include <jni.h>
#include "unix_like_serial_lib.h"

#if defined (__linux__)
/*
 *
 */
jobjectArray getusb_firmware_version(JNIEnv *env, jint usbvid_to_match, jint usbpid_to_match, jstring serial_number) {

	int x = 0;
	int vid = 0;
	int pid = 0;
	struct udev *udev_ctx;
	struct udev_enumerate *enumerator;
	struct udev_list_entry *devices, *dev_list_entry;
	const char *sysattr_val;
	const char *path;
	struct udev_device *udev_device;
	char *endptr;
	char buffer[128];
	const char* serial = NULL;
	const char *prop_val;
	struct jstrarray_list list = {0};
	jstring usb_dev_info;
	jclass strClass = NULL;
	jobjectArray usbDevicesFwVerFound = NULL;

	if(serial_number != NULL) {
		serial = (*env)->GetStringUTFChars(env, serial_number, NULL);
		if((serial == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			throw_serialcom_exception(env, 3, 0, E_GETSTRUTFCHARSTR);
			return NULL;
		}
	}

	init_jstrarraylist(&list, 10);

	/* libudev is reference counted. Memory is freed when counts reach to zero. */
	udev_ctx = udev_new();
	enumerator = udev_enumerate_new(udev_ctx);
	udev_enumerate_add_match_subsystem(enumerator, "usb");
	udev_enumerate_scan_devices(enumerator);
	devices = udev_enumerate_get_list_entry(enumerator);

	udev_list_entry_foreach(dev_list_entry, devices) {
		path = udev_list_entry_get_name(dev_list_entry);
		udev_device = udev_device_new_from_syspath(udev_enumerate_get_udev(enumerator), path);
		if(udev_device == NULL) {
			continue;
		}

		if(strcmp("usb_device", udev_device_get_devtype(udev_device)) == 0) {

			/* match vid */
			sysattr_val = udev_device_get_sysattr_value(udev_device, "idVendor");
			if(sysattr_val != NULL) {
				vid = 0x0000FFFF & (int)strtol(sysattr_val, &endptr, 16);
				if(vid != usbvid_to_match) {
					udev_device_unref(udev_device);
					continue;
				}
			}else {
				udev_device_unref(udev_device);
				continue;
			}

			/* match pid */
			sysattr_val = udev_device_get_sysattr_value(udev_device, "idProduct");
			if(sysattr_val != NULL) {
				pid = 0x0000FFFF & (int)strtol(sysattr_val, &endptr, 16);
				if(pid != usbpid_to_match) {
					udev_device_unref(udev_device);
					continue;
				}
			}else {
				udev_device_unref(udev_device);
				continue;
			}

			/* match serial number if requested by application */
			if (serial != NULL) {
				sysattr_val = udev_device_get_sysattr_value(udev_device, "serial");
				if(sysattr_val != NULL) {
					if(strcasecmp(sysattr_val, serial) != 0) {
						udev_device_unref(udev_device);
						continue;
					}
				}else {
					udev_device_unref(udev_device);
					continue;
				}
			}

			/* reaching here means that this is the device whose firmware version application need to know. */
			prop_val = udev_device_get_property_value(udev_device, "ID_REVISION");
			memset(buffer, '\0', sizeof(buffer));
			snprintf(buffer, 128, "%d", (0x0000FFFF & (int)strtol(prop_val, &endptr, 16)));
			usb_dev_info = (*env)->NewStringUTF(env, buffer);
			if((usb_dev_info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
				free_jstrarraylist(&list);
				udev_device_unref(udev_device);
				udev_enumerate_unref(enumerator);
				udev_unref(udev_ctx);
				throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
				return NULL;
			}
			insert_jstrarraylist(&list, usb_dev_info);
		}
		udev_device_unref(udev_device);
	}

	/* Create a JAVA/JNI style array of String object, populate it and return to java layer. */
	strClass = (*env)->FindClass(env, JAVALSTRING);
	if((strClass == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		free_jstrarraylist(&list);
		udev_enumerate_unref(enumerator);
		udev_unref(udev_ctx);
		throw_serialcom_exception(env, 3, 0, E_FINDCLASSSSTRINGSTR);
		return NULL;
	}

	usbDevicesFwVerFound = (*env)->NewObjectArray(env, (jsize) list.index, strClass, NULL);
	if((usbDevicesFwVerFound == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		free_jstrarraylist(&list);
		udev_enumerate_unref(enumerator);
		udev_unref(udev_ctx);
		throw_serialcom_exception(env, 3, 0, E_NEWOBJECTARRAYSTR);
		return NULL;
		return linux_listusb_clean_throw_exp(env, 2, E_NEWOBJECTARRAYSTR, &list, NULL, NULL, NULL);
	}

	for (x=0; x < list.index; x++) {
		(*env)->SetObjectArrayElement(env, usbDevicesFwVerFound, x, list.base[x]);
		if((*env)->ExceptionOccurred(env)) {
			free_jstrarraylist(&list);
			udev_enumerate_unref(enumerator);
			udev_unref(udev_ctx);
			throw_serialcom_exception(env, 3, 0, E_SETOBJECTARRAYSTR);
			return NULL;
		}
	}

	free_jstrarraylist(&list);
	udev_enumerate_unref(enumerator);
	udev_unref(udev_ctx);
	return usbDevicesFwVerFound;
}
#endif

#if defined (__APPLE__)


#endif
