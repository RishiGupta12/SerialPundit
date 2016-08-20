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

import com.serialpundit.core.SerialComException;
import com.serialpundit.core.SerialComSystemProperty;
import com.serialpundit.serial.internal.SerialComSLabsUSBXpressJNIBridge;

/**
 * <p>Silicon labs provides libraries to communicate with their USB-UART devices. More information can 
 * be found here : http://www.silabs.com/products/mcu/Pages/USBXpress.aspx</p>
 * 
 * <ul>
 * <li><p>The data types used in java layer may be bigger in size than the native layer. For example; if native 
 * function returns 16 bit signed integer, than java method will return 32 bit integer. This is done to make 
 * sure that no data loss occur. SerialPundit take care of sign and their applicability internally.</p></li>
 * 
 * <li>Developers are requested to check with vendor library documentation if a particular function is supported
 * for desired platform or not and also how does a particular API will behave. Also consider paying attention to 
 * valid values and range when passing arguments to a method.</li>
 * 
 * <li><p>The application note for USBXpress library is here : 
 * http://www.silabs.com/Support%20Documents/TechnicalDocs/an169.pdf</p></li>
 * 
 * <li>It seems like USBXpress library uses user space drivers. So if you encounter any problems 
 * with permissions add the following udev rules file at appropriate location in your system : 
 * <github repository>/tools-and-utilities/99-sp-cp210x.rules</li>
 * 
 * <li><p>SerialPundit version 1.0.4 is linked to v4.0 version of USBXpress from silicon labs.</p></li>
 * </ul>
 * 
 * @author Rishi Gupta
 */
public final class SerialComSLabsUSBXpress extends SerialComVendorLib {

    private final SerialComSLabsUSBXpressJNIBridge mSerialComSLabsUSBXpressJNIBridge;

    public static final int SI_RETURN_SERIAL_NUMBER = 0x00;
    public static final int SI_RETURN_DESCRIPTION = 0x01;
    public static final int SI_RETURN_LINK_NAME = 0x02;
    public static final int SI_RETURN_VID = 0x03;
    public static final int SI_RETURN_PID = 0x04;

    public static final int SI_RX_NO_OVERRUN = 0x01;
    public static final int SI_RX_EMPTY = 0x02;
    public static final int SI_RX_OVERRUN = 0x03;
    public static final int SI_RX_READY = 0x04;

    public static final int SI_HELD_INACTIVE = 0x01;
    public static final int SI_HELD_ACTIVE = 0x02;
    public static final int SI_FIRMWARE_CONTROLLED = 0x03;          
    public static final int SI_RECEIVE_FLOW_CONTROL = 0x04;
    public static final int SI_TRANSMIT_ACTIVE_SIGNAL = 0x05;
    public static final int SI_STATUS_INPUT = 0x06;
    public static final int SI_HANDSHAKE_LINE = 0x07;

    /**
     * <p>Allocates a new SerialComSLabsUSBXpress object, extract and load shared libraries as 
     * required.</p>
     * 
     * @param libDirectory directory in which native library will be extracted and vendor library will 
     *         be found.
     * @param vlibName name of vendor library to load and link.
     * @param cpuArch architecture of CPU this library is running on.
     * @param osType operating system this library is running on.
     * @param serialComSystemProperty instance of SerialComSystemProperty to get required java properties.
     * @throws SerialComUnexpectedException if a critical java system property is null in system.
     * @throws SecurityException if any java system property can not be accessed.
     * @throws FileNotFoundException if the vendor library file is not found.
     * @throws SerialComLoadException if any file system related issue occurs.
     * @throws UnsatisfiedLinkError if loading/linking shared library fails.
     * @throws SerialComException if initializing native library fails.
     */
    public SerialComSLabsUSBXpress(File libDirectory, String vlibName, int cpuArch, int osType, 
            SerialComSystemProperty serialComSystemProperty) throws SerialComException {

        mSerialComSLabsUSBXpressJNIBridge = new SerialComSLabsUSBXpressJNIBridge();
        SerialComSLabsUSBXpressJNIBridge.loadNativeLibrary(libDirectory, vlibName, cpuArch, osType, serialComSystemProperty);
    }

    /**
     * <p>Executes SI_GetNumDevices function of USBXpress library.</p>
     * 
     * <p>Returns the number of devices connected to the host.</p>
     * 
     * @return number of the devices connected to host presently.
     * @throws SerialComException if an I/O error occurs.
     */
    public int getNumDevices() throws SerialComException {
        int ret = mSerialComSLabsUSBXpressJNIBridge.getNumDevices();
        if(ret < 0) {
            throw new SerialComException("Could not get the number of devices connected to host. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes SI_GetProductString function of USBXpress library.</p>
     * <p>Returns product description, serial number, vid, pid or full path based on flag passed.</p>
     * 
     * <p>The argument flag can be one of the constant SI_RETURN_SERIAL_NUMBER, SI_RETURN_DESCRIPTION, 
     * SI_RETURN_LINK_NAME, SI_RETURN_VID and SI_RETURN_PID.</p>
     * 
     * @param index index of device in list.
     * @param flag indicates which property is to be fetched.
     * @return product description, serial number or full path.
     * @throws SerialComException if an I/O error occurs.
     * @throws IllegalArgumentException if invalid flag is passed.
     */
    public String getProductString(int index, int flag) throws SerialComException {
        String ret = null;
        if((flag == SI_RETURN_SERIAL_NUMBER) || (flag == SI_RETURN_DESCRIPTION) || (flag == SI_RETURN_LINK_NAME) 
                || (flag == SI_RETURN_VID) || (flag == SI_RETURN_PID)) {
            ret = mSerialComSLabsUSBXpressJNIBridge.getProductString(index, flag);
            if(ret == null) {
                throw new SerialComException("Could not get the requested information. Please retry !");
            }
            return ret;
        }

        throw new IllegalArgumentException("Invalid flag passed for requested operation !");
    }

    /**
     * <p>Executes SI_Open function of USBXpress library.</p>
     * 
     * <p>Open the device and return a handle which will be used for subsequent accesses.</p>
     * 
     * @param index of the device that needs to be opened.
     * @return handle of the opened device or -1 if method fails.
     * @throws SerialComException if an I/O error occurs.
     * @throws IllegalArgumentException if index is negative.
     */
    public long open(final int index) throws SerialComException {
        if(index < 0) {
            throw new IllegalArgumentException("Argument index can not be negative !");
        }
        long handle = mSerialComSLabsUSBXpressJNIBridge.open(index);
        if(handle < 0) {
            throw new SerialComException("Could not open the requested device at given index. Please retry !");
        }else {
            return handle;
        }
    }

    /**
     * <p>Executes SI_Close function of USBXpress library.</p>
     * 
     * <p>Closes an opened device.</p>
     * 
     * @param handle of the device that is to be close.
     * @return true on success.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean close(final long handle) throws SerialComException {
        int ret = mSerialComSLabsUSBXpressJNIBridge.close(handle);
        if(ret < 0) {
            throw new SerialComException("Could not close the requested device. Please retry !");
        }

        return true;
    }

    /**
     * <p>Executes SI_Read function of USBXpress library.</p>
     * 
     * <p>Read data from the device.</p>
     * 
     * @param handle handle of the device from which to read data.
     * @param buffer byte buffer where data read will be placed.
     * @param numOfBytesToRead number of bytes to be tried to read.
     * @return number of bytes read.
     * @throws SerialComException if an I/O error occurs.
     * @throws IllegalArgumentException if buffer is null or numOfBytesToRead is negative or zero.
     */
    public int read(long handle, final byte[] buffer, int numOfBytesToRead) throws SerialComException {
        if(buffer == null) {
            throw new IllegalArgumentException("Argument buffer can not be null !");
        }
        if(numOfBytesToRead <= 0) {
            throw new IllegalArgumentException("Argument numOfBytesToRead can not be negative or zero !");
        }
        int ret = mSerialComSLabsUSBXpressJNIBridge.read(handle, buffer, numOfBytesToRead);
        if(ret < 0) {
            throw new SerialComException("Could not read the data from the requested device. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes SI_Write function of USBXpress library.</p>
     * 
     * <p>Write data from given buffer to the device.</p>
     * 
     * @param handle handle of the device to which data is to be sent.
     * @param buffer byte buffer that contains the data to be written to the device.
     * @param numOfBytesToWrite Number of bytes to write to the device.
     * @return number of bytes written to the device.
     * @throws SerialComException if an I/O error occurs.
     * @throws IllegalArgumentException if buffer is null or numOfBytesToWrite is negative or zero.
     */
    public int write(long handle, final byte[] buffer, int numOfBytesToWrite) throws SerialComException {
        if(buffer == null) {
            throw new IllegalArgumentException("Argument buffer can not be null !");
        }
        if(numOfBytesToWrite <= 0) {
            throw new IllegalArgumentException("Argument numOfBytesToWrite can not be negative or zero !");
        }
        int ret = mSerialComSLabsUSBXpressJNIBridge.write(handle, buffer, numOfBytesToWrite);
        if(ret < 0) {
            throw new SerialComException("Could not send data to the requested device. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes SI_CancelIo function of USBXpress library.</p>
     * 
     * <p>Cancels any pending IO on a device opened with an OVERLAPPED object.</p>
     * 
     * @param handle handle of the device whose pending IO operations are to be cancelled.
     * @return true if the operation executed successfully.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean cancelIO(final long handle) throws SerialComException {
        int ret = mSerialComSLabsUSBXpressJNIBridge.cancelIO(handle);
        if(ret < 0) {
            throw new SerialComException("Could not cancel the IO operations in progress. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes SI_FlushBuffers function of USBXpress library.</p>
     * 
     * <p>Flushes buffers of device or drivers as requested through additional arguments.</p>
     * 
     * @param handle of the device whose buffer is to be flushed.
     * @param flushTransmit indicates whether transmit buffer is to be flushed or not.
     * @param flushReceive indicates whether receive buffer is to be flushed or not.
     * @return true on success.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean flushBuffer(final long handle, byte flushTransmit, byte flushReceive) throws SerialComException {
        int ret = mSerialComSLabsUSBXpressJNIBridge.flushBuffer(handle, flushTransmit, flushReceive);
        if(ret < 0) {
            throw new SerialComException("Could not flush the buffers as requested. Please retry !");
        }

        return true;
    }

    /**
     * <p>Executes SI_SetTimeouts function of USBXpress library.</p>
     * 
     * <p>Sets the read and write time out values for the given device.</p>
     * 
     * @param readTimeOut read time out in milliseconds.
     * @param writeTimeOut write time out in milliseconds.
     * @return true if the operation executed successfully.
     * @throws SerialComException if an I/O error occurs.
     * @throws IllegalArgumentException if divisor is negative.
     */
    public boolean setTimeouts(long readTimeOut, long writeTimeOut) throws SerialComException {
        if(readTimeOut < 0) {
            throw new IllegalArgumentException("Argument readTimeOut can not be negative !");
        }
        if(writeTimeOut < 0) {
            throw new IllegalArgumentException("Argument writeTimeOut can not be negative !");
        }

        int ret = mSerialComSLabsUSBXpressJNIBridge.setTimeouts(readTimeOut, writeTimeOut);
        if(ret < 0) {
            throw new SerialComException("Could not set the desired timeout values for the requested device. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes SI_GetTimeouts function of USBXpress library.</p>
     * 
     * <p>Gets the current read and write time out values for the given device.</p>
     * 
     * @param handle handle of the device whose timeout values need to be fetched.
     * @return array of timeout in sequence of read timeout and write out at index 0 and 1 respectively.
     * @throws SerialComException if an I/O error occurs.
     */
    public long[] getTimeouts(final long handle, long readTimeOut, long writeTimeOut) throws SerialComException {
        long[] ret = mSerialComSLabsUSBXpressJNIBridge.getTimeouts();
        if(ret == null) {
            throw new SerialComException("Could not get the timeout values for the requested device. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes SI_CheckRXQueue function of USBXpress library.</p>
     * 
     * <p>Returns the number of bytes in the receive queue and a status value that indicates if an overrun 
     * (SI_QUEUE_OVERRUN) has occurred and if the RX queue is ready (SI_QUEUE_READY) for reading. The application 
     * can compare return value with constant SI_RX_XXXXX defined in this class.</p>
     * 
     * @param handle handle of the device whose status need to be fetched.
     * @return array containing number of bytes and flag at index 0 and 1 respectively.
     * @throws SerialComException if an I/O error occurs.
     */
    public long[] checkRXQueue(final long handle) throws SerialComException {
        long[] ret = mSerialComSLabsUSBXpressJNIBridge.checkRXQueue(handle);
        if(ret == null) {
            throw new SerialComException("Could not get the requested values for the given device. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes SI_SetBaudRate function of USBXpress library.</p>
     * 
     * <p>Sets the Baud Rate. Refer to the device data sheet for a list of Baud Rates supported by the device.</p>
     * 
     * @param handle of the device whose baud rate is to be set.
     * @param baudrate baud rate value to set.
     * @return true on success.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean setBaudRate(long handle, int baudrate) throws SerialComException {
        int ret = mSerialComSLabsUSBXpressJNIBridge.setBaudRate(handle, baudrate);
        if(ret < 0) {
            throw new SerialComException("Could not set the baud rate. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes SI_SetBaudDivisor function of USBXpress library.</p>
     * 
     * <p>Sets the Baud Rate directly by using a specific divisor value. This function is obsolete; 
     * use SI_SetBaudRate instead.</p>
     * 
     * @param handle of the device whose baud rate is to be set.
     * @param divisor divisor value to set.
     * @return true on success.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean setBaudDivisor(long handle, int divisor) throws SerialComException {
        int ret = mSerialComSLabsUSBXpressJNIBridge.setBaudRate(handle, divisor);
        if(ret < 0) {
            throw new SerialComException("Could not set the given divisor. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes SI_SetLineControl function of USBXpress library.</p>
     * 
     * <p>Adjusts the line control settings: word length, stop bits, and parity. Refer to the device data sheet 
     * for valid line control settings.</p>
     * 
     * @param handle of the device whose baud control settings is to be set.
     * @param lineControl bit mask of line control settings.
     * @return true on success.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean setLineControl(long handle, int lineControl) throws SerialComException {
        int ret = mSerialComSLabsUSBXpressJNIBridge.setLineControl(handle, lineControl);
        if(ret < 0) {
            throw new SerialComException("Could not set the line control settings. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes SI_SetFlowControl function of USBXpress library.</p>
     * 
     * <p>Adjusts the following flow control settings: set hardware handshaking, software 
     * handshaking, and modem control signals.</p>
     * 
     * @param handle of the device whose flow control settings is to be set.
     * @param ctsMaskCode can be SI_STATUS_INPUT or SI_HANDSHAKE_LINE.
     * @param rtsMaskCode can be SI_HELD_INACTIVE, SI_HELD_ACTIVE, SI_FIRMWARE_CONTROLLED or 
     *         SI_TRANSMIT_ACTIVE_SIGNAL.
     * @param dtrMaskCode can be SI_HELD_INACTIVE, SI_HELD_ACTIVE or SI_FIRMWARE_CONTROLLED.
     * @param dsrMaskCode can be SI_STATUS_INPUT or SI_HANDSHAKE_LINE.
     * @param dcdMaskCode can be SI_STATUS_INPUT or SI_HANDSHAKE_LINE.
     * @param flowXonXoff Sets software flow control to be off if the value is 0, and on using the 
     *         character value specified if value is non-zero.
     * @return true on success.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean setFlowControl(long handle, int ctsMaskCode,int rtsMaskCode, int dtrMaskCode, 
            int dsrMaskCode, int dcdMaskCode, int flowXonXoff) throws SerialComException {
        int ret = mSerialComSLabsUSBXpressJNIBridge.setFlowControl(handle, ctsMaskCode, rtsMaskCode, 
                dtrMaskCode, dsrMaskCode, dcdMaskCode, flowXonXoff);
        if(ret < 0) {
            throw new SerialComException("Could not set the flow control settings. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes SI_GetModemStatus function of USBXpress library.</p>
     * 
     * <p>Gets the Modem Status from the device. This includes the modem pin states.</p>
     * 
     * @param handle of the device whose modem status is to be fetched.
     * @return modem status in following sequence DTR status bit 0, RTS status bit 1, 
     *          CTS state bit 4, DSR state bit 5, RI state bit 6 and DCD state bit 7.
     * @throws SerialComException if an I/O error occurs.
     */
    public byte getModemStatus(long handle) throws SerialComException {
        int ret = mSerialComSLabsUSBXpressJNIBridge.getModemStatus(handle);
        if(ret < 0) {
            throw new SerialComException("Could not get modem status. Please retry !");
        }
        return (byte)ret;
    }

    /**
     * <p>Executes SI_SetBreak function of USBXpress library.</p>
     * 
     * <p>Sends a break state (transmit or reset) to a CP210x device. Note that this function 
     * is not necessarily synchronized with queued transmit data.</p>
     * 
     * @param handle of the device for whom break condition is to be set as given.
     * @param breakValue break is reset if this is 0x0000 and break is transmitted if this is 0x0001.
     * @return true if break condition set as given.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean setBreak(long handle, int breakValue) throws SerialComException {
        int ret = mSerialComSLabsUSBXpressJNIBridge.setBreak(handle, breakValue);
        if(ret < 0) {
            throw new SerialComException("Could not set the break condition as given. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes SI_ReadLatch function of USBXpress library.</p>
     * 
     * <p>Gets the current port latch value (least significant four bits) from the device.</p>
     * 
     * @param handle handle of the opened COM port.
     * @return GPIO latch value [Logic High = 1, Logic Low = 0].
     * @throws SerialComException if an I/O error occurs.
     */
    public long readLatch(final long handle) throws SerialComException {
        long ret = mSerialComSLabsUSBXpressJNIBridge.readLatch(handle);
        if(ret < 0) {
            throw new SerialComException("Could not read the port latch value for given device. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes SI_WriteLatch function of USBXpress library.</p>
     * 
     * <p>Sets the current port latch value (least significant four bits) from the device.</p>
     * 
     * @param handle handle of the opened COM port.
     * @param mask determines which pins to change [Change = 1, Leave = 0].
     * @param latchValue value to write to GPIO latch [Logic High = 1, Logic Low = 0].
     * @return true if value gets set successfully.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean writeLatch(final long handle, long mask, long latchValue) throws SerialComException {
        int ret = mSerialComSLabsUSBXpressJNIBridge.writeLatch(handle, mask, latchValue);
        if(ret < 0) {
            throw new SerialComException("Could not write the given latch value on the given device. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes SI_GetPartNumber function of USBXpress library.</p>
     * 
     * <p>Retrieves the part number of the CP210x device for a given handle.</p>
     * 
     * @param handle handle of the device for whose part number is to found.
     * @return part number associated with the given handle.
     * @throws SerialComException if an I/O error occurs.
     */
    public String getPartNumber(long handle) throws SerialComException {
        String ret = mSerialComSLabsUSBXpressJNIBridge.getPartNumber(handle);
        if(ret == null) {
            throw new SerialComException("Could not get the part number. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes SI_DeviceIOControl function of USBXpress library.</p>
     * 
     * <p>Interface for any miscellaneous device control functions. A separate call to SI_DeviceIOControl 
     * is required for each input or output operation. A single call cannot be used to perform both an 
     * input and output operation simultaneously.</p>
     * 
     * @param handle handle of the device for whom IO control operation is to be performed.
     * @param inputBuf buffer in which data bytes will be saved.
     * @param numBytesToRead number of bytes to read from port and place in input buffer.
     * @param outputBuf buffer which contains data to be written to serial port.
     * @param numOfBytesToWrite number of data bytes to write to port.
     * @return number of bytes read if operation was read, number of written if operation was to write data.
     * @throws SerialComException if an I/O error occurs.
     */
    public int deviceIOControl(long handle, int ctrlCode, byte[] inputBuf, int numBytesToRead, 
            byte[] outputBuf, int numOfBytesToWrite) throws SerialComException {
        int ret = mSerialComSLabsUSBXpressJNIBridge.deviceIOControl(handle, ctrlCode, inputBuf, 
                numBytesToRead, outputBuf, numOfBytesToWrite);
        if(ret < 0) {
            throw new SerialComException("Could not perform the IO control operation. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes SI_GetDLLVersion function of USBXpress library.</p>
     * 
     * <p>Obtains the version of the DLL that is currently in use.</p>
     * 
     * @return driver version number for the requested device handle.
     * @throws SerialComException if an I/O error occurs.
     */
    public long getDllVersion() throws SerialComException {
        long ret = mSerialComSLabsUSBXpressJNIBridge.getDllVersion();
        if(ret < 0) {
            throw new SerialComException("Could not get the dll version for the requested device. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes SI_GetDriverVersion function of USBXpress library.</p>
     * 
     * <p>Obtains the version of the Driver that is currently in the Windows System directory.</p>
     * 
     * @return driver version number for the requested device handle.
     * @throws SerialComException if an I/O error occurs.
     */
    public long getDriverVersion() throws SerialComException {
        long ret =  mSerialComSLabsUSBXpressJNIBridge.getDriverVersion();
        if(ret < 0) {
            throw new SerialComException("Could not get the driver version for the requested device. Please retry !");
        }
        return ret;
    }
}
