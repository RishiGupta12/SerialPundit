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

package test35;

import java.io.IOException;
import java.util.Random;
import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.SerialComManager.BAUDRATE;
import com.embeddedunveiled.serial.SerialComManager.DATABITS;
import com.embeddedunveiled.serial.SerialComManager.FLOWCONTROL;
import com.embeddedunveiled.serial.SerialComManager.PARITY;
import com.embeddedunveiled.serial.SerialComManager.STOPBITS;

// how fast and times continuous read - writes
public class Test35 {
	public static void main(String[] args) {
		try {	
			SerialComManager scm = new SerialComManager();

			String PORT = null;
			String PORT1 = null;
			int osType = scm.getOSType();
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

//			// TEST 1
//			try {
//				long handle = scm.openComPort(PORT, true, true, true);
//				scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
//				scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);
//
//				long handle1 = scm.openComPort(PORT1, true, true, true);
//				scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
//				scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
//
//				for(int x = 0; x<5000; x++) {
//					scm.writeString(handle, "HELLO", 0);
//					if(osType == SerialComManager.OS_LINUX) {
//						Thread.sleep(100);
//					}else if(osType == SerialComManager.OS_WINDOWS) {
//						Thread.sleep(100);
//					}else if(osType == SerialComManager.OS_MAC_OS_X) {
//						Thread.sleep(100);
//					}else if(osType == SerialComManager.OS_SOLARIS) {
//						Thread.sleep(100);
//					}else{
//					}
//					String data = scm.readString(handle1);
//					System.out.println("Iteration : " + x + "==" + "data read is : " + data);
//				}
//
//				scm.closeComPort(handle);
//				scm.closeComPort(handle1);
//				System.out.println("TEST 1 PASSED !");
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
			
			PORT = "/dev/pts/1";
			PORT1 = "/dev/pts/3";

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

					if(osType == SerialComManager.OS_LINUX) {
						Thread.sleep(100);
					}else if(osType == SerialComManager.OS_WINDOWS) {
						Thread.sleep(100);
					}else if(osType == SerialComManager.OS_MAC_OS_X) {
						Thread.sleep(100);
					}else if(osType == SerialComManager.OS_SOLARIS) {
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
