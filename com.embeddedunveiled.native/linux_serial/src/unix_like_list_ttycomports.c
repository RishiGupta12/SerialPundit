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
jobjectArray listAvailableComPorts(JNIEnv *env) {

	int x = 0;
	struct jstrarray_list list = {0};
	jstring serial_device;
	jclass strClass = NULL;
	jobjectArray serialDevicesFound = NULL;

	struct udev *udev_ctx;
	struct udev_enumerate *enumerator;
	struct udev_list_entry *devices, *dev_list_entry;
	const char *device_node;
	const char *path;
	struct udev_device *udev_device;

	/* allocate memory for 100 jstrings */
	init_jstrarraylist(&list, 100);

	udev_ctx = udev_new();
	enumerator = udev_enumerate_new(udev_ctx);
	/* devices which claim to be tty devices will be registered with tty framework whether
	 * they are real or virtual (ttyUSB, rfcomm, pseudo terminal). */
	udev_enumerate_add_match_subsystem(enumerator, "tty");
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
		if(device_node != NULL) {
			serial_device = (*env)->NewStringUTF(env, device_node);
			if((serial_device == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
				(*env)->ExceptionClear(env);
				udev_device_unref(udev_device);
				udev_enumerate_unref(enumerator);
				udev_unref(udev_ctx);
				free_jstrarraylist(&list);
				throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
				return NULL;
			}
			insert_jstrarraylist(&list, serial_device);
		}

		udev_device_unref(udev_device);
	}
	udev_enumerate_unref(enumerator);
	udev_unref(udev_ctx);

	/* Create a JAVA/JNI style array of String object, populate it and return to java layer. */
	strClass = (*env)->FindClass(env, JAVALSTRING);
	if((strClass == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		(*env)->ExceptionClear(env);
		free_jstrarraylist(&list);
		throw_serialcom_exception(env, 3, 0, E_FINDCLASSSSTRINGSTR);
		return NULL;
	}

	serialDevicesFound = (*env)->NewObjectArray(env, (jsize) list.index, strClass, NULL);
	if((serialDevicesFound == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		(*env)->ExceptionClear(env);
		free_jstrarraylist(&list);
		throw_serialcom_exception(env, 3, 0, E_NEWOBJECTARRAYSTR);
		return NULL;
	}

	for (x=0; x < list.index; x++) {
		(*env)->SetObjectArrayElement(env, serialDevicesFound, x, list.base[x]);
		if((*env)->ExceptionOccurred(env)) {
			(*env)->ExceptionClear(env);
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 3, 0, E_SETOBJECTARRAYSTR);
			return NULL;
		}
	}

	/* free/release memories allocated finally (Top command will show memory accumulation if it not
	 * freed for debugging). */
	free_jstrarraylist(&list);
	return serialDevicesFound;
}
#endif

#if defined (__APPLE__)
jobjectArray listAvailableComPorts(JNIEnv *env, jobject obj) {

	int x = 0;
	struct jstrarray_list list = {0};
	jstring serial_device;
	jclass strClass = NULL;
	jobjectArray serialDevicesFound = NULL;

	CFMutableDictionaryRef matching_dictionary = NULL;
	io_iterator_t iterator;
	io_service_t service = 0;
	kern_return_t kr = 0;;
	CFStringRef cf_callout_path;
	char callout_path[512];

	/* allocate memory for 100 jstrings */
	init_jstrarraylist(&list, 100);

	/* Set up a dictionary that matches all devices with a provider class of IOSerialBSDClient.*/
	matching_dictionary = IOServiceMatching(kIOSerialBSDServiceValue);
	if(matching_dictionary == NULL) {
		/* handle error */
	}
	kr = IOServiceGetMatchingServices(kIOMasterPortDefault, matching_dictionary, &iterator);
	if(kr != KERN_SUCCESS) {
		set_error_status(env, obj, status, kr);
		free_jstrarraylist(&list);
		return NULL;
	}
	if(!iterator) {
		/* handle error*/
	}

	/* Iterate over all matching objects. */
	while((service = IOIteratorNext(iterator)) != 0) {
		memset(callout_path, 0, sizeof(callout_path));

		/* Get the character device path in UTF-8 encoding. In mac os x each serial device shows up
		 * twice in /dev, once as a tty.* and once as a cu.*. The TTY devices are for calling into UNIX
		 * systems, whereas CU (Call-Up) devices are for calling out from them (for example, modems).
		 * The technical difference is that /dev/tty.* devices will wait (or listen) for DCD (data-carrier-detect),
		 * for example someone calling in, before responding. The /dev/cu.* devices on the other hand do not
		 * assert DCD, so they will always connect (respond or succeed) immediately. */
		cf_callout_path = IORegistryEntryCreateCFProperty(service, CFSTR(kIOCalloutDeviceKey), kCFAllocatorDefault, 0);
		CFStringGetCString(cf_callout_path, callout_path, sizeof(callout_path), kCFStringEncodingUTF8);
		CFRelease(cf_callout_path);
		serial_device = (*env)->NewStringUTF(env, callout_path);
		insert_jstrarraylist(&list, serial_device);

		IOObjectRelease(service);
	}

	IOObjectRelease(iterator);   /* Release iterator. */

	/* Create a JAVA/JNI style array of String object, populate it and return to java layer. */
	strClass = (*env)->FindClass(env, JAVALSTRING);
	if((strClass == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		(*env)->ExceptionClear(env);
		free_jstrarraylist(&list);
		throw_serialcom_exception(env, 3, 0, E_FINDCLASSSSTRINGSTR);
		return NULL;
	}

	serialDevicesFound = (*env)->NewObjectArray(env, (jsize) list.index, strClass, NULL);
	if((serialDevicesFound == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		(*env)->ExceptionClear(env);
		free_jstrarraylist(&list);
		throw_serialcom_exception(env, 3, 0, E_NEWOBJECTARRAYSTR);
		return NULL;
	}

	for (x=0; x < list.index; x++) {
		(*env)->SetObjectArrayElement(env, serialDevicesFound, x, list.base[x]);
		if((*env)->ExceptionOccurred(env)) {
			(*env)->ExceptionClear(env);
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 3, 0, E_SETOBJECTARRAYSTR);
			return NULL;
		}
	}

	/* free/release memories allocated finally (Top command will show memory accumulation if it not
	 * freed for debugging). */
	free_jstrarraylist(&list);
	return serialDevicesFound;
}
#endif
