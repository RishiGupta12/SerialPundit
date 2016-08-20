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
 * in the root of 'sp-tty.jar' file.</p>
 * 
 * @author Rishi Gupta
 */
public final class SerialComFTDID2XXJNIBridge {

    /**
     * <p>Allocates a new SerialComFTDID2XXJNIBridge object.</p>
     */
    public SerialComFTDID2XXJNIBridge() {
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
                libToExtractFromJar = "spd2xxwinx64.dll";
                break;
            case SerialComPlatform.OS_LINUX:
                libToExtractFromJar = "spd2xxlnxx64.so";
                break;
            case SerialComPlatform.OS_MAC_OS_X:
                libToExtractFromJar = "spd2xxmacx64.dylib";
                break;
            default :
            }
        }
        else if(cpuArch == SerialComPlatform.ARCH_X86) {
            switch(osType) {
            case SerialComPlatform.OS_WINDOWS:
                libToExtractFromJar = "spd2xxwinx86.dll";
                break;
            case SerialComPlatform.OS_LINUX:
                libToExtractFromJar = "spd2xxlnxx86.so";
                break;
            case SerialComPlatform.OS_MAC_OS_X:
                libToExtractFromJar = "spd2xxmacx86.dylib";
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
                    // vendor supplied shared library
                    System.load(vlibFileFinal.toString());
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
    /* D2XX Classic Functions */
    public native int setVidPid(int vid, int pid);
    public native int[] getVidPid();
    public native int createDeviceInfoList();
    public native String[] getDeviceInfoList(int numOfDevices);
    public native String[] getDeviceInfoDetail(int index);
    public native String[] listDevices(int pvArg1, int dwFlags);
    public native long open(int index);
    public native long openEx(String serialOrDescription, long locationId, int dwFlags);
    public native int close(long handle);
    public native int read(long handle, byte[] buffer, int numOfBytesToRead);
    public native int write(long handle, byte[] buffer, int numOfBytesToWrite);
    public native int setBaudRate(long handle, int baudRate);
    public native int setDivisor(long handle, int divisor);
    public native int setDataCharacteristics(long handle, int dataBits, int stopBits, int parity);
    public native int setTimeouts(long handle, long readTimeOut, long writeTimeOut);
    public native int setFlowControl(long handle, int flctrl, byte xonch, byte xoffch);
    public native int setDTR(long handle);
    public native int clearDTR(long handle);
    public native int setRTS(long handle);
    public native int clearRTS(long handle);
    public native int getModemStatus(long handle);
    public native int getQueueStatus(long handle);
    public native String[] getDeviceInfo(long handle);
    public native long getDriverVersion(long handle);
    public native long getLibraryVersion();
    public native long getComPortNumber(long handle);
    public native long[] getStatus(long handle);
    public native int setEventNotificationAndWait(long handle, int eventMask);
    public native int setChars(long handle, byte evch, byte even, byte erch, byte eren);
    public native int setBreakOn(long handle);
    public native int setBreakOff(long handle);
    public native int purge(long handle, boolean purgeTxBuffer, boolean purgeRxBuffer);
    public native int resetDevice(long handle);
    public native int resetPort(long handle);
    public native int cyclePort(long handle);
    public native int rescan();
    public native int reload(int vid, int pid);
    public native int setResetPipeRetryCount(long handle, int count);
    public native int stopInTask(long handle);
    public native int restartInTask(long handle);
    public native int setDeadmanTimeout(long handle, int count);

    /* EEPROM Programming Interface Functions */
    public native int readEE(long handle, int offset);
    public native int writeEE(long handle, int offset, int valueToWrite);
    public native int eraseEE(long handle);
    public native int[] eeRead(long handle, int version, char[] manufacturer, char[] manufacturerID, 
            char[] description, char[] serialNumber);
    // eeReadEx also calls eeRead()
    public native int eeProgram(long handle, String manufacturer, String manufacturerID, 
            String description, String serialNumber, int[] values);
    public native int eeProgramEx(long handle, String manufacturer, String manufacturerID, 
            String description, String serialNumber, int[] values);
    public native int eeUAsize(long handle);
    public native int eeUAread(long handle, byte[] buffer, int length);
    public native int eeUAwrite(long handle, byte[] buffer, int length);
    public native int[] eepromRead(long handle, int deviceType, char[] manufacturer,
            char[] manufacturerID, char[] description, char[] serialNumber);
    public native int eepromProgram(long handle, int devType, int[] dataToBeWritten,
            String manufacturer, String manufacturerID, String description,
            String serialNumber);

    /* Extended API Functions */
    public native int setLatencyTimer(long handle, int value);
    public native int getLatencyTimer(long handle);
    public native int setBitMode(long handle, int mask, int mode);
    public native int getBitMode(long handle);
    public native int setUSBParameters(long handle, int inTransferSize, int outTransferSize);

    /* FT-Win32 API Functions */
    public native long w32CreateFile(String serialNum, String description,
            long location, int dwAttrsAndFlags, int dwAccess, boolean overLapped);
    public native int w32CloseHandle(long handle);
    public native int w32ReadFile(long handle, byte[] buffer, int numOfBytesToRead);
    public native int w32WriteFile(long handle, byte[] buffer, int numOfBytesToWrite);
    public native int w32GetOverlappedResult(long handle, boolean wait);
    public native int w32EscapeCommFunction(long handle, short function);
    public native int w32GetCommModemStatus(long handle);
    public native int w32SetupComm(long handle, int readBufSize, int writeBufSize);
    public native int w32SetCommState(long handle, int[] dcb);
    public native int[] w32GetCommState(long handle);
    public native int w32SetCommTimeouts(long handle, int readIntervalTimeout,
            int readTotalTimeoutMultiplier, int readTotalTimeoutConstant,
            int writeTotalTimeoutMultiplier, int writeTotalTimeoutConstant);
    public native int[] w32GetCommTimeouts(long handle);
    public native int w32SetCommBreak(long handle);
    public native int w32ClearCommBreak(long handle);
    public native int w32SetCommMask(long handle, int flag);
    public native int w32GetCommMask(long handle);
    public native int w32WaitCommEvent(long handle, int event);
    public native int w32PurgeComm(long handle, int event);
    public native String w32GetLastError(long handle);
    public native int[] w32ClearCommError(long handle);
}
