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

package test78;

import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComSystemProperty;
import com.serialpundit.serial.SerialComManager;
import com.serialpundit.serial.vendor.SerialComMCHPSimpleIO;
import com.serialpundit.serial.vendor.SerialComVendorLib;

public class Test78  {

	static SerialComManager scm;
	public static SerialComPlatform scp = null;
	static int osType;
	static SerialComMCHPSimpleIO mchpsio = null;
	static String vendorSuppliedLib;
	static String libpath;

	public static void main(String[] args) {
		try {
			scm = new SerialComManager();
			scp = new SerialComPlatform(new SerialComSystemProperty());

			osType = scp.getOSType();
			if(osType == SerialComPlatform.OS_LINUX) { 
			}else if(osType == SerialComPlatform.OS_WINDOWS) {
				libpath = "D:\\zz\\mchpsio";
				vendorSuppliedLib = "SimpleIO-UM.dll";
			}else if(osType == SerialComPlatform.OS_MAC_OS_X) {
			}else {
			}

			mchpsio = (SerialComMCHPSimpleIO) scm.getVendorLibFromFactory(SerialComVendorLib.VLIB_MCHP_SIMPLEIO, libpath, vendorSuppliedLib);

			try {
				System.out.println("initMCP2200() : " + mchpsio.initMCP2200(0x04d8, 0x00df));
			}catch (Exception e) {
				System.out.println("initMCP2200() : " + e.getMessage());
			}

			try {
				System.out.println("isConnected() : " + mchpsio.isConnected());
			}catch (Exception e) {
				System.out.println("isConnected() : " + e.getMessage());
			}

			try {
				System.out.println("configureMCP2200() : " + mchpsio.configureMCP2200((byte) 1, 9600, 1, 1, 
						false, true, true, false));
			}catch (Exception e) {
				System.out.println("configureMCP2200() : " + e.getMessage());
			}

			try {
				System.out.println("setPin() : " + mchpsio.setPin(1));
			}catch (Exception e) {
				System.out.println("setPin() : " + e.getMessage());
			}

			try {
				System.out.println("clearPin() : " + mchpsio.clearPin(1));
			}catch (Exception e) {
				System.out.println("clearPin() : " + e.getMessage());
			}

			try {
				System.out.println("readPinValue() : " + mchpsio.readPinValue(1));
			}catch (Exception e) {
				System.out.println("readPinValue() : " + e.getMessage());
			}

			try {
				System.out.println("readPin() : " + mchpsio.readPin(1));
			}catch (Exception e) {
				System.out.println("readPin() : " + e.getMessage());
			}

			try {
				System.out.println("writePort() : " + mchpsio.writePort(1));
			}catch (Exception e) {
				System.out.println("writePort() : " + e.getMessage());
			}

			try {
				System.out.println("readPort() : " + mchpsio.readPort());
			}catch (Exception e) {
				System.out.println("readPort() : " + e.getMessage());
			}

			try {
				System.out.println("readPortValue() : " + mchpsio.readPortValue());
			}catch (Exception e) {
				System.out.println("readPortValue() : " + e.getMessage());
			}

			try {
				System.out.println("selectDevice() : " + mchpsio.selectDevice(0));
			}catch (Exception e) {
				System.out.println("selectDevice() : " + e.getMessage());
			}

			try {
				System.out.println("getSelectedDevice() : " + mchpsio.getSelectedDevice());
			}catch (Exception e) {
				System.out.println("getSelectedDevice() : " + e.getMessage());
			}

			try {
				System.out.println("getNumOfDevices() : " + mchpsio.getNumOfDevices());
			}catch (Exception e) {
				System.out.println("getNumOfDevices() : " + e.getMessage());
			}

			try {
				System.out.println("getSelectedDeviceInfo() : " + mchpsio.getSelectedDeviceInfo());
			}catch (Exception e) {
				System.out.println("getSelectedDeviceInfo() : " + e.getMessage());
			}

			try {
				System.out.println("readEEPROM() : " + mchpsio.readEEPROM(0x00));
			}catch (Exception e) {
				System.out.println("readEEPROM() : " + e.getMessage());
			}

			//			try {
			//				System.out.println("writeEEPROM() : " + mchpsio.writeEEPROM(0x00, (short) 0x00));
			//			}catch (Exception e) {
			//				System.out.println("writeEEPROM() : " + e.getMessage());
			//			}

			try {
				System.out.println("fnRxLED() : " + mchpsio.fnRxLED(SerialComMCHPSimpleIO.ON));
			}catch (Exception e) {
				System.out.println("fnRxLED() : " + e.getMessage());
			}

			try {
				System.out.println("fnTxLED() : " + mchpsio.fnTxLED(SerialComMCHPSimpleIO.ON));
			}catch (Exception e) {
				System.out.println("fnTxLED() : " + e.getMessage());
			}

			try {
				System.out.println("hardwareFlowControl() : " + mchpsio.hardwareFlowControl(0));
			}catch (Exception e) {
				System.out.println("hardwareFlowControl() : " + e.getMessage());
			}

			try {
				System.out.println("fnULoad() : " + mchpsio.fnULoad(1));
			}catch (Exception e) {
				System.out.println("fnULoad() : " + e.getMessage());
			}

			try {
				System.out.println("fnSuspend() : " + mchpsio.fnSuspend(1));
			}catch (Exception e) {
				System.out.println("fnSuspend() : " + e.getMessage());
			}

			try {
				System.out.println("fnInvertUartPol() : " + mchpsio.fnInvertUartPol(1));
			}catch (Exception e) {
				System.out.println("fnInvertUartPol() : " + e.getMessage());
			}

			try {
				System.out.println("fnSetBaudRate() : " + mchpsio.fnSetBaudRate(115200));
			}catch (Exception e) {
				System.out.println("fnSetBaudRate() : " + e.getMessage());
			}

			try {
				System.out.println("configureIO() : " + mchpsio.configureIO((short) 1));
			}catch (Exception e) {
				System.out.println("configureIO() : " + e.getMessage());
			}

			try {
				System.out.println("configureIoDefaultOutput() : " + mchpsio.configureIoDefaultOutput((short)1, (short)1));
			}catch (Exception e) {
				System.out.println("configureIoDefaultOutput() : " + e.getMessage());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
