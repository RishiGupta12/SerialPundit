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

package test68;

import com.embeddedunveiled.serial.SerialComHID;
import com.embeddedunveiled.serial.SerialComHIDdevice;
import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.usb.SerialComUSB;
import com.embeddedunveiled.serial.usb.SerialComUSBHID;

public class Test68  {

	public static SerialComManager scm = null;
	public static SerialComUSBHID scuh = null;
	public static String PORT = null;
	public static long handle = 0;
	public static int ret = 0;
	public static byte[] inputReportBuffer = new byte[64];
	public static byte[] outputReportBuffer = new byte[16];

	public static void main(String[] args) {

		try {
			scm = new SerialComManager();
			scuh = (SerialComUSBHID) scm.getSerialComHIDInstance(SerialComHID.HID_USB, null, null);
		} catch (Exception e) {
			e.printStackTrace();
		}

		int osType = scm.getOSType();
		if(osType == SerialComManager.OS_LINUX) {
			PORT = "/dev/hidraw1";
		}else if(osType == SerialComManager.OS_WINDOWS) {
			PORT = "HID\\VID_04D8&PID_00DF&MI_02\\7&33842c3f&0&0000";
		}else if(osType == SerialComManager.OS_MAC_OS_X) {
			PORT = null;
		}else if(osType == SerialComManager.OS_SOLARIS) {
			PORT = null;
		}else{
		}

		try {
			scm = new SerialComManager();
			scuh = (SerialComUSBHID) scm.getSerialComHIDInstance(SerialComHID.HID_USB, null, null);
			SerialComHIDdevice[] usbHidDevices = scuh.listUSBHIDdevicesWithInfo(SerialComUSB.V_ALL);
			for(int x=0; x < usbHidDevices.length; x++) {
				usbHidDevices[x].dumpDeviceInfo();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			handle = scuh.openHidDevice(PORT);
			System.out.println("\nopened handle : " + handle);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			outputReportBuffer[0] = (byte) 0x80;
			ret = scuh.writeOutputReport(handle, (byte) -1, outputReportBuffer);
			System.out.println("\nwriteOutputReport : " + ret);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			Thread.sleep(1000);
			ret = scuh.readInputReportWithTimeout(handle, inputReportBuffer, inputReportBuffer.length, 100);
			System.out.println("\nreadInputReportWithTimeout : " + ret);
			for(int q=0; q<ret; q++) {
				System.out.println(inputReportBuffer[q]);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			System.out.println("\ncloseHidDevice : " + scuh.closeHidDevice(handle));
		} catch (Exception e) {
			e.printStackTrace();
		}

		//		try {
		//			scm = new SerialComManager();
		//			SerialComHID sch = scm.getSerialComHIDInstance(SerialComHID.HID_GENERIC, null, null);
		//			SerialComHIDdevice[] hidDevices = sch.listHIDdevicesWithInfo();
		//			for(int x=0; x< hidDevices.length; x++) {
		//				hidDevices[x].dumpDeviceInfo();
		//			}
		//		} catch (Exception e) {
		//			e.printStackTrace();
		//		}
	}
}