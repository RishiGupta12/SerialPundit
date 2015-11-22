/************************************************************************************************************
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
*************************************************************************************************************/

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

/* Windows */
#include <windows.h>
#include <process.h>
#include <setupapi.h>
#include <tchar.h>
#include <strsafe.h>

#include <jni.h>
#include "windows_serial_lib.h"

/* Common interface with java layer for supported OS types. */
#include "../../com_embeddedunveiled_serial_internal_SerialComPortJNIBridge.h"

#undef  UART_NATIVE_LIB_VERSION
#define UART_NATIVE_LIB_VERSION "1.0.4"

/* Reference to JVM shared among all the threads within a process. It may be unsafe to cache a JNIEnv* instance
   and keep using it, as it may vary depending on the currently active thread. So save a JavaVM* instance only, 
   which will never change. */
JavaVM *jvm;

/* When creating data looper threads, we pass some data to thread. A index in this array, holds pointer to
* the structure which is passed as parameter to a thread. Every time a data looper thread is created, we
* save the location of parameters passed to it and update the index to be used next time. */
int dtp_index = 0;
struct looper_thread_params handle_looper_info[MAX_NUM_THREADS] = { 0 };

/* Holds information for USB hot plug monitor facility. */
int usb_dev_monitor_index = 0;
struct usb_dev_monitor_info usb_hotplug_monitor_info[MAX_NUM_THREADS] = { { 0 } };

/* The threads of a single process can use a critical section object for mutual-exclusion synchronization.
 * Used to protect global data from concurrent access. */
CRITICAL_SECTION csmutex;

/* Called when this shared library is loaded or un-loaded. This clean up resources on library exit. */
BOOL WINAPI DllMain(HANDLE hModule, DWORD reason_for_call, LPVOID lpReserved) {
	switch (reason_for_call) {
		case DLL_PROCESS_DETACH:
			DeleteCriticalSection(&csmutex);
			break;
	}
	return TRUE;
}

 /*
 * Class:     com_embeddedunveiled_serial_internal_SerialComPortJNIBridge
 * Method:    initNativeLib
 * Signature: ()I
 *
 * This function gets the JVM interface (used in the Invocation API) associated with the current
 * thread and save it so that it can be used across native library, threads etc. It creates and
 * prepares critical section to synchronize access to global data. Clear all exceptions and prepares
 * SerialComException class for exception throwing.
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComPortJNIBridge_initNativeLib(JNIEnv *env,
	jobject obj) {
	
	int ret = 0;
	jclass serialComExceptionClass = NULL;

	ret = (*env)->GetJavaVM(env, &jvm);
	if(ret < 0) {
		serialComExceptionClass = (*env)->FindClass(env, SCOMEXPCLASS);
		if((serialComExceptionClass == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			(*env)->ExceptionClear(env);
			LOGE(E_FINDCLASSSCOMEXPSTR, "NATIVE initNativeLib() could not get JVM.");
			return -1;
		}
		(*env)->ThrowNew(env, serialComExceptionClass, E_GETJVMSTR);
		return -1;
	}

	/* Initialise critical section (does not return any value). */
	InitializeCriticalSection(&csmutex);

	/* clear if something unexpected was there. */
	if((*env)->ExceptionCheck(env) == JNI_TRUE) {
		(*env)->ExceptionClear(env);
	}
	return 0;	
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComPortJNIBridge
 * Method:    getNativeLibraryVersion
 * Signature: ()Ljava/lang/String;
 * 
 * Returns native library version from hard-coded string or null.
 * 
 * @return version string if function succeeds otherwise NULL.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jstring JNICALL Java_com_embeddedunveiled_serial_internal_SerialComPortJNIBridge_getNativeLibraryVersion(JNIEnv *env, jobject obj) {
	jstring version = NULL;
	version = (*env)->NewStringUTF(env, UART_NATIVE_LIB_VERSION);
	if((version == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		(*env)->ExceptionClear(env);
		throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
		return NULL;
	}
	return version;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComPortJNIBridge
 * Method:    listAvailableComPorts
 * Signature: ()[Ljava/lang/String;
 *
 * This returns serial style ports known to system at this moment. This information is gleaned by reading 
 * windows registry for serial ports.
 * 
 * Use registry editor to see available serial ports; HKEY_LOCAL_MACHINE->HARDWARE->DEVICEMAP->SERIALCOMM.
 *
 * @return array of serial ports found in system on success otherwise NULL if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jobjectArray JNICALL Java_com_embeddedunveiled_serial_internal_SerialComPortJNIBridge_listAvailableComPorts(JNIEnv *env,
		jobject obj) {

	HKEY hKey;
	TCHAR achClass[MAX_PATH] = TEXT("");  /* buffer for class name       */
	DWORD cchClassName = MAX_PATH;        /* size of class string        */
	DWORD cSubKeys = 0;                   /* number of subkeys           */
	DWORD cbMaxSubKey;                    /* longest subkey size         */
	DWORD cchMaxClass;                    /* longest class string        */
	DWORD cValues;                        /* number of values for key    */
	DWORD cchMaxValue;                    /* longest value name          */
	DWORD cbMaxValueData;                 /* longest value data          */
	DWORD cbSecurityDescriptor;           /* size of security descriptor */
	FILETIME ftLastWriteTime;             /* last write time             */
	LONG result = 0;
	DWORD ret_code = 0;
	DWORD cchValueName = 1024;
	DWORD cchValueData = 1024;
	TCHAR nameBuffer[1024];
	TCHAR valueBuffer[1024];
	
	int x = 0;
	struct jstrarray_list list = {0};
	jstring serial_device;
	jclass strClass = NULL;
	jobjectArray serialDevicesFound = NULL;
	
	/* allocate memory for 100 jstrings */
	init_jstrarraylist(&list, 100);

	/* Try to open the registry key for serial communication devices. */
	result = RegOpenKeyEx(HKEY_LOCAL_MACHINE,     /* pre-defined key                                                                    */
		TEXT("HARDWARE\\DEVICEMAP\\SERIALCOMM"),  /* name of the registry subkey to be opened                                           */
		0,                                        /* option to apply when opening the key                                               */
		KEY_READ | KEY_WOW64_64KEY,               /* access rights to the key to be opened, user might run 32 bit JRE on 64 bit machine */
		&hKey);                                   /* variable that receives a handle to the opened key                                  */
		
	if(result != ERROR_SUCCESS) {
		throw_serialcom_exception(env, 4, result, NULL);
		return NULL;
	}

	/* Count number of values for this key. */
	result = 0;
	result = RegQueryInfoKey(hKey,                    /* key handle                    */
		                     achClass,                /* buffer for class name         */
							 &cchClassName,           /* size of class string          */
							 NULL,                    /* reserved                      */
							 &cSubKeys,               /* number of subkeys             */
							 &cbMaxSubKey,            /* longest subkey size           */
							 &cchMaxClass,            /* longest class string          */
							 &cValues,                /* number of values for this key */
							 &cchMaxValue,            /* longest value name            */
							 &cbMaxValueData,         /* longest value data            */
							 &cbSecurityDescriptor,   /* security descriptor           */
							 &ftLastWriteTime);       /* last write time               */

	/* For each entry try to get names and return array constructed out of these names. */
	if(cValues > 0) {
			for (x = 0; x < (int)cValues; x++) {
				nameBuffer[0] = '\0';
				valueBuffer[0] = '\0';
				cchValueName = 1024;
				cchValueData = 1024;
				result = RegEnumValue(hKey,                      /* handle to an open registry key                                                                       */
					                  x,                         /* index of the value to be retrieved                                                                   */
									  nameBuffer,              /* pointer to a buffer that receives the name of the value as a null-terminated string                  */
									  &cchValueName,           /* pointer to a variable that specifies the size of the buffer pointed to by the lpValueName parameter  */
									  NULL,                    /* reserved                                                                                             */
									  NULL,                    /* pointer to a variable that receives a code indicating the type of data stored in the specified value */
									  (LPBYTE) valueBuffer,    /* pointer to a buffer that receives the value data for this registry entry                             */
									  &cchValueData);          /* pointer to a variable that specifies the size of the buffer pointed to by the lpData parameter       */
				if(result == ERROR_SUCCESS) {
					valueBuffer[cchValueData / 2] = '\0';
					serial_device = (*env)->NewString(env, valueBuffer, (cchValueData / 2));
					if((serial_device == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
						(*env)->ExceptionClear(env);
						RegCloseKey(hKey);
						free_jstrarraylist(&list);
						throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
						return NULL;
					}
					insert_jstrarraylist(&list, serial_device);
				}else if(result == ERROR_NO_MORE_ITEMS) {
					break;
				}else {
				}
			}
	}
	
	/* Close the key , no more required. */
	RegCloseKey(hKey);
	
	/* Create a JAVA/JNI style array of String object, populate it and return to java layer. */
	strClass = (*env)->FindClass(env, JAVALSTRING);
	if((strClass == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		(*env)->ExceptionClear(env);
		free_jstrarraylist(&list);
		throw_serialcom_exception(env, 3, 0, E_FINDCLASSSSTRINGSTR);
		return NULL;
	}

	serialDevicesFound = (*env)->NewObjectArray(env, (jsize) list.index, strClass, NULL);
	if((serialDevicesFound == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		(*env)->ExceptionClear(env);
		free_jstrarraylist(&list);
		throw_serialcom_exception(env, 3, 0, E_NEWOBJECTARRAYSTR);
		return NULL;
	}

	for (x=0; x < list.index; x++) {
		(*env)->SetObjectArrayElement(env, serialDevicesFound, x, list.base[x]);
		if((*env)->ExceptionOccurred(env)) {
			(*env)->ExceptionClear(env);
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 3, 0, E_SETOBJECTARRAYSTR);
			return NULL;
		}
	}

	free_jstrarraylist(&list);
	return serialDevicesFound;	
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComPortJNIBridge
 * Method:    listUSBdevicesWithInfo
 * Signature: (I)[Ljava/lang/String;
 *
 * Find USB devices with information about them using platform specific facilities. The info sequence is :
 * Vendor ID, Product ID, Serial number, Product, Manufacturer, USB bus number, USB Device number.
 *
 * @return array of Strings containing info about USB device(s) otherwise NULL if an error occurs or no
 *         devices found.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jobjectArray JNICALL Java_com_embeddedunveiled_serial_internal_SerialComPortJNIBridge_listUSBdevicesWithInfo(JNIEnv *env,
		jobject obj, jint vendorFilter) {
	return list_usb_devices(env, vendorFilter);
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComPortJNIBridge
 * Method:    listComPortFromUSBAttributes
 * Signature: (IILjava/lang/String;)[Ljava/lang/String;
 *
 * Find the COM Port/ device node assigned to USB-UART converter device using platform specific
 * facilities. USB serial number is optional and case-insensitive.
 *
 * @return array of Strings containing com ports if found matching given criteria otherwise NULL if
 *         error occurs or no node matching criteria is found.
 * @throws SerialComException if any JNI function, Win API call or C function fails.

 */
JNIEXPORT jobjectArray JNICALL Java_com_embeddedunveiled_serial_internal_SerialComPortJNIBridge_findComPortFromUSBAttributes(JNIEnv *env,
		jobject obj, jint vid, jint pid, jstring serial) {
	return vcp_node_from_usb_attributes(env, vid, pid, serial);
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComPortJNIBridge
 * Method:    isUSBDevConnected
 * Signature: (IILjava/lang/String;)I
 *
 * Enumerate and check if given usb device identified by its USB-IF VID and PID is connected to
 * system or not. USB serial number is optional and case-insensitive.
 *
 * @return 1 if device is connected, 0 if not connected , -1 if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComPortJNIBridge_isUSBDevConnected(JNIEnv *env,
	jobject obj, jint vid, jint pid, jstring serial_number) {
		return is_usb_dev_connected(env, vid, pid, serial_number);
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComPortJNIBridge
 * Method:    openComPort
 * Signature: (Ljava/lang/String;ZZZ)J
 *
 * Communications ports cannot be shared in the same manner as text files are shared in Windows.
 * Use overlapped I/O for simultaneous reading/writing. Nonoverlapped I/O causes WriteFile to 
 * block if a ReadFile is in progress, and vice versa.
 *
 * @return valid handle on success otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jlong JNICALL Java_com_embeddedunveiled_serial_internal_SerialComPortJNIBridge_openComPort(JNIEnv *env,
		jobject obj, jstring portName, jboolean enableRead, 
		jboolean enableWrite, jboolean exclusiveOwner) {

	int ret = 0;
	DWORD dwerror = 0;
	COMSTAT comstat;
	int OPEN_MODE = 0;
	int SHARING = 0;
	DCB dcb = { 0 };                      /* Device control block for RS-232 serial devices */
	COMMTIMEOUTS lpCommTimeouts;
	wchar_t portFullName[512] = { 0 };
	HANDLE hComm = INVALID_HANDLE_VALUE;
	const jchar* port = NULL;

	port = (*env)->GetStringChars(env, portName, JNI_FALSE);
	if((port == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_GETSTRUTFCHARSTR);
		return -1;
	}

	/* To specify a COM port number greater than 9, use the following syntax : "\\.\COMXX". */
	swprintf_s(portFullName, 512/2, TEXT("\\\\.\\%s"), port);

	/* Access style; read, write or both. None case is handles in java layer itself. */
	if((enableRead == JNI_TRUE) && (enableWrite == JNI_TRUE)) {
		OPEN_MODE = GENERIC_READ | GENERIC_WRITE;
	}else if(enableRead == JNI_TRUE) {
		OPEN_MODE = GENERIC_READ;
	}else if(enableWrite == JNI_TRUE) {
		OPEN_MODE = GENERIC_WRITE;
	}else {
	}

	/* Exclusive ownership claim; '0' means no sharing. As per this link sharing has to be 0 means exclusive access.
	 * msdn.microsoft.com/en-us/library/windows/desktop/aa363858%28v=vs.85%29.aspx */
	SHARING = 0;

	/* The CreateFile function opens a communications port. */
	hComm = CreateFile(portFullName,
		               OPEN_MODE,                                     /* Access style; read, write or both */
					   SHARING,                                       /* Exclusive owner or shared */
					   NULL,                                          /* Security */
					   OPEN_EXISTING,                                 /* Open existing port */
					   FILE_FLAG_OVERLAPPED,                         /* Overlapping operations permitted */
					   NULL);					                      /* hTemplateFile */
	if(hComm == INVALID_HANDLE_VALUE) {
		(*env)->ReleaseStringChars(env, portName, port);
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}
	
	(*env)->ReleaseStringChars(env, portName, port);

	/* Clear the device's communication error flag if set previously due to any reason. */
	ClearCommError(hComm, &dwerror, &comstat);
	
	/* Set up input/output buffer sizes. Specify the recommended sizes for the internal buffers used
	   by the driver for the specified device. The device driver receives the recommended buffer sizes,
	   but is free to use any input and output (I/O) buffering scheme. */
	SetupComm(hComm, CommInBufSize, CommOutBufSize);

	/* Make sure that the device we are going to operate on, is a valid serial port in sane state. */
	SecureZeroMemory(&dcb, sizeof(DCB));
	dcb.DCBlength = sizeof(DCB);

	/* Retrieves the current control settings for a specified communications device. */
	ret = GetCommState(hComm, &dcb);
	if(ret == 0) {
		CloseHandle(hComm);
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}

	/* Set port to 9600 8N1 setting, no flow control. Bring the port in sane state. */
	dcb.BaudRate = CBR_9600;
	dcb.ByteSize = 8;
	dcb.Parity = NOPARITY;
	dcb.StopBits = ONESTOPBIT;
	dcb.fBinary = TRUE;                    /* Windows does not support non-binary mode transfers, so this member must be TRUE. */
	dcb.fOutxCtsFlow = FALSE;
	dcb.fOutxDsrFlow = FALSE;
	dcb.fDtrControl = DTR_CONTROL_ENABLE;
	dcb.fDsrSensitivity = FALSE;
	dcb.fTXContinueOnXoff = TRUE;
	dcb.fOutX = FALSE;
	dcb.fInX = FALSE;
	dcb.fErrorChar = FALSE;
	dcb.fRtsControl = RTS_CONTROL_ENABLE;
	dcb.fAbortOnError = FALSE;
	dcb.XonLim = 2048;
	dcb.XoffLim = 2048;
	dcb.XonChar = (CHAR) 0x11;      /* DC1, CTRL-Q, Default value */
	dcb.XoffChar = (CHAR) 0X13;     /* DC3, CTRL-S, Default value */
	dcb.fNull = FALSE;              /* Do not discard when null bytes are received. */
	ret = SetCommState(hComm, &dcb);
	if(ret == 0) {
		CloseHandle(hComm);
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}

	/* Set correct default timing parameters that will define how ReadFile and WriteFile functions will behave. */
	SecureZeroMemory(&lpCommTimeouts, sizeof(COMMTIMEOUTS));
	lpCommTimeouts.ReadIntervalTimeout = 1;
	lpCommTimeouts.ReadTotalTimeoutConstant = 150;
	lpCommTimeouts.WriteTotalTimeoutConstant = 1000;
	ret = SetCommTimeouts(hComm, &lpCommTimeouts);
	if(ret == 0) {
		CloseHandle(hComm);
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}
	
	/* Reset communication mask. */
	SetCommMask(hComm, EV_BREAK|EV_CTS|EV_DSR|EV_ERR|EV_RING|EV_RLSD|EV_RXCHAR|EV_RXFLAG|EV_TXEMPTY);

	return (jlong)hComm;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComPortJNIBridge
 * Method:    closeComPort
 * Signature: (J)I
 *
 * Exclusive ownership is cleared automatically by system when port is closed.
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComPortJNIBridge_closeComPort(JNIEnv *env, 
	jobject obj, jlong handle) {

	jint ret = -1;
	DWORD errorVal = 0;

	/* Flush remaining data in IO buffers if any by chance. */
	ret = FlushFileBuffers((HANDLE)handle);	
	if(ret == 0) {
		LOGEN("closeComPort()", "FlushFileBuffers() failed to flush data with windows error code : ", GetLastError());
	}

	/* Release DTR line. */
	ret = EscapeCommFunction((HANDLE)handle, CLRDTR);
	if(ret == 0) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}

	/* Close the port. */
	ret = CloseHandle((HANDLE)handle);
	if(ret == 0) {
		errorVal = GetLastError();
		if(errorVal == ERROR_INVALID_HANDLE) {
			/* This is not an error in windows. */
		}else {
			throw_serialcom_exception(env, 4, errorVal, NULL);
			return -1;
		}
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComPortJNIBridge
 * Method:    readBytes
 * Signature: (JI)[B
 *
 * The maximum number of bytes that read system call can read is the value that can be 
 * stored in an object of type ssize_t. In JNI programming 'jbyte' is 'signed char'. 
 * Default count is set to 1024 in java layer.
 *
 * 1. If data is read from serial port and no error occurs, return array of bytes.
 * 2. If there is no data to read from serial port and no error occurs, return NULL.
 * 3. If error occurs for whatever reason, return NULL and throw exception.
 *
 * The number of bytes return can be less than the request number of bytes but can never be 
 * greater than the requested number of bytes. This is implemented using total_read variable. 
 * Size request should not be more than 2048.
 *
 * This function do not block any signals and handles the following scenarios :
 * 1. Complete read in 1st pass itself.
 * 2. Partial read followed by complete read.
 * 3. Partial read followed by partial read then complete read.
 * 
 * Blocking read with 150ms timeout. If data is available return even before 150ms has passed. 
 * use GetTickCount64() for rough experiments.
 *
 * @return data read or NULL.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jbyteArray JNICALL Java_com_embeddedunveiled_serial_internal_SerialComPortJNIBridge_readBytes(JNIEnv *env,
		jobject obj, jlong handle, jint count) {

	int ret = 0;
	DWORD errorVal = 0;
	jbyte data_buf[2*1024];
	OVERLAPPED overlapped;
	jbyteArray data_read;
	DWORD wait_status = 0;
	DWORD num_of_bytes_read = 0;

	/* Only hEvent member need to be initialled and others can be left 0. */
	memset(&overlapped, 0, sizeof(overlapped));
	overlapped.hEvent = CreateEvent(NULL, FALSE, FALSE, NULL);
	if(overlapped.hEvent == NULL) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return NULL;
	}

	/* ReadFile resets the event to a nonsignaled state when it begins the I/O operation. */
	ret = ReadFile((HANDLE)handle, data_buf, count, &num_of_bytes_read, &overlapped);

	if(ret == 0) {
		errorVal = GetLastError();
		if(errorVal == ERROR_IO_PENDING) {

			wait_status = WaitForSingleObject(overlapped.hEvent, 1000);

			if(wait_status == WAIT_OBJECT_0) {
				ret = GetOverlappedResult((HANDLE)handle, &overlapped, &num_of_bytes_read, FALSE);
				CloseHandle(overlapped.hEvent);

				if(ret > 0) {
					/* return data read from serial port if exist */
					if(num_of_bytes_read > 0) {
						data_read = (*env)->NewByteArray(env, num_of_bytes_read);
						(*env)->SetByteArrayRegion(env, data_read, 0, num_of_bytes_read, data_buf);
						return data_read;
					}
					return NULL;
				}else if(ret == 0) {
					errorVal = GetLastError();
					if((errorVal == ERROR_HANDLE_EOF) || (errorVal == ERROR_IO_INCOMPLETE)) {
						return NULL;
					}else {
						/* This case indicates error. */
						throw_serialcom_exception(env, 4, errorVal, NULL);
						return NULL;
					}
				}else {
				}
			}else if(wait_status == WAIT_FAILED) {
				/* This case indicates error. */
				throw_serialcom_exception(env, 4, GetLastError(), NULL);
				CloseHandle(overlapped.hEvent);
				return NULL;
			}else if(wait_status == WAIT_TIMEOUT) {
				CloseHandle(overlapped.hEvent);
				return NULL;
			}else {
				CloseHandle(overlapped.hEvent);
				return NULL;
			}
		}else {
			/* This case indicates error. */
			CloseHandle(overlapped.hEvent);
			throw_serialcom_exception(env, 4, errorVal, NULL);
			return NULL;
		}
	}else if(ret > 0) {
		CloseHandle(overlapped.hEvent);
		if(num_of_bytes_read > 0) {
			/* This indicates we got success and have read data in first go itself. */
			data_read = (*env)->NewByteArray(env, num_of_bytes_read);
			(*env)->SetByteArrayRegion(env, data_read, 0, num_of_bytes_read, data_buf);
			return data_read;
		}else {
			return NULL;
		}

	}else {
	}

	CloseHandle(overlapped.hEvent);
	return NULL;
}

/*
 * Method:    readBytesBlocking
 * Signature: (JIJ)[B
 *
 * Blocks on :
 * (1) serial port file descriptor to become available for reading
 * (2) event file descriptor that is used to come out of blocking state
 *
 * @return data read after coming out of blocking state or NULL if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
 JNIEXPORT jbyteArray JNICALL Java_com_embeddedunveiled_serial_internal_SerialComPortJNIBridge_readBytesBlocking(JNIEnv *env,
	 jobject obj, jlong handle, jint count, jlong context) {
	
	int ret = 0;
	DWORD errorVal;
	jbyte data_buf[2 * 1024];
	DWORD num_of_bytes_read;
	OVERLAPPED overlapped;
	OVERLAPPED ovRead;
	jbyteArray data_read;
	DWORD events_mask = 0;
	DWORD wait_status = 0;
	int loop = 1;
	DWORD errors;
	COMSTAT comstat;
	HANDLE wait_event_handles[2];

	while (loop != -1) {
		memset(&overlapped, 0, sizeof(overlapped));
		overlapped.hEvent = CreateEvent(NULL, FALSE, FALSE, NULL);
		if(overlapped.hEvent == NULL) {
			throw_serialcom_exception(env, 4, GetLastError(), NULL);
			return NULL;
		}

		wait_event_handles[0] = ((OVERLAPPED *)context)->hEvent;
		wait_event_handles[1] = overlapped.hEvent;

		events_mask = 0;
		ResetEvent(overlapped.hEvent);
		ret = WaitCommEvent((HANDLE)handle, &events_mask, &overlapped);

		if(ret == 0) {

			errorVal = GetLastError();
			if (errorVal == ERROR_IO_PENDING) {

				wait_status = WaitForMultipleObjects(2, wait_event_handles, FALSE, INFINITE);

				/* check if read operation is un-blocked through another thread */
				switch (wait_status) {
					case WAIT_OBJECT_0 + 0:
						/* come out of blocking state */
						CancelIo((HANDLE)handle);
						GetOverlappedResult((HANDLE)handle, &overlapped, &num_of_bytes_read, TRUE);
						CloseHandle(overlapped.hEvent);
						throw_serialcom_exception(env, 3, 0, E_UNBLOCKIO);
						return NULL;
				}

				ClearCommError((HANDLE)handle, &errors, &comstat);
				if(comstat.cbInQue == 0) {
					CancelIo((HANDLE)handle);
					GetOverlappedResult((HANDLE)handle, &overlapped, &num_of_bytes_read, TRUE);
					CloseHandle(overlapped.hEvent);
					continue;
				}

				/* check if there is data to read */
				switch (wait_status) {
					case WAIT_OBJECT_0 + 1:
						CancelIo((HANDLE)handle);
						GetOverlappedResult((HANDLE)handle, &overlapped, &num_of_bytes_read, TRUE);
						CloseHandle(overlapped.hEvent);
						if(!(events_mask & EV_RXCHAR)) {
							/* loop back for events other than data character */
							continue;
						}
						loop = -1;
						break;
					case WAIT_TIMEOUT :
					case WAIT_FAILED :
					default :
						CancelIo((HANDLE)handle);
						GetOverlappedResult((HANDLE)handle, &overlapped, &num_of_bytes_read, TRUE);
						CloseHandle(overlapped.hEvent);
						throw_serialcom_exception(env, 4, GetLastError(), NULL);
						return NULL;
				}
			}
		}else {
			ClearCommError((HANDLE)handle, &errors, &comstat);
			CancelIo((HANDLE)handle);
			GetOverlappedResult((HANDLE)handle, &overlapped, &num_of_bytes_read, TRUE);
			CloseHandle(overlapped.hEvent);
			if(comstat.cbInQue == 0) {
				continue;
			}
			loop = -1;
		}
	}

	memset(&ovRead, 0, sizeof(ovRead));
	ovRead.hEvent = CreateEvent(NULL, FALSE, FALSE, NULL);
	if(ovRead.hEvent == NULL) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return NULL;
	}

	/* ReadFile resets the event to a nonsignaled state when it begins the I/O operation. */
	ret = ReadFile((HANDLE)handle, data_buf, count, &num_of_bytes_read, &ovRead);

	if(ret == 0) {
		errorVal = GetLastError();
		if (errorVal == ERROR_IO_PENDING) {

			wait_status = WaitForSingleObject(ovRead.hEvent, 1000);

			if(wait_status == WAIT_OBJECT_0) {
				ret = GetOverlappedResult((HANDLE)handle, &ovRead, &num_of_bytes_read, TRUE);
				if(ret > 0) {
					/* return data read from serial port */
					data_read = (*env)->NewByteArray(env, num_of_bytes_read);
					(*env)->SetByteArrayRegion(env, data_read, 0, num_of_bytes_read, data_buf);
					CloseHandle(ovRead.hEvent);
					return data_read;
				}else if(ret == 0) {
					errorVal = GetLastError();
					if((errorVal == ERROR_HANDLE_EOF) || (errorVal == ERROR_IO_INCOMPLETE)) {
						return NULL;
					}else {
						/* This case indicates error. */
						CancelIo((HANDLE)handle);
						GetOverlappedResult((HANDLE)handle, &ovRead, &num_of_bytes_read, TRUE);
						CloseHandle(ovRead.hEvent);
						throw_serialcom_exception(env, 4, errorVal, NULL);
						return NULL;
					}
				}else {
				}
			}else if(wait_status == WAIT_FAILED) {
				/* This case indicates error. */
				CancelIo((HANDLE)handle);
				GetOverlappedResult((HANDLE)handle, &ovRead, &num_of_bytes_read, TRUE);
				CloseHandle(ovRead.hEvent);
				throw_serialcom_exception(env, 4, GetLastError(), NULL);
				return NULL;
			}else if(wait_status == WAIT_TIMEOUT) {
			}else {
			}
		}else {
			/* This case indicates error. */
			CancelIo((HANDLE)handle);
			GetOverlappedResult((HANDLE)handle, &ovRead, &num_of_bytes_read, TRUE);
			CloseHandle(ovRead.hEvent);
			throw_serialcom_exception(env, 4, GetLastError(), NULL);
			return NULL;
		}
	}else if(ret > 0) {
		/* This indicates we got success and have read data in first go itself. */
		data_read = (*env)->NewByteArray(env, num_of_bytes_read);
		(*env)->SetByteArrayRegion(env, data_read, 0, num_of_bytes_read, data_buf);
		CloseHandle(ovRead.hEvent);
		return data_read;
	}else {
	}

	CloseHandle(ovRead.hEvent);
	return NULL;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComPortJNIBridge
 * Method:    readBytesDirect
 * Signature: (JLjava/nio/ByteBuffer;II)I
 *
 * It does not modify the direct byte buffer attributes position, capacity, limit and mark. The
 * application design is expected to take care of this as and when required in appropriate manner.
 *
 * @return number of bytes read from serial port, 0 if there was no data in serial port buffer, -1 
  *        if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComPortJNIBridge_readBytesDirect(JNIEnv *env,
		jobject obj, jlong handle, jobject buffer, jint offset, jint length) {
	return -1;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComPortJNIBridge
 * Method:    writeSingleByte
 * Signature: (JB)I
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComPortJNIBridge_writeSingleByte(JNIEnv *env,
		jobject obj, jlong handle, jbyte dataByte) {
		
	int ret = 0;
	BOOL result = FALSE;
	DWORD errorVal = 0;
	DWORD num_of_bytes_written = 0;
	OVERLAPPED ovWrite;

	memset(&ovWrite, 0, sizeof(ovWrite));
	ovWrite.hEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
	if (ovWrite.hEvent == NULL) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}

	result = WriteFile((HANDLE)handle, &dataByte, 1, &num_of_bytes_written, &ovWrite);
	if(result == FALSE) {
		errorVal = GetLastError();
		if(errorVal == ERROR_IO_PENDING) {
			if(WaitForSingleObject(ovWrite.hEvent, 1000) == WAIT_OBJECT_0) {
				ret = GetOverlappedResult((HANDLE)handle, &ovWrite, &num_of_bytes_written, TRUE);
				if(ret == 0) {
					CloseHandle(ovWrite.hEvent);
					throw_serialcom_exception(env, 4, GetLastError(), NULL);
					return -1;
				}else {
					// success, so flush all data out of serial port
					FlushFileBuffers((HANDLE)handle);
				}
			}
		}else {
			CloseHandle(ovWrite.hEvent);
			throw_serialcom_exception(env, 4, errorVal, NULL);
			return -1;
		}
	}else {
		FlushFileBuffers((HANDLE)handle);
	}

	CloseHandle(ovWrite.hEvent);
	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComPortJNIBridge
 * Method:    writeBytes
 * Signature: (J[BI)I
 *
 * Try writing all data using a loop by handling partial writes. tcdrain() waits until all output written
 * to the object referred to by fd has been transmitted. This is used to make sure that data gets sent out
 * of the serial port physically before write returns.
 *
 * If the number of bytes to be written is 0, then behavior is undefined as per POSIX standard. Therefore
 * we do not allow dummy writes with absolutely no data at all and this is handled at java layer. This
 * function does not block any signals.
 *
 * To segregate issues with buffer size or handling from device or driver specific implementation consider
 * using pseudo terminals (/dev/pts/1). If this works then check termios structure settings for real device.
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComPortJNIBridge_writeBytes(JNIEnv *env,
		jobject obj, jlong handle, jbyteArray buffer, jint delay) {
	
	int ret = 0;
	int status = 0;
	int index = 0;
	BOOL result = FALSE;
	DWORD errorVal = 0;
	OVERLAPPED ovWrite;
	DWORD num_of_bytes_written = 0;

	jbyte* data_buf = (*env)->GetByteArrayElements(env, buffer, JNI_FALSE);
	DWORD num_bytes_to_write = (*env)->GetArrayLength(env, buffer);

	/* Only hEvent member need to be initialled and others can be left 0. */
	memset(&ovWrite, 0, sizeof(ovWrite));
	ovWrite.hEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
	if(ovWrite.hEvent == NULL) {
		(*env)->ReleaseByteArrayElements(env, buffer, data_buf, 0);
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}

	if(delay == 0) {
		/* no delay between successive bytes sent */
		result = WriteFile((HANDLE)handle, data_buf, num_bytes_to_write, &num_of_bytes_written, &ovWrite);
		if(result == FALSE) {
			errorVal = GetLastError();
			if(errorVal == ERROR_IO_PENDING) {
				if(WaitForSingleObject(ovWrite.hEvent, 1000) == WAIT_OBJECT_0) {
					ret = GetOverlappedResult((HANDLE)handle, &ovWrite, &num_of_bytes_written, TRUE);
					if(ret == 0) {
						(*env)->ReleaseByteArrayElements(env, buffer, data_buf, 0);
						CloseHandle(ovWrite.hEvent);
						throw_serialcom_exception(env, 4, GetLastError(), NULL);
						return -1;
					}else {
						/* success, so flush all data out of serial port */
						FlushFileBuffers((HANDLE)handle);
					}
				}
			}else {
				(*env)->ReleaseByteArrayElements(env, buffer, data_buf, 0);
				CloseHandle(ovWrite.hEvent);
				throw_serialcom_exception(env, 4, errorVal, NULL);
				return -1;
			}
		}else {
			/* success in one shot, so flush all data out of serial port */
			FlushFileBuffers((HANDLE)handle);
		}
	}else {
		/* delay between successive bytes sent */
		while (num_bytes_to_write > 0) {
			result = WriteFile((HANDLE)handle, &data_buf[index], 1, &num_of_bytes_written, &ovWrite);
			if(result == FALSE) {
				errorVal = GetLastError();
				if(errorVal == ERROR_IO_PENDING) {
					if(WaitForSingleObject(ovWrite.hEvent, 1000) == WAIT_OBJECT_0) {
						ret = GetOverlappedResult((HANDLE)handle, &ovWrite, &num_of_bytes_written, TRUE);
						if(ret == 0) {
							(*env)->ReleaseByteArrayElements(env, buffer, data_buf, 0);
							CloseHandle(ovWrite.hEvent);
							throw_serialcom_exception(env, 4, GetLastError(), NULL);
							return -1;
						}
					}
				}else {
					(*env)->ReleaseByteArrayElements(env, buffer, data_buf, 0);
					CloseHandle(ovWrite.hEvent);
					throw_serialcom_exception(env, 4, errorVal, NULL);
					return -1;
				}
			}
			num_bytes_to_write -= num_of_bytes_written;
			index = index + num_of_bytes_written;
			FlushFileBuffers((HANDLE)handle);
			serial_delay(delay);
		}
	}

	(*env)->ReleaseByteArrayElements(env, buffer, data_buf, 0);
	CloseHandle(ovWrite.hEvent);
	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComPortJNIBridge
 * Method:    writeBytesDirect
 * Signature: (JLjava/nio/ByteBuffer;II)I
 *
 * Sends data bytes from Java NIO direct byte buffer out of serial port from the given position upto
 * length number of bytes in chunks of 1024 bytes. This function handles partial write scenario for write 
 * operations.
 *
 * It does not modify the direct byte buffer attributes position, capacity, limit and mark. The application
 * design is expected to take care of this as and when required in appropriate manner in Java layer itself. 
 * Also it does not consume or modify the data in the given buffer.
 *
 * @return number of bytes written to serial port on success otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComPortJNIBridge_writeBytesDirect(JNIEnv *env,
		jobject obj, jlong handle, jobject buffer, jint offset, jint length) {

	int i = 0;
	int ret = 0;
	BOOL result = FALSE;
	int index = 0;
	DWORD num_of_bytes_written = 0;
	DWORD num_bytes_to_write = 0;
	int total_bytes_written = 0;
	int remaining_bytes = 0;
	OVERLAPPED ovWrite;
	jbyte* data_buf = NULL;
	DWORD errorVal = 0;

	/* get base address of this direct byte buffer */
	data_buf = (jbyte *)(*env)->GetDirectBufferAddress(env, buffer);
	if ((data_buf == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_GETDIRCTBUFADDRSTR);
		return -1;
	}

	/* Only hEvent member need to be initialled and others can be left 0. */
	memset(&ovWrite, 0, sizeof(ovWrite));
	ovWrite.hEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
	if (ovWrite.hEvent == NULL) {
		(*env)->ReleaseByteArrayElements(env, buffer, data_buf, 0);
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}

	/* initial settings for WriteFile() */
	index = offset;
	if (length < 1024) {
		num_bytes_to_write = length;
	}else {
		num_bytes_to_write = 1024;
	}

	while (total_bytes_written < length) {
		result = WriteFile((HANDLE)handle, &data_buf[index], num_bytes_to_write, &num_of_bytes_written, &ovWrite);
		if (result == FALSE) {
			errorVal = GetLastError();
			if (errorVal == ERROR_IO_PENDING) {
				if (WaitForSingleObject(ovWrite.hEvent, 1000) == WAIT_OBJECT_0) {
					ret = GetOverlappedResult((HANDLE)handle, &ovWrite, &num_of_bytes_written, TRUE);
					if (ret == 0) {
						(*env)->ReleaseByteArrayElements(env, buffer, data_buf, 0);
						CloseHandle(ovWrite.hEvent);
						throw_serialcom_exception(env, 4, GetLastError(), NULL);
						return -1;
					}
					/* success, sent data out of serial port physically */
					FlushFileBuffers((HANDLE)handle);
				}
			}else {
				(*env)->ReleaseByteArrayElements(env, buffer, data_buf, 0);
				CloseHandle(ovWrite.hEvent);
				throw_serialcom_exception(env, 4, errorVal, NULL);
				return -1;
			}
		}else {
			/* success in one shot, sent data out of serial port physically */
			FlushFileBuffers((HANDLE)handle);
		}

		total_bytes_written = total_bytes_written + num_of_bytes_written;
		index = index + num_of_bytes_written;
		remaining_bytes = length - total_bytes_written;
		if (remaining_bytes >= 1024) {
			num_bytes_to_write = 1024;
		}else {
			num_bytes_to_write = remaining_bytes;
		}
	}

	(*env)->ReleaseByteArrayElements(env, buffer, data_buf, 0);
	CloseHandle(ovWrite.hEvent);
	return total_bytes_written;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComPortJNIBridge
 * Method:    createBlockingIOContext
 * Signature: ()J
 *
 * This will create event object/file descriptor that will be used to wait upon in addition to
 * serial port file descriptor, so as to bring blocked read call out of waiting state. This is needed
 * if application is willing to close the serial port but unable because a blocked reader exist.
 *
 * @return context on success otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jlong JNICALL Java_com_embeddedunveiled_serial_internal_SerialComPortJNIBridge_createBlockingIOContext(JNIEnv *env,
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
 * Class:     com_embeddedunveiled_serial_internal_SerialComPortJNIBridge
 * Method:    unblockBlockingIOOperation
 * Signature: (J)I
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComPortJNIBridge_unblockBlockingIOOperation(JNIEnv *env, 
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
 * Class:     com_embeddedunveiled_serial_internal_SerialComPortJNIBridge
 * Method:    destroyBlockingIOContext
 * Signature: (J)I
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComPortJNIBridge_destroyBlockingIOContext(JNIEnv *env,
	jobject obj, jlong context) {

	OVERLAPPED *overlapped = (OVERLAPPED *) context;
	CloseHandle((*overlapped).hEvent);
	free(overlapped);
	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComPortJNIBridge
 * Method:    configureComPortData
 * Signature: (JIIIII)I
 *
 * Configures format of data that will be exchanged through serial port electrically.
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComPortJNIBridge_configureComPortData(JNIEnv *env,
		jobject obj, jlong handle, jint dataBits, jint stopBits, jint parity, jint baudRateTranslated,
		jint custBaudTranslated) {

	int ret = 0;
	DWORD baud = -1;
	DCB dcb = { 0 };

	SecureZeroMemory(&dcb, sizeof(DCB));
	dcb.DCBlength = sizeof(DCB);
	ret = GetCommState((HANDLE)handle, &dcb);
	if(ret == 0) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}

	if(baudRateTranslated == 251) {
		baud = custBaudTranslated;
	}else {
		/* Baudrate support depends upon operating system, driver and chipset used. */
		switch (baudRateTranslated) {
			case 0: baud = 0;
				break;
			case 50: baud = 50;
				break;
			case 75: baud = 75;
				break;
			case 110: baud = CBR_110;
				break;
			case 134: baud = 134;
				break;
			case 150: baud = 150;
				break;
			case 200: baud = 200;
				break;
			case 300: baud = CBR_300;
				break;
			case 600: baud = CBR_600;
				break;
			case 1200: baud = CBR_1200;
				break;
			case 1800: baud = 1800;
				break;
			case 2400: baud = CBR_2400;
				break;
			case 4800: baud = CBR_4800;
				break;
			case 9600: baud = CBR_9600;
				break;
			case 14400: baud = CBR_14400;
				break;
			case 19200: baud = CBR_19200;
				break;
			case 28800: baud = 28800;
				break;
			case 38400: baud = CBR_38400;
				break;
			case 56000: baud = 56000;
				break;
			case 57600: baud = 57600;
				break;
			case 115200: baud = 115200;
				break;
			case 128000: baud = 128000;
				break;
			case 153600: baud = 153600;
				break;
			case 230400: baud = 230400;
				break;
			case 256000: baud = 256000;
				break;
			case 460800: baud = 460800;
				break;
			case 500000: baud = 500000;
				break;
			case 576000: baud = 576000;
				break;
			case 921600: baud = 921600;
				break;
			case 1000000: baud = 1000000;
				break;
			case 1152000: baud = 1152000;
				break;
			case 1500000: baud = 1500000;
				break;
			case 2000000: baud = 2000000;
				break;
			case 2500000: baud = 2500000;
				break;
			case 3000000: baud = 3000000;
				break;
			case 3500000: baud = 3500000;
				break;
			case 4000000: baud = 4000000;
				break;
			default: baud = -1;
				break;
		}
	}
	/* Set baud rate after appropriate manipulation has been done. */
	dcb.BaudRate = baud;

	/* Set data bits. This Specifies the bits per data byte transmitted and received. */
	dcb.ByteSize = (BYTE)dataBits;

	/* Set stop bits. */
	if(stopBits == 1) {
		dcb.StopBits = ONESTOPBIT;
	}else if(stopBits == 4) {
		dcb.StopBits = ONE5STOPBITS;
	}else if (stopBits == 2) {
		dcb.StopBits = TWOSTOPBITS;
	}else {
	}

	/* Set parity */
	dcb.fParity = TRUE;
	if(parity == 1) {
		dcb.fParity = FALSE;
		dcb.Parity = NOPARITY;
	}else if (parity == 2) {
		dcb.Parity = ODDPARITY;
	}else if (parity == 3) {
		dcb.Parity = EVENPARITY;
	}else if (parity == 4) {
		dcb.Parity = MARKPARITY;
	}else if (parity == 5) {
		dcb.Parity = SPACEPARITY;
	}else {
	}

	ret = SetCommState((HANDLE)handle, &dcb);
	if(ret == 0) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComPortJNIBridge
 * Method:    configureComPortControl
 * Signature: (JIBBZZ)I
 *
 * Defines how the data communication through serial port will be controlled.
 *
 * For software flow control; IXON, IXOFF, and IXANY are used . If IXOFF is set, then software 
 * flow control is enabled on the TTY's input queue. The TTY transmits a STOP character when the 
 * program cannot keep up with its input queue and transmits a START character when its input queue 
 * in nearly empty again. If IXON is set, software flow control is enabled on the TTY's output queue. 
 * The TTY blocks writes by the program when the device to which it is connected cannot keep up with 
 * it. If IXANY is set, then any character received by the TTY from the device restarts the output 
 * that has been suspended.
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComPortJNIBridge_configureComPortControl(JNIEnv *env,
		jobject obj, jlong handle, jint flowctrl, jbyte xon, jbyte xoff, 
		jboolean ParFraError, jboolean overFlowErr) {
		
	int ret = 0;
	DWORD baud = -1;
	HANDLE hComm = (HANDLE)handle;
	DCB dcb = { 0 };

	FillMemory(&dcb, sizeof(dcb), 0);
	dcb.DCBlength = sizeof(DCB);
	ret = GetCommState(hComm, &dcb);
	if(ret == 0) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}

	/* Set flow control. */
	dcb.fDtrControl = DTR_CONTROL_ENABLE;
	dcb.fRtsControl = RTS_CONTROL_ENABLE;
	if(flowctrl == 1) {                          /* No flow control. */
		dcb.fOutX = FALSE;
		dcb.fInX = FALSE;
		dcb.fOutxCtsFlow = FALSE;
		dcb.fOutxDsrFlow = FALSE;
		dcb.fDsrSensitivity = FALSE;
		dcb.XonChar = 0x00;
		dcb.XoffChar = 0x00;
		dcb.XonLim = 0x00;
		dcb.XoffLim = 0x00;
	}else if(flowctrl == 2) {                    /* Hardware flow control. */
		dcb.fOutX = FALSE;
		dcb.fInX = FALSE;
		dcb.fOutxCtsFlow = TRUE;
		dcb.fOutxDsrFlow = TRUE;
		dcb.fDsrSensitivity = TRUE;
	}else if(flowctrl == 3) {                    /* Software flow control. */
		dcb.fOutX = TRUE;
		dcb.fInX = TRUE;
		dcb.fOutxCtsFlow = FALSE;
		dcb.fOutxDsrFlow = FALSE;
		dcb.fDsrSensitivity = FALSE;
		dcb.XonChar = (CHAR) xon;
		dcb.XoffChar = (CHAR) xoff;
		dcb.XonLim = 2048;
		dcb.XoffLim = 2048;
	}else {
	}

	ret = SetCommState(hComm, &dcb);
	if(ret == 0) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}
	
	/* Abort outstanding I/O operations, clear port's I/O buffer (flush old garbage values). */
	PurgeComm(hComm, PURGE_RXABORT | PURGE_RXCLEAR | PURGE_TXABORT | PURGE_TXCLEAR);

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComPortJNIBridge
 * Method:    getCurrentConfigurationU
 * Signature: (J)[I
 *
 * Applicable for UNIX like OS only.
 * 
 * @return NULL always.
 */
JNIEXPORT jintArray JNICALL Java_com_embeddedunveiled_serial_internal_SerialComPortJNIBridge_getCurrentConfigurationU(JNIEnv *env, 
	jobject obj, jlong handle) {
	return NULL;
}

/* clean up and throw exception if function to get current serial port configuration fails. */
jobject getCurrentConfigurationW_clean_exp(JNIEnv *env, int task, const char *expmsg, struct jstrarray_list *list) {
	(*env)->ExceptionClear(env);
	free_jstrarraylist(list);

	jclass serialComExceptionClass = (*env)->FindClass(env, SCOMEXPCLASS);
	if ((serialComExceptionClass == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		(*env)->ExceptionClear(env);
		if (task == 1) {
			LOGE(E_FINDCLASSSCOMEXPSTR, E_NEWSTRUTFSTR);
		}else {
			LOGE(E_FINDCLASSSCOMEXPSTR, expmsg);
		}
		return NULL;
	}

	if (task == 1) {
		(*env)->ThrowNew(env, serialComExceptionClass, E_NEWSTRUTFSTR);
	}else {
		(*env)->ThrowNew(env, serialComExceptionClass, expmsg);
	}
	return NULL;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComPortJNIBridge
 * Method:    getCurrentConfigurationW
 * Signature: (J)[Ljava/lang/String;
 *
 * @return serial port configuration array constructed out of termios structure on success otherwise
 *         NULL if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jobjectArray JNICALL Java_com_embeddedunveiled_serial_internal_SerialComPortJNIBridge_getCurrentConfigurationW(JNIEnv *env,
	jobject obj, jlong handle) {
	
	DCB dcb;
	int ret = 0;
	int x = 0;
	char buffer[128];
	struct jstrarray_list list = { 0 };
	jstring info;
	jclass strClass = NULL;
	jobjectArray currentConfigurationRead = NULL;

	init_jstrarraylist(&list, 35);

	FillMemory(&dcb, sizeof(dcb), 0);
	dcb.DCBlength = sizeof(DCB);
	ret = GetCommState((HANDLE)handle, &dcb);
	if(ret == 0) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return NULL;
	}

	_snprintf_s(buffer, 128, 128, "DCBlength : %lu\0", dcb.DCBlength);
	info = (*env)->NewStringUTF(env, buffer);
	if ((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		return getCurrentConfigurationW_clean_exp(env, 1, 0, &list);
	}
	insert_jstrarraylist(&list, info);

	_snprintf_s(buffer, 128, 128, "BaudRate : %lu\0", dcb.BaudRate);
	info = (*env)->NewStringUTF(env, buffer);
	if ((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		return getCurrentConfigurationW_clean_exp(env, 1, 0, &list);
	}
	insert_jstrarraylist(&list, info);

	if (dcb.fBinary == TRUE) {
		_snprintf_s(buffer, 128, 128, "fBinary : TRUE\0");
	}else {
		_snprintf_s(buffer, 128, 128, "fBinary : FALSE\0");
	}
	info = (*env)->NewStringUTF(env, buffer);
	if ((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		return getCurrentConfigurationW_clean_exp(env, 1, 0, &list);
	}
	insert_jstrarraylist(&list, info);

	if (dcb.fParity == TRUE) {
		_snprintf_s(buffer, 128, 128, "fParity : TRUE\0");
	}else {
		_snprintf_s(buffer, 128, 128, "fParity : FALSE\0");
	}
	info = (*env)->NewStringUTF(env, buffer);
	if ((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		return getCurrentConfigurationW_clean_exp(env, 1, 0, &list);
	}
	insert_jstrarraylist(&list, info);

	if (dcb.fOutxCtsFlow == TRUE) {
		_snprintf_s(buffer, 128, 128, "fOutxCtsFlow : TRUE\0");
	}else {
		_snprintf_s(buffer, 128, 128, "fOutxCtsFlow : FALSE\0");
	}
	info = (*env)->NewStringUTF(env, buffer);
	if ((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		return getCurrentConfigurationW_clean_exp(env, 1, 0, &list);
	}
	insert_jstrarraylist(&list, info);

	if (dcb.fOutxDsrFlow == TRUE) {
		_snprintf_s(buffer, 128, 128, "fOutxDsrFlow : TRUE\0");
	}else {
		_snprintf_s(buffer, 128, 128, "fOutxDsrFlow : FALSE\0");
	}
	info = (*env)->NewStringUTF(env, buffer);
	if ((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		return getCurrentConfigurationW_clean_exp(env, 1, 0, &list);
	}
	insert_jstrarraylist(&list, info);

	if(dcb.fDtrControl == DTR_CONTROL_DISABLE) {
		_snprintf_s(buffer, 128, 128, "fDtrControl : DTR_CONTROL_DISABLE\0");
	}else if(dcb.fDtrControl == DTR_CONTROL_ENABLE) {
		_snprintf_s(buffer, 128, 128, "fDtrControl : DTR_CONTROL_ENABLE\0");
	}else if(dcb.fDtrControl == DTR_CONTROL_HANDSHAKE) {
		_snprintf_s(buffer, 128, 128, "fDtrControl : DTR_CONTROL_HANDSHAKE\0");
	}else {
	}
	info = (*env)->NewStringUTF(env, buffer);
	if ((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		return getCurrentConfigurationW_clean_exp(env, 1, 0, &list);
	}
	insert_jstrarraylist(&list, info);

	if (dcb.fDsrSensitivity == TRUE) {
		_snprintf_s(buffer, 128, 128, "fDsrSensitivity : TRUE\0");
	}else {
		_snprintf_s(buffer, 128, 128, "fDsrSensitivity : FALSE\0");
	}
	info = (*env)->NewStringUTF(env, buffer);
	if ((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		return getCurrentConfigurationW_clean_exp(env, 1, 0, &list);
	}
	insert_jstrarraylist(&list, info);

	if (dcb.fTXContinueOnXoff == TRUE) {
		_snprintf_s(buffer, 128, 128, "fTXContinueOnXoff : TRUE\0");
	}else {
		_snprintf_s(buffer, 128, 128, "fTXContinueOnXoff : FALSE\0");
	}
	info = (*env)->NewStringUTF(env, buffer);
	if ((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		return getCurrentConfigurationW_clean_exp(env, 1, 0, &list);
	}
	insert_jstrarraylist(&list, info);

	if (dcb.fOutX == TRUE) {
		_snprintf_s(buffer, 128, 128, "fOutX : TRUE\0");
	}else {
		_snprintf_s(buffer, 128, 128, "fOutX : FALSE\0");
	}
	info = (*env)->NewStringUTF(env, buffer);
	if ((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		return getCurrentConfigurationW_clean_exp(env, 1, 0, &list);
	}
	insert_jstrarraylist(&list, info);

	if (dcb.fInX == TRUE) {
		_snprintf_s(buffer, 128, 128, "fInX : TRUE\0");
	}else {
		_snprintf_s(buffer, 128, 128, "fInX : FALSE\0");
	}
	info = (*env)->NewStringUTF(env, buffer);
	if ((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		return getCurrentConfigurationW_clean_exp(env, 1, 0, &list);
	}
	insert_jstrarraylist(&list, info);

	if (dcb.fErrorChar == TRUE) {
		_snprintf_s(buffer, 128, 128, "fErrorChar : TRUE\0");
	}else {
		_snprintf_s(buffer, 128, 128, "fErrorChar : FALSE\0");
	}
	info = (*env)->NewStringUTF(env, buffer);
	if ((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		return getCurrentConfigurationW_clean_exp(env, 1, 0, &list);
	}
	insert_jstrarraylist(&list, info);

	if (dcb.fNull == TRUE) {
		_snprintf_s(buffer, 128, 128, "fNull : TRUE\0");
	}else {
		_snprintf_s(buffer, 128, 128, "fNull : FALSE\0");
	}
	info = (*env)->NewStringUTF(env, buffer);
	if ((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		return getCurrentConfigurationW_clean_exp(env, 1, 0, &list);
	}
	insert_jstrarraylist(&list, info);

	if (dcb.fRtsControl == RTS_CONTROL_DISABLE) {
		_snprintf_s(buffer, 128, 128, "fRtsControl : RTS_CONTROL_DISABLE\0");
	}else if (dcb.fRtsControl == RTS_CONTROL_ENABLE) {
		_snprintf_s(buffer, 128, 128, "fRtsControl : RTS_CONTROL_ENABLE\0");
	}else if (dcb.fRtsControl == RTS_CONTROL_HANDSHAKE) {
		_snprintf_s(buffer, 128, 128, "fRtsControl : RTS_CONTROL_HANDSHAKE\0");
	}else if (dcb.fRtsControl == RTS_CONTROL_TOGGLE) {
		_snprintf_s(buffer, 128, 128, "fRtsControl : RTS_CONTROL_TOGGLE\0");
	}else {
	}
	info = (*env)->NewStringUTF(env, buffer);
	if ((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		return getCurrentConfigurationW_clean_exp(env, 1, 0, &list);
	}
	insert_jstrarraylist(&list, info);

	if (dcb.fAbortOnError == TRUE) {
		_snprintf_s(buffer, 128, 128, "fAbortOnError : TRUE\0");
	}else {
		_snprintf_s(buffer, 128, 128, "fAbortOnError : FALSE\0");
	}
	info = (*env)->NewStringUTF(env, buffer);
	if ((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		return getCurrentConfigurationW_clean_exp(env, 1, 0, &list);
	}
	insert_jstrarraylist(&list, info);

	info = (*env)->NewStringUTF(env, "fDummy2 : NA");
	if ((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		return getCurrentConfigurationW_clean_exp(env, 1, 0, &list);
	}
	insert_jstrarraylist(&list, info);

	info = (*env)->NewStringUTF(env, "wReserved : NA");
	if ((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		return getCurrentConfigurationW_clean_exp(env, 1, 0, &list);
	}
	insert_jstrarraylist(&list, info);

	_snprintf_s(buffer, 128, 128, "XonLim : %lu\0", dcb.XonLim);
	info = (*env)->NewStringUTF(env, buffer);
	if ((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		return getCurrentConfigurationW_clean_exp(env, 1, 0, &list);
	}
	insert_jstrarraylist(&list, info);

	_snprintf_s(buffer, 128, 128, "XoffLim : %lu\0", dcb.XoffLim);
	info = (*env)->NewStringUTF(env, buffer);
	if ((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		return getCurrentConfigurationW_clean_exp(env, 1, 0, &list);
	}
	insert_jstrarraylist(&list, info);

	_snprintf_s(buffer, 128, 128, "ByteSize : %lu\0", dcb.ByteSize);
	info = (*env)->NewStringUTF(env, buffer);
	if ((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		return getCurrentConfigurationW_clean_exp(env, 1, 0, &list);
	}
	insert_jstrarraylist(&list, info);

	_snprintf_s(buffer, 128, 128, "Parity : %lu\0", dcb.Parity);
	info = (*env)->NewStringUTF(env, buffer);
	if ((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		return getCurrentConfigurationW_clean_exp(env, 1, 0, &list);
	}
	insert_jstrarraylist(&list, info);

	_snprintf_s(buffer, 128, 128, "StopBits : %lu\0", dcb.StopBits);
	info = (*env)->NewStringUTF(env, buffer);
	if ((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		return getCurrentConfigurationW_clean_exp(env, 1, 0, &list);
	}
	insert_jstrarraylist(&list, info);

	_snprintf_s(buffer, 128, 128, "XonChar : %c\0", dcb.XonChar);
	info = (*env)->NewStringUTF(env, buffer);
	if ((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		return getCurrentConfigurationW_clean_exp(env, 1, 0, &list);
	}
	insert_jstrarraylist(&list, info);

	_snprintf_s(buffer, 128, 128, "XoffChar : %c\0", dcb.XoffChar);
	info = (*env)->NewStringUTF(env, buffer);
	if ((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		return getCurrentConfigurationW_clean_exp(env, 1, 0, &list);
	}
	insert_jstrarraylist(&list, info);

	_snprintf_s(buffer, 128, 128, "ErrorChar : %c\0", dcb.ErrorChar);
	info = (*env)->NewStringUTF(env, buffer);
	if ((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		return getCurrentConfigurationW_clean_exp(env, 1, 0, &list);
	}
	insert_jstrarraylist(&list, info);

	_snprintf_s(buffer, 128, 128, "EofChar : %c\0", (char)dcb.EofChar);
	info = (*env)->NewStringUTF(env, buffer);
	if ((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		return getCurrentConfigurationW_clean_exp(env, 1, 0, &list);
	}
	insert_jstrarraylist(&list, info);

	_snprintf_s(buffer, 128, 128, "EvtChar : %c\0", dcb.EvtChar);
	info = (*env)->NewStringUTF(env, buffer);
	if ((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		return getCurrentConfigurationW_clean_exp(env, 1, 0, &list);
	}
	insert_jstrarraylist(&list, info);

	info = (*env)->NewStringUTF(env, "wReserved1 : NA");
	if ((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		return getCurrentConfigurationW_clean_exp(env, 1, 0, &list);
	}
	insert_jstrarraylist(&list, info);

	/* Create a JAVA/JNI style array of String object, populate it and return to java layer. */
	strClass = (*env)->FindClass(env, JAVALSTRING);
	if ((strClass == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		return getCurrentConfigurationW_clean_exp(env, 2, E_FINDCLASSSSTRINGSTR, &list);
	}

	currentConfigurationRead = (*env)->NewObjectArray(env, (jsize)list.index, strClass, NULL);
	if ((currentConfigurationRead == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		return getCurrentConfigurationW_clean_exp(env, 2, E_NEWOBJECTARRAYSTR, &list);
	}

	for (x = 0; x < list.index; x++) {
		(*env)->SetObjectArrayElement(env, currentConfigurationRead, x, list.base[x]);
		if ((*env)->ExceptionOccurred(env)) {
			return getCurrentConfigurationW_clean_exp(env, 2, E_SETOBJECTARRAYSTR, &list);
		}
	}

	free_jstrarraylist(&list);
	return currentConfigurationRead;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComPortJNIBridge
 * Method:    getByteCount
 * Signature: (J)[I
 *
 * Return array's sequence is : number of input bytes, number of output bytes in tty buffers.
 *
 * @return array containing number of bytes in input and output buffer 0 on success otherwise
 *         NULL if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jintArray JNICALL Java_com_embeddedunveiled_serial_internal_SerialComPortJNIBridge_getByteCount(JNIEnv *env,
		jobject obj, jlong handle) {
		
	int ret = -1;
	jint val[2] = {0, 0};
	jintArray byteCounts = NULL;
	DWORD errors;
	COMSTAT comstat;
	
	ret = ClearCommError((HANDLE)handle, &errors, &comstat);
	if(ret == 0) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return NULL;
	}
	
	byteCounts = (*env)->NewIntArray(env, 2);
	if((byteCounts == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_NEWINTARRAYSTR);
		return NULL;
	}

	val[0] = (jint) comstat.cbInQue;
	val[1] = (jint) comstat.cbOutQue;
	
	(*env)->SetIntArrayRegion(env, byteCounts, 0, 2, val);
	if((*env)->ExceptionOccurred(env)) {
		throw_serialcom_exception(env, 3, 0, E_SETINTARRREGIONSTR);
		return NULL;
	}
	
	return byteCounts;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComPortJNIBridge
 * Method:    clearPortIOBuffers
 * Signature: (JZZ)I
 *
 * This will discard all pending data in given buffers. Received data therefore can not be read by
 * application or/and data to be transmitted in output buffer will get discarded i.e. not transmitted.
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComPortJNIBridge_clearPortIOBuffers(JNIEnv *env,
		jobject obj, jlong handle, jboolean rxPortbuf, jboolean txPortbuf) {

	int ret = 0;
	int PORTIOBUFFER = 0;

	if((rxPortbuf == JNI_TRUE) && (txPortbuf == JNI_TRUE)) {
		/* flushes both the input and output queue. */
		PORTIOBUFFER = PURGE_RXCLEAR | PURGE_TXCLEAR;
	}else if(rxPortbuf == JNI_TRUE) {
		/* flushes the input queue, which contains data that have been received but not yet read. */
		PORTIOBUFFER = PURGE_RXCLEAR;
	}else if(txPortbuf == JNI_TRUE) {
		/* flushes the output queue, which contains data that have been written but not yet transmitted. */
		PORTIOBUFFER = PURGE_TXCLEAR;
	}else {
		/* this case is handled in java layer itself */
	}

	ret = PurgeComm((HANDLE)handle, PORTIOBUFFER);
	if(ret == 0) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComPortJNIBridge
 * Method:    setRTS
 * Signature: (JZ)I
 *
 * Sets the RTS line to low or high voltages as defined by enabled argument. This causes value
 * in UART control register to change.
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComPortJNIBridge_setRTS(JNIEnv *env,
		jobject obj, jlong handle, jboolean enabled) {

	int ret = 0;
	DWORD rts_state = 0;

	if(enabled == JNI_TRUE){
		rts_state = SETRTS;
	}else {
		rts_state = CLRRTS;
	}

	ret = EscapeCommFunction((HANDLE)handle, rts_state);
	if(ret == 0) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComPortJNIBridge
 * Method:    setDTR
 * Signature: (JZ)I
 *
 * Sets the DTR line to low or high voltages as defined by enabled argument. This causes value in
 * UART control register to change.
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComPortJNIBridge_setDTR(JNIEnv *env,
		jobject obj, jlong handle, jboolean enabled) {

	int ret = 0;
	DWORD dtr_state = 0;

	if(enabled == JNI_TRUE){
		dtr_state = SETDTR;
	}else {
		dtr_state = CLRDTR;
	}

	ret = EscapeCommFunction((HANDLE)handle, dtr_state);
	if(ret == 0) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComPortJNIBridge
 * Method:    getLinesStatus
 * Signature: (J)[I
 *
 * The status of modem/control lines is returned as array of integers where '1' means line is asserted
 * and '0' means de-asserted. The sequence of lines matches in both java layer and native layer.
 * Last three values i.e. DTR, RTS, LOOP are set to 0, as Windows does not have any API to read there 
 * status.
 * 
 * Return sequence is CTS, DSR, DCD, RI, LOOP, RTS, DTR respectively.
 *
 * @return array containing status of modem control lines 0 on success otherwise NULL if
 *         an error occurs.
 * @throws SerialComException if anyone; FindClass, GetJavaVM or pthread_mutex_init function fails.
 */
JNIEXPORT jintArray JNICALL Java_com_embeddedunveiled_serial_internal_SerialComPortJNIBridge_getLinesStatus(JNIEnv *env,
		jobject obj, jlong handle) {

	int ret = -1;
	DWORD modem_stat = 0;
	jint status[7] = {0};
	jintArray current_status = NULL;

	current_status = (*env)->NewIntArray(env, 7);
	if((current_status == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_NEWINTARRAYSTR);
		return NULL;
	}
	
	ret = GetCommModemStatus((HANDLE)handle, &modem_stat);
	if(ret == 0) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return NULL;
	}
	
	status[0] = (modem_stat & MS_CTS_ON)  ? 1 : 0;
	status[1] = (modem_stat & MS_DSR_ON)  ? 1 : 0;
	status[2] = (modem_stat & MS_RLSD_ON) ? 1 : 0;
	status[3] = (modem_stat & MS_RING_ON) ? 1 : 0;
	status[4] = 0;
	status[5] = 0;
	status[6] = 0;
	
	(*env)->SetIntArrayRegion(env, current_status, 0, 7, status);
	if((*env)->ExceptionOccurred(env)) {
		throw_serialcom_exception(env, 3, 0, E_SETINTARRREGIONSTR);
		return NULL;
	}

	return current_status;	
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComPortJNIBridge
 * Method:    findDriverServingComPort
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 *
 * Find the name of the driver which is currently associated with the given serial port.
 *
 * @return name of driver if found for given serial port, empty string if no driver found for
 *         given serial port, NULL if any error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jstring JNICALL Java_com_embeddedunveiled_serial_internal_SerialComPortJNIBridge_findDriverServingComPort(JNIEnv *env,
		jobject obj, jstring comPortName) {
	return find_driver_for_given_com_port(env, comPortName);
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComPortJNIBridge
 * Method:    findIRQnumberForComPort
 * Signature: (J)Ljava/lang/String;
 *
 * Find the address and IRQ number associated with the given handle of serial port.
 *
 * @return address and IRQ string if found for given handle, empty string if no address/IRQ found for
 *         given handle, NULL if any error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jstring JNICALL Java_com_embeddedunveiled_serial_internal_SerialComPortJNIBridge_findIRQnumberForComPort(JNIEnv *env,
		jobject obj, jlong handle) {
	return find_address_irq_for_given_com_port(env, handle);
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComPortJNIBridge
 * Method:    sendBreak
 * Signature: (JI)I
 *
 * The duration is in milliseconds. If the line is held in the logic low condition (space in
 * UART jargon) for longer than a character time, this is a break condition that can be detected by
 * the UART. This applies break condition as per EIA232 standard.
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComPortJNIBridge_sendBreak(JNIEnv *env,
		jobject obj, jlong handle, jint duration) {

	int ret = -1;

	/* Set break condition. */
	ret = SetCommBreak((HANDLE)handle);
	if(ret == 0) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}

	ret = serial_delay(duration);
	if(ret < 0) {
		throw_serialcom_exception(env, 1, (-1 * ret), NULL);
		return -1;
	}

	/* Release break condition. */
	ret = ClearCommBreak((HANDLE)handle);
	if(ret == 0) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComPortJNIBridge
 * Method:    getInterruptCount
 * Signature: (J)[I
 *
 * Not supported by Windows OS itself. Applicable mainly for Linux.
 *
 * @return always NULL.
 */
JNIEXPORT jintArray JNICALL Java_com_embeddedunveiled_serial_internal_SerialComPortJNIBridge_getInterruptCount(JNIEnv *env,
		jobject obj, jlong handle) {
		return NULL;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComPortJNIBridge
 * Method:    fineTuneRead
 * Signature: (JIIIII)I
 *
 * This function gives more precise control over the behavior of read operation in terms of
 * timeout and number of bytes.
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComPortJNIBridge_fineTuneRead(JNIEnv *env,
		jobject obj, jlong handle, jint vmin, jint vtime, jint rit, jint rttm, jint rttc) {

	int ret = 0;
	COMMTIMEOUTS lpCommTimeouts = { 0 };
	
	ret = GetCommTimeouts((HANDLE)handle, &lpCommTimeouts);
	if(ret == 0) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}
	
	lpCommTimeouts.ReadIntervalTimeout = rit;
	lpCommTimeouts.ReadTotalTimeoutMultiplier = rttm;
	lpCommTimeouts.ReadTotalTimeoutConstant = rttc;
	
	ret = SetCommTimeouts((HANDLE)handle, &lpCommTimeouts);
	if(ret == 0) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}
	
	return 0;
}

/*
 * Both setUpDataLooperThread() and setUpEventLooperThread() 
 * call same function setupLooperThread() to create managed thread.
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
int setupLooperThread(JNIEnv *env, jobject obj, jlong handle, jobject looper_obj_ref, int data_enabled, 
	int event_enabled, int global_index, int new_dtp_index) {
	
	HANDLE hComm = (HANDLE)handle;
	HANDLE thread_handle;
	struct looper_thread_params params;
	unsigned thread_id;
	DWORD errorVal = 0;
	DWORD status = 0;

	jobject looper_ref = (*env)->NewGlobalRef(env, looper_obj_ref);
	if((looper_ref == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		LeaveCriticalSection(&csmutex);
		throw_serialcom_exception(env, 3, 0, E_NEWGLOBALREFSTR);
		return -1;
	}

	/* Set the values that will be passed to data thread. */
	params.jvm = jvm;
	params.hComm = hComm;
	params.looper = looper_ref;
	params.thread_handle = 0;
	params.wait_event_handles[0] = 0;
	params.wait_event_handles[1] = 0;
	params.thread_exit = 0;
	params.csmutex = &csmutex;             /* Same mutex is shared across all the threads. */
	params.data_enabled = data_enabled;
	params.event_enabled = event_enabled;
	params.init_done = -1;
	params.init_done_event_handle = CreateEvent(NULL, TRUE, FALSE, NULL);
	if (params.init_done_event_handle == NULL) {
		(*env)->DeleteGlobalRef(env, looper_ref);
		LeaveCriticalSection(&csmutex);
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}
	params.custom_err_code = 0;
	params.standard_err_code = 0;

	/* We have prepared data to be passed to thread, so create reference and pass it. */
	handle_looper_info[global_index] = params;
	void *arg = &handle_looper_info[global_index];

	/* Managed thread creation. The _beginthreadex initializes Certain CRT (C Run-Time) internals that 
	 * ensures that other C functions will work exactly as expected. */
	_set_errno(0);
	thread_handle = (HANDLE) _beginthreadex(NULL,   /* default security attributes */
					0,                              /* use default stack size      */
					&event_data_looper,             /* thread function name        */
					arg,                            /* argument to thread function */
					0,                              /* start thread immediately    */
					&thread_id);                    /* thread identifier           */
	if(thread_handle == 0) {
		(*env)->DeleteGlobalRef(env, looper_ref);
		throw_serialcom_exception(env, 5, errno, NULL);
		LeaveCriticalSection(&csmutex);
		return -1;
	}

	if(new_dtp_index) {
		/* update address where data parameters for next thread will be stored. */
		dtp_index++;
	}

	LeaveCriticalSection(&csmutex);

	/* wait till worker thread initialize completely */
	status = WaitForSingleObject(((struct looper_thread_params*) arg)->init_done_event_handle, INFINITE);
	CloseHandle(((struct looper_thread_params*) arg)->init_done_event_handle);
	
	if(0 == ((struct looper_thread_params*) arg)->init_done) {
		/* Save the thread handle which will be used when listener is unregistered. */
		((struct looper_thread_params*) arg)->thread_handle = thread_handle;
	}else {
		WaitForSingleObject(((struct looper_thread_params*) arg)->thread_handle, INFINITE);
		CloseHandle(((struct looper_thread_params*) arg)->thread_handle);
		((struct looper_thread_params*) arg)->thread_handle = 0;
		(*env)->DeleteGlobalRef(env, looper_ref);

		if((((struct looper_thread_params*) arg)->custom_err_code) > 0) {
			/* indicates custom error message should be used in exception.*/
			throw_serialcom_exception(env, 2, ((struct looper_thread_params*) arg)->custom_err_code, NULL);
		}else {
			/* indicates posix/os-specific error message should be used in exception.*/
			throw_serialcom_exception(env, 1, ((struct looper_thread_params*) arg)->standard_err_code, NULL);
		}
		return -1;
	}

	return 0; /* success */
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComPortJNIBridge
 * Method:    setUpDataLooperThread
 * Signature: (JLcom/embeddedunveiled/serial/internal/SerialComLooper;)I
 *
 * Creates new native worker thread. Both setUpDataLooperThread() and setUpEventLooperThread() 
 * call same function setupLooperThread() to create managed thread.
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComPortJNIBridge_setUpDataLooperThread(JNIEnv *env,
		jobject obj, jlong handle, jobject looper) {

	/* Check whether thread for this handle already exist or not. If it exist just update event
	 * mask to wait for otherwise create thread. */
	int x = 0;
	int ret = 0;
	int thread_exist = 0;
	struct looper_thread_params *ptr;
	ptr = handle_looper_info;
	HANDLE hComm = (HANDLE)handle;
	DWORD event_mask;
	DWORD updated_mask = 0;
	DWORD error_type = 0;
	int global_index = dtp_index;
	int new_dtp_index = 1;

	for (x = 0; x < MAX_NUM_THREADS; x++) {
		if(ptr->hComm == hComm) {
			thread_exist = 1;
			break;
		}
		ptr++;
	}

	/* we make sure that thread creation and data passing is atomic. */
	EnterCriticalSection(&csmutex);

	if(thread_exist == 1) {
		/* Thread exist so just update event to listen to. */
		ret = GetCommMask(hComm, &event_mask);
		updated_mask = event_mask | EV_RXCHAR;
		
		ret = SetCommMask(hComm, updated_mask);
		if(ret == 0) {
			throw_serialcom_exception(env, 4, GetLastError(), NULL);
			LeaveCriticalSection(&csmutex);
			return -1;
		}

		/* set data_enabled flag */
		ptr->data_enabled = 1;
		LeaveCriticalSection(&csmutex);
	}else {
		/* Not found in our records, so we create the thread. */
		ptr = handle_looper_info;
		for (x = 0; x < MAX_NUM_THREADS; x++) {
			if(ptr->hComm == (HANDLE)-1) {
				global_index = x;
				new_dtp_index = 0;
				break;
			}
			ptr++;
		}
		/* new_dtp_index is 0 if we reuse existing index otherwise 1. critical section will be released
		   by setupLooperThread function. */
		ret = setupLooperThread(env, obj, handle, looper, 1, 0, global_index, new_dtp_index);
		return ret;
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComPortJNIBridge
 * Method:    destroyDataLooperThread
 * Signature: (J)I
 *
 * If a process attempts to change the device handle's event mask by using the SetCommMask function 
 * while an overlapped  WaitCommEvent operation is in progress, WaitCommEvent returns immediately. 
 * Further WaitForMultipleObjects() comes out of waiting state when signaled. When either WaitCommEvent
 * or WaitForMultipleObjects returns, thread can check if it asked to exit. This is equivalent to 'evfd' 
 * concept used in Linux.
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComPortJNIBridge_destroyDataLooperThread(JNIEnv *env,
		jobject obj, jlong handle) {

	int ret = 0;
	int x = 0;
	DWORD a = 0;
	DWORD b = a & EV_CTS;
	DWORD error_type = 0;
	DWORD event_mask = 0;
	HANDLE hComm = (HANDLE)handle;
	struct looper_thread_params *ptr;
	int reset_hComm_field = 0;

	/* handle_looper_info is global array holding all the information. */
	ptr = handle_looper_info;

	EnterCriticalSection(&csmutex);

	ret = GetCommMask(hComm, &event_mask);
	if(ret == 0) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		LeaveCriticalSection(&csmutex);
		return -1;
	}

	if((b & event_mask) == 1) {
		/* Event listener exist, so just tell thread to wait for control events events only. */
		event_mask = 0;
		event_mask = event_mask | EV_BREAK | EV_CTS | EV_DSR | EV_ERR | EV_RING | EV_RLSD | EV_RXFLAG;
		
		ret = SetCommMask(hComm, event_mask);
		if(ret == 0) {
			throw_serialcom_exception(env, 4, GetLastError(), NULL);
			LeaveCriticalSection(&csmutex);
			return -1;
		}
		
		/* unset data_enabled flag */
		ptr = handle_looper_info;
		for (x = 0; x < MAX_NUM_THREADS; x++) {
			if(ptr->hComm == hComm) {
				ptr->data_enabled = 0;
				break;
			}
			ptr++;
		}
	}else {
		/* Destroy thread as event listener does not exist and user wish to unregister data listener also. */
		for (x = 0; x < MAX_NUM_THREADS; x++) {
			if(ptr->hComm == hComm) {
				ptr->thread_exit = 1;
				reset_hComm_field = 1;
				ptr->data_enabled = 0;
				ptr->event_enabled = 0;
				break;
			}
			ptr++;
		}
	}

	/* This causes WaitForMultipleObjects() in worker thread to come out of waiting state. */
	ret = SetEvent(ptr->wait_event_handles[0]);
	if(ret == 0) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		LeaveCriticalSection(&csmutex);
		return -1;
	}

	/* Wait till worker thread actually terminate and OS clean up resources. */
	WaitForSingleObject(ptr->thread_handle, INFINITE);
	CloseHandle(ptr->thread_handle);
	ptr->thread_handle = 0;

	/* If neither data nor event thread exist for this file descriptor remove entry for it from global array. */
	if(reset_hComm_field) {
		ptr->hComm = -1;
		(*env)->DeleteGlobalRef(env, ptr->looper);
	}

	LeaveCriticalSection(&csmutex);
	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComPortJNIBridge
 * Method:    setUpEventLooperThread
 * Signature: (JLcom/embeddedunveiled/serial/internal/SerialComLooper;)I
 * 
 * Both data and event creation function call same function setupLooperThread().
 * 
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComPortJNIBridge_setUpEventLooperThread(JNIEnv *env,
		jobject obj, jlong handle, jobject looper) {

	/* Check whether thread for this handle already exist or not.
	* If it exist just update event mask to wait for otherwise create thread. */
	int x = 0;
	int ret = 0;
	int thread_exist = 0;
	struct looper_thread_params *ptr;
	ptr = handle_looper_info;
	HANDLE hComm = (HANDLE)handle;
	DWORD event_mask;
	DWORD updated_mask = 0;
	DWORD error_type = 0;
	int global_index = dtp_index;
	int new_dtp_index = 1;

	for (x = 0; x < MAX_NUM_THREADS; x++) {
		if(ptr->hComm == hComm) {
			thread_exist = 1;
			break;
		}
		ptr++;
	}
	/* we make sure that thread creation and data passing is atomic. */
	EnterCriticalSection(&csmutex);

	if(thread_exist == 1) {
		/* Thread exist so just update event to listen to. */
		ret = GetCommMask(hComm, &event_mask);
		updated_mask = event_mask | EV_BREAK | EV_CTS | EV_DSR | EV_ERR | EV_RING | EV_RLSD | EV_RXFLAG;
		
		ret = SetCommMask(hComm, updated_mask);
		if(ret == 0) {
			throw_serialcom_exception(env, 4, GetLastError(), NULL);
			LeaveCriticalSection(&csmutex);
			return -1;
		}
		
		/* set event_enabled flag */
		ptr->event_enabled = 1;
		LeaveCriticalSection(&csmutex);
	}else {
		/* Not found in our records, so we create the thread. */
		ptr = handle_looper_info;
		for (x = 0; x < MAX_NUM_THREADS; x++) {
			if (ptr->hComm == (HANDLE)-1) {
				global_index = x;
				new_dtp_index = 0;
				break;
			}
			ptr++;
		}
		
		/* new_dtp_index is 0 if we reuse existing index otherwise 1. */
		ret = setupLooperThread(env, obj, handle, looper, 0, 1, global_index, new_dtp_index);
		return ret;
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComPortJNIBridge
 * Method:    destroyEventLooperThread
 * Signature: (J)I
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComPortJNIBridge_destroyEventLooperThread(JNIEnv *env,
		jobject obj, jlong handle) {
	
	int ret = 0;
	int x = 0;
	DWORD a = 0;
	DWORD b = a & EV_RXCHAR;
	DWORD error_type = 0;
	DWORD event_mask = 0;
	HANDLE hComm = (HANDLE)handle;
	struct looper_thread_params *ptr;
	int reset_hComm_field = 0;
	
	/* handle_looper_info is global array holding all the information. */
	ptr = handle_looper_info;

	EnterCriticalSection(&csmutex);

	ret = GetCommMask(hComm, &event_mask);
	if(ret == 0) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		LeaveCriticalSection(&csmutex);
		return -1;
	}

	if((b & event_mask) == 1) {
		/* Data listener exist, so just tell thread to wait for data events only. */
		event_mask = 0;
		event_mask = event_mask | EV_RXCHAR;
		
		ret = SetCommMask(hComm, event_mask);
		if(ret == 0) {
			throw_serialcom_exception(env, 4, GetLastError(), NULL);
			LeaveCriticalSection(&csmutex);
			return -1;
		}
		
		/* unset event_enabled flag */
		ptr = handle_looper_info;
		for (x = 0; x < MAX_NUM_THREADS; x++) {
			if (ptr->hComm == hComm) {
				ptr->event_enabled = 0;
				break;
			}
			ptr++;
		}
	}else {
		/* Destroy thread as data listener does not exist and user wish to unregister event listener also. */
		for (x = 0; x < MAX_NUM_THREADS; x++) {
			if(ptr->hComm == hComm) {
				ptr->thread_exit = 1;
				reset_hComm_field = 1;
				ptr->data_enabled = 0;
				ptr->event_enabled = 0;
				break;
			}
			ptr++;
		}

		/* This causes thread to come out of waiting state. */
		ret = SetEvent(ptr->wait_event_handles[0]);
		if(ret == 0) {
			throw_serialcom_exception(env, 4, GetLastError(), NULL);
			LeaveCriticalSection(&csmutex);
			return -1;
		}
	}

	/* Wait till worker thread actually terminate and OS clean up resources. */
	WaitForSingleObject(ptr->thread_handle, INFINITE);
	CloseHandle(ptr->thread_handle);
	ptr->thread_handle = 0;

	/* If neither data nor event thread exist for this file descriptor remove entry for it from global array. */
	if(reset_hComm_field) {
		ptr->hComm = -1;
		(*env)->DeleteGlobalRef(env, ptr->looper);
	}

	LeaveCriticalSection(&csmutex);
	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComPortJNIBridge
 * Method:    registerUSBHotPlugEventListener
 * Signature: (Lcom/embeddedunveiled/serial/ISerialComUSBHotPlugListener;IILjava/lang/String;)I
 *
 * Create a native thread that works with operating system specific mechanism for USB hot plug
 * facility.
 *
 * @return index of an array at which information about this worker thread is saved on success
 *         otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, WIN API call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComPortJNIBridge_registerUSBHotPlugEventListener(JNIEnv *env,
	jobject obj, jobject hotPlugListener, jint filterVID, jint filterPID, jstring serialNumber) {

	int x = 0;
	DWORD errorVal = 0;
	HANDLE thread_handle = 0;
	struct usb_dev_monitor_info params;
	unsigned thread_id = 0;
	int empty_index_found = 0;
	struct usb_dev_monitor_info *ptr = NULL;
	void *arg;
	jobject usbHotPlugListener = NULL;
	const char* serial_number = NULL;
	DWORD status;

	ptr = &usb_hotplug_monitor_info[0];
	EnterCriticalSection(&csmutex);

	usbHotPlugListener = (*env)->NewGlobalRef(env, hotPlugListener);
	if ((usbHotPlugListener == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_NEWGLOBALREFSTR);
		LeaveCriticalSection(&csmutex);
		return -1;
	}

	if (serialNumber != NULL) {
		serial_number = (*env)->GetStringUTFChars(env, serialNumber, NULL);
		if ((serial_number == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			throw_serialcom_exception(env, 3, 0, E_GETSTRUTFCHARSTR);
			LeaveCriticalSection(&csmutex);
			return -1;
		}
	}

	/* Check if there is an unused index then we will reuse it. */
	for (x = 0; x < MAX_NUM_THREADS; x++) {
		if (ptr->thread_handle == 0) {
			empty_index_found = 1;
			break;
		}
		ptr++;
	}

	params.jvm = jvm;
	params.wait_event_handle = 0;
	params.usbHotPlugEventListener = usbHotPlugListener;
	params.thread_exit = 0;
	params.usb_vid_to_match = (int) filterVID;
	params.usb_pid_to_match = (int) filterPID;
	if (serialNumber != NULL) {
		memset(params.serial_number_to_match, '\0', sizeof(params.serial_number_to_match));
		strcpy_s(params.serial_number_to_match, sizeof(params.serial_number_to_match), serial_number);
	}else {
		params.serial_number_to_match[0] = '\0';
	}
	params.init_done = -1;
	params.init_done_event_handle = CreateEvent(NULL, TRUE, FALSE, NULL);
	if (params.init_done_event_handle == NULL) {
		LeaveCriticalSection(&csmutex);
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}
	params.custom_err_code = -1;
	params.standard_err_code = -1;
	params.csmutex = &csmutex;
	params.info = usb_hotplug_monitor_info;

	if (empty_index_found == 1) {
		usb_hotplug_monitor_info[x] = params;
		arg = &usb_hotplug_monitor_info[x];
	}else {
		usb_hotplug_monitor_info[usb_dev_monitor_index] = params;
		arg = &usb_hotplug_monitor_info[usb_dev_monitor_index];
	}

	/* Managed thread creation. The _beginthreadex initializes Certain CRT (C Run-Time) internals that ensures 
	   that other C functions can be used from worker thread and they will work exactly as expected. */
	_set_errno(0);
	thread_handle = (HANDLE) _beginthreadex(NULL,                         /* default security attributes */
		                                    0,                            /* use default stack size      */
											&usb_device_hotplug_monitor,  /* thread function name        */
											arg,                          /* argument to thread function */
											0,                            /* start thread immediately    */
											&thread_id);                  /* thread identifier           */
	if (thread_handle == 0) {
		(*env)->DeleteGlobalRef(env, hotPlugListener);
		throw_serialcom_exception(env, 5, errno, NULL);
		LeaveCriticalSection(&csmutex);
		return -1;
	}

	/* Save the data thread handle which will be used when listener is unregistered. */
	((struct usb_dev_monitor_info*) arg)->thread_handle = thread_handle;

	LeaveCriticalSection(&csmutex);

	/* wait till worker thread initialize completely */
	status = WaitForSingleObject(((struct usb_dev_monitor_info*) arg)->init_done_event_handle, INFINITE);
	CloseHandle(((struct usb_dev_monitor_info*) arg)->init_done_event_handle);

	if (0 == ((struct usb_dev_monitor_info*) arg)->init_done) {
		/* Save the thread handle which will be used when listener is unregistered. */
		((struct usb_dev_monitor_info*) arg)->thread_handle = thread_handle;

		if (empty_index_found == 1) {
			return x;
		}else {
			/* update index where data for next thread will be saved. */
			usb_dev_monitor_index++;
			return (usb_dev_monitor_index - 1);
		}
	}else {
		WaitForSingleObject(((struct usb_dev_monitor_info*) arg)->thread_handle, INFINITE);
		CloseHandle(((struct usb_dev_monitor_info*) arg)->thread_handle);
		((struct usb_dev_monitor_info*) arg)->thread_handle = 0;
		(*env)->DeleteGlobalRef(env, hotPlugListener);

		if ((((struct usb_dev_monitor_info*) arg)->custom_err_code) > 0) {
			/* indicates custom error message should be used in exception.*/
			throw_serialcom_exception(env, 2, ((struct usb_dev_monitor_info*) arg)->custom_err_code, NULL);
		}else {
			/* indicates posix/os-specific error message should be used in exception.*/
			throw_serialcom_exception(env, 1, ((struct usb_dev_monitor_info*) arg)->standard_err_code, NULL);
		}
		return -1;
	}

	/* should not be reached */
	return -1;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComPortJNIBridge
 * Method:    unregisterUSBHotPlugEventListener
 * Signature: (I)I
 *
 * Destroy worker thread used for USB hot plug monitoring. The java layer sends index in array
 * where info about the thread to be destroyed is stored.
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComPortJNIBridge_unregisterUSBHotPlugEventListener(JNIEnv *env,
		jobject obj, jint index) {

	int ret = 0;
	struct usb_dev_monitor_info *ptr = &usb_hotplug_monitor_info[index];

	EnterCriticalSection(&csmutex);

	/* Set the flag that will be checked by worker thread for exit condition. */
	ptr->thread_exit = 1;

	/* make worker thread come out of waiting state through posting message to message queue. */
	ret = PostMessage(ptr->window_handle, 0x0100, 0, 0);
	if (ret == 0) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		LeaveCriticalSection(&csmutex);
		return -1;
	}

	/* Wait till worker thread actually terminate and OS clean up resources. */
	WaitForSingleObject(ptr->thread_handle, INFINITE);
	CloseHandle(ptr->thread_handle);
	ptr->thread_handle = 0;
	ptr->window_handle = 0;

	(*env)->DeleteGlobalRef(env, ptr->usbHotPlugEventListener);

	LeaveCriticalSection(&csmutex);
	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComPortJNIBridge
 * Method:    pauseListeningEvents
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComPortJNIBridge_pauseListeningEvents(JNIEnv *env,
		jobject obj, jlong fd) {
	return -1;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComPortJNIBridge
 * Method:    resumeListeningEvents
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComPortJNIBridge_resumeListeningEvents(JNIEnv *env,
		jobject obj, jlong fd) {
	return -1;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComPortJNIBridge
 * Method:    ioctlExecuteOperation
 * Signature: (JJ)J
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jlong JNICALL Java_com_embeddedunveiled_serial_internal_SerialComPortJNIBridge_ioctlExecuteOperation(JNIEnv *env,
		jobject obj, jlong fd, jlong operationCode) {
	return -1;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComPortJNIBridge
 * Method:    ioctlSetValue
 * Signature: (JJJ)J
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jlong JNICALL Java_com_embeddedunveiled_serial_internal_SerialComPortJNIBridge_ioctlSetValue(JNIEnv *env,
		jobject obj, jlong fd, jlong operationCode, jlong value) {
	return -1;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComPortJNIBridge
 * Method:    ioctlGetValue
 * Signature: (JJ)J
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jlong JNICALL Java_com_embeddedunveiled_serial_internal_SerialComPortJNIBridge_ioctlGetValue(JNIEnv *env,
		jobject obj, jlong fd, jlong operationCode) {
	return -1;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComPortJNIBridge
 * Method:    ioctlSetValueIntArray
 * Signature: (JJ[I)J
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jlong JNICALL Java_com_embeddedunveiled_serial_internal_SerialComPortJNIBridge_ioctlSetValueIntArray(JNIEnv *env,
		jobject obj, jlong v, jlong f, jintArray r) {
	return -1;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComPortJNIBridge
 * Method:    ioctlSetValueCharArray
 * Signature: (JJ[B)J
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jlong JNICALL Java_com_embeddedunveiled_serial_internal_SerialComPortJNIBridge_ioctlSetValueCharArray(JNIEnv *env,
		jobject obj, jlong q, jlong c, jbyteArray v) {
	return -1;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComPortJNIBridge
 * Method:    getCDCUSBDevPowerInfo
 * Signature: (Ljava/lang/String;)[Ljava/lang/String;
 *
 * @return 0 on success otherwise NULL if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jobjectArray JNICALL Java_com_embeddedunveiled_serial_internal_SerialComPortJNIBridge_getCDCUSBDevPowerInfo
(JNIEnv *env, jobject obj, jstring comPortName) {
	return get_usbdev_powerinfo(env, comPortName);
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComPortJNIBridge
 * Method:    setLatencyTimer
 * Signature: (Ljava/lang/String;B)I
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComPortJNIBridge_setLatencyTimer(JNIEnv *env, 
	jobject obj, jstring comPortName, jbyte timerValue) {
	return set_latency_timer_value(env, comPortName, timerValue);
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComPortJNIBridge
 * Method:    getLatencyTimer
 * Signature: (Ljava/lang/String;)I
 *
 * @return currently set latency timer value on success otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComPortJNIBridge_getLatencyTimer(JNIEnv *env, 
	jobject obj, jstring comPortName) {
	return get_latency_timer_value(env, comPortName);
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComPortJNIBridge
 * Method:    rescanUSBDevicesHW
 * Signature: ()I
 * 
 * This is the programmatic equivalent of clicking the "Scan For Hardware Changes" menu item in the Device Manager.
 *
 * @return 0 on success otherwise -1 if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComPortJNIBridge_rescanUSBDevicesHW(JNIEnv *env,
	jobject obj) {
	return -1;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComPortJNIBridge
 * Method:    listBTSPPDevNodesWithInfo
 * Signature: ()[Ljava/lang/String;
 *
 * @return array of Strings containing info about rfcomm device nodes found, NULL if error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jobjectArray JNICALL Java_com_embeddedunveiled_serial_internal_SerialComPortJNIBridge_listBTSPPDevNodesWithInfo(JNIEnv *env,
		jobject obj) {
	return NULL;
}
