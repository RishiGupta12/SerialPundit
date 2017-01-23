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

import java.util.concurrent.Executors;

import com.serialpundit.serial.SerialComManager;
import com.serialpundit.serial.nullmodem.SerialComNullModem;
import com.serialpundit.serial.SerialComLineErrors;
import com.serialpundit.serial.SerialComManager.BAUDRATE;
import com.serialpundit.serial.SerialComManager.DATABITS;
import com.serialpundit.serial.SerialComManager.FLOWCONTROL;
import com.serialpundit.serial.SerialComManager.PARITY;
import com.serialpundit.serial.SerialComManager.STOPBITS;

/* 
 * LOAD module to support large number of devices for this test.
 * 
 * $ insmod ./tty2com.ko max_num_vtty_dev=5000
 */

public final class NullModemTest {

	public static void main(String[] args) throws Exception {

		SerialComManager scm = new SerialComManager();
		final SerialComNullModem scnm = scm.getSerialComNullModemInstance();
		scnm.initialize();
		String[] a = null;
		
		int uu = 1;
		
		scnm.destroyAllCreatedVirtualDevices();

		try {
			String[] ports = scnm.listNextAvailablePorts();
			System.out.println("before: " + ports[0] + " : " + ports[1]);

			String[] ports1 = scnm.createStandardNullModemPair(-1, -1);
			Thread.sleep(1000);

			String[] portsa = scnm.listNextAvailablePorts();
			System.out.println("after: " + portsa[0] + " : " + portsa[1]);
		}catch (Exception e) {
			e.printStackTrace();
			scnm.destroyAllCreatedVirtualDevices();
		}

		//		// create loopback, writing to it then read, don't configure terminal, it should not block
		//		String lbp1 = scnm.createStandardLoopBackDevice(-1);
		//		System.out.println("loop back dev : " + lbp1);
		//		long lbp1hand1 = scm.openComPort(lbp1, true, true, false);
		//		scm.writeString(lbp1hand1, "data", 0);
		//		System.out.println("written string data");
		//		Thread.sleep(100);
		//		System.out.println("read string : " + scm.readString(lbp1hand1));
		//		scm.closeComPort(lbp1hand1);

		// num bytes in i/o buffer
		try {
			String[] ports = scnm.createStandardNullModemPair(-1, -1);
			Thread.sleep(500);
			long hand1 = scm.openComPort(ports[0], true, true, false);
			scm.configureComPortData(hand1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_ODD, BAUDRATE.B115200, 0);
			scm.configureComPortControl(hand1, FLOWCONTROL.NONE, 'x', 'x', true, true);
			long hand2 = scm.openComPort(ports[4], true, true, false);
			scm.configureComPortData(hand2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_ODD, BAUDRATE.B115200, 0);
			scm.configureComPortControl(hand2, FLOWCONTROL.NONE, 'x', 'x', true, true);

			int[] b = scm.getByteCountInPortIOBuffer(hand1);
			System.out.println("before bytes: " + b[0] + " : " + b[1]);
			
			scm.writeString(hand2, "tewwwwwwwwwwwwwst", 0);
			Thread.sleep(10);
			
			int[] c = scm.getByteCountInPortIOBuffer(hand1);
			System.out.println("after bytes: " + c[0] + " : " + c[1]);

			scm.closeComPort(hand1);
			scm.closeComPort(hand2);
		}catch (Exception e) {
			scnm.destroyAllCreatedVirtualDevices();
			e.printStackTrace();
		}
		
		if(uu == 1) {
			return;
		}

		try {
			Executors.newSingleThreadExecutor().execute(new Runnable() {
				@Override 
				public void run() {
					try {
						for(int x=0; x<1000; x++) {
							scnm.createStandardNullModemPair(-1, -1);
							Thread.sleep(10);
						}
					}catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			Executors.newSingleThreadExecutor().execute(new Runnable() {
				@Override 
				public void run() {
					try {
						for(int x=0; x<1000; x++) {
							scnm.createStandardLoopBackDevice(-1);
							Thread.sleep(10);
						}
					}catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			Executors.newSingleThreadExecutor().execute(new Runnable() {
				@Override 
				public void run() {
					try {
						for(int x=0; x<1000; x++) {
							scnm.createStandardNullModemPair(-1, -1);
							Thread.sleep(10);
						}
					}catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			try {
				scnm.destroyAllCreatedVirtualDevices();
			}catch (Exception e) {
				e.printStackTrace();
			}

			try {
				scnm.createStandardLoopBackDevice(-1);
			}catch (Exception e) {
				e.printStackTrace();
			}

			try {
				a = scnm.createStandardLoopBackDevice(2);
			}catch (Exception e) {
				e.printStackTrace();
			}

			try {
				scnm.createStandardNullModemPair(-1, -1);
			}catch (Exception e) {
				e.printStackTrace();
			}

			try {
				scnm.createStandardNullModemPair(-1, 2);
			}catch (Exception e) {
				e.printStackTrace();
			}

			try {
				scnm.createStandardNullModemPair(7, -1);
			}catch (Exception e) {
				e.printStackTrace();
			}

			try {
				scnm.createStandardNullModemPair(9, 16);
			}catch (Exception e) {
				e.printStackTrace();
			}

			try {
				String[] abc = scnm.getLastLoopBackDeviceNode();
				System.out.println("last loopback node : " + abc[0]);
			}catch (Exception e) {
				e.printStackTrace();
			}

			try {
				String[] str = scnm.getLastNullModemPairNodes();
				System.out.println("last null modem node : " + str[0] + " <--> " + str[3]);
			}catch (Exception e) {
				e.printStackTrace();
			}

			try {
				scnm.destroyAllCreatedVirtualDevices();
			}catch (Exception e) {
				e.printStackTrace();
			}

			try {
				scnm.createCustomLoopBackDevice(25, 0, 0, false);
			}catch (Exception e) {
				e.printStackTrace();
			}

			try {
				scnm.createCustomLoopBackDevice(-1, SerialComNullModem.SP_CON_CTS, SerialComNullModem.SP_CON_DCD | SerialComNullModem.SP_CON_DSR, false);
			}catch (Exception e) {
				e.printStackTrace();
			}

			try {
				scnm.createCustomLoopBackDevice(65, 0, SerialComNullModem.SP_CON_CTS | SerialComNullModem.SP_CON_DCD | SerialComNullModem.SP_CON_DSR, false);
			}catch (Exception e) {
				e.printStackTrace();
			}

			try {
				scnm.createCustomLoopBackDevice(29, SerialComNullModem.SP_CON_CTS | SerialComNullModem.SP_CON_DCD | SerialComNullModem.SP_CON_DSR, 0, false);
			}catch (Exception e) {
				e.printStackTrace();
			}


			try {
				scnm.destroyAllCreatedVirtualDevices();
			}catch (Exception e) {
				e.printStackTrace();
			}

			try {
				scnm.createCustomNullModemPair(-1, SerialComNullModem.SP_CON_CTS, SerialComNullModem.SP_CON_DCD | SerialComNullModem.SP_CON_DSR, 
						false, -1, SerialComNullModem.SP_CON_CTS, SerialComNullModem.SP_CON_DCD | SerialComNullModem.SP_CON_DSR, false);
			}catch (Exception e) {
				e.printStackTrace();
			}

			try {
				scnm.destroyAllCreatedVirtualDevices();
			}catch (Exception e) {
				e.printStackTrace();
			}

			/********* Final clean up (Release operating system specific resources held by null modem class) *********/
			Thread.sleep(15000); // run after executor threads have created all the ports
			scnm.destroyAllCreatedVirtualDevices();
			scnm.deinitialize();
			System.out.println("Done !");
			System.exit(0);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}
