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

package test48;

import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComSystemProperty;

public class Test48 {

	public static void main(String[] args) {

		SerialComSystemProperty prop = new SerialComSystemProperty();
		SerialComPlatform scp = new SerialComPlatform(prop);

		int osType = scp.getOSType();

		try {
			osType = scp.getOSType();
			System.out.println("OS : " + osType + "\n");
			if(osType == SerialComPlatform.OS_LINUX) {
				System.out.println("OS IS LINUX");
			}else if(osType == SerialComPlatform.OS_WINDOWS) {
				System.out.println("OS IS WINDOWS");
			}else if(osType == SerialComPlatform.OS_MAC_OS_X) {
				System.out.println("OS IS MAC");
			}else if(osType == SerialComPlatform.OS_SOLARIS) {
				System.out.println("OS IS SOLARIS");
			}else{
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			int cpuArch = scp.getCPUArch(osType);
			System.out.println("ARCH : " + cpuArch + "\n");
			if(cpuArch == SerialComPlatform.ARCH_X86) {
				System.out.println("i386/i486/i586/i686/i786/i886/i986/IA-32 based architecture");
			}else if(cpuArch == SerialComPlatform.ARCH_AMD64) {
				System.out.println("x86_64/amd64 architecture");
			}else if(cpuArch == SerialComPlatform.ARCH_IA64) {
				System.out.println("ARCH_IA64 architecture");
			}else if(cpuArch == SerialComPlatform.ARCH_IA64_32) {
				System.out.println("ARCH_IA64_32 architectures");
			}else{
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
