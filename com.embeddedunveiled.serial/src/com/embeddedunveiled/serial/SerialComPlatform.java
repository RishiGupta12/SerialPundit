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
package com.embeddedunveiled.serial;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;

/**
 * <p>This class identifies various hardware and software platform attributes 
 * like operating system and CPU architecture etc.</p>
 */
public final class SerialComPlatform {
	
	SerialComSystemProperty mSerialComSystemProperty = null;
	
	/**
	 * <p>Allocates a new SerialComPlatform object.</p>
	 * 
	 * @param mSerialComSystemProperty instance which gives various info about system properties
	 */
	public SerialComPlatform(SerialComSystemProperty mSerialComSystemProperty) {
		this.mSerialComSystemProperty = mSerialComSystemProperty;
	}
	
	/** 
	 * <p>Identifies the operating system on which scm library is running.</p>
	 * 
	 * @return SerialComManager.OS_UNKNOWN if platform is unknown to scm otherwise one of the SerialComManager.OS_XXXX constant
	 * @throws SerialComUnexpectedException if os.name system property is null
	 */
	public final int getOSType() throws SecurityException, SerialComUnexpectedException {
		int osType = SerialComManager.OS_UNKNOWN;
		
		String osName = mSerialComSystemProperty.getOSName();
		if(osName == null) {
			throw new SerialComUnexpectedException("getOSType()", "The os.name java system property is null in the system");
		}
		
		if(osName.contains("windows")) {
			osType = SerialComManager.OS_WINDOWS;
		}else if(osName.contains("linux")) {
			if(isAndroid()) {
				osType = SerialComManager.OS_ANDROID;
			}else {
				osType = SerialComManager.OS_LINUX;
			}
		}else if(osName.contains("mac os") || osName.contains("macos") || osName.contains("darwin")) {
			osType = SerialComManager.OS_MAC_OS_X;
		}else if(osName.contains("solaris") || osName.contains("sunos")) {
			osType = SerialComManager.OS_SOLARIS;
		}else if(osName.contains("freebsd") || osName.contains("free bsd")) {
			osType = SerialComManager.OS_FREEBSD;
		}else if(osName.contains("netbsd")) {
			osType = SerialComManager.OS_NETBSD;
		}else if(osName.contains("openbsd")) {
			osType = SerialComManager.OS_OPENBSD;
		}else if(osName.contains("aix")) {
			osType = SerialComManager.OS_IBM_AIX;
		}else if(osName.contains("hp-ux")) {
			osType = SerialComManager.OS_HP_UX;
		}else {
		}
		return osType;
	}

	/** <p>Identifies CPU architecture scm library is running on.</p>
	 * <p>Packages that are compiled for i386 architecture are compatible with i486, i586, i686, i786, i886 and i986 architectures.
	 * Packages that are compiled for x86_64 architecture are compatible with amd64 architecture.</p>
	 * 
	 * @return SerialComManager.ARCH_UNKNOWN if platform is unknown to scm otherwise one of the SerialComManager.ARCH_XXXX constant
	 * @throws IOException 
	 */
	public final int getCPUArch(int osType) throws IOException {
		int cpuArch = SerialComManager.ARCH_UNKNOWN;
		BufferedReader cpuProperties = null;
		String line = null;
		String property = null;
		
		String osArch = mSerialComSystemProperty.getOSArch();
		if(osArch == null) {
			throw new SerialComUnexpectedException("getCPUArch()", "The os.arch java system property is null in the system");
		}
		
		if(osArch.startsWith("arm")) {
			if(osType == SerialComManager.OS_LINUX) {
				cpuProperties = new BufferedReader(new FileReader("/proc/cpuinfo"));
				while((line = cpuProperties.readLine()) != null) {
					property = line.toLowerCase(Locale.ENGLISH);
					if(property.contains("armv7")) {
						cpuArch = SerialComManager.ARCH_ARMV7;
						break;
					}else if(property.contains("armv6")) {
						cpuArch = SerialComManager.ARCH_ARMV6;
						break;
					}else if(property.contains("armv5")) {
						cpuArch = SerialComManager.ARCH_ARMV5;
						break;
					}else {
					}
				}
				cpuProperties.close();
			}
		}else if(osArch.equals("x86") || osArch.equals("i386") || osArch.equals("i486") || osArch.equals("i586") || osArch.equals("i686") 
				|| osArch.equals("i786") || osArch.equals("i886") || osArch.equals("i986") || osArch.equals("pentium") || osArch.equals("i86pc")) {
			cpuArch = SerialComManager.ARCH_X86;
		}else if(osArch.equals("amd64") || osArch.equals("x86_64") || osArch.equals("em64t") || osArch.equals("x86-64") || osArch.equals("universal")) {
			cpuArch = SerialComManager.ARCH_AMD64; // universal may be needed for openjdk7 in Mac
		}else if(osArch.equals("ia64") || osArch.equals("ia64w")) {
			cpuArch = SerialComManager.ARCH_IA64;
		}else if(osArch.equals("ia64_32") || osArch.equals("ia64n")) {
			cpuArch = SerialComManager.ARCH_IA64_32;
		}else if(osArch.equals("ppc") || osArch.equals("power") || osArch.equals("powerpc") || osArch.equals("power_pc") || osArch.equals("power_rs")) {
			cpuArch = SerialComManager.ARCH_PPC32;
		}else if(osArch.equals("ppc64") || osArch.equals("power64") || osArch.equals("powerpc64") || osArch.equals("power_pc64") || osArch.equals("power_rs64")) {
			cpuArch = SerialComManager.ARCH_PPC64;
		}else if(osArch.equals("powerpc64le")) {
			cpuArch = SerialComManager.ARCH_PPC64LE;
		}else if(osArch.equals("sparc")) {
			cpuArch = SerialComManager.ARCH_SPARC32;
		}else if(osArch.equals("sparcv9")) {
			cpuArch = SerialComManager.ARCH_SPARC64;
		}else if(osArch.equals("pa-risc") || osArch.equals("pa-risc2.0")) {
			cpuArch = SerialComManager.ARCH_PA_RISC32;
		}else if(osArch.equals("pa-risc2.0w")) {
			cpuArch = SerialComManager.ARCH_PA_RISC64;
		}else if(osArch.equals("s390")) {
			cpuArch = SerialComManager.ARCH_S390;
		}else if(osArch.equals("s390x")) {
			cpuArch = SerialComManager.ARCH_S390X;
		}else {
		}
		
		return cpuArch;
	}
	
	/** 
	 * <p>Identifies whether library is running on an android platform.</p>
	 * 
	 * @return true if platform is android false otherwise
	 * @throws SerialComUnexpectedException if java.vm.vendor system property is null
	 */
	private boolean isAndroid() throws SerialComUnexpectedException {
		// java.vm.vendor system property in android always returns The Android Project as per android javadocs.
		String osVendor = mSerialComSystemProperty.getJavaVmVendor();
		if(osVendor == null) {
			throw new SerialComUnexpectedException("isAndroid()", "The java.vm.vendor java system property is null in the system");
		}
		
		if(osVendor.contains("android")) {
			return true;
		}
		return false;
	}
	
	/**
	 * <p>Conformance to the standard ABI for the ARM architecture helps in inter-operation between re-locatable or 
	 * executable files built by different tool chains.</p>
	 * 
	 * @return either ABI_ARMHF or ABI_ARMEL constant value as per identification
	 * @throws SerialComUnexpectedException if java.home system property is null
	 */
	public final int getJAVAABIType() throws SerialComUnexpectedException {
		int abiType = SerialComManager.ABI_ARMEL;
		String javaHome = mSerialComSystemProperty.getJavaHome();
		if(javaHome == null) {
			throw new SerialComUnexpectedException("getARMABIType()", "The java.home java system property is null in the system");
		}
		
	    try {
	        String[] cmdarray = { "/bin/sh", "-c", "find '" + javaHome +
	                "' -name 'libjvm.so' | head -1 | xargs readelf -A | " +
	                "grep 'Tag_ABI_VFP_args: VFP registers'" };
	        int exitValueOfSubProcess = Runtime.getRuntime().exec(cmdarray).waitFor();
	        if(exitValueOfSubProcess == 0) {
	        	abiType = SerialComManager.ABI_ARMHF;
	        }
	    }catch (IOException e) {
	    	return SerialComManager.ABI_ARMEL;
	    }catch (InterruptedException e) {
	    	return SerialComManager.ABI_ARMEL;
	    }
	    
	    return abiType;
	}
	
}
