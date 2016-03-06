/*
 * Author : Rishi Gupta
 * 
 * This file is part of 'serial communication manager' library.
 * Copyright (C) <2014-2016>  <Rishi Gupta>
 *
 * This 'serial communication manager' is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by the Free Software 
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * The 'serial communication manager' is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR 
 * A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with 'serial communication manager'.  If not, see <http://www.gnu.org/licenses/>.
 */

package test74;

import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.vendor.CP210XbaudConfigs;
import com.embeddedunveiled.serial.vendor.SerialComSLabsCP210xManufacturing;
import com.embeddedunveiled.serial.vendor.SerialComVendorLib;

public class Test74  {

	static SerialComManager scm;
	static int osType;
	static SerialComSLabsCP210xManufacturing cpman = null;
	static long handle;
	static String vendorSuppliedLib;
	static String libpath;
	static int index = 3;

	public static void main(String[] args) {
		try {
			scm = new SerialComManager();
			osType = scm.getOSType();
			if(osType == SerialComManager.OS_LINUX) { 
				libpath = "/home/r/ws-host-uart/tmp";
				vendorSuppliedLib = "libcp210xmanufacturing.so.1.0";
			}else if(osType == SerialComManager.OS_WINDOWS) {
			}else if(osType == SerialComManager.OS_MAC_OS_X) {
			}else {
			}

			cpman = (SerialComSLabsCP210xManufacturing) scm.getVendorLibInstance(SerialComVendorLib.VLIB_SLABS_CP210XMANUFACTURING, 
					libpath, vendorSuppliedLib);

			try {
				handle = cpman.open(index);
				System.out.println("\nhandle : " + handle);
			}catch (Exception e) {
				System.out.println("\nhandle : " + e.getMessage());
			}

			try {
				System.out.println("\nnumber of devices : " + cpman.getNumDevices());
			}catch (Exception e) {
				System.out.println("\n" + e.getMessage());
			}

			try {
				System.out.println("\ndescription : " + cpman.getProductString(index, SerialComSLabsCP210xManufacturing.CP210x_RETURN_DESCRIPTION));
			}catch (Exception e) {
				System.out.println("\n" + e.getMessage());
			}

			try {
				System.out.println("\nserial number : " + cpman.getProductString(index, SerialComSLabsCP210xManufacturing.CP210x_RETURN_SERIAL_NUMBER));
			}catch (Exception e) {
				System.out.println("\n" + e.getMessage());
			}

			try {
				System.out.println("\nfull path : " + cpman.getProductString(index, SerialComSLabsCP210xManufacturing.CP210x_RETURN_FULL_PATH));
			}catch (Exception e) {
				System.out.println("\n" + e.getMessage());
			}

			try {
				System.out.println("\ngetDeviceProductString : " + cpman.getDeviceProductString(handle));
			}catch (Exception e) {
				System.out.println("\n" + e.getMessage());
			}

			try {
				System.out.println("\ninterface string : " + cpman.getDeviceInterfaceString(handle, (byte)0));
			}catch (Exception e) {
				System.out.println("\ninterface string : " + e.getMessage());
			}

			try {
				System.out.println("\ngetDeviceManufacturerString : " + cpman.getDeviceManufacturerString(handle));
			}catch (Exception e) {
				System.out.println("\n" + e.getMessage());
			}

			try {
				System.out.println("\npart number : " + cpman.getPartNumber(handle));
			}catch (Exception e) {
				System.out.println("\n" + e.getMessage());
			}

			try {
				System.out.println("\ngetDeviceSerialNumber : " + cpman.getDeviceSerialNumber(handle));
			}catch (Exception e) {
				System.out.println("\n" + e.getMessage());
			}

			try {
				System.out.println("\nself power : " + cpman.getSelfPower(handle));
			}catch (Exception e) {
				System.out.println("\n" + e.getMessage());
			}

			try {
				System.out.println("\nmax power : " + cpman.getMaxPower(handle));
			}catch (Exception e) {
				System.out.println("\n" + e.getMessage());
			}

			try {
				byte[] mode = cpman.getDeviceMode(handle);
				System.out.println("\n" + "mode : " + mode[0] + mode[1]);
			}catch (Exception e) {
				System.out.println("\n" + "mode : " + e.getMessage());
			}

			try {
				System.out.println("\n" + "flush config : " +  cpman.getFlushBufferConfig(handle));
			}catch (Exception e) {
				System.out.println("\n" + "flush config : " + e.getMessage());
			}

			try {
				System.out.println("\n" + "device version : " + cpman.getDeviceVersion(handle));
			}catch (Exception e) {
				System.out.println("\n" + e.getMessage());
			}

			try {
				System.out.println("\n");
				CP210XbaudConfigs[] baud = cpman.getBaudRateConfig(handle);
				for(int x=0; x<baud.length; x++) {
					baud[x].dumpBaudInfo();
				}
			}catch (Exception e) {
				System.out.println("\n" + e.getMessage());
			}

			try {
				int[] portconfig = cpman.getPortConfig(handle);
				System.out.println("\n" + "port config : " + portconfig[0] + portconfig[1] + portconfig[2] 
						+ portconfig[3]);
			}catch (Exception e) {
				System.out.println("\n" + "port config : " + e.getMessage());
			}

			try {
				int[] dualportconfig = cpman.getDualPortConfig(handle);
				System.out.println("\n" + "dual port config : " + dualportconfig[0] + dualportconfig[1] + 
						dualportconfig[2] + dualportconfig[3] + dualportconfig[4] + dualportconfig[5]);
			}catch (Exception e) {
				System.out.println("\n" + "dual port config : " + e.getMessage());
			}

			try {
				int[] quadportconfig = cpman.getQuadPortConfig(handle);
				System.out.println("\n" + "quad port config array length : " + quadportconfig.length);
				for(int x=0; x<quadportconfig.length; x++) {
					System.out.println("\n" + "quad port config " + x + " : " + quadportconfig[x]);
				}
			}catch (Exception e) {
				System.out.println("\n" + "quad port config : " + e.getMessage());
			}

			try {
				System.out.println("\n" + "vid : " + cpman.getDeviceVid(handle));
			}catch (Exception e) {
				System.out.println("\n" + e.getMessage());
			}

			try {
				System.out.println("\n" + "pid : " + cpman.getDevicePid(handle));
			}catch (Exception e) {
				System.out.println("\n" + e.getMessage());
			}

			try {
				System.out.println("\n" + "LockValue : " + cpman.getLockValue(handle));
			}catch (Exception e) {
				System.out.println("\n" + e.getMessage());
			}

			try {
				System.out.println("\n" + "Reset : " + cpman.reset(handle));
			}catch (Exception e) {
				System.out.println("\n" + e.getMessage());
			}

			try {
				System.out.println("\n" + "Close : " + cpman.close(handle));
			}catch (Exception e) {
				System.out.println("\n" + e.getMessage());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
