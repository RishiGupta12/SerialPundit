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

import java.io.IOException;

/** 
 * <p>This exception is thrown if native shared library extraction sub-system encounter any error. 
 * This limit the scope of exceptions in context of serial operation only. </p>
 * 
 * @author Rishi Gupta
 */
public final class SerialComLoadException extends IOException {

	private static final long serialVersionUID = 505443460439405812L;

	/**
     * <p>Constructs an SerialComLoadException object with the specified detail message.</p>
     *
     * @param exceptionMsg message describing reason for this exception.
     */
	public SerialComLoadException(String exceptionMsg) {
		super(exceptionMsg);
	}
}
