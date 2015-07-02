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
 * <p>This limit the scope of exceptions in context of serial operation only.</p>
 */
public final class SerialComException extends IOException {

	private static final long serialVersionUID = -2454774155396601296L;
	private String portName;
	private String methodName;
	private String exceptionMsg;

	/**
     * Constructs an SerialComException object with the specified detail message.
     *
     * @param methodName name of method where exception occurred
     * @param exceptionMsg message describing reason for exception
     */
	public SerialComException(String methodName, String exceptionMsg) {
		super(exceptionMsg + " in method " + methodName);
		this.methodName = methodName;
		this.exceptionMsg = exceptionMsg;
	}

	/**
     * Constructs an SerialComException object with the specified detail message.
     *
     * @param portName name of the port on which this exception occurred
     * @param methodName name of method where exception occurred
     * @param exceptionMsg message describing reason for exception
     */
	public SerialComException(String portName, String methodName, String exceptionMsg) {
		super(exceptionMsg + " in method " + methodName + " for port " + portName);
		this.portName = portName;
		this.methodName = methodName;
		this.exceptionMsg = exceptionMsg;
	}

	/** 
	 * <p>Get port in use on which this exception occurred. </p>
	 * @return portName serial port identifier
	 */
	public String getPortName() {
		return portName;
	}

	/** 
	 * <p>Get method name during execution of which the exception occurred. </p>
	 * @return method which had thrown this exception
	 */
	public String getMethodName() {
		return methodName;
	}

	/** 
	 * <p>Get the specific type of exception. </p>
	 * @return exceptionMsg reason for exception
	 */
	public String getExceptionMsg() {
		return exceptionMsg;
	}
}
