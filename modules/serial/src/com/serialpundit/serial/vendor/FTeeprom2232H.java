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
 * <p>Represents the C structure 'ft_eeprom_2232h' declared in ftd2xx.h header file.</p>
 * 
 * @author Rishi Gupta
 */
public final class FTeeprom2232H extends FTeepromHeader {

    /* same array is shared between super class and sub class, they extract values 
     * from index applicable to them. */
    private final int[] data2232H;

    /**
     * <p>Construct and allocates a new FTeeprom2232H object with given details.</p>
     * 
     * <p>The sequence of values must be defined in following order: deviceType, VendorId, ProductId, 
     * SerNumEnable, MaxPower, SelfPowered, RemoteWakeup, PullDownEnable, ALSlowSlew, ALSchmittInput, 
     * ALDriveCurrent, AHSlowSlew, AHSchmittInput, AHDriveCurrent, BLSlowSlew, BLSchmittInput, 
     * BLDriveCurrent, BHSlowSlew, BHSchmittInput, BHDriveCurrent, AIsFifo, AIsFifoTar, AIsFastSer, 
     * BIsFifo, BIsFifoTar, BIsFastSer, PowerSaveEnable, ADriverType, BDriverType respectively.</p>
     * 
     * <p>Values should be initialized when instantiating this object for writing purpose. For reading 
     * purpose there is no need to initialize.</p>
     * 
     * <p>If the class is instantiated for writing purpose, the size of data array must be 29 and each 
     * member (value at index) must be initialized to a valid value. Although all members are of 'int 
     * data type' but deviceType must be one of the constants FT_DEVICE_XXXX, VendorId and ProductId must 
     * use lower 16 bit only, rest all the members should be 8 bit value (upper 24 bits must be all 0).</p>
     * 
     * @param data array containing values of member variables for C structure.
     * @throws IllegalArgumentException if data is null or its size is not equal to 29.
     */
    public FTeeprom2232H(int[] data) {
        super(data);
        if(data == null) {
            throw new IllegalArgumentException("Argument data can not be null !");
        }
        if(data.length != 29) {
            throw new IllegalArgumentException("Argument data must be of length 29 !");
        }
        this.data2232H = data;
    }

    /**
     * <p>Retrieves the value of ALSlowSlew variable (drive options) in ft_eeprom_2232h structure.</p>
     * 
     * @return non-zero if AL pins have slow slew.
     */
    public int getALSlowSlew() {
        return data2232H[8];
    }

    /**
     * <p>Retrieves the value of ALSchmittInput variable (drive options) in ft_eeprom_2232h structure.</p>
     * 
     * @return non-zero if AL pins are Schmitt input.
     */
    public int getALSchmittInput() {
        return data2232H[9];
    }

    /**
     * <p>Retrieves the value of ALDriveCurrent variable (drive options) in ft_eeprom_2232h structure.</p>
     * 
     * @return driver current value (valid values are 4mA, 8mA, 12mA, 16mA).
     */
    public int getALDriveCurrent() {
        return data2232H[10];
    }

    /**
     * <p>Retrieves the value of AHSlowSlew variable (drive options) in ft_eeprom_2232h structure.</p>
     * 
     * @return non-zero if AH pins have slow slew.
     */
    public int getAHSlowSlew() {
        return data2232H[11];
    }

    /**
     * <p>Retrieves the value of AHSchmittInput variable (drive options) in ft_eeprom_2232h structure.</p>
     * 
     * @return non-zero if AH pins are Schmitt input.
     */
    public int getAHSchmittInput() {
        return data2232H[12];
    }

    /**
     * <p>Retrieves the value of AHDriveCurrent variable (drive options) in ft_eeprom_2232h structure.</p>
     * 
     * @return driver current value (valid values are 4mA, 8mA, 12mA, 16mA).
     */
    public int getAHDriveCurrent() {
        return data2232H[13];
    }

    /**
     * <p>Retrieves the value of BLSlowSlew variable (drive options) in ft_eeprom_2232h structure.</p>
     * 
     * @return non-zero if BL pins have slow slew.
     */
    public int getBLSlowSlew() {
        return data2232H[14];
    }

    /**
     * <p>Retrieves the value of BLSchmittInput variable (drive options) in ft_eeprom_2232h structure.</p>
     * 
     * @return non-zero if BL pins are Schmitt input.
     */
    public int getBLSchmittInput() {
        return data2232H[15];
    }

    /**
     * <p>Retrieves the value of BLDriveCurrent variable (drive options) in ft_eeprom_2232h structure.</p>
     * 
     * @return driver current value (valid values are 4mA, 8mA, 12mA, 16mA).
     */
    public int getBLDriveCurrent() {
        return data2232H[16];
    }

    /**
     * <p>Retrieves the value of BHSlowSlew variable (drive options) in ft_eeprom_2232h structure.</p>
     * 
     * @return non-zero if BH pins have slow slew.
     */
    public int getBHSlowSlew() {
        return data2232H[17];
    }

    /**
     * <p>Retrieves the value of BHSchmittInput variable (drive options) in ft_eeprom_2232h structure.</p>
     * 
     * @return non-zero if BH pins are Schmitt input.
     */
    public int getBHSchmittInput() {
        return data2232H[18];
    }

    /**
     * <p>Retrieves the value of BHDriveCurrent variable (drive options) in ft_eeprom_2232h structure.</p>
     * 
     * @return driver current value (valid values are 4mA, 8mA, 12mA, 16mA).
     */
    public int getBHDriveCurrent() {
        return data2232H[19];
    }

    /**
     * <p>Retrieves the value of AIsFifo variable (hardware options) in ft_eeprom_2232h structure.</p>
     * 
     * @return non-zero if interface is 245 FIFO.
     */
    public int getAIsFifo() {
        return data2232H[20];
    }

    /**
     * <p>Retrieves the value of AIsFifoTar variable (hardware options) in ft_eeprom_2232h structure.</p>
     * 
     * @return non-zero if interface is 245 FIFO CPU target.
     */
    public int getAIsFifoTar() {
        return data2232H[21];
    }

    /**
     * <p>Retrieves the value of AIsFifo variable (hardware options) in ft_eeprom_2232h structure.</p>
     * 
     * @return non-zero if interface is Fast serial.
     */
    public int getAIsFastSer() {
        return data2232H[22];
    }

    /**
     * <p>Retrieves the value of BIsFifo variable (hardware options) in ft_eeprom_2232h structure.</p>
     * 
     * @return non-zero if interface is 245 FIFO.
     */
    public int getBIsFifo() {
        return data2232H[23];
    }

    /**
     * <p>Retrieves the value of BIsFifoTar variable (hardware options) in ft_eeprom_2232h structure.</p>
     * 
     * @return non-zero if interface is 245 FIFO CPU target.
     */
    public int getBIsFifoTar() {
        return data2232H[24];
    }

    /**
     * <p>Retrieves the value of AIsFifo variable (hardware options) in ft_eeprom_2232h structure.</p>
     * 
     * @return non-zero if interface is Fast serial.
     */
    public int getBIsFastSer() {
        return data2232H[25];
    }

    /**
     * <p>Retrieves the value of PowerSaveEnable variable (hardware options) in ft_eeprom_2232h structure.</p>
     * 
     * @return non-zero if using BCBUS7 to save power for self-powered designs.
     */
    public int getPowerSaveEnable() {
        return data2232H[26];
    }

    /**
     * <p>Retrieves the value of ADriverType variable (driver options) in ft_eeprom_2232h structure.</p>
     * 
     * @return driver type.
     */
    public int getADriverType() {
        return data2232H[27];
    }

    /**
     * <p>Retrieves the value of BDriverType variable (driver options) in ft_eeprom_2232h structure.</p>
     * 
     * @return driver type.
     */
    public int getBDriverType() {
        return data2232H[28];
    }

    /**
     * <p>Retrieves all the value defined for ft_eeprom_header and ft_eeprom_2232h structures.</p>
     * 
     * @return array of member's values.
     */
    public int[] getAllMembers() {
        return data2232H;
    }
}
