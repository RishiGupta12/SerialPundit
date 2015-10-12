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

package com.embeddedunveiled.serial.internal;

import com.embeddedunveiled.serial.ISerialComUSBHotPlugListener;

/**
 * <p>Encapsulates the information associated with USB hot plug listener and 
 * its native part.</p>
 * 
 * @author Rishi Gupta
 */
public final class SerialComUSBHotPlugInfo {

	private int index = -1;
	private ISerialComUSBHotPlugListener mSerialComUSBHotPlugListener = null;

	/**
	 * <p>Allocates a new SerialComUSBHotPlugInfo object.</p>
	 * 
	 * @param mSerialComUSBHotPlugListener listener for which info this is stored
	 * @param index array index at which information about native thread is stored for this listener
	 */
	public SerialComUSBHotPlugInfo(ISerialComUSBHotPlugListener mSerialComUSBHotPlugListener, int index) {
		this.mSerialComUSBHotPlugListener = mSerialComUSBHotPlugListener;
		this.index = index;
	}

	/** 
	 * <p>Gives reference to ISerialComUSBHotPlugListener object.</p>
	 * 
	 * @return reference to ISerialComUSBHotPlugListener object
	 */	
	public ISerialComUSBHotPlugListener getSerialComUSBHotPlugListener() {
		return mSerialComUSBHotPlugListener;
	}

	/** 
	 * <p>Gives index of information about this USB hot plug listener.</p>
	 * @return array index of information about USB hot plug listener
	 */
	public int getSerialComUSBHotPlugListenerIndex() {
		return index;
	}
}
