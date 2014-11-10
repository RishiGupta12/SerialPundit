/***************************************************************************************************
 * Author : Rishi Gupta
 * Email  : gupt21@gmail.com
 *
 * This file is part of 'serial communication manager' program.
 *
 * 'serial communication manager' is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * 'serial communication manager' is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with serial communication manager. If not, see <http://www.gnu.org/licenses/>.
 *
 ***************************************************************************************************/

#if defined (_WIN32) || defined (__WIN32__) || defined (__TOS_WIN) || defined (__WINDOWS__)

#include <jni.h>
#include <windows.h>
#include <process.h>
#include "windows_serial_lib.h"

#define MAX_NUM_THREADS 1024

/* Do not let any exception propagate. Handle and clear it. */
void LOGE(JNIEnv *env) {
	(*env)->ExceptionDescribe(env);
	(*env)->ExceptionClear(env);
}

/* This thread wait for both data and control event to occur on the specified port. When data is received on port or a control event has
 * occurred, it enqueue this to data or event to corresponding queue. Separate blocking queue for data and events are managed by java layer. */
unsigned event_data_looper(void* arg) {
	int x = 0;
	int ret = 0;
	COMSTAT com_stat;
	DWORD error_type = 0;
	DWORD errorVal = 0;
	OVERLAPPED overlapped;
	memset(&overlapped,0,sizeof(overlapped));
	DWORD events_mask = 0;
	DWORD mask_applied = 0;
	jbyte data_buf[1024];
	DWORD num_of_bytes_read;
	jbyteArray data_read;

	int CTS =  0x01;  // 0000001
	int DSR =  0x02;  // 0000010
	int DCD =  0x04;  // 0000100
	int RI  =  0x08;  // 0001000
	int cts,dsr,dcd,ri = 0;
	int event = 0;

	struct looper_thread_params* params = (struct looper_thread_params*) arg;
	JavaVM *jvm = (*params).jvm;
	HANDLE hComm = (*params).hComm;
	jobject looper_ref = (*params).looper_ref;
	int data_enabled = (*params).data_enabled;
	int event_enabled = (*params).event_enabled;
	CRITICAL_SECTION csmutex = (*params).csmutex;

	/* The JNIEnv is valid only in the current thread. So, threads created should attach itself to the VM and obtain a JNI interface pointer. */
	void* env1;
	JNIEnv* env;
	if( (*jvm)->AttachCurrentThread(jvm, &env1, NULL) != JNI_OK ) {
		fprintf(stderr, "%s \n", "NATIVE event_data_looper() thread failed to attach itself to JVM.");
	}
	env = (JNIEnv*) env1;

	jclass SerialComLooper = (*env)->GetObjectClass(env, looper);
	if(SerialComLooper == NULL) {
		fprintf(stderr, "%s \n", "NATIVE event_data_looper() thread could not get class of object of type looper !");
		fprintf(stderr, "%s \n", "NATIVE event_data_looper() thread exiting. Please RETRY registering data listener !");
		EnterCriticalSection(&csmutex);
		close(((struct looper_thread_params*) arg)->thread_handle);
		((struct looper_thread_params*) arg)->thread_handle = 0;
		LeaveCriticalSection(&csmutex);
		return 0;   /* For unrecoverable errors we would like to exit and try again. */
	}

	jmethodID data_mid = (*env)->GetMethodID(env, SerialComLooper, "insertInDataQueue", "([B)V");
	if((*env)->ExceptionOccurred(env)) {
		LOGE(env);
	}
	if(data_mid == NULL) {
		fprintf(stderr, "%s \n", "NATIVE event_data_looper() thread failed to retrieve method id of method insertInDataQueue in class SerialComLooper !");
		fprintf(stderr, "%s \n", "NATIVE event_data_looper() thread exiting. Please RETRY registering data listener !");
		EnterCriticalSection(&csmutex);
		close(((struct looper_thread_params*) arg)->thread_handle);
		((struct looper_thread_params*) arg)->thread_handle = 0;
		LeaveCriticalSection(&csmutex);
		return 0; /* For unrecoverable errors we would like to exit and try again. */
	}

	jmethodID event_mid = (*env)->GetMethodID(env, SerialComLooper, "insertInEventQueue", "(I)V");
	if((*env)->ExceptionOccurred(env)) {
		LOGE(env);
	}
	if(event_mid == NULL) {
		fprintf(stderr, "%s \n", "NATIVE event_data_looper() thread failed to retrieve method id of method insertInEventQueue in class SerialComLooper !");
		fprintf(stderr, "%s \n", "NATIVE event_data_looper() thread exiting. Please RETRY registering event listener !");
		EnterCriticalSection(&csmutex);
		close(((struct looper_thread_params*) arg)->thread_handle);
		((struct looper_thread_params*) arg)->thread_handle = 0;
		LeaveCriticalSection(&csmutex);
		return 0; /* For unrecoverable errors we would like to exit and try again. */
	}
	
	/* Set the event mask this thread will wait for. */
	if(data_enabled == 1) {
		mask_applied = mask_applied | EV_RXCHAR;
	}
	if(event_enabled == 1) {
		mask_applied = mask_applied | EV_BREAK | EV_CTS | EV_DSR | EV_ERR | EV_RING | EV_RLSD | EV_RXFLAG;
	}
	ret = SetCommMask(hComm, mask_applied);
	if(ret == 0) {
		errorVal = GetLastError();
		fprintf(stderr, "%s %ld\n", "NATIVE event_data_looper() failed in SetCommMask() with error number : ", errorVal);
		ClearCommError(hComm, &error_type, &com_stat);
		EnterCriticalSection(&csmutex);
		close(((struct looper_thread_params*) arg)->thread_handle);
		((struct looper_thread_params*) arg)->thread_handle = 0;
		LeaveCriticalSection(&csmutex);
		return 0; /* For unrecoverable errors we would like to exit and try again. */
	}

	/* Only hEvent member need to be initialled and others can be left 0.
	 * The OVERLAPPED structure must contain a handle to a manual-reset event object. */
	overlapped.hEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
	if(overlapped.hEvent == NULL) {
		fprintf(stderr, "%s %ld\n", "NATIVE CreateEvent() in event_data_looper() failed creating overlapped event handle !");
		EnterCriticalSection(&csmutex);
		close(((struct looper_thread_params*) arg)->thread_handle);
		((struct looper_thread_params*) arg)->thread_handle = 0;
		LeaveCriticalSection(&csmutex);
		return 0; /* For unrecoverable errors we would like to exit and try again. */
	}

	/* This keep looping forever until listener is unregistered, waiting for data or event and passing it to java layer which put it in the queue. */
	while(1) {
		ret = WaitCommEvent(hComm, &events_mask, &overlapped);
		if(ret == 0) {
			errorVal = GetLastError();
			fprintf(stderr, "%s %ld\n", "NATIVE event_data_looper() failed in WaitCommEvent() with error number : ", errorVal);
			ClearCommError(hComm, &error_type, &com_stat);
			continue;
		}

		if(events_mask == 0) {
			/* This indicates either the event mask was updated or thread is informed to exit.
			 * If mask is updated continue to wait on new mask otherwise exit thread. */
			if(((struct looper_thread_params*) arg)->thread_exit == 1) {
				EnterCriticalSection(&csmutex);
				close(((struct looper_thread_params*) arg)->thread_handle);
				((struct looper_thread_params*) arg)->thread_handle = 0;
				LeaveCriticalSection(&csmutex);
				return 0;
			}else{
				continue;
			}
		}

		/* Check it is data or control event and enqueue in appropriate queue in java layer. */
		if(events_mask & EV_RXCHAR) {
			ret = ReadFile(hComm, data_buf, (DWORD)count, &num_of_bytes_read, &overlapped);
			if(ret == 0) {
				errorVal = GetLastError();
				if(errorVal == ERROR_IO_PENDING) {
					ret = GetOverlappedResult(hComm, &overlapped, &num_of_bytes_read, TRUE);
				    if(ret == 0) {
				    	errorVal = GetLastError();
				    	fprintf(stderr, "%s %ld\n", "NATIVE GetOverlappedResult() in event_data_looper() failed with error number : ", errorVal);
				    }else {
				    	/* once we have successfully read the data, pass this to java layer inserting in data queue. */
						data_read = (*env)->NewByteArray(env, num_of_bytes_read);
						(*env)->SetByteArrayRegion(env, data_read, 0, num_of_bytes_read, data_buf);
				    	(*env)->CallVoidMethod(env, looper, data_mid, dataRead);
				    	if((*env)->ExceptionOccurred(env)) {
				    		LOGE(env);
				    	}
				    }
				}
			}
		}else {
			/* It is control event, so figure out what event(s) occurred and enqueue it in event queue. */
			cts = (events_mask & EV_CTS)  ? 1 : 0;
			dsr = (events_mask & EV_DSR)  ? 1 : 0;
			dcd = (events_mask & EV_RLSD) ? 1 : 0;
			ri = (events_mask & EV_RING)  ? 1 : 0;
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
			fprintf(stderr, "%s %d\n", "NATIVE event_data_looper() sending bit mapped events ", event);

	    	/* Pass this to java layer inserting event in event queue. */
	    	(*env)->CallVoidMethod(env, looper, event_mid, event);
	    	if((*env)->ExceptionOccurred(env)) {
	    		LOGE(env);
	    	}
		}
	}  /* Go back to loop again waiting for an event to occur. */

	CloseHandle(overlapped.hEvent);
	return 0;
}

#endif /* End identifying and compiling for Windows OS */


