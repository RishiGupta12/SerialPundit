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

package com.serialpundit.hid;

/**
 * <p>The interface IHIDInputReportListener should be implemented by class who wish to 
 * receive input reports from HID device whenever an input report is received by host computer.</p>
 * 
 * @author Rishi Gupta
 */
public interface IHIDInputReportListener {

    /**
     * <p> This method gets called whenever an input report is received by host computer.
     * 
     * <p>The class implementing this interface is expected to override onNewInputReportAvailable() method. 
     * This method gets called from a Java worker thread.</p>
     * 
     * <p>The argument numOfBytesRead contains the size of input report and reportBuffer contains input 
     * report read from device. If the device uses numbered reports, first byte in reportBuffer array will be 
     * report number. If the device does not uses numbered reports, first byte in reportBuffer will be beginning 
     * of data itself. The reportBuffer reference is same as what was passed when registering this listener.</p>
     * 
     * @param numOfBytesRead number of bytes read from HID device as input report.
     * @param reportBuffer byte array that will contain data read from HID device.
     */
    public abstract void onNewInputReportAvailable(int numOfBytesRead, byte[] reportBuffer);

    /***
     * <p>If any exception occurs while worker thread was waiting for new input report or while reading it, worker 
     * thread calls this method passing the error information.</p>
     * 
     * @param e exception instance.
     */
    public abstract void onNewInputReportAvailableError(Exception e);
}
