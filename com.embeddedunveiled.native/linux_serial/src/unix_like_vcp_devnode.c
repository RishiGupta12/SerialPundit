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
#include <sys/types.h>
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
#include <jni.h>
#include "unix_like_serial_lib.h"

/*
 * Find device nodes (like COMxx, ttyUSBxx etc) assigned by operating system to the USB-UART bridge/converter(s)
 * from the USB device attributes.
 *
 * The USB strings are Unicode, UCS2 encoded, but the strings returned from udev_device_get_sysattr_value() are UTF-8 encoded.
 * GetStringUTFChars() returns in modified UTF-8 encoding.
 */
jobjectArray vcp_node_from_usb_attributes(JNIEnv *env, jobject obj, jint usbvid_to_match, jint usbpid_to_match, jstring serial_num) {
	int x = 0;
	struct jstrarray_list list = {0};
	jclass strClass = NULL;
	jobjectArray vcpPortsFound = NULL;
	const char* serial_to_match = NULL;
	jstring vcp_node;

#if defined (__linux__)
	struct udev *udev_ctx;
	struct udev_enumerate *enumerator;
	struct udev_list_entry *devices, *dev_list_entry;
	const char *prop_val;
	const char *path;
	struct udev_device *udev_device;
	int usb_vid;
	int usb_pid;
	char *endptr;
	int matched = 0;
#endif
#if defined (__APPLE__)
	kern_return_t kr;
	CFDictionaryRef matching_dictionary = NULL;
	io_iterator_t iterator = 0;
	io_service_t usb_dev_obj;
	CFNumberRef num_ref;
	CFStringRef str_ref;
	int result;
	char hexcharbuffer[5];

	/* For storing USB descriptor attributes string like manufacturer, product, serial number etc.
	 * in any encoding 1024 is sufficient. We prevented malloc() every time for every new attribute. */
	char charbuffer[1024];
#endif

	init_jstrarraylist(&list, 50);
	if(serial_num != NULL) {
		serial_to_match = (*env)->GetStringUTFChars(env, serial_num, NULL);
		if((serial_to_match == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 3, 0, E_GETSTRUTFCHARSTR);
			return NULL;
		}
	}

#if defined (__linux__)
	udev_ctx = udev_new();
	enumerator = udev_enumerate_new(udev_ctx);
	udev_enumerate_add_match_subsystem(enumerator, "tty");
	udev_enumerate_scan_devices(enumerator);
	devices = udev_enumerate_get_list_entry(enumerator);

	udev_list_entry_foreach(dev_list_entry, devices) {
		path = udev_list_entry_get_name(dev_list_entry);
		udev_device = udev_device_new_from_syspath(udev_enumerate_get_udev(enumerator), path);
		if(udev_device == NULL) {
			continue;
		}

		matched = 0;
		prop_val = udev_device_get_property_value(udev_device, "ID_VENDOR_ID");
		if(prop_val != NULL) {
			usb_vid = 0x0000FFFF & (int) strtol(prop_val, &endptr, 16);
			if(usb_vid == usbvid_to_match) {
				matched = 1;
			}
		}
		if(matched == 0) {
			udev_device_unref(udev_device);
			continue;
		}

		matched = 0;
		prop_val = udev_device_get_property_value(udev_device, "ID_MODEL_ID");
		if(prop_val != NULL) {
			usb_pid = 0x0000FFFF & (int) strtol(prop_val, &endptr, 16);
			if(usb_pid == usbpid_to_match) {
				matched = 1;
			}
		}
		if(matched == 0) {
			udev_device_unref(udev_device);
			continue;
		}

		if(serial_to_match != NULL) {
			matched = 0;
			prop_val = udev_device_get_property_value(udev_device, "ID_SERIAL_SHORT");
			if(prop_val != NULL) {
				if(!strcasecmp(prop_val, serial_to_match)) {
					matched = 1;
				}
			}
		}

		if(matched == 1) {
			/* this device met all criteria, get dev node for this */
			prop_val = udev_device_get_property_value(udev_device, "DEVNAME");
			if(prop_val != NULL) {
				vcp_node = (*env)->NewStringUTF(env, prop_val);
				if(vcp_node != NULL) {
					insert_jstrarraylist(&list, vcp_node);
				}
			}
		}

		udev_device_unref(udev_device);
	}
	udev_enumerate_unref(enumerator);
	udev_unref(udev_ctx);
	(*env)->ReleaseStringUTFChars(env, serial_num, serial_to_match);
#endif

#if defined (__APPLE__)
	/* TODO */
#endif

	if(matched == 1) {
		/* Matching node found, create a JAVA/JNI style array of String object,
		 * populate it and return to java layer. */
		strClass = (*env)->FindClass(env, JAVALSTRING);
		if((strClass == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			(*env)->ExceptionClear(env);
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 3, 0, E_FINDCLASSSSTRINGSTR);
			return NULL;
		}

		vcpPortsFound = (*env)->NewObjectArray(env, (jsize) list.index, strClass, NULL);
		if((vcpPortsFound == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			(*env)->ExceptionClear(env);
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 3, 0, E_NEWOBJECTARRAYSTR);
			return NULL;
		}

		for (x=0; x < list.index; x++) {
			(*env)->SetObjectArrayElement(env, vcpPortsFound, x, list.base[x]);
			if((*env)->ExceptionOccurred(env)) {
				(*env)->ExceptionClear(env);
				free_jstrarraylist(&list);
				throw_serialcom_exception(env, 3, 0, E_SETOBJECTARRAYSTR);
				return NULL;
			}
		}

		free_jstrarraylist(&list);
		return vcpPortsFound;
	}

	return NULL;
}
