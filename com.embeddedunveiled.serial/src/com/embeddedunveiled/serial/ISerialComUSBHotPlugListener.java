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

package com.embeddedunveiled.serial;

/**
 * <p>The interface ISerialComUSBHotPlugListener need to be implemented by class who wish to get notified 
 * whenever a specified USB device is added to the system or removed from the system.</p>
 * 
 * <p>Application should call registerUSBHotPlugEventListener method in SerialComManager class to register 
 * listener. Whenever an event occurs callback method onUSBHotPlugEvent() gets called containing event that 
 * occurred.</p>
 * 
 * @author Rishi Gupta
 */
public interface ISerialComUSBHotPlugListener {

	/** 
	 * <p>Whenever a USB device is plugged into system or unplugged from system, onUSBHotPlugEvent() 
	 * method will be called by native layer.</p>
	 * 
	 * <p>The event value SerialComUSB.DEV_ADDED indicates USB device has been added to the system. 
	 * The event value SerialComUSB.DEV_REMOVED indicates USB device has been removed from system.</p>
	 * 
	 * @param event integer value indicating whether a USB device was plugged or un-plugged from 
	 *         system.
	 */
	public abstract void onUSBHotPlugEvent(int event);
}
