/*
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

package test71;

import com.embeddedunveiled.serial.SerialComHID;
import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.SerialComUtil;
import com.embeddedunveiled.serial.usb.SerialComUSBHID;

// tested with MCP2200
public class Test71  {

	public static void main(String[] args) {
		try {
			SerialComManager scm = new SerialComManager();
			
			String PORT = null;
			int osType = scm.getOSType();
			if(osType == SerialComManager.OS_LINUX) {
				PORT = "/dev/hidraw1";
			}else if(osType == SerialComManager.OS_WINDOWS) {
				PORT = "COM51";
			}else if(osType == SerialComManager.OS_MAC_OS_X) {
				PORT = "/dev/cu.usbserial-A70362A3";
			}else if(osType == SerialComManager.OS_SOLARIS) {
				PORT = null;
			}else{
			}
			
			String[] str = "Transport_USB-VID_USB-PID_USB-serialnumber_LocationID".split("_", 5);
			for(int x=0; x<str.length; x++) {
				System.out.println(str[x]);
			}
			
			System.out.println("" + SerialComUtil.hexStrToLongNumber("00DF"));
			
			SerialComUSBHID scuh = (SerialComUSBHID) scm.getSerialComHIDInstance(SerialComHID.HID_USB, null, null);
			
//			long handle = scuh.openHidDevice(PORT);
			
			// Bus 003 Device 040: ID 04d8:00df Microchip Technology, Inc.
//			long handle = scuh.openHidDeviceByUSBAttributes(0x04d8, 0X00DF, "0000980371", -1, -1, -1);
//			long handle = scuh.openHidDeviceByUSBAttributes(0x04d8, 0X00DF, "0000980371", -1, 3, -1);
			long handle = scuh.openHidDeviceByUSBAttributes(0x04d8, 0X00DF, "0000980371", -1, -1, -1);
			System.out.println("" + handle);
			
			scuh.closeHidDevice(handle);
			
			System.out.println("done");
//			SerialComHID sch = scm.getSerialComHIDInstance(SerialComHID.HID_GENERIC, null, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
