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

package test54;

import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.SerialComInByteStream;
import com.embeddedunveiled.serial.SerialComManager.BAUDRATE;
import com.embeddedunveiled.serial.SerialComManager.DATABITS;
import com.embeddedunveiled.serial.SerialComManager.FLOWCONTROL;
import com.embeddedunveiled.serial.SerialComManager.PARITY;
import com.embeddedunveiled.serial.SerialComManager.SMODE;
import com.embeddedunveiled.serial.SerialComManager.STOPBITS;

class ClosePort extends Test54 implements Runnable {
	@Override
	public void run() {
		try {
			Thread.sleep(500); // make sure closed is called after read is blocked
			System.out.println("closing stream...");
			in.close();
			System.out.println("closed stream.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

// test if port is closed port while read was blocked 
public class Test54 {

	private static Thread mThread = null;
	public static long handle = 0;
	public static SerialComManager scm = null;
	public static SerialComInByteStream in = null;

	public static void main(String[] args) {
		try {
			scm = new SerialComManager();

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

			PORT = "/dev/pts/1";
			PORT1 = "/dev/pts3";

			handle = scm.openComPort(PORT, true, true, true);
			scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);

			// test 1
			in = scm.createInputByteStream(handle, SMODE.BLOCKING);
			mThread = new Thread(new ClosePort());
			mThread.start();
			System.out.println("1- created input stream, proccedding to call read which will block because of no data !");
			in.read();
			System.out.println("main thread, in.read() returned from blocked read !");

			Thread.sleep(1000); // let the previous stream be closed and removed from information object in SerialComManager class
			
			// test 2
			in = scm.createInputByteStream(handle, SMODE.BLOCKING);
			mThread = new Thread(new ClosePort());
			mThread.start();
			System.out.println("\n1- created input stream, proccedding to call read which will block because of no data !");
			byte[] b = new byte[50];
			in.read(b);
			System.out.println("main thread, in.read(b) returned from blocked read !");

			scm.closeComPort(handle);
			System.out.println("closed serial port.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
