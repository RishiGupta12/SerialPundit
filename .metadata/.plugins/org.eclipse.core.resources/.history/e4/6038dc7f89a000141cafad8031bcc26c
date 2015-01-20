/***************************************************************************************************
 * Author : Rishi Gupta
 * Email  : gupt21@gmail.com
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

#if defined (__linux__) || defined (__APPLE__) || defined (__SunOS)

#ifndef UNIX_LIKE_SERIAL_LIB_H_
#define UNIX_LIKE_SERIAL_LIB_H_

#include <pthread.h>		/* POSIX thread definitions	      */

/* Structure representing data that is passed to each data looper thread with info corresponding to that file descriptor. */
struct com_thread_params {
	JavaVM *jvm;
	int fd;
	jobject looper;
	pthread_t data_thread_id;
	pthread_t event_thread_id;
	int evfd;
	int epfd;
	pthread_mutex_t mutex;
};

/* function prototypes */
extern void LOGE(JNIEnv *env);
extern int serial_delay(unsigned usecs);
extern void *data_looper(void *params);
extern void *event_looper(void *params);

#endif /* UNIX_LIKE_SERIAL_LIB_H_ */

#endif /* end compiling for Unix-like OS */
