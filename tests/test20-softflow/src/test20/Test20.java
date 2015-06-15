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

import java.util.concurrent.atomic.AtomicBoolean;

import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.SerialComManager.BAUDRATE;
import com.embeddedunveiled.serial.SerialComManager.DATABITS;
import com.embeddedunveiled.serial.SerialComManager.FLOWCONTROL;
import com.embeddedunveiled.serial.SerialComManager.PARITY;
import com.embeddedunveiled.serial.SerialComManager.STOPBITS;
import com.embeddedunveiled.serial.ISerialComDataListener;
import com.embeddedunveiled.serial.SerialComDataEvent;

class Data1 extends Test20 implements ISerialComDataListener{
	@Override
	public void onNewSerialDataAvailable(SerialComDataEvent data) {
		System.out.println("foun\n");
		byte[] b = data.getDataBytes();
		for(int a=0; a<b.length; a++) {
			if(b[a] == (byte) '#') {
				exit.set(true);
				System.out.println("found xoff : " + b[a]);
			}else if(b[a] == (byte) '$') {
				System.out.println("found xon : " + b[a]);
			}
		}
	}

	@Override
	public void onDataListenerError(int arg0) {
		System.out.println("" + arg0);
	}
}

/*
 * port will send xoff and xon after buffer limit is reached.
 */
public class Test20 {
	protected static AtomicBoolean exit = new AtomicBoolean(false);
	
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

//			char XON = (char) 17;  replace '$' with XON to  test with CTRL+Q
//			char XOFF = (char) 19; replace '#' with XOFF to test with CTRL+S
			Data1 receiver = new Data1();

			long receiverHandle = scm.openComPort(PORT, true, true, true);
			scm.configureComPortData(receiverHandle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(receiverHandle, FLOWCONTROL.SOFTWARE, '$', '#', false, false);

			long senderHandle = scm.openComPort(PORT1, true, true, true);
			scm.configureComPortData(senderHandle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(senderHandle, FLOWCONTROL.SOFTWARE, '$', '#', false, false);
			
			scm.registerDataListener(receiverHandle, receiver);

			for(int x=0; x<100; x++) {
				scm.writeString(receiverHandle, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0);
				if(exit.get() == true) {
					break;
				}
			}
			
			for(int x=0; x<10; x++) {
				scm.readString(senderHandle);
			}
			
			Thread.sleep(1000);
			scm.unregisterDataListener(receiver);
			scm.closeComPort(receiverHandle);
			scm.closeComPort(senderHandle);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
