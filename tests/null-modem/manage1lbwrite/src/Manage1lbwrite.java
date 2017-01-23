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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.serialpundit.serial.SerialComManager;
import com.serialpundit.serial.nullmodem.SerialComNullModem;
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

public final class Manage1lbwrite {

	public static void main(String[] args) throws Exception {

		final SerialComManager scm = new SerialComManager();
		final SerialComNullModem scnm = scm.getSerialComNullModemInstance();
		scnm.initialize();

		final int a = 1800;
		final int b = 999999;

		final ExecutorService taskGroupA = Executors.newSingleThreadExecutor();
		taskGroupA.execute(new Runnable() {
			@Override 
			public void run() {
				System.out.println("taskGroupA started ");

				long handle = 0;
				try {
					for(int x=0; x < a; x++) {
						String[] ports = scnm.createStandardLoopBackDevice(-1);
						System.out.println("created " + ports[0]);
						Thread.sleep(500);

						try {
							handle = scm.openComPort(ports[0], true, true, false);

							try {
								scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_ODD, BAUDRATE.B115200, 0);
								scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', true, true);
								scm.writeString(handle, "test", 0);
								try {
									scm.closeComPort(handle);
								} catch (Exception e) {
									e.printStackTrace();
								}
							} catch (Exception e1) {
								try {
									scm.closeComPort(handle);
								} catch (Exception e) {
									e.printStackTrace();
								}
								e1.printStackTrace();
							}

						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				taskGroupA.shutdown();
				System.out.println("taskGroupA thread exited ");
			}
		});

		final ExecutorService taskGroupB = Executors.newSingleThreadExecutor();
		taskGroupB.execute(new Runnable() {
			@Override 
			public void run() {
				System.out.println("taskGroupB started ");
				try {
					for(int x=0; x < b; x++) {
						scnm.destroyAllCreatedVirtualDevices();
						Thread.sleep(2);
						System.out.println("destroyed");
					}
				}catch (Exception e) {
					e.printStackTrace();
				}

				taskGroupB.shutdown();
				System.out.println("taskGroupB thread exited ");
			}
		});

		System.out.println("Wait started ");

		taskGroupA.awaitTermination(10, TimeUnit.MINUTES);
		System.out.println("taskGroupA exited ");

		taskGroupB.awaitTermination(10, TimeUnit.MINUTES);
		System.out.println("taskGroupB exited ");

		scnm.deinitialize();
		System.out.println("Done ");
	}
}
