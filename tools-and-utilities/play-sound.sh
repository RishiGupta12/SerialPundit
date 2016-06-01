#!/bin/bash
#
# This file is part of SerialPundit project and software.
# 
# Copyright (C) 2014-2016, Rishi Gupta. All rights reserved.
#
# The SerialPundit software is DUAL licensed. It is made available under the terms of the GNU Affero 
# General Public License (AGPL) v3.0 for non-commercial use and under the terms of a commercial 
# license for commercial use of this software. 
#
# The SerialPundit software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
# without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
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

