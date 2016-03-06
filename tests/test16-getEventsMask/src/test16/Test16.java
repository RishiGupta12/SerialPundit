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

package test16;

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
	}
}

public class Test16 {
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

			// instantiate class which is will implement ISerialComEventListener interface
			EventListener eventListener = new EventListener();

			long handle = scm.openComPort(PORT, true, true, true);
			scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
			scm.registerLineEventListener(handle, eventListener);

			long handle1 = scm.openComPort(PORT1, true, true, true);
			scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);

			// get current active mask
			int mask1 = scm.getEventsMask(eventListener);
			System.out.println("mask before : " + mask1);

			// mask CTS, so only changes to CTS line will be reported.
			scm.setEventsMask(eventListener, SerialComManager.CTS);

			// get current active mask
			int mask2 = scm.getEventsMask(eventListener);
			System.out.println("mask after : " + mask2);

			// unregister data listener
			scm.unregisterLineEventListener(handle, eventListener);

			// close the port releasing handle
			scm.closeComPort(handle);
			scm.closeComPort(handle1);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
