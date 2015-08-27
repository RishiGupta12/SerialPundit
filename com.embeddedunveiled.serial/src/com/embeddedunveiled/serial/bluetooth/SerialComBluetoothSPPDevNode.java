/*
 * Author : Rishi Gupta
 * 
 * This file is part of 'serial communication manager' library.
 *
 * The 'serial communication manager' is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by the Free Software 
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * The 'serial communication manager' is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with serial communication manager. If not, see <http://www.gnu.org/licenses/>.
 */

package com.embeddedunveiled.serial.bluetooth;

/**
 * <p>Represents a device node created for serial port emulation over bluetooth channel 
 * using rfcomm service.</p>
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
		System.out.println("\nDevice node : " + deviceNode + "\nAddress : " + address + "\nChannel : " + channel);
	}
}
