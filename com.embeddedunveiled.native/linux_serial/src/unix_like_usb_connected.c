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
 *
 * Returns 1 if device is connected, returns 0 if not connected and -1 if error occurs.
 */
jint is_usb_dev_connected(JNIEnv *env, jobject obj, jint usbvid_to_match, jint usbpid_to_match, jobject status) {

#if defined (__linux__)
	int vid = 0;
	int pid = 0;
	struct udev *udev_ctx;
	struct udev_enumerate *enumerator;
	struct udev_list_entry *devices, *dev_list_entry;
	const char *sysattr_val;
	const char *path;
	struct udev_device *udev_device;
	char *endptr;
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

#if defined (__linux__)
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
			sysattr_val = udev_device_get_sysattr_value(udev_device, "idVendor");
			if(sysattr_val != NULL) {
				vid = 0x0000FFFF & (int)strtol(sysattr_val, &endptr, 16);
				if(vid != usbvid_to_match) {
					udev_device_unref(udev_device);
					continue;
				}
			}
			sysattr_val = udev_device_get_sysattr_value(udev_device, "idProduct");
			if(sysattr_val != NULL) {
				pid = 0x0000FFFF & (int)strtol(sysattr_val, &endptr, 16);
				if(pid != usbpid_to_match) {
					udev_device_unref(udev_device);
					continue;
				}
			}

			/* reaching here means match occurred. */
			udev_device_unref(udev_device);
			udev_enumerate_unref(enumerator);
			udev_unref(udev_ctx);
			return 1;
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
			if(vendor_to_match != 0) {
				/* we need to apply filter for identify specific vendor */
				if(vendor_to_match != (result & 0x0000FFFF)) {
					CFRelease(num_ref);
					IOObjectRelease(usb_dev_obj);
					continue;
				}
			}
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

	return 0;
}
