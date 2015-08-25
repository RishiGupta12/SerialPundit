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
 * @return file descriptor number if function succeeds otherwise -1.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jlong JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_openHidDevice(JNIEnv *env, jobject obj, jstring pathName) {
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
 * @return 0 if function succeeds otherwise -1.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_closeHidDevice(JNIEnv *env, jobject obj, jlong fd) {
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
 * @return report descriptor size in bytes if function succeeds otherwise -1.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_getReportDescriptorSize(JNIEnv *env, jobject obj, jlong fd) {
	return get_report_descriptor_size(env, fd);
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    writeOutputReport
 * Signature: (JB[B)I
 *
 * @return 0 if function succeeds otherwise -1.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_writeOutputReport(JNIEnv *env, jobject obj, jlong fd, jbyte reportId, jbyteArray report) {
	int ret = -1;
	int index = 0;
	int status = 0;
	int count = 0;
	jbyte* data_buf = NULL;

	data_buf = (*env)->GetByteArrayElements(env, report, JNI_FALSE);
	if((data_buf == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_GETBYTEARRELEMTSTR);
		return -1;
	}
	count = (int) (*env)->GetArrayLength(env, report);

	while(count > 0) {
		errno = 0;
		ret = write(fd, &data_buf[index], count);
		if(ret < 0) {
			if(errno == EINTR) {
				errno = 0;
				continue;
			}else {
				status = (-1 * errno);
				break;
			}
		}else if(ret == 0) {
			errno = 0;
			continue;
		}else {
		}

		count = count - ret;
		index = index + ret;
	}
	(*env)->ReleaseByteArrayElements(env, report, data_buf, 0);
	if(status < 0) {
		throw_serialcom_exception(env, 1, (-1 * status), NULL);
		return -1;
	}
	return status;
}

#endif









