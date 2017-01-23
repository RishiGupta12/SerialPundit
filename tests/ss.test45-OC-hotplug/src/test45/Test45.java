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

package test45;

import com.serialpundit.usb.ISerialComUSBHotPlugListener;
import com.serialpundit.usb.SerialComUSB;

//event 2 indicates port removal, 1 indicates additional of port
class USBHotPlugEventHandler implements ISerialComUSBHotPlugListener {

	@Override
	public void onUSBHotPlugEvent(int event, int vid, int pid, String serial) {
		System.out.println("event " + event + " vid:" + vid + " pid:" + pid + " serial:" + serial);
	}
}

public class Test45 {
	public static void main(String[] args) {

		// REGISTER SAME HANDLER
		try {
			SerialComUSB scusb = new SerialComUSB(null, null);
			USBHotPlugEventHandler eventhandler = new USBHotPlugEventHandler();
			int x = 0;
			int handle;
			int handle1;
			int handle2;
			int handle3;
			for (x=0; x<100; x++) {
				System.out.println("Iteration :" + x);
				handle  = scusb.registerUSBHotPlugEventListener(eventhandler, 0x0403, 0x6001, null);
				handle1 = scusb.registerUSBHotPlugEventListener(eventhandler, 0x10C4, 0xEA60, "0001");
				handle2 = scusb.registerUSBHotPlugEventListener(eventhandler, 0x04d8, 0x00df, "0000980371");
				handle3 = scusb.registerUSBHotPlugEventListener(eventhandler, 0x0403, 0x6001, "A7036479");
				scusb.unregisterUSBHotPlugEventListener(handle);
				scusb.unregisterUSBHotPlugEventListener(handle1);
				scusb.unregisterUSBHotPlugEventListener(handle2);
				scusb.unregisterUSBHotPlugEventListener(handle3);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Test 1 done !");

		// REGISTER/UNREGISTER MANY TIMES
		try {
			SerialComUSB scusb = new SerialComUSB(null, null);
			int x = 0;
			int handle;
			for (x=0; x<500000; x++) {
				System.out.println("Iteration :" + x);
				USBHotPlugEventHandler eventhandler = new USBHotPlugEventHandler();
				handle = scusb.registerUSBHotPlugEventListener(eventhandler, 0x0403, 0x6001, null);
				scusb.unregisterUSBHotPlugEventListener(handle);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Test 2 done !");
	}
}
