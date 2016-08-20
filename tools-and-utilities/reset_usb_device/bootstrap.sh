#!/bin/sh
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

cd "$(dirname "$0")"

command -v libtoolize >/dev/null 2>&1
if  [ $? -ne 0 ]; then
    command -v libtool >/dev/null 2>&1
    if  [ $? -ne 0 ]; then
        echo "could not find libtool." 1>&2
        exit 1
    fi
fi

command -v autoreconf >/dev/null 2>&1
if [ $? -ne 0 ]; then
    echo "autoconf and automake are required." 1>&2
    exit 1
fi

command -v pkg-config >/dev/null 2>&1
if [ $? -ne 0 ]; then
    echo "pkg-config is required." 1>&2
    exit 1
fi

# The autoreconf runs autoconf, autoheader, aclocal, automake, libtoolize, and autopoint (when appropriate) 
# repeatedly to update the GNU Build System in the specified directories and their subdirectories.
autoreconf -vis
status=$?
if [ $status -ne 0 ]; then
    echo "autoreconf exited with status $status" 1>&2
    exit 1
fi

echo "Bootstrap script completed successfully."
exit 0

