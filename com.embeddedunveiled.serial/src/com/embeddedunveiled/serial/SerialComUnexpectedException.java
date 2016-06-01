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

package com.embeddedunveiled.serial;

import java.io.IOException;

/** 
 * <p>Exception thrown in situation which was not supposed to happen. 
 * This limit the scope of exceptions in context of serial operation only.</p>
 * 
 * @author Rishi Gupta
 */
public final class SerialComUnexpectedException extends IOException {


    private static final long serialVersionUID = 3053241529497992367L;

    /**
     * <p>Constructs and allocate a SerialComUnexpectedException object with the specified details.</p>
     *
     * @param exceptionMsg message describing reason for exception.
     */
    public SerialComUnexpectedException(String exceptionMsg) {
        super(exceptionMsg);
    }
}
