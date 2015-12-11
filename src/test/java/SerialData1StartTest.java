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

import org.junit.Test;
import org.junit.Assert;

import java.util.Arrays;

public class SerialData1StartTest  extends BaseSerial1Test{
	
	/*
	 * connect bluetooth dongle, 3G dongle, USB-UART converter and all of them should be 
	 * detected by this library apart from regular ports.
	 * Test1
	 */	
	@Test
	public void listPortsTest() throws SerialComException{
		
		LOG.debug("listPortsTest()");
		String[] ports = {};
		ports = scm.listAvailableComPorts();
		for(String port: ports){
			LOG.debug("Port: {}", port );
		}		
		
	}
	
	/*
	 * readWriteWithoutOwnership() Test2
	 */
	@Test
	public void readWriteWithoutOwnership() throws SerialComException, InterruptedException{
		LOG.debug("readWriteWithoutOwnership");
		
		String data111 = scm.readString(handle2);
		LOG.debug("data read for 1 byte is : " + data111);
		
		// test single byte
		if(scm.writeString(handle1, "1", 0) == true) {
			LOG.debug("write success 1 byte");
		}
		Thread.sleep(100);
		String data = scm.readString(handle2);
		Assert.assertEquals("1", data);
		LOG.debug("data read for 1 byte is : " + data);
		
		// test 2 byte
		if(scm.writeString(handle1, "22", 0) == true) {
			LOG.debug("write success 2 byte");
		}
		Thread.sleep(100);
		data = scm.readString(handle2);
		Assert.assertEquals("22", data);
		LOG.debug("data read for 2 byte is : " + data);
		
		// test 3 byte
		if(scm.writeString(handle1, "333", 0) == true) {
			LOG.debug("write success 3 byte");
		}
		Thread.sleep(100);
		data = scm.readString(handle2);
		Assert.assertEquals("333", data);
		LOG.debug("data read for 3 byte is : " + data);
		
		// test 4 byte
		if(scm.writeString(handle1, "4444", 0) == true) {
			LOG.debug("write success 4 byte");
		}
		Thread.sleep(100);
		data = scm.readString(handle2);
		Assert.assertEquals("4444", data);
		LOG.debug("data read for 4 byte is : " + data);
		
		// test 5 byte
		if(scm.writeString(handle1, "55555", 0) == true) {
			LOG.debug("write success 5 byte");
		}
		Thread.sleep(100);
		data = scm.readString(handle2);
		Assert.assertEquals("55555", data);
		LOG.debug("data read for 5 byte is : " + data);
		
		// test 10 byte
		if(scm.writeString(handle1, "1000000000", 0) == true) {
			LOG.debug("write success 10 byte");
		}
		Thread.sleep(100);
		data = scm.readString(handle2);
		Assert.assertEquals("1000000000", data);
		LOG.debug("data read for 10 byte is : " + data);
	}
	
	/*
	 * osType() Test3
	 */
	@Test
	public void osType() throws SerialComException, InterruptedException{
		LOG.debug("osType()");
		scm.registerDataListener(handle1, dataListener);    // register data listener for this port

		scm.writeString(handle2, "test string", 0);
		Thread.sleep(1000); // wait for data to be displayed on console
		
		int soType = scm.getOSType();
		if(soType == SerialComManager.OS_LINUX) {
			scm.fineTuneRead(handle1, 5, 1, 0, 0, 0);
		}
		
		scm.writeString(handle2, "test string", 0);
		Thread.sleep(1000); // wait for data to be displayed on console
		
		scm.unregisterDataListener(dataListener); // unregister data listener
	}
	
	/*
	 * openConfigurePorts() Test4
	 */
	@Test
	public void openConfigurePorts() throws SerialComException, InterruptedException{
		LOG.debug("openConfigurePorts()");
		String[] config = scm.getCurrentConfiguration(handle1);
		LOG.debug(Arrays.toString(config));
	}
	
	/*
	 * getLineStatus() Test5
	 */
	@Test
	public void getLineStatus() throws SerialComException, InterruptedException{
		LOG.debug("getLineStatus()");
		// read the status
		int[] state = scm.getLinesStatus(handle1);
		LOG.debug("CTS state = " + state[0]);
		LOG.debug("DSR state = " + state[1]);
		LOG.debug("CD state = " +  state[2]);
		LOG.debug("RI state = " +  state[3]);
		
		LOG.debug("\n");
		
		int[] state1 = scm.getLinesStatus(handle2);
		LOG.debug("CTS state = " + state1[0]);
		LOG.debug("DSR state = " + state1[1]);
		LOG.debug("CD state = " +  state1[2]);
		LOG.debug("RI state = " +  state1[3]);
	}
	
	/*
	 * eventListener() Test6
	 */
	@Test
	public void eventListener() throws SerialComException, InterruptedException{
		LOG.debug("eventListener");
		
		LOG.debug("" + scm.registerLineEventListener(handle1, eventListener));
		Thread.sleep(100);
		scm.setDTR(handle2, false);
		Thread.sleep(200);
		scm.setRTS(handle2, false);

		Thread.sleep(200);
		scm.setDTR(handle2, true);
		Thread.sleep(200);
		scm.setRTS(handle2, true);
		Thread.sleep(200);

		scm.setDTR(handle2, false);
		Thread.sleep(200);
		scm.setRTS(handle2, false);
		Thread.sleep(200);

		scm.setDTR(handle2, true);
		Thread.sleep(200);
		scm.setRTS(handle2, true);
		Thread.sleep(200);

		// unregister data listener
		scm.unregisterLineEventListener(eventListener);
		Thread.sleep(200);
	}
	
	/*
	 * cleanPortIO() Test7
	 */
	@Test 
	public void cleanPortIO() throws SerialComException, InterruptedException{
		LOG.debug("cleanPortIO()");
		scm.clearPortIOBuffers(handle2, true, true);

		byte[] arr = new byte[10];
		for(int x=0; x<10; x++) {
			arr[x] = (byte) 202;
		}

		scm.writeBytes(handle1, arr, 0);
		Thread.sleep(200);

		// before
		int[] before1 = scm.getByteCountInPortIOBuffer(handle2);
		System.out.println("BEFORE1 :: input : " + before1[0] + " output : " + before1[1]);

		// after
		scm.clearPortIOBuffers(handle1, true, true);
		int[] after1 = scm.getByteCountInPortIOBuffer(handle2);
		System.out.println("AFTER1 :: input : " + after1[0] + " output : " + after1[1]);
	}
	
	/*
	 * sendBreak() Test9
	 */
	@Test
	public void sendBreak() throws SerialComException, InterruptedException{
		LOG.debug("sendBreak()");
		// 2000 milli seconds
		scm.sendBreak(handle1, 2000);
	}
}
