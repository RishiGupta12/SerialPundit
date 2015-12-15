
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

#include "stdafx.h"
#include <tchar.h>
#include <setupapi.h>
#include <jni.h>
#include "windows_serial_lib.h"

static const GUID GUID_DEVINTERFACE_USB_DEVICE = { 0xA5DCBF10L, 0x6530, 0x11D2, { 0x90, 0x1F, 0x00, 0xC0, 0x4F, 0xB9, 0x51, 0xED } };

/*
 * when a new USB composite device is plugged into a computer, the USB hub driver creates a physical device object (PDO) and 
 * notifies the operating system that its set of child devices has changed. After querying the hub driver for the hardware 
 * identifiers associated with the new PDO, the operating system searches the appropriate INF files to find a match for the 
 * identifiers. If it finds a match other than USB\COMPOSITE, it loads the driver indicated in the INF file. However, if no 
 * other match is found, the operating system uses the compatible ID USB\COMPOSITE, for which it loads the USB Generic Parent 
 * driver. The Generic Parent driver then creates a separate PDO and generates a separate set of hardware identifiers for each 
 * interface of the composite device.
 */
jobjectArray getusb_firmware_version(JNIEnv *env, jint usbvid_to_match, jint usbpid_to_match, jstring serial_number) {

	int x = 0;
	int i = 0;
	int vid = 0;
	int pid = 0;
	BOOL ret = FALSE;
	DWORD size = 0;
	DWORD regproptype;
	DWORD error_code = 0;
	DWORD member_index = 0;
	HDEVINFO usb_dev_info_set;
	SP_DEVINFO_DATA usb_dev_instance;
	TCHAR buffer[1024];
	TCHAR hexcharbuffer[64];
	TCHAR *ptrend;
	const jchar* serial = NULL;
	struct jstrarray_list list = { 0 };
	jstring usb_dev_info;
	jclass strClass = NULL;
	jobjectArray usbDevicesFwVerFound = NULL;

	/* extract serial number if application has given */
	if (serial_number != NULL) {
		serial = (*env)->GetStringChars(env, serial_number, JNI_FALSE);
		if ((serial == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			(*env)->ExceptionClear(env);
			throw_serialcom_exception(env, 3, 0, E_GETSTRCHARSTR);
			return NULL;
		}
	}

	init_jstrarraylist(&list, 10);

	/* get information set for all devices matching the GUID */
	usb_dev_info_set = SetupDiGetClassDevs(&GUID_DEVINTERFACE_USB_DEVICE, NULL, 0, DIGCF_DEVICEINTERFACE | DIGCF_PRESENT);
	if (usb_dev_info_set == INVALID_HANDLE_VALUE) {
		SetupDiDestroyDeviceInfoList(usb_dev_info_set);
		throw_serialcom_exception(env, 4, HRESULT_FROM_SETUPAPI(GetLastError()), NULL);
		return NULL;
	}

	/* enumerate all devices in this information set */
	while (1) {
		ZeroMemory(&usb_dev_instance, sizeof(usb_dev_instance));
		usb_dev_instance.cbSize = sizeof(usb_dev_instance);

		/* from information set get device device by index */
		ret = SetupDiEnumDeviceInfo(usb_dev_info_set, member_index, &usb_dev_instance);
		if (ret == FALSE) {
			error_code = GetLastError();
			if (error_code == ERROR_NO_MORE_ITEMS) {
				break;
			}else {
				SetupDiDestroyDeviceInfoList(usb_dev_info_set);
				throw_serialcom_exception(env, 4, HRESULT_FROM_SETUPAPI(GetLastError()), NULL);
				return NULL;
			}
		}

		/* for this device find its instance ID (USB\VID_04D8&PID_00DF\000098037)
		 * this is variable 'Device Instance Path' in device manager. */
		memset(buffer, '\0', 1024);
		ret = SetupDiGetDeviceInstanceId(usb_dev_info_set, &usb_dev_instance, buffer, 1024, &size);
		if (ret == FALSE) {
			SetupDiDestroyDeviceInfoList(usb_dev_info_set);
			throw_serialcom_exception(env, 4, HRESULT_FROM_SETUPAPI(GetLastError()), NULL);
			return NULL;
		}

		/* extract vid and match */
		x = 0;
		while (buffer[x] != '\0') {
			if ((buffer[x] == 'V') && (buffer[x + 1] == 'I') && (buffer[x + 2] == 'D') && (buffer[x + 3] == '_')) {
				break;
			}
			x++;
		}
		x = x + 4;
		i = 0;
		while (buffer[x] != '&') {
			hexcharbuffer[i] = buffer[x];
			i++;
			x++;
		}
		hexcharbuffer[i] = '\0';
		vid = (int)_tcstol(hexcharbuffer, &ptrend, 16);
		if (vid != usbvid_to_match) {
			member_index++;
			continue;
		}

		/* extract pid and match */
		x = 6;
		while (buffer[x] != '\0') {
			if ((buffer[x] == 'P') && (buffer[x + 1] == 'I') && (buffer[x + 2] == 'D') && (buffer[x + 3] == '_')) {
				break;
			}
			x++;
		}
		x = x + 4;
		i = 0;
		while (buffer[x] != '\\') {
			hexcharbuffer[i] = buffer[x];
			i++;
			x++;
		}
		hexcharbuffer[i] = '\0';
		pid = (int)_tcstol(hexcharbuffer, &ptrend, 16);
		if (pid != usbpid_to_match) {
			member_index++;
			continue;
		}

		/* match serial number if requested by application */
		if (serial != NULL) {
			x++;
			i = 0;
			while (buffer[x] != '\0') {
				hexcharbuffer[i] = buffer[x];
				i++;
				x++;
			}
			hexcharbuffer[i] = '\0';
			/* case insensitive comparision */
			ret = _tcsicmp(serial, hexcharbuffer);
			if (ret != 0) {
				member_index++;
				continue;
			}
		}

		/* reaching here means this is the device whose firmware version is to be obtained. Using hardware ID try to 
		 * get bcdDevice value (USB\VID_04D8&PID_00DF&REV_0101) */

		memset(buffer, '\0', 1024);
		ret = SetupDiGetDeviceRegistryProperty(usb_dev_info_set, &usb_dev_instance, SPDRP_HARDWAREID, &regproptype, (BYTE *)buffer, sizeof(buffer), &size);
		if (ret == FALSE) {
			SetupDiDestroyDeviceInfoList(usb_dev_info_set);
			throw_serialcom_exception(env, 4, HRESULT_FROM_SETUPAPI(GetLastError()), NULL);
			return NULL;
		}

		x = 16;
		while (buffer[x] != '\0') {
			if ((buffer[x] == 'R') && (buffer[x + 1] == 'E') && (buffer[x + 2] == 'V') && (buffer[x + 3] == '_')) {
				hexcharbuffer[0] = buffer[x + 4];
				hexcharbuffer[1] = buffer[x + 5];
				hexcharbuffer[2] = buffer[x + 6];
				hexcharbuffer[3] = buffer[x + 7];
				hexcharbuffer[4] = '\0';
				break;
			}
			x++;
		}

		memset(buffer, '\0', sizeof(buffer));
		_sntprintf_s(buffer, 1024, 1024, TEXT("%d"), ((int)_tcstol(hexcharbuffer, &ptrend, 16)));

		usb_dev_info = (*env)->NewString(env, buffer, (jsize)_tcslen(buffer));
		if ((usb_dev_info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			(*env)->ExceptionClear(env);
			SetupDiDestroyDeviceInfoList(usb_dev_info_set);
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 3, 0, E_NEWSTRSTR);
			return NULL;
		}
		insert_jstrarraylist(&list, usb_dev_info);

		/* user might have connected more than one device of same VID/PID/serial, so get info about all */
		member_index++;
	}

	SetupDiDestroyDeviceInfoList(usb_dev_info_set);

	/* Create a JAVA/JNI style array of String object, populate it and return to java layer. */
	strClass = (*env)->FindClass(env, JAVALSTRING);
	if ((strClass == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		free_jstrarraylist(&list);
		throw_serialcom_exception(env, 3, 0, E_FINDCLASSSSTRINGSTR);
		return NULL;
	}

	usbDevicesFwVerFound = (*env)->NewObjectArray(env, (jsize)list.index, strClass, NULL);
	if ((usbDevicesFwVerFound == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		free_jstrarraylist(&list);
		throw_serialcom_exception(env, 3, 0, E_NEWOBJECTARRAYSTR);
		return NULL;
	}

	for (x = 0; x < list.index; x++) {
		(*env)->SetObjectArrayElement(env, usbDevicesFwVerFound, x, list.base[x]);
		if ((*env)->ExceptionOccurred(env)) {
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 3, 0, E_SETOBJECTARRAYSTR);
			return NULL;
		}
	}

	free_jstrarraylist(&list);
	return usbDevicesFwVerFound;
}