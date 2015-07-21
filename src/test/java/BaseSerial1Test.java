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

import java.io.IOException;


import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.embeddedunveiled.serial.SerialComManager.BAUDRATE;
import com.embeddedunveiled.serial.SerialComManager.DATABITS;
import com.embeddedunveiled.serial.SerialComManager.FLOWCONTROL;
import com.embeddedunveiled.serial.SerialComManager.PARITY;
import com.embeddedunveiled.serial.SerialComManager.STOPBITS;


public abstract class BaseSerial1Test{
	
	protected static Logger LOG = LoggerFactory.getLogger(BaseSerial1Test.class);
	 
	static SerialComManager scm;	
	
	static String PORT1 = null;
	static String PORT2 = null;	
	
	long handle1;
	long handle2;
	
	class Data implements ISerialComDataListener{
		@Override
		public void onNewSerialDataAvailable(SerialComDataEvent data) {
			String str = new String(data.getDataBytes()); 
			LOG.debug("Read from serial port : " + str);
			Assert.assertTrue("" != str);
		}

		@Override
		public void onDataListenerError(int arg0) {
			LOG.debug("onDataListenerError : " + arg0);
		}
	}
	
	// instantiate class which is will implement ISerialComDataListener interface
	Data dataListener = new Data();
	
	class EventListener implements ISerialComEventListener{
		@Override
		public void onNewSerialEvent(SerialComLineEvent lineEvent) {
			LOG.debug("eventCTS : " + lineEvent.getCTS());
			LOG.debug("eventDSR : " + lineEvent.getDSR());
		}
	}
	// instantiate class which is will implement ISerialComEventListener interface
	EventListener eventListener = new EventListener();
	
	//ExecuteShellComand shell = new ExecuteShellComand();
	
	
	
	@BeforeClass
	public static void beforeClass(){
		try {
			scm = new SerialComManager();
			//scm.enableDebugging(true);
			int osType = scm.getOSType();
			if(osType == SerialComManager.OS_LINUX) { 
				/*
				 *  Use:
				 *  socat PTY,link=/dev/ttyS98 PTY,link=/dev/ttyS99
				 *  socat -d -v -x PTY,link=/tmp/serial,wait-slave,raw /dev/tty_dgrp_a_9,raw
				 *  
				 */
				
				//PORT1 = "/dev/tty_dgrp_a_2";
				//PORT2 = "/dev/tty_dgrp_a_3";
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
		}catch(IOException e){
			LOG.error("BaseSerial1Test: " + e);
		}finally{
		
		}
	}
	
	@Before
	public void before() throws SerialComException{	
	
		LOG.debug(" before(): ");
		// open and configure port that will listen data
		handle1 = scm.openComPort(PORT1, true, true, true);
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		LOG.debug(" PORT 1: {} opened", PORT1);
		try {
			LOG.debug("ditty-rp -a {}:{}", PORT1, Runtime.getRuntime().exec("ditty-rp -a " + PORT1));
		} catch (IOException e){
			LOG.error("BaseSerial1Test: " + e);
		}
		
		// open and configure port which will send data
		handle2 = scm.openComPort(PORT2, true, true, true);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
		LOG.debug(" PORT 2: {} opened", PORT2);
		try {
			LOG.debug("ditty-rp -a {}:{}", PORT2, Runtime.getRuntime().exec("ditty-rp -a " + PORT2));
		} catch (IOException e){
			LOG.error("BaseSerial1Test: " + e);
		}
	}
	
	@After
	public  void after() throws SerialComException{		
		LOG.debug("after()");
		scm.closeComPort(handle1);
		scm.closeComPort(handle2);
		
	}
	
	
}








