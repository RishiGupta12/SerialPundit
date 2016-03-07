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
#################################################################################################

# If xdotool is not installed, install it using below command.
# sudo apt-get install xdotool

# File to send to Java application
FILE_TO_SEND=$1
COM_PORT=$2

# Simulate automated keypress after some time
(sleep 4; echo $FILE_TO_SEND)&
(sleep 2; xdotool key ctrl+a ctrl+s)&

# Both send and receive command must be set
picocom -b 9600 $COM_PORT --send-cmd "sx -avv" --receive-cmd "rx -vv"

