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

# Linux provides generic usb-serial interface with limited features that can be used to communicate
# with usb-serial device when full-fledged dedicated driver has not been developed for it. The Linux
# kernel should have been compiled with this module.

# Run this script as root user. Replace VID and PID with USB-IF VID and PID of your device. For example
# if VID is 0x0403 then VID=0403 should be used. The XXXX and YYYY are in hexadecimal format.

VID=XXXX
PID=YYYY

set -e

echo $VID $PID > /sys/bus/usb-serial/drivers/generic/new_id


# There may be one more method if usb-serial is to be loaded as loadable module. Pass this module
# VID and PID of your device to use generic usb-serial interface to drive your device.

# modprobe usbserial vendor=0xXXXX product=0xYYYY

# Additionally to enable dynamic debugging messages from Linux drivers, update the log level.
# $ echo 8 > /proc/sys/kernel/printk

