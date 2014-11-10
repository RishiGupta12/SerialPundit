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

/* This limit the scope of exceptions in context of serial operation only. */
public final class SerialComException extends Exception {

	private static final long serialVersionUID = -2454774155396601296L;
    private String portName;
    private String methodName;
    private String exceptionType;

    public SerialComException(String methodName, String exceptionType){
        super(exceptionType + " in method " + methodName);
        this.methodName = methodName;
        this.exceptionType = exceptionType;
    }
    
    public SerialComException(String portName, String methodName, String exceptionType){
        super(exceptionType + " in method " + methodName + " for port " + portName);
        this.portName = portName;
        this.methodName = methodName;
        this.exceptionType = exceptionType;
    }

    /* Get port in use on which this exception occurred. */
    public String getPortName(){
        return portName;
    }

    /* Get method name during execution of which the exception occurred. */
    public String getMethodName(){
        return methodName;
    }

    /* Get the specific type of exception. */
    public String getExceptionType(){
        return exceptionType;
    }
}
