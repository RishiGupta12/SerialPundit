/*
 * This file is part of SerialPundit.
 * 
 * Copyright (C) 2014-2016, Rishi Gupta. All rights reserved.
 *
 * The SerialPundit is DUAL LICENSED. It is made available under the terms of the GNU Affero 
 * General Public License (AGPL) v3.0 for non-commercial use and under the terms of a commercial 
 * license for commercial use of this software. 
 * 
 * The SerialPundit is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package test53;

import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComSystemProperty;
import com.serialpundit.serial.SerialComManager;
import com.serialpundit.serial.SerialComManager.BAUDRATE;
import com.serialpundit.serial.SerialComManager.DATABITS;
import com.serialpundit.serial.SerialComManager.FLOWCONTROL;
import com.serialpundit.serial.SerialComManager.PARITY;
import com.serialpundit.serial.SerialComManager.STOPBITS;
import com.serialpundit.serial.SerialComOutByteStream;
import com.serialpundit.serial.SerialComInByteStream;
import com.serialpundit.serial.SerialComManager.SMODE;

public final class Test53 {

	public static void main(String[] args) {
		try {
			SerialComManager scm = new SerialComManager();
			SerialComPlatform scp = new SerialComPlatform(new SerialComSystemProperty());
			SerialComOutByteStream out = null;
			SerialComInByteStream in = null;

			String PORT = null;
			String PORT1 = null;
			int osType = scp.getOSType();
			if(osType == SerialComPlatform.OS_LINUX) {
				PORT = "/dev/ttyUSB0";
				PORT1 = "/dev/ttyUSB1";
			}else if(osType == SerialComPlatform.OS_WINDOWS) {
				PORT = "COM51";
				PORT1 = "COM52";
			}else if(osType == SerialComPlatform.OS_MAC_OS_X) {
				PORT = "/dev/cu.usbserial-A70362A3";
				PORT1 = "/dev/cu.usbserial-A602RDCH";
			}else if(osType == SerialComPlatform.OS_SOLARIS) {
				PORT = null;
				PORT1 = null;
			}else{
			}

			/* tests with non-blocking mode */
			long handle = scm.openComPort(PORT, true, true, true);
			scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);

			long handle1 = scm.openComPort(PORT1, true, true, true);
			scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);

			out = (SerialComOutByteStream) scm.getIOStreamInstance(SerialComManager.OutputStream, handle, SMODE.NONBLOCKING);
			in = (SerialComInByteStream) scm.getIOStreamInstance(SerialComManager.InputStream, handle1, SMODE.NONBLOCKING);

			/* must throw exception as stream already exist for given handle */
			try {
				out = (SerialComOutByteStream) scm.getIOStreamInstance(SerialComManager.OutputStream, handle, SMODE.NONBLOCKING);
			} catch (Exception e) {
				e.printStackTrace();
			}

			/* must throw exception as stream already exist for given handle */
			try {
				in = (SerialComInByteStream) scm.getIOStreamInstance(SerialComManager.InputStream, handle1, SMODE.NONBLOCKING);
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				out.write(65); // print ASCII value of A in GUI application
				int x = in.read();
				System.out.println("x : " + x);
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				byte[] b = new byte[50];
				out.write("HELLO WORLD".getBytes());
				Thread.sleep(200);
				int y = in.read(b);
				System.out.println("y : " + y + " b : " + new String(b));
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				byte[] bb = new byte[50];
				out.write("ABCDEFGHIJKL".getBytes());
				Thread.sleep(200);
				int z = in.read(bb, 4, 3);
				System.out.println("z : " + z);
				System.out.println("bb6 : " + bb[4]); // print 65 ASCII value of A
				System.out.println("bb7 : " + bb[5]); // print 66 ASCII value of B
				System.out.println("bb8 : " + bb[6]); // print 67 ASCII value of C
			} catch (Exception e) {
				e.printStackTrace();
			}

			in.close();
			out.close();

			scm.closeComPort(handle);
			scm.closeComPort(handle1);

			/* tests with blocking mode */
			handle = scm.openComPort(PORT, true, true, true);
			scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);
			handle1 = scm.openComPort(PORT1, true, true, true);
			scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);

			in = (SerialComInByteStream) scm.getIOStreamInstance(SerialComManager.InputStream, handle, SMODE.BLOCKING);
			out = (SerialComOutByteStream) scm.getIOStreamInstance(SerialComManager.OutputStream, handle1, SMODE.BLOCKING);

			try {
				out.write(65); // print ASCII value of A in GUI application
				int x = in.read();
				System.out.println("x : " + x);
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				byte[] b = new byte[50];
				out.write("HELLO WORLD".getBytes());
				Thread.sleep(200);
				int y = in.read(b);
				System.out.println("y : " + y + " b : " + new String(b));
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				byte[] bb = new byte[50];
				out.write("ABCDEFGHIJKL".getBytes());
				Thread.sleep(200);
				int z = in.read(bb, 4, 3);
				System.out.println("z : " + z);
				System.out.println("bb6 : " + bb[4]); // print 65 ASCII value of A
				System.out.println("bb7 : " + bb[5]); // print 66 ASCII value of B
				System.out.println("bb8 : " + bb[6]); // print 67 ASCII value of C
			} catch (Exception e) {
				e.printStackTrace();
			}

			in.close();
			out.close();

			/* stress testing */
			for(int a=0; a<5000; a++) {
				System.out.println("itertaion :" + a);				
				SerialComInByteStream ina = (SerialComInByteStream) scm.getIOStreamInstance(SerialComManager.InputStream, handle, SMODE.NONBLOCKING);
				SerialComOutByteStream outa = (SerialComOutByteStream) scm.getIOStreamInstance(SerialComManager.OutputStream, handle, SMODE.NONBLOCKING);
				ina.close();
				outa.close();
			}

			scm.closeComPort(handle);
			scm.closeComPort(handle1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
