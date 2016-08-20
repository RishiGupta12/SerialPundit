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
 * <p>Represents the C structure 'ft_eeprom_header' declared in ftd2xx.h header file.</p>
 * 
 * @author Rishi Gupta
 */
public class FTeepromHeader extends FTeepromData {

    private final int[] commonData;

    /**
     * <p>Construct and allocates a new FTeepromHeader object with given details.</p>
     * 
     * <p>This class must not be instantiated directly but one of the subclasses must be used 
     * to populate correct values.</p>
     * 
     * @param data values of member variables in C structure.
     */
    public FTeepromHeader(int[] data) {
        super(data);
        this.commonData = data;
    }

    /**
     * <p>Retrieves the value of deviceType variable in ft_eeprom_header structure.</p>
     * 
     * @return one of the constant FT_DEVICE_XXXX defined in SerialComFTDID2XX class.
     */
    public final int getDeviceType() {
        return commonData[0];
    }

    /**
     * <p>Retrieves the value of VendorId variable (device descriptor options) in ft_eeprom_header structure.</p>
     * 
     * @return USB vendor ID of this device.
     */
    public final int getVendorID() {
        return commonData[1];
    }

    /**
     * <p>Retrieves the value of ProductId variable (device descriptor options) in ft_eeprom_header structure.</p>
     * 
     * @return USB product ID of this device.
     */
    public final int getProductID() {
        return commonData[2];
    }

    /**
     * <p>Retrieves the value of SerNumEnable variable (device descriptor options) in ft_eeprom_header structure.</p>
     * 
     * @return non-zero if serial number is to be used.
     */
    public final int getSerNumEnable() {
        return commonData[3];
    }

    /**
     * <p>Retrieves the value of MaxPower variable (config descriptor options) in ft_eeprom_header structure.</p>
     * 
     * @return maximum power that may be consumed by this USB device.
     */
    public final int getMaxPower() {
        return commonData[4];
    }

    /**
     * <p>Retrieves the value of SelfPowered variable (config descriptor options) in ft_eeprom_header structure.</p>
     * 
     * @return 0 if bus powered, 1 if self powered.
     */
    public final int getSelfPowered() {
        return commonData[5];
    }

    /**
     * <p>Retrieves the value of RemoteWakeup variable (config descriptor options) in ft_eeprom_header structure.</p>
     * 
     * @return 1 if capable or 0 if not capable.
     */
    public final int getRemoteWakeup() {
        return commonData[6];
    }

    /**
     * <p>Retrieves the value of PullDownEnable variable (hardware options) in ft_eeprom_header structure.</p>
     * 
     * @return non-zero if pull down in suspend enabled.
     */
    public final int getPullDownEnable() {
        return commonData[7];
    }
}
