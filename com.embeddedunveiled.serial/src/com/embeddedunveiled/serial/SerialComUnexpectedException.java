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

import java.io.IOException;

/** 
 * <p>This exception is thrown in situation which was not supposed to happen. 
 * This limit the scope of exceptions in context of serial operation only. </p>
 */
public final class SerialComUnexpectedException extends IOException {

	private static final long serialVersionUID = -2454774135396601246L;
	
	/**
     * <p>Constructs an SerialComUnexpectedException object with the specified detail message.</p>
     *
     * @param methodName name of method where exception occurred
     * @param exceptionMsg message describing reason for exception
     */
	public SerialComUnexpectedException(String methodName, String exceptionMsg) {
		super(exceptionMsg + " in method " + methodName);
	}
}
