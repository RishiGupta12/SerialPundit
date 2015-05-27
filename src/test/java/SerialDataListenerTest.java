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


public class SerialDataListenerTest extends BaseSerialTest{
	
	/*
	 * minDataLength
	 */
	@Test
	public void minDataLength() throws SerialComException, InterruptedException{
		LOG.debug("minDataLength()");
		// register data listener for this port
		Boolean datalist = scm.registerDataListener(handle1, dataListener);
		LOG.debug("registerDataListener: " + datalist);
		scm.setMinDataLength(handle1, 5);
		
		scm.writeString(handle2, "test", 0);
		Thread.sleep(1000);

		// although test string has been transmitted, but listener will not get called because test has only
		// 4 bytes where as we set minimum length as 5, so let us transmit 1 more byte and listener will
		// get called.
		scm.writeString(handle2, "H", 0);
		Thread.sleep(1000);
		
		// unregister data listener
		scm.unregisterDataListener(dataListener);
	}
	
	/*
	 * dataToBeDisplayed
	 */
	
	@Test
	public void dataToBeDisplayed() throws SerialComException, InterruptedException{
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
	
}
