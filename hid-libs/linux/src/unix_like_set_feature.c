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

#if defined (__APPLE__)
#include <CoreFoundation/CoreFoundation.h>
#include <IOKit/hid/IOHIDKeys.h>
#include <IOKit/hid/IOHIDManager.h>
#endif

#include "unix_like_hid.h"

#if defined (__linux__)
/*
 * return number of bytes read if function succeeds otherwise -1 if error occurs.
 * throws SerialComException if any JNI function, system call or C function fails.
 */
jint linux_send_feature_report(JNIEnv *env, jlong fd, jbyte reportID, jbyteArray report, jint length) {
	int ret = -1;
	int count = 0;

	count = (int) (*env)->GetArrayLength(env, report);
	jbyte* buffer = (jbyte *) malloc(count + 1);

	/* The first byte of SFEATURE and GFEATURE is the report number */
	buffer[0] = reportID;

	(*env)->GetByteArrayRegion(env, report, 0, count, &buffer[1]);
	if((*env)->ExceptionOccurred(env) != NULL) {
		throw_serialcom_exception(env, 3, 0, E_GETBYTEARRREGIONSTR);
		return -1;
	}

	errno = 0;
	/* this ioctl returns number of bytes written to the device */
	ret = ioctl(fd, HIDIOCSFEATURE(count+1), buffer);
	if(ret < 0) {
		throw_serialcom_exception(env, 1, errno, NULL);
		return -1;
	}

	return ret;
}
#endif

#if defined (__APPLE__)
#endif
