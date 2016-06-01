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

# Modify JDK_INCLUDE_DIR and JNI_HEADER_FILE_PATH as per your system JDK installation
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

JDK_INCLUDE_DIR="$(dirname "$0")/../../../packages/jdk1.7.0_75/include"
JNI_HEADER_FILE_PATH="$(dirname "$0")/../../../packages/jdk1.7.0_75/include/jni.h"

# Do not modify anything after this line
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

set -e
PROJECT_ROOT_DIR_PATH="$(dirname "$0")/.."

# Build java source files and place class files in bin folder.
# The javac command compiles Java source code into Java bytecodes.
javac -target 1.6 -source 1.6 -d $PROJECT_ROOT_DIR_PATH/com.embeddedunveiled.serial/bin -sourcepath $PROJECT_ROOT_DIR_PATH/com.embeddedunveiled.serial/src/com/embeddedunveiled/serial/*.java
echo "javac done"

# Create artifact scm-1.0.4.jar
jar cf "$PROJECT_ROOT_DIR_PATH/scm-1.0.4.jar" -C $PROJECT_ROOT_DIR_PATH/com.embeddedunveiled.serial/bin com -C $PROJECT_ROOT_DIR_PATH/com.embeddedunveiled.serial libs
echo "jar done"

echo "  "
echo "~~~~~~~~~~ Build completed. Jar file is in project root folder. ~~~~~~~~~~"




















