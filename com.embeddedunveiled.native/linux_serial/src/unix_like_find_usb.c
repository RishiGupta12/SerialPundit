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
#include <IOKit/serial/IOSerialKeys.h>
#include <IOKit/serial/ioss.h>
#include <IOKit/IOBSD.h>
#include <IOKit/IOMessage.h>
#include <IOKit/usb/IOUSBLib.h>
#endif
#include <jni.h>
#include "unix_like_serial_lib.h"

/*
 * Finds information about USB devices using operating system specific facilities and API.
 * The sequence of entries in array must match with what java layer expect. If a particular USB attribute
 * is not set in descriptor or can not be obtained "---" is placed in its place.
 */
jobjectArray list_usb_devices(JNIEnv *env, jobject obj, jobject status) {
	int x = 0;
	struct jstrarray_list list = {0};
	jstring usb_dev_info;
	jclass strClass = NULL;
	jobjectArray usbDevicesFound = NULL;

#if defined (__linux__)
	struct udev *udev_ctx;
	struct udev_enumerate *enumerator;
	struct udev_list_entry *devices, *dev_list_entry;
	const char *sysattr_val;
	const char *path;
	struct udev_device *udev_device;
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

	init_jstrarraylist(&list, 100);

#if defined (__linux__)
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
			sysattr_val = udev_device_get_sysattr_value(udev_device, "idVendor");
			if(sysattr_val != NULL) {
				usb_dev_info = (*env)->NewStringUTF(env, sysattr_val);
			}else {
				usb_dev_info = (*env)->NewStringUTF(env, "---");
			}
			insert_jstrarraylist(&list, usb_dev_info);

			sysattr_val = udev_device_get_sysattr_value(udev_device, "idProduct");
			if(sysattr_val != NULL) {
				usb_dev_info = (*env)->NewStringUTF(env, sysattr_val);
			}else {
				usb_dev_info = (*env)->NewStringUTF(env, "---");
			}
			insert_jstrarraylist(&list, usb_dev_info);

			sysattr_val = udev_device_get_sysattr_value(udev_device, "serial");
			if(sysattr_val != NULL) {
				usb_dev_info = (*env)->NewStringUTF(env, sysattr_val);
			}else {
				usb_dev_info = (*env)->NewStringUTF(env, "---");
			}
			insert_jstrarraylist(&list, usb_dev_info);

			sysattr_val = udev_device_get_sysattr_value(udev_device, "product");
			if(sysattr_val != NULL) {
				usb_dev_info = (*env)->NewStringUTF(env, sysattr_val);
			}else {
				usb_dev_info = (*env)->NewStringUTF(env, "---");
			}
			insert_jstrarraylist(&list, usb_dev_info);

			sysattr_val = udev_device_get_sysattr_value(udev_device, "manufacturer");
			if(sysattr_val != NULL) {
				usb_dev_info = (*env)->NewStringUTF(env, sysattr_val);
			}else {
				usb_dev_info = (*env)->NewStringUTF(env, "---");
			}
			insert_jstrarraylist(&list, usb_dev_info);
		}
		udev_device_unref(udev_device);
	}
	udev_enumerate_unref(enumerator);
	udev_unref(udev_ctx);

#endif
#if defined (__APPLE__)
	matching_dictionary = IOServiceMatching("IOUSBDevice");
	if(matching_dictionary == NULL) {
		/* handle error*/
	}
	kr = IOServiceGetMatchingServices(kIOMasterPortDefault, matching_dictionary, &iterator);
	if(kr != KERN_SUCCESS) {
		/* handle error*/
	}
	if(!iterator) {
		/* handle error*/
	}

	while((usb_dev_obj = IOIteratorNext(iterator)) != 0) {

		memset(hexcharbuffer, '\0', sizeof(hexcharbuffer));
		num_ref = (CFNumberRef) IORegistryEntrySearchCFProperty(usb_dev_obj, kIOServicePlane, CFSTR("idVendor"),
				                                        NULL, kIORegistryIterateRecursively | kIORegistryIterateParents);
		if(num_ref) {
			CFNumberGetValue(num_ref, kCFNumberSInt32Type, &result);
			snprintf(hexcharbuffer, 5, "%04X", result & 0x0000FFFF);
			usb_dev_info = (*env)->NewStringUTF(env, hexcharbuffer);
			CFRelease(num_ref);
		}else {
			usb_dev_info = (*env)->NewStringUTF(env, "---");
		}
		insert_jstrarraylist(&list, usb_dev_info);

		memset(hexcharbuffer, '\0', sizeof(hexcharbuffer));
		num_ref = (CFNumberRef) IORegistryEntrySearchCFProperty(usb_dev_obj, kIOServicePlane, CFSTR("idProduct"),
				                                        NULL, kIORegistryIterateRecursively | kIORegistryIterateParents);
		if(num_ref) {
			CFNumberGetValue(num_ref, kCFNumberSInt32Type, &result);
			snprintf(hexcharbuffer, 5, "%04X", result & 0x0000FFFF);
			usb_dev_info = (*env)->NewStringUTF(env, hexcharbuffer);
			CFRelease(num_ref);
		}else {
			usb_dev_info = (*env)->NewStringUTF(env, "---");
		}
		insert_jstrarraylist(&list, usb_dev_info);

		str_ref = (CFStringRef) IORegistryEntrySearchCFProperty(usb_dev_obj, kIOServicePlane, CFSTR("USB Serial Number"),
				                                        NULL, kIORegistryIterateRecursively | kIORegistryIterateParents);
		if(str_ref) {
			memset(charbuffer, '\0', sizeof(charbuffer));
			CFStringGetCString(str_ref, charbuffer, sizeof(charbuffer), kCFStringEncodingUTF8);
			usb_dev_info = (*env)->NewStringUTF(env, charbuffer);
			CFRelease(str_ref);
		}else {
			usb_dev_info = (*env)->NewStringUTF(env, "---");
		}
		insert_jstrarraylist(&list, usb_dev_info);

		str_ref = (CFStringRef) IORegistryEntrySearchCFProperty(usb_dev_obj, kIOServicePlane, CFSTR("USB Product Name"),
				                                        NULL, kIORegistryIterateRecursively | kIORegistryIterateParents);
		if(str_ref) {
			memset(charbuffer, '\0', sizeof(charbuffer));
			CFStringGetCString(str_ref, charbuffer, sizeof(charbuffer), kCFStringEncodingUTF8);
			usb_dev_info = (*env)->NewStringUTF(env, charbuffer);
			CFRelease(str_ref);
		}else {
			usb_dev_info = (*env)->NewStringUTF(env, "---");
		}
		insert_jstrarraylist(&list, usb_dev_info);

		str_ref = (CFStringRef) IORegistryEntrySearchCFProperty(usb_dev_obj, kIOServicePlane, CFSTR("USB Vendor Name"),
				                                        NULL, kIORegistryIterateRecursively | kIORegistryIterateParents);
		if(str_ref) {
			memset(charbuffer, '\0', sizeof(charbuffer));
			CFStringGetCString(str_ref, charbuffer, sizeof(charbuffer), kCFStringEncodingUTF8);
			usb_dev_info = (*env)->NewStringUTF(env, charbuffer);
			CFRelease(str_ref);
		}else {
			usb_dev_info = (*env)->NewStringUTF(env, "---");
		}
		insert_jstrarraylist(&list, usb_dev_info);

		IOObjectRelease(usb_dev_obj);
	}

	IOObjectRelease(iterator);

#endif

	/* Create a JAVA/JNI style array of String object, populate it and return to java layer. */
	strClass = (*env)->FindClass(env, "java/lang/String");
	if((*env)->ExceptionOccurred(env)) {
		(*env)->ExceptionClear(env);
		set_error_status(env, obj, status, E_FINDCLASS);
		free_jstrarraylist(&list);
		return NULL;
	}

	usbDevicesFound = (*env)->NewObjectArray(env, (jsize) list.index, strClass, NULL);
	if((*env)->ExceptionOccurred(env)) {
		(*env)->ExceptionClear(env);
		set_error_status(env, obj, status, E_NEWOBJECTARRAY);
		free_jstrarraylist(&list);
		return NULL;
	}

	for (x=0; x < list.index; x++) {
		(*env)->SetObjectArrayElement(env, usbDevicesFound, x, list.base[x]);
		if((*env)->ExceptionOccurred(env)) {
			(*env)->ExceptionClear(env);
			free_jstrarraylist(&list);
			return NULL;
		}
	}

	free_jstrarraylist(&list);
	return usbDevicesFound;
}
