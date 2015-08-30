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
	 * <p>Some bluetooth HID keyboard and mouse use a USB dongle which make them appear as USB HID 
	 * device to system. The keyboard/mouse communicate with dongle over bluetooth frequency while 
	 * dongle communicate with system as USB HID device. This is also the reason why sometimes 
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
			numOfDevices = usbhidDevicesInfo.length / 9;
			usbHidDevicesFound = new SerialComHIDdevice[numOfDevices];
			for(int x=0; x < numOfDevices; x++) {
				usbHidDevicesFound[x] = new SerialComHIDdevice(usbhidDevicesInfo[i], usbhidDevicesInfo[i+1], usbhidDevicesInfo[i+2], 
						usbhidDevicesInfo[i+3], usbhidDevicesInfo[i+4], usbhidDevicesInfo[i+5], usbhidDevicesInfo[i+6],
						usbhidDevicesInfo[i+7], usbhidDevicesInfo[i+8]);
				i = i + 9;
			}
			return usbHidDevicesFound;
		}else {
			throw new SerialComException("Could not find USB HID devices. Please retry !");
		}	
	}

	/** 
	 */
	public long openHidDeviceByUSBAttributes(int usbVidToMatch, int usbPidToMatch, final String serialNumber) 
			throws SerialComException {
		if(usbVidToMatch < 0) {
			throw new IllegalArgumentException("Argument usbVidToMatch can not be negative !");
		}
		if(usbPidToMatch < 0) {
			throw new IllegalArgumentException("Argument usbPidToMatch can not be negative !");
		}
		String serialNum = null;
		if(serialNumber != null) {
			serialNum = serialNumber.toLowerCase();
		}

		long handle = mHIDJNIBridge.openHidDeviceByUSBAttributes(usbVidToMatch, usbPidToMatch, serialNum);
		if(handle < 0) {
			// extra check.
			throw new SerialComException("Could not open the HID device by USB attributes. Please retry !");
		}
		return handle;
	}
}
