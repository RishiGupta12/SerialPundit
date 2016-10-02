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

package com.serialpundit.hid.internal;

/**
 * <p>Acts as a medium between threads to indicate whether input report reader worker thread should 
 * exit or not.</p>
 * 
 * @author Rishi Gupta
 */
public final class InputReportListenerState {

    private volatile boolean exitInputReportListenerFlag;

    /** 
     * <p>Allocates a new InputReportListenerState with state as false.</p>
     */ 
    public InputReportListenerState() {
        exitInputReportListenerFlag = false;
    }

    /** 
     * <p>Sets the state of input report listener worker thread to the given state.</p>
     */ 
    public void setInputReportListenerState(boolean state) {
        exitInputReportListenerFlag = state;
    }

    /** 
     * <p>Tells whether worker thread should continue to listen for input report availability or not.</p>
     * 
     * @return true if the thread should exit false otherwise.
     */ 
    public boolean shouldInputReportListenerExit() {
        return exitInputReportListenerFlag;
    }
}
