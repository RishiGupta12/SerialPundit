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

public class SerialData8Status extends BaseSerial1Test{
	
	/*
	 * lineStatus() Test52
	 */
	@Test
	public void lineStatus() throws SerialComException, InterruptedException{
		LOG.debug("lineStatus()");
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
	 * iOPutStream() Test53
	 */
	@Test
	public void iOPutStream() throws SerialComException, InterruptedException{
		LOG.debug("iOPutStream()");
		SerialComOutByteStream out = scm.createOutputByteStream(handle1);
		SerialComInByteStream in = scm.createInputByteStream(handle2);
		try {
			//We write int 65. Read int.
			out.write(65); 
			int x = in.read();
			LOG.debug("We sent 65 and we receive : " + x);
			//We write String.getBytes. We read Bytes. 
			out.write("hello world".getBytes());
			Thread.sleep(500);
			byte[] b = new byte[50];
			in.read(b);
			LOG.debug("We sent 'hello world' and we receive: " + new String(b));
			//We write String.getBytes. We read 
			out.write("ABC".getBytes());
			Thread.sleep(500);
			byte[] bb = new byte[50];
			in.read(bb, 6, 3);
			LOG.debug("Print ASCII value of A : " + bb[6]); // print 65 ASCII value of A
			LOG.debug("Print ASCII value of B : " + bb[7]); // print 66 ASCII value of B
			LOG.debug("Print ASCII value of C : " + bb[8]); // print 67 ASCII value of C
		} catch (Exception e){
			LOG.error("iOPutStream()" + e);
		}
	}
	
	/*
	 * closePort() Test54
	 */
	private static Thread mThread = null;
	class ClosePort implements Runnable {
		@Override
		public void run() {
			try {
				Thread.sleep(5000); // make sure closed is called after read is blocked
				LOG.debug("closing");
				scm.closeComPort(handle1);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	@Test
	public void closePort() throws SerialComException, InterruptedException{
		LOG.debug("closePort()");
		mThread = new Thread(new ClosePort());
		mThread.start();
		try {
			SerialComInByteStream in = scm.createInputByteStream(handle1);
			byte[] b = new byte[50];
			in.read(b);
			LOG.debug("b : " + new String(b));
			
			LOG.debug("out of read");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
