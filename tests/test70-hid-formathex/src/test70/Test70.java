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

package test70;

import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.hid.SerialComHID;
import com.embeddedunveiled.serial.usb.SerialComUSBHID;

public class Test70 {

	public static SerialComManager scm = null;
	public static String PORT = null;
	public static String PORT1 = null;

	public static void main(String[] args) {
		try {
			scm = new SerialComManager();
//			SerialComUSBHID scuh = (SerialComUSBHID) scm.getSerialComHIDInstance(SerialComHID.HID_USB, null, null);
//			System.out.println(scuh.formatReportToHex("hello".getBytes()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
