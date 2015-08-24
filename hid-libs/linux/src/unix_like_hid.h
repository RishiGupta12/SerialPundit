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

#ifndef UNIX_LIKE_HID_H_
#define UNIX_LIKE_HID_H_

#if defined (__linux__)
#include <libudev.h>
#endif
#include <pthread.h>
#if defined (__APPLE__)
#include <CoreFoundation/CoreFoundation.h>
#include <IOKit/IOKitLib.h>
#include <IOKit/serial/IOSerialKeys.h>
#include <IOKit/serial/ioss.h>
#include <IOKit/IOBSD.h>
#include <IOKit/IOMessage.h>
#include <IOKit/usb/IOUSBLib.h>
#endif
#include <jni.h>

/* Constant string defines */
#define E_GETSTRUTFCHARSTR "JNI call GetStringUTFChars failed !"


/* Custom error codes and messages for SCM library */
#define ERROR_OFFSET 15000


/* function prototypes (declared in reverse order of use) */
extern void throw_serialcom_exception(JNIEnv *env, int type, int error_code, const char *);

#endif /* UNIX_LIKE_HID_H_ */
