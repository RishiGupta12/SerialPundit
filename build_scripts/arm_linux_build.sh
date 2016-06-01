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
 
# Steps are :
# - generate header file
# - build C source
# - copy native libraries into libs folder
# - build java source files and place class files in bin folder
# - export artifact scm.jar in scripts_output folder

# This scripts assumes that you have installed toolchain i.e. 
# $ sudo apt-get install gcc-4.6-multilib-arm-linux-gnueabi
# $ sudo apt-get install gcc-arm-linux-gnueabihf

# Modify PATH as per your system
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

PROJECT_ROOT_DIR_PATH="/home/r/ws-host-uart/serial-com-manager"
JDK_INCLUDE_DIR="/home/r/packages/jdk1.7.0_75/include"
JNI_HEADER_FILE_PATH="/home/r/packages/jdk1.7.0_75/include/jni.h"

# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
# Do not modify anything after this line
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

set -e

echo "~~~~~~~~~ Build starts.....wait for message Build completed. ~~~~~~~~~~~~~~~~~~~~"
echo "  "

LIB_VERSION="1.0.0"
a="windows_"
b="_x86_64.dll"
c="_x86.dll"
d="mac_"
e="_x86_64.dylib"
f="_x86.dylib"
g="linux_"
h="_armel.so"
i="_armhf.so"
j="scm"
k="jar"

# <~~~~ Generating header file ~~~>
javah -jni -d $PROJECT_ROOT_DIR_PATH/com.embeddedunveiled.native -classpath $PROJECT_ROOT_DIR_PATH/com.embeddedunveiled.serial/src com.embeddedunveiled.serial.SerialComJNINativeInterface

# <~~~~~~~~~~~~~~~ Build for armel ~~~~~~~~~~~~~~~>
# Building file: unix_like_serial.c
arm-linux-gnueabi-gcc-4.6 -I$JDK_INCLUDE_DIR -include$JNI_HEADER_FILE_PATH -O0 -g3 -Wall -c -fmessage-length=0 -fPIC -pthread -o $PROJECT_ROOT_DIR_PATH/scripts_output/arm_linux_serial_el.o $PROJECT_ROOT_DIR_PATH/com.embeddedunveiled.native/linux_serial/src/unix_like_serial.c

# Building file: unix_like_serial_lib.c
arm-linux-gnueabi-gcc-4.6 -I$JDK_INCLUDE_DIR -include$JNI_HEADER_FILE_PATH -O0 -g3 -Wall -c -fmessage-length=0 -fPIC -pthread -o $PROJECT_ROOT_DIR_PATH/scripts_output/arm_linux_serial_lib_el.o $PROJECT_ROOT_DIR_PATH/com.embeddedunveiled.native/linux_serial/src/unix_like_serial_lib.c

# Building target: linux_X.X.X_x86_64.so
arm-linux-gnueabi-gcc-4.6 -shared -o $PROJECT_ROOT_DIR_PATH/scripts_output/$g$LIB_VERSION$h $PROJECT_ROOT_DIR_PATH/scripts_output/arm_linux_serial_el.o $PROJECT_ROOT_DIR_PATH/scripts_output/arm_linux_serial_lib_el.o -lpthread -ludev

# Clean up
if [ -f $PROJECT_ROOT_DIR_PATH/scripts_output/arm_linux_serial_lib_el.o  ]; then
rm $PROJECT_ROOT_DIR_PATH/scripts_output/arm_linux_serial_lib_el.o
fi

if [ -f $PROJECT_ROOT_DIR_PATH/scripts_output/arm_linux_serial_el.o  ]; then
rm $PROJECT_ROOT_DIR_PATH/scripts_output/arm_linux_serial_el.o
fi

# <~~~~~~~~~~~~~~~ Build for armhf ~~~~~~~~~~~~~~~>
# Building file: unix_like_serial.c
arm-linux-gnueabihf-gcc-4.6 -I$JDK_INCLUDE_DIR -include$JNI_HEADER_FILE_PATH -O0 -g3 -Wall -c -fmessage-length=0 -fPIC -lpthread -o $PROJECT_ROOT_DIR_PATH/scripts_output/arm_linux_serial_hf.o $PROJECT_ROOT_DIR_PATH/com.embeddedunveiled.native/linux_serial/src/unix_like_serial.c

# Building file: unix_like_serial_lib.c
arm-linux-gnueabihf-gcc-4.6 -I$JDK_INCLUDE_DIR -include$JNI_HEADER_FILE_PATH -O0 -g3 -Wall -c -fmessage-length=0 -fPIC -lpthread -o $PROJECT_ROOT_DIR_PATH/scripts_output/arm_linux_serial_lib_hf.o $PROJECT_ROOT_DIR_PATH/com.embeddedunveiled.native/linux_serial/src/unix_like_serial_lib.c

# Building target: linux_X.X.X_x86_64.so
arm-linux-gnueabihf-gcc-4.6 -shared -o $PROJECT_ROOT_DIR_PATH/scripts_output/$g$LIB_VERSION$i $PROJECT_ROOT_DIR_PATH/scripts_output/arm_linux_serial_hf.o $PROJECT_ROOT_DIR_PATH/scripts_output/arm_linux_serial_lib_el.o -lpthread -ludev

# Clean up
if [ -f $PROJECT_ROOT_DIR_PATH/scripts_output/unix_like_serial_lib_32.o  ]; then
rm $PROJECT_ROOT_DIR_PATH/scripts_output/unix_like_serial_lib_32.o
fi

if [ -f $PROJECT_ROOT_DIR_PATH/scripts_output/unix_like_serial_32.o  ]; then
rm $PROJECT_ROOT_DIR_PATH/scripts_output/unix_like_serial_32.o
fi

# <~~~~~ Copy all shared libraries in libs folder that will be packaged in jar ~~~~>
if [ -f $PROJECT_ROOT_DIR_PATH/scripts_output/$g$LIB_VERSION$h  ]; then
cp $PROJECT_ROOT_DIR_PATH/scripts_output/$g$LIB_VERSION$h $PROJECT_ROOT_DIR_PATH/com.embeddedunveiled.serial/libs
fi

if [ -f $PROJECT_ROOT_DIR_PATH/scripts_output/$g$LIB_VERSION$i ]; then
cp $PROJECT_ROOT_DIR_PATH/scripts_output/$g$LIB_VERSION$i $PROJECT_ROOT_DIR_PATH/com.embeddedunveiled.serial/libs
fi

if [ -f $PROJECT_ROOT_DIR_PATH/com.embeddedunveiled.native/windows_serial/x64/Debug/$a$LIB_VERSION$b  ]; then
cp $PROJECT_ROOT_DIR_PATH/com.embeddedunveiled.native/windows_serial/x64/Debug/$a$LIB_VERSION$b $PROJECT_ROOT_DIR_PATH/com.embeddedunveiled.serial/libs
fi

if [ -f $PROJECT_ROOT_DIR_PATH/com.embeddedunveiled.native/windows_serial/Debug/$a$LIB_VERSION$c ]; then
cp $PROJECT_ROOT_DIR_PATH/com.embeddedunveiled.native/windows_serial/Debug/$a$LIB_VERSION$c $PROJECT_ROOT_DIR_PATH/com.embeddedunveiled.serial/libs
fi

if [ -f $PROJECT_ROOT_DIR_PATH/com.embeddedunveiled.native/linux_serial/Debug/$d$LIB_VERSION$e  ]; then
cp $PROJECT_ROOT_DIR_PATH/com.embeddedunveiled.native/linux_serial/Debug/$d$LIB_VERSION$e $PROJECT_ROOT_DIR_PATH/com.embeddedunveiled.serial/libs
fi

if [ -f $PROJECT_ROOT_DIR_PATH/com.embeddedunveiled.native/linux_serial/Debug/$d$LIB_VERSION$f ]; then
cp $PROJECT_ROOT_DIR_PATH/com.embeddedunveiled.native/linux_serial/Debug/$d$LIB_VERSION$f $PROJECT_ROOT_DIR_PATH/com.embeddedunveiled.serial/libs
fi

# <~~~~~ Build java source files and place class files in bin folder ~~~~>
javac -d $PROJECT_ROOT_DIR_PATH/com.embeddedunveiled.serial/bin $PROJECT_ROOT_DIR_PATH/com.embeddedunveiled.serial/src/com/embeddedunveiled/serial/*.java

# <~~~~~ Export artifact scm.jar ~~~~>
jar cf $PROJECT_ROOT_DIR_PATH/scripts_output/$j$LIB_VERSION$k -C $PROJECT_ROOT_DIR_PATH/com.embeddedunveiled.serial/bin com -C $PROJECT_ROOT_DIR_PATH/com.embeddedunveiled.serial libs

echo "  "
echo "~~~~~~~~~~ Build completed. Jar file is in scripts_output folder. ~~~~~~~~~~"




















