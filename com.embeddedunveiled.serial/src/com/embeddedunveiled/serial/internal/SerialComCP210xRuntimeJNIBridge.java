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

package com.embeddedunveiled.serial.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.FileNotFoundException;
import com.embeddedunveiled.serial.SerialComLoadException;
import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.SerialComUnexpectedException;

/**
 * <p>This class is an interface between java and native shared library.</p>
 */
public final class SerialComCP210xRuntimeJNIBridge {

	/**
	 * <p>Allocates a new SerialComCP210xRuntimeJNIBridge object.</p>
	 */
	public SerialComCP210xRuntimeJNIBridge() {
	}

	/**
	 * <p>Extract native library from jar in a working directory and load it. Also load vendor's library.</p>
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
			SerialComSystemProperty serialComSystemProperty) throws UnsatisfiedLinkError, 
			SerialComLoadException, SerialComUnexpectedException, SecurityException, FileNotFoundException {
		String libToExtractFromJar = null;
		File libFile = null;
		File vlibFile = null;
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
				libToExtractFromJar = "win_cp210xr_" + SerialComManager.JAVA_LIB_VERSION + "_x86_64.dll";
				break;
			case SerialComManager.OS_LINUX:
				libToExtractFromJar = "linux_cp210xr_" + SerialComManager.JAVA_LIB_VERSION + "_x86_64.so";
				break;
			case SerialComManager.OS_MAC_OS_X:
				libToExtractFromJar = "mac_cp210xr_" + SerialComManager.JAVA_LIB_VERSION + "_x86_64.dylib";
			default:
			}
		}else if(cpuArch == SerialComManager.ARCH_X86) {
			switch(osType) {
			case SerialComManager.OS_WINDOWS:
				libToExtractFromJar = "win_cp210xr_" + SerialComManager.JAVA_LIB_VERSION + "_x86.dll";
				break;
			case SerialComManager.OS_LINUX:
				libToExtractFromJar = "linux_cp210xr_" + SerialComManager.JAVA_LIB_VERSION + "_x86.so";
				break;
			case SerialComManager.OS_MAC_OS_X:
				libToExtractFromJar = "mac_cp210xr_" + SerialComManager.JAVA_LIB_VERSION + "_x86.dylib";
			default:
			}
		}else {
		}

		/* Extract shared library from jar into working directory */
		try {
			libFile = new File(libDirectory.getAbsolutePath() + fileSeparator + libToExtractFromJar);
			input = SerialComPortJNIBridge.class.getResourceAsStream("/vendor-libs/" + libToExtractFromJar);
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

		// load libraries in reverse order of dependencies
		try {
			// vendor supplied shared library
			vlibFile = new File(libDirectory.getAbsolutePath() + fileSeparator + vlibName);
			System.load(vlibFile.toString());
		} catch (Exception e) {
			throw (UnsatisfiedLinkError) new UnsatisfiedLinkError("Could not load " + vlibFile.toString() + " native library !").initCause(e);
		}
		try {
			// scm JNI glue shared library
			System.load(libFile.toString());
		} catch (Exception e) {
			throw (UnsatisfiedLinkError) new UnsatisfiedLinkError("Could not load " + libFile.toString() + " native library !").initCause(e);
		}

		return true;
	}

	public native int readLatch(long handle);
	public native boolean writeLatch(long handle, long mask, long latchValue);
	public native int getPartNumber(long handle);
	public native String getDeviceProductString(long handle);
	public native String getDeviceSerialNumber(long handle);
	public native String getDeviceInterfaceString(long handle);
}
