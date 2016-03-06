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

package test45;

import com.embeddedunveiled.serial.ISerialComUSBHotPlugListener;
import com.embeddedunveiled.serial.SerialComManager;

//event 2 indicates port removal, 1 indicates additional of port
class USBHotPlugEventHandler implements ISerialComUSBHotPlugListener {

	@Override
	public void onUSBHotPlugEvent(int event, int vid, int pid, String serial) {
		System.out.println("event " + event + " vid:" + vid + " pid:" + pid + " serial:" + serial);
	}
}

public class Test45 {
	public static void main(String[] args) {

		// REGISTER SAME HANDLER
		try {
			SerialComManager scm = new SerialComManager();
			USBHotPlugEventHandler eventhandler = new USBHotPlugEventHandler();
			int x = 0;
			int handle;
			int handle1;
			int handle2;
			int handle3;
			for (x=0; x<100; x++) {
				System.out.println("Iteration :" + x);
				handle  = scm.registerUSBHotPlugEventListener(eventhandler, 0x0403, 0x6001, null);
				handle1 = scm.registerUSBHotPlugEventListener(eventhandler, 0x10C4, 0xEA60, "0001");
				handle2 = scm.registerUSBHotPlugEventListener(eventhandler, 0x04d8, 0x00df, "0000980371");
				handle3 = scm.registerUSBHotPlugEventListener(eventhandler, 0x0403, 0x6001, "A7036479");
				scm.unregisterUSBHotPlugEventListener(handle);
				scm.unregisterUSBHotPlugEventListener(handle1);
				scm.unregisterUSBHotPlugEventListener(handle2);
				scm.unregisterUSBHotPlugEventListener(handle3);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Test 1 done !");

		// REGISTER/UNREGISTER MANY TIMES
		try {
			SerialComManager scm = new SerialComManager();
			int x = 0;
			int handle;
			for (x=0; x<500000; x++) {
				System.out.println("Iteration :" + x);
				USBHotPlugEventHandler eventhandler = new USBHotPlugEventHandler();
				handle = scm.registerUSBHotPlugEventListener(eventhandler, 0x0403, 0x6001, null);
				scm.unregisterUSBHotPlugEventListener(handle);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Test 2 done !");
	}
}
