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

package com.embeddedunveiled.serial;

/**
 * <p>Encapsulates data received from serial port. Application can call getDataBytes() method
 * on an instance of this class to retrieve data.</p>
 */
public final class SerialComDataEvent {

	private byte[] mData;

	public SerialComDataEvent(byte[] data){
		this.mData = data;
	}

	/**
	 * <p>This method return array of bytes which represents data bytes read from serial port.</p>
	 * @return data read from serial port.
	 */
	public byte[] getDataBytes() {
		return mData;
	}
	
	/**
	 * <p>This method return length of data read from serial port.</p>
	 * @return length of data read from serial port.
	 */
	public int getDataBytesLength() {
		return mData.length;
	}
}
