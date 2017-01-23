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

/**
 * <p>The SerialPundit provides two set of APIs for communicating with a HID device; raw mode API and 
 * parsed mode API. In raw mode, input reports received and output reports sent are not parsed. In 
 * parsed mode, input reports received and output reports sent are parsed. </p>
 * 
 * <p>In Windows, system supplied HID minidriver and HID class drivers provide most of the functionality 
 * and required support for HID-compliant devices. However we may have to write our own HID minidriver
 * if it is difficult to make desired changes to HID-compliant device firmware or if we need to make a 
 * non-HID compliant device into a HID device without updating the firmware.</p>
 *
 * <p>Note that a driver can override the descriptors and reports from a HID device.</p>
 * 
 * <p>The SerialComRawHID class extends this class.</p>
 * 
 * @author Rishi Gupta
 */
public class SerialComHID {

    /**<p>The value indicating instance of SerialComRawHID class. Integer constant with value 0x01.</p>*/
    public static final int MODE_RAW = 0x01;

    /**<p>The value indicating instance of SerialComParsedHID class. Integer constant with value 0x02.</p>*/
    public static final int MODE_PARSED = 0x02;

    /**<p>The value indicating instance of SerialComHID class (HID transport neutral). Integer constant with 
     * value 0x03.</p>*/
    public static final int HID_GENERIC = 0x03;

    /**<p>The value indicating instance of SerialComUSBHID class (HID over USB). Integer constant 
     * with value 0x04.</p>*/
    public static final int HID_USB = 0x04;

    /**<p>The value indicating instance of SerialComBluetoothHID class (HID over Bluetooth). Integer 
     * constant with value 0x05.</p>*/
    public static final int HID_BLUETOOTH = 0x05;

    /**<p>The value indicating instance of SerialComI2CHID class (HID over I2C). Integer constant with 
     * value 0x06.</p>*/
    public static final int HID_I2C = 0x06;

    /** <p>The exception message indicating that a blocked read method has been unblocked 
     * and made to return to caller explicitly (irrespective there was data to read or not). </p>*/
    public static final String EXP_UNBLOCK_HIDIO  = "I/O operation unblocked !";

    /**
     * <p>Allocates a new SerialComHID object.</p>
     * 
     */
    public SerialComHID() {
    }
}
