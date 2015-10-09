/**
 * Author : Rishi Gupta
 * 
 * This file is part of 'serial communication manager' library.
 *
 * The 'serial communication manager' is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by the Free Software 
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * The 'serial communication manager' is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with serial communication manager. If not, see <http://www.gnu.org/licenses/>.
 */

package test59;

import com.embeddedunveiled.serial.SerialComManager;

/* Connect many USB-UART through USB HUB together and check if device is connetced or not */

public class Test59 {
	public static void main(String[] args) {
		try {
			SerialComManager scm = new SerialComManager();
			// FTDI FT232RL
			System.out.println("FT232 status : " + scm.isUSBDevConnected(0x0403, 0x6001, null));
		}catch (Exception e) {
			e.printStackTrace();
		}
		try {
			SerialComManager scm = new SerialComManager();
			// FTDI FT232RL
			System.out.println("FT232 status (with serial) : " + scm.isUSBDevConnected(0x0403, 0x6001, "A7036479"));
		}catch (Exception e) {
			e.printStackTrace();
		}

		try {
			SerialComManager scm = new SerialComManager();
			// Prolific PL2303
			System.out.println("PL2303 status : " + scm.isUSBDevConnected(0x067B, 0x2303, null));
		}catch (Exception e) {
			e.printStackTrace();
		}

		try {
			SerialComManager scm = new SerialComManager();
			// Microchip MCP2200
			System.out.println("MCP2200 status : " + scm.isUSBDevConnected(0x04D8, 0x00DF, null));
		}catch (Exception e) {
			e.printStackTrace();
		}

		try {
			SerialComManager scm = new SerialComManager();
			// Microchip MCP2200
			System.out.println("MCP2200 status (with serial): " + scm.isUSBDevConnected(0x04D8, 0x00DF, "0000980371"));
		}catch (Exception e) {
			e.printStackTrace();
		}

		try {
			SerialComManager scm = new SerialComManager();
			// Chinese CH340
			System.out.println("CH340 status : " + scm.isUSBDevConnected(0x4348, 0x5523, null));
		}catch (Exception e) {
			e.printStackTrace();
		}

		try {
			SerialComManager scm = new SerialComManager();
			// Silicon labs CP2102
			System.out.println("CP2102 status : " + scm.isUSBDevConnected(0x10C4, 0xEA60, null));
		}catch (Exception e) {
			e.printStackTrace();
		}

		try {
			SerialComManager scm = new SerialComManager();
			// Silicon labs CP2102
			System.out.println("CP2102 status (with serial): " + scm.isUSBDevConnected(0x10C4, 0xEA60, "0001"));
		}catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("\ndone");
	}
}
