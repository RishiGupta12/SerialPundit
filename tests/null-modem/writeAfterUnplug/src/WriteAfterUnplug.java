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

import com.serialpundit.serial.SerialComManager;
import com.serialpundit.serial.nullmodem.SerialComNullModem;
import com.serialpundit.serial.SerialComManager.BAUDRATE;
import com.serialpundit.serial.SerialComManager.DATABITS;
import com.serialpundit.serial.SerialComManager.FLOWCONTROL;
import com.serialpundit.serial.SerialComManager.PARITY;
import com.serialpundit.serial.SerialComManager.STOPBITS;

// Expected com.serialpundit.core.SerialComException: Input/output error

public final class WriteAfterUnplug {

	public static void main(String[] args) throws Exception {

		final SerialComManager scm = new SerialComManager();
		final SerialComNullModem scnm = scm.getSerialComNullModemInstance();
		scnm.initialize();
		
		String[] ports = scnm.createStandardLoopBackDevice(-1);
		Thread.sleep(1000);
		
		long handle = scm.openComPort(ports[0], true, true, false);
		scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_ODD, BAUDRATE.B115200, 0);
		scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', true, true);
		
		Thread.sleep(10);
		
		scnm.destroyAllCreatedVirtualDevices();
		
		try {
			scm.readString(handle);
			scm.writeString(handle, "test", 0);
		} catch (Exception e) {
		    e.printStackTrace();
		}
		
		try {
			scm.readString(handle);
		} catch (Exception e) {
		    e.printStackTrace();
		}
		
		try {
			scm.closeComPort(handle);
		} catch (Exception e) {
		    e.printStackTrace();
		}

		scnm.deinitialize();
		System.out.println("Done ");
	}
}

