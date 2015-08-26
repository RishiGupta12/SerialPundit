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

/* - This file contains native code to communicate with tty-style port in Unix-like operating systems.
 * - When printing error number, number returned by OS is printed as it is.
 * - There will be only one instance of this shared library at runtime. So if something goes wrong
 *   it will affect everything, until this library has been unloaded and then loaded again.
 * - Wherever possible avoid JNI data types.
 * - Sometimes, the JNI does not like some pointer arithmetic so it is avoided wherever possible. */

#if defined (__linux__) || defined (__APPLE__) || defined (__SunOS) || defined(__sun) || defined(__FreeBSD__) \
		|| defined(__OpenBSD__) || defined(__NetBSD__) || defined(__hpux__) || defined(_AIX)

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

/* C */
#include <stdarg.h>      /* ISO C Standard. Variable arguments  */
#include <stdio.h>       /* ISO C99 Standard: Input/output      */
#include <stdlib.h>      /* Standard ANSI routines              */
#include <string.h>      /* String function definitions         */
#include <errno.h>       /* Error number definitions            */

/* Unix */
#include <unistd.h>      /* UNIX standard function definitions  */
#include <fcntl.h>       /* File control definitions            */
#include <dirent.h>      /* Format of directory entries         */
#include <sys/types.h>   /* Primitive System Data Types         */
#include <sys/stat.h>    /* Defines the structure of the data   */
#include <sys/select.h>
#include <sys/ioctl.h>

/* jni_md.h contains the machine-dependent typedefs for data types. Instruct compiler to include it. */
#include <jni.h>
#include "unix_like_hid.h"

/* Common interface with java layer for supported OS types. */
#include "../../com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge.h"

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    openHidDevice
 * Signature: (Ljava/lang/String;)J
 *
 * @return file descriptor number if function succeeds otherwise -1 if error occurs.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jlong JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_openHidDevice(JNIEnv *env,
		jobject obj, jstring pathName) {
	long fd;
	const char* node = NULL;

	node = (*env)->GetStringUTFChars(env, pathName, NULL);
	if((node == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_GETSTRUTFCHARSTR);
		return -1;
	}

	errno = 0;
	fd = open(node, O_RDWR);
	if(fd < 0) {
		(*env)->ReleaseStringUTFChars(env, pathName, node);
		throw_serialcom_exception(env, 1, errno, NULL);
		return -1;
	}
	(*env)->ReleaseStringUTFChars(env, pathName, node);

	return fd;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    closeHidDevice
 * Signature: (J)I
 *
 * @return 0 if function succeeds otherwise -1 if error occurs.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_closeHidDevice(JNIEnv *env,
		jobject obj, jlong fd) {
	int ret = -1;
	do {
		errno = 0;
		ret = close(fd);
		if(ret < 0) {
			if(errno == EINTR) {
				errno = 0;
				continue;
			}else {
				throw_serialcom_exception(env, 1, errno, NULL);
				return -1;
			}
		}
		break;
	}while (1);

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    getReportDescriptorSize
 * Signature: (J)I
 *
 * @return report descriptor size in bytes if function succeeds otherwise -1 if error occurs.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_getReportDescriptorSize(JNIEnv *env,
		jobject obj, jlong fd) {
	return get_report_descriptor_size(env, fd);
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    writeOutputReport
 * Signature: (JB[B)I
 *
 * @return number of bytes sent to device if function succeeds otherwise -1 if error occurs.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_writeOutputReport(JNIEnv *env,
		jobject obj, jlong fd, jbyte reportID, jbyteArray report) {
	int ret = -1;
	int count = 0;
	jbyte* buffer = NULL;

	count = (int) (*env)->GetArrayLength(env, report);
	buffer = (jbyte *) malloc(count + 1);

	/* The first byte of SFEATURE and GFEATURE is the report number */
	buffer[0] = reportID;

	(*env)->GetByteArrayRegion(env, report, 0, count, &buffer[1]);
	if((*env)->ExceptionOccurred(env) != NULL) {
		throw_serialcom_exception(env, 3, 0, E_GETBYTEARRREGIONSTR);
		return -1;
	}

	errno = 0;
	ret = write(fd, buffer, count+1);
	if(ret < 0) {
		throw_serialcom_exception(env, 1, errno, NULL);
		return -1;
	}

	return ret;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    readInputReport
 * Signature: (J[BI)I
 *
 * @return number of bytes read if function succeeds otherwise -1 if error occurs.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_readInputReport(JNIEnv *env,
		jobject obj, jlong fd, jbyteArray reportBuffer, jint length) {
	int ret = -1;

	jbyte* buffer = (jbyte *) malloc(length);
	if(!buffer) {
		throw_serialcom_exception(env, 3, 0, E_MALLOCSTR);
		return -1;
	}

	do {
		errno = 0;
		ret = read(fd, buffer, length);
		if(ret > 0) {
			/* copy data from native buffer to Java buffer. */
			(*env)->SetByteArrayRegion(env, reportBuffer, 0, ret, buffer);
			free(buffer);
			return ret;
		}else if(ret < 0) {
			free(buffer);
			throw_serialcom_exception(env, 1, errno, NULL);
			return -1;
		}else {
			free(buffer);
			return 0;
		}
	}while (1);

	return -1;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    readInputReportWithTimeout
 * Signature: (J[BII)I
 *
 * @return number of bytes read if function succeeds otherwise -1 if error occurs.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_readInputReportWithTimeout(JNIEnv *env,
		jobject obj, jlong fd, jbyteArray reportBuffer, jint length, jint timeoutVal) {
	int ret = -1;
	fd_set readset;
	struct timespec ts;

	ts.tv_sec  = timeoutVal/1000;
	ts.tv_nsec = 0;

	jbyte* buffer = (jbyte *) malloc(length);
	if(!buffer) {
		throw_serialcom_exception(env, 3, 0, E_MALLOCSTR);
		return -1;
	}

	do {
		FD_ZERO(&readset);
		FD_SET(fd, &readset);
		errno = 0;
		ret = pselect(fd + 1, &readset, 0, 0, &ts, 0);
	} while ((ret < 0) && (errno == EINTR));

	if(ret > 0) {
		/* data can be read with out blocking. */
		do {
			errno = 0;
			ret = read(fd, buffer, length);
			if(ret > 0) {
				/* copy data from native buffer to Java buffer. */
				(*env)->SetByteArrayRegion(env, reportBuffer, 0, ret, buffer);
				free(buffer);
				return ret;
			}else if(ret < 0) {
				free(buffer);
				throw_serialcom_exception(env, 1, errno, NULL);
				return -1;
			}else {
				free(buffer);
				return 0;
			}
		}while (1);
	}else if(ret == 0) {
		/* timeout occurred. */
		free(buffer);
		return 0;
	}else {
		/* error occurred. */
		free(buffer);
		throw_serialcom_exception(env, 1, errno, NULL);
		return -1;
	}

	return -1;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    sendFeatureReport
 * Signature: (JB[B)I
 *
 * @return number of bytes sent to HID device if function succeeds otherwise -1 if error occurs.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_sendFeatureReport(JNIEnv *env,
		jobject obj, jlong fd, jbyte reportID, jbyteArray report) {
	int ret = -1;
	int count = 0;

	count = (int) (*env)->GetArrayLength(env, report);
	jbyte* buffer = (jbyte *) malloc(count + 1);

	/* The first byte of SFEATURE and GFEATURE is the report number */
	buffer[0] = reportID;

	(*env)->GetByteArrayRegion(env, report, 0, count, &buffer[1]);
	if((*env)->ExceptionOccurred(env) != NULL) {
		throw_serialcom_exception(env, 3, 0, E_GETBYTEARRREGIONSTR);
		return -1;
	}

	errno = 0;
	ret = ioctl(fd, HIDIOCSFEATURE(count+1), buffer);
	if(ret < 0) {
		throw_serialcom_exception(env, 1, errno, NULL);
		return -1;
	}

	return ret;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    getFeatureReport
 * Signature: (J[B)I
 *
 * @return number of bytes received from HID device if function succeeds otherwise -1 if error occurs.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_getFeatureReport(JNIEnv *env,
		jobject obj, jlong fd, jbyteArray reportBuffer) {
	int ret = -1;
	int length = 0;

	length = (int) (*env)->GetArrayLength(env, reportBuffer);
	jbyte* buffer = (jbyte *) malloc(length);

	errno = 0;
	ret = ioctl(fd, HIDIOCGFEATURE(length), buffer);
	if(ret > 0) {
		/* copy data from native buffer to Java buffer. */
		(*env)->SetByteArrayRegion(env, reportBuffer, 0, ret, buffer);
		free(buffer);
		return ret;
	}else if(ret < 0) {
		free(buffer);
		throw_serialcom_exception(env, 1, errno, NULL);
		return -1;
	}else {
	}

	/* no bytes read, ret will be zero */
	free(buffer);
	return ret;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    getManufacturerString
 * Signature: (J)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_getManufacturerString
(JNIEnv *, jobject, jlong) {

}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    getIndexedString
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_getIndexedString
(JNIEnv *, jobject, jint) {

}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    getProductString
 * Signature: (J)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_getProductString
(JNIEnv *, jobject, jlong) {

}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    getSerialNumberString
 * Signature: (J)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_getSerialNumberString
(JNIEnv *, jobject, jlong) {

}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    listUSBHIDdevicesWithInfo
 * Signature: (I)[Ljava/lang/String;
 *
 * @return array of Strings containing USB HID devices if found matching given criteria, zero length
 *         array if no node matching given criteria is found, NULL if error occurs.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jobjectArray JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_listUSBHIDdevicesWithInfo(JNIEnv *env,
		jobject obj, jint vendorFilter) {
	return list_usb_hid_devices(env, obj, vendorFilter);
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    openHidDeviceByUSBAttributes
 * Signature: (IILjava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_openHidDeviceByUSBAttributes
(JNIEnv *, jobject, jint, jint, jstring) {

}

#endif
