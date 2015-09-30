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
#endif
#include <jni.h>
#include "unix_like_serial_lib.h"

/*
 * Cleans up resources and set exception that will get thrown upon return to java layer.
 */
#if defined (__linux__)
jstring linux_rfcomm_cleanexp(JNIEnv *env, int task, const char *expmsg, struct jstrarray_list *list, struct udev_device *udev_device,
		struct udev_enumerate *enumerator, struct udev *udev_ctx) {
	(*env)->ExceptionClear(env);
	free_jstrarraylist(list);
	if(task == 1) {
		udev_device_unref(udev_device);
		udev_enumerate_unref(enumerator);
		udev_unref(udev_ctx);
	}else {
	}

	jclass serialComExceptionClass = (*env)->FindClass(env, SCOMEXPCLASS);
	if((serialComExceptionClass == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		(*env)->ExceptionClear(env);
		/* LOGE(E_FINDCLASSSCOMEXPSTR, "RISHI"); TODO */
		return NULL;
	}

	if(task == 1) {
		(*env)->ThrowNew(env, serialComExceptionClass, E_NEWSTRUTFSTR);
	}else {
		(*env)->ThrowNew(env, serialComExceptionClass, expmsg);
	}

	return NULL;
}
#endif

/*
 * Finds information about rfcomm device nodes using operating system specific facilities and API.
 * The sequence of entries in array must match with what java layer expect. If a particular attribute
 * is not set or can not be obtained "---" is placed in its place.
 */
jobjectArray list_bt_rfcomm_dev_nodes(JNIEnv *env) {
	int x = 0;
	struct jstrarray_list list = {0};
	jstring rfcomm_dev_info;
	jclass strClass = NULL;
	jobjectArray rfcommNodesFound = NULL;

#if defined (__linux__)
	struct udev *udev_ctx;
	struct udev_enumerate *enumerator;
	struct udev_list_entry *devices, *dev_list_entry;
	const char *sysattr_val;
	const char *path;
	struct udev_device *udev_device;
	struct udev_device *rfcomm_udev_device;
	char *endptr;
	const char *prop_val;
	const char *dev_path;
	const char *device_node;
	char fulldevpath[512];
#endif
#if defined (__APPLE__)
#endif

	init_jstrarraylist(&list, 100);

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

		/* Major number of Bluetooth RFCOMM TTY devices in Linux is 216. Filter devices
		 * based on their major number and then create new udev device and then parse
		 * its properties to get address and channel number. */
		prop_val = udev_device_get_property_value(udev_device, "MAJOR");
		if(prop_val != NULL) {
			if(216 == ((int)strtol(prop_val, &endptr, 10))) {
				dev_path = udev_device_get_devpath(udev_device);
				memset(fulldevpath, '\0', sizeof(fulldevpath));
				/* device path obtained from udev does not contain /sys,
				 * so prefix path with /sys. */
				strncpy(fulldevpath, "/sys", strlen("/sys"));
				strncat(fulldevpath, dev_path, strlen(dev_path));
				if(dev_path != NULL) {
					rfcomm_udev_device = udev_device_new_from_syspath(udev_ctx, fulldevpath);
					if(rfcomm_udev_device != NULL) {

						/* Device NODE REPRESENTING SERIAL PORT */
						device_node = udev_device_get_devnode(udev_device);
						if(device_node == NULL) {
							/* if node can not be found other info would not make sense,
							 * so throw exception as it should not occur. */
							udev_device_unref(rfcomm_udev_device);
							return linux_clean_up_and_throw_exp(env, 2, E_CANNOTFINDDEVNODE, &list, udev_device, enumerator, udev_ctx);
						}
						rfcomm_dev_info = (*env)->NewStringUTF(env, device_node);
						if((rfcomm_dev_info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
							udev_device_unref(rfcomm_udev_device);
							return linux_rfcomm_cleanexp(env, 1, NULL, &list, udev_device, enumerator, udev_ctx);
						}
						insert_jstrarraylist(&list, rfcomm_dev_info);

						/* REMOTE DEVICE BT ADDRESS */
						sysattr_val = udev_device_get_sysattr_value(rfcomm_udev_device, "address");
						if(sysattr_val != NULL) {
							rfcomm_dev_info = (*env)->NewStringUTF(env, sysattr_val);
						}else {
							rfcomm_dev_info = (*env)->NewStringUTF(env, "---");
						}
						if((rfcomm_dev_info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
							udev_device_unref(rfcomm_udev_device);
							return linux_rfcomm_cleanexp(env, 1, NULL, &list, udev_device, enumerator, udev_ctx);
						}
						insert_jstrarraylist(&list, rfcomm_dev_info);

						/* BT CHANNEL NUMBER IN USE FOR SPP */
						sysattr_val = udev_device_get_sysattr_value(rfcomm_udev_device, "channel");
						if(sysattr_val != NULL) {
							rfcomm_dev_info = (*env)->NewStringUTF(env, sysattr_val);
						}else {
							rfcomm_dev_info = (*env)->NewStringUTF(env, "---");
						}
						if((rfcomm_dev_info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
							udev_device_unref(rfcomm_udev_device);
							return linux_rfcomm_cleanexp(env, 1, NULL, &list, udev_device, enumerator, udev_ctx);
						}
						insert_jstrarraylist(&list, rfcomm_dev_info);

						/* no longer needed, free here itself. */
						udev_device_unref(rfcomm_udev_device);
					}
				}
			}

			udev_device_unref(udev_device);
		}
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
		return linux_rfcomm_cleanexp(env, 2, E_FINDCLASSSSTRINGSTR, &list, NULL, NULL, NULL);
#endif
#if defined (__APPLE__)
		return mac_rfcomm_cleanexp(env, 2, E_FINDCLASSSSTRINGSTR, &list, NULL, NULL);
#endif
	}

	rfcommNodesFound = (*env)->NewObjectArray(env, (jsize) list.index, strClass, NULL);
	if((rfcommNodesFound == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
#if defined (__linux__)
		return linux_rfcomm_cleanexp(env, 2, E_NEWOBJECTARRAYSTR, &list, NULL, NULL, NULL);
#endif
#if defined (__APPLE__)
		return mac_rfcomm_cleanexp(env, 2, E_NEWOBJECTARRAYSTR, &list, NULL, NULL);
#endif
	}

	for (x=0; x < list.index; x++) {
		(*env)->SetObjectArrayElement(env, rfcommNodesFound, x, list.base[x]);
		if((*env)->ExceptionOccurred(env)) {
#if defined (__linux__)
			return linux_rfcomm_cleanexp(env, 2, E_SETOBJECTARRAYSTR, &list, NULL, NULL, NULL);
#endif
#if defined (__APPLE__)
			return mac_rfcomm_cleanexp(env, 2, E_SETOBJECTARRAYSTR, &list, NULL, NULL);
#endif
		}
	}

	free_jstrarraylist(&list);
	return rfcommNodesFound;
}
