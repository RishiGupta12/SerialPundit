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
#endif

#include <jni.h>
#include "unix_like_serial_lib.h"

#if defined (__linux__)
jint get_latency_timer_value(JNIEnv *env, jstring comPortName) {

	struct udev *udev_ctx;
	struct udev_enumerate *enumerator;
	struct udev_list_entry *devices, *dev_list_entry;
	const char *device_path;
	const char *device_node;
	const char *path;
	struct udev_device *udev_device;
	const char *com_port_to_match = NULL;
	char buffer[512];
	int fd = 0;
	int ret = 0;
	char *endptr;
	jint timer_value = 0;

	udev_ctx = udev_new();
	enumerator = udev_enumerate_new(udev_ctx);
	udev_enumerate_add_match_subsystem(enumerator, "tty");
	udev_enumerate_scan_devices(enumerator);
	devices = udev_enumerate_get_list_entry(enumerator);

	com_port_to_match = (*env)->GetStringUTFChars(env, comPortName, NULL);
	if((com_port_to_match == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_GETSTRUTFCHARSTR);
		return -1;
	}

	udev_list_entry_foreach(dev_list_entry, devices) {
		path = udev_list_entry_get_name(dev_list_entry);
		udev_device = udev_device_new_from_syspath(udev_enumerate_get_udev(enumerator), path);
		if(udev_device == NULL) {
			continue;
		}

		/* match the device node, if matched get device path, and create absolute path to
		 * latency file and read its value */
		device_node = udev_device_get_devnode(udev_device);
		if(device_node != NULL) {
			if(strcmp(device_node, com_port_to_match) == 0) {

				device_path = udev_device_get_devpath(udev_device);
				if(device_path != NULL) {
					memset(buffer, '\0', sizeof(buffer));
					snprintf(buffer, 512, "/sys%s/device/latency_timer", device_path);

					errno = 0;
					fd = open(buffer, O_RDONLY);
					if(fd < 0) {
						(*env)->ReleaseStringUTFChars(env, comPortName, com_port_to_match);
						throw_serialcom_exception(env, 1, errno, NULL);
						return -1;
					}

					memset(buffer, '\0', sizeof(buffer));
					errno = 0;
					ret = read(fd, buffer, 512);
					if(ret < 0) {
						(*env)->ReleaseStringUTFChars(env, comPortName, com_port_to_match);
						throw_serialcom_exception(env, 1, errno, NULL);
						return -1;
					}
					close(fd);

					timer_value = (jint) strtol(buffer, &endptr, 10);
					(*env)->ReleaseStringUTFChars(env, comPortName, com_port_to_match);
					udev_device_unref(udev_device);
					udev_enumerate_unref(enumerator);
					udev_unref(udev_ctx);
					return timer_value;
				}
			}
		}

		udev_device_unref(udev_device);
	}
	udev_enumerate_unref(enumerator);
	udev_unref(udev_ctx);
	/* reaching here means given com port does not represent ftdi device, throw exception */
	throw_serialcom_exception(env, 3, 0, E_NOTFTDIPORT);
	return -1;
}

#endif

#if defined (__APPLE__)
#endif
