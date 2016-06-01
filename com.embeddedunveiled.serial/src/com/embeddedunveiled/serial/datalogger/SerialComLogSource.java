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

package com.embeddedunveiled.serial.datalogger;

import com.embeddedunveiled.serial.SerialComException;
import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.SerialComManager.BAUDRATE;
import com.embeddedunveiled.serial.SerialComManager.DATABITS;
import com.embeddedunveiled.serial.SerialComManager.FLOWCONTROL;
import com.embeddedunveiled.serial.SerialComManager.PARITY;
import com.embeddedunveiled.serial.SerialComManager.STOPBITS;

/**
 * 
 * @author Rishi Gupta
 */
public final class SerialComLogSource {

    private final SerialComManager scm;
    private final int readingStyle;
    private final int pollingFrequency;
    private long comPortHandle;

    /** <p>The value specifying that the serial port reader thread should poll serial port with given 
     * period in non-blocking mode. Integer constant with value 0x01.</p>*/
    public static final int NONBLOCKING_PERIODIC = 0x01;

    /** <p>The value specifying that the serial port reader thread should block until data is available 
     * at serial  port. Integer constant with value 0x02.</p>*/
    public static final int BLOCKING = 0x02;

    /** <p>The value specifying that the asynchronous data listener mechanism of this library should be 
     * used to read data from serial port. Integer constant with value 0x03.</p>*/
    public static final int DATALISTENER = 0x03;

    public SerialComLogSource(SerialComManager scm, int readingStyle, int pollingFrequency) {
        this.scm = scm;
        if((readingStyle < 0x01) || (readingStyle > 0x03)) {
            throw new IllegalArgumentException("Argument readingStyle is invalid !");
        }
        this.readingStyle = readingStyle;
        this.pollingFrequency = pollingFrequency;
    }

    /**
     * 
     * @param comPort
     * @param dataBits
     * @param stopBits
     * @param baudRate
     * @param parity
     * @param flowctrl
     * @return true on success.
     * @throws SerialComException if opening or configuring serial port fails due to some reason.
     */
    public boolean createComPortSource(String comPort, DATABITS dataBits, STOPBITS stopBits, BAUDRATE baudRate, 
            PARITY parity, FLOWCONTROL flowctrl) throws SerialComException {

        comPortHandle = scm.openComPort(comPort, true, true, true);
        scm.configureComPortData(comPortHandle, dataBits, stopBits, parity, baudRate, 0);
        scm.configureComPortControl(comPortHandle, flowctrl, (char)0x11, (char)0x13, false, false);

        return true;
    }

    /**
     * 
     * @return
     * @throws SerialComException if closing the serial port fails.
     */
    public boolean destroyComPortSource() throws SerialComException {
        scm.closeComPort(comPortHandle);
        return true;
    }
}
