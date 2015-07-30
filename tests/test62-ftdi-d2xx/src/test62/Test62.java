/**
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

package test62;

import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.vendor.FTdevicelistInfoNode;
import com.embeddedunveiled.serial.vendor.SerialComFTDID2XX;
import com.embeddedunveiled.serial.vendor.SerialComVendorLib;

public final class Test62 {
	
	static SerialComFTDID2XX d2xx = null;
	static int ret = 0;
	static boolean result = false;
	
	public static void main(String[] args) {
		try {
			SerialComManager scm = new SerialComManager();
			d2xx = (SerialComFTDID2XX) scm.getVendorLibInstance(SerialComVendorLib.VLIB_FTDI_D2XX, "/home/r/ws-host-uart/tmp", "libftd2xx.so");
			
			result = d2xx.setVidPid(0x0403, 0x6001);
			System.out.println("d2xx.setVidPid : " + result);
			
			int[] combination = d2xx.getVidPid();
			System.out.println("VID : " + combination[0] + ", PID : " + combination[1]);
			
			int a = d2xx.createDeviceInfoList();
			System.out.println("d2xx.createDeviceInfoList : " + a);
			
			FTdevicelistInfoNode[] list = d2xx.getDeviceInfoList(a);
			for(int q=0; q<list.length; q++) {
				list[q].dumpDeviceInfo();
				String[] aa = list[q].interpretFlags();
				System.out.println(aa[0] + ", " + aa[1]);
			}
			
			FTdevicelistInfoNode node = d2xx.getDeviceInfoDetail(0);
			node.dumpDeviceInfo();
			
			long handle = d2xx.open(0);
			System.out.println("open : " + handle);
			
			byte[] buff = new byte[1000];
			int ret = d2xx.write(handle, buff, 100);
			System.out.println("d2xx.write : " + ret);
			
			byte[] buf = new byte[1000];
			ret = d2xx.read(handle, buf, 100);
			System.out.println("d2xx.read : " + ret);
			
			result = d2xx.setBaudRate(handle, 115200);
			System.out.println("d2xx.setBaudRate : " + result);
			
			result = d2xx.close(handle);
			System.out.println("d2xx.close : " + result);
			
			System.out.println("\nbye");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
