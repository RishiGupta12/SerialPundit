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

import com.serialpundit.hid.IHIDInputReportListener;

/**
 * <p>Encapsulates the information like HID device handle and listeners associated with this handle etc.</p>
 * 
 * @author Rishi Gupta
 */
public final class HIDdevHandleInfo {

    private InputReportListenerState irls;
    private IHIDInputReportListener mInputReportListener = null;
    private long context;

    /**
     * <p>Allocates and create new HIDdevHandleInfo object with given details.</p>
     * 
     * @param mInputReportListener instance of input report listener.
     */
    public HIDdevHandleInfo(IHIDInputReportListener mInputReportListener) {
        this.mInputReportListener = mInputReportListener;
    }

    /** 
     * <p>Gives input report listener associated with given HID device handle.</p>
     * 
     * @return input report listener who will get input reports for given handle.
     */	
    public IHIDInputReportListener getInputReportListener() {
        return mInputReportListener;
    }

    /** <p>Set the input report listener who will get input reports for given handle.</p>
     * 
     * @param mInputReportListener input report listener who will get input reports for given handle.
     */
    public void setInputReportListener(IHIDInputReportListener mInputReportListener) {
        this.mInputReportListener = mInputReportListener;
    }

    /** 
     * <p>Gives the context associated with this input report listener.</p>
     * 
     * @return context associated with this input report listener.
     */	
    public long getListenerContext() {
        return context;
    }

    /** <p>Set the context associated with this input report listener.</p>
     * 
     * @param context context associated with this input report listener.
     */
    public void setListenerContext(long context) {
        this.context = context;
    }

    /** <p>Set the state of worker thread's state instance associated with this input report listener.</p>
     * 
     * @param irls instance of InputReportListenerState tied to this listener.
     */
    public void setInputReportListenerStateInstance(InputReportListenerState irls) {
        this.irls = irls;
    }

    /** 
     * <p>Gives worker thread's state instance associated with this input report listener.</p>
     * 
     * @return worker thread's state instance.
     */ 
    public InputReportListenerState getInputReportListenerStateInstance() {
        return irls;
    }
}
