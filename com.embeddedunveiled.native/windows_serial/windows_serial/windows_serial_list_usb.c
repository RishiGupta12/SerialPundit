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
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <tchar.h>
#include <devguid.h>
#include <setupapi.h>
#include <windows.h>
#include <process.h>

#include <jni.h>
#include "windows_serial_lib.h"

jstring win_clean_up_and_throw_exp(JNIEnv *env, int task, const char *expmsg, int error_code, 
	HDEVINFO dev_info_set, struct jstrarray_list *list) {
	
	(*env)->ExceptionClear(env);
	free_jstrarraylist(list);
	SetupDiDestroyDeviceInfoList(dev_info_set);
	
	if(task == 1) {
	}else if(task == 2) {
	}else if(task == 3) {
	}
	
	return NULL;
}

/*
 * Finds information about USB devices using operating system specific facilities and API.
 * The sequence of entries in array must match with what java layer expect. If a particular USB attribute
 * is not set in descriptor or can not be obtained "---" is placed in its place.
 */
jobjectArray list_usb_devices(JNIEnv *env, jint vendor_to_match) {

	int x = 0;
	struct jstrarray_list list = {0};
	jstring usb_dev_info;
	jclass strClass = NULL;
	jobjectArray usbDevicesFound = NULL;
	
	BOOL ret = FALSE;
	DWORD error_code = 0;
	DWORD member_index = 0;
	DWORD regprop_buf_size = 0;
	HDEVINFO dev_info_set;
	SP_DEVINFO_DATA dev_instance;
	TCHAR buffer[2 * 1024];
	
	init_jstrarraylist(&list, 100);
	
	dev_info_set = SetupDiGetClassDevs(&GUID_DEVINTERFACE_USB_DEVICE, NULL, 0, DIGCF_DEVICEINTERFACE | DIGCF_PRESENT);
	if(dev_info_set == INVALID_HANDLE_VALUE) {
		return win_clean_up_and_throw_exp(env, 1, NULL, HRESULT_FROM_SETUPAPI(GetLastError()), dev_info_set, &list);
	}
	
	while(1) {
		ZeroMemory(&dev_instance, sizeof(dev_instance));
		dev_instance.cbSize = sizeof(dev_instance);
		
		ret = SetupDiEnumDeviceInfo(dev_info_set, member_index, &dev_instance);
		if(ret == FALSE) {
			error_code = GetLastError();
			if(error_code == ERROR_NO_MORE_ITEMS) {
				break;
			}else {
				return win_clean_up_and_throw_exp(env, 1, NULL, HRESULT_FROM_SETUPAPI(error_code), dev_info_set, &list);
			}
		}
		
		/*ret = SetupDiGetDeviceRegistryProperty(dev_info_set, &dev_instance, SPDRP_DEVICEDESC, NULL, NULL, 0, &regprop_buf_size);
		if(ret == FALSE) {
			return win_clean_up_and_throw_exp(env, 1, NULL, HRESULT_FROM_SETUPAPI(GetLastError()), dev_info_set, &list);
		}*/
		
		/* TODO chk sizeof(buffer) */
		ret = SetupDiGetDeviceRegistryProperty(dev_info_set, &dev_instance, SPDRP_DEVICEDESC, NULL, &buffer, (2 * sizeof(buffer)), NULL);
		if(ret == FALSE) {
			return win_clean_up_and_throw_exp(env, 1, NULL, HRESULT_FROM_SETUPAPI(GetLastError()), dev_info_set, &list);
		}
		
		member_index++;
	}
	
	/* Create a JAVA/JNI style array of String object, populate it and return to java layer. */
	strClass = (*env)->FindClass(env, JAVALSTRING);
	if((strClass == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {

	}
	
	usbDevicesFound = (*env)->NewObjectArray(env, (jsize) list.index, strClass, NULL);
	if((usbDevicesFound == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {

	}
	
	for (x=0; x < list.index; x++) {
		(*env)->SetObjectArrayElement(env, usbDevicesFound, x, list.base[x]);
		if((*env)->ExceptionOccurred(env)) {

		}
	}
	
	free_jstrarraylist(&list);
	return usbDevicesFound;
}




































