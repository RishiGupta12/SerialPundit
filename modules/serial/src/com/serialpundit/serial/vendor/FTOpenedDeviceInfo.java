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
 * <p>Represents an opened FT device with information about it.</p>
 * 
 * @author Rishi Gupta
 */
public final class FTOpenedDeviceInfo {

    private String type = null;
    private String id = null;
    private String serialNumber = null;
    private String description = null;

    /**
     * <p>Construct and allocates a new FTOpenedDeviceInfo object with given details.</p>
     * 
     * @param type device type.
     * @param id device ID.
     * @param serialNumber serial number of this device.
     * @param description description of this device.
     */
    public FTOpenedDeviceInfo(String type, String id, String serialNumber, String description) {
        this.type = type;
        this.id = id;
        this.serialNumber = serialNumber;
        this.description = description;
    }

    /** 
     * <p>Retrieves the type for this opened FT device.</p>
     * 
     * @return type for this FT device info node.
     * @throws NumberFormatException if the type hex string can not be converted into numerical representation.
     */
    public long getType() {
        return SerialComUtil.hexStrToLongNumber(type);
    }

    /** 
     * <p>Retrieves the id for this opened FT device.</p>
     * 
     * @return id for this FT device info node.
     * @throws NumberFormatException if the id hex string can not be converted into numerical representation.
     */
    public long getId() {
        return SerialComUtil.hexStrToLongNumber(id);
    }

    /** 
     * <p>Retrieves the serial number string for this opened FT device.</p>
     * 
     * @return serial number string for this FT device info node.
     */
    public String getSerialNumber() {
        return serialNumber;
    }

    /** 
     * <p>Retrieves the description for this opened FT device.</p>
     * 
     * @return description string for this FT device info node.
     */
    public String getDescription() {
        return description;
    }

    /** 
     * <p>Prints information about this opened FT device on console.</p>
     */
    public void dumpDeviceInfo() {
        System.out.println("\nType : " + type + 
                "\nID : 0x" + id + 
                "\nSerialNumber : " + serialNumber + 
                "\nDescription : " + description);
    }
}
