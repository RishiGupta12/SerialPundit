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
//package test73;
//
//import com.serialpundit.usb.SerialComUSB;
//import com.serialpundit.usb.SerialComUSBHID;
//
///* 
// * Tests whether throwing exception from different threads work or not.
// * JNI have many things which depends on context and thread in use.
// */
//
//class Test extends Test73 implements Runnable {
//	@Override
//	public void run() {
//		try {
//			scuh.openHidDevice("ffffff");
//		} catch (Exception e) {
//			System.out.println("thrown from worker thread !");
//		}
//	}
//}
//
//public class Test73  {
//
//	static protected SerialComUSBHID scuh;
//
//	public static void main(String[] args) {
//		try {
//			SerialComUSB scu = new SerialComUSB(null, null);
//			scuh = scu.getUSBHIDTransportInstance();
//
//			//worker
//			Thread mThread = new Thread(new Test());
//			mThread.start();
//
//			Thread.sleep(1000);
//			//main
//			scuh.openHidDevice("ffffff");
//		} catch (Exception e) {
//			System.out.println("thrown from main thread !");
//		}
//	}
//}
