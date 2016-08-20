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
APP=SCMLOG
INSTALL_DIR="/usr/SCMLOG"

mkdir -p ~/.local/share/mime/packages
#cp .application-x-SCMLOG.xml ~/.local/share/mime/packages/application-x-SCMLOG.xml
cd ~/.local/share/mime/packages
touch application-x-SCMLOG.xml

# Create mime xml 
echo "<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<mime-info xmlns=\"http://www.freedesktop.org/standards/shared-mime-info\">
    <mime-type type=\"application/x-SCMLOG\">
        <comment>SCMLOG File</comment>
        <icon name=\"application-x-SCMLOG\"/>
        <glob pattern=\"*.scmlog\"/>
    </mime-type>
</mime-info>" > ~/.local/share/mime/packages/application-x-SCMLOG.xml

# Create application desktop
mkdir -p ~/.local/share/applications
#cp .SCMLOG.desktop ~/.local/share/applications/SCMLOG.desktop
echo "[Desktop Entry]
Type=Application
Exec=$INSTALL_DIR/SCMLOG
Path=$INSTALL_DIR
MimeType=application/x-SCMLOG
Name=SCMLOG
GenericName=SCMLOG
Icon=$INSTALL_DIR/scmlog.png
Terminal=false
Categories=SCMLOG;
"> ~/.local/share/applications/SCMLOG.desktop

# copy images to pixmaps
mkdir -p ~/.local/share/pixmaps
cp $INSTALL_DIR/scmlog.png ~/.local/share/pixmaps/scmlog.png
cp $INSTALL_DIR/application-x-SCMLOG.png ~/.local/share/pixmaps/application-x-SCMLOG.png

#update the desktop and mime database
update-desktop-database ~/.local/share/applications
update-mime-database    ~/.local/share/mime

#now register the icon so all the .ISF files will be shown with SCMLOG icon
xdg-icon-resource install --context mimetypes --size 48 $INSTALL_DIR/scmlog.png application-x-SCMLOG

echo "Default application for *.scmlog files installed successfully !"

