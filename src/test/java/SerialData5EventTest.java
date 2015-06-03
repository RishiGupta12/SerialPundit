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

import org.junit.Assert;
import org.junit.Test;

import com.embeddedunveiled.serial.SerialComManager.BAUDRATE;
import com.embeddedunveiled.serial.SerialComManager.DATABITS;
import com.embeddedunveiled.serial.SerialComManager.FLOWCONTROL;
import com.embeddedunveiled.serial.SerialComManager.PARITY;
import com.embeddedunveiled.serial.SerialComManager.STOPBITS;

public class SerialData5EventTest extends BaseSerial2Test{
	
	/*
	 * softwareControl Test20
	 */
	@Test
	public void softwareControl() throws SerialComException, InterruptedException{
		LOG.debug("softwareControl()");
		
		byte[] XON  = new byte[] {(byte) 0x24};   // ASCII value of $ character is 0x24
		byte[] XOFF = new byte[] {(byte) 0x23};   // ASCII value of # character is 0x23

		Data1 receiver = new Data1();
		Data0 sender = new Data0();
		
		scm.configureComPortData(receiverHandle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
		scm.configureComPortControl(receiverHandle, FLOWCONTROL.SOFTWARE, '$', '#', false, false);
		scm.registerDataListener(receiverHandle, receiver);
		
		scm.configureComPortData(senderHandle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
		scm.configureComPortControl(senderHandle, FLOWCONTROL.SOFTWARE, '$', '#', false, false);
		scm.registerDataListener(senderHandle, sender);
		

		// Step 1
		scm.writeString(senderHandle, "str1", 0);
		scm.writeString(receiverHandle, "str2", 0);
		Thread.sleep(100);

		// Step 2
		//scm.writeBytes(receiverHandle, XOFF, 0); //Modified
		Thread.sleep(100);
		// Step 3
		scm.writeString(senderHandle, "str3", 0);
		
		// Step 4
		Thread.sleep(100);

		// Step 5
		scm.writeBytes(receiverHandle, XON, 0);
		Thread.sleep(100);
		
		scm.unregisterDataListener(sender);
		scm.unregisterDataListener(receiver);
	}
	
	/*
	 * portWatcherControl Test 21
	 */
	@Test
	public void portWatcherControl() throws SerialComException, InterruptedException{
		LOG.debug("portWatcherControl()");
		portWatcher pw = new portWatcher();

		scm.configureComPortData(receiverHandle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
		scm.configureComPortControl(receiverHandle, FLOWCONTROL.NONE, '$', '$', false, false);
		scm.registerPortMonitorListener(receiverHandle, pw);
		
		Thread.sleep(500);

		scm.unregisterPortMonitorListener(receiverHandle);
	}
	
	/*
	 * setDTEEvent 22
	 */	
	@Test
	public void setDTEEvent() throws SerialComException, InterruptedException{
		
		LOG.debug("setDTEEvent()");
		EventListener eventListener = new EventListener();
		Data1 DTE1 = new Data1();
		Data0 DCE1 = new Data0();

		// DTE terminal
		long DTE = receiverHandle;
		scm.configureComPortData(DTE, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
		scm.configureComPortControl(DTE, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.registerDataListener(DTE, DTE1);
		scm.setRTS(DTE, true);

		Thread.sleep(100);

		// DCE terminal
		long DCE = senderHandle;
		scm.configureComPortData(DCE, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
		scm.configureComPortControl(DCE, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.registerDataListener(DCE, DCE1);
		scm.registerLineEventListener(DCE, eventListener);

		scm.setDTR(DTE, true);
		scm.setDTR(DCE, true);
		Thread.sleep(100);
		scm.setRTS(DTE, true);
		scm.setRTS(DCE, true);
		Thread.sleep(100);

		// Step 1
		scm.writeString(DTE, "str1", 0);
		Thread.sleep(100);
		scm.writeString(DTE, "str1", 0);
		Thread.sleep(100);
		scm.writeString(DCE, "str2", 0);
		Thread.sleep(100);
		scm.writeString(DCE, "str2", 0);
		Thread.sleep(100);

		// Step 2 dte says to dce don't send data i am full
		scm.setRTS(DTE, false);
		Thread.sleep(1000); // give delay so that send data gets updated

		// Step 3 dce will receive event CTS and will start sending data.
		if(senddata.get() == true) {
			scm.writeString(DCE, "str3", 0);
		}else {
			LOG.debug("seems like DTE is full");
		}

		Thread.sleep(1000);
		scm.unregisterDataListener(DTE1);
		scm.unregisterDataListener(DCE1);
		scm.unregisterLineEventListener(eventListener);
		Thread.sleep(200);
	}
	
	/*
	 * setDTE Test 23
	 */
	@Test
	public void setDTE() throws SerialComException, InterruptedException{
		LOG.debug("setDTE");
		long DTE = receiverHandle;
		scm.configureComPortData(DTE, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
		scm.configureComPortControl(DTE, FLOWCONTROL.HARDWARE, 'x', 'x', false, true);

		long DTE1 = senderHandle;
		scm.configureComPortData(DTE1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
		scm.configureComPortControl(DTE1, FLOWCONTROL.HARDWARE, 'x', 'x', false, true);

		scm.setDTR(DTE, true);
		scm.setDTR(DTE1, true);
		Thread.sleep(100);
		scm.setRTS(DTE, true);
		scm.setRTS(DTE1, true);
		Thread.sleep(100);

		scm.writeString(DTE, "str1", 0);
		Thread.sleep(100);
		scm.writeString(DTE, "str1", 0);
		Thread.sleep(100);
		scm.writeString(DTE, "str1", 0);
		Thread.sleep(100);
		scm.writeString(DTE, "str1", 0);
		scm.writeString(DTE, "str1", 0);
		Thread.sleep(100);
		scm.writeString(DTE, "str1", 0);
		Thread.sleep(100);
		scm.writeString(DTE, "str1", 0);
		Thread.sleep(100);
		scm.writeString(DTE, "str1", 0);

		scm.writeString(DTE1, "str1", 0);
		Thread.sleep(100);
		scm.writeString(DTE1, "str1", 0);
		Thread.sleep(100);
		scm.writeString(DTE1, "str1", 0);
		scm.writeString(DTE1, "str1", 0);
		Thread.sleep(100);
		scm.writeString(DTE1, "str1", 0);
		Thread.sleep(100);
		scm.writeString(DTE1, "str1", 0);
	}
	
	/*
	 * doubleEventLink Test24
	 */
	@Test
	public void doubleEventLink() throws SerialComException, InterruptedException{
		
		LOG.debug("doubleEventLink()");;
		EventListener eventListener = new EventListener();
		Data1 DTE1 = new Data1();
		Data0 DCE1 = new Data0();

		// DTE terminal
		long DTE = receiverHandle;
		scm.configureComPortData(DTE, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
		scm.configureComPortControl(DTE, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.registerDataListener(DTE, DTE1);

		// DCE terminal
		long DCE = senderHandle;
		scm.configureComPortData(DCE, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
		scm.configureComPortControl(DCE, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.registerDataListener(DCE, DCE1);
		scm.registerLineEventListener(DCE, eventListener);

		// Step 1
		scm.writeString(DTE, "str1", 0);
		Thread.sleep(100);
		scm.writeString(DTE, "str1", 0);
		Thread.sleep(100);
		scm.writeString(DCE, "str2", 0);
		Thread.sleep(100);
		scm.writeString(DCE, "str2", 0);
		Thread.sleep(100);

		Thread.sleep(2000);
		scm.unregisterDataListener(DTE1);
		scm.unregisterDataListener(DCE1);
		scm.unregisterLineEventListener(eventListener);
	}
	
	/*
	 * baudRate() Test25
	 */
	@Test
	public void baudRate() throws SerialComException, InterruptedException{
		
		LOG.debug("baudRate()");
		long handle = receiverHandle;
		scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.BCUSTOM, 250000);
		scm.configureComPortControl(handle, FLOWCONTROL.NONE, '$', '$', false, false);

		long handle1 = senderHandle;
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.BCUSTOM, 250000);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, '$', '$', false, false);
		String str = "testing";
		scm.writeString(handle, str, 0);
		Thread.sleep(100);
		String data = scm.readString(handle1);
		LOG.debug("data read is : " + data);
		Assert.assertEquals(str, data);
	}
	
	/*
	 * readWriteLoop() Test26
	 */
	@Test
	public void readWriteLoop() throws SerialComException, InterruptedException{
		
		LOG.debug("readWriteLoop");
		int x = 0;
		scm.configureComPortData(receiverHandle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
		scm.configureComPortControl(receiverHandle, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(senderHandle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
		scm.configureComPortControl(senderHandle, FLOWCONTROL.NONE, 'x', 'x', false, false);

		for(x=0; x<100; x++) {
			if(scm.writeString(receiverHandle, "testing", 0) == false) {
				throw new InterruptedException("I/O write failed");
			}
			if(scm.writeString(senderHandle, "testing1", 0) == false) {
				throw new InterruptedException("I/O write 1 failed");
			}

			Thread.sleep(10);
			String streceive = scm.readString(receiverHandle);
			LOG.debug("data1 read is : " + streceive);
			Assert.assertEquals(streceive, "testing1");
			String stsender = scm.readString(senderHandle);
			LOG.debug("data read is : " + stsender);
			Assert.assertEquals(stsender, "testing");
			LOG.debug("x: " + x);
		}
	}
	
	/*
	 * setDTRRTSLoop() Test27
	 */
	@Test
	public void setDTRRTSLoop() throws SerialComException, InterruptedException{
		
		LOG.debug("setDTRRTSLoop()");
		EventListener eventListener = new EventListener();
		Data0 dataListener = new Data0();
		
		long handle = receiverHandle;
		scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
		scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);

		long handle1 = senderHandle;
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		int x = 0;

		for(x=0; x<5000; x++) {
			LOG.debug("register  : " + scm.registerLineEventListener(handle, eventListener));
			scm.setDTR(handle1, true);
			scm.setRTS(handle1, true);
			scm.setRTS(handle1, false);
			scm.setDTR(handle1, false);
			Thread.sleep(10);
			LOG.debug("unregister : " + scm.unregisterLineEventListener(eventListener));
		}
		LOG.debug("test event pass \n");

		for(x=0; x<5000; x++) {
			LOG.debug("register  : " + scm.registerDataListener(handle, dataListener));
			scm.writeString(handle1, "T", 0);
			Thread.sleep(50);
			LOG.debug("unregister : " + scm.unregisterDataListener(dataListener));
		}
		LOG.debug("test data pass \n");
	}
	
	/*
	 * setDTRRTSInfiniteLoop Test29
	 */
	@Test
	public void setDTRRTSInfiniteLoop() throws SerialComException, InterruptedException{
		
		LOG.debug("setDTRRTSInfiniteLoop()");
		EventListener eventListener = new EventListener();
		Data0 dataListener = new Data0();
		
		long handle = receiverHandle;
		scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
		scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);
		long handle1 = senderHandle;
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);

		LOG.debug("register  : " + scm.registerLineEventListener(handle, eventListener));
		LOG.debug("register  : " + scm.registerDataListener(handle, dataListener));

		while(true) {
			scm.setDTR(handle1, true);
			scm.setRTS(handle1, true);
			scm.setRTS(handle1, false);
			scm.setDTR(handle1, false);
			scm.writeString(handle1, "T", 0);
			Thread.sleep(3000);
		}
	}
	
	/*
	 * rmPortInExecListener() Test31
	 */
	@Test
	public void rmPortInExecListener() throws SerialComException, InterruptedException{
		
		LOG.debug("rmPortInExecListener()");
		EventListener eventListener = new EventListener();
		Data0 dataListener = new Data0();
		
		scm.configureComPortData(receiverHandle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
		scm.configureComPortControl(receiverHandle, FLOWCONTROL.NONE, 'x', 'x', false, false);
		
		scm.configureComPortData(senderHandle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
		scm.configureComPortControl(senderHandle, FLOWCONTROL.NONE, 'x', 'x', false, false);

		LOG.debug("register  : " + scm.registerLineEventListener(receiverHandle, eventListener));
		LOG.debug("register  : " + scm.registerDataListener(receiverHandle, dataListener));

		while(true) {
			scm.setDTR(senderHandle, true);
			scm.setRTS(senderHandle, true);
			scm.setRTS(senderHandle, false);
			scm.setDTR(senderHandle, false);
			scm.writeString(senderHandle, "T", 0);
			Thread.sleep(10);
			scm.readString(receiverHandle);
			Thread.sleep(10000);
		}
	}
	
}
