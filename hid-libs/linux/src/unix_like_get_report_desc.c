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
#include <sys/ioctl.h>

#if defined (__linux__)
#include <linux/types.h>
#include <linux/input.h>
#include <linux/hidraw.h>
#endif

#if defined (__APPLE__)
#include <CoreFoundation/CoreFoundation.h>
#include <IOKit/hid/IOHIDKeys.h>
#include <IOKit/hid/IOHIDManager.h>
#endif

#include <jni.h>
#include "unix_like_hid.h"

#if defined (__linux__)
/*
 * Try to read report descriptor from HID device.
 * 
 * @return byte array containing report descriptor values read from given HID device, NULL if
 *         any error occurs.
 * @throws SerialComException if any JNI function, system call or C function fails.
 */
jbyteArray linux_get_report_descriptor(JNIEnv *env, jlong fd) {
	int ret = -1;
	int desc_size = 0;
	struct hidraw_report_descriptor rpt_desc;
	jbyteArray reportDescriptorRead;

	/* get report descriptor size */
	errno = 0;
	ret = ioctl(fd, HIDIOCGRDESCSIZE, &desc_size);
	if(ret < 0) {
		throw_serialcom_exception(env, 1, errno, NULL);
		return NULL;
	}

	memset(&rpt_desc, 0x0, sizeof(rpt_desc));

	/* get report descriptor */
	rpt_desc.size = desc_size;
	errno = 0;
	ret = ioctl(fd, HIDIOCGRDESC, &rpt_desc);
	if(ret < 0) {
		throw_serialcom_exception(env, 1, errno, NULL);
		return NULL;
	}

	/* construct java array and pass it to java layer */
	reportDescriptorRead = (*env)->NewByteArray(env, rpt_desc.size);
	if((reportDescriptorRead == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		throw_serialcom_exception(env, 3, 0, E_NEWBYTEARRAYSTR);
		return NULL;
	}

	/* values saved are 8 bit, sign does not create difference */
	(*env)->SetByteArrayRegion(env, reportDescriptorRead, (jsize)0, (jsize)rpt_desc.size, (jbyte *)rpt_desc.value);
	if((*env)->ExceptionOccurred(env)) {
		throw_serialcom_exception(env, 3, 0, E_SETBYTEARRREGIONSTR);
		return NULL;
	}

	return reportDescriptorRead;
}

#endif

#if defined (__APPLE__)
#endif

