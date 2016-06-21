/*
 * This file is part of SerialPundit project and software.
 * 
 * Copyright (C) 2014-2016, Rishi Gupta. All rights reserved.
 *
 * The SerialPundit software is DUAL licensed. It is made available under the terms of the GNU Affero 
 * General Public License (AGPL) v3.0 for non-commercial use and under the terms of a commercial 
 * license for commercial use of this software. 
 * 
 * The SerialPundit software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.serialpundit.serial;

/**
 * <p>The interface ISerialComEventListener should be implemented by class who wish to 
 * receive modem/line events on serial port.</p>
 * 
 * @author Rishi Gupta
 */
public interface ISerialComEventListener {

    /**
     * <p>The class implementing this interface is expected to override onNewSerialEvent() method.
     * This method gets called from the looper thread associated with the corresponding listener (handler).
     * The listener can extract detailed information about event from the event object passed by calling
     * various methods on the event object.</p>
     * 
     * @param lineEvent event object containing bit mask of events on serial port control lines
     */
    public abstract void onNewSerialEvent(SerialComLineEvent lineEvent);
}
