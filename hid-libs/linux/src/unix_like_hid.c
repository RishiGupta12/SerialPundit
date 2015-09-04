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

#if defined (__linux__)
#include <libudev.h>
#include <linux/hidraw.h>
#endif

#if defined (__APPLE__)
#include <CoreFoundation/CoreFoundation.h>
#include <IOKit/hid/IOHIDKeys.h>
#include <IOKit/hid/IOHIDManager.h>
#endif

/* jni_md.h contains the machine-dependent typedefs for data types. Instruct compiler to include it. */
#include <jni.h>
#include "unix_like_hid.h"

/* Common interface with java layer for supported OS types. */
#include "../../com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge.h"

#if defined (__APPLE__)
static	IOHIDManagerRef mac_hid_mgr = -1;
#endif

/* Clean up when library is un-loaded. */
__attribute__((destructor)) static void exit_scmhidlib() {
#if defined (__APPLE__)
	IOHIDManagerUnscheduleFromRunLoop(mac_hid_mgr, CFRunLoopGetCurrent( ), kCFRunLoopDefaultMode );
	IOHIDManagerClose(mac_hid_mgr, kIOHIDOptionsTypeNone);
	CFRelease(mac_hid_mgr);
#endif
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    initNativeLib
 * Signature: ()I
 *
 * @return 0 if function succeeds otherwise -1.
 * @throws SerialComException if any function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_initNativeLib
(JNIEnv *env, jobject obj) {
#if defined (__APPLE__)
	IOReturn ret = 0;
	mac_hid_mgr = IOHIDManagerCreate(kCFAllocatorDefault, kIOHIDOptionsTypeNone);
	if(	mac_hid_mgr == -1) {
		// TODO error handling
	}

	/* Associate HID manager with HID devices. */
	IOHIDManagerSetDeviceMatching(mac_hid_mgr, NULL);

	/* associate the HID Manager with the client's run loop. This schedule will propagate to all HID devices
	 * that are currently enumerated and to new HID devices as they are matched by the HID Manager. */
	IOHIDManagerScheduleWithRunLoop(mac_hid_mgr, CFRunLoopGetCurrent(), kCFRunLoopDefaultMode);

	/* This will open both current and future devices that are enumerated. */
	ret = IOHIDManagerOpen(mac_hid_mgr, kIOHIDOptionsTypeNone);
	if(ret != kIOReturnSuccess) {
		// TODO error handling
	}
#endif
	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    listHIDdevicesWithInfo
 * Signature: ()[Ljava/lang/String;
 *
 * @return array of Strings containing HID devices if found, zero length array if no HID device is found,
 *         NULL if an error occurs.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jobjectArray JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_listHIDdevicesWithInfo
(JNIEnv *env, jobject obj) {
	return NULL;
	/*TODO*/
}

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
	const char* deviceNode = NULL;

	deviceNode = (*env)->GetStringUTFChars(env, pathName, NULL);
	if((deviceNode == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_GETSTRUTFCHARSTR);
		return -1;
	}

	errno = 0;
	fd = open(deviceNode, O_RDWR);
	if(fd < 0) {
		(*env)->ReleaseStringUTFChars(env, pathName, deviceNode);
		throw_serialcom_exception(env, 1, errno, NULL);
		return -1;
	}
	(*env)->ReleaseStringUTFChars(env, pathName, deviceNode);

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
 * Signature: (JB[BI)I
 *
 * @return number of bytes sent to device if function succeeds otherwise -1 if error occurs.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_writeOutputReport(JNIEnv *env,
		jobject obj, jlong fd, jbyte reportID, jbyteArray report, jint length) {
#if defined (__linux__)
	return linux_send_output_report(env, fd, reportID, report, length);
#elif defined (__APPLE__)
	return mac_send_output_report(env, fd, reportID, report, length);
#endif
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
			if((*env)->ExceptionOccurred(env) != NULL) {
				throw_serialcom_exception(env, 3, 0, E_SETBYTEARRAYREGION);
				return -1;
			}
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
				if((*env)->ExceptionOccurred(env) != NULL) {
					throw_serialcom_exception(env, 3, 0, E_SETBYTEARRAYREGION);
					return -1;
				}
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
	/* this ioctl returns number of bytes read from device */
	ret = ioctl(fd, HIDIOCGFEATURE(length), buffer);
	if(ret > 0) {
		/* copy data from native buffer to Java buffer. */
		(*env)->SetByteArrayRegion(env, reportBuffer, 0, ret, buffer);
		if((*env)->ExceptionOccurred(env) != NULL) {
			throw_serialcom_exception(env, 3, 0, E_SETBYTEARRAYREGION);
			return -1;
		}
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
JNIEXPORT jstring JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_getManufacturerString(JNIEnv *env,
		jobject obj, jlong fd) {
	return get_hiddev_info_string(env, fd, 1);
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    getProductString
 * Signature: (J)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_getProductString(JNIEnv *env,
		jobject obj, jlong fd) {
	return get_hiddev_info_string(env, fd, 2);
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    getSerialNumberString
 * Signature: (J)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_getSerialNumberString(JNIEnv *env,
		jobject obj, jlong fd) {
	return get_hiddev_info_string(env, fd, 3);
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    getIndexedString
 * Signature: (JI)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_getIndexedString(JNIEnv *env,
		jobject obj, jlong fd, jint index) {
	return get_hiddev_indexed_string(env, fd, index);
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
#if defined (__linux__)
	return linux_enumerate_usb_hid_devices(env, vendorFilter);
#elif defined (__APPLE__)
	return mac_enumerate_usb_hid_devics(env, vendorFilter, mac_hid_mgr);
#endif
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    openHidDeviceByUSBAttributes
 * Signature: (IILjava/lang/String;III)J
 */
JNIEXPORT jlong JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_openHidDeviceByUSBAttributes(JNIEnv *env,
		jobject obj, jint usbvid, jint usbpid, jstring usbserialnumber, jint locationID, jint busnum, jint devnum) {
#if defined (__linux__)
	return linux_usbattrhid_open(env, usbvid, usbpid, usbserialnumber, busnum, devnum);
#elif defined (__APPLE__)
	return mac_usbattrhid_open(env, usbvid, usbpid, usbserialnumber, locationID);
#endif
}

#endif
