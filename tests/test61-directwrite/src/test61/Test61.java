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

package test61;

import java.nio.ByteBuffer;

import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComSystemProperty;
import com.serialpundit.serial.SerialComManager;
import com.serialpundit.serial.SerialComManager.BAUDRATE;
import com.serialpundit.serial.SerialComManager.DATABITS;
import com.serialpundit.serial.SerialComManager.FLOWCONTROL;
import com.serialpundit.serial.SerialComManager.PARITY;
import com.serialpundit.serial.SerialComManager.STOPBITS;

public final class Test61 {

	public static void main(String[] args) {

		String PORT = null;
		String PORT1 = null;
		int osType;

		try {
			SerialComManager scm = new SerialComManager();
			SerialComPlatform scp = new SerialComPlatform(new SerialComSystemProperty());

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

			long handle = scm.openComPort(PORT, true, true, false);
			scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);
			long handle1 = scm.openComPort(PORT1, true, true, false);
			scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);

			ByteBuffer writeBuffer = ByteBuffer.allocateDirect(10 * 1024);
			ByteBuffer readBuffer = ByteBuffer.allocateDirect(10 * 1024);
			//			ByteBuffer buffer = ByteBuffer.allocate(10 * 1024); this should throw exception as it is not direct

			for(int x=0; x<205; x++) {
				writeBuffer.put("--Practising-meditation-will-lead-to-Nirvana--".getBytes());
			}

			System.out.println("Capacity : " + writeBuffer.capacity());
			System.out.println("Position : " + writeBuffer.position());
			System.out.println("" + writeBuffer.get(5) + "," + writeBuffer.get(6) + "," + writeBuffer.get(7));

			scm.writeBytesDirect(handle, writeBuffer, 0, 4 * 1023);
			Thread.sleep(1000);
			System.out.println(scm.readString(handle1, 2 * 1024));
			System.out.println(scm.readString(handle1, 2 * 1024));
			System.out.println(scm.readString(handle1, 2 * 1024));

			System.out.println("Capacity : " + writeBuffer.capacity());
			System.out.println("Position : " + writeBuffer.position());
			System.out.println("" + writeBuffer.get(5));

			// read from same locations where data was written
			System.out.println("\n");
			writeBuffer.clear();
			for(int x=0; x<205; x++) {
				writeBuffer.put("--Practising-meditation-will-lead-to-Nirvana--".getBytes());
			}
			System.out.println("Capacity : " + writeBuffer.capacity());
			System.out.println("Position : " + writeBuffer.position());
			System.out.println("" + writeBuffer.get(5) + "," + writeBuffer.get(6) + "," + writeBuffer.get(7));
			scm.writeBytesDirect(handle, writeBuffer, 0, 4 * 1023);
			Thread.sleep(1000);
			System.out.println("Capacity : " + readBuffer.capacity());
			System.out.println("Position : " + readBuffer.position());
			scm.readBytesDirect(handle1, readBuffer, 0, 9000);
			System.out.println("Capacity : " + readBuffer.capacity());
			System.out.println("Position : " + readBuffer.position());
			System.out.println("" + readBuffer.get(5) + "," + readBuffer.get(6) + "," + readBuffer.get(7));

			scm.closeComPort(handle);
			scm.closeComPort(handle1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
