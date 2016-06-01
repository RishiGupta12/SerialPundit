/*
 * This file is part of SerialPundit project and software.
 * 
 * Copyright (C) 2014-2016, Rishi Gupta. All rights reserved.
 *
 * The SerialPundit software is DUAL licensed. It is made available under the terms of the GNU Affero 
 * General Public License (AGPL) v3.0 for non-commercial use and under the terms of a commercial 
 * license for commercial use of this software. 
 * 
 * The SerialPundit software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.embeddedunveiled.serial.hid;

import com.embeddedunveiled.serial.internal.SerialComHIDJNIBridge;

/**
 * <p>Super class for all HID transport mediums.</p>
 * 
 * @author Rishi Gupta
 */
public class SerialComHIDTransport {

    // sub-classes also uses this reference to invoke native functions.
    protected SerialComHIDJNIBridge mHIDJNIBridge;
    protected int osType;

    /**
     * <p>Allocates a new SerialComHID object.</p>
     * 
     * @param mHIDJNIBridge interface class to native library for calling platform specific routines.
     */
    public SerialComHIDTransport(SerialComHIDJNIBridge mHIDJNIBridge, int osType) {
        this.mHIDJNIBridge = mHIDJNIBridge;
        this.osType = osType;
    }
}
