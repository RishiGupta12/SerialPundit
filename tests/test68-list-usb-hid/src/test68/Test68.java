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

package test68;

import com.serialpundit.usb.SerialComUSB;
import com.serialpundit.usb.SerialComUSBHID;
import com.serialpundit.usb.SerialComUSBHIDdevice;

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
			SerialComUSB scu = new SerialComUSB(null, null);
			SerialComUSBHID scuh = scu.getUSBHIDTransportInstance();
			SerialComUSBHIDdevice[] usbHidDevices = scuh.listUSBHIDdevicesWithInfo(SerialComUSB.V_ALL);

			for(int x=0; x < usbHidDevices.length; x++) {
				usbHidDevices[x].dumpDeviceInfo();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

/* Linux output
 * Transport : USB
Device node : /dev/hidraw1
Vendor id : 0x04d8
Product id : 0x00df
Serial number : 0000980371
Product : MCP2200 USB Serial Port Emulator
Manufacturer : Microchip Technology Inc.
Location : /devices/pci0000:00/0000:00:14.0/usb3/3-3/3-3:1.2/0003:04D8:00DF.0011/hidraw/hidraw1
Transport : USB
Device node : /dev/hidraw0
Vendor id : 0x04ca
Product id : 0x0061
Serial number : ---
Product : USB Optical Mouse
Manufacturer : PixArt
Location : /devices/pci0000:00/0000:00:14.0/usb3/3-4/3-4:1.0/0003:04CA:0061.0012/hidraw/hidraw0

 */
