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

#include <stdarg.h>      /* ISO C Standard. Variable arguments  */
#include <stdio.h>       /* ISO C99 Standard: Input/output      */
#include <stdlib.h>      /* Standard ANSI routines              */
#include <string.h>      /* String function definitions         */

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

#include "scm_cp210xruntime.h"

/* Common interface with java layer for supported OS types. */
#include "../../com_embeddedunveiled_serial_internal_SerialComCP210xRuntimeJNIBridge.h"

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComCP210xRuntimeJNIBridge
 * Method:    readLatch
 * Signature: (J)I
 *
 * @return latch value on success otherwise -1 if an error occurs.
 * @throws SerialComException if any CP210xRT function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComCP210xRuntimeJNIBridge_readLatch
(JNIEnv *env, jobject obj, jlong handle) {

	WORD latch = 0;
	CP210x_STATUS ret = 0;

	ret = CP210xRT_ReadLatch((HANDLE)handle, &latch);
	if(ret != CP210x_SUCCESS) {
		throw_serialcom_exception(env, 2, ret, NULL);
		return -1;
	}

	return (jint)latch;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComCP210xRuntimeJNIBridge
 * Method:    writeLatch
 * Signature: (JJJ)I
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any CP210xRT function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComCP210xRuntimeJNIBridge_writeLatch
(JNIEnv *env, jobject obj, jlong handle, jlong maskValue, jlong latchValue) {

	WORD mask = 0;
	WORD latch = 0;
	CP210x_STATUS ret = 0;

	if((maskValue & SCM_CP210x_GPIO_0) == SCM_CP210x_GPIO_0) {
		mask = mask | CP210x_GPIO_0;
		latch = latch | CP210x_GPIO_0;
	}
	if((maskValue & SCM_CP210x_GPIO_1) == SCM_CP210x_GPIO_1) {
		mask = mask | CP210x_GPIO_1;
		latch = latch | CP210x_GPIO_1;
	}
	if((maskValue & SCM_CP210x_GPIO_2) == SCM_CP210x_GPIO_2) {
		mask = mask | CP210x_GPIO_2;
		latch = latch | CP210x_GPIO_2;
	}
	if((maskValue & SCM_CP210x_GPIO_3) == SCM_CP210x_GPIO_3) {
		mask = mask | CP210x_GPIO_3;
		latch = latch | CP210x_GPIO_3;
	}
	if((maskValue & SCM_CP210x_GPIO_4) == SCM_CP210x_GPIO_4) {
		mask = mask | CP210x_GPIO_4;
		latch = latch | CP210x_GPIO_4;
	}
	if((maskValue & SCM_CP210x_GPIO_5) == SCM_CP210x_GPIO_5) {
		mask = mask | CP210x_GPIO_5;
		latch = latch | CP210x_GPIO_5;
	}
	if((maskValue & SCM_CP210x_GPIO_6) == SCM_CP210x_GPIO_6) {
		mask = mask | CP210x_GPIO_6;
		latch = latch | CP210x_GPIO_6;
	}
	if((maskValue & SCM_CP210x_GPIO_7) == SCM_CP210x_GPIO_7) {
		mask = mask | CP210x_GPIO_7;
		latch = latch | CP210x_GPIO_7;
	}
	if((maskValue & SCM_CP210x_GPIO_8) == SCM_CP210x_GPIO_8) {
		mask = mask | CP210x_GPIO_8;
		latch = latch | CP210x_GPIO_8;
	}
	if((maskValue & SCM_CP210x_GPIO_9) == SCM_CP210x_GPIO_9) {
		mask = mask | CP210x_GPIO_9;
		latch = latch | CP210x_GPIO_9;
	}
	if((maskValue & SCM_CP210x_GPIO_10) == SCM_CP210x_GPIO_10) {
		mask = mask | CP210x_GPIO_10;
		latch = latch | CP210x_GPIO_10;
	}
	if((maskValue & SCM_CP210x_GPIO_11) == SCM_CP210x_GPIO_11) {
		mask = mask | CP210x_GPIO_11;
		latch = latch | CP210x_GPIO_11;
	}
	if((maskValue & SCM_CP210x_GPIO_12) == SCM_CP210x_GPIO_12) {
		mask = mask | CP210x_GPIO_12;
		latch = latch | CP210x_GPIO_12;
	}
	if((maskValue & SCM_CP210x_GPIO_13) == SCM_CP210x_GPIO_13) {
		mask = mask | CP210x_GPIO_13;
		latch = latch | CP210x_GPIO_13;
	}
	if((maskValue & SCM_CP210x_GPIO_14) == SCM_CP210x_GPIO_14) {
		mask = mask | CP210x_GPIO_14;
		latch = latch | CP210x_GPIO_14;
	}
	if((maskValue & SCM_CP210x_GPIO_15) == SCM_CP210x_GPIO_15) {
		mask = mask | CP210x_GPIO_15;
		latch = latch | CP210x_GPIO_15;
	}

	ret = CP210xRT_WriteLatch((HANDLE)handle, mask, latch);
	if(ret != CP210x_SUCCESS) {
		throw_serialcom_exception(env, 2, ret, NULL);
		return -1;
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComCP210xRuntimeJNIBridge
 * Method:    getPartNumber
 * Signature: (J)Ljava/lang/String;
 *
 * @return part number string on success otherwise NULL if an error occurs.
 * @throws SerialComException if any CP210xRT function, JNI function, system call or C function fails.
 */
JNIEXPORT jstring JNICALL Java_com_embeddedunveiled_serial_internal_SerialComCP210xRuntimeJNIBridge_getPartNumber
(JNIEnv *env, jobject obj, jlong handle) {

	CP210x_STATUS ret = 0;
	BYTE part_num;
	char number[16];
	jstring info = NULL;

	ret = CP210xRT_GetPartNumber((HANDLE)handle, &part_num);
	if(ret != CP210x_SUCCESS) {
		throw_serialcom_exception(env, 2, ret, NULL);
		return NULL;
	}

	memset(number, '\0', sizeof(number));
	snprintf(number, 16, "CP21%02d", part_num);
	info = (*env)->NewStringUTF(env, number);
	if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
		return NULL;
	}

	return info;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComCP210xRuntimeJNIBridge
 * Method:    getDeviceProductString
 * Signature: (J)Ljava/lang/String;
 *
 * @return product string on success otherwise NULL if an error occurs.
 * @throws SerialComException if any CP210xRT function, JNI function, system call or C function fails.
 */
JNIEXPORT jstring JNICALL Java_com_embeddedunveiled_serial_internal_SerialComCP210xRuntimeJNIBridge_getDeviceProductString
(JNIEnv *env, jobject obj, jlong handle) {

	CP210x_STATUS ret = 0;
	CP210x_PRODUCT_STRING product;
	BYTE length;
	jstring info = NULL;

	memset(product, '\0', sizeof(product));
	ret = CP210xRT_GetDeviceProductString((HANDLE)handle, product, &length, FALSE);
	if(ret != CP210x_SUCCESS) {
		throw_serialcom_exception(env, 2, ret, NULL);
		return NULL;
	}

	info = (*env)->NewStringUTF(env, product);
	if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
		return NULL;
	}

	return info;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComCP210xRuntimeJNIBridge
 * Method:    getDeviceSerialNumber
 * Signature: (J)Ljava/lang/String;
 *
 * @return serial number string on success otherwise NULL if an error occurs.
 * @throws SerialComException if any CP210xRT function, JNI function, system call or C function fails.
 */
JNIEXPORT jstring JNICALL Java_com_embeddedunveiled_serial_internal_SerialComCP210xRuntimeJNIBridge_getDeviceSerialNumber
(JNIEnv *env, jobject obj, jlong handle) {

	CP210x_STATUS ret = 0;
	CP210x_SERIAL_STRING serial_number;
	BYTE length;
	jstring info = NULL;

	memset(serial_number, '\0', sizeof(serial_number));
	ret = CP210xRT_GetDeviceSerialNumber((HANDLE)handle, serial_number, &length, FALSE);
	if(ret != CP210x_SUCCESS) {
		throw_serialcom_exception(env, 2, ret, NULL);
		return NULL;
	}

	info = (*env)->NewStringUTF(env, serial_number);
	if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
		return NULL;
	}

	return info;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComCP210xRuntimeJNIBridge
 * Method:    getDeviceInterfaceString
 * Signature: (J)Ljava/lang/String;
 *
 * @return device interface string on success otherwise NULL if an error occurs.
 * @throws SerialComException if any CP210xRT function, JNI function, system call or C function fails.
 */
JNIEXPORT jstring JNICALL Java_com_embeddedunveiled_serial_internal_SerialComCP210xRuntimeJNIBridge_getDeviceInterfaceString
(JNIEnv *env, jobject obj, jlong handle) {

	CP210x_STATUS ret = 0;
	CP210x_SERIAL_STRING interface;
	BYTE length;
	jstring info = NULL;

	memset(interface, '\0', sizeof(interface));
	ret = CP210xRT_GetDeviceInterfaceString((HANDLE)handle, interface, &length, FALSE);
	if(ret != CP210x_SUCCESS) {
		throw_serialcom_exception(env, 2, ret, NULL);
		return NULL;
	}

	info = (*env)->NewStringUTF(env, interface);
	if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
		return NULL;
	}

	return info;
}
