/**
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
