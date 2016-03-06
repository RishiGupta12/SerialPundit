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

# This script makes it possible to change the latency timer's value from user space application.
# It change the permissions of latency timer file (sysfs entry) to read/write for all. Setting low 
# timer value may be beneficial for frequent I/O while setting high values may be beneficial for bulk 
# transfer.

# The default drivers in Linux kernel may not allow to change (ignore) timer value or the sysfs file
# itself may not exist. So it is required that either we need to write our own driver for this purpose
# or use drivers provided by FTDI at their website.

# An example sysfs file for latency timer is :
# /sys/devices/pci0000:00/0000:00:14.0/usb3/3-3/3-3:1.0/ttyUSB0/tty/ttyUSB0/device/latency_timer

# To see what are the environment variables set by udev redirect 'env' value and open scmudevenv.txt 
# file in text editor to see list of variables and their values.
# env >> /tmp/scmudevenv.txt

# Input argument ($1) to this script is devpath for the device (udev rule %p).

chmod 0666 "/sys$1/device/latency_timer"

