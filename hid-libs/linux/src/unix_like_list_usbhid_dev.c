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
<<<<<<< HEAD
#if defined (__linux__)
#include <libudev.h>
#endif
#if defined (__APPLE__)
#endif
#include <jni.h>
#include "unix_like_hid.h"

/*
 * Cleans up resources and set exception that will get thrown upon return to java layer.
 */
#if defined (__linux__)
jstring linux_clean_up_and_throw_exp(JNIEnv *env, int task, const char *expmsg,
		struct jstrarray_list *list, struct udev_device *udev_device,
		struct udev_enumerate *enumerator, struct udev *udev_ctx) {
#endif
#if defined (__APPLE__)
	jstring mac_clean_up_and_throw_exp(JNIEnv *env, int task, const char *expmsg,
			struct jstrarray_list *list, io_service_t usb_dev_obj, io_iterator_t iterator) {
#endif

		(*env)->ExceptionClear(env);
		free_jstrarraylist(list);

		/* free memory first, so even if throwing JNI exception fails, this succeeds. */
		if(task == 1) {
#if defined (__linux__)
			udev_device_unref(udev_device);
			udev_enumerate_unref(enumerator);
			udev_unref(udev_ctx);
#endif
#if defined (__APPLE__)
#endif
		}else if(task == 2) {
#if defined (__linux__)
			udev_device_unref(udev_device);
			udev_enumerate_unref(enumerator);
			udev_unref(udev_ctx);
#endif
#if defined (__APPLE__)
#endif
		}else {
		}

		jclass serialComExceptionClass = (*env)->FindClass(env, SCOMEXPCLASS);
		if((serialComExceptionClass == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			(*env)->ExceptionClear(env);
			LOGE(E_FINDCLASSSCOMEXPSTR);
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
	 * Finds information about USB HID devices using operating system specific facilities and API.
	 * The sequence of entries in array must match with what java layer expect. If a particular USB
	 * attribute is not set in descriptor or can not be obtained "---" is placed in its place.
	 *
	 * The array returned will be in following sequence; transport, device node, vendor ID,
	 * product ID, serial, product and manufacturer.
	 */
	jobjectArray list_usb_hid_devices(JNIEnv *env, jint vendor_to_match) {
		int x = 0;
		struct jstrarray_list list = {0};
		jstring vendor_id_info;
		jstring usb_dev_info;
		jclass strClass = NULL;
		jobjectArray usbHidDevicesFound = NULL;

#if defined (__linux__)
		struct udev *udev_ctx;
		struct udev_enumerate *enumerator;
		struct udev_list_entry *devices, *dev_list_entry;
		const char *sysattr_val;
		const char *path;
		struct udev_device *udev_device;
		const char *device_node;
		char *endptr;
#endif
#if defined (__APPLE__)
#endif

		init_jstrarraylist(&list, 100);

#if defined (__linux__)
		/* libudev is reference counted. Memory is freed when counts reach to zero. */
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

			/* save the device node */
			device_node = udev_device_get_devnode(udev_device);
			if(device_node == NULL) {
				return linux_clean_up_and_throw_exp(env, 2, E_CANNOTFINDDEVNODE, &list, udev_device, enumerator, udev_ctx);
			}

			udev_device = udev_device_get_parent_with_subsystem_devtype(udev_device, "usb", "usb_device");
			if(udev_device == NULL) {
				return linux_clean_up_and_throw_exp(env, 2, E_CANNOTFINDPARENTUDEV, &list, udev_device, enumerator, udev_ctx);
			}

			sysattr_val = udev_device_get_sysattr_value(udev_device, "idVendor");
			if(sysattr_val != NULL) {
				if(vendor_to_match != 0) {
					/* we need to apply filter for identify specific vendor */
					if(vendor_to_match != (0x0000FFFF & (int)strtol(sysattr_val, &endptr, 16))) {
						udev_device_unref(udev_device);
						continue;
					}
				}
				vendor_id_info = (*env)->NewStringUTF(env, sysattr_val);
			}else {
				vendor_id_info = (*env)->NewStringUTF(env, "---");
			}
			if((vendor_id_info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
				return linux_clean_up_and_throw_exp(env, 1, NULL, &list, udev_device, enumerator, udev_ctx);
=======

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
jstring linux_clean_throw_exp_usbenumeration(JNIEnv *env, int task, const char *expmsg,
		struct jstrarray_list *list, struct udev_device *udev_device,
		struct udev_enumerate *enumerator, struct udev *udev_ctx) {

	(*env)->ExceptionClear(env);
	free_jstrarraylist(list);

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

	if(task == 1) {
		throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
	}else {
		throw_serialcom_exception(env, 3, 0, expmsg);
	}

	return NULL;
}

/*
 * Finds information about USB HID devices using Linux udev API.
 * The sequence of entries in array must match with what java layer expect. If a particular USB
 * attribute is not set in descriptor or can not be obtained "---" is placed in its place.
 *
 * The array returned will be in following sequence; transport, device node, vendor ID,
 * product ID, serial, product, manufacturer, USB bus number, USB device number, location ID.
 */
jobjectArray linux_enumerate_usb_hid_devices(JNIEnv *env, jint vendor_to_match) {

	int x = 0;
	struct jstrarray_list list = {0};
	jstring vendor_id_info;
	jstring usb_dev_info;
	jclass strClass = NULL;
	jobjectArray usbHidDevicesFound = NULL;

	struct udev *udev_ctx;
	struct udev_enumerate *enumerator;
	struct udev_list_entry *devices, *dev_list_entry;
	const char *sysattr_val;
	const char *prop_val;
	const char *path;
	struct udev_device *udev_device;
	struct udev_device *parent_udev_device;
	const char *device_node;
	char *endptr;

	init_jstrarraylist(&list, 100);

	/* libudev is reference counted. Memory is freed when counts reach to zero. */
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

		/* save the device node */
		device_node = udev_device_get_devnode(udev_device);
		if(device_node == NULL) {
			return linux_clean_throw_exp_usbenumeration(env, 2, E_CANNOTFINDDEVNODE, &list, udev_device, enumerator, udev_ctx);
		}

		parent_udev_device = udev_device_get_parent_with_subsystem_devtype(udev_device, "usb", "usb_device");
		if(parent_udev_device == NULL) {
			udev_device_unref(udev_device);
			continue;
		}

		/* USB-IF VENDOR ID */
		sysattr_val = udev_device_get_sysattr_value(parent_udev_device, "idVendor");
		if(sysattr_val != NULL) {
			if(vendor_to_match != 0) {
				/* we need to apply filter for identify specific vendor */
				if(vendor_to_match != (0x0000FFFF & (int)strtol(sysattr_val, &endptr, 16))) {
					udev_device_unref(udev_device);
					continue;
				}
			}
			vendor_id_info = (*env)->NewStringUTF(env, sysattr_val);
		}else {
			vendor_id_info = (*env)->NewStringUTF(env, "---");
		}
		if((vendor_id_info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			return linux_clean_throw_exp_usbenumeration(env, 1, NULL, &list, udev_device, enumerator, udev_ctx);
		}

		/* reaching here means that the device meets given criteria specific vendor or all vendors. */

		/* TRANSPORT */
		usb_dev_info = (*env)->NewStringUTF(env, "USB");
		if((usb_dev_info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			return linux_clean_throw_exp_usbenumeration(env, 1, NULL, &list, udev_device, enumerator, udev_ctx);
		}
		insert_jstrarraylist(&list, usb_dev_info);

		/* DEVICE NODE */
		usb_dev_info = (*env)->NewStringUTF(env, device_node);
		if((usb_dev_info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			return linux_clean_throw_exp_usbenumeration(env, 1, NULL, &list, udev_device, enumerator, udev_ctx);
		}
		insert_jstrarraylist(&list, usb_dev_info);

		insert_jstrarraylist(&list, vendor_id_info);

		/* USB PRODUCT ID */
		sysattr_val = udev_device_get_sysattr_value(parent_udev_device, "idProduct");
		if(sysattr_val != NULL) {
			usb_dev_info = (*env)->NewStringUTF(env, sysattr_val);
		}else {
			usb_dev_info = (*env)->NewStringUTF(env, "---");
		}
		if((usb_dev_info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			return linux_clean_throw_exp_usbenumeration(env, 1, NULL, &list, udev_device, enumerator, udev_ctx);
		}
		insert_jstrarraylist(&list, usb_dev_info);

		/* SERIAL NUMBER */
		sysattr_val = udev_device_get_sysattr_value(parent_udev_device, "serial");
		if(sysattr_val != NULL) {
			usb_dev_info = (*env)->NewStringUTF(env, sysattr_val);
		}else {
			usb_dev_info = (*env)->NewStringUTF(env, "---");
		}
		if((usb_dev_info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			return linux_clean_throw_exp_usbenumeration(env, 1, NULL, &list, udev_device, enumerator, udev_ctx);
		}
		insert_jstrarraylist(&list, usb_dev_info);

		/* PRODUCT */
		sysattr_val = udev_device_get_sysattr_value(parent_udev_device, "product");
		if(sysattr_val != NULL) {
			usb_dev_info = (*env)->NewStringUTF(env, sysattr_val);
		}else {
			usb_dev_info = (*env)->NewStringUTF(env, "---");
		}
		if((usb_dev_info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			return linux_clean_throw_exp_usbenumeration(env, 1, NULL, &list, udev_device, enumerator, udev_ctx);
		}
		insert_jstrarraylist(&list, usb_dev_info);

		/* MANUFACTURER */
		sysattr_val = udev_device_get_sysattr_value(parent_udev_device, "manufacturer");
		if(sysattr_val != NULL) {
			usb_dev_info = (*env)->NewStringUTF(env, sysattr_val);
		}else {
			usb_dev_info = (*env)->NewStringUTF(env, "---");
		}
		if((usb_dev_info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			return linux_clean_throw_exp_usbenumeration(env, 1, NULL, &list, udev_device, enumerator, udev_ctx);
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
			return linux_clean_throw_exp_usbenumeration(env, 1, NULL, &list, udev_device, enumerator, udev_ctx);
		}
		insert_jstrarraylist(&list, usb_dev_info);

		udev_device_unref(udev_device);
	}

	udev_enumerate_unref(enumerator);
	udev_unref(udev_ctx);

	/* Create a JAVA/JNI style array of String object, populate it and return to java layer. */
	strClass = (*env)->FindClass(env, JAVALSTRING);
	if((strClass == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		return linux_clean_throw_exp_usbenumeration(env, 2, E_FINDCLASSSSTRINGSTR, &list, NULL, NULL, NULL);
	}

	usbHidDevicesFound = (*env)->NewObjectArray(env, (jsize) list.index, strClass, NULL);
	if((usbHidDevicesFound == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		return linux_clean_throw_exp_usbenumeration(env, 2, E_NEWOBJECTARRAYSTR, &list, NULL, NULL, NULL);
	}

	for (x=0; x < list.index; x++) {
		(*env)->SetObjectArrayElement(env, usbHidDevicesFound, x, list.base[x]);
		if((*env)->ExceptionOccurred(env)) {
			return linux_clean_throw_exp_usbenumeration(env, 2, E_SETOBJECTARRAYSTR, &list, NULL, NULL, NULL);
		}
	}

	free_jstrarraylist(&list);
	return usbHidDevicesFound;
}
#endif

#if defined (__APPLE__)
/* Cleans up resources and set exception that will get thrown upon return to java layer. */
jstring mac_clean_throw_exp_usbenumeration(JNIEnv *env, int task, const char *expmsg, CFSetRef hiddev_cfset,
		IOHIDDeviceRef *hiddev_references) {

	(*env)->ExceptionClear(env);
	free_jstrarraylist(list);

	/* free memory first, so even if throwing JNI exception fails, this succeeds. */
	if(task == 1) {
		CFRelease(hiddev_cfset);
		free(hiddev_references);
	}else if(task == 2) {
		CFRelease(hiddev_cfset);
		free(hiddev_references);
	}else {
	}

	if(task == 1) {
		throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
	}else {
		throw_serialcom_exception(env, 3, 0, expmsg);
	}

	return NULL;
}

/*
 * Finds information about USB HID devices using HID manager API.
 * The sequence of entries in array must match with what java layer expect. If a particular USB
 * attribute is not set in descriptor or can not be obtained "---" is placed in its place.
 *
 * The array returned will be in following sequence; transport, device node, vendor ID,
 * product ID, serial, product, manufacturer, USB bus number, USB device number, location ID.
 * The device information is inserted in the order as expected by Java layer.
 *
 * In MAC os x, there is no device node like '/dev/xxx'. Devices are identified by their usage or
 * other properties. So, to maintain consistency, we create device node string as shown below with
 * elements separated by underscore : Transport_USB-VID_USB-PID_USB-serialnumber_LocationID
 */
jobjectArray mac_enumerate_usb_hid_devices(JNIEnv *env, jint vendor_to_match, IOHIDManagerRef mac_hid_mgr) {
	int x = 0;
	struct jstrarray_list list = {0};
	char device_node[128];
	jstring vendor_id_info;
	jstring product_id_info;
	jstring product_info;
	jstring manufacturer_info;
	jstring serial_num_info;
	jstring location_id_info;
	jstring usb_dev_info;
	jclass strClass = NULL;
	jobjectArray usbHidDevicesFound = NULL;

	int result;
	char charbuffer[128];
	char hexcharbuffer[16];
	CFStringRef str_ref;
	CFNumberRef num_ref;
	CFSetRef hiddev_cfset;
	CFIndex num_hid_devices;
	IOHIDDeviceRef *hiddev_references = NULL;

	init_jstrarraylist(&list, 100);

	/* obtain currently enumerated devices matching the given criteria (specified when manager was created). */
	hiddev_cfset = IOHIDManagerCopyDevices(mac_hid_mgr);

	/* check how many devices found matching the given criteria. */
	num_hid_devices = CFSetGetCount(hiddev_cfset);

	if(num_hid_devices > 0) {
		/* allocate memory to collect info about hid devices. */
		hiddev_references = calloc(num_hid_devices, sizeof(IOHIDDeviceRef));
		if(hiddev_references == NULL) {
			return mac_clean_throw_exp_usbenumeration(env, 2, E_CALLOCSTR, &list, hiddev_cfset, hiddev_references);
		}

		/* obtain all the values in set in a C array. */
		CFSetGetValues(hiddev_cfset, (const void **) hiddev_references);

		for (x = 0; x < num_hid_devices; x++) {
			if(!hiddev_references[x]) {
				continue;
			}

			/* TRANSPORT */
			str_ref = IOHIDDeviceGetProperty(hiddev_references[x]), CFSTR(kIOHIDTransportKey));
			if(str_ref) {
				memset(charbuffer, '\0', sizeof(charbuffer));
				CFStringGetCString(str_ref, charbuffer, sizeof(charbuffer), kCFStringEncodingUTF8);
				CFRelease(str_ref);
				if((strcmp(charbuffer, "USB") == 0) || (strcmp(charbuffer, "usb") == 0)) {
					/* This is a USB device, proceed to match vendor ID. */
				}else {
					continue;
				}
			}else {
				continue;
			}

			/* USB-IF VENDOR ID */
			num_ref = IOHIDDeviceGetProperty(hiddev_references[x]), CFSTR(kIOHIDVendorIDKey));
			if(num_ref) {
				CFNumberGetValue(num_ref, kCFNumberSInt32Type, &result);
				CFRelease(num_ref);
				if(vendor_to_match != 0) {
					/* we need to apply filter for identifying specific vendor */
					if(vendor_to_match != (0x0000FFFF & result)) {
						continue;
					}
				}
				memset(hexcharbuffer, '\0', sizeof(hexcharbuffer));
				snprintf(hexcharbuffer, 16, "%04X", result & 0x0000FFFF);
				memset(device_node, '\0', sizeof(charbuffer));
				strcpy(device_node, "usb_");
				strncat(device_node, hexcharbuffer, 4);
				strcat(device_node, "_");
				vendor_id_info = (*env)->NewStringUTF(env, hexcharbuffer);
				if((vendor_id_info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
					return mac_clean_throw_exp_usbenumeration(env, 1, NULL, &list, hiddev_cfset, hiddev_references);
				}
			}else {
				/* vendor ID is a must for a sane system */
				continue;
>>>>>>> upstream/master
			}

			/* reaching here means that the device meets given criteria specific vendor or all vendors. */

<<<<<<< HEAD
			/* TRANSPORT */
			usb_dev_info = (*env)->NewStringUTF(env, "USB");
			if((usb_dev_info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
				return linux_clean_up_and_throw_exp(env, 1, NULL, &list, udev_device, enumerator, udev_ctx);
			}
			insert_jstrarraylist(&list, usb_dev_info);

			/* DEVICE NODE */
			usb_dev_info = (*env)->NewStringUTF(env, device_node);
			if((usb_dev_info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
				return linux_clean_up_and_throw_exp(env, 1, NULL, &list, udev_device, enumerator, udev_ctx);
			}
			insert_jstrarraylist(&list, usb_dev_info);

			/* USB-IF VENDOR ID */
			insert_jstrarraylist(&list, vendor_id_info);

			/* USB PRODUCT ID */
			sysattr_val = udev_device_get_sysattr_value(udev_device, "idProduct");
			if(sysattr_val != NULL) {
				usb_dev_info = (*env)->NewStringUTF(env, sysattr_val);
			}else {
				usb_dev_info = (*env)->NewStringUTF(env, "---");
			}
			if((usb_dev_info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
				return linux_clean_up_and_throw_exp(env, 1, NULL, &list, udev_device, enumerator, udev_ctx);
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
				return linux_clean_up_and_throw_exp(env, 1, NULL, &list, udev_device, enumerator, udev_ctx);
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
				return linux_clean_up_and_throw_exp(env, 1, NULL, &list, udev_device, enumerator, udev_ctx);
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
				return linux_clean_up_and_throw_exp(env, 1, NULL, &list, udev_device, enumerator, udev_ctx);
			}
			insert_jstrarraylist(&list, usb_dev_info);

			udev_device_unref(udev_device);
		}

		udev_enumerate_unref(enumerator);
		udev_unref(udev_ctx);
#endif

#if defined (__APPLE__)
#endif

		/* Create a JAVA/JNI style array of String object, populate it and return to java layer. */
		strClass = (*env)->FindClass(env, JAVALSTRING);
		if((strClass == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
#if defined (__linux__)
			return linux_clean_up_and_throw_exp(env, 2, E_FINDCLASSSSTRINGSTR, &list, NULL, NULL, NULL);
#endif
#if defined (__APPLE__)
#endif
		}

		usbHidDevicesFound = (*env)->NewObjectArray(env, (jsize) list.index, strClass, NULL);
		if((usbHidDevicesFound == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
#if defined (__linux__)
			return linux_clean_up_and_throw_exp(env, 2, E_NEWOBJECTARRAYSTR, &list, NULL, NULL, NULL);
#endif
#if defined (__APPLE__)
#endif
		}

		for (x=0; x < list.index; x++) {
			(*env)->SetObjectArrayElement(env, usbHidDevicesFound, x, list.base[x]);
			if((*env)->ExceptionOccurred(env)) {
#if defined (__linux__)
				return linux_clean_up_and_throw_exp(env, 2, E_SETOBJECTARRAYSTR, &list, NULL, NULL, NULL);
#endif
#if defined (__APPLE__)
#endif
			}
		}

		free_jstrarraylist(&list);
		return usbHidDevicesFound;
	}
=======
			usb_dev_info = (*env)->NewStringUTF(env, "USB");
			if((usb_dev_info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
				return mac_clean_throw_exp_usbenumeration(env, 1, NULL, &list, hiddev_cfset, hiddev_references);
			}
			insert_jstrarraylist(&list, usb_dev_info);

			/* USB PRODUCT ID */
			num_ref = IOHIDDeviceGetProperty(hiddev_references[x]), CFSTR(kIOHIDProductIDKey));
			if(num_ref) {
				CFNumberGetValue(num_ref, kCFNumberSInt32Type, &result);
				CFRelease(num_ref);
				memset(hexcharbuffer, '\0', sizeof(hexcharbuffer));
				snprintf(hexcharbuffer, 16, "%04X", result & 0x0000FFFF);
				strncat(device_node, hexcharbuffer, 4);
				strcat(device_node, "_");
				product_id_info = (*env)->NewStringUTF(env, hexcharbuffer);
			}else {
				product_id_info = (*env)->NewStringUTF(env, "---");
			}
			if((product_id_info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
				return mac_clean_throw_exp_usbenumeration(env, 1, NULL, &list, hiddev_cfset, hiddev_references);
			}

			/* SERIAL NUMBER */
			str_ref = IOHIDDeviceGetProperty(hiddev_references[x]), CFSTR(kIOHIDSerialNumberKey));
			if(str_ref) {
				memset(charbuffer, '\0', sizeof(charbuffer));
				CFStringGetCString(str_ref, charbuffer, sizeof(charbuffer), kCFStringEncodingUTF8);
				CFRelease(str_ref);
				strcat(device_node, charbuffer);
				strcat(device_node, "_");
				serial_num_info = (*env)->NewStringUTF(env, charbuffer);
			}else {
				strcat(device_node, "---");
				strcat(device_node, "_");
				serial_num_info = (*env)->NewStringUTF(env, "---");
			}
			if((serial_num_info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
				return mac_clean_throw_exp_usbenumeration(env, 1, NULL, &list, hiddev_cfset, hiddev_references);
			}

			/* PRODUCT */
			str_ref = IOHIDDeviceGetProperty(hiddev_references[x]), CFSTR(kIOHIDProductKey));
			if(str_ref) {
				memset(charbuffer, '\0', sizeof(charbuffer));
				CFStringGetCString(str_ref, charbuffer, sizeof(charbuffer), kCFStringEncodingUTF8);
				CFRelease(str_ref);
				product_info = (*env)->NewStringUTF(env, charbuffer);
			}else {
				product_info = (*env)->NewStringUTF(env, "---");
			}
			if((product_info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
				return mac_clean_throw_exp_usbenumeration(env, 1, NULL, &list, hiddev_cfset, hiddev_references);
			}

			/* MANUFACTURER */
			str_ref = IOHIDDeviceGetProperty(hiddev_references[x]), CFSTR(kIOHIDManufacturerKey));
			if(str_ref) {
				memset(charbuffer, '\0', sizeof(charbuffer));
				CFStringGetCString(str_ref, charbuffer, sizeof(charbuffer), kCFStringEncodingUTF8);
				CFRelease(str_ref);
				manufacturer_info = (*env)->NewStringUTF(env, charbuffer);
			}else {
				manufacturer_info = (*env)->NewStringUTF(env, "---");
			}
			if((manufacturer_info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
				return mac_clean_throw_exp_usbenumeration(env, 1, NULL, &list, hiddev_cfset, hiddev_references);
			}

			/* Location ID */
			num_ref = IOHIDDeviceGetProperty(hiddev_references[x]), kIOHIDLocationIDKey));
			if(num_ref) {
				CFNumberGetValue(num_ref, kCFNumberSInt32Type, &result);
				CFRelease(num_ref);
				memset(hexcharbuffer, '\0', sizeof(hexcharbuffer));
				snprintf(hexcharbuffer, 16, "0x%08lX", result);
				strncat(device_node, hexcharbuffer, 8);
				location_id_info = (*env)->NewStringUTF(env, hexcharbuffer);
			}else {
				strcat(device_node, "---");
				location_id_info = (*env)->NewStringUTF(env, "---");
			}
			if((location_id_info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
				return mac_clean_throw_exp_usbenumeration(env, 1, NULL, &list, hiddev_cfset, hiddev_references);
			}

			/* TRANSPORT */
			usb_dev_info = (*env)->NewStringUTF(env, "USB");
			if((usb_dev_info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
				return linux_clean_throw_exp_usbenumeration(env, 1, NULL, &list, udev_device, enumerator, udev_ctx);
			}
			insert_jstrarraylist(&list, usb_dev_info);

			/* DEVICE NODE */
			usb_dev_info = (*env)->NewStringUTF(env, device_node);
			if((usb_dev_info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
				return mac_clean_throw_exp_usbenumeration(env, 1, NULL, &list, hiddev_cfset, hiddev_references);
			}
			insert_jstrarraylist(&list, usb_dev_info);

			insert_jstrarraylist(&list, vendor_id_info);
			insert_jstrarraylist(&list, product_id_info);
			insert_jstrarraylist(&list, serial_num_info);
			insert_jstrarraylist(&list, product_info);
			insert_jstrarraylist(&list, manufacturer_info);
			insert_jstrarraylist(&list, location_id_info);
		}
	}

	/* Create a JAVA/JNI style array of String object, populate it and return to java layer. */
	strClass = (*env)->FindClass(env, JAVALSTRING);
	if((strClass == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		return mac_clean_throw_exp_usbenumeration(env, 2, E_FINDCLASSSSTRINGSTR, &list, NULL, NULL, NULL);
	}

	usbHidDevicesFound = (*env)->NewObjectArray(env, (jsize) list.index, strClass, NULL);
	if((usbHidDevicesFound == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		return mac_clean_throw_exp_usbenumeration(env, 2, E_NEWOBJECTARRAYSTR, &list, NULL, NULL, NULL);
	}

	for (x=0; x < list.index; x++) {
		(*env)->SetObjectArrayElement(env, usbHidDevicesFound, x, list.base[x]);
		if((*env)->ExceptionOccurred(env)) {
			return mac_clean_throw_exp_usbenumeration(env, 2, E_SETOBJECTARRAYSTR, &list, NULL, NULL, NULL);
		}
	}

	free_jstrarraylist(&list);
	return usbHidDevicesFound;
}
#endif
>>>>>>> upstream/master
