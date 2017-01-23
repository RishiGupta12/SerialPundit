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

package test35;

import java.io.IOException;
import java.util.Random;

import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComSystemProperty;
import com.serialpundit.serial.SerialComManager;
import com.serialpundit.serial.SerialComManager.BAUDRATE;
import com.serialpundit.serial.SerialComManager.DATABITS;
import com.serialpundit.serial.SerialComManager.FLOWCONTROL;
import com.serialpundit.serial.SerialComManager.PARITY;
import com.serialpundit.serial.SerialComManager.STOPBITS;
import com.serialpundit.serial.ISerialComDataListener;
import com.serialpundit.serial.ISerialComEventListener;
import com.serialpundit.serial.SerialComLineEvent;

// how fast and times continuous read - writes
public class Test35 {
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

			// TEST 1
			try {
				long handle = scm.openComPort(PORT, true, true, true);
				scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
				scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);

				long handle1 = scm.openComPort(PORT1, true, true, true);
				scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
				scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);

				for(int x = 0; x<5000; x++) {
					scm.writeString(handle, "HELLO", 0);
					if(osType == SerialComPlatform.OS_LINUX) {
						Thread.sleep(100);
					}else if(osType == SerialComPlatform.OS_WINDOWS) {
						Thread.sleep(100);
					}else if(osType == SerialComPlatform.OS_MAC_OS_X) {
						Thread.sleep(100);
					}else if(osType == SerialComPlatform.OS_SOLARIS) {
						Thread.sleep(100);
					}else{
					}
					String data = scm.readString(handle1);
					System.out.println("Iteration : " + x + "==" + "data read is : " + data);
				}

				scm.closeComPort(handle);
				scm.closeComPort(handle1);
				System.out.println("TEST 1 PASSED !");
			} catch (Exception e) {
				e.printStackTrace();
			}

			// TEST 2, RANDOM DATA IN SIZE AND VALUE
			try {
				long handle = scm.openComPort(PORT, true, true, true);
				scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
				scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);

				long handle1 = scm.openComPort(PORT1, true, true, true);
				scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
				scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);

				Random randomGenerator = new Random();

				for(int x = 0; x<5000; x++) {

					int size = randomGenerator.nextInt(1000);
					byte[] databufw = new byte[size];

					for(int i = 0; i<size; i++) {
						databufw[i] = (byte) 'A';
					}

					scm.writeBytes(handle, databufw, 0);

					if(osType == SerialComPlatform.OS_LINUX) {
						Thread.sleep(100);
					}else if(osType == SerialComPlatform.OS_WINDOWS) {
						Thread.sleep(100);
					}else if(osType == SerialComPlatform.OS_MAC_OS_X) {
						Thread.sleep(100);
					}else if(osType == SerialComPlatform.OS_SOLARIS) {
						Thread.sleep(100);
					}else{
					}

					byte[] databufr = scm.readBytes(handle1, size);
					if(databufr != null) {
						for(int q = 0; q<size; q++) {
							if(databufw[q] != databufr[q]) {
								scm.closeComPort(handle);
								scm.closeComPort(handle1);
								throw new IOException("databufw[q] != databufr[q]\n");
							}
						}
					}else {
						System.out.println("ERROR ---- NULL BUFFER RECEIVED !");
					}
				}

				scm.closeComPort(handle);
				scm.closeComPort(handle1);
				System.out.println("TEST 2 PASSED !");
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
