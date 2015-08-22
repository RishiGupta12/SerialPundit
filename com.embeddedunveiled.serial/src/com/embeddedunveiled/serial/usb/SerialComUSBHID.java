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

package com.embeddedunveiled.serial.usb;

import com.embeddedunveiled.serial.SerialComException;
import com.embeddedunveiled.serial.SerialComHIDdevice;
import com.embeddedunveiled.serial.internal.SerialComHIDJNIBridge;
import com.embeddedunveiled.serial.internal.SerialComPortHandleInfo;

/**
 * <p></p>
 * 
 */
public final class SerialComUSBHID {

	private SerialComHIDJNIBridge mHIDJNIBridge;

	/**
	 * <p>Allocates a new SerialComUSBHID object with the given details.</p>
	 * 
	 * @param mHIDJNIBridge
	 */
	public SerialComUSBHID(SerialComHIDJNIBridge mHIDJNIBridge) {
		this.mHIDJNIBridge = mHIDJNIBridge;
	}

	/**
	 * <p>Returns an array of SerialComHIDdevice objects containing information about USB-HID devices 
	 * as found by this library. Application can call various  methods on returned SerialComHIDdevice 
	 * object to get specific information like vendor id and product id etc.</p>
	 * 
	 * <p>TODO</p>
	 * 
	 * @param vendorFilter vendor whose devices should be listed (one of the constants SerialComUSB.V_xxxxx or any valid USB VID).
	 * @return list of the HID devices with information about them or empty array if no device matching given criteria found.
	 * @throws SerialComException if an I/O error occurs.
	 * @throws IllegalArgumentException if vendorFilter is negative or invalid number.
	 */
	public SerialComHIDdevice[] listUSBHIDdevicesWithInfo(int vendorFilter) throws SerialComException {
		int i = 0;
		int numOfDevices = 0;
		SerialComHIDdevice[] usbHidDevicesFound = null;

		if((vendorFilter < 0) || (vendorFilter > 0XFFFF)) {
			throw new IllegalArgumentException("Argument vendorFilter can not be negative or greater tha 0xFFFF !");
		}

		String[] usbhidDevicesInfo = mHIDJNIBridge.listUSBHIDdevicesWithInfo(vendorFilter);

		if(usbhidDevicesInfo != null) {
			numOfDevices = usbhidDevicesInfo.length / 7;
			usbHidDevicesFound = new SerialComHIDdevice[numOfDevices];
			for(int x=0; x < numOfDevices; x++) {
				usbHidDevicesFound[x] = new SerialComHIDdevice(usbhidDevicesInfo[i], usbhidDevicesInfo[i+1], usbhidDevicesInfo[i+2], 
						usbhidDevicesInfo[i+3], usbhidDevicesInfo[i+4], usbhidDevicesInfo[i+5], usbhidDevicesInfo[i+6]);
				i = i + 7;
			}
			return usbHidDevicesFound;
		}else {
			return new SerialComHIDdevice[] { };
		}	
	}

	/** 
	 */
	public long openHidDeviceByUSBAttributes(int usbVidToMatch, int usbPidToMatch, final String serialNumber) throws SerialComException {
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


























