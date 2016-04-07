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

# Run this script as root user.

# When a FTDI device is connected to system, linux kernel with udev tries to load appropriate VCP driver
# for connected device. For Linux default VCP driver and d2xx are mutually exclusive. So this script
# tries to unload default VCP driver for FTDI devices.

# Note that once the driver has been unloaded then if the device is un-plugged and plugged again into system
# default driver will get loaded again automatically by udev/kernel. So this script must be run each time
# USB-UART device is plugged into system.

# This has a side effect also that now other programs can not use default FTDI VCP driver, this is due to
# how the FTDI has designed their drivers. It is possible to just unbind default driver for a particular
# device using udev rules (see tools-and-utilities/99-scm-extra-udev.rules for unbinding with the help of script).

# It seems like FTDI D2XX uses user space USB drivers and depends upon libusb for its working. If any error
# is encountered like permission denied place the following udev rules in in /etc/udev/rules.d/ for Ubuntu 
# Linux distribution.
# <github repository>/tools-and-utilities/99-scm-ftdi-d2xx.rules

# Run below shell command to know ftdi vcp driver module is loaded or not.
# lsmod | grep ftdi

set -e
modprobe -r ftdi_sio
echo "ftdi_sio vcp driver unloaded !"

