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

package example;

import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.SerialComManager.BAUDRATE;
import com.embeddedunveiled.serial.SerialComManager.DATABITS;
import com.embeddedunveiled.serial.SerialComManager.FLOWCONTROL;
import com.embeddedunveiled.serial.SerialComManager.PARITY;
import com.embeddedunveiled.serial.SerialComManager.STOPBITS;
import com.embeddedunveiled.serial.SerialComUtil;

final class DataHandler implements Runnable {

	// ~~~~ CHANGE to your port ~~~~~~~~~~~~ //
	private String PORT = "/dev/ttyUSB0";

	private long handle;
	private SerialComManager scm;
	private long context;
	private byte[] buffer;
	private Object lock;

	public DataHandler (Object lock) {
		this.lock = lock;
	}

	@Override
	public void run() {
		try {
			System.out.println("Application thread started !");
			
			scm = new SerialComManager();
			handle = scm.openComPort(PORT, true, true, true);
			scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
			scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);
			context = scm.createBlockingIOContext();

			// When a serial port is opened using this library, by default DTR and RTS line will be asserted.
			// This may make serial device think that application running on host computer is ready for 
			// communication. So, reset these lines until application has finished all initialization. Use 
			// a keyword ("start") to sync application and firmware at startup.
			scm.setDTR(handle, false);
			scm.setRTS(handle, false);

			// { Do application specific initialization here } //

			// notify serial device that host application is now ready.
			scm.setDTR(handle, true);
			scm.setRTS(handle, true);

			// when the initialization is completed, notify firmware and start actual communication.
			scm.writeString(handle, "start", 0);

			// block until serial device sends some data.
			buffer = scm.readBytesBlocking(handle, 32, context);

			// print data received from serial device on console.
			System.out.println(SerialComUtil.byteArrayToHexString(buffer, " "));

			// exit application.
			synchronized(lock) {
				lock.notify();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
}

// Entry point to this application */
public final class RTSFlowControl {

	private final static Object lock = new Object();

	public static void main(String[] args) throws InterruptedException {
		Thread t = new Thread(new DataHandler(lock));
		t.start();
		synchronized(lock) {
			lock.wait();
		}
		System.out.println("Application exited !");
	}
}
