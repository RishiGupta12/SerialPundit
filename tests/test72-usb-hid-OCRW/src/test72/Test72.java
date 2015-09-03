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

package test72;

import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.SerialComManager.BAUDRATE;
import com.embeddedunveiled.serial.SerialComManager.DATABITS;
import com.embeddedunveiled.serial.SerialComManager.FLOWCONTROL;
import com.embeddedunveiled.serial.SerialComManager.PARITY;
import com.embeddedunveiled.serial.SerialComManager.STOPBITS;
import com.embeddedunveiled.serial.SerialComHID;
import com.embeddedunveiled.serial.usb.SerialComUSBHID;

// tested with MCP2200
public class Test72  {

	public static void main(String[] args) {
		try {
			SerialComManager scm = new SerialComManager();
			
			String PORT = null;
			String PORT1 = null;
			int osType = scm.getOSType();
			if(osType == SerialComManager.OS_LINUX) {
				PORT = "/dev/ttyACM0";
				PORT1 = "/dev/ttyUSB1";
			}else if(osType == SerialComManager.OS_WINDOWS) {
				PORT = "COM51";
				PORT1 = "COM52";
			}else if(osType == SerialComManager.OS_MAC_OS_X) {
				PORT = "/dev/cu.usbserial-A70362A3";
				PORT1 = "/dev/cu.usbserial-A602RDCH";
			}else if(osType == SerialComManager.OS_SOLARIS) {
				PORT = null;
				PORT1 = null;
			}else{
			}
			
			SerialComUSBHID scuh = (SerialComUSBHID) scm.getSerialComHIDInstance(SerialComHID.HID_USB, null, null);
			
			// handle : 5
			long handle = scuh.openHidDeviceByUSBAttributes(0x04D8, 0X00DF, "0000980371", -1, -1, -1);
			System.out.println("handle : " + handle);
			
			// number of bytes sent : 16
			byte[] outputReportBuffer = new byte[16];
			outputReportBuffer[0] = (byte) 0x80;
			int ret = scuh.writeOutputReport(handle, (byte) -1, outputReportBuffer);
			System.out.println("number of bytes sent : " + ret);
			
			Thread.sleep(1000);
			
			// factory default device configuration
			// number of bytes in input report : 16
			// 80 00 68 00 FF 00 FF 00 04 E1 00 89 CA 00 01 42
			// 80 00 68 00 FF 00 FF 00 04 E1 00 89 CA 00 09 46
			byte[] inputReportBuffer = new byte[16];
			ret = scuh.readInputReportWithTimeout(handle, inputReportBuffer, 16, 200);
			System.out.println("number of bytes in input report : " + ret);
			System.out.println("input report : " + scuh.formatReportToHex(inputReportBuffer));
			
			scuh.closeHidDevice(handle);
			System.out.println("\ndone !");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
