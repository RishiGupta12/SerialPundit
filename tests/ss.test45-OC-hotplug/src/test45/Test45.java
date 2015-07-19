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

package test45;

import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.SerialComManager.BAUDRATE;
import com.embeddedunveiled.serial.SerialComManager.DATABITS;
import com.embeddedunveiled.serial.SerialComManager.FLOWCONTROL;
import com.embeddedunveiled.serial.SerialComManager.PARITY;
import com.embeddedunveiled.serial.SerialComManager.STOPBITS;
import com.embeddedunveiled.serial.ISerialComHotPlugListener;

//event 2 indicates port removal, 1 indicates additional of port
class portWatcher implements ISerialComHotPlugListener {
	@Override
	public void onHotPlugEvent(int arg0) {
		System.out.println("event " + arg0);
	}
}

public class Test45 {
	public static void main(String[] args) {
		int x = 0;
		try {
			SerialComManager scm = new SerialComManager();

			String PORT = null;
			int osType = scm.getOSType();
			if(osType == SerialComManager.OS_LINUX) {
				PORT = "/dev/ttyUSB0";
			}else if(osType == SerialComManager.OS_WINDOWS) {
				PORT = "COM51";
			}else if(osType == SerialComManager.OS_MAC_OS_X) {
				PORT = "/dev/cu.usbserial-A70362A3";
			}else if(osType == SerialComManager.OS_SOLARIS) {
				PORT = null;
			}else{
			}
			
			long handle = scm.openComPort(PORT, true, true, true);
			scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle, FLOWCONTROL.NONE, '$', '$', false, false);
			
			for (x=0; x<500000; x++) {
				System.out.println("Iteration :" + x);
				portWatcher pw = new portWatcher();
				scm.registerHotPlugEventListener(pw, 0x0403, 0x6001);
//				Thread.sleep(100);
				scm.unregisterHotPlugEventListener(pw);
//				Thread.sleep(100);
			}
			scm.closeComPort(handle);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
