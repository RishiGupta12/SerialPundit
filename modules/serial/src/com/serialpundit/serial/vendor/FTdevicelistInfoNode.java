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
 * <p>Represents a FT_DEVICE_LIST_INFO_NODE structure.</p>
 * 
 * @author Rishi Gupta
 */
public final class FTdevicelistInfoNode {

    private String flags = null;
    private String type = null;
    private String id = null;
    private String locId = null;
    private String serialNumber = null;
    private String description = null;
    private String ftHandle = null;

    /**
     * <p>Construct and allocates a new FTdevicelistInfoNode object with given details.</p>
     * 
     * @param flags flags for this device.
     * @param type device type.
     * @param id device ID.
     * @param locId location id of this device.
     * @param serialNumber serial number of this device.
     * @param description description of this device.
     * @param ftHandle handle for this device.
     */
    public FTdevicelistInfoNode(String flags, String type, String id, String locId, String serialNumber, 
            String description, String ftHandle) {
        this.flags = flags;
        this.type = type;
        this.id = id;
        this.locId = locId;
        this.serialNumber = serialNumber;
        this.description = description;
        this.ftHandle = ftHandle;
    }

    /** 
     * <p>Retrieves the flags for this FT device info node.</p>
     * 
     * @return flags for this FT device info node.
     * @throws NumberFormatException if the flags hex string can not be converted into numerical representation.
     */
    public long getFlags() {
        return SerialComUtil.hexStrToLongNumber(flags);
    }

    /** 
     * <p>Interpret the flags of this device giving information about whether port is open or closed and 
     * is it enumerated as high speed or full speed usb device.</p>
     * 
     * @return Array of string with interpretation of this device flags field.
     * @throws NumberFormatException if the flags hex string can not be converted into numerical representation.
     */
    public String[] interpretFlags() {
        String[] info = new String[2];

        // The flag value is a 4-byte bit map containing miscellaneous data
        short flagBitMap = (short) SerialComUtil.hexStrToLongNumber(flags);

        // Bit 0 (least significant bit) of this number indicates if the port is open (1) or closed (0).
        if((0x01 & flagBitMap) == 0x01) {
            info[0] = new String("Port status : open");
        }else {
            info[0] = new String("Port status : closed");
        }

        // Bit 1 indicates if the device is enumerated as a high-speed USB device (2) or a full-speed USB device (0).
        if((0x02 & flagBitMap) == 0x02) {
            info[1] = new String("Enumerated as : high-speed USB device");
        }else {
            info[1] = new String("Enumerated as : full-speed USB device");
        }

        return info;
    }

    /** 
     * <p>Retrieves the type for this FT device info node.</p>
     * 
     * @return type for this FT device info node.
     * @throws NumberFormatException if the type hex string can not be converted into numerical representation.
     */
    public long getType() {
        return SerialComUtil.hexStrToLongNumber(type);
    }

    /** 
     * <p>Retrieves the id for this FT device info node.</p>
     * 
     * @return id for this FT device info node.
     * @throws NumberFormatException if the id hex string can not be converted into numerical representation.
     */
    public long getId() {
        return SerialComUtil.hexStrToLongNumber(id);
    }

    /** 
     * <p>Retrieves the locId for this FT device info node.</p>
     * 
     * @return locId for this FT device info node.
     * @throws NumberFormatException if the locId hex string can not be converted into numerical representation.
     */
    public long getLocId() {
        return SerialComUtil.hexStrToLongNumber(locId);
    }

    /** 
     * <p>Retrieves the serial number string for this FT device info node.</p>
     * 
     * @return serial number string for this FT device info node.
     */
    public String getSerialNumber() {
        return serialNumber;
    }

    /** 
     * <p>Retrieves the description for this FT device info node.</p>
     * 
     * @return description string for this FT device info node.
     */
    public String getDescription() {
        return description;
    }

    /** 
     * <p>Retrieves the ftHandle for this FT device info node.</p>
     * 
     * @return ftHandle for this FT device info node.
     * @throws NumberFormatException if the ftHandle hex string can not be converted into numerical representation.
     */
    public long getFThandle() {
        return SerialComUtil.hexStrToLongNumber(ftHandle);
    }

    /** 
     * <p>Prints information about this FT device info node on console.</p>
     */
    public void dumpDeviceInfo() {
        System.out.println("Flags : 0x" + flags + 
                "\nType : 0x" + type + 
                "\nID : 0x" + id + 
                "\nLocId : 0x" + locId + 
                "\nSerialNumber : " + serialNumber + 
                "\nDescription : " + description + 
                "\nftHandle : 0x" + ftHandle );
    }
}
