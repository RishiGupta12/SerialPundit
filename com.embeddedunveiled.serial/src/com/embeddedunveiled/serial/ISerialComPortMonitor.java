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
 * <p>This interface need to be implemented by class who wants to monitor serial port.</p>
 */
public interface ISerialComPortMonitor {

	/** 
	 * <p>Whenever a serial device is plugged or unplugged from system, onPortMonitorEvent() method will
	 * be called by native layer.</p>
	 * 
	 * <p>The event 2 indicates port removal, 1 indicates additional of port.</p>
	 * 
	 * <p>Note that port removal event indicates that the port for which this monitor was registered has
	 * been removed physically from system. However, port addition event is fired every time a serial port
	 * is plugged into system.</p>
	 */
	public abstract void onPortMonitorEvent(int event);

}