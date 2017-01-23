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

# Run this script as root user. This script has been tested with Ubuntu 12.04.

if [[ $EUID -ne 0 ]]; then
   echo "This script must be run as root user !" 1>&2
   exit 1
fi

KDIR=$(uname -r)

ufile="/etc/udev/rules.d/99-tty2com.rules"
if [[ -f "$ufile" ]]; then
    rm "$ufile"
    udevadm control --reload-rules
    udevadm trigger --attr-match=subsystem=tty
fi

dfile="/lib/modules/$KDIR/kernel/drivers/tty/tty2com.ko"
if [[ -f "$dfile" ]]; then
    rm "$dfile"
    depmod
fi

echo "uninstallation complete !"

