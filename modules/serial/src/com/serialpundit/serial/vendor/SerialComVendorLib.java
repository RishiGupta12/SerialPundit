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

package com.serialpundit.serial.vendor;

import java.io.File;
import java.io.FileNotFoundException;

import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComSystemProperty;
import com.serialpundit.core.SerialComException;

/**
 * <p>Super class for all classes which implements vendor specific API to talk to 
 * their devices using the libraries provided by vendor. These libraries from vendor 
 * may be propriety or not.</p>
 * 
 * @author Rishi Gupta
 */
public class SerialComVendorLib {

    /**<p>The value indicating proprietary D2XX software interface from Future Technology Devices 
     * International Ltd.</p>*/
    public static final int VLIB_FTDI_D2XX = 0x01;

    /**<p>The value indicating 'SimpleIO' library from Microchip Technology Inc.</p>*/
    public static final int VLIB_MCHP_SIMPLEIO = 0x02;

    /**<p>The value indicating 'CP210xRuntime' library from Silicon Laboratories, Inc.</p>*/
    public static final int VLIB_SLABS_CP210XRUNTIME = 0x03;

    /**<p>The value indicating 'CP210xManufacturing' library from Silicon Laboratories, Inc.</p>*/
    public static final int VLIB_SLABS_CP210XMANUFACTURING = 0x04;

    /**<p>The value indicating 'USBXpress' library from Silicon Laboratories, Inc.</p>*/
    public static final int VLIB_SLABS_USBXPRESS = 0x05;

    /**
     * <p>Allocates a new SerialComVendorLib object.</p>
     */
    public SerialComVendorLib() {
    }

    /**
     * <p>Gives an instance of the class which implements API defined by vendor in their propriety library.</p>
     * 
     * @param vendorLibIdentifier one of the constant VLIB_XXXX_XXXX in SerialComVendorLib class.
     * @param libDirectory directory where vendor library is placed.
     * @param vlibName full name of the vendor library (for ex. libftd2xx.so.1.1.12).
     * @param cpuArch architecture of CPU this library is running on.
     * @param osType operating system this library is running on.
     * @param serialComSystemProperty instance of SerialComSystemProperty to get required java properties.
     * @return object of class on which vendor specific API calls can be made otherwise null.
     * @throws SerialComException if java system properties can not be is null, if any file system related issue 
     *         occurs, if invalid vendorLibIdentifier is passed.
     * @throws SecurityException if java system properties can not be  accessed or required files can not be accessed.
     * @throws UnsatisfiedLinkError if loading/linking shared library fails.
     * @throws FileNotFoundException if the vendor library file is not found.
     */
    public SerialComVendorLib getVendorLibInstance(int vendorLibIdentifier, File libDirectory, String vlibName, 
            int cpuArch, int osType, SerialComSystemProperty serialComSystemProperty) throws SerialComException {

        SerialComVendorLib vendorLib = null;

        switch(vendorLibIdentifier) {

        case SerialComVendorLib.VLIB_FTDI_D2XX :

            if(!((cpuArch == SerialComPlatform.ARCH_AMD64) || (cpuArch == SerialComPlatform.ARCH_X86))) {
                throw new SerialComException("FTDI D2XX library is not supported for this CPU architecture !");
            }
            if(!((osType == SerialComPlatform.OS_WINDOWS) || (osType == SerialComPlatform.OS_LINUX) || (osType == SerialComPlatform.OS_MAC_OS_X))) {
                throw new SerialComException("FTDI D2XX library is not supported for this operating system !");
            }
            vendorLib = new SerialComFTDID2XX(libDirectory, vlibName, cpuArch, osType, serialComSystemProperty);
            break;

        case SerialComVendorLib.VLIB_MCHP_SIMPLEIO :

            if(!((cpuArch == SerialComPlatform.ARCH_AMD64) || (cpuArch == SerialComPlatform.ARCH_X86))) {
                throw new SerialComException("Microchip SimpleIO library is not supported for this CPU architecture !");
            }
            if(osType != SerialComPlatform.OS_WINDOWS) {
                throw new SerialComException("Microchip SimpleIO library is not supported for this operating system !");
            }
            vendorLib = new SerialComMCHPSimpleIO(libDirectory, vlibName, cpuArch, osType, serialComSystemProperty);
            break;

        case SerialComVendorLib.VLIB_SLABS_CP210XRUNTIME :

            if(!((cpuArch == SerialComPlatform.ARCH_AMD64) || (cpuArch == SerialComPlatform.ARCH_X86))) {
                throw new SerialComException("Silicon labs cp210x runtime dll library is not supported for this CPU architecture !");
            }
            if(osType != SerialComPlatform.OS_WINDOWS) {
                throw new SerialComException("Silicon labs cp210x runtime dll library is not supported for this operating system !");
            }
            vendorLib = new SerialComSLabsCP210xRuntime(libDirectory, vlibName, cpuArch, osType, serialComSystemProperty);
            break;

        case SerialComVendorLib.VLIB_SLABS_CP210XMANUFACTURING :

            if(!((cpuArch == SerialComPlatform.ARCH_AMD64) || (cpuArch == SerialComPlatform.ARCH_X86))) {
                throw new SerialComException("Silicon labs cp210x manufacturing dll library is not supported for this CPU architecture !");
            }
            if((osType != SerialComPlatform.OS_WINDOWS) && (osType != SerialComPlatform.OS_LINUX)) {
                throw new SerialComException("Silicon labs cp210x manufacturing dll library is not supported for this operating system !");
            }
            vendorLib = new SerialComSLabsCP210xManufacturing(libDirectory, vlibName, cpuArch, osType, serialComSystemProperty);
            break;

        case SerialComVendorLib.VLIB_SLABS_USBXPRESS :

            if(!((cpuArch == SerialComPlatform.ARCH_AMD64) || (cpuArch == SerialComPlatform.ARCH_X86))) {
                throw new SerialComException("Silicon labs usbxpress library is not supported for this CPU architecture !");
            }
            if(osType != SerialComPlatform.OS_WINDOWS) {
                throw new SerialComException("Silicon labs usbxpress library is not supported for this operating system !");
            }
            vendorLib = new SerialComSLabsCP210xManufacturing(libDirectory, vlibName, cpuArch, osType, serialComSystemProperty);
            break;

        default :
            throw new IllegalArgumentException("Given argument vendorLibIdentifier is invalid !");
        }

        return vendorLib;
    }
}
