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


# Change permissions to read/write for all. Setting low value may be beneficial for frequent I/O
# while setting high values may be beneficial for bulk transfer.
# For high speed I/O Low latency is required. For FTDI FT232RL this can be set via sysfs entry.
# Note default built-in drivers does not allow to change this timer value. Driver provided by FTDI
# at their website need to be used for changing FTDI specific parameters.

chmod 0666 "/sys$1/device/latency_timer"
