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
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <tchar.h>
#include <windows.h>
#include <setupapi.h>
#include <Cfgmgr32.h>
#include <jni.h>
#include "windows_hid.h"

/* {A5DCBF10-6530-11D2-901F-00C04FB951ED} USB device (excluding hub and host controller) */
static const GUID GUID_DEVINTERFACE_USB_DEVICE = { 0xA5DCBF10, 0x6530, 0x11D2, 0x90, 0x1F, 0x00, 0xC0, 0x4F, 0xB9, 0x51, 0xED };

/* {4D1E55B2-F16F-11CF-88CB-001111000030} Drivers for HID collections register instances of this
device interface class to notify the operating system and applications of the presence of HID
collections. The system-supplied HID class driver registers an instance of this device interface
class for a HID collection. For example, the HID class driver registers an interface for a USB
keyboard or mouse device.*/
static const GUID GUID_DEVINTERFACE_HID = { 0X4D1E55B2, 0XF16F, 0X11CF, 0X88, 0XCB, 0X00, 0X11, 0X11, 0X00, 0X00, 0X30 };

/* System defined device property keys */
static const DEVPROPKEY DEVPKEY_Device_BusReportedDeviceDesc = { 0x540b947e, 0x8b40, 0x45bc, 0xa8, 0xa2, 0x6a, 0x0b, 0x89, 0x4c, 0xbd, 0xa2, 4 };
static const DEVPROPKEY DEVPKEY_Device_Manufacturer = { 0xa45c254e, 0xdf1c, 0x4efd, 0x80, 0x20, 0x67, 0xd1, 0x46, 0xa8, 0x50, 0xe0, 13 };

/*
 * Find the required information about given HID device.
 *
 * @return required information string if found, empty string if required information is not
 *         provided by underlying system, NULL if any error occurs.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
jstring get_hiddev_info_string(JNIEnv *env, jlong handle, int info_required) {

	int q = 0;
	int i = 0;
	int x = 0;
	int result = 0;
	BOOL ret = FALSE;
	LONG status = 0;
	DWORD error_code = 0;
	DWORD errorVal = 0;
	DWORD size = 0;
	DWORD charbuffer_size = 0;
	DWORD driver_name_size = 0;
	ULONG buffer_size = 0;
	ULONG devprop_buffer_size = 0;
	DEVPROPTYPE proptype;
	DWORD regproptype;

	CONFIGRET cmret = 0;
	DEVINST firstchild = 0;
	DEVINST next_sibling = 0;
	DEVINST current_sibling = 0;

	DWORD hid_member_index = 0;
	HDEVINFO hid_dev_info_set;
	SP_DEVINFO_DATA hid_dev_instance;

	DWORD usb_member_index = 0;
	HDEVINFO usb_dev_info_set;
	SP_DEVINFO_DATA usb_dev_instance;

	/* size of these buffers is hardcoded in functions using them */
	TCHAR buffer[1024];
	TCHAR devprop_buffer[1024];
	TCHAR keybuf[1024];
	TCHAR charbuffer[512];
	char cmerror[256];
	TCHAR hwid_to_match[512];

	jclass strClass = NULL;
	jstring requiredInfo;
	struct hid_dev_info* info = (struct hid_dev_info*) handle;

	/* find hardware ID of the given device by enumerating information set */
	hid_dev_info_set = SetupDiGetClassDevs(&GUID_DEVINTERFACE_HID, NULL, NULL, DIGCF_DEVICEINTERFACE);
	if (hid_dev_info_set == INVALID_HANDLE_VALUE) {
		SetupDiDestroyDeviceInfoList(hid_dev_info_set);
		throw_serialcom_exception(env, 4, HRESULT_FROM_SETUPAPI(GetLastError()), NULL);
		return NULL;
	}

	/* enumerate all devices in this information set starting from 0th index */
	hid_member_index = 0;
	while (1) {
		ZeroMemory(&hid_dev_instance, sizeof(hid_dev_instance));
		hid_dev_instance.cbSize = sizeof(hid_dev_instance);

		/* from information set, get device by index */
		ret = SetupDiEnumDeviceInfo(hid_dev_info_set, hid_member_index, &hid_dev_instance);
		if (ret == FALSE) {
			error_code = GetLastError();
			if (error_code == ERROR_NO_MORE_ITEMS) {
				break;
			}else {
				SetupDiDestroyDeviceInfoList(hid_dev_info_set);
				throw_serialcom_exception(env, 4, HRESULT_FROM_SETUPAPI(GetLastError()), NULL);
				return NULL;
			}
		}

		/* for this device find its instance ID, for example; HID\VID_04D8&PID_00DF&MI_02\7&33842C3F&0&0000
		 * this is variable 'Device Instance Path' in device manager. */
		memset(buffer, '\0', 1024);
		ret = SetupDiGetDeviceInstanceId(hid_dev_info_set, &hid_dev_instance, buffer, 1024, &size);
		if (ret == FALSE) {
			SetupDiDestroyDeviceInfoList(hid_dev_info_set);
			throw_serialcom_exception(env, 4, HRESULT_FROM_SETUPAPI(GetLastError()), NULL);
			return NULL;
		}

		/* check if device instance ID matches */
		ret = _tcsicmp(buffer, info->instance);
		if (ret != 0) {
			hid_member_index++;
			continue;
		}

		/* get HardwareID of this device;
		   HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Enum\HID\VID_04D8&PID_00DF&MI_02\7&33842C3F&0&0000\HardwareID */
		memset(keybuf, '\0', 1024);
		_stprintf_s(keybuf, 1024, TEXT("SYSTEM\\CurrentControlSet\\Enum\\%s"), buffer);

		/* HID\VID_04D8&PID_00DF&REV_0101&MI_02 */
		charbuffer_size = sizeof(charbuffer);
		memset(charbuffer, '\0', 512);
		status = RegGetValue(HKEY_LOCAL_MACHINE, keybuf, TEXT("HardwareID"), RRF_RT_REG_MULTI_SZ, NULL, (PVOID)charbuffer, &charbuffer_size);
		if (status != ERROR_SUCCESS) {
			SetupDiDestroyDeviceInfoList(hid_dev_info_set);
			throw_serialcom_exception(env, 4, HRESULT_FROM_SETUPAPI(GetLastError()), NULL);
			return NULL;
		}

		/* save hardware ID for later use */
		for (x = 0; x < 512; x++) {
			hwid_to_match[x] = charbuffer[x];
		}

		break;
	}

	/* release HID info set as it is no longer needed */
	SetupDiDestroyDeviceInfoList(hid_dev_info_set);


	/* ~~~~~~~~~~~~~~~~~~~~~~~~ Try HID over USB ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */


	/* get information set for all usb devices matching the GUID */
	usb_dev_info_set = SetupDiGetClassDevs(&GUID_DEVINTERFACE_USB_DEVICE, NULL, NULL, DIGCF_PRESENT | DIGCF_DEVICEINTERFACE);
	if (usb_dev_info_set == INVALID_HANDLE_VALUE) {
		SetupDiDestroyDeviceInfoList(usb_dev_info_set);
		throw_serialcom_exception(env, 4, HRESULT_FROM_SETUPAPI(GetLastError()), NULL);
		return NULL;
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
				throw_serialcom_exception(env, 4, HRESULT_FROM_SETUPAPI(GetLastError()), NULL);
				return NULL;
			}
		}

		/* for this device find its instance ID (USB\VID_04D8&PID_00DF\000098037)
		 * this is the variable 'Device Instance Path' in device manager. */
		memset(buffer, '\0', sizeof(buffer));
		ret = SetupDiGetDeviceInstanceId(usb_dev_info_set, &usb_dev_instance, buffer, 1024, &size);
		if (ret == FALSE) {
			SetupDiDestroyDeviceInfoList(usb_dev_info_set);
			throw_serialcom_exception(env, 4, HRESULT_FROM_SETUPAPI(GetLastError()), NULL);
			return NULL;
		}

		/* fetch and examine USB interface */
		cmret = CM_Get_Child(&firstchild, usb_dev_instance.DevInst, 0);
		if (cmret != CR_SUCCESS) {
			if (cmret == CR_NO_SUCH_DEVNODE) {
				/* this device does not have any child, so check if this is a HID class device or not */
				memset(devprop_buffer, '\0', 1024);
				ret = SetupDiGetDeviceRegistryProperty(usb_dev_info_set, &usb_dev_instance, SPDRP_CLASSGUID, &regproptype, (BYTE *)devprop_buffer, sizeof(devprop_buffer), &size);
				if (ret == FALSE) {
					SetupDiDestroyDeviceInfoList(usb_dev_info_set);
					throw_serialcom_exception(env, 4, HRESULT_FROM_SETUPAPI(GetLastError()), NULL);
					return NULL;
				}

				/* check if this is a USB HID interface */
				ret = _tcsicmp(devprop_buffer, TEXT("{745A17A0-74D3-11D0-B6FE-00A0C90F57DA}"));
				if (ret != 0) {
					usb_member_index++;
					continue;
				}

				/* reaching here means that this is a USB HID interface, get its device instance path.
				   ID : USB\VID_04D8&PID_00DF&REV_0101&MI_02 */
				memset(devprop_buffer, '\0', 1024);
				cmret = CM_Get_Device_ID(firstchild, devprop_buffer, 1024, 0);
				if (cmret != CR_SUCCESS) {
					_snprintf_s(cmerror, 256, 256, "CM_Get_Device_ID failed with CR_xxxx error code : 0x%X\0", cmret);
					SetupDiDestroyDeviceInfoList(usb_dev_info_set);
					throw_serialcom_exception(env, 3, 0, cmerror);
					return NULL;
				}

				/* get the HardwareID of this USB HID interface. HardwareID is multi-sz, however
				   we use only 1st string from multi-string HardwareID for our matching.
				   HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Enum\USB\VID_04D8&PID_00DF&MI_02\6&19D88AD8&0&0002\HardwareID */
				memset(keybuf, '\0', 1024);
				_stprintf_s(keybuf, 1024, TEXT("SYSTEM\\CurrentControlSet\\Enum\\%s"), devprop_buffer);

				charbuffer_size = sizeof(charbuffer);
				memset(charbuffer, '\0', 512);

				/* HWID : USB\VID_04D8&PID_00DF&REV_0101 and USB\VID_04D8&PID_00DF and so on */
				status = RegGetValue(HKEY_LOCAL_MACHINE, keybuf, TEXT("HardwareID"), RRF_RT_REG_MULTI_SZ, NULL, (PVOID)charbuffer, &charbuffer_size);
				if (status != ERROR_SUCCESS) {
					SetupDiDestroyDeviceInfoList(usb_dev_info_set);
					throw_serialcom_exception(env, 4, GetLastError(), NULL);
					return NULL;
				}

				/* charbuffer now contains hardwareID, cook it a little bit to enable suitable matching (from USB to HID) */
				if ((charbuffer[0] == 'U') && (charbuffer[1] == 'S') && (charbuffer[2] == 'B')) {
					charbuffer[0] = 'H';
					charbuffer[1] = 'I';
					charbuffer[2] = 'D';
				}

				/* check if the hardware ID of this USB interface (child device) matches that of the given HID device */
				ret = _tcsicmp(charbuffer, hwid_to_match);
				if (ret == 0) {

					/* hardware ID matches, so get the required information to be returned to java layer */

					if (info_required == MANUFACTURER_STRING) {

						memset(devprop_buffer, '\0', sizeof(devprop_buffer));
						ret = SetupDiGetDeviceProperty(usb_dev_info_set, &usb_dev_instance, &DEVPKEY_Device_Manufacturer, &proptype, (BYTE *)devprop_buffer, sizeof(devprop_buffer), &size, 0);
						if (ret == FALSE) {
							SetupDiDestroyDeviceInfoList(usb_dev_info_set);
							throw_serialcom_exception(env, 4, HRESULT_FROM_SETUPAPI(GetLastError()), NULL);
							return NULL;
						}

						/* return manufacturer name to java application */
						requiredInfo = (*env)->NewString(env, devprop_buffer, (jsize)_tcslen(devprop_buffer));
						if ((requiredInfo == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
							SetupDiDestroyDeviceInfoList(usb_dev_info_set);
							throw_serialcom_exception(env, 3, 0, E_NEWSTR);
							return NULL;
						}

						SetupDiDestroyDeviceInfoList(usb_dev_info_set);
						return requiredInfo;

					}else if (info_required == PRODUCT_STRING) {

						memset(devprop_buffer, '\0', sizeof(devprop_buffer));
						ret = SetupDiGetDeviceProperty(usb_dev_info_set, &usb_dev_instance, &DEVPKEY_Device_BusReportedDeviceDesc, &proptype, (BYTE *)devprop_buffer, sizeof(devprop_buffer), &size, 0);
						if (ret == FALSE) {
							/* fallback to SPDRP_DEVICEDESC if DEVPKEY_Device_BusReportedDeviceDesc fails */
							ret = SetupDiGetDeviceRegistryProperty(usb_dev_info_set, &usb_dev_instance, SPDRP_DEVICEDESC, &regproptype, (BYTE *)devprop_buffer, sizeof(devprop_buffer), &size);
							if (ret == FALSE) {
								/* if second attempt fails, throw error, we need to investigate drivers/firmware etc */
								SetupDiDestroyDeviceInfoList(usb_dev_info_set);
								throw_serialcom_exception(env, 4, HRESULT_FROM_SETUPAPI(GetLastError()), NULL);
								return NULL;
							}
						}

						/* return product string (iProduct field of USB device descriptor) to java application */
						requiredInfo = (*env)->NewString(env, devprop_buffer, (jsize)_tcslen(devprop_buffer));
						if ((requiredInfo == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
							SetupDiDestroyDeviceInfoList(usb_dev_info_set);
							throw_serialcom_exception(env, 3, 0, E_NEWSTR);
							return NULL;
						}

						SetupDiDestroyDeviceInfoList(usb_dev_info_set);
						return requiredInfo;

					}else if (info_required == SERIAL_STRING) {

						/* buffer contains USB device instance ID (USB\VID_04D8&PID_00DF\000098037) */
						x = 10;
						while (buffer[x] != '\\') {
							x++;
						}

						x++;
						i = 0;
						while (buffer[x] != '\0') {
							charbuffer[i] = buffer[x];
							i++;
							x++;
						}
						charbuffer[i] = '\0';

						/* return serial number string to java application */
						requiredInfo = (*env)->NewString(env, charbuffer, (jsize)_tcslen(charbuffer));
						if ((requiredInfo == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
							SetupDiDestroyDeviceInfoList(usb_dev_info_set);
							throw_serialcom_exception(env, 3, 0, E_NEWSTR);
							return NULL;
						}

						SetupDiDestroyDeviceInfoList(usb_dev_info_set);
						return requiredInfo;

					}else {
						/* would not happen */
					}
				}
			}else {
				/* error happend when getting child of USB device */
				_snprintf_s(cmerror, 256, 256, "CM_Get_Child failed with CR_xxxx error code : 0x%X\0", cmret);
				SetupDiDestroyDeviceInfoList(usb_dev_info_set);
				throw_serialcom_exception(env, 3, 0, cmerror);
				return NULL;
			}
		}

		/* reaching here means that this USB device has at-least one child device node, examine first child now */

		devprop_buffer_size = sizeof(devprop_buffer);
		memset(devprop_buffer, '\0', 1024);
		cmret = CM_Get_DevNode_Registry_Property(firstchild, CM_DRP_CLASSGUID, &proptype, (PVOID)devprop_buffer, &devprop_buffer_size, 0);
		if (cmret != CR_SUCCESS) {
			_snprintf_s(cmerror, 256, 256, "CM_Get_DevNode_Registry_Property failed with CR_xxxx error code : 0x%X\0", cmret);
			SetupDiDestroyDeviceInfoList(usb_dev_info_set);
			throw_serialcom_exception(env, 3, 0, cmerror);
			return NULL;
		}

		/* check if this is a USB HID interface */
		ret = _tcsicmp(devprop_buffer, TEXT("{745A17A0-74D3-11D0-B6FE-00A0C90F57DA}"));
		if (ret == 0) {
			
			/* reaching here means that this is a USB HID interface, get its device instance path.
			   ID : USB\VID_04D8&PID_00DF&REV_0101&MI_02 */
			memset(devprop_buffer, '\0', 1024);
			cmret = CM_Get_Device_ID(firstchild, devprop_buffer, 1024, 0);
			if (cmret != CR_SUCCESS) {
				_snprintf_s(cmerror, 256, 256, "CM_Get_Device_ID failed with CR_xxxx error code : 0x%X\0", cmret);
				SetupDiDestroyDeviceInfoList(usb_dev_info_set);
				throw_serialcom_exception(env, 3, 0, cmerror);
				return NULL;
			}

			/* get the HardwareID of this USB HID interface. HardwareID is multi-sz, however
			   we use only 1st string from multi-string HardwareID for our matching.
			   HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Enum\USB\VID_04D8&PID_00DF&MI_02\6&19D88AD8&0&0002\HardwareID */
			memset(keybuf, '\0', 1024);
			_stprintf_s(keybuf, 1024, TEXT("SYSTEM\\CurrentControlSet\\Enum\\%s"), devprop_buffer);

			charbuffer_size = sizeof(charbuffer);
			memset(charbuffer, '\0', 512);

			/* USB\VID_04D8&PID_00DF&REV_0101 and USB\VID_04D8&PID_00DF and so on */
			status = RegGetValue(HKEY_LOCAL_MACHINE, keybuf, TEXT("HardwareID"), RRF_RT_REG_MULTI_SZ, NULL, (PVOID)charbuffer, &charbuffer_size);
			if (status != ERROR_SUCCESS) {
				SetupDiDestroyDeviceInfoList(usb_dev_info_set);
				throw_serialcom_exception(env, 4, GetLastError(), NULL);
				return NULL;
			}

			/* charbuffer now contains hardwareID, cook it a little bit to enable suitable matching (from USB to HID) */
			if ((charbuffer[0] == 'U') && (charbuffer[1] == 'S') && (charbuffer[2] == 'B')) {
				charbuffer[0] = 'H';
				charbuffer[1] = 'I'; 
				charbuffer[2] = 'D';
			}

			/* check if the hardware ID of this USB interface (child device) matches that of the given HID device */
			ret = _tcsicmp(charbuffer, hwid_to_match);
			if (ret == 0) {

				/* hardware ID matches, so get the required information to be returned to java layer */

				if (info_required == MANUFACTURER_STRING) {

					memset(devprop_buffer, '\0', sizeof(devprop_buffer));
					ret = SetupDiGetDeviceProperty(usb_dev_info_set, &usb_dev_instance, &DEVPKEY_Device_Manufacturer, &proptype, (BYTE *)devprop_buffer, sizeof(devprop_buffer), &size, 0);
					if (ret == FALSE) {
						SetupDiDestroyDeviceInfoList(usb_dev_info_set);
						throw_serialcom_exception(env, 4, HRESULT_FROM_SETUPAPI(GetLastError()), NULL);
						return NULL;
					}

					/* return manufacturer name to java application */
					requiredInfo = (*env)->NewString(env, devprop_buffer, (jsize)_tcslen(devprop_buffer));
					if ((requiredInfo == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
						SetupDiDestroyDeviceInfoList(usb_dev_info_set);
						throw_serialcom_exception(env, 3, 0, E_NEWSTR);
						return NULL;
					}

					SetupDiDestroyDeviceInfoList(usb_dev_info_set);
					return requiredInfo;

				}else if (info_required == PRODUCT_STRING) {

					memset(devprop_buffer, '\0', sizeof(devprop_buffer));
					ret = SetupDiGetDeviceProperty(usb_dev_info_set, &usb_dev_instance, &DEVPKEY_Device_BusReportedDeviceDesc, &proptype, (BYTE *)devprop_buffer, sizeof(devprop_buffer), &size, 0);
					if (ret == FALSE) {
						/* fallback to SPDRP_DEVICEDESC if DEVPKEY_Device_BusReportedDeviceDesc fails */
						ret = SetupDiGetDeviceRegistryProperty(usb_dev_info_set, &usb_dev_instance, SPDRP_DEVICEDESC, &regproptype, (BYTE *)devprop_buffer, sizeof(devprop_buffer), &size);
						if (ret == FALSE) {
							/* if second attempt fails, throw error, we need to investigate drivers/firmware etc */
							SetupDiDestroyDeviceInfoList(usb_dev_info_set);
							throw_serialcom_exception(env, 4, HRESULT_FROM_SETUPAPI(GetLastError()), NULL);
							return NULL;
						}
					}

					/* return product string (iProduct field of USB device descriptor) to java application */
					requiredInfo = (*env)->NewString(env, devprop_buffer, (jsize)_tcslen(devprop_buffer));
					if ((requiredInfo == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
						SetupDiDestroyDeviceInfoList(usb_dev_info_set);
						throw_serialcom_exception(env, 3, 0, E_NEWSTR);
						return NULL;
					}

					SetupDiDestroyDeviceInfoList(usb_dev_info_set);
					return requiredInfo;

				}else if (info_required == SERIAL_STRING) {

					/* buffer contains USB device instance ID (USB\VID_04D8&PID_00DF\000098037) */
					x = 10;
					while (buffer[x] != '\\') {
						x++;
					}

					x++;
					i = 0;
					while (buffer[x] != '\0') {
						charbuffer[i] = buffer[x];
						i++;
						x++;
					}
					charbuffer[i] = '\0';

					/* return serial number string to java application */
					requiredInfo = (*env)->NewString(env, charbuffer, (jsize)_tcslen(charbuffer));
					if ((requiredInfo == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
						SetupDiDestroyDeviceInfoList(usb_dev_info_set);
						throw_serialcom_exception(env, 3, 0, E_NEWSTR);
						return NULL;
					}

					SetupDiDestroyDeviceInfoList(usb_dev_info_set);
					return requiredInfo;

				}else {
					/* would not happen */
				}
			}
		}

		/* Check if this usb device has more than one interface. if it has enumerate over each 
		   one by one, match the hardware ID and if matched collect required information and 
		   return it to java layer */

		current_sibling = firstchild;
		while (1) {
			cmret = CM_Get_Sibling(&next_sibling, current_sibling, 0);
			if (cmret != CR_SUCCESS) {
				if (cmret == CR_NO_SUCH_DEVNODE) {
					/* done iterating over all interfaces, move to examine next USB device in information set */
					break;
				}else {
					_snprintf_s(cmerror, 256, 256, "CM_Get_Sibling failed with CR_xxxx error code : 0x%X\0", cmret);
					SetupDiDestroyDeviceInfoList(usb_dev_info_set);
					throw_serialcom_exception(env, 3, 0, cmerror);
					return NULL;
				}
			}

			/* reaching here means USB device has more than 1 interfaces, get class of this interface (sibling) */
			devprop_buffer_size = sizeof(devprop_buffer);
			memset(devprop_buffer, '\0', sizeof(devprop_buffer));
			cmret = CM_Get_DevNode_Registry_Property(next_sibling, CM_DRP_CLASSGUID, &proptype, (VOID *)devprop_buffer, &devprop_buffer_size, 0);
			if (cmret != CR_SUCCESS) {
				_snprintf_s(cmerror, 256, 256, "CM_Get_DevNode_Registry_Property failed with CR_xxxx error code : 0x%X\0", cmret);
				SetupDiDestroyDeviceInfoList(usb_dev_info_set);
				throw_serialcom_exception(env, 3, 0, cmerror);
				return NULL;
			}

			/* check if this is a HID device interface */
			ret = _tcsicmp(devprop_buffer, TEXT("{745A17A0-74D3-11D0-B6FE-00A0C90F57DA}"));
			if (ret != 0) {
				/* this is not HID interface, move to check next interface */
				current_sibling = next_sibling;
				continue;
			}

			/* reaching here means that this sibling (interface) is a USB HID type, get its device instance path.
			   ID : USB\VID_04D8&PID_00DF&REV_0101&MI_02 */
			memset(devprop_buffer, '\0', 1024);
			cmret = CM_Get_Device_ID(next_sibling, devprop_buffer, 1024, 0);
			if (cmret != CR_SUCCESS) {
				_snprintf_s(cmerror, 256, 256, "CM_Get_Device_ID failed with CR_xxxx error code : 0x%X\0", cmret);
				SetupDiDestroyDeviceInfoList(usb_dev_info_set);
				throw_serialcom_exception(env, 3, 0, cmerror);
				return NULL;
			}

			/* get the HardwareID of this USB HID interface. HardwareID is multi-sz, however
			   we use only 1st string from multi-string HardwareID for our matching.
			   HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Enum\USB\VID_04D8&PID_00DF&MI_02\6&19D88AD8&0&0002\HardwareID */
			memset(keybuf, '\0', 1024);
			_stprintf_s(keybuf, 1024, TEXT("SYSTEM\\CurrentControlSet\\Enum\\%s"), devprop_buffer);

			charbuffer_size = sizeof(charbuffer);
			memset(charbuffer, '\0', 512);

			/* HWID : USB\VID_04D8&PID_00DF&REV_0101&MI_02 and USB\VID_04D8&PID_00DF&MI_02 and so on. 
			   keybuf : SYSTEM\CurrentControlSet\Enum\USB\VID_04D8&PID_00DF&MI_02\6&19D88AD8&0&0002 */
			status = RegGetValue(HKEY_LOCAL_MACHINE, keybuf, TEXT("HardwareID"), RRF_RT_REG_MULTI_SZ, NULL, (PVOID)charbuffer, &charbuffer_size);
			if (status != ERROR_SUCCESS) {
				SetupDiDestroyDeviceInfoList(usb_dev_info_set);
				throw_serialcom_exception(env, 4, GetLastError(), NULL);
				return NULL;
			}

			/* charbuffer now contains hardwareID, cook it a little bit to enable suitable matching (from USB to HID) */
			if ((charbuffer[0] == 'U') && (charbuffer[1] == 'S') && (charbuffer[2] == 'B')) {
				charbuffer[0] = 'H';
				charbuffer[1] = 'I';
				charbuffer[2] = 'D';
			}

			/* check if the hardware ID of this USB interface (child device) matches that of the given 
			   HID device about whom information is to be obtained */
			ret = _tcsicmp(charbuffer, hwid_to_match);
			if (ret == 0) {

				/* hardware ID matches, so get the required information to be returned to java layer */

				if (info_required == MANUFACTURER_STRING) {

					memset(devprop_buffer, '\0', sizeof(devprop_buffer));
					ret = SetupDiGetDeviceProperty(usb_dev_info_set, &usb_dev_instance, &DEVPKEY_Device_Manufacturer, &proptype, (BYTE *)devprop_buffer, sizeof(devprop_buffer), &size, 0);
					if (ret == FALSE) {
						SetupDiDestroyDeviceInfoList(usb_dev_info_set);
						throw_serialcom_exception(env, 4, HRESULT_FROM_SETUPAPI(GetLastError()), NULL);
						return NULL;
					}

					/* return manufacturer name to java application */
					requiredInfo = (*env)->NewString(env, devprop_buffer, (jsize)_tcslen(devprop_buffer));
					if ((requiredInfo == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
						SetupDiDestroyDeviceInfoList(usb_dev_info_set);
						throw_serialcom_exception(env, 3, 0, E_NEWSTR);
						return NULL;
					}

					SetupDiDestroyDeviceInfoList(usb_dev_info_set);
					return requiredInfo;

				}else if (info_required == PRODUCT_STRING) {

					memset(devprop_buffer, '\0', sizeof(devprop_buffer));
					ret = SetupDiGetDeviceProperty(usb_dev_info_set, &usb_dev_instance, &DEVPKEY_Device_BusReportedDeviceDesc, &proptype, (BYTE *)devprop_buffer, sizeof(devprop_buffer), &size, 0);
					if (ret == FALSE) {
						/* fallback to SPDRP_DEVICEDESC if DEVPKEY_Device_BusReportedDeviceDesc fails */
						ret = SetupDiGetDeviceRegistryProperty(usb_dev_info_set, &usb_dev_instance, SPDRP_DEVICEDESC, &regproptype, (BYTE *)devprop_buffer, sizeof(devprop_buffer), &size);
						if (ret == FALSE) {
							/* if second attempt fails, throw error, we need to investigate drivers/firmware etc */
							SetupDiDestroyDeviceInfoList(usb_dev_info_set);
							throw_serialcom_exception(env, 4, HRESULT_FROM_SETUPAPI(GetLastError()), NULL);
							return NULL;
						}
					}

					/* return product string (iProduct field of USB device descriptor) to java application */
					requiredInfo = (*env)->NewString(env, devprop_buffer, (jsize)_tcslen(devprop_buffer));
					if ((requiredInfo == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
						SetupDiDestroyDeviceInfoList(usb_dev_info_set);
						throw_serialcom_exception(env, 3, 0, E_NEWSTR);
						return NULL;
					}

					SetupDiDestroyDeviceInfoList(usb_dev_info_set);
					return requiredInfo;

				}else if (info_required == SERIAL_STRING) {

					/* buffer contains USB device instance ID (USB\VID_04D8&PID_00DF\000098037) */
					x = 10;
					while (buffer[x] != '\\') {
						x++;
					}

					x++;
					i = 0;
					while (buffer[x] != '\0') {
						charbuffer[i] = buffer[x];
						i++;
						x++;
					}
					charbuffer[i] = '\0';

					/* return serial number string to java application */
					requiredInfo = (*env)->NewString(env, charbuffer, (jsize)_tcslen(charbuffer));
					if ((requiredInfo == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
						SetupDiDestroyDeviceInfoList(usb_dev_info_set);
						throw_serialcom_exception(env, 3, 0, E_NEWSTR);
						return NULL;
					}

					SetupDiDestroyDeviceInfoList(usb_dev_info_set);
					return requiredInfo;

				}else {
					/* would not happen */
				}
			}

			/* set this sibling as base sibling for fetching next sibling, loop over to get and check next
			   interface (sibling) */
			current_sibling = next_sibling;
		}

		/* increment to get and examine the next usb device for HID class */
		usb_member_index++;
	}

	/* reaching here means given HID handle does not belong to USB HID device */
	SetupDiDestroyDeviceInfoList(usb_dev_info_set);


	/* ~~~~~~~~~~~~~~~~~~~~~~~~ Try HID over BLUETOOTH ~~~~~~~~~~~~~~~~~~~~~~~~ */


	/* reaching here means manufacturer string not found for given handle, return empty string. */
	requiredInfo = (*env)->NewStringUTF(env, "");
	if ((requiredInfo == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		(*env)->ExceptionClear(env);
		throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
		return NULL;
	}

	return requiredInfo;
}