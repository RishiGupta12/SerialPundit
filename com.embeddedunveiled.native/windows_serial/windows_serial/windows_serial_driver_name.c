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
#include <windows.h>
#include <setupapi.h>
#include <Cfgmgr32.h>
#include <jni.h>
#include "windows_serial_lib.h"

/* {4D36E978-E325-11CE-BFC1-08002BE10318} Ports (COM & LPT ports) */
static const GUID GUID_DEVINTERFACE_SERENUM_BUS_ENUMERATOR = { 0x4D36E978, 0xE325, 0x11CE, { 0xBF, 0xC1, 0x08, 0x00, 0x2B, 0xE1, 0x03, 0x18 } };

/* {A5DCBF10-6530-11D2-901F-00C04FB951ED} USB device (excluding hub and host controller) */
static const GUID GUID_DEVINTERFACE_USB_DEVICE = { 0xA5DCBF10, 0x6530, 0x11D2, 0x90, 0x1F, 0x00, 0xC0, 0x4F, 0xB9, 0x51, 0xED };

/* USB device's interface functionality class */
static const DEVPROPKEY DEVPKEY_Device_ClassGuid = { 0xa45c254e, 0xdf1c, 0x4efd, 0x80, 0x20, 0x67, 0xd1, 0x46, 0xa8, 0x50, 0xe0, 10 };

/* 
 * Check if the given COM port name belongs to a USB-UART converter (CDC/ACM interface). If yes, find driver name 
 * and return it to caller. Iterate over all USB device's interface looking for CDC/ACM interface. When found
 * match COM port name, if matched get its driver name from regostry property.
 *
 * Return 1 if found, 0 if not found, -1 if an error occurs (also throws exception in this case).
 */
int isCOMportOnUSB(JNIEnv *env, const jchar *port_name, TCHAR *driver_name) {

	BOOL ret = FALSE;
	LONG status = 0;
	DWORD error_code = 0;
	DWORD size = 0;
	DWORD regproptype;
	DWORD charbuffer_size = 0;
	DWORD usb_member_index = 0;
	HDEVINFO usb_dev_info_set;
	SP_DEVINFO_DATA usb_dev_instance;
	CONFIGRET cmret = 0;
	DEVINST firstchild = 0;
	DEVINST next_sibling = 0;
	DEVINST current_sibling = 0;

	/* size of these buffers is hardcoded in functions using them */
	TCHAR buffer[1024];
	TCHAR devprop_buffer[1024];
	TCHAR keybuf[1024];
	TCHAR charbuffer[128];
	char cmerror[256];

	/* get information set for all usb devices matching the GUID */
	usb_dev_info_set = SetupDiGetClassDevs(&GUID_DEVINTERFACE_USB_DEVICE, NULL, NULL, DIGCF_PRESENT | DIGCF_DEVICEINTERFACE);
	if (usb_dev_info_set == INVALID_HANDLE_VALUE) {
		SetupDiDestroyDeviceInfoList(usb_dev_info_set);
		throw_serialcom_exception(env, 4, HRESULT_FROM_SETUPAPI(GetLastError()), NULL);
		return -1;
	}

	/* enumerate all devices in this information set */
	usb_member_index = 0;
	while (1) {
		ZeroMemory(&usb_dev_instance, sizeof(usb_dev_instance));
		usb_dev_instance.cbSize = sizeof(usb_dev_instance);

		/* from information set, get device by index */
		ret = SetupDiEnumDeviceInfo(usb_dev_info_set, usb_member_index, &usb_dev_instance);
		if (ret == FALSE) {
			error_code = GetLastError();
			if (error_code == ERROR_NO_MORE_ITEMS) {
				break;
			}else {
				SetupDiDestroyDeviceInfoList(usb_dev_info_set);
				throw_serialcom_exception(env, 4, HRESULT_FROM_SETUPAPI(error_code), NULL);
				return -1;
			}
		}

		_tprintf(TEXT("0 %d\n"), 0);

		cmret = CM_Get_Child(&firstchild, usb_dev_instance.DevInst, 0);
		if (cmret != CR_SUCCESS) {
			if (cmret == CR_NO_SUCH_DEVNODE) {
				_tprintf(TEXT("01 %d\n"), 01);
				/* this device does not have any child, so check if this is a "Ports" class device or not */
				memset(devprop_buffer, '\0', 1024);
				ret = SetupDiGetDeviceRegistryProperty(usb_dev_info_set, &usb_dev_instance, SPDRP_CLASSGUID, &regproptype, (BYTE *)devprop_buffer, sizeof(devprop_buffer), &size);
				if (ret == FALSE) {
					SetupDiDestroyDeviceInfoList(usb_dev_info_set);
					throw_serialcom_exception(env, 4, HRESULT_FROM_SETUPAPI(GetLastError()), NULL);
					return -1;
				}

				/* match GUID */
				ret = _tcsicmp(devprop_buffer, TEXT("{4D36E978-E325-11CE-BFC1-08002BE10318}"));
				if (ret != 0) {
					usb_member_index++;
					continue;
				}

				/* reaching here means that the device is COM port device (CDC/ACM), get its COM port name/number
				 * HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Enum\FTDIBUS\VID_VID+PID_PID+Serial_Number\0000\DeviceParameters\PortName
				 * HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Enum\USB\VID_VID+PID_PID\Serial_Number\DeviceParameters\PortName */
				memset(keybuf, '\0', 1024);
				_stprintf_s(keybuf, 1024, TEXT("SYSTEM\\CurrentControlSet\\Enum\\%s\\Device Parameters"), buffer);

				charbuffer_size = sizeof(charbuffer);
				memset(charbuffer, '\0', 128);
				status = RegGetValue(HKEY_LOCAL_MACHINE, keybuf, TEXT("PortName"), RRF_RT_REG_SZ, NULL, (PVOID)charbuffer, &charbuffer_size);
				if (status != ERROR_SUCCESS) {
					SetupDiDestroyDeviceInfoList(usb_dev_info_set);
					throw_serialcom_exception(env, 4, GetLastError(), NULL);
					return -1;
				}

				_tprintf(TEXT("1 %d\n"), 1);
				/* match port name */
				ret = _tcsicmp(charbuffer, port_name);
				if (ret != 0) {
					usb_member_index++;
					continue;
				}

				_tprintf(TEXT("2 %d\n"), 2);

				memset(keybuf, '\0', 1024);
				_stprintf_s(keybuf, 1024, TEXT("SYSTEM\\CurrentControlSet\\Enum\\%s"), buffer);

				_tprintf(TEXT("%s\n"), buffer);

				charbuffer_size = sizeof(charbuffer);
				memset(charbuffer, '\0', 128);
				status = RegGetValue(HKEY_LOCAL_MACHINE, keybuf, TEXT("Service"), RRF_RT_REG_SZ, NULL, (PVOID)charbuffer, &charbuffer_size);
				if (status != ERROR_SUCCESS) {
					_tprintf(TEXT("1 %d\n"), 1);
					SetupDiDestroyDeviceInfoList(usb_dev_info_set);
					throw_serialcom_exception(env, 4, GetLastError(), NULL);
					return -1;
				}

				_tprintf(TEXT("%s\n"), keybuf);
			}else {
				_tprintf(TEXT("442 %d\n"), 42);
			}
		}

		/* increment to get and examine the next usb device for COM ports class */
		usb_member_index++;
	}

	return 1;
}

/*
 * Find the name of the driver which is currently associated with the given serial port.
 *
 * Typically, a driver service is a type of kernel-level filter driver implemented as a Windows service that 
 * enables applications to work with devices. Windows copies the .sys file to the %SystemRoot%\system32\drivers 
 * directory.
 * 
 * The GUID_DEVINTERFACE_SERENUM_BUS_ENUMERATOR is not used for USB devices due to 2 reasons : (1) Some devices 
 * like MCP200 may not get listed properly in device registry. (2) For composite devices CDC/ACM interface specific
 * function driver need to be found.
 * 
 * Return driver name string if driver found, empty string if not found, NULL if an error occur.
 */
jstring find_driver_for_given_com_port(JNIEnv *env, jstring comPortName) {

	int x = 0;
	const jchar* port_name = NULL;
	jstring driverfound = NULL;
	TCHAR driver_name[64];

	/* extract serial number (as an array of Unicode characters) */
	port_name = (*env)->GetStringChars(env, comPortName, JNI_FALSE);
	if ((port_name == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_GETSTRUTFCHARSTR);
		return NULL;
	}

	/* first loop over USB-UART device enteries */
	x = isCOMportOnUSB(env, port_name, driver_name);
	if (x == 1) {
		driverfound = (*env)->NewString(env, driver_name, (jsize) _tcslen(driver_name));
		if ((driverfound == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			(*env)->ExceptionClear(env);
			throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
			return NULL;
		}
		return driverfound;
	}else if (x < 0) {
		return NULL;
	}else {
	}

	/* reaching here means given COM port does not belong to USB device */

	return NULL;
}
