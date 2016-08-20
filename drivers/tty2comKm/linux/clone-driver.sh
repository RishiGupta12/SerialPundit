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

# Using the sparse checkout feature of git, this script will clone tty2comKm driver.

cd "$(dirname "$0")"
mkdir drv
cd drv
git init
git remote add -f origin https://github.com/RishiGupta12/serial-communication-manager.git
git config core.sparseCheckout true
echo "drivers/tty2comKm" >> .git/info/sparse-checkout
git pull origin master

