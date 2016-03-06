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
 * <p>Acts as a messenger between application and SCM library to specify 
 * whether sending/receiving file should continue or be aborted.</p>
 * 
 * @author Rishi Gupta
 */
public final class SerialComXModemAbort {

	private boolean abortTransferNow;

	/**
	 * <p>Allocates a new SerialComXModemAbort object.</p>
	 */
	public SerialComXModemAbort() {
		abortTransferNow = false; // initial state.
	}

	/** 
	 * <p>Instructs SCM library to stop sending file if called by file sender,
	 *  or to stop receiving file if called by file receiver using xmodem or 
	 *  its variant protocols.</p>
	 */
	public void abortTransfer() {
		abortTransferNow = true;
	}

	/** 
	 * <p>Checks whether file transfer or reception should be aborted or not.</p>
	 * 
	 * @return true if it should be aborted otherwise false if file transfer 
	 *          should continue.
	 */
	public boolean isTransferToBeAborted() {
		return abortTransferNow;
	}
}
