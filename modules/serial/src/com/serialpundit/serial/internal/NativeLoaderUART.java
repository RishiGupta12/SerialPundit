/*
 * This file is part of SerialPundit.
 * 
 * Copyright (C) 2014-2016, Rishi Gupta. All rights reserved.
 *
 * The SerialPundit is DUAL LICENSED. It is made available under the terms of the GNU Affero 
 * General Public License (AGPL) v3.0 for non-commercial use and under the terms of a commercial 
 * license for commercial use of this software. 
 * 
 * The SerialPundit is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.serialpundit.serial.internal;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * <p>Helper class which gets loaded by top-most class loader.</p>
 * 
 * @author Rishi Gupta
 */
public class NativeLoaderUART {

    // This class must define this method.
    public void load(final String nativeLibToLoad) {
        try {
            AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
                public Boolean run() {
                    System.load(nativeLibToLoad);
                    return true;
                }
            });
        } catch (Exception e) {
            throw (UnsatisfiedLinkError) new UnsatisfiedLinkError("Could not load " + nativeLibToLoad).initCause(e);
        }
    }
}
