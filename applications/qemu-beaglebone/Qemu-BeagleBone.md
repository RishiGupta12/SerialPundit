## Goals
- Emulate a beaglebone on host system using QEMU.
- Install Java SE Embedded JVM and find serial ports in system.

This note describes how to build linaro qemu on ubuntu 16.04 and emulate Beaglebone.

## Steps

Follow steps in the given order.

- Change into qemu-beaglebone directory (retain directory structure as downloaded from repository).
```sh
$ cd $YourPath/qemu-beaglebone
```
- Build FindPorts application after copying sp-tty.jar and sp-core.jar files in 'sp' directory.
```sh
javac -cp ./sp-tty.jar:sp-core.jar FindPorts.java
```
- Build linaro qemu, install it, verify installation and see supported machines. You man need to remove default qemu if required.
```sh
$ sudo apt-get remove qemu-system-arm
$ git clone git://git.linaro.org/qemu/qemu-linaro.git
$ cd qemu-linaro
$ sudo apt-get install libpixman-1-dev
$ sudo apt-get install libfdt-dev
$ sudo apt-get install zlib1g-dev
$ mkdir build
$ cd build
$ ../configure --prefix=$YourPath/qemu-beaglebone --disable-werror
$ make -j8
$ make install
$ export PATH=$PATH:$YourPath/qemu-beaglebone/bin
$ qemu-system-arm -machine help
```
- Install or update linaro-media-create.
```sh
$ cd $YourPath/qemu-beaglebone
$ sudo add-apt-repository ppa:linaro-maintainers/tools
$ sudo apt-get update
$ sudo apt-get install linaro-image-tools
```
- Download the nano image and omap3 hardware pack.
```sh
$ wget http://releases.linaro.org/platform/linaro-n/nano/11.09/nano-n-tar-20110929-0.tar.gz
$ wget http://releases.linaro.org/platform/linaro-n/nano/11.09/hwpack_linaro-omap3_20110929-1_armel_supported.tar.gz
```
- Create the VM disk image.
```sh
sudo linaro-media-create --image_file beagle_sd.img --dev beagle --binary nano-n-tar-20110929-0.tar.gz --hwpack hwpack_linaro-omap3_20110929-1_armel_supported.tar.gz
```
- Download JVM/JRE for embedded systems (ARMv5/ARMv6/ARMv7 Linux - SoftFP ABI, Little Endian 2) from link below.
```
http://www.oracle.com/technetwork/java/embedded/embedded-se/downloads/javase-embedded-downloads-2209751.html
```
- Unpack the downloaded JVM.

- Modify the root file system so that it contains embedded JVM. The output of fdisk is also shown, note number 106496.
```sh
$ fdisk -lu beagle_sd.img | grep beagle
Disk beagle_sd.img: 3221 MB, 3221225472 bytes
beagle_sd.img1   *          63      106494       53216    c  W95 FAT32 (LBA)
beagle_sd.img2          106496     6291455     3092480   83  Linux
```
- Make a directory to mount this root file system for modification.
```sh
$ cd $YourPath/qemu-beaglebone
$ mkdir sprootfs
```
- Mount the downloaded root file system.
```sh
$ sudo mount -o loop,offset=$[106496*512] beagle_sd.img sprootfs
```
- Copy the downloaded java binaries, jars, class and libudev.so files in respective folders. Change $YourPath as per location of files on your system.
```sh
$ sudo cp $YourPath/ejdk1.8.0_91 $YourPath/sprootfs/home -r
$ sudo cp $YourPath/sp-tty.jar $YourPath/sprootfs/home
$ sudo cp $YourPath/sp-core.jar $YourPath/sprootfs/home
$ sudo cp $YourPath/FindPorts.class $YourPath/sprootfs/home
$ sudo cp $YourPath/libudev.so $YourPath/sprootfs/lib
```
- Unmount root file system.
```sh
$ cd $YourPath/qemu-beaglebone
$ sudo umount sprootfs
```
- Run emulator to emulate beaglebone. When booted terminal command prompt will be provided.
```sh
$ qemu-system-arm -M beagle -m 256 -sd ./beagle_sd.img -clock unix -serial stdio -device usb-kbd -device usb-mouse -usb -device usb-net,netdev=mynet -netdev user,id=mynet
```
- Once booted we will get system shell. Set JVM path on emulated system and check if java is executing.
```sh
# export PATH=$PATH:/home/ejdk1.8.0_91-sf/linux_arm_sflt/jre/bin
# java -version
```
- Execute out test application finally.
```sh
# java -classpath .:sp-tty.jar:sp-core.jar FindPorts
```

To get out of emualtion and back to host shell terminal run 'poweroff' on qemu shell and then press CTRL+C.

