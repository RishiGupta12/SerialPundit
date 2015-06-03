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
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.embeddedunveiled.serial.SerialComManager.BAUDRATE;
import com.embeddedunveiled.serial.SerialComManager.DATABITS;
import com.embeddedunveiled.serial.SerialComManager.FLOWCONTROL;
import com.embeddedunveiled.serial.SerialComManager.PARITY;
import com.embeddedunveiled.serial.SerialComManager.STOPBITS;

public abstract class BaseSerialEventListenerTest {
	
	protected static Logger LOG = LoggerFactory.getLogger(BaseSerialEventListenerTest.class);
	
	static SerialComManager scm;	
	
	static String PORT1 = null;
	static String PORT2 = null;	
	
	Long handle1;
	Long handle2;
	
	class EventListener implements ISerialComEventListener{
		@Override
		public void onNewSerialEvent(SerialComLineEvent lineEvent) {
			System.out.println("eventCTS : " + lineEvent.getCTS());
			System.out.println("eventDSR : " + lineEvent.getDSR());
		}
	}
	// instantiate class which is will implement ISerialComEventListener interface
	EventListener eventListener = new EventListener();
	
	class DataListener implements ISerialComDataListener{
		@Override
		public void onNewSerialDataAvailable(SerialComDataEvent data) {
			System.out.println("Read from serial port : " + new String(data.getDataBytes()) + "\n");
		}

		@Override
		public void onDataListenerError(int arg0) {
			// TODO Auto-generated method stub

		}
	}
	
	DataListener dataListener = new DataListener();
	
	@BeforeClass
	public static void beforeClass(){
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
		}finally{
		
		}
	}
	
	@Before
	public void before() throws SerialComException{	
	
		LOG.debug(" before(): ");
		
		LOG.debug(" PORT 1: {} opened", PORT1);
		handle1 = scm.openComPort(PORT1, true, true, true);
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.registerLineEventListener(handle1, eventListener);
		try {
			LOG.debug("ditty-rp -a {}:{}", PORT1, Runtime.getRuntime().exec("ditty-rp -a " + PORT1));
		} catch (IOException e){
			LOG.error("BaseSerialTest: " + e);
		}
		
		handle2 = scm.openComPort(PORT2, true, true, true);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		LOG.debug(" PORT 2: {} opened", PORT2);
		try {
			LOG.debug("ditty-rp -a {}:{}", PORT2, Runtime.getRuntime().exec("ditty-rp -a " + PORT2));
		} catch (IOException e){
			LOG.error("BaseSerialTest: " + e);
		}
	}
	
	@After
	public  void after() throws SerialComException{		
		LOG.debug("after()");

		// unregister data listener
		scm.unregisterLineEventListener(eventListener);
		scm.closeComPort(handle1);
		scm.closeComPort(handle2);
		
	}

}
