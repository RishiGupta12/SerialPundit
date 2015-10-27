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
#include <errno.h>
#include <fcntl.h>
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
/* */
jobjectArray get_usbdev_powerinfo(JNIEnv *env, jstring comPortName) {

	int x = 0;
	int fd = 0;
	int ret = 0;
	const char* port_name_to_match = NULL;
	char com_port_name_to_match[256];
	char buffer[512];
	jclass strClass = NULL;
	jstring info = NULL;
	jobjectArray powerInfo = NULL;
	struct jstrarray_list list = {0};
	struct udev *udev_ctx;
	struct udev_enumerate *enumerator;
	struct udev_list_entry *devices, *dev_list_entry;
	const char *path;
	struct udev_device *udev_device;
	struct udev_device *parent_device;
	const char *sysattr_val;
	const char *prop_val;

	init_jstrarraylist(&list, 10);

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
		path = udev_list_entry_get_name(dev_list_entry);
		udev_device = udev_device_new_from_syspath(udev_enumerate_get_udev(enumerator), path);
		if(udev_device == NULL) {
			continue;
		}

		sysattr_val = udev_device_get_devnode(udev_device);
		if(sysattr_val != NULL) {
			/* if the device node given matches enumerated node, proceed to collect power
			 * information about it */
			if(strcmp(com_port_name_to_match, sysattr_val) == 0) {
				parent_device = udev_device_get_parent(udev_device);
				if(parent_device == NULL) {
					udev_device_unref(udev_device);
					continue;
				}
				parent_device = udev_device_get_parent(parent_device);
				if(parent_device == NULL) {
					udev_device_unref(udev_device);
					continue;
				}
				parent_device = udev_device_get_parent(parent_device);
				if(parent_device == NULL) {
					udev_device_unref(udev_device);
					continue;
				}

				/* get the sysfs path to this USB device (port) */
				prop_val = udev_device_get_property_value(parent_device, "DEVPATH");
				if(prop_val != NULL) {
					/* /sys/bus/usb/devices/3-3/power/autosuspend_delay_ms. gives time after which
					 * usb device will be allowed to suspend if it was idle */
					memset(buffer, '\0', sizeof(buffer));
					snprintf(buffer, 512, "/sys%s/power/autosuspend_delay_ms", prop_val);

					errno = 0;
					fd = open(buffer, O_RDONLY);
					if(fd < 0) {
						(*env)->ReleaseStringUTFChars(env, comPortName, port_name_to_match);
						throw_serialcom_exception(env, 1, errno, NULL);
						return NULL;
					}

					memset(buffer, '\0', sizeof(buffer));
					errno = 0;
					ret = read(fd, buffer, 512);
					if(ret < 0) {
						(*env)->ReleaseStringUTFChars(env, comPortName, port_name_to_match);
						throw_serialcom_exception(env, 1, errno, NULL);
						return NULL;
					}else if(ret == 0) {
						info = (*env)->NewStringUTF(env, "---");
					}else {
						info = (*env)->NewStringUTF(env, buffer);
					}
					if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
						(*env)->ReleaseStringUTFChars(env, comPortName, port_name_to_match);
						throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
						return NULL;
					}

					insert_jstrarraylist(&list, info);
					close(fd);

					/* /sys/bus/usb/devices/3-3/power/control. gives whether device is kept always on or
					 * is allowed to auto suspend */
					memset(buffer, '\0', sizeof(buffer));
					snprintf(buffer, 512, "/sys%s/power/control", prop_val);

					errno = 0;
					fd = open(buffer, O_RDONLY);
					if(fd < 0) {
						(*env)->ReleaseStringUTFChars(env, comPortName, port_name_to_match);
						throw_serialcom_exception(env, 1, errno, NULL);
						return NULL;
					}

					memset(buffer, '\0', sizeof(buffer));
					errno = 0;
					ret = read(fd, buffer, 512);
					if(ret < 0) {
						(*env)->ReleaseStringUTFChars(env, comPortName, port_name_to_match);
						throw_serialcom_exception(env, 1, errno, NULL);
						return NULL;
					}else if(ret == 0) {
						info = (*env)->NewStringUTF(env, "---");
					}else {
						info = (*env)->NewStringUTF(env, buffer);
					}
					if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
						(*env)->ReleaseStringUTFChars(env, comPortName, port_name_to_match);
						throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
						return NULL;
					}

					insert_jstrarraylist(&list, info);
					close(fd);

					/* /sys/bus/usb/devices/3-3/power/runtime_status. gives whether device is active
					 * or suspended */
					memset(buffer, '\0', sizeof(buffer));
					snprintf(buffer, 512, "/sys%s/power/runtime_status", prop_val);

					errno = 0;
					fd = open(buffer, O_RDONLY);
					if(fd < 0) {
						(*env)->ReleaseStringUTFChars(env, comPortName, port_name_to_match);
						throw_serialcom_exception(env, 1, errno, NULL);
						return NULL;
					}

					memset(buffer, '\0', sizeof(buffer));
					errno = 0;
					ret = read(fd, buffer, 512);
					if(ret < 0) {
						(*env)->ReleaseStringUTFChars(env, comPortName, port_name_to_match);
						throw_serialcom_exception(env, 1, errno, NULL);
						return NULL;
					}else if(ret == 0) {
						info = (*env)->NewStringUTF(env, "---");
					}else {
						info = (*env)->NewStringUTF(env, buffer);
					}
					if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
						(*env)->ReleaseStringUTFChars(env, comPortName, port_name_to_match);
						throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
						return NULL;
					}

					insert_jstrarraylist(&list, info);
					close(fd);
				}
			}
		}

		udev_device_unref(udev_device);
	}

	udev_enumerate_unref(enumerator);
	udev_unref(udev_ctx);
	(*env)->ReleaseStringUTFChars(env, comPortName, port_name_to_match);

	/* create a JAVA/JNI style array of String object, populate it and return to java layer. */
	strClass = (*env)->FindClass(env, JAVALSTRING);
	if((strClass == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		(*env)->ExceptionClear(env);
		free_jstrarraylist(&list);
		throw_serialcom_exception(env, 3, 0, E_FINDCLASSSSTRINGSTR);
		return NULL;
	}

	powerInfo = (*env)->NewObjectArray(env, (jsize) list.index, strClass, NULL);
	if((powerInfo == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		(*env)->ExceptionClear(env);
		free_jstrarraylist(&list);
		throw_serialcom_exception(env, 3, 0, E_NEWOBJECTARRAYSTR);
		return NULL;
	}

	for (x=0; x < list.index; x++) {
		(*env)->SetObjectArrayElement(env, powerInfo, x, list.base[x]);
		if((*env)->ExceptionOccurred(env)) {
			(*env)->ExceptionClear(env);
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 3, 0, E_SETOBJECTARRAYSTR);
			return NULL;
		}
	}

	free_jstrarraylist(&list);
	return powerInfo;
}
#endif
