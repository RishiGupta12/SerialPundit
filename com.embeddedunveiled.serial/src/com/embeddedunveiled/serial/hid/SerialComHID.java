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

import com.embeddedunveiled.serial.internal.SerialComHIDJNIBridge;

/**
 * <p>The SCM library provides two set of APIs for communicating with a HID device. First is raw HID 
 * communication using methods in SerialComRawHID class. In raw mode, input reports received and sent 
 * output reports are not parsed. Second is SerialComParsedHID class in which reports are parsed.</p>
 * 
 * <p>In Windows, system supplied HID minidriver and HID class drivers provide most of the functionality 
 * and required support for HID-compliant devices. However we may have to write our own HID minidriver
 * if it is difficult to make desired changes to HID-compliant device firmware or if we need to make a 
 * non-HID compliant device into a HID device without updating the firmware.</p>
 * 
 * @author Rishi Gupta
 */
public class SerialComHID {

	/**<p>The value indicating instance of SerialComRawHID class. Integer constant with value 0x01.</p>*/
	public static final int MODE_RAW = 0x01;

	/**<p>The value indicating instance of SerialComParsedHID class. Integer constant with value 0x02.</p>*/
	public static final int MODE_PARSED = 0x02;

	/**<p>The value indicating instance of SerialComHID class (HID transport neutral). Integer constant with 
	 * value 0x03.</p>*/
	public static final int HID_GENERIC = 0x03;

	/**<p>The value indicating instance of SerialComUSBHID class (HID over USB). Integer constant 
	 * with value 0x04.</p>*/
	public static final int HID_USB = 0x04;

	/**<p>The value indicating instance of SerialComBluetoothHID class (HID over Bluetooth). Integer 
	 * constant with value 0x05.</p>*/
	public static final int HID_BLUETOOTH = 0x05;

	/**<p>The value indicating instance of SerialComI2CHID class (HID over I2C). Integer constant with 
	 * value 0x06.</p>*/
	public static final int HID_I2C = 0x06;

	/** <p>The exception message indicating that a blocked read method has been unblocked 
	 * and made to return to caller explicitly (irrespective there was data to read or not). </p>*/
	public static final String EXP_UNBLOCK_HIDIO  = "I/O operation unblocked !";

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
}
