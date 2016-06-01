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

package test81;

import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.usb.SerialComUSB;

public class Test81 {
	public static void main(String[] args) {
		try {
			SerialComManager scm = new SerialComManager();

			String PORT = null;
			int osType = scm.getOSType();
			if(osType == SerialComManager.OS_LINUX) {
				PORT = "/dev/ttyUSB0";
			}else if(osType == SerialComManager.OS_WINDOWS) {
				PORT = "COM51";
			}else if(osType == SerialComManager.OS_MAC_OS_X) {
				PORT = "/dev/cu.usbserial-A70362A3";
			}else if(osType == SerialComManager.OS_SOLARIS) {
				PORT = null;
			}else{
			}

			SerialComUSB usbsys = scm.getSerialComUSBInstance();

			try {
				System.out.println("value : " + usbsys.getLatencyTimer(PORT));
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				System.out.println("value : " + usbsys.getLatencyTimer("/dev/fdfsf"));
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				System.out.println("value : " + usbsys.setLatencyTimer(PORT, (byte) 5));
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				System.out.println("value : " + usbsys.setLatencyTimer("/dev/fdfsf", (byte) 5));
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
