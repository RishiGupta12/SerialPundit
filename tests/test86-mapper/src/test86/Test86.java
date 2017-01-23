/*
 * This file is part of SerialPundit.
 * 
 * Copyright (C) 2014-2016, Rishi Gupta. All rights reserved.
 *
 * The SerialPundit is DUAL LICENSED. It is made available under the terms of the GNU Affero 
 * General Public License (AGPL) v3.0 for non-commercial use and under the terms of a commercial 
 * license for commercial use of this software. 
 * 
 * The SerialPundit is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
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
