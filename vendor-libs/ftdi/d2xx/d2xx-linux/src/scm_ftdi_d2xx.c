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
#endif

#if defined (_WIN32) && !defined(UNIX)
#include <windows.h>
#include <process.h>
#include <tchar.h>
#endif

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
 * @return valid handle on success otherwise -1 if an error occurs.
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

	if(dwFlags == SCM_OPEN_BY_SERIAL_NUMBER) {
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
		(*env)->ReleaseStringUTFChars(env, serialOrDescription, ser_or_desc);
	}else if(dwFlags == SCM_OPEN_BY_DESCRIPTION) {
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
		(*env)->ReleaseStringUTFChars(env, serialOrDescription, ser_or_desc);
	}else if(dwFlags == SCM_OPEN_BY_LOCATION) {
		if(locationId < 0) {
			throw_serialcom_exception(env, 3, 0, E_IllegalARG);
			return -1;
		}
		ftStatus = FT_OpenEx((PVOID)locationId, FT_OPEN_BY_LOCATION, &ftHandle);
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
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_close(JNIEnv *env, jobject obj, jlong handle) {
	FT_STATUS ftStatus = 0;
	ftStatus = FT_Close((FT_HANDLE) handle);
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
 * @return number of bytes read on success otherwise -1 if an error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_read(JNIEnv *env, jobject obj,
		jlong handle, jbyteArray buffer, jint count) {

	FT_STATUS ftStatus = 0;
	DWORD num_of_bytes_read;

	jbyte* data_buffer = (jbyte *) calloc(count, sizeof(jbyte));
	if(data_buffer == NULL) {
		throw_serialcom_exception(env, 3, 0, E_CALLOCSTR);
		return -1;
	}

	ftStatus = FT_Read((FT_HANDLE)handle, data_buffer, count, &num_of_bytes_read);
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
 * @return number of bytes written to the device on success otherwise -1 if an error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_write(JNIEnv *env, jobject obj,
		jlong handle, jbyteArray buffer, jint count) {

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

	ftStatus = FT_Write((FT_HANDLE)handle, data_buffer, count, &num_of_bytes_written);
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
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_setBaudRate(JNIEnv *env, jobject obj, jlong handle, jint baudRate) {
	FT_STATUS ftStatus = 0;
	ftStatus = FT_SetBaudRate((FT_HANDLE)handle, (int)baudRate);
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
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_setDivisor(JNIEnv *env, jobject obj, jlong handle, jint divisor) {
	FT_STATUS ftStatus = 0;
	ftStatus = FT_SetDivisor((FT_HANDLE)handle, (USHORT)divisor);
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
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_setDataCharacteristics(JNIEnv *env, jobject obj,
		jlong handle, jint databits, jint stopbits, jint parity) {

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


	ftStatus = FT_SetDataCharacteristics((FT_HANDLE)handle, dbits, sbits, par);
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
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_setTimeouts(JNIEnv *env, jobject obj,
		jlong handle, jlong read_timeout, jlong write_timeout) {

	FT_STATUS ftStatus = 0;
	ftStatus = FT_SetTimeouts((FT_HANDLE)handle, (ULONG)read_timeout, (ULONG)write_timeout);
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
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_setFlowControl(JNIEnv *env, jobject obj,
		jlong handle, jint mode, jchar xon, jchar off) {

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

	ftStatus = FT_SetFlowControl((FT_HANDLE)handle, flowctrl, (UCHAR)xon, (UCHAR)off);
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
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_setDTR(JNIEnv *env, jobject obj, jlong handle) {
	FT_STATUS ftStatus = 0;
	ftStatus = FT_SetDtr((FT_HANDLE)handle);
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
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_clearDTR(JNIEnv *env, jobject obj, jlong handle) {
	FT_STATUS ftStatus = 0;
	ftStatus = FT_ClrDtr((FT_HANDLE)handle);
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
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_setRTS(JNIEnv *env, jobject obj, jlong handle) {
	FT_STATUS ftStatus = 0;
	ftStatus = FT_SetRts((FT_HANDLE)handle);
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
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_clearRTS(JNIEnv *env, jobject obj, jlong handle) {
	FT_STATUS ftStatus = 0;
	ftStatus = FT_ClrRts((FT_HANDLE)handle);
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
 * @return bit mapped status value otherwise -1 if an error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_getModemStatus(JNIEnv *env, jobject obj, jlong handle) {
	FT_STATUS ftStatus = 0;
	DWORD status = 0;
	ftStatus = FT_GetModemStatus((FT_HANDLE)handle, &status);
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
 * @return number of bytes in receive queue otherwise -1 if an error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_getQueueStatus(JNIEnv *env, jobject obj, jlong handle) {
	FT_STATUS ftStatus = 0;
	DWORD num_bytes_in_rx_buffer = 0;
	ftStatus = FT_GetModemStatus((FT_HANDLE)handle, &num_bytes_in_rx_buffer);
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
 * @return array of string containing info about the requested device or NULL if an error occurs.
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
 * @return driver version number otherwise -1 if an error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jlong JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_getDriverVersion(JNIEnv *env, jobject obj, jlong handle) {

	FT_STATUS ftStatus = 0;
	DWORD version = 0;

	ftStatus = FT_GetDriverVersion((FT_HANDLE)handle, &version);
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
 * @return D2xx library version number otherwise -1 if an error occurs.
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
 * @return COM port number if found, -2 if no COM Port is assigned to this device otherwise -1 if an error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jlong JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_getComPortNumber(JNIEnv *env, jobject obj, jlong handle) {

	FT_STATUS ftStatus = 0;
	DWORD com_port_number = 0;

	ftStatus = FT_GetComPortNumber((FT_HANDLE)handle, &com_port_number);
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
 * @return array containing number of bytes in rx buffer, number of bytes in tx buffer, modem event status otherwise NULL if an error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jlongArray JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_getStatus(JNIEnv *env, jobject obj, jlong handle) {

	FT_STATUS ftStatus = 0;
	DWORD event = 0;
	DWORD num_rx_bytes = 0;
	DWORD num_tx_bytes = 0;
	jlongArray current_status = NULL;
	jlong status[3] = {0};

	FT_GetStatus((FT_HANDLE)handle, &num_rx_bytes, &num_tx_bytes, &event);
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
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_setChars(JNIEnv *env, jobject obj, jlong handle,
		jchar eventChar, jchar eventEnable, jchar errorChar, jchar errorEnable) {

	FT_STATUS ftStatus = 0;
	ftStatus = FT_SetChars((FT_HANDLE)handle, eventChar, eventEnable, errorChar, errorEnable);
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
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_setBreakOn(JNIEnv *env, jobject obj, jlong handle) {
	FT_STATUS ftStatus = 0;
	ftStatus = FT_SetBreakOn((FT_HANDLE)handle);
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
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_setBreakOff(JNIEnv *env, jobject obj, jlong handle) {
	FT_STATUS ftStatus = 0;
	ftStatus = FT_SetBreakOff((FT_HANDLE)handle);
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
 * @return 0 on success otherwise -1 if an error occurs.
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
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_resetDevice(JNIEnv *env, jobject obj, jlong handle) {
	FT_STATUS ftStatus = 0;
	ftStatus = FT_ResetDevice((FT_HANDLE)handle);
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
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_resetPort(JNIEnv *env, jobject obj, jlong handle) {
	FT_STATUS ftStatus = 0;
	ftStatus = FT_ResetPort((FT_HANDLE)handle);
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
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_cyclePort(JNIEnv *env, jobject obj, jlong handle) {
	FT_STATUS ftStatus = 0;
	ftStatus = FT_CyclePort((FT_HANDLE)handle);
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
 * @return 0 on success otherwise -1 if an error occurs.
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
 * @return 0 on success otherwise -1 if an error occurs.
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
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_setResetPipeRetryCount(JNIEnv *env, jobject obj, jlong handle, jint count) {
	FT_STATUS ftStatus = 0;
	ftStatus = FT_SetResetPipeRetryCount((FT_HANDLE)handle, (DWORD)count);
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
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_stopInTask(JNIEnv *env, jobject obj, jlong handle) {
	FT_STATUS ftStatus = 0;
	ftStatus = FT_StopInTask((FT_HANDLE)handle);
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
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_restartInTask(JNIEnv *env, jobject obj, jlong handle) {
	FT_STATUS ftStatus = 0;
	ftStatus = FT_RestartInTask((FT_HANDLE)handle);
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
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_setDeadmanTimeout(JNIEnv *env, jobject obj, jlong handle, jint timeout) {
	FT_STATUS ftStatus = 0;
	ftStatus = FT_SetDeadmanTimeout((FT_HANDLE)handle, (DWORD)timeout);
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
 * @return value at offset on success otherwise -1 if an error occurs.
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
 * @return 0 on success otherwise -1 if an error occurs.
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
 * @return 0 on success otherwise -1 if an error occurs.
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
 * Signature: (JI[C[C[C[C)[I
 *
 * @return array of data read from eeprom on success otherwise NULL if an error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jintArray JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_eeRead
  (JNIEnv *env, jobject obj, jlong handle, jint version, jcharArray manufacturer,
		  jcharArray manufacturerID, jcharArray description, jcharArray serialNumber) {

	int x = 0;
	FT_STATUS ftStatus = 0;
	FT_PROGRAM_DATA ftData;
	jintArray info = NULL;
	jint values[130];

	char manufacturer_buf[32];
	char manufacturer_idbuf[16];
	char description_buf[64];
	char serialNumber_buf[16];
	jchar manufacturer_bufj[32];
	jchar manufacturer_idbufj[16];
	jchar description_bufj[64];
	jchar serialNumber_bufj[16];

	memset(manufacturer_buf, '\0', sizeof(manufacturer_buf));
	memset(manufacturer_idbuf, '\0', sizeof(manufacturer_idbuf));
	memset(description_buf, '\0', sizeof(description_buf));
	memset(serialNumber_buf, '\0', sizeof(serialNumber_buf));
	memset(manufacturer_bufj, '\0', sizeof(manufacturer_bufj));
	memset(manufacturer_idbufj, '\0', sizeof(manufacturer_idbufj));
	memset(description_bufj, '\0', sizeof(description_bufj));
	memset(serialNumber_bufj, '\0', sizeof(serialNumber_bufj));

	ftData.Signature1     = 0x00000000;
	ftData.Signature2     = 0xffffffff;
	ftData.Version        = (DWORD) version;
	ftData.Manufacturer   = manufacturer_buf;
	ftData.ManufacturerId = manufacturer_idbuf;
	ftData.Description    = description_buf;
	ftData.SerialNumber   = serialNumber_buf;

	ftStatus = FT_EE_Read((FT_HANDLE) handle, &ftData);
	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return NULL;
	}

	for(x=0; x<32; x++) {
		manufacturer_bufj[x] = (jchar) manufacturer_buf[x];
	}
	(*env)->SetCharArrayRegion(env, manufacturer, 0, 32, manufacturer_bufj);
	if((*env)->ExceptionOccurred(env)) {
		throw_serialcom_exception(env, 3, 0, E_SETCHARARRREGIONSTR);
		return NULL;
	}

	for(x=0; x<16; x++) {
		manufacturer_idbufj[x] = (jchar) manufacturer_idbuf[x];
	}
	(*env)->SetCharArrayRegion(env, manufacturerID, 0, 16, manufacturer_idbufj);
	if((*env)->ExceptionOccurred(env)) {
		throw_serialcom_exception(env, 3, 0, E_SETCHARARRREGIONSTR);
		return NULL;
	}

	for(x=0; x<64; x++) {
		description_bufj[x] = (jchar) description_buf[x];
	}
	(*env)->SetCharArrayRegion(env, description, 0, 64, description_bufj);
	if((*env)->ExceptionOccurred(env)) {
		throw_serialcom_exception(env, 3, 0, E_SETCHARARRREGIONSTR);
		return NULL;
	}

	for(x=0; x<16; x++) {
		serialNumber_bufj[x] = (jchar) serialNumber_buf[x];
	}
	(*env)->SetCharArrayRegion(env, serialNumber, 0, 16, serialNumber_bufj);
	if((*env)->ExceptionOccurred(env)) {
		throw_serialcom_exception(env, 3, 0, E_SETCHARARRREGIONSTR);
		return NULL;
	}

	values[0] = (jint) ftData.Signature1;
	values[1] = (jint) ftData.Signature2;
	values[2] = (jint) ftData.Version;
	values[3] = (jint) ftData.VendorId;
	values[4] = (jint) ftData.ProductId;
	values[5] = (jint) 0;
	values[6] = (jint) 0;
	values[7] = (jint) 0;
	values[8] = (jint) 0;
	values[9] = (jint) ftData.MaxPower;
	values[10] = (jint) ftData.PnP;
	values[11] = (jint) ftData.SelfPowered;
	values[12] = (jint) ftData.RemoteWakeup;

	/* Rev4 (FT232B) extensions */
	values[13] = (jint) ftData.Rev4;
	values[14] = (jint) ftData.IsoIn;
	values[15] = (jint) ftData.IsoOut;
	values[16] = (jint) ftData.PullDownEnable;
	values[17] = (jint) ftData.SerNumEnable;
	values[18] = (jint) ftData.USBVersionEnable;
	values[19] = (jint) ftData.USBVersion;

	/* Rev 5 (FT2232) extensions */
	values[20] = (jint) ftData.Rev5;
	values[21] = (jint) ftData.IsoInA;
	values[22] = (jint) ftData.IsoInB;
	values[23] = (jint) ftData.IsoOutA;
	values[24] = (jint) ftData.IsoOutB;
	values[25] = (jint) ftData.PullDownEnable5;
	values[26] = (jint) ftData.SerNumEnable5;
	values[27] = (jint) ftData.USBVersionEnable5;
	values[28] = (jint) ftData.USBVersion5;
	values[29] = (jint) ftData.AIsHighCurrent;
	values[30] = (jint) ftData.BIsHighCurrent;
	values[31] = (jint) ftData.IFAIsFifo;
	values[32] = (jint) ftData.IFAIsFifoTar;
	values[33] = (jint) ftData.IFAIsFastSer;
	values[34] = (jint) ftData.AIsVCP;
	values[35] = (jint) ftData.IFBIsFifo;
	values[36] = (jint) ftData.IFBIsFifoTar;
	values[37] = (jint) ftData.IFBIsFastSer;
	values[38] = (jint) ftData.BIsVCP;

	/* Rev 6 (FT232R) extensions */
	values[39] = (jint) ftData.UseExtOsc;
	values[40] = (jint) ftData.HighDriveIOs;
	values[41] = (jint) ftData.EndpointSize;
	values[42] = (jint) ftData.PullDownEnableR;
	values[43] = (jint) ftData.SerNumEnableR;
	values[44] = (jint) ftData.InvertTXD;
	values[45] = (jint) ftData.InvertRXD;
	values[46] = (jint) ftData.InvertRTS;
	values[47] = (jint) ftData.InvertCTS;
	values[48] = (jint) ftData.InvertDTR;
	values[49] = (jint) ftData.InvertDSR;
	values[50] = (jint) ftData.InvertDCD;
	values[51] = (jint) ftData.InvertRI;
	values[52] = (jint) ftData.Cbus0;
	values[53] = (jint) ftData.Cbus1;
	values[54] = (jint) ftData.Cbus2;
	values[55] = (jint) ftData.Cbus3;
	values[56] = (jint) ftData.Cbus4;
	values[57] = (jint) ftData.RIsD2XX;

	/* Rev 7 (FT2232H) Extensions */
	values[58] = (jint) ftData.PullDownEnable7;
	values[59] = (jint) ftData.SerNumEnable7;
	values[60] = (jint) ftData.ALSlowSlew;
	values[61] = (jint) ftData.ALSchmittInput;
	values[62] = (jint) ftData.ALDriveCurrent;
	values[63] = (jint) ftData.AHSlowSlew;
	values[64] = (jint) ftData.AHSchmittInput;
	values[65] = (jint) ftData.AHDriveCurrent;
	values[66] = (jint) ftData.BLSlowSlew;
	values[67] = (jint) ftData.BLSchmittInput;
	values[68] = (jint) ftData.BLDriveCurrent;
	values[69] = (jint) ftData.BHSlowSlew;
	values[70] = (jint) ftData.BHSchmittInput;
	values[71] = (jint) ftData.BHDriveCurrent;
	values[72] = (jint) ftData.IFAIsFifo7;
	values[73] = (jint) ftData.IFAIsFifoTar7;
	values[74] = (jint) ftData.IFAIsFastSer7;
	values[75] = (jint) ftData.AIsVCP7;
	values[76] = (jint) ftData.IFBIsFifo7;
	values[77] = (jint) ftData.IFBIsFifoTar7;
	values[78] = (jint) ftData.IFBIsFastSer7;
	values[79] = (jint) ftData.BIsVCP7;
	values[80] = (jint) ftData.PowerSaveEnable;

	/* Rev 8 (FT4232H) Extensions */
	values[81]  = (jint) ftData.PullDownEnable8;
	values[82]  = (jint) ftData.SerNumEnable8;
	values[83]  = (jint) ftData.ASlowSlew;
	values[84]  = (jint) ftData.ASchmittInput;
	values[85]  = (jint) ftData.ADriveCurrent;
	values[86]  = (jint) ftData.BSlowSlew;
	values[87]  = (jint) ftData.BSchmittInput;
	values[88]  = (jint) ftData.BDriveCurrent;
	values[89]  = (jint) ftData.CSlowSlew;
	values[90]  = (jint) ftData.CSchmittInput;
	values[91]  = (jint) ftData.CDriveCurrent;
	values[92]  = (jint) ftData.DSlowSlew;
	values[93]  = (jint) ftData.DSchmittInput;
	values[94]  = (jint) ftData.DDriveCurrent;
	values[95]  = (jint) ftData.ARIIsTXDEN;
	values[96]  = (jint) ftData.BRIIsTXDEN;
	values[97]  = (jint) ftData.CRIIsTXDEN;
	values[98]  = (jint) ftData.DRIIsTXDEN;
	values[99]  = (jint) ftData.AIsVCP8;
	values[100] = (jint) ftData.BIsVCP8;
	values[101] = (jint) ftData.CIsVCP8;
	values[102] = (jint) ftData.DIsVCP8;

	/* Rev 9 (FT232H) Extensions */
	values[103] = (jint) ftData.PullDownEnableH;
	values[104] = (jint) ftData.SerNumEnableH;
	values[105] = (jint) ftData.ACSlowSlewH;
	values[106] = (jint) ftData.ACSchmittInputH;
	values[107] = (jint) ftData.ACDriveCurrentH;
	values[108] = (jint) ftData.ADSlowSlewH;
	values[109] = (jint) ftData.ADSchmittInputH;
	values[110] = (jint) ftData.ADDriveCurrentH;
	values[111] = (jint) ftData.Cbus0H;
	values[112] = (jint) ftData.Cbus1H;
	values[113] = (jint) ftData.Cbus2H;
	values[114] = (jint) ftData.Cbus3H;
	values[115] = (jint) ftData.Cbus4H;
	values[116] = (jint) ftData.Cbus5H;
	values[117] = (jint) ftData.Cbus6H;
	values[118] = (jint) ftData.Cbus7H;
	values[119] = (jint) ftData.Cbus8H;
	values[120] = (jint) ftData.Cbus9H;
	values[121] = (jint) ftData.IsFifoH;
	values[122] = (jint) ftData.IsFifoTarH;
	values[123] = (jint) ftData.IsFastSerH;
	values[124] = (jint) ftData.IsFT1248H;
	values[125] = (jint) ftData.FT1248CpolH;
	values[126] = (jint) ftData.FT1248LsbH;
	values[127] = (jint) ftData.FT1248FlowControlH;
	values[128] = (jint) ftData.IsVCPH;
	values[129] = (jint) ftData.PowerSaveEnableH;

	info = (*env)->NewIntArray(env, 130);
	if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_NEWINTARRAYSTR);
		return NULL;
	}

	(*env)->SetIntArrayRegion(env, info, 0, 130, values);
	if((*env)->ExceptionOccurred(env) != NULL) {
		throw_serialcom_exception(env, 3, 0, E_SETINTARRREGIONSTR);
		return NULL;
	}
	return info;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    eeProgram
 * Signature: (JLjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[I)I
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_eeProgram
  (JNIEnv *env, jobject obj, jlong handle, jstring manufacturer, jstring manufacturerID, jstring description,
		  jstring serialNumber, jintArray values) {

	FT_STATUS ftStatus = 0;
	FT_PROGRAM_DATA ftData;
	char *manufacturer_buf;
	char *manufacturer_idbuf;
	char *description_buf;
	char *serialNumber_buf;
	jint* data_values = NULL;

	data_values = (*env)->GetIntArrayElements(env, values, JNI_FALSE);
	if((data_values == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_GETINTARRELEMTSTR);
		return -1;
	}

	manufacturer_buf = (char *) (*env)->GetStringUTFChars(env, manufacturer, NULL);
	if((manufacturer_buf == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_GETSTRUTFCHARSTR);
		return -1;
	}
	manufacturer_idbuf = (char *) (*env)->GetStringUTFChars(env, manufacturerID, NULL);
	if((manufacturer_idbuf == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		(*env)->ReleaseStringUTFChars(env, manufacturer, manufacturer_buf);
		throw_serialcom_exception(env, 3, 0, E_GETSTRUTFCHARSTR);
		return -1;
	}
	description_buf = (char *) (*env)->GetStringUTFChars(env, description, NULL);
	if((description_buf == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		(*env)->ReleaseStringUTFChars(env, manufacturer, manufacturer_buf);
		(*env)->ReleaseStringUTFChars(env, manufacturerID, manufacturer_idbuf);
		throw_serialcom_exception(env, 3, 0, E_GETSTRUTFCHARSTR);
		return -1;
	}
	serialNumber_buf = (char *) (*env)->GetStringUTFChars(env, serialNumber, NULL);
	if((serialNumber_buf == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		(*env)->ReleaseStringUTFChars(env, manufacturer, manufacturer_buf);
		(*env)->ReleaseStringUTFChars(env, manufacturerID, manufacturer_idbuf);
		(*env)->ReleaseStringUTFChars(env, description, description_buf);
		throw_serialcom_exception(env, 3, 0, E_GETSTRUTFCHARSTR);
		return -1;
	}

	ftData.Signature1     = (DWORD) data_values[0]; /* 0x00000000 */
	ftData.Signature2     = (DWORD) data_values[1]; /* 0xffffffff */
	ftData.Version        = (DWORD) data_values[2];
	ftData.VendorId       = (WORD) data_values[3];
	ftData.ProductId      = (WORD) data_values[4];
	ftData.Manufacturer   = manufacturer_buf;
	ftData.ManufacturerId = manufacturer_idbuf;
	ftData.Description    = description_buf;
	ftData.SerialNumber   = serialNumber_buf;
	ftData.MaxPower       = (WORD) data_values[9];
	ftData.PnP            = (WORD) data_values[10];
	ftData.SelfPowered    = (WORD) data_values[11];
	ftData.RemoteWakeup   = (WORD) data_values[12];

	/* Rev4 (FT232B) extensions */
	ftData.Rev4             = (UCHAR) data_values[13];
	ftData.IsoIn            = (UCHAR) data_values[14];
	ftData.IsoOut           = (UCHAR) data_values[15];
	ftData.PullDownEnable   = (UCHAR) data_values[16];
	ftData.SerNumEnable     = (UCHAR) data_values[17];
	ftData.USBVersionEnable = (UCHAR) data_values[18];
	ftData.USBVersion       = (WORD)  data_values[19];

	/* Rev 5 (FT2232) extensions */
	ftData.Rev5              = (UCHAR) data_values[20];
	ftData.IsoInA            = (UCHAR) data_values[21];
	ftData.IsoInB            = (UCHAR) data_values[22];
	ftData.IsoOutA           = (UCHAR) data_values[23];
	ftData.IsoOutB           = (UCHAR) data_values[24];
	ftData.PullDownEnable5   = (UCHAR) data_values[25];
	ftData.SerNumEnable5     = (UCHAR) data_values[26];
	ftData.USBVersionEnable5 = (UCHAR) data_values[27];
	ftData.USBVersion5       = (WORD)  data_values[28];
	ftData.AIsHighCurrent    = (UCHAR) data_values[29];
	ftData.BIsHighCurrent    = (UCHAR) data_values[30];
	ftData.IFAIsFifo         = (UCHAR) data_values[31];
	ftData.IFAIsFifoTar      = (UCHAR) data_values[32];
	ftData.IFAIsFastSer      = (UCHAR) data_values[33];
	ftData.AIsVCP            = (UCHAR) data_values[34];
	ftData.IFBIsFifo         = (UCHAR) data_values[35];
	ftData.IFBIsFifoTar      = (UCHAR) data_values[36];
	ftData.IFBIsFastSer      = (UCHAR) data_values[37];
	ftData.BIsVCP            = (UCHAR) data_values[38];

	/* Rev 6 (FT232R) extensions */
	ftData.UseExtOsc       = (UCHAR) data_values[39];
	ftData.HighDriveIOs    = (UCHAR) data_values[40];
	ftData.EndpointSize    = (UCHAR) data_values[41];
	ftData.PullDownEnableR = (UCHAR) data_values[42];
	ftData.SerNumEnableR   = (UCHAR) data_values[43];
	ftData.InvertTXD       = (UCHAR) data_values[44];
	ftData.InvertRXD       = (UCHAR) data_values[45];
	ftData.InvertRTS       = (UCHAR) data_values[46];
	ftData.InvertCTS       = (UCHAR) data_values[47];
	ftData.InvertDTR       = (UCHAR) data_values[48];
	ftData.InvertDSR       = (UCHAR) data_values[49];
	ftData.InvertDCD       = (UCHAR) data_values[50];
	ftData.InvertRI        = (UCHAR) data_values[51];
	ftData.Cbus0           = (UCHAR) data_values[52];
	ftData.Cbus1           = (UCHAR) data_values[53];
	ftData.Cbus2           = (UCHAR) data_values[54];
	ftData.Cbus3           = (UCHAR) data_values[55];
	ftData.Cbus4           = (UCHAR) data_values[56];
	ftData.RIsD2XX         = (UCHAR) data_values[57];

	/* Rev 7 (FT2232H) Extensions */
	ftData.PullDownEnable7  = (UCHAR) data_values[58];
	ftData.SerNumEnable7    = (UCHAR) data_values[59];
	ftData.ALSlowSlew       = (UCHAR) data_values[60];
	ftData.ALSchmittInput   = (UCHAR) data_values[61];
	ftData.ALDriveCurrent   = (UCHAR) data_values[62];
	ftData.AHSlowSlew       = (UCHAR) data_values[63];
	ftData.AHSchmittInput   = (UCHAR) data_values[64];
	ftData.AHDriveCurrent   = (UCHAR) data_values[65];
	ftData.BLSlowSlew       = (UCHAR) data_values[66];
	ftData.BLSchmittInput   = (UCHAR) data_values[67];
	ftData.BLDriveCurrent   = (UCHAR) data_values[68];
	ftData.BHSlowSlew       = (UCHAR) data_values[69];
	ftData.BHSchmittInput   = (UCHAR) data_values[70];
	ftData.BHDriveCurrent   = (UCHAR) data_values[71];
	ftData.IFAIsFifo7       = (UCHAR) data_values[72];
	ftData.IFAIsFifoTar7    = (UCHAR) data_values[73];
	ftData.IFAIsFastSer7    = (UCHAR) data_values[74];
	ftData.AIsVCP7          = (UCHAR) data_values[75];
	ftData.IFBIsFifo7       = (UCHAR) data_values[76];
	ftData.IFBIsFifoTar7    = (UCHAR) data_values[77];
	ftData.IFBIsFastSer7    = (UCHAR) data_values[78];
	ftData.BIsVCP7          = (UCHAR) data_values[79];
	ftData.PowerSaveEnable  = (UCHAR) data_values[80];

	/* Rev 8 (FT4232H) Extensions */
	ftData.PullDownEnable8 = (UCHAR) data_values[81];
	ftData.SerNumEnable8   = (UCHAR) data_values[82];
	ftData.ASlowSlew       = (UCHAR) data_values[83];
	ftData.ASchmittInput   = (UCHAR) data_values[84];
	ftData.ADriveCurrent   = (UCHAR) data_values[85];
	ftData.BSlowSlew       = (UCHAR) data_values[86];
	ftData.BSchmittInput   = (UCHAR) data_values[87];
	ftData.BDriveCurrent   = (UCHAR) data_values[88];
	ftData.CSlowSlew       = (UCHAR) data_values[89];
	ftData.CSchmittInput   = (UCHAR) data_values[90];
	ftData.CDriveCurrent   = (UCHAR) data_values[91];
	ftData.DSlowSlew       = (UCHAR) data_values[92];
	ftData.DSchmittInput   = (UCHAR) data_values[93];
	ftData.DDriveCurrent   = (UCHAR) data_values[94];
	ftData.ARIIsTXDEN      = (UCHAR) data_values[95];
	ftData.BRIIsTXDEN      = (UCHAR) data_values[96];
	ftData.CRIIsTXDEN      = (UCHAR) data_values[97];
	ftData.DRIIsTXDEN      = (UCHAR) data_values[98];
	ftData.AIsVCP8         = (UCHAR) data_values[99];
	ftData.BIsVCP8         = (UCHAR) data_values[100];
	ftData.CIsVCP8         = (UCHAR) data_values[101];
	ftData.DIsVCP8         = (UCHAR) data_values[102];

	/* Rev 9 (FT232H) Extensions */
	ftData.PullDownEnableH    = (UCHAR) data_values[103];
	ftData.SerNumEnableH      = (UCHAR) data_values[104];
	ftData.ACSlowSlewH        = (UCHAR) data_values[105];
	ftData.ACSchmittInputH    = (UCHAR) data_values[106];
	ftData.ACDriveCurrentH    = (UCHAR) data_values[107];
	ftData.ADSlowSlewH        = (UCHAR) data_values[108];
	ftData.ADSchmittInputH    = (UCHAR) data_values[109];
	ftData.ADDriveCurrentH    = (UCHAR) data_values[110];
	ftData.Cbus0H             = (UCHAR) data_values[111];
	ftData.Cbus1H             = (UCHAR) data_values[112];
	ftData.Cbus2H             = (UCHAR) data_values[113];
	ftData.Cbus3H             = (UCHAR) data_values[114];
	ftData.Cbus4H             = (UCHAR) data_values[115];
	ftData.Cbus5H             = (UCHAR) data_values[116];
	ftData.Cbus6H             = (UCHAR) data_values[117];
	ftData.Cbus7H             = (UCHAR) data_values[118];
	ftData.Cbus8H             = (UCHAR) data_values[119];
	ftData.Cbus9H             = (UCHAR) data_values[120];
	ftData.IsFifoH            = (UCHAR) data_values[121];
	ftData.IsFifoTarH         = (UCHAR) data_values[122];
	ftData.IsFastSerH         = (UCHAR) data_values[123];
	ftData.IsFT1248H          = (UCHAR) data_values[124];
	ftData.FT1248CpolH        = (UCHAR) data_values[125];
	ftData.FT1248LsbH         = (UCHAR) data_values[126];
	ftData.FT1248FlowControlH = (UCHAR) data_values[127];
	ftData.IsVCPH             = (UCHAR) data_values[128];
	ftData.PowerSaveEnableH   = (UCHAR) data_values[129];

	ftStatus = FT_EE_Program((FT_HANDLE) handle, &ftData);
	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return -1;
	}

	(*env)->ReleaseStringUTFChars(env, manufacturer, manufacturer_buf);
	(*env)->ReleaseStringUTFChars(env, manufacturerID, manufacturer_idbuf);
	(*env)->ReleaseStringUTFChars(env, description, description_buf);
	(*env)->ReleaseStringUTFChars(env, serialNumber, serialNumber_buf);
	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    eeProgramEx
 * Signature: (JLjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[I)I
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_eeProgramEx
  (JNIEnv *env, jobject obj, jlong handle, jstring manufacturer, jstring manufacturerID, jstring description,
		  jstring serialNumber, jintArray values) {

	FT_STATUS ftStatus = 0;
	FT_PROGRAM_DATA ftData;
	char *manufacturer_buf;
	char *manufacturer_idbuf;
	char *description_buf;
	char *serialNumber_buf;
	jint* data_values = NULL;

	data_values = (*env)->GetIntArrayElements(env, values, JNI_FALSE);
	if((data_values == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_GETINTARRELEMTSTR);
		return -1;
	}

	manufacturer_buf = (char *) (*env)->GetStringUTFChars(env, manufacturer, NULL);
	if((manufacturer_buf == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_GETSTRUTFCHARSTR);
		return -1;
	}
	manufacturer_idbuf = (char *) (*env)->GetStringUTFChars(env, manufacturerID, NULL);
	if((manufacturer_idbuf == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		(*env)->ReleaseStringUTFChars(env, manufacturer, manufacturer_buf);
		throw_serialcom_exception(env, 3, 0, E_GETSTRUTFCHARSTR);
		return -1;
	}
	description_buf = (char *) (*env)->GetStringUTFChars(env, description, NULL);
	if((description_buf == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		(*env)->ReleaseStringUTFChars(env, manufacturer, manufacturer_buf);
		(*env)->ReleaseStringUTFChars(env, manufacturerID, manufacturer_idbuf);
		throw_serialcom_exception(env, 3, 0, E_GETSTRUTFCHARSTR);
		return -1;
	}
	serialNumber_buf = (char *) (*env)->GetStringUTFChars(env, serialNumber, NULL);
	if((serialNumber_buf == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		(*env)->ReleaseStringUTFChars(env, manufacturer, manufacturer_buf);
		(*env)->ReleaseStringUTFChars(env, manufacturerID, manufacturer_idbuf);
		(*env)->ReleaseStringUTFChars(env, description, description_buf);
		throw_serialcom_exception(env, 3, 0, E_GETSTRUTFCHARSTR);
		return -1;
	}

	ftData.Signature1     = (DWORD) data_values[0]; /* 0x00000000 */
	ftData.Signature2     = (DWORD) data_values[1]; /* 0xffffffff */
	ftData.Version        = (DWORD) data_values[2];
	ftData.VendorId       = (WORD) data_values[3];
	ftData.ProductId      = (WORD) data_values[4];
	ftData.Manufacturer   = (DWORD) 0;
	ftData.ManufacturerId = (DWORD) 0;
	ftData.Description    = (DWORD) 0;
	ftData.SerialNumber   = (DWORD) 0;
	ftData.MaxPower       = (WORD) data_values[9];
	ftData.PnP            = (WORD) data_values[10];
	ftData.SelfPowered    = (WORD) data_values[11];
	ftData.RemoteWakeup   = (WORD) data_values[12];

	/* Rev4 (FT232B) extensions */
	ftData.Rev4             = (UCHAR) data_values[13];
	ftData.IsoIn            = (UCHAR) data_values[14];
	ftData.IsoOut           = (UCHAR) data_values[15];
	ftData.PullDownEnable   = (UCHAR) data_values[16];
	ftData.SerNumEnable     = (UCHAR) data_values[17];
	ftData.USBVersionEnable = (UCHAR) data_values[18];
	ftData.USBVersion       = (WORD)  data_values[19];

	/* Rev 5 (FT2232) extensions */
	ftData.Rev5              = (UCHAR) data_values[20];
	ftData.IsoInA            = (UCHAR) data_values[21];
	ftData.IsoInB            = (UCHAR) data_values[22];
	ftData.IsoOutA           = (UCHAR) data_values[23];
	ftData.IsoOutB           = (UCHAR) data_values[24];
	ftData.PullDownEnable5   = (UCHAR) data_values[25];
	ftData.SerNumEnable5     = (UCHAR) data_values[26];
	ftData.USBVersionEnable5 = (UCHAR) data_values[27];
	ftData.USBVersion5       = (WORD)  data_values[28];
	ftData.AIsHighCurrent    = (UCHAR) data_values[29];
	ftData.BIsHighCurrent    = (UCHAR) data_values[30];
	ftData.IFAIsFifo         = (UCHAR) data_values[31];
	ftData.IFAIsFifoTar      = (UCHAR) data_values[32];
	ftData.IFAIsFastSer      = (UCHAR) data_values[33];
	ftData.AIsVCP            = (UCHAR) data_values[34];
	ftData.IFBIsFifo         = (UCHAR) data_values[35];
	ftData.IFBIsFifoTar      = (UCHAR) data_values[36];
	ftData.IFBIsFastSer      = (UCHAR) data_values[37];
	ftData.BIsVCP            = (UCHAR) data_values[38];

	/* Rev 6 (FT232R) extensions */
	ftData.UseExtOsc       = (UCHAR) data_values[39];
	ftData.HighDriveIOs    = (UCHAR) data_values[40];
	ftData.EndpointSize    = (UCHAR) data_values[41];
	ftData.PullDownEnableR = (UCHAR) data_values[42];
	ftData.SerNumEnableR   = (UCHAR) data_values[43];
	ftData.InvertTXD       = (UCHAR) data_values[44];
	ftData.InvertRXD       = (UCHAR) data_values[45];
	ftData.InvertRTS       = (UCHAR) data_values[46];
	ftData.InvertCTS       = (UCHAR) data_values[47];
	ftData.InvertDTR       = (UCHAR) data_values[48];
	ftData.InvertDSR       = (UCHAR) data_values[49];
	ftData.InvertDCD       = (UCHAR) data_values[50];
	ftData.InvertRI        = (UCHAR) data_values[51];
	ftData.Cbus0           = (UCHAR) data_values[52];
	ftData.Cbus1           = (UCHAR) data_values[53];
	ftData.Cbus2           = (UCHAR) data_values[54];
	ftData.Cbus3           = (UCHAR) data_values[55];
	ftData.Cbus4           = (UCHAR) data_values[56];
	ftData.RIsD2XX         = (UCHAR) data_values[57];

	/* Rev 7 (FT2232H) Extensions */
	ftData.PullDownEnable7  = (UCHAR) data_values[58];
	ftData.SerNumEnable7    = (UCHAR) data_values[59];
	ftData.ALSlowSlew       = (UCHAR) data_values[60];
	ftData.ALSchmittInput   = (UCHAR) data_values[61];
	ftData.ALDriveCurrent   = (UCHAR) data_values[62];
	ftData.AHSlowSlew       = (UCHAR) data_values[63];
	ftData.AHSchmittInput   = (UCHAR) data_values[64];
	ftData.AHDriveCurrent   = (UCHAR) data_values[65];
	ftData.BLSlowSlew       = (UCHAR) data_values[66];
	ftData.BLSchmittInput   = (UCHAR) data_values[67];
	ftData.BLDriveCurrent   = (UCHAR) data_values[68];
	ftData.BHSlowSlew       = (UCHAR) data_values[69];
	ftData.BHSchmittInput   = (UCHAR) data_values[70];
	ftData.BHDriveCurrent   = (UCHAR) data_values[71];
	ftData.IFAIsFifo7       = (UCHAR) data_values[72];
	ftData.IFAIsFifoTar7    = (UCHAR) data_values[73];
	ftData.IFAIsFastSer7    = (UCHAR) data_values[74];
	ftData.AIsVCP7          = (UCHAR) data_values[75];
	ftData.IFBIsFifo7       = (UCHAR) data_values[76];
	ftData.IFBIsFifoTar7    = (UCHAR) data_values[77];
	ftData.IFBIsFastSer7    = (UCHAR) data_values[78];
	ftData.BIsVCP7          = (UCHAR) data_values[79];
	ftData.PowerSaveEnable  = (UCHAR) data_values[80];

	/* Rev 8 (FT4232H) Extensions */
	ftData.PullDownEnable8 = (UCHAR) data_values[81];
	ftData.SerNumEnable8   = (UCHAR) data_values[82];
	ftData.ASlowSlew       = (UCHAR) data_values[83];
	ftData.ASchmittInput   = (UCHAR) data_values[84];
	ftData.ADriveCurrent   = (UCHAR) data_values[85];
	ftData.BSlowSlew       = (UCHAR) data_values[86];
	ftData.BSchmittInput   = (UCHAR) data_values[87];
	ftData.BDriveCurrent   = (UCHAR) data_values[88];
	ftData.CSlowSlew       = (UCHAR) data_values[89];
	ftData.CSchmittInput   = (UCHAR) data_values[90];
	ftData.CDriveCurrent   = (UCHAR) data_values[91];
	ftData.DSlowSlew       = (UCHAR) data_values[92];
	ftData.DSchmittInput   = (UCHAR) data_values[93];
	ftData.DDriveCurrent   = (UCHAR) data_values[94];
	ftData.ARIIsTXDEN      = (UCHAR) data_values[95];
	ftData.BRIIsTXDEN      = (UCHAR) data_values[96];
	ftData.CRIIsTXDEN      = (UCHAR) data_values[97];
	ftData.DRIIsTXDEN      = (UCHAR) data_values[98];
	ftData.AIsVCP8         = (UCHAR) data_values[99];
	ftData.BIsVCP8         = (UCHAR) data_values[100];
	ftData.CIsVCP8         = (UCHAR) data_values[101];
	ftData.DIsVCP8         = (UCHAR) data_values[102];

	/* Rev 9 (FT232H) Extensions */
	ftData.PullDownEnableH    = (UCHAR) data_values[103];
	ftData.SerNumEnableH      = (UCHAR) data_values[104];
	ftData.ACSlowSlewH        = (UCHAR) data_values[105];
	ftData.ACSchmittInputH    = (UCHAR) data_values[106];
	ftData.ACDriveCurrentH    = (UCHAR) data_values[107];
	ftData.ADSlowSlewH        = (UCHAR) data_values[108];
	ftData.ADSchmittInputH    = (UCHAR) data_values[109];
	ftData.ADDriveCurrentH    = (UCHAR) data_values[110];
	ftData.Cbus0H             = (UCHAR) data_values[111];
	ftData.Cbus1H             = (UCHAR) data_values[112];
	ftData.Cbus2H             = (UCHAR) data_values[113];
	ftData.Cbus3H             = (UCHAR) data_values[114];
	ftData.Cbus4H             = (UCHAR) data_values[115];
	ftData.Cbus5H             = (UCHAR) data_values[116];
	ftData.Cbus6H             = (UCHAR) data_values[117];
	ftData.Cbus7H             = (UCHAR) data_values[118];
	ftData.Cbus8H             = (UCHAR) data_values[119];
	ftData.Cbus9H             = (UCHAR) data_values[120];
	ftData.IsFifoH            = (UCHAR) data_values[121];
	ftData.IsFifoTarH         = (UCHAR) data_values[122];
	ftData.IsFastSerH         = (UCHAR) data_values[123];
	ftData.IsFT1248H          = (UCHAR) data_values[124];
	ftData.FT1248CpolH        = (UCHAR) data_values[125];
	ftData.FT1248LsbH         = (UCHAR) data_values[126];
	ftData.FT1248FlowControlH = (UCHAR) data_values[127];
	ftData.IsVCPH             = (UCHAR) data_values[128];
	ftData.PowerSaveEnableH   = (UCHAR) data_values[129];

	ftStatus = FT_EE_ProgramEx((FT_HANDLE) handle, &ftData, manufacturer_buf, manufacturer_idbuf,
			description_buf, serialNumber_buf);
	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return -1;
	}

	(*env)->ReleaseStringUTFChars(env, manufacturer, manufacturer_buf);
	(*env)->ReleaseStringUTFChars(env, manufacturerID, manufacturer_idbuf);
	(*env)->ReleaseStringUTFChars(env, description, description_buf);
	(*env)->ReleaseStringUTFChars(env, serialNumber, serialNumber_buf);
	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    eeUAsize
 * Signature: (J)I
 *
 * @return size of eeprom user area in bytes on success otherwise -1 if an error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_eeUAsize
  (JNIEnv *env, jobject obj, jlong handle) {

	FT_STATUS ftStatus = 0;
	DWORD size = 0;
	ftStatus = FT_EE_UASize((FT_HANDLE)handle, &size);
	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return -1;
	}
	return size;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    eeUAread
 * Signature: (J[BI)I
 *
 * @return number of bytes read from eeprom user area on success otherwise -1 if an error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_eeUAread
  (JNIEnv *env, jobject obj, jlong handle, jbyteArray buffer, jint length) {

	int x = 0;
	DWORD num_bytes_read;
	FT_STATUS ftStatus = 0;
	UCHAR *data_char_buffer = NULL;
	jbyte* data_buffer = NULL;

	data_buffer = (jbyte *) calloc(length, sizeof(jbyte));
	if(data_buffer == NULL) {
		throw_serialcom_exception(env, 3, 0, E_CALLOCSTR);
		return -1;
	}

	data_char_buffer = (UCHAR *) calloc(length, sizeof(UCHAR));
	if(data_char_buffer == NULL) {
		free(data_buffer);
		throw_serialcom_exception(env, 3, 0, E_CALLOCSTR);
		return -1;
	}

	ftStatus = FT_EE_UARead((FT_HANDLE)handle, data_char_buffer, (DWORD)length, &num_bytes_read);
	if(ftStatus != FT_OK) {
		free(data_buffer);
		free(data_char_buffer);
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return -1;
	}

	if(num_bytes_read > 0) {
		for(x=0; x<num_bytes_read; x++) {
			data_buffer[x] = (jbyte) data_char_buffer[x];
		}
		(*env)->SetByteArrayRegion(env, buffer, 0, (unsigned int)num_bytes_read, data_buffer);
		if((*env)->ExceptionOccurred(env)) {
			free(data_buffer);
			free(data_char_buffer);
			throw_serialcom_exception(env, 3, 0, E_SETBYTEARRREGIONSTR);
			return -1;
		}
	}

	free(data_buffer);
	free(data_char_buffer);
	return num_bytes_read;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    eeUAwrite
 * Signature: (J[BI)I
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_eeUAwrite
  (JNIEnv *env, jobject obj, jlong handle, jbyteArray buffer, jint length) {

	int x = 0;
	FT_STATUS ftStatus = 0;
	jbyte* data_buffer = NULL;
	UCHAR *data_to_write = NULL;

	data_buffer = (jbyte *) calloc(length, sizeof(jbyte));
	if(data_buffer == NULL) {
		throw_serialcom_exception(env, 3, 0, E_CALLOCSTR);
		return -1;
	}

	data_to_write = (UCHAR *) calloc(length, sizeof(UCHAR));
	if(data_to_write == NULL) {
		free(data_buffer);
		throw_serialcom_exception(env, 3, 0, E_CALLOCSTR);
		return -1;
	}

	(*env)->GetByteArrayRegion(env, buffer, 0, length, data_buffer);
	if((*env)->ExceptionOccurred(env)) {
		free(data_buffer);
		free(data_to_write);
		throw_serialcom_exception(env, 3, 0, E_GETBYTEARRREGIONSTR);
		return -1;
	}

	for(x=0; x<length; x++) {
		data_to_write[x] = (UCHAR) data_buffer[x];
	}

	ftStatus = FT_EE_UAWrite((FT_HANDLE)handle, data_to_write, length);
	if(ftStatus != FT_OK) {
		free(data_buffer);
		free(data_to_write);
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return -1;
	}

	free(data_buffer);
	free(data_to_write);
	return 0;
}

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
 * @return 0 on success otherwise -1 if an error occurs.
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
 * @return latency time value on success otherwise -1 if an error occurs.
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
 * @return 0 on success otherwise -1 if an error occurs.
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
 * @return bit mode value on success otherwise -1 if an error occurs.
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
 * @return 0 on success otherwise -1 if an error occurs.
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
 *
 * @return valid handle on success otherwise -1 if an error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jlong JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_w32CreateFile
  (JNIEnv *env, jobject obj, jstring serialNum, jstring description, jlong location, jint dwAttrsAndFlags,
		  jint dwAccess, jboolean overLapped) {

	FT_HANDLE ftHandle = 0;
	const char* serial = NULL;
	const char* descr = NULL;
	DWORD attr_and_flags = 0;
	DWORD dw_create = 0;
	DWORD dw_access = 0;

	/* open by serial */
	if((dwAttrsAndFlags & SCM_OPEN_BY_SERIAL_NUMBER) == SCM_OPEN_BY_SERIAL_NUMBER) {
		serial = (*env)->GetStringUTFChars(env, serialNum, NULL);
		if((serial == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			throw_serialcom_exception(env, 3, 0, E_GETSTRUTFCHARSTR);
			return -1;
		}
#if defined (__linux__) || defined (__APPLE__)
		dw_create = 0;
		dw_access = 0;
		attr_and_flags = FT_OPEN_BY_SERIAL_NUMBER;
#endif
#if defined (_WIN32) && !defined(UNIX)
		dw_create = OPEN_EXISTING;
		if((dwAccess & SCM_GENERIC_READ) == SCM_GENERIC_READ) {
			dw_access = dw_access | GENERIC_READ;
		}
		if((dwAccess & SCM_GENERIC_WRITE) == SCM_GENERIC_WRITE) {
			dw_access = dw_access | GENERIC_WRITE;
		}
		if(overLapped == JNI_TRUE) {
			attr_and_flags = FILE_ATTRIBUTE_NORMAL | FT_OPEN_BY_SERIAL_NUMBER | FILE_FLAG_OVERLAPPED;
		}else {
			attr_and_flags = FILE_ATTRIBUTE_NORMAL | FT_OPEN_BY_SERIAL_NUMBER;
		}
#endif
		ftHandle = FT_W32_CreateFile(serial, dw_access, 0, 0, dw_create, attr_and_flags, 0);
	}

	/* open by description */
	if((dwAttrsAndFlags & SCM_OPEN_BY_DESCRIPTION) == SCM_OPEN_BY_DESCRIPTION) {
		descr = (*env)->GetStringUTFChars(env, description, NULL);
		if((descr == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			throw_serialcom_exception(env, 3, 0, E_GETSTRUTFCHARSTR);
			return -1;
		}
#if defined (__linux__) || defined (__APPLE__)
		dw_create = 0;
		dw_access = 0;
		attr_and_flags = FT_OPEN_BY_DESCRIPTION;
#endif
#if defined (_WIN32) && !defined(UNIX)
		dw_create = OPEN_EXISTING;
		if((dwAccess & SCM_GENERIC_READ) == SCM_GENERIC_READ) {
			dw_access = dw_access | GENERIC_READ;
		}
		if((dwAccess & SCM_GENERIC_WRITE) == SCM_GENERIC_WRITE) {
			dw_access = dw_access | GENERIC_WRITE;
		}
		if(overLapped == JNI_TRUE) {
			attr_and_flags = FILE_ATTRIBUTE_NORMAL | FT_OPEN_BY_DESCRIPTION | FILE_FLAG_OVERLAPPED;
		}else {
			attr_and_flags = FILE_ATTRIBUTE_NORMAL | FT_OPEN_BY_DESCRIPTION;
		}
#endif
		ftHandle = FT_W32_CreateFile(descr, dw_access, 0, 0, dw_create, attr_and_flags, 0);
	}

	/* open by location */
	if((dwAttrsAndFlags & SCM_OPEN_BY_LOCATION) == SCM_OPEN_BY_LOCATION) {
#if defined (__linux__) || defined (__APPLE__)
		dw_create = 0;
		dw_access = 0;
		attr_and_flags = FT_OPEN_BY_LOCATION;
#endif
#if defined (_WIN32) && !defined(UNIX)
		dw_create = OPEN_EXISTING;
		if((dwAccess & SCM_GENERIC_READ) == SCM_GENERIC_READ) {
			dw_access = dw_access | GENERIC_READ;
		}
		if((dwAccess & SCM_GENERIC_WRITE) == SCM_GENERIC_WRITE) {
			dw_access = dw_access | GENERIC_WRITE;
		}
		if(overLapped == JNI_TRUE) {
			attr_and_flags = FILE_ATTRIBUTE_NORMAL | FT_OPEN_BY_LOCATION | FILE_FLAG_OVERLAPPED;
		}else {
			attr_and_flags = FILE_ATTRIBUTE_NORMAL | FT_OPEN_BY_LOCATION;
		}
#endif
		ftHandle = FT_W32_CreateFile((PVOID)location, dw_access, 0, 0, dw_create, attr_and_flags, 0);
	}

	/* 0xFFFFFFFFF is INVALID_HANDLE_VALUE, type cast is done to handle compiler warning. */
	if(ftHandle == (FT_HANDLE)0xFFFFFFFFF) {
		throw_serialcom_exception(env, 3, 0, E_INVALIDHANDLE);
		return -1;
	}

	return (jlong)ftHandle;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    w32CloseHandle
 * Signature: (J)I
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_w32CloseHandle
  (JNIEnv *env, jobject obj, jlong handle) {
	FT_STATUS ftStatus = 0;
	ftStatus = FT_Close((FT_HANDLE) handle);
	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return -1;
	}
	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    w32ReadFile
 * Signature: (J[BI)I
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_w32ReadFile
  (JNIEnv *env, jobject obj, jlong handle, jbyteArray buffer, jint numOfBytesToRead) {

}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    w32WriteFile
 * Signature: (J[BI)I
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_w32WriteFile
  (JNIEnv *env, jobject obj, jlong handle, jbyteArray, jint) {

}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    w32GetOverlappedResult
 * Signature: (JZ)I
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_w32GetOverlappedResult
  (JNIEnv *env, jobject obj, jlong handle, jboolean) {

}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    w32EscapeCommFunction
 * Signature: (JS)I
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_w32EscapeCommFunction
  (JNIEnv *env, jobject obj, jlong handle, jshort function) {

	BOOL ret;
	DWORD func = 0;
	if((function & SCM_SETRTS) == SCM_SETRTS) {
		func = SETRTS;
	}

	ret = FT_W32_EscapeCommFunction((FT_HANDLE)handle, func);
	if(ret == 0) {
		throw_serialcom_exception(env, 3, 0, E_SETRTS);
		return -1;
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    w32GetCommModemStatus
 * Signature: (J)I
 *
 * @return bit mask of constants MS_CTS_ON, MS_DSR_ON, MS_RING_ON and MS_RLSD_ON otherwise -1 if an error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_w32GetCommModemStatus
  (JNIEnv *env, jobject obj, jlong handle) {

	int stat = 0;
	DWORD status = 0;
	BOOL ret = 0;
	ret = FT_W32_GetCommModemStatus((FT_HANDLE)handle, &status);
	if(ret == 0) {
		throw_serialcom_exception(env, 3, 0, E_GETMODEMSTATUS);
		return -1;
	}

	/* set all the bits corresponding to lines status */
	if((status & MS_CTS_ON) == MS_CTS_ON) {
		stat = stat | SCM_MS_CTS_ON;
	}
	if((status & MS_DSR_ON) == MS_DSR_ON) {
		stat = stat | SCM_MS_DSR_ON;
	}
	if((status & MS_RING_ON) == MS_RING_ON) {
		stat = stat | SCM_MS_RING_ON;
	}
	if((status & MS_RLSD_ON) == MS_RLSD_ON) {
		stat = stat | SCM_MS_RLSD_ON;
	}
	return stat;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    w32SetupComm
 * Signature: (JII)I
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_w32SetupComm
  (JNIEnv *env, jobject obj, jlong handle, jint readBufSize, jint writeBufSize) {

	BOOL ret = 0;
	ret = FT_W32_SetupComm((FT_HANDLE)handle, (DWORD)readBufSize, (DWORD)writeBufSize);
	if(ret == 0) {
		throw_serialcom_exception(env, 3, 0, E_SETUPCOMM);
		return -1;
	}
	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    w32SetCommState
 * Signature: (J[I)I
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_w32SetCommState
  (JNIEnv *env, jobject obj, jlong handle, jintArray dcbValues) {

	BOOL ret = 0;
	FTDCB dcb;
	jint* dcbval = NULL;

	/* The JVM may return pointer to original buffer or pointer to its copy depending upon
	 * whether underlying garbage collector supports pinning or not. */
	dcbval = (*env)->GetIntArrayElements(env, dcbValues, JNI_FALSE);
	if((dcbval == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_GETINTARRELEMTSTR);
		return -1;
	}

	dcb.DCBlength = sizeof(dcb);
	dcb.BaudRate = (DWORD)dcbval[1];
	dcb.fBinary = (DWORD)dcbval[2];
	dcb.fParity = (DWORD)dcbval[3];
	dcb.fOutxCtsFlow = (DWORD)dcbval[4];
	dcb.fOutxDsrFlow = (DWORD)dcbval[5];
	dcb.fDtrControl = (DWORD)dcbval[6];
	dcb.fDsrSensitivity = (DWORD)dcbval[7];
	dcb.fTXContinueOnXoff = (DWORD)dcbval[8];
	dcb.fOutX = (DWORD)dcbval[9];
	dcb.fInX = (DWORD)dcbval[10];
	dcb.fErrorChar = (DWORD)dcbval[11];
	dcb.fNull = (DWORD)dcbval[12];
	dcb.fRtsControl = (DWORD)dcbval[13];
	dcb.fAbortOnError = (DWORD)dcbval[14];
	dcb.fDummy2 = 0;
	dcb.wReserved = 0;
	dcb.XonLim = (WORD)dcbval[17];
	dcb.XoffLim = (WORD)dcbval[18];
	dcb.ByteSize = (WORD)dcbval[19];
	dcb.Parity = (WORD)dcbval[20];
	dcb.StopBits = (WORD)dcbval[21];
	dcb.XonChar = (CHAR)dcbval[22];
	dcb.XoffChar = (CHAR)dcbval[23];
	dcb.ErrorChar = (CHAR)dcbval[24];
	dcb.EofChar = (CHAR)dcbval[25];
	dcb.EvtChar = (CHAR)dcbval[26];
	dcb.wReserved1 = 0;

	ret = FT_W32_SetCommState((FT_HANDLE)handle, &dcb);
	if(ret == 0) {
		throw_serialcom_exception(env, 3, 0, E_SETUPCOMMSTATE);
		return -1;
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    w32GetCommState
 * Signature: (J)[I
 *
 * @return array of integers containing DCB values on success otherwise NULL if an error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jintArray JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_w32GetCommState
  (JNIEnv *env, jobject obj, jlong handle) {

	BOOL ret = 0;
	FTDCB dcb;
	jint values[28];

	jintArray dcbvalues = (*env)->NewIntArray(env, 28);
	if((dcbvalues == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_NEWINTARRAYSTR);
		return NULL;
	}

	ret = FT_W32_GetCommState((FT_HANDLE)handle, &dcb);
	if(ret == 0) {
		throw_serialcom_exception(env, 3, 0, E_GETUPCOMMSTATE);
		return NULL;
	}

	values[0]  = (jint) dcb.DCBlength;
	values[1]  = (jint) dcb.BaudRate;
	values[2]  = (jint) dcb.fBinary;
	values[3]  = (jint) dcb.fParity;
	values[4]  = (jint) dcb.fOutxCtsFlow;
	values[5]  = (jint) dcb.fOutxDsrFlow;
	values[6]  = (jint) dcb.fDtrControl;
	values[7]  = (jint) dcb.fDsrSensitivity;
	values[8]  = (jint) dcb.fTXContinueOnXoff;
	values[9]  = (jint) dcb.fOutX;
	values[10] = (jint) dcb.fInX;
	values[11] = (jint) dcb.fErrorChar;
	values[12] = (jint) dcb.fNull;
	values[13] = (jint) dcb.fRtsControl;
	values[14] = (jint) dcb.fAbortOnError;
	values[15] = (jint) dcb.fDummy2;
	values[16] = (jint) dcb.wReserved;
	values[17] = (jint) dcb.XonLim;
	values[18] = (jint) dcb.XoffLim;
	values[19] = (jint) dcb.ByteSize;
	values[20] = (jint) dcb.Parity;
	values[21] = (jint) dcb.StopBits;
	values[22] = (jint) dcb.XonChar;
	values[23] = (jint) dcb.XoffChar;
	values[24] = (jint) dcb.ErrorChar;
	values[25] = (jint) dcb.EofChar;
	values[26] = (jint) dcb.EvtChar;
	values[27] = (jint) dcb.wReserved1;

	(*env)->SetIntArrayRegion(env, dcbvalues, 0, 28, values);
	if((*env)->ExceptionOccurred(env) != NULL) {
		throw_serialcom_exception(env, 3, 0, E_SETINTARRREGIONSTR);
		return NULL;
	}

	return dcbvalues;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    w32SetCommTimeouts
 * Signature: (JIIIII)I
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_w32SetCommTimeouts
  (JNIEnv *env, jobject obj, jlong handle, jint readIntervalTimeout, jint readTotalTimeoutMultiplier,
		  jint readTotalTimeoutConstant, jint writeTotalTimeoutMultiplier, jint writeTotalTimeoutConstant) {

	FTTIMEOUTS ftTS;
	BOOL ret = 0;

	ftTS.ReadIntervalTimeout = (DWORD)readIntervalTimeout;
	ftTS.ReadTotalTimeoutMultiplier = (DWORD)readTotalTimeoutMultiplier;
	ftTS.ReadTotalTimeoutConstant = (DWORD)readTotalTimeoutConstant;
	ftTS.WriteTotalTimeoutMultiplier = (DWORD)writeTotalTimeoutMultiplier;
	ftTS.WriteTotalTimeoutConstant = (DWORD)writeTotalTimeoutConstant;

	ret = FT_W32_SetCommTimeouts((FT_HANDLE)handle, &ftTS);
	if(ret == 0) {
		throw_serialcom_exception(env, 3, 0, E_SETCOMMTIMEOUTS);
		return -1;
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    w32GetCommTimeouts
 * Signature: (J)[I
 *
 * @return array of integers containing timeout values on success otherwise NULL if an error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jintArray JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_w32GetCommTimeouts
  (JNIEnv *env, jobject obj, jlong handle) {

	FTTIMEOUTS ftTS;
	BOOL ret = 0;
	jint values[28];

	jintArray timeout_values = (*env)->NewIntArray(env, 5);
	if((timeout_values == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_NEWINTARRAYSTR);
		return NULL;
	}

	ret = FT_W32_GetCommTimeouts((FT_HANDLE)handle, &ftTS);
	if(ret == 0) {
		throw_serialcom_exception(env, 3, 0, E_GETCOMMTIMEOUTS);
		return NULL;
	}

	values[0]  = (jint) ftTS.ReadIntervalTimeout;
	values[1]  = (jint) ftTS.ReadTotalTimeoutMultiplier;
	values[2]  = (jint) ftTS.ReadTotalTimeoutConstant;
	values[3]  = (jint) ftTS.WriteTotalTimeoutMultiplier;
	values[4]  = (jint) ftTS.WriteTotalTimeoutConstant;

	(*env)->SetIntArrayRegion(env, timeout_values, 0, 5, values);
	if((*env)->ExceptionOccurred(env) != NULL) {
		throw_serialcom_exception(env, 3, 0, E_SETINTARRREGIONSTR);
		return NULL;
	}

	return timeout_values;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    w32SetCommBreak
 * Signature: (J)I
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_w32SetCommBreak
  (JNIEnv *env, jobject obj, jlong handle) {

	BOOL ret = 0;
	ret = FT_W32_SetCommBreak((FT_HANDLE)handle);
	if(ret == 0) {
		throw_serialcom_exception(env, 3, 0, E_SETCOMMBREAK);
		return -1;
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    w32ClearCommBreak
 * Signature: (J)I
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_w32ClearCommBreak
  (JNIEnv *env, jobject obj, jlong handle) {

	BOOL ret = 0;
	ret = FT_W32_ClearCommBreak((FT_HANDLE)handle);
	if(ret == 0) {
		throw_serialcom_exception(env, 3, 0, E_CLEARCOMMBREAK);
		return -1;
	}
	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    w32SetCommMask
 * Signature: (JI)I
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_w32SetCommMask
  (JNIEnv *env, jobject obj, jlong handle, jint mask) {

	BOOL ret = 0;
	DWORD dwmask = 0;

	if((mask & SCM_EV_RXCHAR) == SCM_EV_RXCHAR) {
		dwmask = dwmask | EV_RXCHAR;
	}
	if((mask & SCM_EV_RXFLAG) == SCM_EV_RXFLAG) {
		dwmask = dwmask | EV_RXFLAG;
	}
	if((mask & SCM_EV_TXEMPTY) == SCM_EV_TXEMPTY) {
		dwmask = dwmask | EV_TXEMPTY;
	}
	if((mask & SCM_EV_CTS) == SCM_EV_CTS) {
		dwmask = dwmask | EV_CTS;
	}
	if((mask & SCM_EV_DSR) == SCM_EV_DSR) {
		dwmask = dwmask | EV_DSR;
	}
	if((mask & SCM_EV_RLSD) == SCM_EV_RLSD) {
		dwmask = dwmask | EV_RLSD;
	}
	if((mask & SCM_EV_BREAK) == SCM_EV_BREAK) {
		dwmask = dwmask | EV_BREAK;
	}
	if((mask & SCM_EV_ERR) == SCM_EV_ERR) {
		dwmask = dwmask | EV_ERR;
	}
	if((mask & SCM_EV_RING) == SCM_EV_RING) {
		dwmask = dwmask | EV_RING;
	}
	if((mask & SCM_EV_PERR) == SCM_EV_PERR) {
		dwmask = dwmask | EV_PERR;
	}
	if((mask & SCM_EV_RX80FULL) == SCM_EV_RX80FULL) {
		dwmask = dwmask | EV_RX80FULL;
	}
	if((mask & SCM_EV_EVENT1) == SCM_EV_EVENT1) {
		dwmask = dwmask | EV_EVENT1;
	}
	if((mask &SCM_EV_EVENT2) == SCM_EV_EVENT2) {
		dwmask = dwmask | EV_EVENT2;
	}

	ret = FT_W32_SetCommMask((FT_HANDLE)handle, dwmask);
	if(ret == 0) {
		throw_serialcom_exception(env, 3, 0, E_SETCOMMMASK);
		return -1;
	}
	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    w32GetCommMask
 * Signature: (J)I
 *
 * @return bit mask of active event monitored on success otherwise -1 if an error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_w32GetCommMask
  (JNIEnv *env, jobject obj, jlong handle) {

	BOOL ret = 0;
	DWORD mask = 0;
	jint dwmask = 0;
	ret = FT_W32_GetCommMask((FT_HANDLE)handle, &mask);
	if(ret == 0) {
		throw_serialcom_exception(env, 3, 0, E_GETCOMMMASK);
		return -1;
	}

	if((mask & EV_RXCHAR) == EV_RXCHAR) {
		dwmask = dwmask | SCM_EV_RXCHAR;
	}
	if((mask & EV_RXFLAG) == EV_RXFLAG) {
		dwmask = dwmask | SCM_EV_RXFLAG;
	}
	if((mask & EV_TXEMPTY) == EV_TXEMPTY) {
		dwmask = dwmask | SCM_EV_TXEMPTY;
	}
	if((mask & EV_CTS) == EV_CTS) {
		dwmask = dwmask | SCM_EV_CTS;
	}
	if((mask & EV_DSR) == EV_DSR) {
		dwmask = dwmask | SCM_EV_DSR;
	}
	if((mask & EV_RLSD) == EV_RLSD) {
		dwmask = dwmask | SCM_EV_RLSD;
	}
	if((mask & EV_BREAK) == EV_BREAK) {
		dwmask = dwmask | SCM_EV_BREAK;
	}
	if((mask & EV_ERR) == EV_ERR) {
		dwmask = dwmask | SCM_EV_ERR;
	}
	if((mask & EV_RING) == EV_RING) {
		dwmask = dwmask | SCM_EV_RING;
	}
	if((mask & EV_PERR) == EV_PERR) {
		dwmask = dwmask | SCM_EV_PERR;
	}
	if((mask & EV_RX80FULL) == EV_RX80FULL) {
		dwmask = dwmask | SCM_EV_RX80FULL;
	}
	if((mask & EV_EVENT1) == EV_EVENT1) {
		dwmask = dwmask | SCM_EV_EVENT1;
	}
	if((mask &EV_EVENT2) == EV_EVENT2) {
		dwmask = dwmask | SCM_EV_EVENT2;
	}

	return dwmask;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    w32WaitCommEvent
 * Signature: (JI)I
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_w32WaitCommEvent
  (JNIEnv *, jobject, jlong, jint);

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    w32PurgeComm
 * Signature: (JI)I
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_w32PurgeComm
  (JNIEnv *env, jobject obj, jlong handle, jint flag) {
	BOOL ret = 0;
	DWORD dwflag = 0;

	if((flag & SCM_PURGE_TXABORT) == SCM_PURGE_TXABORT) {
		dwflag = dwflag | PURGE_TXABORT;
	}
	if((flag & SCM_PURGE_RXABORT) == SCM_PURGE_RXABORT) {
		dwflag = dwflag | PURGE_RXABORT;
	}
	if((flag & SCM_PURGE_TXCLEAR) == SCM_PURGE_TXCLEAR) {
		dwflag = dwflag | PURGE_TXCLEAR;
	}
	if((flag & SCM_PURGE_RXCLEAR) == SCM_PURGE_RXCLEAR) {
		dwflag = dwflag | PURGE_RXCLEAR;
	}

	ret = FT_W32_PurgeComm((FT_HANDLE)handle, dwflag);
	if(ret == 0) {
		throw_serialcom_exception(env, 3, 0, E_PURGECOMM);
		return -1;
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    w32GetLastError
 * Signature: (J)Ljava/lang/String;
 *
 * @return error string for linux/mac, error number converted to string for windows on success
 *         otherwise NULL if an error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jstring JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_w32GetLastError
  (JNIEnv *env, jobject obj, jlong handle) {

	DWORD error_number = 0;
	jstring error_string = NULL;
#if defined (_WIN32) && !defined(UNIX)
	char buffer[64];
#endif

	error_number = FT_W32_GetLastError((FT_HANDLE)handle);
	if(error_number == 0) {
		throw_serialcom_exception(env, 3, 0, E_GETLASTERROR);
		return NULL;
	}

#if defined (_WIN32) && !defined(UNIX)
	memset(buffer, '\0', sizeof(buffer));
	snprintf(buffer, 64, "%u", error_number);
	error_string = (*env)->NewStringUTF(env, buffer);
	if((error_string == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
		return NULL;
	}
#endif

	/* In Linux and Mac OS X, this function returns a DWORD that directly maps to the FT Errors
	 * (for example the FT_INVALID_HANDLE error number). */
#if defined (__linux__) || defined (__APPLE__)
	switch (error_number) {
		case FT_INVALID_HANDLE: error_string = (*env)->NewStringUTF(env, "FT_INVALID_HANDLE");
			break;
		case FT_DEVICE_NOT_FOUND: error_string = (*env)->NewStringUTF(env, "FT_DEVICE_NOT_FOUND");
			break;
		case FT_DEVICE_NOT_OPENED: error_string = (*env)->NewStringUTF(env, "FT_DEVICE_NOT_OPENED");
			break;
		case FT_IO_ERROR: error_string = (*env)->NewStringUTF(env, "FT_IO_ERROR");
			break;
		case FT_INSUFFICIENT_RESOURCES: error_string = (*env)->NewStringUTF(env, "FT_INSUFFICIENT_RESOURCES");
			break;
		case FT_INVALID_PARAMETER: error_string = (*env)->NewStringUTF(env, "FT_INVALID_PARAMETER");
			break;
		case FT_INVALID_BAUD_RATE: error_string = (*env)->NewStringUTF(env, "FT_INVALID_BAUD_RATE");
			break;
		case FT_DEVICE_NOT_OPENED_FOR_ERASE: error_string = (*env)->NewStringUTF(env, "FT_DEVICE_NOT_OPENED_FOR_ERASE");
			break;
		case FT_DEVICE_NOT_OPENED_FOR_WRITE: error_string = (*env)->NewStringUTF(env, "FT_DEVICE_NOT_OPENED_FOR_WRITE");
			break;
		case FT_FAILED_TO_WRITE_DEVICE: error_string = (*env)->NewStringUTF(env, "FT_FAILED_TO_WRITE_DEVICE");
			break;
		case FT_EEPROM_READ_FAILED: error_string = (*env)->NewStringUTF(env, "FT_EEPROM_READ_FAILED");
			break;
		case FT_EEPROM_WRITE_FAILED: error_string = (*env)->NewStringUTF(env, "FT_EEPROM_WRITE_FAILED");
			break;
		case FT_EEPROM_ERASE_FAILED: error_string = (*env)->NewStringUTF(env, "FT_EEPROM_ERASE_FAILED");
			break;
		case FT_EEPROM_NOT_PRESENT: error_string = (*env)->NewStringUTF(env, "FT_EEPROM_NOT_PRESENT");
			break;
		case FT_EEPROM_NOT_PROGRAMMED: error_string = (*env)->NewStringUTF(env, "FT_EEPROM_NOT_PROGRAMMED");
			break;
		case FT_INVALID_ARGS: error_string = (*env)->NewStringUTF(env, "FT_INVALID_ARGS");
			break;
		case FT_NOT_SUPPORTED: error_string = (*env)->NewStringUTF(env, "FT_NOT_SUPPORTED");
			break;
		case FT_OTHER_ERROR: error_string = (*env)->NewStringUTF(env, "FT_OTHER_ERROR");
			break;
		case FT_DEVICE_LIST_NOT_READY: error_string = (*env)->NewStringUTF(env, "FT_DEVICE_LIST_NOT_READY");
			break;
		default : error_string = (*env)->NewStringUTF(env, "Unknown error occurred !");
	}

	if((error_string == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
		return NULL;
	}
#endif

	return error_string;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    w32ClearCommError
 * Signature: (J)[I
 *
 * @return array of integers containing values on success otherwise -1 if an error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jintArray JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_w32ClearCommError
  (JNIEnv *env, jobject obj, jlong handle) {

	BOOL ret = 0;
	FTCOMSTAT comstat;
	DWORD errors;
	jintArray info = NULL;
	jint values[11];

	ret = FT_W32_ClearCommError((FT_HANDLE)handle, &errors, &comstat);
	if(ret == 0) {
		throw_serialcom_exception(env, 3, 0, E_CLEARCOMMERROR);
		return NULL;
	}

	info = (*env)->NewIntArray(env, 11);
	if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_NEWINTARRAYSTR);
		return NULL;
	}
	values[0]  = (jint) errors;
	values[1]  = (jint) comstat.fCtsHold;
	values[2]  = (jint) comstat.fDsrHold;
	values[3]  = (jint) comstat.fRlsdHold;
	values[4]  = (jint) comstat.fXoffHold;
	values[5]  = (jint) comstat.fXoffSent;
	values[6]  = (jint) comstat.fEof;
	values[7]  = (jint) comstat.fTxim;
	values[8]  = (jint) comstat.fReserved;
	values[9]  = (jint) comstat.cbInQue;
	values[10] = (jint) comstat.cbOutQue;

	(*env)->SetIntArrayRegion(env, info, 0, 11, values);
	if((*env)->ExceptionOccurred(env) != NULL) {
		throw_serialcom_exception(env, 3, 0, E_SETINTARRREGIONSTR);
		return NULL;
	}
	return info;
}
