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

package test30;

import com.embeddedunveiled.serial.ISerialComDataListener;
import com.embeddedunveiled.serial.SerialComDataEvent;
import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.SerialComManager.BAUDRATE;
import com.embeddedunveiled.serial.SerialComManager.DATABITS;
import com.embeddedunveiled.serial.SerialComManager.FLOWCONTROL;
import com.embeddedunveiled.serial.SerialComManager.PARITY;
import com.embeddedunveiled.serial.SerialComManager.STOPBITS;
import com.embeddedunveiled.serial.ISerialComEventListener;
import com.embeddedunveiled.serial.SerialComLineEvent;

class EventListener implements ISerialComEventListener{
	@Override
	public void onNewSerialEvent(SerialComLineEvent lineEvent) {
		System.out.println("eventCTS : " + lineEvent.getCTS());
		System.out.println("eventDSR : " + lineEvent.getDSR());
	}
}

class DataListener implements ISerialComDataListener{
	@Override
	public void onNewSerialDataAvailable(SerialComDataEvent data) {
		System.out.println("Read from serial port : " + new String(data.getDataBytes()) + "\n");
	}

	@Override
	public void onDataListenerError(int arg0) {
	}
}

/* maximum 4*1024 bytes can be written in one go */
public class Test30 {
	public static void main(String[] args) {
		try {
			SerialComManager scm = new SerialComManager();
			EventListener eventListener = new EventListener();
			DataListener dataListener = new DataListener();

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

			int x = 0;
			byte[] data = new byte[4096];
			for(x=0; x<data.length; x++ ) {
				data[x] = (byte) 'a';
			}

			long handle = scm.openComPort(PORT, true, true, true);
			scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);

			long handle1 = scm.openComPort(PORT1, true, true, true);
			scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);

			scm.writeBytes(handle, data, 0);
			Thread.sleep(50);
			
			String str = scm.readString(handle1);
			System.out.println("data length read : " + str.length());
			String str1 = scm.readString(handle1);
			System.out.println("data length read : " + str1.length());
			String str2 = scm.readString(handle1);
			System.out.println("data length read : " + str2.length());
			String str3 = scm.readString(handle1);
			System.out.println("data length read : " + str3.length());
			
			scm.writeBytes(handle, data, 0);
			Thread.sleep(50);
			
			String str4 = scm.readString(handle1, 2000);
			System.out.println("data length read : " + str.length());

			scm.closeComPort(handle);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
