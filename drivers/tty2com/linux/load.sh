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

cd "$(dirname "$0")"

rmmod tty2com &>/dev/null

set -e

if [[ $EUID -ne 0 ]]; then
   echo "This script must be run as root user !" 1>&2
   exit 1
fi

file="$(dirname "$0")/tty2com.ko"

#insmod ./tty2com.ko max_num_vtty_dev=20 init_num_nm_pair=1 init_num_lb_dev=1
if [ -f "$file" ]; then
    insmod ./tty2com.ko
    echo "Driver tty2com loaded successfully !"
    exit 0
fi

echo "File tty2com.ko not found in current directory !" 1>&2
exit 0

