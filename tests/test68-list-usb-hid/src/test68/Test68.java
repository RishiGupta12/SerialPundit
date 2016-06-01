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

package test68;

import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.hid.SerialComHID;
import com.embeddedunveiled.serial.hid.SerialComHIDdevice;
import com.embeddedunveiled.serial.hid.SerialComRawHID;
import com.embeddedunveiled.serial.usb.SerialComUSB;
import com.embeddedunveiled.serial.usb.SerialComUSBHID;

/*  Windows output for MCP2000

		Transport : USB
		Device node : HID\VID_0079&PID_0011\6&1CF4CDB9&0&0000
		Vendor id : 0x0079
		Product id : 0x0011
		Serial number : 5&2768E75C&0&2
		Product : USB Gamepad 
		Manufacturer : (Standard system devices)
		Location : PCIROOT(0)#PCI(1400)#USBROOT(0)#USB(2)-Port_#0002.Hub_#0001

		Transport : USB
		Device node : HID\VID_04D8&PID_00DF&MI_02\7&33842C3F&0&0000
		Vendor id : 0x04D8
		Product id : 0x00DF
		Serial number : 0000980371
		Product : MCP2200 USB Serial Port Emulator
		Manufacturer : (Standard USB Host Controller)
		Location : PCIROOT(0)#PCI(1400)#USBROOT(0)#USB(3)-Port_#0003.Hub_#0001
 */

public class Test68  {

	public static void main(String[] args) {

		try {
			SerialComManager scm = new SerialComManager();
			SerialComRawHID scrh = (SerialComRawHID) scm.getSerialComHIDInstance(SerialComHID.MODE_RAW, null, null);
			SerialComUSBHID scuh = (SerialComUSBHID) scrh.getHIDTransportInstance(SerialComHID.HID_USB);

			SerialComHIDdevice[] usbHidDevices = scuh.listUSBHIDdevicesWithInfo(SerialComUSB.V_ALL);
			for(int x=0; x < usbHidDevices.length; x++) {
				usbHidDevices[x].dumpDeviceInfo();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
