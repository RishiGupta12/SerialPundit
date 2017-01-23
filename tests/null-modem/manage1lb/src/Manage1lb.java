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

/* 
 * LOAD module to support large number of devices for this test.
 * 
 * $ insmod ./tty2com.ko max_num_vtty_dev=5000
 */

public final class Manage1lb {

	public static void main(String[] args) throws Exception {

		SerialComManager scm = new SerialComManager();
		final SerialComNullModem scnm = scm.getSerialComNullModemInstance();
		scnm.initialize();

		final int a = 2000;
		final int b = 2000;

		final ExecutorService taskGroupA = Executors.newSingleThreadExecutor();
		taskGroupA.execute(new Runnable() {
			@Override 
			public void run() {
				System.out.println("taskGroupA started ");
				try {
					for(int x=0; x < a; x++) {
						scnm.createStandardLoopBackDevice(-1);
					}
				}catch (Exception e) {
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
