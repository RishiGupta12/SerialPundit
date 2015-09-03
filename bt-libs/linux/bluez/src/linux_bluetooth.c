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

/* - This file contains native code to communicate with tty-style port in Unix-like operating systems.
 * - When printing error number, number returned by OS is printed as it is.
 * - There will be only one instance of this shared library at runtime. So if something goes wrong
 *   it will affect everything, until this library has been unloaded and then loaded again.
 * - Wherever possible avoid JNI data types.
 * - Sometimes, the JNI does not like some pointer arithmetic so it is avoided wherever possible. */

#if defined (__linux__) || defined (__APPLE__) || defined (__SunOS) || defined(__sun) || defined(__FreeBSD__) \
		|| defined(__OpenBSD__) || defined(__NetBSD__) || defined(__hpux__) || defined(_AIX)

/* Make primitives such as read and write resume, in case they are interrupted by signal,
 * before they actually start reading or writing data. The partial success case are handled
 * at appropriate places in functions applicable.
 * For details see features.h about MACROS defined below. */
#ifndef _BSD_SOURCE
#define _BSD_SOURCE
#endif
#ifndef _GNU_SOURCE
#define _GNU_SOURCE
#endif

/* C */
#include <stdarg.h>      /* ISO C Standard. Variable arguments  */
#include <stdio.h>       /* ISO C99 Standard: Input/output      */
#include <stdlib.h>      /* Standard ANSI routines              */
#include <string.h>      /* String function definitions         */
#include <errno.h>       /* Error number definitions            */

/* Unix */

#if defined (__linux__)
#endif

#if defined (__APPLE__)
#endif

#if defined (__SunOS)
#endif

/* jni_md.h contains the machine-dependent typedefs for data types. Instruct compiler to include it. */
#include <jni.h>
#include "linux_bluetooth.h"

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComBluetoothJNIBridge
 * Method:    listBluetoothAdaptorsWithInfo
 * Signature: ()[Ljava/lang/String;
 *
 * @return array of Strings containing info about bluetooth adaptors found otherwise NULL if error occurs or no adaptors are found.
 * @throws SerialComException if any JNI function, system call or C function fails.
 *
 * Find local bluetooth adaptors with information about them using platform specific facilities.
 */
JNIEXPORT jobjectArray JNICALL Java_com_embeddedunveiled_serial_internal_SerialComBluetoothJNIBridge_listBluetoothAdaptorsWithInfo
  (JNIEnv *env, jobject obj) {
	return list_local_bt_adaptors(env);
}

#endif
