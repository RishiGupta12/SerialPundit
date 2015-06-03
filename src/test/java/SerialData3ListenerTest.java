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


public class SerialData3ListenerTest extends BaseSerial1Test{
	
	/*
	 * registerData() Test14 Solution: "test" and "H" 
	 */
	@Test
	public void registerData() throws SerialComException, InterruptedException{
		LOG.debug("registerData()");
		
		// register data listener for this port
		scm.registerDataListener(handle1, dataListener);
		scm.fineTuneRead(handle1, 6, 1, 0, 0, 0);
		scm.writeString(handle2, "test", 0);
		Thread.sleep(1000);

		// although "test" string (4 bytes) have been transmitted, but listener will not get called because test
		// has only 4 bytes where as we set minimum length as 5, so let us transmit 1 more byte and listener will
		// get called.
		scm.writeString(handle2, "H", 0);
		Thread.sleep(1000);

		// unregister data listener
		scm.unregisterDataListener(dataListener);
		
		scm.writeString(handle2, "t", 0);
		Thread.sleep(1000);
		byte[] data = scm.readBytes(handle1);
		Assert.assertEquals(1, data.length);
		LOG.debug("length : " + data.length);
	}
	
	/*
	 * bufferTest()
	 */
	
	@Test
	public void bufferTest() throws SerialComException, InterruptedException{
		LOG.debug("bufferTest()");
		
		// register data listener for this port
		LOG.debug("1 " + scm.registerDataListener(handle1, dataListener));
		// wait for data to be displayed on console
		// modified: we sleep after of each writing
		scm.writeString(handle2, "test", 0);
		Thread.sleep(100);
		LOG.debug("2 " + scm.unregisterDataListener(dataListener));

		Thread.sleep(100);
		LOG.debug("3 " + scm.registerDataListener(handle1, dataListener));

		scm.writeString(handle2, "test string", 0);
		Thread.sleep(100);
		LOG.debug("4 " + scm.unregisterDataListener(dataListener));

		Thread.sleep(100);
		LOG.debug("5 " + scm.registerDataListener(handle1, dataListener));

		scm.writeString(handle2, "test string", 0);
		Thread.sleep(100);
		LOG.debug("6 " + scm.unregisterDataListener(dataListener));
		Thread.sleep(100);
	}
	
	/*
	 * dataLoop() Test19
	 */
	@Test
	public void dataLoop() throws SerialComException, InterruptedException{
		LOG.debug("dataLoop");
		
		int x = 0;
		while(x != 50) {
			LOG.debug("1==" + x + scm.registerDataListener(handle1, dataListener));
			Thread.sleep(100);
			scm.writeString(handle2, "T", 0);
			Thread.sleep(100);
			LOG.debug("2" + scm.unregisterDataListener(dataListener));
			x++;
		}
	}
	
}
