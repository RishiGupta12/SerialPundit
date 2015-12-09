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

#include "stdafx.h"
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <tchar.h>
#include <windows.h>
#include <jni.h>
#include "windows_hid.h"

/* 
 * Allocate memory of given size and initializes elements as appropriate.
 * The elements in this array list will be java.lang.String object constructed
 * from an array of characters in modified UTF-8 encoding by calling JNI
 * NewStringUTF(..) function.
 */
int init_jstrarraylist(struct jstrarray_list *al, int initial_size) {
	al->base = (jstring *) calloc(initial_size, sizeof(jstring));
	if (al->base == NULL) {
		return -1;
	}
	al->index = 0;
	al->current_size = initial_size;
	return 0;
}

/* 
 * Insert given jstring object reference at next position expanding memory size
 * allocated if required. 
 */
int insert_jstrarraylist(struct jstrarray_list *al, jstring element) {
	if (al->index >= al->current_size) {
		al->current_size = al->current_size * 2;
		al->base = (jstring *) realloc(al->base, al->current_size * sizeof(jstring));
		if (al->base == NULL) {
			return -1;
		}
	}
	al->base[al->index] = element;
	al->index++;
	return 0;
}

/* 
 * Java garbage collector is responsible for releasing memory occupied by jstring objects.
 * We just free memory that we allocated explicitly. 
 */
void free_jstrarraylist(struct jstrarray_list *al) {
	free(al->base);
}

/*
 * Allocate memory of given size and initializes elements as appropriate.
 * The elements in this array list will be pointer to structures.
 */
int init_hiddev_instance_list(struct hiddev_instance_list *al, int initial_size) {
	al->base = calloc(initial_size, sizeof(struct hiddev_inst_cont_id *));
	if (al->base == NULL) {
		return -1;
	}
	al->index = 0;
	al->current_size = initial_size;
	return 0;
}

/*
 * Insert given pointer to structure at next position expanding memory size
 * allocated if required.
 */
int insert_hiddev_instance_list(struct hiddev_instance_list *al, struct hiddev_inst_cont_id *element) {
	if (al->index >= al->current_size) {
		al->current_size = al->current_size * 2;
		al->base = realloc(al->base, al->current_size * sizeof(struct hiddev_inst_cont_id *));
		if (al->base == NULL) {
			return -1;
		}
	}
	al->base[al->index] = element;
	al->index++;
	return 0;
}

/*
 * Free memory for all the elements and the list itself.
 */
void free_hiddev_instance_list(struct hiddev_instance_list *al) {
	int x = 0;
	for (x = 0; x < al->index; x++) {
		free(al->base[al->index]);
	}
	free(al->base);
}