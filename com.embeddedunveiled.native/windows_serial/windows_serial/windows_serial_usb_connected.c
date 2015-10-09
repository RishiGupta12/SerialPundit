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
 * Finds information about USB devices using operating system specific facilities and API.
 * The sequence of entries in array must match with what java layer expect. If a particular USB attribute
 * is not set in descriptor or can not be obtained "---" is placed in its place.
 *
 * Returns 1 if device is connected, returns 0 if not connected, -1 if an error occurs.
 */
jint is_usb_dev_connected(JNIEnv *env, jint usbvid_to_match, jint usbpid_to_match, jstring serial_number) {

	int x = 0;
	int i = 0;
	int vid = 0;
	int pid = 0;
	BOOL ret = FALSE;
	DWORD size = 0;
	DWORD error_code = 0;
	DWORD member_index = 0;
	DWORD regprop_buf_size = 0;
	HDEVINFO dev_info_set;
	SP_DEVINFO_DATA dev_instance;
	TCHAR buffer[1024];
	TCHAR hexcharbuffer[64];
	TCHAR *ptrend;
	const jchar* serial = NULL;

	/* get information set for all devices matching the GUID */
	dev_info_set = SetupDiGetClassDevs(&GUID_DEVINTERFACE_USB_DEVICE, NULL, 0, DIGCF_DEVICEINTERFACE | DIGCF_PRESENT);
	if (dev_info_set == INVALID_HANDLE_VALUE) {
		SetupDiDestroyDeviceInfoList(dev_info_set);
		throw_serialcom_exception(env, 4, HRESULT_FROM_SETUPAPI(GetLastError()), NULL);
		return -1;
	}

	/* enumerate all devices in this information set */
	while (1) {
		ZeroMemory(&dev_instance, sizeof(dev_instance));
		dev_instance.cbSize = sizeof(dev_instance);

		/* from information set get device device by index */
		ret = SetupDiEnumDeviceInfo(dev_info_set, member_index, &dev_instance);
		if (ret == FALSE) {
			error_code = GetLastError();
			if (error_code == ERROR_NO_MORE_ITEMS) {
				break;
			}else {
				SetupDiDestroyDeviceInfoList(dev_info_set);
				throw_serialcom_exception(env, 4, HRESULT_FROM_SETUPAPI(GetLastError()), NULL);
				return -1;
			}
		}

		/* for this device find its instance ID (USB\VID_04D8&PID_00DF\000098037)
		 * this is variable 'Device Instance Path' in device manager. */
		memset(buffer, '\0', 1024);
		ret = SetupDiGetDeviceInstanceId(dev_info_set, &dev_instance, buffer, 1024, &size);
		if (ret == FALSE) {
			SetupDiDestroyDeviceInfoList(dev_info_set);
			throw_serialcom_exception(env, 4, HRESULT_FROM_SETUPAPI(GetLastError()), NULL);
			return -1;
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
		vid = (int) _tcstol(hexcharbuffer, &ptrend, 16);
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

		/* extract serial number and match if requested by application */
		if (serial_number != NULL) {
			serial = (*env)->GetStringChars(env, serial_number, JNI_FALSE);
			if ((serial == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
				SetupDiDestroyDeviceInfoList(dev_info_set);
				throw_serialcom_exception(env, 3, 0, E_GETSTRUTFCHARSTR);
				return -1;
			}
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

		/* reaching here means device is connected to system at present which matches given criteria. */
		SetupDiDestroyDeviceInfoList(dev_info_set);
		return 1;
	}

	/* reaching here means device is not connected to system at present which matches given criteria. */
	SetupDiDestroyDeviceInfoList(dev_info_set);
	return 0;
}
