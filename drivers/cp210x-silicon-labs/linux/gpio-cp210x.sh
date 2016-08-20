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
GPIODIRPATH=$d"/sp_cp210x_gpio"
echo $GPIODIRPATH

