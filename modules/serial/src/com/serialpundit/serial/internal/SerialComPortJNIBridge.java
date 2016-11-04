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
import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Comparator;
import java.io.InputStream;
import java.io.FileOutputStream;

import com.serialpundit.core.NativeLibLoader;
import com.serialpundit.core.SerialComException;
import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComSystemProperty;
import com.serialpundit.serial.SerialComLineErrors;
import com.serialpundit.serial.internal.SerialComLooper;
import com.serialpundit.serial.nullmodem.Itty2comHotPlugListener;

/**
 * <p>This class is an interface between java and native shared library. The native library is found 
 * in the root of 'sp-tty.jar' file.</p>
 * 
 * <p>In Linux/Windows/Solaris it is possible to run 32 bit JVM on 64 bit OS, while perhaps in mac os x 
 * JVM must match with underlying OS architecture. Whether 32 or 64 bit sp-dll/so file is to be loaded, 
 * is decided by arch of JVM this process is running on. SerialPundit includes both 32 and 64 bit dll/so 
 * and therefore 32 bit application using this library with 32 JVM can be run on a 64 bit OS system.</p>
 * 
 * @author Rishi Gupta
 */
public final class SerialComPortJNIBridge {

    private static final Comparator<String> comparator = new Comparator<String>() {

        @Override
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
     * <p>Allocates a new SerialComPortJNIBridge object.</p>
     */
    public SerialComPortJNIBridge() {
    }

    /**
     * <p>Extract native library from jar in a working directory, load and link it. The native library is found 
     * in the root of 'sp-tty.jar' file.</p> 
     * 
     * @param directoryPath null for default directory or user supplied directory path.
     * @param loadedLibName null for default name or user supplied name of loaded library.
     * @param serialComSystemProperty instance of SerialComSystemProperty to get required java properties.
     * @param cpuArch architecture of CPU this library is running on.
     * @param osType operating system this library is running on.
     * @param abiType binary application interface type to correctly link.
     * @param hotDeploy true if tomcat hot deployment is needed otherwise false.
     * @return true on success.
     * @throws SerialComException if java system properties can not be  accessed or required files can not be 
     *         accessed, if shared library is not found, it can not be loaded, linked and initialized etc.
     */
    public static boolean loadNativeLibrary(String directoryPath, String loadedLibName, SerialComSystemProperty serialComSystemProperty,
            int osType, int cpuArch, int abiType, boolean hotDeploy) throws SerialComException {

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

        try {
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
        } catch (Exception e) {
            throw (SerialComException) new SerialComException(e.getMessage()).initCause(e);
        }

        /* Find the native library that will be extracted based on arch and os type */
        if(cpuArch == SerialComPlatform.ARCH_AMD64) {
            switch(osType) {
            case SerialComPlatform.OS_WINDOWS:
                libToExtractFromJar = "spcomwinx64.dll";
                libExtension = ".dll";
                break;
            case SerialComPlatform.OS_LINUX:
                libToExtractFromJar = "spcomlnxx64.so";
                libExtension = ".so";
                break;
            case SerialComPlatform.OS_MAC_OS_X:
                libToExtractFromJar = "spcommacx64.dylib";
                libExtension = ".dylib";
                break;
            default :
            }
        }
        else if(cpuArch == SerialComPlatform.ARCH_X86) {
            switch(osType) {
            case SerialComPlatform.OS_WINDOWS:
                libToExtractFromJar = "spcomwinx86.dll";
                libExtension = ".dll";
                break;
            case SerialComPlatform.OS_LINUX:
                libToExtractFromJar = "spcomlnxx86.so";
                libExtension = ".so";
                break;
            case SerialComPlatform.OS_MAC_OS_X:
                libToExtractFromJar = "spcommacx86.dylib";
                libExtension = ".dylib";
                break;
            default :
            }
        }
        else if(cpuArch == SerialComPlatform.ARCH_ARMV8) {
            if(osType == SerialComPlatform.OS_LINUX) {
                libExtension = ".so";
                if(abiType == SerialComPlatform.ABI_ARMHF) {
                    libToExtractFromJar = "spcomlnxarmv8hf.so";
                }else if(abiType == SerialComPlatform.ABI_ARMEL) {
                    libToExtractFromJar = "spcomlnxarmv8el.so";
                }else {
                }
            }else {
                throw new SerialComException("Please report us your platform !");
            }
        }
        else if(cpuArch == SerialComPlatform.ARCH_ARMV7) {
            if(osType == SerialComPlatform.OS_LINUX) {
                libExtension = ".so";
                if(abiType == SerialComPlatform.ABI_ARMHF) {
                    libToExtractFromJar = "spcomlnxarmv7hf.so";
                }else if(abiType == SerialComPlatform.ABI_ARMEL) {
                    libToExtractFromJar = "spcomlnxarmv7el.so";
                }else {
                }
            }else {
                throw new SerialComException("Please report us your platform !");
            }
        }
        else if(cpuArch == SerialComPlatform.ARCH_ARMV6) {
            if(osType == SerialComPlatform.OS_LINUX) {
                libExtension = ".so";
                if(abiType == SerialComPlatform.ABI_ARMHF) {
                    libToExtractFromJar = "spcomlnxarmv6hf.so";
                }else if(abiType == SerialComPlatform.ABI_ARMEL) {
                    libToExtractFromJar = "spcomlnxarmv6el.so";
                }else {
                }
            }else {
                throw new SerialComException("Please report us your platform !");
            }
        }
        else if(cpuArch == SerialComPlatform.ARCH_ARMV5) {
            if(osType == SerialComPlatform.OS_LINUX) {
                libExtension = ".so";
                libToExtractFromJar = "spcomlnxarmv5.so";
            }
        }
        else {
            throw new SerialComException("This architecture is unknown to serialpundit. Please contact us !");
        }

        try {
            // For hot deployment if the library is already extracted, loaded/linked return without going further.
            // For desktop applications, the static variable 'nativeLibLoadAndInitAlready' would prevent extraction 
            // and loading if it has been already done previously.
            if(hotDeploy == true) {
                try {
                    Class.forName("com.serialpundit.serial.internal.NativeLoaderUART");
                    return true;
                } catch (ClassNotFoundException e) {
                }
            }

            // Extraction required, extract native shared library from jar into working directory 
            try {
                if(loadedLibName == null) {
                    libFile = new File(workingDir.getAbsolutePath() + fileSeparator + libToExtractFromJar);
                }else {
                    libFile = new File(workingDir.getAbsolutePath() + fileSeparator + loadedLibName.trim() + libExtension);
                }

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

                    if((libFile != null) && libFile.exists() && libFile.isFile()) {
                        // congratulations successfully extracted native shared library
                    }else {
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

            if(hotDeploy != true) {
                // Try loading the dynamic shared library from the local file system finally as privileged action
                final File libFileFinal = libFile;
                AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
                    public Boolean run() {
                        System.load(libFileFinal.toString());
                        return true;
                    }
                });
            }
            else {
                InputStream cin = SerialComPortJNIBridge.class.getResourceAsStream("NativeLoaderUART.class");
                if(cin == null) {
                    throw new SerialComException("NativeLoaderUART.class not found in jar file !");
                }
                NativeLibLoader nll = new NativeLibLoader("com.serialpundit.serial.internal.NativeLoaderUART", cin);
                nll.load(libFile.toString());
                cin.close();
            }
        } catch (Exception e) {
            throw (SerialComException) new SerialComException(e.getMessage()).initCause(e);
        } catch (UnsatisfiedLinkError e) {
            throw (SerialComException) new SerialComException(e.getMessage()).initCause(e);
        }

        return true;
    }

    /* Native library calls */
    public native int initNativeLib();
    public native String getNativeLibraryVersion();
    public native String[] listAvailableComPorts();

    public native int setUpDataLooperThread(long handle, SerialComLooper looper);
    public native int setUpEventLooperThread(long handle, SerialComLooper looper);
    public native int destroyDataLooperThread(long handle);
    public native int destroyEventLooperThread(long handle);
    public native int pauseListeningEvents(long handle);
    public native int resumeListeningEvents(long handle);

    // Open-close
    public native long openComPort(String portName, boolean enableRead, boolean enableWrite, boolean exclusiveOwner);
    public native int closeComPort(long handle);

    // read
    public native byte[] readBytes(long handle, int byteCount);
    public native int readBytesP(long handle, byte[] buffer, int offset, int length, long context, SerialComLineErrors lineErr);
    public native byte[] readBytesBlocking(long handle, int byteCount, long context);
    public native int readBytesDirect(long handle, ByteBuffer buffer, int offset, int length);

    // write
    public native int writeBytes(long handle, byte[] buffer, int delay);
    public native int writeBytesDirect(long handle, ByteBuffer buffer, int offset, int length);
    public native int writeSingleByte(long handle, byte dataByte);
    public native int writeBytesBlocking(long handle, byte[] buffer, long context);

    public native long createBlockingIOContext();
    public native int unblockBlockingIOOperation(long context);
    public native int destroyBlockingIOContext(long context);

    // Modem control, buffer
    public native int setRTS(long handle, boolean enabled);
    public native int setDTR(long handle, boolean enabled);
    public native int[] getLinesStatus(long handle);
    public native int[] getInterruptCount(long handle);
    public native String findDriverServingComPort(String comPortName);
    public native String findIRQnumberForComPort(long handle);
    public native int sendBreak(long handle, int duration);
    public native int[] getByteCount(long handle);
    public native int clearPortIOBuffers(long handle, boolean rxPortbuf, boolean txPortbuf);

    // Configuration
    public native int configureComPortData(long handle, int dataBits, int stopBits, int parity, int baudRateTranslated, int custBaudTranslated);
    public native int configureComPortControl(long handle, int flowctrl, byte xonCh, byte xoffCh, boolean ParFraError, boolean overFlowErr);
    public native int[] getCurrentConfigurationU(long handle);
    public native String[] getCurrentConfigurationW(long handle);
    public native int fineTuneRead(long handle, int vmin, int vtime, int rit, int rttm, int rttc);

    // Null modem driver
    public native int setuptty2com();
    public native int unsetuptty2com();
    public native String[] listNextAvailablePorts();
    public native String[] listExistingStandardNullModemPorts();
    public native String[] listExistingCustomNullModemPorts();
    public native String[] listExistingStandardLoopbackPorts();
    public native String[] listExistingCustomLoopbackPorts();
    public native String[] listAllExistingPorts();
    public native String[] listAllExistingPortsWithInfo();
    public native String[] createStandardNullModemPair(int deviceIndex1, int deviceIndex2);
    public native String[] createCustomNullModemPair(int idx1, int rtsMap1, int dtrMap1, boolean setDTRatOpen1, int idx2, int rtsMap2, int dtrMap2, boolean setDTRatOpen2);
    public native String[] createStandardLoopBackDevice(int deviceIndex);
    public native String[] createCustomLoopBackDevice(int deviceIndex, int rtsMap, int dtrMap, boolean setDTRatOpen);
    public native int destroyAllCreatedVirtualDevices();
    public native int destroyAllCreatedNullModemPairs();
    public native int destroyAllCreatedLoopbackDevices();
    public native int destroyGivenVirtualDevice(String device);
    public native String[] getLastLoopBackDeviceNode();
    public native String[] getLastNullModemPairNodes();
    public native int emulateSerialEvent(String devNode, int error);
    public native int emulateLineRingingEvent(String devNode, boolean state);
    public native int registerTTY2COMHotPlugEventListener(Itty2comHotPlugListener hotPlugListener, String deviceNode);
    public native int unregisterTTY2COMHotPlugEventListener(int opaqueHandle);
    public native String[] getStatsForGivenDevice(String deviceNode);
    public native Object emulateFaultyCable(String deviceNode, boolean state);
}
