/*
 * Author : Rishi Gupta
 * 
 * This file is part of 'serial communication manager' library.
 * Copyright (C) <2014-2016>  <Rishi Gupta>
 *
 * This 'serial communication manager' is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by the Free Software 
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * The 'serial communication manager' is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR 
 * A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with 'serial communication manager'.  If not, see <http://www.gnu.org/licenses/>.
 */

package test86;

import java.io.File;

import com.embeddedunveiled.serial.SerialComException;
import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.mapper.SerialComPortMapper;

public class Test86 {
	static SerialComManager scm = null;
	static SerialComPortMapper scpm = null;
	public static void main(String[] args) {		
		try {
			scm = new SerialComManager();
			scpm = scm.getSerialComPortMapperInstance(null, null);

			scpm.startMappingService();

			int osType = scm.getOSType();
			if(osType == SerialComManager.OS_LINUX) {
				scpm.mapAliasToExistingComPort("/home/r/mapsymlink", "/dev/tty3");
				File f = new File("/home/r/mapsymlink");
				if(f.exists() && !f.isDirectory()) { 
					System.out.println("Port mapped !");
				}
			}else if(osType == SerialComManager.OS_WINDOWS) {
			}else if(osType == SerialComManager.OS_MAC_OS_X) {
			}else if(osType == SerialComManager.OS_SOLARIS) {
			}else{
			}

			scpm.stopMappingService();
			System.out.println("Done !");
		}catch (Exception e) {
			try {
				scpm.stopMappingService();
			} catch (SerialComException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}
}
