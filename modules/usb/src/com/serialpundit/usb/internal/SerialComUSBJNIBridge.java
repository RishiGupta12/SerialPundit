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

package com.serialpundit.usb.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.serialpundit.core.SerialComException;
import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComSystemProperty;
import com.serialpundit.usb.ISerialComUSBHotPlugListener;

/**
 * <p>This class is an interface between java and native shared library. The native library is found 
 * in the root of 'sp-usb.jar' file. Library will be spusblnxx64.so etc.</p>
 * 
 * @author Rishi Gupta
 */
public final class SerialComUSBJNIBridge {

    private static final Comparator<String> comparator = new Comparator<String>() {

        public int compare(final String valueA, final String valueB) {

            if(valueA.equalsIgnoreCase(valueB)){
                return valueA.compareTo(valueB);
            }

            int al = valueA.length();
            int bl = valueB.length();
            int minLength = (al <= bl) ? al : bl;

            int shiftA = 0;
            int shiftB = 0;

            for(int i = 0; i < minLength; i++){

                char charA = valueA.charAt(i - shiftA);
                char charB = valueB.charAt(i - shiftB);

                if(charA != charB){
                    if(Character.isDigit(charA) && Character.isDigit(charB)){

                        int[] resultsA = {-1, (i - shiftA)};
                        String numVal = "";
                        for(int x = (i - shiftA); x < al; x++){
                            resultsA[1] = x;
                            char c = valueA.charAt(x);
                            if(Character.isDigit(c)){
                                numVal += c;
                            }else {
                                break;
                            }
                        }
                        try {
                            resultsA[0] = Integer.valueOf(numVal);
                        } catch (Exception e) {
                            //Do nothing
                        }

                        int[] resultsB = {-1, (i - shiftB)};
                        numVal = "";
                        for(int x = (i - shiftB); x < bl; x++){
                            resultsB[1] = x;
                            char c = valueB.charAt(x);
                            if(Character.isDigit(c)){
                                numVal += c;
                            }else {
                                break;
                            }
                        }
                        try {
                            resultsB[0] = Integer.valueOf(numVal);
                        } catch (Exception e) {
                            //Do nothing
                        }

                        if(resultsA[0] != resultsB[0]){
                            return resultsA[0] - resultsB[0];
                        }
                        if(al < bl){
                            i = resultsA[1];
                            shiftB = resultsA[1] - resultsB[1];
                        }else {
                            i = resultsB[1];
                            shiftA = resultsB[1] - resultsA[1];
                        }
                    }else {
                        if(Character.toLowerCase(charA) - Character.toLowerCase(charB) != 0){
                            return Character.toLowerCase(charA) - Character.toLowerCase(charB);
                        }
                    }
                }
            }
            return valueA.compareToIgnoreCase(valueB);
        }
    };

    /**
     * <p>Allocates a new SerialComUSBJNIBridge object.</p>
     */
    public SerialComUSBJNIBridge() {
    }

    /**
     * <p>Extract native library from jar in a 'directoryPath' directory, load and link it. Native library is found in 
     * the root of 'sp-usb.jar' file.</p> 
     * 
     * @param directoryPath null for default directory or user supplied directory path.
     * @param loadedLibName null for default name or user supplied name of loaded library.
     * @param serialComSystemProperty instance of SerialComSystemProperty to get required java properties.
     * @param cpuArch architecture of CPU this library is running on.
     * @param osType operating system this library is running on.
     * @param abitype binary application interface type to correctly link.
     * @return true on success.
     * @throws SerialComException if java system properties can not be  accessed or required files can not be 
     *         accessed, if shared library is not found, it can not be loaded, linked and initialized etc.
     */
    public static boolean loadNativeLibrary(String directoryPath, String loadedLibName, SerialComSystemProperty serialComSystemProperty,
            int osType, int cpuArch, int abitype) throws SerialComException  {

        String javaTmpDir = null;
        String userHomeDir = null;
        String fileSeparator = null;
        File baseDir = null;
        File workingDir = null;
        boolean isTmpDir = false;
        boolean isUserHomeDir = false;
        String libToExtractFromJar = null;
        File libFile = null;
        String libExtension = null;
        InputStream input = null;
        FileOutputStream output = null;

        fileSeparator = serialComSystemProperty.getfileSeparator();
        if((fileSeparator == null) || (fileSeparator.length() == 0)) {
            throw new SerialComException("The file.separator java system property is either null or empty !");
        }

        /* Prepare directory in which native shared library will be extracted from jar */
        if(directoryPath == null) {
            // user did not supplied any directory path so try tmp and user home
            javaTmpDir = serialComSystemProperty.getJavaIOTmpDir();
            if((javaTmpDir == null) || (javaTmpDir.length() == 0)) {
                throw new SerialComException("The java.io.tmpdir java system property is either null or empty !");
            }

            baseDir = new File(javaTmpDir);
            if(baseDir.exists() && baseDir.isDirectory() && baseDir.canWrite()) {
                isTmpDir = true;
                // temp directory will be used
            }else {
                // access to temp directory failed, let us try access to user's home directory
                userHomeDir = serialComSystemProperty.getUserHome();
                if((userHomeDir == null) || (userHomeDir.length() == 0)) {
                    throw new SerialComException("The user.home java system property is either null or empty !");
                }
                baseDir = new File(userHomeDir);
                if(!baseDir.exists()) {
                    throw new SerialComException("User home directory does not exist. Also unable to access tmp/temp directory !");
                }
                if(!baseDir.isDirectory()) {
                    throw new SerialComException("User home directory is not a directory. Also unable to access tmp/temp directory !");
                }
                if(!baseDir.canWrite()) {
                    throw new SerialComException("User home directory is not writeable (permissions ??). Also unable to access tmp/temp directory !");
                }
                isUserHomeDir = true;
            }

            // for tmp or user home create unique directory inside them for our use only
            workingDir = new File(baseDir.toString() + fileSeparator + "sp_tuartx1");
            if(!workingDir.exists()) {
                if(!workingDir.mkdir()) {
                    if(isTmpDir == true) {
                        throw new SerialComException("Can not create sp_tuartx1 unique directory in tmp/temp directory !");
                    }else if(isUserHomeDir == true) {
                        throw new SerialComException("Can not create sp_tuartx1 unique directory in user's home directory !");
                    }else {
                    }
                }
            }
        }else {
            // user specified directory, so try it
            baseDir = new File(directoryPath);
            if(!baseDir.exists()) {
                throw new SerialComException("Given " + directoryPath + " directory does not exist !");
            }
            if(!baseDir.isDirectory()) {
                throw new SerialComException("Given " + directoryPath + " is not a directory !");
            }
            if(!baseDir.canWrite()) {
                throw new SerialComException("Given " + directoryPath + " directory is not writeable !");
            }

            // for user specified directory base itself will be working directory
            workingDir = baseDir;
        }

        /* Find the native library that will be extracted based on arch and os type */
        if(cpuArch == SerialComPlatform.ARCH_AMD64) {
            switch(osType) {
            case SerialComPlatform.OS_WINDOWS:
                libToExtractFromJar = "spusbwinx64.dll";
                libExtension = ".dll";
                break;
            case SerialComPlatform.OS_LINUX:
                libToExtractFromJar = "spusblnxx64.so";
                libExtension = ".so";
                break;
            case SerialComPlatform.OS_MAC_OS_X:
                libToExtractFromJar = "spusbmacx64.dylib";
                libExtension = ".dylib";
                break;
            default :
            }
        }
        else if(cpuArch == SerialComPlatform.ARCH_X86) {
            switch(osType) {
            case SerialComPlatform.OS_WINDOWS:
                libToExtractFromJar = "spusbwinx86.dll";
                libExtension = ".dll";
                break;
            case SerialComPlatform.OS_LINUX:
                libToExtractFromJar = "spusblnxx86.so";
                libExtension = ".so";
                break;
            case SerialComPlatform.OS_MAC_OS_X:
                libToExtractFromJar = "spusbmacx86.dylib";
                libExtension = ".dylib";
                break;
            default :
            }
        }
        else if(cpuArch == SerialComPlatform.ARCH_ARMV7) {
            if(osType == SerialComPlatform.OS_LINUX) {
                libExtension = ".so";
                if(abitype == SerialComPlatform.ABI_ARMHF) {
                    libToExtractFromJar = "spusblnxarmv7hf.so";
                }else if(abitype == SerialComPlatform.ABI_ARMEL) {
                    libToExtractFromJar = "spusblnxarmv7el.so";
                }else {
                }
            }else {
                throw new SerialComException("Please report us your platform !");
            }
        }
        else if(cpuArch == SerialComPlatform.ARCH_ARMV6) {
            if(osType == SerialComPlatform.OS_LINUX) {
                libExtension = ".so";
                if(abitype == SerialComPlatform.ABI_ARMHF) {
                    libToExtractFromJar = "spusblnxarmv6hf.so";
                }else if(abitype == SerialComPlatform.ABI_ARMEL) {
                    libToExtractFromJar = "spusblnxarmv6el.so";
                }else {
                }
            }else {
                throw new SerialComException("Please report us your platform !");
            }
        }
        else if(cpuArch == SerialComPlatform.ARCH_ARMV5) {
            if(osType == SerialComPlatform.OS_LINUX) {
                libExtension = ".so";
                libToExtractFromJar = "spusblnxarmv5.so";
            }
        }
        else {
            throw new SerialComException("This architecture is unknown to this library. Please contact us !");
        }

        /* Extract shared library from jar into working directory */
        try {
            if(loadedLibName == null) {
                libFile = new File(workingDir.getAbsolutePath() + fileSeparator + libToExtractFromJar);
            }else {
                libFile = new File(workingDir.getAbsolutePath() + fileSeparator + loadedLibName.trim() + libExtension);
            }

            input = SerialComUSBJNIBridge.class.getResourceAsStream("/" + libToExtractFromJar);
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

                if((libFile != null) && libFile.exists() && libFile.isFile()) {
                    // congratulations successfully extracted
                }else {
                    throw new SerialComException("Can not extract native shared library " + libToExtractFromJar + " from sp-usb.jar file !");
                }
            }else {
                throw new SerialComException("Can not get shared library " + libToExtractFromJar + " resource as stream from sp-usb.jar file !");
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

        /* Try loading the dynamic shared library from the local file system finally as privileged action */
        final File libFileFinal = libFile;
        try {
            AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
                public Boolean run() {
                    System.load(libFileFinal.toString());
                    return true;
                }
            });
        } catch (Exception e) {
            throw (SerialComException) new SerialComException("Could not load " + libFile.toString() + " native library !").initCause(e);
        }

        return true;
    }

    public String[] findComPortFromUSBAttribute(int usbVidToMatch, int usbPidToMatch, String serialNumber) {
        String[] ports = findComPortFromUSBAttributes(usbVidToMatch, usbPidToMatch, serialNumber);
        if(ports != null) {
            if(ports.length < 2) {
                return ports;
            }else {
                ArrayList<String> portsFound = new ArrayList<String>();
                for(String portName : ports){
                    portsFound.add(portName);
                }
                Collections.sort(portsFound, comparator);
                return portsFound.toArray(new String[portsFound.size()]);
            }
        }
        return null;
    }

    /* Native library calls */
    
    public native void initNativeLib();

    // usb
    public native String[] listUSBdevicesWithInfo(int vendorFilter);
    public native String[] getFirmwareRevisionNumber(int vid, int pid, String serialNumber);
    public native int registerUSBHotPlugEventListener(ISerialComUSBHotPlugListener hotPlugListener, int filterVID, int filterPID, String serialNumber);
    public native int unregisterUSBHotPlugEventListener(int index);
    public native int isUSBDevConnected(int vendorID, int productID, String serialNumber);

    // serial port
    public native String[] findComPortFromUSBAttributes(int usbVidToMatch, int usbPidToMatch, String serialNumber);
    public native String[] getCDCUSBDevPowerInfo(String portNameVal);
    public native int setLatencyTimer(String comPort, byte timerValue);
    public native int getLatencyTimer(String comPort);

    // hid
    public native String[] listUSBHIDdevicesWithInfo(int vendorFilter);
}
