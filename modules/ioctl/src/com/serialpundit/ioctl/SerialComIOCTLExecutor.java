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

package com.serialpundit.ioctl;

import java.io.IOException;

import com.serialpundit.core.SerialComException;
import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComSystemProperty;
import com.serialpundit.ioctl.internal.SerialComIOCTLJNIBridge;

/**
 * <p>Executes device-specific input/output operations and other operations which cannot be executed using 
 * regular system calls.</p>
 * 
 * <p>Application is expected to consult appropriate document to supply valid values to the methods as 
 * arguments and should understand what to expect by executing that particular IOCTL call. The IOCTL number 
 * (operation code) are unique to each driver and must be obtained from them.</p>
 * 
 * Following are typical use cases:
 * 
 * <ul>
 * <li>Access to GPIO pins in USB-UART, USB-HID, USB-I2C etc converters. It provide access to UART registers 
 * in the chip for example for reading the value at a particular port latch. The operating standard API for serial 
 * port communication provided by operating system generally does not have provision to explore modern facilities 
 * in USB-UART chip. The IOCTL calls helps in reading and writing to registers inside chip for example to configure 
 * a GPIO pin as digital output.
 * 
 * <p>It should be noted that the USB-UART bridge generally have one time programmable memory 
 * and therefore configuration/customization settings must be done thoughtfully either programmatically 
 * or through vendor provided utility.</p></li>
 * </ul>
 * 
 * @author Rishi Gupta
 */
public final class SerialComIOCTLExecutor {

    private SerialComPlatform mSerialComPlatform;
    private final SerialComSystemProperty mSerialComSystemProperty;
    private static int osType = SerialComPlatform.OS_UNKNOWN;
    private static int cpuArch = SerialComPlatform.ARCH_UNKNOWN;
    private static int abiType = SerialComPlatform.ABI_UNKNOWN;
    private static final Object lockA = new Object();
    private static SerialComIOCTLJNIBridge mIOCTLJNIBridge;

    /**
     * <p>Allocates a new SerialComIOCTLExecutor object and load/link native libraries if required.</p>
     * 
     */
    public SerialComIOCTLExecutor(String libDirectory, String loadedLibName) throws SecurityException, IOException {

        mSerialComSystemProperty = new SerialComSystemProperty();

        synchronized(lockA) {
            if(osType <= 0) {
                mSerialComPlatform = new SerialComPlatform(mSerialComSystemProperty);
                osType = mSerialComPlatform.getOSType();
                if(osType == SerialComPlatform.OS_UNKNOWN) {
                    throw new SerialComException("Could not identify operating system. Please report to us your environemnt so that we can add support for it !");
                }
                cpuArch = mSerialComPlatform.getCPUArch(osType);
                if(cpuArch == SerialComPlatform.ARCH_UNKNOWN) {
                    throw new SerialComException("Could not identify CPU architecture. Please report to us your environemnt so that we can add support for it !");
                }
                if((cpuArch == SerialComPlatform.ARCH_ARMV7) || (cpuArch == SerialComPlatform.ARCH_ARMV6) || (cpuArch == SerialComPlatform.ARCH_ARMV5)) {
                    if(osType == SerialComPlatform.OS_LINUX) {
                        abiType = mSerialComPlatform.getABIType();
                    }
                }
            }
            if(mIOCTLJNIBridge == null) {
                mIOCTLJNIBridge = new SerialComIOCTLJNIBridge();
                SerialComIOCTLJNIBridge.loadNativeLibrary(libDirectory, loadedLibName, mSerialComSystemProperty, osType, cpuArch, abiType);
            }
        }
    }

    /**
     * <p>Executes the requested operation on the specified handle.</p>
     *
     * @param handle handle of the port on which to execute this ioctl operation.
     * @param operationCode unique ioctl operation code (device/driver specific).
     * @return true if operation executed successfully.
     * @throws SerialComException if the operation can not be completed as requested.
     */
    public boolean ioctlExecuteOperation(long handle, long operationCode) throws SerialComException {
        long ret = 0;
        ret = mIOCTLJNIBridge.ioctlExecuteOperation(handle, operationCode);
        if(ret < 0) {
            throw new SerialComException("Could not exceute requested IOCTL operation. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes the requested operation on the specified handle passing the given value to operation.</p>
     * <p>This may be used to toggle GPIO pins present on some state-of-art USB to UART bridges. For example 
     * for Silicon labs CP210X series using 'CP210x VCP Linux 3.0 Driver Kit' the GPIO 0 pin can be toggled 
     * as shown below: </p>
     * <p>Turn on  : ioctlSetValue(handle, 0x8001, 0x00010001) </p>
     * <p>Turn off : ioctlSetValue(handle, 0x8001, 0x00000001) </p>
     * 
     * <p>Modern USB-UART bridge generally have user-configurable GPIO pins for status and control information. 
     * Each of these GPIO pins may be used as inputs, open-drain outputs, or push-pull outputs. Care must be 
     * taken to correctly interface these GPIO pins for required amount of current.</p>
     * 
     * <p>Further GPIO pins may have multiplexed functionality. For example a particular GPIO Pin may be configured 
     * as GPIO to control external peripheral or may be configured as RTS modem line. It is advised to consult 
     * datasheet. GPIO pins may also be configured at power-up so that they can be tailored to fit the needs of the 
     * application design.</p>
     * 
     * <p>This method can be used to write to UART registers in a USB-UART device for example EXAR XR22801/802/804 etc.</p>
     *
     * @param handle handle of the port on which to execute this ioctl operation.
     * @param operationCode unique ioctl operation code.
     * @param value the value to be passed to the IOCTL operation.
     * @return true if operation executed successfully.
     * @throws SerialComException if the operation can not be completed as requested.
     */
    public boolean ioctlSetValue(long handle, long operationCode, long value) throws SerialComException {
        long ret = 0;
        ret = mIOCTLJNIBridge.ioctlSetValue(handle, operationCode, value);
        if(ret < 0) {
            throw new SerialComException("Could not set the value using IOCTL opertaion. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes the requested operation on the specified handle. This operation returns a numerical value. 
     * This method can be used to read a register in chip. For example to get the status of GPIOs pins on 
     * CP210X series from Silicon labs using 'CP210x VCP Linux 3.0 Driver Kit' the following call can be made:</p>
     * 
     * <p>long value = ioctlSetValue(handle, 0x8000) </p>
     * 
     * <p>This method can be used to read UART registers in a USB-UART device for example EXAR XR21B1420/1422/1424 family etc.</p>
     * 
     * @param handle handle of the port on which to execute this ioctl operation.
     * @param operationCode unique ioctl operation code.
     * @return value requested.
     * @throws SerialComException if the operation can not be completed as requested.
     */
    public long ioctlGetValue(long handle, long operationCode) throws SerialComException {
        long ret = 0;
        ret = mIOCTLJNIBridge.ioctlGetValue(handle, operationCode);
        if(ret < 0) {
            throw new SerialComException("Could not execute the given IOCTL operation. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes the requested operation on the specified handle passing the given value to operation.</p>
     *
     * @param handle handle of the port on which to execute this ioctl operation.
     * @param operationCode unique ioctl operation code.
     * @param values the value to be passed to the IOCTL operation.
     * @return true if operation executed successfully.
     * @throws SerialComException if the operation can not be completed as requested.
     */
    public boolean ioctlSetValueIntArray(long handle, long operationCode, int[] values) throws SerialComException {
        long ret = 0;
        ret = mIOCTLJNIBridge.ioctlSetValueIntArray(handle, operationCode, values);
        if(ret < 0) {
            throw new SerialComException("Could not execute the given IOCTL operation. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes the requested operation on the specified handle passing the given value to operation.</p>
     * 
     * <p>Although values argument is of type byte however this method can be also used if native ioctl requires 
     * argument to be of type unsigned char (C language).</p>
     *
     * @param handle handle of the port on which to execute this ioctl operation.
     * @param operationCode unique ioctl operation code.
     * @param values the value to be passed to the IOCTL operation.
     * @return true if operation executed successfully.
     * @throws SerialComException if the operation can not be completed as requested.
     */
    public boolean ioctlSetValueCharArray(long handle, long operationCode, byte[] values) throws SerialComException {
        long ret = 0;
        ret = mIOCTLJNIBridge.ioctlSetValueCharArray(handle, operationCode, values);
        if(ret < 0) {
            throw new SerialComException("Could not execute the given IOCTL operation. Please retry !");
        }
        return true;
    }
}
