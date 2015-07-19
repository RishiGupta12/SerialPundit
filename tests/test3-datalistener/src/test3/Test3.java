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

package test3;

import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.SerialComManager.BAUDRATE;
import com.embeddedunveiled.serial.SerialComManager.DATABITS;
import com.embeddedunveiled.serial.SerialComManager.FLOWCONTROL;
import com.embeddedunveiled.serial.SerialComManager.PARITY;
import com.embeddedunveiled.serial.SerialComManager.STOPBITS;
import com.embeddedunveiled.serial.ISerialComDataListener;
import com.embeddedunveiled.serial.SerialComDataEvent;

class Data implements ISerialComDataListener{
	@Override
	public void onNewSerialDataAvailable(SerialComDataEvent data) {
		System.out.println("Read from serial port : " + new String(data.getDataBytes()));
		System.out.println("data length : " + data.getDataBytesLength() );
	}
	@Override
	public void onDataListenerError(int arg0) {
		System.out.println("onDataListenerError called " + arg0);
	}
}

public class Test3 {
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

			// instantiate class which is will implement ISerialComDataListener interface
			Data dataListener = new Data();

			// open and configure port that will listen data
			long handle = scm.openComPort(PORT, true, true, true);
			scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);
			
			scm.registerDataListener(handle, dataListener);    // register data listener for this port

			// open and configure port which will send data
			long handle1 = scm.openComPort(PORT1, true, true, true);
			scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
			
			scm.writeString(handle1, "test string", 0);
			Thread.sleep(1000); // wait for data to be displayed on console
			
			if(osType == SerialComManager.OS_LINUX) {
				scm.fineTuneRead(handle, 5, 1, 0, 0, 0);
			}
			
			scm.writeString(handle1, "test string", 0);
			Thread.sleep(1000); // wait for data to be displayed on console
			
			scm.unregisterDataListener(dataListener); // unregister data listener
			scm.closeComPort(handle);                 // close the port releasing handle
			scm.closeComPort(handle1);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}