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

package test80;

import java.io.File;

import com.embeddedunveiled.serial.ISerialComXmodemProgress;
import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.SerialComManager.BAUDRATE;
import com.embeddedunveiled.serial.SerialComManager.DATABITS;
import com.embeddedunveiled.serial.SerialComManager.FLOWCONTROL;
import com.embeddedunveiled.serial.SerialComManager.FTPPROTO;
import com.embeddedunveiled.serial.SerialComManager.FTPVAR;
import com.embeddedunveiled.serial.SerialComManager.PARITY;
import com.embeddedunveiled.serial.SerialComManager.STOPBITS;
import com.embeddedunveiled.serial.SerialComXModemAbort;

class Send extends Test80 implements Runnable, ISerialComXmodemProgress {

	public SerialComXModemAbort transferStatea = new SerialComXModemAbort();

	@Override
	public void run() {
		try {
			long handle1 = scm.openComPort(PORT1, true, true, true);
			scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);

			// text mode
			boolean statusc = scm.sendFile(handle1, new File(sndtfilepath), FTPPROTO.XMODEM, FTPVAR.CHKSUM, true, this, transferStatea);
			System.out.println("\nsent text status : " + statusc);

			done = true;

			// binary mode
			boolean statusb = scm.sendFile(handle1, new File(sndbfilepath), FTPPROTO.XMODEM, FTPVAR.CHKSUM, false, this, transferStatea);
			System.out.println("\nsent binary status : " + statusb);

			scm.closeComPort(handle1);
			System.out.println("sender done !");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onXmodemSentProgressUpdate(long arg0, int percent) {
		System.out.println("text block number sent : " + arg0 + ", percent : " + percent);
	}
	@Override
	public void onXmodemReceiveProgressUpdate(long arg0) {	
	}
}

// send file from one thread and receive from other using XMODEM checksum protocol 
public class Test80 implements ISerialComXmodemProgress {

	private static SerialComXModemAbort transferStatec = new SerialComXModemAbort();
	private static Thread mThread = null;
	public static SerialComManager scm = null;
	public static String PORT = null;
	public static String PORT1 = null;
	public static String sndtfilepath = null;
	public static String rcvtfilepath = null;
	public static String sndbfilepath = null;
	public static String rcvbfilepath = null;
	public static boolean done = false;

	public static void main(String[] args) {
		try {
			scm = new SerialComManager();

			int osType = scm.getOSType();
			if(osType == SerialComManager.OS_LINUX) {
				PORT = "/dev/ttyUSB0";
				PORT1 = "/dev/ttyUSB1";
				sndtfilepath = "/home/r/tmp/atsnd.txt";
				rcvtfilepath = "/home/r/tmp/atrcv.txt";
				sndbfilepath = "/home/r/tmp/absnd.jpg";
				rcvbfilepath = "/home/r/tmp/abrcv.jpg";
			}else if(osType == SerialComManager.OS_WINDOWS) {
				PORT = "COM51";
				PORT1 = "COM52";
				sndtfilepath = "D:\\atsnd.txt";
				rcvtfilepath = "D:\\atrcv.txt";
			}else if(osType == SerialComManager.OS_MAC_OS_X) {
				PORT = "/dev/cu.usbserial-A70362A3";
				PORT1 = "/dev/cu.usbserial-A602RDCH";
			}else if(osType == SerialComManager.OS_SOLARIS) {
				PORT = null;
				PORT1 = null;
			}else{
			}

			PORT = "/dev/pts/2";
			PORT1 = "/dev/pts/4";

			long handle = scm.openComPort(PORT, true, true, true);
			scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);

			mThread = new Thread(new Send());
			mThread.start();

			// ascii text mode
			boolean status = scm.receiveFile(handle, new File(rcvtfilepath), FTPPROTO.XMODEM, FTPVAR.CHKSUM, true, new Test80(), transferStatec);
			System.out.println("\nreceived status text : " + status);

			while(done == false) { 
				Thread.sleep(10);
			}

			// binary mode
			boolean statusa = scm.receiveFile(handle, new File(rcvbfilepath), FTPPROTO.XMODEM, FTPVAR.CHKSUM, false, new Test80(), transferStatec);
			System.out.println("\nreceived status binary : " + statusa);

			scm.closeComPort(handle);

			System.out.println("receiver done !");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onXmodemReceiveProgressUpdate(long arg0) {
		System.out.println("text block number received : " + arg0);
	}
	@Override
	public void onXmodemSentProgressUpdate(long arg0, int percent) {
	}
}
