/*
 * Author : Rishi Gupta
 *
 * This file is part of 'serial communication manager' library.
 * Copyright (C) <2014-2016>  <Rishi Gupta>
 *
 * This 'serial communication manager' is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * The 'serial communication manager' is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with 'serial communication manager'.  If not, see <http://www.gnu.org/licenses/>.
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
