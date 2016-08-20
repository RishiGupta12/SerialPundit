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

echo "Running the MIME Script..."
APP=SPLOG
INSTALL_DIR="/usr/SPLOG"

mkdir -p ~/.local/share/mime/packages
#cp .application-x-SPLOG.xml ~/.local/share/mime/packages/application-x-SPLOG.xml
cd ~/.local/share/mime/packages
touch application-x-SPLOG.xml

# Create mime xml 
echo "<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<mime-info xmlns=\"http://www.freedesktop.org/standards/shared-mime-info\">
    <mime-type type=\"application/x-SPLOG\">
        <comment>SPLOG File</comment>
        <icon name=\"application-x-SPLOG\"/>
        <glob pattern=\"*.splog\"/>
    </mime-type>
</mime-info>" > ~/.local/share/mime/packages/application-x-SPLOG.xml

# Create application desktop
mkdir -p ~/.local/share/applications
#cp .SPLOG.desktop ~/.local/share/applications/SPLOG.desktop
echo "[Desktop Entry]
Type=Application
Exec=$INSTALL_DIR/SPLOG
Path=$INSTALL_DIR
MimeType=application/x-SPLOG
Name=SPLOG
GenericName=SPLOG
Icon=$INSTALL_DIR/splog.png
Terminal=false
Categories=SPLOG;
"> ~/.local/share/applications/SPLOG.desktop

# copy images to pixmaps
mkdir -p ~/.local/share/pixmaps
cp $INSTALL_DIR/splog.png ~/.local/share/pixmaps/splog.png
cp $INSTALL_DIR/application-x-SPLOG.png ~/.local/share/pixmaps/application-x-SPLOG.png

#update the desktop and mime database
update-desktop-database ~/.local/share/applications
update-mime-database    ~/.local/share/mime

#now register the icon so all the .ISF files will be shown with SPLOG icon
xdg-icon-resource install --context mimetypes --size 48 $INSTALL_DIR/splog.png application-x-SPLOG

echo "Default application for *.splog files installed successfully !"

