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

package test74;

import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.vendor.CP210XbaudConfigs;
import com.embeddedunveiled.serial.vendor.SerialComSLabsCP210xManufacturing;
import com.embeddedunveiled.serial.vendor.SerialComVendorLib;

public class Test74  {

	static SerialComManager scm;
	static int osType;
	static SerialComSLabsCP210xManufacturing cpman = null;
	static String PORT;
	static long handle;
	static String vendorSuppliedLib;
	static String libpath;
	static int index = 3;

	public static void main(String[] args) {
		try {
			scm = new SerialComManager();
			osType = scm.getOSType();
			if(osType == SerialComManager.OS_LINUX) { 
				PORT = "/dev/ttyUSB0";
				libpath = "/home/r/ws-host-uart/tmp";
				vendorSuppliedLib = "libcp210xmanufacturing.so.1.0";
			}else if(osType == SerialComManager.OS_WINDOWS) {
			}else if(osType == SerialComManager.OS_MAC_OS_X) {
			}else {
			}

			cpman = (SerialComSLabsCP210xManufacturing) scm.getVendorLibInstance(SerialComVendorLib.VLIB_SLABS_CP210XMANUFACTURING, 
					libpath, vendorSuppliedLib);

			handle = cpman.open(index);

			try {
				System.out.println(cpman.getNumDevices());
			}catch (Exception e) {
				System.out.println(e.getMessage());
			}

			try {
				System.out.println(cpman.getProductString(index, SerialComSLabsCP210xManufacturing.CP210x_RETURN_DESCRIPTION));
			}catch (Exception e) {
				System.out.println(e.getMessage());
			}

			try {
				System.out.println(cpman.getProductString(index, SerialComSLabsCP210xManufacturing.CP210x_RETURN_SERIAL_NUMBER));
			}catch (Exception e) {
				System.out.println(e.getMessage());
			}

			try {
				System.out.println(cpman.getProductString(index, SerialComSLabsCP210xManufacturing.CP210x_RETURN_FULL_PATH));
			}catch (Exception e) {
				System.out.println(e.getMessage());
			}

			try {
				System.out.println(cpman.getPartNumber(handle));
			}catch (Exception e) {
				System.out.println(e.getMessage());
			}

			try {
				System.out.println(cpman.getSelfPower(handle));
			}catch (Exception e) {
				System.out.println(e.getMessage());
			}

			try {
				System.out.println(cpman.getMaxPower(handle));
			}catch (Exception e) {
				System.out.println(e.getMessage());
			}

			//			byte[] mode = cpman.getDeviceMode(handle);
			//			System.out.println("mode : " + mode[0] + mode[1]);

			try {
				System.out.println(cpman.getFlushBufferConfig(handle));
			}catch (Exception e) {
				System.out.println(e.getMessage());
			}

			try {
				System.out.println(cpman.getDeviceVersion(handle));
			}catch (Exception e) {
				System.out.println(e.getMessage());
			}

			try {
				CP210XbaudConfigs[] baud = cpman.getBaudRateConfig(handle);
				for(int x=0; x<baud.length; x++) {
					baud[x].dumpBaudInfo();
				}
			}catch (Exception e) {
				System.out.println(e.getMessage());
			}


			cpman.close(handle);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
