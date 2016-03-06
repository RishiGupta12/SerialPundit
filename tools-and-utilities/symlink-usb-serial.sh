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
#################################################################################################

# Connect usb-uart device and run this script providing VID/PID combination as shown below. For 
# example for FT232RL run it as:
# ./symlink-usb-serial.sh 0403:6001

# It is a handy script for use in system integration when udev may not be available for example in 
# embedded system environment or we may be not willing to use udev rules.

# There are many ways in which this script can be modifed and integrated with udev rules. An example
# of udev rule is given below based on bus number and device number.

# ACTION=="add",    SUBSYSTEM=="usb", ENV{DEVTYPE}=="usb_device", ATTRS{idVendor}=="10c4", \
# RUN+="/bin/bash /usr/bin/scm-udev-notify.sh -a add    -p '%p' -b '$attr{busnum}' -d '$attr{devnum}'"

# ACTION=="remove", SUBSYSTEM=="usb", ENV{DEVTYPE}=="usb_device", ATTRS{idVendor}=="10c4", \
# RUN+="/bin/bash /usr/bin/scm-udev-notify.sh -a remove -p '%p' -b '$attr{busnum}' -d '$attr{devnum}'"

set -e

# Find device path
devPath=`lsusb | grep $1 | sed -r 's/Bus ([0-9]{3}) Device ([0-9]{3}).*/bus\/usb\/\1\/\2/g;'`
echo "Found $1 @ $devPath"

# Find serial number
a='/dev/'
devicePath=$a$devPath
serialNumber=$(lsusb -D $devicePath | sed -n 's/.*iSerial  *. \(.*\)/\1/p')
echo $serialNumber

# Find device node
nodesymlink=$(ls /dev/serial/by-id | grep $serialNumber)
fullnodesymlink='/dev/serial/by-id/'$nodesymlink
node=$(readlink -f $fullnodesymlink)
echo $node

# Create device node for non-root user in user's home directory
ln -sf $node $HOME'/usbtty-'${serialNumber}
echo 'symlink created'' '$HOME'/usbtty-'${serialNumber}' --> '$node


