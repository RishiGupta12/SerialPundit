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

package test;

import com.serialpundit.serial.SerialComManager;
import com.serialpundit.serial.nullmodem.SerialComNullModem;
import com.serialpundit.serial.SerialComManager.BAUDRATE;
import com.serialpundit.serial.SerialComManager.DATABITS;
import com.serialpundit.serial.SerialComManager.FLOWCONTROL;
import com.serialpundit.serial.SerialComManager.PARITY;
import com.serialpundit.serial.SerialComManager.STOPBITS;


public final class FaultyCableTest {

	public static void main(String[] args) throws Exception {

		SerialComManager scm = new SerialComManager();
		final SerialComNullModem scnm = scm.getSerialComNullModemInstance();
		scnm.initialize();

		try {
			String[] ports = scnm.createStandardNullModemPair(-1, -1);
			System.out.println("PORTS:" + ports[0] + "," + ports[4]);
			Thread.sleep(700);

			long hand1 = scm.openComPort(ports[0], true, true, true);
			scm.configureComPortData(hand1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(hand1, FLOWCONTROL.NONE, 'x', 'x', false, false);

			long hand2 = scm.openComPort(ports[4], true, true, true);
			scm.configureComPortData(hand2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(hand2, FLOWCONTROL.NONE, 'x', 'x', false, false);

			byte[] data = new byte[5];
			data[0] = (byte) 4;
			data[1] = (byte) 234;
			data[2] = (byte) 14;
			data[3] = (byte) 248;
			data[4] = (byte) -1;

			scm.writeBytes(hand1, data);
			Thread.sleep(100);

			System.out.println("Sent ------------");
			for(int x=0; x < data.length; x++) {
				System.out.println(data[x]);
			}

			System.out.println("Received------------");
			byte[] datarcv = scm.readBytes(hand2);
			if(datarcv != null) {
				for(int x=0; x < datarcv.length; x++) {
					System.out.println(datarcv[x]);
				}
			}
			else {
				System.out.println("no rcv");
			}


			/* -------------------------------------------------------- */

			scnm.emulateFaultyCable(ports[0], true);

			scm.writeBytes(hand1, data);
			Thread.sleep(100);

			System.out.println("\nSent ------------");
			for(int x=0; x < data.length; x++) {
				System.out.println(data[x]);
			}

			System.out.println("Received------------");
			byte[] datarcv1 = scm.readBytes(hand2);
			if(datarcv1 != null) {
				for(int x=0; x < datarcv1.length; x++) {
					System.out.println(datarcv1[x]);
				}
			}
			else {
				System.out.println("No data received after adding faulty cable error");
			}

			/* -------------------------------------------------------- */

			scnm.emulateFaultyCable(ports[0], false);

			scm.writeBytes(hand1, data);
			Thread.sleep(100);

			System.out.println("\nSent ------------");
			for(int x=0; x < data.length; x++) {
				System.out.println(data[x]);
			}

			System.out.println("Received------------");
			byte[] datarcv2 = scm.readBytes(hand2);
			if(datarcv2 != null) {
				for(int x=0; x < datarcv2.length; x++) {
					System.out.println(datarcv2[x]);
				}
			}
			else {
				System.out.println("No data received after removing faulty cable error");
			}

			scm.closeComPort(hand2);
			scm.closeComPort(hand1);

			/* ------------------------------------------------------------- */

			hand1 = scm.openComPort(ports[0], true, true, true);
			scm.configureComPortData(hand1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_ODD, BAUDRATE.B115200, 0);
			scm.configureComPortControl(hand1, FLOWCONTROL.NONE, 'x', 'x', true, true);

			hand2 = scm.openComPort(ports[4], true, true, true);
			scm.configureComPortData(hand2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_ODD, BAUDRATE.B115200, 0);
			scm.configureComPortControl(hand2, FLOWCONTROL.NONE, 'x', 'x', true, true);

			scnm.emulateFaultyCable(ports[0], true);

			scm.writeBytes(hand1, data);
			Thread.sleep(100);

			System.out.println("\nSent ------------");
			for(int x=0; x < data.length; x++) {
				System.out.println(data[x]);
			}

			System.out.println("Received------------");
			byte[] datarcv4 = new byte[20];
			int ret = scm.readBytes(hand2, datarcv4, 0, 10, -1, null);
			for(int x=0; x < ret; x++) {
				System.out.println(datarcv4[x]);
			}

			/* -------------------------------------------------------- */

			scnm.emulateFaultyCable(ports[0], false);

			scm.writeBytes(hand1, data);
			Thread.sleep(100);

			System.out.println("\nSent ------------");
			for(int x=0; x < data.length; x++) {
				System.out.println(data[x]);
			}

			System.out.println("Received------------");
			byte[] datarcv5 = new byte[20];
			int ret1 = scm.readBytes(hand2, datarcv5, 0, 10, -1, null);
			for(int x=0; x < ret1; x++) {
				System.out.println(datarcv5[x]);
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
