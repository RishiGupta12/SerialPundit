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

public class SerialData4EventListenerTest extends BaseSerialEventListenerTest{
	
	
	/*
	 * EventListener
	 */
	@Test
	public void eventListener() throws SerialComException, InterruptedException{
				
		LOG.debug("eventListener");
		
		// both event will be called
		Thread.sleep(1000);
		LOG.debug("handle2: " + handle2);
		scm.setRTS(handle2, false);
		Thread.sleep(1000);
		scm.setDTR(handle2, false);

		// mask CTS, so only changes to CTS line will be reported.
		scm.setEventsMask(eventListener, SerialComManager.CTS);
		Thread.sleep(1000);
		scm.setRTS(handle2, true);
		Thread.sleep(1000);
		scm.setDTR(handle2, true);
		Thread.sleep(1000);
		scm.setRTS(handle2, false);
		Thread.sleep(1000);
		scm.setDTR(handle2, false);

		Thread.sleep(1000);					
	}
	
	/*
	 * eventsMask
	 */
	@Test
	public void eventMask() throws SerialComException, InterruptedException{
		
		LOG.debug("eventMask()");
		
		// get current active mask
		int mask1 = scm.getEventsMask(eventListener);
		System.out.println("mask before : " + mask1);

		// mask CTS, so only changes to CTS line will be reported.
		scm.setEventsMask(eventListener, SerialComManager.CTS);

		// get current active mask
		int mask2 = scm.getEventsMask(eventListener);
		System.out.println("mask after : " + mask2);
	}
}
