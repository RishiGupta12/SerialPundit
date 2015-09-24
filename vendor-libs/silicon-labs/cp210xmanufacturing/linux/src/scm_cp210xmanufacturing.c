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

#include "CP210xManufacturing.h"
#include "scm_cp210xmanufacturing.h"

/* Common interface with java layer for supported OS types. */
#include "../../com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge.h"

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge
 * Method:    getNumDevices
 * Signature: ()I
 *
 * @return number of devices connected on success otherwise -1 if an error occurs.
 * @throws SerialComException if any CP210x function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge_getNumDevices(JNIEnv *env, jobject obj) {

	DWORD num_devices = 0;
	CP210x_STATUS ret = 0;

	ret = CP210x_GetNumDevices(&num_devices);
	if(ret != CP210x_SUCCESS) {
		throw_serialcom_exception(env, 2, ret, NULL);
		return -1;
	}

	return (jint)num_devices;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge
 * Method:    getProductString
 * Signature: (II)Ljava/lang/String;
 *
 * @return serial number or product string or full path on success otherwise NULL if an error occurs.
 * @throws SerialComException if any CP210x function, JNI function, system call or C function fails.
 */
JNIEXPORT jstring JNICALL Java_com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge_getProductString
(JNIEnv *env, jobject obj, jint index, jint flag) {

	CP210x_STATUS ret = 0;
	CP210x_DEVICE_STRING dev_buffer;
	DWORD infoflag = 0;
	jstring info = NULL;

	if((flag & SCM_CP210x_RETURN_SERIAL_NUMBER) == SCM_CP210x_RETURN_SERIAL_NUMBER) {
		infoflag = infoflag | CP210x_RETURN_SERIAL_NUMBER;
	}else 	if((flag & SCM_CP210x_RETURN_DESCRIPTION) == SCM_CP210x_RETURN_DESCRIPTION) {
		infoflag = infoflag | CP210x_RETURN_DESCRIPTION;
	}else 	if((flag & SCM_CP210x_RETURN_FULL_PATH) == SCM_CP210x_RETURN_FULL_PATH) {
		infoflag = infoflag | CP210x_RETURN_FULL_PATH;
	}else {
	}

	memset(dev_buffer, '\0', sizeof(dev_buffer));
	ret = CP210x_GetProductString((DWORD)index, dev_buffer, infoflag);
	if(ret != CP210x_SUCCESS) {
		throw_serialcom_exception(env, 2, ret, NULL);
		return NULL;
	}

	info = (*env)->NewStringUTF(env, dev_buffer);
	if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
		return NULL;
	}

	return info;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge
 * Method:    open
 * Signature: (I)J
 *
 * @return valid handle on success otherwise -1 if an error occurs.
 * @throws SerialComException if any CP210x function, JNI function, system call or C function fails.
 */
JNIEXPORT jlong JNICALL Java_com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge_open
(JNIEnv *env, jobject obj, jint deviceNumber) {

	CP210x_STATUS ret = 0;
	HANDLE handle;

	ret = CP210x_Open((DWORD)deviceNumber, &handle);
	if(ret != CP210x_SUCCESS) {
		throw_serialcom_exception(env, 2, ret, NULL);
		return -1;
	}

	return (jlong)handle;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge
 * Method:    close
 * Signature: (J)I
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any CP210x function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge_close
(JNIEnv *env, jobject obj, jlong handle) {

	CP210x_STATUS ret = 0;

	ret = CP210x_Close((HANDLE)handle);
	if(ret != CP210x_SUCCESS) {
		throw_serialcom_exception(env, 2, ret, NULL);
		return -1;
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge
 * Method:    getPartNumber
 * Signature: (J)Ljava/lang/String;
 *
 * @return part number string on success otherwise NULL if an error occurs.
 * @throws SerialComException if any CP210x function, JNI function, system call or C function fails.
 */
JNIEXPORT jstring JNICALL Java_com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge_getPartNumber
(JNIEnv *env, jobject obj, jlong handle) {

	CP210x_STATUS ret = 0;
	BYTE part_num;
	jstring info = NULL;

	ret = CP210x_GetPartNumber((HANDLE)handle, &part_num);
	if(ret != CP210x_SUCCESS) {
		throw_serialcom_exception(env, 2, ret, NULL);
		return NULL;
	}

	if(part_num == CP210x_CP2101_VERSION) {
		info = (*env)->NewStringUTF(env, "CP2101");
	}else if(part_num == CP210x_CP2102_VERSION) {
		info = (*env)->NewStringUTF(env, "CP2102");
	}else if(part_num == CP210x_CP2103_VERSION) {
		info = (*env)->NewStringUTF(env, "CP2103");
	}else if(part_num == CP210x_CP2104_VERSION) {
		info = (*env)->NewStringUTF(env, "CP2104");
	}else if(part_num == CP210x_CP2105_VERSION) {
		info = (*env)->NewStringUTF(env, "CP2105");
	}else if(part_num == CP210x_CP2108_VERSION) {
		info = (*env)->NewStringUTF(env, "CP2108");
	}else if(part_num == CP210x_CP2109_VERSION) {
		info = (*env)->NewStringUTF(env, "CP2109");
	}else {
	}

	if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
		return NULL;
	}

	return info;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge
 * Method:    setVid
 * Signature: (JI)I
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any CP210x function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge_setVid
(JNIEnv *env, jobject obj, jlong handle, jint usbVID) {

	CP210x_STATUS ret = 0;

	ret = CP210x_SetVid((HANDLE)handle, (WORD)usbVID);
	if(ret != CP210x_SUCCESS) {
		throw_serialcom_exception(env, 2, ret, NULL);
		return -1;
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge
 * Method:    setPid
 * Signature: (JI)I
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any CP210x function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge_setPid
(JNIEnv *env, jobject obj, jlong handle, jint usbPID) {

	CP210x_STATUS ret = 0;

	ret = CP210x_SetPid((HANDLE)handle, (WORD)usbPID);
	if(ret != CP210x_SUCCESS) {
		throw_serialcom_exception(env, 2, ret, NULL);
		return -1;
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge
 * Method:    setProductString
 * Signature: (JLjava/lang/String;)I
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any CP210x function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge_setProductString
(JNIEnv *env, jobject obj, jlong handle, jstring productString) {

	CP210x_STATUS ret = 0;
	const char* product = NULL;

	product = (*env)->GetStringUTFChars(env, productString, NULL);
	if((product == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_GETSTRUTFCHARSTR);
		return -1;
	}

	ret = CP210x_SetProductString((HANDLE)handle, (char*)product, (BYTE)strlen(product), TRUE);
	if(ret != CP210x_SUCCESS) {
		throw_serialcom_exception(env, 2, ret, NULL);
		return -1;
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge
 * Method:    setSerialNumber
 * Signature: (JLjava/lang/String;)I
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any CP210x function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge_setSerialNumber
(JNIEnv *env, jobject obj, jlong handle, jstring serialNumber) {

	CP210x_STATUS ret = 0;
	const char* serial = NULL;

	serial = (*env)->GetStringUTFChars(env, serialNumber, NULL);
	if((serial == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_GETSTRUTFCHARSTR);
		return -1;
	}

	ret = CP210x_SetSerialNumber((HANDLE)handle, (char*)serial, (BYTE)strlen(serial), TRUE);
	if(ret != CP210x_SUCCESS) {
		throw_serialcom_exception(env, 2, ret, NULL);
		return -1;
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge
 * Method:    setInterfaceString
 * Signature: (JBLjava/lang/String;)I
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any CP210x function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge_setInterfaceString
(JNIEnv *env, jobject obj, jlong handle, jbyte bInterfaceNumber, jstring interfaceString) {

	CP210x_STATUS ret = 0;
	const char* iString = NULL;

	iString = (*env)->GetStringUTFChars(env, interfaceString, NULL);
	if((iString == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_GETSTRUTFCHARSTR);
		return -1;
	}

	ret = CP210x_SetInterfaceString((HANDLE)handle, (BYTE)bInterfaceNumber, (char*)iString, (BYTE)strlen(iString), TRUE);
	if(ret != CP210x_SUCCESS) {
		throw_serialcom_exception(env, 2, ret, NULL);
		return -1;
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge
 * Method:    setSelfPower
 * Signature: (JZ)I
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any CP210x function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge_setSelfPower
(JNIEnv *env, jobject obj, jlong handle, jboolean selfPower) {

	CP210x_STATUS ret = 0;
	BOOL self_power;

	if(selfPower == JNI_TRUE) {
		self_power = TRUE;
	}else {
		self_power = FALSE;
	}

	ret = CP210x_SetSelfPower((HANDLE)handle, self_power);
	if(ret != CP210x_SUCCESS) {
		throw_serialcom_exception(env, 2, ret, NULL);
		return -1;
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge
 * Method:    setMaxPower
 * Signature: (JB)I
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any CP210x function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge_setMaxPower
(JNIEnv *env, jobject obj, jlong handle, jbyte maxPower) {

	CP210x_STATUS ret = 0;

	ret = CP210x_SetMaxPower((HANDLE)handle, (BYTE)maxPower);
	if(ret != CP210x_SUCCESS) {
		throw_serialcom_exception(env, 2, ret, NULL);
		return -1;
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge
 * Method:    setFlushBufferConfig
 * Signature: (JI)I
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any CP210x function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge_setFlushBufferConfig
(JNIEnv *env, jobject obj, jlong handle, jint flag) {

	CP210x_STATUS ret = 0;
	BYTE bFlushBufferConfig = 0x00;

	if((flag & SCM_FC_OPEN_TX) == SCM_FC_OPEN_TX) {
		bFlushBufferConfig = bFlushBufferConfig | FC_OPEN_TX;
	}
	if((flag & SCM_FC_OPEN_RX) == SCM_FC_OPEN_RX) {
		bFlushBufferConfig = bFlushBufferConfig | FC_OPEN_RX;
	}
	if((flag & SCM_FC_CLOSE_TX) == SCM_FC_CLOSE_TX) {
		bFlushBufferConfig = bFlushBufferConfig | FC_CLOSE_TX;
	}
	if((flag & SCM_FC_CLOSE_RX) == SCM_FC_CLOSE_RX) {
		bFlushBufferConfig = bFlushBufferConfig | FC_CLOSE_RX;
	}

	ret = CP210x_SetFlushBufferConfig((HANDLE)handle, bFlushBufferConfig);
	if(ret != CP210x_SUCCESS) {
		throw_serialcom_exception(env, 2, ret, NULL);
		return -1;
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge
 * Method:    setDeviceMode
 * Signature: (JBB)I
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any CP210x function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge_setDeviceMode
(JNIEnv *env, jobject obj, jlong handle, jbyte bDeviceModeECI, jbyte bDeviceModeSCI) {

	CP210x_STATUS ret = 0;

	ret = CP210x_SetDeviceMode((HANDLE)handle, (BYTE)bDeviceModeECI, (BYTE)bDeviceModeSCI);
	if(ret != CP210x_SUCCESS) {
		throw_serialcom_exception(env, 2, ret, NULL);
		return -1;
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge
 * Method:    setDeviceVersion
 * Signature: (JI)I
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any CP210x function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge_setDeviceVersion
(JNIEnv *env, jobject obj, jlong handle, jint version) {

	CP210x_STATUS ret = 0;

	ret = CP210x_SetDeviceVersion((HANDLE)handle, (WORD)version);
	if(ret != CP210x_SUCCESS) {
		throw_serialcom_exception(env, 2, ret, NULL);
		return -1;
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge
 * Method:    setBaudRateConfig
 * Signature: (JIIII)I
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any CP210x function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge_setBaudRateConfig
(JNIEnv *env, jobject obj, jlong handle, jint baudGen, jint timer0Reload, jint prescalar, jint baudrate) {

	CP210x_STATUS ret = 0;
	BAUD_CONFIG configuration;

	configuration.BaudGen = (WORD)baudGen;
	configuration.Timer0Reload = (WORD)timer0Reload;
	configuration.Prescaler = (BYTE)prescalar;
	configuration.BaudRate = (DWORD)baudrate;

	ret = CP210x_SetBaudRateConfig((HANDLE)handle, &configuration);
	if(ret != CP210x_SUCCESS) {
		throw_serialcom_exception(env, 2, ret, NULL);
		return -1;
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge
 * Method:    setPortConfig
 * Signature: (JIIII)I
 *
 * This function may be mainly applicable for CP2103/4 devices.
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any CP210x function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge_setPortConfig
(JNIEnv *env, jobject obj, jlong handle, jint mode, jint resetLatch, jint suspendLatch, jint enhancedFxn) {

	CP210x_STATUS ret = 0;
	PORT_CONFIG configuration = { 0 };

	/* mode push pull or open drain */
	if((mode & SCM_PORT_RI_ON) == SCM_PORT_RI_ON) {
		configuration.Mode = configuration.Mode | PORT_RI_ON;
	}
	if((mode & SCM_PORT_DCD_ON) == SCM_PORT_DCD_ON) {
		configuration.Mode = configuration.Mode | PORT_DCD_ON;
	}
	if((mode & SCM_PORT_DTR_ON) == SCM_PORT_DTR_ON) {
		configuration.Mode = configuration.Mode | PORT_DTR_ON;
	}
	if((mode & SCM_PORT_DSR_ON) == SCM_PORT_DSR_ON) {
		configuration.Mode = configuration.Mode | PORT_DSR_ON;
	}
	if((mode & SCM_PORT_TXD_ON) == SCM_PORT_TXD_ON) {
		configuration.Mode = configuration.Mode | PORT_TXD_ON;
	}
	if((mode & SCM_PORT_RXD_ON) == SCM_PORT_RXD_ON) {
		configuration.Mode = configuration.Mode | PORT_RXD_ON;
	}
	if((mode & SCM_PORT_RTS_ON) == SCM_PORT_RTS_ON) {
		configuration.Mode = configuration.Mode | PORT_RTS_ON;
	}
	if((mode & SCM_PORT_CTS_ON) == SCM_PORT_CTS_ON) {
		configuration.Mode = configuration.Mode | PORT_CTS_ON;
	}
	if((mode & SCM_PORT_GPIO_0_ON) == SCM_PORT_GPIO_0_ON) {
		configuration.Mode = configuration.Mode | PORT_GPIO_0_ON;
	}
	if((mode & SCM_PORT_GPIO_1_ON) == SCM_PORT_GPIO_1_ON) {
		configuration.Mode = configuration.Mode | PORT_GPIO_1_ON;
	}
	if((mode & SCM_PORT_GPIO_2_ON) == SCM_PORT_GPIO_2_ON) {
		configuration.Mode = configuration.Mode | PORT_GPIO_2_ON;
	}
	if((mode & SCM_PORT_GPIO_3_ON) == SCM_PORT_GPIO_3_ON) {
		configuration.Mode = configuration.Mode | PORT_GPIO_3_ON;
	}
	if((mode & SCM_PORT_SUSPEND_ON) == SCM_PORT_SUSPEND_ON) {
		configuration.Mode = configuration.Mode | PORT_SUSPEND_ON;
	}
	if((mode & SCM_PORT_SUSPEND_BAR_ON) == SCM_PORT_SUSPEND_BAR_ON) {
		configuration.Mode = configuration.Mode | PORT_SUSPEND_BAR_ON;
	}

	/* reset latch logic high or low */
	if((resetLatch & SCM_PORT_RI_ON) == SCM_PORT_RI_ON) {
		configuration.Reset_Latch = configuration.Reset_Latch | PORT_RI_ON;
	}
	if((resetLatch & SCM_PORT_DCD_ON) == SCM_PORT_DCD_ON) {
		configuration.Reset_Latch = configuration.Reset_Latch | PORT_DCD_ON;
	}
	if((resetLatch & SCM_PORT_DTR_ON) == SCM_PORT_DTR_ON) {
		configuration.Reset_Latch = configuration.Reset_Latch | PORT_DTR_ON;
	}
	if((resetLatch & SCM_PORT_DSR_ON) == SCM_PORT_DSR_ON) {
		configuration.Reset_Latch = configuration.Reset_Latch | PORT_DSR_ON;
	}
	if((resetLatch & SCM_PORT_TXD_ON) == SCM_PORT_TXD_ON) {
		configuration.Reset_Latch = configuration.Reset_Latch | PORT_TXD_ON;
	}
	if((resetLatch & SCM_PORT_RXD_ON) == SCM_PORT_RXD_ON) {
		configuration.Reset_Latch = configuration.Reset_Latch | PORT_RXD_ON;
	}
	if((resetLatch & SCM_PORT_RTS_ON) == SCM_PORT_RTS_ON) {
		configuration.Reset_Latch = configuration.Reset_Latch | PORT_RTS_ON;
	}
	if((resetLatch & SCM_PORT_CTS_ON) == SCM_PORT_CTS_ON) {
		configuration.Reset_Latch = configuration.Reset_Latch | PORT_CTS_ON;
	}
	if((resetLatch & SCM_PORT_GPIO_0_ON) == SCM_PORT_GPIO_0_ON) {
		configuration.Reset_Latch = configuration.Reset_Latch | PORT_GPIO_0_ON;
	}
	if((resetLatch & SCM_PORT_GPIO_1_ON) == SCM_PORT_GPIO_1_ON) {
		configuration.Reset_Latch = configuration.Reset_Latch | PORT_GPIO_1_ON;
	}
	if((resetLatch & SCM_PORT_GPIO_2_ON) == SCM_PORT_GPIO_2_ON) {
		configuration.Reset_Latch = configuration.Reset_Latch | PORT_GPIO_2_ON;
	}
	if((resetLatch & SCM_PORT_GPIO_3_ON) == SCM_PORT_GPIO_3_ON) {
		configuration.Reset_Latch = configuration.Reset_Latch | PORT_GPIO_3_ON;
	}
	if((resetLatch & SCM_PORT_SUSPEND_ON) == SCM_PORT_SUSPEND_ON) {
		configuration.Reset_Latch = configuration.Reset_Latch | PORT_SUSPEND_ON;
	}
	if((resetLatch & SCM_PORT_SUSPEND_BAR_ON) == SCM_PORT_SUSPEND_BAR_ON) {
		configuration.Reset_Latch = configuration.Reset_Latch | PORT_SUSPEND_BAR_ON;
	}

	/* suspend latch logic high or low */
	if((suspendLatch & SCM_PORT_RI_ON) == SCM_PORT_RI_ON) {
		configuration.Suspend_Latch = configuration.Suspend_Latch | PORT_RI_ON;
	}
	if((suspendLatch & SCM_PORT_DCD_ON) == SCM_PORT_DCD_ON) {
		configuration.Suspend_Latch = configuration.Suspend_Latch | PORT_DCD_ON;
	}
	if((suspendLatch & SCM_PORT_DTR_ON) == SCM_PORT_DTR_ON) {
		configuration.Suspend_Latch = configuration.Suspend_Latch | PORT_DTR_ON;
	}
	if((suspendLatch & SCM_PORT_DSR_ON) == SCM_PORT_DSR_ON) {
		configuration.Suspend_Latch = configuration.Suspend_Latch | PORT_DSR_ON;
	}
	if((suspendLatch & SCM_PORT_TXD_ON) == SCM_PORT_TXD_ON) {
		configuration.Suspend_Latch = configuration.Suspend_Latch | PORT_TXD_ON;
	}
	if((suspendLatch & SCM_PORT_RXD_ON) == SCM_PORT_RXD_ON) {
		configuration.Suspend_Latch = configuration.Suspend_Latch | PORT_RXD_ON;
	}
	if((suspendLatch & SCM_PORT_RTS_ON) == SCM_PORT_RTS_ON) {
		configuration.Suspend_Latch = configuration.Suspend_Latch | PORT_RTS_ON;
	}
	if((suspendLatch & SCM_PORT_CTS_ON) == SCM_PORT_CTS_ON) {
		configuration.Suspend_Latch = configuration.Suspend_Latch | PORT_CTS_ON;
	}
	if((suspendLatch & SCM_PORT_GPIO_0_ON) == SCM_PORT_GPIO_0_ON) {
		configuration.Suspend_Latch = configuration.Suspend_Latch | PORT_GPIO_0_ON;
	}
	if((suspendLatch & SCM_PORT_GPIO_1_ON) == SCM_PORT_GPIO_1_ON) {
		configuration.Suspend_Latch = configuration.Suspend_Latch | PORT_GPIO_1_ON;
	}
	if((suspendLatch & SCM_PORT_GPIO_2_ON) == SCM_PORT_GPIO_2_ON) {
		configuration.Suspend_Latch = configuration.Suspend_Latch | PORT_GPIO_2_ON;
	}
	if((suspendLatch & SCM_PORT_GPIO_3_ON) == SCM_PORT_GPIO_3_ON) {
		configuration.Suspend_Latch = configuration.Suspend_Latch | PORT_GPIO_3_ON;
	}
	if((suspendLatch & SCM_PORT_SUSPEND_ON) == SCM_PORT_SUSPEND_ON) {
		configuration.Suspend_Latch = configuration.Suspend_Latch | PORT_SUSPEND_ON;
	}
	if((suspendLatch & SCM_PORT_SUSPEND_BAR_ON) == SCM_PORT_SUSPEND_BAR_ON) {
		configuration.Suspend_Latch = configuration.Suspend_Latch | PORT_SUSPEND_BAR_ON;
	}

	/* enhanced functions */
	if((enhancedFxn & SCM_EF_GPIO_0_TXLED) == SCM_EF_GPIO_0_TXLED) {
		configuration.EnhancedFxn = configuration.EnhancedFxn | EF_GPIO_0_TXLED;
	}
	if((enhancedFxn & SCM_EF_GPIO_1_RXLED) == SCM_EF_GPIO_1_RXLED) {
		configuration.EnhancedFxn = configuration.EnhancedFxn | EF_GPIO_1_RXLED;
	}
	if((enhancedFxn & SCM_EF_GPIO_2_RS485) == SCM_EF_GPIO_2_RS485) {
		configuration.EnhancedFxn = configuration.EnhancedFxn | EF_GPIO_2_RS485;
	}
	if((enhancedFxn & SCM_EF_RS485_INVERT) == SCM_EF_RS485_INVERT) {
		configuration.EnhancedFxn = configuration.EnhancedFxn | EF_RS485_INVERT;
	}
	if((enhancedFxn & SCM_EF_WEAKPULLUP) == SCM_EF_WEAKPULLUP) {
		configuration.EnhancedFxn = configuration.EnhancedFxn | EF_WEAKPULLUP;
	}
	if((enhancedFxn & SCM_EF_RESERVED_1) == SCM_EF_RESERVED_1) {
		configuration.EnhancedFxn = configuration.EnhancedFxn | EF_RESERVED_1;
	}
	if((enhancedFxn & SCM_EF_SERIAL_DYNAMIC_SUSPEND) == SCM_EF_SERIAL_DYNAMIC_SUSPEND) {
		configuration.EnhancedFxn = configuration.EnhancedFxn | EF_SERIAL_DYNAMIC_SUSPEND;
	}
	if((enhancedFxn & SCM_EF_GPIO_DYNAMIC_SUSPEND) == SCM_EF_GPIO_DYNAMIC_SUSPEND) {
		configuration.EnhancedFxn = configuration.EnhancedFxn | EF_GPIO_DYNAMIC_SUSPEND;
	}

	ret = CP210x_SetPortConfig((HANDLE)handle, &configuration);
	if(ret != CP210x_SUCCESS) {
		throw_serialcom_exception(env, 2, ret, NULL);
		return -1;
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge
 * Method:    setDualPortConfig
 * Signature: (JIIIIII)I
 *
 * This function may be mainly applicable for CP2105 devices.
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any CP210x function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge_setDualPortConfig
(JNIEnv *env, jobject obj, jlong handle, jint mode, jint resetLatch, jint suspendLatch, jint enhancedFxnECI,
		jint enhancedFxnSCI, jint enhancedFxnDevice) {

	CP210x_STATUS ret = 0;
	DUAL_PORT_CONFIG configuration = { 0 };

	/* mode push pull or open drain */
	if((mode & SCM_PORT_RI_SCI_ON) == SCM_PORT_RI_SCI_ON) {
		configuration.Mode = configuration.Mode | PORT_RI_SCI_ON;
	}
	if((mode & SCM_PORT_DCD_SCI_ON) == SCM_PORT_DCD_SCI_ON) {
		configuration.Mode = configuration.Mode | PORT_DCD_SCI_ON;
	}
	if((mode & SCM_PORT_DTR_SCI_ON) == SCM_PORT_DTR_SCI_ON) {
		configuration.Mode = configuration.Mode | PORT_DTR_SCI_ON;
	}
	if((mode & SCM_PORT_DSR_SCI_ON) == SCM_PORT_DSR_SCI_ON) {
		configuration.Mode = configuration.Mode | PORT_DSR_SCI_ON;
	}
	if((mode & SCM_PORT_TXD_SCI_ON) == SCM_PORT_TXD_SCI_ON) {
		configuration.Mode = configuration.Mode | PORT_TXD_SCI_ON;
	}
	if((mode & SCM_PORT_RXD_SCI_ON) == SCM_PORT_RXD_SCI_ON) {
		configuration.Mode = configuration.Mode | PORT_RXD_SCI_ON;
	}
	if((mode & SCM_PORT_RTS_SCI_ON) == SCM_PORT_RTS_SCI_ON) {
		configuration.Mode = configuration.Mode | PORT_RTS_SCI_ON;
	}
	if((mode & SCM_PORT_CTS_SCI_ON) == SCM_PORT_CTS_SCI_ON) {
		configuration.Mode = configuration.Mode | PORT_CTS_SCI_ON;
	}
	if((mode & SCM_PORT_GPIO_0_SCI_ON) == SCM_PORT_GPIO_0_SCI_ON) {
		configuration.Mode = configuration.Mode | PORT_GPIO_0_SCI_ON;
	}
	if((mode & SCM_PORT_GPIO_1_SCI_ON) == SCM_PORT_GPIO_1_SCI_ON) {
		configuration.Mode = configuration.Mode | PORT_GPIO_1_SCI_ON;
	}
	if((mode & SCM_PORT_GPIO_2_SCI_ON) == SCM_PORT_GPIO_2_SCI_ON) {
		configuration.Mode = configuration.Mode | PORT_GPIO_2_SCI_ON;
	}
	if((mode & SCM_PORT_SUSPEND_SCI_ON) == SCM_PORT_SUSPEND_SCI_ON) {
		configuration.Mode = configuration.Mode | PORT_SUSPEND_SCI_ON;
	}

	if((mode & SCM_PORT_RI_ECI_ON) == SCM_PORT_RI_ECI_ON) {
		configuration.Mode = configuration.Mode | PORT_RI_ECI_ON;
	}
	if((mode & SCM_PORT_DCD_ECI_ON) == SCM_PORT_DCD_ECI_ON) {
		configuration.Mode = configuration.Mode | PORT_DCD_ECI_ON;
	}
	if((mode & SCM_PORT_DTR_ECI_ON) == SCM_PORT_DTR_ECI_ON) {
		configuration.Mode = configuration.Mode | PORT_DTR_ECI_ON;
	}
	if((mode & SCM_PORT_DSR_ECI_ON) == SCM_PORT_DSR_ECI_ON) {
		configuration.Mode = configuration.Mode | PORT_DSR_ECI_ON;
	}
	if((mode & SCM_PORT_TXD_ECI_ON) == SCM_PORT_TXD_ECI_ON) {
		configuration.Mode = configuration.Mode | PORT_TXD_ECI_ON;
	}
	if((mode & SCM_PORT_RXD_ECI_ON) == SCM_PORT_RXD_ECI_ON) {
		configuration.Mode = configuration.Mode | PORT_RXD_ECI_ON;
	}
	if((mode & SCM_PORT_RTS_ECI_ON) == SCM_PORT_RTS_ECI_ON) {
		configuration.Mode = configuration.Mode | PORT_RTS_ECI_ON;
	}
	if((mode & SCM_PORT_CTS_ECI_ON) == SCM_PORT_CTS_ECI_ON) {
		configuration.Mode = configuration.Mode | PORT_CTS_ECI_ON;
	}
	if((mode & SCM_PORT_GPIO_0_ECI_ON) == SCM_PORT_GPIO_0_ECI_ON) {
		configuration.Mode = configuration.Mode | PORT_GPIO_0_ECI_ON;
	}
	if((mode & SCM_PORT_GPIO_1_ECI_ON) == SCM_PORT_GPIO_1_ECI_ON) {
		configuration.Mode = configuration.Mode | PORT_GPIO_1_ECI_ON;
	}
	if((mode & SCM_PORT_SUSPEND_ECI_ON) == SCM_PORT_SUSPEND_ECI_ON) {
		configuration.Mode = configuration.Mode | PORT_SUSPEND_ECI_ON;
	}

	/* reset latch logic high or low */
	if((resetLatch & SCM_PORT_RI_SCI_ON) == SCM_PORT_RI_SCI_ON) {
		configuration.Reset_Latch = configuration.Reset_Latch | PORT_RI_SCI_ON;
	}
	if((resetLatch & SCM_PORT_DCD_SCI_ON) == SCM_PORT_DCD_SCI_ON) {
		configuration.Reset_Latch = configuration.Reset_Latch | PORT_DCD_SCI_ON;
	}
	if((resetLatch & SCM_PORT_DTR_SCI_ON) == SCM_PORT_DTR_SCI_ON) {
		configuration.Reset_Latch = configuration.Reset_Latch | PORT_DTR_SCI_ON;
	}
	if((resetLatch & SCM_PORT_DSR_SCI_ON) == SCM_PORT_DSR_SCI_ON) {
		configuration.Reset_Latch = configuration.Reset_Latch | PORT_DSR_SCI_ON;
	}
	if((resetLatch & SCM_PORT_TXD_SCI_ON) == SCM_PORT_TXD_SCI_ON) {
		configuration.Reset_Latch = configuration.Reset_Latch | PORT_TXD_SCI_ON;
	}
	if((resetLatch & SCM_PORT_RXD_SCI_ON) == SCM_PORT_RXD_SCI_ON) {
		configuration.Reset_Latch = configuration.Reset_Latch | PORT_RXD_SCI_ON;
	}
	if((resetLatch & SCM_PORT_RTS_SCI_ON) == SCM_PORT_RTS_SCI_ON) {
		configuration.Reset_Latch = configuration.Reset_Latch | PORT_RTS_SCI_ON;
	}
	if((resetLatch & SCM_PORT_CTS_SCI_ON) == SCM_PORT_CTS_SCI_ON) {
		configuration.Reset_Latch = configuration.Reset_Latch | PORT_CTS_SCI_ON;
	}
	if((resetLatch & SCM_PORT_GPIO_0_SCI_ON) == SCM_PORT_GPIO_0_SCI_ON) {
		configuration.Reset_Latch = configuration.Reset_Latch | PORT_GPIO_0_SCI_ON;
	}
	if((resetLatch & SCM_PORT_GPIO_1_SCI_ON) == SCM_PORT_GPIO_1_SCI_ON) {
		configuration.Reset_Latch = configuration.Reset_Latch | PORT_GPIO_1_SCI_ON;
	}
	if((resetLatch & SCM_PORT_GPIO_2_SCI_ON) == SCM_PORT_GPIO_2_SCI_ON) {
		configuration.Reset_Latch = configuration.Reset_Latch | PORT_GPIO_2_SCI_ON;
	}
	if((resetLatch & SCM_PORT_SUSPEND_SCI_ON) == SCM_PORT_SUSPEND_SCI_ON) {
		configuration.Reset_Latch = configuration.Reset_Latch | PORT_SUSPEND_SCI_ON;
	}

	if((resetLatch & SCM_PORT_RI_ECI_ON) == SCM_PORT_RI_ECI_ON) {
		configuration.Reset_Latch = configuration.Reset_Latch | PORT_RI_ECI_ON;
	}
	if((resetLatch & SCM_PORT_DCD_ECI_ON) == SCM_PORT_DCD_ECI_ON) {
		configuration.Reset_Latch = configuration.Reset_Latch | PORT_DCD_ECI_ON;
	}
	if((resetLatch & SCM_PORT_DTR_ECI_ON) == SCM_PORT_DTR_ECI_ON) {
		configuration.Reset_Latch = configuration.Reset_Latch | PORT_DTR_ECI_ON;
	}
	if((resetLatch & SCM_PORT_DSR_ECI_ON) == SCM_PORT_DSR_ECI_ON) {
		configuration.Reset_Latch = configuration.Reset_Latch | PORT_DSR_ECI_ON;
	}
	if((resetLatch & SCM_PORT_TXD_ECI_ON) == SCM_PORT_TXD_ECI_ON) {
		configuration.Reset_Latch = configuration.Reset_Latch | PORT_TXD_ECI_ON;
	}
	if((resetLatch & SCM_PORT_RXD_ECI_ON) == SCM_PORT_RXD_ECI_ON) {
		configuration.Reset_Latch = configuration.Reset_Latch | PORT_RXD_ECI_ON;
	}
	if((resetLatch & SCM_PORT_RTS_ECI_ON) == SCM_PORT_RTS_ECI_ON) {
		configuration.Reset_Latch = configuration.Reset_Latch | PORT_RTS_ECI_ON;
	}
	if((resetLatch & SCM_PORT_CTS_ECI_ON) == SCM_PORT_CTS_ECI_ON) {
		configuration.Reset_Latch = configuration.Reset_Latch | PORT_CTS_ECI_ON;
	}
	if((resetLatch & SCM_PORT_GPIO_0_ECI_ON) == SCM_PORT_GPIO_0_ECI_ON) {
		configuration.Reset_Latch = configuration.Reset_Latch | PORT_GPIO_0_ECI_ON;
	}
	if((resetLatch & SCM_PORT_GPIO_1_ECI_ON) == SCM_PORT_GPIO_1_ECI_ON) {
		configuration.Reset_Latch = configuration.Reset_Latch | PORT_GPIO_1_ECI_ON;
	}
	if((resetLatch & SCM_PORT_SUSPEND_ECI_ON) == SCM_PORT_SUSPEND_ECI_ON) {
		configuration.Reset_Latch = configuration.Reset_Latch | PORT_SUSPEND_ECI_ON;
	}

	/* suspend latch logic high or low */
	if((suspendLatch & SCM_PORT_RI_SCI_ON) == SCM_PORT_RI_SCI_ON) {
		configuration.Suspend_Latch = configuration.Suspend_Latch | PORT_RI_SCI_ON;
	}
	if((suspendLatch & SCM_PORT_DCD_SCI_ON) == SCM_PORT_DCD_SCI_ON) {
		configuration.Suspend_Latch = configuration.Suspend_Latch | PORT_DCD_SCI_ON;
	}
	if((suspendLatch & SCM_PORT_DTR_SCI_ON) == SCM_PORT_DTR_SCI_ON) {
		configuration.Suspend_Latch = configuration.Suspend_Latch | PORT_DTR_SCI_ON;
	}
	if((suspendLatch & SCM_PORT_DSR_SCI_ON) == SCM_PORT_DSR_SCI_ON) {
		configuration.Suspend_Latch = configuration.Suspend_Latch | PORT_DSR_SCI_ON;
	}
	if((suspendLatch & SCM_PORT_TXD_SCI_ON) == SCM_PORT_TXD_SCI_ON) {
		configuration.Suspend_Latch = configuration.Suspend_Latch | PORT_TXD_SCI_ON;
	}
	if((suspendLatch & SCM_PORT_RXD_SCI_ON) == SCM_PORT_RXD_SCI_ON) {
		configuration.Suspend_Latch = configuration.Suspend_Latch | PORT_RXD_SCI_ON;
	}
	if((suspendLatch & SCM_PORT_RTS_SCI_ON) == SCM_PORT_RTS_SCI_ON) {
		configuration.Suspend_Latch = configuration.Suspend_Latch | PORT_RTS_SCI_ON;
	}
	if((suspendLatch & SCM_PORT_CTS_SCI_ON) == SCM_PORT_CTS_SCI_ON) {
		configuration.Suspend_Latch = configuration.Suspend_Latch | PORT_CTS_SCI_ON;
	}
	if((suspendLatch & SCM_PORT_GPIO_0_SCI_ON) == SCM_PORT_GPIO_0_SCI_ON) {
		configuration.Suspend_Latch = configuration.Suspend_Latch | PORT_GPIO_0_SCI_ON;
	}
	if((suspendLatch & SCM_PORT_GPIO_1_SCI_ON) == SCM_PORT_GPIO_1_SCI_ON) {
		configuration.Suspend_Latch = configuration.Suspend_Latch | PORT_GPIO_1_SCI_ON;
	}
	if((suspendLatch & SCM_PORT_GPIO_2_SCI_ON) == SCM_PORT_GPIO_2_SCI_ON) {
		configuration.Suspend_Latch = configuration.Suspend_Latch | PORT_GPIO_2_SCI_ON;
	}
	if((suspendLatch & SCM_PORT_SUSPEND_SCI_ON) == SCM_PORT_SUSPEND_SCI_ON) {
		configuration.Suspend_Latch = configuration.Suspend_Latch | PORT_SUSPEND_SCI_ON;
	}

	if((suspendLatch & SCM_PORT_RI_ECI_ON) == SCM_PORT_RI_ECI_ON) {
		configuration.Suspend_Latch = configuration.Suspend_Latch | PORT_RI_ECI_ON;
	}
	if((suspendLatch & SCM_PORT_DCD_ECI_ON) == SCM_PORT_DCD_ECI_ON) {
		configuration.Suspend_Latch = configuration.Suspend_Latch | PORT_DCD_ECI_ON;
	}
	if((suspendLatch & SCM_PORT_DTR_ECI_ON) == SCM_PORT_DTR_ECI_ON) {
		configuration.Suspend_Latch = configuration.Suspend_Latch | PORT_DTR_ECI_ON;
	}
	if((suspendLatch & SCM_PORT_DSR_ECI_ON) == SCM_PORT_DSR_ECI_ON) {
		configuration.Suspend_Latch = configuration.Suspend_Latch | PORT_DSR_ECI_ON;
	}
	if((suspendLatch & SCM_PORT_TXD_ECI_ON) == SCM_PORT_TXD_ECI_ON) {
		configuration.Suspend_Latch = configuration.Suspend_Latch | PORT_TXD_ECI_ON;
	}
	if((suspendLatch & SCM_PORT_RXD_ECI_ON) == SCM_PORT_RXD_ECI_ON) {
		configuration.Suspend_Latch = configuration.Suspend_Latch | PORT_RXD_ECI_ON;
	}
	if((suspendLatch & SCM_PORT_RTS_ECI_ON) == SCM_PORT_RTS_ECI_ON) {
		configuration.Suspend_Latch = configuration.Suspend_Latch | PORT_RTS_ECI_ON;
	}
	if((suspendLatch & SCM_PORT_CTS_ECI_ON) == SCM_PORT_CTS_ECI_ON) {
		configuration.Suspend_Latch = configuration.Suspend_Latch | PORT_CTS_ECI_ON;
	}
	if((suspendLatch & SCM_PORT_GPIO_0_ECI_ON) == SCM_PORT_GPIO_0_ECI_ON) {
		configuration.Suspend_Latch = configuration.Suspend_Latch | PORT_GPIO_0_ECI_ON;
	}
	if((suspendLatch & SCM_PORT_GPIO_1_ECI_ON) == SCM_PORT_GPIO_1_ECI_ON) {
		configuration.Suspend_Latch = configuration.Suspend_Latch | PORT_GPIO_1_ECI_ON;
	}
	if((suspendLatch & SCM_PORT_SUSPEND_ECI_ON) == SCM_PORT_SUSPEND_ECI_ON) {
		configuration.Suspend_Latch = configuration.Suspend_Latch | PORT_SUSPEND_ECI_ON;
	}

	/* enhanced functions ECI */
	if((enhancedFxnECI & SCM_EF_GPIO_0_TXLED_ECI) == SCM_EF_GPIO_0_TXLED_ECI) {
		configuration.EnhancedFxn_ECI = configuration.EnhancedFxn_ECI | EF_GPIO_0_TXLED_ECI;
	}
	if((enhancedFxnECI & SCM_EF_GPIO_1_RXLED_ECI) == SCM_EF_GPIO_1_RXLED_ECI) {
		configuration.EnhancedFxn_ECI = configuration.EnhancedFxn_ECI | EF_GPIO_1_RXLED_ECI;
	}
	if((enhancedFxnECI & SCM_EF_GPIO_1_RS485_ECI) == SCM_EF_GPIO_1_RS485_ECI) {
		configuration.EnhancedFxn_ECI = configuration.EnhancedFxn_ECI | EF_GPIO_1_RS485_ECI;
	}
	if((enhancedFxnECI & SCM_EF_RS485_INVERT) == SCM_EF_RS485_INVERT) {
		configuration.EnhancedFxn_ECI = configuration.EnhancedFxn_ECI | EF_RS485_INVERT;
	}
	if((enhancedFxnECI & SCM_EF_INVERT_SUSPEND_ECI) == SCM_EF_INVERT_SUSPEND_ECI) {
		configuration.EnhancedFxn_ECI = configuration.EnhancedFxn_ECI | EF_INVERT_SUSPEND_ECI;
	}
	if((enhancedFxnECI & SCM_EF_DYNAMIC_SUSPEND_ECI) == SCM_EF_DYNAMIC_SUSPEND_ECI) {
		configuration.EnhancedFxn_ECI = configuration.EnhancedFxn_ECI | EF_DYNAMIC_SUSPEND_ECI;
	}

	/* enhanced functions SCI */
	if((enhancedFxnSCI & SCM_EF_GPIO_0_TXLED_SCI) == SCM_EF_GPIO_0_TXLED_SCI) {
		configuration.EnhancedFxn_SCI = configuration.EnhancedFxn_SCI | EF_GPIO_0_TXLED_SCI;
	}
	if((enhancedFxnSCI & SCM_EF_GPIO_1_RXLED_SCI) == SCM_EF_GPIO_1_RXLED_SCI) {
		configuration.EnhancedFxn_SCI = configuration.EnhancedFxn_SCI | EF_GPIO_1_RXLED_SCI;
	}
	if((enhancedFxnSCI & SCM_EF_INVERT_SUSPEND_SCI) == SCM_EF_INVERT_SUSPEND_SCI) {
		configuration.EnhancedFxn_SCI = configuration.EnhancedFxn_SCI | EF_INVERT_SUSPEND_SCI;
	}
	if((enhancedFxnSCI & SCM_EF_DYNAMIC_SUSPEND_SCI) == SCM_EF_DYNAMIC_SUSPEND_SCI) {
		configuration.EnhancedFxn_SCI = configuration.EnhancedFxn_SCI | EF_DYNAMIC_SUSPEND_SCI;
	}

	/* enhanced functions device */
	if((enhancedFxnDevice & SCM_EF_WEAKPULLUP) == SCM_EF_WEAKPULLUP) {
		configuration.EnhancedFxn_Device = configuration.EnhancedFxn_Device | EF_WEAKPULLUP;
	}

	ret = CP210x_SetDualPortConfig((HANDLE)handle, &configuration);
	if(ret != CP210x_SUCCESS) {
		throw_serialcom_exception(env, 2, ret, NULL);
		return -1;
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge
 * Method:    setQuadPortConfig
 * Signature: (J[I[I[B)I
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge_setQuadPortConfig
(JNIEnv *env, jobject obj, jlong handle, jintArray resetLatch, jintArray suspendLatch, jbyteArray config) {
	return -1;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge
 * Method:    setLockValue
 * Signature: (J)I
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any CP210x function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge_setLockValue
(JNIEnv *env, jobject obj, jlong handle) {

	CP210x_STATUS ret = 0;

	ret = CP210x_SetLockValue((HANDLE)handle);
	if(ret != CP210x_SUCCESS) {
		throw_serialcom_exception(env, 2, ret, NULL);
		return -1;
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge
 * Method:    getDeviceVid
 * Signature: (J)I
 *
 * @return usb vendor ID on success otherwise -1 if an error occurs.
 * @throws SerialComException if any CP210x function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge_getDeviceVid
(JNIEnv *env, jobject obj, jlong handle) {

	CP210x_STATUS ret = 0;
	WORD vid = 0;

	ret = CP210x_GetDeviceVid((HANDLE)handle, &vid);
	if(ret != CP210x_SUCCESS) {
		throw_serialcom_exception(env, 2, ret, NULL);
		return -1;
	}

	return (jint)vid;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge
 * Method:    getDevicePid
 * Signature: (J)I
 *
 * @return usb product ID on success otherwise -1 if an error occurs.
 * @throws SerialComException if any CP210x function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge_getDevicePid
(JNIEnv *env, jobject obj, jlong handle) {

	CP210x_STATUS ret = 0;
	WORD pid = 0;

	ret = CP210x_GetDevicePid((HANDLE)handle, &pid);
	if(ret != CP210x_SUCCESS) {
		throw_serialcom_exception(env, 2, ret, NULL);
		return -1;
	}

	return (jint)pid;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge
 * Method:    getDeviceProductString
 * Signature: (J)Ljava/lang/String;
 *
 * @return usb product string on success otherwise NULL if an error occurs.
 * @throws SerialComException if any CP210x function, JNI function, system call or C function fails.
 */
JNIEXPORT jstring JNICALL Java_com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge_getDeviceProductString
(JNIEnv *env, jobject obj, jlong handle) {

	CP210x_STATUS ret = 0;
	BYTE length;
	CP210x_PRODUCT_STRING product;
	jstring info = NULL;

	memset(product, '\0', sizeof(product));
	ret = CP210x_GetDeviceProductString((HANDLE)handle, &product, &length, TRUE);
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
 * Class:     com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge
 * Method:    getDeviceInterfaceString
 * Signature: (JB)Ljava/lang/String;
 *
 * @return usb interface string on success otherwise NULL if an error occurs.
 * @throws SerialComException if any CP210x function, JNI function, system call or C function fails.
 */
JNIEXPORT jstring JNICALL Java_com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge_getDeviceInterfaceString
(JNIEnv *env, jobject obj, jlong handle, jbyte bInterfaceNumber) {

	CP210x_STATUS ret = 0;
	BYTE length;
	char buffer[CP210x_MAX_SERIAL_STRLEN];
	jstring info = NULL;

	memset(buffer, '\0', sizeof(buffer));
	ret = CP210x_GetDeviceInterfaceString((HANDLE)handle, (BYTE)bInterfaceNumber, buffer, &length, TRUE);
	if(ret != CP210x_SUCCESS) {
		throw_serialcom_exception(env, 2, ret, NULL);
		return NULL;
	}

	info = (*env)->NewStringUTF(env, buffer);
	if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
		return NULL;
	}

	return info;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge
 * Method:    getDeviceSerialNumber
 * Signature: (J)Ljava/lang/String;
 *
 * @return usb device serial number string on success otherwise NULL if an error occurs.
 * @throws SerialComException if any CP210x function, JNI function, system call or C function fails.
 */
JNIEXPORT jstring JNICALL Java_com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge_getDeviceSerialNumber
(JNIEnv *env, jobject obj, jlong handle) {

	CP210x_STATUS ret = 0;
	BYTE length;
	CP210x_SERIAL_STRING serial;
	jstring info = NULL;

	memset(serial, '\0', sizeof(serial));
	ret = CP210x_GetDeviceSerialNumber((HANDLE)handle, &serial, &length, TRUE);
	if(ret != CP210x_SUCCESS) {
		throw_serialcom_exception(env, 2, ret, NULL);
		return NULL;
	}

	info = (*env)->NewStringUTF(env, serial);
	if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
		return NULL;
	}

	return info;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge
 * Method:    getSelfPower
 * Signature: (J)I
 *
 * @return 1 if self powered bit is set, 2 if self powered bit is cleared otherwise -1 if an error occurs.
 * @throws SerialComException if any CP210x function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge_getSelfPower
(JNIEnv *env, jobject obj, jlong handle) {

	CP210x_STATUS ret = 0;
	BOOL self_power = 0;

	ret = CP210x_GetSelfPower((HANDLE)handle, &self_power);
	if(ret != CP210x_SUCCESS) {
		throw_serialcom_exception(env, 2, ret, NULL);
		return -1;
	}

	if(self_power == TRUE) {
		/* self powered bit is set */
		return 1;
	}
	return 2;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge
 * Method:    getMaxPower
 * Signature: (J)I
 *
 * @return max power consumption value otherwise -1 if an error occurs.
 * @throws SerialComException if any CP210x function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge_getMaxPower
(JNIEnv *env, jobject obj, jlong handle) {

	CP210x_STATUS ret = 0;
	BYTE max_power = 0;

	ret = CP210x_GetMaxPower((HANDLE)handle, &max_power);
	if(ret != CP210x_SUCCESS) {
		throw_serialcom_exception(env, 2, ret, NULL);
		return -1;
	}

	return (jint)max_power;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge
 * Method:    getFlushBufferConfig
 * Signature: (J)I
 *
 * @return flush buffer configuration bit mask value otherwise -1 if an error occurs.
 * @throws SerialComException if any CP210x function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge_getFlushBufferConfig
(JNIEnv *env, jobject obj, jlong handle) {

	CP210x_STATUS ret = 0;
	WORD flush_config;
	jint config = 0;

	ret = CP210x_GetFlushBufferConfig((HANDLE)handle, &flush_config);
	if(ret != CP210x_SUCCESS) {
		throw_serialcom_exception(env, 2, ret, NULL);
		return -1;
	}

	if((flush_config & FC_OPEN_TX) == FC_OPEN_TX) {
		config = config | SCM_FC_OPEN_TX;
	}
	if((flush_config & FC_OPEN_RX) == FC_OPEN_RX) {
		config = config | SCM_FC_OPEN_RX;
	}
	if((flush_config & FC_CLOSE_TX) == FC_CLOSE_TX) {
		config = config | SCM_FC_CLOSE_TX;
	}
	if((flush_config & FC_CLOSE_RX) == FC_CLOSE_RX) {
		config = config | SCM_FC_CLOSE_RX;
	}

	return config;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge
 * Method:    getDeviceMode
 * Signature: (J)[B
 *
 * @return array of bytes on success otherwise -1 if an error occurs.
 * @throws SerialComException if any CP210x function, JNI function, system call or C function fails.
 */
JNIEXPORT jbyteArray JNICALL Java_com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge_getDeviceMode
(JNIEnv *env, jobject obj, jlong handle) {

	BYTE deviceModeECI;
	BYTE deviceModeSCI;
	CP210x_STATUS ret = 0;
	jbyte data[2];
	jbyteArray info;

	ret = CP210x_GetDeviceMode((HANDLE)handle, &deviceModeECI, &deviceModeSCI);
	if(ret != CP210x_SUCCESS) {
		throw_serialcom_exception(env, 2, ret, NULL);
		return NULL;
	}

	info = (*env)->NewByteArray(env, 2);
	if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_NEWBYTEARRAYSTR);
		return NULL;
	}

	(*env)->SetByteArrayRegion(env, info, 0, 2, data);
	if((*env)->ExceptionOccurred(env)) {
		throw_serialcom_exception(env, 3, 0, E_SETBYTEARRREGIONSTR);
		return NULL;
	}
	return info;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge
 * Method:    getDeviceVersion
 * Signature: (J)I
 *
 * @return version on success otherwise -1 if an error occurs.
 * @throws SerialComException if any CP210x function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge_getDeviceVersion
(JNIEnv *env, jobject obj, jlong handle) {

	WORD version;
	CP210x_STATUS ret = 0;

	ret = CP210x_GetDeviceVersion((HANDLE)handle, &version);
	if(ret != CP210x_SUCCESS) {
		throw_serialcom_exception(env, 2, ret, NULL);
		return -1;
	}

	return (jint)version;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge
 * Method:    getBaudRateConfig
 * Signature: (J)[I
 *
 * Return sequence is baudGen, timer0Reload, prescalar and baudrate respectively.
 *
 * @return array of baud rate configurations on success otherwise NULL if an error occurs.
 * @throws SerialComException if any CP210x function, JNI function, system call or C function fails.
 */
JNIEXPORT jintArray JNICALL Java_com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge_getBaudRateConfig
(JNIEnv *env, jobject obj, jlong handle) {

	int x = 0;
	int y = 0;
	CP210x_STATUS ret = 0;
	BAUD_CONFIG_DATA baud_config;
	jint config[4 * NUM_BAUD_CONFIGS];
	jintArray info = NULL;

	ret = CP210x_GetBaudRateConfig((HANDLE)handle, baud_config);
	if(ret != CP210x_SUCCESS) {
		throw_serialcom_exception(env, 2, ret, NULL);
		return NULL;
	}

	for(x = 0; x < NUM_BAUD_CONFIGS; x++) {
		config[y] = (jint) baud_config[x].BaudGen;
		config[y + 1] = (jint) baud_config[x].Timer0Reload;
		config[y + 2] = (jint) baud_config[x].Prescaler;
		config[y + 3] = (jint) baud_config[x].BaudRate;
		y = y + 4;
	}

	info = (*env)->NewIntArray(env, (4 * NUM_BAUD_CONFIGS));
	if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_NEWINTARRAYSTR);
		return NULL;
	}

	(*env)->SetIntArrayRegion(env, info, 0, (4 * NUM_BAUD_CONFIGS), config);
	if((*env)->ExceptionOccurred(env) != NULL) {
		throw_serialcom_exception(env, 3, 0, E_SETINTARRREGIONSTR);
		return NULL;
	}

	return info;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge
 * Method:    getPortConfig
 * Signature: (J)[I
 *
 * Return sequence is mode, resetLatch, suspendLatch, enhancedFxn respectively.
 *
 * @return port configuration on success otherwise NULL if an error occurs.
 * @throws SerialComException if any CP210x function, JNI function, system call or C function fails.
 */
JNIEXPORT jintArray JNICALL Java_com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge_getPortConfig
(JNIEnv *env, jobject obj, jlong handle) {

	CP210x_STATUS ret = 0;
	PORT_CONFIG port_config;
	jint config[4];
	jintArray info = NULL;

	ret = CP210x_GetPortConfig((HANDLE)handle, &port_config);
	if(ret != CP210x_SUCCESS) {
		throw_serialcom_exception(env, 2, ret, NULL);
		return NULL;
	}

	config[0] = (jint) port_config.Mode;
	config[1] = (jint) port_config.Reset_Latch;
	config[2] = (jint) port_config.Suspend_Latch;
	config[3] = (jint) port_config.EnhancedFxn;

	info = (*env)->NewIntArray(env, 4);
	if((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_NEWINTARRAYSTR);
		return NULL;
	}

	(*env)->SetIntArrayRegion(env, info, 0, 4, config);
	if((*env)->ExceptionOccurred(env) != NULL) {
		throw_serialcom_exception(env, 3, 0, E_SETINTARRREGIONSTR);
		return NULL;
	}

	return info;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge
 * Method:    getLockValue
 * Signature: (J)I
 *
 * @return lock value on success otherwise -1 if an error occurs.
 * @throws SerialComException if any CP210x function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge_getLockValue
(JNIEnv *env, jobject obj, jlong handle) {

	BYTE lock_value;
	CP210x_STATUS ret = 0;

	ret = CP210x_GetLockValue((HANDLE)handle, &lock_value);
	if(ret != CP210x_SUCCESS) {
		throw_serialcom_exception(env, 2, ret, NULL);
		return -1;
	}

	return (jint)lock_value;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge
 * Method:    reset
 * Signature: (J)I
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any CP210x function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge_reset
(JNIEnv *env, jobject obj, jlong handle) {

	CP210x_STATUS ret = 0;

	ret = CP210x_Reset((HANDLE)handle);
	if(ret != CP210x_SUCCESS) {
		throw_serialcom_exception(env, 2, ret, NULL);
		return -1;
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge
 * Method:    createHexFile
 * Signature: (JLjava/lang/String;)I
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any CP210x function, JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComCP210xManufacturingJNIBridge_createHexFile
(JNIEnv *env, jobject obj, jlong handle, jstring fileName) {

	CP210x_STATUS ret = 0;
	const char* file = NULL;

	file = (*env)->GetStringUTFChars(env, fileName, NULL);
	if((file == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_GETSTRUTFCHARSTR);
		return -1;
	}

	ret = CP210x_CreateHexFile((HANDLE)handle, file);
	if(ret != CP210x_SUCCESS) {
		throw_serialcom_exception(env, 2, ret, NULL);
		return -1;
	}

	return 0;
}
