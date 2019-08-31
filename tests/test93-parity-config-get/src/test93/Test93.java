/*
 * This file is part of SerialPundit.
 *
 * Copyright (C) 2014-2018, Rishi Gupta. All rights reserved.
 *
 * The SerialPundit is DUAL LICENSED. It is made available under the terms of the GNU Affero
 * General Public License (AGPL) v3.0 for non-commercial use and under the terms of a commercial
 * license for commercial use of this software.
 *
 * The SerialPundit is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package test93;

import java.util.Arrays;

import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComSystemProperty;
import com.serialpundit.serial.SerialComManager;
import com.serialpundit.serial.SerialComManager.BAUDRATE;
import com.serialpundit.serial.SerialComManager.DATABITS;
import com.serialpundit.serial.SerialComManager.FLOWCONTROL;
import com.serialpundit.serial.SerialComManager.PARITY;
import com.serialpundit.serial.SerialComManager.STOPBITS;

public class Test93 {
	public static void main(String[] args) {
		try {
			SerialComManager scm = new SerialComManager();

			String PORT = null;
			String PORT1 = null;
			SerialComPlatform scp = new SerialComPlatform(new SerialComSystemProperty());

			int osType = scp.getOSType();
			if(osType == SerialComPlatform.OS_LINUX) {
				PORT = "/dev/ttyACM0";
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
			
			// open and configure NO parity, print values, close
			long handle = scm.openComPort(PORT, true, true, true);
			scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
			scm.configureComPortControl(handle, FLOWCONTROL.RTS_CTS, 'x', 'x', false, false);
			String[] config = scm.getCurrentConfiguration(handle);
			
			/* [0, 4, 0, -2147476304, 0, 0, 3, 28, 127, 21, 4, 1, 0, 0, 17, 19, 26, 0, 18, 15, 23, 22, 0, 9600, 9600] */
			System.out.println(Arrays.toString(config));
			
			scm.closeComPort(handle);
			
			// open and configure EVEN parity, print values, close
			long handle1 = scm.openComPort(PORT, true, true, true);
			scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_EVEN, BAUDRATE.B9600, 0);
			scm.configureComPortControl(handle1, FLOWCONTROL.RTS_CTS, 'x', 'x', true, true);
			String[] config1 = scm.getCurrentConfiguration(handle1);
			
			/* [0, 8216, 0, -2147476048, 0, 0, 3, 28, 127, 21, 4, 1, 0, 0, 17, 19, 26, 0, 18, 15, 23, 22, 0, 9600, 9600] */
			System.out.println(Arrays.toString(config1));
			
			scm.closeComPort(handle1);

			// open and configure ODD parity, print values, close
			long handle2 = scm.openComPort(PORT, true, true, true);
			scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_ODD, BAUDRATE.B9600, 0);
			scm.configureComPortControl(handle2, FLOWCONTROL.RTS_CTS, 'x', 'x', true, true);
			String[] config2 = scm.getCurrentConfiguration(handle2);
			
			/* [0, 8216, 0, -2147475536, 0, 0, 3, 28, 127, 21, 4, 1, 0, 0, 17, 19, 26, 0, 18, 15, 23, 22, 0, 9600, 9600] */
			System.out.println(Arrays.toString(config2));
			
			scm.closeComPort(handle2);
			
			/* UNIX: c_iflag, c_oflag, c_cflag, c_lflag, c_line, c_cc[0], c_cc[1], c_cc[2], c_cc[3] c_cc[4], 
			 *       c_cc[5], c_cc[6], c_cc[7], c_cc[8], c_cc[9], c_cc[10], c_cc[11], c_cc[12], c_cc[13], 
			 *       c_cc[14], c_cc[15], c_cc[16], c_ispeed and c_ospeed. */
			
			/* Windows: DCBlength, BaudRate, fBinary, fParity, fOutxCtsFlow, fOutxDsrFlow, fDtrControl, 
			 * fDsrSensitivity, fTXContinueOnXoff, fOutX, fInX, fErrorChar, fNull, fRtsControl, fAbortOnError, 
			 * fDummy2, wReserved, XonLim, XoffLim, ByteSize, Parity, StopBits, XonChar, XoffChar, ErrorChar, 
			 * StopBits, EvtChar, wReserved1. */

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
