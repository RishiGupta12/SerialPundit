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

package test19;

import com.embeddedunveiled.serial.SerialComManager;

public class Test19 {
	public static void main(String[] args) {		
		int x = 0;
		long handle = 0;
		for(x=0; x<5000; x++) {
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

				handle = scm.openComPort(PORT, true, true, true);
				System.out.println("open status :" + handle + " at " + "x== " + x);
				System.out.println("close status :" + scm.closeComPort(handle));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}	
	}
}
