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
#include "unix_like_serial_lib.h"

/* Allocate memory of given size and initializes elements as appropriate. */
void init_array_list(struct array_list *al, int initial_size) {
	al->base = (char **) calloc(initial_size, sizeof(char *));
	if(al->base == NULL) {
		fprintf(stderr, "array calloc %s %d\n", "failed : ", errno);
		fflush(stderr);
	}
	al->index = 0;
	al->current_size = initial_size;
}

/* Insert given string at next position expanding memory size allocated
 * if required. */
void insert_array_list(struct array_list *al, char *element) {
	if(al->index >= al->current_size) {
		al->current_size = al->current_size * 2;
		al->base = (char **) realloc(al->base, al->current_size * sizeof(char *));
		if(al->base == NULL) {
			fprintf(stderr, "array realloc %s %d\n", "failed : ", errno);
			fflush(stderr);
		}
	}
	al->base[al->index] = element;
	al->index++;
}

/* Releases memory allocated to each elements and release the memory
 * allocated to the list itself as well. */
void free_array_list(struct array_list *al) {
	int x = 0;
	for (x=0; x < al->index; x++) {
		free(al->base[x]);
	}
	free(al->base);
}
