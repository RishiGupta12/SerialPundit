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
#include "unix_like_serial_lib.h"

/* Allocate memory of given size and initializes elements as appropriate.
 * The elements in this array list will be java.lang.String object constructed
 * from an array of characters in modified UTF-8 encoding by calling JNI
 * NewStringUTF(..) function. */
void init_jstrarraylist(struct jstrarray_list *al, int initial_size) {
	al->base = (jstring *) calloc(initial_size, sizeof(jstring));
	if(al->base == NULL) {
		fprintf(stderr, "array calloc %s %d\n", "failed : ", errno);
		fflush(stderr);
	}
	al->index = 0;
	al->current_size = initial_size;
}

/* Insert given jstring object reference at next position expanding memory size
 * allocated if required. */
void insert_jstrarraylist(struct jstrarray_list *al, jstring element) {
	if(al->index >= al->current_size) {
		al->current_size = al->current_size * 2;
		al->base = (jstring *) realloc(al->base, al->current_size * sizeof(jstring));
		if(al->base == NULL) {

			fprintf(stderr, "array realloc %s %d\n", "failed : ", errno);
			fflush(stderr);
		}
	}
	al->base[al->index] = element;
	al->index++;
}

/* Java garbage collector is responsible for releasing memory occupied by jstring objects.
 * We just free memory that we allocated explicitly. */
void free_jstrarraylist(struct jstrarray_list *al) {
	free(al->base);
}
