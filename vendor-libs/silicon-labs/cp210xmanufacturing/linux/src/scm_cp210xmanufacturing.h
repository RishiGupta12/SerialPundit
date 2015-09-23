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

#ifndef SCM_CP210XMANUFACTURING_H_
#define SCM_CP210XMANUFACTURING_H_

#include <jni.h>

/* Mask and Latch value bit definitions. These must match their values in java layer. */
#define SCM_CP210x_RETURN_SERIAL_NUMBER 0x00
#define SCM_CP210x_RETURN_DESCRIPTION   0x01
#define SCM_CP210x_RETURN_FULL_PATH     0x02

// Flush Buffer definitions
// When these bits are set, the device will flush that buffer
#define SCM_FC_OPEN_TX	 0x01
#define SCM_FC_OPEN_RX	 0x02
#define SCM_FC_CLOSE_TX	 0x04
#define SCM_FC_CLOSE_RX 0x08

/* Constant string defines */
#define FAILTHOWEXP "JNI call ThrowNew failed to throw exception !"
#define SCOMEXPCLASS "com/embeddedunveiled/serial/SerialComException"
#define E_FINDCLASSSCOMEXPSTR "Can not find class com/embeddedunveiled/serial/SerialComException, Probably out of memory."
#define E_NEWSTRUTFSTR "JNI call NewStringUTF failed !"
#define E_GETSTRUTFCHARSTR "JNI call GetStringUTFChars failed !"
#define E_NEWINTARRAYSTR "JNI call NewIntArray failed !"
#define E_SETINTARRREGIONSTR "JNI call SetIntArrayRegion failed. Probably index out of bound !"

/* function prototypes (declared in reverse order of use) */
int LOGE(const char *error_msg);
void throw_serialcom_exception(JNIEnv *env, int type, int error_code, const char *);

#endif /* SCM_CP210XMANUFACTURING_H_ */
