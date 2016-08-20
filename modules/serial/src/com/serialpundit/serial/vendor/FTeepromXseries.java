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
 * <p>Represents the C structure 'ft_eeprom_x_series' declared in ftd2xx.h header file.</p>
 * 
 * @author Rishi Gupta
 */
public final class FTeepromXseries extends FTeepromHeader {

    /* same array is shared between super class and sub class, they extract values 
     * from index applicable to them. */
    private final int[] dataXseries;

    /**
     * <p>Construct and allocates a new FTeeprom232R object with given details.</p>
     * 
     * <p>The sequence of values must be defined in following order: deviceType, VendorId, ProductId, 
     * SerNumEnable, MaxPower, SelfPowered, RemoteWakeup, PullDownEnable, ACSlowSlew, ACSchmittInput, 
     * ACDriveCurrent, ADSlowSlew, ADSchmittInput, ADDriveCurrent, Cbus0, Cbus1, Cbus2, Cbus3, Cbus4,
     * Cbus5, Cbus6, InvertTXD, InvertRXD, InvertRTS, InvertCTS, InvertDTR, InvertDSR, InvertDCD, 
     * InvertRI, BCDEnable, BCDForceCbusPWREN, BCDDisableSleep, I2CSlaveAddress, I2CDeviceId, 
     * I2CDisableSchmitt, FT1248Cpol, FT1248Lsb, FT1248FlowControl, RS485EchoSuppress, PowerSaveEnable, 
     * DriverType respectively.</p>
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
     * @throws IllegalArgumentException if data is null or its size is not equal to 41.
     */
    public FTeepromXseries(int[] data) {
        super(data);
        if(data == null) {
            throw new IllegalArgumentException("Argument data can not be null !");
        }
        if(data.length != 41) {
            throw new IllegalArgumentException("Argument data must be of length 41 !");
        }
        this.dataXseries = data;
    }

    /**
     * <p>Retrieves the value of ACSlowSlew variable (drive options) in ft_eeprom_x_series structure.</p>
     * 
     * @return non-zero if AC bus pins have slow slew.
     */
    public int getACSlowSlew() {
        return dataXseries[8];
    }

    /**
     * <p>Retrieves the value of ACSchmittInput variable (drive options) in ft_eeprom_x_series structure.</p>
     * 
     * @return non-zero if AC bus pins are Schmitt input.
     */
    public int getACSchmittInput() {
        return dataXseries[9];
    }

    /**
     * <p>Retrieves the value of ACDriveCurrent variable (drive options) in ft_eeprom_x_series structure.</p>
     * 
     * @return driver current value (valid values are 4mA, 8mA, 12mA, 16mA).
     */
    public int getACDriveCurrent() {
        return dataXseries[10];
    }	

    /**
     * <p>Retrieves the value of ADSlowSlew variable (drive options) in ft_eeprom_x_series structure.</p>
     * 
     * @return non-zero if AD bus pins have slow slew.
     */
    public int getADSlowSlew() {
        return dataXseries[11];
    }

    /**
     * <p>Retrieves the value of ADSchmittInput variable (drive options) in ft_eeprom_x_series structure.</p>
     * 
     * @return non-zero if AD bus pins are Schmitt input.
     */
    public int getADSchmittInput() {
        return dataXseries[12];
    }

    /**
     * <p>Retrieves the value of ADDriveCurrent variable (drive options) in ft_eeprom_x_series structure.</p>
     * 
     * @return driver current value (valid values are 4mA, 8mA, 12mA, 16mA).
     */
    public int getADDriveCurrent() {
        return dataXseries[13];
    }

    /**
     * <p>Retrieves the value of Cbus0 variable (CBUS options) in ft_eeprom_x_series structure.</p>
     * 
     * @return Cbus0 mux control value.
     */
    public int getCbus0() {
        return dataXseries[14];
    }

    /**
     * <p>Retrieves the value of Cbus1 variable (CBUS options) in ft_eeprom_x_series structure.</p>
     * 
     * @return Cbus1 mux control value.
     */
    public int getCbus1() {
        return dataXseries[15];
    }

    /**
     * <p>Retrieves the value of Cbus2 variable (CBUS options) in ft_eeprom_x_series structure.</p>
     * 
     * @return Cbus2 mux control value.
     */
    public int getCbus2() {
        return dataXseries[16];
    }

    /**
     * <p>Retrieves the value of Cbus3 variable (CBUS options) in ft_eeprom_x_series structure.</p>
     * 
     * @return Cbus3 mux control value.
     */
    public int getCbus3() {
        return dataXseries[17];
    }

    /**
     * <p>Retrieves the value of Cbus4 variable (CBUS options) in ft_eeprom_x_series structure.</p>
     * 
     * @return Cbus4 mux control value.
     */
    public int getCbus4() {
        return dataXseries[18];
    }

    /**
     * <p>Retrieves the value of Cbus5 variable (CBUS options) in ft_eeprom_x_series structure.</p>
     * 
     * @return Cbus5 mux control value.
     */
    public int getCbus5() {
        return dataXseries[19];
    }

    /**
     * <p>Retrieves the value of Cbus6 variable (CBUS options) in ft_eeprom_x_series structure.</p>
     * 
     * @return Cbus6 mux control value.
     */
    public int getCbus6() {
        return dataXseries[20];
    }

    /**
     * <p>Retrieves the value of InvertTXD variable (UART signal options) in ft_eeprom_x_series structure.</p>
     * 
     * @return non-zero if invert TXD.
     */
    public int getInvertTXD() {
        return dataXseries[21];
    }

    /**
     * <p>Retrieves the value of InvertRXD variable (UART signal options) in ft_eeprom_x_series structure.</p>
     * 
     * @return non-zero if invert RXD.
     */
    public int getInvertRXD() {
        return dataXseries[22];
    }	

    /**
     * <p>Retrieves the value of InvertRTS variable (UART signal options) in ft_eeprom_x_series structure.</p>
     * 
     * @return non-zero if invert RTS.
     */
    public int getInvertRTS() {
        return dataXseries[23];
    }

    /**
     * <p>Retrieves the value of InvertCTS variable (UART signal options) in ft_eeprom_x_series structure.</p>
     * 
     * @return non-zero if invert CTS.
     */
    public int getInvertCTS() {
        return dataXseries[24];
    }

    /**
     * <p>Retrieves the value of InvertDTR variable (UART signal options) in ft_eeprom_x_series structure.</p>
     * 
     * @return non-zero if invert DTR.
     */
    public int getInvertDTR() {
        return dataXseries[25];
    }

    /**
     * <p>Retrieves the value of InvertDSR variable (UART signal options) in ft_eeprom_x_series structure.</p>
     * 
     * @return non-zero if invert DSR.
     */
    public int getInvertDSR() {
        return dataXseries[26];
    }

    /**
     * <p>Retrieves the value of InvertDCD variable (UART signal options) in ft_eeprom_x_series structure.</p>
     * 
     * @return non-zero if invert DCD.
     */
    public int getInvertDCD() {
        return dataXseries[27];
    }

    /**
     * <p>Retrieves the value of InvertRI variable (UART signal options) in ft_eeprom_x_series structure.</p>
     * 
     * @return non-zero if invert RI.
     */
    public int getInvertRI() {
        return dataXseries[28];
    }

    /**
     * <p>Retrieves the value of BCDEnable variable (battery charge detect options) in 
     * ft_eeprom_x_series structure.</p>
     * 
     * @return value for battery charge enable.
     */
    public int getBCDEnable() {
        return dataXseries[29];
    }

    /**
     * <p>Retrieves the value of BCDForceCbusPWREN variable (battery charge detect options) 
     * in ft_eeprom_x_series structure.</p>
     * 
     * @return value indicating assert or not the power enable signal on CBUS when charging port detected.
     */
    public int getBCDForceCbusPWREN() {
        return dataXseries[30];
    }

    /**
     * <p>Retrieves the value of BCDDisableSleep variable (battery charge detect options) in 
     * ft_eeprom_x_series structure.</p>
     * 
     * @return value indicating whether force the device never to go into sleep mode.
     */
    public int getBCDDisableSleep() {
        return dataXseries[31];
    }

    /**
     * <p>Retrieves the value of I2CSlaveAddress variable (I2C options) in ft_eeprom_x_series structure.</p>
     * 
     * @return I2C slave device address.
     */
    public int getI2CSlaveAddress() {
        return dataXseries[32];
    }

    /**
     * <p>Retrieves the value of I2CDeviceId variable (I2C options) in ft_eeprom_x_series structure.</p>
     * 
     * @return I2C device ID.
     */
    public int getI2CDeviceId() {
        return dataXseries[33];
    }

    /**
     * <p>Retrieves the value of I2CDisableSchmitt variable (I2C options) in ft_eeprom_x_series structure.</p>
     * 
     * @return value indicating disable I2C Schmitt trigger.
     */
    public int getI2CDisableSchmitt() {
        return dataXseries[34];
    }

    /**
     * <p>Retrieves the value of FT1248Cpol variable (FT1248 options) in ft_eeprom_x_series structure.</p>
     * 
     * @return FT1248 clock polarity - clock idle high (1) or clock idle low (0).
     */
    public int getFT1248Cpol() {
        return dataXseries[35];
    }

    /**
     * <p>Retrieves the value of FT1248Lsb variable (FT1248 options) in ft_eeprom_x_series structure.</p>
     * 
     * @return FT1248 data is LSB (1) or MSB (0).
     */
    public int getFT1248Lsb() {
        return dataXseries[36];
    }

    /**
     * <p>Retrieves the value of FT1248FlowControl variable (FT1248 options) in ft_eeprom_x_series structure.</p>
     * 
     * @return FT1248 flow control enable value.
     */
    public int getFT1248FlowControl() {
        return dataXseries[37];
    }

    /**
     * <p>Retrieves the value of RS485EchoSuppress variable (hardware options) in ft_eeprom_x_series structure.</p>
     * 
     * @return value indicating RS485EchoSuppress.
     */
    public int getRS485EchoSuppress() {
        return dataXseries[38];
    }

    /**
     * <p>Retrieves the value of PowerSaveEnable variable (hardware options) in ft_eeprom_x_series structure.</p>
     * 
     * @return value indicating power save option.
     */
    public int getPowerSaveEnable() {
        return dataXseries[39];
    }

    /**
     * <p>Retrieves the value of DriverType variable (driver options) in ft_eeprom_x_series structure.</p>
     * 
     * @return driver type.
     */
    public int getADriverType() {
        return dataXseries[40];
    }

    /**
     * <p>Retrieves all the value defined for ft_eeprom_header and ft_eeprom_x_series structures.</p>
     * 
     * @return array of member's values.
     */
    public int[] getAllMembers() {
        return dataXseries;
    }
}
