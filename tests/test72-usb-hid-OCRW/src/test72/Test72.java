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

import com.embeddedunveiled.serial.SerialComException;
import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.SerialComHID;
import com.embeddedunveiled.serial.usb.SerialComUSBHID;

// tested with MCP2200
public class Test72  {

	public static SerialComManager scm = null;
	public static SerialComUSBHID scuh = null;
	public static String PORT = null;
	public static long handle = 0;
	public static int ret = 0;
	public static byte[] inputReportBuffer = new byte[32];
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
			//PORT = "/dev/usb/hiddev0";
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
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			// opened handle : 5
			handle = scuh.openHidDevice(PORT, true);
			System.out.println("\nopened handle : " + handle);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			// writeOutputReport : 17
			outputReportBuffer[0] = (byte) 0x80;
			ret = scuh.writeOutputReport(handle, (byte) -1, outputReportBuffer);
			System.out.println("\nwriteOutputReport : " + ret);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			Thread.sleep(500); // let device prepare response to output report we sent previously
			// MCP2200
			// readInputReportWithTimeout : 16
			// 80 00 6A 00 FF 00 FF 00 04 E1 00 88 CB 08 05 46 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
			ret = scuh.readInputReportWithTimeout(handle, inputReportBuffer, 100);
			System.out.println("\nreadInputReportWithTimeout : " + ret);
			System.out.println(scuh.formatReportToHex(inputReportBuffer, " "));
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			System.out.println("\nManufacturer string: " + scuh.getManufacturerString(handle));

			System.out.println("\nProduct string: " + scuh.getProductString(handle));

			System.out.println("\nSerial string: " + scuh.getSerialNumberString(handle));
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			System.out.println("\nString at index : 0 is :" + scuh.getIndexedString(handle, 0));
		} catch (Exception e) {
			e.printStackTrace();
		}

		/* For dragonrise Joystick [USB Gamepad ] from frontech with idVendor=0079, idProduct=0011 
		 * report descriptor is :- 
		 * in hex : 05 01 09 04 A1 01 A1 02 14 75 08 95 03 81 01 26 FF 00 95 02 09 30 09 31 81 02 75 01 95 04 81 01 25 01 95 0A 05 09 19 01 29 0A 81 02 95 0A 81 01 C0 C0
		 * parsed : 
			    0x05, 0x01,        // Usage Page (Generic Desktop Ctrls)
				0x09, 0x04,        // Usage (Joystick)
				0xA1, 0x01,        // Collection (Application)
				0xA1, 0x02,        //   Collection (Logical)
				0x14,              //     Logical Minimum
				0x75, 0x08,        //     Report Size (8)
				0x95, 0x03,        //     Report Count (3)
				0x81, 0x01,        //     Input (Const,Array,Abs,No Wrap,Linear,Preferred State,No Null Position)
				0x26, 0xFF, 0x00,  //     Logical Maximum (255)
				0x95, 0x02,        //     Report Count (2)
				0x09, 0x30,        //     Usage (X)
				0x09, 0x31,        //     Usage (Y)
				0x81, 0x02,        //     Input (Data,Var,Abs,No Wrap,Linear,Preferred State,No Null Position)
				0x75, 0x01,        //     Report Size (1)
				0x95, 0x04,        //     Report Count (4)
				0x81, 0x01,        //     Input (Const,Array,Abs,No Wrap,Linear,Preferred State,No Null Position)
				0x25, 0x01,        //     Logical Maximum (1)
				0x95, 0x0A,        //     Report Count (10)
				0x05, 0x09,        //     Usage Page (Button)
				0x19, 0x01,        //     Usage Minimum (0x01)
				0x29, 0x0A,        //     Usage Maximum (0x0A)
				0x81, 0x02,        //     Input (Data,Var,Abs,No Wrap,Linear,Preferred State,No Null Position)
				0x95, 0x0A,        //     Report Count (10)
				0x81, 0x01,        //     Input (Const,Array,Abs,No Wrap,Linear,Preferred State,No Null Position)
				0xC0,              //   End Collection
				0xC0,              // End Collection
				// 50 bytes
		 */

		/* For MCP2200 report descriptor is :- 
		 * in hex : 06 00 FF 09 01 A1 01 19 01 29 10 15 00 26 FF 00 75 08 95 10 81 00 19 01 29 10 91 00 C0
		 * parsed :
		 * 	0x06, 0x00, 0xFF,  // Usage Page (Vendor Defined 0xFF00)
				0x09, 0x01,        // Usage (0x01)
				0xA1, 0x01,        // Collection (Application)
				0x19, 0x01,        //   Usage Minimum (0x01)
				0x29, 0x10,        //   Usage Maximum (0x10)
				0x15, 0x00,        //   Logical Minimum (0)
				0x26, 0xFF, 0x00,  //   Logical Maximum (255)
				0x75, 0x08,        //   Report Size (8)
				0x95, 0x10,        //   Report Count (16)
				0x81, 0x00,        //   Input (Data,Array,Abs,No Wrap,Linear,Preferred State,No Null Position)
				0x19, 0x01,        //   Usage Minimum (0x01)
				0x29, 0x10,        //   Usage Maximum (0x10)
				0x91, 0x00,        //   Output (Data,Array,Abs,No Wrap,Linear,Preferred State,No Null Position,Non-volatile)
				0xC0,              // End Collection
				// 29 bytes
		 */
		try {
			byte[] desc = scuh.getReportDescriptor(handle);
			System.out.println("\nnumber of bytes in descriptor : " + desc.length);
			System.out.println("descriptor in hex read from device: " + scuh.formatReportToHex(desc, " "));
		} catch (SerialComException e1) {
			e1.printStackTrace();
		}

		try {
			System.out.println("\ncloseHidDevice : " + scuh.closeHidDevice(handle));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
