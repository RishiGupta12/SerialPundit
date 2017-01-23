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

package com.serialpundit.serial.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;

import com.serialpundit.core.SerialComException;
import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComSystemProperty;

/**
 * <p>This class is an interface between java and native shared library. The native library is found 
 * in the root of 'sp-tty.jar' file. Microchip provides only windows dll.</p>
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
     * <p>Extract native library from jar in a working directory, load and link it. The root of sp-tty.jar file 
     * is searched for the required native library for vendor specific communication. It also load vendor's 
     * native shared library.</p>
     *  
     * @param libDirectory directory in which native library will be extracted and vendor library will be found.
     * @param vlibName name of vendor library to load and link.
     * @param cpuArch architecture of CPU this library is running on.
     * @param osType operating system this library is running on.
     * @param serialComSystemProperty instance of SerialComSystemProperty to get required java properties.
     * @return true on success.
     * @throws SerialComException if java system properties can not be  accessed or required files can not be 
     *         accessed, if shared library is not found, it can not be loaded, linked and initialized etc.
     */
    public static boolean loadNativeLibrary(File libDirectory, String vlibName, int cpuArch, int osType, 
            SerialComSystemProperty serialComSystemProperty) throws SerialComException {

        String libToExtractFromJar = null;
        File libFile = null;
        InputStream input = null;
        FileOutputStream output = null;
        String fileSeparator = null;

        fileSeparator = serialComSystemProperty.getfileSeparator();
        if((fileSeparator == null) || (fileSeparator.length() == 0)) {
            throw new SerialComException("The file.separator java system property is either null or empty !");
        }

        /* Find the native library that will be extracted based on arch and os type */
        if(cpuArch == SerialComPlatform.ARCH_AMD64) {
            switch(osType) {
            case SerialComPlatform.OS_WINDOWS:
                libToExtractFromJar = "spmchpsiowinx64.dll";
                break;
            case SerialComPlatform.OS_LINUX:
                libToExtractFromJar = "spmchpsiolnxx64.so";
                break;
            case SerialComPlatform.OS_MAC_OS_X:
                libToExtractFromJar = "spmchpsiomacx64.dylib";
                break;
            default :
            }
        }
        else if(cpuArch == SerialComPlatform.ARCH_X86) {
            switch(osType) {
            case SerialComPlatform.OS_WINDOWS:
                libToExtractFromJar = "spmchpsiowinx86.dll";
                break;
            case SerialComPlatform.OS_LINUX:
                libToExtractFromJar = "spmchpsiolnxx86.so";
                break;
            case SerialComPlatform.OS_MAC_OS_X:
                libToExtractFromJar = "spmchpsiomacx86.dylib";
                break;
            default :
            }
        }
        else {
            throw new SerialComException("This architecture is unknown to this library. Please contact us !");
        }

        /* Extract shared library from jar into working directory */
        try {
            libFile = new File(libDirectory.getAbsolutePath() + fileSeparator + libToExtractFromJar);
            input = SerialComPortJNIBridge.class.getResourceAsStream("/" + libToExtractFromJar);
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
                    throw new SerialComException("Can not extract native shared library " + libToExtractFromJar + " from sp-tty.jar file !");
                }
            }else {
                throw new SerialComException("Can not get shared library " + libToExtractFromJar + " resource as stream from sp-tty.jar file !");
            }
        } catch (Exception e) {
            throw (SerialComException) new SerialComException(libFile.toString()).initCause(e);
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

        // load libraries in reverse order of dependencies

        /* Try loading the dynamic shared library from the local file system finally as privileged action */
        final File vlibFileFinal = new File(libDirectory.getAbsolutePath() + fileSeparator + vlibName);
        final File libFileFinal = libFile;

        try {
            AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
                public Boolean run() {
                    /* vendor supplied shared library. SimpleIO-UM.dll has to be linked explicitly as it does 
                     * not have .lib file associated. Native layer will load and resolve all symbols to external 
                     * SimpleIO-UM.dll. */
                    loadAndLinkSimpleIODLL(vlibFileFinal.toString());
                    return true;
                }
            });
        } catch (Exception e) {
            throw (SerialComException) new SerialComException("Could not load " + vlibFileFinal.toString() + " native library !").initCause(e);
        } catch (UnsatisfiedLinkError e) {
            throw (SerialComException) new SerialComException(e.getMessage()).initCause(e);
        }

        try {
            AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
                public Boolean run() {
                    // JNI glue shared library
                    System.load(libFileFinal.toString());
                    return true;
                }
            });
        } catch (Exception e) {
            throw (SerialComException) new SerialComException("Could not load " + libFileFinal.toString() + " native library !").initCause(e);
        } catch (UnsatisfiedLinkError e) {
            throw (SerialComException) new SerialComException(e.getMessage()).initCause(e);
        }

        return true;
    }

    /* Native library calls */
    private native static int loadAndLinkSimpleIODLL(String vendorLibraryWithAbsolutePath); // native lib to resolve symbols dynamically

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
