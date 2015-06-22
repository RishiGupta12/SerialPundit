#!/bin/bash
# Generates JNI-C header file
PROJECT_ROOT_DIR_PATH="/home/r/ws-host-uart/serial-communication-manager"
JDK_INCLUDE_DIR="/home/r/packages/jdk1.7.0_75/include"
JNI_HEADER_FILE_PATH="/home/r/packages/jdk1.7.0_75/include/jni.h"

# Generating header file
javah -jni -d $PROJECT_ROOT_DIR_PATH/com.embeddedunveiled.native -classpath $PROJECT_ROOT_DIR_PATH/com.embeddedunveiled.serial/src com.embeddedunveiled.serial.SerialComJNINativeInterface
echo "javah done"
