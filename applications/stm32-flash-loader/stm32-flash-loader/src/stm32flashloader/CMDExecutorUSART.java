/*
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

package stm32flashloader;

import java.util.concurrent.atomic.AtomicBoolean;
import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.SerialComManager.BAUDRATE;
import com.embeddedunveiled.serial.SerialComManager.DATABITS;
import com.embeddedunveiled.serial.SerialComManager.FLOWCONTROL;
import com.embeddedunveiled.serial.SerialComManager.PARITY;
import com.embeddedunveiled.serial.SerialComManager.STOPBITS;

public final class CMDExecutorUSART implements Runnable {

	private final AtomicBoolean exitApplication;
	private final Object forwardlock;
	private final Object backwardlock;
	private SerialComManager scm;
	private long comPortHandle = -1;
	private String comPortName;
	private BAUDRATE baudRate;
	private PARITY parity;
	private boolean echo;
	private int timeout;
	private boolean setRTS;
	private boolean setDTR;

	public CMDExecutorUSART(AtomicBoolean exitApplication, Object forwardlock, Object backwardlock) {
		this.exitApplication = exitApplication;
		this.forwardlock = forwardlock;
		this.backwardlock = backwardlock;
	}

	// first set serial port parameters than start worker thread.
	void setComPortParameters(String comPortName, BAUDRATE baudRate, boolean echo, int timeout, PARITY parity,
			boolean setRTS, boolean setDTR) {
		this.comPortName = comPortName;
		this.baudRate = baudRate;
		this.echo = echo;
		this.timeout = timeout;
		this.parity = parity;
		this.setRTS = setRTS;
		this.setDTR = setDTR;
	}

	void setCommandToExecute() {

	}

	@Override
	public void run() {

		// open and configure serial port on host connected to STM32xxx USART port.
		try {
			scm = new SerialComManager();
			comPortHandle = scm.openComPort(comPortName, true, true, true);
			scm.configureComPortData(comPortHandle, DATABITS.DB_8, STOPBITS.SB_1, parity, baudRate, 0);
			scm.configureComPortControl(comPortHandle, FLOWCONTROL.NONE, 'x', 'x', false, false);
			scm.setDTR(comPortHandle, setDTR);
			scm.setRTS(comPortHandle, setRTS);
		} catch (Exception e1) {
			e1.printStackTrace();
			if(comPortHandle != -1) {
				try {
					scm.closeComport(comPortHandle);
				} catch (Exception e2) {
				}
			}
			return;
		}

		while(exitApplication.get() == false) {

			// wait until command to execute has been specified and we are informed.
			synchronized(lock) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}



			if(guimode == false) {
				// exit application if running in command line mode after given command has been executed.
				return;
			}
		}
	}
}
