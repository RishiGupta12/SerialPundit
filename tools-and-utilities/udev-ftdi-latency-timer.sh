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

# This script makes it possible to change the latency timer's value from user space application.
# It change the permissions of latency timer file (sysfs entry) to read/write for all. Setting low 
# timer value may be beneficial for frequent I/O while setting high values may be beneficial for bulk 
# transfer.

# The default drivers in Linux kernel may not allow to change (ignore) timer value or the sysfs file
# itself may not exist. So it is required that either we need to write our own driver for this purpose
# or use drivers provided by FTDI at their website.

# An example sysfs file for latency timer is :
# /sys/devices/pci0000:00/0000:00:14.0/usb3/3-3/3-3:1.0/ttyUSB0/tty/ttyUSB0/device/latency_timer

# To see what are the environment variables set by udev redirect 'env' value and open spudevenv.txt 
# file in text editor to see list of variables and their values.
# env >> /tmp/spudevenv.txt

# Input argument ($1) to this script is devpath for the device (udev rule %p).

chmod 0666 "/sys$1/device/latency_timer"

