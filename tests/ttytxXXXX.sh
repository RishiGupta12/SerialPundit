#!/bin/bash
#
# Author : Rishi Gupta
# 
# This file is part of 'serial communication manager' library.
#
# The 'serial communication manager' is free software: you can redistribute it and/or modify
# it under the terms of the GNU Lesser General Public License as published by the Free Software 
# Foundation, either version 3 of the License, or (at your option) any later version.
#
# The 'serial communication manager' is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
# PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public License
# along with serial communication manager. If not, see <http://www.gnu.org/licenses/>.
#################################################################################################

# Run as root user. It creates device nodes to emulate perle port server nodes.

set -e
mknod /dev/ttytx0003 c 63 2
mknod /dev/ttytx0004 c 63 3
mknod /dev/ttytx0005 c 63 5
mknod /dev/ttytx0006 c 63 6
mknod /dev/ttytx0007 c 63 7
mknod /dev/ttytx0089 c 63 4
mknod /dev/ttytx0763 c 63 9
