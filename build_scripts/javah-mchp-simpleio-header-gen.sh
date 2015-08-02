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

# Generates JNI-C header file for Microchip SimpleIO SerialComMCHPSIOJNIBridge java class

#set system specific absolute paths
PROJECT_ROOT_DIR_PATH="/home/r/ws-host-uart/serial-communication-manager"
JDK_INCLUDE_DIR="/home/r/packages/jdk1.7.0_75/include"
JNI_HEADER_FILE_PATH="/home/r/packages/jdk1.7.0_75/include/jni.h"

# Generating header file
set -e
javah -jni -d $PROJECT_ROOT_DIR_PATH/vendor-libs/microchip/simpleio -classpath $PROJECT_ROOT_DIR_PATH/com.embeddedunveiled.serial/src com.embeddedunveiled.serial.internal.SerialComMCHPSIOJNIBridge
echo "javah : Microchip SimpleIO SerialComMCHPSIOJNIBridge done !"
