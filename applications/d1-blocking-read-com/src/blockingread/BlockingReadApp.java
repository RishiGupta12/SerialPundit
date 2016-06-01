/*
 * This file is part of SerialPundit project and software.
 * 
 * Copyright (C) 2014-2016, Rishi Gupta. All rights reserved.
 *
 * The SerialPundit software is DUAL licensed. It is made available under the terms of the GNU Affero 
 * General Public License (AGPL) v3.0 for non-commercial use and under the terms of a commercial 
 * license for commercial use of this software. 
 * 
 * The SerialPundit software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package blockingread;

import com.embeddedunveiled.serial.SerialComException;
import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.SerialComManager.BAUDRATE;
import com.embeddedunveiled.serial.SerialComManager.DATABITS;
import com.embeddedunveiled.serial.SerialComManager.FLOWCONTROL;
import com.embeddedunveiled.serial.SerialComManager.PARITY;
import com.embeddedunveiled.serial.SerialComManager.STOPBITS;

public final class BlockingReadApp {

	public static void main(String[] args) {

		String PORT = null;
		long handle = -1;
		long context;
		byte[] dataRead;

		// get serial communication manager instance
		SerialComManager scm;
		try {
			scm = new SerialComManager();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

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

		try {
			handle = scm.openComPort(PORT, true, true, true);
			scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
			scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);

			scm.writeString(handle, "test ", 0);

			context = scm.createBlockingIOContext();

			for(int x=0; x<5; x++) {
				dataRead = scm.readBytesBlocking(handle, 100, context);
				if(dataRead != null) {
					System.out.println("Data read : " + new String(dataRead));
				}
			}

			scm.unblockBlockingIOOperation(context);
			scm.destroyBlockingIOContext(context);

			scm.closeComPort(handle);
		} catch (SerialComException e) {
			if(handle == -1) {
				try {
					scm.closeComPort(handle);
				} catch (SerialComException e1) {
				}
			}
			e.printStackTrace();
		}
	}
}
