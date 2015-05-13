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

package test46;

import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.SerialComManager.BAUDRATE;
import com.embeddedunveiled.serial.SerialComManager.DATABITS;
import com.embeddedunveiled.serial.SerialComManager.FLOWCONTROL;
import com.embeddedunveiled.serial.SerialComManager.PARITY;
import com.embeddedunveiled.serial.SerialComManager.STOPBITS;

/* return nuber of byts should be same as requested, less number return if less data in buffer
 * req nuber of read 5, got 5
req nuber of read 9, got 9
req nuber of read 18, got 18
req nuber of read 25, got 25
req nuber of read 38, got 38
req nuber of read 128, got 128
req nuber of read 1000, got 347
 */
public class Test46 {
	public static void main(String[] args) {
		SerialComManager scm = new SerialComManager();
		try {
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

			long handle = scm.openComPort(PORT, true, true, true);
			scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle, FLOWCONTROL.NONE, '$', '$', false, false);
			long handle1 = scm.openComPort(PORT1, true, true, true);
			scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle1, FLOWCONTROL.NONE, '$', '$', false, false);
			
			int x = 0;
			for(x=0; x<10; x++) {
				scm.writeString(handle1, "qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq", 0);
			}

			Thread.sleep(1000); // let data reach physically to port and OS buffer it
			byte[] data = null;
			data = scm.readBytes(handle, 0);
			System.out.println("req nuber of read 0, " + "got " + data.length);
			data = scm.readBytes(handle, 1);
			System.out.println("req nuber of read 1, " + "got " + data.length);
			data = scm.readBytes(handle, 2);
			System.out.println("req nuber of read 2, " + "got " + data.length);
			data = scm.readBytes(handle, 3);
			System.out.println("req nuber of read 3, " + "got " + data.length);
			data = scm.readBytes(handle, 5);
			System.out.println("req nuber of read 5, " + "got " + data.length);
			data = scm.readBytes(handle, 9);
			System.out.println("req nuber of read 9, " + "got " + data.length);
			data = scm.readBytes(handle, 18);
			System.out.println("req nuber of read 18, " + "got " + data.length);
			data = scm.readBytes(handle, 25);
			System.out.println("req nuber of read 25, " + "got " + data.length);
			data = scm.readBytes(handle, 38);
			System.out.println("req nuber of read 38, " + "got " + data.length);
			data = scm.readBytes(handle, 128);
			System.out.println("req nuber of read 128, " + "got " + data.length);
			data = scm.readBytes(handle, 1000);
			System.out.println("req nuber of read 1000, " + "got " + data.length);
			
			scm.closeComPort(handle);
			scm.closeComPort(handle1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
