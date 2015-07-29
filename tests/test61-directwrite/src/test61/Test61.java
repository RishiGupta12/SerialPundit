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

package test61;

import java.nio.ByteBuffer;

import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.SerialComManager.BAUDRATE;
import com.embeddedunveiled.serial.SerialComManager.DATABITS;
import com.embeddedunveiled.serial.SerialComManager.FLOWCONTROL;
import com.embeddedunveiled.serial.SerialComManager.PARITY;
import com.embeddedunveiled.serial.SerialComManager.STOPBITS;

public final class Test61 {
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
			
			PORT = "/dev/pts/1";
			PORT1 = "/dev/pts/3";
			long handle = scm.openComPort(PORT, true, true, false);
			scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);
			long handle1 = scm.openComPort(PORT1, true, true, false);
			scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
			
			ByteBuffer writeBuffer = ByteBuffer.allocateDirect(10 * 1024);
			ByteBuffer readBuffer = ByteBuffer.allocateDirect(10 * 1024);
//			ByteBuffer buffer = ByteBuffer.allocate(10 * 1024); this should throw exception as it is not direct

			for(int x=0; x<205; x++) {
				writeBuffer.put("--Practising-meditation-will-lead-to-Nirvana--".getBytes());
			}
			
			System.out.println("Capacity : " + writeBuffer.capacity());
			System.out.println("Position : " + writeBuffer.position());
			System.out.println("" + writeBuffer.get(5) + "," + writeBuffer.get(6) + "," + writeBuffer.get(7));
			
			scm.writeBytesDirect(handle, writeBuffer, 0, 4 * 1023);
			Thread.sleep(1000);
			System.out.println(scm.readString(handle1, 2 * 1024));
			System.out.println(scm.readString(handle1, 2 * 1024));
			System.out.println(scm.readString(handle1, 2 * 1024));
			
			System.out.println("Capacity : " + writeBuffer.capacity());
			System.out.println("Position : " + writeBuffer.position());
			System.out.println("" + writeBuffer.get(5));
			
			// read from same locations where data was written
			System.out.println("\n");
			writeBuffer.clear();
			for(int x=0; x<205; x++) {
				writeBuffer.put("--Practising-meditation-will-lead-to-Nirvana--".getBytes());
			}
			System.out.println("Capacity : " + writeBuffer.capacity());
			System.out.println("Position : " + writeBuffer.position());
			System.out.println("" + writeBuffer.get(5) + "," + writeBuffer.get(6) + "," + writeBuffer.get(7));
			scm.writeBytesDirect(handle, writeBuffer, 0, 4 * 1023);
			Thread.sleep(1000);
			System.out.println("Capacity : " + readBuffer.capacity());
			System.out.println("Position : " + readBuffer.position());
			scm.readBytesDirect(handle1, readBuffer, 0, 9000);
			System.out.println("Capacity : " + readBuffer.capacity());
			System.out.println("Position : " + readBuffer.position());
			System.out.println("" + readBuffer.get(5) + "," + readBuffer.get(6) + "," + readBuffer.get(7));

			scm.closeComPort(handle);
			scm.closeComPort(handle1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
