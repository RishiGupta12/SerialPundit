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

package example;

import javax.swing.SwingUtilities;

import ui.GraphPlotter;
import ui.ICleanUpListener;

import com.embeddedunveiled.serial.SerialComInByteStream;
import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.SerialComManager.SMODE;
import com.embeddedunveiled.serial.SerialComOutByteStream;
import com.embeddedunveiled.serial.SerialComManager.BAUDRATE;
import com.embeddedunveiled.serial.SerialComManager.DATABITS;
import com.embeddedunveiled.serial.SerialComManager.FLOWCONTROL;
import com.embeddedunveiled.serial.SerialComManager.PARITY;
import com.embeddedunveiled.serial.SerialComManager.STOPBITS;

final class CleanUp implements ICleanUpListener {

	private SerialComManager scm;
	private SerialComOutByteStream out;
	private SerialComInByteStream in;
	private long comPortHandle;

	public CleanUp(SerialComManager scm, SerialComInByteStream in, SerialComOutByteStream out, long comPortHandle) {
		this.scm = scm;
		this.in = in;
		this.out = out;
		this.comPortHandle = comPortHandle;
	}

	@Override
	public void onAppExit() {
		try {
			in.close();
			out.close();
			scm.closeComPort(comPortHandle);
			System.out.println("Clean up completed !");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

public final class ByteStreamIntegrationApp {

	private SerialComManager scm;
	private int osType;
	private SerialComOutByteStream out;
	private SerialComInByteStream in;
	private String PORT;
	private long comPortHandle;
	private CleanUp cleanup;
	private GraphPlotter plotter;

	protected void begin() {
		try {
			System.out.println("Application started !");
			
			// instantiate serial library.
			scm = new SerialComManager();

			// based on the operating system decide serial port to use.
			osType = scm.getOSType();
			if(osType == SerialComManager.OS_LINUX) {
				PORT = "/dev/ttyUSB0";
			}else if(osType == SerialComManager.OS_WINDOWS) {
				PORT = "COM51";
			}else if(osType == SerialComManager.OS_MAC_OS_X) {
				PORT = "/dev/cu.usbserial-A70362A3";
			}else{
			}

			// open serial port.
			comPortHandle = scm.openComPort(PORT, true, true, true);
			scm.configureComPortData(comPortHandle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(comPortHandle, FLOWCONTROL.NONE, 'x', 'x', false, false);

			// create input and output byte streams.
			out = scm.createOutputByteStream(comPortHandle, SMODE.BLOCKING);
			in = scm.createInputByteStream(comPortHandle, SMODE.BLOCKING);

			// prepare class that will be used when application exits.
			cleanup = new CleanUp(scm, in, out, comPortHandle);

			// setup GUI.
			plotter = new GraphPlotter(in, out, cleanup);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* Entry point to this application. */
	public static void main(String[] args) {
		// Setup GUI in event-dispatching thread for thread-safety.
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				ByteStreamIntegrationApp app = new ByteStreamIntegrationApp();
				app.begin();
			}
		});
	}
}
