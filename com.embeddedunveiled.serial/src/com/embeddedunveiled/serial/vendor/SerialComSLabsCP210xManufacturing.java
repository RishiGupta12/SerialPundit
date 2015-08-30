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
import com.embeddedunveiled.serial.internal.SerialComCP210xManufacturingJNIBridge;
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
 * <p>[2] The application note for CP210XRuntime library is here : 
 * https://www.silabs.com/Support%20Documents/TechnicalDocs/an144.pdf</p>
 * 
 * <p>SCM version 1.0.4 is linked to v3.4 version of CP210xManufacturing library.</p>
 */
public final class SerialComSLabsCP210xManufacturing extends SerialComVendorLib {

	/**<p>Constant representing one of the flag to be used with getProductString() method. </p>*/
	public static final int CP210x_RETURN_SERIAL_NUMBER = 0x00;

	/**<p>Constant representing one of the flag to be used with getProductString() method. </p>*/
	public static final int CP210x_RETURN_DESCRIPTION = 0x01;

	/**<p>Constant representing one of the flag to be used with getProductString() method. </p>*/
	public static final int CP210x_RETURN_FULL_PATH = 0x02;

	/**<p>Constant representing one of the bit in bit mask to be used with setFlushBufferConfig() method. </p>*/
	public static final int FC_OPEN_TX = 0x01;

	/**<p>Constant representing one of the bit in bit mask to be used with setFlushBufferConfig() method. </p>*/
	public static final int FC_OPEN_RX = 0x02;

	/**<p>Constant representing one of the bit in bit mask to be used with setFlushBufferConfig() method. </p>*/
	public static final int FC_CLOSE_TX = 0x04;

	/**<p>Constant representing one of the bit in bit mask to be used with setFlushBufferConfig() method. </p>*/
	public static final int FC_CLOSE_RX = 0x08;

	private final SerialComCP210xManufacturingJNIBridge mSerialComCP210xManufacturingJNIBridge;

	/**
	 * <p>Allocates a new SerialComSLabsCP210xManufacturing object and extract and load shared libraries as 
	 * required.</p>
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
	public SerialComSLabsCP210xManufacturing(File libDirectory, String vlibName, int cpuArch, int osType, 
			SerialComSystemProperty serialComSystemProperty) throws UnsatisfiedLinkError, SerialComLoadException, 
			SerialComUnexpectedException, SecurityException, FileNotFoundException {
		mSerialComCP210xManufacturingJNIBridge = new SerialComCP210xManufacturingJNIBridge();
		SerialComCP210xManufacturingJNIBridge.loadNativeLibrary(libDirectory, vlibName, cpuArch, osType, 
				serialComSystemProperty);
	}

	/**
	 * <p>Executes CP210x_GetNumDevices function of CP210xManufacturing library.</p>
	 * <p>Returns the number of CP210x devices connected to the host.</p>
	 * 
	 * @return number of the CP210X devices connected to host presently.
	 * @throws SerialComException if an I/O error occurs.
	 */
	public int getNumDevices() throws SerialComException {
		int ret = mSerialComCP210xManufacturingJNIBridge.getNumDevices();
		if(ret < 0) {
			throw new SerialComException("Could not get the number of devices connected to host. Please retry !");
		}
		return ret;
	}

	/**
	 * <p>Executes CP210x_GetProductString function of CP210xManufacturing library.</p>
	 * 
	 * <p>Returns product description, serial number or full path based on flag passed.</p>
	 * 
	 * <p>The argument flag can be one of the constant CP210x_RETURN_SERIAL_NUMBER, 
	 * CP210x_RETURN_DESCRIPTION or CP210x_RETURN_FULL_PATH.</p>
	 * 
	 * @param index index of device in list.
	 * @param flag indicates which property is to be fetched.
	 * @return product description, serial number or full path.
	 * @throws SerialComException if an I/O error occurs.
	 * @throws IllegalArgumentException if invalid flag is passed.
	 */
	public String getProductString(int index, int flag) throws SerialComException {
		String ret = null;
		if((flag == CP210x_RETURN_FULL_PATH) || (flag == CP210x_RETURN_DESCRIPTION) 
				|| (flag == CP210x_RETURN_SERIAL_NUMBER)) {
			ret = mSerialComCP210xManufacturingJNIBridge.getProductString(index, flag);
			if(ret == null) {
				throw new SerialComException("Could not get the requested information. Please retry !");
			}
			return ret;
		}

		throw new IllegalArgumentException("Invalid flag passed for requested operation !");
	}

	/**
	 * <p>Executes CP210x_Open function of of CP210xManufacturing library.</p>
	 * 
	 * <p>Open the device and return a handle which will be used for subsequent accesses.</p>
	 * 
	 * @param index of the device that needs to be opened.
	 * @return handle of the opened device or -1 if method fails.
	 * @throws SerialComException if an I/O error occurs.
	 * @throws IllegalArgumentException if index is negative.
	 */
	public long open(final int index) throws SerialComException {
		if(index < 0) {
			throw new IllegalArgumentException("Argument index can not be negative !");
		}
		long handle = mSerialComCP210xManufacturingJNIBridge.open(index);
		if(handle < 0) {
			throw new SerialComException("Could not open the requested device at given index. Please retry !");
		}else {
			return handle;
		}
	}

	/**
	 * <p>Executes CP210x_Close function of of CP210xManufacturing library.</p>
	 * 
	 * <p>Closes an opened cp210x device.</p>
	 * 
	 * @param handle of the device that is to be close.
	 * @return true on success.
	 * @throws SerialComException if an I/O error occurs.
	 */
	public boolean close(final long handle) throws SerialComException {
		int ret = mSerialComCP210xManufacturingJNIBridge.close(handle);
		if(ret < 0) {
			throw new SerialComException("Could not close the requested device. Please retry !");
		}

		return true;
	}

	/**
	 * <p>Executes CP210x_GetPartNumber function of CP210xManufacturing library.</p>
	 * 
	 * <p>Returns the part number associated with the given handle.</p>
	 * 
	 * @return part number associated with the given handle.
	 * @throws SerialComException if an I/O error occurs.
	 */
	public String getPartNumber(long handle) throws SerialComException {
		String ret = mSerialComCP210xManufacturingJNIBridge.getPartNumber(handle);
		if(ret == null) {
			throw new SerialComException("Could not get the part number. Please retry !");
		}
		return ret;
	}

	/**
	 * <p>Executes CP210x_SetVid function of of CP210xManufacturing library.</p>
	 * 
	 * <p>Sets the 2-byte Vendor ID field of the Device Descriptor of a CP210x device.</p>
	 * 
	 * @param handle of the device.
	 * @param vid 16 bit Vendor ID.
	 * @return true on success.
	 * @throws SerialComException if an I/O error occurs.
	 */
	public boolean setVid(final long handle, int vid) throws SerialComException {
		int ret = mSerialComCP210xManufacturingJNIBridge.setVid(handle, vid);
		if(ret < 0) {
			throw new SerialComException("Could not set the USB VID. Please retry !");
		}

		return true;
	}

	/**
	 * <p>Executes CP210x_SetPid function of of CP210xManufacturing library.</p>
	 * 
	 * <p>Sets the 2-byte Product ID field of the Device Descriptor of a CP210x device.</p>
	 * 
	 * @param handle of the device.
	 * @param pid 16 bit Product ID.
	 * @return true on success.
	 * @throws SerialComException if an I/O error occurs.
	 */
	public boolean setPid(final long handle, int pid) throws SerialComException {
		int ret = mSerialComCP210xManufacturingJNIBridge.setPid(handle, pid);
		if(ret < 0) {
			throw new SerialComException("Could not set the USB PID. Please retry !");
		}

		return true;
	}

	/**
	 * <p>Executes CP210x_SetProductString function of of CP210xManufacturing library.</p>
	 * 
	 * <p>Sets the Product Description String of the String Descriptor of a CP210x device.</p>
	 * 
	 * @param handle of the device.
	 * @param description string that need to be saved in device.
	 * @return true on success.
	 * @throws SerialComException if an I/O error occurs.
	 */
	public boolean setProductString(final long handle, String description) throws SerialComException {
		int ret = mSerialComCP210xManufacturingJNIBridge.setProductString(handle, description);
		if(ret < 0) {
			throw new SerialComException("Could not set description for the product. Please retry !");
		}

		return true;
	}

	/**
	 * <p>Executes CP210x_SetSerialNumber function of of CP210xManufacturing library.</p>
	 * 
	 * <p>Sets the Serial Number String of the String Descriptor of a CP210x device.</p>
	 * 
	 * @param handle of the device.
	 * @param serialNumber string that need to be saved in device.
	 * @return true on success.
	 * @throws SerialComException if an I/O error occurs.
	 */
	public boolean setSerialNumber(final long handle, String serialNumber) throws SerialComException {
		int ret = mSerialComCP210xManufacturingJNIBridge.setSerialNumber(handle, serialNumber);
		if(ret < 0) {
			throw new SerialComException("Could not set serial number for the product. Please retry !");
		}

		return true;
	}

	/**
	 * <p>Executes CP210x_SetSelfPower function of of CP210xManufacturing library.</p>
	 * 
	 * <p>Sets or clears the Self-Powered bit of the Power Attributes field of the Configuration Descriptor of a CP210x device.</p>
	 * 
	 * @param handle of the device.
	 * @param selfPower if true will set, if false self power bit will be cleared.
	 * @return true on success.
	 * @throws SerialComException if an I/O error occurs.
	 */
	public boolean setSelfPower(final long handle, boolean selfPower) throws SerialComException {
		int ret = mSerialComCP210xManufacturingJNIBridge.setSelfPower(handle, selfPower);
		if(ret < 0) {
			throw new SerialComException("Could not set/clear Self-Powered bit for the product. Please retry !");
		}

		return true;
	}

	/**
	 * <p>Executes CP210x_SetMaxPower function of of CP210xManufacturing library.</p>
	 * 
	 * <p>Sets the Max Power field of the Configuration Descriptor of a CP210x device.</p>
	 * 
	 * @param handle of the device.
	 * @param maxPower 1-byte value representing the maximum power consumption of the CP210x USB device, expressed in 2 mA units.
	 * @return true on success.
	 * @throws SerialComException if an I/O error occurs.
	 */
	public boolean setMaxPower(final long handle, byte maxPower) throws SerialComException {
		int ret = mSerialComCP210xManufacturingJNIBridge.setMaxPower(handle, maxPower);
		if(ret < 0) {
			throw new SerialComException("Could not set the max power field for the product. Please retry !");
		}

		return true;
	}

	/**
	 * <p>Executes CP210x_SetFlushBufferConfig function of of CP210xManufacturing library.</p>
	 * 
	 * <p>Sets the Flush Buffer configuration of a CP210x device.</p>
	 * 
	 * <p>The argument flag can be bit mask of constants FC_OPEN_TX, FC_OPEN_RX, FC_CLOSE_TX, FC_CLOSE_RX.</p>
	 * 
	 * @param handle of the device.
	 * @param flag bit mask indicating which buffer to flush and upon which event.
	 * @return true on success.
	 * @throws SerialComException if an I/O error occurs.
	 * @throws IllegalArgumentException if invalid flag is passed.
	 */
	public boolean setFlushBufferConfig(final long handle, int flag) throws SerialComException {
		if(flag > 0x0F) {
			throw new IllegalArgumentException("Invalid flag passed for the requested operation !");
		}
		int ret = mSerialComCP210xManufacturingJNIBridge.setFlushBufferConfig(handle, flag);
		if(ret < 0) {
			throw new SerialComException("Could not set the flushing configuration for the product. Please retry !");
		}

		return true;
	}

	/**
	 * <p>Executes CP210x_SetDeviceVersion function of of CP210xManufacturing library.</p>
	 * 
	 * <p>Sets the Device Release Version field of the Device Descriptor of a CP210x device.</p>
	 * 
	 * @param handle of the device.
	 * @param version 2-byte Device Release Version number in Binary-Coded Decimal (BCD) format 
	 *         with the upper two nibbles containing the two decimal digits of the major version 
	 *         and the lower two nibbles containing the two decimal digits of the minor version.
	 * @return true on success.
	 * @throws SerialComException if an I/O error occurs.
	 * @throws IllegalArgumentException if invalid flag is passed.
	 */
	public boolean setDeviceVersion(final long handle, int version) throws SerialComException {
		if(version > 0xFFFF) {
			throw new IllegalArgumentException("Invalid flag passed for the requested operation !");
		}
		int ret = mSerialComCP210xManufacturingJNIBridge.setDeviceVersion(handle, version);
		if(ret < 0) {
			throw new SerialComException("Could not set the device version for the product. Please retry !");
		}

		return true;
	}

	/**
	 * <p>Executes CP210x_SetBaudRateConfig function of of CP210xManufacturing library.</p>
	 * 
	 * <p>Sets the baud rate configuration data of a CP210x device.</p>
	 * 
	 * @param handle of the device.
	 * @param baudGen BaudGen field of BAUD_CONFIG structure defined in CP210XManufacturingDLL.h header file.
	 * @param timer0Reload Timer0Reload field of BAUD_CONFIG structure defined in CP210XManufacturingDLL.h header file.
	 * @param prescalar Pre-scaler field of BAUD_CONFIG structure defined in CP210XManufacturingDLL.h header file.
	 * @param baudrate BaudRate field of BAUD_CONFIG structure defined in CP210XManufacturingDLL.h header file.
	 * @return true on success.
	 * @throws SerialComException if an I/O error occurs.
	 */
	public boolean setBaudRateConfig(long handle, int baudGen, int timer0Reload, int prescalar, int baudrate) throws SerialComException {
		int ret = mSerialComCP210xManufacturingJNIBridge.setBaudRateConfig(handle, baudGen, timer0Reload, prescalar, baudrate);
		if(ret < 0) {
			throw new SerialComException("Could not set the baud rate configuration values for the product. Please retry !");
		}

		return true;
	}

	/**
	 * <p>Executes CP210x_SetPortConfig function of of CP210xManufacturing library.</p>
	 * 
	 * <p>Sets the current port pin configuration from the CP210x device.</p>
	 * 
	 * @param handle of the device.
	 * @param mode Mode field of PORT_CONFIG structure defined in CP210XManufacturingDLL.h header file.
	 * @param resetLatch Reset_Latch field of PORT_CONFIG structure defined in CP210XManufacturingDLL.h header file.
	 * @param suspendLatch Suspend_Latch field of PORT_CONFIG structure defined in CP210XManufacturingDLL.h header file.
	 * @param enhancedFxn EnhancedFxn field of PORT_CONFIG structure defined in CP210XManufacturingDLL.h header file.
	 * @return true on success.
	 * @throws SerialComException if an I/O error occurs.
	 */
	public boolean setPortConfig(long handle, int mode, int resetLatch, int suspendLatch, int enhancedFxn) throws SerialComException {
		int ret = mSerialComCP210xManufacturingJNIBridge.setPortConfig(handle, mode, resetLatch, suspendLatch, enhancedFxn);
		if(ret < 0) {
			throw new SerialComException("Could not set the port configuration values for the product. Please retry !");
		}

		return true;
	}

	/**
	 * <p>Executes CP210x_SetLockValue function of of CP210xManufacturing library.</p>
	 * 
	 * <p>Sets the 1-byte Lock Value of a CP210x device.</p>
	 * 
	 * @param handle of the device.
	 * @return true on success.
	 * @throws SerialComException if an I/O error occurs.
	 */
	public boolean setLockValue(final long handle) throws SerialComException {
		int ret = mSerialComCP210xManufacturingJNIBridge.setLockValue(handle);
		if(ret < 0) {
			throw new SerialComException("Could not set the lock value on the device. Please retry !");
		}

		return true;
	}

	/**
	 * <p>Executes CP210x_GetDeviceVid function of CP210xManufacturing library.</p>
	 * 
	 * <p>Returns the 2-byte Vendor ID field of the Device Descriptor of a CP210x device.</p>
	 * 
	 * @param handle of the device.
	 * @return USB vendor ID of this device.
	 * @throws SerialComException if an I/O error occurs.
	 */
	public int getDeviceVid(long handle) throws SerialComException {
		int ret = mSerialComCP210xManufacturingJNIBridge.getDeviceVid(handle);
		if(ret < 0) {
			throw new SerialComException("Could not get the device USB VID. Please retry !");
		}
		return ret;
	}

	/**
	 * <p>Executes CP210x_GetDevicePid function of CP210xManufacturing library.</p>
	 * 
	 * <p>Returns the 2-byte Product ID field of the Device Descriptor of a CP210x device.</p>
	 * 
	 * @param handle of the device.
	 * @return USB product ID of this device.
	 * @throws SerialComException if an I/O error occurs.
	 */
	public int getDevicePid(long handle) throws SerialComException {
		int ret = mSerialComCP210xManufacturingJNIBridge.getDevicePid(handle);
		if(ret < 0) {
			throw new SerialComException("Could not get the device USB VID. Please retry !");
		}
		return ret;
	}

	/**
	 * <p>Executes CP210x_GetDeviceProductString function of CP210xManufacturing library.</p>
	 * 
	 * <p>Returns the Product Description String of the String Descriptor of a CP210x device.</p>
	 * 
	 * @param handle of the device.
	 * @return product description of the device.
	 * @throws SerialComException if an I/O error occurs.
	 */
	public String getDeviceProductString(long handle) throws SerialComException {
		String ret = mSerialComCP210xManufacturingJNIBridge.getDeviceProductString(handle);
		if(ret == null) {
			throw new SerialComException("Could not get the product description string. Please retry !");
		}
		return ret;
	}

	/**
	 * <p>Executes CP210x_GetDeviceSerialNumber function of CP210xManufacturing library.</p>
	 * 
	 * <p>Gets the Serial Number String of the String Descriptor of a CP210x device.</p>
	 * 
	 * @param handle of the device.
	 * @return serial number of the device.
	 * @throws SerialComException if an I/O error occurs.
	 */
	public String getDeviceSerialNumber(long handle) throws SerialComException {
		String ret = mSerialComCP210xManufacturingJNIBridge.getDeviceSerialNumber(handle);
		if(ret == null) {
			throw new SerialComException("Could not get the product serial number string. Please retry !");
		}
		return ret;
	}

	/**
	 * <p>Executes CP210x_GetSelfPower function of CP210xManufacturing library.</p>
	 * 
	 * <p>Returns the state of the Self-Powered bit of the Power Attributes field of the Configuration Descriptor of a CP210x device.</p>
	 * 
	 * @param handle of the device.
	 * @return true if self powered bit is high or false is self power bit is low.
	 * @throws SerialComException if an I/O error occurs.
	 */
	public boolean getSelfPower(long handle) throws SerialComException {
		int ret = mSerialComCP210xManufacturingJNIBridge.getSelfPower(handle);
		if(ret < 0) {
			throw new SerialComException("Could not determine the self powered bit value. Please retry !");
		}
		if(ret == 0) {
			return false;
		}
		return true;
	}

	/**
	 * <p>Executes CP210x_GetMaxPower function of CP210xManufacturing library.</p>
	 * 
	 * <p>Returns the 1-byte Max Power field of the Configuration Descriptor of a CP210x device.</p>
	 * 
	 * @param handle of the device.
	 * @return max power field value.
	 * @throws SerialComException if an I/O error occurs.
	 */
	public byte getMaxPower(long handle) throws SerialComException {
		int ret = mSerialComCP210xManufacturingJNIBridge.getMaxPower(handle);
		if(ret < 0) {
			throw new SerialComException("Could not determine the max power field value. Please retry !");
		}
		return (byte)ret;
	}

	/**
	 * <p>Executes CP210x_GetFlushBufferConfig function of CP210xManufacturing library.</p>
	 * 
	 * <p>Returns the flush buffer configuration of a CP210x device.</p>
	 * 
	 * @param handle of the device.
	 * @return bit mask of constants FC_OPEN_TX, FC_OPEN_RX, FC_CLOSE_TX, FC_CLOSE_RX.
	 * @throws SerialComException if an I/O error occurs.
	 */
	public short getFlushBufferConfig(long handle) throws SerialComException {
		int ret = mSerialComCP210xManufacturingJNIBridge.getFlushBufferConfig(handle);
		if(ret < 0) {
			throw new SerialComException("Could not determine flush buffer config. Please retry !");
		}
		return (byte)ret;
	}

	/**
	 * <p>Executes CP210x_GetDeviceVersion function of CP210xManufacturing library.</p>
	 * 
	 * <p>Returns the device version of a CP210x device.</p>
	 * 
	 * @param handle of the device.
	 * @return device version.
	 * @throws SerialComException if an I/O error occurs.
	 */
	public int getDeviceVersion(long handle) throws SerialComException {
		int ret = mSerialComCP210xManufacturingJNIBridge.getDeviceVersion(handle);
		if(ret < 0) {
			throw new SerialComException("Could not get the device version. Please retry !");
		}
		return ret;
	}

	/**
	 * <p>Executes CP210x_GetBaudRateConfig function of CP210xManufacturing library.</p>
	 * 
	 * <p>Gets the baud rate configuration data of a CP210x device.</p>
	 * 
	 * @param handle of the device.
	 * @return array of integers containing values (starting from index 0) baudGen, timer0Reload, prescalar and baudrate respectively.
	 * @throws SerialComException if an I/O error occurs.
	 */
	public int[] getBaudRateConfig(long handle) throws SerialComException {
		int[] ret = mSerialComCP210xManufacturingJNIBridge.getBaudRateConfig(handle);
		if(ret == null) {
			throw new SerialComException("Could not get the baud rate configuration values. Please retry !");
		}
		return ret;
	}

	/**
	 * <p>Executes CP210x_GetPortConfig function of CP210xManufacturing library.</p>
	 * 
	 * <p>Gets the current port pin configuration from the CP210x device.</p>
	 * 
	 * @param handle of the device.
	 * @return array of integers containing values (starting from index 0) mode, resetLatch, suspendLatch, 
	 *          enhancedFxn respectively.
	 * @throws SerialComException if an I/O error occurs.
	 */
	public int[] getPortConfig(long handle) throws SerialComException {
		int[] ret = mSerialComCP210xManufacturingJNIBridge.getPortConfig(handle);
		if(ret == null) {
			throw new SerialComException("Could not get the port configuration values. Please retry !");
		}
		return ret;
	}

	/**
	 * <p>Executes CP210x_GetLockValue function of CP210xManufacturing library.</p>
	 * 
	 * <p>Returns the 1-byte Lock Value of a CP210x device.</p>
	 * 
	 * @param handle of the device.
	 * @return lock value of device.
	 * @throws SerialComException if an I/O error occurs.
	 */
	public byte getLockValue(long handle) throws SerialComException {
		int ret = mSerialComCP210xManufacturingJNIBridge.getLockValue(handle);
		if(ret < 0) {
			throw new SerialComException("Could not determine the lock value. Please retry !");
		}
		return (byte)ret;
	}

	/**
	 * <p>Executes CP210x_Reset function of CP210xManufacturing library.</p>
	 * 
	 * <p>Initiates a reset of the USB interface.</p>
	 * 
	 * @param handle of the device.
	 * @return true on success.
	 * @throws SerialComException if an I/O error occurs.
	 */
	public boolean reset(long handle) throws SerialComException {
		int ret = mSerialComCP210xManufacturingJNIBridge.reset(handle);
		if(ret < 0) {
			throw new SerialComException("Could not reset the device. Please retry !");
		}
		return true;
	}

	/**
	 * <p>Executes CP210x_CreateHexFile function of CP210xManufacturing library.</p>
	 * 
	 * @param handle of the device.
	 * @param fileName name of file.
	 * @return true on success.
	 * @throws SerialComException if an I/O error occurs.
	 */
	public boolean createHexFile(long handle, String fileName) throws SerialComException {
		int ret = mSerialComCP210xManufacturingJNIBridge.createHexFile(handle, fileName);
		if(ret < 0) {
			throw new SerialComException("Could not perform the requested operation. Please retry !");
		}
		return true;
	}
}
