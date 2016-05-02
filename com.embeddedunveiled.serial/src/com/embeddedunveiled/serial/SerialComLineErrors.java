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
 * <p>An instance of this class should be passed if the application wish to know whether parity, framing, 
 * overrun etc errors has happened on serial port while receiving data or not. Pass instance of this class 
 * to readBytes() method.</p>
 * 
 * @author Rishi Gupta
 */
public final class SerialComLineErrors {

    /** <p>The value indicating parity error while receiving data at serial port. Integer constant with value 0x01. </p>*/
    public static final int ERR_PARITY  = 0x01;

    /** <p>The value indicating framing error while receiving data at serial port. Integer constant with value 0x02. </p>*/
    public static final int ERR_FRAME   = 0x02;

    /** <p>The value indicating overrun error while receiving data at serial port. Integer constant with value 0x03. </p>*/
    public static final int ERR_OVERRUN = 0x03;

    private int lineError;

    /**
     * <p>Allocate and reset the errors bit mask.</p>
     */
    public SerialComLineErrors() {
        lineError = 0;
    }

    /**
     * <p>Reset the errors bit mask.</p>
     */
    public void resetLineErrors() {
        lineError = 0;
    }

    /**
     * <p>Tells whether parity error has occurred while receiving data at serial port or not.</p>
     * 
     * <p>Linux does not differentiate between framing and parity errors. So if either error occurs 
     * both parity and framing error will be set.</p>
     * 
     * @return true if parity error has occurred otherwise false.
     */
    public boolean hasParityErrorOccurred() {
        return ((lineError & ERR_PARITY) == ERR_PARITY) ? true : false;
    }

    /**
     * <p>Tells whether framing error has occurred while receiving data at serial port or not.</p>
     * 
     * <p>Linux does not differentiate between framing and parity errors. So if either error occurs 
     * both parity and framing error will be set.</p>
     * 
     * @return true if framing error has occurred otherwise false.
     */
    public boolean hasFramingErrorOccurred() {
        return ((lineError & ERR_FRAME) == ERR_FRAME) ? true : false;
    }

    /**
     * <p>Tells whether overrun error has occurred while receiving data at serial port or not.</p>
     * 
     * @return true if overrun error has occurred otherwise false.
     */
    public boolean hasOverrunErrorOccurred() {
        return ((lineError & ERR_OVERRUN) == ERR_OVERRUN) ? true : false;
    }

    /**
     * <p>Tells whether any error has occurred while receiving data at serial port or not. This method can be 
     * used to save time that would have been spent in checking individual errors. Typically applications 
     * discard data read from serial port if any error occurs.</p>
     * 
     * @return true if any error has occurred otherwise false.
     */
    public boolean hasAnyErrorOccurred() {
        return (lineError != 0) ? true : false;
    }
}
