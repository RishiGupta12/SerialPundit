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

package test66;

import com.embeddedunveiled.serial.SerialComManager;

public class Test66  {

	public static SerialComManager scm = null;
	public static String PORT = null;
	public static String PORT1 = null;

	public static void main(String[] args) {
		try {
			scm = new SerialComManager();

			int osType = scm.getOSType();
			if(osType == SerialComManager.OS_LINUX) {
				PORT = "/dev/ttyUSB0";
				PORT1 = "/dev/ttyS1";
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
			
			System.out.println("driver : " + scm.findDriverServingComPort(PORT));
			System.out.println("driver : " + scm.findDriverServingComPort(PORT1));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
