/**
 * Author : Rishi Gupta
 * 
 * This file is part of 'serial communication manager' library.
 * Copyright (C) <2014-2016>  <Rishi Gupta>
 *
 * This 'serial communication manager' is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by the Free Software 
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * The 'serial communication manager' is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR 
 * A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with 'serial communication manager'.  If not, see <http://www.gnu.org/licenses/>.
 */

package test58;

import com.embeddedunveiled.serial.SerialComManager;

/* Connect many USB-UART through USB HUB together */

public class Test58 {
	public static void main(String[] args) {
		SerialComManager scm = null;
		try {
			scm = new SerialComManager();
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			// CP2102
			String[] vcpNodes = scm.findComPortFromUSBAttributes(0x10C4, 0xEA60, "0001");
			for(int x=0; x< vcpNodes.length; x++) {
				System.out.println("cp2102 " + vcpNodes[x]);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			// FTDI FT232RL
			String[] vcpNodes = scm.findComPortFromUSBAttributes(0x0403, 0x6001, null);
			for(int x=0; x< vcpNodes.length; x++) {
				System.out.println("FT232RL null " + vcpNodes[x]);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			// FTDI FT232RL
			String[] vcpNodes = scm.findComPortFromUSBAttributes(0x0403, 0x6001, "A70362A3");
			for(int x=0; x< vcpNodes.length; x++) {
				System.out.println("FT232RL A70362A3 " + vcpNodes[x]);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			// FTDI FT232RL
			String[] vcpNodes = scm.findComPortFromUSBAttributes(0x0403, 0x6001, "A602RDCH");
			for(int x=0; x< vcpNodes.length; x++) {
				System.out.println("FT232RL A602RDCH " + vcpNodes[x]);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			// Prolific PL2303
			String[] vcpNodes = scm.findComPortFromUSBAttributes(0x067B, 0x2303, null);
			for(int x=0; x< vcpNodes.length; x++) {
				System.out.println("PL2303 " + vcpNodes[x]);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			// Microchip 18F4550 (CDC RS-232 Emulation Demo)
			String[] vcpNodes = scm.findComPortFromUSBAttributes(0x04D8, 0x000A, null);
			for(int x=0; x< vcpNodes.length; x++) {
				System.out.println("18F4550 " + vcpNodes[x]);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			// Mcp2200
			String[] vcpNodes = scm.findComPortFromUSBAttributes(0x04D8, 0x00DF, "0000980371");
			for(int x=0; x< vcpNodes.length; x++) {
				System.out.println("Mcp2200 " + vcpNodes[x]);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			// Mcp2200
			String[] vcpNodes = scm.findComPortFromUSBAttributes(0x04D8, 0x00DF, null);
			for(int x=0; x< vcpNodes.length; x++) {
				System.out.println("Mcp2200 " + vcpNodes[x]);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			// Chinese CH341
			String[] vcpNodes = scm.findComPortFromUSBAttributes(0x4348, 0x5523, null);
			for(int x=0; x< vcpNodes.length; x++) {
				System.out.println("CH341 " + vcpNodes[x]);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("\ndone");
	}
}
