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

/* {A5DCBF10-6530-11D2-901F-00C04FB951ED} USB device (excluding hub and host controller) */
static const GUID GUID_DEVINTERFACE_USB_DEVICE = { 0xA5DCBF10, 0x6530, 0x11D2, 0x90, 0x1F, 0x00, 0xC0, 0x4F, 0xB9, 0x51, 0xED };

/*
 * Get the current latency timer value for ftdi devices.
 * return value read on success or -1 if any error occurs.
 */
jint get_latency_timer_value(JNIEnv *env, jstring comPortName) {

	const jchar* port_name = NULL;
	int x = 0;
	BOOL ret = FALSE;
	LONG status = 0;
	DWORD error_code = 0;
	DWORD size = 0;
	DWORD regproptype;
	DWORD charbuffer_size = 0;
	DWORD driver_name_size = 0;
	ULONG buffer_size = 0;
	DWORD usb_member_index = 0;
	HDEVINFO usb_dev_info_set;
	SP_DEVINFO_DATA usb_dev_instance;
	ULONG devprop_buffer_size = 0;
	DEVPROPTYPE proptype;
	CONFIGRET cmret = 0;
	DEVINST firstchild = 0;
	DEVINST next_sibling = 0;
	DEVINST current_sibling = 0;
	DWORD timer_value;
	DWORD timervalue_size;

	/* size of these buffers is hardcoded in functions using them */
	TCHAR buffer[1024];
	TCHAR devprop_buffer[1024];
	TCHAR keybuf[1024];
	TCHAR charbuffer[128];
	char cmerror[256];

	/* extract com port name to match (as an array of Unicode characters) */
	port_name = (*env)->GetStringChars(env, comPortName, JNI_FALSE);
	if ((port_name == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_GETSTRUTFCHARSTR);
		return -1;
	}

	/* get information set for all usb devices matching the GUID */
	usb_dev_info_set = SetupDiGetClassDevs(&GUID_DEVINTERFACE_USB_DEVICE, NULL, NULL, DIGCF_PRESENT | DIGCF_DEVICEINTERFACE);
	if (usb_dev_info_set == INVALID_HANDLE_VALUE) {
		SetupDiDestroyDeviceInfoList(usb_dev_info_set);
		(*env)->ReleaseStringChars(env, comPortName, port_name);
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
			}
			else {
				SetupDiDestroyDeviceInfoList(usb_dev_info_set);
				(*env)->ReleaseStringChars(env, comPortName, port_name);
				throw_serialcom_exception(env, 4, HRESULT_FROM_SETUPAPI(error_code), NULL);
				return -1;
			}
		}

		/* for this device find its instance ID (USB\VID_04D8&PID_00DF\000098037)
		 * this is variable 'Device Instance Path' in device manager. */
		memset(buffer, '\0', 1024);
		ret = SetupDiGetDeviceInstanceId(usb_dev_info_set, &usb_dev_instance, buffer, 1024, &size);
		if (ret == FALSE) {
			SetupDiDestroyDeviceInfoList(usb_dev_info_set);
			(*env)->ReleaseStringChars(env, comPortName, port_name);
			throw_serialcom_exception(env, 4, HRESULT_FROM_SETUPAPI(GetLastError()), NULL);
			return -1;
		}

		cmret = CM_Get_Child(&firstchild, usb_dev_instance.DevInst, 0);
		if (cmret != CR_SUCCESS) {
			if (cmret == CR_NO_SUCH_DEVNODE) {
				/* this device does not have any child, so check if this device itself is a "Ports" class device or not */
				memset(devprop_buffer, '\0', 1024);
				ret = SetupDiGetDeviceRegistryProperty(usb_dev_info_set, &usb_dev_instance, SPDRP_CLASSGUID, &regproptype, (BYTE *)devprop_buffer, sizeof(devprop_buffer), &size);
				if (ret == FALSE) {
					SetupDiDestroyDeviceInfoList(usb_dev_info_set);
					(*env)->ReleaseStringChars(env, comPortName, port_name);
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
					(*env)->ReleaseStringChars(env, comPortName, port_name);
					throw_serialcom_exception(env, 4, GetLastError(), NULL);
					return -1;
				}

				/* match port name */
				ret = _tcsicmp(charbuffer, port_name);
				if (ret != 0) {
					usb_member_index++;
					continue;
				}

				/* HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Enum\FTDIBUS\VID_VID+PID_PID+Serial_Number\0000\DeviceParameters\LatencyTimer */
				status = RegGetValue(HKEY_LOCAL_MACHINE, keybuf, TEXT("LatencyTimer"), RRF_RT_REG_DWORD, NULL, &timer_value, &timervalue_size);
				if (status != ERROR_SUCCESS) {
					SetupDiDestroyDeviceInfoList(usb_dev_info_set);
					(*env)->ReleaseStringChars(env, comPortName, port_name);
					throw_serialcom_exception(env, 4, status, NULL);
					return -1;
				}

				SetupDiDestroyDeviceInfoList(usb_dev_info_set);
				(*env)->ReleaseStringChars(env, comPortName, port_name);
				return (jint)timer_value;
			}else {
				SetupDiDestroyDeviceInfoList(usb_dev_info_set);
				(*env)->ReleaseStringChars(env, comPortName, port_name);
				_snprintf_s(cmerror, 256, 256, "CM_Get_DevNode_Registry_Property CR_xxxx error code : 0x%X\0", cmret);
				throw_serialcom_exception(env, 3, 0, cmerror);
				return -1;
			}
		}

		/* reaching here means that this USB device has at-least one child device node, examine child now */

		devprop_buffer_size = sizeof(devprop_buffer);
		memset(devprop_buffer, '\0', sizeof(devprop_buffer));
		cmret = CM_Get_DevNode_Registry_Property(firstchild, CM_DRP_CLASSGUID, &proptype, (PVOID)devprop_buffer, &devprop_buffer_size, 0);
		if (cmret != CR_SUCCESS) {
			SetupDiDestroyDeviceInfoList(usb_dev_info_set);
			(*env)->ReleaseStringChars(env, comPortName, port_name);
			_snprintf_s(cmerror, 256, 256, "CM_Get_DevNode_Registry_Property CR_xxxx error code : 0x%X\0", cmret);
			throw_serialcom_exception(env, 3, 0, cmerror);
			return -1;
		}

		/* match GUID */
		ret = _tcsicmp(devprop_buffer, TEXT("{4D36E978-E325-11CE-BFC1-08002BE10318}"));
		if (ret == 0) {
			/* reaching here means that the child device is COM port device (CDC/ACM), get its COM port name/number */
			memset(buffer, '\0', 1024);
			cmret = CM_Get_Device_ID(firstchild, buffer, 1024, 0);
			if (cmret != CR_SUCCESS) {
				SetupDiDestroyDeviceInfoList(usb_dev_info_set);
				(*env)->ReleaseStringChars(env, comPortName, port_name);
				_snprintf_s(cmerror, 256, 256, "CM_Get_Device_ID CR_xxxx error code : 0x%X\0", cmret);
				throw_serialcom_exception(env, 3, 0, cmerror);
				return -1;
			}

			memset(keybuf, '\0', 1024);
			_stprintf_s(keybuf, 1024, TEXT("SYSTEM\\CurrentControlSet\\Enum\\%s\\Device Parameters"), buffer);

			charbuffer_size = sizeof(charbuffer);
			memset(charbuffer, '\0', 128);

			status = RegGetValue(HKEY_LOCAL_MACHINE, keybuf, TEXT("PortName"), RRF_RT_REG_SZ, NULL, (PVOID)charbuffer, &charbuffer_size);
			if (status != ERROR_SUCCESS) {
				SetupDiDestroyDeviceInfoList(usb_dev_info_set);
				(*env)->ReleaseStringChars(env, comPortName, port_name);
				throw_serialcom_exception(env, 4, GetLastError(), NULL);
				return -1;
			}

			/* match port name */
			ret = _tcsicmp(charbuffer, port_name);
			if (ret == 0) {
				status = RegGetValue(HKEY_LOCAL_MACHINE, keybuf, TEXT("LatencyTimer"), RRF_RT_REG_DWORD, NULL, &timer_value, &timervalue_size);
				if (status != ERROR_SUCCESS) {
					SetupDiDestroyDeviceInfoList(usb_dev_info_set);
					(*env)->ReleaseStringChars(env, comPortName, port_name);
					throw_serialcom_exception(env, 4, GetLastError(), NULL);
					return -1;
				}

				/* clean up and return timer value found */
				SetupDiDestroyDeviceInfoList(usb_dev_info_set);
				(*env)->ReleaseStringChars(env, comPortName, port_name);
				return (jint)timer_value;
			}
		}

		/* reaching here means first child of this USB device was not CDC/ACM interface, need to
		   iterate over all the interfaces (siblings) this device has examining them for matching our criteria */
		current_sibling = firstchild;
		while (1) {
			cmret = CM_Get_Sibling(&next_sibling, current_sibling, 0);
			if (cmret != CR_SUCCESS) {
				if (cmret == CR_NO_SUCH_DEVNODE) {
					/* done iterating over all interfaces, move to next examine next USB device */
					break;
				}
				else {
					SetupDiDestroyDeviceInfoList(usb_dev_info_set);
					_snprintf_s(cmerror, 256, 256, "CM_Get_Sibling failed with CR_xxxx error code : 0x%X\0", cmret);
					(*env)->ReleaseStringChars(env, comPortName, port_name);
					throw_serialcom_exception(env, 3, 0, cmerror);
					return -1;
				}

			}

			/* reaching here means USB device has more than 1 interfaces, get class of this interface (sibling) */
			devprop_buffer_size = sizeof(devprop_buffer);
			memset(devprop_buffer, '\0', sizeof(devprop_buffer));
			cmret = CM_Get_DevNode_Registry_Property(next_sibling, CM_DRP_CLASSGUID, &proptype, (VOID *)devprop_buffer, &devprop_buffer_size, 0);
			if (cmret != CR_SUCCESS) {
				SetupDiDestroyDeviceInfoList(usb_dev_info_set);
				_snprintf_s(cmerror, 256, 256, "CM_Get_DevNode_Registry_Property failed with CR_xxxx error code : 0x%X\0", cmret);
				(*env)->ReleaseStringChars(env, comPortName, port_name);
				throw_serialcom_exception(env, 3, 0, cmerror);
				return -1;
			}

			/* match GUID for this sibling */
			ret = _tcsicmp(devprop_buffer, TEXT("{4D36E978-E325-11CE-BFC1-08002BE10318}"));
			if (ret != 0) {
				/* this interface is not CDC/ACM, loop over to next interface */
				current_sibling = next_sibling;
				continue;
			}

			/* reaching here means that this sibling (interface) is a CDC/ACM type, get its COM port name/number */
			buffer_size = (ULONG)_tcslen(buffer);
			memset(buffer, '\0', 1024);
			cmret = CM_Get_Device_ID(next_sibling, buffer, 1024, 0);
			if (cmret != CR_SUCCESS) {
				SetupDiDestroyDeviceInfoList(usb_dev_info_set);
				_snprintf_s(cmerror, 256, 256, "CM_Get_Device_ID failed with CR_xxxx error code : 0x%X\0", cmret);
				(*env)->ReleaseStringChars(env, comPortName, port_name);
				throw_serialcom_exception(env, 3, 0, cmerror);
				return -1;
			}

			memset(keybuf, '\0', 1024);
			_stprintf_s(keybuf, 1024, TEXT("SYSTEM\\CurrentControlSet\\Enum\\%s\\Device Parameters"), buffer);

			charbuffer_size = sizeof(charbuffer);
			memset(charbuffer, '\0', 128);
			status = RegGetValue(HKEY_LOCAL_MACHINE, keybuf, TEXT("PortName"), RRF_RT_REG_SZ, NULL, (PVOID)charbuffer, &charbuffer_size);
			if (status != ERROR_SUCCESS) {
				SetupDiDestroyDeviceInfoList(usb_dev_info_set);
				(*env)->ReleaseStringChars(env, comPortName, port_name);
				throw_serialcom_exception(env, 4, GetLastError(), NULL);
				return -1;
			}

			/* match port name */
			ret = _tcsicmp(charbuffer, port_name);
			if (ret == 0) {
				status = RegGetValue(HKEY_LOCAL_MACHINE, keybuf, TEXT("LatencyTimer"), RRF_RT_REG_DWORD, NULL, &timer_value, &timervalue_size);
				if (status != ERROR_SUCCESS) {
					SetupDiDestroyDeviceInfoList(usb_dev_info_set);
					(*env)->ReleaseStringChars(env, comPortName, port_name);
					throw_serialcom_exception(env, 4, GetLastError(), NULL);
					return -1;
				}

				/* clean up and return timer value found */
				SetupDiDestroyDeviceInfoList(usb_dev_info_set);
				(*env)->ReleaseStringChars(env, comPortName, port_name);
				return (jint)timer_value;
			}

			/* set this sibling as base sibling for fetching next sibling, loop over to get and check next
			interface (sibling) */
			current_sibling = next_sibling;
		}

		/* increment to get and examine the next usb device for COM ports class */
		usb_member_index++;
	}

	/* reaching here means that probably given com port does not represent ftdi device or does not exist at all */
	SetupDiDestroyDeviceInfoList(usb_dev_info_set);
	(*env)->ReleaseStringChars(env, comPortName, port_name);
	throw_serialcom_exception(env, 3, 0, E_NOTFTDIPORT);
	return -1;
}

/*
 * Set the latency timer value for ftdi devices to fine tune performance.
 */
jint set_latency_timer_value(JNIEnv *env, jstring comPortName, jbyte timerValue) {

	const char *com_port_to_match = NULL;

	com_port_to_match = (*env)->GetStringUTFChars(env, comPortName, NULL);
	if ((com_port_to_match == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_GETSTRUTFCHARSTR);
		return -1;
	}

	(*env)->ReleaseStringUTFChars(env, comPortName, com_port_to_match);

	/* reaching here means given com port does not represent ftdi device, throw exception */
	throw_serialcom_exception(env, 3, 0, E_NOTFTDIPORT);
	return -1;
}
