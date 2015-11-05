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
#include <windows.h>
#include <dbt.h>
#include <process.h>
#include <tchar.h>
#include <strsafe.h>
#include <jni.h>
#include "windows_serial_lib.h"

/* Access to global shared information */
struct usb_dev_monitor_info *usbport_monitor_info_ptr = NULL;

/*
 * Callback which will be invoked by operating system whenever a USB device is removed or added into system.
 * Some parts of this function may require explicit handling of ANSI charcter set encoding.
 * 
 * Returns 1 if this function itself handle the event otherwise return value from default handler.
 */
LRESULT CALLBACK usb_hotplug_event_handler(HWND window_handle, UINT msg, WPARAM event, LPARAM event_data) {

	int x = 0;
	int i = 0;
	int z = 0;
	int ret = 0;
	JNIEnv* env;
	LRESULT result = 1;
	PDEV_BROADCAST_HDR dev_bcast_hdr = NULL;
	PDEV_BROADCAST_DEVICEINTERFACE dev_bcast_iface = NULL;
	CHAR dev_name[512];
	CHAR hexchardev_name[512];
	CHAR *ptrend;

	/* these 3 must match their value in SerialComUSB class. */
	int USB_DEV_ANY     = 0x00;
	int USB_DEV_ADDED   = 0x01;
	int USB_DEV_REMOVED = 0x02;

	switch (msg) {
	case WM_DEVICECHANGE:

		dev_bcast_hdr = (PDEV_BROADCAST_HDR) event_data;

		if (event == DBT_DEVICEARRIVAL) {
			/* broadcasted when a device has been inserted and is available for use */
			if (dev_bcast_hdr->dbch_devicetype == DBT_DEVTYP_DEVICEINTERFACE) {

				/* type cast to extract interface name */
				dev_bcast_iface = (PDEV_BROADCAST_DEVICEINTERFACE) event_data;

				/* \\?\USB#VID_10C4&PID_EA60#0001#{a5dcbf10-6530-11d2-901f-00c04fb951ed} */
				_snprintf_s(dev_name, 512, 512, "%S\0", (CHAR *) dev_bcast_iface->dbcc_name);

				for (z = 0; z < MAX_NUM_THREADS; z++) {
					if (usbport_monitor_info_ptr[z].window_handle == window_handle) {

						/* extract vid and match, proceed to match pid only if vid matches */
						if (usbport_monitor_info_ptr[z].usb_vid_to_match != USB_DEV_ANY) {
							x = 6;
							while (dev_name[x] != '\0') {
								if ((dev_name[x] == 'V') && (dev_name[x + 1] == 'I') && (dev_name[x + 2] == 'D') && (dev_name[x + 3] == '_')) {
									break;
								}
								x++;
							}
							x = x + 4;
							i = 0;
							while (dev_name[x] != '&') {
								hexchardev_name[i] = dev_name[x];
								i++;
								x++;
							}
							hexchardev_name[i] = '\0';

							/* match */
							if (usbport_monitor_info_ptr[z].usb_vid_to_match != ((int)strtol(hexchardev_name, &ptrend, 16))) {
								return 1;
							}
						}

						/* extract pid and match, proceed to match serial only if pid matches */
						if (usbport_monitor_info_ptr[z].usb_pid_to_match != USB_DEV_ANY) {
							x = 10;
							while (dev_name[x] != '\0') {
								if ((dev_name[x] == 'P') && (dev_name[x + 1] == 'I') && (dev_name[x + 2] == 'D') && (dev_name[x + 3] == '_')) {
									break;
								}
								x++;
							}
							x = x + 4;
							i = 0;
							while (dev_name[x] != '#') {
								hexchardev_name[i] = dev_name[x];
								i++;
								x++;
							}
							hexchardev_name[i] = '\0';

							/* match */
							if (usbport_monitor_info_ptr[z].usb_pid_to_match != ((int)strtol(hexchardev_name, &ptrend, 16))) {
								return 1;
							}
						}

						/* extract serial and match if required by application */
						if (usbport_monitor_info_ptr[z].serial_number_to_match[0] != '\0') {
							x++;
							i = 0;
							while (dev_name[x] != '#') {
								hexchardev_name[i] = dev_name[x];
								i++;
								x++;
							}
							hexchardev_name[i] = '\0';

							/* case insensitive comparision */
							ret = _stricmp(usbport_monitor_info_ptr[z].serial_number_to_match, hexchardev_name);
							if (ret != 0) {
								return 1;
							}
						}

						/* reaching here means device matches all criteria, invoke application's usb hot plug listener */
						env = usbport_monitor_info_ptr[z].env;
						(*env)->CallVoidMethod(env, usbport_monitor_info_ptr[z].usbHotPlugEventListener, usbport_monitor_info_ptr[z].onUSBHotPlugEventMethodID, USB_DEV_ADDED);
						if ((*env)->ExceptionOccurred(env)) {
							LOGE("JNI call CallVoidMethod() in usb_hotplug_event_handler() ", "failed !");
						}
						return 1;

					}
				}

			}
		}else if (event == DBT_DEVICEREMOVECOMPLETE) {
			/* broadcasted when a device has been removed and is no longer available */
			if (dev_bcast_hdr->dbch_devicetype == DBT_DEVTYP_DEVICEINTERFACE) {

				/* type cast to extract interface name */
				dev_bcast_iface = (PDEV_BROADCAST_DEVICEINTERFACE) event_data;

				/* \\?\USB#VID_10C4&PID_EA60#0001#{a5dcbf10-6530-11d2-901f-00c04fb951ed} */
				_snprintf_s(dev_name, 512, 512, "%S\n", (CHAR *)dev_bcast_iface->dbcc_name);


				for (z = 0; z < MAX_NUM_THREADS; z++) {
					if (usbport_monitor_info_ptr[z].window_handle == window_handle) {

						/* extract vid and match, proceed to match pid only if vid matches */
						if (usbport_monitor_info_ptr[z].usb_vid_to_match != USB_DEV_ANY) {
							x = 6;
							while (dev_name[x] != '\0') {
								if ((dev_name[x] == 'V') && (dev_name[x + 1] == 'I') && (dev_name[x + 2] == 'D') && (dev_name[x + 3] == '_')) {
									break;
								}
								x++;
							}
							x = x + 4;
							i = 0;
							while (dev_name[x] != '&') {
								hexchardev_name[i] = dev_name[x];
								i++;
								x++;
							}
							hexchardev_name[i] = '\0';

							/* match */
							if (usbport_monitor_info_ptr[z].usb_vid_to_match != ((int)strtol(hexchardev_name, &ptrend, 16))) {
								return 1;
							}
						}

						/* extract pid and match, proceed to match serial only if pid matches */
						if (usbport_monitor_info_ptr[z].usb_pid_to_match != USB_DEV_ANY) {
							x = 10;
							while (dev_name[x] != '\0') {
								if ((dev_name[x] == 'P') && (dev_name[x + 1] == 'I') && (dev_name[x + 2] == 'D') && (dev_name[x + 3] == '_')) {
									break;
								}
								x++;
							}
							x = x + 4;
							i = 0;
							while (dev_name[x] != '#') {
								hexchardev_name[i] = dev_name[x];
								i++;
								x++;
							}
							hexchardev_name[i] = '\0';

							/* match */
							if (usbport_monitor_info_ptr[z].usb_pid_to_match != ((int)strtol(hexchardev_name, &ptrend, 16))) {
								return 1;
							}
						}

						/* extract serial and match if required by application */
						if (usbport_monitor_info_ptr[z].serial_number_to_match[0] != '\0') {
							x++;
							i = 0;
							while (dev_name[x] != '#') {
								hexchardev_name[i] = dev_name[x];
								i++;
								x++;
							}
							hexchardev_name[i] = '\0';

							/* case insensitive comparision */
							ret = _stricmp(usbport_monitor_info_ptr[z].serial_number_to_match, hexchardev_name);
							if (ret != 0) {
								return 1;
							}
						}

						/* reaching here means device matches all criteria, invoke application's usb hot plug listener */
						env = usbport_monitor_info_ptr[z].env;
						(*env)->CallVoidMethod(env, usbport_monitor_info_ptr[z].usbHotPlugEventListener, usbport_monitor_info_ptr[z].onUSBHotPlugEventMethodID, USB_DEV_REMOVED);
						if ((*env)->ExceptionOccurred(env)) {
						LOGE("JNI call CallVoidMethod() in usb_hotplug_event_handler() ", "failed !");
						}
						return 1;
					}
				}
			}
		}else {
			/* do nothing */
		}

		break;
	case WM_CLOSE:
		break;
	case WM_DESTROY:
		/* PostQuitMessage posts the WM_QUIT message to the currently executing thread. */
		PostQuitMessage(0);
		break;
	default:
		result = DefWindowProc(window_handle, msg, event, event_data);
	}

	return result;
}

/* This thread keep polling for the physical existence of a port/file/device. When port removal is detected, this
* informs java listener and exit. Associate the handler with a class, that class with a window and register that
* window with notification system. */
unsigned __stdcall usb_device_hotplug_monitor(void *arg) {

	int i = 0;
	int ret = 0;
	BOOL result = FALSE;
	DWORD errorVal = 0;
	MSG msg;
	ATOM atom;
	HDEVNOTIFY notification_handle = NULL;
	DEV_BROADCAST_DEVICEINTERFACE dev_broadcast_iface;
	HWND window_handle;
	WNDCLASSEX wndClass = { 0 };
	HINSTANCE hInstance;
	TCHAR classname_buf[64];

	struct usb_dev_monitor_info* ptr = (struct usb_dev_monitor_info*) arg;
	jmethodID onUSBHotPlugEventMethodID = NULL;
	jclass usbHotPlugEventListenerClass = NULL;
	jobject usbHotPlugEventListenerObj = (*ptr).usbHotPlugEventListener;
	JavaVM *jvm = (*ptr).jvm;
	JNIEnv* env = NULL;
	void* env1 = NULL;

	EnterCriticalSection(((struct usb_dev_monitor_info*) arg)->csmutex);

	/* USB device (excluding hub and host controller) */
	const GUID GUID_DEVINTERFACE_USB_DEVICE = { 0xA5DCBF10, 0x6530, 0x11D2, 0x90, 0x1F, 0x00, 0xC0, 0x4F, 0xB9, 0x51, 0xED };

	if ((*jvm)->AttachCurrentThread(jvm, &env1, NULL) != JNI_OK) {
		((struct  usb_dev_monitor_info*) arg)->custom_err_code = E_ATTACHCURRENTTHREAD;
		((struct usb_dev_monitor_info*) arg)->init_done = 2;
		LeaveCriticalSection(((struct usb_dev_monitor_info*) arg)->csmutex);
		return 0;
	}
	env = (JNIEnv*)env1;

	usbHotPlugEventListenerClass = (*env)->GetObjectClass(env, usbHotPlugEventListenerObj);
	if ((usbHotPlugEventListenerClass == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		((struct usb_dev_monitor_info*) arg)->custom_err_code = E_GETOBJECTCLASS;
		((struct usb_dev_monitor_info*) arg)->init_done = 2;
		(*jvm)->DetachCurrentThread(jvm);
		LeaveCriticalSection(((struct usb_dev_monitor_info*) arg)->csmutex);
		/* For unrecoverable errors we would like to exit and try registering again. */
		return 0;
	}

	onUSBHotPlugEventMethodID = (*env)->GetMethodID(env, usbHotPlugEventListenerClass, "onUSBHotPlugEvent", "(I)V");
	if ((onUSBHotPlugEventMethodID == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		((struct usb_dev_monitor_info*) arg)->custom_err_code = E_GETMETHODID;
		((struct usb_dev_monitor_info*) arg)->init_done = 2;
		(*jvm)->DetachCurrentThread(jvm);
		LeaveCriticalSection(((struct usb_dev_monitor_info*) arg)->csmutex);
		return 0;
	}

	/* Registers a window class for subsequent use in calls to the CreateWindow or CreateWindowEx function.  */
	memset(classname_buf, '\0', 64);
	_stprintf_s(classname_buf, 64, TEXT("SCM USB : %p"), ((struct usb_dev_monitor_info*) arg)->thread_handle);
	hInstance = (HINSTANCE)GetModuleHandle(NULL);
	wndClass.cbSize = sizeof(WNDCLASSEX);
	wndClass.style = 0;
	wndClass.lpfnWndProc = (WNDPROC)usb_hotplug_event_handler;
	wndClass.cbClsExtra = 0;
	wndClass.cbWndExtra = 0;
	wndClass.hInstance = hInstance;
	wndClass.hIcon = NULL;
	wndClass.hCursor = NULL;
	wndClass.hbrBackground = NULL;
	wndClass.lpszMenuName = NULL;
	wndClass.lpszClassName = classname_buf;
	wndClass.hIconSm = NULL;
	atom = RegisterClassEx(&wndClass);
	if (atom == 0) {
		/* should not happen, just log it for later analysis */
		LOGEN("RegisterClassEx() failed in ", "usb_device_hotplug_monitor() with Windows error code : ", GetLastError());
	}

	/* Create message only window.  Windows will deliver messages to this window. */
	window_handle = CreateWindowEx(WS_EX_TOPMOST, classname_buf, TEXT("scm usb hot plug event thread window"), 0,
									0, 0, 0, 0, HWND_MESSAGE, 0, 0, 0);
	if (window_handle == NULL) {
		UnregisterClass(wndClass.lpszClassName, hInstance);
		((struct usb_dev_monitor_info*) arg)->standard_err_code = GetLastError();
		((struct usb_dev_monitor_info*) arg)->init_done = 2;
		(*jvm)->DetachCurrentThread(jvm);
		LeaveCriticalSection(((struct usb_dev_monitor_info*) arg)->csmutex);
		return 0;
	}

	/* Register with the system to receive device notifications. */
	SecureZeroMemory(&dev_broadcast_iface, sizeof(dev_broadcast_iface));
	dev_broadcast_iface.dbcc_size = sizeof(dev_broadcast_iface);
	dev_broadcast_iface.dbcc_devicetype = DBT_DEVTYP_DEVICEINTERFACE;
	dev_broadcast_iface.dbcc_classguid = GUID_DEVINTERFACE_USB_DEVICE;
	notification_handle = RegisterDeviceNotification(window_handle,                /* events recipient window */
													&dev_broadcast_iface,          /* type of device for which notification will be sent */
													DEVICE_NOTIFY_WINDOW_HANDLE);  /* type of recipient handle */
	if (notification_handle == NULL) {
		DestroyWindow(window_handle);
		UnregisterClass(wndClass.lpszClassName, hInstance);
		((struct usb_dev_monitor_info*) arg)->standard_err_code = GetLastError();
		((struct usb_dev_monitor_info*) arg)->init_done = 2;
		(*jvm)->DetachCurrentThread(jvm);
		LeaveCriticalSection(((struct usb_dev_monitor_info*) arg)->csmutex);
		return 0;
	}

	/* Save so that event callback function can use them. */
	usbport_monitor_info_ptr = ((struct usb_dev_monitor_info*) arg)->info;
	((struct usb_dev_monitor_info*) arg)->env = env;
	((struct usb_dev_monitor_info*) arg)->onUSBHotPlugEventMethodID = onUSBHotPlugEventMethodID;
	((struct usb_dev_monitor_info*) arg)->window_handle = window_handle;

	/* indicate success to the caller so it can return success to java layer */
	((struct usb_dev_monitor_info*) arg)->init_done = 0;
	LeaveCriticalSection(((struct usb_dev_monitor_info*) arg)->csmutex);

	/* message loop */
	while (1) {
		/* block until there is a message in the queue. */
		result = GetMessage(&msg, NULL, 0, 0);
		if (result > 0) {
			if (((struct usb_dev_monitor_info*) arg)->thread_exit == 1) {
				/* application wish to unregister usb hot plug listener, get out of loop and exit thread */
				break;
			}
			TranslateMessage(&msg);
			DispatchMessage(&msg);
		}else if (result == 0) {
			/* WM_QUIT received, get out of loop and exit thread */
			break;
		}else {
			/* should not happen, just log it for later analysis */
			LOGEN("GetMessage() failed in ", "usb_device_hotplug_monitor() with Windows error code : %d", GetLastError());
		}
	}

	UnregisterDeviceNotification(notification_handle);
	DestroyWindow(window_handle);
	UnregisterClass(wndClass.lpszClassName, hInstance);
	(*jvm)->DetachCurrentThread(jvm);
	return 0;
}

