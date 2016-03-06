/*
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

package com.embeddedunveiled.serial.internal;

import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;

import com.embeddedunveiled.serial.SerialComLoadException;
import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.SerialComUnexpectedException;
import com.embeddedunveiled.serial.internal.SerialComSystemProperty;

/**
 * <p>This class is an interface between java and native shared library. The native library is found 
 * in 'lib-comdbfree' folder in 'scm-x.x.x.jar' file.</p>
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
	 * <p>Extract native library from jar in a working directory, load and link it. The 'lib-comdbfree' folder in 
	 * 'scm-x.x.x.jar'.</p> 
	 * 
	 * @param directoryPath null for default directory or user supplied directory path.
	 * @param loadedLibName null for default name or user supplied name of loaded library.
	 * @param serialComSystemProperty instance of SerialComSystemProperty to get required java properties.
	 * @param cpuArch architecture of CPU this library is running on.
	 * @param osType operating system this library is running on.
	 * @param javaABIType binary application interface type to correctly link.
	 * @throws SecurityException if java system properties can not be  accessed.
	 * @throws SerialComUnexpectedException if java system property is null.
	 * @throws SerialComLoadException if any file system related issue occurs.
	 * @throws UnsatisfiedLinkError if loading/linking shared library fails.
	 */
	public static boolean loadNativeLibrary(String directoryPath, String loadedLibName, SerialComSystemProperty serialComSystemProperty,
			int osType, int cpuArch, int javaABIType) throws SerialComUnexpectedException, SerialComLoadException {

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
		if(fileSeparator == null) {
			throw new SerialComUnexpectedException("The file.separator java system property is null in the system !");
		}

		/* Prepare directory in which native shared library will be extracted from jar */
		if(directoryPath == null) {
			// user did not supplied any directory path so try tmp and user home
			javaTmpDir = serialComSystemProperty.getJavaIOTmpDir();
			if(javaTmpDir == null) {
				throw new SerialComUnexpectedException("The java.io.tmpdir java system property is null in the system !");
			}

			baseDir = new File(javaTmpDir);
			if(baseDir.exists() && baseDir.isDirectory() && baseDir.canWrite()) {
				isTmpDir = true;
				// temp directory will be used
			}else {
				// access to temp directory failed, let us try access to user's home directory
				userHomeDir = serialComSystemProperty.getUserHome();
				if(userHomeDir == null) {
					throw new SerialComUnexpectedException("The user.home java system property is null in the system !");
				}
				baseDir = new File(userHomeDir);
				if(!baseDir.exists()) {
					throw new SerialComLoadException("User home directory does not exist. Also unable to access tmp/temp directory !");
				}
				if(!baseDir.isDirectory()) {
					throw new SerialComLoadException("User home directory is not a directory. Also unable to access tmp/temp directory !");
				}
				if(!baseDir.canWrite()) {
					throw new SerialComLoadException("User home directory is not writeable (permissions ??). Also unable to access tmp/temp directory !");
				}
				isUserHomeDir = true;
			}

			// for tmp or user home create unique directory inside them for our use only
			workingDir = new File(baseDir.toString() + fileSeparator + "scm_tuartx1");
			if(!workingDir.exists()) {
				if(!workingDir.mkdir()) {
					if(isTmpDir == true) {
						throw new SerialComLoadException("Can not create scm_tuartx1 unique directory in temp directory !");
					}else if(isUserHomeDir == true) {
						throw new SerialComLoadException("Can not create scm_tuartx1 unique directory in user home directory !");
					}else {
					}
				}
			}
		}else {
			// user specified directory, so try it
			baseDir = new File(directoryPath);
			if(!baseDir.exists()) {
				throw new SerialComLoadException("Given " + directoryPath + " directory does not exist !");
			}
			if(!baseDir.isDirectory()) {
				throw new SerialComLoadException("Given " + directoryPath + " is not a directory !");
			}
			if(!baseDir.canWrite()) {
				throw new SerialComLoadException("Given " + directoryPath + " directory is not writeable !");
			}

			// for user specified directory base itself will be working directory
			workingDir = baseDir;
		}

		/* Find the native library that will be extracted based on arch and os type */
		if(cpuArch == SerialComManager.ARCH_AMD64) {
			switch(osType) {
			case SerialComManager.OS_WINDOWS:
				libToExtractFromJar = "windb_" + SerialComManager.JAVA_LIB_VERSION + "_x86_64.dll";
				libExtension = ".dll";
				break;
			default :
			}
		}else if(cpuArch == SerialComManager.ARCH_X86) {
			switch(osType) {
			case SerialComManager.OS_WINDOWS:
				libToExtractFromJar = "windb_" + SerialComManager.JAVA_LIB_VERSION + "_x86.dll";
				libExtension = ".dll";
				break;
			default :
			}
		}else if(cpuArch == SerialComManager.ARCH_ARMV7) {
		}else if(cpuArch == SerialComManager.ARCH_ARMV6) {
		}else if(cpuArch == SerialComManager.ARCH_ARMV5) {
		}else {
		}

		/* Extract shared library from jar into working directory */
		try {
			if(loadedLibName == null) {
				libFile = new File(workingDir.getAbsolutePath() + fileSeparator + libToExtractFromJar);
			}else {
				libFile = new File(workingDir.getAbsolutePath() + fileSeparator + loadedLibName.trim() + libExtension);
			}

			input = SerialComDBReleaseJNIBridge.class.getResourceAsStream("/lib-comdbfree/" + libToExtractFromJar);
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
					throw new SerialComLoadException("Can not extract native shared library " + libToExtractFromJar + " from scm-x.x.x.jar file !");
				}
			}else {
				throw new SerialComLoadException("Can not get shared library " + libToExtractFromJar + " resource as stream from scm-x.x.x.jar file using class loader !");
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

		/* Try loading the dynamic shared library from the local file system finally */
		try {
			System.load(libFile.toString());
		} catch (Exception e) {
			throw (UnsatisfiedLinkError) new UnsatisfiedLinkError("Could not load " + libFile.toString() + " native library !").initCause(e);
		}

		return true;
	}

	// loads driver on demand and initializes what is required.
	public native int startSerialComDBReleaseSerive();

	public native int stopSerialComDBReleaseSerive();
	public native int releaseComPort(String comPortName);
	public native int releaseAllComPorts(String[] excludeList);
	public native String[] getComPortNumbersInUse();
	public native int getCurrentComDBDatabaseSize();
	public native int resizeComDBDatabase(int newSize);
}
