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

import java.io.File;
import java.io.FileNotFoundException;

import com.serialpundit.core.SerialComSystemProperty;
import com.serialpundit.core.SerialComException;
import com.serialpundit.serial.internal.SerialComCP210xRuntimeJNIBridge;

/**
 * <p>Silicon labs provides libraries to communicate with their USB-UART devices. More information can 
 * be found here : https://www.silabs.com/products/mcu/Pages/USBtoUARTBridgeVCPDrivers.aspx</p>
 * 
 * <ul>
 * <li>The data types used in java layer may be bigger in size than the native layer. For example; if native 
 * function returns 16 bit signed integer, than java method will return 32 bit integer. This is done to make 
 * sure that no data loss occur. This library take care of sign and their applicability internally.</li>
 * 
 * <li><p>Developers are requested to check with vendor library documentation if a particular function is supported
 * for desired platform or not and also how does a particular API will behave. Also consider paying attention to 
 * valid values and range when passing arguments to a method.</p></li>
 * 
 * <li>The application note for CP210XRuntime library is here : 
 * https://www.silabs.com/Support%20Documents/TechnicalDocs/an223.pdf</li>
 * 
 * <li><p>SerialPundit version 1.0.4 is linked to v3.4.0.0 version of CP210xRuntime library (CP210xRuntime.dll).</p></li>
 * </ul>
 * 
 * @author Rishi Gupta
 */
public final class SerialComSLabsCP210xRuntime extends SerialComVendorLib {

    /**<p>Bit mask to represent mask and latch definition for GPIO0 in CP210X terminology.</p>*/
    public static final int CP210x_GPIO_0  = 0x0001;

    /**<p>Bit mask to represent mask and latch definition for GPIO1 in CP210X terminology.</p>*/
    public static final int CP210x_GPIO_1  = 0x0002;

    /**<p>Bit mask to represent mask and latch definition for GPIO2 in CP210X terminology.</p>*/
    public static final int CP210x_GPIO_2  = 0x0004;

    /**<p>Bit mask to represent mask and latch definition for GPIO3 in CP210X terminology.</p>*/
    public static final int CP210x_GPIO_3  = 0x0008;

    /**<p>Bit mask to represent mask and latch definition for GPIO4 in CP210X terminology.</p>*/
    public static final int CP210x_GPIO_4  = 0x0010;

    /**<p>Bit mask to represent mask and latch definition for GPIO5 in CP210X terminology.</p>*/
    public static final int CP210x_GPIO_5  = 0x0020;

    /**<p>Bit mask to represent mask and latch definition for GPIO6 in CP210X terminology.</p>*/
    public static final int CP210x_GPIO_6  = 0x0040;

    /**<p>Bit mask to represent mask and latch definition for GPIO7 in CP210X terminology.</p>*/
    public static final int CP210x_GPIO_7  = 0x0080;

    /**<p>Bit mask to represent mask and latch definition for GPIO8 in CP210X terminology.</p>*/
    public static final int CP210x_GPIO_8  = 0x0100;

    /**<p>Bit mask to represent mask and latch definition for GPIO9 in CP210X terminology.</p>*/
    public static final int CP210x_GPIO_9  = 0x0200;

    /**<p>Bit mask to represent mask and latch definition for GPIO10 in CP210X terminology.</p>*/
    public static final int CP210x_GPIO_10 = 0x0400;

    /**<p>Bit mask to represent mask and latch definition for GPIO11 in CP210X terminology.</p>*/
    public static final int CP210x_GPIO_11 = 0x0800;

    /**<p>Bit mask to represent mask and latch definition for GPIO12 in CP210X terminology.</p>*/
    public static final int CP210x_GPIO_12 = 0x1000;

    /**<p>Bit mask to represent mask and latch definition for GPIO13 in CP210X terminology.</p>*/
    public static final int CP210x_GPIO_13 = 0x2000;

    /**<p>Bit mask to represent mask and latch definition for GPIO14 in CP210X terminology.</p>*/
    public static final int CP210x_GPIO_14 = 0x4000;

    /**<p>Bit mask to represent mask and latch definition for GPIO15 in CP210X terminology.</p>*/
    public static final int CP210x_GPIO_15 = 0x8000;

    private final SerialComCP210xRuntimeJNIBridge mSerialComCP210xRuntimeJNIBridge;

    /**
     * <p>Allocates a new SerialComSLabsCP210xRuntime object and extract and load shared libraries as 
     * required.</p>
     * 
     * @param libDirectory directory in which native library will be extracted and vendor library will 
     *        be found.
     * @param vlibName name of vendor library to load and link.
     * @param cpuArch architecture of CPU this library is running on.
     * @param osType operating system this library is running on.
     * @param serialComSystemProperty instance of SerialComSystemProperty to get required java properties.
     * @throws SerialComException if java system properties can not be is null, if any file system related issue occurs.
     * @throws SecurityException if java system properties can not be  accessed or required files can not be accessed.
     * @throws UnsatisfiedLinkError if loading/linking shared library fails.
     * @throws FileNotFoundException if the vendor library file is not found.
     */
    public SerialComSLabsCP210xRuntime(File libDirectory, String vlibName, int cpuArch, int osType, 
            SerialComSystemProperty serialComSystemProperty) throws SerialComException {

        mSerialComCP210xRuntimeJNIBridge = new SerialComCP210xRuntimeJNIBridge();
        SerialComCP210xRuntimeJNIBridge.loadNativeLibrary(libDirectory, vlibName, cpuArch, osType, serialComSystemProperty);
    }

    /**
     * <p>Executes CP210xRT_ReadLatch function of CP210XRuntime library.</p>
     * 
     * <p>Gets the current port latch value from the device.</p>
     * 
     * @param handle handle of the opened COM port.
     * @return bit mask of constant(s) CP210x_GPIO_XX.
     * @throws SerialComException if an I/O error occurs.
     */
    public long readLatch(final long handle) throws SerialComException {
        long ret = mSerialComCP210xRuntimeJNIBridge.readLatch(handle);
        if(ret < 0) {
            throw new SerialComException("Could not read the port latch value for given device. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes CP210xRT_WriteLatch function of CP210XRuntime library.</p>
     * 
     * <p>Sets the current port latch value for the device.</p>
     * 
     * @param handle handle of the opened COM port.
     * @param mask determines which pins to change [Change = 1, Leave = 0].
     * @param latchValue bit mask value to write to GPIO latch [Logic High = 1, Logic Low = 0].
     * @return true if value gets set successfully.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean writeLatch(final long handle, long mask, long latchValue) throws SerialComException {
        int ret = mSerialComCP210xRuntimeJNIBridge.writeLatch(handle, mask, latchValue);
        if(ret < 0) {
            throw new SerialComException("Could not write the given latch value on the given device. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes CP210xRT_GetPartNumber function of CP210XRuntime library.</p>
     * 
     * <p>Gets the part number of the current device.</p>
     * 
     * @param handle handle of the opened COM port.
     * @return part number string of the current device.
     * @throws SerialComException if an I/O error occurs.
     */
    public String getPartNumber(final long handle) throws SerialComException {
        String ret = mSerialComCP210xRuntimeJNIBridge.getPartNumber(handle);
        if(ret == null) {
            throw new SerialComException("Could not get the part number of the current device. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes CP210xRT_GetDeviceProductString function of CP210XRuntime library.</p>
     * 
     * <p>Gets the product string in the current device.</p>
     * 
     * @param handle handle of the opened COM port.
     * @return product string.
     * @throws SerialComException if an I/O error occurs.
     */
    public String getDeviceProductString(final long handle) throws SerialComException {
        String ret = mSerialComCP210xRuntimeJNIBridge.getDeviceProductString(handle);
        if(ret == null) {
            throw new SerialComException("Could not get the product string of the current device. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes CP210xRT_GetDeviceSerialNumber function of CP210XRuntime library.</p>
     * 
     * <p>Gets the serial number in the current device.</p>
     * 
     * @param handle handle of the opened COM port.
     * @return serial number string.
     * @throws SerialComException if an I/O error occurs.
     */
    public String getDeviceSerialNumber(final long handle) throws SerialComException {
        String ret = mSerialComCP210xRuntimeJNIBridge.getDeviceSerialNumber(handle);
        if(ret == null) {
            throw new SerialComException("Could not get the serial number of the current device. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes CP210xRT_GetDeviceInterfaceString function of CP210XRuntime library.</p>
     * 
     * <p>Gets the interface string of the current device.</p>
     * 
     * @param handle handle of the opened COM port.
     * @return interface string.
     * @throws SerialComException if an I/O error occurs.
     */
    public String getDeviceInterfaceString(final long handle) throws SerialComException {
        String ret = mSerialComCP210xRuntimeJNIBridge.getDeviceInterfaceString(handle);
        if(ret == null) {
            throw new SerialComException("Could not get the interface string of the current device. Please retry !");
        }
        return ret;
    }
}
