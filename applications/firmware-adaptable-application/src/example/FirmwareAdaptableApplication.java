/*
 * This file is part of SerialPundit project and software.
 * 
 * Copyright (C) 2014-2016, Rishi Gupta. All rights reserved.
 *
 * The SerialPundit software is DUAL licensed. It is made available under the terms of the GNU Affero 
 * General Public License (AGPL) v3.0 for non-commercial use and under the terms of a commercial 
 * license for commercial use of this software. 
 * 
 * The SerialPundit software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package example;

import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicBoolean;

import com.embeddedunveiled.serial.SerialComException;
import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.usb.SerialComUSB;
import com.embeddedunveiled.serial.SerialComIOCTLExecutor;
import com.embeddedunveiled.serial.SerialComManager.BAUDRATE;
import com.embeddedunveiled.serial.SerialComManager.DATABITS;
import com.embeddedunveiled.serial.SerialComManager.FLOWCONTROL;
import com.embeddedunveiled.serial.SerialComManager.PARITY;
import com.embeddedunveiled.serial.SerialComManager.STOPBITS;

class CommonFunctionality implements Runnable {

	protected final SerialComManager scm;
	protected boolean turnOnLED = false;

	private final int PRODUCT_VID;
	private final int PRODUCT_PID;
	private String[] comPorts;
	private AtomicBoolean exitThread;
	private long comPortHandle = -1;
	private SerialComIOCTLExecutor ioctl;

	public CommonFunctionality(SerialComManager scm, AtomicBoolean exitThread, int PRODUCT_VID, int PRODUCT_PID) {
		this.scm = scm;
		this.exitThread = exitThread;
		this.PRODUCT_VID = PRODUCT_VID;
		this.PRODUCT_PID = PRODUCT_PID;
	}

	@Override
	public void run() {

		while(exitThread.get() == false) {
			try {
				comPorts = scm.findComPortFromUSBAttributes(PRODUCT_VID, PRODUCT_PID, null);
				if(comPorts.length > 0) {
					comPortHandle = scm.openComPort(comPorts[0], true, true, true);
					scm.configureComPortData(comPortHandle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
					scm.configureComPortControl(comPortHandle, FLOWCONTROL.NONE, 'x', 'x', false, false);

					// If the desired USB-CDC device is found and has been opened for communication
					// turn on LED to inform user that device is ready for end use case.
					if(turnOnLED == true) {
						try {
							ioctl = scm.getIOCTLExecutor(comPortHandle);
							// set GPIO.0 high.
							ioctl.ioctlSetValue(comPortHandle, 0x8001, 0x00010001);
						} catch (SerialComException e) {
							System.out.println("IOCTL status : " + e.getExceptionMsg());
						}
					}
				}
				while(exitThread.get() == false) {
					if(comPortHandle != -1) {
						scm.writeString(comPortHandle, "FirmwareTest", Charset.forName("US-ASCII"), 0);
						System.out.println("Data read : " + scm.readString(comPortHandle));
						Thread.sleep(2500);
					}
				}
			} catch (Exception e) {
				//				e.printStackTrace();
			}
		}

		if(comPortHandle != -1) {
			try {
				scm.closeComPort(comPortHandle);
			} catch (SerialComException e) {
				e.printStackTrace();
			}
		}
	}
}

// Bar code reader without LED indicator.
class OldBarCodeReader extends CommonFunctionality {

	public OldBarCodeReader(SerialComManager scm, AtomicBoolean exitThread, int PRODUCT_VID, int PRODUCT_PID) {
		super(scm, exitThread, PRODUCT_VID, PRODUCT_PID);
	}

	public void setup() {
		// do nothing.
	}
}

// Bar code reader with LED indicator.
class NewBarCodeReader extends CommonFunctionality {

	public NewBarCodeReader(SerialComManager scm, AtomicBoolean exitThread, int PRODUCT_VID, int PRODUCT_PID) {
		super(scm, exitThread, PRODUCT_VID, PRODUCT_PID);
	}

	public void setup() {
		turnOnLED = true;
	}
}

public final class FirmwareAdaptableApplication {

	/* MODIFY VID AND PID AS PER YOUR USB-UART DEVICE. This example is for CP2104. */
	private final int PRODUCT_VID = 0x10C4;
	private final int PRODUCT_PID = 0xEA60;
	/* *************************************************************************** */

	private SerialComManager scm;
	private SerialComUSB scusb;
	private NewBarCodeReader newReader;
	private OldBarCodeReader oldReader;
	private Thread workerThread;
	private String fwversion;
	private final Object lock = new Object();
	private AtomicBoolean exitThread = new AtomicBoolean(false);

	private void begin() throws Exception {

		scm = new SerialComManager();
		scusb = scm.getSerialComUSBInstance();

		if(scm.isUSBDevConnected(PRODUCT_VID, PRODUCT_PID, null)) {
			String[] fwver = scusb.getFirmwareRevisionNumber(PRODUCT_VID, PRODUCT_PID, null);
			if(fwver.length > 0) {
				fwversion = fwver[0];
				System.out.println("Found CP2104 with firmware version : " + fwversion);
			}

			if(fwversion.equals("1.00")) {
				newReader = new NewBarCodeReader(scm, exitThread, PRODUCT_VID, PRODUCT_PID);
				newReader.setup();
				workerThread = new Thread(newReader);
			}else {
				oldReader = new OldBarCodeReader(scm, exitThread, PRODUCT_VID, PRODUCT_PID);
				oldReader.setup();
				workerThread = new Thread(oldReader);
			}
			workerThread.start();

			synchronized(lock) {
				lock.wait();
			}
		}else {
			System.out.println("USB device is not connected to system !");
		}
	}

	/* Entry point to the application */
	public static void main(String[] args) throws Exception {
		FirmwareAdaptableApplication app = new FirmwareAdaptableApplication();
		app.begin();
	}
}
