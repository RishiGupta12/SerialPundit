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

package com.embeddedunveiled.serial;

import com.embeddedunveiled.serial.internal.SerialComHIDJNIBridge;

/**
 * <p></p>
 * 
 */
public class SerialComHID {

	private SerialComHIDJNIBridge mHIDJNIBridge;

	/**
	 * <p>Allocates a new SerialComHID object.</p>
	 * 
	 * @param mHIDJNIBridge
	 */
	public SerialComHID(SerialComHIDJNIBridge mHIDJNIBridge) {
		this.mHIDJNIBridge = mHIDJNIBridge;
	}

	/**
	 * <p>Returns an array of SerialComHIDdevice objects containing information about HID devices 
	 * as found by this library. The HID devices found may be USB HID or Bluetooth HID device. 
	 * Application can call various  methods on returned SerialComHIDdevice object to get specific 
	 * information like vendor id and product id etc.</p>
	 * 
	 * <p>TODO</p>
	 * 
	 * @return list of the HID devices with information about them or empty array if no device matching given criteria found.
	 * @throws SerialComException if an I/O error occurs.
	 * @throws IllegalArgumentException if vendorFilter is negative or invalid number.
	 */
	public SerialComHIDdevice[] listHIDdevicesWithInfo() throws SerialComException {
		int i = 0;
		int numOfDevices = 0;
		SerialComHIDdevice[] hidDevicesFound = null;

		String[] hidDevicesInfo = mHIDJNIBridge.listHIDdevicesWithInfo();

		if(hidDevicesInfo != null) {
			numOfDevices = hidDevicesInfo.length / 7;
			hidDevicesFound = new SerialComHIDdevice[numOfDevices];
			for(int x=0; x < numOfDevices; x++) {
				hidDevicesFound[x] = new SerialComHIDdevice(hidDevicesInfo[i], hidDevicesInfo[i+1], hidDevicesInfo[i+2], 
						hidDevicesInfo[i+3], hidDevicesInfo[i+4], hidDevicesInfo[i+5], hidDevicesInfo[i+6]);
				i = i + 7;
			}
			return hidDevicesFound;
		}else {
			return new SerialComHIDdevice[] { };
		}	
	}

	/** 
	 */
	public long openHidDevice(final String pathName) throws SerialComException {
		if(pathName == null) {
			throw new IllegalArgumentException("Argument pathName can not be null !");
		}
		String pathNameVal = pathName.trim();
		if(pathNameVal.length() == 0) {
			throw new IllegalArgumentException("Argument pathName can not be empty string !");
		}

		long handle = mHIDJNIBridge.openHidDevice(pathNameVal);
		if(handle < 0) {
			/* JNI should have already thrown exception, this is an extra check to increase reliability of program. */
			throw new SerialComException("Could not open the HID device " + pathNameVal + ". Please retry !");
		}
		return handle;
	}

	/**
	 */
	public boolean closeHidDevice(long handle) throws SerialComException {
		int ret = mHIDJNIBridge.closeHidDevice(handle);
		if(ret < 0) {
			throw new SerialComException("Could not close the given HID device. Please retry !");
		}
		return true;
	}

	/** 
	 */
	public int getReportDescriptorSize(final long handle) throws SerialComException {
		int reportDescriptorSize = mHIDJNIBridge.getReportDescriptorSize(handle);
		if(reportDescriptorSize < 0) {
			// extra check
			throw new SerialComException("Could not determine the report descriptor size. Please retry !");
		}
		return reportDescriptorSize;
	}

	/**
	 */
	public boolean writeOutputReport(long handle, byte reportId, final byte[] data) throws SerialComException {
		if(data == null) {
			throw new IllegalArgumentException("Argumenet data can not be null !");
		}
		if(data.length == 0) {
			throw new IllegalArgumentException("Argumenet data can not be of zero length !");
		}

		int ret = mHIDJNIBridge.writeOutputReport(handle, reportId, data);
		if(ret < 0) {
			throw new SerialComException("Could not write output report to HID device. Please retry !");
		}
		return true;
	}

	/** 
	 */
	public int readInputReport(long handle, byte[] dataBuffer) throws SerialComException {
		if(dataBuffer == null) {
			throw new IllegalArgumentException("Argumenet dataBuffer can not be null !");
		}
		int ret = mHIDJNIBridge.readInputReport(handle, dataBuffer);
		if(ret < 0) {
			throw new SerialComException("Could not read input report from HID device. Please retry !");
		}
		return ret;
	}

	/** 
	 */
	public int readInputReportWithTimeout(long handle, byte[] dataBuffer, int timeoutValue) throws SerialComException {
		if(dataBuffer == null) {
			throw new IllegalArgumentException("Argumenet dataBuffer can not be null !");
		}
		int ret = mHIDJNIBridge.readInputReportWithTimeout(handle, dataBuffer, timeoutValue);
		if(ret < 0) {
			throw new SerialComException("Could not read input report from HID device. Please retry !");
		}
		return ret;
	}

	/**
	 */
	public boolean sendFeatureReport(long handle, byte reportId, final byte[] data) throws SerialComException {
		if(data == null) {
			throw new IllegalArgumentException("Argumenet data can not be null !");
		}
		if(data.length == 0) {
			throw new IllegalArgumentException("Argumenet data can not be of zero length !");
		}

		int ret = mHIDJNIBridge.sendFeatureReport(handle, reportId, data);
		if(ret < 0) {
			throw new SerialComException("Could not send feature report to HID device. Please retry !");
		}
		return true;
	}

	/** 
	 */
	public int getFeatureReport(long handle, byte[] dataBuffer) throws SerialComException {
		if(dataBuffer == null) {
			throw new IllegalArgumentException("Argumenet dataBuffer can not be null !");
		}
		int ret = mHIDJNIBridge.getFeatureReport(handle, dataBuffer);
		if(ret < 0) {
			throw new SerialComException("Could not get feature report from HID device. Please retry !");
		}
		return ret;
	}

	/**
	 */
	public String getManufacturerString(long handle) throws SerialComException {
		String ret = mHIDJNIBridge.getManufacturerString(handle);
		if(ret == null) {
			throw new SerialComException("Could not get the manufacturer string from the HID device. Please retry !");
		}
		return ret;
	}

	/**
	 */
	public String getIndexedString(int index) throws SerialComException {
		String ret = mHIDJNIBridge.getIndexedString(index);
		if(ret == null) {
			throw new SerialComException("Could not get the string at given index from the HID device. Please retry !");
		}
		return ret;
	}

	/**
	 */
	public String getProductString(long handle) throws SerialComException {
		String ret = mHIDJNIBridge.getProductString(handle);
		if(ret == null) {
			throw new SerialComException("Could not get the product string from the HID device. Please retry !");
		}
		return ret;
	}

	/**
	 */
	public String getSerialNumberString(long handle) throws SerialComException {
		String ret = mHIDJNIBridge.getSerialNumberString(handle);
		if(ret == null) {
			throw new SerialComException("Could not get the serial number string from the HID device. Please retry !");
		}
		return ret;
	}

}


























