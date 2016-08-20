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

package p2.scalepollread;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.serialpundit.serial.SerialComManager;
import com.serialpundit.serial.SerialComManager.BAUDRATE;
import com.serialpundit.serial.SerialComManager.DATABITS;
import com.serialpundit.serial.SerialComManager.FLOWCONTROL;
import com.serialpundit.serial.SerialComManager.PARITY;
import com.serialpundit.serial.SerialComManager.STOPBITS;

final class Task implements Runnable {

	private final long comPortHandle;
	private final SerialComManager scm;

	public Task(SerialComManager scm, long comPortHandle) {
		this.comPortHandle = comPortHandle;
		this.scm = scm;
	}

	@Override
	public void run() {
		try {
			byte[] dataRead = scm.readBytes(comPortHandle);
			if(dataRead != null) {
				System.out.println("" + comPortHandle + " : " + new String(dataRead));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}


public final class ScaledPollRead {

	public static void main(String[] args) throws Exception {

		SerialComManager scm = new SerialComManager();

		// create service
		ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(10);
		
		String PORT="/dev/ttyUSB0";
		String PORT1="/dev/ttyUSB1";

		// create tasks to be executed
		for(int x=0; x<1000; x++) {
			long comPortHandle1 = scm.openComPort(PORT, true, true, false);
			scm.configureComPortData(comPortHandle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(comPortHandle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
			Runnable task1 = new Task(scm, comPortHandle1);		
			scheduledExecutor.scheduleAtFixedRate(task1, 0, 100, TimeUnit.MILLISECONDS);
		}

		long comPortHandle2 = scm.openComPort(PORT1, true, true, true);
		scm.configureComPortData(comPortHandle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
		scm.configureComPortControl(comPortHandle2, FLOWCONTROL.NONE, 'x', 'x', false, false);

		while(true) {
			scm.writeString(comPortHandle2, "ttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt", 0);
		}
	}
}
