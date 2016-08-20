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

package test21;

import com.serialpundit.usb.ISerialComUSBHotPlugListener;
import com.serialpundit.usb.SerialComUSB;

// event 2 indicates port removal, 1 indicates additional of port
class USBHotPlugEventHandler implements ISerialComUSBHotPlugListener {

	@Override
	public void onUSBHotPlugEvent(int event, int vid, int pid, String serial) {
		System.out.println("event " + event + " vid:" + vid + " pid:" + pid + " serial:" + serial);
	}
}

public class Test21 {
	public static void main(String[] args) {
		try {
			int handle = 0;
			SerialComUSB scusb = new SerialComUSB(null, null);
			USBHotPlugEventHandler eventhandler = new USBHotPlugEventHandler();

			System.out.println("registering");

			// ALL
			handle = scusb.registerUSBHotPlugEventListener(eventhandler, SerialComUSB.DEV_ANY, SerialComUSB.DEV_ANY, null);

			// FT232
						handle = scusb.registerUSBHotPlugEventListener(eventhandler, 0x0403, 0x6001, "A7036479");
						
						handle = scusb.registerUSBHotPlugEventListener(eventhandler, 0x0403, 0x6001, null);

			// CP2102
			//			handle = scusb.registerUSBHotPlugEventListener(eventhandler, 0x10C4, 0xEA60, "0001");

			// MCP2200
			handle = scusb.registerUSBHotPlugEventListener(eventhandler, 0x04d8, 0x00df, "0000980371");

			System.out.println("sleeping");
			Thread.sleep(50444400);
			System.out.println("unregsitering");
			scusb.unregisterUSBHotPlugEventListener(handle);

			System.out.println("unregistered");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
