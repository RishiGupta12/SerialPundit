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


# This script installs application binaries in /usr/share/tty2com directory, driver in 
# /lib/modules/$KDIR/kernel/drivers/tty directory and application launcher in /usr/bin directory.

# This script does clean install every time it is executed. It installs tty2com driver as standard 
# Linux install. To load/unload driver use modprobe command. To laucn application run tty2com from 
# shell.

# Change into working directory
cd "$(dirname "$0")"

# Variable initilization
KDIR=$(uname -r)
DRIVER_BINARY=$(pwd)"/tty2com.ko"
INSTALLATION_DIRECTORY="/usr/share/tty2com"

echo "Installation started..."

function cleanup_installation {
    rm -f /lib/modules/$KDIR/kernel/drivers/tty/tty2com.ko &>/dev/null
    rm -f /etc/udev/rules.d/99-tty2com.rules &>/dev/null
    rm -f /usr/share/tty2com/userguide.pdf &>/dev/null
    rm -f /usr/bin/tty2com &>/dev/null
    rm -f /usr/share/tty2com/sp-tty2com-app.jar &>/dev/null
    rm -f /usr/share/tty2com/sp-tty.jar &>/dev/null
    rm -f /usr/share/tty2com/sp-core.jar &>/dev/null
}

# Check for root privileges
if [ $EUID -ne 0 ]; then
	echo "Please run this script as root user. Exiting installer !!"
	exit -1
fi

echo -n "Downloading resources..."

# Download resources to be installed
wget -r -q https://github.com/RishiGupta12/SerialPundit/tree/master/prebuilt-release/sp-core.jar
if [ $? != '0' ]; then
    echo -e "Can not download sp-core.jar. Aborting installation !!"
    exit -1
fi

echo -n "..."

wget -r -q https://github.com/RishiGupta12/SerialPundit/tree/master/prebuilt-release/sp-tty.jar
if [ $? != '0' ]; then
    echo "Can not download sp-tty.jar. Aborting installation !!"
    exit -1
fi

echo -n "..."

wget -r -q https://github.com/RishiGupta12/SerialPundit/tree/master/drivers/tty2com/sp-tty2com-app.jar
if [ $? != '0' ]; then
    echo -e "Can not download sp-tty2comapp.jar. Aborting installation !!"
    exit -1
fi

wget -r -q -O tty2com.sh https://github.com/RishiGupta12/SerialPundit/blob/master/drivers/tty2com/linux/tty2com?raw=true
if [ $? != '0' ]; then
    echo "Can not download tty2com file. Aborting installation !!"
    exit -1
fi

echo -n "..."

wget -r -q -O tty2com.c https://github.com/RishiGupta12/SerialPundit/blob/master/drivers/tty2com/linux/tty2com.c?raw=true
if [ $? != '0' ]; then
    echo "Can not download tty2com.c. Aborting installation !!"
    exit -1
fi

echo -n "..."

wget -r -q -O Makefile https://github.com/RishiGupta12/SerialPundit/blob/master/drivers/tty2com/linux/Makefile?raw=true
if [ $? != '0' ]; then
    echo "Can not download Makefile. Aborting installation !!"
    exit -1
fi

echo -n "..."

wget -r -q -O 99-tty2com.rules https://github.com/RishiGupta12/SerialPundit/blob/master/drivers/tty2com/linux/99-tty2com.rules?raw=true
if [ $? != '0' ]; then
    echo "Can not download 99-tty2com.rules. Aborting installation !!"
    exit -1
fi

echo -n "..."

wget -r -q https://github.com/RishiGupta12/SerialPundit/tree/master/drivers/tty2com/userguide.pdf
if [ $? != '0' ]; then
    echo "Can not download userguide.pdf. Aborting installation !!"
    exit -1
fi

wget -r -q https://github.com/RishiGupta12/SerialPundit/tree/master/drivers/tty2com/resources/icon.png
if [ $? != '0' ]; then
    echo "Can not download icon.png. Aborting installation !!"
    exit -1
fi

wget -r -q https://github.com/RishiGupta12/SerialPundit/tree/master/drivers/tty2com/resources/splash.png
if [ $? != '0' ]; then
    echo "Can not download splash.png. Aborting installation !!"
    exit -1
fi

wget -r -q https://github.com/RishiGupta12/SerialPundit/tree/master/drivers/tty2com/resources/tty2com.desktop
if [ $? != '0' ]; then
    echo "Can not download tty2com.desktop. Aborting installation !!"
    exit -1
fi

echo "Building driver..."

# Build the null modem driver
make clean
make
rm -rf *.o *~ core .depend .*.cmd *.mod.c .tmp_versions modules.order Module.symvers

if [ ! -f $DRIVER_BINARY ]; then
   echo "Can not build driver. Aborting installation !!"
   exit -1
fi

echo "Installing driver and associated files..."

# Install driver and 
cp -f $DRIVER_BINARY /lib/modules/$KDIR/kernel/drivers/tty
if [ $? != '0' ]; then
    echo "Can not copy driver $DRIVER_BINARY to /lib/modules/$KDIR/kernel/drivers/tty. Aborting installation !!"
    exit -1
fi

# Install udev rules for driver
cp -f ./99-tty2com.rules /etc/udev/rules.d
if [ $? != '0' ]; then
    echo "Can not copy 99-tty2com.rules to /etc/udev/rules.d. Aborting installation !!"
    cleanup_installation
    exit -1
fi

chmod 0444 /etc/udev/rules.d/99-tty2com.rules

# Update udev rules notification
if [ -f /sbin/udevadm ]
then
    udevadm control --reload-rules >/dev/null 2>&1
    udevadm trigger --attr-match=subsystem=tty >/dev/null 2>&1
else
    /sbin/udevcontrol reload_rules >/dev/null 2>&1
    /sbin/udevtrigger --subsystem-match=block >/dev/null 2>&1
fi

# Inform modprobe about newly installed module
echo "Resolving dependencies..."
depmod

# Install jar application files
echo "Installing application..."

rm -rf $INSTALLATION_DIRECTORY &>/dev/null

mkdir $INSTALLATION_DIRECTORY
if [ $? != '0' ]; then
    echo "Can not create installation $INSTALLATION_DIRECTORY directory. Aborting installation !!"
    cleanup_installation
    exit -1
fi

cp -f ./sp-tty2com-app.jar $INSTALLATION_DIRECTORY
if [ $? != '0' ]; then
    echo "Can not copy sp-tty2com-app.jar in installation directory. Aborting installation !!"
    cleanup_installation
    exit -1
fi

spa="/sp-tty2com-app.jar"
chmod 0555 $INSTALLATION_DIRECTORY$spa

cp -f ./sp-tty.jar $INSTALLATION_DIRECTORY
if [ $? != '0' ]; then
    echo "Can not copy sp-tty.jar in installation directory. Aborting installation !!"
    cleanup_installation
    exit -1
fi

spt="/sp-tty.jar"
chmod 0555 $INSTALLATION_DIRECTORY$spt

cp -f ./sp-core.jar $INSTALLATION_DIRECTORY
if [ $? != '0' ]; then
    echo "Can not copy sp-core.jar in installation directory. Aborting installation !!"
    cleanup_installation
    exit -1
fi

spc="/sp-core.jar"
chmod 0555 $INSTALLATION_DIRECTORY$spc

cp -f ./tty2com /usr/bin
if [ $? != '0' ]; then
    echo "Can not copy tty2com in installation directory. Aborting installation !!"
    cleanup_installation
    exit -1
fi

chmod 0555 /usr/bin/tty2com

echo "Installing documentation..."

cp -f ./userguide.pdf $INSTALLATION_DIRECTORY
if [ $? != '0' ]; then
    echo "Can not copy userguide.pdf in installation directory. Aborting installation !!"
    cleanup_installation
    exit -1
fi

ug="/userguide.pdf"
chmod 0444 $INSTALLATION_DIRECTORY$ug

# Notify success
echo "Done installing. Thanks."
exit 0

