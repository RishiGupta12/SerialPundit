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

package com.embeddedunveiled.serial.ftp;

/** 
 * <p>Acts as a messenger between application and this library to specify 
 * whether sending/receiving file should continue or be aborted.</p>
 * 
 * @author Rishi Gupta
 */
public final class SerialComFTPCMDAbort {

    private volatile boolean abortTransferNow;

    /**
     * <p>Allocates a new SerialComFTPCMDAbort object with initial state as continue to 
     * transfer.</p>
     */
    public SerialComFTPCMDAbort() {
        abortTransferNow = false; // initial state.
    }

    /** 
     * <p>Instructs this library to stop sending file if called by file sender,
     *  or to stop receiving file if called by file receiver using Xmodem or 
     *  its variant protocols.</p>
     */
    public void abortTransfer() {
        abortTransferNow = true;
    }

    /** 
     * <p>Checks whether file transfer or reception should be aborted or not.</p>
     * 
     * @return true if it should be aborted otherwise false if file transfer 
     *          should continue.
     */
    public boolean isTransferToBeAborted() {
        return abortTransferNow;
    }
}
