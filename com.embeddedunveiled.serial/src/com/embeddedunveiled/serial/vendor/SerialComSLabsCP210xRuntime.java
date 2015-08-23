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

package com.embeddedunveiled.serial.vendor;

import java.io.File;
import java.io.FileNotFoundException;
import com.embeddedunveiled.serial.SerialComException;
import com.embeddedunveiled.serial.SerialComLoadException;
import com.embeddedunveiled.serial.SerialComUnexpectedException;
import com.embeddedunveiled.serial.internal.SerialComCP210xRuntimeJNIBridge;
import com.embeddedunveiled.serial.internal.SerialComSystemProperty;

/**
 * <p>Silicon labs Inc. provides libraries to communicate with their USB-UART devices. More information can 
 * be found here : https://www.silabs.com/products/mcu/Pages/USBtoUARTBridgeVCPDrivers.aspx</p>
 * 
 * <p>[0] The data types used in java layer may be bigger in size than the native layer. For example; if native 
 * function returns 16 bit signed integer, than java method will return 32 bit integer. This is done to make 
 * sure that no data loss occur. This library take care of sign and their applicability internally.</p>
 * 
 * <p>[1] Developers are requested to check with vendor library documentation if a particular function is supported
 * for desired platform or not and also how does a particular API will behave. Also consider paying attention to 
 * valid values and range when passing arguments to a method.</p>
 * 
 * <p>[2] The application note for CP210XRuntime library is here : https://www.silabs.com/Support%20Documents/TechnicalDocs/an223.pdf</p>
 * 
 * <p>SCM version 1.0.4 is linked to v3.4 version of CP210xRuntime library.</p>
 */
public final class SerialComSLabsCP210xRuntime extends SerialComVendorLib {

	private final SerialComCP210xRuntimeJNIBridge mSerialComCP210xRuntimeJNIBridge;

	/**
	 * <p>Allocates a new SerialComSLabsCP210xRuntime object and extract and load shared libraries as required.</p>
	 * 
	 * @param libDirectory directory in which native library will be extracted and vendor library will be found.
	 * @param vlibName name of vendor library to load and link.
	 * @param cpuArch architecture of CPU this library is running on.
	 * @param osType operating system this library is running on.
	 * @param serialComSystemProperty instance of SerialComSystemProperty to get required java properties.
	 * @throws SerialComUnexpectedException if a critical java system property is null in system.
	 * @throws SecurityException if any java system property can not be accessed.
	 * @throws FileNotFoundException if the vendor library file is not found.
	 * @throws SerialComLoadException if any file system related issue occurs.
	 * @throws UnsatisfiedLinkError if loading/linking shared library fails.
	 * @throws SerialComException if initializing native library fails.
	 */
	public SerialComSLabsCP210xRuntime(File libDirectory, String vlibName, int cpuArch, int osType, SerialComSystemProperty serialComSystemProperty) 
			throws UnsatisfiedLinkError, SerialComLoadException, SerialComUnexpectedException, SecurityException, FileNotFoundException {
		mSerialComCP210xRuntimeJNIBridge = new SerialComCP210xRuntimeJNIBridge();
		SerialComCP210xRuntimeJNIBridge.loadNativeLibrary(libDirectory, vlibName, cpuArch, osType, serialComSystemProperty);
	}

	/**
	 * <p>Executes CP210xRT_ReadLatch function of CP210XRuntime library.</p>
	 * 
	 * <p>Gets the current port latch value from the device.</p>
	 * 
	 * @param handle handle of the opened COM port.
	 * @return GPIO latch value [Logic High = 1, Logic Low = 0].
	 * @throws SerialComException if an I/O error occurs.
	 */
	public long readLatch(final long handle) throws SerialComException {
		long ret = mSerialComCP210xRuntimeJNIBridge.readLatch(handle);
		if(ret < 0) {
			throw new SerialComException("Could not read the port latch value for given device. Please retry !");
		}
		return ret;
	}

	/**
	 * <p>Executes CP210xRT_WriteLatch function of CP210XRuntime library.</p>
	 * 
	 * <p>Sets the current port latch value for the device.</p>
	 * 
	 * @param handle handle of the opened COM port.
	 * @param mask determines which pins to change [Change = 1, Leave = 0].
	 * @param latchValue value to write to GPIO latch [Logic High = 1, Logic Low = 0].
	 * @return true if value gets set successfully.
	 * @throws SerialComException if an I/O error occurs.
	 */
	public boolean writeLatch(final long handle, long mask, long latchValue) throws SerialComException {
		int ret = mSerialComCP210xRuntimeJNIBridge.writeLatch(handle, mask, latchValue);
		if(ret < 0) {
			throw new SerialComException("Could not write the given latch value on the given device. Please retry !");
		}
		return true;
	}
	
	/**
	 * <p>Executes CP210xRT_GetPartNumber function of CP210XRuntime library.</p>
	 * 
	 * <p>Gets the part number of the current device.</p>
	 * 
	 * @param handle handle of the opened COM port.
	 * @return part number of the current device.
	 * @throws SerialComException if an I/O error occurs.
	 */
	public int getPartNumber(final long handle) throws SerialComException {
		int ret = mSerialComCP210xRuntimeJNIBridge.getPartNumber(handle);
		if(ret < 0) {
			throw new SerialComException("Could not get the part number of the current device. Please retry !");
		}
		return ret;
	}
	
	/**
	 * <p>Executes CP210xRT_GetDeviceProductString function of CP210XRuntime library.</p>
	 * 
	 * <p>Gets the product string in the current device.</p>
	 * 
	 * @param handle handle of the opened COM port.
	 * @return product string.
	 * @throws SerialComException if an I/O error occurs.
	 */
	public String getDeviceProductString(final long handle) throws SerialComException {
		String ret = mSerialComCP210xRuntimeJNIBridge.getDeviceProductString(handle);
		if(ret == null) {
			throw new SerialComException("Could not get the product string of the current device. Please retry !");
		}
		return ret;
	}
	
	/**
	 * <p>Executes CP210xRT_GetDeviceSerialNumber function of CP210XRuntime library.</p>
	 * 
	 * <p>Gets the serial number in the current device.</p>
	 * 
	 * @param handle handle of the opened COM port.
	 * @return serial number string.
	 * @throws SerialComException if an I/O error occurs.
	 */
	public String getDeviceSerialNumber(final long handle) throws SerialComException {
		String ret = mSerialComCP210xRuntimeJNIBridge.getDeviceSerialNumber(handle);
		if(ret == null) {
			throw new SerialComException("Could not get the serial number of the current device. Please retry !");
		}
		return ret;
	}
	
	/**
	 * <p>Executes CP210xRT_GetDeviceInterfaceString function of CP210XRuntime library.</p>
	 * 
	 * <p>Gets the interface string of the current device.</p>
	 * 
	 * @param handle handle of the opened COM port.
	 * @return interface string.
	 * @throws SerialComException if an I/O error occurs.
	 */
	public String getDeviceInterfaceString(final long handle) throws SerialComException {
		String ret = mSerialComCP210xRuntimeJNIBridge.getDeviceInterfaceString(handle);
		if(ret == null) {
			throw new SerialComException("Could not get the interface string of the current device. Please retry !");
		}
		return ret;
	}
	
}
