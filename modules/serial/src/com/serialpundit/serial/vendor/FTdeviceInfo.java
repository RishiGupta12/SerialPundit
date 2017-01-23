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

import com.serialpundit.core.util.SerialComUtil;

/**
 * <p>Represents a device returned by calling listDevices() method in 
 * SerialComFTDID2XX class.</p>
 * 
 * @author Rishi Gupta
 */
public final class FTdeviceInfo {

    private String locId = null;
    private String serialNumber = null;
    private String description = null;

    /**
     * <p>Construct and allocates a new FTdeviceInfo object with given details.</p>
     * 
     * @param locId location ID of the device.
     * @param serialNumber serial number of this device.
     * @param description description of this device.
     */
    public FTdeviceInfo(String locId, String serialNumber, String description) {
        this.locId = locId;
        this.serialNumber = serialNumber;
        this.description = description;
    }

    /** 
     * <p>Retrieves the locId for this FT device.</p>
     * 
     * @return locId for this FT device.
     * @throws NumberFormatException if the locId hex string can not be converted into numerical 
     *          representation.
     */
    public long getLocId() {
        return SerialComUtil.hexStrToLongNumber(locId);
    }

    /** 
     * <p>Retrieves the serial number string for this FT device.</p>
     * 
     * @return serial number string for this FT device.
     */
    public String getSerialNumber() {
        return serialNumber;
    }

    /** 
     * <p>Retrieves the description for this FT device.</p>
     * 
     * @return description string for this FT device.
     */
    public String getDescription() {
        return description;
    }

    /** 
     * <p>Prints information about this FT device on console.</p>
     */
    public void dumpDeviceInfo() {
        System.out.println("LocID : 0x" + locId + 
                "\nSerialNumber : " + serialNumber + 
                "\nDescription : " + description);
    }
}
