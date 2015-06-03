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


import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.junit.Assert;
import org.junit.Test;


import com.embeddedunveiled.serial.SerialComManager.ENDIAN;
import com.embeddedunveiled.serial.SerialComManager.NUMOFBYTES;

public class SerialData2Test extends BaseSerial1Test{
	
	
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
	 * try opening serial port for read and write without exclusive ownership
	 */		
	@Test
	public void exclusiveOwnershipTest() throws SerialComException, InterruptedException{
		
		LOG.debug("exclusiveOwnershipTest()");

		
		// try to send data out of serial port
		if(scm.writeString(handle1, "ATI\r\nSCM DIGI", 0) == true) {
			LOG.debug("DIGI Sent: ATI\r\nSCM DIGI");
		}
		
		// try to send data out of serial port
		if(scm.writeString(handle2, "ATI\r\nSCM PERLE", 0) == true) {
			LOG.debug("PERLE Sent: ATI\r\nSCM PERLE");
		}		
		
		Thread.sleep(1500);
		
		String s1 = "";
		s1 = scm.readString(handle1);
		LOG.debug("DIGI Received:{}", s1);
		Assert.assertTrue(s1 != "");
		
		
		String s2 = "";
		s2 = scm.readString(handle2);
		LOG.debug("PERLE Received:{}", s2);
		Assert.assertTrue(s2 != "");
	}
	
	
	/*
	 * Test readString
	 */		
	@Test
	public void readStringTest() throws SerialComException, InterruptedException, UnsupportedEncodingException{	
	
		LOG.debug("readStringTest()");
		
		String out = "Send test string";
		String in = "";
		

		LOG.debug(out);
				
		scm.writeString(handle1, out, Charset.forName("UTF-8"), 0);
	
		// wait for data to be displayed on console
		Thread.sleep(1000);
		
		in = scm.readString(handle2); //MODIFIED: scm.readString(handle1)
		
		LOG.debug(in);
		
		//test if strings are the same
		Assert.assertEquals(out, in);	
	}
	
	/*
	 * Test endianTest
	 */		
	@Test
	public void endianTest() throws SerialComException, InterruptedException, UnsupportedEncodingException{	
	
		LOG.debug("endianTest()");	
		
			// 350 = 00000001 01011110, 2 bytes required, big endian format
			scm.writeSingleInt(handle1, 350, 0, ENDIAN.E_BIG, NUMOFBYTES.NUM_2);
			Thread.sleep(100);
			byte[] arr = new byte[2];
			arr = scm.readBytes(handle2, 2);
			
			//1  which is 00000001
			LOG.debug("dataa: " + arr[0]);
			Assert.assertEquals(1, arr[0]); 
			
			// 94 which is 01011110
			LOG.debug("datab: " + arr[1]); 
			Assert.assertEquals(94, arr[1]);
			
			
			// 350 = 00000001 01011110, 2 bytes required, little endian format
			scm.writeSingleInt(handle1, 350, 0, ENDIAN.E_LITTLE, NUMOFBYTES.NUM_2);
			Thread.sleep(100);
			byte[] arr1 = new byte[2];
			arr1 = scm.readBytes(handle2, 2);
			Assert.assertEquals(94, arr1[0]);
			Assert.assertEquals(1, arr1[1]);
			LOG.debug("dataa: " + arr1[0]); // prints 94 which is 01011110
			LOG.debug("datab: " + arr1[1]); // prints 1  which is 00000001
		
			// 7050354 = 00000000 01101011 10010100 01110010, 4 bytes required, big endian format
			scm.writeSingleInt(handle1, 7050354, 0, ENDIAN.E_BIG, NUMOFBYTES.NUM_4);
			Thread.sleep(100);
			byte[] arr2 = new byte[4];
			arr2 = scm.readBytes(handle2, 4);
			Assert.assertEquals(0, arr2[0]);
			Assert.assertEquals(107, arr2[1]);
			Assert.assertEquals(-108, arr2[2]);
			Assert.assertEquals(114, arr2[3]);
			LOG.debug("dataa: " + arr2[0]); // prints 0    which is 00000000
			LOG.debug("datab: " + arr2[1]); // prints 107  which is 01101011
			LOG.debug("datac: " + arr2[2]); // prints -108 which is 10010100
			LOG.debug("datad: " + arr2[3]); // prints 114  which is 01110010
		
			// 7050354 = 00000000 01101011 10010100 01110010, 4 bytes required, little endian format
			scm.writeSingleInt(handle1, 7050354, 0, ENDIAN.E_LITTLE, NUMOFBYTES.NUM_4);
			Thread.sleep(100);
			byte[] arr3 = new byte[4];
			arr3 = scm.readBytes(handle2, 4);
			Assert.assertEquals(114, arr3[0]);
			Assert.assertEquals(-108, arr3[1]);
			Assert.assertEquals(107, arr3[2]);
			Assert.assertEquals(0, arr3[3]);
			LOG.debug("dataa: " + arr3[0]); // prints 114  which is 01110010
			LOG.debug("datab: " + arr3[1]); // prints -108 which is 10010100
			LOG.debug("datac: " + arr3[2]); // prints 107  which is 01101011
			LOG.debug("datad: " + arr3[3]); // prints 0    which is 00000000	
	}
	
	/*
	 * Send Buffer Test
	 */
	@Test
	public void bufferTest() throws SerialComException, InterruptedException, UnsupportedEncodingException{
		LOG.debug("bufferTest");
		
		/* 350 = 00000001 01011110, 650 = 00000010 10001010 , 2 bytes required, big endian format.
		 * In java numbers are in 2's complement so byte 10001010 in 650 will be -118 */
		int[] buf = {350, 650};
		scm.writeIntArray(handle1, buf, 0, ENDIAN.E_BIG, NUMOFBYTES.NUM_2);
		Thread.sleep(100);
		byte[] arr4 = new byte[4];
		arr4 = scm.readBytes(handle2);
		
		Assert.assertEquals(1, arr4[0]);
		Assert.assertEquals(94, arr4[1]);
		Assert.assertEquals(2, arr4[2]);
		Assert.assertEquals(-118, arr4[3]);
		LOG.debug("dataa: " + arr4[0]); // prints 1    which is 00000001
		LOG.debug("datab: " + arr4[1]); // prints 94   which is 01011110
		LOG.debug("datac: " + arr4[2]); // prints 2    which is 00000001
		LOG.debug("datad: " + arr4[3]); // prints -118 which is 10001010
	}	
}