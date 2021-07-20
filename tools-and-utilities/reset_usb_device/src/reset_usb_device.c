/************************************************************************************************
 * This file is part of SerialPundit.
 * 
 * Copyright (C) 2014-2021, Rishi Gupta. All rights reserved.
 *
 * The SerialPundit is DUAL LICENSED. It is made available under the terms of the GNU Affero 
 * General Public License (AGPL) v3.0 for non-commercial use and under the terms of a commercial 
 * license for commercial use of this software. 
 * 
 * The SerialPundit is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ************************************************************************************************/

#ifdef HAVE_CONFIG_H
#include <config.h>
#endif

#include <stdio.h>
#include <unistd.h>
#include <fcntl.h>
#include <errno.h>
#include <sys/ioctl.h>
#include <linux/usbdevice_fs.h>

int main(int argc, char **argv) {
    
    int ret, fd;

    if(argv[1] == NULL) {
        fprintf(stderr, "failed with error code : %d\n", EINVAL);
        return -1;
    }
    
    errno = 0;
    fd = open(argv[1], O_WRONLY);
    if(fd < 0) {
        fprintf(stderr, "open failed with error code : %d\n", errno);
		close(fd);
        return -1;
    }
    
    errno = 0;
    ret = ioctl(fd, USBDEVFS_RESET, 0);
    if(ret < 0) {
		close(fd);
        fprintf(stderr, "ioctl failed with error code : %d\n", errno);
        return -1;
    }
    
    close(fd);
    return 0;
}

