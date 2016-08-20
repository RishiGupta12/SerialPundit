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

package test46;

import java.io.IOException;

import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComSystemProperty;
import com.serialpundit.serial.SerialComManager;
import com.serialpundit.serial.SerialComManager.BAUDRATE;
import com.serialpundit.serial.SerialComManager.DATABITS;
import com.serialpundit.serial.SerialComManager.FLOWCONTROL;
import com.serialpundit.serial.SerialComManager.PARITY;
import com.serialpundit.serial.SerialComManager.STOPBITS;

/* return number of bytes should be same as requested, less number return if less data in buffer
 * req nubemr of read 5, got 5
req nuber of read 9, got 9
req nuber of read 18, got 18
req nuber of read 25, got 25
req nuber of read 38, got 38
req nuber of read 128, got 128
req nuber of read 1000, got 347
 */
public class Test46 {
	public static void main(String[] args) throws SecurityException, IOException {
		SerialComManager scm = new SerialComManager();
		try {
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

			long handle = scm.openComPort(PORT, true, true, true);
			scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle, FLOWCONTROL.NONE, '$', '$', false, false);
			long handle1 = scm.openComPort(PORT1, true, true, true);
			scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle1, FLOWCONTROL.NONE, '$', '$', false, false);

			int x = 0;
			for(x=0; x<10; x++) {
				scm.writeString(handle1, "qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq", 0);
			}

			Thread.sleep(1000); // let data reach physically to port and OS buffer it
			
			byte[] data = scm.readBytes(handle, 0);
			if(data != null) { 
				System.out.println("req nuber of read 0, " + "got " + data.length);
			}else {
				System.out.println("req nuber of read 0, " + "got " + "null");	
			}
			data = scm.readBytes(handle, 1);
			System.out.println("req nuber of read 1, " + "got " + data.length);
			data = scm.readBytes(handle, 2);
			System.out.println("req nuber of read 2, " + "got " + data.length);
			data = scm.readBytes(handle, 3);
			System.out.println("req nuber of read 3, " + "got " + data.length);
			data = scm.readBytes(handle, 5);
			System.out.println("req nuber of read 5, " + "got " + data.length);
			data = scm.readBytes(handle, 9);
			System.out.println("req nuber of read 9, " + "got " + data.length);
			data = scm.readBytes(handle, 18);
			System.out.println("req nuber of read 18, " + "got " + data.length);
			data = scm.readBytes(handle, 25);
			System.out.println("req nuber of read 25, " + "got " + data.length);
			data = scm.readBytes(handle, 38);
			System.out.println("req nuber of read 38, " + "got " + data.length);
			data = scm.readBytes(handle, 128);
			System.out.println("req nuber of read 128, " + "got " + data.length);
			data = scm.readBytes(handle, 1000);
			System.out.println("req nuber of read 1000, " + "got " + data.length);

			scm.closeComPort(handle);
			scm.closeComPort(handle1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
