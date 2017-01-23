///*
// * This file is part of SerialPundit.
// * 
// * Copyright (C) 2014-2016, Rishi Gupta. All rights reserved.
// *
// * The SerialPundit is DUAL LICENSED. It is made available under the terms of the GNU Affero 
// * General Public License (AGPL) v3.0 for non-commercial use and under the terms of a commercial 
// * license for commercial use of this software. 
// * 
// * The SerialPundit is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
// * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
// */
//
//package test71;
//
//import com.embeddedunveiled.serial.SerialComManager;
//import com.embeddedunveiled.serial.hid.SerialComHID;
//import com.embeddedunveiled.serial.usb.SerialComUSBHID;
//
//// tested with MCP2200
//public class Test71  {
//
//	public static void main(String[] args) {
//		try {
//			SerialComManager scm = new SerialComManager();
//
//			String PORT = null;
//			int osType = scm.getOSType();
//			if(osType == SerialComManager.OS_LINUX) {
//				PORT = "/dev/hidraw1";
//			}else if(osType == SerialComManager.OS_WINDOWS) {
//				PORT = "COM51";
//			}else if(osType == SerialComManager.OS_MAC_OS_X) {
//				PORT = "/dev/cu.usbserial-A70362A3";
//			}else if(osType == SerialComManager.OS_SOLARIS) {
//				PORT = null;
//			}else{
//			}
//
//			SerialComUSBHID scuh = (SerialComUSBHID) scm.getSerialComHIDInstance(SerialComHID.HID_USB, null, null);
//
//			//			long handle = scuh.openHidDevice(PORT);
//
//			// Bus 003 Device 040: ID 04d8:00df Microchip Technology, Inc.
//			//			long handle = scuh.openHidDeviceByUSBAttributes(0x04d8, 0X00DF, "0000980371", -1, -1, -1);
//			//			long handle = scuh.openHidDeviceByUSBAttributes(0x04d8, 0X00DF, "0000980371", -1, 3, -1);
//			long handle = scuh.openHidDeviceByUSBAttributes(0x04d8, 0X00DF, "0000980371", -1, -1, -1);
//			System.out.println("" + handle);
//
//			scuh.closeHidDevice(handle);
//
//			System.out.println("done");
//			//			SerialComHID sch = scm.getSerialComHIDInstance(SerialComHID.HID_GENERIC, null, null);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//}
