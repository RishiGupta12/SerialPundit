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

package com.serialpundit.serial.ftp;

import com.serialpundit.serial.internal.ISerialComFTPProgress;

/**
 * <p>The interface ISerialComYmodemProgress should be implemented by class who wish to 
 * know how many blocks have been sent or received using Ymodem protocol.</p>
 * 
 * <p>The graphical user interface applications may want to show progress for example 
 * using a progress bar to inform user about how much data have been sent to receiver 
 * end. Such applications can use this interface for this purpose.</p>
 * 
 * @author Rishi Gupta
 */
public interface ISerialComYmodemProgress extends ISerialComFTPProgress {

    /**
     * <p>The class implementing this interface is expected to override onYmodemSentProgressUpdate() 
     * method. This method gets called whenever a block is sent using Ymodem protocol.</p>
     * 
     * <p>This method should return as early as possible. Application might schedule GUI update 
     * for future.</p>
     * 
     * @param fileName name of file that is currently getting sent.
     * @param numBlock number of the block sent by this application till the time this method is 
     *         called. It includes both newly sent and re-sent blocks i.e. it represent total 
     *         number of blocks sent from sender to receiver.
     * @param percentOfBlocksSent update in terms of percentage.
     */
    public abstract void onYmodemSentProgressUpdate(String fileName, long numBlock, int percentOfBlocksSent);

    /**
     * <p>The class implementing this interface is expected to override onYmodemReceiveProgressUpdate() 
     * method. This method gets called whenever a block is sent using Ymodem protocol.</p>
     * 
     * <p>This method should return as early as possible. Application might schedule GUI update 
     * for future.</p>
     * 
     * @param fileName name of file currently being received from sender.
     * @param numBlock number of the block received by this application till the time this method 
     *         is called. It includes both new blocks and resent blocks i.e. it represent total 
     *         number of blocks received from file sender.
     * @param percentOfBlocksReceived update in terms of percentage.
     */
    public abstract void onYmodemReceiveProgressUpdate(String fileName, long numBlock, int percentOfBlocksReceived);
}
