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

	ftStatus = FT_GetVIDPID(&vid, &pid);
	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return NULL;
	}

	vidpidcombination = (*env)->NewIntArray(env, 2);
	(*env)->SetIntArrayRegion(env, vidpidcombination, 0, 1, &vid);
	if((*env)->ExceptionOccurred(env)) {
		throw_serialcom_exception(env, 3, 0, E_SETINTARRREGIONSTR);
		return NULL;
	}
	(*env)->SetIntArrayRegion(env, vidpidcombination, 1, 1, &pid);
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
	FT_STATUS ftStatus;
	FT_DEVICE_LIST_INFO_NODE *devInfo;
	unsigned int num_of_devices = numOfDevices;
	int i = 0;
	char hexcharbuffer[256];
	struct jstrarray_list list = {0};
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
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return NULL;
	}

	init_jstrarraylist(&list, 100);

	for (i = 0; i < num_of_devices; i++) {
		memset(hexcharbuffer, '\0', sizeof(hexcharbuffer));
		snprintf(hexcharbuffer, 256, "%x", devInfo[i].Flags);
		info = (*env)->NewStringUTF(env, hexcharbuffer);
		if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			free(devInfo);
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
			return NULL;
		}
		insert_jstrarraylist(&list, info);

		memset(hexcharbuffer, '\0', sizeof(hexcharbuffer));
		snprintf(hexcharbuffer, 256, "%x", devInfo[i].Type);
		info = (*env)->NewStringUTF(env, hexcharbuffer);
		if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			free(devInfo);
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
			return NULL;
		}
		insert_jstrarraylist(&list, info);

		memset(hexcharbuffer, '\0', sizeof(hexcharbuffer));
		snprintf(hexcharbuffer, 256, "%x", devInfo[i].ID);
		info = (*env)->NewStringUTF(env, hexcharbuffer);
		if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			free(devInfo);
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
			return NULL;
		}
		insert_jstrarraylist(&list, info);

		memset(hexcharbuffer, '\0', sizeof(hexcharbuffer));
		snprintf(hexcharbuffer, 256, "%x", devInfo[i].LocId);
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
		snprintf(hexcharbuffer, 256, "%x", devInfo[i].ftHandle);
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
	FT_STATUS ftStatus;
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
	snprintf(hexcharbuffer, 256, "%x", devInfo->Flags);
	info = (*env)->NewStringUTF(env, hexcharbuffer);
	if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		free(devInfo);
		free_jstrarraylist(&list);
		throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
		return NULL;
	}
	insert_jstrarraylist(&list, info);
	memset(hexcharbuffer, '\0', sizeof(hexcharbuffer));
	snprintf(hexcharbuffer, 256, "%x", devInfo->Type);
	info = (*env)->NewStringUTF(env, hexcharbuffer);
	if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		free(devInfo);
		free_jstrarraylist(&list);
		throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
		return NULL;
	}
	insert_jstrarraylist(&list, info);
	memset(hexcharbuffer, '\0', sizeof(hexcharbuffer));
	snprintf(hexcharbuffer, 256, "%x", devInfo->ID);
	info = (*env)->NewStringUTF(env, hexcharbuffer);
	if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		free(devInfo);
		free_jstrarraylist(&list);
		throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
		return NULL;
	}
	insert_jstrarraylist(&list, info);
	memset(hexcharbuffer, '\0', sizeof(hexcharbuffer));
	snprintf(hexcharbuffer, 256, "%x", devInfo->LocId);
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
	snprintf(hexcharbuffer, 256, "%x", devInfo->ftHandle);
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
 * Method:    open
 * Signature: (I)J
 *
 * @return handle on success otherwise -1 if error occurs.
 * @throws SerialComException if any FTDI D2XX function, JNI function, system call or C function fails.
 */
JNIEXPORT jlong JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_open(JNIEnv *env, jobject obj, jint index) {
	FT_HANDLE ftHandle;
	FT_STATUS ftStatus;

	ftStatus = FT_Open(index, &ftHandle);
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
	FT_STATUS ftStatus;
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
	FT_STATUS ftStatus;
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
	FT_STATUS ftStatus;
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
	FT_STATUS ftStatus;
	ftStatus = FT_SetBaudRate(ftHandle, (int)baudRate);
	if(ftStatus != FT_OK) {
		throw_serialcom_exception(env, 2, ftStatus, NULL);
		return -1;
	}
	return 0;
}































#endif
