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
#include <sys/types.h>
#include <linux/serial.h>
#endif
#if defined (__APPLE__)
#endif
#include <jni.h>
#include "unix_like_serial_lib.h"

/*
 * Find the address and IRQ number associated with the given handle of serial port.
 */
jstring find_address_irq_for_given_com_port(JNIEnv *env, jlong fd) {
	char serial_info[256];
	jstring addressIRQInfo = NULL;
#if defined (__linux__)
	int ret = 0;
	struct serial_struct port_info;
#endif

#if defined (__APPLE__)
#endif

	memset(serial_info, '\0', 256);

#if defined (__linux__)
	port_info.reserved_char[0] = 0;
	errno = 0;
	ret = ioctl(fd, TIOCGSERIAL, &port_info);
	if(ret < 0) {
		throw_serialcom_exception(env, 1, errno, NULL);
		return NULL;
	}

	snprintf(serial_info, 256, "Port: 0x%.4x, IRQ: %d", port_info.port, port_info.irq);
	addressIRQInfo = (*env)->NewStringUTF(env, serial_info);
	if((addressIRQInfo == NULL) || ((*env)->ExceptionOccurred(env) != NULL)) {
		(*env)->ExceptionClear(env);
		throw_serialcom_exception(env, 3, 0, E_NEWSTRUTFSTR);
		return NULL;
	}
#endif

#if defined (__APPLE__)
	/* TODO */
#endif

	return addressIRQInfo;
}

