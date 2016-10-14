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

package com.serialpundit.usb;

/**
 * <p>The interface ISerialComUSBHotPlugListener need to be implemented by class who wish to get notified 
 * whenever a specified USB device is added to the system or removed from the system.</p>
 * 
 * <p>Application should call registerUSBHotPlugEventListener method in SerialComManager class to register 
 * listener. Whenever an event occurs callback method onUSBHotPlugEvent() gets called containing event that 
 * occurred.</p>
 *
 * <p>It is possible for serialpundit to pass VID/PID/SERIAL of USB devices added even if application does 
 * not mentioned USB-IF VID/PID etc at the time of registering hot plug listener. However this is not done 
 * due to the fact of increasing overall performance of hot plug system. Application should devise its own 
 * method of handling such a scenario.</p>
 * 
 * @author Rishi Gupta
 */
public interface ISerialComUSBHotPlugListener {

    /** 
     * <p>Whenever a USB device is plugged into system or unplugged from system, onUSBHotPlugEvent() 
     * method will be called by native layer.</p>
     * 
     * <p>The event value SerialComUSB.DEV_ADDED indicates USB device has been added to the system. 
     * The event value SerialComUSB.DEV_REMOVED indicates USB device has been removed from system.</p>
     * 
     * <p>USBVID will be 0, if SerialComUSB.DEV_ANY was passed to registerUSBHotPlugEventListener for
     * filterVID argument. USBPID will be 0, if SerialComUSB.DEV_ANY was passed to registerUSBHotPlugEventListener 
     * for filterPID and serialNumber will be empty string if null was passed to registerUSBHotPlugEventListener
     * for serialNumber argument.</p>
     * 
     * @param event integer value indicating whether a USB device was plugged or un-plugged from system.
     * @param USBVID USB-IF vendor id of USB device for which this method is invoked.
     * @param USBPID Product id of USB device for which this method is invoked.
     * @param serialNumber serial number of USB device for which this method is invoked.
     */
    public abstract void onUSBHotPlugEvent(int event, int USBVID, int USBPID, String serialNumber);
}
