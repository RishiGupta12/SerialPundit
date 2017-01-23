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

package test87;

import com.serialpundit.usb.SerialComUSB;
import com.serialpundit.usb.SerialComUSBHID;
import com.serialpundit.usb.SerialComUSBHIDdevice;
import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComSystemProperty;
import com.serialpundit.hid.IHIDInputReportListener;
import com.serialpundit.hid.SerialComRawHID;

//tested with MCP2200 for HID raw mode communication
public class Test87 implements IHIDInputReportListener {

	public static void main(String[] args) {

		SerialComRawHID scrh = null;
		SerialComPlatform scp = null;
		String PORT = null;

		try {
			try {
				scrh = new SerialComRawHID(null, null);
				scp = new SerialComPlatform(new SerialComSystemProperty());
			} catch (Exception e) {
				e.printStackTrace();
			}

			int osType = scp.getOSType();
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

			// send id to device which does not uses numbered reports
			for(int x=0; x<2; x++) {
				try {
					byte[] outputReportBuffer = new byte[16];
					long handle = scrh.openHidDeviceR(PORT, true);
					scrh.writeOutputReportR(handle, (byte) 0x02, outputReportBuffer);
					scrh.closeHidDeviceR(handle);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			System.out.println("Test 0 done !");

			try {
				for(int x=0; x<1000; x++) {
					long handle = scrh.openHidDeviceR(PORT, true);
					scrh.closeHidDeviceR(handle);
				}
				System.out.println("Test 1 done !");
			}catch (Exception e) {
				e.printStackTrace();
			}

			if(osType == SerialComPlatform.OS_LINUX) {
				try {
					for(int x=0; x<1000; x++) {
						long handle = scrh.openHidDeviceR(PORT, true);
						byte[] desc = scrh.getReportDescriptorR(handle);
						scrh.closeHidDeviceR(handle);
					}
					System.out.println("Test 2 done !");
				}catch (Exception e) {
					e.printStackTrace();
				}
			}

			try {
				for(int x=0; x<1000; x++) {
					long handle = scrh.openHidDeviceR(PORT, true);
					byte[] phydesc = scrh.getPhysicalDescriptorR(handle);
					scrh.closeHidDeviceR(handle);
				}
				System.out.println("Test 3 done !");
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				for(int x=0; x<1000; x++) {
					scrh.findDriverServingHIDDeviceR(PORT);
				}
				System.out.println("Test 4 done !");
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				byte[] inputReportBuffer = new byte[32];
				for(int x=0; x<1000; x++) {
					long handle = scrh.openHidDeviceR(PORT, true);
					scrh.readInputReportWithTimeoutR(handle, inputReportBuffer, 5);
					scrh.closeHidDeviceR(handle);
				}
				System.out.println("Test 5 done !");
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				byte[] outputReportBuffer = new byte[16];
				for(int x=0; x<1000; x++) {
					long handle = scrh.openHidDeviceR(PORT, true);
					scrh.writeOutputReportR(handle, (byte) -1, outputReportBuffer);
					scrh.closeHidDeviceR(handle);
				}
				System.out.println("Test 6 done !");
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				byte[] inputReportBuffer = new byte[32];
				byte[] outputReportBuffer = new byte[16];
				for(int x=0; x<1000; x++) {
					long handle = scrh.openHidDeviceR(PORT, true);
					outputReportBuffer[0] = (byte) 0x80;
					scrh.writeOutputReportR(handle, (byte) -1, outputReportBuffer);
					scrh.readInputReportWithTimeoutR(handle, inputReportBuffer, 5);
					scrh.closeHidDeviceR(handle);
				}
				System.out.println("Test 7 done !");
			} catch (Exception e) {
				e.printStackTrace();
			}

			if(osType == SerialComPlatform.OS_WINDOWS) {
				try {
					for(int x=0; x<1000; x++) {
						long handle = scrh.openHidDeviceR(PORT, true);
						scrh.getIndexedStringR(handle, 1);
						scrh.closeHidDeviceR(handle);
					}
					System.out.println("Test 8 done !");
				}catch (Exception e) {
					e.printStackTrace();
				}
			}

			try {
				for(int x=0; x<1000; x++) {
					long handle = scrh.openHidDeviceR(PORT, true);
					scrh.getManufacturerStringR(handle);
					scrh.closeHidDeviceR(handle);
				}
				System.out.println("Test 9 done !");
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				for(int x=0; x<1000; x++) {
					long handle = scrh.openHidDeviceR(PORT, true);
					scrh.getProductStringR(handle);
					scrh.closeHidDeviceR(handle);
				}
				System.out.println("Test 10 done !");
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				for(int x=0; x<1000; x++) {
					long handle = scrh.openHidDeviceR(PORT, true);
					scrh.getSerialNumberStringR(handle);
					scrh.closeHidDeviceR(handle);
				}
				System.out.println("Test 11 done !");
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				for(int x=0; x<1000; x++) {
					long handle = scrh.openHidDeviceR(PORT, true);
					scrh.getManufacturerStringR(handle);
					scrh.closeHidDeviceR(handle);
				}
				System.out.println("Test 12 done !");
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				Test87 listener = new Test87();
				byte[] inputReportBuffer = new byte[32];
				for(int x=0; x<1000; x++) {
					long handle = scrh.openHidDeviceR(PORT, true);
					scrh.registerInputReportListener(handle, listener, inputReportBuffer);
					scrh.unregisterInputReportListener(listener);
					scrh.closeHidDeviceR(handle);
				}
				System.out.println("Test 13 done !");
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				byte[] qq = new byte[10];
				for(int x=0; x<1000; x++) {
					scrh.formatReportToHexR(qq, " ");
				}
				System.out.println("Test 14 done !");
			} catch (Exception e) {
				e.printStackTrace();
			}

			// send incorrect number of bytes to DUT (less than expected output report buffer size)
			try {
				byte[] inputReportBuffer = new byte[32];
				byte[] outputReportBuffer = new byte[10];
				for(int x=0; x<1000; x++) {
					long handle = scrh.openHidDeviceR(PORT, true);
					outputReportBuffer[0] = (byte) 0x80;
					scrh.writeOutputReportR(handle, (byte) -1, outputReportBuffer);
					scrh.readInputReportWithTimeoutR(handle, inputReportBuffer, 5);
					scrh.closeHidDeviceR(handle);
				}
				System.out.println("Test 15 done !");
			} catch (Exception e) {
				e.printStackTrace();
			}

			// send incorrect number of bytes to DUT (more than expected output report buffer size)
			try {
				byte[] inputReportBuffer = new byte[32];
				byte[] outputReportBuffer = new byte[22];
				for(int x=0; x<1000; x++) {
					long handle = scrh.openHidDeviceR(PORT, true);
					outputReportBuffer[0] = (byte) 0x80;
					scrh.writeOutputReportR(handle, (byte) -1, outputReportBuffer);
					scrh.readInputReportWithTimeoutR(handle, inputReportBuffer, 5);
					scrh.closeHidDeviceR(handle);
				}
				System.out.println("Test 16 done !");
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onNewInputReportAvailable(int numBytes, byte[] report) {
		// do nothing
	}
}