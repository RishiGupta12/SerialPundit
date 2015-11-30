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
 * <p>Super class for all HID transport mediums.</p>
 * 
 * @author Rishi Gupta
 */
public class SerialComHIDTransport {

	// sub-classes also uses this reference to invoke native functions.
	protected SerialComHIDJNIBridge mHIDJNIBridge;
	protected int osType;

	/**
	 * <p>Allocates a new SerialComHID object.</p>
	 * 
	 * @param mHIDJNIBridge interface class to native library for calling platform specific routines.
	 */
	public SerialComHIDTransport(SerialComHIDJNIBridge mHIDJNIBridge, int osType) {
		this.mHIDJNIBridge = mHIDJNIBridge;
		this.osType = osType;
	}
}
