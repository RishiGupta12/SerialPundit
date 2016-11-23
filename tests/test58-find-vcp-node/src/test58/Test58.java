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

package test58;

import com.serialpundit.usb.SerialComUSB;

/* Connect many USB-UART through USB HUB together */

public class Test58 {
	public static void main(String[] args) {
		SerialComUSB scusb = null;
		try {
			scusb = new SerialComUSB(null, null);
		}catch (Exception e) {
			e.printStackTrace();
		}

		try {
			// CP2102
			String[] vcpNodes = scusb.findComPortFromUSBAttributes(0x10C4, 0xEA60, "0001");
			for(int x=0; x< vcpNodes.length; x++) {
				System.out.println("cp2102 " + vcpNodes[x]);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}

		try {
			// FTDI FT232RL
			String[] vcpNodes = scusb.findComPortFromUSBAttributes(0x0403, 0x6001, null);
			for(int x=0; x< vcpNodes.length; x++) {
				System.out.println("FT232RL null " + vcpNodes[x]);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}

		try {
			// FTDI FT232RL
			String[] vcpNodes = scusb.findComPortFromUSBAttributes(0x0403, 0x6001, "A70362A3");
			for(int x=0; x< vcpNodes.length; x++) {
				System.out.println("FT232RL A70362A3 " + vcpNodes[x]);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}

		try {
			// FTDI FT232RL
			String[] vcpNodes = scusb.findComPortFromUSBAttributes(0x0403, 0x6001, "A602RDCH");
			for(int x=0; x< vcpNodes.length; x++) {
				System.out.println("FT232RL A602RDCH " + vcpNodes[x]);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}

		try {
			// FTDI FT2232
			String[] vcpNodes = scusb.findComPortFromUSBAttributes(0x0403, 0x6010, null);
			for(int x=0; x< vcpNodes.length; x++) {
				System.out.println("FT2232 null " + vcpNodes[x]);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}

		try {
			// FTDI FT4232RL
			String[] vcpNodes = scusb.findComPortFromUSBAttributes(0x0403, 0x6011, null);
			for(int x=0; x< vcpNodes.length; x++) {
				System.out.println("FT4232 null " + vcpNodes[x]);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}

		try {
			// Prolific PL2303
			String[] vcpNodes = scusb.findComPortFromUSBAttributes(0x067B, 0x2303, null);
			for(int x=0; x< vcpNodes.length; x++) {
				System.out.println("PL2303 " + vcpNodes[x]);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}

		try {
			// Microchip 18F4550 (CDC RS-232 Emulation Demo)
			String[] vcpNodes = scusb.findComPortFromUSBAttributes(0x04D8, 0x000A, null);
			for(int x=0; x< vcpNodes.length; x++) {
				System.out.println("18F4550 " + vcpNodes[x]);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}

		try {
			// Mcp2200
			String[] vcpNodes = scusb.findComPortFromUSBAttributes(0x04D8, 0x00DF, "0000980371");
			for(int x=0; x< vcpNodes.length; x++) {
				System.out.println("Mcp2200 " + vcpNodes[x]);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}

		try {
			// Mcp2200
			String[] vcpNodes = scusb.findComPortFromUSBAttributes(0x04D8, 0x00DF, null);
			for(int x=0; x< vcpNodes.length; x++) {
				System.out.println("Mcp2200 " + vcpNodes[x]);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}

		try {
			// Chinese CH341
			String[] vcpNodes = scusb.findComPortFromUSBAttributes(0x4348, 0x5523, null);
			for(int x=0; x< vcpNodes.length; x++) {
				System.out.println("CH341 " + vcpNodes[x]);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("\ndone");
	}
}
