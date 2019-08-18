#!/bin/bash
#
# This file is part of SerialPundit.
# 
# Copyright (C) 2014-2018, Rishi Gupta. All rights reserved.
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

# checkpatch.pl from branch v5.3-rc3
# run as: ./static-analysis-checkpatch.sh ~/linux5.0/linux/scripts/checkpatch.pl

if [ -z "$1" ]; then
	echo "checkpatch.pl path not passed"
	exit 1
else
	$1 -f --no-tree ./tty2com.c
fi

echo "Checkpatch checking done."
