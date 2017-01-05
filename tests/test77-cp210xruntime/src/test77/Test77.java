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

package test77;

import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComSystemProperty;
import com.serialpundit.serial.SerialComManager;
import com.serialpundit.serial.vendor.SerialComSLabsCP210xRuntime;
import com.serialpundit.serial.vendor.SerialComVendorLib;

public class Test77  {

	static SerialComManager scm;
	public static SerialComPlatform scp = null;
	static int osType;
	static SerialComSLabsCP210xRuntime cprun = null;
	static long handle;
	static String vendorSuppliedLib;
	static String libpath;
	static int index = 3;
	static String PORT = null;

	public static void main(String[] args) {
		try {
			scm = new SerialComManager();
			scp = new SerialComPlatform(new SerialComSystemProperty());

			osType = scp.getOSType();
			if(osType == SerialComPlatform.OS_LINUX) { 
				libpath = "/home/r/ws-host-uart/tmp";
				vendorSuppliedLib = "libcp210xmanufacturing.so.1.0";
			}else if(osType == SerialComPlatform.OS_WINDOWS) {
				libpath = "D:\\cp210xruntime\\RuntimeDLL\\x86_64";
				vendorSuppliedLib = "CP210xRuntime.dll";
				PORT = "COM9";
			}else if(osType == SerialComPlatform.OS_MAC_OS_X) {
			}else {
			}

			System.out.println(libpath);

			cprun = (SerialComSLabsCP210xRuntime) scm.getVendorLibFromFactory(SerialComVendorLib.VLIB_SLABS_CP210XRUNTIME , libpath, vendorSuppliedLib);

			handle = scm.openComPort(PORT, true, true, true);
			System.out.println("handle : " + handle);

			try {
				System.out.println("readLatch : " + cprun.readLatch(handle));
			}catch (Exception e) {
				e.printStackTrace();
			}

			try {
				System.out.println("writeLatch : " + cprun.writeLatch(handle, SerialComSLabsCP210xRuntime.CP210x_GPIO_1, SerialComSLabsCP210xRuntime.CP210x_GPIO_1));
			}catch (Exception e) {
				e.printStackTrace();
			}

			try {
				System.out.println("getPartNumber : " + cprun.getPartNumber(handle));
			}catch (Exception e) {
				e.printStackTrace();
			}

			try {
				System.out.println("getDeviceProductString : " + cprun.getDeviceProductString(handle));
			}catch (Exception e) {
				e.printStackTrace();
			}

			try {
				System.out.println("getDeviceSerialNumber : " + cprun.getDeviceSerialNumber(handle));
			}catch (Exception e) {
				e.printStackTrace();
			}


			try {
				System.out.println("getDeviceInterfaceString : " + cprun.getDeviceInterfaceString(handle));
			}catch (Exception e) {
				e.printStackTrace();
			}

			scm.closeComPort(handle);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
