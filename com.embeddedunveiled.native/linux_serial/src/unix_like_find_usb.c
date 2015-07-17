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

jobjectArray list_usb_devices(JNIEnv *env, jobject obj, jobject status) {
	int x = 0;
	struct array_list list = {0};
	char *usb_dev_info;
	jclass strClass = NULL;
	jobjectArray usbDevicesFound = NULL;

#if defined (__linux__)
	struct udev *udev_ctx;
	struct udev_enumerate *enumerator;
	struct udev_list_entry *devices, *dev_list_entry;
	const char *sysattr_val;
#endif
#if defined (__APPLE__)
	kern_return_t kr;
	CFDictionaryRef matching_dictionary = NULL;
	io_iterator_t iterator = 0;
	io_service_t usb_dev_obj;
	int result;
	char hexcharbuffer[5];
	size_t size_to_allocate;
#endif

	init_array_list(&list, 100);

#if defined (__linux__)
	udev_ctx = udev_new();
	enumerator = udev_enumerate_new(udev_ctx);
	udev_enumerate_add_match_subsystem(enumerator, "usb");
	udev_enumerate_scan_devices(enumerator);
	devices = udev_enumerate_get_list_entry(enumerator);

	udev_list_entry_foreach(dev_list_entry, devices) {
		const char *path;
		struct udev_device *udev_device;
		path = udev_list_entry_get_name(dev_list_entry);
		udev_device = udev_device_new_from_syspath(udev_enumerate_get_udev(enumerator), path);
		if(udev_device == NULL) {
			continue;
		}

		if(strcmp("usb_device", udev_device_get_devtype(udev_device)) == 0) {
			sysattr_val = udev_device_get_sysattr_value(udev_device, "idVendor");
			if(sysattr_val != NULL) {
				usb_dev_info = strdup(sysattr_val);
			}else {
				usb_dev_info = strdup("---");
			}
			insert_array_list(&list, usb_dev_info);

			sysattr_val = udev_device_get_sysattr_value(udev_device, "idProduct");
			if(sysattr_val != NULL) {
				usb_dev_info = strdup(sysattr_val);
			}else {
				usb_dev_info = strdup("---");
			}
			insert_array_list(&list, usb_dev_info);

			sysattr_val = udev_device_get_sysattr_value(udev_device, "serial");
			if(sysattr_val != NULL) {
				usb_dev_info = strdup(sysattr_val);
			}else {
				usb_dev_info = strdup("---");
			}
			insert_array_list(&list, usb_dev_info);

			sysattr_val = udev_device_get_sysattr_value(udev_device, "product");
			if(sysattr_val != NULL) {
				usb_dev_info = strdup(sysattr_val);
			}else {
				usb_dev_info = strdup("---");
			}
			insert_array_list(&list, usb_dev_info);

			sysattr_val = udev_device_get_sysattr_value(udev_device, "manufacturer");
			if(sysattr_val != NULL) {
				usb_dev_info = strdup(sysattr_val);
			}else {
				usb_dev_info = strdup("---");
			}
			insert_array_list(&list, usb_dev_info);
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
		fprintf(stderr,"No devices matched\n");
		fflush(stderr);
		return NULL;
	}

	while((usb_dev_obj = IOIteratorNext(iterator)) != 0) {

		memset(hexcharbuffer, '\0', sizeof(hexcharbuffer));
		CFNumberRef vid_num_ref = (CFNumberRef) IORegistryEntrySearchCFProperty(usb_dev_obj, kIOServicePlane, CFSTR("idVendor"),
				                                        NULL, kIORegistryIterateRecursively | kIORegistryIterateParents);
		if(vid_num_ref) {
			CFNumberGetValue(vid_num_ref, kCFNumberSInt32Type, &result);
			snprintf(hexcharbuffer, 5, "%04X", result & 0x0000FFFF);
			usb_dev_info = strdup(hexcharbuffer);
			fflush(stderr);
			CFRelease(vid_num_ref);
			fflush(stderr);
		}else {
			usb_dev_info = strdup("---");
		}
		insert_array_list(&list, usb_dev_info);

		memset(hexcharbuffer, '\0', sizeof(hexcharbuffer));
		CFNumberRef pid_num_ref = (CFNumberRef) IORegistryEntrySearchCFProperty(usb_dev_obj, kIOServicePlane, CFSTR("idProduct"),
				                                        NULL, kIORegistryIterateRecursively | kIORegistryIterateParents);
		if(pid_num_ref) {
			CFNumberGetValue(pid_num_ref, kCFNumberSInt32Type, &result);
			snprintf(hexcharbuffer, 5, "%04X", result & 0x0000FFFF);
			usb_dev_info = strdup(hexcharbuffer);
			CFRelease(pid_num_ref);
		}else {
			usb_dev_info = strdup("---");
		}
		insert_array_list(&list, usb_dev_info);

		CFStringRef serial_str_ref = (CFStringRef) IORegistryEntrySearchCFProperty(usb_dev_obj, kIOServicePlane, CFSTR("USB Serial Number"),
				                                        NULL, kIORegistryIterateRecursively | kIORegistryIterateParents);
		if(serial_str_ref) {
			size_to_allocate = CFStringGetMaximumSizeForEncoding(CFStringGetLength(serial_str_ref), kCFStringEncodingUTF8);
			usb_dev_info = (char *) malloc(size_to_allocate);
			CFStringGetCString(serial_str_ref, usb_dev_info, size_to_allocate, kCFStringEncodingUTF8);
			CFRelease(serial_str_ref);
		}else {
			usb_dev_info = strdup("---");
		}
		insert_array_list(&list, usb_dev_info);

		CFStringRef product_str_ref = (CFStringRef) IORegistryEntrySearchCFProperty(usb_dev_obj, kIOServicePlane, CFSTR("USB Product Name"),
				                                        NULL, kIORegistryIterateRecursively | kIORegistryIterateParents);
		if(product_str_ref) {
			size_to_allocate = CFStringGetMaximumSizeForEncoding(CFStringGetLength(product_str_ref), kCFStringEncodingUTF8);
			usb_dev_info = (char *) malloc(size_to_allocate);
			CFStringGetCString(product_str_ref, usb_dev_info, size_to_allocate, kCFStringEncodingUTF8);
			CFRelease(product_str_ref);
		}else {
			usb_dev_info = strdup("---");
		}
		insert_array_list(&list, usb_dev_info);

		CFStringRef manufacturer_str_ref = (CFStringRef) IORegistryEntrySearchCFProperty(usb_dev_obj, kIOServicePlane, CFSTR("USB Vendor Name"),
				                                        NULL, kIORegistryIterateRecursively | kIORegistryIterateParents);
		if(manufacturer_str_ref) {
			size_to_allocate = CFStringGetMaximumSizeForEncoding(CFStringGetLength(manufacturer_str_ref), kCFStringEncodingUTF8);
			usb_dev_info = (char *) malloc(size_to_allocate);
			CFStringGetCString(manufacturer_str_ref, usb_dev_info, size_to_allocate, kCFStringEncodingUTF8);
			CFRelease(manufacturer_str_ref);
		}else {
			usb_dev_info = strdup("---");
		}
		insert_array_list(&list, usb_dev_info);

		IOObjectRelease(usb_dev_obj);
	}

	IOObjectRelease(iterator);

#endif

	/* Create a JAVA/JNI style array of String object, populate it and return to java layer. */
	strClass = (*env)->FindClass(env, "java/lang/String");
	if((*env)->ExceptionOccurred(env)) {
		(*env)->ExceptionClear(env);
		set_error_status(env, obj, status, E_FINDCLASS);
		free_array_list(&list);
		return NULL;
	}

	usbDevicesFound = (*env)->NewObjectArray(env, (jsize) list.index, strClass, NULL);
	if((*env)->ExceptionOccurred(env)) {
		(*env)->ExceptionClear(env);
		set_error_status(env, obj, status, E_NEWOBJECTARRAY);
		free_array_list(&list);
		return NULL;
	}

	for (x=0; x < list.index; x++) {
		(*env)->SetObjectArrayElement(env, usbDevicesFound, x, (*env)->NewStringUTF(env, list.base[x]));
		if((*env)->ExceptionOccurred(env)) {
			(*env)->ExceptionClear(env);
			free_array_list(&list);
			return NULL;
		}
	}

	free_array_list(&list);
	return usbDevicesFound;
}
