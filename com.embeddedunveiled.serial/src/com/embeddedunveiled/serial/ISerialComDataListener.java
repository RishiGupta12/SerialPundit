/*
 * Author : Rishi Gupta
 * Email  : gupt21@gmail.com
 * 
 * This file is part of 'serial communication manager' library.
 *
 * 'serial communication manager' is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * 'serial communication manager' is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with serial communication manager. If not, see <http://www.gnu.org/licenses/>.
 */

package com.embeddedunveiled.serial;

/**
 * This interface represents Completion handler in our proactor design pattern.
 * 
 * The class implementing this interface is expected to override onNewSerialEvent() method.
 * This method gets called from the looper thread associated with the corresponding listener (handler).
 * 
 * The listener can extract data bytes from the event object passed by calling getDataBytes() method.
 * 
 * By default, the listener is called for every new byte available. This behaviour can be modified by 
 * using available API.
 *  
 */

public interface ISerialComDataListener {
	public abstract void onNewSerialDataAvailable(SerialComDataEvent dataEvent);
}
