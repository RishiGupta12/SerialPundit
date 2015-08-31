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
 * @author Rishi Gupta
 */
public class SerialComHID {

	/**<p>The value indicating instance of class SerialComHID. Integer constant with value 0x01.</p>*/
	public static final int HID_GENERIC = 0x01;

	/**<p>The value indicating instance of class SerialComHID. Integer constant with value 0x02.</p>*/
	public static final int HID_USB = 0x02;

	/**<p>The value indicating instance of class SerialComHID. Integer constant with value 0x03.</p>*/
	public static final int HID_BLUETOOTH = 0x03;

	// sub-classes also uses this reference to invoke native functions.
	protected SerialComHIDJNIBridge mHIDJNIBridge;

	/**
	 * <p>Allocates a new SerialComHID object.</p>
	 * 
	 * @param mHIDJNIBridge interface class to native library for calling platform specific routines.
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
	 * @return list of the HID devices with information about them or empty array if no device 
	 *          matching given criteria found.
	 * @throws SerialComException if an I/O error occurs.
	 * @throws IllegalArgumentException if vendorFilter is negative or invalid number.
	 */
	public final SerialComHIDdevice[] listHIDdevicesWithInfo() throws SerialComException {
		int i = 0;
		int numOfDevices = 0;
		SerialComHIDdevice[] hidDevicesFound = null;

		String[] hidDevicesInfo = mHIDJNIBridge.listHIDdevicesWithInfo();

		if(hidDevicesInfo != null) {
			if(hidDevicesInfo.length < 3) {
				return new SerialComHIDdevice[] { };
			}
			numOfDevices = hidDevicesInfo.length / 9;
			hidDevicesFound = new SerialComHIDdevice[numOfDevices];
			for(int x=0; x < numOfDevices; x++) {
				hidDevicesFound[x] = new SerialComHIDdevice(hidDevicesInfo[i], hidDevicesInfo[i+1], hidDevicesInfo[i+2], 
						hidDevicesInfo[i+3], hidDevicesInfo[i+4], hidDevicesInfo[i+5], hidDevicesInfo[i+6],
						hidDevicesInfo[i+7], hidDevicesInfo[i+8]);
				i = i + 9;
			}
			return hidDevicesFound;
		}else {
			throw new SerialComException("Could not find HID devices. Please retry !");
		}	
	}

	/**
	 * <p>Converts report read from human interface device to hexadecimal string. This may be 
	 * useful when report is to be passed to next level as hex data or report is to be 
	 * feed into external HID report parser tool.</p>
	 * 
	 * @param report report to be converted into hex string.
	 * @return constructed hex string if report.length > 0 otherwise empty string.
	 * @throws IllegalArgumentException if report is null.
	 */
	public final String formatReportToHex(byte[] report) throws SerialComException {
		return SerialComUtil.byteArrayToHexString(report, " ");
	}

	/**
	 * <p>Opens a HID device for communication using its path name.</p>
	 * 
	 * @param pathName device node full path for Unix-like OS and port name for Windows.
	 * @return handle of the opened HID device.
	 * @throws SerialComException if an IO error occurs.
	 * @throws IllegalArgumentException if pathName is null or empty string.
	 */
	public final long openHidDevice(final String pathName) throws SerialComException {
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
	 * <p>Closes a HID device.</p>
	 * 
	 * @param handle handle of the device to be closed.
	 * @return true if device closed successfully.
	 * @throws SerialComException if fails to close the device or an IO error occurs.
	 */
	public final boolean closeHidDevice(long handle) throws SerialComException {
		int ret = mHIDJNIBridge.closeHidDevice(handle);
		if(ret < 0) {
			throw new SerialComException("Could not close the given HID device. Please retry !");
		}
		return true;
	}

	/**
	 * <p>Gives the size of the report descriptor used by HID device represented by given handle.</p>
	 * 
	 * @param handle handle of the HID device whose report descriptor size is to be determined.
	 * @return size of report descriptor in bytes.
	 * @throws SerialComException if an I/O error occurs.
	 */
	public final int getReportDescriptorSize(final long handle) throws SerialComException {
		int reportDescriptorSize = mHIDJNIBridge.getReportDescriptorSize(handle);
		if(reportDescriptorSize < 0) {
			throw new SerialComException("Could not determine the report descriptor size. Please retry !");
		}
		return reportDescriptorSize;
	}

	/**
	 * <p>Write the given output report to HID device. For devices which support only single report, report ID 
	 * value must be 0x00. Report ID items are used to indicate which data fields are represented in each 
	 * report structure. A Report ID item tag assigns a 1-byte identification prefix to each report transfer. 
	 * If no Report ID item tags are present in the Report descriptor, it can be assumed that only one Input, 
	 * Output, and Feature report structure exists and together they represent all of the device’s data.</p>
	 * 
	 * <p>Only Input reports are sent via the Interrupt In pipe. Feature and Output reports must be initiated 
	 * by the host via the Control pipe or an optional Interrupt Out pipe.</p>
	 * 
	 * @param handle handle of the HID device to which this report will be sent.
	 * @param reportId unique identifier for the report type.
	 * @param report report to be written to HID device.
	 * @return number of bytes written to HID device.
	 * @throws SerialComException if an I/O error occurs.
	 * @throws IllegalArgumentException if report is null or empty array. 
	 */
	public final int writeOutputReport(long handle, byte reportId, final byte[] report) throws SerialComException {
		if(report == null) {
			throw new IllegalArgumentException("Argumenet report can not be null !");
		}
		if(report.length == 0) {
			throw new IllegalArgumentException("Argumenet report can not be of zero length !");
		}

		int ret = mHIDJNIBridge.writeOutputReport(handle, reportId, report);
		if(ret < 0) {
			throw new SerialComException("Could not write output report to the HID device. Please retry !");
		}
		return ret;
	}

	/**
	 * <p>Reads input report from HID device. The buffer passed must be large enough to hold the input 
	 * report excluding its report ID, if report IDs are used otherwise it should be plus one additional 
	 * byte that specifies a nonzero report ID or zero.</p>
	 * 
	 * @param handle handle of the HID device from whom input report is to be read.
	 * @param reportBuffer byte buffer in which input report will be saved.
	 * @param length number of bytes to read from HID device as report bytes.
	 * @return number of bytes read from HID device.
	 * @throws SerialComException if an I/O error occurs.
	 * @throws IllegalArgumentException if reportBuffer is null or if length is negative.
	 */
	public final int readInputReport(long handle, byte[] reportBuffer, int length) throws SerialComException {
		if(reportBuffer == null) {
			throw new IllegalArgumentException("Argumenet dataBuffer can not be null !");
		}
		if(length < 0) {
			throw new IllegalArgumentException("Argumenet length can not be negative !");
		}

		int ret = mHIDJNIBridge.readInputReport(handle, reportBuffer, length);
		if(ret < 0) {
			throw new SerialComException("Could not read input report from HID device. Please retry !");
		}
		return ret;
	}

	/**
	 * <p>Try to read input report from HID device within the given timeout limit. The buffer passed must be 
	 * large enough to hold the input report excluding its report ID, if report IDs are used otherwise it 
	 * should be plus one additional byte that specifies a nonzero report ID or zero.</p>
	 * 
	 * @param handle handle of the HID device from whom input report is to be read.
	 * @param reportBuffer byte buffer in which input report will be saved.
	 * @param length number of bytes to read from HID device as report bytes.
	 * @param timeoutValue time in milliseconds after which read must return with whatever data is read 
	 *         till that time or no data read at all.
	 * @return number of bytes read from HID device.
	 * @throws SerialComException if an I/O error occurs.
	 * @throws IllegalArgumentException if reportBuffer is null or if length is negative.
	 */
	public final int readInputReportWithTimeout(long handle, byte[] reportBuffer, int length, int timeoutValue) throws SerialComException {
		if(reportBuffer == null) {
			throw new IllegalArgumentException("Argumenet reportBuffer can not be null !");
		}
		if(length < 0) {
			throw new IllegalArgumentException("Argumenet length can not be negative !");
		}

		int ret = mHIDJNIBridge.readInputReportWithTimeout(handle, reportBuffer, length, timeoutValue);
		if(ret < 0) {
			throw new SerialComException("Could not read input report from HID device. Please retry !");
		}
		return ret;
	}

	/**
	 * <p>Send a feature report to the HID device. For devices which support only single report, report ID 
	 * value must be 0x00. Typically any data item that application wish to write in HID device and read 
	 * it back may be some time later is sent to device as feature report. This may be device application 
	 * specific configuration also.</p>
	 * 
	 * @param handle handle of the HID device to which this feature report will be sent.
	 * @param reportId unique identifier for the report type.
	 * @param report feature report to be sent to HID device.
	 * @return number of bytes sent to HID device.
	 * @throws SerialComException if an I/O error occurs.
	 * @throws IllegalArgumentException if report is null or empty array. 
	 */
	public final int sendFeatureReport(long handle, byte reportId, final byte[] report) throws SerialComException {
		if(report == null) {
			throw new IllegalArgumentException("Argumenet report can not be null !");
		}
		if(report.length == 0) {
			throw new IllegalArgumentException("Argumenet report can not be of zero length !");
		}

		int ret = mHIDJNIBridge.sendFeatureReport(handle, reportId, report);
		if(ret < 0) {
			throw new SerialComException("Could not send feature report to HID device. Please retry !");
		}
		return ret;
	}

	/**
	 * <p>Get a feature report from the HID device. The very first byte in the reportBuffer will be 
	 * report ID.</p>
	 * 
	 * @param handle handle of the HID device from whom feature report is to be read.
	 * @param length number of bytes to read into reportBuffer as part of feature report. This 
	 *         should also include report ID byte i.e. number of bytes in report plus one.
	 * @return number of bytes read from HID device.
	 * @throws SerialComException if an I/O error occurs.
	 * @throws IllegalArgumentException if reportBuffer is null or its length is zero or if length 
	 *          is negative.
	 */
	public final int getFeatureReport(long handle, byte[] reportBuffer, int length) throws SerialComException {
		if(reportBuffer == null) {
			throw new IllegalArgumentException("Argumenet reportBuffer can not be null !");
		}
		if(length < 0) {
			throw new IllegalArgumentException("Argumenet length can not be negative !");
		}

		int ret = mHIDJNIBridge.getFeatureReport(handle, reportBuffer);
		if(ret < 0) {
			throw new SerialComException("Could not get feature report from HID device. Please retry !");
		}
		return ret;
	}

	/**
	 */
	public final String getManufacturerString(long handle) throws SerialComException {
		String ret = mHIDJNIBridge.getManufacturerString(handle);
		if(ret == null) {
			throw new SerialComException("Could not get the manufacturer string from the HID device. Please retry !");
		}
		return ret;
	}

	/**
	 */
	public final String getProductString(long handle) throws SerialComException {
		String ret = mHIDJNIBridge.getProductString(handle);
		if(ret == null) {
			throw new SerialComException("Could not get the product string from the HID device. Please retry !");
		}
		return ret;
	}

	/**
	 */
	public final String getSerialNumberString(long handle) throws SerialComException {
		String ret = mHIDJNIBridge.getSerialNumberString(handle);
		if(ret == null) {
			throw new SerialComException("Could not get the serial number string from the HID device. Please retry !");
		}
		return ret;
	}
	
	/**
	 */
	public final String getIndexedString(long handle, int index) throws SerialComException {
		String ret = mHIDJNIBridge.getIndexedString(handle, index);
		if(ret == null) {
			throw new SerialComException("Could not get the string at given index from the HID device. Please retry !");
		}
		return ret;
	}
}
