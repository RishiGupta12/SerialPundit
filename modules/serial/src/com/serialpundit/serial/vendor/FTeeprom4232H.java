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
 * <p>Represents the C structure 'ft_eeprom_4232h' declared in ftd2xx.h header file.</p>
 * 
 * @author Rishi Gupta
 */
public final class FTeeprom4232H extends FTeepromHeader {

    /* same array is shared between super class and sub class, they extract values 
     * from index applicable to them. */
    private final int[] data4232H;

    /**
     * <p>Construct and allocates a new FTeeprom2232H object with given details.</p>
     * 
     * <p>The sequence of values must be defined in following order: deviceType, VendorId, ProductId, 
     * SerNumEnable, MaxPower, SelfPowered, RemoteWakeup, PullDownEnable, ASlowSlew, ASchmittInput,
     * ADriveCurrent, BSlowSlew, BSchmittInput, BDriveCurrent, CSlowSlew, CSchmittInput, CDriveCurrent, 
     * DSlowSlew, DSchmittInput, DDriveCurrent, ARIIsTXDEN, BRIIsTXDEN, CRIIsTXDEN, DRIIsTXDEN, 
     * ADriverType, BDriverType, CDriverType, DDriverType respectively.</p>
     * 
     * <p>Values should be initialized when instantiating this object for writing purpose. For reading 
     * purpose there is no need to initialize.</p>
     * 
     * <p>If the class is instantiated for writing purpose, the size of data array must be 28 and each 
     * member (value at index) must be initialized to a valid value. Although all members are of 'int 
     * data type' but deviceType must be one of the constants FT_DEVICE_XXXX, VendorId and ProductId must 
     * use lower 16 bit only, rest all the members should be 8 bit value (upper 24 bits must be all 0).</p>
     * 
     * @param data array containing values of member variables for C structure.
     * @throws IllegalArgumentException if data is null or its size is not 28.
     */
    public FTeeprom4232H(int[] data) {
        super(data);
        if(data == null) {
            throw new IllegalArgumentException("Argument data can not be null !");
        }
        if(data.length != 28) {
            throw new IllegalArgumentException("Argument data must be of length 28 !");
        }
        this.data4232H = data;
    }

    /**
     * <p>Retrieves the value of ASlowSlew variable (drive options) in ft_eeprom_4232h structure.</p>
     * 
     * @return non-zero if A pins have slow slew.
     */
    public int getASlowSlew() {
        return data4232H[8];
    }

    /**
     * <p>Retrieves the value of ASchmittInput variable (drive options) in ft_eeprom_4232h structure.</p>
     * 
     * @return non-zero if A pins are Schmitt input.
     */
    public int getASchmittInput() {
        return data4232H[9];
    }

    /**
     * <p>Retrieves the value of ADriveCurrent variable (drive options) in ft_eeprom_4232h structure.</p>
     * 
     * @return driver current value (valid values are 4mA, 8mA, 12mA, 16mA).
     */
    public int getADriveCurrent() {
        return data4232H[10];
    }

    /**
     * <p>Retrieves the value of BSlowSlew variable (drive options) in ft_eeprom_4232h structure.</p>
     * 
     * @return non-zero if B pins have slow slew.
     */
    public int getBSlowSlew() {
        return data4232H[11];
    }

    /**
     * <p>Retrieves the value of BSchmittInput variable (drive options) in ft_eeprom_4232h structure.</p>
     * 
     * @return non-zero if B pins are Schmitt input.
     */
    public int getBSchmittInput() {
        return data4232H[12];
    }

    /**
     * <p>Retrieves the value of BDriveCurrent variable (drive options) in ft_eeprom_4232h structure.</p>
     * 
     * @return driver current value (valid values are 4mA, 8mA, 12mA, 16mA).
     */
    public int getBDriveCurrent() {
        return data4232H[13];
    }

    /**
     * <p>Retrieves the value of CSlowSlew variable (drive options) in ft_eeprom_4232h structure.</p>
     * 
     * @return non-zero if C pins have slow slew.
     */
    public int getCSlowSlew() {
        return data4232H[14];
    }

    /**
     * <p>Retrieves the value of CSchmittInput variable (drive options) in ft_eeprom_4232h structure.</p>
     * 
     * @return non-zero if C pins are Schmitt input.
     */
    public int getCSchmittInput() {
        return data4232H[15];
    }

    /**
     * <p>Retrieves the value of CDriveCurrent variable (drive options) in ft_eeprom_4232h structure.</p>
     * 
     * @return driver current value (valid values are 4mA, 8mA, 12mA, 16mA).
     */
    public int getCDriveCurrent() {
        return data4232H[16];
    }

    /**
     * <p>Retrieves the value of DSlowSlew variable (drive options) in ft_eeprom_4232h structure.</p>
     * 
     * @return non-zero if D pins have slow slew.
     */
    public int getDSlowSlew() {
        return data4232H[17];
    }

    /**
     * <p>Retrieves the value of DSchmittInput variable (drive options) in ft_eeprom_4232h structure.</p>
     * 
     * @return non-zero if D pins are Schmitt input.
     */
    public int getDSchmittInput() {
        return data4232H[18];
    }

    /**
     * <p>Retrieves the value of DDriveCurrent variable (drive options) in ft_eeprom_4232h structure.</p>
     * 
     * @return driver current value (valid values are 4mA, 8mA, 12mA, 16mA).
     */
    public int getDDriveCurrent() {
        return data4232H[19];
    }

    /**
     * <p>Retrieves the value of ARIIsTXDEN variable (hardware options) in ft_eeprom_4232h structure.</p>
     * 
     * @return non-zero if port A uses RI as RS485 TXDEN.
     */
    public int getARIIsTXDEN() {
        return data4232H[20];
    }

    /**
     * <p>Retrieves the value of BRIIsTXDEN variable (hardware options) in ft_eeprom_4232h structure.</p>
     * 
     * @return non-zero if port B uses RI as RS485 TXDEN.
     */
    public int getBRIIsTXDEN() {
        return data4232H[21];
    }

    /**
     * <p>Retrieves the value of CRIIsTXDEN variable (hardware options) in ft_eeprom_4232h structure.</p>
     * 
     * @return non-zero if port C uses RI as RS485 TXDEN.
     */
    public int getCRIIsTXDEN() {
        return data4232H[22];
    }

    /**
     * <p>Retrieves the value of DRIIsTXDEN variable (hardware options) in ft_eeprom_4232h structure.</p>
     * 
     * @return non-zero if port D uses RI as RS485 TXDEN.
     */
    public int getDRIIsTXDEN() {
        return data4232H[23];
    }

    /**
     * <p>Retrieves the value of ADriverType variable (driver options) in ft_eeprom_4232h structure.</p>
     * 
     * @return driver type.
     */
    public int getADriverType() {
        return data4232H[24];
    }

    /**
     * <p>Retrieves the value of BDriverType variable (driver options) in ft_eeprom_4232h structure.</p>
     * 
     * @return driver type.
     */
    public int getBDriverType() {
        return data4232H[25];
    }

    /**
     * <p>Retrieves the value of CDriverType variable (driver options) in ft_eeprom_4232h structure.</p>
     * 
     * @return driver type.
     */
    public int getCDriverType() {
        return data4232H[26];
    }

    /**
     * <p>Retrieves the value of DDriverType variable (driver options) in ft_eeprom_4232h structure.</p>
     * 
     * @return driver type.
     */
    public int getDDriverType() {
        return data4232H[27];
    }

    /**
     * <p>Retrieves all the value defined for ft_eeprom_header and ft_eeprom_232h structures.</p>
     * 
     * @return array of member's values.
     */
    public int[] getAllMembers() {
        return data4232H;
    }
}
