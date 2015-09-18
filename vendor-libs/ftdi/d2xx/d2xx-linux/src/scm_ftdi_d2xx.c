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

#if defined (__linux__) || defined (__APPLE__)

/* Make primitives such as read and write resume, in case they are interrupted by signal,
 * before they actually start reading or writing data. The partial success case are handled
 * at appropriate places in functions applicable.
 * For details see features.h about MACROS defined below. */
#ifndef _BSD_SOURCE
#define _BSD_SOURCE
#endif
#ifndef _GNU_SOURCE
#define _GNU_SOURCE
#endif

#include <unistd.h>      /* UNIX standard function definitions  */
#include <stdarg.h>      /* ISO C Standard. Variable arguments  */
#include <stdio.h>       /* ISO C99 Standard: Input/output      */
#include <stdlib.h>      /* Standard ANSI routines              */
#include <string.h>      /* String function definitions         */
#include <fcntl.h>       /* File control definitions            */
#include <errno.h>       /* Error number definitions            */
#include <sys/types.h>   /* Primitive System Data Types         */
#include <sys/stat.h>    /* Defines the structure of the data   */
#include "scm_ftdi_d2xx.h"

/* Common interface with java layer for supported OS types. */
#include "../../com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge.h"


/* ********************* D2XX Classic Functions ******************** */

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    setVidPid
 * Signature: (II)I
 *
 * @return 0 on success -1 if error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_setVidPid(JNIEnv *env, jobject obj, jint vid, jint pid) {
	FT_STATUS ftStatus = 0;
	ftStatus = FT_SetVIDPID(vid, pid);
	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return -1;
	}
	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    getVidPid
 * Signature: ()[I
 *
 * @return array containing vid and pid or NULL if something fails.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jintArray JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_getVidPid(JNIEnv *env, jobject obj) {
	FT_STATUS ftStatus = 0;
	DWORD vid = 0;
	DWORD pid = 0;
	jintArray vidpidcombination = NULL;
	jint combo[2] = {0};

	ftStatus = FT_GetVIDPID(&vid, &pid);
	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return NULL;
	}

	combo[0] = vid;
	combo[1] = pid;
	vidpidcombination = (*env)->NewIntArray(env, 2);
	(*env)->SetIntArrayRegion(env, vidpidcombination, 0, 2, combo);
	if((*env)->ExceptionOccurred(env)) {
		throw_serialcom_exception(env, 3, 0, E_SETINTARRREGIONSTR);
		return NULL;
	}

	return vidpidcombination;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    createDeviceInfoList
 * Signature: ()I
 *
 * @return 0 on success otherwise -1 if error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_createDeviceInfoList(JNIEnv *env, jobject obj) {
	FT_STATUS ftStatus = 0;
	DWORD numDevices;
	ftStatus = FT_CreateDeviceInfoList(&numDevices);
	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return -1;
	}
	return numDevices;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    getDeviceInfoList
 * Signature: (I)[Ljava/lang/String;
 *
 * @return array of string containing info about connected devices or NULL if something fails.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jobjectArray JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_getDeviceInfoList(JNIEnv *env, jobject obj, jint numOfDevices) {
	FT_STATUS ftStatus = 0;
	FT_DEVICE_LIST_INFO_NODE *devInfo;
	int i = 0;
	char hexcharbuffer[256];
	struct jstrarray_list list = {0};
	unsigned int num_of_devices = numOfDevices;
	jstring info;
	jclass strClass = NULL;
	jobjectArray devicesFound = NULL;

	devInfo = (FT_DEVICE_LIST_INFO_NODE *) calloc(num_of_devices, sizeof(FT_DEVICE_LIST_INFO_NODE));
	if(devInfo == NULL) {
		throw_serialcom_exception(env, 3, 0, E_CALLOCSTR);
		return NULL;
	}

	ftStatus = FT_GetDeviceInfoList(devInfo, &num_of_devices);
	if(ftStatus != FT_OK) {
		free(devInfo);
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return NULL;
	}

	init_jstrarraylist(&list, 100);

	for (i = 0; i < num_of_devices; i++) {
		memset(hexcharbuffer, '\0', sizeof(hexcharbuffer));
		snprintf(hexcharbuffer, 256, "%lX", devInfo[i].Flags);
		info = (*env)->NewStringUTF(env, hexcharbuffer);
		if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			free(devInfo);
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
			return NULL;
		}
		insert_jstrarraylist(&list, info);

		memset(hexcharbuffer, '\0', sizeof(hexcharbuffer));
		snprintf(hexcharbuffer, 256, "%lX", devInfo[i].Type);
		info = (*env)->NewStringUTF(env, hexcharbuffer);
		if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			free(devInfo);
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
			return NULL;
		}
		insert_jstrarraylist(&list, info);

		memset(hexcharbuffer, '\0', sizeof(hexcharbuffer));
		snprintf(hexcharbuffer, 256, "%lX", devInfo[i].ID);
		info = (*env)->NewStringUTF(env, hexcharbuffer);
		if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			free(devInfo);
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
			return NULL;
		}
		insert_jstrarraylist(&list, info);

		memset(hexcharbuffer, '\0', sizeof(hexcharbuffer));
		snprintf(hexcharbuffer, 256, "%X", (unsigned int)devInfo[i].LocId);
		info = (*env)->NewStringUTF(env, hexcharbuffer);
		if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			free(devInfo);
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
			return NULL;
		}
		insert_jstrarraylist(&list, info);

		info = (*env)->NewStringUTF(env, devInfo[i].SerialNumber);
		if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			free(devInfo);
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
			return NULL;
		}
		insert_jstrarraylist(&list, info);

		info = (*env)->NewStringUTF(env, devInfo[i].Description);
		if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			free(devInfo);
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
			return NULL;
		}
		insert_jstrarraylist(&list, info);

		memset(hexcharbuffer, '\0', sizeof(hexcharbuffer));
		snprintf(hexcharbuffer, 256, "%lX", devInfo[i].ftHandle);
		info = (*env)->NewStringUTF(env, hexcharbuffer);
		if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			free(devInfo);
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
			return NULL;
		}
		insert_jstrarraylist(&list, info);

	}

	/* Create a JAVA/JNI style array of String object, populate it and return to java layer. */
	strClass = (*env)->FindClass(env, "java/lang/String");
	if((strClass == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		free(devInfo);
		free_jstrarraylist(&list);
		throw_serialcom_exception(env, 3, 0, E_FINDCLASSSSTRINGSTR);
		return NULL;
	}

	devicesFound = (*env)->NewObjectArray(env, (jsize) list.index, strClass, NULL);
	if((devicesFound == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		free(devInfo);
		free_jstrarraylist(&list);
		throw_serialcom_exception(env, 3, 0, E_NEWOBJECTARRAYSTR);
		return NULL;
	}

	for (i=0; i < list.index; i++) {
		(*env)->SetObjectArrayElement(env, devicesFound, i, list.base[i]);
		if((*env)->ExceptionOccurred(env)) {
			free(devInfo);
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 3, 0, E_SETOBJECTARRAYSTR);
			return NULL;
		}
	}

	free(devInfo);
	free_jstrarraylist(&list);
	return devicesFound;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    getDeviceInfoDetail
 * Signature: (I)[Ljava/lang/String;
 *
 * @return array of string containing info about the device at given index or NULL if something fails.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jobjectArray JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_getDeviceInfoDetail(JNIEnv *env, jobject obj, jint index) {
	FT_STATUS ftStatus = 0;
	FT_DEVICE_LIST_INFO_NODE *devInfo;
	int i = 0;
	char hexcharbuffer[256];
	struct jstrarray_list list = {0};
	jstring info;
	jclass strClass = NULL;
	jobjectArray devInfoNode = NULL;

	devInfo = (FT_DEVICE_LIST_INFO_NODE *) calloc(1, sizeof(FT_DEVICE_LIST_INFO_NODE));
	if(devInfo == NULL) {
		throw_serialcom_exception(env, 3, 0, E_CALLOCSTR);
		return NULL;
	}

	ftStatus = FT_GetDeviceInfoDetail(index, &(devInfo->Flags), &(devInfo->Type), &(devInfo->ID), &(devInfo->LocId),
			&(devInfo->SerialNumber), &(devInfo->Description), &(devInfo->ftHandle));
	if(ftStatus != FT_OK) {
		free(devInfo);
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return NULL;
	}

	init_jstrarraylist(&list, 10);

	memset(hexcharbuffer, '\0', sizeof(hexcharbuffer));
	snprintf(hexcharbuffer, 256, "%lX", devInfo->Flags);
	info = (*env)->NewStringUTF(env, hexcharbuffer);
	if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		free(devInfo);
		free_jstrarraylist(&list);
		throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
		return NULL;
	}
	insert_jstrarraylist(&list, info);
	memset(hexcharbuffer, '\0', sizeof(hexcharbuffer));
	snprintf(hexcharbuffer, 256, "%lX", devInfo->Type);
	info = (*env)->NewStringUTF(env, hexcharbuffer);
	if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		free(devInfo);
		free_jstrarraylist(&list);
		throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
		return NULL;
	}
	insert_jstrarraylist(&list, info);
	memset(hexcharbuffer, '\0', sizeof(hexcharbuffer));
	snprintf(hexcharbuffer, 256, "%lX", devInfo->ID);
	info = (*env)->NewStringUTF(env, hexcharbuffer);
	if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		free(devInfo);
		free_jstrarraylist(&list);
		throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
		return NULL;
	}
	insert_jstrarraylist(&list, info);
	memset(hexcharbuffer, '\0', sizeof(hexcharbuffer));
	snprintf(hexcharbuffer, 256, "%X", (unsigned int)devInfo->LocId);
	info = (*env)->NewStringUTF(env, hexcharbuffer);
	if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		free(devInfo);
		free_jstrarraylist(&list);
		throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
		return NULL;
	}
	insert_jstrarraylist(&list, info);
	info = (*env)->NewStringUTF(env, devInfo->SerialNumber);
	if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		free(devInfo);
		free_jstrarraylist(&list);
		throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
		return NULL;
	}
	insert_jstrarraylist(&list, info);
	info = (*env)->NewStringUTF(env, devInfo->Description);
	if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		free(devInfo);
		free_jstrarraylist(&list);
		throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
		return NULL;
	}
	insert_jstrarraylist(&list, info);
	memset(hexcharbuffer, '\0', sizeof(hexcharbuffer));
	snprintf(hexcharbuffer, 256, "%lX", devInfo->ftHandle);
	info = (*env)->NewStringUTF(env, hexcharbuffer);
	if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		free(devInfo);
		free_jstrarraylist(&list);
		throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
		return NULL;
	}
	insert_jstrarraylist(&list, info);

	strClass = (*env)->FindClass(env, "java/lang/String");
	if((strClass == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		free(devInfo);
		free_jstrarraylist(&list);
		throw_serialcom_exception(env, 3, 0, E_FINDCLASSSSTRINGSTR);
		return NULL;
	}

	devInfoNode = (*env)->NewObjectArray(env, (jsize) list.index, strClass, NULL);
	if((devInfoNode == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		free(devInfo);
		free_jstrarraylist(&list);
		throw_serialcom_exception(env, 3, 0, E_NEWOBJECTARRAYSTR);
		return NULL;
	}

	for (i=0; i < list.index; i++) {
		(*env)->SetObjectArrayElement(env, devInfoNode, i, list.base[i]);
		if((*env)->ExceptionOccurred(env)) {
			free(devInfo);
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 3, 0, E_SETOBJECTARRAYSTR);
			return NULL;
		}
	}

	free(devInfo);
	free_jstrarraylist(&list);
	return devInfoNode;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    listDevices
 * Signature: (II)[Ljava/lang/String;
 *
 * @return array of string containing info about the device(s) or NULL if something fails.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 *
 * Operations are of 3 types :
 * 1) find number of devices connected only.
 * 2) find a particular info about a device at a particular index
 * 3) find info about all devices
 *
 * It constructs  string array in following sequence per device : String id, String serialNumber, String description.
 */
JNIEXPORT jobjectArray JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_listDevices(JNIEnv *env, jobject obj, jint pvArg1, jint dwFlags) {
	FT_STATUS ftStatus = 0;
	DWORD num_of_dev_connected = 0;
	DWORD num_dev_found = 0;
	int x = 0;
	int y = 0;
	int operation = 0;
	long locid = 0;
	char buffer[256];
	struct jstrarray_list list = {0};
	jstring info = NULL;
	jclass strClass = NULL;
	jobjectArray infoFound = NULL;
	int have_str = 0;
	int have_long = 0;

	/* pointer to an array of pointer to string/long for dynamic allocation */
	char **base_str = ((void *)0);
	long *base_long = ((void *)0);

	init_jstrarraylist(&list, 50);

	/* the constant values defined in SerialComFTDID2XX class are
	 * bitwise-OR to know what operation user has requested. For ex;
	 * SerialComFTDID2XX.FT_LIST_ALL | SerialComFTDID2XX.FT_OPEN_BY_SERIAL_NUMBER == 0x0C */
	if((dwFlags & 0x01) == 0x01) {
		operation = 1; /* FT_LIST_NUMBER_ONLY */
	}else if(dwFlags == 0x0A) {
		operation = 2; /* FT_LIST_BY_INDEX | FT_OPEN_BY_SERIAL_NUMBER */
	}else if(dwFlags == 0x12) {
		operation = 3; /* FT_LIST_BY_INDEX | FT_OPEN_BY_DESCRIPTION */
	}else if(dwFlags == 0x22) {
		operation = 4; /* FT_LIST_BY_INDEX | FT_OPEN_BY_LOCATION */
	}else if(dwFlags == 0x0C) {
		operation = 5; /* FT_LIST_ALL | FT_OPEN_BY_SERIAL_NUMBER */
	}else if(dwFlags == 0x14) {
		operation = 6; /* FT_LIST_ALL | FT_OPEN_BY_DESCRIPTION */
	}else if(dwFlags == 0x24) {
		operation = 7; /* FT_LIST_ALL | FT_OPEN_BY_LOCATION */
	}else {
		return NULL;
	}

	if(operation == 1) {
		/* FT_LIST_NUMBER_ONLY */
		ftStatus = FT_ListDevices(&num_of_dev_connected, NULL, FT_LIST_NUMBER_ONLY);
		if(ftStatus != FT_OK) {
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 2, ftStatus, NULL);
			return NULL;
		}
		for (x = 0; x < num_of_dev_connected; x++) {
			for (y = 0; y < 3; y++) {
				info = (*env)->NewStringUTF(env, "---");
				if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
					free_jstrarraylist(&list);
					throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
					return NULL;
				}
				insert_jstrarraylist(&list, info);
			}
		}

	}else if(operation == 2) {
		/* FT_LIST_BY_INDEX | FT_OPEN_BY_SERIAL_NUMBER */
		memset(buffer, '\0', sizeof(buffer));
		ftStatus = FT_ListDevices((PVOID)pvArg1, buffer, FT_LIST_BY_INDEX | FT_OPEN_BY_SERIAL_NUMBER);
		if(ftStatus != FT_OK) {
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 2, ftStatus, NULL);
			return NULL;
		}
		info = (*env)->NewStringUTF(env, "---");
		if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
			return NULL;
		}
		insert_jstrarraylist(&list, info);
		info = (*env)->NewStringUTF(env, buffer);
		if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
			return NULL;
		}
		insert_jstrarraylist(&list, info);
		info = (*env)->NewStringUTF(env, "---");
		if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
			return NULL;
		}
		insert_jstrarraylist(&list, info);

	}else if(operation == 3) {
		/* FT_LIST_BY_INDEX | FT_OPEN_BY_DESCRIPTION */
		memset(buffer, '\0', sizeof(buffer));
		ftStatus = FT_ListDevices((PVOID)pvArg1, buffer, FT_LIST_BY_INDEX | FT_OPEN_BY_DESCRIPTION);
		if(ftStatus != FT_OK) {
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 2, ftStatus, NULL);
			return NULL;
		}
		info = (*env)->NewStringUTF(env, "---");
		if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
			return NULL;
		}
		insert_jstrarraylist(&list, info);
		info = (*env)->NewStringUTF(env, "---");
		if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
			return NULL;
		}
		insert_jstrarraylist(&list, info);
		info = (*env)->NewStringUTF(env, buffer);
		if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
			return NULL;
		}
		insert_jstrarraylist(&list, info);

	}else if(operation == 4) {
		/* FT_LIST_BY_INDEX | FT_OPEN_BY_LOCATION */
		ftStatus = FT_ListDevices((PVOID)pvArg1, &locid, FT_LIST_BY_INDEX | FT_OPEN_BY_LOCATION);
		if(ftStatus != FT_OK) {
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 2, ftStatus, NULL);
			return NULL;
		}
		memset(buffer, '\0', sizeof(buffer));
		snprintf(buffer, sizeof(buffer), "%lX", locid);
		info = (*env)->NewStringUTF(env, buffer);
		if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
			return NULL;
		}
		insert_jstrarraylist(&list, info);
		info = (*env)->NewStringUTF(env, "---");
		if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
			return NULL;
		}
		insert_jstrarraylist(&list, info);
		info = (*env)->NewStringUTF(env, "---");
		if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
			return NULL;
		}
		insert_jstrarraylist(&list, info);

	}else if(operation == 5) {
		/* FT_LIST_ALL | FT_OPEN_BY_SERIAL_NUMBER */
		ftStatus = FT_ListDevices(&num_of_dev_connected, NULL, FT_LIST_NUMBER_ONLY);
		if(ftStatus != FT_OK) {
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 2, ftStatus, NULL);
			return NULL;
		}

		/* if there is no FT device return NULL */
		if(num_of_dev_connected == 0) {
			free_jstrarraylist(&list);
			return NULL;
		}

		/* Create array of pointers to string */
		base_str = (char **) calloc((num_of_dev_connected + 1), sizeof(char *));
		if(base_str == NULL) {
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 3, 0, E_CALLOCSTR);
			return NULL;
		}
		have_str = 1;

		/* put address of each string in array */
		for (x=0; x<num_of_dev_connected; x++) {
			base_str[x] = (char *) calloc(128, sizeof(char));
		}

		/* last entry in this array should be null as per documentation */
		base_str[num_of_dev_connected] = NULL;

		ftStatus = FT_ListDevices(base_str, &num_dev_found, FT_LIST_ALL | FT_OPEN_BY_SERIAL_NUMBER);
		if(ftStatus != FT_OK) {
			free(base_str);
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 2, ftStatus, NULL);
			return NULL;
		}

		/* user might insert or remove devices during the time we calculated how many devices
		 * are connected and just before FT_ListDevices is called again to get serial description,
		 * so we need to handle this. */
		if(num_dev_found >= num_of_dev_connected) {
			for (x=0; x<num_of_dev_connected; x++) {
				info = (*env)->NewStringUTF(env, "---");
				if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
					free(base_str);
					free_jstrarraylist(&list);
					throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
					return NULL;
				}
				insert_jstrarraylist(&list, info);
				info = (*env)->NewStringUTF(env, base_str[x]);
				if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
					free(base_str);
					free_jstrarraylist(&list);
					throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
					return NULL;
				}
				insert_jstrarraylist(&list, info);
				info = (*env)->NewStringUTF(env, "---");
				if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
					free(base_str);
					free_jstrarraylist(&list);
					throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
					return NULL;
				}
				insert_jstrarraylist(&list, info);
			}
		}else {
			for (x=0; x<num_dev_found; x++) {
				info = (*env)->NewStringUTF(env, "---");
				if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
					free(base_str);
					free_jstrarraylist(&list);
					throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
					return NULL;
				}
				insert_jstrarraylist(&list, info);
				info = (*env)->NewStringUTF(env, base_str[x]);
				if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
					free(base_str);
					free_jstrarraylist(&list);
					throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
					return NULL;
				}
				insert_jstrarraylist(&list, info);
				info = (*env)->NewStringUTF(env, "---");
				if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
					free(base_str);
					free_jstrarraylist(&list);
					throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
					return NULL;
				}
				insert_jstrarraylist(&list, info);
			}
		}

	}else if(operation == 6) {
		/* FT_LIST_ALL | FT_OPEN_BY_DESCRIPTION */
		ftStatus = FT_ListDevices(&num_of_dev_connected, NULL, FT_LIST_NUMBER_ONLY);
		if(ftStatus != FT_OK) {
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 2, ftStatus, NULL);
			return NULL;
		}

		/* if there is no FT device return NULL */
		if(num_of_dev_connected == 0) {
			free_jstrarraylist(&list);
			return NULL;
		}

		/* Create array of pointers to string */
		base_str = (char **) calloc((num_of_dev_connected + 1), sizeof(char *));
		if(base_str == NULL) {
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 3, 0, E_CALLOCSTR);
			return NULL;
		}
		have_str = 1;

		/* put address of each string in array */
		for (x=0; x<num_of_dev_connected; x++) {
			base_str[x] = (char *) calloc(128, sizeof(char));
		}

		/* last entry in this array should be null as per documentation */
		base_str[num_of_dev_connected] = NULL;

		ftStatus = FT_ListDevices(base_str, &num_dev_found, FT_LIST_ALL | FT_OPEN_BY_DESCRIPTION);
		if(ftStatus != FT_OK) {
			free(base_str);
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 2, ftStatus, NULL);
			return NULL;
		}

		/* user might insert or remove devices during the time we calculated how many devices
		 * are connected and just before FT_ListDevices is called again to get serial description,
		 * so we need to handle this. */
		if(num_dev_found >= num_of_dev_connected) {
			for (x=0; x<num_of_dev_connected; x++) {
				info = (*env)->NewStringUTF(env, "---");
				if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
					free(base_str);
					free_jstrarraylist(&list);
					throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
					return NULL;
				}
				insert_jstrarraylist(&list, info);
				info = (*env)->NewStringUTF(env, "---");
				if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
					free(base_str);
					free_jstrarraylist(&list);
					throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
					return NULL;
				}
				insert_jstrarraylist(&list, info);
				info = (*env)->NewStringUTF(env, base_str[x]);
				if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
					free(base_str);
					free_jstrarraylist(&list);
					throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
					return NULL;
				}
				insert_jstrarraylist(&list, info);
			}
		}else {
			for (x=0; x<num_dev_found; x++) {
				info = (*env)->NewStringUTF(env, "---");
				if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
					free(base_str);
					free_jstrarraylist(&list);
					throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
					return NULL;
				}
				insert_jstrarraylist(&list, info);
				info = (*env)->NewStringUTF(env, "---");
				if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
					free(base_str);
					free_jstrarraylist(&list);
					throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
					return NULL;
				}
				insert_jstrarraylist(&list, info);
				info = (*env)->NewStringUTF(env, base_str[x]);
				if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
					free(base_str);
					free_jstrarraylist(&list);
					throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
					return NULL;
				}
				insert_jstrarraylist(&list, info);
			}
		}

	}else if(operation == 7) {
		/* FT_LIST_ALL | FT_OPEN_BY_LOCATION */
		ftStatus = FT_ListDevices(&num_of_dev_connected, NULL, FT_LIST_NUMBER_ONLY);
		if(ftStatus != FT_OK) {
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 2, ftStatus, NULL);
			return NULL;
		}

		/* if there is no FT device return NULL */
		if(num_of_dev_connected == 0) {
			free_jstrarraylist(&list);
			return NULL;
		}

		/* Create array of pointers to unsigned long */
		base_long = (long *) calloc((num_of_dev_connected + 1), sizeof(long));
		if(base_long == NULL) {
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 3, 0, E_CALLOCSTR);
			return NULL;
		}
		have_long = 1;

		/* last entry in this array should be null as per documentation */
		base_long[num_of_dev_connected] = NULL;

		ftStatus = FT_ListDevices(base_long, &num_dev_found, FT_LIST_ALL | FT_OPEN_BY_LOCATION);
		if(ftStatus != FT_OK) {
			free(base_long);
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 2, ftStatus, NULL);
			return NULL;
		}

		/* user might insert or remove devices during the time we calculated how many devices
		 * are connected and just before FT_ListDevices is called again to get serial description,
		 * so we need to handle this. */
		if(num_dev_found >= num_of_dev_connected) {
			for (x=0; x<num_of_dev_connected; x++) {
				memset(buffer, '\0', sizeof(buffer));
				snprintf(buffer, sizeof(buffer), "%lX", base_long[x]);
				info = (*env)->NewStringUTF(env, buffer);
				if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
					free(base_long);
					free_jstrarraylist(&list);
					throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
					return NULL;
				}
				insert_jstrarraylist(&list, info);
				info = (*env)->NewStringUTF(env, "---");
				if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
					free(base_long);
					free_jstrarraylist(&list);
					throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
					return NULL;
				}
				insert_jstrarraylist(&list, info);
				info = (*env)->NewStringUTF(env, "---");
				if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
					free(base_long);
					free_jstrarraylist(&list);
					throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
					return NULL;
				}
				insert_jstrarraylist(&list, info);
			}
		}else {
			for (x=0; x<num_dev_found; x++) {
				memset(buffer, '\0', sizeof(buffer));
				snprintf(buffer, sizeof(buffer), "%lX", base_long[x]);
				info = (*env)->NewStringUTF(env, buffer);
				if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
					free(base_long);
					free_jstrarraylist(&list);
					throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
					return NULL;
				}
				insert_jstrarraylist(&list, info);
				info = (*env)->NewStringUTF(env, "---");
				if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
					free(base_long);
					free_jstrarraylist(&list);
					throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
					return NULL;
				}
				insert_jstrarraylist(&list, info);
				info = (*env)->NewStringUTF(env, "---");
				if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
					free(base_long);
					free_jstrarraylist(&list);
					throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
					return NULL;
				}
				insert_jstrarraylist(&list, info);
			}
		}

	}else {
		return NULL;
	}

	strClass = (*env)->FindClass(env, "java/lang/String");
	if((strClass == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		if(have_str == 1) {
			free(base_str);
		}
		if(have_long == 1) {
			free(base_long);
		}
		free_jstrarraylist(&list);
		throw_serialcom_exception(env, 3, 0, E_FINDCLASSSSTRINGSTR);
		return NULL;
	}

	infoFound = (*env)->NewObjectArray(env, (jsize) list.index, strClass, NULL);
	if((infoFound == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		if(have_str == 1) {
			free(base_str);
		}
		if(have_long == 1) {
			free(base_long);
		}
		free_jstrarraylist(&list);
		throw_serialcom_exception(env, 3, 0, E_NEWOBJECTARRAYSTR);
		return NULL;
	}

	for (x=0; x < list.index; x++) {
		(*env)->SetObjectArrayElement(env, infoFound, x, list.base[x]);
		if((*env)->ExceptionOccurred(env)) {
			if(have_str == 1) {
				free(base_str);
			}
			if(have_long == 1) {
				free(base_long);
			}
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 3, 0, E_SETOBJECTARRAYSTR);
			return NULL;
		}
	}

	if(have_str == 1) {
		free(base_str);
	}
	if(have_long == 1) {
		free(base_long);
	}
	free_jstrarraylist(&list);
	return infoFound;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    open
 * Signature: (I)J
 *
 * @return handle on success otherwise -1 if error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jlong JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_open(JNIEnv *env, jobject obj, jint index) {
	FT_HANDLE ftHandle;
	FT_STATUS ftStatus = 0;

	ftStatus = FT_Open(index, &ftHandle);
	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return -1;
	}

	return (long)ftHandle;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    openEx
 * Signature: (Ljava/lang/String;JI)J
 *
 * @return handle on success otherwise -1 if error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jlong JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_openEx(JNIEnv *env, jobject obj,
		                                                               jstring serialOrDescription, jlong locationId, jint dwFlags) {
	FT_HANDLE ftHandle;
	FT_STATUS ftStatus = 0;
	const char* ser_or_desc;

	if(dwFlags == 0x08) {
		if(serialOrDescription == NULL) {
			throw_serialcom_exception(env, 3, 0, E_IllegalARG);
			return -1;
		}
		ser_or_desc = (*env)->GetStringUTFChars(env, serialOrDescription, NULL);
		if((ser_or_desc == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			throw_serialcom_exception(env, 3, 0, E_GETSTRUTFCHARSTR);
			return -1;
		}
		ftStatus = FT_OpenEx((char*)ser_or_desc, FT_OPEN_BY_SERIAL_NUMBER, &ftHandle);
	}else if(dwFlags == 0x10) {
		if(serialOrDescription == NULL) {
			throw_serialcom_exception(env, 3, 0, E_IllegalARG);
			return -1;
		}
		ser_or_desc = (*env)->GetStringUTFChars(env, serialOrDescription, NULL);
		if((ser_or_desc == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			throw_serialcom_exception(env, 3, 0, E_GETSTRUTFCHARSTR);
			return -1;
		}
		ftStatus = FT_OpenEx((char*)ser_or_desc, FT_OPEN_BY_DESCRIPTION, &ftHandle);
	}else if(dwFlags == 0x20) {
		if(locationId < 0) {
			throw_serialcom_exception(env, 3, 0, E_IllegalARG);
			return -1;
		}
		ftStatus = FT_OpenEx(locationId, FT_OPEN_BY_LOCATION, &ftHandle);
	}else {
		throw_serialcom_exception(env, 3, 0, E_IllegalARG);
		return -1;
	}

	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return -1;
	}
	return (long)ftHandle;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    close
 * Signature: (J)I
 *
 * @return 0 on success otherwise -1 if error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_close(JNIEnv *env, jobject obj, jlong handle) {
	FT_STATUS ftStatus = 0;
	FT_HANDLE ftHandle = (FT_HANDLE) handle;

	ftStatus = FT_Close(ftHandle);
	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return -1;
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    read
 * Signature: (J[BI)I
 *
 * @return number of bytes read on success otherwise -1 if error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_read(JNIEnv *env, jobject obj,
		jlong handle, jbyteArray buffer, jint count) {
	FT_HANDLE ftHandle = (FT_HANDLE)handle;
	FT_STATUS ftStatus = 0;
	DWORD num_of_bytes_read;

	jbyte* data_buffer = (jbyte *) calloc(count, sizeof(jbyte));
	if(data_buffer == NULL) {
		throw_serialcom_exception(env, 3, 0, E_CALLOCSTR);
		return -1;
	}

	ftStatus = FT_Read(ftHandle, data_buffer, count, &num_of_bytes_read);
	if(ftStatus != FT_OK) {
		free(data_buffer);
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return -1;
	}

	if(num_of_bytes_read > 0) {
		(*env)->SetByteArrayRegion(env, buffer, 0, (unsigned int)num_of_bytes_read, data_buffer);
		if((*env)->ExceptionOccurred(env)) {
			free(data_buffer);
			throw_serialcom_exception(env, 3, 0, E_SETBYTEARRREGIONSTR);
			return -1;
		}
	}

	free(data_buffer);
	return num_of_bytes_read;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    write
 * Signature: (J[BI)I
 *
 * @return number of bytes written to the device on success otherwise -1 if error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_write(JNIEnv *env, jobject obj,
		jlong handle, jbyteArray buffer, jint count) {
	FT_HANDLE ftHandle = (FT_HANDLE)handle;
	FT_STATUS ftStatus = 0;
	DWORD num_of_bytes_written;

	jbyte* data_buffer = (jbyte *) calloc(count, sizeof(jbyte));
	if(data_buffer == NULL) {
		throw_serialcom_exception(env, 3, 0, E_CALLOCSTR);
		return -1;
	}

	(*env)->GetByteArrayRegion(env, buffer, 0, count, data_buffer);
	if((*env)->ExceptionOccurred(env)) {
		free(data_buffer);
		throw_serialcom_exception(env, 3, 0, E_GETBYTEARRREGIONSTR);
		return -1;
	}

	ftStatus = FT_Write(ftHandle, data_buffer, count, &num_of_bytes_written);
	if(ftStatus != FT_OK) {
		free(data_buffer);
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return -1;
	}

	free(data_buffer);
	return num_of_bytes_written;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    setBaudRate
 * Signature: (JI)I
 *
 * @return 0 on success otherwise -1 if error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_setBaudRate(JNIEnv *env, jobject obj, jlong handle, jint baudRate) {
	FT_HANDLE ftHandle = (FT_HANDLE)handle;
	FT_STATUS ftStatus = 0;
	ftStatus = FT_SetBaudRate(ftHandle, (int)baudRate);
	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return -1;
	}
	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    setDivisor
 * Signature: (JI)I
 *
 * @return 0 on success otherwise -1 if error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_setDivisor(JNIEnv *env, jobject obj, jlong handle, jint divisor) {
	FT_HANDLE ftHandle = (FT_HANDLE)handle;
	FT_STATUS ftStatus = 0;
	ftStatus = FT_SetDivisor(ftHandle, (USHORT)divisor);
	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return -1;
	}
	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    setDataCharacteristics
 * Signature: (JIII)I
 *
 * @return 0 on success otherwise -1 if error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_setDataCharacteristics(JNIEnv *env, jobject obj,
		jlong handle, jint databits, jint stopbits, jint parity) {
	FT_HANDLE ftHandle = (FT_HANDLE)handle;
	FT_STATUS ftStatus = 0;
	UCHAR dbits = 0;
	UCHAR sbits = 0;
	UCHAR par = 0;

	if(databits == 7) {
		dbits = FT_BITS_7;
	}else if(databits == 8) {
		dbits = FT_BITS_8;
	}else {
	}

	if(stopbits == 1) {
		sbits = FT_STOP_BITS_1;
	}else if(stopbits == 2) {
		sbits = FT_STOP_BITS_2;
	}else {
	}

	if(parity == 1) {
		par = FT_PARITY_NONE;
	}else if(parity == 2) {
		par = FT_PARITY_ODD;
	}else if(parity == 3) {
		par = FT_PARITY_EVEN;
	}else if(parity == 4) {
		par = FT_PARITY_MARK;
	}else if(parity == 5) {
		par = FT_PARITY_SPACE;
	}else {
	}


	ftStatus = FT_SetDataCharacteristics(ftHandle, dbits, sbits, par);
	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return -1;
	}
	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    setTimeouts
 * Signature: (JJJ)I
 *
 * @return 0 on success otherwise -1 if error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_setTimeouts(JNIEnv *env, jobject obj,
		jlong handle, jlong read_timeout, jlong write_timeout) {
	FT_HANDLE ftHandle = (FT_HANDLE)handle;
	FT_STATUS ftStatus = 0;

	ftStatus = FT_SetTimeouts(ftHandle, (ULONG)read_timeout, (ULONG)write_timeout);
	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return -1;
	}
	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    setFlowControl
 * Signature: (JICC)I
 *
 * @return 0 on success otherwise -1 if error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_setFlowControl(JNIEnv *env, jobject obj,
		jlong handle, jint mode, jchar xon, jchar off) {
	FT_HANDLE ftHandle = (FT_HANDLE)handle;
	FT_STATUS ftStatus = 0;
	USHORT flowctrl = 0;

	if(mode == 1) {
		flowctrl = FT_FLOW_NONE;
	}else if(mode == 2) {
		flowctrl = FT_FLOW_RTS_CTS;
	}else if(mode == 3) {
		flowctrl = FT_FLOW_DTR_DSR;
	}else if(mode == 4) {
		flowctrl = FT_FLOW_XON_XOFF;
	}else {
	}

	ftStatus = FT_SetFlowControl(ftHandle, flowctrl, (UCHAR)xon, (UCHAR)off);
	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return -1;
	}
	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    setDTR
 * Signature: (J)I
 *
 * @return 0 on success otherwise -1 if error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_setDTR(JNIEnv *env, jobject obj, jlong handle) {
	FT_HANDLE ftHandle = (FT_HANDLE)handle;
	FT_STATUS ftStatus = 0;

	ftStatus = FT_SetDtr(ftHandle);
	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return -1;
	}
	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    clearDTR
 * Signature: (J)I
 *
 * @return 0 on success otherwise -1 if error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_clearDTR(JNIEnv *env, jobject obj, jlong handle) {
	FT_HANDLE ftHandle = (FT_HANDLE)handle;
	FT_STATUS ftStatus = 0;

	ftStatus = FT_ClrDtr(ftHandle);
	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return -1;
	}
	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    setRTS
 * Signature: (J)I
 *
 * @return 0 on success otherwise -1 if error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_setRTS(JNIEnv *env, jobject obj, jlong handle) {
	FT_HANDLE ftHandle = (FT_HANDLE)handle;
	FT_STATUS ftStatus = 0;

	ftStatus = FT_SetRts(ftHandle);
	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return -1;
	}
	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    clearRTS
 * Signature: (J)I
 *
 * @return 0 on success otherwise -1 if error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_clearRTS(JNIEnv *env, jobject obj, jlong handle) {
	FT_HANDLE ftHandle = (FT_HANDLE)handle;
	FT_STATUS ftStatus = 0;

	ftStatus = FT_ClrRts(ftHandle);
	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return -1;
	}
	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    getModemStatus
 * Signature: (J)I
 *
 * @return bit mapped status value otherwise -1 if error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_getModemStatus(JNIEnv *env, jobject obj, jlong handle) {
	FT_HANDLE ftHandle = (FT_HANDLE)handle;
	FT_STATUS ftStatus = 0;
	DWORD status = 0;

	ftStatus = FT_GetModemStatus(ftHandle, &status);
	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return -1;
	}
	return (int)status;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    getQueueStatus
 * Signature: (J)I
 *
 * @return number of bytes in receive queue otherwise -1 if error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_getQueueStatus(JNIEnv *env, jobject obj, jlong handle) {
	FT_HANDLE ftHandle = (FT_HANDLE)handle;
	FT_STATUS ftStatus = 0;
	DWORD num_bytes_in_rx_buffer = 0;

	ftStatus = FT_GetModemStatus(ftHandle, &num_bytes_in_rx_buffer);
	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return -1;
	}
	return (int)num_bytes_in_rx_buffer;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    getDeviceInfo
 * Signature: (J)[Ljava/lang/String;
 *
 * @return array of string containing info about the requested device or NULL if something fails.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jobjectArray JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_getDeviceInfo(JNIEnv *env, jobject obj, jlong handle) {
	FT_HANDLE ftHandle = (FT_HANDLE)handle;
	FT_STATUS ftStatus = 0;
	FT_DEVICE ftDevice;
	DWORD device_id;
	char serial_number[32];
	char description[128];
	const char *dev_type = NULL;
	int i = 0;
	char hexcharbuffer[256];
	struct jstrarray_list list = {0};
	jstring info;
	jclass strClass = NULL;
	jobjectArray infoFound = NULL;

	ftStatus = FT_GetDeviceInfo(ftHandle, &ftDevice, &device_id, serial_number, description, NULL);
	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return NULL;
	}

	init_jstrarraylist(&list, 5);

	if(ftDevice == FT_DEVICE_232H) {
		dev_type = "FT_DEVICE_232H";
	}else if (ftDevice == FT_DEVICE_4232H) {
		dev_type = "FT_DEVICE_4232H";
	}else if(ftDevice == FT_DEVICE_2232H) {
		dev_type = "FT_DEVICE_2232H";
	}else if(ftDevice == FT_DEVICE_232R) {
		dev_type = "FT_DEVICE_232R";
	}else if(ftDevice == FT_DEVICE_2232C) {
		dev_type = "FT_DEVICE_2232C";
	}else if(ftDevice == FT_DEVICE_BM) {
		dev_type = "FT_DEVICE_FTU232BM";
	}else if(ftDevice == FT_DEVICE_AM) {
		dev_type = "FT_DEVICE_FT8U232AM";
	}else if(ftDevice == FT_DEVICE_100AX) {
		dev_type = "FT_DEVICE_100AX";
	}else if(ftDevice == FT_DEVICE_X_SERIES) {
		dev_type = "FT_DEVICE_X_SERIES";
	}else {
		dev_type = "FT_DEVICE_UNKNOWN";
	}

	info = (*env)->NewStringUTF(env, dev_type);
	if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		free_jstrarraylist(&list);
		throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
		return NULL;
	}
	insert_jstrarraylist(&list, info);

	memset(hexcharbuffer, '\0', sizeof(hexcharbuffer));
	snprintf(hexcharbuffer, 256, "%x", device_id);
	info = (*env)->NewStringUTF(env, hexcharbuffer);
	if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		free_jstrarraylist(&list);
		throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
		return NULL;
	}
	insert_jstrarraylist(&list, info);

	info = (*env)->NewStringUTF(env, serial_number);
	if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		free_jstrarraylist(&list);
		throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
		return NULL;
	}
	insert_jstrarraylist(&list, info);

	info = (*env)->NewStringUTF(env, description);
	if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		free_jstrarraylist(&list);
		throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
		return NULL;
	}
	insert_jstrarraylist(&list, info);

	strClass = (*env)->FindClass(env, "java/lang/String");
	if((strClass == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		free_jstrarraylist(&list);
		throw_serialcom_exception(env, 3, 0, E_FINDCLASSSSTRINGSTR);
		return NULL;
	}

	infoFound = (*env)->NewObjectArray(env, (jsize) list.index, strClass, NULL);
	if((infoFound == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		free_jstrarraylist(&list);
		throw_serialcom_exception(env, 3, 0, E_NEWOBJECTARRAYSTR);
		return NULL;
	}

	for (i=0; i < list.index; i++) {
		(*env)->SetObjectArrayElement(env, infoFound, i, list.base[i]);
		if((*env)->ExceptionOccurred(env)) {
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 3, 0, E_SETOBJECTARRAYSTR);
			return NULL;
		}
	}

	free_jstrarraylist(&list);
	return infoFound;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    getDriverVersion
 * Signature: (J)J
 *
 * @return driver version number otherwise -1 if error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jlong JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_getDriverVersion(JNIEnv *env, jobject obj, jlong handle) {
	FT_HANDLE ftHandle = (FT_HANDLE)handle;
	FT_STATUS ftStatus = 0;
	DWORD version = 0;

	ftStatus = FT_GetDriverVersion(ftHandle, &version);
	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return -1;
	}
	return (long)version;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    getLibraryVersion
 * Signature: ()J
 *
 * @return D2xx library version number otherwise -1 if error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jlong JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_getLibraryVersion(JNIEnv *env, jobject obj) {
	FT_STATUS ftStatus = 0;
	DWORD version = 0;

	ftStatus = FT_GetLibraryVersion(&version);
	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return -1;
	}
	return (long)version;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    getComPortNumber
 * Signature: (J)J
 *
 * @return COM port number if found, -2 if no COM Port is assigned to this device otherwise -1 if error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jlong JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_getComPortNumber(JNIEnv *env, jobject obj, jlong handle) {
	FT_HANDLE ftHandle = (FT_HANDLE)handle;
	FT_STATUS ftStatus = 0;
	DWORD com_port_number = 0;

	ftStatus = FT_GetComPortNumber(ftHandle, &com_port_number);
	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return -1;
	}

	if(com_port_number == -1) {
		return -2;
	}
	return (long)com_port_number;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    getStatus
 * Signature: (J)[J
 *
 * @return array containing number of bytes in rx buffer, number of bytes in tx buffer, modem event status otherwise NULL if error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jlongArray JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_getStatus(JNIEnv *env, jobject obj, jlong handle) {
	FT_HANDLE ftHandle = (FT_HANDLE)handle;
	FT_STATUS ftStatus = 0;
	DWORD event = 0;
	DWORD num_rx_bytes = 0;
	DWORD num_tx_bytes = 0;
	jlongArray current_status = NULL;
	jlong status[3] = {0};

	FT_GetStatus(ftHandle, &num_rx_bytes, &num_tx_bytes, &event);
	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return NULL;
	}

	status[0] = (long) num_rx_bytes;
	status[1] = (long) num_tx_bytes;
	status[2] = (long) event;

	current_status = (*env)->NewLongArray(env, 3);
	if((current_status == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_NEWLONGARRAYSTR);
		return NULL;
	}

	(*env)->SetLongArrayRegion(env, current_status, 0, 3, status);
	if((*env)->ExceptionOccurred(env)) {
		throw_serialcom_exception(env, 3, 0, E_SETLONGARRREGIONSTR);
		return NULL;
	}

	return current_status;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    setChars
 * Signature: (JCCCC)I
 *
 * @return 0 on success otherwise -1 if error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_setChars(JNIEnv *env, jobject obj, jlong handle,
		jchar eventChar, jchar eventEnable, jchar errorChar, jchar errorEnable) {
	FT_HANDLE ftHandle = (FT_HANDLE)handle;
	FT_STATUS ftStatus = 0;

	ftStatus = FT_SetChars(ftHandle, eventChar, eventEnable, errorChar, errorEnable);
	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return -1;
	}
	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    setBreakOn
 * Signature: (J)I
 *
 * @return 0 on success otherwise -1 if error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_setBreakOn(JNIEnv *env, jobject obj, jlong handle) {
	FT_HANDLE ftHandle = (FT_HANDLE)handle;
	FT_STATUS ftStatus = 0;
	ftStatus = FT_SetBreakOn(ftHandle);
	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return -1;
	}
	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    setBreakOff
 * Signature: (J)I
 *
 * @return 0 on success otherwise -1 if error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_setBreakOff(JNIEnv *env, jobject obj, jlong handle) {
	FT_HANDLE ftHandle = (FT_HANDLE)handle;
	FT_STATUS ftStatus = 0;
	ftStatus = FT_SetBreakOff(ftHandle);
	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return -1;
	}
	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    purge
 * Signature: (JZZ)I
 *
 * @return 0 on success otherwise -1 if error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_purge(JNIEnv *env, jobject obj, jlong handle,
		jboolean purgeTxBuffer, jboolean purgeRxBuffer) {

	FT_HANDLE ftHandle = (FT_HANDLE)handle;
	FT_STATUS ftStatus = 0;

	if((purgeTxBuffer == JNI_TRUE) && (purgeRxBuffer == JNI_TRUE)) {
		ftStatus = FT_Purge(ftHandle, FT_PURGE_RX | FT_PURGE_TX);
	}else if(purgeTxBuffer == JNI_TRUE) {
		ftStatus = FT_Purge(ftHandle, FT_PURGE_TX);
	}else if(purgeRxBuffer == JNI_TRUE) {
		ftStatus = FT_Purge(ftHandle, FT_PURGE_RX);
	}else {
	}

	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return -1;
	}
	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    resetDevice
 * Signature: (J)I
 *
 * @return 0 on success otherwise -1 if error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_resetDevice(JNIEnv *env, jobject obj, jlong handle) {
	FT_HANDLE ftHandle = (FT_HANDLE)handle;
	FT_STATUS ftStatus = 0;
	ftStatus = FT_ResetDevice(ftHandle);
	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return -1;
	}
	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    resetPort
 * Signature: (J)I
 *
 * @return 0 on success otherwise -1 if error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_resetPort(JNIEnv *env, jobject obj, jlong handle) {
	FT_HANDLE ftHandle = (FT_HANDLE)handle;
	FT_STATUS ftStatus = 0;
	ftStatus = FT_ResetPort(ftHandle);
	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return -1;
	}
	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    cyclePort
 * Signature: (J)I
 *
 * @return 0 on success otherwise -1 if error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_cyclePort(JNIEnv *env, jobject obj, jlong handle) {
	FT_HANDLE ftHandle = (FT_HANDLE)handle;
	FT_STATUS ftStatus = 0;
	ftStatus = FT_CyclePort(ftHandle);
	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return -1;
	}
	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    rescan
 * Signature: ()I
 *
 * @return 0 on success otherwise -1 if error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_rescan(JNIEnv *env, jobject obj) {
	FT_STATUS ftStatus = 0;
	ftStatus = FT_Rescan();
	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return -1;
	}
	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    reload
 * Signature: (II)I
 *
 * @return 0 on success otherwise -1 if error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_reload(JNIEnv *env, jobject obj, jint vid, jint pid) {
	FT_STATUS ftStatus = 0;
	ftStatus = FT_Reload((WORD)vid, (WORD)pid);
	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return -1;
	}
	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    setResetPipeRetryCount
 * Signature: (JI)I
 *
 * @return 0 on success otherwise -1 if error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_setResetPipeRetryCount(JNIEnv *env, jobject obj, jlong handle, jint count) {
	FT_HANDLE ftHandle = (FT_HANDLE)handle;
	FT_STATUS ftStatus = 0;
	ftStatus = FT_SetResetPipeRetryCount(ftHandle, (DWORD)count);
	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return -1;
	}
	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    stopInTask
 * Signature: (J)I
 *
 * @return 0 on success otherwise -1 if error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_stopInTask(JNIEnv *env, jobject obj, jlong handle) {
	FT_HANDLE ftHandle = (FT_HANDLE)handle;
	FT_STATUS ftStatus = 0;
	ftStatus = FT_StopInTask(ftHandle);
	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return -1;
	}
	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    restartInTask
 * Signature: (J)I
 *
 * @return 0 on success otherwise -1 if error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_restartInTask(JNIEnv *env, jobject obj, jlong handle) {
	FT_HANDLE ftHandle = (FT_HANDLE)handle;
	FT_STATUS ftStatus = 0;
	ftStatus = FT_RestartInTask(ftHandle);
	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return -1;
	}
	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    setDeadmanTimeout
 * Signature: (JI)I
 *
 * @return 0 on success otherwise -1 if error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_setDeadmanTimeout(JNIEnv *env, jobject obj, jlong handle, jint timeout) {
	FT_HANDLE ftHandle = (FT_HANDLE)handle;
	FT_STATUS ftStatus = 0;
	ftStatus = FT_SetDeadmanTimeout(ftHandle, (DWORD)timeout);
	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return -1;
	}
	return 0;
}


/* ********************* EEPROM Programming Interface Functions ******************** */


/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    readEE
 * Signature: (JI)I
 *
 * @return value at offset on success otherwise -1 if error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_readEE
  (JNIEnv *env, jobject obj, jlong handle, jint offset) {
	WORD value;
	FT_STATUS ftStatus = 0;

	ftStatus = FT_ReadEE((FT_HANDLE) handle, (DWORD) offset, &value);
	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return -1;
	}

	return (jint) value;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    writeEE
 * Signature: (JII)I
 *
 * @return 0 on success otherwise -1 if error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_writeEE
  (JNIEnv *env, jobject obj, jlong handle, jint offset, jint value) {
	FT_STATUS ftStatus = 0;
	ftStatus = FT_WriteEE((FT_HANDLE) handle, (DWORD) offset, (WORD) value);
	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return -1;
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    eraseEE
 * Signature: (J)I
 *
 * @return 0 on success otherwise -1 if error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_eraseEE
  (JNIEnv *env, jobject obj, jlong handle) {
	FT_STATUS ftStatus = 0;

	ftStatus = FT_EraseEE((FT_HANDLE) handle);
	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return -1;
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    eeRead
 * Signature: (JI[B[B[B[B)[I
 */
JNIEXPORT jintArray JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_eeRead
  (JNIEnv *env, jobject obj, jlong handle, jint version, jbyteArray manufacturer, jbyteArray manufacturerID,
		  jbyteArray description, jbyteArray serialNumber) {

	FT_STATUS ftStatus = 0;
	FT_PROGRAM_DATA ftData;
	char manufacturer_buf[32];
	char manufacturer_idbuf[16];
	char description_buf[64];
	char serialNumber_buf[16];

	memset(manufacturer_buf, '\0', 32);
	memset(manufacturer_idbuf, '\0', 16);
	memset(description_buf, '\0', 64);
	memset(serialNumber_buf, '\0', 16);

	ftData.Signature1 = 0x00000000;
	ftData.Signature2 = 0xffffffff;
	ftData.Version = (DWORD) version;

	ftData.Manufacturer = manufacturer_buf;
	ftData.ManufacturerId = manufacturer_idbuf;
	ftData.Description = description_buf;
	ftData.SerialNumber = serialNumber_buf;

	ftStatus = FT_EE_Read((FT_HANDLE) handle, &ftData);
	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return NULL;
	}


}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    eeProgram
 * Signature: (JILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[I)I
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_eeProgram
  (JNIEnv *, jobject, jlong, jint, jstring, jstring, jstring, jstring, jintArray);

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    eeProgramEx
 * Signature: (JILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[I)I
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_eeProgramEx
  (JNIEnv *, jobject, jlong, jint, jstring, jstring, jstring, jstring, jintArray);

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    eeUAsize
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_eeUAsize
  (JNIEnv *, jobject, jlong);

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    eeUAread
 * Signature: (J[BI)I
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_eeUAread
  (JNIEnv *, jobject, jlong, jbyteArray, jint);

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    eeUAwrite
 * Signature: (J[BI)I
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_eeUAwrite
  (JNIEnv *, jobject, jlong, jbyteArray, jint);

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    eepromRead
 * Signature: (JI[B[B[B[B)[I
 */
JNIEXPORT jintArray JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_eepromRead
  (JNIEnv *, jobject, jlong, jint, jbyteArray, jbyteArray, jbyteArray, jbyteArray);

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    eepromProgram
 * Signature: (JI[ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_eepromProgram
  (JNIEnv *, jobject, jlong, jint, jintArray, jstring, jstring, jstring, jstring);


/* ********************* Extended API Functions *********************/


/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    setLatencyTimer
 * Signature: (JI)I
 *
 * @return 0 on success otherwise -1 if error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_setLatencyTimer
  (JNIEnv *env, jobject obj, jlong handle, jint timerValue) {

	FT_STATUS ftStatus = 0;
	ftStatus = FT_SetLatencyTimer((FT_HANDLE) handle, (UCHAR) timerValue);
	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return -1;
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    getLatencyTimer
 * Signature: (J)I
 *
 * @return latency time value on success otherwise -1 if error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_getLatencyTimer
  (JNIEnv *env, jobject obj, jlong handle) {

	UCHAR latency_timer_value = 0;
	FT_STATUS ftStatus = 0;
	ftStatus = FT_GetLatencyTimer((FT_HANDLE) handle, &latency_timer_value);
	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return -1;
	}

	return (jint)latency_timer_value;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    setBitMode
 * Signature: (JII)I
 *
 * @return 0 on success otherwise -1 if error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_setBitMode
  (JNIEnv *env, jobject obj, jlong handle, jint mask, jint mode) {

	FT_STATUS ftStatus = 0;
	ftStatus = FT_SetBitMode((FT_HANDLE) handle, (UCHAR) mask, (UCHAR) mode);
	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return -1;
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    getBitMode
 * Signature: (J)I
 *
 * @return bit mode value on success otherwise -1 if error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_getBitMode
  (JNIEnv *env, jobject obj, jlong handle) {

	FT_STATUS ftStatus = 0;
	UCHAR bit_mode;
	ftStatus = FT_GetBitMode((FT_HANDLE) handle, &bit_mode);
	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return -1;
	}

	return (jint) bit_mode;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    setUSBParameters
 * Signature: (JII)I
 *
 * @return 0 on success otherwise -1 if error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_setUSBParameters
  (JNIEnv *env, jobject obj, jlong handle, jint inTransferSize, jint outTransferSize) {

	FT_STATUS ftStatus = 0;
	ftStatus = FT_SetUSBParameters((FT_HANDLE) handle, (DWORD) inTransferSize, (DWORD) outTransferSize);
	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return -1;
	}

	return 0;
}


/* ********************* FT-Win32 API Functions ******************** */


/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    w32CreateFile
 * Signature: (Ljava/lang/String;Ljava/lang/String;JIIZ)J
 */
JNIEXPORT jlong JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_w32CreateFile
  (JNIEnv *, jobject, jstring, jstring, jlong, jint, jint, jboolean);

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    w32CloseHandle
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_w32CloseHandle
  (JNIEnv *, jobject, jlong);

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    w32ReadFile
 * Signature: (J[BI)I
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_w32ReadFile
  (JNIEnv *, jobject, jlong, jbyteArray, jint);

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    w32WriteFile
 * Signature: (J[BI)I
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_w32WriteFile
  (JNIEnv *, jobject, jlong, jbyteArray, jint);

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    w32GetOverlappedResult
 * Signature: (JZ)I
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_w32GetOverlappedResult
  (JNIEnv *, jobject, jlong, jboolean);

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    w32EscapeCommFunction
 * Signature: (JS)I
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_w32EscapeCommFunction
  (JNIEnv *, jobject, jlong, jshort);

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    w32GetCommModemStatus
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_w32GetCommModemStatus
  (JNIEnv *, jobject, jlong);

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    w32SetupComm
 * Signature: (JII)I
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_w32SetupComm
  (JNIEnv *, jobject, jlong, jint, jint);

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    w32SetCommState
 * Signature: (J[Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_w32SetCommState
  (JNIEnv *, jobject, jlong, jobjectArray);

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    w32GetCommState
 * Signature: (J)[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_w32GetCommState
  (JNIEnv *, jobject, jlong);

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    w32SetCommTimeouts
 * Signature: (JIIIII)I
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_w32SetCommTimeouts
  (JNIEnv *, jobject, jlong, jint, jint, jint, jint, jint);

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    w32GetCommTimeouts
 * Signature: (J)[I
 */
JNIEXPORT jintArray JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_w32GetCommTimeouts
  (JNIEnv *, jobject, jlong);

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    w32SetCommBreak
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_w32SetCommBreak
  (JNIEnv *, jobject, jlong);

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    w32ClearCommBreak
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_w32ClearCommBreak
  (JNIEnv *, jobject, jlong);

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    w32SetCommMask
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_w32SetCommMask
  (JNIEnv *, jobject, jlong, jint);

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    w32GetCommMask
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_w32GetCommMask
  (JNIEnv *, jobject, jlong);

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    w32WaitCommEvent
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_w32WaitCommEvent
  (JNIEnv *, jobject, jlong, jint);

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    w32PurgeComm
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_w32PurgeComm
  (JNIEnv *, jobject, jlong, jint);

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    w32GetLastError
 * Signature: (J)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_w32GetLastError
  (JNIEnv *, jobject, jlong);

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    w32ClearCommError
 * Signature: (J)[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_w32ClearCommError
  (JNIEnv *, jobject, jlong);

#endif
