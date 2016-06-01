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

package test79;

import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.usb.SerialComUSB;
import com.embeddedunveiled.serial.usb.SerialComUSBPowerInfo;

public class Test79  {

	static SerialComManager scm;
	static int osType;
	static String PORT;
	static String PORT1;
	static SerialComUSB usbsys;

	public static void main(String[] args) {
		try {
			scm = new SerialComManager();
			osType = scm.getOSType();
			if(osType == SerialComManager.OS_LINUX) {
				PORT = "/dev/ttyUSB0";
				PORT1 = "/dev/ttyUSB1";
			}else if(osType == SerialComManager.OS_WINDOWS) {
				PORT = "COM51";
				PORT1 = "COM52";
			}else if(osType == SerialComManager.OS_MAC_OS_X) {
				PORT = "/dev/cu.usbserial-A70362A3";
				PORT1 = "/dev/cu.usbserial-A602RDCH";
			}else if(osType == SerialComManager.OS_SOLARIS) {
				PORT = null;
				PORT1 = null;
			}else{
			}

			usbsys = scm.getSerialComUSBInstance();

			SerialComUSBPowerInfo info = usbsys.getCDCUSBDevPowerInfo(PORT);
			info.dumpDevicePowerInfo();

			SerialComUSBPowerInfo info1 = usbsys.getCDCUSBDevPowerInfo(PORT1);
			info1.dumpDevicePowerInfo();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
