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

