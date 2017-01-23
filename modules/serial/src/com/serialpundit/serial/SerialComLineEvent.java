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
 * <p>Encapsulate events that happens on serial port control lines. Application can call various
 * methods on an instance of this class to know which event has happen and to get its details.
 * Whenever an event happens, an object of this class containing details about the event is passed to
 * registered listener.</p>
 * 
 * @author Rishi Gupta
 */

public final class SerialComLineEvent {

    private int mOldLineEvent;
    private int mNewLineEvent;
    private int mChanged;

    /**
     * <p>The looper object remembers state of lines and pass both previous and new state.</p>
     * 
     * @param oldLineState previous line state
     * @param newLineState new line state
     */
    public SerialComLineEvent(int oldLineState, int newLineState) {
        mOldLineEvent = oldLineState;
        mNewLineEvent = newLineState;
        mChanged = mOldLineEvent ^ mNewLineEvent;  // XOR old with new state to find the one(s) that changed
    }

    /**
     * <p>Gives the status of CTS (clear to send) control line.
     * Transition from 0 to 1 means line is asserted and vice-versa.</p>
     * 
     * @return 0 if CTS is not changed, 1 if CTS went from 0 to 1, 2 if CTS went from 1 to 0
     */
    public int getCTS() {
        if((mChanged & SerialComManager.CTS) == SerialComManager.CTS) {				// CTS has changed
            if((mNewLineEvent  & SerialComManager.CTS) == SerialComManager.CTS) {
                return 1; 											                // CTS went from 0 to 1
            }else {
                return 2; 											                // CTS went from 1 to 0
            }
        }else {
            return 0;     											                // CTS is not changed
        }
    }

    /**
     * <p>Gives the status of DSR (data set ready) control line.
     * Transition from 0 to 1 means line is asserted and vice-versa.</p>
     * 
     * @return 0 if DSR is not changed, 1 if DSR went from 0 to 1, 2 if DSR went from 1 to 0
     */
    public int getDSR() {
        if((mChanged & SerialComManager.DSR) == SerialComManager.DSR) {				// DSR has changed
            if((mNewLineEvent  & SerialComManager.DSR) == SerialComManager.DSR) {
                return 1; 											               // DSR went from 0 to 1
            }else {
                return 2; 											               // DSR went from 1 to 0
            }
        }else {
            return 0;     											               // DSR is not changed
        }
    }

    /**
     * <p>Gives the status of DCD (data carrier detect) control line.
     * Transition from 0 to 1 means line is asserted and vice-versa.</p>
     * 
     * @return 0 if DCD is not changed, 1 if DCD went from 0 to 1, 2 if DCD went from 1 to 0
     */
    public int getDCD() {
        if((mChanged & SerialComManager.DCD) == SerialComManager.DCD) {			// DCD has changed
            if((mNewLineEvent  & SerialComManager.DCD) == SerialComManager.DCD) {
                return 1; 											                // DCD went from 0 to 1
            }else {
                return 2; 											                // DCD went from 1 to 0
            }
        }else {
            return 0;     											                // DCD is not changed
        }
    }

    /**
     * <p>Gives the status of RI (ring indicator) control line.
     * Transition from 0 to 1 means line is asserted and vice-versa.</p>
     * 
     * @return 0 if RI is not changed, 1 if RI went from 0 to 1, 2 if RI went from 1 to 0
     */
    public int getRI() {
        if((mChanged & SerialComManager.RI) == SerialComManager.RI) {			   // RI has changed
            if((mNewLineEvent  & SerialComManager.RI) == SerialComManager.RI) {
                return 1; 											               // RI went from 0 to 1
            }else {
                return 2; 											               // RI went from 1 to 0
            }
        }else {
            return 0;     											               // RI is not changed
        }
    }
}
