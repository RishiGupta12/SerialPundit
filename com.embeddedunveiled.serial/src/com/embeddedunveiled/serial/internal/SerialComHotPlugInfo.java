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

import com.embeddedunveiled.serial.ISerialComHotPlugListener;

/**
 * <p>Encapsulates the information associated with hot plug listener and its native part.</p>
 */
public final class SerialComHotPlugInfo {
	
	private int index = -1;
	private ISerialComHotPlugListener mSerialComHotPlugListener = null;

	/**
	 * <p>Allocates a new SerialComHotPlugInfo object.</p>
	 * 
	 * @param mSerialComHotPlugListener listener for which info this is stored
	 * @param index array index at which information about native thread is stored for this listener
	 */
	public SerialComHotPlugInfo(ISerialComHotPlugListener mSerialComHotPlugListener, int index) {
		this.mSerialComHotPlugListener = mSerialComHotPlugListener;
		this.index = index;
	}
	
	/** 
	 * <p>Gives reference to ISerialComHotPlugListener object.</p>
	 * @return reference to ISerialComHotPlugListener object
	 */	
	public ISerialComHotPlugListener getSerialComHotPlugListener() {
		return mSerialComHotPlugListener;
	}

	/** 
	 * <p>Gives index of information about this hot plug listener.</p>
	 * @return array index of information about hot plug listener
	 */
	public int getSerialComHotPlugListenerIndex() {
		return index;
	}

}
