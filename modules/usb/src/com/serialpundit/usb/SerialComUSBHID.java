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

package com.serialpundit.usb;

import com.serialpundit.core.SerialComException;
import com.serialpundit.usb.internal.SerialComUSBJNIBridge;

/**
 * <p>Provides methods to communicate with USB HID devices.</p>
 * 
 * <p>A USB HID device should have standard device descriptor, standard configuration descriptor, standard 
 * interface descriptor for the HID class, class specific HID descriptor, standard endpoint descriptor for 
 * Interrupt IN endpoint and class specific report descriptor.</p>
 * 
 * @author Rishi Gupta
 */
public final class SerialComUSBHID {

    private final SerialComUSBJNIBridge mUSBJNIBridge;

    /**
     * <p>Construct and allocates a new SerialComUSBHID object with given details.</p>
     * 
     * @param mUSBJNIBridge interface to native library for calling platform specific routines.
     */
    public SerialComUSBHID(SerialComUSBJNIBridge mUSBJNIBridge) {
        this.mUSBJNIBridge = mUSBJNIBridge;
    }

    /**
     * <p>Returns an array of SerialComHIDdevice objects containing information about USB-HID devices 
     * as found by this library. Application can call various  methods on returned SerialComHIDdevice 
     * object to get specific information like vendor id and product id etc.</p>
     * 
     * <p>The information about HID device returned includes, transport, vendor ID, product ID, serial 
     * number, product, manufacturer, location etc. In situations where two or more devices with exactly 
     * same vendor ID, product ID and serial number are present into system, information like location 
     * can be used to further categories them into unique devices. Application can also use some custom 
     * protocol to identify devices that are of interest to them.</p>
     * 
     * <p>[1] Some bluetooth HID keyboard and mouse use a USB dongle which make them appear as USB HID 
     * device to system. The keyboard/mouse communicate with dongle over bluetooth frequency while 
     * dongle communicate with computer as USB HID device. This is also the reason why sometimes 
     * bluetooth keyboard/mouse works even when there is no bluetooth stack installed in system.</p>
     * 
     * @param vendorFilter vendor whose devices should be listed (one of the constants SerialComUSB.V_xxxxx 
     *        or any valid USB-IF VID).
     * @return list of the HID devices with information about them or empty array if no device matching 
     *         given criteria found.
     * @throws SerialComException if an I/O error occurs.
     * @throws IllegalArgumentException if vendorFilter is negative or invalid number.
     */
    public SerialComUSBHIDdevice[] listUSBHIDdevicesWithInfo(int vendorFilter) throws SerialComException {
        int i = 0;
        int numOfDevices = 0;
        SerialComUSBHIDdevice[] usbHidDevicesFound = null;

        if((vendorFilter < 0) || (vendorFilter > 0XFFFF)) {
            throw new IllegalArgumentException("Argument vendorFilter can not be negative or greater than 0xFFFF !");
        }

        String[] usbhidDevicesInfo = mUSBJNIBridge.listUSBHIDdevicesWithInfo(vendorFilter);

        if(usbhidDevicesInfo != null) {
            if(usbhidDevicesInfo.length <= 3) {
                // if no devices found return empty array.
                return new SerialComUSBHIDdevice[] { };
            }

            // number of elements sent by native layer will be multiple of 7
            // if device(s) is found to populate SerialComHIDdevice object.
            numOfDevices = usbhidDevicesInfo.length / 8;
            usbHidDevicesFound = new SerialComUSBHIDdevice[numOfDevices];
            for(int x=0; x < numOfDevices; x++) {
                usbHidDevicesFound[x] = new SerialComUSBHIDdevice(usbhidDevicesInfo[i], usbhidDevicesInfo[i+1], usbhidDevicesInfo[i+2], 
                        usbhidDevicesInfo[i+3], usbhidDevicesInfo[i+4], usbhidDevicesInfo[i+5], usbhidDevicesInfo[i+6],
                        usbhidDevicesInfo[i+7]);
                i = i + 8;
            }
            return usbHidDevicesFound;
        }else {
            throw new SerialComException("Could not find USB HID devices. Please retry !");
        }	
    }
}
