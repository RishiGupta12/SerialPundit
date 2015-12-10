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
#include <errno.h>
#include <jni.h>
#include "windows_hid.h"

/*
 * Prints fatal error on console. Java application can deploy a Java level framework which redirects
 * data for STDERR to a log file.
 */
int LOGE(const char *msg_a, const char *msg_b) {
	fprintf(stderr, "%s , %s\n", msg_a, msg_b);
	fflush(stderr);
	return 0;
}

int LOGEN(const char *msg_a, const char *msg_b, unsigned int error_num) {
	fprintf(stderr, "%s , %s , error code : %d\n", msg_a, msg_b, error_num);
	fflush(stderr);
	return 0;
}

/*
 * For C-standard/POSIX/OS specific/Custom/JNI errors, this function is called. It sets a pointer which is checked
 * by java method when native function returns. If the pointer is set exception of class as set by this function is
 * thrown.
 *
 * The type 1 indicates standard (C-standard/POSIX) error, 2 indicate custom (defined by this library)
 * error, 3 indicates custom error with message string, 4 indicates error number specific to Windows OS.
 */
void throw_serialcom_exception(JNIEnv *env, int type, int error_code, const char *msg) {

	DWORD ret = -1;
	errno_t err_code = 0;
	char buffer[256];
	jclass serialComExceptionClass = NULL;

	(*env)->ExceptionClear(env);
	serialComExceptionClass = (*env)->FindClass(env, SCOMEXPCLASS);
	if ((serialComExceptionClass == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		(*env)->ExceptionClear(env);
		LOGE(E_FINDCLASSSCOMEXPSTR, FAILTHOWEXP);
		return;
	}

	switch (type) {
		case 4:
			/* Caller has given Windows error code */
			memset(buffer, '\0', sizeof(buffer));
			ret = FormatMessageA(FORMAT_MESSAGE_FROM_SYSTEM | FORMAT_MESSAGE_IGNORE_INSERTS,
				NULL, error_code, MAKELANGID(LANG_ENGLISH, SUBLANG_ENGLISH_US),
				(LPSTR)&buffer, sizeof(buffer), NULL);
			if (ret == 0) {
				LOGEN(FAILTHOWEXP, "FormatMessageA()", GetLastError());
			}
			ret = (*env)->ThrowNew(env, serialComExceptionClass, buffer);
			if (ret < 0) {
				LOGE(FAILTHOWEXP, buffer);
			}
			break;
		case 3:
			/* Caller has given exception message explicitly */
			ret = (*env)->ThrowNew(env, serialComExceptionClass, msg);
			if (ret < 0) {
				LOGE(FAILTHOWEXP, msg);
			}
			break;
		case 2:
			/* Caller has given custom error code, need to get exception message corresponding to this code. */
			break;
		case 1:
			/* Caller has given posix error code, get error message corresponding to this code. */
			return;
		case 5:
			/* Caller has given <errno.h> error code for windows, get error message corresponding to this code. */
			memset(buffer, '\0', sizeof(buffer));
			err_code = strerror_s(buffer, sizeof(buffer), error_code);
			if (err_code != 0) {
				LOGEN(FAILTHOWEXP, "strerror_s()", err_code);
			}
	}
}
