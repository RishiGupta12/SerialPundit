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

# When a FTDI device is connected to system, mac os x try to load appropriate VCP driver
# for connected device. For Mac os x default VCP driver and d2xx are mutually exclusive. So this 
# script tries to unload default VCP driver for FTDI devices.

# Note that once the driver has been unloaded then if the device is un-plugged and plugged again into 
# system default driver will get loaded again automatically by kernel. So this script must be run each 
# time USB-UART device is plugged into system.

# This has a side effect also that now other programs can not use default FTDI VCP driver, this is due to
# how the FTDI has designed their drivers.

# Run below shell command to know ftdi vcp driver module is unloaded or not.
# kextstat | grep FTDI

set -e
kextunload –b com.apple.driver.AppleUSBFTDI
echo "AppleUSBFTDI VCP driver unloaded !"

# Another way to unload driver is to specify full path
# kextunload /System/Library/Extensions/IOUSBFamily.kext/Contents/Plugins/AppleUSBFTDI.kext

