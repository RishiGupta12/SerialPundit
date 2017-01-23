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

package com.serialpundit.serial.nullmodem;

/**
 * <p>The interface Itty2comHotPlugListener should be implemented by class who wish to get notified 
 * whenever a virtual device is added to the system or removed from the system by null modem driver.</p>
 * 
 * <p>Application should call registerTTY2COMHotPlugEventListener method in SerialComNullModem class to register 
 * listener. Whenever an event occurs callback method onTTY2COMHotPlugEvent() gets called containing event that 
 * occurred.</p>
 * 
 * @author Rishi Gupta
 */
public interface Itty2comHotPlugListener {

    /** 
     * <p>Whenever a virtual serial device is plugged into system or unplugged from system,onTTY2COMHotPlugEvent() 
     * method will be called by native layer.</p>
     * 
     * <p>The event value SerialComNullModem.DEV_ADDED indicates virtual serial device has been added to the system. 
     * The event value SerialComNullModem.DEV_REMOVED indicates virtual serial device has been removed from system.</p>
     * 
     * @param event integer value indicating whether a virtual serial device was plugged or un-plugged from system.
     * @param comPort device node of virtual serial device for which this method is invoked.
     */
    public abstract void onTTY2COMHotPlugEvent(int event, final String comPort);
}
