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
################################################################################################

# Run this script as root user. This script has been tested with Ubuntu 12.04.

if [[ $EUID -ne 0 ]]; then
   echo "This script must be run as root user !" 1>&2
   exit 1
fi

KDIR=$(uname -r)

ufile="/etc/udev/rules.d/99-tty2comKm.rules"
if [[ -f "$ufile" ]]; then
    rm "$ufile"
    udevadm control --reload-rules
    udevadm trigger --attr-match=subsystem=tty
fi

dfile="/lib/modules/$KDIR/kernel/drivers/tty/tty2comKm.ko"
if [[ -f "$dfile" ]]; then
    rm "$dfile"
    depmod
fi

echo "uninstallation complete !"

