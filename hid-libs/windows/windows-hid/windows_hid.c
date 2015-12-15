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

/* Project is built with unicode character set enabled. The multi threaded (MD) version of run time library is
 * used. Worker threads are created through _beginthreadex() to make sure CRT functions are accessed in thread
 * safe way. */

/* stdafx.h must come as first include when using precompiled headers and Microsoft compiler. */
#include "stdafx.h"

/* C */
#include <stdarg.h>      /* ISO C Standard. Variable arguments  */
#include <stdio.h>       /* ISO C99 Standard: Input/output      */
#include <stdlib.h>      /* Standard ANSI routines              */
#include <string.h>      /* String function definitions         */
#include <errno.h>       /* Error number definitions            */

/* jni_md.h contains the machine-dependent typedefs for data types. Instruct compiler to include it. */
#include <jni.h>
#include <Hidsdi.h>
#include <Hidpi.h>
#include <tchar.h>
#include "windows_hid.h"

/* Common interface with java layer for supported OS types. */
#include "../../com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge.h"

/* Called when this shared library is loaded or un-loaded. This clean up resources on library exit. */
BOOL WINAPI DllMain(HANDLE hModule, DWORD reason_for_call, LPVOID lpReserved) {
	return TRUE;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    initNativeLib
 * Signature: ()I
 *
 * @return 0 always to represent success.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_initNativeLib(JNIEnv *env, jobject obj) {
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
 * Opens given HID device using its node (path). For windows, this function returns pointer to 
 * structure that contains information about this device including opened handle.
 *
 * The pathName is device path of collection. A Top Level Collection is a grouping of functionality 
 * that is targeting a particular software consumer (or type of consumer) of the functionality. Top
 * Level Collections are also referred to as Application Collections. In Windows, the HID device 
 * setup class (HIDClass) generates a unique physical device object (PDO) for each Top Level Collection 
 * described by the Report Descriptor.
 *
 * @return information structure pointer if function succeeds otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, WINAPI or C function fails.
 */
JNIEXPORT jlong JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_openHidDeviceByPathR(JNIEnv *env, 
	jobject obj, jstring pathName, jboolean shared) {

	int x = 0;
	BOOLEAN ret = FALSE;
	NTSTATUS result;
	DWORD shared_mode = 0; /* 0 means exclusive excess */
	const jchar* device_node = NULL;
	wchar_t dev_instance[1024];
	wchar_t dev_full_path[1024];
	struct hid_dev_info* info = NULL;

	/* allocate structure that will hold all the information related to this handle */
	info = (struct hid_dev_info *) calloc(1, sizeof(struct hid_dev_info));
	if (info == NULL) {
		throw_serialcom_exception(env, 3, 0, E_CALLOCSTR);
		return -1;
	}

	/* extract device path to match (as an array of Unicode characters) */
	device_node = (*env)->GetStringChars(env, pathName, JNI_FALSE);
	if ((device_node == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_GETSTRCHARSTR);
		return -1;
	}

	/* save extracted java string locally */
	memset(dev_instance, '\0', 1024);
	swprintf_s(dev_instance, 1024, TEXT("%s"), device_node);

	/* save device instance for later use if required associating this handle and device instance */
	for (x = 0; x < 1024; x++) {
		info->instance[x] = dev_instance[x];
	}

	/* HID\VID_04D8&PID_00DF&MI_02\7&33842c3f&0&0000 (replace \ with #) */
	x = 0;
	while (dev_instance[x] != '\0') {
		if (dev_instance[x] == '\\') {
			dev_instance[x] = '#';
		}
		x++;
	}

	/* \\?\hid#vid_04d8&pid_00df&mi_02#7&33842c3f&0&0000#{4d1e55b2-f16f-11cf-88cb-001111000030} */
	memset(dev_full_path, '\0', 1024);
	swprintf_s(dev_full_path, 1024, TEXT("\\\\?\\%s#{4d1e55b2-f16f-11cf-88cb-001111000030}"), dev_instance);
	
	/* if the device is to be shared with others, set sharing flags */
	if(shared == JNI_TRUE) {
		shared_mode = FILE_SHARE_READ | FILE_SHARE_WRITE;
	}

	/* open the device using the cooked device path (dev_full_path) */
	info->handle = CreateFile(dev_full_path, (GENERIC_READ | GENERIC_WRITE), shared_mode, 
		                      NULL, OPEN_EXISTING, FILE_FLAG_OVERLAPPED, NULL);
	if (info->handle == INVALID_HANDLE_VALUE) {
		free(info);
		(*env)->ReleaseStringChars(env, pathName, device_node);
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}

	/* set the maximum number of input reports that the HID class driver ring buffer can hold for a 
	   specified top-level collection. */
	ret = HidD_SetNumInputBuffers(info->handle, 256);
	if (ret != TRUE) {
		free(info);
		(*env)->ReleaseStringChars(env, pathName, device_node);
		throw_serialcom_exception(env, 3, 0, E_REPORTINBUFSIZE);
		return -1;
	}

	/* save the preparsed data for this device for retriving further information about this device. 
	   windows will allocate memory for parsed data structure. */
	ret = HidD_GetPreparsedData(info->handle, &(info->parsed_data));
	if (ret != TRUE) {
		free(info);
		(*env)->ReleaseStringChars(env, pathName, device_node);
		throw_serialcom_exception(env, 3, 0, E_PARSEDDATA);
		return -1;
	}

	/* get device capabilities so that when performing read/writes correct report length 
	   can be sent to the device. */
	result = HidP_GetCaps(info->parsed_data, &(info->collection_capabilities));
	if (result != HIDP_STATUS_SUCCESS) {
		free(info);
		(*env)->ReleaseStringChars(env, pathName, device_node);
		throw_serialcom_exception(env, 3, 0, E_DEVCAPABILITIES);
		return -1;
	}

	/* flush queue if anyside effect or garbage exist */
	HidD_FlushQueue(info->handle);

	(*env)->ReleaseStringChars(env, pathName, device_node);
	return (jlong) info;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    closeHidDeviceR
 * Signature: (J)I
 *
 * Closes handle to HID device and free up resources.
 *
 * @return 0 if function succeeds otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, WINAPI or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_closeHidDeviceR(JNIEnv *env,
	jobject obj, jlong handle) {

	jint ret = -1;
	DWORD errorVal = 0;
	struct hid_dev_info* info = (struct hid_dev_info*) handle;

	/* Close the port. */
	ret = CloseHandle(info->handle);
	if (ret == 0) {
		errorVal = GetLastError();
		if (errorVal == ERROR_INVALID_HANDLE) {
			/* This is not an error in windows. */
			HidD_FreePreparsedData(info->parsed_data);
			free(info);
		}else {
			throw_serialcom_exception(env, 4, errorVal, NULL);
			return -1;
		}
	}

	HidD_FreePreparsedData(info->parsed_data);
	free(info);
	return 0;
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
 * @throws SerialComException if any JNI function, WINAPI or C function fails.
 */
JNIEXPORT jlong JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_createBlockingHIDIOContextR(JNIEnv *env,
		jobject obj) {

	OVERLAPPED *overlapped = NULL;

	overlapped = (OVERLAPPED *) calloc(1, sizeof(OVERLAPPED));
	if (overlapped == NULL) {
		throw_serialcom_exception(env, 3, 0, E_CALLOCSTR);
		return -1;
	}

	/* auto reset, non-signaled, unnamed event object */
	overlapped->hEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
	if (overlapped->hEvent == NULL) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}

	return (jlong) overlapped;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    unblockBlockingHIDIOOperationR
 * Signature: (J)I
 *
 * Causes data event or event as required to emulate an event.
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, WINAPI or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_unblockBlockingHIDIOOperationR(JNIEnv *env,
		jobject obj, jlong context) {

	int ret = 0;
	OVERLAPPED *overlapped = (OVERLAPPED *) context;

	ret = SetEvent(overlapped->hEvent);
	if(ret == 0) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    destroyBlockingIOContextR
 * Signature: (J)I
 *
 * Releases the event object or closes handles as required.
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, WINAPI or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_destroyBlockingIOContextR(JNIEnv *env,
		jobject obj, jlong context) {

	OVERLAPPED *overlapped = (OVERLAPPED *) context;
	CloseHandle((*overlapped).hEvent);
	free(overlapped);
	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    writeOutputReportR
 * Signature: (JB[BI)I
 * 
 * Sends an output report to given HID device after properly formating the given report.
 *
 * For MCP2200 both the OutputReportByteLength and InputReportByteLength are 17 bytes.
 *
 * @return number of bytes sent to device if function succeeds otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_writeOutputReportR(JNIEnv *env,
	jobject obj, jlong handle, jbyte reportID, jbyteArray report, jint length) {

	int ret = 0;
	BOOL result = FALSE;
	DWORD errorVal = 0;
	jbyte* buffer = NULL;
	struct hid_dev_info* info = (struct hid_dev_info*) handle;
	OVERLAPPED ovWrite;
	DWORD num_of_bytes_written = 0;

	/* allocate memory accounting for report ID byte irrespective of whether device uses numbered reports or not */
	buffer = (jbyte *) calloc(info->collection_capabilities.OutputReportByteLength, sizeof(unsigned char));
	if (buffer == NULL) {
		throw_serialcom_exception(env, 3, 0, E_CALLOCSTR);
		return -1;
	}

	/* If the device uses numbered report set very first byte to report number otherwise set it to 0x00 */
	if (reportID < 0) {
		buffer[0] = 0x00;
	}else {
		buffer[0] = reportID;
	}

	/* if user suplied more data than this device can accept, throw error. */
	if (length > (info->collection_capabilities.OutputReportByteLength -1)) {
		free(buffer);
		throw_serialcom_exception(env, 3, 0, E_INVALIDOUTLEN);
		return -1;
	}

	/* copy report data fromm java to native buffer. */
	(*env)->GetByteArrayRegion(env, report, 0, length, &buffer[1]);
	if ((*env)->ExceptionOccurred(env) != NULL) {
		free(buffer);
		throw_serialcom_exception(env, 3, 0, E_GETBYTEARRREGIONSTR);
		return -1;
	}

	/* if the user supplied buffer has less number of bytes than maximum number of bytes 
	   a given output report for this device can have, add padding zeroes to the report 
	   buffer that will be sent to device. */
	if (length < (info->collection_capabilities.OutputReportByteLength - 1)) {
		memset(&buffer[length + 1], 0x00, (info->collection_capabilities.OutputReportByteLength - length -1));
	}

	/* prepare for async write operation. */
	memset(&ovWrite, 0, sizeof(ovWrite));
	ovWrite.hEvent = CreateEvent(NULL, FALSE, FALSE, NULL);
	if (ovWrite.hEvent == NULL) {
		free(buffer);
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}

	/* write report data to device. */
	result = WriteFile(info->handle, (PVOID)buffer, info->collection_capabilities.OutputReportByteLength, &num_of_bytes_written, &ovWrite);
	if (result == FALSE) {
		errorVal = GetLastError();
		if (errorVal == ERROR_IO_PENDING) {
			if (WaitForSingleObject(ovWrite.hEvent, 1000) == WAIT_OBJECT_0) {
				ret = GetOverlappedResult((HANDLE)handle, &ovWrite, &num_of_bytes_written, TRUE);
				if (ret == 0) {
					CancelIo(info->handle);
					CloseHandle(ovWrite.hEvent);
					free(buffer);
					throw_serialcom_exception(env, 4, GetLastError(), NULL);
					return -1;
				}
			}
		}else {
			CancelIo(info->handle);
			CloseHandle(ovWrite.hEvent);
			free(buffer);
			throw_serialcom_exception(env, 4, errorVal, NULL);
			return -1;
		}
	}

	CloseHandle(ovWrite.hEvent);
	free(buffer);

	/* if driver has a bug, sometimes it will not report correct number of bytes sent even if they 
	   have been sent correctly, therefore return info->collection_capabilities.OutputReportByteLength 
	   as we know reaching here means success in writing */
	return (jint)info->collection_capabilities.OutputReportByteLength;
}


/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    readInputReportR
 * Signature: (J[BIJ)I
 *
 * It reads a raw HID report (i.e. no report parsing is done).
 * 
 * @return number of bytes read if function succeeds otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, WINAPI or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_readInputReportR(JNIEnv *env,
		jobject obj, jlong handle, jbyteArray reportBuffer, jint length, jlong context) {

	int ret = 0;
	DWORD errorVal = 0;
	jbyte data_buf[1024];
	OVERLAPPED overlapped;
	DWORD wait_status = 0;
	HANDLE wait_event_handles[2];
	DWORD num_of_bytes_read = 0;
	int have_data = 0;
	struct hid_dev_info* info = (struct hid_dev_info*) handle;

	/* Only hEvent member need to be initialized and others can be left 0. */
	memset(&overlapped, 0, sizeof(overlapped));
	overlapped.hEvent = CreateEvent(NULL, FALSE, FALSE, NULL);
	if (overlapped.hEvent == NULL) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}

	wait_event_handles[0] = ((OVERLAPPED *)context)->hEvent;
	wait_event_handles[1] = overlapped.hEvent;

	/* An application should only use the HidD_GetXxx routines to obtain the current state of a device.
	If an application attempts to use HidD_GetInputReport to continuously obtain input reports, the
	reports can be lost. In addition, some devices might not support HidD_GetInputReport, and will
	become unresponsive if this routine is used. */

	/* ReadFile resets the event to a nonsignaled state when it begins the I/O operation. */
	ret = ReadFile(info->handle, (PVOID)data_buf, info->collection_capabilities.InputReportByteLength, &num_of_bytes_read, &overlapped);

	if (ret == 0) {
		errorVal = GetLastError();

		if (errorVal == ERROR_IO_PENDING) {

			wait_status = WaitForMultipleObjects(2, wait_event_handles, FALSE, INFINITE);

			/* check if read operation is un-blocked through another thread */
			switch (wait_status) {
				case WAIT_OBJECT_0 + 0:
					/* come out of blocking state */
					CancelIo((HANDLE)handle);
					GetOverlappedResult((HANDLE)handle, &overlapped, &num_of_bytes_read, FALSE);
					CloseHandle(overlapped.hEvent);
					throw_serialcom_exception(env, 3, 0, EXP_UNBLOCKHIDIO);
					return -1;
			}

			if (wait_status == (WAIT_OBJECT_0 + 1)) {
				ret = GetOverlappedResult(info->handle, &overlapped, &num_of_bytes_read, TRUE);
				CloseHandle(overlapped.hEvent);

				if (ret > 0) {
					/* return data read from HID device if read from it */
					if (num_of_bytes_read > 0) {
						have_data = 1;
					}
				}else if (ret == 0) {
					errorVal = GetLastError();
					if ((errorVal == ERROR_HANDLE_EOF) || (errorVal == ERROR_IO_INCOMPLETE)) {
						CancelIo(info->handle);
						return 0;
					}else {
						/* This case indicates error. */
						throw_serialcom_exception(env, 4, errorVal, NULL);
						return -1;
					}
				}else {
				}
			}else if (wait_status == WAIT_FAILED) {
				/* This case indicates error. */
				CloseHandle(overlapped.hEvent);
				throw_serialcom_exception(env, 4, GetLastError(), NULL);
				return -1;
			}else if (wait_status == WAIT_TIMEOUT) {
				CloseHandle(overlapped.hEvent);
				return 0;
			}else {
				CloseHandle(overlapped.hEvent);
				return 0;
			}
		}else {
			/* This case indicates error. */
			CloseHandle(overlapped.hEvent);
			throw_serialcom_exception(env, 4, errorVal, NULL);
			return -1;
		}
	}else if (ret > 0) {
		CloseHandle(overlapped.hEvent);
		if (num_of_bytes_read > 0) {
			/* This indicates we got success and have read data in first go itself. */
			have_data = 1;
		}
	}else {
		CloseHandle(overlapped.hEvent);
	}

	/* if report was read from device pass that to java layer application. */
	if (have_data > 0) {
		if (data_buf[0] == (jbyte)0x00) {

			/* if user supplied report buffer wass smaller than required to accomodate read report, throw exception */
			if (length < (jint)(num_of_bytes_read - 1)) {
				throw_serialcom_exception(env, 3, 0, E_INVALIDINLEN);
				return -1;
			}

			/* if the device does not uses numbered reports, strip 1st byte.
			other operating systems like Linux also does so internally. */
			(*env)->SetByteArrayRegion(env, reportBuffer, 0, (num_of_bytes_read - 1), &data_buf[1]);
			if ((*env)->ExceptionOccurred(env) != NULL) {
				throw_serialcom_exception(env, 3, 0, E_SETBYTEARRAYREGION);
				return -1;
			}
			return (num_of_bytes_read - 1);
		}else {

			/* if user supplied report buffer wass smaller than required to accomodate read report, throw exception */
			if (length < (jint)num_of_bytes_read) {
				throw_serialcom_exception(env, 3, 0, E_INVALIDINLEN);
				return -1;
			}

			(*env)->SetByteArrayRegion(env, reportBuffer, 0, num_of_bytes_read, data_buf);
			if ((*env)->ExceptionOccurred(env) != NULL) {
				throw_serialcom_exception(env, 3, 0, E_SETBYTEARRAYREGION);
				return -1;
			}
			return num_of_bytes_read;
		}
	}

	/* must not be reached */
	return -1;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    readInputReportWithTimeoutR
 * Signature: (J[BII)I
 *
 * Read input report blocking for the time less than or equal to given timeout value. 
 * First byte will represent report ID if device uses numbered reports otherwise 1st 
 * byte will be report byte itself.
 * 
 * It reads a raw HID report (i.e. no report parsing is done).
 *
 * @return number of bytes read if function succeeds otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, WINAPI or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_readInputReportWithTimeoutR(JNIEnv *env,
		jobject obj, jlong handle, jbyteArray reportBuffer, jint length, jint timeoutVal) {

	int ret = 0;
	DWORD errorVal = 0;
	jbyte data_buf[1024];
	OVERLAPPED overlapped;
	DWORD wait_status = 0;
	DWORD num_of_bytes_read = 0;
	int have_data = 0;
	struct hid_dev_info* info = (struct hid_dev_info*) handle;

	/* only hEvent member need to be initialized and others can be left 0. */
	memset(&overlapped, 0, sizeof(overlapped));
	overlapped.hEvent = CreateEvent(NULL, FALSE, FALSE, NULL);
	if (overlapped.hEvent == NULL) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}
	
	/* An application should only use the HidD_GetXxx routines to obtain the current state of a device. 
	   If an application attempts to use HidD_GetInputReport to continuously obtain input reports, the 
	   reports can be lost. In addition, some devices might not support HidD_GetInputReport, and will 
	   become unresponsive if this routine is used. */

	/* ReadFile resets the event to a nonsignaled state when it begins the I/O operation. */
	ret = ReadFile(info->handle, (PVOID)data_buf, info->collection_capabilities.InputReportByteLength, &num_of_bytes_read, &overlapped);

	if (ret == 0) {
		errorVal = GetLastError();
		if (errorVal == ERROR_IO_PENDING) {

			wait_status = WaitForSingleObject(overlapped.hEvent, (DWORD)timeoutVal);

			if (wait_status == WAIT_OBJECT_0) {
				ret = GetOverlappedResult(info->handle, &overlapped, &num_of_bytes_read, FALSE);
				CloseHandle(overlapped.hEvent);

				if (ret > 0) {
					/* return data read from HID device if read from it */
					if (num_of_bytes_read > 0) {
						have_data = 1;
					}
				}else if (ret == 0) {
					errorVal = GetLastError();
					if ((errorVal == ERROR_HANDLE_EOF) || (errorVal == ERROR_IO_INCOMPLETE)) {
						CancelIo(info->handle);
						return 0;
					}else {
						/* This case indicates error. */
						throw_serialcom_exception(env, 4, errorVal, NULL);
						return -1;
					}
				}else {
				}
			}else if (wait_status == WAIT_FAILED) {
				/* This case indicates error. */
				CloseHandle(overlapped.hEvent);
				throw_serialcom_exception(env, 4, GetLastError(), NULL);
				return -1;
			}else if (wait_status == WAIT_TIMEOUT) {
				CloseHandle(overlapped.hEvent);
				return 0;
			}else {
				CloseHandle(overlapped.hEvent);
				return 0;
			}
		}else {
			/* This case indicates error. */
			CloseHandle(overlapped.hEvent);
			throw_serialcom_exception(env, 4, errorVal, NULL);
			return -1;
		}
	}else if (ret > 0) {
		CloseHandle(overlapped.hEvent);
		if (num_of_bytes_read > 0) {
			/* This indicates we got success and have read data in first go itself. */
			have_data = 1;
		}
	}else {
		CloseHandle(overlapped.hEvent);
	}

	/* if report was read from device pass that to java layer application. */
	if (have_data > 0) {
		if (data_buf[0] == (jbyte)0x00) {

			/* if user supplied report buffer wass smaller than required to accomodate read report, throw exception */
			if (length < (jint)(num_of_bytes_read - 1)) {
				throw_serialcom_exception(env, 3, 0, E_INVALIDINLEN);
				return -1;
			}

			/* if the device does not uses numbered reports, strip 1st byte.
			   other operating systems like Linux also does so internally. */
			(*env)->SetByteArrayRegion(env, reportBuffer, 0, (num_of_bytes_read -1), &data_buf[1]);
			if ((*env)->ExceptionOccurred(env) != NULL) {
				throw_serialcom_exception(env, 3, 0, E_SETBYTEARRAYREGION);
				return -1;
			}
			return (num_of_bytes_read - 1);
		}else {

			/* if user supplied report buffer wass smaller than required to accomodate read report, throw exception */
			if (length < (jint)num_of_bytes_read) {
				throw_serialcom_exception(env, 3, 0, E_INVALIDINLEN);
				return -1;
			}

			(*env)->SetByteArrayRegion(env, reportBuffer, 0, num_of_bytes_read, data_buf);
			if ((*env)->ExceptionOccurred(env) != NULL) {
				throw_serialcom_exception(env, 3, 0, E_SETBYTEARRAYREGION);
				return -1;
			}
			return num_of_bytes_read;
		}
	}

	/* reaching here means no data was read from device */
	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    sendFeatureReportR
 * Signature: (JB[BI)I
 *
 * Send a feature report to the given HID device.
 *
 * @return number of bytes sent to HID device if function succeeds otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, WINAPI or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_sendFeatureReportR(JNIEnv *env,
	jobject obj, jlong handle, jbyte reportID, jbyteArray reportBuffer, jint length) {
	
	int ret = 0;
	jbyte* buffer = NULL;
	struct hid_dev_info* info = (struct hid_dev_info*) handle;

	/* allocate memory accounting for report ID byte irresepective of whether device uses numbered reports or not */
	buffer = (jbyte *) calloc(info->collection_capabilities.FeatureReportByteLength, sizeof(unsigned char));
	if (buffer == NULL) {
		throw_serialcom_exception(env, 3, 0, E_CALLOCSTR);
		return -1;
	}

	/* If the device uses numbered report set very first byte to report number otherwise set it to 0x00 */
	if (reportID < 0) {
		buffer[0] = 0x00;
	}else {
		buffer[0] = reportID;
	}

	/* if user has given report of incorrect size, throw error. */
	if (length > (info->collection_capabilities.FeatureReportByteLength - 1)) {
		free(buffer);
		throw_serialcom_exception(env, 3, 0, E_INVALIDFETLEN);
		return -1;
	}

	/* copy report data fromm java to native buffer. */
	(*env)->GetByteArrayRegion(env, reportBuffer, 0, length, &buffer[1]);
	if ((*env)->ExceptionOccurred(env) != NULL) {
		free(buffer);
		throw_serialcom_exception(env, 3, 0, E_GETBYTEARRREGIONSTR);
		return -1;
	}

	ret = HidD_SetFeature(info->handle, (PVOID)buffer, (ULONG)info->collection_capabilities.FeatureReportByteLength);
	if (ret != TRUE) {
		free(buffer);
		throw_serialcom_exception(env, 3, 0, E_SETFEATUREREPORT);
		return -1;
	}

	free(buffer);
	return (jint)info->collection_capabilities.FeatureReportByteLength;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    getFeatureReportR
 * Signature: (JB[BI)I
 *
 * Get feature report from given HID device.
 *
 * @return number of bytes received from HID device if function succeeds otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, WINAPI or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_getFeatureReportR(JNIEnv *env, 
	jobject obj, jlong handle, jbyte reportID, jbyteArray reportBuffer, jint length) {

	int ret = 0;
	jbyte data_buf[1024];
	struct hid_dev_info* info = (struct hid_dev_info*) handle;

	/* If the device uses numbered report set very first byte to report number otherwise set it to 0x00 */
	if (reportID < 0) {
		data_buf[0] = 0x00;
	}else {
		data_buf[0] = reportID;
	}

	/* if user has given report of incorrect size, throw error. */
	if (length > (info->collection_capabilities.FeatureReportByteLength - 1)) {
		throw_serialcom_exception(env, 3, 0, E_INVALIDFETLEN);
		return -1;
	}

	ret = HidD_GetFeature(info->handle, (PVOID)data_buf, (ULONG)info->collection_capabilities.FeatureReportByteLength);
	if (ret != TRUE) {
		throw_serialcom_exception(env, 3, 0, E_GETFEATUREREPORT);
		return -1;
	}

	/* copy data to be returned to java layer */
	if (data_buf[0] == (jbyte)0x00) {
		/* if the device does not uses numbered reports, strip 1st byte.
		   other operating systems like Linux also does so internally. */
		(*env)->SetByteArrayRegion(env, reportBuffer, 0, (info->collection_capabilities.FeatureReportByteLength - 1), &data_buf[1]);
		if ((*env)->ExceptionOccurred(env) != NULL) {
			throw_serialcom_exception(env, 3, 0, E_SETBYTEARRAYREGION);
			return -1;
		}		
		return (info->collection_capabilities.FeatureReportByteLength - 1);
	}else {
		(*env)->SetByteArrayRegion(env, reportBuffer, 0, info->collection_capabilities.FeatureReportByteLength, data_buf);
		if ((*env)->ExceptionOccurred(env) != NULL) {
			throw_serialcom_exception(env, 3, 0, E_SETBYTEARRAYREGION);
			return -1;
		}	
		return info->collection_capabilities.FeatureReportByteLength;
	}

	/* should not be reached */
	return -1;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    getManufacturerStringR
 * Signature: (J)Ljava/lang/String;
 *
 * Get the name of manufacturer and return to caller.
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, WINAPI or C function fails.
 */
JNIEXPORT jstring JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_getManufacturerStringR(JNIEnv *env,
	jobject obj, jlong handle) {
	return get_hiddev_info_string(env, handle, MANUFACTURER_STRING);
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    getProductStringR
 * Signature: (J)Ljava/lang/String;
 *
 * Get product information string and return to caller.
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, WINAPI or C function fails.
 */
JNIEXPORT jstring JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_getProductStringR(JNIEnv *env,
	jobject obj, jlong handle) {
	return get_hiddev_info_string(env, handle, PRODUCT_STRING);
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    getSerialNumberStringR
 * Signature: (J)Ljava/lang/String;
 *
 * Read the serial number string and return to caller.
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, WINAPI or C function fails.
 */
JNIEXPORT jstring JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_getSerialNumberStringR(JNIEnv *env,
	jobject obj, jlong handle) {
	return get_hiddev_info_string(env, handle, SERIAL_STRING);
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    getIndexedStringR
 * Signature: (JI)Ljava/lang/String;
 *
 * Get string as returned by descriptor for given index.
 *
 * @return string at the given index or NULL if error occurs.
 * @throws SerialComException if any JNI function, WINAPI or C function fails.
 */
JNIEXPORT jstring JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_getIndexedStringR(JNIEnv *env,
	jobject obj, jlong handle, jint index) {

	BOOLEAN ret;
	wchar_t buffer[512];
	jstring indexedstr = NULL;
	struct hid_dev_info* info = (struct hid_dev_info*) handle;

	memset(buffer, '\0', 512);
	ret = HidD_GetIndexedString(info->handle, (ULONG)index, (PVOID)buffer, (ULONG)sizeof(buffer));
	if (ret != TRUE) {
		throw_serialcom_exception(env, 3, 0, E_INDEXSTR);
		return NULL;
	}

	indexedstr = (*env)->NewString(env, buffer, (jsize)_tcslen(buffer));
	if ((indexedstr == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		(*env)->ExceptionClear(env);
		throw_serialcom_exception(env, 3, 0, E_NEWSTR);
		return NULL;
	}

	return indexedstr;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    listUSBHIDdevicesWithInfo
 * Signature: (I)[Ljava/lang/String;
 *
 * @return array of Strings containing USB HID devices if found matching given criteria, zero length
 *         array if no node matching given criteria is found, NULL if error occurs.
 * @throws SerialComException if any JNI function, WINAPI or C function fails.
 */
JNIEXPORT jobjectArray JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_listUSBHIDdevicesWithInfo(JNIEnv *env,
	jobject obj, jint vendorFilter) {
	return enumerate_usb_hid_devices(env, vendorFilter);
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    getReportDescriptorR
 * Signature: (J)[B
 *
 * Try to read HID report descriptor from the given HID device.
 *
 * @return always return NULL as of now.
 */
JNIEXPORT jbyteArray JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_getReportDescriptorR(JNIEnv *env,
		jobject obj, jlong handle) {
		return NULL;
}

/*
* Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
* Method:    getPhysicalDescriptorR
* Signature: (J)[B
*
* Try to read physical descriptor from the given HID device.
*
* @return byte array containing physical descriptor values read from given HID device, NULL if
*         any error occurs.
* @throws SerialComException if any JNI function, WINAPI or C function fails.
*/
JNIEXPORT jbyteArray JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_getPhysicalDescriptorR(JNIEnv *env,
	jobject obj, jlong handle) {

	BOOLEAN ret = FALSE;
	unsigned char buffer[1024];
	struct hid_dev_info* info = (struct hid_dev_info*) handle;
	jbyteArray physicalDescriptor = NULL;

	memset(buffer, '\0', 1024);
	ret = HidD_GetPhysicalDescriptor(info->handle, (PVOID)buffer, 1024);
	if (ret == FALSE) {
		throw_serialcom_exception(env, 3, 0, E_PHYSICALDESC);
		return NULL;
	}

	(*env)->SetByteArrayRegion(env, physicalDescriptor, 0, (jsize)strlen(buffer), (const jbyte *)buffer);
	if ((*env)->ExceptionOccurred(env) != NULL) {
		throw_serialcom_exception(env, 3, 0, E_SETBYTEARRAYREGION);
		return NULL;
	}

	return physicalDescriptor;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    flushInputReportQueueR
 * Signature: (J)I
 *
 * Empty the input report buffer maintained by operating system. For windows, it deletes all pending input 
 * reports in a top-level collection's input queue.
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, WINAPI or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_flushInputReportQueueR(JNIEnv *env,
		jobject obj, jlong handle) {
		
		BOOLEAN ret = FALSE;
		struct hid_dev_info* info = (struct hid_dev_info*) handle;

		ret = HidD_FlushQueue(info->handle);
		if(ret == FALSE) {
			throw_serialcom_exception(env, 3, 0, E_FLUSHIN);
			return -1;
		}
		
		return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    findDriverServingHIDDevice
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 *
 * Gets driver who is responsible for communication with the given HID device.
 *
 * @return driver name for given HID device on success or NULL if an error occurs.
 * @throws SerialComException if any JNI function, WINAPI or C function fails.
 */
JNIEXPORT jstring JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_findDriverServingHIDDeviceR(JNIEnv *env,
		jobject obj, jstring hidDevNode) {
	return find_driver_for_given_hiddevice(env, hidDevNode);
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    readPlatformSpecificInputReportR
 * Signature: (JB[BI)I
 *
 * It reads a raw HID report (i.e. no report parsing is done) into the given buffer. If the device uses
 * numbered report, 1st byte will be report ID otherwise 1st byte will be report data itself.
 * This function is mainly used when exchanged reports are inconsistent with their HID report 
 * descriptor.
 *
 * @return 1 if operation completed successfully, -1 if an error occurs.
 * @throws SerialComException if any JNI function, WINAPI or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_readPlatformSpecificInputReportR(JNIEnv *env,
		jobject obj, jlong handle, jbyte reportID, jbyteArray report, jint length) {
		
	BOOLEAN ret = FALSE;
	jbyte* buffer = NULL;
	int final_length = 0;
	struct hid_dev_info* info = (struct hid_dev_info*) handle;

	/* If the device uses numbered report set very first byte to report number otherwise set it to 0x00 */
	if (reportID < 0) {
		buffer = (jbyte *) calloc(length, sizeof(unsigned char));
		if (buffer == NULL) {
			throw_serialcom_exception(env, 3, 0, E_CALLOCSTR);
			return -1;
		}
		buffer[0] = 0x00;
		final_length = length;
		ret = HidD_GetInputReport(info->handle, (PVOID)buffer, (ULONG)length);
	}else {
		buffer = (jbyte *) calloc((length + 1), sizeof(unsigned char));
		if (buffer == NULL) {
			throw_serialcom_exception(env, 3, 0, E_CALLOCSTR);
			return -1;
		}
		buffer[0] = reportID;
		final_length = length + 1;
		ret = HidD_GetInputReport(info->handle, (PVOID)buffer, (ULONG)(length + 1));
	}
		
	if(ret == FALSE) {
		free(buffer);
		throw_serialcom_exception(env, 3, 0, E_HidDGetInputReport);
		return -1;
	}
		
	/* copy data from native buffer to Java buffer. */
	(*env)->SetByteArrayRegion(env, report, 0, final_length, buffer);
	if((*env)->ExceptionOccurred(env) != NULL) {
		free(buffer);
		throw_serialcom_exception(env, 3, 0, E_SETBYTEARRAYREGION);
		return -1;
	}
	
	free(buffer);
	return 1;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    writePlatformSpecificOutputReportR
 * Signature: (JB[BI)I
 * 
 * Sends output report to HID device. This function is mainly used when exchanged reports are inconsistent 
 * with their HID report descriptor.
 * 
 * @return 1 if function succeeds otherwise -1 if error occurs.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_writePlatformSpecificOutputReportR(JNIEnv *env,
		jobject obj, jlong handle, jbyte reportID, jbyteArray report, jint length) {
		
	BOOLEAN ret = FALSE;
	jbyte* buffer = NULL;
	struct hid_dev_info* info = (struct hid_dev_info*) handle;

	buffer = (jbyte *) calloc((length + 1), sizeof(unsigned char));
	if (buffer == NULL) {
		throw_serialcom_exception(env, 3, 0, E_CALLOCSTR);
		return -1;
	}
		
	if(reportID < 0) {
		/* send 0x00 as 1st byte if device does not support report ID */
		buffer[0] = 0x00;
	}else {
		buffer[0] = reportID;
	}
	
	(*env)->GetByteArrayRegion(env, report, 0, length, &buffer[1]);
	if((*env)->ExceptionOccurred(env) != NULL) {
		free(buffer);
		throw_serialcom_exception(env, 3, 0, E_GETBYTEARRREGIONSTR);
		return -1;
	}
	
	/* an application should only use these routines to set the current state of a collection. Some devices might not support 
	   HidD_SetOutputReport and will become unresponsive if this routine is used. */
	ret = HidD_SetOutputReport(info->handle, (PVOID)buffer, (ULONG)(length + 1));
	if(ret == FALSE) {
		free(buffer);
		throw_serialcom_exception(env, 3, 0, E_HidDSetOutputReport);
		return -1;
	}
	
	free(buffer);
	return 1;
}

