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

package test62;

import com.serialpundit.serial.SerialComManager;
import com.serialpundit.serial.vendor.FTOpenedDeviceInfo;
import com.serialpundit.serial.vendor.FTdeviceInfo;
import com.serialpundit.serial.vendor.FTdevicelistInfoNode;
import com.serialpundit.serial.vendor.SerialComFTDID2XX;
import com.serialpundit.serial.vendor.SerialComFTDID2XX.DATABITS;
import com.serialpundit.serial.vendor.SerialComFTDID2XX.FLOWCTRL;
import com.serialpundit.serial.vendor.SerialComFTDID2XX.PARITY;
import com.serialpundit.serial.vendor.SerialComFTDID2XX.STOPBITS;
import com.serialpundit.serial.vendor.SerialComVendorLib;

/* ACTIVATE correct udev rule before executing these tests. 
 * OR tools-and-utilities/udev-ftdi-unload-vcp-driver.sh */
public final class Test62 {

	static SerialComFTDID2XX d2xx = null;
	static long ret = 0;
	static boolean result = false;
	static long handle = 0;
	static long handle1 = 0;
	static FTdevicelistInfoNode node;
	static FTdeviceInfo[] devinfo = null;
	static int[] arrayInt = null;
	static long[] arrayLong = null;

	public static void main(String[] args) {
		try {
			SerialComManager scm = new SerialComManager();
			d2xx = (SerialComFTDID2XX) scm.getVendorLibFromFactory(SerialComVendorLib.VLIB_FTDI_D2XX, "/home/r/ws-host-uart/tmp", "libftd2xx.so");

			/* ********************* D2XX Classic Functions ******************** */

			try {
				result = d2xx.setVidPid(0x0403, 0x6001);
				System.out.println("d2xx.setVidPid : " + result);
			}catch(Exception e) {
				e.printStackTrace();
			}

			try {
				int[] combination = d2xx.getVidPid();
				System.out.println("\nVID : " + combination[0] + ", PID : " + combination[1]);
			}catch(Exception e) {
				e.printStackTrace();
			}

			try {
				int a = d2xx.createDeviceInfoList();
				System.out.println("\nd2xx.createDeviceInfoList : " + a);

				FTdevicelistInfoNode[] list = d2xx.getDeviceInfoList(a);
				for(int q=0; q<list.length; q++) {
					list[q].dumpDeviceInfo();
					String[] aa = list[q].interpretFlags();
					System.out.println(aa[0] + ", " + aa[1]);
					System.out.println("\n");
				}
			}catch(Exception e) {
				e.printStackTrace();
			}

			try {
				System.out.println("\n");
				FTdevicelistInfoNode node = d2xx.getDeviceInfoDetail(0);
				node.dumpDeviceInfo();
			}catch(Exception e) {
				e.printStackTrace();
			}

			try {
				System.out.println("\n");
				devinfo = d2xx.listDevices(0, SerialComFTDID2XX.FT_LIST_BY_INDEX | SerialComFTDID2XX.FT_OPEN_BY_DESCRIPTION);
				devinfo[0].dumpDeviceInfo();
			}catch(Exception e) {
				e.printStackTrace();
			}

			try {
				System.out.println("\n");
				devinfo = d2xx.listDevices(0, SerialComFTDID2XX.FT_LIST_BY_INDEX | SerialComFTDID2XX.FT_OPEN_BY_LOCATION);
				if(devinfo.length == 0) {
					System.out.println("0 length== d2xx.listDevices(0, SerialComFTDID2XX.FT_LIST_BY_INDEX | SerialComFTDID2XX.FT_OPEN_BY_LOCATION);\n");
				}else {
					devinfo[0].dumpDeviceInfo();
				}
			}catch(Exception e) {
				e.printStackTrace();
			}

			try {
				System.out.println("\n");
				devinfo = d2xx.listDevices(1, SerialComFTDID2XX.FT_LIST_BY_INDEX | SerialComFTDID2XX.FT_OPEN_BY_LOCATION);
				if(devinfo.length == 0) {
					System.out.println("0 length== d2xx.listDevices(0, SerialComFTDID2XX.FT_LIST_BY_INDEX | SerialComFTDID2XX.FT_OPEN_BY_LOCATION);\n");
				}else {
					devinfo[0].dumpDeviceInfo();
				}
			}catch(Exception e) {
				e.printStackTrace();
			}

			try {
				System.out.println("\n");
				devinfo = d2xx.listDevices(0, SerialComFTDID2XX.FT_LIST_ALL | SerialComFTDID2XX.FT_OPEN_BY_SERIAL_NUMBER);
				devinfo[0].dumpDeviceInfo();
			}catch(Exception e) {
				e.printStackTrace();
			}

			try {
				System.out.println("\n");
				devinfo = d2xx.listDevices(0, SerialComFTDID2XX.FT_LIST_ALL | SerialComFTDID2XX.FT_OPEN_BY_DESCRIPTION);
				devinfo[0].dumpDeviceInfo();
			}catch(Exception e) {
				e.printStackTrace();
			}

			try {
				System.out.println("\n");
				devinfo = d2xx.listDevices(0, SerialComFTDID2XX.FT_LIST_ALL | SerialComFTDID2XX.FT_OPEN_BY_LOCATION);
				devinfo[0].dumpDeviceInfo();
			}catch(Exception e) {
				e.printStackTrace();
			}

			try {
				System.out.println("\n");
				handle = d2xx.openEx("A70362A3", 0, SerialComFTDID2XX.FT_OPEN_BY_SERIAL_NUMBER);
				System.out.println("d2xx.openex serial : " + handle);
				result = d2xx.close(handle);
				System.out.println("d2xx.close : " + result);
			}catch(Exception e) {
				e.printStackTrace();
			}

			try {
				System.out.println("\n");
				handle = d2xx.openEx("FT232R USB UART", 0, SerialComFTDID2XX.FT_OPEN_BY_DESCRIPTION);
				System.out.println("d2xx.openex description : " + handle);
				result = d2xx.close(handle);
				System.out.println("d2xx.close : " + result);
			}catch(Exception e) {
				e.printStackTrace();
			}

			try {
				System.out.println("\n");
				handle = d2xx.openEx(null, 0, SerialComFTDID2XX.FT_OPEN_BY_LOCATION);
				System.out.println("d2xx.openex location id : " + handle);
				result = d2xx.close(handle);
				System.out.println("d2xx.close : " + result);
			}catch(Exception e) {
				e.printStackTrace();
			}

			handle = d2xx.open(0);
			System.out.println("\nd2xx.open 0: " + handle);

			handle1 = d2xx.open(1);
			System.out.println("\nd2xx.open 1: " + handle1);

			result = d2xx.setBaudRate(handle, 115200);
			System.out.println("\nd2xx.setBaudRate : " + result);
			result = d2xx.setBaudRate(handle1, 115200);
			System.out.println("d2xx.setBaudRate : " + result);

			result = d2xx.setDataCharacteristics(handle, DATABITS.FT_BITS_8, STOPBITS.FT_STOP_BITS_1, PARITY.FT_PARITY_NONE);
			System.out.println("\nd2xx.setDataCharacteristics : " + result);
			result = d2xx.setDataCharacteristics(handle, DATABITS.FT_BITS_8, STOPBITS.FT_STOP_BITS_1, PARITY.FT_PARITY_NONE);
			System.out.println("d2xx.setDataCharacteristics : " + result);

			result = d2xx.setFlowControl(handle, FLOWCTRL.FT_FLOW_NONE, 'x', 'x');
			System.out.println("\nd2xx.setFlowControl : " + result);
			result = d2xx.setFlowControl(handle1, FLOWCTRL.FT_FLOW_NONE, 'x', 'x');
			System.out.println("d2xx.setFlowControl : " + result);

			byte[] buff = new byte[1000];
			for(int x=0; x<120; x++) {
				buff[x] = (byte) 'A';
			}
			ret = d2xx.write(handle, buff, 100);
			System.out.println("\nd2xx.write : " + ret);

			Thread.sleep(500);

			byte[] buf = new byte[1000];
			ret = d2xx.read(handle1, buf, 100);
			System.out.println("\nd2xx.read : " + ret);
			System.out.println("d2xx.read data: " + new String(buf));

			result = d2xx.setDivisor(handle, 30000);
			System.out.println("\nd2xx.setDivisor : " + result);

			result = d2xx.setTimeouts(handle, 5, 5);
			System.out.println("d2xx.setTimeouts : " + result);

			result = d2xx.setDTR(handle);
			System.out.println("d2xx.setDTR : " + result);

			result = d2xx.clearDTR(handle);
			System.out.println("d2xx.clearDTR : " + result);

			result = d2xx.setRTS(handle);
			System.out.println("d2xx.setRTS : " + result);

			ret = d2xx.getModemStatus(handle);
			System.out.println("d2xx.getModemStatus : " + ret);

			result = d2xx.clearRTS(handle);
			System.out.println("d2xx.clearRTS : " + result);

			ret = d2xx.getQueueStatus(handle);
			System.out.println("d2xx.getQueueStatus : " + ret);

			FTOpenedDeviceInfo info = d2xx.getDeviceInfo(handle);
			info.dumpDeviceInfo();

			ret = d2xx.getDriverVersion(handle);
			System.out.println("\nd2xx.getDriverVersion : " + ret);

			ret = d2xx.getLibraryVersion();
			System.out.println("\nd2xx.getLibraryVersion : " + ret);

			arrayLong = d2xx.getStatus(handle);
			System.out.println("\nd2xx.getStatus : " + arrayLong[0] + arrayLong[1] + arrayLong[2]);

			result = d2xx.setChars(handle, 'a', 'w', 's', 'w');
			System.out.println("\nd2xx.setChars : " + result);

			result = d2xx.setBreakOn(handle);
			System.out.println("\nd2xx.setBreakOn : " + result);

			result = d2xx.setBreakOff(handle);
			System.out.println("\nd2xx.setBreakOff : " + result);

			result = d2xx.purge(handle, true, true);
			System.out.println("\nd2xx.purge : " + result);

			result = d2xx.stopInTask(handle);
			System.out.println("\nd2xx.stopInTask : " + result);

			result = d2xx.restartInTask(handle);
			System.out.println("\nd2xx.restartInTask : " + result);			

			result = d2xx.setDeadmanTimeout(handle, 5000);
			System.out.println("\nd2xx.setDeadmanTimeout : " + result);

			result = d2xx.resetDevice(handle);
			System.out.println("\nd2xx.resetDevice : " + result);

			try {
				ret = d2xx.getComPortNumber(handle);
				System.out.println("d2xx.getComPortNumber : " + ret);
			}catch(Exception e) {
				e.printStackTrace();
			}

			try {
				result = d2xx.resetPort(handle);
				System.out.println("d2xx.resetPort : " + result);
			}catch(Exception e) {
				e.printStackTrace();
			}

			try {
				result = d2xx.rescan();
				System.out.println("d2xx.rescan : " + result);
			}catch(Exception e) {
				e.printStackTrace();
			}

			try {
				result = d2xx.reload(0x0403, 0x6001);
				System.out.println("d2xx.reload : " + result);
			}catch(Exception e) {
				e.printStackTrace();
			}

			try {
				result = d2xx.setResetPipeRetryCount(handle, 5);
				System.out.println("d2xx.setResetPipeRetryCount : " + result);	
			}catch(Exception e) {
				e.printStackTrace();
			}

			try {
				result = d2xx.setEventNotificationAndWait(handle, SerialComFTDID2XX.EV_CTS);
				System.out.println("d2xx.setEventNotificationAndWait : " + result);	
			}catch(Exception e) {
				e.printStackTrace();
			}

			/* ********************* Extended API Functions *********************/

			try {
				result = d2xx.setLatencyTimer(handle, 5);
				System.out.println("d2xx.setLatencyTimer : " + result);	
			}catch(Exception e) {
				e.printStackTrace();
			}

			try {
				ret = d2xx.getLatencyTimer(handle);
				System.out.println("d2xx.getLatencyTimer : " + ret);	
			}catch(Exception e) {
				e.printStackTrace();
			}

			try {
				ret = d2xx.getBitMode(handle);
				System.out.println("d2xx.getBitMode : " + ret);	
			}catch(Exception e) {
				e.printStackTrace();
			}

			try {
				result = d2xx.setBitMode(handle, 0, 0);
				System.out.println("d2xx.setBitMode : " + result);	
			}catch(Exception e) {
				e.printStackTrace();
			}

			try {
				result = d2xx.setUSBParameters(handle, 1024, 1024);
				System.out.println("d2xx.setUSBParameters : " + result);	
			}catch(Exception e) {
				e.printStackTrace();
			}

			/* ********************* FT-Win32 API Functions ******************** */

			try {
				result = d2xx.w32SetCommTimeouts(handle, 1, 1, 150, 1, 1000);
				System.out.println("d2xx.w32SetCommTimeouts : " + result);
			}catch(Exception e) {
				e.printStackTrace();
			}

			try {
				int[] ret1 = d2xx.w32GetCommTimeouts(handle);
				for(int q=0; q<ret1.length; q++) {
					System.out.println("d2xx.w32GetCommTimeouts : " + ret1[q]);
				}
			}catch(Exception e) {
				e.printStackTrace();
			}

			try {
				result = d2xx.w32SetCommBreak(handle);
				System.out.println("d2xx.w32SetCommBreak : " + result);
			}catch(Exception e) {
				e.printStackTrace();
			}

			try {
				result = d2xx.w32ClearCommBreak(handle);
				System.out.println("d2xx.w32ClearCommBreak : " + result);
			}catch(Exception e) {
				e.printStackTrace();
			}

			try {
				result = d2xx.w32SetCommMask(handle, SerialComFTDID2XX.EV_CTS);
				System.out.println("d2xx.w32SetCommMask : " + result);
			}catch(Exception e) {
				e.printStackTrace();
			}

			try {
				ret = d2xx.w32GetCommMask(handle);
				System.out.println("d2xx.w32GetCommMask : " + ret);
			}catch(Exception e) {
				e.printStackTrace();
			}

			try {
				result = d2xx.w32PurgeComm(handle, SerialComFTDID2XX.PURGE_RXABORT);
				System.out.println("d2xx.w32PurgeComm : " + result);
			}catch(Exception e) {
				e.printStackTrace();
			}

			try {
				String res = d2xx.w32GetLastError(handle);
				System.out.println("d2xx.w32GetLastError : " + res);
			}catch(Exception e) {
				e.printStackTrace();
			}

			try {
				System.out.println("\n");
				int[] result1 = d2xx.w32ClearCommError(handle);
				for(int q=0; q<result1.length; q++) {
					System.out.println("d2xx.w32ClearCommError : " + result1[q]);
				}
			}catch(Exception e) {
				e.printStackTrace();
			}

			try {
				result = d2xx.w32WaitCommEvent(handle, SerialComFTDID2XX.EV_CTS);
				System.out.println("d2xx.w32WaitCommEvent : " + result);
			}catch(Exception e) {
				e.printStackTrace();
			}

			System.out.println("\nbye");
			result = d2xx.close(handle);
			System.out.println("d2xx.close0 : " + result);
			result = d2xx.close(handle1);
			System.out.println("d2xx.close1 : " + result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
