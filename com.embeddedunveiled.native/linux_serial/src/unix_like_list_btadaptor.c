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

#include <unistd.h>      /* UNIX standard function definitions  */
#include <stdarg.h>      /* ISO C Standard. Variable arguments  */
#include <stdio.h>       /* ISO C99 Standard: Input/output      */
#include <stdlib.h>      /* Standard ANSI routines              */
#include <string.h>      /* String function definitions         */
#include <fcntl.h>       /* File control definitions            */
#include <errno.h>       /* Error number definitions            */
#include <sys/types.h>   /* Primitive System Data Types         */
#include <sys/stat.h>    /* Defines the structure of the data   */
#include <sys/ioctl.h>
#if defined (__linux__)
#include <sys/socket.h>
#include <sys/param.h>
#include <bluetooth/bluetooth.h>
#include <bluetooth/hci.h>
#include <bluetooth/hci_lib.h>
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
 * Finds information about Bluetooth adaptors using operating system specific facilities and API.
 * The sequence of entries in array must match with what java layer expect. If a particular attribute
 * is not set or can not be obtained "---" is placed in its place.
 */
jobjectArray list_local_bt_adaptors(JNIEnv *env, jobject obj) {
	int x = 0;
	int y = 0;
	int fd = 0;
	int ret = 0;
	int sock = 0;
	struct hci_dev_info di;
	struct hci_dev_list_req *dl;
	struct hci_dev_req *dr;
	char addr[18];
	char name[249];

	struct jstrarray_list list = {0};
	jstring bt_dev_info;
	jclass strClass = NULL;
	jobjectArray btDevicesInfo = NULL;

	init_jstrarraylist(&list, 50);

	/* Open HCI socket  */
	errno = 0;
	sock = socket(AF_BLUETOOTH, SOCK_RAW, BTPROTO_HCI);
	if(sock < 0) {
		free_jstrarraylist(&list);
		throw_serialcom_exception(env, 1, errno, NULL);
		return NULL;
	}

	dl = (struct hci_dev_list_req *) malloc(HCI_MAX_DEV * sizeof(struct hci_dev_req) + sizeof(struct hci_dev_list_req));
	if(dl == NULL) {
		free_jstrarraylist(&list);
		throw_serialcom_exception(env, 3, 0, E_MALLOCSTR);
		return NULL;
	}
	dl->dev_num = HCI_MAX_DEV;
	dr = dl->dev_req;

	/* Find number of devices */
	errno = 0;
	ret = ioctl(sock, HCIGETDEVLIST, (void *) dl);
	if(ret < 0) {
		free(dl);
		free_jstrarraylist(&list);
		throw_serialcom_exception(env, 1, errno, NULL);
		return NULL;
	}

	for (x = 0; x < dl->dev_num; x++) {
		di.dev_id = (dr + x)->dev_id;
		if(ioctl(sock, HCIGETDEVINFO, (void *) &di) < 0) {
			continue;
		}

		/* Open BT device */
		fd = hci_open_dev(di.dev_id);
		if(fd < 0) {
			free(dl);
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 3, 0, E_HCIOPENDEV);
			return NULL;
		}

		/* Get friendly name and process it */
		if(hci_read_local_name(fd, sizeof(name), name, 1000) < 0) {
			hci_close_dev(fd);
			free(dl);
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 3, 0, E_HCIREADNAME);
			return NULL;
		}
		for (y = 0; y < 248 && name[y]; y++) {
			if ((unsigned char) name[y] < 32 || name[y] == 127)
				name[y] = '.';
		}
		name[248] = '\0';
		bt_dev_info = (*env)->NewStringUTF(env, name);
		if((bt_dev_info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			hci_close_dev(fd);
			free(dl);
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
			return NULL;
		}
		insert_jstrarraylist(&list, bt_dev_info);

		/* Get BT address */
		if(hci_read_bd_addr(fd, &di.bdaddr, 1000) < 0) {
			hci_close_dev(fd);
			free(dl);
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 3, 0, E_HCIBTADDR);
			return NULL;
		}
		memset(addr, '\0', sizeof(addr));
		ba2str(&di.bdaddr, addr);
		bt_dev_info = (*env)->NewStringUTF(env, addr);
		if((bt_dev_info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			hci_close_dev(fd);
			free(dl);
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
			return NULL;
		}
		insert_jstrarraylist(&list, bt_dev_info);

		/* Get type */
		bt_dev_info = (*env)->NewStringUTF(env, hci_typetostr((di.type & 0x30) >> 4));
		if((bt_dev_info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			hci_close_dev(fd);
			free(dl);
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
			return NULL;
		}
		insert_jstrarraylist(&list, bt_dev_info);

		/* Get bus */
		bt_dev_info = (*env)->NewStringUTF(env, hci_bustostr(di.type & 0x0f));
		if((bt_dev_info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			hci_close_dev(fd);
			free(dl);
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
			return NULL;
		}
		insert_jstrarraylist(&list, bt_dev_info);

		hci_close_dev(fd);
	}

	strClass = (*env)->FindClass(env, JAVALSTRING);
	if((strClass == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		free(dl);
		free_jstrarraylist(&list);
		throw_serialcom_exception(env, 3, 0, E_FINDCLASSSSTRINGSTR);
		return NULL;
	}

	btDevicesInfo = (*env)->NewObjectArray(env, (jsize) list.index, strClass, NULL);
	if((btDevicesInfo == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		free(dl);
		free_jstrarraylist(&list);
		throw_serialcom_exception(env, 3, 0, E_NEWOBJECTARRAYSTR);
		return NULL;
	}

	for (x=0; x < list.index; x++) {
		(*env)->SetObjectArrayElement(env, btDevicesInfo, x, list.base[x]);
		if((*env)->ExceptionOccurred(env)) {
			free(dl);
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 3, 0, E_SETOBJECTARRAYSTR);
			return NULL;
		}
	}

	free(dl);
	free_jstrarraylist(&list);
	return btDevicesInfo;
}
