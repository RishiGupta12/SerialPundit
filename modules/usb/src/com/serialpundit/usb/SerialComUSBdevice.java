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

package com.serialpundit.usb;

import com.serialpundit.core.util.SerialComUtil;

/**
 * <p>Represents a USB device with information about it.</p>
 * 
 * @author Rishi Gupta
 */
public final class SerialComUSBdevice {

    private final String idVendor;
    private final String idProduct;
    private final String serial;
    private final String product;
    private final String manufacturer;
    private final String location;

    /**
     * <p>Construct and allocates a new SerialComUSBdevice object with the given details.</p>
     * 
     * @param idVendor USB-IF unique vendor id of this device.
     * @param idProduct USB product id of this device.
     * @param serial serial number of this device.
     * @param product product identifier/description of this device.
     * @param manufacturer company manufacturing of this device.
     * @param location location information of this device.
     * @throws SerialComException if the object can not be constructed.
     */
    public SerialComUSBdevice(String idVendor, String idProduct, String serial, String product, 
            String manufacturer, String location) {
        this.idVendor = idVendor;
        this.idProduct = idProduct;
        this.serial = serial;
        this.product = product;
        this.manufacturer = manufacturer;
        this.location = location;
    }

    /** 
     * <p>Retrieves the vendor id of the USB device.</p>
     * 
     * @return vendor id of the USB device.
     * @throws NumberFormatException if the USB vendor id hex string can not be converted into 
     *          numerical representation.
     */
    public int getVendorID() {
        if("---".equals(idVendor)) {
            return 0;
        }
        return (int) SerialComUtil.hexStrToLongNumber(idVendor);
    }

    /** 
     * <p>Retrieves the product id of the USB device.</p>
     * 
     * @return product id of the USB device.
     * @throws NumberFormatException if the USB product id hex string can not be converted into 
     *          numerical representation.
     */
    public int getProductID() {
        if("---".equals(idProduct)) {
            return 0;
        }
        return (int) SerialComUtil.hexStrToLongNumber(idProduct);
    }

    /** 
     * <p>Retrieves the serial number string of the USB device.</p>
     * 
     * @return serial number string of the USB device.
     */
    public String getSerialNumber() {
        return serial;
    }

    /** 
     * <p>Retrieves the product string of the USB device.</p>
     * 
     * @return serial number string of the USB device.
     */
    public String getProductString() {
        return product;
    }

    /** 
     * <p>Retrieves the manufacturer string of the USB device.</p>
     * 
     * @return serial number string of the USB device.
     */
    public String getManufacturerString() {
        return manufacturer;
    }

    /** 
     * <p>Retrieves the location information of this usb device.</p>
     * 
     * @return location information about this USB device.
     */
    public String getLocation() {
        return location;
    }

    /** 
     * <p>Prints information about this device on console.</p>
     */
    public void dumpDeviceInfo() {
        System.out.println(
                "\nVendor id : 0x" + idVendor + 
                "\nProduct id : 0x" + idProduct + 
                "\nSerial number : " + serial + 
                "\nProduct : " + product + 
                "\nManufacturer : " + manufacturer +
                "\nLocation : " + location);
    }
}
