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







#endif
