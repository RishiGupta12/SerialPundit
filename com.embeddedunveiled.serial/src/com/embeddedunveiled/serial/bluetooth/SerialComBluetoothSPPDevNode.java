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

/**
 * <p>Represents a device node created for serial port emulation over bluetooth channel 
 * using rfcomm service.</p>
 * 
 * @author Rishi Gupta
 */
public final class SerialComBluetoothSPPDevNode {

    private String deviceNode;
    private String address;
    private String channel;

    /**
     * <p>Construct and allocates a new SerialComBluetoothAdapter object with given details.</p>
     * 
     * @param address bluetooth mac address of remote device.
     * @param channel bluetooth channel number in use for serial port profile connection.
     */
    public SerialComBluetoothSPPDevNode(String deviceNode, String address, String channel) {
        this.deviceNode = deviceNode;
        this.address = address;
        this.channel = channel;
    }

    /**
     * <p>Returns the device node (including full path for unix-like operating systems) 
     * representing serial port connection over bluetooth channel with remote device.</p>
     *
     * @return device node to be used for serial communication.
     */
    public String getDeviceNode() {
        return deviceNode;
    }

    /**
     * <p>Returns the hardware address of the remote Bluetooth device 
     * for example "00:11:22:AA:BB:CC".</p>
     *
     * @return Bluetooth hardware address of remote device as hex string.
     */
    public String getAddress() {
        return address;
    }

    /**
     * <p>Returns the channel number in use for serial port profile connection.</p>
     *
     * @return the channel number in use for serial port profile connection.
     */
    public int getChannelNumber() {
        return Integer.parseInt(channel, 10);
    }

    /** 
     * <p>Prints information about this device node on console.</p>
     */
    public void dumpDeviceInfo() {
        System.out.println(
                "\nDevice node : " + deviceNode + 
                "\nAddress : " + address + 
                "\nChannel : " + channel);
    }
}
