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
import com.embeddedunveiled.serial.vendor.FTOpenedDeviceInfo;
import com.embeddedunveiled.serial.vendor.FTdeviceInfo;
import com.embeddedunveiled.serial.vendor.FTdevicelistInfoNode;
import com.embeddedunveiled.serial.vendor.SerialComFTDID2XX;
import com.embeddedunveiled.serial.vendor.SerialComFTDID2XX.DATABITS;
import com.embeddedunveiled.serial.vendor.SerialComFTDID2XX.FLOWCTRL;
import com.embeddedunveiled.serial.vendor.SerialComFTDID2XX.PARITY;
import com.embeddedunveiled.serial.vendor.SerialComFTDID2XX.STOPBITS;
import com.embeddedunveiled.serial.vendor.SerialComVendorLib;

public final class Test62 {

	static SerialComFTDID2XX d2xx = null;
	static long ret = 0;
	static boolean result = false;
	static long handle = 0;
	static FTdevicelistInfoNode node;
	static FTdeviceInfo[] devinfo = null;
	static int[] arrayInt = null;
	static long[] arrayLong = null;

	public static void main(String[] args) {
		try {
			SerialComManager scm = new SerialComManager();
			d2xx = (SerialComFTDID2XX) scm.getVendorLibInstance(SerialComVendorLib.VLIB_FTDI_D2XX, "/home/r/ws-host-uart/tmp", "libftd2xx.so");

//			result = d2xx.setVidPid(0x0403, 0x6001);
//			System.out.println("d2xx.setVidPid : " + result);
//
//			int[] combination = d2xx.getVidPid();
//			System.out.println("VID : " + combination[0] + ", PID : " + combination[1]);
//
			int a = d2xx.createDeviceInfoList();
			System.out.println("d2xx.createDeviceInfoList : " + a);
//
//			FTdevicelistInfoNode[] list = d2xx.getDeviceInfoList(a);
//			for(int q=0; q<list.length; q++) {
//				list[q].dumpDeviceInfo();
//				String[] aa = list[q].interpretFlags();
//				System.out.println(aa[0] + ", " + aa[1]);
//			}
//
//			FTdevicelistInfoNode node = d2xx.getDeviceInfoDetail(0);
//			node.dumpDeviceInfo();

//			devinfo = d2xx.listDevices(0, SerialComFTDID2XX.FT_LIST_BY_INDEX | SerialComFTDID2XX.FT_OPEN_BY_SERIAL_NUMBER);
//			devinfo[0].dumpDeviceInfo();
//
//			devinfo = d2xx.listDevices(0, SerialComFTDID2XX.FT_LIST_BY_INDEX | SerialComFTDID2XX.FT_OPEN_BY_DESCRIPTION);
//			devinfo[0].dumpDeviceInfo();
//			
//			devinfo = d2xx.listDevices(0, SerialComFTDID2XX.FT_LIST_BY_INDEX | SerialComFTDID2XX.FT_OPEN_BY_LOCATION);
//			if(devinfo.length == 0) {
//				System.out.println("0 length== d2xx.listDevices(0, SerialComFTDID2XX.FT_LIST_BY_INDEX | SerialComFTDID2XX.FT_OPEN_BY_LOCATION);\n");
//			}else {
//				devinfo[0].dumpDeviceInfo();
//			}
//			devinfo = d2xx.listDevices(1, SerialComFTDID2XX.FT_LIST_BY_INDEX | SerialComFTDID2XX.FT_OPEN_BY_LOCATION);
//			if(devinfo.length == 0) {
//				System.out.println("0 length== d2xx.listDevices(0, SerialComFTDID2XX.FT_LIST_BY_INDEX | SerialComFTDID2XX.FT_OPEN_BY_LOCATION);\n");
//			}else {
//				devinfo[0].dumpDeviceInfo();
//			}

//			devinfo = d2xx.listDevices(0, SerialComFTDID2XX.FT_LIST_ALL | SerialComFTDID2XX.FT_OPEN_BY_SERIAL_NUMBER);
//			devinfo[0].dumpDeviceInfo();

//			devinfo = d2xx.listDevices(0, SerialComFTDID2XX.FT_LIST_ALL | SerialComFTDID2XX.FT_OPEN_BY_DESCRIPTION);
//			devinfo[0].dumpDeviceInfo();

//			devinfo = d2xx.listDevices(0, SerialComFTDID2XX.FT_LIST_ALL | SerialComFTDID2XX.FT_OPEN_BY_LOCATION);
//			devinfo[0].dumpDeviceInfo();

			handle = d2xx.open(0);
			System.out.println("d2xx.open : " + handle);
			
//			handle = d2xx.openEx("A70362A3", 0, SerialComFTDID2XX.FT_OPEN_BY_SERIAL_NUMBER);
//			System.out.println("d2xx.openex serial : " + handle);
//			result = d2xx.close(handle);
//			System.out.println("d2xx.close : " + result);
//			
//			handle = d2xx.openEx("FT232R USB UART", 0, SerialComFTDID2XX.FT_OPEN_BY_DESCRIPTION);
//			System.out.println("d2xx.openex description : " + handle);
//			result = d2xx.close(handle);
//			System.out.println("d2xx.close : " + result);
			
//			handle = d2xx.openEx(null, 0, SerialComFTDID2XX.FT_OPEN_BY_LOCATION);
//			System.out.println("d2xx.openex location id : " + handle);
//			result = d2xx.close(handle);
//			System.out.println("d2xx.close : " + result);
			
//
//			node = d2xx.getDeviceInfoDetail(0);
//			node.dumpDeviceInfo();
//
//			byte[] buff = new byte[1000];
//			ret = d2xx.write(handle, buff, 100);
//			System.out.println("d2xx.write : " + ret);
//
//			byte[] buf = new byte[1000];
//			ret = d2xx.read(handle, buf, 100);
//			System.out.println("d2xx.read : " + ret);
//
//			result = d2xx.setBaudRate(handle, 115200);
//			System.out.println("d2xx.setBaudRate : " + result);

//			result = d2xx.setDataCharacteristics(handle, DATABITS.FT_BITS_8, STOPBITS.FT_STOP_BITS_1, PARITY.FT_PARITY_NONE);
//			System.out.println("d2xx.setDataCharacteristics : " + result);
//
//			result = d2xx.setDivisor(handle, 30000);
//			System.out.println("d2xx.setDivisor : " + result);
//
//			result = d2xx.setTimeouts(handle, 5, 5);
//			System.out.println("d2xx.setTimeouts : " + result);
//
//			result = d2xx.setFlowControl(handle, FLOWCTRL.FT_FLOW_NONE, 'x', 'x');
//			System.out.println("d2xx.setFlowControl : " + result);
//
//			result = d2xx.setDTR(handle);
//			System.out.println("d2xx.setDTR : " + result);
//
//			result = d2xx.clearDTR(handle);
//			System.out.println("d2xx.clearDTR : " + result);
//
//			result = d2xx.setRTS(handle);
//			System.out.println("d2xx.setRTS : " + result);
//
//			result = d2xx.clearRTS(handle);
//			System.out.println("d2xx.clearRTS : " + result);
//
//			ret = d2xx.getModemStatus(handle);
//			System.out.println("d2xx.getModemStatus : " + ret);
//
//			ret = d2xx.getQueueStatus(handle);
//			System.out.println("d2xx.getQueueStatus : " + ret);
//
//			FTOpenedDeviceInfo info = d2xx.getDeviceInfo(handle);
//			info.dumpDeviceInfo();
//
//			ret = d2xx.getDriverVersion(handle);
//			System.out.println("d2xx.getDriverVersion : " + ret);
//
//			ret = d2xx.getLibraryVersion();
//			System.out.println("d2xx.getLibraryVersion : " + ret);
//
//			arrayLong = d2xx.getStatus(handle);
//			System.out.println("d2xx.getStatus : " + arrayLong[0] + arrayLong[1] + arrayLong[2]);
//
//			result = d2xx.setChars(handle, 'a', 'w', 's', 'w');
//			System.out.println("d2xx.setChars : " + result);
//
//			result = d2xx.setBreakOn(handle);
//			System.out.println("d2xx.setBreakOn : " + result);
//
//			result = d2xx.setBreakOff(handle);
//			System.out.println("d2xx.setBreakOff : " + result);
//
//			result = d2xx.purge(handle, true, true);
//			System.out.println("d2xx.purge : " + result);
//
//			result = d2xx.resetDevice(handle);
//			System.out.println("d2xx.resetDevice : " + result);
//
//			result = d2xx.stopInTask(handle);
//			System.out.println("d2xx.stopInTask : " + result);
//
//			result = d2xx.restartInTask(handle);
//			System.out.println("d2xx.restartInTask : " + result);			
//
//			result = d2xx.setDeadmanTimeout(handle, 5000);
//			System.out.println("d2xx.setDeadmanTimeout : " + result);	

			// windows only
			//			ret = d2xx.getComPortNumber(handle);
			//			System.out.println("d2xx.getComPortNumber : " + ret);

			//			result = d2xx.resetPort(handle);
			//			System.out.println("d2xx.resetPort : " + result);

			//			result = d2xx.rescan();
			//			System.out.println("d2xx.rescan : " + result);

			//			result = d2xx.reload(0x0403, 0x6001);
			//			System.out.println("d2xx.reload : " + result);

			//			result = d2xx.setResetPipeRetryCount(handle, 5);
			//			System.out.println("d2xx.setResetPipeRetryCount : " + result);			


			System.out.println("\nbye");
			result = d2xx.close(handle);
			System.out.println("d2xx.close : " + result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
