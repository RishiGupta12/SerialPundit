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

/**
 * <p>The interface IHIDInputReportListener should be implemented by class who wish to 
 * receive input reports from HID device whenever an input report is received by host computer.</p>
 * 
 * @author Rishi Gupta
 */
public interface IHIDInputReportListener {

	/**
	 * <p> This method gets called whenever an input report is received by host computer.
	 * 
	 * <p>The class implementing this interface is expected to override onNewInputReportAvailable() method. 
	 * This method gets called from a Java worker thread.</p>
	 * 
	 * <p>The argument numOfBytesRead contains the size of input report and reportBuffer contains input 
	 * report read from device. If the device uses numbered reports, first byte in reportBuffer array will be 
	 * report number. If the device does not uses numbered reports, first byte in reportBuffer will be beginning 
	 * of data itself. The reportBuffer reference is same as what was passed when registering this listener.</p>
	 * 
	 * @param numOfBytesRead number of bytes read from HID device as input report.
	 * @param reportBuffer byte array that will contain data read from HID device.
	 */
	public abstract void onNewInputReportAvailable(int numOfBytesRead, byte[] reportBuffer);
}
