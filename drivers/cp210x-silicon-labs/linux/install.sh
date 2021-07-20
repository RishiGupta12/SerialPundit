#!/bin/bash
#
# This file is part of SerialPundit.
# 
# Copyright (C) 2014-2021, Rishi Gupta. All rights reserved.
#
# The SerialPundit is DUAL LICENSED. It is made available under the terms of the GNU Affero 
# General Public License (AGPL) v3.0 for non-commercial use and under the terms of a commercial 
# license for commercial use of this software. 
#
# The SerialPundit is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
# without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
#################################################################################################

# Run this script as root user. This script has been tested with Ubuntu 12.04.

set -e

if [[ $EUID -ne 0 ]]; then
   echo "This script must be run as root user !" 1>&2
   exit 0
fi

cd "$(dirname "$0")"

KDIR=$(uname -r)

cp ./sp_cp210x.ko /lib/modules/$KDIR/kernel/drivers/usb/serial

echo "wait resolving dependencies !"
depmod
echo "done"

