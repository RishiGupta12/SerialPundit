#include <stdio.h>
#include <fcntl.h>
#include <errno.h>
#include <sys/ioctl.h>
#include <linux/usbdevice_fs.h>

int main(int argc, char **argv) {
	const char *filename;
	int fd;
	filename = argv[1];
	fd = open(filename, O_WRONLY);
	errno = 0;
	if(fd < 0) {
		fprintf(stderr, "failed with error code : %d\n", errno);
		return 0;
	}
	ioctl(fd, USBDEVFS_RESET, 0);
	close(fd);
	return 0;
}
