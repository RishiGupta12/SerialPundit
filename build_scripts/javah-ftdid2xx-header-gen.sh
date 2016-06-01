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

# Generates JNI-C header file for FTDI D2XX SerialComFTDID2XXJNIBridge java class

#set system specific paths
PROJECT_ROOT_DIR_PATH="$(dirname "$0")/.."
JDK_INCLUDE_DIR="$(dirname "$0")/../../../packages/jdk1.7.0_75/include"
JNI_HEADER_FILE_PATH="$(dirname "$0")/../../../packages/jdk1.7.0_75/include/jni.h"

# Generate header file. 
set -e
javah -jni -d "$PROJECT_ROOT_DIR_PATH/../lib-vendor/ftdi/d2xx" -classpath $PROJECT_ROOT_DIR_PATH/com.embeddedunveiled.serial/src com.embeddedunveiled.serial.internal.SerialComFTDID2XXJNIBridge
echo "javah : FTDI D2XX SerialComFTDID2XXJNIBridge done !"
