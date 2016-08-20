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

# If xdotool is not installed, install it using below command.
# sudo apt-get install xdotool

# Run this script as ./minicom.sh RECEIVE_PORT RECEIVE_FILE SEND_PORT SEND_FILE
# For ex; ./minicom.sh /dev/ttyUSB0 /home/user/xyz/pic.txt /dev/ttyUSB1 /home/user/xyz/pic1.txt
# *_PORT and *_FILE must be absolute names (with path).

set -e

if [[ -z "$1" ]]; then
	echo "RECEIVE_PORT not specified. Exiting."
	exit 0
fi
if [[ -z "$2" ]]; then
	echo "RECEIVE_FILE not specified. Exiting."
	exit 0
fi
if [[ -z "$3" ]]; then
	echo "SEND_PORT not specified. Exiting."
	exit 0
fi
if [[ -z "$4" ]]; then
	echo "SEND_FILE not specified. Exiting."
	exit 0
fi

###### Setup and start file receiver JAVA APPLICATION

# compile java source files
cd "$(dirname "$0")"
javac -cp ./sp-tty.jar:sp-core.jar ./Java/com/xmodemftp/XmodemFTPFileReceiver.java

# create application jar
cd Java
jar -cfe ../app.jar com.xmodemftp.XmodemFTPFileReceiver com/xmodemftp/XmodemFTPFileReceiver.class

# launch receiver
cd ..
(sleep 13; java -cp .:sp-tty.jar:sp-core.jar:app.jar com.xmodemftp.XmodemFTPFileReceiver $1 $2; exit 0)&

###### Setup and start file sender SHELL SCRIPT(S)

# get directory where file to be sent is kept
DIR=$(dirname $4)

# simulate keyevents and typing file name
(sleep 4; xdotool key ctrl+a s; sleep 1; xdotool key Down Down; sleep 1; xdotool key Return; sleep 1; xdotool key Right; xdotool key Return; sleep 1; xdotool type $DIR; xdotool key Return; sleep 1; xdotool key Down; sleep 1; xdotool key space; sleep 1; xdotool key Return; sleep 20; xdotool key Return; sleep 2; xdotool key ctrl+a x; sleep 2; xdotool key Return; sleep 3; exit 0)&

# both send and receive command must be set
minicom -D $3 -b 9600

exit 0

