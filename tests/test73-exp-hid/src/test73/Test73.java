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

package test73;

import com.embeddedunveiled.serial.SerialComHID;
import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.usb.SerialComUSBHID;

/* 
 * Tests whether throwing exception from different threads work or not.
 * JNI have many things which depends on context and thread in use.
 */

class Test extends Test73 implements Runnable {
	@Override
	public void run() {
		try {
			scuh.openHidDevice("ffffff");
		} catch (Exception e) {
			System.out.println("thrown from worker thread !");
		}
	}
}

public class Test73  {
	
	static protected SerialComUSBHID scuh;
	
	public static void main(String[] args) {
		try {
			SerialComManager scm = new SerialComManager();
			scuh = (SerialComUSBHID) scm.getSerialComHIDInstance(SerialComHID.HID_USB, null, null);
			
			//worker
			Thread mThread = new Thread(new Test());
			mThread.start();
			
			Thread.sleep(1000);
			//main
			scuh.openHidDevice("ffffff");
		} catch (Exception e) {
			System.out.println("thrown from main thread !");
		}
	}
}
