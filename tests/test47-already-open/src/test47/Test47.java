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

package test47;

import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComSystemProperty;
import com.serialpundit.serial.SerialComManager;

// Opening an already opened port should throw exception
public final class Test47 {
	public static void main(String[] args) {
		try {
			SerialComManager scm = new SerialComManager();

			SerialComPlatform scp = new SerialComPlatform(new SerialComSystemProperty());

			String PORT = null;
			int osType = scp.getOSType();
			if(osType == SerialComPlatform.OS_LINUX) {
				PORT = "/dev/ttyUSB0";
			}else if(osType == SerialComPlatform.OS_WINDOWS) {
				PORT = "COM51";
			}else if(osType == SerialComPlatform.OS_MAC_OS_X) {
				PORT = "/dev/cu.usbserial-A70362A3";
			}else if(osType == SerialComPlatform.OS_SOLARIS) {
				PORT = null;
			}else{
			}

			long handle = 0;

			handle = scm.openComPort(PORT, true, true, true);
			handle = scm.openComPort(PORT, true, true, true);
			scm.closeComPort(handle);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
