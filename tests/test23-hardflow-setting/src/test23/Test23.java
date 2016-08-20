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

package test23;

import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComSystemProperty;
import com.serialpundit.serial.SerialComManager;
import com.serialpundit.serial.SerialComManager.BAUDRATE;
import com.serialpundit.serial.SerialComManager.DATABITS;
import com.serialpundit.serial.SerialComManager.FLOWCONTROL;
import com.serialpundit.serial.SerialComManager.PARITY;
import com.serialpundit.serial.SerialComManager.STOPBITS;

public class Test23 {
	public static void main(String[] args) {
		try {
			SerialComManager scm = new SerialComManager();

			String PORT = null;
			String PORT1 = null;
			SerialComPlatform scp = new SerialComPlatform(new SerialComSystemProperty());

			int osType = scp.getOSType();
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

			long DTE = scm.openComPort(PORT, true, true, true);
			scm.configureComPortData(DTE, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
			scm.configureComPortControl(DTE, FLOWCONTROL.RTS_CTS, 'x', 'x', false, true);

			long DTE1 = scm.openComPort(PORT1, true, true, true);
			scm.configureComPortData(DTE1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
			scm.configureComPortControl(DTE1, FLOWCONTROL.RTS_CTS, 'x', 'x', false, true);

			scm.setDTR(DTE, true);
			scm.setDTR(DTE1, true);
			Thread.sleep(100);
			scm.setRTS(DTE, true);
			scm.setRTS(DTE1, true);
			Thread.sleep(100);

			scm.writeString(DTE, "str1", 0);
			Thread.sleep(100);
			scm.writeString(DTE, "str1", 0);
			Thread.sleep(100);
			scm.writeString(DTE, "str1", 0);
			Thread.sleep(100);
			scm.writeString(DTE, "str1", 0);
			scm.writeString(DTE, "str1", 0);
			Thread.sleep(100);
			scm.writeString(DTE, "str1", 0);
			Thread.sleep(100);
			scm.writeString(DTE, "str1", 0);
			Thread.sleep(100);
			scm.writeString(DTE, "str1", 0);

			scm.writeString(DTE1, "str1", 0);
			Thread.sleep(100);
			scm.writeString(DTE1, "str1", 0);
			Thread.sleep(100);
			scm.writeString(DTE1, "str1", 0);
			scm.writeString(DTE1, "str1", 0);
			Thread.sleep(100);
			scm.writeString(DTE1, "str1", 0);
			Thread.sleep(100);
			scm.writeString(DTE1, "str1", 0);

			scm.closeComPort(DTE);
			scm.closeComPort(DTE1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
