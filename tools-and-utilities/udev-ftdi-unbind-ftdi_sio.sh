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


# When a FTDI device is connected to system linux kernel with udev tries to load appropriate VCP 
# driver for connected device. It is possible to just unbind default driver for a particular
# device using udev rules (see tools-and-utilities/99-sp-extra-udev.rules for unbinding with the 
# help of script).

# Input argument to this script is devpath for the device (udev rule %p).

id="aa"

# extract the device ID
in=$1
IFS='/' list=($in)
for item in "${list[@]}"; 
do 
	id=$item;
done

# unbind the driver from USB interface
echo $id > "/sys$1/driver/unbind"

