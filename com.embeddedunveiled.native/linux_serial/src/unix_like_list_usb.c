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

#if defined (__linux__)
/*
 * Cleans up resources and set exception that will get thrown upon return to java layer.
 */
jstring linux_listusb_clean_throw_exp(JNIEnv *env, int task, const char *expmsg,
		struct jstrarray_list *list, struct udev_device *udev_device,
		struct udev_enumerate *enumerator, struct udev *udev_ctx) {

	(*env)->ExceptionClear(env);
	free_jstrarraylist(list);

	/* free memory first, so even if throwing JNI exception fails, this succeeds. */
	if(task == 1) {
		udev_device_unref(udev_device);
		udev_enumerate_unref(enumerator);
		udev_unref(udev_ctx);
	}else {
	}

	jclass serialComExceptionClass = (*env)->FindClass(env, SCOMEXPCLASS);
	if((serialComExceptionClass == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		(*env)->ExceptionClear(env);
		if(task == 1) {
			LOGE(E_FINDCLASSSCOMEXPSTR, E_NEWSTRUTFSTR);
		}else {
			LOGE(E_FINDCLASSSCOMEXPSTR, expmsg);
		}
		return NULL;
	}

	if(task == 1) {
		(*env)->ThrowNew(env, serialComExceptionClass, E_NEWSTRUTFSTR);
	}else {
		(*env)->ThrowNew(env, serialComExceptionClass, expmsg);
	}

	return NULL;
}

/*
 * Finds information about USB devices using operating system specific facilities and API.
 * The sequence of entries in array must match with what java layer expect (6 informations
 * per USB device). If a particular USB attribute is not set in descriptor or can not be
 * obtained "---" is placed in its place.
 *
 * Return array of USB device's information found, empty array if no USB device is found,
 * NULL if an error occurs (additionally throws exception).
 */
jobjectArray list_usb_devices(JNIEnv *env, jint vendor_to_match) {
	int x = 0;
	struct jstrarray_list list = {0};
	jstring usb_dev_info;
	jclass strClass = NULL;
	jobjectArray usbDevicesFound = NULL;

	struct udev *udev_ctx;
	struct udev_enumerate *enumerator;
	struct udev_list_entry *devices, *dev_list_entry;
	const char *sysattr_val;
	const char *prop_val;
	const char *path;
	struct udev_device *udev_device;
	char *endptr;

	init_jstrarraylist(&list, 100);

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

			/* In context of this library, application is not interested in USB hub and USB
			 * host controllers. Skip then from listing. */
			sysattr_val = udev_device_get_sysattr_value(udev_device, "bDeviceClass");
			if(sysattr_val != NULL) {
				if(0x09 == strtol(sysattr_val, &endptr, 16)) {
					udev_device_unref(udev_device);
					continue;
				}
			}

			/* USB-IF vendor ID */
			sysattr_val = udev_device_get_sysattr_value(udev_device, "idVendor");
			if(sysattr_val != NULL) {
				if(vendor_to_match != 0) {
					/* we need to apply filter for identify specific vendor */
					if(vendor_to_match != (0x0000FFFF & (int)strtol(sysattr_val, &endptr, 16))) {
						udev_device_unref(udev_device);
						continue;
					}
				}
				usb_dev_info = (*env)->NewStringUTF(env, sysattr_val);
			}else {
				usb_dev_info = (*env)->NewStringUTF(env, "---");
			}
			if((usb_dev_info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
				return linux_listusb_clean_throw_exp(env, 1, NULL, &list, udev_device, enumerator, udev_ctx);
			}
			insert_jstrarraylist(&list, usb_dev_info);

			/* USB product ID */
			sysattr_val = udev_device_get_sysattr_value(udev_device, "idProduct");
			if(sysattr_val != NULL) {
				usb_dev_info = (*env)->NewStringUTF(env, sysattr_val);
			}else {
				usb_dev_info = (*env)->NewStringUTF(env, "---");
			}
			if((usb_dev_info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
				return linux_listusb_clean_throw_exp(env, 1, NULL, &list, udev_device, enumerator, udev_ctx);
			}
			insert_jstrarraylist(&list, usb_dev_info);

			/* SERIAL NUMBER */
			sysattr_val = udev_device_get_sysattr_value(udev_device, "serial");
			if(sysattr_val != NULL) {
				usb_dev_info = (*env)->NewStringUTF(env, sysattr_val);
			}else {
				usb_dev_info = (*env)->NewStringUTF(env, "---");
			}
			if((usb_dev_info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
				return linux_listusb_clean_throw_exp(env, 1, NULL, &list, udev_device, enumerator, udev_ctx);
			}
			insert_jstrarraylist(&list, usb_dev_info);

			/* PRODUCT */
			sysattr_val = udev_device_get_sysattr_value(udev_device, "product");
			if(sysattr_val != NULL) {
				usb_dev_info = (*env)->NewStringUTF(env, sysattr_val);
			}else {
				usb_dev_info = (*env)->NewStringUTF(env, "---");
			}
			if((usb_dev_info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
				return linux_listusb_clean_throw_exp(env, 1, NULL, &list, udev_device, enumerator, udev_ctx);
			}
			insert_jstrarraylist(&list, usb_dev_info);

			/* MANUFACTURER */
			sysattr_val = udev_device_get_sysattr_value(udev_device, "manufacturer");
			if(sysattr_val != NULL) {
				usb_dev_info = (*env)->NewStringUTF(env, sysattr_val);
			}else {
				usb_dev_info = (*env)->NewStringUTF(env, "---");
			}
			if((usb_dev_info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
				return linux_listusb_clean_throw_exp(env, 1, NULL, &list, udev_device, enumerator, udev_ctx);
			}
			insert_jstrarraylist(&list, usb_dev_info);

			/* LOCATION */
			prop_val = udev_device_get_property_value(udev_device, "DEVPATH");
			if(prop_val != NULL) {
				usb_dev_info = (*env)->NewStringUTF(env, prop_val);
			}else {
				usb_dev_info = (*env)->NewStringUTF(env, "---");
			}
			if((usb_dev_info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
				return linux_listusb_clean_throw_exp(env, 1, NULL, &list, udev_device, enumerator, udev_ctx);
			}
			insert_jstrarraylist(&list, usb_dev_info);

		}
		udev_device_unref(udev_device);
	}
	udev_enumerate_unref(enumerator);
	udev_unref(udev_ctx);

	/* Create a JAVA/JNI style array of String object, populate it and return to java layer. */
	strClass = (*env)->FindClass(env, JAVALSTRING);
	if((strClass == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		return linux_listusb_clean_throw_exp(env, 2, E_FINDCLASSSSTRINGSTR, &list, NULL, NULL, NULL);
	}

	usbDevicesFound = (*env)->NewObjectArray(env, (jsize) list.index, strClass, NULL);
	if((usbDevicesFound == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		return linux_listusb_clean_throw_exp(env, 2, E_NEWOBJECTARRAYSTR, &list, NULL, NULL, NULL);
	}

	for (x=0; x < list.index; x++) {
		(*env)->SetObjectArrayElement(env, usbDevicesFound, x, list.base[x]);
		if((*env)->ExceptionOccurred(env)) {
			return linux_listusb_clean_throw_exp(env, 2, E_SETOBJECTARRAYSTR, &list, NULL, NULL, NULL);
		}
	}

	free_jstrarraylist(&list);
	return usbDevicesFound;
}
#endif

#if defined (__APPLE__)
/*
 * Cleans up resources and set exception that will get thrown upon return to java layer.
 */
jstring mac_listusb_clean_throw_exp(JNIEnv *env, int task, const char *expmsg, struct jstrarray_list *list,
		io_service_t usb_dev_obj, io_iterator_t iterator) {

	/* free memory first, so even if throwing JNI exception fails, this succeeds. */
	if(task == 1) {
		IOObjectRelease(usb_dev_obj);
		IOObjectRelease(iterator);
	}else {
	}

	jclass serialComExceptionClass = (*env)->FindClass(env, SCOMEXPCLASS);
	if((serialComExceptionClass == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		(*env)->ExceptionClear(env);
		if(task == 1) {
			LOGE(E_FINDCLASSSCOMEXPSTR, E_NEWSTRUTFSTR);
		}else {
			LOGE(E_FINDCLASSSCOMEXPSTR, expmsg);
		}
		return NULL;
	}

	if(task == 1) {
		(*env)->ThrowNew(env, serialComExceptionClass, E_NEWSTRUTFSTR);
	}else {
		(*env)->ThrowNew(env, serialComExceptionClass, expmsg);
	}

	return NULL;
}

/*
 * Finds information about USB devices using operating system specific facilities and API.
 * The sequence of entries in array must match with what java layer expect (6 informations
 * per USB device). If a particular USB attribute is not set in descriptor or can not be
 * obtained "---" is placed in its place.
 *
 * Return array of USB device's information found, empty array if no USB device is found,
 * NULL if an error occurs (additionally throws exception).
 */
jobjectArray list_usb_devices(JNIEnv *env, jint vendor_to_match) {
	int x = 0;
	struct jstrarray_list list = {0};
	jstring usb_dev_info;
	jclass strClass = NULL;
	jobjectArray usbDevicesFound = NULL;

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

	init_jstrarraylist(&list, 100);

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

		/* USB-IF vendor ID */
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
			CFRelease(num_ref);
			snprintf(hexcharbuffer, 5, "%04X", result & 0x0000FFFF);
			usb_dev_info = (*env)->NewStringUTF(env, hexcharbuffer);
		}else {
			usb_dev_info = (*env)->NewStringUTF(env, "---");
		}
		if((usb_dev_info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			return mac_listusb_clean_throw_exp(env, 1, NULL, &list, usb_dev_obj, iterator);
		}
		insert_jstrarraylist(&list, usb_dev_info);

		/* USB product ID */
		memset(hexcharbuffer, '\0', sizeof(hexcharbuffer));
		num_ref = (CFNumberRef) IORegistryEntrySearchCFProperty(usb_dev_obj, kIOServicePlane, CFSTR("idProduct"),
				                                        NULL, kIORegistryIterateRecursively | kIORegistryIterateParents);
		if(num_ref) {
			CFNumberGetValue(num_ref, kCFNumberSInt32Type, &result);
			CFRelease(num_ref);
			snprintf(hexcharbuffer, 5, "%04X", result & 0x0000FFFF);
			usb_dev_info = (*env)->NewStringUTF(env, hexcharbuffer);
		}else {
			usb_dev_info = (*env)->NewStringUTF(env, "---");
		}
		if((usb_dev_info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			return mac_listusb_clean_throw_exp(env, 1, NULL, &list, usb_dev_obj, iterator);
		}
		insert_jstrarraylist(&list, usb_dev_info);

		/* SERIAL NUMBER */
		str_ref = (CFStringRef) IORegistryEntrySearchCFProperty(usb_dev_obj, kIOServicePlane, CFSTR("USB Serial Number"),
				                                        NULL, kIORegistryIterateRecursively | kIORegistryIterateParents);
		if(str_ref) {
			memset(charbuffer, '\0', sizeof(charbuffer));
			CFStringGetCString(str_ref, charbuffer, sizeof(charbuffer), kCFStringEncodingUTF8);
			CFRelease(str_ref);
			usb_dev_info = (*env)->NewStringUTF(env, charbuffer);
		}else {
			usb_dev_info = (*env)->NewStringUTF(env, "---");
		}
		if((usb_dev_info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			return mac_listusb_clean_throw_exp(env, 1, NULL, &list, usb_dev_obj, iterator);
		}
		insert_jstrarraylist(&list, usb_dev_info);

		/* PRODUCT */
		str_ref = (CFStringRef) IORegistryEntrySearchCFProperty(usb_dev_obj, kIOServicePlane, CFSTR("USB Product Name"),
				                                        NULL, kIORegistryIterateRecursively | kIORegistryIterateParents);
		if(str_ref) {
			memset(charbuffer, '\0', sizeof(charbuffer));
			CFStringGetCString(str_ref, charbuffer, sizeof(charbuffer), kCFStringEncodingUTF8);
			CFRelease(str_ref);
			usb_dev_info = (*env)->NewStringUTF(env, charbuffer);
		}else {
			usb_dev_info = (*env)->NewStringUTF(env, "---");
		}
		if((usb_dev_info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			return mac_listusb_clean_throw_exp(env, 1, NULL, &list, usb_dev_obj, iterator);
		}
		insert_jstrarraylist(&list, usb_dev_info);

		/* MANUFACTURER */
		str_ref = (CFStringRef) IORegistryEntrySearchCFProperty(usb_dev_obj, kIOServicePlane, CFSTR("USB Vendor Name"),
				                                        NULL, kIORegistryIterateRecursively | kIORegistryIterateParents);
		if(str_ref) {
			memset(charbuffer, '\0', sizeof(charbuffer));
			CFStringGetCString(str_ref, charbuffer, sizeof(charbuffer), kCFStringEncodingUTF8);
			CFRelease(str_ref);
			usb_dev_info = (*env)->NewStringUTF(env, charbuffer);
		}else {
			usb_dev_info = (*env)->NewStringUTF(env, "---");
		}
		if((usb_dev_info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			return mac_listusb_clean_throw_exp(env, 1, NULL, &list, usb_dev_obj, iterator);
		}
		insert_jstrarraylist(&list, usb_dev_info);

		/* LOCATION TODO */
		str_ref = (CFStringRef) IORegistryEntrySearchCFProperty(usb_dev_obj, kIOServicePlane, CFSTR("USB Vendor Name"),
				                                        NULL, kIORegistryIterateRecursively | kIORegistryIterateParents);
		if(str_ref) {
			memset(charbuffer, '\0', sizeof(charbuffer));
			CFStringGetCString(str_ref, charbuffer, sizeof(charbuffer), kCFStringEncodingUTF8);
			CFRelease(str_ref);
			usb_dev_info = (*env)->NewStringUTF(env, charbuffer);
		}else {
			usb_dev_info = (*env)->NewStringUTF(env, "---");
		}
		if((usb_dev_info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			return mac_listusb_clean_throw_exp(env, 1, NULL, &list, usb_dev_obj, iterator);
		}
		insert_jstrarraylist(&list, usb_dev_info);

		IOObjectRelease(usb_dev_obj);
	}

	IOObjectRelease(iterator);

	/* Create a JAVA/JNI style array of String object, populate it and return to java layer. */
	strClass = (*env)->FindClass(env, JAVALSTRING);
	if((strClass == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		return mac_listusb_clean_throw_exp(env, 2, E_FINDCLASSSSTRINGSTR, &list, NULL, NULL);
	}

	usbDevicesFound = (*env)->NewObjectArray(env, (jsize) list.index, strClass, NULL);
	if((usbDevicesFound == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		return mac_listusb_clean_throw_exp(env, 2, E_NEWOBJECTARRAYSTR, &list, NULL, NULL);
	}

	for (x=0; x < list.index; x++) {
		(*env)->SetObjectArrayElement(env, usbDevicesFound, x, list.base[x]);
		if((*env)->ExceptionOccurred(env)) {
			return mac_listusb_clean_throw_exp(env, 2, E_SETOBJECTARRAYSTR, &list, NULL, NULL);
		}
	}

	free_jstrarraylist(&list);
	return usbDevicesFound;
}
#endif
