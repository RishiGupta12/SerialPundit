#!/bin/bash
#
# This file is part of SerialPundit.
# 
# Copyright (C) 2014-2016, Rishi Gupta. All rights reserved.
#
# The SerialPundit is DUAL LICENSED. It is made available under the terms of the GNU Affero 
# General Public License (AGPL) v3.0 for non-commercial use and under the terms of a commercial 
# license for commercial use of this software. 
#
# The SerialPundit is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
# without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
#################################################################################################

# Run this script as root user.

# When a CP210X device is connected to system linux kernel with udev tries to load appropriate VCP driver
# for connected device. If we do not want default driver, this script will unload default driver and load
# custom driver provided to this script.

# Note that once the driver has been unloaded then if the device is un-plugged and plugged again into system
# default driver will get loaded again automatically by udev/kernel. So this script must be run each time
# USB-UART device is plugged into system.

# echo 8 > /proc/sys/kernel/printk

set -e

if [[ $EUID -ne 0 ]]; then
   echo "This script must be run as root user !" 1>&2
   exit 1
fi

cd "$(dirname "$0")"

modprobe -r cp210x
rmmod sp_cp210x 2>/dev/null

file="$(dirname "$0")/sp_cp210x.ko"

if [ -f "$file" ]; then
	modprobe usbserial
	insmod ./sp_cp210x.ko
	echo "default cp210x unloaded and custom driver loaded !"
	exit 0
fi

echo "File sp_cp210x.ko not found !" 1>&2
exit 1

