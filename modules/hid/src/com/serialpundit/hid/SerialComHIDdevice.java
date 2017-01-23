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

package com.serialpundit.hid;

import com.serialpundit.core.SerialComException;
import com.serialpundit.core.util.SerialComUtil;

/**
 * <p>Represents a HID device with information about it.</p>
 * 
 * @author Rishi Gupta
 */
public final class SerialComHIDdevice {

    private final String transport;
    private final String deviceNode;
    private final String idVendor;
    private final String idProduct;
    private final String serial;
    private final String product;
    private final String manufacturer;
    private final String location;

    /**
     * <p>Construct and allocates a new SerialComHIDdevice object with given details.</p>
     * 
     * @param transport communication medium USB or Bluetooth this devices uses.
     * @param deviceNode identifier that can be used to open this device.
     * @param idVendor USB-IF unique vendor id of this device.
     * @param idProduct USB product id of this device.
     * @param serial serial number of this device.
     * @param product product identifier/description of this device.
     * @param manufacturer company manufacturing of this device.
     * @param location location in device tree created dynamically.
     * @throws SerialComException if the object can not be constructed.
     */
    public SerialComHIDdevice(String transport, String deviceNode, String idVendor, String idProduct, 
            String serial, String product, String manufacturer, String location) {
        this.transport = transport;
        this.deviceNode = deviceNode;
        this.idVendor = idVendor;
        this.idProduct = idProduct;
        this.serial = serial;
        this.product = product;
        this.manufacturer = manufacturer;
        this.location = location;
    }

    /** 
     * <p>Returns USB or Bluetooth i.e. transport this device uses.</p>
     * 
     * @return USB or Bluetooth string whichever is applicable for this device.
     */
    public String getTransportType() {
        return transport;
    }

    /** 
     * <p>Returns device node representing this device in system.</p>
     * 
     * <p>In MAC os x, there is no device file for HID devices like '/dev/xxx'. Devices are identified 
     * by their usage or other properties. Therefore to maintain consistency, we create device node 
     * string with various properties separated by underscore : 
     * Transport_USB-VID_USB-PID_USB-serialnumber_LocationID</p>
     * 
     * @return string device node.
     */
    public String getDeviceNode() {
        return deviceNode;
    }

    /** 
     * <p>Retrieves the vendor id of the USB device.</p>
     * 
     * @return vendor id of the USB device or -1 if location ID is not applicable for this platform.
     * @throws NumberFormatException if the USB vendor id hex string can not be converted into numerical 
     *          representation.
     */
    public int getVendorID() {
        if("---".equals(idVendor)) {
            return -1;
        }
        return (int) SerialComUtil.hexStrToLongNumber(idVendor);
    }

    /** 
     * <p>Retrieves the product id of the USB device.</p>
     * 
     * @return product id of the USB device or -1 if location ID is not applicable for this platform.
     * @throws NumberFormatException if the USB product id hex string can not be converted into numerical 
     *          representation.
     */
    public int getProductID() {
        if("---".equals(idProduct)) {
            return -1;
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
     * <p>Retrieves the location of the USB device in system.</p>
     * 
     * @return location information about this device.
     */
    public String getLocation() {
        return location;
    }

    /** 
     * <p>Prints information about this device on console.</p>
     */
    public void dumpDeviceInfo() {
        System.out.println(
                "Transport : " + transport +
                "\nDevice node : " + deviceNode +
                "\nVendor id : 0x" + idVendor + 
                "\nProduct id : 0x" + idProduct + 
                "\nSerial number : " + serial + 
                "\nProduct : " + product + 
                "\nManufacturer : " + manufacturer +
                "\nLocation : " + location);
    }
}
