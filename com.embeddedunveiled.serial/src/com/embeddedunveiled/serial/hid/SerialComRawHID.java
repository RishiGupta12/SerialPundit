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

package com.embeddedunveiled.serial.hid;

import com.embeddedunveiled.serial.ISerialComUSBHotPlugListener;
import com.embeddedunveiled.serial.SerialComException;
import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.SerialComUtil;
import com.embeddedunveiled.serial.internal.SerialComHIDJNIBridge;
import com.embeddedunveiled.serial.usb.SerialComUSBHID;

/**
 * <p>Contains APIs to communicate with a HID class device in raw mode. The reports sent/received 
 * will not be parsed. The application must understand meaning and format of each field in report 
 * exchanged.</p>
 * 
 * <p>Applications may develop user space drivers using raw HID methods in this class.</p>
 * 
 * @author Rishi Gupta
 */
public final class SerialComRawHID extends SerialComHID {

	/**
	 * <p>Construct and allocates a new SerialComRawHID object with given details.</p>
	 * 
	 * @param mHIDJNIBridge interface class to native library for calling platform specific routines.
	 * @param osType operating system this library is running on.
	 * @throws SerialComException if the object can not be constructed.
	 */
	public SerialComRawHID(SerialComHIDJNIBridge mHIDJNIBridge, int osType) {
		super(mHIDJNIBridge, osType);
	}

	/**
	 * <P>Find all the device instances claiming to be HID device.</p>
	 * 
	 * <p>Returns an array of SerialComHIDdevice objects containing information about HID devices 
	 * as found by this library. The HID devices found may be USB HID or Bluetooth HID device. 
	 * Application can call various  methods on returned SerialComHIDdevice object to get specific 
	 * information like vendor id and product id etc.</p>
	 * 
	 * <p>The information about HID device returned includes, transport, vendor ID, product ID, serial 
	 * number, product, manufacturer, USB bus number, USB device number, location ID etc. In situations 
	 * where two or more devices with exactly same vendor ID, product ID and serial number are present 
	 * into system, information like location can be used to further categories them into unique devices. 
	 * Application can also use some custom protocol to identify devices that are of interest to them.</p>
	 * 
	 * @return list of the HID devices with information about them or empty array if no device 
	 *          matching given criteria found.
	 * @throws SerialComException if an I/O error occurs.
	 */
	public final SerialComHIDdevice[] listHIDdevicesWithInfoR() throws SerialComException {
		int i = 0;
		int numOfDevices = 0;
		SerialComHIDdevice[] hidDevicesFound = null;

		String[] hidDevicesInfo = mHIDJNIBridge.listHIDdevicesWithInfoR();

		if(hidDevicesInfo != null) {
			if(hidDevicesInfo.length < 3) {
				return new SerialComHIDdevice[] { };
			}
			numOfDevices = hidDevicesInfo.length / 8;
			hidDevicesFound = new SerialComHIDdevice[numOfDevices];
			for(int x=0; x < numOfDevices; x++) {
				hidDevicesFound[x] = new SerialComHIDdevice(hidDevicesInfo[i], hidDevicesInfo[i+1], hidDevicesInfo[i+2], 
						hidDevicesInfo[i+3], hidDevicesInfo[i+4], hidDevicesInfo[i+5], hidDevicesInfo[i+6],
						hidDevicesInfo[i+7]);
				i = i + 8;
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
	 * @param separator separator string to be placed between two consecutive bytes (useful 
	 *         when printing values on console).
	 * @return constructed hex string if report.length > 0 otherwise empty string.
	 * @throws IllegalArgumentException if report is null.
	 */
	public final String formatReportToHexR(byte[] report, String separator) throws SerialComException {
		return SerialComUtil.byteArrayToHexString(report, separator);
	}

	/**
	 * <p>Opens a HID device for communication using its path name.</p>
	 * 
	 * <P>Applications can register USB hot plug listener to get notified when the desired USB device 
	 * is plugged into system. Once the listener is invoked indicating device is added, application 
	 * can find the device node representing this USB-HID device and proceed to open it.</p>
	 * 
	 * <p>In Linux it may be required to add correct udev rules so as to grant permission to 
	 * access to the USB-HID device. Refer this udev rule file for MCP2200 as an example : 
	 * https://github.com/RishiGupta12/serial-communication-manager/blob/master/tests/99-scm-mcp2200-hid.rules</p>
	 * 
	 * <p>In Windows, a unique physical device object (PDO) is created for each Top Level Collection 
	 * described by the Report Descriptor and there will be device instance for each Top Level 
	 * Collection. This means same USB HID interface may have many HID device instances associated 
	 * with it.</p>
	 * 
	 * <p>Windows supports many top level collection and some of them might be opened in shared mode while 
	 * may be available for exclusive access only. Some of the HID devices may be reserved for system use 
	 * only and operating system may provide a dedicated framework/driver and API for it. Some devices 
	 * need to be switched from keyboard emulation mode to HID mode to make them accessible by application.</p>
	 * 
	 * @param pathName device node full path for Unix-like OS and device instance for Windows 
	 *         (as obtained by listing HID devices).
	 * @param shared set to true if the device is to be opened in shared mode otherwise false 
	 *         for exclusive access.
	 * @return handle of the opened HID device.
	 * @throws SerialComException if an IO error occurs.
	 * @throws IllegalArgumentException if pathName is null or empty string.
	 * @see com.embeddedunveiled.serial.SerialComManager#registerUSBHotPlugEventListener(ISerialComUSBHotPlugListener, int, int, String)
	 * @see com.embeddedunveiled.serial.hid.SerialComHID#listHIDdevicesWithInfo()
	 */
	public final long openHidDeviceR(final String pathName, boolean shared) throws SerialComException {
		if(pathName == null) {
			throw new IllegalArgumentException("Argument pathName can not be null !");
		}
		String pathNameVal = pathName.trim();
		if(pathNameVal.length() == 0) {
			throw new IllegalArgumentException("Argument pathName can not be empty string !");
		}

		long handle = mHIDJNIBridge.openHidDeviceR(pathNameVal, shared, osType);
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
	public final boolean closeHidDeviceR(long handle) throws SerialComException {
		int ret = mHIDJNIBridge.closeHidDeviceR(handle);
		if(ret < 0) {
			throw new SerialComException("Could not close the given HID device. Please retry !");
		}
		return true;
	}

	/** 
	 * <p>Prepares a context that should be passed to readInputReport and unblockBlockingHIDIOOperation 
	 * methods.</p>
	 * 
	 * <p>Application must catch exception thrown by this method. When this method returns and 
	 * exception with message SerialComHID.EXP_UNBLOCK_HIDIO is thrown, it indicates that the 
	 * blocked read method was explicitly unblocked by another thread (possibly because it is 
	 * going to close the device).</p>
	 * 
	 * @return context that should be passed to readInputReport and unblockBlockingHIDIOOperation 
	 *          methods.
	 * @throws SerialComException if an I/O error occurs.
	 */
	public long createBlockingHIDIOContextR() throws SerialComException {
		long ret = mHIDJNIBridge.createBlockingHIDIOContextR();
		if(ret < 0) {
			throw new SerialComException("Could not create blocking HID I/O context. Please retry !");
		}
		return ret;
	}

	/** 
	 * <p>Unblocks any blocked operation if it exist. This causes closing of HID device possible 
	 * gracefully and return the worker thread that called blocking read/write to return and proceed 
	 * as per application design.</p>
	 * 
	 * @param context context obtained from call to createBlockingIOContext method for blocking 
	 *         I/O operations.
	 * @return true if blocked operation was unblocked successfully.
	 * @throws SerialComException if an I/O error occurs.
	 */
	public boolean unblockBlockingHIDIOOperationR(long context) throws SerialComException {
		int ret = mHIDJNIBridge.unblockBlockingHIDIOOperationR(context);
		if(ret < 0) {
			throw new SerialComException("Could not unblock the blocked HID I/O operation. Please retry !");
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
	 * <p>If the HID device uses numbered report, reportID should be set to report number. If the HID 
	 * device does not uses numbered reports reportID must be set to -1. The report (report array) should 
	 * should contain only report bytes (it should not contain report ID).</p>
	 * 
	 * @param handle handle of the HID device to which this report will be sent.
	 * @param reportId unique identifier for the report type or -1 if device does not use report IDs.
	 * @param report report to be sent to the HID device.
	 * @return number of bytes sent to the HID device.
	 * @throws SerialComException if an I/O error occurs.
	 * @throws IllegalArgumentException if report is null or empty array. 
	 */
	public final int writeOutputReportR(long handle, byte reportId, final byte[] report) throws SerialComException {
		if(report == null) {
			throw new IllegalArgumentException("Argumenet report can not be null !");
		}
		if(report.length == 0) {
			throw new IllegalArgumentException("Argumenet report can not be of zero length !");
		}

		int ret = mHIDJNIBridge.writeOutputReportR(handle, reportId, report, report.length);
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
	 * given buffer. If the device uses numbered reports, first byte in reportBuffer array will be report 
	 * number. If the device does not uses numbered reports, first byte in reportBuffer will be beginning 
	 * of data itself.</p>
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
	 * @param context context obtained by a call to createBlockingIOContext method.
	 * @return number of bytes read from HID device.
	 * @throws SerialComException if an I/O error occurs.
	 * @throws IllegalArgumentException if reportBuffer is null or if length is negative.
	 */
	public final int readInputReportR(long handle, byte[] reportBuffer, long context) throws SerialComException {
		if(reportBuffer == null) {
			throw new IllegalArgumentException("Argumenet dataBuffer can not be null !");
		}

		int ret = mHIDJNIBridge.readInputReportR(handle, reportBuffer, reportBuffer.length, context);
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
	 * <p>If input report is read from HID device, it returns number of bytes read and places data bytes in 
	 * given buffer. If there was no data to read it returns 0.</p>
	 * 
	 * <p>Input report (controls) are sources of data for application running on host processor (USB Host side) 
	 * for example X and Y coordinates obtained from touch screen or state of a GPIO pin. It can also be a 
	 * response to a command sent previously as output report.</p>
	 * 
	 * @param handle handle of the HID device from whom input report is to be read.
	 * @param reportBuffer byte buffer in which input report will be saved.
	 * @param timeoutValue time in milliseconds after which read must return with whatever data is read 
	 *         till that time or no data read at all.
	 * @return number of bytes read from HID device.
	 * @throws SerialComException if an I/O error occurs.
	 * @throws IllegalArgumentException if reportBuffer is null or if length is negative.
	 */
	public final int readInputReportWithTimeoutR(long handle, byte[] reportBuffer, int timeoutValue) 
			throws SerialComException {

		if(reportBuffer == null) {
			throw new IllegalArgumentException("Argumenet reportBuffer can not be null !");
		}

		int ret = mHIDJNIBridge.readInputReportWithTimeoutR(handle, reportBuffer, reportBuffer.length, timeoutValue);
		if(ret < 0) {
			throw new SerialComException("Could not read input report from HID device. Please retry !");
		}

		return ret;
	}

	/**
	 * <p>Send a feature report to the HID device. If the HID device uses numbered reports, set reportID 
	 * to report number. If the HID device does not uses numbered reports set reportID to -1. The report 
	 * byte array should contain only report data bytes.</p>
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
	public final int sendFeatureReportR(long handle, byte reportId, final byte[] report) throws SerialComException {
		if(report == null) {
			throw new IllegalArgumentException("Argumenet report can not be null !");
		}
		if(report.length == 0) {
			throw new IllegalArgumentException("Argumenet report can not be of zero length !");
		}

		int ret = mHIDJNIBridge.sendFeatureReportR(handle, reportId, report, report.length);
		if(ret < 0) {
			throw new SerialComException("Could not send feature report to HID device. Please retry !");
		}
		return ret;
	}

	/**
	 * <p>Read a feature report to the HID device. If the HID device uses numbered reports, set reportID 
	 * to report number. If the HID device does not uses numbered reports set reportID to -1. If the 
	 * featured report is read from HID device, data read will be placed in report byte array. This 
	 * array will contain feature report (excluding report ID).</p>
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
	public final int getFeatureReportR(long handle, byte reportId, final byte[] report) throws SerialComException {
		if(report == null) {
			throw new IllegalArgumentException("Argumenet reportBuffer can not be null !");
		}

		int ret = mHIDJNIBridge.getFeatureReportR(handle, reportId, report, report.length);
		if(ret < 0) {
			throw new SerialComException("Could not get feature report from HID device. Please retry !");
		}
		return ret;
	}

	/**
	 * <p>Gives the manufacturer of the HID device.</p>
	 * 
	 * @param handle handle of the HID device whose manufacturer is to be found.
	 * @return manufacturer name string.
	 * @throws SerialComException if an I/O error occurs.
	 */
	public final String getManufacturerStringR(long handle) throws SerialComException {
		String ret = mHIDJNIBridge.getManufacturerStringR(handle);
		if(ret == null) {
			throw new SerialComException("Could not get the manufacturer string from the HID device. Please retry !");
		}
		return ret;
	}

	/**
	 * <p>Gives the product name of the HID device.</p>
	 * 
	 * @param handle handle of the HID device whose product name is to be found.
	 * @return product name string of the HID device.
	 * @throws SerialComException if an I/O error occurs.
	 */
	public final String getProductStringR(long handle) throws SerialComException {
		String ret = mHIDJNIBridge.getProductStringR(handle);
		if(ret == null) {
			throw new SerialComException("Could not get the product string from the HID device. Please retry !");
		}
		return ret;
	}

	/**
	 * <p>Gives the serial number of the HID device.</p>
	 * 
	 * @param handle handle of the HID device whose serial number is to be found.
	 * @return serial number string of the HID device.
	 * @throws SerialComException if an I/O error occurs.
	 */
	public final String getSerialNumberStringR(long handle) throws SerialComException {
		String ret = mHIDJNIBridge.getSerialNumberStringR(handle);
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
	 * @return string at given index read from the HID device.
	 * @throws SerialComException if an I/O error occurs.
	 */
	public final String getIndexedStringR(long handle, int index) throws SerialComException {
		if(osType == SerialComManager.OS_WINDOWS) {
			String ret = mHIDJNIBridge.getIndexedStringR(handle, index);
			if(ret == null) {
				throw new SerialComException("Could not get the string at given index from the HID device. Please retry !");
			}
			return ret;
		}else {
			throw new SerialComException("Not supported on this operating system !");
		}
	}

	/**
	 * <p>Gives the name of the driver who is driving the given HID device.</p>
	 * 
	 * @param hidDeviceNode device node (port name) for HID device whose driver is to be found.
	 * @return name of driver serving given HID device.
	 * @throws SerialComException if operation can not be completed successfully.
	 * @throws IllegalArgumentException if argument hidDeviceNode is null or is an empty string.
	 */
	public String findDriverServingHIDDeviceR(String hidDeviceNode) throws SerialComException {
		if(hidDeviceNode == null) {
			throw new IllegalArgumentException("Argument hidDeviceNode can not be null !");
		}
		if(hidDeviceNode.length() == 0) {
			throw new IllegalArgumentException("Argument hidDeviceNode can not be empty string !");
		}
		if(hidDeviceNode.length() > 256) {
			// linux have 256 as maximum length of file name.
			throw new IllegalArgumentException("Argument hidDeviceNode string can not be greater than 256 in length !");
		}

		String driverName = mHIDJNIBridge.findDriverServingHIDDeviceR(hidDeviceNode);
		if(driverName == null) {
			throw new SerialComException("Failed to find driver serving the given HID device. Please retry !");
		}
		return driverName;
	}

	/**
	 * <p>Gives the report descriptor as supplied by device itself.</p>
	 * 
	 * @param handle handle of the device whose report descriptor is to be obtained.
	 * @return HID report descriptor as array of bytes otherwise empty array.
	 * @throws SerialComException if operation can not be completed successfully.
	 */
	public byte[] getReportDescriptorR(long handle) throws SerialComException {
		byte[] reportDescriptorRead = mHIDJNIBridge.getReportDescriptorR(handle);
		if(reportDescriptorRead != null) {
			return reportDescriptorRead;
		}
		return new byte[0];
	}

	/**
	 * <p>Returns an instance of class SerialComUSBHID for HID over USB operations.</p>
	 * 
	 * @param transport one of the HID_XXX constants.
	 * @return object of one of the subclasses of SerialComHIDTransport class.
	 * @throws SerialComException if operation can not be completed successfully.
	 */
	public SerialComHIDTransport getHIDTransportInstance(int transport) throws SerialComException {

		if(transport == SerialComHID.HID_USB) {
			return new SerialComUSBHID(mHIDJNIBridge, osType);
		}else if(transport == SerialComHID.HID_BLUETOOTH) {
			//TODO
		}else {
			throw new IllegalArgumentException("Argument transport must be one of the HID_XXX constants !");
		}

		return null;
	}
}
