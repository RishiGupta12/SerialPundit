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

package test26;

import java.io.IOException;

import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComSystemProperty;
import com.serialpundit.serial.SerialComManager;
import com.serialpundit.serial.SerialComManager.BAUDRATE;
import com.serialpundit.serial.SerialComManager.DATABITS;
import com.serialpundit.serial.SerialComManager.FLOWCONTROL;
import com.serialpundit.serial.SerialComManager.PARITY;
import com.serialpundit.serial.SerialComManager.STOPBITS;

public class Test26 {
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

			int x = 0;
			long handle;
			long handle1;

			handle = scm.openComPort(PORT, true, true, true);
			scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);
			handle1 = scm.openComPort(PORT1, true, true, true);
			scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);

			for(x=0; x<10000; x++) {
				scm.writeString(handle, "testing", 0);
				scm.writeString(handle1, "testing1", 0);
			}

			for(x=0; x<10000; x++) {
				if(scm.writeString(handle, "testing", 0) == false) {
					throw new IOException("write failed");
				}
				if(scm.writeString(handle1, "testing1", 0) == false) {
					throw new IOException("write 1 failed");
				}

				Thread.sleep(10);
				System.out.println("data1 read is : " + scm.readString(handle1));
				System.out.println("data read is : " + scm.readString(handle));
			}

			System.out.println("close status : " + scm.closeComPort(handle));
			System.out.println("close status : " + scm.closeComPort(handle1));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
