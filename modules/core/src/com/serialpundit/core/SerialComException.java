/*
 * This file is part of SerialPundit.
 * 
 * Copyright (C) 2014-2018, Rishi Gupta. All rights reserved.
 *
 * The SerialPundit is DUAL LICENSED. It is made available under the terms of the GNU Affero 
 * General Public License (AGPL) v3.0 for non-commercial use and under the terms of a commercial 
 * license for commercial use of this software. 
 * 
 * The SerialPundit is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.serialpundit.core;

import java.io.IOException;

/** 
 * <p>Limit the scope of exceptions in context of SerialPundit only.</p>
 * 
 * @author Rishi Gupta
 */
public final class SerialComException extends IOException {

    private static final long serialVersionUID = -7712581908221865522L;
    private String exceptionMsg;

    /**
     * <p>Constructs and allocate a new SerialComException object with the given details.</p>
     * 
     * @param exceptionMsg message describing reason for exception.
     */
    public SerialComException(String exceptionMsg) {
        super(exceptionMsg);
        this.exceptionMsg = exceptionMsg;
    }

    /** 
     * <p>Get the specific type of exception. </p>
     * 
     * @return reason for exception.
     */
    public String getMessage() {
        return exceptionMsg;
    }

    /** 
     * <p>Get the specific type of exception. </p>
     * 
     * @return reason for exception.
     */
    public String getExceptionMsg() {
        return exceptionMsg;
    }
}
