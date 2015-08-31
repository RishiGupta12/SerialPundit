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
 * <p>The interface ISerialComProgressXmodem should be implemented by class who wish to 
 * know how many blocks have been sent or received using xmodem protocol.</p>
 * 
 * <p>The graphical user interface applications may want to show progress for example 
 * using a progress bar to inform user about how much data have been sent to receiver 
 * end. Such applications can use this interface for this purpose.</p>
 * 
 * @author Rishi Gupta
 */
public interface ISerialComProgressXmodem {

	/**
	 * <p>The class implementing this interface is expected to override onXmodemSentProgressUpdate() 
	 * method. This method gets called whenever a block is sent using Xmodem protocol.</p>
	 * 
	 * <p>This method should return as early as possible. Application might schedule GUI update 
	 * for future.</p>
	 * 
	 * @param numBlock number of the block sent by this application till the time this method is 
	 *         called. It includes both newly sent and re-sent blocks i.e. it represent total 
	 *         number of blocks sent from sender to receiver.
	 */
	public abstract void onXmodemSentProgressUpdate(long numBlock);

	/**
	 * <p>The class implementing this interface is expected to override onXmodemReceiveProgressUpdate() 
	 * method. This method gets called whenever a block is sent using Xmodem protocol.</p>
	 * 
	 * <p>This method should return as early as possible. Application might schedule GUI update 
	 * for future.</p>
	 * 
	 * @param numBlock number of the block received by this application till the time this method 
	 *         is called. It includes both new blocks and resent blocks i.e. it represent total 
	 *         number of blocks received from file sender.
	 */
	public abstract void onXmodemReceiveProgressUpdate(long numBlock);
}
