##Goals
- Emulate a beaglebone on host system using QEMU.
- Install Java SE Embedded JVM and find serial ports.

This note describes how to test this library for embedded platforms without having real embedded board.

##Steps

Follow steps in the given order.

- Install Qemu and verify installation
```sh
$ sudo apt-get install qemu-system
$ qemu-system-arm -version
```
- Create a working directory for our private use and change into that directory.
```sh
$ mkdir scm
$ cd scm
```
- Install or update linaro-media-create.
```sh
$ sudo add-apt-repository ppa:linaro-maintainers/tools
$ sudo apt-get update
$ sudo apt-get install linaro-image-tools qemu-user-static qemu-system
```
- Download the nano image and omap3 hardware pack.
```sh
$ wget http://releases.linaro.org/platform/linaro-n/nano/11.09/nano-n-tar-20110929-0.tar.gz
$ wget http://releases.linaro.org/platform/linaro-n/nano/11.09/hwpack_linaro-omap3_20110929-1_armel_supported.tar.gz
```
- Generate the image for qemu.
```sh
sudo linaro-media-create --image_file beagle_sd.img --dev beagle --binary nano-n-tar-20110929-0.tar.gz --hwpack hwpack_linaro-omap3_20110929-1_armel_supported.tar.gz
```
- Download JVM for embedded systems (ARMv5/ARMv6/ARMv7 Linux - SoftFP ABI, Little Endian 2) from link below.
```
http://www.oracle.com/technetwork/java/embedded/embedded-se/downloads/javase-embedded-downloads-2209751.html
```
- Unpack the downloaded java pack.
- Modify the root file system so that it contains embedded JVM. The output of fdisk is also shown, note number 106496.
```sh
$ fdisk -lu beagle_sd.img | grep beagle
Disk beagle_sd.img: 3221 MB, 3221225472 bytes
beagle_sd.img1   *          63      106494       53216    c  W95 FAT32 (LBA)
beagle_sd.img2          106496     6291455     3092480   83  Linux
```
- Make a directory to mount this root file system for modification.
```sh
mkdir scmrootfs
```
- Mount the downloaded root file system.
```sh
$ sudo mount -o loop,offset=$[106496*512] beagle_sd.img scmrootfs
```
- Copy the downloaded java binaries in home folder. Change $YourPath as per location of files on your system.
```sh
$ sudo cp $YourPath/ejdk1.8.0_91 $YourPath/scmrootfs/home -r
```
- Unmount root file system.
```sh
$ sudo umount scmrootfs
```
- Run emulator to emulate beaglebone. When booted terminal command prompt will be provided.
```sh
$ qemu-system-arm -M beagle -m 256 -sd ./beagle_sd.img -clock unix -serial stdio -device usb-kbd -device usb-mouse -usb -device usb-net,netdev=mynet -netdev user,id=mynet
```

