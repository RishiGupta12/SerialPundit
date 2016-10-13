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

package com.serialpundit.core;

import java.io.IOException;

/** 
 * <p>Exception thrown when a blocking operation times out. 
 * This limit the scope of exceptions in context of serial operation only.</p>
 * 
 * @author Rishi Gupta
 */
public final class SerialComTimeOutException extends IOException {

    private static final long serialVersionUID = -641684462902085593L;

    /**
     * <p>Constructs an SerialComTimeOutException object with the specified detail message.</p>
     *
     * @param exceptionMsg message describing reason for exception.
     */
    public SerialComTimeOutException(String exceptionMsg) {
        super(exceptionMsg);
    }
}
