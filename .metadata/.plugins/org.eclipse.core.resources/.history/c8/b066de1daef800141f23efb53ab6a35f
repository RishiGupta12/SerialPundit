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

package test48;

import com.embeddedunveiled.serial.SerialComManager;

public class Test48 {
	public static void main(String[] args) {
		try {
			SerialComManager scm = new SerialComManager();
			int osType = SerialComManager.getOSType();
			if(osType == SerialComManager.OS_LINUX) {
				System.out.println("OS IS LINUX");
			}else if(osType == SerialComManager.OS_WINDOWS) {
				System.out.println("OS IS WINDOWS");
			}else if(osType == SerialComManager.OS_MAC_OS_X) {
				System.out.println("OS IS MAC");
			}else if(osType == SerialComManager.OS_SOLARIS) {
				System.out.println("OS IS SOLARIS");
			}else{
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}