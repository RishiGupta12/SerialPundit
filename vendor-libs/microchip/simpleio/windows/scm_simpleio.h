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

#ifndef SCM_SIMPLEIO_H_
#define SCM_SIMPLEIO_H_

#include <jni.h>

/* These must match their values in Java layer */
#define OFF 0
#define ON 1
#define TOGGLE 3
#define BLINKSLOW 4
#define BLINKFAST 5

/* Constant string defines */
#define E_UNKNOWN "Unknown error occurred !"
#define FAILTHOWEXP "JNI call ThrowNew failed to throw exception !"
#define SCOMEXPCLASS "com/embeddedunveiled/serial/SerialComException"
#define E_FINDCLASSSCOMEXPSTR "Can not find class com/embeddedunveiled/serial/SerialComException, Probably out of memory."
#define E_NEWSTRUTFSTR "JNI call NewStringUTF failed !"
#define E_GETSTRUTFCHARSTR "JNI call GetStringUTFChars failed !"

int LOGE(const char *msg_a, const char *msg_b);
int LOGEN(const char *msg_a, const char *msg_b, unsigned int error_num);
void throw_serialcom_exception(JNIEnv *env, int type, int error_code, const char *msg);

#endif /* SCM_SIMPLEIO_H_ */