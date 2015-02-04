/***************************************************************************************************
 * Author : Rishi Gupta
 * Email  : gupt21@gmail.com
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

/* We have tried to follow the philosophy that resources specific to thread should be held by thread
 * and that the thread is responsible for cleaning them before exiting. */

#include "stdafx.h"
#include <jni.h>
#include <windows.h>
#include <process.h>
#include <tchar.h>
#include "windows_serial_lib.h"

#define DEBUG 1

/* Do not let any exception propagate. Handle and clear it. */
void LOGE(JNIEnv *env) {
	(*env)->ExceptionDescribe(env);
	(*env)->ExceptionClear(env);
}

/* This thread wait for both data and control event to occur on the specified port. When data is received on port or a control event has
 * occurred, it enqueue this to data or event to corresponding queue. Separate blocking queue for data and events are managed by java layer. */
unsigned __stdcall event_data_looper(void* arg) {
	int ret = 0;
	BOOL result = FALSE;
	COMSTAT com_stat;
	DWORD error_type = 0;
	DWORD errorVal = 0;
	DWORD events_mask = 0;
	DWORD mask_applied = 0;
	jbyte data_buf[1024];
	DWORD num_of_bytes_read;
	jbyteArray data_read;
	DWORD count = 0;
	OVERLAPPED overlapped;
	BOOL eventOccurred = FALSE;
	DWORD dwEvent;

	int CTS =  0x01;  // 0000001
	int DSR =  0x02;  // 0000010
	int DCD =  0x04;  // 0000100
	int RI  =  0x08;  // 0001000
	int lines_status = 0;
	int cts, dsr, dcd, ri = 0;
	int event = 0;

	struct looper_thread_params* params = (struct looper_thread_params*) arg;
	JavaVM *jvm = (*params).jvm;
	HANDLE hComm = (*params).hComm;
	jobject looper = (*params).looper;
	int data_enabled = (*params).data_enabled;
	int event_enabled = (*params).event_enabled;

	/* The JNIEnv is valid only in the current thread. So, threads created should attach itself to the VM and obtain a JNI interface pointer. */
	void* env1;
	JNIEnv* env;
	if((*jvm)->AttachCurrentThread(jvm, &env1, NULL) != JNI_OK) {
		if(DEBUG) fprintf(stderr, "%s \n", "NATIVE event_data_looper() thread failed to attach itself to JVM.");
		if(DEBUG) fflush(stderr);
		((struct looper_thread_params*) arg)->init_done = -240;
		((struct looper_thread_params*) arg)->thread_handle = 0;
		return 0;
	}
	env = (JNIEnv*) env1;

	jclass SerialComLooper = (*env)->GetObjectClass(env, looper);
	if(SerialComLooper == NULL) {
		if(DEBUG) fprintf(stderr, "%s \n", "NATIVE event_data_looper() thread could not get class of object of type looper !");
		if(DEBUG) fprintf(stderr, "%s \n", "NATIVE event_data_looper() thread exiting. Please RETRY registering data listener !");
		if(DEBUG) fflush(stderr);
		EnterCriticalSection(((struct looper_thread_params*) arg)->csmutex);
		CloseHandle(((struct looper_thread_params*) arg)->thread_handle);
		((struct looper_thread_params*) arg)->init_done = -240;
		((struct looper_thread_params*) arg)->thread_handle = 0;
		LeaveCriticalSection(((struct looper_thread_params*) arg)->csmutex);
		return 0;   /* For unrecoverable errors we would like to exit and try again. */
	}

	jmethodID data_mid = (*env)->GetMethodID(env, SerialComLooper, "insertInDataQueue", "([B)V");
	if((*env)->ExceptionOccurred(env)) {
		LOGE(env);
	}
	if(data_mid == NULL) {
		if(DEBUG) fprintf(stderr, "%s \n", "NATIVE event_data_looper() thread failed to retrieve method id of method insertInDataQueue in class SerialComLooper !");
		if(DEBUG) fprintf(stderr, "%s \n", "NATIVE event_data_looper() thread exiting. Please RETRY registering data listener !");
		if(DEBUG) fflush(stderr);
		EnterCriticalSection(((struct looper_thread_params*) arg)->csmutex);
		CloseHandle(((struct looper_thread_params*) arg)->thread_handle);
		((struct looper_thread_params*) arg)->init_done = -240;
		((struct looper_thread_params*) arg)->thread_handle = 0;
		LeaveCriticalSection(((struct looper_thread_params*) arg)->csmutex);
		return 0; /* For unrecoverable errors we would like to exit and try again. */
	}

	jmethodID event_mid = (*env)->GetMethodID(env, SerialComLooper, "insertInEventQueue", "(I)V");
	if((*env)->ExceptionOccurred(env)) {
		LOGE(env);
	}
	if(event_mid == NULL) {
		if(DEBUG) fprintf(stderr, "%s \n", "NATIVE event_data_looper() thread failed to retrieve method id of method insertInEventQueue in class SerialComLooper !");
		if(DEBUG) fprintf(stderr, "%s \n", "NATIVE event_data_looper() thread exiting. Please RETRY registering event listener !");
		if(DEBUG) fflush(stderr);
		EnterCriticalSection(((struct looper_thread_params*) arg)->csmutex);
		CloseHandle(((struct looper_thread_params*) arg)->thread_handle);
		((struct looper_thread_params*) arg)->init_done = -240;
		((struct looper_thread_params*) arg)->thread_handle = 0;
		LeaveCriticalSection(((struct looper_thread_params*) arg)->csmutex);
		return 0; /* For unrecoverable errors we would like to exit and try again. */
	}
	
	/* Set the event mask this thread will wait for. */
	if(data_enabled == 1) {
		/* A character was received and placed in the input buffer. */
		mask_applied = mask_applied | EV_RXCHAR;
	}
	if(event_enabled == 1) {
		mask_applied = mask_applied | EV_BREAK | EV_CTS | EV_ERR | EV_RING | EV_RXFLAG | EV_DSR | EV_RLSD;
	}

	ret = SetCommMask(hComm, mask_applied);
	if(ret == 0) {
		errorVal = GetLastError();
		if(DEBUG) fprintf(stderr, "%s %ld\n", "NATIVE event_data_looper() failed in SetCommMask() with error number : ", errorVal);
		if(DEBUG) fflush(stderr);
		ClearCommError(hComm, &error_type, &com_stat);
		EnterCriticalSection(((struct looper_thread_params*) arg)->csmutex);
		CloseHandle(((struct looper_thread_params*) arg)->thread_handle);
		((struct looper_thread_params*) arg)->init_done = -240;
		((struct looper_thread_params*) arg)->thread_handle = 0;
		LeaveCriticalSection(((struct looper_thread_params*) arg)->csmutex);
		return 0; /* For unrecoverable errors we would like to exit and try again. */
	}

	((struct looper_thread_params*) arg)->wait_event_handles[0] = CreateEvent(NULL, FALSE, FALSE, NULL);
	if (((struct looper_thread_params*) arg)->wait_event_handles[0] == NULL) {
		if (DEBUG) fprintf(stderr, "%s\n", "NATIVE event_data_looper() failed to create thread exit event handle.");
		if (DEBUG) fflush(stderr);
		EnterCriticalSection(((struct looper_thread_params*) arg)->csmutex);
		CloseHandle(((struct looper_thread_params*) arg)->thread_handle);
		((struct looper_thread_params*) arg)->init_done = -240;
		((struct looper_thread_params*) arg)->thread_handle = 0;
		LeaveCriticalSection(((struct looper_thread_params*) arg)->csmutex);
		return 0;
	}

	/* indicate success to caller so it can return success to java layer */
	((struct looper_thread_params*) arg)->init_done = 1;

	/* This keep looping forever until listener is unregistered, waiting for data or event and passing it to java layer which put it in the queue. */
	while(1) {
		eventOccurred = FALSE;

		/* The OVERLAPPED structure is used by the kernel to store progress of the operation.  Only hEvent member need to be initialled and others 
		   can be left 0. The OVERLAPPED structure must contain a handle to a manual-reset event object.  */
		memset(&overlapped, 0, sizeof(overlapped));
		overlapped.hEvent = CreateEvent(NULL, FALSE, FALSE, NULL);   /* auto reset, unnamed event object */
		if(overlapped.hEvent == NULL) {
			if(DEBUG) fprintf(stderr, "%s\n", "NATIVE CreateEvent() in event_data_looper() failed creating overlapped event handle !");
			if(DEBUG) fflush(stderr);
			EnterCriticalSection(((struct looper_thread_params*) arg)->csmutex);
			CloseHandle(((struct looper_thread_params*) arg)->thread_handle);
			((struct looper_thread_params*) arg)->thread_handle = 0;
			LeaveCriticalSection(((struct looper_thread_params*) arg)->csmutex);
			return 0;       /* For unrecoverable errors we would like to exit and try again. */
		}

		((struct looper_thread_params*) arg)->wait_event_handles[1] = overlapped.hEvent;

		/* If the overlapped operation cannot be completed immediately, the function returns FALSE and the GetLastError function returns ERROR_IO_PENDING,
		   indicating that the operation is executing in the background. When this happens, the system sets the hEvent member of the OVERLAPPED structure
		   to the not-signaled state before WaitCommEvent returns, and then it sets it to the signaled state when one of the specified events or an error 
		   occurs.

		   Note that sometimes if port is physically removed from system while listener thread is still alive, system may become slow or hang. This need
		   more testing and clarification from Windows OS perspective. */
		ret = WaitCommEvent(hComm, &events_mask, &overlapped);
		if(ret == 0) {
			errorVal = GetLastError();
			ClearCommError(hComm, &error_type, &com_stat);
			if(errorVal == ERROR_IO_PENDING) {
				dwEvent = WaitForMultipleObjects(2, ((struct looper_thread_params*) arg)->wait_event_handles, FALSE, INFINITE);
				switch (dwEvent) {
					case WAIT_OBJECT_0 + 0:
						/* Thread is asked to exit. */
						if(1 == ((struct looper_thread_params*) arg)->thread_exit) {
							EnterCriticalSection(((struct looper_thread_params*) arg)->csmutex);
							CloseHandle(((struct looper_thread_params*) arg)->thread_handle);
							((struct looper_thread_params*) arg)->thread_handle = 0;
							LeaveCriticalSection(((struct looper_thread_params*) arg)->csmutex);
							CloseHandle(overlapped.hEvent);
							return 0;
						}
						break;
					case WAIT_OBJECT_0 + 1:
						/* Some event on serial port has happened. */
						eventOccurred = TRUE;
						break;
					case WAIT_FAILED:
						if(DEBUG) fprintf(stderr, "Unexpected WAIT_FAILED in WaitForMultipleObjects() with error : %ld\n", GetLastError());
						if(DEBUG) fflush(stderr);
						break;
					default:
						if(DEBUG) fprintf(stderr, "Unexpected WaitForMultipleObjects() with error : %ld\n", GetLastError());
						if(DEBUG) fflush(stderr);
				}
			}else {
				continue;
			}
		}else {
			eventOccurred = TRUE;
		}

		CloseHandle(overlapped.hEvent);

		/* Check it is data or control event and enqueue in appropriate queue in java layer with the help of java method. */
		if(eventOccurred == TRUE) {
			if((mask_applied & EV_RXCHAR) && (events_mask & EV_RXCHAR)) {
				/* A data event has occured and application has registered listener for data also, so send data to application. */
				memset(&overlapped, 0, sizeof(overlapped));
				overlapped.hEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
				if(overlapped.hEvent == NULL) {
					//TODO WHT IF FAIL
				}

				result = ReadFile(hComm, data_buf, sizeof(data_buf), &num_of_bytes_read, &overlapped);
				if(result == TRUE) {
					data_read = (*env)->NewByteArray(env, num_of_bytes_read);
					(*env)->SetByteArrayRegion(env, data_read, 0, num_of_bytes_read, data_buf);
					(*env)->CallVoidMethod(env, looper, data_mid, data_read);
					if((*env)->ExceptionOccurred(env)) {
						LOGE(env);
					}
				}else {
					errorVal = GetLastError();
					ClearCommError(hComm, &error_type, &com_stat);
					if(errorVal == ERROR_IO_PENDING) {
						if(WaitForSingleObject(overlapped.hEvent, INFINITE) == WAIT_OBJECT_0) {
							if (GetOverlappedResult(hComm, &overlapped, &num_of_bytes_read, FALSE)) {
								data_read = (*env)->NewByteArray(env, num_of_bytes_read);
								(*env)->SetByteArrayRegion(env, data_read, 0, num_of_bytes_read, data_buf);
								(*env)->CallVoidMethod(env, looper, data_mid, data_read);
								if((*env)->ExceptionOccurred(env)) {
									LOGE(env);
								}
							}
						}
					}else {
						if(DEBUG) fprintf(stderr, "ReadFile failed with error : %ld\n", errorVal);
						if(DEBUG) fflush(stderr);
					}
				}

				CloseHandle(overlapped.hEvent);
			}

			if((mask_applied & EV_CTS) || (mask_applied & EV_DSR) || (mask_applied & EV_RLSD) || (mask_applied & EV_RING)) {
				/* application has registered listener for control event, so send it if occured. */
				if((events_mask & EV_CTS) || (events_mask & EV_DSR) || (events_mask & EV_RLSD) || (events_mask & EV_RING)) {
					/* waitcommevent says control event has occured, so we get it. */
					lines_status = 0;
					cts = 0;
					dsr = 0;
					dcd = 0;
					ri = 0;
					event = 0;

					ret = GetCommModemStatus(hComm, &lines_status);
					if(ret == 0) {
						errorVal = GetLastError();
						if(DEBUG) fprintf(stderr, "%s %ld\n", "NATIVE GetCommModemStatus() in data_event_looper() failed with error number : ", errorVal);
						if(DEBUG) fflush(stderr);
						continue;
					}

					cts = (lines_status & MS_CTS_ON)  ? 1 : 0;
					dsr = (lines_status & MS_DSR_ON)  ? 1 : 0;
					dcd = (lines_status & MS_RLSD_ON) ? 1 : 0;
					ri  = (lines_status & MS_RING_ON) ? 1 : 0;

					if(cts) {
						event = event | CTS;
					}
					if(dsr) {
						event = event | DSR;
					}
					if(dcd) {
						event = event | DCD;
					}
					if(ri) {
						event = event | RI;
					}

					if(cts || dsr || dcd || ri) {
						/* It is control event(s), so enqueue it in event queue. */
						if(DEBUG) fprintf(stderr, "%s %d\n", "NATIVE event_data_looper() sending bit mapped events ", event);
						if(DEBUG) fflush(stderr);
						(*env)->CallVoidMethod(env, looper, event_mid, event);
						if((*env)->ExceptionOccurred(env)) {
							LOGE(env);
						}
					}
				}
			}
		}
	} /* Go back to loop again waiting for a control event or data event to occur using WAITCOMMEVENT(). */

	return 0;
}

/* This thread keep polling for the physical existence of a port/file/device. When port removal is detected, this
* informs java listener and exit. */
unsigned __stdcall port_monitor(void *arg) {
	struct port_info* params = (struct port_info*) arg;
	JavaVM *jvm = (*params).jvm;
	jobject port_listener = (*params).port_listener;
	int lines_status = 0;
	int ret = 0;

	void* env1;
	JNIEnv* env;
	if((*jvm)->AttachCurrentThread(jvm, &env1, NULL) != JNI_OK) {
		if(DEBUG) fprintf(stderr, "%s \n", "NATIVE event_looper() thread failed to attach itself to JVM.");
		if(DEBUG) fflush(stderr);
	}
	env = (JNIEnv*)env1;

	jclass portListener = (*env)->GetObjectClass(env, port_listener);
	if(portListener == NULL) {
		if(DEBUG) fprintf(stderr, "%s \n", "NATIVE port_monitor() thread could not get class of object of type IPortMonitor !");
		if(DEBUG) fprintf(stderr, "%s \n", "NATIVE port_monitor() thread exiting. Please RETRY registering port listener !");
		if(DEBUG) fflush(stderr);
		return 0;
	}

	jmethodID mid = (*env)->GetMethodID(env, portListener, "onPortRemovedEvent", "()V");
	if((*env)->ExceptionOccurred(env)) {
		LOGE(env);
	}
	if(mid == NULL) {
		if(DEBUG) fprintf(stderr, "%s \n", "NATIVE port_monitor() thread failed to retrieve method id of method onPortRemovedEvent !");
		if(DEBUG) fprintf(stderr, "%s \n", "NATIVE port_monitor() thread exiting. Please RETRY registering data listener !");
		if(DEBUG) fflush(stderr);
		return 0;
	}

	while (1) {
		if(1 == ((struct port_info*) arg)->thread_exit) {
			return 0;
		}

		ret = GetCommModemStatus(((struct port_info*) arg)->hComm, &lines_status);
		if(ret == 0) {
			DWORD errorVal = GetLastError();
			/* if (DEBUG) fprintf(stderr, "%s %ld\n", "NATIVE event_data_looper() failed in SetCommMask() with error number : ", errorVal);
			if (DEBUG) fflush(stderr); */
			(*env)->CallVoidMethod(env, port_listener, mid, 0);
			if((*env)->ExceptionOccurred(env)) {
				LOGE(env);
			}
			break;
		}

		Sleep(750); /* 750 milliseocnds */
		if (DEBUG) fprintf(stderr, "%s \n", "NAT!");
		if (DEBUG) fflush(stderr);
	}

	return 0;
}