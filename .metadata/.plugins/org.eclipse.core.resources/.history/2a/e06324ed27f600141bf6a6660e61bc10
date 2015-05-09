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
 * <p>This interface represents Completion handler in our proactor design pattern.</p>
 */
public interface ISerialComEventListener {
	
	/**
	 * <p>The class implementing this interface is expected to override onNewSerialEvent() method.
	 * This method gets called from the looper thread associated with the corresponding listener (handler).
	 * The listener can extract detailed information about event from the event object passed by calling
	 * various methods on the event object.</p>
	 */
	public abstract void onNewSerialEvent(SerialComLineEvent lineEvent);
	
}
