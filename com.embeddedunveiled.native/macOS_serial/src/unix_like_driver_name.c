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
#include "unix_like_serial_lib.h"

#if defined (__linux__)
/*
 * Find the name of the driver which is currently associated with the given serial port.
 *
 * A serial device can be on pci, usb, bluetooth or pseudo. For Linux we walk down the
 * sysfs tree until a driver is found for the given device node (comPortName).
 */
jstring find_driver_for_given_com_port(JNIEnv *env, jstring comPortName) {

	int x = 0;
	const char* port_name_to_match = NULL;
	char com_port_name_to_match[256];
	jstring driver_name = NULL;

	struct udev *udev_ctx;
	struct udev_enumerate *enumerator;
	struct udev_list_entry *devices, *dev_list_entry;
	const char *prop_val_port_name;
	const char *prop_val_driver_name;
	const char *path;
	struct udev_device *udev_device;
	struct udev_device *current_udev_device;
	struct udev_device *tmp_udev_device;

	port_name_to_match = (*env)->GetStringUTFChars(env, comPortName, NULL);
	if((com_port_name_to_match == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_GETSTRUTFCHARSTR);
		return NULL;
	}
	memset(com_port_name_to_match, '\0', 256);
	strcpy(com_port_name_to_match, port_name_to_match);

	udev_ctx = udev_new();
	enumerator = udev_enumerate_new(udev_ctx);
	udev_enumerate_add_match_subsystem(enumerator, "tty");
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
		prop_val_port_name = udev_device_get_property_value(udev_device, "DEVNAME");

		/* If the device node name matches what we are looking for get driver for it.
		 * if no driver is found driver_name will be NULL when control reaches at the
		 * end of this function in which case empty string will be returned to java
		 * layer. */
		if(prop_val_port_name != NULL) {
			if(strcmp(com_port_name_to_match, prop_val_port_name) == 0) {

				current_udev_device = udev_device;

				for(x = 0; x < 5; x++) {
					prop_val_driver_name = udev_device_get_driver(current_udev_device);
					if(prop_val_driver_name != NULL) {
						driver_name = (*env)->NewStringUTF(env, prop_val_driver_name);
						if((driver_name == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
							(*env)->ExceptionClear(env);
							udev_device_unref(udev_device);
							udev_enumerate_unref(enumerator);
							udev_unref(udev_ctx);
							(*env)->ReleaseStringUTFChars(env, comPortName, port_name_to_match);
							throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
							return NULL;
						}
						break;
					}else {
						/* reaching here means driver is not found for current udev device,
						 * so find its parent and then try to find its driver iteratively.
						 * at last either driver will be found or not. also if parent does not exist
						 * at any iteration, there is no need to iterate further */
						tmp_udev_device = current_udev_device;
						current_udev_device = NULL; /* reset */
						current_udev_device = udev_device_get_parent(tmp_udev_device);
						if(current_udev_device == NULL) {
							/*  parent does not exist, stop processing */
							break;
						}
					}
				}

				udev_device_unref(udev_device);
				break;
			}
		}

		/* the 'udev_device' released only after desired property value has been copied to some
		 * other memory region like one allocated via NewStringUTF(). */
		udev_device_unref(udev_device);
	}

	udev_enumerate_unref(enumerator);
	udev_unref(udev_ctx);
	(*env)->ReleaseStringUTFChars(env, comPortName, port_name_to_match);

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
/* TODO */
#endif
