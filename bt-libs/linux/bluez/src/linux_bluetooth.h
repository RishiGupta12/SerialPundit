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

#ifndef LINUX_BLUETOOTH_H_
#define LINUX_BLUETOOTH_H_

#if defined (__linux__)
#endif

#if defined (__APPLE__)
#endif

#include <jni.h>

/* It is possible to re-factor some functions or things like that to factor in common function.
 * We have knowingly kept internal dependencies to minimum so as to accommodate future changes.*/

/* Constant string defines */
#define SCOMEXPCLASS "com/embeddedunveiled/serial/SerialComException"
#define JAVALSTRING "java/lang/String"
#define FAILTHOWEXP "JNI call ThrowNew failed to throw exception !"
#define E_FINDCLASSSCOMEXPSTR "Can not find class com/embeddedunveiled/serial/SerialComException. Probably out of memory !"
#define E_FINDCLASSSSTRINGSTR "Can not find class java/lang/String. Probably out of memory !"
#define E_NEWSTRUTFSTR "JNI call NewStringUTF failed !"
#define E_MALLOCSTR "malloc() failed to allocate requested memory !"
#define E_NEWOBJECTARRAYSTR "JNI call NewObjectArray failed. Probably out of memory !"
#define E_SETOBJECTARRAYSTR "JNI call SetObjectArrayElement failed. Either index violation or wrong class used !"
#define E_HCIOPENDEV "Could not open BT HCI device !"
#define E_HCIREADNAME "Could not read local name of BT HCI device !"
#define E_HCIBTADDR "Could not determine address of BT HCI device !"

/* This holds information for implementing dynamically growing array in C language. */
struct jstrarray_list {
	jstring *base;      /* pointer to an array of pointers to string */
	int index;         /* array element index                       */
	int current_size;  /* size of this array                        */
};

/* function prototypes (declared in reverse order of use) */
extern int LOGE(const char *error_msg);
extern void throw_serialcom_exception(JNIEnv *env, int type, int error_code, const char *);
extern void free_jstrarraylist(struct jstrarray_list *al);
extern void insert_jstrarraylist(struct jstrarray_list *al, jstring element);
extern void init_jstrarraylist(struct jstrarray_list *al, int initial_size);
extern jobjectArray list_local_bt_adaptors(JNIEnv *env);

#endif /* LINUX_BLUETOOTH_H_ */
