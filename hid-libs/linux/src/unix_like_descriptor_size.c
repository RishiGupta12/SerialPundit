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
<<<<<<< HEAD
#include <linux/hidraw.h>
#if defined (__APPLE__)
#endif
#include <sys/ioctl.h>
#include "unix_like_hid.h"

/*
 */
=======
#include <sys/ioctl.h>

#if defined (__linux__)
#include <linux/hidraw.h>
#endif

#if defined (__APPLE__)
#endif

#include "unix_like_hid.h"

/*

>>>>>>> upstream/master
jint get_report_descriptor_size(JNIEnv *env, jlong fd) {
	int ret = 0;
	int size = 0;

#if defined (__linux__)
	errno = 0;
	ret = ioctl(fd, HIDIOCGRDESCSIZE, &size);
	if(ret < 0) {
		throw_serialcom_exception(env, 1, errno, NULL);
		return -1;
	}
#endif

	return (jint)size;
}
<<<<<<< HEAD
=======
*/
<<<<<<< HEAD
>>>>>>> upstream/master
=======

>>>>>>> upstream/master
