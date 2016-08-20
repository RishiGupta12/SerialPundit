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

package com.serialpundit.serial.vendor;

/**
 * <p>Represents the C structure 'ft_eeprom_232r' declared in ftd2xx.h header file.</p>
 * 
 * @author Rishi Gupta
 */
public final class FTeeprom232R extends FTeepromHeader {

    /* same array is shared between super class and sub class, they extract values 
     * from index applicable to them. */
    private final int[] data232R;

    /**
     * <p>Construct and allocates a new FTeeprom232R object with given details.</p>
     * 
     * <p>The sequence of values must be defined in following order: deviceType, VendorId, ProductId, 
     * SerNumEnable, MaxPower, SelfPowered, RemoteWakeup, PullDownEnable, IsHighCurrent, useUseExtOsc, 
     * InvertTXD, InvertRXD, InvertRTS, InvertCTS, InvertDTR, InvertDSR, InvertDCD, InvertRI, Cbus0, 
     * Cbus1, Cbus2, Cbus3, Cbus4, DriverType respectively.</p>
     * 
     * <p>Values should be initialized when instantiating this object for writing purpose. For reading 
     * purpose there is no need to initialize.</p>
     * 
     * <p>If the class is instantiated for writing purpose, the size of data array must be 24 and each 
     * member (value at index) must be initialized to a valid value. Although all members are of 'int 
     * data type' but deviceType must be one of the constants FT_DEVICE_XXXX, VendorId and ProductId must 
     * use lower 16 bit only, rest all the members should be 8 bit value (upper 24 bits must be all 0).</p>
     * 
     * @param data array containing values of member variables for C structure.
     * @throws IllegalArgumentException if data is null or its size is not equal to 24.
     */
    public FTeeprom232R(int[] data) {
        super(data);
        if(data == null) {
            throw new IllegalArgumentException("Argument data can not be null !");
        }
        if(data.length != 24) {
            throw new IllegalArgumentException("Argument data must be of length 24 !");
        }
        this.data232R = data;
    }

    /**
     * <p>Retrieves the value of IsHighCurrent variable (drive options) in ft_eeprom_232r structure.</p>
     * 
     * @return non-zero if interface is high current.
     */
    public int getIsHighCurrent() {
        return data232R[8];
    }

    /**
     * <p>Retrieves the value of UseExtOsc variable (hardware options) in ft_eeprom_232r structure.</p>
     * 
     * @return value indicating whether external oscillator should be used or not.
     */
    public int getUseExtOsc() {
        return data232R[9];
    }

    /**
     * <p>Retrieves the value of InvertTXD variable (hardware options) in ft_eeprom_232r structure.</p>
     * 
     * @return non-zero if invert TXD.
     */
    public int getInvertTXD() {
        return data232R[10];
    }

    /**
     * <p>Retrieves the value of InvertRXD variable (hardware options) in ft_eeprom_232r structure.</p>
     * 
     * @return non-zero if invert RXD.
     */
    public int getInvertRXD() {
        return data232R[11];
    }	

    /**
     * <p>Retrieves the value of InvertRTS variable (hardware options) in ft_eeprom_232r structure.</p>
     * 
     * @return non-zero if invert RTS.
     */
    public int getInvertRTS() {
        return data232R[12];
    }

    /**
     * <p>Retrieves the value of InvertCTS variable (hardware options) in ft_eeprom_232r structure.</p>
     * 
     * @return non-zero if invert CTS.
     */
    public int getInvertCTS() {
        return data232R[13];
    }

    /**
     * <p>Retrieves the value of InvertDTR variable (hardware options) in ft_eeprom_232r structure.</p>
     * 
     * @return non-zero if invert DTR.
     */
    public int getInvertDTR() {
        return data232R[14];
    }

    /**
     * <p>Retrieves the value of InvertDSR variable (hardware options) in ft_eeprom_232r structure.</p>
     * 
     * @return non-zero if invert DSR.
     */
    public int getInvertDSR() {
        return data232R[15];
    }

    /**
     * <p>Retrieves the value of InvertDCD variable (hardware options) in ft_eeprom_232r structure.</p>
     * 
     * @return non-zero if invert DCD.
     */
    public int getInvertDCD() {
        return data232R[16];
    }

    /**
     * <p>Retrieves the value of InvertRI variable (hardware options) in ft_eeprom_232r structure.</p>
     * 
     * @return non-zero if invert RI.
     */
    public int getInvertRI() {
        return data232R[17];
    }

    /**
     * <p>Retrieves the value of Cbus0 variable (hardware options) in ft_eeprom_232r structure.</p>
     * 
     * @return Cbus0 mux control value.
     */
    public int getCbus0() {
        return data232R[18];
    }

    /**
     * <p>Retrieves the value of Cbus1 variable (hardware options) in ft_eeprom_232r structure.</p>
     * 
     * @return Cbus1 mux control value.
     */
    public int getCbus1() {
        return data232R[19];
    }

    /**
     * <p>Retrieves the value of Cbus2 variable (hardware options) in ft_eeprom_232r structure.</p>
     * 
     * @return Cbus2 mux control value.
     */
    public int getCbus2() {
        return data232R[20];
    }

    /**
     * <p>Retrieves the value of Cbus3 variable (hardware options) in ft_eeprom_232r structure.</p>
     * 
     * @return Cbus3 mux control value.
     */
    public int getCbus3() {
        return data232R[21];
    }

    /**
     * <p>Retrieves the value of Cbus4 variable (hardware options) in ft_eeprom_232r structure.</p>
     * 
     * @return Cbus4 mux control value.
     */
    public int getCbus4() {
        return data232R[22];
    }

    /**
     * <p>Retrieves the value of DriverType variable (hardware options) in ft_eeprom_232r structure.</p>
     * 
     * @return driver type.
     */
    public int getDriverType() {
        return data232R[23];
    }

    /**
     * <p>Retrieves all the value defined for ft_eeprom_header and ft_eeprom_232h structures.</p>
     * 
     * @return array of member's values.
     */
    public int[] getAllMembers() {
        return data232R;
    }
}
