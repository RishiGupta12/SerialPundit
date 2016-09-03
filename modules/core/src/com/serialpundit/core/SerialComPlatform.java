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

package com.serialpundit.core;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Locale;

import com.serialpundit.core.util.SerialComUtil;

/**
 * <p>Identifies various hardware and software platform attributes 
 * like operating system and CPU architecture etc.</p>
 * 
 * @author Rishi Gupta
 */
public final class SerialComPlatform {

    /** <p>The value indicating that operating system is unknown to SerialPundit. Integer constant with 
     * value 0x00. </p>*/
    public static final int OS_UNKNOWN  = 0x00;

    /** <p>The value indicating the Linux operating system. Integer constant with value 0x01. </p>*/
    public static final int OS_LINUX    = 0x01;

    /** <p>The value indicating the Windows operating system. Integer constant with value 0x02. </p>*/
    public static final int OS_WINDOWS  = 0x02;

    /** <p>The value indicating the Solaris operating system. Integer constant with value 0x03. </p>*/
    public static final int OS_SOLARIS  = 0x03;

    /** <p>The value indicating the Mac OS X operating system. Integer constant with value 0x04. </p>*/
    public static final int OS_MAC_OS_X = 0x04;

    /** <p>The value indicating the FreeBSD operating system.. Integer constant with value 0x05. </p>*/
    public static final int OS_FREEBSD  = 0x05;

    /** <p>The value indicating the NetBSD operating system. Integer constant with value 0x06. </p>*/
    public static final int OS_NETBSD   = 0x06;

    /** <p>The value indicating the OpenBSD operating system. Integer constant with value 0x07. </p>*/
    public static final int OS_OPENBSD  = 0x07;

    /** <p>The value indicating the IBM AIX operating system. Integer constant with value 0x08. </p>*/
    public static final int OS_IBM_AIX  = 0x08;

    /** <p>The value indicating the HP-UX operating system. Integer constant with value 0x09. </p>*/
    public static final int OS_HP_UX    = 0x09;

    /** <p>The value indicating the Android operating system. Integer constant with value 0x0A. </p>*/
    public static final int OS_ANDROID  = 0x0A;

    /** <p>The value indicating that the platform architecture is unknown to SerialPundit. Integer 
     * constant with value 0x00. </p>*/
    public static final int ARCH_UNKNOWN = 0x00;

    /** <p>The common value indicating that this software is running on a 32 bit Intel 
     * i386/i486/i586/i686/i786/i886/i986/IA-32 based architecture. Integer constant with value 0x01. </p>*/
    public static final int ARCH_X86 = 0x01;

    /** <p>The common value indicating that this software is running on a 64 bit Intel x86_64 (x86-64/x64/Intel 64) 
     * and AMD amd64 based architecture. Integer constant with value 0x02. </p>*/
    public static final int ARCH_AMD64 = 0x02;

    /** <p>The value indicating that this software is running on a 64 bit Intel/HP Itanium based architecture. 
     * Integer constant with value 0x03. </p>*/
    public static final int ARCH_IA64 = 0x03;

    /** <p>The value indicating that this software is running on an IA64 32 bit based architecture. Integer 
     * constant with value 0x04. </p>*/
    public static final int ARCH_IA64_32 = 0x04;

    /** <p>The value indicating that this software is running on a 32 bit PowerPC based architecture from 
     * Apple–IBM–Motorola. Integer constant with value 0x05. </p>*/
    public static final int ARCH_PPC32 = 0x05;

    /** <p>The value indicating that this software is running on a 64 bit PowerPC based architecture from 
     * Apple–IBM–Motorola. Integer constant with value 0x06. </p>*/
    public static final int ARCH_PPC64 = 0x06;

    /** <p>The value indicating that this software is running on a 64 bit PowerPC based architecture in 
     * little endian mode from Apple–IBM–Motorola. Integer constant with value 0x06. </p>*/
    public static final int ARCH_PPC64LE = 0x06;

    /** <p>The value indicating that this software is running on a 32 bit Sparc based architecture from 
     * Sun Microsystems. Integer constant with value 0x07. </p>*/
    public static final int ARCH_SPARC32 = 0x07;

    /** <p>The value indicating that this software is running on a 64 bit Sparc based architecture from 
     * Sun Microsystems. Integer constant with value 0x08. </p>*/
    public static final int ARCH_SPARC64 = 0x08;

    /** <p>The value indicating that this software is running on a 32 bit PA-RISC based architecture. 
     * Integer constant with value 0x09. </p>*/
    public static final int ARCH_PA_RISC32 = 0x09;

    /** <p>The value indicating that this software is running on a 64 bit PA-RISC based architecture. 
     * Integer constant with value 0x0A. </p>*/
    public static final int ARCH_PA_RISC64 = 0x0A;

    /** <p>The value indicating that this software is running on a 32 bit IBM S/390 system. Integer 
     * constant with value 0x0B. </p>*/
    public static final int ARCH_S390 = 0x0B;

    /** <p>The value indicating that this software is running on a 64 bit IBM S/390 system. Integer 
     * constant with value 0x0C. </p>*/
    public static final int ARCH_S390X = 0x0C;

    /** <p>The value indicating that this software is running on a ARMv5 based architecture CPU. Integer 
     * constant with value 0x0D. </p>*/
    public static final int ARCH_ARMV5 = 0x0D;

    /** <p>The value indicating that this software is running on a ARMv6 based platform. Integer 
     * constant with value 0x0E. </p>*/
    public static final int ARCH_ARMV6 = 0x0E;

    /** <p>The value indicating that this software is running on a ARMv7 based platform. Integer 
     * constant with value 0x10. </p>*/
    public static final int ARCH_ARMV7 = 0x0F;

    /** <p>The value indicating that this software is running on a ARMv8 based platform. Integer 
     * constant with value 0x10. </p>*/
    public static final int ARCH_ARMV8 = 0x10;

    /** <p>The value indicating that ABI type is unknown as of now. Integer constant with value 0x00. </p>*/
    public static final int ABI_UNKNOWN = 0x00;

    /** <p>The value indicating hard float ABI. Integer constant with value 0x01. </p>*/
    public static final int ABI_ARMHF =  0x01;

    /** <p>The value indicating soft float ABI. Integer constant with value 0x02. </p>*/
    public static final int ABI_ARMEL = 0x02;

    private int osType = SerialComPlatform.OS_UNKNOWN;
    private int cpuArch = SerialComPlatform.ARCH_UNKNOWN;
    private int abiType = ABI_UNKNOWN;
    private final SerialComSystemProperty mSerialComSystemProperty;

    /**
     * <p>Allocates a new SerialComPlatform object.</p>
     * 
     * @param mSerialComSystemProperty instance of class which gives various info about system properties.
     */
    public SerialComPlatform(SerialComSystemProperty mSerialComSystemProperty) {
        this.mSerialComSystemProperty = mSerialComSystemProperty;
    }

    /**
     * <p>Gives operating system type as identified by serialpundit. To interpret returned integer value see 
     * the OS_xxxxx constants defined in SerialComPlatform class.</p>
     * 
     * <p>This method may be used to develop application with consistent behavior across different operating systems.
     * For example let us assume that in a poll based application calling Thread.sleep(10) make ~10 milliseconds sleep 
     * in Linux operating system but causes ~50 milliseconds sleep in Windows because these operating system may have 
     * different resolution for sleep timings. To deal with this write the application code in following manner :</p>
     * int osType = scm.getOSType();<br>
     * if(osType == SerialComPlatform.OS_LINUX) {<br>
     *  Thread.sleep(10);<br>
     * }else if(osType == SerialComPlatform.OS_WINDOWS) {<br>
     *  Thread.sleep(1);<br>
     * }else {<br>
     *  Thread.sleep(5);<br>
     * }<br>
     * 
     * @return one of the constants OS_xxxxx as defined in SerialComPlatform class.
     * @throws SecurityException if java system properties can not be  accessed.
     * @throws NullPointerException if the "os.name" java system property is null or empty.
     */    
    public final int getOSType() throws SecurityException {

        // return cached copy if available
        if(osType != SerialComPlatform.OS_UNKNOWN) {
            return osType;
        }

        String osName = mSerialComSystemProperty.getOSName();
        if(osName == null || osName.length() == 0) {
            throw new NullPointerException("The os.name java system property is null or empty in the system !");
        }

        if(osName.contains("windows")) {
            osType = SerialComPlatform.OS_WINDOWS;
        }
        else if(osName.contains("linux")) {
            if(isAndroid()) {
                osType = SerialComPlatform.OS_ANDROID;
            }else {
                osType = SerialComPlatform.OS_LINUX;
            }
        }
        else if(osName.contains("mac os") || osName.contains("macos") || osName.contains("darwin")) {
            osType = SerialComPlatform.OS_MAC_OS_X;
        }
        else if(osName.contains("solaris") || osName.contains("sunos")) {
            osType = SerialComPlatform.OS_SOLARIS;
        }
        else if(osName.contains("freebsd") || osName.contains("free bsd")) {
            osType = SerialComPlatform.OS_FREEBSD;
        }
        else if(osName.contains("netbsd")) {
            osType = SerialComPlatform.OS_NETBSD;
        }
        else if(osName.contains("openbsd")) {
            osType = SerialComPlatform.OS_OPENBSD;
        }
        else if(osName.contains("aix")) {
            osType = SerialComPlatform.OS_IBM_AIX;
        }
        else if(osName.contains("hp-ux")) {
            osType = SerialComPlatform.OS_HP_UX;
        }
        else {
        }

        return osType;
    }

    /** 
     * <p>Identifies CPU architecture SerialPundit is running on. To interpret return integer see 
     * constants defined in SerialComPlatform class.</p>
     * 
     * <p>Packages that are compiled for i386 architecture are compatible with i486, i586, i686, i786, 
     * i886 and i986 architectures. Packages that are compiled for x86_64 architecture are compatible 
     * with amd64 architecture.</p>
     * 
     * @return SerialComPlatform.ARCH_UNKNOWN if platform is unknown to SCM otherwise one of the 
     *         SerialComPlatform.ARCH_XXXX constant.
     * @throws SecurityException if java system properties can not be  accessed.
     * @throws NullPointerException if the "os.arch" java system property is null.
     * @throws FileNotFoundException if file "/proc/cpuinfo" can not be found for Linux on ARM platform.
     * @throws IOException if file operations on "/proc/cpuinfo" fails for Linux on ARM platform.
     */
    public final int getCPUArch(int osType) throws SecurityException, IOException {

        // return cached copy if available
        if(cpuArch != SerialComPlatform.ARCH_UNKNOWN) {
            return cpuArch;
        }

        BufferedReader cpuProperties = null;
        String line = null;
        String property = null;

        String osArch = mSerialComSystemProperty.getOSArch();
        if(osArch == null) {
            throw new NullPointerException("The os.arch java system property is null in the system !");
        }

        // AArch64 provides user-space compatibility with ARMv7-A ISA, the 32-bit architecture a.k.a. 
        // "AArch32" and the old 32-bit instruction set, now named "A32". This means it may be possible 
        // to run 32 bit user space executables on ARMv8 platform.
        if(osArch.startsWith("arm") || osArch.startsWith("aarch")) {
            if(osType == SerialComPlatform.OS_LINUX) {
                cpuProperties = new BufferedReader(new FileReader("/proc/cpuinfo"));
                while((line = cpuProperties.readLine()) != null) {
                    property = line.toLowerCase(Locale.ENGLISH);
                    if(property.contains("aarch64") || property.contains("armv8")) {
                        cpuArch = SerialComPlatform.ARCH_ARMV8;
                        break;
                    }
                    else if(property.contains("armv7") || property.contains("aarch32")) {
                        cpuArch = SerialComPlatform.ARCH_ARMV7;
                        break;
                    }
                    else if(property.contains("armv6")) {
                        cpuArch = SerialComPlatform.ARCH_ARMV6;
                        break;
                    }
                    else if(property.contains("armv5")) {
                        cpuArch = SerialComPlatform.ARCH_ARMV5;
                        break;
                    }
                    else {
                    }
                }
                cpuProperties.close();
            }
        }
        else if(osArch.equals("x86") || osArch.equals("i386") || osArch.equals("i486") || osArch.equals("i586") || 
                osArch.equals("i686") || osArch.equals("i786") || osArch.equals("i886") || osArch.equals("i986") || 
                osArch.equals("pentium") || osArch.equals("i86pc")) {
            cpuArch = SerialComPlatform.ARCH_X86;
        }
        else if(osArch.equals("amd64") || osArch.equals("x86_64") || osArch.equals("em64t") || osArch.equals("x86-64") || 
                osArch.equals("ia-32e") || osArch.equals("universal")) {
            cpuArch = SerialComPlatform.ARCH_AMD64; // universal may be needed for openjdk7 in Mac.
        }
        else if(osArch.equals("ia64") || osArch.equals("ia64w")) {
            cpuArch = SerialComPlatform.ARCH_IA64;
        }
        else if(osArch.equals("ia64_32") || osArch.equals("ia64n")) {
            cpuArch = SerialComPlatform.ARCH_IA64_32;
        }
        else if(osArch.equals("ppc") || osArch.equals("power") || osArch.equals("powerpc") || osArch.equals("power_pc") || 
                osArch.equals("power_rs")) {
            cpuArch = SerialComPlatform.ARCH_PPC32;
        }
        else if(osArch.equals("ppc64") || osArch.equals("power64") || osArch.equals("powerpc64") || osArch.equals("power_pc64") || 
                osArch.equals("power_rs64")) {
            cpuArch = SerialComPlatform.ARCH_PPC64;
        }
        else if(osArch.equals("powerpc64le")) {
            cpuArch = SerialComPlatform.ARCH_PPC64LE;
        }
        else if(osArch.equals("sparc")) {
            cpuArch = SerialComPlatform.ARCH_SPARC32;
        }
        else if(osArch.equals("sparcv9")) {
            cpuArch = SerialComPlatform.ARCH_SPARC64;
        }
        else if(osArch.equals("pa-risc") || osArch.equals("pa-risc2.0")) {
            cpuArch = SerialComPlatform.ARCH_PA_RISC32;
        }
        else if(osArch.equals("pa-risc2.0w")) {
            cpuArch = SerialComPlatform.ARCH_PA_RISC64;
        }
        else if(osArch.equals("s390")) {
            cpuArch = SerialComPlatform.ARCH_S390;
        }
        else if(osArch.equals("s390x")) {
            cpuArch = SerialComPlatform.ARCH_S390X;
        }
        else {
        }

        return cpuArch;
    }

    /** 
     * <p>Identifies whether thsi software is running on an android platform.</p>
     * 
     * @return true if platform is android false otherwise.
     * @throws NullPointerException if "java.vm.vendor" java system property is null.
     */
    private boolean isAndroid() {

        // java.vm.vendor system property in android always returns The Android Project as per android javadocs.
        String osVendor = mSerialComSystemProperty.getJavaVmVendor();

        if((osVendor == null) || (osVendor.length() ==0)) {
            throw new NullPointerException("The java.vm.vendor java system property is null or empty in the system !");
        }

        if(osVendor.contains("android")) {
            return true;
        }
        return false;
    }

    /**
     * <p>Identifies ABI (application binary interface) type. Conformance to the standard ABI for the 
     * ARM architecture helps in inter-operation between re-locatable or executable files built by different 
     * tool chains.</p>
     * 
     * @return constant SerialComPlatform.ABI_ARMHF or SerialComPlatform.ABI_ARMEL value as per the 
     *         identification.
     * @throws NullPointerException if some java system properties are null.
     */
    public final int getABIType() throws IOException {

        if(abiType != ABI_UNKNOWN) {
            return abiType;
        }


        abiType = AccessController.doPrivileged(new PrivilegedAction<Integer>() {

            private final String[] gnueabihf = new String[] { "gnueabihf", "armhf" };

            public Integer run() {

                // Decision based on ABI type of shell executable. Output may have string :
                // GNU bash, version 4.2.37(1)-release (arm-unknown-linux-gnueabihf)
                try {
                    String output[] = SerialComUtil.execute("/bin/bash --version");
                    if(output != null) {
                        for(String line : output) {
                            if(!line.isEmpty() && line.toLowerCase().contains("gnueabihf")) {
                                return SerialComPlatform.ABI_ARMHF;
                            }
                        }
                    }
                }catch (Exception e) {
                }

                // Decision based on system properties
                if (SerialComUtil.contains(System.getProperty("sun.boot.library.path").toLowerCase(), gnueabihf) || 
                        SerialComUtil.contains(System.getProperty("java.library.path").toLowerCase(), gnueabihf) ||
                        SerialComUtil.contains(System.getProperty("java.home").toLowerCase(), gnueabihf)) {
                    return SerialComPlatform.ABI_ARMHF;
                }

                // Decision based on ABI type of shell executable. Output may have string :
                // Tag_ABI_VFP_args: VFP registers
                try {
                    String output[] = SerialComUtil.execute("/usr/bin/readelf -A /proc/self/exe");
                    if(output != null) {
                        for(String line : output) {
                            if(!line.isEmpty() && line.toLowerCase().contains("tag_abi_vfp_args: vfp registers")) {
                                return SerialComPlatform.ABI_ARMHF;
                            }
                        }
                    }
                }catch (Exception e) {
                }

                // Reaching here means platform is using soft float abi
                return SerialComPlatform.ABI_ARMEL;
            } } );

        return abiType;
    }
}
