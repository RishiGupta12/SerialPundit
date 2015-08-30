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

import com.embeddedunveiled.serial.SerialComException;
import com.embeddedunveiled.serial.internal.SerialComPortJNIBridge;

public final class SerialComBluetooth {

	private SerialComPortJNIBridge mComPortJNIBridge;

	/**
	 * <p>Construct and allocates a new SerialComBluetooth object with given details.</p>
	 * 
	 * @param comPortJNIBridge interface to native library.
	 */
	public SerialComBluetooth(SerialComPortJNIBridge comPortJNIBridge) {
		mComPortJNIBridge = comPortJNIBridge;
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
		String[] btDevicesInfo = mComPortJNIBridge.listBluetoothAdaptorsWithInfo();

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

	/**
	 * <p>Gives device node, remote bluetooth device address and channel number in use for device nodes 
	 * which are using the rfcomm service for emulating serial port over bluetooth.</p>
	 * 
	 * @return list of the BT SPP device node(s) with information about them or empty array if no 
	 *          device node is found.
	 * @throws SerialComException if an I/O error occurs.
	 */
	public SerialComBluetoothSPPDevNode[] listBTSPPDevNodesWithInfo() throws SerialComException {
		int i = 0;
		int numOfDevices = 0;
		SerialComBluetoothSPPDevNode[] btSerialNodesFound = null;
		String[] btSerialNodesInfo = mComPortJNIBridge.listBTSPPDevNodesWithInfo();

		if(btSerialNodesInfo != null) {
			if(btSerialNodesInfo.length < 2) {
				return new SerialComBluetoothSPPDevNode[] { };
			}
			numOfDevices = btSerialNodesInfo.length / 3;
			btSerialNodesFound = new SerialComBluetoothSPPDevNode[numOfDevices];
			for(int x=0; x<numOfDevices; x++) {
				btSerialNodesFound[x] = new SerialComBluetoothSPPDevNode(btSerialNodesInfo[i], btSerialNodesInfo[i+1], 
						btSerialNodesInfo[i+2]);
				i = i + 3;
			}
			return btSerialNodesFound;
		}else {
			throw new SerialComException("Could not find HID devices. Please retry !");
		}
	}
}
