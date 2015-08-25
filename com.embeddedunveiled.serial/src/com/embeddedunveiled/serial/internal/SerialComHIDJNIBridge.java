/*
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

package com.embeddedunveiled.serial.internal;

import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;

import com.embeddedunveiled.serial.SerialComLoadException;
import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.SerialComUnexpectedException;
import com.embeddedunveiled.serial.internal.SerialComSystemProperty;

/**
 * <p>This class is an interface between java and native shared library.</p>
 */
public final class SerialComHIDJNIBridge {

	/**
	 * <p>Allocates a new SerialComHIDJNIBridge.java object.</p>
	 */
	public SerialComHIDJNIBridge() {
	}

	/**
	 * <p>Extract native library from jar in a working directory, load and link it.</p> 
	 * 
	 * @param directoryPath null for default directory or user supplied directory path
	 * @param loadedLibName null for default name or user supplied name of loaded library
	 * @param serialComSystemProperty instance of SerialComSystemProperty to get required java properties
	 * @param cpuArch architecture of CPU this library is running on
	 * @param osType operating system this library is running on
	 * @param javaABIType binary application interface type to correctly link
	 * @throws SecurityException if java system properties can not be  accessed
	 * @throws SerialComUnexpectedException if java system property is null
	 * @throws SerialComLoadException if any file system related issue occurs
	 * @throws UnsatisfiedLinkError if loading/linking shared library fails
	 */
	public static boolean loadNativeLibrary(String directoryPath, String loadedLibName, SerialComSystemProperty serialComSystemProperty,
			int osType, int cpuArch, int javaABIType) throws SecurityException, SerialComUnexpectedException, 
			SerialComLoadException, UnsatisfiedLinkError {
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
				libToExtractFromJar = "winhid_" + SerialComManager.JAVA_LIB_VERSION + "_x86_64.dll";
				libExtension = ".dll";
				break;
			case SerialComManager.OS_LINUX:
				libToExtractFromJar = "linuxhid_" + SerialComManager.JAVA_LIB_VERSION + "_x86_64.so";
				libExtension = ".so";
				break;
			case SerialComManager.OS_MAC_OS_X:
				libToExtractFromJar = "machid_" + SerialComManager.JAVA_LIB_VERSION + "_x86_64.dylib";
				libExtension = ".dylib";
				break;
			default :
			}
		}else if(cpuArch == SerialComManager.ARCH_X86) {
			switch(osType) {
			case SerialComManager.OS_WINDOWS:
				libToExtractFromJar = "winhid_" + SerialComManager.JAVA_LIB_VERSION + "_x86.dll";
				libExtension = ".dll";
				break;
			case SerialComManager.OS_LINUX:
				libToExtractFromJar = "linuxhid_" + SerialComManager.JAVA_LIB_VERSION + "_x86.so";
				libExtension = ".so";
				break;
			case SerialComManager.OS_MAC_OS_X:
				libToExtractFromJar = "machid_" + SerialComManager.JAVA_LIB_VERSION + "_x86.dylib";
				libExtension = ".dylib";
				break;
			default :
			}
		}else if(cpuArch == SerialComManager.ARCH_ARMV7) {
			if(osType == SerialComManager.OS_LINUX) {
				libExtension = ".so";
				if(javaABIType == SerialComManager.ABI_ARMHF) {
					libToExtractFromJar = "linuxhid_" + SerialComManager.JAVA_LIB_VERSION + "_armv7hf.so";
				}else if(javaABIType == SerialComManager.ABI_ARMEL) {
					libToExtractFromJar = "linuxhid_" + SerialComManager.JAVA_LIB_VERSION + "_armv7el.so";
				}else {
				}
			}
		}else if(cpuArch == SerialComManager.ARCH_ARMV6) {
			if(osType == SerialComManager.OS_LINUX) {
				libExtension = ".so";
				if(javaABIType == SerialComManager.ABI_ARMHF) {
					libToExtractFromJar = "linuxhid_" + SerialComManager.JAVA_LIB_VERSION + "_armv6hf.so";
				}else if(javaABIType == SerialComManager.ABI_ARMEL) {
					libToExtractFromJar = "linuxhid_" + SerialComManager.JAVA_LIB_VERSION + "_armv6el.so";
				}else {
				}
			}
		}else if(cpuArch == SerialComManager.ARCH_ARMV5) {
			if(osType == SerialComManager.OS_LINUX) {
				libExtension = ".so";
				libToExtractFromJar = "linuxhid_" + SerialComManager.JAVA_LIB_VERSION + "_armv5.so";
			}
		}else {
		}

		/* Extract shared library from jar into working directory */
		try {
			if(loadedLibName == null) {
				libFile = new File(workingDir.getAbsolutePath() + fileSeparator + libToExtractFromJar);
			}else {
				libFile = new File(workingDir.getAbsolutePath() + fileSeparator + loadedLibName.trim() + libExtension);
			}

			input = SerialComHIDJNIBridge.class.getResourceAsStream("/libs/" + libToExtractFromJar);
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

	// Common
	public native String[] listHIDdevicesWithInfo();
	public native long openHidDevice(String pathNameVal);
	public native int closeHidDevice(long handle);
	public native int getReportDescriptorSize(long handle);
	public native int writeOutputReport(long handle, byte reportId, byte[] report);
	public native int readInputReport(long handle, byte[] reportBuffer, int length);
	public native int readInputReportWithTimeout(long handle, byte[] reportBuffer, int length, int timeoutValue);
	public native int sendFeatureReport(long handle, byte reportId, byte[] report);
	public native int getFeatureReport(long handle, byte[] reportBuffer);
	public native String getManufacturerString(long handle);
	public native String getIndexedString(int index);
	public native String getProductString(long handle);
	public native String getSerialNumberString(long handle);

	// USB HID
	public native String[] listUSBHIDdevicesWithInfo(int vendorFilter);
	public native long openHidDeviceByUSBAttributes(int usbVidToMatch, int usbPidToMatch, String serialNum);

	// Bluetooth HID

}












