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

package com.embeddedunveiled.serial;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.embeddedunveiled.serial.SerialComManager.BAUDRATE;
import com.embeddedunveiled.serial.SerialComManager.DATABITS;
import com.embeddedunveiled.serial.SerialComManager.FLOWCONTROL;
import com.embeddedunveiled.serial.SerialComManager.PARITY;
import com.embeddedunveiled.serial.SerialComManager.STOPBITS;



public class SerialIndep1PortLoopTest {
	
	int x = 0;
	protected static Logger LOG = LoggerFactory.getLogger(BaseSerial1Test.class);
	
	static SerialComManager scm;	
	
	static String PORT1 = null;
	static String PORT2 = null;	
	
	Long handle1;
	
	/*
	 * loopPort
	 */
	@Test
	public void loopPort() throws SerialComException, InterruptedException{
		LOG.debug("loopPort");
		for(x=0; x<5000; x++) {
			try {
				scm = new SerialComManager();
				//scm.enableDebugging(true);
					int osType = SerialComManager.getOSType();
				if(osType == SerialComManager.OS_LINUX) { 
					/*
					 *  Use:
					 *  socat PTY,link=/dev/ttyS98 PTY,link=/dev/ttyS99
					 *  socat -d -v -x PTY,link=/tmp/serial,wait-slave,raw /dev/tty_dgrp_a_9,raw
					 *  
					 */
					PORT1 = "/dev/ttyS98";
					PORT2 = "/dev/ttyS99";
				}else if(osType == SerialComManager.OS_WINDOWS) {
					PORT1 = "COM51";
					PORT2 = "COM52";
				}else if(osType == SerialComManager.OS_MAC_OS_X) {
					PORT1 = "/dev/cu.usbserial-A70362A3";
					PORT2 = "/dev/cu.usbserial-A602RDCH";
				}else if(osType == SerialComManager.OS_SOLARIS) {
					PORT1 = null;
					PORT2 = null;
				}else{
					
				}	
				handle1 = scm.openComPort(PORT1, true, true, true);
				LOG.debug("open status :" + handle1 + " at " + "x== " + x);
				Boolean bool = scm.closeComPort(handle1);
				Assert.assertTrue(bool);
				LOG.debug("close status :" + bool);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/*
	 * checkOS() Test43
	 */
	protected static AtomicBoolean exit = new AtomicBoolean(false);
	class DataListener implements ISerialComDataListener{

		int y = 0;
		
		@Override
		public void onNewSerialDataAvailable(SerialComDataEvent data) {
			byte[] buf = data.getDataBytes();
			System.out.println("DataListener : " + new String(buf));
			System.out.println("DataListener : " + buf.length);
			
			y = y + buf.length;
			if(y >= 20) {
				exit.set(true);
			}
		}
		@Override
		public void onDataListenerError(int arg0) {
			System.out.println("error : " + arg0);
		}
	}
	@Test
	public void checkOS() throws SerialComException, InterruptedException{
		LOG.debug("checkOS()");
		SerialComManager scm = new SerialComManager();
		
		int x = 0;
		for(x=0; x<1000; x++) {
			LOG.debug("\n" + "Iteration : " + x);
			try {
				DataListener dataListener = new DataListener();
	
				int osType = SerialComManager.getOSType();
				if(osType == SerialComManager.OS_LINUX) { 
					/*
					 *  Use:
					 *  socat PTY,link=/dev/ttyS98 PTY,link=/dev/ttyS99
					 *  socat -d -v -x PTY,link=/tmp/serial,wait-slave,raw /dev/tty_dgrp_a_9,raw
					 *  
					 */
					PORT1 = "/dev/ttyS98";
					PORT2 = "/dev/ttyS99";
				}else if(osType == SerialComManager.OS_WINDOWS) {
					PORT1 = "COM51";
					PORT2 = "COM52";
				}else if(osType == SerialComManager.OS_MAC_OS_X) {
					PORT1 = "/dev/cu.usbserial-A70362A3";
					PORT2 = "/dev/cu.usbserial-A602RDCH";
				}else if(osType == SerialComManager.OS_SOLARIS) {
					PORT1 = null;
					PORT2 = null;
				}else{
					
				}	

				long handle = scm.openComPort(PORT1, true, true, true);
				scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
				scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);
				long handle1 = scm.openComPort(PORT2, true, true, true);
				scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
				scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);

				LOG.debug("main thread register  : " + scm.registerDataListener(handle, dataListener));
				
				if(osType == SerialComManager.OS_WINDOWS) {
					Thread.sleep(500);
				}
				
				scm.writeString(handle1, "22222222222222222222", 0); // length of this string is 20
				
				// wait till data listener has received all the data
				while(exit.get() == false) { 
					if(osType == SerialComManager.OS_LINUX) {
						Thread.sleep(1);
					}else if(osType == SerialComManager.OS_WINDOWS) {
						Thread.sleep(600);
					}else if(osType == SerialComManager.OS_MAC_OS_X) {
						Thread.sleep(500);
					}else if(osType == SerialComManager.OS_SOLARIS) {
						Thread.sleep(500);
					}else{
					}
					scm.writeString(handle1, "22222222222222222222", 0);
				}
				exit.set(false);                                     // reset flag
				
				LOG.debug("main thread unregister : " + scm.unregisterDataListener(dataListener));
				if(osType == SerialComManager.OS_LINUX) {
					Thread.sleep(1);
				}else if(osType == SerialComManager.OS_WINDOWS) {
					Thread.sleep(500);
				}else if(osType == SerialComManager.OS_MAC_OS_X) {
					Thread.sleep(500);
				}else if(osType == SerialComManager.OS_SOLARIS) {
					Thread.sleep(500);
				}else{
				}
				
				scm.closeComPort(handle);
				scm.closeComPort(handle1);
				if(osType == SerialComManager.OS_LINUX) {
					Thread.sleep(1);
				}else if(osType == SerialComManager.OS_WINDOWS) {
					Thread.sleep(500);
				}else if(osType == SerialComManager.OS_MAC_OS_X) {
					Thread.sleep(500);
				}else if(osType == SerialComManager.OS_SOLARIS) {
					Thread.sleep(500);
				}else{
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
