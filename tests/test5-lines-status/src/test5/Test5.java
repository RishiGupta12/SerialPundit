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

package test5;

import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComSystemProperty;
import com.serialpundit.serial.SerialComManager;
import com.serialpundit.serial.SerialComManager.BAUDRATE;
import com.serialpundit.serial.SerialComManager.DATABITS;
import com.serialpundit.serial.SerialComManager.FLOWCONTROL;
import com.serialpundit.serial.SerialComManager.PARITY;
import com.serialpundit.serial.SerialComManager.STOPBITS;

public class Test5 {
	public static void main(String[] args) {
		try {
			SerialComManager scm = new SerialComManager();

			String PORT = null;
			String PORT1 = null;
			SerialComPlatform scp = new SerialComPlatform(new SerialComSystemProperty());

			int osType = scp.getOSType();
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

			long handle = scm.openComPort(PORT, true, true, true);
			scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);
			long handle1 = scm.openComPort(PORT1, true, true, true);
			scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);

			// reset everything and read initial state
			scm.setRTS(handle, false);
			scm.setDTR(handle, false);
			scm.setRTS(handle1, false);
			scm.setDTR(handle1, false);
			int[] state = scm.getLinesStatus(handle);
			System.out.println("CTS state = " + state[0]);
			System.out.println("DSR state = " + state[1]);
			System.out.println("CD state = " +  state[2]);
			System.out.println("RI state = " +  state[3] + "\n");
			state = scm.getLinesStatus(handle1);
			System.out.println("CTS state = " + state[0]);
			System.out.println("DSR state = " + state[1]);
			System.out.println("CD state = " +  state[2]);
			System.out.println("RI state = " +  state[3] + "\n");

			// test set RTS/DTR on 1st handle and check state on other end
			scm.setRTS(handle, true);
			scm.setDTR(handle, true);
			state = scm.getLinesStatus(handle1);
			System.out.println("CTS state = " + state[0]);
			System.out.println("DSR state = " + state[1]);
			System.out.println("CD state = " +  state[2]);
			System.out.println("RI state = " +  state[3] + "\n");

			// test set RTS/DTR on 1st handle and check state on other end
			scm.setRTS(handle1, true);
			scm.setDTR(handle1, true);
			state = scm.getLinesStatus(handle);
			System.out.println("CTS state = " + state[0]);
			System.out.println("DSR state = " + state[1]);
			System.out.println("CD state = " +  state[2]);
			System.out.println("RI state = " +  state[3] + "\n");

			// reset everything again and read state change
			scm.setRTS(handle, false);
			scm.setDTR(handle, false);
			scm.setRTS(handle1, false);
			scm.setDTR(handle1, false);
			state = scm.getLinesStatus(handle);
			System.out.println("CTS state = " + state[0]);
			System.out.println("DSR state = " + state[1]);
			System.out.println("CD state = " +  state[2]);
			System.out.println("RI state = " +  state[3] + "\n");
			state = scm.getLinesStatus(handle1);
			System.out.println("CTS state = " + state[0]);
			System.out.println("DSR state = " + state[1]);
			System.out.println("CD state = " +  state[2]);
			System.out.println("RI state = " +  state[3] + "\n");

			//set everything again and read state change
			scm.setRTS(handle, true);
			scm.setDTR(handle, true);
			scm.setRTS(handle1, true);
			scm.setDTR(handle1, true);
			state = scm.getLinesStatus(handle);
			System.out.println("CTS state = " + state[0]);
			System.out.println("DSR state = " + state[1]);
			System.out.println("CD state = " +  state[2]);
			System.out.println("RI state = " +  state[3] + "\n");
			state = scm.getLinesStatus(handle1);
			System.out.println("CTS state = " + state[0]);
			System.out.println("DSR state = " + state[1]);
			System.out.println("CD state = " +  state[2]);
			System.out.println("RI state = " +  state[3] + "\n");

			// set RTS but unset DTR
			scm.setRTS(handle, true);
			scm.setDTR(handle, false);
			state = scm.getLinesStatus(handle1);
			System.out.println("CTS state = " + state[0]);
			System.out.println("DSR state = " + state[1]);
			System.out.println("CD state = " +  state[2]);
			System.out.println("RI state = " +  state[3] + "\n");
			scm.setRTS(handle1, true);
			scm.setDTR(handle1, false);
			state = scm.getLinesStatus(handle);
			System.out.println("CTS state = " + state[0]);
			System.out.println("DSR state = " + state[1]);
			System.out.println("CD state = " +  state[2]);
			System.out.println("RI state = " +  state[3] + "\n");

			// set DTR but unset RTS
			scm.setRTS(handle, false);
			scm.setDTR(handle, true);
			state = scm.getLinesStatus(handle1);
			System.out.println("CTS state = " + state[0]);
			System.out.println("DSR state = " + state[1]);
			System.out.println("CD state = " +  state[2]);
			System.out.println("RI state = " +  state[3] + "\n");
			scm.setRTS(handle1, false);
			scm.setDTR(handle1, true);
			state = scm.getLinesStatus(handle);
			System.out.println("CTS state = " + state[0]);
			System.out.println("DSR state = " + state[1]);
			System.out.println("CD state = " +  state[2]);
			System.out.println("RI state = " +  state[3] + "\n");

			scm.closeComPort(handle);
			scm.closeComPort(handle1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
