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

# Script to execute with the help of udev rule. It plays given sound file whenever a USB-serial
# device is added or removed. Two different sounds can be played for connect and disconnect events. 
# When installing your software, copy event.wav audio file in /usr/share/scm directory.

# This script can also be executed from within Java code as shown below to indicate events to user.

# Asynchronous :
# ProcessBuilder pb = new ProcessBuilder("/usr/bin/play-sound.sh");
# Process p = pb.start();

# Synchronous :
# ProcessBuilder pb = new ProcessBuilder("/usr/bin/play-sound.sh");
# Process p = pb.start();
# p.waitFor();

aplay /usr/share/scm/event.wav

