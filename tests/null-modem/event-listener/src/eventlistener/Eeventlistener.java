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

package eventlistener;

import java.io.IOException;
import java.util.Arrays;

import com.serialpundit.serial.SerialComManager;
import com.serialpundit.serial.nullmodem.SerialComNullModem;
import com.serialpundit.serial.ISerialComEventListener;
import com.serialpundit.serial.SerialComLineEvent;
import com.serialpundit.serial.SerialComManager.BAUDRATE;
import com.serialpundit.serial.SerialComManager.DATABITS;
import com.serialpundit.serial.SerialComManager.FLOWCONTROL;
import com.serialpundit.serial.SerialComManager.PARITY;
import com.serialpundit.serial.SerialComManager.STOPBITS;

class EventListener implements ISerialComEventListener {
	@Override
	public void onNewSerialEvent(SerialComLineEvent lineEvent) {
		System.out.println("eventCTS : " + lineEvent.getCTS());
		System.out.println("eventDSR : " + lineEvent.getDSR());
	}
}

public class Eeventlistener {

	static SerialComNullModem scnm = null;

	public static void main(String[] args) throws IOException {
		try {
			SerialComManager scm = new SerialComManager();
			scnm = scm.getSerialComNullModemInstance();
			scnm.initialize();

			String[] ports = scnm.createStandardNullModemPair(-1, -1);
			Thread.sleep(100);

			// instantiate class which is will implement ISerialComEventListener interface
			EventListener eventListener = new EventListener();

			long handle = scm.openComPort(ports[0], true, true, true);
			scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);
			System.out.println("regisration status : " + scm.registerLineEventListener(handle, eventListener));

			long handle1 = scm.openComPort(ports[4], true, true, true);
			scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);

			int[] interrupts = scm.getInterruptCount(handle);
			System.out.println("1 : " + Arrays.toString(interrupts));

			Thread.sleep(100);
			scm.setDTR(handle1, false);
			Thread.sleep(100);
			scm.setRTS(handle1, false);

			interrupts = scm.getInterruptCount(handle);
			System.out.println("2 : " + Arrays.toString(interrupts));

			Thread.sleep(100);
			scm.setDTR(handle1, true);
			Thread.sleep(100);
			scm.setRTS(handle1, true);
			Thread.sleep(100);

			interrupts = scm.getInterruptCount(handle);
			System.out.println("3 : " + Arrays.toString(interrupts));

			scm.setDTR(handle1, false);
			Thread.sleep(100);
			scm.setRTS(handle1, false);
			Thread.sleep(100);

			interrupts = scm.getInterruptCount(handle);
			System.out.println("3 : " + Arrays.toString(interrupts));

			scm.setDTR(handle1, true);
			Thread.sleep(100);
			scm.setRTS(handle1, true);
			Thread.sleep(100);

			interrupts = scm.getInterruptCount(handle);
			System.out.println("4 : " + Arrays.toString(interrupts));

			scm.setDTR(handle1, false);
			Thread.sleep(100);
			scm.setRTS(handle1, false);
			Thread.sleep(100);

			interrupts = scm.getInterruptCount(handle);
			System.out.println("5 : " + Arrays.toString(interrupts));

			scm.setDTR(handle1, true);
			Thread.sleep(100);
			scm.setRTS(handle1, true);
			Thread.sleep(100);

			interrupts = scm.getInterruptCount(handle);
			System.out.println("6 : " + Arrays.toString(interrupts));

			// unregister data listener
			scm.unregisterLineEventListener(handle, eventListener);
			Thread.sleep(100);
			scm.closeComPort(handle);
			scm.closeComPort(handle1);

			scnm.destroyAllCreatedVirtualDevices();
			scnm.deinitialize();
			System.out.println("Done !");
		} catch (Exception e) {
			scnm.destroyAllCreatedVirtualDevices();
			scnm.deinitialize();
			System.out.println("Done !");
			e.printStackTrace();
		}
	}
}
