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

package com.embeddedunveiled.serial;

import java.io.File;
import java.nio.ByteBuffer;
import java.io.InputStream;
import java.io.FileOutputStream;

/* Load OS specific C-library. Extract native library in our unique "_tuartx1" directory inside 
 * OS/User specific tmp directory and load from here.
 * http://docs.oracle.com/javase/7/docs/api/ */

/**
 * <p>This class load native library and is an interface between java and native shared library.</p>
 * <p>1. Extract from jar.</p>
 * <p>2. Locate in user specific temp folder.</p>
 * <p>3. Try to load native shared library.</p>
 * <p>4. Try to link functions. </p>
 */
public final class SerialComJNINativeInterface {
	
	/**
	 * <p>Allocates a new SerialComJNINativeInterface object.</p>
	 * @param loadedLibName 
	 * @param directoryPath 
	 */
	public SerialComJNINativeInterface() {
	}
	
	public static boolean loadNativeLibrary(String directoryPath, String loadedLibName, SerialComSystemProperty serialComSystemProperty, int osType, int cpuArch) throws
						SerialComUnexpectedException, SecurityException, SerialComLoadException, UnsatisfiedLinkError {
		String javaTmpDir = null;
		String userHomeDir = null;
		String fileSeparator = null;
		File baseDir = null;
		File workingDir = null;
		boolean isTmpDir = false;
		boolean isUserHomeDir = false;
		String libNameOnly = null;
		File libFile = null;
		InputStream input = null;
		FileOutputStream output = null;
		
		/* prepare directory in which native shared library will be extracted from jar */
		if(directoryPath == null) {
			// user did not supplied any directory path so try tmp and user home
			javaTmpDir = serialComSystemProperty.getJavaIOTmpDir();
			if(javaTmpDir == null) {
				throw new SerialComUnexpectedException("loadNativeLibrary()", SerialComErrorMapper.ERR_PROP_JAVA_IO_TMPDIR);
			}
			
			baseDir = new File(javaTmpDir);
			if(baseDir.exists() && baseDir.isDirectory() && baseDir.canWrite()) {
				isTmpDir = true;
				// temp directory will be used
			}else {
				// access to temp directory failed, let us try access to user's home directory
				userHomeDir = serialComSystemProperty.getUserHome();
				if(userHomeDir == null) {
					throw new SerialComUnexpectedException("loadNativeLibrary()", SerialComErrorMapper.ERR_PROP_USER_HOME);
				}
				baseDir = new File(userHomeDir);
				if(!baseDir.exists()) {
					throw new SerialComLoadException("loadNativeLibrary()", SerialComErrorMapper.ERR_USER_DIR_NOT_EXIST_TMP);
				}
				if(!baseDir.isDirectory()) {
					throw new SerialComLoadException("loadNativeLibrary()", SerialComErrorMapper.ERR_USER_IS_NOT_DIR_TMP);
				}
				if(!baseDir.canWrite()) {
					throw new SerialComLoadException("loadNativeLibrary()", SerialComErrorMapper.ERR_USER_DIR_NOT_WRITABLE_TMP);
				}
				isUserHomeDir = true;
			}
		}else {
			// user specified directory, so try it
			baseDir = new File(directoryPath);
			if(!baseDir.exists()) {
				throw new SerialComLoadException("loadNativeLibrary()", SerialComErrorMapper.ERR_GIVEN_DIR_NOT_EXIST);
			}
			if(!baseDir.isDirectory()) {
				throw new SerialComLoadException("loadNativeLibrary()", SerialComErrorMapper.ERR_GIVEN_IS_NOT_DIR);
			}
			if(!baseDir.canWrite()) {
				throw new SerialComLoadException("loadNativeLibrary()", SerialComErrorMapper.ERR_GIVEN_DIR_NOT_WRITABLE);
			}
		}
		
		fileSeparator = serialComSystemProperty.getfileSeparator();
		if(fileSeparator == null) {
			throw new SerialComUnexpectedException("loadNativeLibrary()", SerialComErrorMapper.ERR_PROP_FILE_SEPARATOR);
		}
		
		if((isTmpDir == true) || (isUserHomeDir == true)) {
			// for tmp or user home create unique directory inside them for our use only
			workingDir = new File(baseDir.toString() + fileSeparator + "scm_tuartx1");
			if(!workingDir.exists()) {
				if(!workingDir.mkdir()) {
					if(isTmpDir == true) {
						throw new SerialComLoadException("loadNativeLibrary()", SerialComErrorMapper.ERR_CREATE_UNIQUE_DIR_TMP);
					}else if(isUserHomeDir == true) {
						throw new SerialComLoadException("loadNativeLibrary()", SerialComErrorMapper.ERR_CREATE_UNIQUE_DIR_USER);
					}else {
					}
				}
			}
		}

		/* prepare the name of the native library that will be loaded based on arch and os type */
		if(cpuArch == SerialComManager.ARCH_AMD64) {
			switch(osType) {
				case SerialComManager.OS_WINDOWS:
					libNameOnly = "windows_" + SerialComManager.JAVA_LIB_VERSION + "_x86_64.dll";
					break;
				case SerialComManager.OS_LINUX:
					libNameOnly = "linux_" + SerialComManager.JAVA_LIB_VERSION + "_x86_64.so";
					break;
				case SerialComManager.OS_MAC_OS_X:
					libNameOnly = "mac_" + SerialComManager.JAVA_LIB_VERSION + "_x86_64.dylib";
					break;
				default :
			}
		}else if(cpuArch == SerialComManager.ARCH_X86) {
			switch(osType) {
				case SerialComManager.OS_WINDOWS:
					libNameOnly = "windows_" + SerialComManager.JAVA_LIB_VERSION + "_x86.dll";
					break;
				case SerialComManager.OS_LINUX:
					libNameOnly = "linux_" + SerialComManager.JAVA_LIB_VERSION + "_x86.so";
					break;
				case SerialComManager.OS_MAC_OS_X:
					libNameOnly = "mac_" + SerialComManager.JAVA_LIB_VERSION + "_x86.dylib";
					break;
				default :
			}
		}else if(cpuArch == SerialComManager.ARCH_IA64) {
			
		}else {
			//TODO for more platforms
		}
		
		/* extract shared library from jar into working directory */
		try {
			libFile = new File(workingDir.getAbsolutePath() + fileSeparator + libNameOnly);
			input = SerialComJNINativeInterface.class.getResourceAsStream("/libs/" + libNameOnly);
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
				}else {
					throw new SerialComLoadException("loadNativeLibrary()", SerialComErrorMapper.ERR_CANNOT_EXTRACT_LIB);
				}
			}else {
				throw new SerialComLoadException("loadNativeLibrary()", SerialComErrorMapper.ERR_CANNOT_RES_AS_STREAM);
			}
		} catch (Exception e) {
			 throw (SerialComLoadException) new SerialComLoadException("loadNativeLibrary()", libFile.toString()).initCause(e);
		} finally {
			try {
				if(output != null) {
					output.close();
				}
				if(input != null) {
					input.close();
				}
			} catch (Exception e) {
				// ignore
			}
		}
		
		/* Try loading the dynamic shared library from the local file system now. */
		try {
			System.load(libFile.toString());
		} catch (Exception e) {
			throw (UnsatisfiedLinkError) new UnsatisfiedLinkError(SerialComErrorMapper.ERR_CAN_NOT_LOAD_NATIVE_LIB).initCause(e);
		}
		
		return true;
	}

	public native int initNativeLib();
	public native String getNativeLibraryVersion(SerialComRetStatus retStatus);
	public native boolean debug(boolean enableDebug);
	public native String[] listAvailableComPorts(SerialComRetStatus retStatus);

	public native int registerPortMonitorListener(long handle, String portName, ISerialComPortMonitor portMonitor);
	public native int unregisterPortMonitorListener(long handle);

	public native long openComPort(String portName, boolean enableRead, boolean enableWrite, boolean exclusiveOwner);
	public native int closeComPort(long handle);
	public native byte[] readBytes(long handle, int byteCount, SerialComReadStatus retStatus);
	public native byte[] readBytesBlocking(long handle, int byteCount, SerialComReadStatus retStatus);
	public native int writeBytes(long handle, byte[] buffer, int delay);
	public native int writeBytesBulk(long handle, ByteBuffer buffer);

	public native int configureComPortData(long handle, int dataBits, int stopBits, int parity, int baudRateTranslated, int custBaudTranslated);
	public native int configureComPortControl(long handle, int flowctrl, char xon, char xoff, boolean ParFraError, boolean overFlowErr);
	public native int[] getCurrentConfigurationU(long handle);
	public native String[] getCurrentConfigurationW(long handle);

	public native int setUpDataLooperThread(long handle, SerialComLooper looper);
	public native int setUpEventLooperThread(long handle, SerialComLooper looper);
	public native int destroyDataLooperThread(long handle);
	public native int destroyEventLooperThread(long handle);

	public native int pauseListeningEvents(long handle);
	public native int resumeListeningEvents(long handle);

	public native int setRTS(long handle, boolean enabled);
	public native int setDTR(long handle, boolean enabled);
	public native int[] getLinesStatus(long handle);
	public native int[] getInterruptCount(long handle);

	public native int sendBreak(long handle, int duration);
	public native int[] getByteCount(long handle);
	public native int clearPortIOBuffers(long handle, boolean rxPortbuf, boolean txPortbuf);
	public native int fineTuneRead(long handle, int vmin, int vtime, int rit, int rttm, int rttc);

	public native long ioctlExecuteOperation(long handle, long operationCode);
	public native long ioctlSetValue(long handle, long operationCode, long value);
	public native long ioctlGetValue(long handle, long operationCode);
	public native long ioctlSetValueIntArray(long handle, long operationCode, int[] values);
	public native long ioctlSetValueCharArray(long handle, long operationCode, byte[] values);
}
