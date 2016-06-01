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

package com.embeddedunveiled.serial.bluetooth;

import com.embeddedunveiled.serial.SerialComException;
import com.embeddedunveiled.serial.internal.SerialComBluetoothJNIBridge;

/**
 * <p>TODO</p>
 * 
 * @author Rishi Gupta
 */
public final class SerialComBluetooth {

    /**<p>The value indicating BlueZ bluetooth stack on Linux. Integer constant with value 0x01.</p>*/
    public static final int BTSTACK_LINUX_BLUEZ = 0x01;

    private SerialComBluetoothJNIBridge mSerialComBluetoothJNIBridge;

    /**
     * <p>Construct and allocates a new SerialComBluetooth object with given details.</p>
     * 
     * @param mSerialComBluetoothJNIBridge interface to native library.
     */
    public SerialComBluetooth(SerialComBluetoothJNIBridge mSerialComBluetoothJNIBridge) {
        this.mSerialComBluetoothJNIBridge = mSerialComBluetoothJNIBridge;
    }

    /**
     * <p>Returns an array containing information about all the Bluetooth adaptors present in the system 
     * found by this library. </p>
     * 
     * @return list of the local Bluetooth adaptor(s) with information about them or empty array if 
     *          no adaptor found.
     * @throws SerialComException if an I/O error occurs.
     */
    public SerialComBluetoothAdapter[] listBluetoothAdaptorsWithInfo() throws SerialComException {
        int i = 0;
        int numOfDevices = 0;
        SerialComBluetoothAdapter[] btDevicesFound = null;
        String[] btDevicesInfo = mSerialComBluetoothJNIBridge.listBluetoothAdaptorsWithInfo();

        if(btDevicesInfo != null) {
            numOfDevices = btDevicesInfo.length / 4;
            btDevicesFound = new SerialComBluetoothAdapter[numOfDevices];
            for(int x=0; x < numOfDevices; x++) {
                btDevicesFound[x] = new SerialComBluetoothAdapter(btDevicesInfo[i], btDevicesInfo[i+1], 
                        btDevicesInfo[i+2], btDevicesInfo[i+3]);
                i = i + 4;
            }
            return btDevicesFound;
        }else {
            return new SerialComBluetoothAdapter[] { };
        }	
    }
}
