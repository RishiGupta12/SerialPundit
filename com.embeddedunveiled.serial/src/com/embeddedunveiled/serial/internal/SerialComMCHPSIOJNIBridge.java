/*
 * This file is part of SerialPundit project and software.
 * 
 * Copyright (C) 2014-2016, Rishi Gupta. All rights reserved.
 *
 * The SerialPundit software is DUAL licensed. It is made available under the terms of the GNU Affero 
 * General Public License (AGPL) v3.0 for non-commercial use and under the terms of a commercial 
 * license for commercial use of this software. 
 * 
 * The SerialPundit software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.embeddedunveiled.serial.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.FileNotFoundException;

import com.embeddedunveiled.serial.SerialComLoadException;
import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.SerialComUnexpectedException;

/**
 * <p>This class is an interface between java and native shared library. The native library is found 
 * in 'lib-vendor' folder in 'scm-x.x.x.jar' file.</p>
 * 
 * @author Rishi Gupta
 */
public final class SerialComMCHPSIOJNIBridge {

    /**
     * <p>Allocates a new SerialComMCHPSIOJNIBridge object.</p>
     */
    public SerialComMCHPSIOJNIBridge() {
    }

    /**
     * <p>Extract native library from jar in a working directory, load and link it. The 'lib-vendor' folder in 
     * 'scm-x.x.x.jar' file is searched for the required native library for vendor specific communication. It 
     * also load vendor's native shared library.</p>
     *  
     * @param libDirectory directory in which native library will be extracted and vendor library will be found
     * @param vlibName name of vendor library to load and link
     * @param cpuArch architecture of CPU this library is running on
     * @param osType operating system this library is running on
     * @param serialComSystemProperty instance of SerialComSystemProperty to get required java properties
     * @throws SerialComUnexpectedException if a critical java system property is null in system
     * @throws SecurityException if any java system property can not be accessed
     * @throws FileNotFoundException if the vendor library file is not found
     * @throws SerialComLoadException if any file system related issue occurs
     * @throws UnsatisfiedLinkError if loading/linking shared library fails
     */
    public static boolean loadNativeLibrary(File libDirectory, String vlibName, int cpuArch, int osType, 
            SerialComSystemProperty serialComSystemProperty) throws SerialComUnexpectedException, SerialComLoadException {
        String libToExtractFromJar = null;
        File libFile = null;
        InputStream input = null;
        FileOutputStream output = null;
        String fileSeparator = null;

        fileSeparator = serialComSystemProperty.getfileSeparator();
        if(fileSeparator == null) {
            throw new SerialComUnexpectedException("The file.separator java system property is null in the system !");
        }

        /* Find the native library that will be extracted based on arch and os type */
        if(cpuArch == SerialComManager.ARCH_AMD64) {
            switch(osType) {
            case SerialComManager.OS_WINDOWS:
                libToExtractFromJar = "win_mchpsio_" + SerialComManager.JAVA_LIB_VERSION + "_x86_64.dll";
                break;
            case SerialComManager.OS_LINUX:
                libToExtractFromJar = "linux_mchpsio_" + SerialComManager.JAVA_LIB_VERSION + "_x86_64.so";
                break;
            case SerialComManager.OS_MAC_OS_X:
                libToExtractFromJar = "mac_mchpsio_" + SerialComManager.JAVA_LIB_VERSION + "_x86_64.dylib";
            default:
            }
        }else if(cpuArch == SerialComManager.ARCH_X86) {
            switch(osType) {
            case SerialComManager.OS_WINDOWS:
                libToExtractFromJar = "win_mchpsio_" + SerialComManager.JAVA_LIB_VERSION + "_x86.dll";
                break;
            case SerialComManager.OS_LINUX:
                libToExtractFromJar = "linux_mchpsio_" + SerialComManager.JAVA_LIB_VERSION + "_x86.so";
                break;
            case SerialComManager.OS_MAC_OS_X:
                libToExtractFromJar = "mac_mchpsio_" + SerialComManager.JAVA_LIB_VERSION + "_x86.dylib";
            default:
            }
        }else {
        }

        /* Extract shared library from jar into working directory */
        try {
            libFile = new File(libDirectory.getAbsolutePath() + fileSeparator + libToExtractFromJar);
            input = SerialComPortJNIBridge.class.getResourceAsStream("/lib-vendor/" + libToExtractFromJar);
            output = new FileOutputStream(libFile);
            if(input != null) {
                int read;
                byte[] buffer = new byte[4096];
                while((read = input.read(buffer)) != -1){
                    output.write(buffer, 0, read);
                }
                output.flush();
                output.close();
                output = null;
                if(!((libFile != null) && libFile.exists() && libFile.isFile())) {
                    throw new SerialComLoadException("Can not extract native shared library from scm-x.x.x.jar file !");
                }
            }else {
                throw new SerialComLoadException("Can not get shared library resource as stream from scm-x.x.x.jar file using class loader !");
            }
        } catch (Exception e) {
            throw (SerialComLoadException) new SerialComLoadException(libFile.toString()).initCause(e);
        } finally {
            try {
                if(output != null) {
                    output.close();
                }
            } catch (Exception e) {
                // ignore
            }
            try {
                if(input != null) {
                    input.close();
                }
            } catch (Exception e) {
                // ignore
            }
        }

        try {
            // scm JNI glue shared library
            System.load(libFile.toString());
        } catch (Exception e) {
            throw (UnsatisfiedLinkError) new UnsatisfiedLinkError("Could not load " + libFile.toString() + " native library !").initCause(e);
        }

        try {
            /* SimpleIO-UM.dll has to be linked explicitly as it does not have .lib file associated.
             * Native layer will load and resolve all symbols to external SimpleIO-UM.dll. */
            loadAndLinkSimpleIODLL(libDirectory.getAbsolutePath() + fileSeparator + vlibName);
        } catch (Exception e) {
            e.printStackTrace();
            throw (UnsatisfiedLinkError) new UnsatisfiedLinkError("Could not load/link " + vlibName + " native library !");
        }

        return true;
    }

    private native static int loadAndLinkSimpleIODLL(String vendorLibraryWithAbsolutePath);

    public native int initMCP2200(int vendorID, int productID);
    public native int isConnected();
    public native int configureMCP2200(byte ioMap, long baudRateParam, int rxLEDMode,
            int txLEDMode, boolean flow, boolean uload, boolean sspnd, boolean invert);
    public native int setPin(int pinNumber);
    public native int clearPin(int pinNumber);
    public native int readPinValue(int pinNumber);
    public native int readPin(int pinNumber);
    public native int writePort(int portValue);
    public native int readPort();
    public native int readPortValue();
    public native int selectDevice(int uiDeviceNumber);
    public native int getSelectedDevice();
    public native int getNumOfDevices();
    public native String getDeviceInfo(int uiDeviceNumber);
    public native String getSelectedDeviceInfo();
    public native int readEEPROM(int uiEEPAddress);
    public native int writeEEPROM(int uiEEPAddress, short ucValue);
    public native int fnRxLED(int mode);
    public native int fnTxLED(int mode);
    public native int hardwareFlowControl(int onOff);
    public native int fnULoad(int onOff);
    public native int fnSuspend(int onOff);
    public native int fnInvertUartPol(int onOff);
    public native int fnSetBaudRate(long baudRateParam);
    public native int configureIO(short ioMap);
    public native int configureIoDefaultOutput(short ioMap, short ucDefValue);
}
