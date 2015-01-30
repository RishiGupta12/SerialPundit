#!/bin/bash
set -e

#set the variable and flags to be passed

JDK_INCLUDE_DIR="/home/r/packages/jdk/jdk1.6.0_45/include"
JNI_HEADER_FILE_PATH="/home/r/packages/jdk/jdk1.6.0_45/include/jni.h"
OUTPUT_FILE_NAME="linux_1.0.0_x86_64.so"

if [ 1==1 ]
then

#Building file: unix_like_serial.c
gcc -I$JDK_INCLUDE_DIR -include$JNI_HEADER_FILE_PATH -O0 -g3 -Wall -c -fmessage-length=0 -v -fPIC -m64 -o unix_like_serial.o unix_like_serial.c

#Building file: unix_like_serial_lib.c
gcc -I$JDK_INCLUDE_DIR -include$JNI_HEADER_FILE_PATH -O0 -g3 -Wall -c -fmessage-length=0 -v -fPIC -m64 -o unix_like_serial_lib.o unix_like_serial_lib.c

#Building target: linux_1.0.0_x86_64.so
gcc -shared -o $OUTPUT_FILE_NAME unix_like_serial.o unix_like_serial_lib.o
else

fi
