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
#endif
#include <jni.h>
#include "unix_like_hid.h"

#if defined (__linux__)
/*
 * Find the name of the driver which is currently associated with the given HID device.
 *
 * A HID device can be on usb, bluetooth or pseudo. For Linux we walk down the
 * sysfs tree until a driver is found for the given device node.
 */
jstring linux_find_driver_for_given_hiddevice(JNIEnv *env, jstring hidDevNode) {

	const char* hid_name_to_match = NULL;
	jstring driver_name = NULL;
	int check_for_parent = -1;
	struct udev *udev_ctx;
	struct udev_enumerate *enumerator;
	struct udev_list_entry *devices, *dev_list_entry;
	const char *prop_val_port_name;
	const char *prop_val_driver_name;
	const char *device_node;
	const char *path;
	struct udev_device *udev_device;
	struct udev_device *parent_device;

	hid_name_to_match = (*env)->GetStringUTFChars(env, hidDevNode, NULL);
	if((hid_name_to_match == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_GETSTRUTFCHARSTR);
		return NULL;
	}

	udev_ctx = udev_new();
	enumerator = udev_enumerate_new(udev_ctx);
	udev_enumerate_add_match_subsystem(enumerator, "hidraw");
	udev_enumerate_scan_devices(enumerator);
	devices = udev_enumerate_get_list_entry(enumerator);

	udev_list_entry_foreach(dev_list_entry, devices) {

		/* from the sysfs filename create a udev_device object representing it. */
		path = udev_list_entry_get_name(dev_list_entry);
		udev_device = udev_device_new_from_syspath(udev_enumerate_get_udev(enumerator), path);
		if(udev_device == NULL) {
			continue;
		}

		/* get the device node for this udev device. */
		device_node = udev_device_get_devnode(udev_device);

		/* If the device node name matches what we are looking for get driver for it.
		 * if we fail to get driver name than return empty string (prop_val_driver_name
		 * will be NULL when control reaches at the end of this function). */
		if(device_node != NULL) {
			if(strcmp(hid_name_to_match, device_node) == 0) {
				prop_val_driver_name = udev_device_get_driver(udev_device);
				if(prop_val_driver_name != NULL) {
					driver_name = (*env)->NewStringUTF(env, prop_val_driver_name);
					if((driver_name == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
						(*env)->ExceptionClear(env);
						udev_device_unref(udev_device);
						udev_enumerate_unref(enumerator);
						udev_unref(udev_ctx);
						(*env)->ReleaseStringUTFChars(env, hidDevNode, hid_name_to_match);
						throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
						return NULL;
					}
					check_for_parent = -1;
				}else {
					check_for_parent = 1;
				}

				if(check_for_parent == 1) {
					parent_device = udev_device_get_parent(udev_device);
					if(parent_device != NULL) {
						prop_val_driver_name = udev_device_get_driver(parent_device);
						if(prop_val_driver_name != NULL) {
							driver_name = (*env)->NewStringUTF(env, prop_val_driver_name);
							if((driver_name == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
								(*env)->ExceptionClear(env);
								udev_device_unref(udev_device);
								udev_enumerate_unref(enumerator);
								udev_unref(udev_ctx);
								(*env)->ReleaseStringUTFChars(env, hidDevNode, hid_name_to_match);
								throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
								return NULL;
							}else {
								/* if both the parent and driver found, return driver name to caller. */
								check_for_parent = -1;
							}
						}else {
							/* if the parent is found but driver not found then analyze next parent device. */
							check_for_parent = 1;
						}
					}else {
						/* if the parent does not exist, make no more attempts to analyze parent devices further down the tree. */
						check_for_parent = -1;
					}
				}

				if(check_for_parent == 1) {
					parent_device = udev_device_get_parent(parent_device );
					if(parent_device != NULL) {
						prop_val_driver_name = udev_device_get_driver(parent_device);
						if(prop_val_driver_name != NULL) {
							driver_name = (*env)->NewStringUTF(env, prop_val_driver_name);
							if((driver_name == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
								(*env)->ExceptionClear(env);
								udev_device_unref(udev_device);
								udev_enumerate_unref(enumerator);
								udev_unref(udev_ctx);
								(*env)->ReleaseStringUTFChars(env, hidDevNode, hid_name_to_match);
								throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
								return NULL;
							}else {
								check_for_parent = -1;
							}
						}else {
							check_for_parent = 1;
						}
					}else {
						check_for_parent = -1;
					}
				}

				if(check_for_parent == 1) {
					parent_device = udev_device_get_parent(parent_device);
					if(parent_device != NULL) {
						prop_val_driver_name = udev_device_get_driver(parent_device);
						if(prop_val_driver_name != NULL) {
							driver_name = (*env)->NewStringUTF(env, prop_val_driver_name);
							if((driver_name == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
								(*env)->ExceptionClear(env);
								udev_device_unref(udev_device);
								udev_enumerate_unref(enumerator);
								udev_unref(udev_ctx);
								(*env)->ReleaseStringUTFChars(env, hidDevNode, hid_name_to_match);
								throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
								return NULL;
							}else {
								check_for_parent = -1;
							}
						}else {
							check_for_parent = 1;
						}
					}else {
						check_for_parent = -1;
					}
				}

				if(check_for_parent == 1) {
					parent_device = udev_device_get_parent(udev_device);
					if(parent_device != NULL) {
						prop_val_driver_name = udev_device_get_driver(parent_device );
						if(prop_val_driver_name != NULL) {
							driver_name = (*env)->NewStringUTF(env, prop_val_driver_name);
							if((driver_name == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
								(*env)->ExceptionClear(env);
								udev_device_unref(udev_device);
								udev_enumerate_unref(enumerator);
								udev_unref(udev_ctx);
								(*env)->ReleaseStringUTFChars(env, hidDevNode, hid_name_to_match);
								throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
								return NULL;
							}else {
								check_for_parent = -1;
							}
						}else {
							check_for_parent = 1;
						}
					}else {
						check_for_parent = -1;
					}
				}

				if(check_for_parent == 1) {
					parent_device = udev_device_get_parent(parent_device);
					if(parent_device != NULL) {
						prop_val_driver_name = udev_device_get_driver(parent_device);
						if(prop_val_driver_name != NULL) {
							driver_name = (*env)->NewStringUTF(env, prop_val_driver_name);
							if((driver_name == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
								(*env)->ExceptionClear(env);
								udev_device_unref(udev_device);
								udev_enumerate_unref(enumerator);
								udev_unref(udev_ctx);
								(*env)->ReleaseStringUTFChars(env, hidDevNode, hid_name_to_match);
								throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
								return NULL;
							}
						}
					}
				}

				/* whether prop_val_driver_name is NULL (return empty string) or not
				 * (return driver name found), no more iteration is needed. */
				udev_device_unref(udev_device);
				break;
			}
		}

		/* released only after desired property value has been saved to some
		 * other memory region like one got from NewStringUTF(). */
		udev_device_unref(udev_device);
	}

	udev_enumerate_unref(enumerator);
	udev_unref(udev_ctx);
	(*env)->ReleaseStringUTFChars(env, hidDevNode, hid_name_to_match);

	/* if the driver name is found return it to caller otherwise return empty string. */
	if(driver_name == NULL) {
		driver_name = (*env)->NewStringUTF(env, "");
		if((driver_name == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			(*env)->ExceptionClear(env);
			throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
			return NULL;
		}
	}
	return driver_name;
}
#endif

#if defined (__APPLE__)
jstring linux_find_driver_for_given_hiddevice(JNIEnv *env, jstring comPortName) {

}
#endif
