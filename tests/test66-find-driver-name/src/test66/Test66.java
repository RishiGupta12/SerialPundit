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

package test66;

import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComSystemProperty;
import com.serialpundit.serial.SerialComManager;

public class Test66  {

	public static SerialComManager scm = null;
	public static String PORT = null;
	public static String PORT1 = null;
	public static int osType = 0;

	public static void main(String[] args) {
		try {
			scm = new SerialComManager();
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

			System.out.println("driver : " + scm.findDriverServingComPort(PORT));
			System.out.println("driver : " + scm.findDriverServingComPort(PORT1));
			System.out.println("driver : " + scm.findDriverServingComPort("/dev/ttyS0"));
			System.out.println("driver : " + scm.findDriverServingComPort("/dev/pts/6"));
			System.out.println("driver : " + scm.findDriverServingComPort("/dev/tty2com0"));
			System.out.println("driver : " + scm.findDriverServingComPort("/home/r/xyz1")); // symlink to ttyUSB0

			// /home/r/xyz2 -> /home/r/xyz1 -> /dev/ttyUSB0
			System.out.println("driver : " + scm.findDriverServingComPort("/home/r/xyz2")); // ultimate symlink to ttyUSB0
			System.out.println("driver : " + scm.findDriverServingComPort("/dev/pts/3"));   // present
			System.out.println("driver : " + scm.findDriverServingComPort("/dev/pts/100")); // not present
			System.out.println("driver : " + scm.findDriverServingComPort("/dev/tty2com0"));
			System.out.println("driver : " + scm.findDriverServingComPort("/dev/tty2com102"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
