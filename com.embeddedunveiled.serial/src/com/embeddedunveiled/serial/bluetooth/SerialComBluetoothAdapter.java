/*
 * Author : Rishi Gupta
 * 
 * This file is part of 'serial communication manager' library.
 * Copyright (C) <2014-2016>  <Rishi Gupta>
 *
 * This 'serial communication manager' is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by the Free Software 
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * The 'serial communication manager' is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR 
 * A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with 'serial communication manager'.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.embeddedunveiled.serial.bluetooth;

/**
 * <p>Represents the local device Bluetooth adapter and information about it.</p>
 * 
 * @author Rishi Gupta
 */
public final class SerialComBluetoothAdapter {

	private String name;
	private String address;
	private String type;
	private String bus;

	/**
	 * <p>Construct and allocates a new SerialComBluetoothAdapter object with given details.</p>
	 * 
	 * @param name friendly name of this adaptor.
	 * @param address bluetooth address.
	 */
	public SerialComBluetoothAdapter(String name, String address, String type, String bus) {
		this.name = name;
		this.address = address;
		this.type = type;
		this.bus = bus;
	}

	/**
	 * <p>Get the friendly Bluetooth name of the local Bluetooth adapter. 
	 * This name is visible to remote Bluetooth devices.</p>
	 *
	 * @return the Bluetooth name or null on error.
	 */
	public String getName() {
		return name;
	}

	/**
	 * <p>Returns the hardware address of the local Bluetooth adapter 
	 * for example "00:11:22:AA:BB:CC".</p>
	 *
	 * @return Bluetooth hardware address as string.
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * <p>Returns the type of this device.</p>
	 *
	 * @return the type or null on error.
	 */
	public String getType() {
		return name;
	}

	/**
	 * <p>Gives information about which bus this device is connected to.</p>
	 *
	 * @return bus type.
	 */
	public String getBus() {
		return address;
	}

	/** 
	 * <p>Prints information about this Bluetooth adaptor on console.</p>
	 */
	public void dumpDeviceInfo() {
		System.out.println(
				"Name : " + name + 
				"\nAddress : " + address + 
				"\nType : " + type + 
				"\nBus : " + bus);
	}
}
