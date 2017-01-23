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

package test79;

import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComSystemProperty;
import com.serialpundit.usb.SerialComUSB;
import com.serialpundit.usb.SerialComUSBPowerInfo;

public class Test79  {

	static int osType;
	static String PORT;
	static String PORT1;
	static SerialComUSB usbsys;

	public static void main(String[] args) {
		try {
			SerialComPlatform scp = new SerialComPlatform(new SerialComSystemProperty());
			usbsys = new SerialComUSB(null, null);

			osType = scp.getOSType();
			if(osType == SerialComPlatform.OS_LINUX) {
				PORT = "/dev/ttyUSB0";
				PORT1 = "/dev/ttyUSB1";
			}else if(osType == SerialComPlatform.OS_WINDOWS) {
				PORT = "COM51";
				PORT1 = "COM52";
			}else if(osType == SerialComPlatform.OS_MAC_OS_X) {
				PORT = "/dev/cu.usbserial-A70362A3";
				PORT1 = "/dev/cu.usbserial-A602RDCH";
			}else if(osType == SerialComPlatform.OS_SOLARIS) {
				PORT = null;
				PORT1 = null;
			}else{
			}

			System.out.println("port : " + PORT + " : " + PORT1);

			SerialComUSBPowerInfo info = usbsys.getCDCUSBDevPowerInfo(PORT);
			info.dumpDevicePowerInfo();

			SerialComUSBPowerInfo info1 = usbsys.getCDCUSBDevPowerInfo(PORT1);
			info1.dumpDevicePowerInfo();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
