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

package com.embeddedunveiled.serial;

/**
 * <p>The interface ISerialComDataListener should be implemented by class who wish to 
 * receive data from serial port.</p>
 * 
 * @author Rishi Gupta
 */
public interface ISerialComDataListener {

	/**
	 * <p> This method is called whenever data is received on serial port.</p>
	 * 
	 * <p>The class implementing this interface is expected to override onNewSerialDataAvailable() method.
	 * This method gets called from the looper thread associated with the corresponding listener (handler).</p>
	 * 
	 * <p>Application may tune the behavior by using fineTuneReadBehaviour() API. </p>
	 * 
	 * @param data bytes read from serial port.
	 */
	public abstract void onNewSerialDataAvailable(byte[] data);

	/**
	 * <p> This method is called whenever an error occurred the data listener mechanism.</p>
	 * 
	 * <p>This methods helps in creating fault-tolerant and recoverable application design in case
	 * unexpected situations like serial port removal, bug encountered in OS or driver during operation
	 * occurs. In a nutshell situations which are outside the scope of scm may be handled using this method.</p>
	 * 
	 * <p>Developer can implement different recovery policies like unregister listener, close com port
	 * and then open and register listener again. Another policy might be to send email to system 
	 * administrator so that he can take appropriate actions to recover from situation.</p>
	 * 
	 * <p>Swing/AWT GUI applications might play beep sound to inform user about port addition or removal.</p>
	 * 
	 * @param errorNum operating system specific error number
	 */
	public abstract void onDataListenerError(int errorNum);
}
