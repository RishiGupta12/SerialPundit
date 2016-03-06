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

package test69;

import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.bluetooth.SerialComBluetooth;
import com.embeddedunveiled.serial.bluetooth.SerialComBluetoothSPPDevNode;

public class Test69  {

	public static SerialComManager scm = null;

	public static void main(String[] args) {
		try {
			scm = new SerialComManager();
			SerialComBluetooth bt = scm.getSerialComBluetoothInstance();
			SerialComBluetoothSPPDevNode[] nodes = bt.listBTSPPDevNodesWithInfo();
			for(int x=0; x< nodes.length; x++) {
				nodes[x].dumpDeviceInfo();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
