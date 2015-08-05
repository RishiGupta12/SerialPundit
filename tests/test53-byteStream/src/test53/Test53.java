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

			long handle = scm.openComPort(PORT, true, true, true);
			scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);
			long handle1 = scm.openComPort(PORT1, true, true, true);
			scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
			
			SerialComOutByteStream out = scm.createOutputByteStream(handle, SMODE.NONBLOCKING);
			SerialComInByteStream in = scm.createInputByteStream(handle1, SMODE.NONBLOCKING);
			
			/* comment out and test re-creation of stream will throw exception. 
			out = scm.createOutputByteStream(handle, SMODE.BLOCKING);
			in = scm.createInputByteStream(handle1, SMODE.BLOCKING); */
			
			out.write(65); // print ASCII value of A in GUI application
			int x = in.read();
			System.out.println("x : " + x);
			
			byte[] b = new byte[50];
			out.write("HELLO WORLD".getBytes());
			Thread.sleep(500);
			int y = in.read(b);
			System.out.println("y : " + y + " b : " + new String(b));
			
			byte[] bb = new byte[50];
			out.write("ABCDEFGHIJKL".getBytes());
			Thread.sleep(500);
			int z = in.read(bb, 4, 3);
			System.out.println("z : " + z);
			System.out.println("bb6 : " + bb[4]); // print 65 ASCII value of A
			System.out.println("bb7 : " + bb[5]); // print 66 ASCII value of B
			System.out.println("bb8 : " + bb[6]); // print 67 ASCII value of C
			
			in.close();
			out.close();
			
			for(int a=0; a<5000; a++) {
				System.out.println("itertaion :" + a);
				SerialComOutByteStream outa = scm.createOutputByteStream(handle, SMODE.NONBLOCKING);
				SerialComInByteStream ina = scm.createInputByteStream(handle, SMODE.NONBLOCKING);
				ina.close();
				outa.close();
			}
			
			scm.closeComPort(handle);
			scm.closeComPort(handle1);
			
			handle = scm.openComPort(PORT, true, true, true);
			scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);
			handle1 = scm.openComPort(PORT1, true, true, true);
			scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
			
			out = scm.createOutputByteStream(handle, SMODE.BLOCKING);
			in = scm.createInputByteStream(handle1, SMODE.BLOCKING);
			
			out.write(65); // print ASCII value of A in GUI application
			x = in.read();
			System.out.println("x : " + x);
			
			b = new byte[50];
			out.write("HELLO WORLD".getBytes());
			Thread.sleep(500);
			y = in.read(b);
			System.out.println("y : " + y + " b : " + new String(b));
			
			bb = new byte[50];
			out.write("KLMHFTRYUEWAQ".getBytes());
			Thread.sleep(500);
			z = in.read(bb, 4, 3);
			System.out.println("z : " + z);
			System.out.println("bb6 : " + bb[4]); // print 75 ASCII value of K
			System.out.println("bb7 : " + bb[5]); // print 76 ASCII value of L
			System.out.println("bb8 : " + bb[6]); // print 77 ASCII value of M
			
			in.close();
			out.close();
			
			scm.closeComPort(handle);
			scm.closeComPort(handle1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
