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

package test59;

import com.serialpundit.usb.SerialComUSB;

/* Connect many USB-UART through USB HUB together and check if device is connetced or not */

public class Test59 {
	public static void main(String[] args) {
		try {
			SerialComUSB scusb = new SerialComUSB(null, null);
			// FTDI FT232RL
			System.out.println("FT232 status : " + scusb.isUSBDevConnected(0x0403, 0x6001, null));
		}catch (Exception e) {
			e.printStackTrace();
		}
		try {
			SerialComUSB scusb = new SerialComUSB(null, null);
			// FTDI FT232RL
			System.out.println("FT232 status (with serial) : " + scusb.isUSBDevConnected(0x0403, 0x6001, "A7036479"));
		}catch (Exception e) {
			e.printStackTrace();
		}

		try {
			SerialComUSB scusb = new SerialComUSB(null, null);
			// Prolific PL2303
			System.out.println("PL2303 status : " + scusb.isUSBDevConnected(0x067B, 0x2303, null));
		}catch (Exception e) {
			e.printStackTrace();
		}

		try {
			SerialComUSB scusb = new SerialComUSB(null, null);
			// Microchip MCP2200
			System.out.println("MCP2200 status : " + scusb.isUSBDevConnected(0x04D8, 0x00DF, null));
		}catch (Exception e) {
			e.printStackTrace();
		}

		try {
			SerialComUSB scusb = new SerialComUSB(null, null);
			// Microchip MCP2200
			System.out.println("MCP2200 status (with serial): " + scusb.isUSBDevConnected(0x04D8, 0x00DF, "0000980371"));
		}catch (Exception e) {
			e.printStackTrace();
		}

		try {
			SerialComUSB scusb = new SerialComUSB(null, null);
			// Chinese CH340
			System.out.println("CH340 status : " + scusb.isUSBDevConnected(0x4348, 0x5523, null));
		}catch (Exception e) {
			e.printStackTrace();
		}

		try {
			SerialComUSB scusb = new SerialComUSB(null, null);
			// Silicon labs CP2102
			System.out.println("CP2102 status : " + scusb.isUSBDevConnected(0x10C4, 0xEA60, null));
		}catch (Exception e) {
			e.printStackTrace();
		}

		try {
			SerialComUSB scusb = new SerialComUSB(null, null);
			// Silicon labs CP2102
			System.out.println("CP2102 status (with serial): " + scusb.isUSBDevConnected(0x10C4, 0xEA60, "0001"));
		}catch (Exception e) {
			e.printStackTrace();
		}

		/* ~~~~~~~~~~~~~~~~~~ firmware version ~~~~~~~~~~~~~~~~~` */
		System.out.println("\n _______________ \n");

		SerialComUSB scusb = null;

		try {
			scusb = new SerialComUSB(null, null);
		}catch (Exception e) {
			e.printStackTrace();
		}

		try {
			// FTDI FT232RL
			String[] aa = scusb.getFirmwareRevisionNumber(0x0403, 0x6001, null);
			for(int x=0; x<aa.length; x++) {
				System.out.println("FT232 firmware version : " + aa[x]);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}

		try {
			// MCP2200
			String[] aa = scusb.getFirmwareRevisionNumber(0x04D8, 0x00DF, null);
			for(int x=0; x<aa.length; x++) {
				System.out.println("mcp2200 firmware version : " + aa[x]);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}

		try {
			// MCP2200
			String[] aa = scusb.getFirmwareRevisionNumber(0x04D8, 0x00DF, "0000980371");
			for(int x=0; x<aa.length; x++) {
				System.out.println("mcp2200 firmware version : " + aa[x]);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}

		try {
			// cp2102
			String[] aa = scusb.getFirmwareRevisionNumber(0x10C4, 0xEA60, null);
			for(int x=0; x<aa.length; x++) {
				System.out.println("cp2102 firmware version : " + aa[x]);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}

		try {
			// cp2102
			String[] aa = scusb.getFirmwareRevisionNumber(0x10C4, 0xEA60, "0001");
			for(int x=0; x<aa.length; x++) {
				System.out.println("cp2102 firmware version : " + aa[x]);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}

		try {
			// pl2303
			String[] aa = scusb.getFirmwareRevisionNumber(0x067B, 0x2303, null);
			for(int x=0; x<aa.length; x++) {
				System.out.println("mcp2200 firmware version : " + aa[x]);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}

		try {
			// ch340
			String[] aa = scusb.getFirmwareRevisionNumber(0x4348, 0x5523, null);
			for(int x=0; x<aa.length; x++) {
				System.out.println("ch340 firmware version : " + aa[x]);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("\ndone");
	}
}
