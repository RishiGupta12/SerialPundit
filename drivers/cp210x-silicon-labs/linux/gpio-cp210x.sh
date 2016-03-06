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

# Connect cp210x device and run this script providing VID/PID combination as : 
# ./gpio-cp210x.sh 10c4:ea60

set -e

if [ $# -eq 0 ] || [ -z "$1" ] ; then
 echo "No arguments supplied. Run as ./gpio-cp210x.sh 10c4:ea60"
 exit 0;
fi

# Find device path
devPath=`lsusb | grep -i $1 | sed -r 's/Bus ([0-9]{3}) Device ([0-9]{3}).*/bus\/usb\/\1\/\2/g;'`
echo "Found $1 @ $devPath"

# Find serial number
a='/dev/'
devicePath=$a$devPath
serialNumber=$(lsusb -D $devicePath | sed -n 's/.*iSerial  *. \(.*\)/\1/p')

# Find device node
nodesymlink=$(ls /dev/serial/by-id | grep $serialNumber)

fullnodesymlink='/dev/serial/by-id/'$nodesymlink
node=$(readlink -f $fullnodesymlink)

# Extract kernel node name
b=$(echo $node | sed 's/dev//g')
c=$(echo $b | sed 's/\///g')

# Get sysfs path 
# (/sys/class/tty/ttyUSB0 -> ../../devices/pci0000:00/0000:00:14.0/usb3/3-3/3-3:1.0/ttyUSB0/tty/ttyUSB0)
# 
# The variable GPIODIRPATH is the path we were searching.
node2=$(readlink -f "/sys/class/tty/$c")
cd $node2/../..
d=`pwd`
GPIODIRPATH=$d"/scm_cp210x_gpio"
echo $GPIODIRPATH


