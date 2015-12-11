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


public class SerialData6CheckWR extends BaseSerial1Test {
	
	
	/*
	 * lengthWRData() Test30: what happens if port is removed while data/event listeners exist
	 */
	@Test
	public void lenghtWRData() throws SerialComException, InterruptedException{
		LOG.debug("lengthWRData()");
		int x = 0;
		byte[] data = new byte[4096];
		for(x=0; x<data.length; x++ ) {
			data[x] = (byte) 'a';
		}
		scm.writeBytes(handle1, data, 0);
		Thread.sleep(50);
		
		String str = scm.readString(handle2);
		LOG.debug("data length read : " + str.length());
		String str1 = scm.readString(handle2);
		LOG.debug("data length read : " + str1.length());
		String str2 = scm.readString(handle2);
		LOG.debug("data length read : " + str2.length());
		
		String str3 = scm.readString(handle2);		
		Assert.assertEquals(str3.length(), 1024);
		LOG.debug("data length read : " + str3.length());
		
		scm.writeBytes(handle1, data, 0);
		Thread.sleep(50);
		
		String str4 = scm.readString(handle2, 2000);
		Assert.assertEquals(str4.length(), 2000);
		LOG.debug("data length read : " + str4.length());
	}
	
	/*
	 * writeAndPortRemoved() Test32: print error, return false if write in progress and port removed.
	 */
	/*
	 * readAndPortRemoved() Test33
	 */
	
	/*
	 * twoInstancesOfSCM() Test34: We need another test for complet this test
	 */
	@Test
	public void twoInstancesOfSCM() throws SerialComException, InterruptedException{
		LOG.debug("twoInstancesOfSCM");
		scm.getLibraryVersions();
		//	Thread.sleep(3000);
		scm.writeString(handle1, "testing", 0);
		Thread.sleep(3000);
		LOG.debug(" : " + scm.readString(handle2));
	}
	
	/*
	 * fastWrtieAndRead() Test35 
	 */
	@Test
	public void fastWriteAndRead() throws SerialComException, InterruptedException{
		LOG.debug("fastWriteAndRead");
		for(int x = 0; x<200; x++) {
			LOG.debug("Iteration : " + x);
			scm.writeString(handle1, "1111111111", 0);
			
			int soType = scm.getOSType();
			if(soType == SerialComManager.OS_LINUX) {
				Thread.sleep(10);
			}else if(soType == SerialComManager.OS_WINDOWS) {
				Thread.sleep(500);
			}else if(soType == SerialComManager.OS_MAC_OS_X) {
				Thread.sleep(500);
			}else if(soType == SerialComManager.OS_SOLARIS) {
				Thread.sleep(500);
			}else{
			}
			byte[] bb = scm.readBytes(handle2, 102);
			LOG.debug("read STR : " + new String(bb));
		}
	}
	
	/*
	 * JVMWithLotOfUsageMemory() Test36
	 */
	@Test
	public void JVMWithLotOfUsageMemory() throws SerialComException, InterruptedException{
		LOG.debug("JVMWithLotOfUsageMemory");
		class TestThreads implements Runnable {
			@Override
			public void run() {
				byte[] bb = new byte[1000*1024];
				LOG.debug("Bytes: " + bb);
				while(true);
			}
		}
		
		int x = 0;
		
		Thread thread = null;
		for(x=0; x<10; x++) {
			thread = new Thread(new TestThreads());
			thread.start();
		}
		x = 0;
		for(x=0; x<10; x++) {
			scm.writeString(handle1, "aaaaaaaaaaaafvaddddddddddddddddddddddddddddddddddddaaaaaaaaaaaaaaaaaaa", 0);
			scm.readString(handle2, 20);
		}
	}
	
	/*
	 * transferDocuments() Test37 Test38
	 *
	@Test
	public void transferDocuments() throws SerialComException, InterruptedException{
		LOG.debug("transferDocuments()");
		try {
			scm.sendFile(handle1, new File("/home/pedro/Escritorio/a.txt"), FILETXPROTO.XMODEM);
			Thread.sleep(20);			
		} catch (Exception e){
			LOG.error("SerialCheck. sendDocs: " + e);
		}
		try{
			scm.receiveFile(handle2, new File("/home/pedro/Escritorio/b.txt"), FILETXPROTO.XMODEM);
			Thread.sleep(20);
		} catch (Exception e){
			LOG.error("SerialCheck. receiveDocs: " + e);
		}
	}*/
	
	/*
	 * requestAmountOfBytes() Test39
	 */
	@Test
	public void requestAmountOfBytes() throws SerialComException, InterruptedException{
		LOG.debug("requestAmountOfBytes()");
		String testStr = "Files were transferred one packet at a time. When received, the packet's checksum was calculated by the receiver and compared to the one received from the sender at the end of the packet.";
		int i = 0;
		while (i != 5){
			testStr = testStr + testStr;
			i++;
		}
		LOG.debug("Before Send Data length: " + testStr.length());
		LOG.debug("register  : " + scm.registerDataListener(handle1, dataListener));
		scm.writeString(handle2, testStr, 0);
		Thread.sleep(5000);
		LOG.debug("unregister : " + scm.unregisterDataListener(dataListener));
	}
}
