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

package t2chotplug;

import com.serialpundit.serial.SerialComManager;
import com.serialpundit.serial.nullmodem.Itty2comHotPlugListener;
import com.serialpundit.serial.nullmodem.SerialComNullModem;

// event 2 indicates port removal, 1 indicates additional of port
class HotPlugEventHandler1 implements Itty2comHotPlugListener {
	@Override
	public void onTTY2COMHotPlugEvent(int arg0, String arg1) {
		System.out.println("HotPlugEventHandler1 event " + arg0 + " port:" + arg1);
	}
}

class HotPlugEventHandler2 implements Itty2comHotPlugListener {
	@Override
	public void onTTY2COMHotPlugEvent(int arg0, String arg1) {
		System.out.println("HotPlugEventHandler2 event " + arg0 + " port:" + arg1);
	}
}

public class Testt2chotplug {

	public static void main(String[] args) {
		try {
			int handle1 = 0;
			int handle2 = 0;

			final SerialComManager scm = new SerialComManager();
			final SerialComNullModem scnm = scm.getSerialComNullModemInstance();
			scnm.initialize();

			HotPlugEventHandler1 evHandler1 = new HotPlugEventHandler1();
			HotPlugEventHandler2 evHandler2 = new HotPlugEventHandler2();

			System.out.println("registering");

			// ALL
			handle1 = scnm.registerTTY2COMHotPlugEventListener(evHandler1, null);
			handle2 = scnm.registerTTY2COMHotPlugEventListener(evHandler2, "/dev/tty2com1");

			scnm.createStandardLoopBackDevice(-1);
			//			Thread.sleep(1000);

			scnm.createStandardNullModemPair(-1, -1);

			//			Thread.sleep(1000);

			scnm.destroyAllCreatedVirtualDevices();
			Thread.sleep(500);

			System.out.println("unregsitering");
			scnm.unregisterTTY2COMHotPlugEventListener(handle1);
			scnm.unregisterTTY2COMHotPlugEventListener(handle2);

			System.out.println("unregistered");
			scnm.deinitialize();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
