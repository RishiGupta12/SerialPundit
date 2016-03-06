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

package test24;

import com.embeddedunveiled.serial.ISerialComEventListener;
import com.embeddedunveiled.serial.SerialComLineEvent;
import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.SerialComManager.BAUDRATE;
import com.embeddedunveiled.serial.SerialComManager.DATABITS;
import com.embeddedunveiled.serial.SerialComManager.FLOWCONTROL;
import com.embeddedunveiled.serial.SerialComManager.PARITY;
import com.embeddedunveiled.serial.SerialComManager.STOPBITS;
import com.embeddedunveiled.serial.ISerialComDataListener;
import com.embeddedunveiled.serial.SerialComDataEvent;

class DataListener0 implements ISerialComDataListener{
	@Override
	public void onNewSerialDataAvailable(SerialComDataEvent data) {
		System.out.println("DCE GOT FROM DTE : " + new String(data.getDataBytes()));
	}

	@Override
	public void onDataListenerError(int arg0) {
	}
}

class DataListener1 implements ISerialComDataListener{
	@Override
	public void onNewSerialDataAvailable(SerialComDataEvent data) {
		System.out.println("DTE GOT FROM DCE : " + new String(data.getDataBytes()));
	}

	@Override
	public void onDataListenerError(int arg0) {
	}
}

class EventListener0 implements ISerialComEventListener {
	@Override
	public void onNewSerialEvent(SerialComLineEvent lineEvent) {
		System.out.println("0eventCTS : " + lineEvent.getCTS());
		System.out.println("0eventDTR : " + lineEvent.getDSR());
	}
}

class EventListener1 implements ISerialComEventListener {
	@Override
	public void onNewSerialEvent(SerialComLineEvent lineEvent) {
		System.out.println("1eventCTS : " + lineEvent.getCTS());
		System.out.println("1eventDTR : " + lineEvent.getDSR());
	}
}

public class Test24 {
	public static void main(String[] args) {
		SerialComManager scm = null;
		String PORT = null;
		String PORT1 = null;
		int osType = 0;

		try {
			scm = new SerialComManager();
			osType = scm.getOSType();

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
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Test 1
		try {
			EventListener0 eventListener0 = new EventListener0();
			EventListener0 eventListener1 = new EventListener0();
			DataListener0 dataListenerDTE0 = new DataListener0();
			DataListener1 dataListenerDCE1 = new DataListener1();

			// DTE terminal
			long handleDTE = scm.openComPort(PORT, true, true, true);
			scm.configureComPortData(handleDTE, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
			scm.configureComPortControl(handleDTE, FLOWCONTROL.NONE, 'x', 'x', false, false);
			scm.registerDataListener(handleDTE, dataListenerDTE0);
			scm.registerLineEventListener(handleDTE, eventListener0);

			// DCE terminal
			long handleDCE = scm.openComPort(PORT1, true, true, true);
			scm.configureComPortData(handleDCE, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
			scm.configureComPortControl(handleDCE, FLOWCONTROL.NONE, 'x', 'x', false, false);
			scm.registerDataListener(handleDCE, dataListenerDCE1);
			scm.registerLineEventListener(handleDCE, eventListener1);

			// Step 1
			scm.writeString(handleDTE, "str1", 0);
			Thread.sleep(50);
			scm.writeString(handleDTE, "str1", 0);
			Thread.sleep(50);
			scm.writeString(handleDCE, "str2", 0);
			Thread.sleep(50);
			scm.writeString(handleDCE, "str2", 0);

			Thread.sleep(200);

			scm.unregisterDataListener(dataListenerDTE0);
			scm.unregisterLineEventListener(eventListener0);
			scm.unregisterDataListener(dataListenerDCE1);
			scm.unregisterLineEventListener(eventListener1);

			scm.closeComPort(handleDTE);
			scm.closeComPort(handleDCE);
			System.out.println("done 1!");
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Test 2
		// register/unregister data/event in various permutation/combinations.
		try {
			long handle = scm.openComPort(PORT, true, true, true);
			scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
			scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);
			long handle1 = scm.openComPort(PORT1, true, true, true);
			scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
			scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);

			DataListener0 dataListener0 = new DataListener0();
			DataListener1 dataListener1 = new DataListener1();
			EventListener0 eventListener0 = new EventListener0();
			EventListener1 eventListener1 = new EventListener1();

			scm.registerDataListener(handle, dataListener0);
			scm.unregisterDataListener(dataListener0);
			scm.registerLineEventListener(handle, eventListener0);
			scm.unregisterLineEventListener(eventListener0);

			scm.registerDataListener(handle, dataListener0);
			scm.registerLineEventListener(handle, eventListener0);
			scm.unregisterDataListener(dataListener0);
			scm.unregisterLineEventListener(eventListener0);

			scm.registerLineEventListener(handle, eventListener0);
			scm.registerDataListener(handle, dataListener0);
			scm.unregisterDataListener(dataListener0);
			scm.unregisterLineEventListener(eventListener0);

			scm.registerLineEventListener(handle, eventListener0);
			scm.registerDataListener(handle, dataListener0);
			scm.unregisterLineEventListener(eventListener0);
			scm.unregisterDataListener(dataListener0);

			scm.registerDataListener(handle, dataListener0);
			scm.unregisterDataListener(dataListener0);
			scm.registerLineEventListener(handle1, eventListener0);
			scm.unregisterLineEventListener(eventListener0);

			scm.registerDataListener(handle, dataListener0);
			scm.registerLineEventListener(handle1, eventListener1);
			scm.unregisterDataListener(dataListener0);
			scm.unregisterLineEventListener(eventListener1);

			scm.registerLineEventListener(handle1, eventListener0);
			scm.registerDataListener(handle, dataListener0);
			scm.unregisterDataListener(dataListener0);
			scm.unregisterLineEventListener(eventListener0);

			scm.registerLineEventListener(handle1, eventListener1);
			scm.registerDataListener(handle, dataListener0);
			scm.unregisterLineEventListener(eventListener1);
			scm.unregisterDataListener(dataListener0);
			System.out.println("done 2!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
