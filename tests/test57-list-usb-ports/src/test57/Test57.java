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

package test57;

import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.usb.SerialComUSB;
import com.embeddedunveiled.serial.usb.SerialComUSBdevice;

/*
 */
public class Test57 {
	public static void main(String[] args) {
		try {
			SerialComManager scm = new SerialComManager();
			SerialComUSBdevice[] usbDevices;
			usbDevices = scm.listUSBdevicesWithInfo(SerialComUSB.V_ALL);
			for(int x=0; x< usbDevices.length; x++) {
				usbDevices[x].dumpDeviceInfo();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}

		//		try {
		//			SerialComManager scm = new SerialComManager();
		//			SerialComUSBdevice[] usbDevices;
		//			usbDevices = scm.listUSBdevicesWithInfo(SerialComUSB.V_FTDI);
		//			for(int x=0; x< usbDevices.length; x++) {
		//				usbDevices[x].dumpDeviceInfo();
		//			}
		//		}catch (Exception e) {
		//			e.printStackTrace();
		//		}
		//		
		//		try {
		//			SerialComManager scm = new SerialComManager();
		//			SerialComUSBdevice[] usbDevices;
		//			usbDevices = scm.listUSBdevicesWithInfo(SerialComUSB.V_PL);
		//			for(int x=0; x< usbDevices.length; x++) {
		//				usbDevices[x].dumpDeviceInfo();
		//			}
		//		}catch (Exception e) {
		//			e.printStackTrace();
		//		}
		//		
		//		try {
		//			SerialComManager scm = new SerialComManager();
		//			for(long a=0; a<50000; a++) {
		//				scm.listUSBdevicesWithInfo(SerialComUSB.V_ALL);
		//			}
		//		}catch (Exception e) {
		//			e.printStackTrace();
		//		}

		System.out.println("done");
	}
}

/*

Vendor id : 0x0403
Product id : 0x6001
Serial number : A7036479
Product : FT232R USB UART
Manufacturer : FTDI
Location : PCIROOT(0)#PCI(1400)#USBROOT(0)#USB(3)#USB(3)-Port_#0003.Hub_#0002

Vendor id : 0x04CA
Product id : 0x0061
Serial number : 5&2768E75C&0&4
Product : USB Optical Mouse
Manufacturer : (Standard system devices)
Location : PCIROOT(0)#PCI(1400)#USBROOT(0)#USB(4)-Port_#0004.Hub_#0001

Vendor id : 0x04D8
Product id : 0x00DF
Serial number : 0000980371
Product : MCP2200 USB Serial Port Emulator
Manufacturer : (Standard USB Host Controller)
Location : PCIROOT(0)#PCI(1400)#USBROOT(0)#USB(2)-Port_#0002.Hub_#0001

Vendor id : 0x067B
Product id : 0x2303
Serial number : 6&3A94452&0&2
Product : USB-Serial Controller C
Manufacturer : Prolific
Location : PCIROOT(0)#PCI(1400)#USBROOT(0)#USB(3)#USB(2)-Port_#0002.Hub_#0002

Vendor id : 0x105B
Product id : 0xE065
Serial number : 1C3E84E539E2
Product : BCM43142A0
Manufacturer : Broadcom
Location : PCIROOT(0)#PCI(1400)#USBROOT(0)#USB(7)-Port_#0007.Hub_#0001

Vendor id : 0x10C4
Product id : 0xEA60
Serial number : 0001
Product : CP2102 USB to UART Bridge Controller
Manufacturer : Silicon Laboratories
Location : PCIROOT(0)#PCI(1400)#USBROOT(0)#USB(3)#USB(1)-Port_#0001.Hub_#0002

Vendor id : 0x174F
Product id : 0x1474
Serial number : LENOVO_EASYCAMERA
Product : Lenovo EasyCamera
Manufacturer : (Standard USB Host Controller)
Location : PCIROOT(0)#PCI(1400)#USBROOT(0)#USB(1)-Port_#0001.Hub_#0001

Vendor id : 0x4348
Product id : 0x5523
Serial number : 6&3A94452&0&4
Product : USB-SER!
Manufacturer : wch.cn
Location : PCIROOT(0)#PCI(1400)#USBROOT(0)#USB(3)#USB(4)-Port_#0004.Hub_#0002

 */
