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

# Run this script as root user. Replace VID and PID with USB-IF VID and PID of your device. For example
# if VID is 0x0403 then VID=0403 should be used. The XXXX and YYYY are in hexadecimal format.

# Assume we changed the VID/PID of CP210X device. Now the Linux kernel driver has to be compiled with support
# for these VID/PID combinations added. But on a running kernel this might not be possible in addition to 
# other scenarios. We can dynamically make existing driver serve this device by associating VID/PID with
# the driver with the help of new_id sysfs file.

# Also this is not limited to cp210x devices but can be used with any usb-serial driver.

VID=XXXX
PID=YYYY

set -e

modprobe usbserial
modprobe cp210x

echo $VID $PID > /sys/bus/usb-serial/drivers/cp210x/new_id

echo "Done !"

