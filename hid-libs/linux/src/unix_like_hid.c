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

/* In Linux, USB HID raw devices are those devices which are not strictly human interface device. */

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
 * @return 0 if initialization succeeds, -2 if SerialComException class can not be found,
 *         -3 if global reference can not be created.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_initNativeLib(JNIEnv *env, jobject obj) {
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
 * Method:    openHidDeviceByPath
 * Signature: (Ljava/lang/String;Z)J
 *
 * @return file descriptor number if function succeeds otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jlong JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_openHidDeviceByPath
(JNIEnv *env, jobject obj, jstring pathName, jboolean shared) {

#if defined (__linux__)
	long fd;
	int OPEN_MODE = O_RDWR;
	const char* deviceNode = NULL;

	deviceNode = (*env)->GetStringUTFChars(env, pathName, NULL);
	if((deviceNode == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_GETSTRUTFCHARSTR);
		return -1;
	}
	
	/* TODO ioctl(fevdev, EVIOCGRAB, 0); */
	if(shared == JNI_TRUE) {
		OPEN_MODE = O_RDWR;
	}

	errno = 0;
	fd = open(deviceNode, OPEN_MODE);
	if(fd < 0) {
		(*env)->ReleaseStringUTFChars(env, pathName, deviceNode);
		throw_serialcom_exception(env, 1, errno, NULL);
		return -1;
	}
	(*env)->ReleaseStringUTFChars(env, pathName, deviceNode);

	return fd;
#endif

#if defined (__APPLE__)
	return -1;
#endif
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    closeHidDevice
 * Signature: (J)I
 *
 * @return 0 if function succeeds otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_closeHidDevice(JNIEnv *env,
		jobject obj, jlong fd) {

#if defined (__linux__)
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
#endif

#if defined (__APPLE__)
	IOReturn ret = -1;
	ret = IOHIDDeviceClose(fd, kIOHIDOptionsTypeSeizeDevice);
	if(ret != kIOReturnSuccess) {
		/*TODO handle error */
	}
#endif
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    writeOutputReport
 * Signature: (JB[BI)I
 *
 * @return number of bytes sent to device if function succeeds otherwise -1 if an error occurs.
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
 * TODO MAC RETURN 1ST BYTE AS REPORT ID OR NOT.
 *
 * It reads a raw HID report (i.e. no report parsing is done).
 *
 * @return number of bytes read if function succeeds otherwise -1 if an error occurs.
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
 * Read input report blocking for the time less than or equal to given timeout value.
 *
 * It reads a raw HID report (i.e. no report parsing is done).
 *
 * @return number of bytes read if function succeeds otherwise -1 if an error occurs.
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
 * Signature: (JB[BI)I
 *
 * @return number of bytes sent to HID device if function succeeds otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_sendFeatureReport(JNIEnv *env,
		jobject obj, jlong fd, jbyte reportID, jbyteArray report, jint length) {
#if defined (__linux__)
	return linux_send_feature_report(env, fd, reportID, report, length);
#elif defined (__APPLE__)
	return mac_send_feature_report(env, fd, reportID, report, length);
#endif
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    getFeatureReport
 * Signature: (JB[BI)I
 *
 * @return number of bytes received from HID device if function succeeds otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_getFeatureReport
(JNIEnv *env, jobject obj, jlong fd, jbyte reportID, jbyteArray report, jint length) {
#if defined (__linux__)
	return linux_get_feature_report(env, fd, reportID, report, length);
#elif defined (__APPLE__)
	return mac_get_feature_report(env, fd, reportID, report, length);
#endif
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    getManufacturerString
 * Signature: (J)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_getManufacturerString(JNIEnv *env,
		jobject obj, jlong fd) {
#if defined (__linux__)
	return linux_get_hiddev_info_string(env, fd, 1);
#elif defined (__APPLE__)
	return mac_get_hiddev_info_string(env, fd, 1);
#endif
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    getProductString
 * Signature: (J)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_getProductString(JNIEnv *env,
		jobject obj, jlong fd) {
#if defined (__linux__)
	return linux_get_hiddev_info_string(env, fd, 2);
#elif defined (__APPLE__)
	return mac_get_hiddev_info_string(env, fd, 2);
#endif
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    getSerialNumberString
 * Signature: (J)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_getSerialNumberString(JNIEnv *env,
		jobject obj, jlong fd) {
#if defined (__linux__)
	return linux_get_hiddev_info_string(env, fd, 3);
#elif defined (__APPLE__)
	return mac_get_hiddev_info_string(env, fd, 3);
#endif
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    getIndexedString
 * Signature: (JI)Ljava/lang/String;
 *
 * @return string at the given index or NULL if error occurs.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jstring JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_getIndexedString(JNIEnv *env,
		jobject obj, jlong fd, jint index) {
#if defined (__linux__)
	return NULL;
#elif defined (__APPLE__)
	return NULL;
#endif
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

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    getReportDescriptor
 * Signature: (J)[B
 *
 * Try to read report descriptor from the given HID device.
 *
 * @return byte array containing report descriptor values read from given HID device, NULL if
 *         any error occurs.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jbyteArray JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_getReportDescriptor
(JNIEnv *env, jobject obj, jlong fd) {
#if defined (__linux__)
	return linux_get_report_descriptor(env, fd);
#elif defined (__APPLE__)
#endif
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    findDriverServingHIDDevice
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 *
 * Gets driver who is responsible for communication with given HID device.
 *
 * @return driver name for given HID device on success or NULL if an error occurs.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jstring JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_findDriverServingHIDDevice
(JNIEnv *env, jobject obj, jstring hidDevNode) {
#if defined (__linux__)
	return linux_find_driver_for_given_hiddevice(env, hidDevNode);
#elif defined (__APPLE__)
	return mac_find_driver_for_given_hiddevice(env, hidDevNode);
#endif
}

#endif /* end compiling*/

