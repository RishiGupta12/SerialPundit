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

package test57;

import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.SerialComUSBdevice;

/*
 */
public class Test57 {
	public static void main(String[] args) {
		try {
			SerialComManager scm = new SerialComManager();
			SerialComUSBdevice[] usbDevices;
			usbDevices = scm.listUSBdevicesWithInfo();
			for(int x=0; x< usbDevices.length; x++) {
				usbDevices[x].dumpDeviceInfo();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}
