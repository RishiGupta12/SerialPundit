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

package com.embeddedunveiled.serial.usb;

import com.embeddedunveiled.serial.SerialComException;
import com.embeddedunveiled.serial.SerialComHID;
import com.embeddedunveiled.serial.SerialComHIDdevice;
import com.embeddedunveiled.serial.internal.SerialComHIDJNIBridge;

/**
 * <p>Provides methods to communicate with USB HID devices.</p>
 * 
 * <p>A USB HID device should have standard device descriptor, standard configuration descriptor, standard 
 * interface descriptor for the HID class, class specific HID descriptor, standard endpoint descriptor for 
 * Interrupt IN endpoint and class specific report descriptor.</p>
 * 
 * @author Rishi Gupta
 */
public final class SerialComUSBHID extends SerialComHID {

	/**
	 * <p>Allocates a new SerialComUSBHID object with the given details.</p>
	 * 
	 * @param mHIDJNIBridge interface class to native library for calling platform specific routines.
	 */
	public SerialComUSBHID(SerialComHIDJNIBridge mHIDJNIBridge) {
		super(mHIDJNIBridge);
	}

	/**
	 * <p>Returns an array of SerialComHIDdevice objects containing information about USB-HID devices 
	 * as found by this library. Application can call various  methods on returned SerialComHIDdevice 
	 * object to get specific information like vendor id and product id etc.</p>
	 * 
	 * <p>The information about HID device returned includes, transport, vendor ID, product ID, serial 
	 * number, product, manufacturer, USB bus number, USB device number, location ID etc. In situations 
	 * where two or more devices with exactly same vendor ID, product ID and serial number are present 
	 * into system, information like location ID, USB bus number and USB device number can be used to 
	 * further categories them into unique devices. Application can also use some custom protocol to 
	 * identify devices that are of interest to them.</p>
	 * 
	 * <p>[1] Some bluetooth HID keyboard and mouse use a USB dongle which make them appear as USB HID 
	 * device to system. The keyboard/mouse communicate with dongle over bluetooth frequency while 
	 * dongle communicate with computer as USB HID device. This is also the reason why sometimes 
	 * bluetooth keyboard/mouse works even when there is no bluetooth stack installed in system.</p>
	 * 
	 * @param vendorFilter vendor whose devices should be listed (one of the constants SerialComUSB.V_xxxxx 
	 *         or any valid USB-IF VID).
	 * @return list of the HID devices with information about them or empty array if no device matching 
	 *          given criteria found.
	 * @throws SerialComException if an I/O error occurs.
	 * @throws IllegalArgumentException if vendorFilter is negative or invalid number.
	 */
	public SerialComHIDdevice[] listUSBHIDdevicesWithInfo(int vendorFilter) throws SerialComException {
		int i = 0;
		int numOfDevices = 0;
		SerialComHIDdevice[] usbHidDevicesFound = null;

		if((vendorFilter < 0) || (vendorFilter > 0XFFFF)) {
			throw new IllegalArgumentException("Argument vendorFilter can not be negative or greater than 0xFFFF !");
		}

		String[] usbhidDevicesInfo = mHIDJNIBridge.listUSBHIDdevicesWithInfo(vendorFilter);

		if(usbhidDevicesInfo != null) {
			if(usbhidDevicesInfo.length <= 3) {
				// if no devices found return empty array.
				return new SerialComHIDdevice[] { };
			}

			// number of elements sent by native layer will be multiple of 7
			// if device(s) is found to populate SerialComHIDdevice object.
			numOfDevices = usbhidDevicesInfo.length / 10;
			usbHidDevicesFound = new SerialComHIDdevice[numOfDevices];
			for(int x=0; x < numOfDevices; x++) {
				usbHidDevicesFound[x] = new SerialComHIDdevice(usbhidDevicesInfo[i], usbhidDevicesInfo[i+1], usbhidDevicesInfo[i+2], 
						usbhidDevicesInfo[i+3], usbhidDevicesInfo[i+4], usbhidDevicesInfo[i+5], usbhidDevicesInfo[i+6],
						usbhidDevicesInfo[i+7], usbhidDevicesInfo[i+8], usbhidDevicesInfo[i+9]);
				i = i + 10;
			}
			return usbHidDevicesFound;
		}else {
			throw new SerialComException("Could not find USB HID devices. Please retry !");
		}	
	}

	/**
	 * Opens a HID device for communication using the given USB attributes. If two or more devices have same 
	 * USB vendor ID, USB product ID and serial number, then location ID, USB bus number and device number 
	 * can be used to further reduce the scope of the device to be opened. Information about devices attached 
	 * to system can be obtained by listing them.
	 * 
	 * @param usbVidToMatch USB vendor ID to match. It must be supplied and must be valid.
	 * @param usbPidToMatch USB product ID to match. It must be supplied and must be valid.
	 * @param serialNumber USB device serial number to match (case insensitive) or null if matching 
	 *         is not required (optional).
	 * @param locationID location ID to match (OS assigned location ID to this device) or -1 if 
	 *         matching is not required (optional).
	 * @param usbBusNumber USB bus number to match (USB device should be found on this bus) or -1 
	 *         if matching is not required (optional).
	 * @param usbDevNumber USB device number to match (device number assigned by OS) or -1 if matching 
	 *         is not required (optional).
	 * @throws SerialComException if an I/O error occurs.
	 * @throws IllegalArgumentException if usbVidToMatch or usbPidToMatch is negative or or invalid number.
	 * @see com.embeddedunveiled.serial.usb.SerialComUSBHID#listUSBHIDdevicesWithInfo(int)
	 */
	public long openHidDeviceByUSBAttributes(int usbVidToMatch, int usbPidToMatch, final String serialNumber,
			int locationID, int usbBusNumber, int usbDevNumber) throws SerialComException {
		if((usbVidToMatch < 0) || (usbVidToMatch > 0XFFFF)) {
			throw new IllegalArgumentException("Argument usbVidToMatch can not be negative or greater than 0xFFFF !");
		}
		if((usbPidToMatch < 0) || (usbPidToMatch > 0XFFFF)) {
			throw new IllegalArgumentException("Argument usbPidToMatch can not be negative or greater than 0xFFFF !");
		}
		String serialNum = null;
		if(serialNumber != null) {
			serialNum = serialNumber.toLowerCase();
		}

		long handle = mHIDJNIBridge.openHidDeviceByUSBAttributes(usbVidToMatch, usbPidToMatch, serialNum, 
				locationID, usbBusNumber, usbDevNumber);
		if(handle < 0) {
			// extra check.
			throw new SerialComException("Could not open the HID device by USB attributes. Please retry !");
		}
		return handle;
	}
}
