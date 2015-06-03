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

import java.util.concurrent.atomic.AtomicBoolean;


public class BaseSerial2Test {
	
	protected static Logger LOG = LoggerFactory.getLogger(BaseSerial2Test.class);
	
	static SerialComManager scm;	
	
	static String PORT1 = null;
	static String PORT2 = null;	
	
	Long receiverHandle;
	Long senderHandle;
	
	class Data0 implements ISerialComDataListener{
		@Override
		public void onNewSerialDataAvailable(SerialComDataEvent data) {
			LOG.debug("Sender got from receiver : " + new String(data.getDataBytes()));
		}

		@Override
		public void onDataListenerError(int arg0) {
		}
	}

	class Data1 implements ISerialComDataListener{
		@Override
		public void onNewSerialDataAvailable(SerialComDataEvent data) {
			LOG.debug("Receiver got from sender : " + new String(data.getDataBytes()));
		}

		@Override
		public void onDataListenerError(int arg0) {
		}
	}
	
	protected static AtomicBoolean senddata = new AtomicBoolean(true);
	
	class EventListener implements ISerialComEventListener {
		@Override
		public void onNewSerialEvent(SerialComLineEvent lineEvent) {
			LOG.debug("eventCTS : " + lineEvent.getCTS());
			senddata.set(false);
		}
	}
	
	class portWatcher implements ISerialComPortMonitor{
		@Override
		public void onPortMonitorEvent(int event) {
			LOG.debug("==" + event);
		}
	}
	
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
		// open and configure port that will listen data
		receiverHandle = scm.openComPort(PORT1, true, true, true);
		
		try {
			LOG.debug("ditty-rp -a {}:{}", PORT1, Runtime.getRuntime().exec("ditty-rp -a " + PORT1));
		} catch (IOException e){
			LOG.error("BaseSerial2Test: " + e);
		}
		
		// open and configure port which will send data
		senderHandle = scm.openComPort(PORT2, true, true, true);
				
		try {
			LOG.debug("ditty-rp -a {}:{}", PORT2, Runtime.getRuntime().exec("ditty-rp -a " + PORT2));
		} catch (IOException e){
			LOG.error("BaseSerial2Test: " + e);
		}
	}
	
	@After
	public  void after() throws SerialComException{		
		LOG.debug("after()");
		if (receiverHandle != null){
			scm.closeComPort(receiverHandle);
		}
		if (senderHandle != null){
			scm.closeComPort(senderHandle);
		}
	}
	

}
