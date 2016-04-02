#!/bin/bash
#
# Author : Rishi Gupta
#
# This file is part of 'serial communication manager' library.
# Copyright (C) <2014-2016>  <Rishi Gupta>
#
# This 'serial communication manager' is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as published by the Free Software 
# Foundation, either version 3 of the License, or (at your option) any later version.
#
# The 'serial communication manager' is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR 
# A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with 'serial communication manager'.  If not, see <http://www.gnu.org/licenses/>.
################################################################################################

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
rmmod scm_cp210x 2>/dev/null

file="$(dirname "$0")/scm_cp210x.ko"

if [ -f "$file" ]; then
	modprobe usbserial
	insmod ./scm_cp210x.ko
	echo "default cp210x unloaded and custom driver loaded !"
	exit 0
fi

echo "File scm_cp210x.ko not found !" 1>&2
exit 1

