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
