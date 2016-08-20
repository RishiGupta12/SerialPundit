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

# Build the kernel driver for cp210x devices.

set -e

#touch -c ./sp_cp210x.c

cd "$(dirname "$0")"

make clean

make

rm -rf *.o *~ core .depend .*.cmd *.mod.c .tmp_versions modules.order Module.symvers 2>/dev/null

