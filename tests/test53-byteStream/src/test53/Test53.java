/**
 * Author : Rishi Gupta
 * 
 * This file is part of 'serial communication manager' library.
 * Copyright (C) <2014-2016>  <Rishi Gupta>
 *
 * This 'serial communication manager' is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by the Free Software 
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * The 'serial communication manager' is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR 
 * A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with 'serial communication manager'.  If not, see <http://www.gnu.org/licenses/>.
 */

package test53;

import com.embeddedunveiled.serial.SerialComInByteStream;
import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.SerialComManager.SMODE;
import com.embeddedunveiled.serial.SerialComOutByteStream;
import com.embeddedunveiled.serial.SerialComManager.BAUDRATE;
import com.embeddedunveiled.serial.SerialComManager.DATABITS;
import com.embeddedunveiled.serial.SerialComManager.FLOWCONTROL;
import com.embeddedunveiled.serial.SerialComManager.PARITY;
import com.embeddedunveiled.serial.SerialComManager.STOPBITS;

public final class Test53 {

	public static void main(String[] args) {
		try {
			SerialComManager scm = new SerialComManager();
			SerialComOutByteStream out = null;
			SerialComInByteStream in = null;

			String PORT = null;
			String PORT1 = null;
			int osType = scm.getOSType();
			if(osType == SerialComManager.OS_LINUX) {
				PORT = "/dev/ttyUSB0";
				PORT1 = "/dev/ttyUSB1";
			}else if(osType == SerialComManager.OS_WINDOWS) {
				PORT = "COM51";
				PORT1 = "COM52";
			}else if(osType == SerialComManager.OS_MAC_OS_X) {
				PORT = "/dev/cu.usbserial-A70362A3";
				PORT1 = "/dev/cu.usbserial-A602RDCH";
			}else if(osType == SerialComManager.OS_SOLARIS) {
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

			out = (SerialComOutByteStream) scm.createIOStream(SerialComManager.OutputStream, handle, SMODE.NONBLOCKING);
			in = (SerialComInByteStream) scm.createIOStream(SerialComManager.InputStream, handle1, SMODE.NONBLOCKING);

			/* must throw exception as stream already exist for given handle */
			try {
				out = (SerialComOutByteStream) scm.createIOStream(SerialComManager.OutputStream, handle, SMODE.NONBLOCKING);
			} catch (Exception e) {
				e.printStackTrace();
			}

			/* must throw exception as stream already exist for given handle */
			try {
				in = (SerialComInByteStream) scm.createIOStream(SerialComManager.InputStream, handle1, SMODE.NONBLOCKING);
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

//			in.close();
//			out.close();

			scm.closeComPort(handle);
			scm.closeComPort(handle1);

			/* tests with blocking mode */
			handle = scm.openComPort(PORT, true, true, true);
			scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);
			handle1 = scm.openComPort(PORT1, true, true, true);
			scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);

			out = (SerialComOutByteStream) scm.createIOStream(SerialComManager.OutputStream, handle, SMODE.BLOCKING);
			in = (SerialComInByteStream) scm.createIOStream(SerialComManager.InputStream, handle1, SMODE.NONBLOCKING);

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
				SerialComOutByteStream outa = (SerialComOutByteStream) scm.createIOStream(SerialComManager.OutputStream, handle, SMODE.NONBLOCKING);
				SerialComInByteStream ina = (SerialComInByteStream) scm.createIOStream(SerialComManager.InputStream, handle1, SMODE.NONBLOCKING);
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
