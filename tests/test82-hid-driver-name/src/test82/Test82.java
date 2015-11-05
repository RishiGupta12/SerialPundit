/*
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

package test82;

import com.embeddedunveiled.serial.SerialComHID;
import com.embeddedunveiled.serial.SerialComManager;

public class Test82  {

	public static SerialComManager scm = null;
	public static String PORT = null;
	public static String PORT1 = null;

	public static void main(String[] args) {
		try {
			scm = new SerialComManager();
			String PORT = null;
			int osType = scm.getOSType();
			if(osType == SerialComManager.OS_LINUX) {
				PORT = "/dev/hiddev0";
			}else if(osType == SerialComManager.OS_WINDOWS) {
				PORT = "COM51";
			}else if(osType == SerialComManager.OS_MAC_OS_X) {
				PORT = "/dev/cu.usbserial-A70362A3";
			}else if(osType == SerialComManager.OS_SOLARIS) {
				PORT = null;
			}else{
			}
			SerialComHID sch = scm.getSerialComHIDInstance(SerialComHID.HID_GENERIC, null, null);
			System.out.println("driver : "+ sch.findDriverServingHIDDevice(PORT));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
