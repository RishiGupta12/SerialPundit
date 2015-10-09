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

/* The {A5DCBF10-6530-11D2-901F-00C04FB951ED} device interface class is defined for USB devices that are attached to a USB 
 * hub. The system-supplied USB hub driver registers instances of GUID_DEVINTERFACE_USB_DEVICE to notify the system and
 * applications of the presence of USB devices that are attached to a USB hub. 
 *
 * Similarly the {4D36E978-E325-11CE-BFC1-08002BE10318} is a GUID for Ports (COM & LPT ports) which can be seen here
 * HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Control\DeviceClasses\{4D36E978-E325-11CE-BFC1-08002BE10318} */
static const GUID GUID_DEVINTERFACE_USB_DEVICE = { 0xA5DCBF10, 0x6530, 0x11D2, 0x90, 0x1F, 0x00, 0xC0, 0x4F, 0xB9, 0x51, 0xED };

static const DEVPROPKEY DEVPKEY_Device_ClassGuid = { 0xa45c254e, 0xdf1c, 0x4efd, 0x80, 0x20, 0x67, 0xd1, 0x46, 0xa8, 0x50, 0xe0, 10 };

/*
 * Find device nodes (like COMxx) assigned by operating system to the USB-UART bridge/converter(s)
 * from the USB device attributes. USB device can be composite or non-composite. The USB strings are Unicoded.
 *
 * 1. Iterate over all USB devices and check if the given USB device is connected or not by matching vid, pid and serial number.
 *
 * 2. If connetced try to see if it has child or not. If not then check if this device is CDC/ACM device, if yes get COM port
 *    assigned to it and add in array to be returned to application.
 *
 * 3. If the connected USB device has one child (one interface), check if this interface is Ports class (COM and LPT ports in windows).
 *    if yes get COM port assigned to it and add in array to be returned to application.
 *
 * 4. If the USB device has more than one interface, than iterate over all the interfaces using parent->child>sibling relationship
 *    as done in windows. Parent is USB device itself, first child will be the first interface in this device and sibling will be the 
 *    next interface in this device. While iterating over interfaces check if it is CDC/ACM and if yes get COM port assigned to it 
 *    and add in array to be returned to application.
 * 
 * Another approach might be to iterate over all usb devices (GUID GUID_DEVINTERFACE_USB_DEVICE) and then for each usb device 
 * iterate over com ports (GUID_DEVINTERFACE_SERENUM_BUS_ENUMERATOR) and match container ID. Device nodes created for a USB device 
 * and all its interfaces will have exactly same container ID. This is done in windows to create device centric tree.
 * 
 * Return COM port node(s) if found, 0 length array if no COM port(s) found, NULL if an error occurs.
 */
jobjectArray vcp_node_from_usb_attributes(JNIEnv *env, jint usbvid_to_match, jint usbpid_to_match, jstring serial_number) {

	int x = 0;
	int i = 0;
	int vid = 0;
	int pid = 0;
	BOOL ret = FALSE;
	LONG status = 0;
	DWORD error_code = 0;
	DWORD size = 0;
	DWORD regproptype;
	DWORD charbuffer_size = 0;
	ULONG devprop_buffer_size = 0;
	DEVPROPTYPE proptype;
	ULONG buffer_size = 0;
	TCHAR *ptrend;

	/* size of these buffers is hardcoded in functions using them */
	TCHAR buffer[1024];
	TCHAR devprop_buffer[1024];
	TCHAR portname_keybuf[1024];
	TCHAR charbuffer[128];
	char cmerror[256];

	DWORD usb_member_index = 0;
	HDEVINFO usb_dev_info_set;
	SP_DEVINFO_DATA usb_dev_instance;

	CONFIGRET cmret = 0;
	DEVINST firstchild = 0;
	DEVINST next_sibling = 0;
	DEVINST current_sibling = 0;

	const jchar* serial = NULL;
	struct jstrarray_list list = { 0 };
	jclass strClass = NULL;
	jstring vcp_node;
	jobjectArray vcpPortsFound = NULL;

	init_jstrarraylist(&list, 50);

	/* get information set for all usb devices matching the GUID */
	usb_dev_info_set = SetupDiGetClassDevs(&GUID_DEVINTERFACE_USB_DEVICE, NULL, NULL, DIGCF_PRESENT | DIGCF_DEVICEINTERFACE);
	if(usb_dev_info_set == INVALID_HANDLE_VALUE) {
		SetupDiDestroyDeviceInfoList(usb_dev_info_set);
		free_jstrarraylist(&list);
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
		if(ret == FALSE) {
			error_code = GetLastError();
			if(error_code == ERROR_NO_MORE_ITEMS) {
				break;
			}else {
				SetupDiDestroyDeviceInfoList(usb_dev_info_set);
				free_jstrarraylist(&list);
				throw_serialcom_exception(env, 4, HRESULT_FROM_SETUPAPI(error_code), NULL);
				return NULL;
			}
		}

		/* for this device find its instance ID (USB\VID_04D8&PID_00DF\000098037)
		 * this is the variable 'Device Instance Path' in device manager. */
		memset(buffer, '\0', 1024);
		ret = SetupDiGetDeviceInstanceId(usb_dev_info_set, &usb_dev_instance, buffer, 1024, &size);
		if(ret == FALSE) {
			SetupDiDestroyDeviceInfoList(usb_dev_info_set);
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 4, HRESULT_FROM_SETUPAPI(GetLastError()), NULL);
			return NULL;
		}

		/* extract vid and match */
		x = 0;
		while (buffer[x] != '\0') {
			if((buffer[x] == 'V') && (buffer[x + 1] == 'I') && (buffer[x + 2] == 'D') && (buffer[x + 3] == '_')) {
				break;
			}
			x++;
		}
		x = x + 4;
		i = 0;
		while (buffer[x] != '&') {
			charbuffer[i] = buffer[x];
			i++;
			x++;
		}
		charbuffer[i] = '\0'; /* indicate end of string */
		vid = (int) _tcstol(charbuffer, &ptrend, 16);
		if(vid != usbvid_to_match) {
			usb_member_index++;
			continue;
		}

		/* extract pid and match */
		x = 6;
		while (buffer[x] != '\0') {
			if((buffer[x] == 'P') && (buffer[x + 1] == 'I') && (buffer[x + 2] == 'D') && (buffer[x + 3] == '_')) {
				break;
			}
			x++;
		}
		x = x + 4;
		i = 0;
		while (buffer[x] != '\\') {
			charbuffer[i] = buffer[x];
			i++;
			x++;
		}
		charbuffer[i] = '\0';
		pid = (int) _tcstol(charbuffer, &ptrend, 16);
		if(pid != usbpid_to_match) {
			usb_member_index++;
			continue;
		}

		/* extract serial number (as an array of Unicode characters) and match if required */
		if(serial_number != NULL) {
			serial = (*env)->GetStringChars(env, serial_number, JNI_FALSE);
			if((serial == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
				(*env)->ExceptionClear(env);
				SetupDiDestroyDeviceInfoList(usb_dev_info_set);
				free_jstrarraylist(&list);
				throw_serialcom_exception(env, 3, 0, E_GETSTRUTFCHARSTR);
				return NULL;
			}
			x++;
			i = 0;
			while (buffer[x] != '\0') {
				charbuffer[i] = buffer[x];
				i++;
				x++;
			}
			charbuffer[i] = '\0';
			ret = _tcsicmp(serial, charbuffer);
			if(ret != 0) {
				usb_member_index++;
				continue;
			}
		}else {
			x++;
			i = 0;
			while (buffer[x] != '\0') {
				charbuffer[i] = buffer[x];
				i++;
				x++;
			}
			charbuffer[i] = '\0';
		}

		/* reaching here means device meets all criteria, examine for CDC/ACM interface */
		cmret = CM_Get_Child(&firstchild, usb_dev_instance.DevInst, 0);
		if(cmret != CR_SUCCESS) {
			if(cmret == CR_NO_SUCH_DEVNODE) {
				/* this device does not have any child, so check if this is a "Ports" class device or not */
				memset(devprop_buffer, '\0', 1024);
				ret = SetupDiGetDeviceRegistryProperty(usb_dev_info_set, &usb_dev_instance, SPDRP_CLASSGUID, &regproptype, (BYTE *)devprop_buffer, sizeof(devprop_buffer), &size);
				if (ret == FALSE) {
					SetupDiDestroyDeviceInfoList(usb_dev_info_set);
					free_jstrarraylist(&list);
					throw_serialcom_exception(env, 4, HRESULT_FROM_SETUPAPI(GetLastError()), NULL);
					return NULL;
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
				memset(portname_keybuf, '\0', 1024);
				_stprintf_s(portname_keybuf, 1024, TEXT("SYSTEM\\CurrentControlSet\\Enum\\%s\\Device Parameters"), buffer);

				charbuffer_size = sizeof(charbuffer);
				memset(charbuffer, '\0', 128);
				status = RegGetValue(HKEY_LOCAL_MACHINE, portname_keybuf, TEXT("PortName"), RRF_RT_REG_SZ, NULL, (PVOID)charbuffer, &charbuffer_size);
				if (status != ERROR_SUCCESS) {
					SetupDiDestroyDeviceInfoList(usb_dev_info_set);
					free_jstrarraylist(&list);
					throw_serialcom_exception(env, 4, GetLastError(), NULL);
					return NULL;
				}

				/* create java string for com port name found */
				vcp_node = (*env)->NewString(env, charbuffer, (jsize) _tcslen(charbuffer));
				if ((vcp_node == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
					(*env)->ExceptionClear(env);
					SetupDiDestroyDeviceInfoList(usb_dev_info_set);
					free_jstrarraylist(&list);
					throw_serialcom_exception(env, 3, 0, E_NEWSTRSTR);
					return NULL;
				}
				insert_jstrarraylist(&list, vcp_node);

				/* loop back to get next USB device */
				usb_member_index++;
				continue;
			}else {
				/* error happend when getting child of USB device */
				SetupDiDestroyDeviceInfoList(usb_dev_info_set);
				free_jstrarraylist(&list);
				throw_serialcom_exception(env, 4, HRESULT_FROM_SETUPAPI(GetLastError()), NULL);
				return NULL;
			}
		}

		/* reaching here means that this USB device has at-least one child device node */

		devprop_buffer_size = sizeof(devprop_buffer);
		memset(devprop_buffer, '\0', sizeof(devprop_buffer));
		cmret = CM_Get_DevNode_Registry_Property(firstchild, CM_DRP_CLASSGUID, &proptype, (PVOID)devprop_buffer, &devprop_buffer_size, 0);
		if (cmret != CR_SUCCESS) {
			SetupDiDestroyDeviceInfoList(usb_dev_info_set);
			free_jstrarraylist(&list);
			_snprintf_s(cmerror, 256, 256, "CM_Get_DevNode_Registry_Property CR_xxxx error code : 0x%X\0", cmret);
			throw_serialcom_exception(env, 3, 0, cmerror);
			return NULL;
		}

		/* match GUID */
		ret = _tcsicmp(devprop_buffer, TEXT("{4D36E978-E325-11CE-BFC1-08002BE10318}"));
		if (ret != 0) {
			usb_member_index++;
			continue;
		}

		/* reaching here means that the child device is COM port device (CDC/ACM), get its COM port name/number */
		memset(buffer, '\0', 1024);
		cmret = CM_Get_Device_ID(firstchild, buffer, 1024, 0);
		if (cmret != CR_SUCCESS) {
			SetupDiDestroyDeviceInfoList(usb_dev_info_set);
			free_jstrarraylist(&list);
			_snprintf_s(cmerror, 256, 256, "CM_Get_Device_ID CR_xxxx error code : 0x%X\0", cmret);
			throw_serialcom_exception(env, 3, 0, cmerror);
			return NULL;
		}

		memset(portname_keybuf, '\0', 1024);
		_stprintf_s(portname_keybuf, 1024, TEXT("SYSTEM\\CurrentControlSet\\Enum\\%s\\Device Parameters"), buffer);

		charbuffer_size = sizeof(charbuffer);
		memset(charbuffer, '\0', sizeof(charbuffer));
		status = RegGetValue(HKEY_LOCAL_MACHINE, portname_keybuf, TEXT("PortName"), RRF_RT_REG_SZ, NULL, (PVOID)charbuffer, &charbuffer_size);
		if (status != ERROR_SUCCESS) {
			SetupDiDestroyDeviceInfoList(usb_dev_info_set);
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 4, GetLastError(), NULL);
			return NULL;
		}

		/* create java string for com port name found */
		vcp_node = (*env)->NewString(env, charbuffer, (jsize) _tcslen(charbuffer));
		if ((vcp_node == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
			(*env)->ExceptionClear(env);
			SetupDiDestroyDeviceInfoList(usb_dev_info_set);
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 3, 0, E_NEWSTRSTR);
			return NULL;
		}
		insert_jstrarraylist(&list, vcp_node);

		/* loop over all the siblings i.e. all interfaces this USB device has, checking if any of them is CDC/ACM type.
		 * if yes, get its COM port name/number */
		current_sibling = firstchild;
		while (1) {
			cmret = CM_Get_Sibling(&next_sibling, current_sibling, 0);
			if (cmret != CR_SUCCESS) {
				if (cmret == CR_NO_SUCH_DEVNODE) {
					/* done iterating over all interfaces */
					break;
				}else {
					SetupDiDestroyDeviceInfoList(usb_dev_info_set);
					free_jstrarraylist(&list);
					_snprintf_s(cmerror, 256, 256, "CM_Get_Sibling failed with CR_xxxx error code : 0x%X\0", cmret);
					throw_serialcom_exception(env, 3, 0, cmerror);
					return NULL;
				}

			}

			/* reaching here means USB device has more than 1 interface, get class of this interface (sibling) */
			devprop_buffer_size = sizeof(devprop_buffer);
			memset(devprop_buffer, '\0', sizeof(devprop_buffer));
			cmret = CM_Get_DevNode_Registry_Property(next_sibling, CM_DRP_CLASSGUID, &proptype, (VOID *)devprop_buffer, &devprop_buffer_size, 0);
			if (cmret != CR_SUCCESS) {
				SetupDiDestroyDeviceInfoList(usb_dev_info_set);
				free_jstrarraylist(&list);
				_snprintf_s(cmerror, 256, 256, "CM_Get_DevNode_Registry_Property failed with CR_xxxx error code : 0x%X\0", cmret);
				throw_serialcom_exception(env, 3, 0, cmerror);
				return NULL;
			}

			/* match GUID for this sibling */
			ret = _tcsicmp(devprop_buffer, TEXT("{4D36E978-E325-11CE-BFC1-08002BE10318}"));
			if (ret != 0) {
				/* this interface is not CDC/ACM, loop over to next interface */
				current_sibling = next_sibling;
				continue;
			}

			/* reaching here means that this sibling (interface) is a CDC/ACM type, get its COM port name/number */
			buffer_size = (ULONG) _tcslen(buffer);
			memset(buffer, '\0', 1024);
			cmret = CM_Get_Device_ID(next_sibling, buffer, 1024, 0);
			if (cmret != CR_SUCCESS) {
				SetupDiDestroyDeviceInfoList(usb_dev_info_set);
				free_jstrarraylist(&list);
				_snprintf_s(cmerror, 256, 256, "CM_Get_Device_ID failed with CR_xxxx error code : 0x%X\0", cmret);
				throw_serialcom_exception(env, 3, 0, cmerror);
				return NULL;
			}

			memset(portname_keybuf, '\0', 1024);
			_stprintf_s(portname_keybuf, 1024, TEXT("SYSTEM\\CurrentControlSet\\Enum\\%s\\Device Parameters"), buffer);

			charbuffer_size = sizeof(charbuffer);
			memset(charbuffer, '\0', 128);
			status = RegGetValue(HKEY_LOCAL_MACHINE, portname_keybuf, TEXT("PortName"), RRF_RT_REG_SZ, NULL, (PVOID)charbuffer, &charbuffer_size);
			if (status != ERROR_SUCCESS) {
				SetupDiDestroyDeviceInfoList(usb_dev_info_set);
				free_jstrarraylist(&list);
				throw_serialcom_exception(env, 4, GetLastError(), NULL);
				return NULL;
			}

			/* create java string for com port name found */
			vcp_node = (*env)->NewString(env, charbuffer, (jsize) _tcslen(charbuffer));
			if ((vcp_node == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
				(*env)->ExceptionClear(env);
				SetupDiDestroyDeviceInfoList(usb_dev_info_set);
				free_jstrarraylist(&list);
				throw_serialcom_exception(env, 3, 0, E_NEWSTRSTR);
				return NULL;
			}
			insert_jstrarraylist(&list, vcp_node);

			/* set this sibling as base sibling for fetching next sibling, loop over to get and check next interface (sibling) */
			current_sibling = next_sibling;
		}

		/* increment to get and examine the next usb device for COM ports class */
		usb_member_index++;
	}

	SetupDiDestroyDeviceInfoList(usb_dev_info_set);

	/* matching node found, create a JAVA/JNI style array of String object, populate it and return to java layer. */
	strClass = (*env)->FindClass(env, JAVALSTRING);
	if((strClass == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		(*env)->ExceptionClear(env);
		free_jstrarraylist(&list);
		throw_serialcom_exception(env, 3, 0, E_FINDCLASSSSTRINGSTR);
		return NULL;
	}

	vcpPortsFound = (*env)->NewObjectArray(env, (jsize)list.index, strClass, NULL);
	if((vcpPortsFound == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		(*env)->ExceptionClear(env);
		free_jstrarraylist(&list);
		throw_serialcom_exception(env, 3, 0, E_NEWOBJECTARRAYSTR);
		return NULL;
	}

	for (x = 0; x < list.index; x++) {
		(*env)->SetObjectArrayElement(env, vcpPortsFound, x, list.base[x]);
		if((*env)->ExceptionOccurred(env)) {
			(*env)->ExceptionClear(env);
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 3, 0, E_SETOBJECTARRAYSTR);
			return NULL;
		}
	}

	free_jstrarraylist(&list);
	return vcpPortsFound;
}
