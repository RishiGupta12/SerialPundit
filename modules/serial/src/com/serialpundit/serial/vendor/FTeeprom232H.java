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
 * <p>Represents the C structure 'ft_eeprom_232h' declared in ftd2xx.h header file.</p>
 * 
 * @author Rishi Gupta
 */
public final class FTeeprom232H extends FTeepromHeader {

    /* same array is shared between super class and sub class, they extract values 
     * from index applicable to them. */
    private final int[] data232H;

    /**
     * <p>Construct and allocates a new FTeeprom232R object with given details.</p>
     * 
     * <p>The sequence of values must be defined in following order: deviceType, VendorId, ProductId, 
     * SerNumEnable, MaxPower, SelfPowered, RemoteWakeup, PullDownEnable, ACSlowSlew, ACSchmittInput, 
     * ACDriveCurrent, ADSlowSlew, ADSchmittInput, ADDriveCurrent, Cbus0, Cbus1, Cbus2, Cbus3, Cbus4,
     * Cbus5, Cbus6, Cbus7, Cbus8, Cbus9, FT1248Cpol, FT1248Lsb, FT1248FlowControl, IsFifo, IsFifoTar,
     * IsFastSer, IsFT1248, PowerSaveEnable, DriverType respectively.</p>
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
     * @throws IllegalArgumentException if data is null or its size is not equal to 33.
     */
    public FTeeprom232H(int[] data) {
        super(data);
        if(data == null) {
            throw new IllegalArgumentException("Argument data can not be null !");
        }
        if(data.length != 33) {
            throw new IllegalArgumentException("Argument data must be of length 33 !");
        }
        this.data232H = data;
    }

    /**
     * <p>Retrieves the value of ACSlowSlew variable (drive options) in ft_eeprom_232h structure.</p>
     * 
     * @return non-zero if AC bus pins have slow slew.
     */
    public int getACSlowSlew() {
        return data232H[8];
    }

    /**
     * <p>Retrieves the value of ACSchmittInput variable (drive options) in ft_eeprom_232h structure.</p>
     * 
     * @return non-zero if AC bus pins are Schmitt input.
     */
    public int getACSchmittInput() {
        return data232H[9];
    }

    /**
     * <p>Retrieves the value of ACDriveCurrent variable (drive options) in ft_eeprom_232h structure.</p>
     * 
     * @return driver current value (valid values are 4mA, 8mA, 12mA, 16mA).
     */
    public int getACDriveCurrent() {
        return data232H[10];
    }

    /**
     * <p>Retrieves the value of ADSlowSlew variable (drive options) in ft_eeprom_232h structure.</p>
     * 
     * @return non-zero if AD bus pins have slow slew.
     */
    public int getADSlowSlew() {
        return data232H[11];
    }

    /**
     * <p>Retrieves the value of ADSchmittInput variable (drive options) in ft_eeprom_232h structure.</p>
     * 
     * @return non-zero if AD bus pins are Schmitt input.
     */
    public int getADSchmittInput() {
        return data232H[12];
    }

    /**
     * <p>Retrieves the value of ADDriveCurrent variable (drive options) in ft_eeprom_232h structure.</p>
     * 
     * @return driver current value (valid values are 4mA, 8mA, 12mA, 16mA).
     */
    public int getADDriveCurrent() {
        return data232H[13];
    }

    /**
     * <p>Retrieves the value of Cbus0 variable (CBUS options) in ft_eeprom_232h structure.</p>
     * 
     * @return Cbus0 mux control value.
     */
    public int getCbus0() {
        return data232H[14];
    }

    /**
     * <p>Retrieves the value of Cbus1 variable (CBUS options) in ft_eeprom_232h structure.</p>
     * 
     * @return Cbus1 mux control value.
     */
    public int getCbus1() {
        return data232H[15];
    }

    /**
     * <p>Retrieves the value of Cbus2 variable (CBUS options) in ft_eeprom_232h structure.</p>
     * 
     * @return Cbus2 mux control value.
     */
    public int getCbus2() {
        return data232H[16];
    }

    /**
     * <p>Retrieves the value of Cbus3 variable (CBUS options) in ft_eeprom_232h structure.</p>
     * 
     * @return Cbus3 mux control value.
     */
    public int getCbus3() {
        return data232H[17];
    }

    /**
     * <p>Retrieves the value of Cbus4 variable (CBUS options) in ft_eeprom_232h structure.</p>
     * 
     * @return Cbus4 mux control value.
     */
    public int getCbus4() {
        return data232H[18];
    }

    /**
     * <p>Retrieves the value of Cbus5 variable (CBUS options) in ft_eeprom_232h structure.</p>
     * 
     * @return Cbus5 mux control value.
     */
    public int getCbus5() {
        return data232H[19];
    }

    /**
     * <p>Retrieves the value of Cbus6 variable (CBUS options) in ft_eeprom_232h structure.</p>
     * 
     * @return Cbus6 mux control value.
     */
    public int getCbus6() {
        return data232H[20];
    }

    /**
     * <p>Retrieves the value of Cbus7 variable (CBUS options) in ft_eeprom_232h structure.</p>
     * 
     * @return Cbus7 mux control value.
     */
    public int getCbus7() {
        return data232H[21];
    }

    /**
     * <p>Retrieves the value of Cbus8 variable (CBUS options) in ft_eeprom_232h structure.</p>
     * 
     * @return Cbus8 mux control value.
     */
    public int getCbus8() {
        return data232H[22];
    }

    /**
     * <p>Retrieves the value of Cbus9 variable (CBUS options) in ft_eeprom_232h structure.</p>
     * 
     * @return Cbus9 mux control value.
     */
    public int getCbus9() {
        return data232H[23];
    }

    /**
     * <p>Retrieves the value of FT1248Cpol variable (FT1248 options) in ft_eeprom_232h structure.</p>
     * 
     * @return FT1248 clock polarity - clock idle high (1) or clock idle low (0).
     */
    public int getFT1248Cpol() {
        return data232H[24];
    }

    /**
     * <p>Retrieves the value of FT1248Lsb variable (FT1248 options) in ft_eeprom_232h structure.</p>
     * 
     * @return FT1248 data is LSB (1) or MSB (0).
     */
    public int getFT1248Lsb() {
        return data232H[25];
    }

    /**
     * <p>Retrieves the value of FT1248FlowControl variable (FT1248 options) in ft_eeprom_232h structure.</p>
     * 
     * @return FT1248 flow control enable value.
     */
    public int getFT1248FlowControl() {
        return data232H[26];
    }

    /**
     * <p>Retrieves the value of IsFifo variable (hardware options) in ft_eeprom_232h structure.</p>
     * 
     * @return non-zero if interface is 245 FIFO.
     */
    public int getIsFifo() {
        return data232H[27];
    }

    /**
     * <p>Retrieves the value of IsFifoTar variable (hardware options) in ft_eeprom_232h structure.</p>
     * 
     * @return non-zero if interface is 245 FIFO CPU target.
     */
    public int getIsFifoTar() {
        return data232H[28];
    }

    /**
     * <p>Retrieves the value of IsFifo variable (hardware options) in ft_eeprom_232h structure.</p>
     * 
     * @return non-zero if interface is Fast serial.
     */
    public int getIsFastSer() {
        return data232H[29];
    }

    /**
     * <p>Retrieves the value of IsFT1248 variable (hardware options) in ft_eeprom_232h structure.</p>
     * 
     * @return non-zero if interface is Fast serial.
     */
    public int getIsFT1248() {
        return data232H[30];
    }


    /**
     * <p>Retrieves the value of PowerSaveEnable variable (hardware options) in ft_eeprom_232h structure.</p>
     * 
     * @return power save enable value.
     */
    public int getPowerSaveEnable() {
        return data232H[31];
    }

    /**
     * <p>Retrieves the value of DriverType variable (driver options) in ft_eeprom_232h structure.</p>
     * 
     * @return driver type.
     */
    public int getBDriverType() {
        return data232H[32];
    }

    /**
     * <p>Retrieves all the value defined for ft_eeprom_header and ft_eeprom_232h structures.</p>
     * 
     * @return array of member's values.
     */
    public int[] getAllMembers() {
        return data232H;
    }
}
