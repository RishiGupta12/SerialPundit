/**
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

package test48;

import com.embeddedunveiled.serial.SerialComManager;

public class Test48 {
	public static void main(String[] args) {
		SerialComManager scm = null;
		try {
			scm = new SerialComManager();
		} catch (Exception e1) {
			e1.printStackTrace();
		} 

		try {
			int osType = scm.getOSType();
			System.out.println("OS : " + osType + "\n");
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

		try {
			int cpuArch = scm.getCPUArchitecture();
			System.out.println("ARCH : " + cpuArch + "\n");
			if(cpuArch == SerialComManager.ARCH_X86) {
				System.out.println("i386/i486/i586/i686/i786/i886/i986/IA-32 based architecture");
			}else if(cpuArch == SerialComManager.ARCH_AMD64) {
				System.out.println("x86_64/amd64 architecture");
			}else if(cpuArch == SerialComManager.ARCH_IA64) {
				System.out.println("ARCH_IA64 architecture");
			}else if(cpuArch == SerialComManager.ARCH_IA64_32) {
				System.out.println("ARCH_IA64_32 architectures");
			}else{
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
