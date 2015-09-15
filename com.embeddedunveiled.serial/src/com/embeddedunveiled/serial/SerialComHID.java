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

	/**<p>The value indicating instance of SerialComHID class. Integer constant with value 0x01.</p>*/
	public static final int HID_GENERIC = 0x01;

	/**<p>The value indicating instance of SerialComUSBHID class. Integer constant with value 0x02.</p>*/
	public static final int HID_USB = 0x02;

	/**<p>The value indicating instance of class SerialComBluetoothHID. Integer constant with value 0x03.</p>*/
	public static final int HID_BLUETOOTH = 0x03;

	// sub-classes also uses this reference to invoke native functions.
	protected SerialComHIDJNIBridge mHIDJNIBridge;
	protected int osType;

	/**
	 * <p>Allocates a new SerialComHID object.</p>
	 * 
	 * @param mHIDJNIBridge interface class to native library for calling platform specific routines.
	 */
	public SerialComHID(SerialComHIDJNIBridge mHIDJNIBridge, int osType) {
		this.mHIDJNIBridge = mHIDJNIBridge;
		this.osType = osType;
	}

	/**
	 * <p>Returns an array of SerialComHIDdevice objects containing information about HID devices 
	 * as found by this library. The HID devices found may be USB HID or Bluetooth HID device. 
	 * Application can call various  methods on returned SerialComHIDdevice object to get specific 
	 * information like vendor id and product id etc.</p>
	 * 
	 * <p>The information about HID device returned includes, transport, vendor ID, product ID, serial 
	 * number, product, manufacturer, USB bus number, USB device number, location ID etc. In situations 
	 * where two or more devices with exactly same vendor ID, product ID and serial number are present 
	 * into system, information like location ID, USB bus number and USB device number can be used to 
	 * further categories them into unique devices. Application can also use some custom protocol to 
	 * identify devices that are of interest to them.</p>
	 * 
	 * @return list of the HID devices with information about them or empty array if no device 
	 *          matching given criteria found.
	 * @throws SerialComException if an I/O error occurs.
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
			numOfDevices = hidDevicesInfo.length / 10;
			hidDevicesFound = new SerialComHIDdevice[numOfDevices];
			for(int x=0; x < numOfDevices; x++) {
				hidDevicesFound[x] = new SerialComHIDdevice(hidDevicesInfo[i], hidDevicesInfo[i+1], hidDevicesInfo[i+2], 
						hidDevicesInfo[i+3], hidDevicesInfo[i+4], hidDevicesInfo[i+5], hidDevicesInfo[i+6],
						hidDevicesInfo[i+7], hidDevicesInfo[i+8], hidDevicesInfo[i+9]);
				i = i + 10;
			}
			return hidDevicesFound;
		}else {
			throw new SerialComException("Could not find HID devices. Please retry !");
		}	
	}

	/**
	 * <p>Converts report read from HID device to hexadecimal string. This may be 
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
	 * <P>Applications can register hotplug listener to get notified when the desired USB device is plugged 
	 * into system. Once the listener is invoked indicating device is added, application can find the device 
	 * node representing this USB-HID device and proceed to open it.</p>
	 * 
	 * <p>In Linux it may be required to add correct udev rules so as to grant permission to 
	 * access to the USB-HID device. Refer this udev rule file for MCP2200 as an example : 
	 * https://github.com/RishiGupta12/serial-communication-manager/blob/master/tests/scm-mcp2200-hid.rules</p>
	 * 
	 * @param pathName device node full path for Unix-like OS and port name for Windows.
	 * @return handle of the opened HID device.
	 * @throws SerialComException if an IO error occurs.
	 * @throws IllegalArgumentException if pathName is null or empty string.
	 * @see com.embeddedunveiled.serial.SerialComManager#registerHotPlugEventListener(ISerialComHotPlugListener, int, int)
	 * @see com.embeddedunveiled.serial.SerialComHID#listHIDdevicesWithInfo()
	 */
	public final long openHidDevice(final String pathName) throws SerialComException {
		if(pathName == null) {
			throw new IllegalArgumentException("Argument pathName can not be null !");
		}
		String pathNameVal = pathName.trim();
		if(pathNameVal.length() == 0) {
			throw new IllegalArgumentException("Argument pathName can not be empty string !");
		}

		long handle = mHIDJNIBridge.openHidDevice(pathNameVal, osType);
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
	 * <p>Sends the given output report to the HID device. Report ID is used to uniquely identify the  
	 * report.</p>
	 * 
	 * <p>Output report (controls) may be a sink for application data, for example, an LED that indicates 
	 * the state of a device. It can represent a command sent from application running on host to USB HID 
	 * device for example to toggle a GPIO pin or vibrate the motor mounted on gamepad.</p>
	 * 
	 * <p>Only Input reports are sent via the Interrupt In pipe. Feature and Output reports must be initiated 
	 * by the host via the Control pipe or an optional Interrupt Out pipe.</p>
	 * 
	 * @param handle handle of the HID device to which this report will be sent.
	 * @param reportId unique identifier for the report type or -1 if device does not use report IDs.
	 * @param report report to be sent to the HID device.
	 * @return number of bytes sent to the HID device.
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

		int ret = mHIDJNIBridge.writeOutputReport(handle, reportId, report, report.length);
		if(ret < 0) {
			throw new SerialComException("Could not write output report to the HID device. Please retry !");
		}
		return ret;
	}

	/**
	 * <p>Reads input report from the given HID device.</p>
	 * 
	 * <p>The size of {@code reportBuffer} passed must be large enough to hold the expected number of bytes 
	 * in input report and one more extra byte if the HID device uses numbered reports. The 1st byte will be 
	 * report ID if device uses numbered reports otherwise the report data will begin at the first byte.</p>
	 * 
	 * <p>If input report is read from device, it returns number of bytes read and places data bytes in 
	 * given buffer.</p>
	 * 
	 * <p>HID devices with custom firmware provide valid HID report descriptor to comply with USB 
	 * standards and to make sure that class driver of operating system recognizes device and serve it. 
	 * However, the data carried in reports may have different meaning and interpretation than what was 
	 * described in report descriptor. This is the case mainly when developing custom application HID device 
	 * which can not be strictly categorized as a HID device, however, leverages HID specifications and 
	 * API to communicate with the host system. Vendors provide a document describing how to interpret a 
	 * particular byte in report received from device or how to construct an output report.</p>
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
	 * <p>Try to read input report from HID device within the given timeout limit.</p>
	 * 
	 * <p>The size of {@code reportBuffer} passed must be large enough to hold the expected number of bytes 
	 * in input report and one more extra byte if the HID device uses numbered reports. The 1st byte will be 
	 * report ID if device uses numbered reports otherwise the report data will begin at the first byte.</p>
	 * 
	 * <p>If input report is read from device, it returns number of bytes read and places data bytes in 
	 * given buffer. If there was no data to read it returns 0.</p>
	 * 
	 * <p>Input report (controls) are sources of data for application running on host processor (USB Host side) 
	 * for example X and Y coordinates obtained from touch screen or state of a GPIO pin. It can also be a 
	 * response to a command sent previously as output report.</p>
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
	public final int readInputReportWithTimeout(long handle, byte[] reportBuffer, int length, int timeoutValue) 
			throws SerialComException {
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
	 * value must be -1.</p>
	 * 
	 * <p>Typically, feature reports are sent/received for configuring USB device or USB host at application 
	 * start-up, or for sending/receiving special event or state information, or for saving any data item that 
	 * application wish to write in HID device and read it back may be some time later.</p>
	 * 
	 * @param handle handle of the HID device to which this feature report will be sent.
	 * @param reportId unique identifier for the report type or -1 if not applicable.
	 * @param report feature report to be sent to the HID device.
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

		int ret = mHIDJNIBridge.sendFeatureReport(handle, reportId, report, report.length);
		if(ret < 0) {
			throw new SerialComException("Could not send feature report to HID device. Please retry !");
		}
		return ret;
	}

	/**
	 * <p>Receive a feature report from the HID device. For devices which support only single report, report ID 
	 * value must be -1.</p>
	 * 
	 * <p>Typically, feature reports are sent/received for configuring USB device or USB host at application 
	 * start-up, or for sending/receiving special event or state information, or for saving any data item that 
	 * application wish to write in HID device and read it back may be some time later.</p>
	 * 
	 * @param handle handle of the HID device from whom feature report is to be read.
	 * @param reportId unique identifier for the report type or -1 if not applicable.
	 * @param report byte type buffer where feature report will be saved.
	 * @return number of bytes read from HID device.
	 * @throws SerialComException if an I/O error occurs.
	 * @throws IllegalArgumentException if reportBuffer is null or its length is zero.
	 */
	public final int getFeatureReport(long handle, byte reportId, final byte[] report) throws SerialComException {
		if(report == null) {
			throw new IllegalArgumentException("Argumenet reportBuffer can not be null !");
		}

		int ret = mHIDJNIBridge.getFeatureReport(handle, reportId, report, report.length);
		if(ret < 0) {
			throw new SerialComException("Could not get feature report from HID device. Please retry !");
		}
		return ret;
	}

	/**
	 * <p>Gives the manufacturer of the HID device.</p>
	 * 
	 * @param handle handle of the HID device whose manufacturer is to be found.
	 * @return manufacturer name.
	 * @throws SerialComException if an I/O error occurs.
	 */
	public final String getManufacturerString(long handle) throws SerialComException {
		String ret = mHIDJNIBridge.getManufacturerString(handle);
		if(ret == null) {
			throw new SerialComException("Could not get the manufacturer string from the HID device. Please retry !");
		}
		return ret;
	}

	/**
	 * <p>Gives the product name of the HID device.</p>
	 * 
	 * @param handle handle of the HID device whose product name is to be found.
	 * @return product name of the HID device.
	 * @throws SerialComException if an I/O error occurs.
	 */
	public final String getProductString(long handle) throws SerialComException {
		String ret = mHIDJNIBridge.getProductString(handle);
		if(ret == null) {
			throw new SerialComException("Could not get the product string from the HID device. Please retry !");
		}
		return ret;
	}

	/**
	 * <p>Gives the serial number of the HID device.</p>
	 * 
	 * @param handle handle of the HID device whose serial number is to be found.
	 * @return serial number of the HID device.
	 * @throws SerialComException if an I/O error occurs.
	 */
	public final String getSerialNumberString(long handle) throws SerialComException {
		String ret = mHIDJNIBridge.getSerialNumberString(handle);
		if(ret == null) {
			throw new SerialComException("Could not get the serial number string from the HID device. Please retry !");
		}
		return ret;
	}

	/**
	 * <p>Gives the string at the given index of string descriptor from HID device.</p>
	 * 
	 * <p>Supported on Windows only as serial communication manager library does not use any 
	 * user space drivers.</p>
	 * 
	 * @param handle handle of the HID device from whom indexed string is to be read.
	 * @return string read from the HID device.
	 * @throws SerialComException if an I/O error occurs.
	 */
	public final String getIndexedString(long handle, int index) throws SerialComException {
		if(osType == SerialComManager.OS_WINDOWS) {
			String ret = mHIDJNIBridge.getIndexedString(handle, index);
			if(ret == null) {
				throw new SerialComException("Could not get the string at given index from the HID device. Please retry !");
			}
			return ret;
		}else {
			throw new SerialComException("Not supported on this operating system !");
		}
	}

	public byte[] getReportDescriptor(long handle) throws SerialComException {
		byte[] reportDescriptorRead = mHIDJNIBridge.getReportDescriptor(handle);
		if(reportDescriptorRead != null) {
			return reportDescriptorRead;
		}
		return new byte[0];
	}
}
