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

package test20;

import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.SerialComManager.BAUDRATE;
import com.embeddedunveiled.serial.SerialComManager.DATABITS;
import com.embeddedunveiled.serial.SerialComManager.FLOWCONTROL;
import com.embeddedunveiled.serial.SerialComManager.PARITY;
import com.embeddedunveiled.serial.SerialComManager.STOPBITS;
import com.embeddedunveiled.serial.ISerialComDataListener;
import com.embeddedunveiled.serial.SerialComDataEvent;

class Data0 implements ISerialComDataListener{
	@Override
	public void onNewSerialDataAvailable(SerialComDataEvent data) {
		System.out.println("Sender got from receiver : " + new String(data.getDataBytes()));
	}

	@Override
	public void onDataListenerError(int arg0) {
	}
}

class Data1 implements ISerialComDataListener{
	@Override
	public void onNewSerialDataAvailable(SerialComDataEvent data) {
		System.out.println("Receiver got from sender : " + new String(data.getDataBytes()));
	}

	@Override
	public void onDataListenerError(int arg0) {
	}
}

public class Test20 {
	public static void main(String[] args) {
		try {
			SerialComManager scm = new SerialComManager();

			String PORT = null;
			String PORT1 = null;
			int osType = SerialComManager.getOSType();
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

			byte[] XON  = new byte[] {(byte) 0x24};   // ASCII value of $ character is 0x24
			byte[] XOFF = new byte[] {(byte) 0x23};   // ASCII value of # character is 0x23

			Data1 receiver = new Data1();
			Data0 sender = new Data0();

			// open and configure port that will listen data
			long receiverHandle = scm.openComPort(PORT, true, true, true);
			scm.configureComPortData(receiverHandle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(receiverHandle, FLOWCONTROL.SOFTWARE, '$', '#', false, false);

			scm.registerDataListener(receiverHandle, receiver);

			// open and configure port which will send data
			long senderHandle = scm.openComPort(PORT1, true, true, true);
			scm.configureComPortData(senderHandle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(senderHandle, FLOWCONTROL.SOFTWARE, '$', '#', false, false);
			scm.registerDataListener(senderHandle, sender);

			// Step 1
			scm.writeString(senderHandle, "str1", 0);
			scm.writeString(receiverHandle, "str2", 0);
			Thread.sleep(1000);

			// Step 2
			scm.writeBytes(receiverHandle, XOFF, 0);
			Thread.sleep(200);

			// Step 3
			scm.writeString(senderHandle, "str3", 0);

			// Step 4
			Thread.sleep(4000);

			// Step 5
			scm.writeBytes(receiverHandle, XON, 0);
			Thread.sleep(100000);

			scm.unregisterDataListener(sender);
			scm.unregisterDataListener(receiver);
			scm.closeComPort(receiverHandle);
			scm.closeComPort(senderHandle);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}