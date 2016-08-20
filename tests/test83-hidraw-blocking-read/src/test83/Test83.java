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

package test83;

import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComSystemProperty;
import com.serialpundit.hid.SerialComRawHID;

class CloseHIDDevice extends Test83 implements Runnable {
	@Override
	public void run() {
		try {
			Thread.sleep(1000); // make sure closed is called after read is blocked
			System.out.println("closing HID handle...");

			System.out.println("\nunblockBlockingHIDIOOperationR : " + scrh.unblockBlockingHIDIOOperationR(context));

			System.out.println("\ncloseHidDevice : " + scrh.closeHidDeviceR(handle));
			System.out.println("closed HID handle !");

			System.out.println("\ndestroyBlockingIOContextR : " + scrh.destroyBlockingIOContextR(context));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

// tested with MCP2200 for HID raw mode communication
public class Test83  {

	public static SerialComRawHID scrh = null;
	public static SerialComPlatform scp = null;
	public static int osType = 0;
	public static int ret = 0;
	public static String PORT = null;
	public static long handle = 0;
	public static long context = 0;
	public static byte[] inputReportBuffer = new byte[32];
	public static byte[] outputReportBuffer = new byte[16];
	private static Thread mThread = null;

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

		System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~ TEST 1 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \n");

		try {
			// opened handle : 5
			handle = scrh.openHidDeviceR(PORT, true);
			System.out.println("opened handle : " + handle);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			context = scrh.createBlockingHIDIOContextR();
		} catch (Exception e) {
			e.printStackTrace();
		}

		mThread = new Thread(new CloseHIDDevice());
		mThread.start();
		System.out.println("1- proccedding to call read which will block because of no data !");

		try {
			ret = scrh.readInputReportR(handle, inputReportBuffer, context);
			System.out.println("\nreadInputReport : " + ret);
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("main thread, in.read() returned from blocked read !");



		System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~ TEST 2 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \n");


		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		try {
			// opened handle : 5
			handle = scrh.openHidDeviceR(PORT, true);
			System.out.println("\nopened handle : " + handle);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			context = scrh.createBlockingHIDIOContextR();
		} catch (Exception e) {
			e.printStackTrace();
		}

		mThread = new Thread(new CloseHIDDevice());
		mThread.start();
		System.out.println("1- proccedding to call read which will block because of no data !");

		try {
			ret = scrh.readInputReportR(handle, inputReportBuffer, context);
			System.out.println("\nreadInputReport : " + ret);
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("main thread, in.read() returned from blocked read !");


		System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~ TEST 3 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \n");

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		try {
			// opened handle : 5
			handle = scrh.openHidDeviceR(PORT, true);
			System.out.println("opened handle : " + handle);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			context = scrh.createBlockingHIDIOContextR();
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
			ret = scrh.readInputReportR(handle, inputReportBuffer, context);
			System.out.println("\nreadInputReportWithTimeout : " + ret);
			System.out.println(scrh.formatReportToHexR(inputReportBuffer, " "));
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			System.out.println("\ndestroyBlockingIOContextR : " + scrh.destroyBlockingIOContextR(context));
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			System.out.println("\ncloseHidDevice : " + scrh.closeHidDeviceR(handle));
			System.out.println("closed HID handle !");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
