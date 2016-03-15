/**
 * Author : Rishi Gupta
 * 
 * This file is part of 'serial communication manager' library.
 * Copyright (C) <2014-2016>  <Rishi Gupta>
 *
 * This 'serial communication manager' is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by the Free Software 
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * The 'serial communication manager' is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR 
 * A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with 'serial communication manager'.  If not, see <http://www.gnu.org/licenses/>.
 */

package p2.scalepollread;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.SerialComManager.BAUDRATE;
import com.embeddedunveiled.serial.SerialComManager.DATABITS;
import com.embeddedunveiled.serial.SerialComManager.FLOWCONTROL;
import com.embeddedunveiled.serial.SerialComManager.PARITY;
import com.embeddedunveiled.serial.SerialComManager.STOPBITS;

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

		// create tasks to be executed
		for(int x=0; x<1000; x++) {
			long comPortHandle1 = scm.openComPort("/dev/pts/3", true, true, false);
			scm.configureComPortData(comPortHandle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(comPortHandle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
			Runnable task1 = new Task(scm, comPortHandle1);		
			scheduledExecutor.scheduleAtFixedRate(task1, 0, 100, TimeUnit.MILLISECONDS);
		}

		long comPortHandle2 = scm.openComPort("/dev/pts/4", true, true, true);
		scm.configureComPortData(comPortHandle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
		scm.configureComPortControl(comPortHandle2, FLOWCONTROL.NONE, 'x', 'x', false, false);

		while(true) {
			scm.writeString(comPortHandle2, "ttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt", 0);
		}
	}
}
