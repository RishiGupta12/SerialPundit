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

package test77;

import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.vendor.SerialComSLabsCP210xRuntime;
import com.embeddedunveiled.serial.vendor.SerialComVendorLib;

public class Test77  {

	static SerialComManager scm;
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
			osType = scm.getOSType();
			if(osType == SerialComManager.OS_LINUX) { 
				libpath = "/home/r/ws-host-uart/tmp";
				vendorSuppliedLib = "libcp210xmanufacturing.so.1.0";
			}else if(osType == SerialComManager.OS_WINDOWS) {
				libpath = "D:\\zz\\srun";
				vendorSuppliedLib = "CP210xRuntime.dll";
				PORT = "COM9";
			}else if(osType == SerialComManager.OS_MAC_OS_X) {
			}else {
			}

			cprun = (SerialComSLabsCP210xRuntime) scm.getVendorLibInstance(SerialComVendorLib.VLIB_SLABS_CP210XRUNTIME , libpath, vendorSuppliedLib);
			
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
