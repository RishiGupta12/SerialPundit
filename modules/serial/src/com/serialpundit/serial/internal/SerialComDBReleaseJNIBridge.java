/*
 * This file is part of SerialPundit.
 * 
 * Copyright (C) 2014-2021, Rishi Gupta. All rights reserved.
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
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.io.InputStream;
import java.io.FileOutputStream;

import com.serialpundit.core.SerialComException;
import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComSystemProperty;

/**
 * <p>This class is an interface between java and native shared library. The native library is found 
 * in the root of 'sp-tty.jar' file.</p>
 * 
 * @author Rishi Gupta
 */
public final class SerialComDBReleaseJNIBridge {

    /**
     * <p>Allocates a new SerialComDBReleaseJNIBridge object.</p>
     */
    public SerialComDBReleaseJNIBridge() {
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
     * @return true on success.
     * @throws SerialComException if java system properties can not be  accessed or required files can not be 
     *         accessed, if shared library is not found, it can not be loaded, linked and initialized etc.
     */
    public static boolean loadNativeLibrary(String directoryPath, String loadedLibName, SerialComSystemProperty serialComSystemProperty,
            int osType, int cpuArch, int abiType) throws SerialComException {

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
                libToExtractFromJar = "spdbrwinx64.dll";
                libExtension = ".dll";
                break;
            case SerialComPlatform.OS_LINUX:
                libToExtractFromJar = "spdbrlnxx64.so";
                libExtension = ".so";
                break;
            case SerialComPlatform.OS_MAC_OS_X:
                libToExtractFromJar = "spdbrmacx64.dylib";
                libExtension = ".dylib";
                break;
            default :
            }
        }
        else if(cpuArch == SerialComPlatform.ARCH_X86) {
            switch(osType) {
            case SerialComPlatform.OS_WINDOWS:
                libToExtractFromJar = "spdbrwinx86.dll";
                libExtension = ".dll";
                break;
            case SerialComPlatform.OS_LINUX:
                libToExtractFromJar = "spdbrlnxx86.so";
                libExtension = ".so";
                break;
            case SerialComPlatform.OS_MAC_OS_X:
                libToExtractFromJar = "spdbrmacx86.dylib";
                libExtension = ".dylib";
                break;
            default :
            }
        }
        else if(cpuArch == SerialComPlatform.ARCH_ARMV7) {
            if(osType == SerialComPlatform.OS_LINUX) {
                libExtension = ".so";
                if(abiType == SerialComPlatform.ABI_ARMHF) {
                    libToExtractFromJar = "spdbrlnxarmv7hf.so";
                }else if(abiType == SerialComPlatform.ABI_ARMEL) {
                    libToExtractFromJar = "spdbrlnxarmv7el.so";
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
                    libToExtractFromJar = "spdbrlnxarmv6hf.so";
                }else if(abiType == SerialComPlatform.ABI_ARMEL) {
                    libToExtractFromJar = "spdbrlnxarmv6el.so";
                }else {
                }
            }else {
                throw new SerialComException("Please report us your platform !");
            }
        }
        else if(cpuArch == SerialComPlatform.ARCH_ARMV5) {
            if(osType == SerialComPlatform.OS_LINUX) {
                libExtension = ".so";
                libToExtractFromJar = "spdbrlnxarmv5.so";
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
                    // congratulations successfully extracted
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
        } catch (UnsatisfiedLinkError e) {
            throw (SerialComException) new SerialComException(e.getMessage()).initCause(e);
        }

        return true;
    }

    /* Native library calls */

    public native int startSerialComDBReleaseSerive(); // loads driver on demand and initializes what is required.
    public native int stopSerialComDBReleaseSerive();
    public native int releaseComPort(String comPortName);
    public native int releaseAllComPorts(String[] excludeList);
    public native String[] getComPortNumbersInUse();
    public native int getCurrentComDBDatabaseSize();
    public native int resizeComDBDatabase(int newSize);
}
