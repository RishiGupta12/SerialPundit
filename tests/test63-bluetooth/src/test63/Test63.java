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

package test63;

import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.bluetooth.SerialComBluetooth;
import com.embeddedunveiled.serial.bluetooth.SerialComBluetoothAdapter;

public final class Test63 {

	public static void main(String[] args) {
		try {
			SerialComManager scm = new SerialComManager();
			SerialComBluetooth bt = scm.getSerialComBluetoothInstance(SerialComBluetooth.BTSTACK_LINUX_BLUEZ, null ,null);
			SerialComBluetoothAdapter[] bluetoothAdaptors = bt.listBluetoothAdaptorsWithInfo();
			for(int x=0; x< bluetoothAdaptors.length; x++) {
				bluetoothAdaptors[x].dumpDeviceInfo();
			}

			/* when using external cheap usb dongle this has to be slow */
			for(int x=0; x< 50000; x++) {
				System.out.println("iteration : " + x);
				bt.listBluetoothAdaptorsWithInfo();
				Thread.sleep(50);
			}

			System.out.println("\ndone !");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
