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
 * Signature: (Ljava/lang/String;)J
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
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jlong JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_openHidDeviceByPath(JNIEnv *env, 
	jobject obj, jstring pathName) {

	int x = 0;
	BOOLEAN ret = FALSE;
	NTSTATUS result;
	const jchar* device_node = NULL;
	wchar_t dev_instance[1024];
	wchar_t dev_full_path[1024];
	struct hid_dev_info* info = NULL;

	/* extract com port name to match (as an array of Unicode characters) */
	device_node = (*env)->GetStringChars(env, pathName, JNI_FALSE);
	if ((device_node == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_GETSTRCHARSTR);
		return -1;
	}

	/* HID\VID_04D8&PID_00DF&MI_02\7&33842c3f&0&0000 (replace \ with #) */
	memset(dev_instance, '\0', 1024);
	swprintf_s(dev_instance, 1024, TEXT("%s"), device_node);
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

	info = (struct hid_dev_info *) calloc(1, sizeof(struct hid_dev_info));
	if (info == NULL) {
		(*env)->ReleaseStringChars(env, pathName, device_node);
		throw_serialcom_exception(env, 3, 0, E_CALLOCSTR);
		return -1;
	}

	/* open the device using the cooked device path (dev_full_path) */
	info->handle = CreateFile(dev_full_path, (GENERIC_READ | GENERIC_WRITE), (FILE_SHARE_READ | FILE_SHARE_WRITE), 
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
 * Method:    closeHidDevice
 * Signature: (J)I
 *
 * Closes handle to HID device and free up resources.
 *
 * @return 0 if function succeeds otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, WINAPI or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_closeHidDevice(JNIEnv *env,
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
 * Method:    writeOutputReport
 * Signature: (JB[BI)I
 * 
 * Sends a output report to given HID device after properly formating the given report.
 *
 * For MCP2200 both the OutputReportByteLength and InputReportByteLength are 17 bytes.
 *
 * @return number of bytes sent to device if function succeeds otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_writeOutputReport(JNIEnv *env,
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

	/* if user has given report of incorrect size, throw error. */
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
		memset(&buffer[length + 1], 0x00, (info->collection_capabilities.OutputReportByteLength - length));
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
	return (jint)num_of_bytes_written;
}


/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    readInputReport
 * Signature: (J[BI)I
 *
 * TODO MAC RETURN 1ST BYTE AS REPORT ID OR NOT.
 *
 * @return number of bytes read if function succeeds otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_readInputReport(JNIEnv *env,
	jobject obj, jlong handle, jbyteArray reportBuffer, jint length) {

	return -1;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    readInputReportWithTimeout
 * Signature: (J[BII)I
 *
 * Read input report blocking for the time less than or equal to given timeout value. First byte will represent
 * report ID if device uses numbered reports otherwise 1st byte will be report byte itself.
 *
 * @return number of bytes read if function succeeds otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, WINAPI or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_readInputReportWithTimeout(JNIEnv *env,
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
			/* if the device does not uses numbered reports, strip 1st byte.
			   other operating systems like Linux also does so internally. */
			(*env)->SetByteArrayRegion(env, reportBuffer, 0, (num_of_bytes_read -1), &data_buf[1]);
			if ((*env)->ExceptionOccurred(env) != NULL) {
				throw_serialcom_exception(env, 3, 0, E_SETBYTEARRAYREGION);
				return -1;
			}
			return (num_of_bytes_read - 1);
		}else {
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
 * Method:    sendFeatureReport
 * Signature: (JB[BI)I
 *
 * Send a feature report to the given HID device.
 *
 * @return number of bytes sent to HID device if function succeeds otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, WINAPI or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_sendFeatureReport(JNIEnv *env,
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

	ret = HidD_SetFeature((HANDLE)handle, (PVOID)buffer, (ULONG)info->collection_capabilities.FeatureReportByteLength);
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
 * Method:    getFeatureReport
 * Signature: (JB[BI)I
 *
 * Get feature report from given HID device.
 *
 * @return number of bytes received from HID device if function succeeds otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, WINAPI or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_getFeatureReport(JNIEnv *env, 
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

	ret = HidD_GetFeature((HANDLE)handle, (PVOID)data_buf, (ULONG)info->collection_capabilities.FeatureReportByteLength);
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
 * Method:    getManufacturerString
 * Signature: (J)Ljava/lang/String;
 *
 * Retrives manufacturer name string of given USB device.
 *
 * @return manufacturer name string of USB device or NULL if error occurs.
 * @throws SerialComException if any JNI function, WINAPI or C function fails.
 */
JNIEXPORT jstring JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_getManufacturerString(JNIEnv *env,
	jobject obj, jlong handle) {

	BOOL ret;
	wchar_t buffer[256];
	jstring manufacturer = NULL;

	memset(buffer, '\0', 256);
	ret = HidD_GetManufacturerString((HANDLE)handle, buffer, sizeof(wchar_t));
	if (ret != TRUE) {
		throw_serialcom_exception(env, 3, 0, E_MANUFACTURER);
		return NULL;
	}

	manufacturer = (*env)->NewString(env, buffer, (jsize)_tcslen(buffer));
	if ((manufacturer == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		(*env)->ExceptionClear(env);
		throw_serialcom_exception(env, 3, 0, E_NEWSTR);
		return NULL;
	}

	return manufacturer;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    getProductString
 * Signature: (J)Ljava/lang/String;
 *
 * Retrives product string of given USB device.
 *
 * @return serial number string of USB device or NULL if error occurs.
 * @throws SerialComException if any JNI function, WINAPI or C function fails.
 */
JNIEXPORT jstring JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_getProductString(JNIEnv *env,
	jobject obj, jlong handle) {

	BOOL ret;
	wchar_t buffer[256];
	jstring product = NULL;

	memset(buffer, '\0', 256);
	ret = HidD_GetProductString((HANDLE)handle, buffer, sizeof(wchar_t));
	if (ret != TRUE) {
		throw_serialcom_exception(env, 3, 0, E_PRODUCT);
		return NULL;
	}

	product = (*env)->NewString(env, buffer, (jsize)_tcslen(buffer));
	if ((product == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		(*env)->ExceptionClear(env);
		throw_serialcom_exception(env, 3, 0, E_NEWSTR);
		return NULL;
	}

	return product;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    getSerialNumberString
 * Signature: (J)Ljava/lang/String;
 *
 * Retrives serial number string of given USB device.
 *
 * @return serial number string of USB device or NULL if error occurs.
 * @throws SerialComException if any JNI function, WINAPI or C function fails.
 */
JNIEXPORT jstring JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_getSerialNumberString(JNIEnv *env,
	jobject obj, jlong handle) {

	BOOL ret;
	wchar_t buffer[256];
	jstring serial = NULL;

	memset(buffer, '\0', 256);
	ret = HidD_GetSerialNumberString((HANDLE)handle, buffer, sizeof(wchar_t));
	if (ret != TRUE) {
		throw_serialcom_exception(env, 3, 0, E_SERIALNUMBER);
		return NULL;
	}

	serial = (*env)->NewString(env, buffer, (jsize)_tcslen(buffer));
	if ((serial == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		(*env)->ExceptionClear(env);
		throw_serialcom_exception(env, 3, 0, E_NEWSTR);
		return NULL;
	}

	return serial;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge
 * Method:    getIndexedString
 * Signature: (JI)Ljava/lang/String;
 *
 * Retrives string at a given index from given USB device.
 *
 * @return string at the given index or NULL if error occurs.
 * @throws SerialComException if any JNI function, WINAPI or C function fails.
 */
JNIEXPORT jstring JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_getIndexedString(JNIEnv *env,
	jobject obj, jlong handle, jint index) {

	BOOL ret;
	wchar_t buffer[256];
	jstring indexedstr = NULL;

	memset(buffer, '\0', 256);
	ret = HidD_GetIndexedString((HANDLE)handle, (ULONG)index, buffer, sizeof(wchar_t));
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
 * Method:    getReportDescriptor
 * Signature: (J)[B
 *
 * Try to read report descriptor from the given HID device.
 *
 * @return NULL always as of now as there is no direct API to get report descriptor in Windows.
 * @throws SerialComException if any JNI function, WINAPI or C function fails.
 */
JNIEXPORT jbyteArray JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_getReportDescriptor(JNIEnv *env, 
	jobject obj, jlong handle) {
	return NULL;
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
JNIEXPORT jstring JNICALL Java_com_embeddedunveiled_serial_internal_SerialComHIDJNIBridge_findDriverServingHIDDevice(JNIEnv *env, 
	jobject obj, jstring hidDevNode) {
	return find_driver_for_given_hiddevice(env, hidDevNode);
}


