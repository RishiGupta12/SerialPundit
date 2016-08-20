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

javadoc -d /home/r/ws-host-uart/sp-project/javadocs -sourcepath /home/r/ws-host-uart/sp-project/modules/core/src;/home/r/ws-host-uart/sp-project/modules/usb/src;/home/r/ws-host-uart/sp-project/modules/serial/src;/home/r/ws-host-uart/sp-project/modules/hid/src -package com.serialpundit.core com.serialpundit.usb com.serialpundit.serial com.serialpundit.hid

exit 0

# Set path to javadoc tool
JAVADOC_TOOL_PATH="$(dirname "$0")/../../../packages/jdk1.7.0_75/bin"

# Root of serialpundit project
PRJ_ROOT_DIR_PATH=$(dirname "$0")/..

# Docs will be saved in this folder
OUTPUT_DIR=$PRJ_ROOT_DIR_PATH/javadocs

# Source folder(s) of all java modules
JAVA_SRC_CORE=$PRJ_ROOT_DIR_PATH/modules/core/src
JAVA_SRC_HID=$PRJ_ROOT_DIR_PATH/modules/hid/src
JAVA_SRC_USB=$PRJ_ROOT_DIR_PATH/modules/usb/src
JAVA_SRC_SERIAL=$PRJ_ROOT_DIR_PATH/modules/serial/src
FINAL_JAVA_SRC_DIRS=$JAVA_SRC_CORE;$JAVA_SRC_HID;$JAVA_SRC_USB;$JAVA_SRC_SERIAL

echo $FINAL_JAVA_SRC_DIRS

# Exclude internal used classes
EXCLUDE_PACKAGES="com.serialpundit.hid.internal:com.serialpundit.usb.internal:com.serialpundit.serial.internal"

### Do not modify anything after this line ###
cd $OUTPUT_DIR

# Remove from working tree and delete physically also
git rm -r $OUTPUT_DIR/*
rm -r $OUTPUT_DIR/*

# Generate javadocs
$JAVADOC_TOOL_PATH/javadoc -d $OUTPUT_DIR -sourcepath $FINAL_JAVA_SRC_DIRS -package com.serialpundit.hid -exclude $EXCLUDE_PACKAGES

