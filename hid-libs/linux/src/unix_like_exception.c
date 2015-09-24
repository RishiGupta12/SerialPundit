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

#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <stdio.h>
#include <errno.h>
#include <jni.h>
#include "unix_like_hid.h"

/*
 * For C-standard/POSIX/OS specific/Custom/JNI errors, this function is called. It sets a pointer which is checked
 * by java method when native function returns. If the pointer is set exception of class as set by this function is thrown.
 *
 * The type 1 indicates standard (C-standard/POSIX/OS specific) error, 2 indicate custom (defined by this library) error,
 * 3 indicates custom error with message string.
 */
void throw_serialcom_exception(JNIEnv *env, int type, int error_code, const char *msg) {
	jint ret = 0;
	char buffer[256];
	char *custom_error_msg = NULL;
<<<<<<< HEAD
	jclass serialComExceptionClass = NULL;
=======

>>>>>>> upstream/master
#if _POSIX_C_SOURCE >= 200112L || _XOPEN_SOURCE >= 600 && ! _GNU_SOURCE
#else
	char *error_msg = NULL;
#endif
	(*env)->ExceptionClear(env);
<<<<<<< HEAD
	serialComExceptionClass = (*env)->FindClass(env, SCOMEXPCLASS);
	if((serialComExceptionClass == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
=======

	/* exception of this class will be thrown whenever it occurs */
	jclass serialComExpCls = (*env)->FindClass(env, SCOMEXPCLASS);
	if((serialComExpCls == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
>>>>>>> upstream/master
		(*env)->ExceptionClear(env);
		LOGE(E_FINDCLASSSCOMEXPSTR);
		return;
	}

	if(type == 1) {
		/* Caller has given posix/os-standard error code, get error message corresponding to this code. */
		/* This need to be made more portable to remove compiler specific dependency */
#if _POSIX_C_SOURCE >= 200112L || _XOPEN_SOURCE >= 600 && ! _GNU_SOURCE
		strerror_r(error_code, buffer, 256);
<<<<<<< HEAD
		ret = (*env)->ThrowNew(env, serialComExceptionClass, buffer);
=======
		ret = (*env)->ThrowNew(env, serialComExpCls, buffer);
>>>>>>> upstream/master
		if(ret < 0) {
			LOGE(FAILTHOWEXP);
		}
#else
		error_msg = strerror_r(error_code, buffer, 256);
		if(error_msg == NULL) {
<<<<<<< HEAD
			ret = (*env)->ThrowNew(env, serialComExceptionClass, buffer);
=======
			ret = (*env)->ThrowNew(env, serialComExpCls, buffer);
>>>>>>> upstream/master
			if(ret < 0) {
				LOGE(FAILTHOWEXP);
			}
		}else {
<<<<<<< HEAD
			ret = (*env)->ThrowNew(env, serialComExceptionClass, error_msg);
=======
			ret = (*env)->ThrowNew(env, serialComExpCls, error_msg);
>>>>>>> upstream/master
			if(ret < 0) {
				LOGE(FAILTHOWEXP);
			}
		}
#endif
	}else if(type == 2) {
		/* Caller has given custom error code, need to get exception message corresponding to this code. */
		switch (error_code) {

		}
<<<<<<< HEAD
		ret = (*env)->ThrowNew(env, serialComExceptionClass, custom_error_msg);
=======
		ret = (*env)->ThrowNew(env, serialComExpCls, custom_error_msg);
>>>>>>> upstream/master
		if(ret < 0) {
			LOGE(FAILTHOWEXP);
		}
	}else {
		/* Caller has given exception message explicitly */
<<<<<<< HEAD
		ret = (*env)->ThrowNew(env, serialComExceptionClass, msg);
=======
		ret = (*env)->ThrowNew(env, serialComExpCls, msg);
>>>>>>> upstream/master
		if(ret < 0) {
			LOGE(FAILTHOWEXP);
		}
	}
}
