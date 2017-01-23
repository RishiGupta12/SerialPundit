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

package test72;

import com.serialpundit.core.SerialComException;
import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComSystemProperty;
import com.serialpundit.hid.IHIDInputReportListener;
import com.serialpundit.hid.SerialComRawHID;

// tested with MCP2200 for HID raw mode communication
public class Test72 implements IHIDInputReportListener {

	public static SerialComRawHID scrh = null;
	static SerialComPlatform scp;
	public static int osType = 0;
	public static String PORT = null;
	public static long handle = 0;
	public static int ret = 0;
	public static byte[] inputReportBuffer = new byte[32];
	public static byte[] outputReportBuffer = new byte[16];

	// callback invoked whenever report is available
	public void onNewInputReportAvailable(int numBytes, byte[] report) {
		try {
			System.out.println("Number of bytes read : " + numBytes + ", Report : " + scrh.formatReportToHexR(report, " "));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

		try {
			scrh = new SerialComRawHID(null, null);
			scp = new SerialComPlatform(new SerialComSystemProperty());
		} catch (Exception e) {
			e.printStackTrace();
		}

		osType = scp.getOSType();
		if(osType == SerialComPlatform.OS_LINUX) {
			PORT = "/dev/hidraw1";
		}else if(osType == SerialComPlatform.OS_WINDOWS) {
			PORT = "HID\\VID_04D8&PID_00DF&MI_02\\7&33842c3f&0&0000";
		}else if(osType == SerialComPlatform.OS_MAC_OS_X) {
			PORT = null;
		}else if(osType == SerialComPlatform.OS_SOLARIS) {
			PORT = null;
		}else{
		}

		try {
			// opened handle : 5
			handle = scrh.openHidDeviceR(PORT, true);
			System.out.println("\nopened handle : " + handle);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			// writeOutputReport : 17
			outputReportBuffer[0] = (byte) 0x80;
			ret = scrh.writeOutputReportR(handle, (byte) -1, outputReportBuffer);
			System.out.println("\nwriteOutputReport : " + ret);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			Thread.sleep(500); // let device prepare response to output report we sent previously
			// MCP2200
			// readInputReportWithTimeout : 16
			// 80 00 6A 00 FF 00 FF 00 04 E1 00 88 CB 08 05 46 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
			ret = scrh.readInputReportWithTimeoutR(handle, inputReportBuffer, 100);
			System.out.println("\nreadInputReportWithTimeout : " + ret);
			System.out.println(scrh.formatReportToHexR(inputReportBuffer, " "));
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			System.out.println("\nManufacturer string: " + scrh.getManufacturerStringR(handle));
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			System.out.println("\nProduct string: " + scrh.getProductStringR(handle));
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			System.out.println("\nSerial string: " + scrh.getSerialNumberStringR(handle));
		} catch (Exception e) {
			e.printStackTrace();
		}

		// supply invalid index, we use just for testing index 0. As per standard; String Index 0 should return a list of supported languages.
		try {
			System.out.println("\nString at index 0 is : " + scrh.getIndexedStringR(handle, 0));
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			System.out.println("\nString at index 1 is : " + scrh.getIndexedStringR(handle, 1));
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			System.out.println("\nString at index 2 is : " + scrh.getIndexedStringR(handle, 2));
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			System.out.println("\nString at index 3 is : " + scrh.getIndexedStringR(handle, 3));
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			System.out.println("\nString at index 4 is : " + scrh.getIndexedStringR(handle, 4));
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
			byte[] desc = scrh.getReportDescriptorR(handle);
			System.out.println("\nnumber of bytes in descriptor : " + desc.length);
			System.out.println("descriptor in hex read from device: " + scrh.formatReportToHexR(desc, " "));
		} catch (SerialComException e1) {
			e1.printStackTrace();
		}

		try {
			byte[] phydesc = scrh.getPhysicalDescriptorR(handle);
			System.out.println("\nnumber of bytes in physical descriptor : " + phydesc.length);
			System.out.println("physical descriptor in hex read from device: " + scrh.formatReportToHexR(phydesc, " "));
		} catch (SerialComException e1) {
			e1.printStackTrace();
		}

		// send cmd to mcp2200, in response it will send result which will be stored in ring buffer
		// reading input report after flush will result in everything as 0
		try {
			outputReportBuffer[0] = (byte) 0x80;
			ret = scrh.writeOutputReportR(handle, (byte) -1, outputReportBuffer);
			System.out.println("\nwriteOutputReport : " + ret);

			Thread.sleep(1000); // let the response come and saved in buffer of operating system
			System.out.println("\nflushInputReportQueueR : " + scrh.flushInputReportQueueR(handle));

			for(int q=0; q<inputReportBuffer.length; q++) {
				inputReportBuffer[q] = 0x00;
			}
			ret = scrh.readInputReportWithTimeoutR(handle, inputReportBuffer, 100);
			System.out.println("\nreadInputReportWithTimeout : " + ret);
			System.out.println(scrh.formatReportToHexR(inputReportBuffer, " "));
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			System.out.println("\ndriver : " + scrh.findDriverServingHIDDeviceR(PORT));
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			// windows mouse
			System.out.println("\ndriver : " + scrh.findDriverServingHIDDeviceR("HID\\VID_04CA&PID_0061\\6&35F47D18&0&0000"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			Test72 tt = new Test72();
			System.out.println("\nregister listener : " + scrh.registerInputReportListener(handle, tt, inputReportBuffer));

			for(int p=0; p<10; p++) {
				outputReportBuffer[0] = (byte) 0x80;
				ret = scrh.writeOutputReportR(handle, (byte) -1, outputReportBuffer);
				System.out.println("writeOutputReport 2: " + p + " : " + ret);
				Thread.sleep(10);
			}

			System.out.println("\nunregister listener : " + scrh.unregisterInputReportListener(tt));
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			byte[] qq = new byte[10];
			qq[0] = (byte)0x80;
			System.out.println("\nwritePlatformSpecificOutputReportR : " + scrh.writePlatformSpecificOutputReportR(handle, (byte)-1, qq));
			Thread.sleep(500);
			System.out.println("\nreadPlatformSpecificInputReportR : " + scrh.readPlatformSpecificInputReportR(handle, (byte)-1, qq));
			System.out.println(scrh.formatReportToHexR(qq, " "));
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			System.out.println("\ncloseHidDevice : " + scrh.closeHidDeviceR(handle));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onNewInputReportAvailableError(Exception arg0) {
		// TODO Auto-generated method stub
		
	}
}
