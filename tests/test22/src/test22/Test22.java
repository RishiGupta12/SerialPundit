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
 
 
 package test22;

import com.embeddedunveiled.serial.ISerialComEventListener;
import com.embeddedunveiled.serial.SerialComLineEvent;
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
		System.out.println("DCE GOT FROM DTE : " + new String(data.getDataBytes()));
	}
}

class Data1 implements ISerialComDataListener{
	@Override
	public void onNewSerialDataAvailable(SerialComDataEvent data) {
		System.out.println("DTE GOT FROM DCE : " + new String(data.getDataBytes()));
	}
}

class EventListener extends Test22 implements ISerialComEventListener {
	@Override
	public void onNewSerialEvent(SerialComLineEvent lineEvent) {
		System.out.println("eventCTS : " + lineEvent.getCTS());
		senddata = false;
	}
}

public class Test22 {
	
	static boolean senddata = true;
	
	public static void main(String[] args) {
		
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
		
		Data1 DTE1 = new Data1();
		Data0 DCE1 = new Data0();
		EventListener eventListener = new EventListener();
		
		try {
			// DTE terminal
			long DTE = scm.openComPort(PORT, true, true, true);
			scm.configureComPortData(DTE, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
			scm.configureComPortControl(DTE, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
			scm.registerDataListener(DTE, DTE1);
			scm.setRTS(DTE, true);

			Thread.sleep(100);
			
			// DCE terminal
			long DCE = scm.openComPort(PORT1, true, true, true);
			scm.configureComPortData(DCE, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
			scm.configureComPortControl(DCE, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
			scm.registerDataListener(DCE, DCE1);
			scm.registerLineEventListener(DCE, eventListener);
			
			scm.setDTR(DTE, true);
			scm.setDTR(DCE, true);
			Thread.sleep(100);
			scm.setRTS(DTE, true);
			scm.setRTS(DCE, true);
			Thread.sleep(100);
			
			// Step 1
			scm.writeString(DTE, "str1", 0);
			Thread.sleep(100);
			scm.writeString(DTE, "str1", 0);
			Thread.sleep(100);
			scm.writeString(DCE, "str2", 0);
			Thread.sleep(100);
			scm.writeString(DCE, "str2", 0);
			Thread.sleep(100);
			
			// Step 2 dte says to dce don't send data i am full
			scm.setRTS(DTE, false);
			Thread.sleep(1000); // give delay so that send data gets updated
			
			// Step 3 dce will receive event CTS and will start sending data.
			if(senddata == true) {
				scm.writeString(DCE, "str3", 0);
			}else {
				System.out.println("seems like DTE is full");
			}
			
			Thread.sleep(1000);
			scm.unregisterDataListener(DTE1);
			scm.unregisterDataListener(DCE1);
			scm.unregisterLineEventListener(eventListener);
			scm.closeComPort(DTE);
			scm.closeComPort(DCE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
