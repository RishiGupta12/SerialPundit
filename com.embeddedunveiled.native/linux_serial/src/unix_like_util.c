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
#include "unix_like_serial_lib.h"

void init_array_list(struct array_list *al, int initial_size) {
  al->base = (char **) calloc(initial_size, sizeof(char *));
  al->index = 0;
  al->current_size = initial_size;
}

void insert_array_list(struct array_list *al, char *element) {
  if(al->index > al->current_size) {
    al->current_size = al->current_size * 2;
    al->base = (char **) realloc(al->base, al->current_size * sizeof(char *));
  }
  al->base[al->index] = element;
  al->index++;
}

void free_array_list(struct array_list *al) {
	int x = 0;
	for (x=0; x < al->index; x++) {
		free(al->base[x]);
	}
	free(al->base);
}
