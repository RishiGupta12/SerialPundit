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




















