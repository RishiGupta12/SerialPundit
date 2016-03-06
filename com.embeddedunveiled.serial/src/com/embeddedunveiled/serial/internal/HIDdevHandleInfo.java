/*
 * Author : Rishi Gupta
 * 
 * This file is part of 'serial communication manager' library.
 * Copyright (C) <2014-2016>  <Rishi Gupta>
 *
 * This 'serial communication manager' is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by the Free Software 
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * The 'serial communication manager' is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR 
 * A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with 'serial communication manager'.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.embeddedunveiled.serial.internal;

import com.embeddedunveiled.serial.hid.IHIDInputReportListener;

/**
 * <p>Encapsulates the information like HID device handle and listeners associated with this handle etc.</p>
 * 
 * @author Rishi Gupta
 */
public final class HIDdevHandleInfo {

	private IHIDInputReportListener mInputReportListener = null;
	private long context;

	/**
	 * <p>Allocates and create new HIDdevHandleInfo object with given details.</p>
	 * 
	 * @param mInputReportListener instance of input report listener.
	 */
	public HIDdevHandleInfo(IHIDInputReportListener mInputReportListener) {
		this.mInputReportListener = mInputReportListener;
	}

	/** 
	 * <p>Gives input report listener associated with given HID device handle.</p>
	 * 
	 * @return input report listener who will get input reports for given handle.
	 */	
	public IHIDInputReportListener getInputReportListener() {
		return mInputReportListener;
	}

	/** <p>Set the input report listener who will get input reports for given handle.</p>
	 * 
	 * @param mInputReportListener input report listener who will get input reports for given handle.
	 */
	public void setInputReportListener(IHIDInputReportListener mInputReportListener) {
		this.mInputReportListener = mInputReportListener;
	}

	/** 
	 * <p>Gives the context associated with this input report listener.</p>
	 * 
	 * @return context associated with this input report listener.
	 */	
	public long getListenerContext() {
		return context;
	}

	/** <p>Set the context associated with this input report listener.</p>
	 * 
	 * @param context context associated with this input report listener.
	 */
	public void setListenerContext(long context) {
		this.context = context;
	}
}
