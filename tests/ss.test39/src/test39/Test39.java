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

package test39;

import com.embeddedunveiled.serial.ISerialComDataListener;
import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.SerialComManager.BAUDRATE;
import com.embeddedunveiled.serial.SerialComManager.DATABITS;
import com.embeddedunveiled.serial.SerialComManager.FLOWCONTROL;
import com.embeddedunveiled.serial.SerialComManager.PARITY;
import com.embeddedunveiled.serial.SerialComManager.STOPBITS;

class DataListener implements ISerialComDataListener{

	@Override
	public void onNewSerialDataAvailable(byte[] arg0) {
		System.out.println(new String(arg0));
	}

	@Override
	public void onDataListenerError(int arg0) {
		System.out.println("error : " + arg0);
	}

}


// read request amount of bytes from serial port
public class Test39 {
	public static void main(String[] args) {
		try {
			SerialComManager scm = new SerialComManager();
			DataListener dataListener = new DataListener();

			String testStr = "Files were transferred one packet at a time. When received, the packet's checksum was calculated by the receiver and compared to the one received from the sender at the end of the packet."
					+ "Files were transferred one packet at a time. When received, the packet's checksum was calculated by the receiver and compared to the one received from the sender at the end of the packet."
					+ "Files were transferred one packet at a time. When received, the packet's checksum was calculated by the receiver and compared to the one received from the sender at the end of the packet."
					+ "Files were transferred one packet at a time. When received, the packet's checksum was calculated by the receiver and compared to the one received from the sender at the end of the packet."
					+ "Files were transferred one packet at a time. When received, the packet's checksum was calculated by the receiver and compared to the one received from the sender at the end of the packet."
					+ "Files were transferred one packet at a time. When received, the packet's checksum was calculated by the receiver and compared to the one received from the sender at the end of the packet."
					+ "Files were transferred one packet at a time. When received, the packet's checksum was calculated by the receiver and compared to the one received from the sender at the end of the packet."
					+ "Files were transferred one packet at a time. When received, the packet's checksum was calculated by the receiver and compared to the one received from the sender at the end of the packet."
					+ "Files were transferred one packet at a time. When received, the packet's checksum was calculated by the receiver and compared to the one received from the sender at the end of the packet."
					+ "Files were transferred one packet at a time. When received, the packet's checksum was calculated by the receiver and compared to the one received from the sender at the end of the packet."
					+ "Files were transferred one packet at a time. When received, the packet's checksum was calculated by the receiver and compared to the one received from the sender at the end of the packet."
					+ "Files were transferred one packet at a time. When received, the packet's checksum was calculated by the receiver and compared to the one received from the sender at the end of the packet."
					+ "Files were transferred one packet at a time. When received, the packet's checksum was calculated by the receiver and compared to the one received from the sender at the end of the packet."
					+ "Files were transferred one packet at a time. When received, the packet's checksum was calculated by the receiver and compared to the one received from the sender at the end of the packet."
					+ "Files were transferred one packet at a time. When received, the packet's checksum was calculated by the receiver and compared to the one received from the sender at the end of the packet."
					+ "Files were transferred one packet at a time. When received, the packet's checksum was calculated by the receiver and compared to the one received from the sender at the end of the packet."
					+ "Files were transferred one packet at a time. When received, the packet's checksum was calculated by the receiver and compared to the one received from the sender at the end of the packet."
					+ "Files were transferred one packet at a time. When received, the packet's checksum was calculated by the receiver and compared to the one received from the sender at the end of the packet."
					+ "Files were transferred one packet at a time. When received, the packet's checksum was calculated by the receiver and compared to the one received from the sender at the end of the packet."
					+ "Files were transferred one packet at a time. When received, the packet's checksum was calculated by the receiver and compared to the one received from the sender at the end of the packet."
					+ "Files were transferred one packet at a time. When received, the packet's checksum was calculated by the receiver and compared to the one received from the sender at the end of the packet."
					+ "Files were transferred one packet at a time. When received, the packet's checksum was calculated by the receiver and compared to the one received from the sender at the end of the packet."
					+ "Files were transferred one packet at a time. When received, the packet's checksum was calculated by the receiver and compared to the one received from the sender at the end of the packet."
					+ "Files were transferred one packet at a time. When received, the packet's checksum was calculated by the receiver and compared to the one received from the sender at the end of the packet."
					+ "Files were transferred one packet at a time. When received, the packet's checksum was calculated by the receiver and compared to the one received from the sender at the end of the packet."
					+ "Files were transferred one packet at a time. When received, the packet's checksum was calculated by the receiver and compared to the one received from the sender at the end of the packet.";


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

			long handle = scm.openComPort(PORT, true, true, true);
			scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);

			long handle1 = scm.openComPort(PORT1, true, true, true);
			scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);

			System.out.println("register  : " + scm.registerDataListener(handle, dataListener));
			//
			//			//			while(true) {
			scm.writeString(handle1, "a", 0);
			//			//			}
			//
			System.out.println("unregister : " + scm.unregisterDataListener(handle, dataListener));
			scm.closeComPort(handle);
			scm.closeComPort(handle1);
			while(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
