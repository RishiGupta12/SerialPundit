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



public class SerialData7PortHandler extends BaseSerial1Test {
	
	/*
	 * portName() Test40
	 */
	@Test
	public void portName() throws SerialComException, InterruptedException{
		LOG.debug("portName()");
		String str = scm.getPortName(handle1);
		
		if(str == null) {
			LOG.debug("unable to find port name");
		}else {
			LOG.debug("port name : " + str);
		}

		String str1 = scm.getPortName(handle2);
		if(str1 == null) {
			LOG.debug("unable to find port name");
		}else {
			LOG.debug("port name : " + str1);
		}
		Assert.assertTrue(str != null);
		Assert.assertTrue(str1 != null);
	}
	
	/*
	 * sendEspecialCharacters() Test41
	 */
	@Test
	public void sendEspecialCharacters() throws SerialComException, InterruptedException{
		LOG.debug("sendEspecialCharacters()");
		
		// send \r\n and chek they are received at other end, OS does not strip them
		LOG.debug("write begin at time : " + System.currentTimeMillis());
		if(scm.writeBytes(handle1, "\r\nOK\r\n".getBytes(), 0) == true) {
			LOG.debug("write success");
		}
		Thread.sleep(10);
		
		// length must be 6
		byte[] data = scm.readBytes(handle2, 100);
		if(data != null && data.length > 0) {
			LOG.debug("data length : " + data.length + " time : " + System.currentTimeMillis());
		}
		Assert.assertEquals(6, data.length);
		Thread.sleep(100);		
	}
	
	/*
	 * cycleInstance() Test45
	 */
	@Test
	public void cycleInstance() throws SerialComException, InterruptedException{
		LOG.debug("cycleInstance()");
		int x = 0;
		for (x=0; x<100; x++) {
			System.out.println("Iteration :" + x);
			if (x == 0){scm.closeComPort(handle1);}
			handle1 = scm.openComPort(PORT1, true, true, true);
			scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle1, FLOWCONTROL.NONE, '$', '$', false, false);
			portWatcher pw = new portWatcher();
			scm.registerPortMonitorListener(handle1, pw);
			Thread.sleep(100);
			scm.unregisterPortMonitorListener(handle1);
			Thread.sleep(100);
			if (x != 99){scm.closeComPort(handle1);}
		}	
	}
	
	/*
	 * bytesShouldBeSame() Test46
	 */
	@Test
	public void bytesShouldBeSame() throws SerialComException, InterruptedException{
		LOG.debug("bytesShouldBeSame()");
		int x = 0;
		for(x=0; x<10; x++) {
			scm.writeString(handle2, "qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq", 0);
		}

		Thread.sleep(1000); // let data reach physically to port and OS buffer it
		byte[] data = null;
		int y[] = {1, 2, 3, 5, 9, 18, 25, 38, 128, 1000};
		int i = 0;
		while (i < 10){
			data = scm.readBytes(handle1, y[i]);
			LOG.debug("req nuber of read 1, " + "got " + data.length);
			i++;
		}
	}
	
	/*
	 * reOpeningPort() Test47
	 */
	@Test
	public void reOpeningPort() throws SerialComException, InterruptedException{
		LOG.debug("reOpeningPort()");
		scm.closeComPort(handle1);
		Long handle = scm.openComPort(PORT1, true, true, true);
		if (handle == null){
			handle = scm.openComPort(PORT1, true, true, true);
			LOG.error("Port is already open");
		}
		Assert.assertTrue(handle != null);
	}
	
	/*
	 * writeDelay() Test49
	 */
	@Test
	public void writeDelay() throws SerialComException, InterruptedException{
		LOG.debug("writeDelay()");
		
		// write with 1 second delay between each byte
		LOG.debug("write begin at time : " + System.currentTimeMillis());
		if(scm.writeBytes(handle1, "aaaaaaaaaa".getBytes(), 1000) == true) {
			LOG.debug("write success");
		}
		Thread.sleep(10);
		
		// this time should be greater than the time at which write started
		for (int x=0; x<8; x++) {
			byte[] data = scm.readBytes(handle2, 100);
			if(data != null && data.length > 0) {
				LOG.debug("data length : " + data.length + " time : " + System.currentTimeMillis());
				Assert.assertEquals(10, data.length);
			}
		}

	}
}
