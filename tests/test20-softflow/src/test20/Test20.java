/*
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

package test20;

import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.SerialComManager.BAUDRATE;
import com.embeddedunveiled.serial.SerialComManager.DATABITS;
import com.embeddedunveiled.serial.SerialComManager.FLOWCONTROL;
import com.embeddedunveiled.serial.SerialComManager.PARITY;
import com.embeddedunveiled.serial.SerialComManager.STOPBITS;


/*
 * port will send xoff and xon after buffer limit is reached.
 * OS will filter the xon/xoff character and application will not receive it.
 */
public class Test20 {

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
				PORT = "COM11";
				PORT1 = "COM5";
			}else if(osType == SerialComManager.OS_MAC_OS_X) {
				PORT = "/dev/cu.usbserial-A70362A3";
				PORT1 = "/dev/cu.usbserial-A602RDCH";
			}else if(osType == SerialComManager.OS_SOLARIS) {
				PORT = null;
				PORT1 = null;
			}else{
			}

			long receiverHandle = scm.openComPort(PORT, true, true, true);
			scm.configureComPortData(receiverHandle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(receiverHandle, FLOWCONTROL.XON_XOFF, (char) 0x11, (char)0x13, false, false);

			long senderHandle = scm.openComPort(PORT1, true, true, true);
			scm.configureComPortData(senderHandle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(senderHandle, FLOWCONTROL.XON_XOFF, (char) 0x11, (char)0x13, false, false);

			byte[] buffer = new byte[1024];
			for(int x=0; x<1024; x++) {
				buffer[x] = (byte) 'A';
			}

			System.out.println("1 write status :" + scm.writeBytes(senderHandle, buffer, 0));
			Thread.sleep(100);
			System.out.println("2 write status :" + scm.writeBytes(senderHandle, buffer, 0));
			Thread.sleep(100);
			System.out.println("3 write status :" + scm.writeBytes(senderHandle, buffer, 0));
			Thread.sleep(100);
			System.out.println("4 write status :" + scm.writeBytes(senderHandle, buffer, 0));
			Thread.sleep(100);
			System.out.println("5 write status :" + scm.writeBytes(senderHandle, buffer, 0));
			System.out.println("6 write status :" + scm.writeBytes(senderHandle, buffer, 0));
			System.out.println("7 write status :" + scm.writeBytes(senderHandle, buffer, 0));
			Thread.sleep(2000);
			System.out.println("8 write status :" + scm.writeBytes(senderHandle, buffer, 0));
			System.out.println("9 write status :" + scm.writeBytes(senderHandle, buffer, 0));

			byte[] dataread = scm.readBytes(receiverHandle);
			if(dataread != null) {
				System.out.println("\n" + new String(dataread));
			}
			dataread = scm.readBytes(receiverHandle);
			if(dataread != null) {
				System.out.println("\n" + new String(dataread));
			}
			dataread = scm.readBytes(receiverHandle);
			if(dataread != null) {
				System.out.println("\n" + new String(dataread));
			}
			dataread = scm.readBytes(receiverHandle);
			if(dataread != null) {
				System.out.println("\n" + new String(dataread));
			}
			dataread = scm.readBytes(receiverHandle);
			if(dataread != null) {
				System.out.println("\n" + new String(dataread));
			}
			dataread = scm.readBytes(receiverHandle);
			if(dataread != null) {
				System.out.println("\n" + new String(dataread));
			}

			System.out.println("10 write status :" + scm.writeBytes(senderHandle, buffer, 0));

			scm.closeComPort(receiverHandle);
			scm.closeComPort(senderHandle);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
