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

/* Project is built with unicode character set enabled. */

#include <windows.h>
#include <tchar.h>
#include <jni.h>
#include "scm_simpleio.h"

/* Common interface with java layer for supported OS types. */
#include "../../com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge.h"

/* global handle to loaded shared library and its functions*/
HMODULE DLL_handle = NULL;

/* global pointers to exported functions */
void(__stdcall *scm_InitMCP2200)(unsigned int, unsigned int);
BOOL(__stdcall *scm_IsConnected)(void);
BOOL(__stdcall *scm_ConfigureMCP2200)(unsigned char, unsigned long, unsigned int, unsigned int, BOOL, BOOL, BOOL, BOOL);
BOOL(__stdcall *scm_SetPin)(unsigned int);
BOOL(__stdcall *scm_ClearPin)(unsigned int);
int(__stdcall *scm_ReadPinValue)(unsigned int);
BOOL(__stdcall *scm_ReadPin)(unsigned int, unsigned int *);
BOOL(__stdcall *scm_WritePort)(unsigned int);
BOOL(__stdcall *scm_ReadPort)(unsigned int *);
int(__stdcall *scm_ReadPortValue)(void);
int(__stdcall *scm_SelectDevice)(unsigned int);
int(__stdcall *scm_GetSelectedDevice)(void);
unsigned int(__stdcall *scm_GetNoOfDevices)(void);
void(__stdcall *scm_GetDeviceInfo)(unsigned int, LPSTR);
void(__stdcall *scm_GetSelectedDeviceInfo)(LPSTR);
int(__stdcall *scm_ReadEEPROM)(unsigned int);
int(__stdcall *scm_WriteEEPROM)(unsigned int, unsigned char);
BOOL(__stdcall *scm_fnRxLED)(unsigned int);
BOOL(__stdcall *scm_fnTxLED)(unsigned int);
BOOL(__stdcall *scm_fnHardwareFlowControl)(unsigned int);
BOOL(__stdcall *scm_fnULoad)(unsigned int);
BOOL(__stdcall *scm_fnSuspend)(unsigned int);
BOOL(__stdcall *scm_fnInvertUartPol)(unsigned int);
BOOL(__stdcall *scm_fnSetBaudRate)(unsigned long);
BOOL(__stdcall *scm_ConfigureIO)(unsigned char);
BOOL(__stdcall *scm_ConfigureIoDefaultOutput)(unsigned char, unsigned char);

/* Called when this shared library is loaded or un-loaded. This clean up resources on library exit. */
BOOL WINAPI DllMain(HANDLE hModule, DWORD reason_for_call, LPVOID lpReserved) {
	switch (reason_for_call) {
	case DLL_PROCESS_DETACH:
		FreeLibrary(DLL_handle);
		break;
	}
	return TRUE;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge
 * Method:    loadAndLinkSimpleIODLL
 * Signature: (Ljava/lang/String;)I
 *
 * @return 0 on success or -1 if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge_loadAndLinkSimpleIODLL
(JNIEnv *env, jobject obj, jstring vendorLibraryWithAbsolutePath) {

	const jchar* vlib = NULL;
	FARPROC function_address = 0;

	throw_serialcom_exception(env, 4, 193, NULL);
	return -1;

	vlib = (*env)->GetStringChars(env, vendorLibraryWithAbsolutePath, JNI_FALSE);
	if ((vlib == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_GETSTRUTFCHARSTR);
		return -1;
	}

	/* load vendor supplied shared library */
	DLL_handle = LoadLibrary(vlib);
	if (DLL_handle == NULL) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}
	
	/* resolve references to all the exported functions */
	scm_InitMCP2200 = (void(__stdcall *)(unsigned int, unsigned int)) GetProcAddress(DLL_handle, "InitMCP2200");
	if (scm_InitMCP2200 == NULL) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}
	scm_IsConnected = (BOOL(__stdcall *)(void)) GetProcAddress(DLL_handle, "IsConnected");
	if (scm_IsConnected == NULL) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}
	scm_ConfigureMCP2200 = (BOOL(__stdcall *)(unsigned char, unsigned long, unsigned int, unsigned int, BOOL, BOOL, BOOL, BOOL)) GetProcAddress(DLL_handle, "ConfigureMCP2200");
	if (scm_ConfigureMCP2200 == NULL) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}
	scm_SetPin = (BOOL(__stdcall *)(unsigned int)) GetProcAddress(DLL_handle, "SetPin");
	if (scm_SetPin == NULL) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}
	scm_ClearPin = (BOOL(__stdcall *)(unsigned int)) GetProcAddress(DLL_handle, "ClearPin");
	if (scm_ClearPin == NULL) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}
	scm_ReadPinValue = (int(__stdcall *)(unsigned int)) GetProcAddress(DLL_handle, "ReadPinValue");
	if (scm_ReadPinValue == NULL) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}
	scm_ReadPin = (BOOL(__stdcall *)(unsigned int, unsigned int *)) GetProcAddress(DLL_handle, "ReadPin");
	if (scm_ReadPin == NULL) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}
	scm_WritePort = (BOOL(__stdcall *)(unsigned int)) GetProcAddress(DLL_handle, "WritePort");
	if (scm_WritePort == NULL) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}
	scm_ReadPort = (BOOL(__stdcall *)(unsigned int *)) GetProcAddress(DLL_handle, "ReadPort");
	if (scm_ReadPort == NULL) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}
	scm_ReadPortValue = (int(__stdcall *)(void)) GetProcAddress(DLL_handle, "ReadPortValue");
	if (scm_ReadPortValue == NULL) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}
	scm_SelectDevice = (int(__stdcall *)(unsigned int)) GetProcAddress(DLL_handle, "SelectDevice");
	if (scm_SelectDevice == NULL) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}
	scm_GetSelectedDevice = (int(__stdcall *)(void)) GetProcAddress(DLL_handle, "GetSelectedDevice");
	if (scm_GetSelectedDevice == NULL) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}
	scm_GetNoOfDevices = (unsigned int(__stdcall *)(void)) GetProcAddress(DLL_handle, "GetNoOfDevices");
	if (scm_GetNoOfDevices == NULL) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}
	scm_GetDeviceInfo = (BOOL(__stdcall *)(unsigned int, LPSTR)) GetProcAddress(DLL_handle, "GetDeviceInfo");
	if (scm_GetDeviceInfo == NULL) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}
	scm_GetSelectedDeviceInfo = (int(__stdcall *)(LPSTR)) GetProcAddress(DLL_handle, "GetSelectedDeviceInfo");
	if (scm_GetSelectedDeviceInfo == NULL) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}
	scm_ReadEEPROM = (int(__stdcall *)(unsigned int)) GetProcAddress(DLL_handle, "ReadEEPROM");
	if (scm_ReadEEPROM == NULL) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}
	scm_WriteEEPROM = (int(__stdcall *)(unsigned int, unsigned char)) GetProcAddress(DLL_handle, "WriteEEPROM");
	if (scm_WriteEEPROM == NULL) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}
	scm_fnRxLED = (BOOL(__stdcall *)(unsigned int)) GetProcAddress(DLL_handle, "fnRxLED");
	if (scm_fnRxLED == NULL) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}
	scm_fnTxLED = (BOOL(__stdcall *)(unsigned int)) GetProcAddress(DLL_handle, "fnTxLED");
	if (scm_fnTxLED == NULL) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}
	scm_fnHardwareFlowControl = (BOOL(__stdcall *)(unsigned int)) GetProcAddress(DLL_handle, "fnHardwareFlowControl");
	if (scm_fnHardwareFlowControl == NULL) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}
	scm_fnULoad = (BOOL(__stdcall *)(unsigned int)) GetProcAddress(DLL_handle, "fnULoad");
	if (scm_fnULoad == NULL) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}
	scm_fnSuspend = (BOOL(__stdcall *)(unsigned int)) GetProcAddress(DLL_handle, "fnSuspend");
	if (scm_fnSuspend == NULL) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}
	scm_fnInvertUartPol = (BOOL(__stdcall *)(unsigned int)) GetProcAddress(DLL_handle, "fnInvertUartPol");
	if (scm_fnInvertUartPol == NULL) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}
	scm_fnSetBaudRate = (BOOL(__stdcall *)(unsigned long)) GetProcAddress(DLL_handle, "fnSetBaudRate");
	if (scm_fnSetBaudRate == NULL) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}
	scm_ConfigureIO = (BOOL(__stdcall *)(unsigned char)) GetProcAddress(DLL_handle, "ConfigureIO");
	if (scm_ConfigureIO == NULL) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}
	scm_ConfigureIoDefaultOutput = (BOOL(__stdcall *)(unsigned char, unsigned char)) GetProcAddress(DLL_handle, "ConfigureIoDefaultOutput");
	if (scm_ConfigureIoDefaultOutput == NULL) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge
 * Method:    initMCP2200
 * Signature: (II)I
 *
 * @return 0 always.
 * @throws none.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge_initMCP2200(JNIEnv *env, 
	jobject obj, jint vendorID, jint productID) {
	scm_InitMCP2200((unsigned int) vendorID, (unsigned int) productID);
	return 0;
};

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge
 * Method:    isConnected
 * Signature: ()I
 *
 * @return 0 if not connected or 1 if connected.
 * @throws none.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge_isConnected(JNIEnv *env, 
	jobject obj) {
	BOOL ret = scm_IsConnected();
	if (ret == TRUE) {
		return 1;
	}
	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge
 * Method:    configureMCP2200
 * Signature: (BJIIZZZZ)I
 *
 * @return 0 on success or -1 if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge_configureMCP2200(JNIEnv *env, 
	jobject obj, jbyte ioMap, jlong baudRateParam, jint rxLEDMode, jint txLEDMode, jboolean flow, jboolean uload, 
	jboolean sspnd, jboolean invert) {

	BOOL ret = FALSE;
	BOOL flowval = FALSE;
	BOOL uloadval = FALSE;
	BOOL sspndval = FALSE;
	BOOL invertval = FALSE;

	if (flow == JNI_TRUE) {
		flowval = TRUE;
	}else {
		flowval = FALSE;
	}
	if (uload == JNI_TRUE) {
		uloadval = TRUE;
	}else {
		uloadval = FALSE;
	}
	if (sspnd == JNI_TRUE) {
		sspndval = TRUE;
	}else {
		sspndval = FALSE;
	}
	if (invert == JNI_TRUE) {
		invertval = TRUE;
	}else {
		invertval = FALSE;
	}

	ret = scm_ConfigureMCP2200(ioMap, (unsigned long)baudRateParam, rxLEDMode, txLEDMode, flowval, uloadval, sspndval, invertval);
	if (ret = FALSE) {
		throw_serialcom_exception(env, 3, 0, "ConfigureMCP2200() returned FALSE !");
		return -1;
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge
 * Method:    setPin
 * Signature: (I)I
 *
 * @return 0 on success or -1 if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge_setPin(JNIEnv *env, 
	jobject obj, jint pinNumber) {

	BOOL ret = FALSE;
	ret = scm_SetPin(pinNumber);
	if (ret = FALSE) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge
 * Method:    clearPin
 * Signature: (I)I
 *
 * @return 0 on success or -1 if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge_clearPin(JNIEnv *env, 
	jobject obj, jint pinNumber) {

	BOOL ret = FALSE;
	ret = scm_ClearPin(pinNumber);
	if (ret = FALSE) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge
 * Method:    readPinValue
 * Signature: (I)I
 *
 * @return pin value read on success or -1 if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge_readPinValue(JNIEnv *env, 
	jobject obj, jint pinNumber) {

	int ret = 0;
	ret = scm_ReadPinValue(pinNumber);
	if (ret = 0x8000) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}

	return (jint)ret;
}

/*
 * Method:    readPin
 * Signature: (I)I
 *
 * @return pin value read on success or -1 if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge_readPin(JNIEnv *env, 
	jobject obj, jint pinNumber) {

	BOOL ret = FALSE;
	unsigned int value = 0;

	ret = scm_ReadPin(pinNumber, &value);
	if (ret = FALSE) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}

	return (jint)value;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge
 * Method:    writePort
 * Signature: (I)I
 *
 * @return 0 on success or -1 if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge_writePort(JNIEnv *env, 
	jobject obj, jint portValue) {

	BOOL ret = FALSE;

	ret = scm_WritePort(portValue);
	if (ret = FALSE) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge
 * Method:    readPort
 * Signature: ()I
 *
 * @return port value read on success or -1 if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge_readPort(JNIEnv *env,
	jobject obj) {

	BOOL ret = FALSE;
	unsigned int value = 0;

	ret = scm_ReadPort(&value);
	if (ret = FALSE) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}

	return (jint)value;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge
 * Method:    readPortValue
 * Signature: ()I
 *
 * @return port value read on success or -1 if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge_readPortValue(JNIEnv *env, 
	jobject obj) {

	int value = 0;

	value = scm_ReadPortValue();
	if (value = 0x8000) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}

	return (jint)value;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge
 * Method:    selectDevice
 * Signature: (I)I
 *
 * @return 0 on success or -1 if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge_selectDevice(JNIEnv *env, 
	jobject obj, jint uiDeviceNumber) {

	int ret = 0;

	ret = scm_SelectDevice(uiDeviceNumber);
	if (ret = 0) {
		if (ret == -1) {
			throw_serialcom_exception(env, 3, 0, "E_WRONG_DEVICE_ID");
		}else if (ret == -2) {
			throw_serialcom_exception(env, 3, 0, "E_INACTIVE_DEVICE");
		}else {
			throw_serialcom_exception(env, 3, 0, "SelectDevice() failed !");
		}
		
		return -1;
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge
 * Method:    getSelectedDevice
 * Signature: ()I
 *
 * @return result of GetSelectDevice() as is on success.
 * @throws none.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge_getSelectedDevice(JNIEnv *env, 
	jobject obj) {
	return scm_GetSelectedDevice();
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge
 * Method:    getNumOfDevices
 * Signature: ()I
 *
 * @return result of GetNoOfDevices() as is on success.
 * @throws none.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge_getNumOfDevices(JNIEnv *env, 
	jobject obj) {
	return scm_GetNoOfDevices();
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge
 * Method:    getDeviceInfo
 * Signature: (I)Ljava/lang/String;
 *
 * @return string returned by GetDeviceInfo() on success or NULL if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jstring JNICALL Java_com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge_getDeviceInfo(JNIEnv *env, 
	jobject obj, jint uiDeviceNumber) {

	char buffer[256];
	jstring info = NULL;

	memset(buffer, '\0', 256);
	scm_GetDeviceInfo(uiDeviceNumber, buffer);
	info = (*env)->NewStringUTF(env, buffer);
	if ((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
		return NULL;
	}
	
	return info;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge
 * Method:    getSelectedDeviceInfo
 * Signature: ()Ljava/lang/String;
 *
 * @return string returned by SelectedDeviceInfo() on success or NULL if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jstring JNICALL Java_com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge_getSelectedDeviceInfo(JNIEnv *env, 
	jobject obj) {

	char buffer[256];
	jstring info = NULL;

	memset(buffer, '\0', 256);
	scm_GetSelectedDeviceInfo(buffer);
	info = (*env)->NewStringUTF(env, buffer);
	if ((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
		return NULL;
	}

	return info;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge
 * Method:    readEEPROM
 * Signature: (I)I
 *
 * @return eeprom value read on success or -1 if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge_readEEPROM(JNIEnv *env, 
	jobject obj, jint uiEEPAddress) {

	int value = 0;

	value = scm_ReadEEPROM(uiEEPAddress);
	if (value < 0) {
		if (value == -3) {
			throw_serialcom_exception(env, 3, 0, "E_WRONG_ADDRESS");
		}else if (value == -4) {
			throw_serialcom_exception(env, 3, 0, "E_CANNOT_SEND_DATA");
		}else {
			throw_serialcom_exception(env, 3, 0, "ReadEEPROM() failed to read eeprom value !");
		}
		return -1;
	}

	return (jint)value;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge
 * Method:    writeEEPROM
 * Signature: (IS)I
 *
 * @return 0 on success or -1 if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge_writeEEPROM(JNIEnv *env, 
	jobject obj, jint uiEEPAddress, jshort ucValue) {

	int ret = 0;

	ret = scm_WriteEEPROM(uiEEPAddress, (unsigned char)ucValue);
	if (ret < 0) {
		if (ret == -3) {
			throw_serialcom_exception(env, 3, 0, "E_WRONG_ADDRESS");
		}else if (ret == -4) {
			throw_serialcom_exception(env, 3, 0, "E_CANNOT_SEND_DATA");
		}else {
			throw_serialcom_exception(env, 3, 0, "WriteEEPROM() failed to write eeprom value !");
		}
		return -1;
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge
 * Method:    fnRxLED
 * Signature: (I)I
 *
 * @return 0 on success or -1 if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge_fnRxLED(JNIEnv *env, 
	jobject obj, jint mode) {

	BOOL ret = FALSE;

	ret = scm_fnRxLED(mode);
	if (ret == FALSE) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge
 * Method:    fnTxLED
 * Signature: (I)I
 *
 * @return 0 on success or -1 if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge_fnTxLED(JNIEnv *env, 
	jobject obj, jint mode) {

	BOOL ret = FALSE;

	ret = scm_fnTxLED(mode);
	if (ret == FALSE) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge
 * Method:    hardwareFlowControl
 * Signature: (I)I
 *
 * @return 0 on success or -1 if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge_hardwareFlowControl(JNIEnv *env, 
	jobject obj, jint onOff) {

	BOOL ret = FALSE;

	ret = scm_fnHardwareFlowControl(onOff);
	if (ret == FALSE) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge
 * Method:    fnULoad
 * Signature: (I)I
 *
 * @return 0 on success or -1 if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge_fnULoad(JNIEnv *env, 
	jobject obj, jint onOff) {

	BOOL ret = FALSE;

	ret = scm_fnULoad(onOff);
	if (ret == FALSE) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge
 * Method:    fnSuspend
 * Signature: (I)I
 *
 * @return 0 on success or -1 if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge_fnSuspend(JNIEnv *env, 
	jobject obj, jint onOff) {

	BOOL ret = FALSE;

	ret = scm_fnSuspend(onOff);
	if (ret == FALSE) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge
 * Method:    fnInvertUartPol
 * Signature: (I)I
 *
 * @return 0 on success or -1 if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge_fnInvertUartPol(JNIEnv *env, 
	jobject obj, jint onOff) {

	BOOL ret = FALSE;

	ret = scm_fnInvertUartPol(onOff);
	if (ret == FALSE) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge
 * Method:    fnSetBaudRate
 * Signature: (J)I
 *
 * @return 0 on success or -1 if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge_fnSetBaudRate(JNIEnv *env, 
	jobject obj, jlong baudRateParam) {

	BOOL ret = FALSE;

	ret = scm_fnSetBaudRate((unsigned long)baudRateParam);
	if (ret == FALSE) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge
 * Method:    configureIO
 * Signature: (S)I
 *
 * 1 - GPIO configured as input
 * 0 - GPIO configured as output
 * MSB  -   -   -   -   -   -  LSB
 * GP7 GP6 GP5 GP4 GP3 GP2 GP1 GP0
 *
 * @return 0 on success or -1 if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge_configureIO(JNIEnv *env, 
	jobject obj, jshort ioMap) {

	BOOL ret = FALSE;

	ret = scm_ConfigureIO((unsigned char)ioMap);
	if (ret == FALSE) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}

	return 0;
}

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge
 * Method:    configureIoDefaultOutput
 * Signature: (SS)I
 *
 * 1 - GPIO configured as input
 * 0 - GPIO configured as output
 * MSB  -   -   -   -   -   -  LSB
 * GP7 GP6 GP5 GP4 GP3 GP2 GP1 GP0
 *
 * @return 0 on success or -1 if an error occurs.
 * @throws SerialComException if any JNI function, Win API call or C function fails.
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComMCHPSIOJNIBridge_configureIoDefaultOutput(JNIEnv *env, 
	jobject obj, jshort ioMap, jshort ucDefValue) {

	BOOL ret = FALSE;

	ret = scm_ConfigureIoDefaultOutput((unsigned char)ioMap, (unsigned char)ucDefValue);
	if (ret == FALSE) {
		throw_serialcom_exception(env, 4, GetLastError(), NULL);
		return -1;
	}

	return 0;
}

