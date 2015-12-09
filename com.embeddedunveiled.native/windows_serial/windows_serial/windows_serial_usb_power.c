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

/*
 * Find information about current power state and selectiive suspend of a USB device.
 */
jobjectArray get_usbdev_powerinfo(JNIEnv *env, jstring comPortName) {

	int x = 0;
	const char* port_name_to_match = NULL;
	char com_port_name_to_match[256];
	char buffer[512];
	jclass strClass = NULL;
	jstring info = NULL;
	jobjectArray powerInfo = NULL;
	struct jstrarray_list list = { 0 };

	init_jstrarraylist(&list, 10);

	port_name_to_match = (*env)->GetStringUTFChars(env, comPortName, NULL);
	if ((com_port_name_to_match == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_GETSTRUTFCHARSTR);
		return NULL;
	}
	memset(com_port_name_to_match, '\0', 256);
	strcpy_s(com_port_name_to_match, 256, port_name_to_match);

	info = (*env)->NewStringUTF(env, buffer);
	if ((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		(*env)->ReleaseStringUTFChars(env, comPortName, port_name_to_match);
		throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
		return NULL;
	}
	insert_jstrarraylist(&list, info);

	info = (*env)->NewStringUTF(env, buffer);
	if ((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		(*env)->ReleaseStringUTFChars(env, comPortName, port_name_to_match);
		throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
		return NULL;
	}
	insert_jstrarraylist(&list, info);

	info = (*env)->NewStringUTF(env, buffer);
	if ((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		(*env)->ReleaseStringUTFChars(env, comPortName, port_name_to_match);
		throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
		return NULL;
	}
	insert_jstrarraylist(&list, info);

	info = (*env)->NewStringUTF(env, buffer);
	if ((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		(*env)->ReleaseStringUTFChars(env, comPortName, port_name_to_match);
		throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
		return NULL;
	}
	insert_jstrarraylist(&list, info);

	info = (*env)->NewStringUTF(env, buffer);
	if ((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		(*env)->ReleaseStringUTFChars(env, comPortName, port_name_to_match);
		throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
		return NULL;
	}
	insert_jstrarraylist(&list, info);

	info = (*env)->NewStringUTF(env, buffer);
	if ((info == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		(*env)->ReleaseStringUTFChars(env, comPortName, port_name_to_match);
		throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
		return NULL;
	}
	insert_jstrarraylist(&list, info);

	(*env)->ReleaseStringUTFChars(env, comPortName, port_name_to_match);

	/* create a JAVA/JNI style array of String object, populate it and return to java layer. */
	strClass = (*env)->FindClass(env, JAVALSTRING);
	if ((strClass == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		(*env)->ExceptionClear(env);
		free_jstrarraylist(&list);
		throw_serialcom_exception(env, 3, 0, E_FINDCLASSSSTRINGSTR);
		return NULL;
	}

	powerInfo = (*env)->NewObjectArray(env, (jsize)list.index, strClass, NULL);
	if ((powerInfo == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		(*env)->ExceptionClear(env);
		free_jstrarraylist(&list);
		throw_serialcom_exception(env, 3, 0, E_NEWOBJECTARRAYSTR);
		return NULL;
	}

	for (x = 0; x < list.index; x++) {
		(*env)->SetObjectArrayElement(env, powerInfo, x, list.base[x]);
		if ((*env)->ExceptionOccurred(env)) {
			(*env)->ExceptionClear(env);
			free_jstrarraylist(&list);
			throw_serialcom_exception(env, 3, 0, E_SETOBJECTARRAYSTR);
			return NULL;
		}
	}

	free_jstrarraylist(&list);
	return powerInfo;
}

