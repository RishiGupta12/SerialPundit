#!/bin/sh

# This file is part of progstm32.
# 
# Copyright (C) 2018, Rishi Gupta. All rights reserved.
# 
# The progstm32 is free software; you can redistribute it and/or modify it 
# under the terms of the GNU Lesser General Public License as published 
# by the Free Software Foundation; either version 2.1 of the License, or 
# (at your option) any later version.
# 
# The progstm32 is distributed in the hope that it will be useful, but 
# WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
# or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
# License for more details.
# 
# You should have received a copy of the GNU Lesser General Public License 
# along with this library; if not, write to the Free Software Foundation,Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

cd "$(dirname '$0')"

# Create too many null modem pairs
a=0
while [ $a -lt 2000 ]
do
	echo "gennm#xxxxx#xxxxx#7-8,x,x,x#4-1,6,x,x#7-8,x,x,x#4-1,6,x,x#y#y" > /proc/sp_vmpscrdk
	a=`expr $a + 1`
done

# Destroy all created devices
echo "del#xxxxx#xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" > /proc/sp_vmpscrdk

echo "Test done"

exit 0
