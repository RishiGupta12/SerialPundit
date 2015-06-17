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
#

# Steps are :
# - generate header file
# - build C source
# - copy native libraries into libs folder
# - build java source files and place class files in bin folder
# - export artifact scm.jar in scripts_output folder

# Modify PATH as per your system
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

#PROJECT_ROOT_DIR_PATH="/home/r/ws-host-uart/serial-communication-manager"
PROJECT_ROOT_DIR_PATH="/home/pedro/git/serial-communication-manager"
#JDK_INCLUDE_DIR="/home/r/packages/jdk1.7.0_75/include"
JDK_INCLUDE_DIR="/usr/include/x86_64-linux-gnu"
#JNI_HEADER_FILE_PATH="/home/r/packages/jdk1.7.0_75/include/jni.h"
JNI_HEADER_FILE_PATH="/usr/include/x86_64-linux-gnu/jni.h"

# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
# Do not modify anything after this line
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

set -e

echo "~~~~~~~~~ Build starts.....wait for message Build completed. ~~~~~~~~~~~~~~~~~~~~"
echo "  "

LIB_VERSION="1.0.3"
a="windows_"
b="_x86_64.dll"
c="_x86.dll"
d="mac_"
e="_x86_64.dylib"
f="_x86.dylib"
g="linux_"
h="_x86_64.so"
i="_x86.so"
j="scm"
k="jar"

# Build java source files and place class files in bin folder
javac -d $PROJECT_ROOT_DIR_PATH/com.embeddedunveiled.serial/bin $PROJECT_ROOT_DIR_PATH/com.embeddedunveiled.serial/src/com/embeddedunveiled/serial/*.java
echo "javac done"

# Generating header file
javah -jni -d $PROJECT_ROOT_DIR_PATH/com.embeddedunveiled.native -classpath $PROJECT_ROOT_DIR_PATH/com.embeddedunveiled.serial/src com.embeddedunveiled.serial.SerialComJNINativeInterface
echo "javah done"

# Building file: unix_like_serial.c
gcc -I$JDK_INCLUDE_DIR -include$JNI_HEADER_FILE_PATH -O0 -g3 -Wall -c -fmessage-length=0 -fPIC -m64 -pthread -o $PROJECT_ROOT_DIR_PATH/scripts_output/unix_like_serial_64.o $PROJECT_ROOT_DIR_PATH/com.embeddedunveiled.native/linux_serial/src/unix_like_serial.c
echo "x64 Building file: unix_like_serial.c"
# Building file: unix_like_serial_lib.c
gcc -I$JDK_INCLUDE_DIR -include$JNI_HEADER_FILE_PATH -O0 -g3 -Wall -c -fmessage-length=0 -fPIC -m64 -pthread -o $PROJECT_ROOT_DIR_PATH/scripts_output/unix_like_serial_lib_64.o $PROJECT_ROOT_DIR_PATH/com.embeddedunveiled.native/linux_serial/src/unix_like_serial_lib.c
echo "x64 Building file: unix_like_serial_lib.c"
# Building target: linux_X.X.X_x86_64.so
gcc -shared -m64 -o $PROJECT_ROOT_DIR_PATH/scripts_output/$g$LIB_VERSION$h $PROJECT_ROOT_DIR_PATH/scripts_output/unix_like_serial_64.o $PROJECT_ROOT_DIR_PATH/scripts_output/unix_like_serial_lib_64.o -lpthread -ludev
echo "x64 Building target: linux_X.X.X_x86_64.so"
# Clean up
if [ -f $PROJECT_ROOT_DIR_PATH/scripts_output/unix_like_serial_lib_64.o  ]; then
rm $PROJECT_ROOT_DIR_PATH/scripts_output/unix_like_serial_lib_64.o
fi

if [ -f $PROJECT_ROOT_DIR_PATH/scripts_output/unix_like_serial_64.o  ]; then
rm $PROJECT_ROOT_DIR_PATH/scripts_output/unix_like_serial_64.o
fi

# Create symlink for build x86 architecture
rm /usr/lib/libudev.so
ln -s /lib/i386-linux-gnu/libudev.so.0 /usr/lib/libudev.so

# Building file: unix_like_serial.c
gcc -I$JDK_INCLUDE_DIR -include$JNI_HEADER_FILE_PATH -O0 -g3 -Wall -c -fmessage-length=0 -fPIC -m32 -lpthread -o $PROJECT_ROOT_DIR_PATH/scripts_output/unix_like_serial_32.o $PROJECT_ROOT_DIR_PATH/com.embeddedunveiled.native/linux_serial/src/unix_like_serial.c
echo "x86 Building file: unix_like_serial.c"
# Building file: unix_like_serial_lib.c
gcc -I$JDK_INCLUDE_DIR -include$JNI_HEADER_FILE_PATH -O0 -g3 -Wall -c -fmessage-length=0 -fPIC -m32 -lpthread -o $PROJECT_ROOT_DIR_PATH/scripts_output/unix_like_serial_lib_32.o $PROJECT_ROOT_DIR_PATH/com.embeddedunveiled.native/linux_serial/src/unix_like_serial_lib.c
echo "x86 Building file: unix_like_serial_lib.c"
# Building target: linux_X.X.X_x86.so
gcc -shared -m32 -o $PROJECT_ROOT_DIR_PATH/scripts_output/$g$LIB_VERSION$i $PROJECT_ROOT_DIR_PATH/scripts_output/unix_like_serial_32.o $PROJECT_ROOT_DIR_PATH/scripts_output/unix_like_serial_lib_32.o -lpthread -ludev
echo "x86 Building target: linux_X.X.X_x86.so"
# Clean up
if [ -f $PROJECT_ROOT_DIR_PATH/scripts_output/unix_like_serial_lib_32.o  ]; then
rm $PROJECT_ROOT_DIR_PATH/scripts_output/unix_like_serial_lib_32.o
fi

if [ -f $PROJECT_ROOT_DIR_PATH/scripts_output/unix_like_serial_32.o  ]; then
rm $PROJECT_ROOT_DIR_PATH/scripts_output/unix_like_serial_32.o
fi

# Copy all shared libraries in libs folder that will be packaged in jar
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

echo "gcc done"

#Undo symlink
rm /usr/lib/libudev.so
ln -s /usr/lib/libudev.so.1 /usr/lib/libudev.so

# Export artifact scm.jar
jar cf $PROJECT_ROOT_DIR_PATH/scripts_output/$j$LIB_VERSION$k -C $PROJECT_ROOT_DIR_PATH/com.embeddedunveiled.serial/bin com -C $PROJECT_ROOT_DIR_PATH/com.embeddedunveiled.serial libs
echo "jar done"

echo "  "
echo "~~~~~~~~~~ Build completed. Jar file is in scripts_output folder. ~~~~~~~~~~"




















