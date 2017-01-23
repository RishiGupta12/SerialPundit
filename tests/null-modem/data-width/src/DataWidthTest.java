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


public final class DataWidthTest {

	public static void main(String[] args) throws Exception {

		SerialComManager scm = new SerialComManager();
		final SerialComNullModem scnm = scm.getSerialComNullModemInstance();
		scnm.initialize();

		try {
			String[] ports = scnm.createStandardNullModemPair(-1, -1);
			System.out.println("PORTS:" + ports[0] + "," + ports[4]);
			Thread.sleep(700);

			long hand1 = scm.openComPort(ports[0], true, true, true);
			scm.configureComPortData(hand1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_ODD, BAUDRATE.B115200, 0);
			scm.configureComPortControl(hand1, FLOWCONTROL.NONE, 'x', 'x', true, true);

			long hand2 = scm.openComPort(ports[4], true, true, true);
			scm.configureComPortData(hand2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_ODD, BAUDRATE.B115200, 0);
			scm.configureComPortControl(hand2, FLOWCONTROL.NONE, 'x', 'x', true, true);

			byte[] data = new byte[5];
			data[0] = (byte) 4;
			data[1] = (byte) 234;
			data[2] = (byte) 14;
			data[3] = (byte) 248;
			data[4] = (byte) -1;

			scm.writeBytes(hand1, data);

			Thread.sleep(50);

			System.out.println("Sent ------------");
			for(int x=0; x < data.length; x++) {
				System.out.println(data[x]);
			}

			System.out.println("Received------------");
			byte[] datarcv = scm.readBytes(hand2);
			for(int x=0; x < datarcv.length; x++) {
				System.out.println(datarcv[x]);
			}
			
			scm.closeComPort(hand2);
			scm.closeComPort(hand1);

			scnm.destroyAllCreatedVirtualDevices();

			scnm.deinitialize();
			System.out.println("Done !");
		}catch (Exception e) {
			e.printStackTrace();
			scnm.destroyAllCreatedVirtualDevices();
		}
	}
}
