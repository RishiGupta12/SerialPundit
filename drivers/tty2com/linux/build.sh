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

cd "$(dirname "$0")"

set -e

make clean

# To enable sparse tool static analysis run this script as ./build.sh C=1
make $1

rm -rf *.o *~ core .depend .*.cmd *.mod.c .tmp_versions modules.order Module.symvers

echo "Build done."

