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

# Run this script as root user.

cd "$(dirname "$0")"
rmmod tty2comKm &>/dev/null
set -e

if [[ $EUID -ne 0 ]]; then
   echo "This script must be run as root user !" 1>&2
   exit 1
fi

file="$(dirname "$0")/tty2comKm.ko"

if [ -f "$file" ]; then
    insmod ./tty2comKm.ko
    echo "Driver tty2comKm loaded successfully !"
    exit 0
fi

echo "File tty2comKm.ko not found in current directory !" 1>&2
exit 0

