It contains various tools, utilities, scripts, configuration files and resources that are required to create complete application for end user.

- __reset_usb_device__ : contain utility to reset usb device programatically and is also a quick reference to setup standard build environment (automake, autoconf, configure, make etc.) for your project.

- __exec-sp-from-script__ : contains script that can be used to execute applications from command line.

- __mac-os-x__ : contains resources specific to mac os x operating system.

- __99-xx-yy.rules__ : these are udev rules files to set correct permissions on device files.

- __cp210x-new-id.sh__ : script to tell to cp10x driver to handle the given device in linux.

- __cp210x-unload-vcp-driver.sh__ : script to unload cp210x driver in linux so that other driver can be used.

- __event.wav__ : sound file that can be used to play sound when serial device is inserted or removed from system.

- __generic-usbserial-linux.sh__ : script to specify driver for new device whose driver is not yet ready in linux.

- __mimescript.sh__ : script to demonstrate how to set default application for a particular file type in linux.

- __pack-sp-jar-in-app-jar.md__ : explains how application can pack sp-*.jar files in their jar file.

- __play-sound.sh__ : script to play given sound file to indicate an event to end user.

- __socat.sh__ : handy script to create virtual serial ports using socat command in linux.

- __symlink-usb-serial.sh__ : handy script for use in system integration when udev may not be available in linux.

- __udev-ftdi-latency-timer.sh__ : script to change latency timer of FTDI devices in linux.

- __udev-ftdi-unbind-ftdi_sio.sh__ : script to unbind default FTDI drivers automatically in linux.

-  __udev-ftdi-unload-vcp-driver.sh__ : script to unload default FTDI driver in linux.

- __wireshark-usb-sniffing.sh__ : handy script to use wireshark to sniff usb-uart communication in linux.


