/*
 * This file is part of SerialPundit.
 * 
 * Copyright (C) 2014-2016, Rishi Gupta. All rights reserved.
 *
 * The SerialPundit is DUAL LICENSED. It is made available under the terms of the GNU Affero 
 * General Public License (AGPL) v3.0 for non-commercial use and under the terms of a commercial 
 * license for commercial use of this software. 
 * 
 * The SerialPundit is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.serialpundit.serial;

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
