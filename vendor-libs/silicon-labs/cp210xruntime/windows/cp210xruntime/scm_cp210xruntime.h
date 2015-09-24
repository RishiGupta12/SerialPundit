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

#ifndef SCM_CP210XRUNTIME_H_
#define SCM_CP210XRUNTIME_H_

#include <jni.h>
#include "CP210xRuntimeDLL.h"    /* CP210xRuntimeDLL.h header file from vendor */

/* Mask and Latch value bit definitions. These must match their values in java layer. */
#define SCM_CP210x_GPIO_0  0x0001
#define SCM_CP210x_GPIO_1  0x0002
#define SCM_CP210x_GPIO_2  0x0004
#define SCM_CP210x_GPIO_3  0x0008
#define SCM_CP210x_GPIO_4  0x0010
#define SCM_CP210x_GPIO_5  0x0020
#define SCM_CP210x_GPIO_6  0x0040
#define SCM_CP210x_GPIO_7  0x0080
#define SCM_CP210x_GPIO_8  0x0100
#define SCM_CP210x_GPIO_9  0x0200
#define SCM_CP210x_GPIO_10 0x0400
#define SCM_CP210x_GPIO_11 0x0800
#define SCM_CP210x_GPIO_12 0x1000
#define SCM_CP210x_GPIO_13 0x2000
#define SCM_CP210x_GPIO_14 0x4000
#define SCM_CP210x_GPIO_15 0x8000

/* Constant string defines */
#define FAILTHOWEXP "JNI call ThrowNew failed to throw exception !"
#define SCOMEXPCLASS "com/embeddedunveiled/serial/SerialComException"
#define E_FINDCLASSSCOMEXPSTR "Can not find class com/embeddedunveiled/serial/SerialComException, Probably out of memory."
#define E_NEWSTRUTFSTR "JNI call NewStringUTF failed !"

#endif /* SCM_CP210XRUNTIME_H_ */

