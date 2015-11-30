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
#include <sys/types.h>   /* Primitive System Data Types         */
#include <sys/select.h>
#include <sys/ioctl.h>

#if defined (__linux__)
#include <stdint.h>
#include <libudev.h>
#include <linux/hidraw.h>
#include <sys/eventfd.h>    /* Linux eventfd for event notification. */
#include <sys/param.h>
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
 * Method:    listHIDdevicesWithInfoR
 * Signature: ()[Ljava/lang/String;
 *
 * @return array of Strings containing HID devices if found, zero length array if no HID device is found,
 *         NULL if an error occurs.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jobjectArray JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_listHIDdevicesWithInfoR(JNIEnv *env,
		jobject obj) {
	return NULL;
	/*TODO*/
}

/* 
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    openHidDeviceByPathR
 * Signature: (Ljava/lang/String;Z)J
 *
 * @return file descriptor number if function succeeds otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jlong JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_openHidDeviceByPathR(JNIEnv *env,
		jobject obj, jstring pathName, jboolean shared) {

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
 * Method:    closeHidDeviceR
 * Signature: (J)I
 *
 * @return 0 if function succeeds otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_closeHidDeviceR(JNIEnv *env,
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
 * Method:    createBlockingHIDIOContextR
 * Signature: ()J
 *
 * This will create event object/file descriptor that will be used to wait upon in addition to
 * HID file descriptor, so as to bring blocked read call out of waiting state. This is needed
 * if application is willing to close the HID device but unable because a blocked reader exist.
 *
 * @return context on success otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jlong JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_createBlockingHIDIOContextR(JNIEnv *env,
		jobject obj) {

#if defined (__linux__)
	int evfd = 0;
	errno = 0;
	evfd  = eventfd(0, 0);
	if(evfd < 0) {
		throw_serialcom_exception(env, 1, errno, NULL);
		return -1;
	}
	return evfd;
#endif

#if defined (__APPLE__)
	int ret = -1;
	jlong *pipeinfo = NULL;
	int pipefdpair[2];
	errno = 0;
	ret = pipe(pipefdpair);
	if(ret < 0) {
		throw_serialcom_exception(env, 1, errno, NULL);
		return -1;
	}
	pipeinfo = (jlong *) calloc(2, sizeof(jlong));
	if(pipeinfo == NULL) {
		throw_serialcom_exception(env, 3, 0, E_CALLOCSTR);
		return -1;
	}
	/* pipe1[0] is reading end, and pipe1[1] is writing end. */
	pipeinfo[0] = pipefdpair[0];
	pipeinfo[1] = pipefdpair[1];
	return pipeinfo;
#endif

	/* should not be reached */
	return -1;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    unblockBlockingHIDIOOperationR
 * Signature: (J)I
 *
 * Causes data event or event as required to emulate an event.
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_unblockBlockingHIDIOOperationR(JNIEnv *env,
		jobject obj, jlong context) {

#if defined (__linux__)
	int ret;
	uint64_t value = 5;
	errno = 0;
	ret = write(((int) context), &value, sizeof(value));
	if(ret <= 0) {
		throw_serialcom_exception(env, 1, errno, NULL);
		return -1;
	}
#endif

#if defined (__APPLE__)
	int ret;
	ret = write(((int) context[1]), "EXIT", strlen("EXIT"));
	if(ret <= 0) {
		throw_serialcom_exception(env, 1, errno, NULL);
		return -1;
	}
#endif

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    destroyBlockingIOContext
 * Signature: (J)I
 *
 * Releases the event object or closes handles as required.
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_destroyBlockingIOContext(JNIEnv *env,
		jobject obj, jlong context) {

#if defined (__linux__)
	close(context);
#endif

#if defined (__APPLE__)
	close((int) context[0]);
	close((int) context[1]);
	free(context);
#endif

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    writeOutputReportR
 * Signature: (JB[BI)I
 *
 * @return number of bytes sent to device if function succeeds otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_writeOutputReportR(JNIEnv *env,
		jobject obj, jlong fd, jbyte reportID, jbyteArray report, jint length) {
#if defined (__linux__)
	return linux_send_output_report(env, fd, reportID, report, length);
#elif defined (__APPLE__)
	return mac_send_output_report(env, fd, reportID, report, length);
#endif
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    readInputReportR
 * Signature: (J[BIJ)I
 *
 * TODO MAC RETURN 1ST BYTE AS REPORT ID OR NOT.
 *
 * It reads a raw HID report (i.e. no report parsing is done).
 *
 * @return number of bytes read if function succeeds otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_readInputReportR(JNIEnv *env,
		jobject obj, jlong fd, jbyteArray reportBuffer, jint length, jlong context) {

	int ret = -1;
	int result = 0;
	fd_set fds;

	jbyte* buffer = (jbyte *) malloc(length);
	if(!buffer) {
		throw_serialcom_exception(env, 3, 0, E_MALLOCSTR);
		return -1;
	}

	/* prepare to block */
	FD_ZERO(&fds);
	FD_SET((int)context, &fds);
	FD_SET(fd, &fds);

	errno = 0;
#if defined (__linux__)
	result = pselect((MAX(fd, (int)context) + 1), &fds, NULL, NULL, NULL, NULL);
#elif defined (__APPLE__)
	result = pselect((MAX(fd, ((int) context[0])) + 1), &fds, NULL, NULL, NULL, NULL);
#endif
	if(result < 0) {
		free(buffer);
		throw_serialcom_exception(env, 1, errno, NULL);
		return -1;
	}

	/* check if we should just come out waiting state and return to caller. if yes, throw
	 * exception with message that will be identified by application to understand that blocked
	 * I/O has been unblocked. */
	if((result > 0) && FD_ISSET((int)context, &fds)) {
		throw_serialcom_exception(env, 3, 0, EXP_UNBLOCKHIDIO);
		return -1;
	}

	if((result > 0) && FD_ISSET(fd, &fds)) {
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
	}

	/* should not be reached */
	return -1;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    readInputReportWithTimeoutR
 * Signature: (J[BII)I
 * 
 * Read input report blocking for the time less than or equal to given timeout value.
 *
 * It reads a raw HID report (i.e. no report parsing is done).
 *
 * @return number of bytes read if function succeeds otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_readInputReportWithTimeoutR(JNIEnv *env,
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
		/* Data read from device. On a device which uses numbered reports, the first
		 * byte of the returned data will be the report number; the report data follows,
		 * beginning in the second byte. For devices which do not use numbered reports,
		 * the report data will begin at the first byte.*/
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
 * Method:    sendFeatureReportR
 * Signature: (JB[BI)I
 *
 * @return number of bytes sent to HID device if function succeeds otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_sendFeatureReportR(JNIEnv *env,
		jobject obj, jlong fd, jbyte reportID, jbyteArray report, jint length) {
#if defined (__linux__)
	return linux_send_feature_report(env, fd, reportID, report, length);
#elif defined (__APPLE__)
	return mac_send_feature_report(env, fd, reportID, report, length);
#endif
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    getFeatureReportR
 * Signature: (JB[BI)I
 *
 * @return number of bytes received from HID device if function succeeds otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_getFeatureReportR(JNIEnv *env,
		jobject obj, jlong fd, jbyte reportID, jbyteArray report, jint length) {
#if defined (__linux__)
	return linux_get_feature_report(env, fd, reportID, report, length);
#elif defined (__APPLE__)
	return mac_get_feature_report(env, fd, reportID, report, length);
#endif
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    getManufacturerStringR
 * Signature: (J)Ljava/lang/String;
 *
 * Get the name of manufacturer and return to caller.
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jstring JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_getManufacturerStringR(JNIEnv *env,
		jobject obj, jlong fd) {
#if defined (__linux__)
	return linux_get_hiddev_info_string(env, fd, 1);
#elif defined (__APPLE__)
	return mac_get_hiddev_info_string(env, fd, 1);
#endif
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    getProductStringR
 * Signature: (J)Ljava/lang/String;
 *
 * Get product information string and return to caller.
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jstring JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_getProductStringR(JNIEnv *env,
		jobject obj, jlong fd) {
#if defined (__linux__)
	return linux_get_hiddev_info_string(env, fd, 2);
#elif defined (__APPLE__)
	return mac_get_hiddev_info_string(env, fd, 2);
#endif
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    getSerialNumberStringR
 * Signature: (J)Ljava/lang/String;
 *
 * Read the serial number string and return to caller.
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jstring JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_getSerialNumberStringR(JNIEnv *env,
		jobject obj, jlong fd) {
#if defined (__linux__)
	return linux_get_hiddev_info_string(env, fd, 3);
#elif defined (__APPLE__)
	return mac_get_hiddev_info_string(env, fd, 3);
#endif
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    getIndexedStringR
 * Signature: (JI)Ljava/lang/String;
 *
 * @return string at the given index or NULL if error occurs.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jstring JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_getIndexedStringR(JNIEnv *env,
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
 * Method:    getReportDescriptorR
 * Signature: (J)[B
 *
 * Try to read report descriptor from the given HID device.
 *
 * @return byte array containing report descriptor values read from given HID device, NULL if
 *         any error occurs.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jbyteArray JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_getReportDescriptorR(JNIEnv *env,
		jobject obj, jlong fd) {
#if defined (__linux__)
	return linux_get_report_descriptor(env, fd);
#elif defined (__APPLE__)
#endif
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    flushInputReportQueueR
 * Signature: (J)I
 *
 * Empty the input report buffer maintained by operating system.
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_flushInputReportQueueR(JNIEnv *env,
		jobject obj, jlong fd) {

#if defined (__linux__)
	int ret = 0;
	struct timespec ts;
	ts.tv_sec  = 2/1000;
	ts.tv_nsec = 0;
	fd_set fds;
	char buffer[1024];

	/* Linux hidraw interface does not provide any API for flushing ring buffer.
	 * So we keep on reading until buffer is empty. The pselect call will timeout
	 * when input report buffer will be empty. */
	while(1) {
		FD_ZERO(&fds);
		FD_SET(fd, &fds);

		errno = 0;
		ret = pselect((fd + 1), &fds, 0, 0, &ts, 0);
		if(ret > 0) {
			/* there is data in buffer, read it and loop back */
			ret = read(fd, buffer, 1000);
			continue;
		}else if(ret == 0) {
			/* buffer is now empty, return success */
			return 0;
		}else {
			/* some error occurred */
			if(errno == EINTR) {
				continue;
			}
			throw_serialcom_exception(env, 1, errno, NULL);
			return -1;
		}
	}

	/* should not be reached */
	return -1;
#endif

#if defined (__APPLE__)
	//TODO
#endif
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    findDriverServingHIDDeviceR
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 *
 * Gets driver who is responsible for communication with given HID device.
 *
 * @return driver name for given HID device on success or NULL if an error occurs.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jstring JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_findDriverServingHIDDevice(JNIEnv *env,
		jobject obj, jstring hidDevNode) {
#if defined (__linux__)
	return linux_find_driver_for_given_hiddevice(env, hidDevNode);
#elif defined (__APPLE__)
	return mac_find_driver_for_given_hiddevice(env, hidDevNode);
#endif
}

#endif /* end compiling for unix-like os */
