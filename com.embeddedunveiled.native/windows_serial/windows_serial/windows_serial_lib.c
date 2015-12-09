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

/* We have tried to follow the philosophy that resources specific to thread should be held by thread
 * and that the thread is responsible for cleaning them before exiting. */

#include "stdafx.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <windows.h>
#include <process.h>
#include <dbt.h>
#include <tchar.h>
#include <strsafe.h>
#include <jni.h>
#include "windows_serial_lib.h"

/* WaitForSingleObject() is used to provide delay whenever required. It returns errno as is and 
 * let caller decide what to do if WaitForSingleObject fails. The caller must multiply return value by -1 
 * to get actual errno code. 
 * 
 * Returns 0 on success, negative error number if an error occurs.
 */
int serial_delay(unsigned milli_seconds) {

	DWORD wait_status = 0;
	OVERLAPPED ov = { 0 };
	
	ov.hEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
	if(ov.hEvent == NULL) {
		return (-1 * GetLastError());
	}
	
	wait_status = WaitForSingleObject(ov.hEvent, milli_seconds);
	CloseHandle(ov.hEvent);
	
	switch (wait_status) {
		case WAIT_TIMEOUT:
			return 0;
		case WAIT_ABANDONED :
			return -1;
		case WAIT_OBJECT_0 :
			return -1;
		case WAIT_FAILED :
			return (-1 * GetLastError());
	}
	
	return -1;
}

/* This thread wait for both data and control event both to occur on the specified port. When data is received on port or a control event has
 * occurred, it enqueue this to data or event to corresponding queue. Separate blocking queue for data and events are managed by java layer. */
unsigned WINAPI event_data_looper(void* arg) {

	int ret = 0;
	int error_count = 0;
	BOOL result = FALSE;
	DWORD error_type = 0;
	DWORD errorVal = 0;
	DWORD events_mask = 0;
	DWORD mask_applied = 0;
	DWORD num_of_bytes_read = 0;
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
	
	void* env1;
	JNIEnv* env;
	jbyte data_buf[1024];
	jbyteArray data_read;
	jclass SerialComLooper = NULL;
	jmethodID data_mid = NULL;
	jmethodID mide = NULL;
	jmethodID event_mid = NULL;

	struct looper_thread_params* params = (struct looper_thread_params*) arg;
	JavaVM *jvm = (*params).jvm;
	HANDLE hComm = (*params).hComm;
	jobject looper = (*params).looper;
	int data_enabled = (*params).data_enabled;
	int event_enabled = (*params).event_enabled;

	/* The JNIEnv is valid only in the current thread. So, threads created should attach 
	 * itself to the VM and obtain a JNI interface pointer. */
	if((*jvm)->AttachCurrentThread(jvm, &env1, NULL) != JNI_OK) {
		((struct looper_thread_params*) arg)->custom_err_code = E_ATTACHCURRENTTHREAD;
		((struct looper_thread_params*) arg)->init_done = 2;
		return 0;
	}
	env = (JNIEnv*) env1;

	/* Local references are valid for the duration of a native method call.
	   They are freed automatically after the native method returns.
	   Local references are only valid in the thread in which they are created.
	   The native code must not pass local references from one thread to another if required. */
	SerialComLooper = (*env)->GetObjectClass(env, looper);
	if((SerialComLooper == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		((struct looper_thread_params*) arg)->custom_err_code = E_GETOBJECTCLASS;
		((struct looper_thread_params*) arg)->init_done = 2;
		(*jvm)->DetachCurrentThread(jvm);
		return 0;   /* For unrecoverable errors we would like to exit and try again. */
	}
	
	event_mid = (*env)->GetMethodID(env, SerialComLooper, "insertInEventQueue", "(I)V");
	if ((event_mid == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		((struct looper_thread_params*) arg)->custom_err_code = E_GETMETHODID;
		((struct looper_thread_params*) arg)->init_done = 2;
		(*jvm)->DetachCurrentThread(jvm);
		return 0;
	}
	
	data_mid = (*env)->GetMethodID(env, SerialComLooper, "insertInDataQueue", "([B)V");
	if ((data_mid == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		((struct looper_thread_params*) arg)->custom_err_code = E_GETMETHODID;
		((struct looper_thread_params*) arg)->init_done = 2;
		(*jvm)->DetachCurrentThread(jvm);
		return 0;
	}
	
	mide = (*env)->GetMethodID(env, SerialComLooper, "insertInDataErrorQueue", "(I)V");
	if((mide == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		((struct looper_thread_params*) arg)->custom_err_code = E_GETMETHODID;
		((struct looper_thread_params*) arg)->init_done = 2;
		(*jvm)->DetachCurrentThread(jvm);
		return 0;
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
		((struct looper_thread_params*) arg)->standard_err_code = GetLastError();
		((struct looper_thread_params*) arg)->init_done = 2;
		(*jvm)->DetachCurrentThread(jvm);
		return 0;
	}

	((struct looper_thread_params*) arg)->wait_event_handles[0] = CreateEvent(NULL, FALSE, FALSE, NULL);
	if(((struct looper_thread_params*) arg)->wait_event_handles[0] == NULL) {
		((struct looper_thread_params*) arg)->standard_err_code = GetLastError();
		((struct looper_thread_params*) arg)->init_done = 2;
		(*jvm)->DetachCurrentThread(jvm);
		return 0;
	}

	/* indicate success to the caller so it can return success to java layer */
	((struct looper_thread_params*) arg)->init_done = 0;
	ret = SetEvent(((struct looper_thread_params*) arg)->init_done_event_handle);
	if (ret == 0) {
		((struct looper_thread_params*) arg)->standard_err_code = GetLastError();
		((struct looper_thread_params*) arg)->init_done = 2;
		(*jvm)->DetachCurrentThread(jvm);
		return 0;
	}

	/* This keep looping forever until listener is unregistered, waiting for data or 
	 * event and passing it to java layer which put it in the queue. */
	while(1) {
	
		eventOccurred = FALSE;

		/* The OVERLAPPED structure is used by the kernel to store progress of the operation. 
		 * Only hEvent member need to be initialled and others can be left 0. The OVERLAPPED 
		 * structure must contain a handle to a manual-reset event object.  */
		memset(&overlapped, 0, sizeof(overlapped));
		overlapped.hEvent = CreateEvent(NULL, FALSE, FALSE, NULL);   /* auto reset, unnamed event object */
		if(overlapped.hEvent == NULL) {
			LOGEN("CreateEvent()", "event_data_looper() failed creating overlapped event handle with error number : ", GetLastError());
			continue;
		}

		((struct looper_thread_params*) arg)->wait_event_handles[1] = overlapped.hEvent;

		/* If the overlapped operation cannot be completed immediately, the function returns FALSE 
		 * and the GetLastError function returns ERROR_IO_PENDING, indicating that the operation is 
		 * executing in the background. When this happens, the system sets the hEvent member of the 
		 * OVERLAPPED structure to the not-signaled state before WaitCommEvent returns, and then it 
		 * sets it to the signaled state when one of the specified events or an error occurs. */
		ret = WaitCommEvent(hComm, &events_mask, &overlapped);
		if(ret == 0) {
			errorVal = GetLastError();
			if(errorVal == ERROR_IO_PENDING) {
				error_count = 0;  /* reset error_count */
				dwEvent = WaitForMultipleObjects(2, ((struct looper_thread_params*) arg)->wait_event_handles, FALSE, INFINITE);
				switch (dwEvent) {
					case WAIT_OBJECT_0 + 0:
						/* Thread is asked to exit. */
						if(1 == ((struct looper_thread_params*) arg)->thread_exit) {							
							(*jvm)->DetachCurrentThread(jvm);
							CloseHandle(overlapped.hEvent);
							return 0;
						}
						break;
					case WAIT_OBJECT_0 + 1:
						/* Some event on serial port has happened. */
						eventOccurred = TRUE;
						break;
					case WAIT_FAILED:
						LOGEN("event_data_looper()", "Unexpected WAIT_FAILED in WaitForMultipleObjects() with error code : ", GetLastError());
						break;
					default:
						LOGEN("event_data_looper()", "Unexpected WAIT_FAILED in WaitForMultipleObjects() with error code : ", GetLastError());
				}
			}else {
				if(((struct looper_thread_params*) arg)->data_enabled == 1) {
					error_count++;
					if(error_count > 25) {
						(*env)->CallVoidMethod(env, looper, mide, errorVal);
						if((*env)->ExceptionOccurred(env)) {
							LOGEN("event_data_looper()", "WaitCommEvent() failed with error code : ", errorVal);
						}
						error_count = 0; /* reset error_count */
					}
				}
				continue;
			}
		}else {
			/* Thread is asked to exit. */
			if(((struct looper_thread_params*) arg)->thread_exit == 1) {
				(*jvm)->DetachCurrentThread(jvm);
				CloseHandle(overlapped.hEvent);
				return 0;
			}
			/* WaitCommEvent tells an event occured in one shot. */
			eventOccurred = TRUE;
		}

		CloseHandle(overlapped.hEvent);

		/* Check it is data or control event and enqueue in appropriate queue in java layer with the help of java method. */
		if(eventOccurred == TRUE) {
			if(events_mask & EV_RXCHAR) {
				/* A data event has occured and application has registered listener for data also, so send data to application. */
				memset(&overlapped, 0, sizeof(overlapped));
				overlapped.hEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
				if(overlapped.hEvent != NULL) {
					result = ReadFile(hComm, data_buf, sizeof(data_buf), &num_of_bytes_read, &overlapped);
					if(result == TRUE) {
						if(num_of_bytes_read > 0) {
							data_read = (*env)->NewByteArray(env, num_of_bytes_read);
							(*env)->SetByteArrayRegion(env, data_read, 0, num_of_bytes_read, data_buf);
							(*env)->CallVoidMethod(env, looper, data_mid, data_read);
							if((*env)->ExceptionOccurred(env)) {
								LOGE("event_data_looper()", "JNI call CallVoidMethod() failed");
							}
						}
					}else {
						errorVal = GetLastError();
						if(errorVal == ERROR_IO_PENDING) {
							if(WaitForSingleObject(overlapped.hEvent, INFINITE) == WAIT_OBJECT_0) {
								if(GetOverlappedResult(hComm, &overlapped, &num_of_bytes_read, FALSE)) {
									if(num_of_bytes_read > 0) {
										data_read = (*env)->NewByteArray(env, num_of_bytes_read);
										(*env)->SetByteArrayRegion(env, data_read, 0, num_of_bytes_read, data_buf);
										(*env)->CallVoidMethod(env, looper, data_mid, data_read);
										if((*env)->ExceptionOccurred(env)) {
											LOGE("event_data_looper()", "JNI call CallVoidMethod() failed");
										}
									}
								}
							}
						}else {
							LOGEN("event_data_looper()", "ReadFile() failed with error code : ", errorVal);
						}
					}

					CloseHandle(overlapped.hEvent);
				}
			}
			
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
						LOGEN("event_data_looper()", "GetCommModemStatus() failed with error code : ", GetLastError());
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
						/* if(DBG) fprintf(stderr, "%s %d\n", "NATIVE event_data_looper() sending bit mapped events ", event);
						   if(DBG) fflush(stderr); */
						(*env)->CallVoidMethod(env, looper, event_mid, event);
						if((*env)->ExceptionOccurred(env)) {
							LOGE("event_data_looper()", "JNI call CallVoidMethod() failed");
						}
					}
			}
		}
	} /* Go back to loop again waiting for a control event or data event to occur using WAITCOMMEVENT(). */

	return 0;
}

