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
static const GUID GUID_DEVINTERFACE_SERENUM_BUS_ENUMERATOR = { 0x4D36E978, 0xE325, 0x11CE, 0xBF, 0xC1, 0x08, 0x00, 0x2B, 0xE1, 0x03, 0x18 };

/* {A5DCBF10-6530-11D2-901F-00C04FB951ED} USB device (excluding hub and host controller) */
static const GUID GUID_DEVINTERFACE_USB_DEVICE = { 0xA5DCBF10, 0x6530, 0x11D2, 0x90, 0x1F, 0x00, 0xC0, 0x4F, 0xB9, 0x51, 0xED };

/* {50906cb8-ba12-11d1-bf5d-0000f805f530} MultiportSerial (intelligent multiport serial cards) */
static const GUID GUID_MULTIPORT_SERIAL_ADAPTOR_DEVICE = { 0x50906CB8, 0xBA12, 0x11D1, 0xBF, 0x5D, 0x00, 0x00, 0xF8, 0x05, 0xF5, 0x30 };

/* USB device's interface functionality class */
static const DEVPROPKEY DEVPKEY_Device_ClassGuid = { 0xa45c254e, 0xdf1c, 0x4efd, 0x80, 0x20, 0x67, 0xd1, 0x46, 0xa8, 0x50, 0xe0, 10 };

/* 
 * Check if the given COM port name belongs to a USB-UART converter (CDC/ACM interface). If yes, find driver name 
 * and return it to caller. Iterate over all USB device's interface looking for CDC/ACM interface. When found
 * match COM port name, if matched get its driver name from regostry property.
 *
 * Return 1 if found, 0 if not found, -1 if an error occurs (also throws exception in this case).
 */
int get_driver_com_port_usb(JNIEnv *env, const jchar *port_name, TCHAR *driver_name) {

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

		/* for this device find its instance ID (USB\VID_04D8&PID_00DF\000098037)
		 * this is variable 'Device Instance Path' in device manager. */
		memset(buffer, '\0', 1024);
		ret = SetupDiGetDeviceInstanceId(usb_dev_info_set, &usb_dev_instance, buffer, 1024, &size);
		if (ret == FALSE) {
			SetupDiDestroyDeviceInfoList(usb_dev_info_set);
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

				/* match port name */
				ret = _tcsicmp(charbuffer, port_name);
				if (ret != 0) {
					usb_member_index++;
					continue;
				}

				memset(keybuf, '\0', 1024);
				_stprintf_s(keybuf, 1024, TEXT("SYSTEM\\CurrentControlSet\\Enum\\%s"), buffer);

				/* HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Enum\USB\VID_VID+PID_PID\Serial_Number\Service */
				charbuffer_size = sizeof(charbuffer);
				memset(charbuffer, '\0', 128);
				status = RegGetValue(HKEY_LOCAL_MACHINE, keybuf, TEXT("Service"), RRF_RT_REG_SZ, NULL, (PVOID)charbuffer, &charbuffer_size);
				if (status != ERROR_SUCCESS) {
					SetupDiDestroyDeviceInfoList(usb_dev_info_set);
					throw_serialcom_exception(env, 4, GetLastError(), NULL);
					return -1;
				}

				/* populate array to be returned to caller, return 1 to indicate driver found */
				memset(driver_name, '\0', 128);
				for (x = 0; x < _tcslen(charbuffer); x++) {
					driver_name[x] = charbuffer[x];
				}

				SetupDiDestroyDeviceInfoList(usb_dev_info_set);
				return 1;
			}else {
				SetupDiDestroyDeviceInfoList(usb_dev_info_set);
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
				_snprintf_s(cmerror, 256, 256, "CM_Get_Device_ID CR_xxxx error code : 0x%X\0", cmret);
				throw_serialcom_exception(env, 3, 0, cmerror);
				return -1;
			}

			memset(keybuf, '\0', 1024);
			_stprintf_s(keybuf, 1024, TEXT("SYSTEM\\CurrentControlSet\\Enum\\%s\\Device Parameters"), buffer);

			charbuffer_size = sizeof(charbuffer);
			memset(charbuffer, '\0', sizeof(charbuffer));
			status = RegGetValue(HKEY_LOCAL_MACHINE, keybuf, TEXT("PortName"), RRF_RT_REG_SZ, NULL, (PVOID)charbuffer, &charbuffer_size);
			if (status != ERROR_SUCCESS) {
				SetupDiDestroyDeviceInfoList(usb_dev_info_set);
				throw_serialcom_exception(env, 4, GetLastError(), NULL);
				return -1;
			}

			/* match port name and find driver name if port name matches */
			ret = _tcsicmp(charbuffer, port_name);
			if (ret == 0) {
				memset(keybuf, '\0', 1024);
				_stprintf_s(keybuf, 1024, TEXT("SYSTEM\\CurrentControlSet\\Enum\\%s"), buffer);

				/* HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Enum\USB\VID_VID+PID_PID\Serial_Number\Service */
				charbuffer_size = sizeof(charbuffer);
				memset(charbuffer, '\0', 128);
				status = RegGetValue(HKEY_LOCAL_MACHINE, keybuf, TEXT("Service"), RRF_RT_REG_SZ, NULL, (PVOID)charbuffer, &charbuffer_size);
				if (status != ERROR_SUCCESS) {
					SetupDiDestroyDeviceInfoList(usb_dev_info_set);
					throw_serialcom_exception(env, 4, GetLastError(), NULL);
					return -1;
				}

				/* populate array to be returned to caller */
				memset(driver_name, '\0', 128);
				for (x = 0; x < _tcslen(charbuffer); x++) {
					driver_name[x] = charbuffer[x];
				}

				/* clean up and return 1 to indicate driver found */
				SetupDiDestroyDeviceInfoList(usb_dev_info_set);
				return 1;
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
				throw_serialcom_exception(env, 4, GetLastError(), NULL);
				return -1;
			}

			/* match port name and find driver name if port name matches */
			ret = _tcsicmp(charbuffer, port_name);
			if (ret == 0) {
				memset(keybuf, '\0', 1024);
				_stprintf_s(keybuf, 1024, TEXT("SYSTEM\\CurrentControlSet\\Enum\\%s"), buffer);

				/* HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Enum\USB\VID_VID+PID_PID\Serial_Number\Service */
				charbuffer_size = sizeof(charbuffer);
				memset(charbuffer, '\0', 128);
				status = RegGetValue(HKEY_LOCAL_MACHINE, keybuf, TEXT("Service"), RRF_RT_REG_SZ, NULL, (PVOID)charbuffer, &charbuffer_size);
				if (status != ERROR_SUCCESS) {
					SetupDiDestroyDeviceInfoList(usb_dev_info_set);
					throw_serialcom_exception(env, 4, GetLastError(), NULL);
					return -1;
				}

				/* populate array to be returned to caller */
				memset(driver_name, '\0', 128);
				for (x = 0; x < _tcslen(charbuffer); x++) {
					driver_name[x] = charbuffer[x];
				}

				/* clean up and return 1 to indicate driver found */
				SetupDiDestroyDeviceInfoList(usb_dev_info_set);
				return 1;
			}

			/* set this sibling as base sibling for fetching next sibling, loop over to get and check next 
			   interface (sibling) */
			current_sibling = next_sibling;
		}

		/* increment to get and examine the next usb device for COM ports class */
		usb_member_index++;
	}

	/* reaching here means given COM port does not represent CDC/ACM interface and therefore does not belong to 
	   USB-UART IC. probably other usb-systems need to be examined for finding given COM port's driver */
	SetupDiDestroyDeviceInfoList(usb_dev_info_set); 
	return 0;
}

/*
 * Check if the given COM port name belongs to a multi port serial adaptor device. If yes, get its driver name
 * and return to caller.
 *
 * Return 1 if found, 0 if not found, -1 if an error occurs (also throws exception in this case).
 */
int get_driver_com_port_multiportadaptor(JNIEnv *env, const jchar *port_name, TCHAR *driver_name) {

	int x = 0;
	BOOL ret = FALSE;
	LONG status = 0;
	DWORD error_code = 0;
	DWORD size = 0;
	DWORD regproptype;
	DWORD charbuffer_size = 0;
	DWORD driver_name_size = 0;
	ULONG buffer_size = 0;
	DWORD multiport_member_index = 0;
	HDEVINFO multiport_dev_info_set;
	SP_DEVINFO_DATA multiport_dev_instance;
	ULONG devprop_buffer_size = 0;
	DEVPROPTYPE proptype;
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

	/* get information set for all multi port serial adaptor devices matching the GUID */
	multiport_dev_info_set = SetupDiGetClassDevs(&GUID_MULTIPORT_SERIAL_ADAPTOR_DEVICE, NULL, NULL, DIGCF_DEVICEINTERFACE);
	if (multiport_dev_info_set == INVALID_HANDLE_VALUE) {
		SetupDiDestroyDeviceInfoList(multiport_dev_info_set);
		throw_serialcom_exception(env, 4, HRESULT_FROM_SETUPAPI(GetLastError()), NULL);
		return -1;
	}

	/* enumerate all devices in this information set */
	multiport_member_index = 0;
	while (1) {
		ZeroMemory(&multiport_dev_instance, sizeof(multiport_dev_instance));
		multiport_dev_instance.cbSize = sizeof(multiport_dev_instance);

		/* from information set, get device by index */
		ret = SetupDiEnumDeviceInfo(multiport_dev_info_set, multiport_member_index, &multiport_dev_instance);
		if (ret == FALSE) {
			error_code = GetLastError();
			if (error_code == ERROR_NO_MORE_ITEMS) {
				break;
			}else {
				SetupDiDestroyDeviceInfoList(multiport_dev_info_set);
				throw_serialcom_exception(env, 4, HRESULT_FROM_SETUPAPI(error_code), NULL);
				return -1;
			}
		}

		/* for this device find its instance ID (USB\VID_04D8&PID_00DF\000098037)
		 * this is variable 'Device Instance Path' in device manager. */
		cmret = CM_Get_Device_ID(multiport_dev_instance.DevInst, buffer, 1024, 0);
		if (cmret != CR_SUCCESS) {
			SetupDiDestroyDeviceInfoList(multiport_dev_info_set);
			_snprintf_s(cmerror, 256, 256, "CM_Get_Device_ID CR_xxxx error code : 0x%X\0", cmret);
			throw_serialcom_exception(env, 3, 0, cmerror);
			return -1;
		}

		/* get its COM port name/number for this device */
		memset(keybuf, '\0', 1024);
		_stprintf_s(keybuf, 1024, TEXT("SYSTEM\\CurrentControlSet\\Enum\\%s\\Device Parameters"), buffer);

		charbuffer_size = sizeof(charbuffer);
		memset(charbuffer, '\0', 128);
		/* ignore error as some devices might not have portname registry key */
		status = RegGetValue(HKEY_LOCAL_MACHINE, keybuf, TEXT("PortName"), RRF_RT_REG_SZ, NULL, (PVOID)charbuffer, &charbuffer_size);
		if (status == ERROR_SUCCESS) {
			/* match port name */
			ret = _tcsicmp(charbuffer, port_name);
			if (ret == 0) {
				/* get driver name */
				memset(keybuf, '\0', 1024);
				_stprintf_s(keybuf, 1024, TEXT("SYSTEM\\CurrentControlSet\\Enum\\%s"), buffer);

				/* HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Enum\XXXX\XXX\XXX\Service */
				charbuffer_size = sizeof(charbuffer);
				memset(charbuffer, '\0', 128);
				status = RegGetValue(HKEY_LOCAL_MACHINE, keybuf, TEXT("Service"), RRF_RT_REG_SZ, NULL, (PVOID)charbuffer, &charbuffer_size);
				if (status != ERROR_SUCCESS) {
					SetupDiDestroyDeviceInfoList(multiport_dev_info_set);
					throw_serialcom_exception(env, 4, GetLastError(), NULL);
					return -1;
				}

				/* populate array to be returned to caller */
				memset(driver_name, '\0', 128);
				for (x = 0; x < _tcslen(charbuffer); x++) {
					driver_name[x] = charbuffer[x];
				}

				/* clean up and return 1 to indicate driver found */
				SetupDiDestroyDeviceInfoList(multiport_dev_info_set);
				return 1;
			}
		}

		/* increment to get and examine the next multiport device for COM ports class */
		multiport_member_index++;
	}

	/* reaching here means given COM port does not belong to multi-port serial adaptor device */
	SetupDiDestroyDeviceInfoList(multiport_dev_info_set);
	return 0;
}

/*
 * Last pass to find driver name. Iterate over all the devices associated with "Ports" class.
 * One by one get their device ID and check if this device has COM port we are searching.
 * If matched get the driver name and return to caller. Note that not all devices might have
 * "PortName" registry associated with them, so ignore them.
 * 
 * Return 1 if found, 0 if not found, -1 if an error occurs (also throws exception in this case).
 */
int get_driver_com_port_others(JNIEnv *env, const jchar *port_name, TCHAR *driver_name) {

	int x = 0;
	BOOL ret = FALSE;
	CONFIGRET cmret = 0;
	LONG status = 0;
	DWORD error_code = 0;
	DWORD charbuffer_size = 0;
	DWORD com_member_index = 0;
	HDEVINFO com_dev_info_set;
	SP_DEVINFO_DATA com_dev_instance;

	/* size of these buffers is hardcoded in functions using them */
	TCHAR buffer[1024];
	TCHAR keybuf[1024];
	TCHAR charbuffer[128];
	char cmerror[256];

	/* get information set for all multi port serial adaptor devices matching the GUID */
	com_dev_info_set = SetupDiGetClassDevs(&GUID_DEVINTERFACE_SERENUM_BUS_ENUMERATOR, NULL, NULL, DIGCF_ALLCLASSES);
	if (com_dev_info_set == INVALID_HANDLE_VALUE) {
		SetupDiDestroyDeviceInfoList(com_dev_info_set);
		throw_serialcom_exception(env, 4, HRESULT_FROM_SETUPAPI(GetLastError()), NULL);
		return -1;
	}

	/* enumerate all devices in this information set */
	com_member_index = 0;
	while (1) {
		ZeroMemory(&com_dev_instance, sizeof(com_dev_instance));
		com_dev_instance.cbSize = sizeof(com_dev_instance);

		/* from information set, get device by index */
		ret = SetupDiEnumDeviceInfo(com_dev_info_set, com_member_index, &com_dev_instance);
		if (ret == FALSE) {
			error_code = GetLastError();
			if (error_code == ERROR_NO_MORE_ITEMS) {
				break;
			}else {
				SetupDiDestroyDeviceInfoList(com_dev_info_set);
				throw_serialcom_exception(env, 4, HRESULT_FROM_SETUPAPI(error_code), NULL);
				return -1;
			}
		}

		cmret = CM_Get_Device_ID(com_dev_instance.DevInst, buffer, 1024, 0);
		if (cmret != CR_SUCCESS) {
			SetupDiDestroyDeviceInfoList(com_dev_info_set);
			_snprintf_s(cmerror, 256, 256, "CM_Get_Device_ID CR_xxxx error code : 0x%X\0", cmret);
			throw_serialcom_exception(env, 3, 0, cmerror);
			return -1;
		}

		/* get its COM port name/number for this device */
		memset(keybuf, '\0', 1024);
		_stprintf_s(keybuf, 1024, TEXT("SYSTEM\\CurrentControlSet\\Enum\\%s\\Device Parameters"), buffer);

		charbuffer_size = sizeof(charbuffer);
		memset(charbuffer, '\0', 128);
		/* ignore error as some devices might not have portname registry key */
		status = RegGetValue(HKEY_LOCAL_MACHINE, keybuf, TEXT("PortName"), RRF_RT_REG_SZ, NULL, (PVOID)charbuffer, &charbuffer_size);
		if (status == ERROR_SUCCESS) {
			/* match port name */
			ret = _tcsicmp(charbuffer, port_name);
			if (ret == 0) {
				/* get driver name */
				memset(keybuf, '\0', 1024);
				_stprintf_s(keybuf, 1024, TEXT("SYSTEM\\CurrentControlSet\\Enum\\%s"), buffer);

				/* HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Enum\XXXX\XXX\XXX\Service */
				charbuffer_size = sizeof(charbuffer);
				memset(charbuffer, '\0', 128);
				status = RegGetValue(HKEY_LOCAL_MACHINE, keybuf, TEXT("Service"), RRF_RT_REG_SZ, NULL, (PVOID)charbuffer, &charbuffer_size);
				if (status != ERROR_SUCCESS) {
					SetupDiDestroyDeviceInfoList(com_dev_info_set);
					throw_serialcom_exception(env, 4, GetLastError(), NULL);
					return -1;
				}

				/* populate array to be returned to caller */
				memset(driver_name, '\0', 128);
				for (x = 0; x < _tcslen(charbuffer); x++) {
					driver_name[x] = charbuffer[x];
				}

				/* clean up and return 1 to indicate driver found */
				SetupDiDestroyDeviceInfoList(com_dev_info_set);
				return 1;
			}
		}

		/* increment to get and examine the next device instance */
		com_member_index++;
	}

	/* reaching here means given COM port's driver not found */
	SetupDiDestroyDeviceInfoList(com_dev_info_set);
	return 0;
}

/*
 * Find the name of the driver which is currently associated with the given serial port.
 *
 * Typically, a driver service is a type of kernel-level filter driver implemented as a Windows service that 
 * enables applications to work with devices. Windows copies the .sys file to the %SystemRoot%\system32\drivers 
 * directory.
 * 
 * The GUID_DEVINTERFACE_SERENUM_BUS_ENUMERATOR is not used for USB devices due to 2 reasons : 
 * (1) Some devices may not have 'upper-level device filter driver (for ex. serenum)' that is used in conjunction with 
 *     Serial (or a vendor-supplied function driver) to provide the functionality of a Plug and Play bus driver for an 
 *     RS-232 port.
 * (2) For composite devices CDC/ACM interface specific function driver is to be found.
 * 
 * Return driver name string if driver found, empty string if not found, NULL if an error occur.
 */
jstring find_driver_for_given_com_port(JNIEnv *env, jstring comPortName) {

	int x = 0;
	const jchar* port_name = NULL;
	jstring driverfound = NULL;
	TCHAR driver_name[128];

	/* extract com port name to match (as an array of Unicode characters) */
	port_name = (*env)->GetStringChars(env, comPortName, JNI_FALSE);
	if ((port_name == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_GETSTRUTFCHARSTR);
		return NULL;
	}

	/* first pass : loop over USB-UART device enteries */
	x = get_driver_com_port_usb(env, port_name, driver_name);
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

	/* reaching here means given COM port does not belong to USB device.
	   second pass : loop over multiport serial adaptor device enteries */
	x = get_driver_com_port_multiportadaptor(env, port_name, driver_name);
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

	/* reaching here means given COM port does not belong to USB device.
	   last pass : loop over other device entires */
	x = get_driver_com_port_others(env, port_name, driver_name);
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

	/* reaching here means that the no driver was found for given COM port name.
	   return empty string */
	driverfound = (*env)->NewStringUTF(env, "");
	if ((driverfound == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		(*env)->ExceptionClear(env);
		throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
		return NULL;
	}
	
	return driverfound;
}
