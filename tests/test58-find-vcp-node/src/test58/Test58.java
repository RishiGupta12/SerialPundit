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

package test58;

import com.embeddedunveiled.serial.SerialComManager;

/* Connect many USB-UART through USB HUB together */

public class Test58 {
	public static void main(String[] args) {
		try {
			SerialComManager scm = new SerialComManager();
			// FTDI FT232RL
			String[] vcpNodes = scm.listComPortFromUSBAttributes(0x0403, 0x6001, "A70362A3");
			for(int x=0; x< vcpNodes.length; x++) {
				System.out.println(vcpNodes[x]);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			SerialComManager scm = new SerialComManager();
			// Prolific PL2303
			String[] vcpNodes = scm.listComPortFromUSBAttributes(0x067B, 0x2303, null);
			for(int x=0; x< vcpNodes.length; x++) {
				System.out.println(vcpNodes[x]);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			SerialComManager scm = new SerialComManager();
			// Microchip 18F4550 (CDC RS-232 Emulation Demo)
			String[] vcpNodes = scm.listComPortFromUSBAttributes(0x04D8, 0x000A, null);
			for(int x=0; x< vcpNodes.length; x++) {
				System.out.println(vcpNodes[x]);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			SerialComManager scm = new SerialComManager();
			// Chinese CH341
			String[] vcpNodes = scm.listComPortFromUSBAttributes(0x4348, 0x5523, null);
			for(int x=0; x< vcpNodes.length; x++) {
				System.out.println(vcpNodes[x]);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("\ndone");
	}
}
