#!/bin/bash
#
# Author : Rishi Gupta
# 
# This file is part of 'serial communication manager' library.
#
# The 'serial communication manager' is free software: you can redistribute it and/or modify
# it under the terms of the GNU Lesser General Public License as published by the Free Software 
# Foundation, either version 3 of the License, or (at your option) any later version.
#
# The 'serial communication manager' is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
# PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public License
# along with serial communication manager. If not, see <http://www.gnu.org/licenses/>.
#################################################################################################


# When a FTDI device is connected to system linux kernel with udev tries to load appropriate VCP driver
# for connected device. It is possible to just unbind default driver for a particular
# device using udev rules (see tests/99-scm-extra-udev.rules for unbinding with the help of script).

id="aa"

# extract the device ID
in=$1
IFS='/' list=($in)
for item in "${list[@]}"; 
do 
	id=$item;
done

# unbind the driver from CDC interface
echo $id > "/sys$1/driver/unbind"
