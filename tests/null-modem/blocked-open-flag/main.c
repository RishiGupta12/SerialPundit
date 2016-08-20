/*
 * This file is part of SerialPundit.
 *
 * Copyright (C) 2014-2016, Rishi Gupta. All rights reserved.
 *
 * The SerialPundit is DUAL LICENSED. It is made available under the terms of the GNU Affero
 * General Public License (AGPL) v3.0 for non-commercial use and under the terms of a commercial
 * license for commercial use of this software.
 *
 * The SerialPundit is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

#include <fcntl.h>
#include <unistd.h>
#include <stdio.h>

/* if O_NDELAY is set it will not block, if it is not set it will block */

int main(int argc, char *argv[]) {

	int fd;

	fprintf(stderr, "1: opened handle : %d\n", 1);
	fflush(stderr);

	// non-blocking
	fd = open("/dev/tty2com0", O_RDWR | O_NOCTTY | O_NDELAY);

	// blocking
	//fd = open("/dev/tty2com0", O_RDWR | O_NOCTTY);

	fprintf(stderr, "2: opened handle : %d\n", fd);
	fflush(stderr);

	close(fd);

	fprintf(stderr, "3: closed handle : %d\n", fd);
	fflush(stderr);

	return 0;
}
