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
#include <stdio.h>

#if defined (__linux__) || defined (__APPLE__)
#include <unistd.h>
#include <errno.h>
#endif

#include "scm_ftdi_d2xx.h"

/* Allocate memory of given size and initializes elements as appropriate.
 * The elements in this array list will be java.lang.String object constructed
 * from an array of characters in modified UTF-8 encoding by calling JNI
 * NewStringUTF(..) function. */
void init_jstrarraylist(struct jstrarray_list *al, int initial_size) {
	al->base = (jstring *) calloc(initial_size, sizeof(jstring));
	if(al->base == NULL) {
		fprintf(stderr, "array calloc %s %d\n", "failed : ", errno);
		fflush(stderr);
	}
	al->index = 0;
	al->current_size = initial_size;
}

/* Insert given jstring object reference at next position expanding memory size
 * allocated if required. */
void insert_jstrarraylist(struct jstrarray_list *al, jstring element) {
	if(al->index >= al->current_size) {
		al->current_size = al->current_size * 2;
		al->base = (jstring *) realloc(al->base, al->current_size * sizeof(jstring));
		if(al->base == NULL) {
			fprintf(stderr, "array realloc %s %d\n", "failed : ", errno);
			fflush(stderr);
		}
	}
	al->base[al->index] = element;
	al->index++;
}

/* Java garbage collector is responsible for releasing memory occupied by jstring objects.
 * We just free memory that we allocated explicitly. */
void free_jstrarraylist(struct jstrarray_list *al) {
	free(al->base);
}

/*
 * For C-standard/POSIX/OS specific/Custom/JNI errors, this function is called. It sets a pointer which is checked
 * by java method when native function returns. If the pointer is set exception of class as set by this function is
 * thrown.
 *
 * The type 1 indicates standard (C-standard/POSIX/OS specific) error, 2 indicate custom (defined by this library)
 * error, 3 indicates custom error with message string.
 */
void throw_serialcom_exception(JNIEnv *env, int type, int error_code, const char *msg) {
	jint ret = 0;
	char buffer[256];
	char *custom_error_msg = NULL;
	jclass serialComExceptionClass = NULL;
#if _POSIX_C_SOURCE >= 200112L || _XOPEN_SOURCE >= 600 && ! _GNU_SOURCE
#else
	char *error_msg = NULL;
#endif
	(*env)->ExceptionClear(env);
	serialComExceptionClass = (*env)->FindClass(env, SCOMEXPCLASS);
	if((serialComExceptionClass == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		(*env)->ExceptionClear(env);
		LOGE(E_FINDCLASSSCOMEXPSTR);
		return;
	}

	if(type == 1) {
		/* Caller has given posix/os-standard error code, get error message corresponding to this code. */
		/* This need to be made more portable to remove compiler specific dependency */
#if _POSIX_C_SOURCE >= 200112L || _XOPEN_SOURCE >= 600 && ! _GNU_SOURCE
		strerror_r(error_code, buffer, 256);
		ret = (*env)->ThrowNew(env, serialComExceptionClass, buffer);
		if(ret < 0) {
			LOGE(FAILTHOWEXP);
		}
#else
		error_msg = strerror_r(error_code, buffer, 256);
		if(error_msg == NULL) {
			ret = (*env)->ThrowNew(env, serialComExceptionClass, buffer);
			if(ret < 0) {
				LOGE(FAILTHOWEXP);
			}
		}else {
			ret = (*env)->ThrowNew(env, serialComExceptionClass, error_msg);
			if(ret < 0) {
				LOGE(FAILTHOWEXP);
			}
		}
#endif
	}else if(type == 2) {
		/* Caller has given custom error code, need to get exception message corresponding to this code. */
		switch (error_code) {
			case FT_INVALID_HANDLE: custom_error_msg = "FT_INVALID_HANDLE";
				break;
			case FT_DEVICE_NOT_FOUND: custom_error_msg = "FT_DEVICE_NOT_FOUND";
				break;
			case FT_DEVICE_NOT_OPENED: custom_error_msg = "FT_DEVICE_NOT_OPENED";
				break;
			case FT_IO_ERROR: custom_error_msg = "FT_IO_ERROR";
				break;
			case FT_INSUFFICIENT_RESOURCES: custom_error_msg = "FT_INSUFFICIENT_RESOURCES";
				break;
			case FT_INVALID_PARAMETER: custom_error_msg = "FT_INVALID_PARAMETER";
				break;
			case FT_INVALID_BAUD_RATE: custom_error_msg = "FT_INVALID_BAUD_RATE";
				break;
			case FT_DEVICE_NOT_OPENED_FOR_ERASE: custom_error_msg = "FT_DEVICE_NOT_OPENED_FOR_ERASE";
				break;
			case FT_DEVICE_NOT_OPENED_FOR_WRITE: custom_error_msg = "FT_DEVICE_NOT_OPENED_FOR_WRITE";
				break;
			case FT_FAILED_TO_WRITE_DEVICE: custom_error_msg = "FT_FAILED_TO_WRITE_DEVICE";
				break;
			case FT_EEPROM_READ_FAILED: custom_error_msg = "FT_EEPROM_READ_FAILED";
				break;
			case FT_EEPROM_WRITE_FAILED: custom_error_msg = "FT_EEPROM_WRITE_FAILED";
				break;
			case FT_EEPROM_ERASE_FAILED: custom_error_msg = "FT_EEPROM_ERASE_FAILED";
				break;
			case FT_EEPROM_NOT_PRESENT: custom_error_msg = "FT_EEPROM_NOT_PRESENT";
				break;
			case FT_EEPROM_NOT_PROGRAMMED: custom_error_msg = "FT_EEPROM_NOT_PROGRAMMED";
				break;
			case FT_INVALID_ARGS: custom_error_msg = "FT_INVALID_ARGS";
				break;
			case FT_NOT_SUPPORTED: custom_error_msg = "FT_NOT_SUPPORTED";
				break;
			case FT_OTHER_ERROR: custom_error_msg = "FT_OTHER_ERROR";
				break;
			case FT_DEVICE_LIST_NOT_READY: custom_error_msg = "FT_DEVICE_LIST_NOT_READY";
				break;
			default : custom_error_msg = "Unknown error occurred !";
		}
		ret = (*env)->ThrowNew(env, serialComExceptionClass, custom_error_msg);
		if(ret < 0) {
			LOGE(FAILTHOWEXP);
		}
	}else {
		/* Caller has given exception message explicitly */
		ret = (*env)->ThrowNew(env, serialComExceptionClass, msg);
		if(ret < 0) {
			LOGE(FAILTHOWEXP);
		}
	}
}
