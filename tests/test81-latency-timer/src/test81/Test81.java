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

package test81;

import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.usb.SerialComUSB;

public class Test81 {
	public static void main(String[] args) {
		try {
			SerialComManager scm = new SerialComManager();

			String PORT = null;
			int osType = scm.getOSType();
			if(osType == SerialComManager.OS_LINUX) {
				PORT = "/dev/ttyUSB0";
			}else if(osType == SerialComManager.OS_WINDOWS) {
				PORT = "COM51";
			}else if(osType == SerialComManager.OS_MAC_OS_X) {
				PORT = "/dev/cu.usbserial-A70362A3";
			}else if(osType == SerialComManager.OS_SOLARIS) {
				PORT = null;
			}else{
			}

			SerialComUSB usbsys = scm.getSerialComUSBInstance();

			try {
				System.out.println("value : " + usbsys.getLatencyTimer(PORT));
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				System.out.println("value : " + usbsys.getLatencyTimer("/dev/fdfsf"));
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				System.out.println("value : " + usbsys.setLatencyTimer(PORT, (byte) 5));
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				System.out.println("value : " + usbsys.setLatencyTimer("/dev/fdfsf", (byte) 5));
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
