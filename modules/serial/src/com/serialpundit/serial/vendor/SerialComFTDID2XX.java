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
import com.serialpundit.serial.internal.SerialComFTDID2XXJNIBridge;

/**
 * <p>FTDI provides two interfaces for their USB-UART ICs; first interface is Virtual COM port and 
 * second is provided via a proprietary DLL (a.k.a. D2XX). The D2XX interface provides special 
 * functions that are not available in standard operating system COM port APIs, such as setting the 
 * device into a different mode or writing data into the device EEPROM.</p>
 * 
 * <p>Using this interface requires FTDI drivers to be installed. For Windows FTDI provides CDM package.
 * For Linux and Mac os x, FTDI drivers are provided but default drivers need to be un-installed.</p>
 * 
 * <p>More information about D2XX is here : http://www.ftdichip.com/Drivers/D2XX.htm </p>
 * 
 * <ul>
 * <li>The data types used in java layer may be bigger in size than the native layer. For example; 
 * if native function returns 16 bit signed integer, than java method will return 32 bit integer. This 
 * is done to make sure that no data loss occur. SerialPundit takes care of sign and their applicability 
 * internally.</li>
 * 
 * <li><p>Developers are requested to check with vendor library documentation if a particular function 
 * is supported for desired platform or not and also how does a particular API will behave. Also consider 
 * paying attention to valid values and range when passing arguments to a method. For FTDI d2xx the API 
 * guide is here : 
 * http://www.ftdichip.com/Support/Documents/ProgramGuides/D2XX_Programmer's_Guide(FT_000071).pdf </p></li>
 * 
 * <li>The VCP and D2XX drivers are mutually exclusive for Linux OS and therefore either udev rule can be 
 * used to unbind/unload default drivers or it can be done manually.
 * <github repository>/tools-and-utilities/udev-ftdi-unbind-ftdi_sio.sh
 * <github repository>/tools-and-utilities/udev-ftdi-unload-vcp-driver.sh
 * </li>
 *
 * <p>The udev rules to support various applications designs are here : 
 * <github repository>/tools-and-utilities/99-sp-extra-udev.rules</p>
 *
 * <li><p>It seems like d2xx drivers are user space usb drivers using libusb. So if you encounter any 
 * problems with permissions add following udev rules : 
 * <github repository>/tools-and-utilities/99-sp-ftdi-d2xx.rules</p></li>
 * 
 * <li>The application notes for FTDI devices are here : http://www.ftdichip.com/Support/Documents/AppNotes.htm</li>
 * 
 * <li><p>SerialPundit version 1.0.4 is linked to d2xx 1.3.6 version for Linux, 2.12.06 for Windows and 1.2.2 for Mac os x.</p></li>
 * </ul>
 * 
 * @author Rishi Gupta
 */
public final class SerialComFTDID2XX extends SerialComVendorLib {

    /** <p>Pre-defined enum constants for number of data bits (word length) in a serial frame. </p>*/
    public enum DATABITS {
        /** <p>Number of data bits in one frame is 7 bits. </p>*/
        FT_BITS_7(7),
        /** <p>Number of data bits in one frame is 8 bits. </p>*/
        FT_BITS_8(8);
        private int value;
        private DATABITS(int value) {
            this.value = value;	
        }
        public int getValue() {
            return this.value;
        }
    }

    /** <p>Pre-defined enum constants for number of stop bits in a serial frame. </p>*/
    public enum STOPBITS {
        /** <p>Number of stop bits in one frame is 1. </p>*/
        FT_STOP_BITS_1(1),
        /** <p>Number of stop bits in one frame is 2. </p>*/
        FT_STOP_BITS_2(2);
        private int value;
        private STOPBITS(int value) {
            this.value = value;	
        }
        public int getValue() {
            return this.value;
        }
    }

    /** <p>Pre-defined enum constants for enabling type of parity in a serial frame. </p>*/
    public enum PARITY {
        /** The uart frame does not contain any parity bit. Errors are handled by application for example 
         * using CRC algorithm.*/
        FT_PARITY_NONE(1),
        /** <p>The number of bits in the frame with the value one is odd. If the sum of bits 
         * with a value of 1 is odd in the frame, the parity bit's value is set to zero. 
         * If the sum of bits with a value of 1 is even in the frame, the parity bit value 
         * is set to 1, making the total count of 1's in the frame an odd number. </p>*/
        FT_PARITY_ODD(2),
        /** <p>The number of bits in the frame with the value one is even. The number 
         * of bits whose value is 1 in a frame is counted. If that total is odd, 
         * the parity bit value is set to 1, making the total count of 1's in the frame 
         * an even number. If the count of ones in a frame a is already even, 
         * the parity bit's value remains 0. </p>
         * <p>Odd parity may be more fruitful since it ensures that at least one state 
         * transition occurs in each character, which makes it more reliable as compared 
         * even parity. </p>
         * <p>Even parity is a special case of a cyclic redundancy check (CRC), 
         * where the 1-bit CRC is generated by the polynomial x+1.</p>*/
        FT_PARITY_EVEN(3),
        /** <p>The parity bit is set to the mark signal condition (logical 1). An application
         * may use the 9th (parity) bit for some form of addressing or special signaling. </p>*/
        FT_PARITY_MARK(4),
        /** <p>The parity bit is set to the space signal condition (logical 0). The mark 
         * and space parity is uncommon, as it adds no error detection information. </p>*/
        FT_PARITY_SPACE(5);
        private int value;
        private PARITY(int value) {
            this.value = value;	
        }
        public int getValue() {
            return this.value;
        }
    }

    /** <p>Pre-defined enum constants for flow control of a serial frame.</p>*/
    public enum FLOWCTRL {
        /**<p>No flow control. Application is responsible to manage data buffers.</p>*/
        FT_FLOW_NONE(1),
        /**<p>Flow control through RTS and CTS lines.</p>*/
        FT_FLOW_RTS_CTS(2),
        /**<p>Flow control through DTR and DSR lines.</p>*/
        FT_FLOW_DTR_DSR(3),
        /**<p>Flow control through software. </p>*/
        FT_FLOW_XON_XOFF(4);
        private int value;
        private FLOWCTRL(int value) {
            this.value = value;	
        }
        public int getValue() {
            return this.value;
        }
    }

    /**<p>Value indicating devices unknown to D2XX library.</p>*/
    public static final int FT_DEVICE_UNKNOWN = 0X01;

    /**<p>Value indicating FTDI AM devices.</p>*/
    public static final int FT_DEVICE_AM = 0x02;

    /**<p>Value indicating FTDI BM devices.</p>*/
    public static final int FT_DEVICE_BM = 0x03;

    /**<p>Value indicating FTDI 100AX devices.</p>*/
    public static final int FT_DEVICE_100AX = 0X04;

    /**<p>Value indicating FTDI 232B (FT2xxB) devices.</p>*/
    public static final int FT_DEVICE_232B = 0X05;

    /**<p>Value indicating FTDI 232R devices.</p>*/
    public static final int FT_DEVICE_232R = 0X06;

    /**<p>Value indicating FTDI 232H devices.</p>*/
    public static final int FT_DEVICE_232H = 0X07;

    /**<p>Value indicating FTDI 2232C devices.</p>*/
    public static final int FT_DEVICE_2232C = 0X08;

    /**<p>Value indicating FTDI 2232H devices.</p>*/
    public static final int FT_DEVICE_2232H = 0X09;

    /**<p>Value indicating FTDI 4232H devices.</p>*/
    public static final int FT_DEVICE_4232H = 0X10;

    /**<p>Value indicating FTDI X (FT2xxX) series devices.</p>*/
    public static final int FT_DEVICE_X_SERIES = 0X11;

    /**<p>Bit mask to represent opening device for reading in D2XX terminology. </p>*/
    public static final int GENERIC_READ = 0x01;  // 0000001

    /**<p>Bit mask to represent opening device for writing in D2XX terminology. </p>*/
    public static final int GENERIC_WRITE = 0x02;  // 0000010

    /**<p>Bit mask to represent FT_LIST_NUMBER_ONLY in D2XX terminology. </p>*/
    public static final int FT_LIST_NUMBER_ONLY = 0x01;  // 0000001

    /**<p>Bit mask to represent FT_LIST_BY_INDEX in D2XX terminology. </p>*/
    public static final int FT_LIST_BY_INDEX = 0x02;  // 0000010

    /**<p>Bit mask to represent FT_LIST_ALL in D2XX terminology. </p>*/
    public static final int FT_LIST_ALL = 0x04;  // 0000100

    /**<p>Bit mask to represent FT_OPEN_BY_SERIAL_NUMBER in D2XX terminology. </p>*/
    public static final int FT_OPEN_BY_SERIAL_NUMBER  = 0x08;  // 0001000

    /**<p>Bit mask to represent FT_OPEN_BY_DESCRIPTION in D2XX terminology. </p>*/
    public static final int FT_OPEN_BY_DESCRIPTION = 0x10;  // 0010000

    /**<p>Bit mask to represent FT_OPEN_BY_LOCATION in D2XX terminology. </p>*/
    public static final int FT_OPEN_BY_LOCATION = 0x20;  // 0100000

    /**<p>Constant indicating that the RTS signal need to be set. </p>*/
    public static final int SETRTS = 1;

    /**<p>Constant indicating that the RTS signal need to be un-set. </p>*/
    public static final int CLRRTS = 2;

    /**<p>Constant indicating that the DTR signal need to be set. </p>*/
    public static final int SETDTR = 3;

    /**<p>Constant indicating that the DTR signal need to be un-set. </p>*/
    public static final int CLRDTR = 4;

    /**<p>Constant indicating that the break condition need to be set. </p>*/
    public static final int SETBREAK = 5;

    /**<p>Constant indicating that the break condition need to be cleared. </p>*/
    public static final int CLRBREAK = 6;

    /**<p>Bit mask to represent modem status in D2XX terminology. </p>*/
    public static final int MS_CTS_ON = 0x01;

    /**<p>Bit mask to represent modem status in D2XX terminology. </p>*/
    public static final int MS_DSR_ON = 0x02;

    /**<p>Bit mask to represent modem status in D2XX terminology. </p>*/
    public static final int MS_RING_ON = 0x04;

    /**<p>Bit mask to represent modem status in D2XX terminology. </p>*/
    public static final int MS_RLSD_ON = 0x08;

    /**<p>Bit mask to represent an event in D2XX terminology. </p>*/
    public static final int EV_RXCHAR = 0x0001;  // Any Character received

    /**<p>Bit mask to represent an event in D2XX terminology. </p>*/
    public static final int EV_RXFLAG = 0x0002;  // Received certain character

    /**<p>Bit mask to represent an event in D2XX terminology. </p>*/
    public static final int EV_TXEMPTY = 0x0004; // Transmit Queue Empty

    /**<p>Bit mask to represent an event in D2XX terminology. </p>*/
    public static final int EV_CTS = 0x0008;  // CTS changed state

    /**<p>Bit mask to represent an event in D2XX terminology. </p>*/
    public static final int EV_DSR = 0x0010;  // DSR changed state

    /**<p>Bit mask to represent an event in D2XX terminology. </p>*/
    public static final int EV_RLSD = 0x0020;  // RLSD changed state

    /**<p>Bit mask to represent an event in D2XX terminology. </p>*/
    public static final int EV_BREAK = 0x0040;  // BREAK received

    /**<p>Bit mask to represent an event in D2XX terminology. </p>*/
    public static final int EV_ERR = 0x0080;  // Line status error occurred

    /**<p>Bit mask to represent an event in D2XX terminology. </p>*/
    public static final int EV_RING = 0x0100;  // Ring signal detected

    /**<p>Bit mask to represent an event in D2XX terminology. </p>*/
    public static final int EV_PERR = 0x0200;  // Printer error occured

    /**<p>Bit mask to represent an event in D2XX terminology. </p>*/
    public static final int EV_RX80FULL  = 0x0400;  // Receive buffer is 80 percent full

    /**<p>Bit mask to represent an event in D2XX terminology. </p>*/
    public static final int EV_EVENT1  = 0x0800;  // Provider specific event 1

    /**<p>Bit mask to represent an event in D2XX terminology. </p>*/
    public static final int EV_EVENT2 = 0x1000; // Provider specific event 2

    /**<p>Bit mask to represent killing all current and pending transmission operations in 
     * D2XX terminology.</p>*/
    public static final int PURGE_TXABORT = 0x0001;

    /**<p>Bit mask to represent killing all current and pending receive operations in D2XX 
     * terminology.</p>*/
    public static final int PURGE_RXABORT = 0x0002;

    /**<p>Bit mask to represent clearing transmit queue in D2XX terminology.</p>*/
    public static final int PURGE_TXCLEAR = 0x0004;

    /**<p>Bit mask to represent clearing receive queue in D2XX terminology.</p>*/
    public static final int PURGE_RXCLEAR = 0x0008;

    /**<p>Bit mask to represent receive queue overflow in D2XX terminology.</p>*/
    public static final int CE_RXOVER   = 0x0001;

    /**<p>Bit mask to represent receive over run in D2XX terminology.</p>*/
    public static final int CE_OVERRUN  = 0x0002;

    /**<p>Bit mask to represent receive parity error in D2XX terminology.</p>*/
    public static final int CE_RXPARITY = 0x0004;

    /**<p>Bit mask to represent receive framing error in D2XX terminology.</p>*/
    public static final int CE_FRAME    = 0x0008;

    /**<p>Bit mask to represent break detection in D2XX terminology.</p>*/
    public static final int CE_BREAK    = 0x0010;

    /**<p>Bit mask to represent that transmit queue is full in D2XX terminology.</p>*/
    public static final int CE_TXFULL   = 0x0100;

    /**<p>Bit mask to represent LPTx timeout in D2XX terminology.</p>*/
    public static final int CE_PTO      = 0x0200;

    /**<p>Bit mask to represent LPTx I/O error in D2XX terminology.</p>*/
    public static final int CE_IOE      = 0x0400;

    /**<p>Bit mask to represent LPTx device not selected in D2XX terminology.</p>*/
    public static final int CE_DNS      = 0x0800;

    /**<p>Bit mask to represent LPTx out of paper in D2XX terminology.</p>*/
    public static final int CE_OOP      = 0x1000;

    /**<p>Bit mask to represent requested mode unsupported in D2XX terminology.</p>*/
    public static final int CE_MODE     = 0x8000;

    private final SerialComFTDID2XXJNIBridge mFTDID2XXJNIBridge;

    /**
     * <p>Allocates a new SerialComFTDID2XX object and extract and load shared libraries as required.</p>
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
    public SerialComFTDID2XX(File libDirectory, String vlibName, int cpuArch, int osType, 
            SerialComSystemProperty serialComSystemProperty) throws SerialComException {

        mFTDID2XXJNIBridge = new SerialComFTDID2XXJNIBridge();
        SerialComFTDID2XXJNIBridge.loadNativeLibrary(libDirectory, vlibName, cpuArch, osType, serialComSystemProperty);
    }

    /* ********************* D2XX Classic Functions ******************** */

    /**
     * <p>Executes FT_SetVIDPID function of D2XX library.</p>
     * 
     * <p>By default, the d2xx driver will support a limited set of VID and PID matched devices 
     * (VID 0x0403 with PIDs 0x6001, 0x6010, 0x6006 only). In order to use the driver with 
     * other VID and PID combinations this method should be called so that the driver can 
     * update its internal device list table.</p>
     * 
     * @param vid USB-IF vendor id of the USB device.
     * @param pid product id of the USB device.
     * @return true if requested operation was successful.
     * @throws SerialComException if an I/O error occurs.
     * @throws IllegalArgumentException if vid or pid is negative or invalid number.
     */
    public boolean setVidPid(int vid, int pid) throws SerialComException {
        if((vid < 0) || (vid > 0XFFFF)) {
            throw new IllegalArgumentException("Argument vid can not be negative or greater tha 0xFFFF !");
        }
        if((pid < 0) || (pid > 0XFFFF)) {
            throw new IllegalArgumentException("Argument pid can not be negative or greater tha 0xFFFF !");
        }
        int ret = mFTDID2XXJNIBridge.setVidPid(vid, pid);
        if(ret < 0) { /* extra check */
            throw new SerialComException("Could not set the VID and PID combination. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes FT_GetVIDPID function of D2XX library.</p>
     * 
     * <p>Retrieves the current VID and PID combination from within the internal device 
     * list table. The sequence of return array is USB VID and USB PID.</p>
     * 
     * @return USB vid and pid combination from within the internal device list table.
     * @throws SerialComException if an I/O error occurs.
     */
    public int[] getVidPid() throws SerialComException {
        int[] combination = null;
        combination = mFTDID2XXJNIBridge.getVidPid();
        if(combination == null) { /* extra check */
            throw new SerialComException("Could not get the VID and PID values. Please retry !");
        }
        return combination;
    }

    /**
     * <p>Executes FT_CreateDeviceInfoList function of D2XX library.</p>
     * 
     * <p>Returns the number of FTDI devices connected to the system presently.
     * If any device is removed or added to the system this method should be 
     * called again so that internal list can be updated by driver.</p>
     * 
     * @return number of FTDI devices connected to the system at the time this method is called.
     * @throws SerialComException if an I/O error occurs.
     */
    public int createDeviceInfoList() throws SerialComException {
        int ret = mFTDID2XXJNIBridge.createDeviceInfoList();
        if(ret < 0) { /* extra check */
            throw new SerialComException("Could not create device info list. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes FT_GetDeviceInfoList function of D2XX library.</p>
     * 
     * <p>Retrieves information about the connected devices and populate them in FTdevicelistInfoNode 
     * class objects.</p>
     * 
     * @param numOfDevices number of FTDI devices connected to system.
     * @return array of device info list (list of FT_DEVICE_LIST_INFO_NODE structure).
     * @throws SerialComException if an I/O error occurs.
     * @throws IllegalArgumentException if numOfDevices is negative or zero.
     */
    public FTdevicelistInfoNode[] getDeviceInfoList(final int numOfDevices) throws SerialComException {
        int i = 0;
        int numOfDev = 0;
        FTdevicelistInfoNode[] infoList = null;
        String[] rawDataList = null;

        if(numOfDevices <= 0) {
            throw new IllegalArgumentException("getDeviceInfoList(), " + "Argument numOfDevices can not be negative or zero !");
        }

        rawDataList = mFTDID2XXJNIBridge.getDeviceInfoList(numOfDevices);
        if(rawDataList != null) {
            numOfDev = rawDataList.length / 7;
            infoList = new FTdevicelistInfoNode[numOfDev];
            for(int x=0; x<numOfDev; x++) {
                infoList[x] = new FTdevicelistInfoNode(rawDataList[i], rawDataList[i+1], rawDataList[i+2], 
                        rawDataList[i+3], rawDataList[i+4], rawDataList[i+5], rawDataList[i+6]);
                i = i + 7;
            }
            return infoList;
        }else {
            return new FTdevicelistInfoNode[] { };
        }	
    }

    /**
     * <p>Executes FT_GetDeviceInfoDetail function of D2XX library.</p>
     * 
     * <p>Retrieves information about the device at the given index.</p>
     * 
     * @param index in list corresponding to the device for which information is to be obtained.
     * @return an object of type FTdevicelistInfoNode containing details of requested device or null.
     * @throws SerialComException if an I/O error occurs.
     * @throws IllegalArgumentException if index is negative.
     */
    public FTdevicelistInfoNode getDeviceInfoDetail(final int index) throws SerialComException {
        if(index < 0) {
            throw new IllegalArgumentException("getDeviceInfoDetail(), " + "Argument index can not be zero !");
        }
        String[] rawData = mFTDID2XXJNIBridge.getDeviceInfoDetail(index);
        if(rawData != null) {
            return new FTdevicelistInfoNode(rawData[0], rawData[1], rawData[2], rawData[3], rawData[4], rawData[5], rawData[6]);
        }else {
            return null;
        }
    }

    /**
     * <p>Executes FT_ListDevices function of D2XX library.</p>
     * 
     * <p>Gets information concerning the devices currently connected. This function can return information 
     * such as the number of devices connected, the device serial number and device description strings, and 
     * the location IDs of connected devices.</p>
     * 
     * <p>The value of dwFlags bit mask can be composed or one or more of these constants: FT_LIST_NUMBER_ONLY, 
     * FT_LIST_BY_INDEX, FT_LIST_ALL, FT_OPEN_BY_SERIAL_NUMBER, FT_OPEN_BY_DESCRIPTION, FT_OPEN_BY_LOCATION.</p>
     * 
     * <p>Length of the returned array indicates number of FT devices found.</p>
     * 
     * @param pvArg1 index in FT device list.
     * @param dwFlags flag specifying what operation should be performed.
     * @return array of FTdeviceInfo types representing information requested or null if something fails.
     * @throws IllegalArgumentException if pvArg1 is negative or dwFlags is not one of the valid constants.
     * @throws SerialComException if an I/O error occurs.
     */
    public FTdeviceInfo[] listDevices(final int pvArg1, final int dwFlags) throws SerialComException {
        int i = 0;
        int numOfDev = 0;
        FTdeviceInfo[] infoList = null;
        if((pvArg1 < 0) || (dwFlags <= 0)) {
            throw new IllegalArgumentException("listDevices(), " + "Argument(s) are not as per specification !");
        }
        String[] rawDataList = mFTDID2XXJNIBridge.listDevices(pvArg1, dwFlags);
        if(rawDataList != null) {
            numOfDev = rawDataList.length / 3;
            infoList = new FTdeviceInfo[numOfDev];
            for(int x=0; x<numOfDev; x++) {
                infoList[x] = new FTdeviceInfo(rawDataList[i], rawDataList[i+1], rawDataList[i+2]);
                i = i + 3;
            }
            return infoList;
        }else {
            return new FTdeviceInfo[] { };
        }
    }

    /**
     * <p>Executes FT_Open function of D2XX library.</p>
     * 
     * <p>Open the device and return a handle which will be used for subsequent accesses.</p>
     * 
     * @param index in list corresponding to the device that needs to be opened.
     * @return handle of the opened device or -1 if method fails.
     * @throws SerialComException if an I/O error occurs.
     * @throws IllegalArgumentException if index is negative.
     */
    public long open(final int index) throws SerialComException {
        if(index < 0) {
            throw new IllegalArgumentException("Argument index can not be negative !");
        }
        long handle = mFTDID2XXJNIBridge.open(index);
        if(handle < 0) {
            throw new SerialComException("Could not open the requested device at given index. Please retry !");
        }else {
            return handle;
        }
    }

    /**
     * <p>Executes FT_OpenEx function of D2XX library.</p>
     * 
     * <p>Open the specified device and return a handle that will be used for subsequent accesses. 
     * The device can be specified by its serial number, device description or location.</p>
     * 
     * <p>The value of dwFlags bit mask can be composed or one or more of these constants: 
     * FT_OPEN_BY_SERIAL_NUMBER, FT_OPEN_BY_DESCRIPTION, FT_OPEN_BY_LOCATION.</p>
     * 
     * @param serialOrDescription serial number string or description string to identify the device to be opened.
     * @param locationId location ID of the device if it is to be opened using location ID.
     * @param dwFlags flag specifying what operation should be performed.
     * @return true on success.
     * @throws SerialComException if an I/O error occurs.
     */
    public long openEx(final String serialOrDescription, long locationId, int dwFlags) throws SerialComException {
        long ret = mFTDID2XXJNIBridge.openEx(serialOrDescription, locationId, dwFlags);
        if(ret < 0) {
            throw new SerialComException("Could not open the requested device using given " + serialOrDescription + ". Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes FT_Close function of D2XX library.</p>
     * 
     * <p>Closes an open FT device.</p>
     * 
     * @param handle of the device that is to be close.
     * @return true on success.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean close(final long handle) throws SerialComException {
        int ret = mFTDID2XXJNIBridge.close(handle);
        if(ret < 0) {
            throw new SerialComException("Could not close the requested device. Please retry !");
        }

        return true;
    }

    /**
     * <p>Executes FT_Read function of D2XX library.</p>
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
        int ret = mFTDID2XXJNIBridge.read(handle, buffer, numOfBytesToRead);
        if(ret < 0) {
            throw new SerialComException("Could not read the data from the requested device. Please retry !");
        }

        return ret;
    }

    /**
     * <p>Executes FT_Write function of D2XX library.</p>
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
        int ret = mFTDID2XXJNIBridge.write(handle, buffer, numOfBytesToWrite);
        if(ret < 0) {
            throw new SerialComException("Could not send data to the requested device. Please retry !");
        }

        return ret;
    }

    /**
     * <p>Executes FT_SetBaudRate function of D2XX library.</p>
     * 
     * <p>Sets the baud rate value for the given FT device.</p>
     * 
     * @param handle handle of the device whose baud rate need to be set.
     * @param baudRate baud rate value to set.
     * @return true if the operation executed successfully.
     * @throws SerialComException if an I/O error occurs.
     * @throws IllegalArgumentException if baudRate is negative.
     */
    public boolean setBaudRate(final long handle, int baudRate) throws SerialComException {
        if(baudRate < 0) {
            throw new IllegalArgumentException("Argument baudRate can not be negative !");
        }
        int ret = mFTDID2XXJNIBridge.setBaudRate(handle, baudRate);
        if(ret < 0) {
            throw new SerialComException("Could not set the desired baud rate value for the requested device. Please retry !");
        }

        return true;
    }

    /**
     * <p>Executes FT_SetDivisor function of D2XX library.</p>
     * 
     * <p>Sets the divisor value for the given FT device.</p>
     * 
     * @param handle handle of the device whose divisor is to be set.
     * @param divisor divisor to be used for setting correct custom baud rate.
     * @return true if the operation executed successfully.
     * @throws SerialComException if an I/O error occurs.
     * @throws IllegalArgumentException if divisor is negative.
     */
    public boolean setDivisor(final long handle, int divisor) throws SerialComException {
        if(divisor < 0) {
            throw new IllegalArgumentException("Argument divisor can not be negative !");
        }

        int ret = mFTDID2XXJNIBridge.setDivisor(handle, divisor);
        if(ret < 0) {
            throw new SerialComException("Could not set the desired divisor value for the requested device. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes FT_SetDataCharacteristics function of D2XX library.</p>
     * 
     * <p>Sets the desired data characteristics for the given FT device.</p>
     * 
     * @param handle handle of the device whose data characteristics need to be set.
     * @param dataBits number of data bits in one frame (refer DATABITS enum in SerialComFTDID2XX class for this).
     * @param stopBits number of stop bits in one frame (refer STOPBITS enum in SerialComFTDID2XX class for this).
     * @param parity of the frame (refer PARITY enum in SerialComFTDID2XX class for this).
     * @return true if the operation executed successfully.
     * @throws SerialComException if an I/O error occurs.
     * @throws IllegalArgumentException if dataBits, stopBits or parity is null.
     */
    public boolean setDataCharacteristics(final long handle, DATABITS dataBits, STOPBITS stopBits, PARITY parity) throws SerialComException {
        if(dataBits == null) {
            throw new IllegalArgumentException("Argument dataBits can not be null !");
        }
        if(stopBits == null) {
            throw new IllegalArgumentException("Argument stopBits can not be null !");
        }
        if(parity == null) {
            throw new IllegalArgumentException("Argument parity can not be null !");
        }

        int ret = mFTDID2XXJNIBridge.setDataCharacteristics(handle, dataBits.getValue(), stopBits.getValue(), parity.getValue());
        if(ret < 0) {
            throw new SerialComException("Could not set the desired data characteristics for the requested device. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes FT_SetTimeouts function of D2XX library.</p>
     * 
     * <p>Sets the read and write time out values for the given FT device.</p>
     * 
     * @param handle handle of the device whose timeouts need to be set.
     * @param readTimeOut read time out in milliseconds.
     * @param writeTimeOut write time out in milliseconds.
     * @return true if the operation executed successfully.
     * @throws SerialComException if an I/O error occurs.
     * @throws IllegalArgumentException if divisor is negative.
     */
    public boolean setTimeouts(final long handle, long readTimeOut, long writeTimeOut) throws SerialComException {
        if(readTimeOut < 0) {
            throw new IllegalArgumentException("Argument readTimeOut can not be negative !");
        }
        if(writeTimeOut < 0) {
            throw new IllegalArgumentException("Argument writeTimeOut can not be negative !");
        }

        int ret = mFTDID2XXJNIBridge.setTimeouts(handle, readTimeOut, writeTimeOut);
        if(ret < 0) {
            throw new SerialComException("Could not set the desired timeout values for the requested device. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes FT_SetFlowControl function of D2XX library.</p>
     * 
     * <p>Sets the flow control mode for the given FT device.</p>
     * 
     * @param handle handle of the device whose flow control is to be set.
     * @param flctrl flow control of serial frame (refer FLOWCTRL enum in SerialComFTDID2XX class for this).
     * @param xon character used to signal Xon if software flow control is used.
     * @param xoff character used to signal Xoff if software flow control is used.
     * @return true if the operation executed successfully.
     * @throws SerialComException if an I/O error occurs.
     * @throws IllegalArgumentException if flctrl is null.
     */
    public boolean setFlowControl(final long handle, FLOWCTRL flctrl, char xon, char xoff) throws SerialComException {
        if(flctrl == null) {
            throw new IllegalArgumentException("Argument flctrl can not be null !");
        }

        int xonch = (int) xon;
        int xoffch = (int) xoff;
        int ret = mFTDID2XXJNIBridge.setFlowControl(handle, flctrl.getValue(), (byte) xonch, (byte) xoffch);
        if(ret < 0) {
            throw new SerialComException("Could not set the desired flow control values for the requested device. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes FT_SetDtr function of D2XX library.</p>
     * 
     * <p>Sets the Data Terminal Ready (DTR) control signal.</p>
     * 
     * @param handle handle of the device for which DTR signal need to be set.
     * @return true if the operation executed successfully.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean setDTR(final long handle) throws SerialComException {
        int ret = mFTDID2XXJNIBridge.setDTR(handle);
        if(ret < 0) {
            throw new SerialComException("Could not set the DTR signal for the requested device. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes FT_ClrDtr function of D2XX library.</p>
     * 
     * <p>This method clears the Data Terminal Ready (DTR) control signal.</p>
     * 
     * @param handle handle of the device for which DTR signal need to be cleared.
     * @return true if the operation executed successfully.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean clearDTR(final long handle) throws SerialComException {
        int ret = mFTDID2XXJNIBridge.clearDTR(handle);
        if(ret < 0) {
            throw new SerialComException("Could not clear the DTR signal for the requested device. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes FT_SetRts function of D2XX library.</p>
     * 
     * <p>Sets the Request To Send (RTS) control signal.</p>
     * 
     * @param handle handle of the device for which RTS signal need to be set.
     * @return true if the operation executed successfully.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean setRTS(final long handle) throws SerialComException {
        int ret = mFTDID2XXJNIBridge.setRTS(handle);
        if(ret < 0) {
            throw new SerialComException("Could not set the RTS signal for the requested device. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes FT_ClrRts function of D2XX library.</p>
     * 
     * <p>This method clears the Request To Send (RTS) control signal.</p>
     * 
     * @param handle handle of the device for which RTS signal need to be cleared.
     * @return true if the operation executed successfully.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean clearRTS(final long handle) throws SerialComException {
        int ret = mFTDID2XXJNIBridge.clearRTS(handle);
        if(ret < 0) {
            throw new SerialComException("Could not clear the RTS signal for the requested device. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes FT_GetModemStatus function of D2XX library.</p>
     * 
     * <p>Gets the modem status and line status from the device.</p>
     * 
     * @param handle handle of the device whose status is to be observed.
     * @return bit mapped status value.
     * @throws SerialComException if an I/O error occurs.
     */
    public int getModemStatus(final long handle) throws SerialComException {
        int ret = mFTDID2XXJNIBridge.getModemStatus(handle);
        if(ret < 0) {
            throw new SerialComException("Could not get the status of modem for the requested device. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes FT_GetQueueStatus function of D2XX library.</p>
     * 
     * <p>Gets the number of bytes in the receive queue.</p>
     * 
     * @param handle handle of the device for whom number of bytes is to be calculated.
     * @return number of bytes in receive queue.
     * @throws SerialComException if an I/O error occurs.
     */
    public int getQueueStatus(final long handle) throws SerialComException {
        int ret = mFTDID2XXJNIBridge.getQueueStatus(handle);
        if(ret < 0) {
            throw new SerialComException("Could not get the number of bytes in receive queue for the requested device. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes FT_GetDeviceInfo function of D2XX library.</p>
     * 
     * <p>Get device information for an open device.</p>
     * 
     * @param handle of the device for which information is to be obtained.
     * @return an object of type FTOpenedDeviceInfo containing details of requested device or null.
     * @throws SerialComException if an I/O error occurs.
     */
    public FTOpenedDeviceInfo getDeviceInfo(final long handle) throws SerialComException {
        String[] rawData = mFTDID2XXJNIBridge.getDeviceInfo(handle);
        if(rawData != null) {
            return new FTOpenedDeviceInfo(rawData[0], rawData[1], rawData[2], rawData[3]);
        }else {
            return null;
        }
    }

    /**
     * <p>Executes FT_GetDriverVersion function of D2XX library.</p>
     * 
     * <p>Gets the D2XX driver version number.</p>
     * 
     * @param handle handle of the device for whom driver version is to found.
     * @return driver version number for the requested device handle.
     * @throws SerialComException if an I/O error occurs.
     */
    public long getDriverVersion(final long handle) throws SerialComException {
        long ret = mFTDID2XXJNIBridge.getDriverVersion(handle);
        if(ret < 0) {
            throw new SerialComException("Could not get the driver version for the requested device. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes FT_GetLibraryVersion function of D2XX library.</p>
     * 
     * <p>Gets the D2XX DLL version number.</p>
     * 
     * @return driver version number for the requested device handle.
     * @throws SerialComException if an I/O error occurs.
     */
    public long getLibraryVersion() throws SerialComException {
        long ret = mFTDID2XXJNIBridge.getLibraryVersion();
        if(ret < 0) {
            throw new SerialComException("Could not get the d2xx library version for the requested device. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes FT_GetComPortNumber function of D2XX library.</p>
     * 
     * <p>Retrieves the COM port associated with a device.</p>
     * 
     * @param handle handle of the device for whom COM port is to found.
     * @return COM Port number assigned or -1 if no COM Port number is assigned to this device.
     * @throws SerialComException if an I/O error occurs.
     */
    public long getComPortNumber(final long handle) throws SerialComException {
        long ret = mFTDID2XXJNIBridge.getComPortNumber(handle);
        if(ret < 0) {
            throw new SerialComException("Could not determine the COM port number for the requested device. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes FT_GetStatus function of D2XX library.</p>
     * 
     * <p>Gets the device status including number of characters in the receive queue, number of 
     * characters in the transmit queue, and the current event status.</p>
     * 
     * @param handle handle of the device for whom information need to be found.
     * @return array containing number of bytes in rx buffer, number of bytes in tx buffer, modem event status.
     * @throws SerialComException if an I/O error occurs.
     */
    public long[] getStatus(final long handle) throws SerialComException {
        long[] info = mFTDID2XXJNIBridge.getStatus(handle);
        if(info == null) {
            throw new SerialComException("Could not determine the required information for the requested device. Please retry !");
        }
        return info;
    }

    /**
     * <p>Executes FT_SetEventNotification function of D2XX library and blocks until one or more 
     * events specified in eventMask occurs. Once this method returns, application can determine 
     * which event has occurred using getStatus method.</p>
     * 
     * @param handle handle of the device for which to set event mask and wait.
     * @param eventMask bit mask of the constants EV_XXXXX in SerialComFTDID2XX class.
     * @return true if one or more event has happened.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean setEventNotificationAndWait(long handle, int eventMask) throws SerialComException {
        int eventsThatOccurredMask = mFTDID2XXJNIBridge.setEventNotificationAndWait(handle, eventMask);
        if(eventsThatOccurredMask < 0) {
            throw new SerialComException("Could not set the event notification and block. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes FT_SetChars function of D2XX library.</p>
     * 
     * <p>Sets the special characters for the device.</p>
     * 
     * @param handle handle of the device for which characters need to be set.
     * @param eventChar event character.
     * @param eventEnable 0 if event character disabled, non-zero otherwise.
     * @param errorChar error character.
     * @param errorEnable 0 if error character disabled, non-zero otherwise.
     * @return true if the operation executed successfully.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean setChars(final long handle, char eventChar, char eventEnable, char errorChar, char errorEnable) throws SerialComException {
        int evch = (int) eventChar;
        int even = (int) eventEnable;
        int erch = (int) errorChar;
        int eren = (int) errorEnable;
        int ret = mFTDID2XXJNIBridge.setChars(handle, (byte)evch, (byte)even, (byte)erch, (byte)eren);
        if(ret < 0) {
            throw new SerialComException("Could not set the given characters for the requested device. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes FT_SetBreakOn function of D2XX library.</p>
     * 
     * <p>Sets the BREAK condition for the device.</p>
     * 
     * @param handle handle of the device for which break condition need to be set.
     * @return true if the operation executed successfully.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean setBreakOn(final long handle) throws SerialComException {
        int ret = mFTDID2XXJNIBridge.setBreakOn(handle);
        if(ret < 0) {
            throw new SerialComException("Could not set the break condition for the requested device. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes FT_SetBreakOff function of D2XX library.</p>
     * 
     * <p>Resets the BREAK condition for the device.</p>
     * 
     * @param handle handle of the device for which break condition need to be reset.
     * @return true if the operation executed successfully.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean setBreakOff(final long handle) throws SerialComException {
        int ret = mFTDID2XXJNIBridge.setBreakOff(handle);
        if(ret < 0) {
            throw new SerialComException("Could not reset the break condition for the requested device. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes FT_Purge function of D2XX library.</p>
     * 
     * <p>This method purges receive and transmit buffers in the device.</p>
     * 
     * @param handle handle of the device for which buffer need to be cleared.
     * @param purgeTxBuffer true if transmit buffer need to be cleared.
     * @param purgeRxBuffer true if receive buffer need to be cleared.
     * @return true if the operation executed successfully.
     * @throws SerialComException if an I/O error occurs.
     * @throws IllegalArgumentException if both purgeTxBuffer and purgeRxBuffer are false.
     */
    public boolean purge(final long handle, boolean purgeTxBuffer, boolean purgeRxBuffer) throws SerialComException {
        if((purgeRxBuffer == false) && (purgeTxBuffer == false)) {
            throw new IllegalArgumentException("Both arguments purgeRxBuffer and purgeTxBuffer can not be false !");
        }
        int ret = mFTDID2XXJNIBridge.purge(handle, purgeTxBuffer, purgeRxBuffer);
        if(ret < 0) {
            throw new SerialComException("Could not purge the buffer(s) for the requested device. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes FT_ResetDevice function of D2XX library.</p>
     * 
     * <p>This method sends a reset command to the device.</p>
     * 
     * @param handle handle of the device which need to be reset.
     * @return true if the operation executed successfully.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean resetDevice(final long handle) throws SerialComException {
        int ret = mFTDID2XXJNIBridge.resetDevice(handle);
        if(ret < 0) {
            throw new SerialComException("Could not reset the requested device. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes FT_ResetPort function of D2XX library.</p>
     * 
     * <p>This method sends a reset command to the port.</p>
     * 
     * @param handle handle of the device whose port need to be reset.
     * @return true if the operation executed successfully.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean resetPort(final long handle) throws SerialComException {
        int ret = mFTDID2XXJNIBridge.resetPort(handle);
        if(ret < 0) {
            throw new SerialComException("Could not reset the requested port. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes FT_CyclePort function of D2XX library.</p>
     * 
     * <p>Send a cycle command to the USB port.</p>
     * 
     * @param handle handle of the device who need to be cycled.
     * @return true if the operation executed successfully.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean cyclePort(final long handle) throws SerialComException {
        int ret = mFTDID2XXJNIBridge.cyclePort(handle);
        if(ret < 0) {
            throw new SerialComException("Could not cycle the requested port. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes FT_Rescan function of D2XX library.</p>
     * 
     * <p>Send a cycle command to the USB port.</p>
     * 
     * @return true if the operation executed successfully.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean rescan() throws SerialComException {
        int ret = mFTDID2XXJNIBridge.rescan();
        if(ret < 0) {
            throw new SerialComException("Could not rescan the system for usb devices. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes FT_Reload function of D2XX library.</p>
     * 
     * <p>Send a cycle command to the USB port.</p>
     * 
     * @param vid Vendor ID of the devices to reload the driver for.
     * @param pid Product ID of the devices to reload the driver for.
     * @return true if the operation executed successfully.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean reload(final int vid, final int pid) throws SerialComException {
        int ret = mFTDID2XXJNIBridge.reload(vid, pid);
        if(ret < 0) {
            throw new SerialComException("Could not cycle the requested port. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes SetResetPipeRetryCount function of D2XX library.</p>
     * 
     * <p>Set the ResetPipeRetryCount value.</p>
     * 
     * @param handle handle of the device for which this count is to be set.
     * @param count maximum number of times that the driver tries to reset a pipe on which an error has occurred.
     * @return true if the operation executed successfully.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean setResetPipeRetryCount(final long handle, int count) throws SerialComException {
        int ret = mFTDID2XXJNIBridge.setResetPipeRetryCount(handle, count);
        if(ret < 0) {
            throw new SerialComException("Could not set the requested count for given device. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes FT_StopInTask function of D2XX library.</p>
     * 
     * <p>Stops the driver's IN task.</p>
     * 
     * @param handle handle of the device for which this count is to be set.
     * @return true if the operation executed successfully.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean stopInTask(final long handle) throws SerialComException {
        int ret = mFTDID2XXJNIBridge.stopInTask(handle);
        if(ret < 0) {
            throw new SerialComException("Could not stop the driver's IN task for given device. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes FT_StopInTask function of D2XX library.</p>
     * 
     * <p>Stops the driver's IN task.</p>
     * 
     * @param handle handle of the device for which this count is to be set.
     * @return true if the operation executed successfully.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean restartInTask(final long handle) throws SerialComException {
        int ret = mFTDID2XXJNIBridge.restartInTask(handle);
        if(ret < 0) {
            throw new SerialComException("Could not restart the driver's IN task for given device. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes FT_SetDeadmanTimeout function of D2XX library.</p>
     * 
     * <p>This method allows the maximum time in milliseconds that a USB request 
     * can remain outstanding to be set.</p>
     * 
     * @param handle handle of the device for which this time out is to be set.
     * @param count timeout value in milliseconds. Default value is 5000.
     * @return true if the operation executed successfully.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean setDeadmanTimeout(final long handle, int count) throws SerialComException {
        int ret = mFTDID2XXJNIBridge.setDeadmanTimeout(handle, count);
        if(ret < 0) {
            throw new SerialComException("Could not set the requested time out for given device. Please retry !");
        }
        return true;
    }


    /* ********************* EEPROM Programming Interface Functions ******************** */


    /**
     * <p>Executes FT_ReadEE function of D2XX library.</p>
     * 
     * <p>Read a value from an EEPROM location.</p>
     * 
     * @param handle handle of the device whose EEPROM is to be read.
     * @param offset EEPROM location to read from.
     * @return value read at given address.
     * @throws SerialComException if an I/O error occurs.
     * @throws IllegalArgumentException if offset is negative.
     */
    public int readEE(final long handle, int offset) throws SerialComException {
        if(offset < 0) {
            throw new IllegalArgumentException("Argument offset can not be negative !");
        }
        int ret = mFTDID2XXJNIBridge.readEE(handle, offset);
        if(ret < 0) {
            throw new SerialComException("Could not read the value from given address offset. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes FT_WriteEE function of D2XX library.</p>
     * 
     * <p>Write a value to an EEPROM location.</p>
     * 
     * @param handle handle of the device whose EEPROM will be written.
     * @param offset EEPROM location to write at.
     * @param valueToWrite value to write at given address.
     * @return true if operation executed successfully.
     * @throws SerialComException if an I/O error occurs.
     * @throws IllegalArgumentException if offset is negative.
     */
    public boolean writeEE(final long handle, int offset, int valueToWrite) throws SerialComException {
        if(offset < 0) {
            throw new IllegalArgumentException("Argument offset can not be negative !");
        }
        int ret = mFTDID2XXJNIBridge.writeEE(handle, offset, valueToWrite);
        if(ret < 0) {
            throw new SerialComException("Could not read the value from given address offset. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes FT_EraseEE function of D2XX library.</p>
     * 
     * <p>Erase the entire contents of an EEPROM, including the user area.</p>
     * 
     * @param handle handle of the device which need to be erased.
     * @return true if entire EEPROM has been erased successfully.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean eraseEE(final long handle) throws SerialComException {
        int ret = mFTDID2XXJNIBridge.eraseEE(handle);
        if(ret < 0) {
            throw new SerialComException("Could not erase the EEPROM. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes FT_EE_Read function of D2XX library.</p>
     * 
     * <p>Read the contents of the EEPROM.</p>
     * 
     * @param handle handle of the device whose contents are to be read.
     * @param version of the FT_PROGRAM_DATA structure defined in FTD2XX.h file.
     * @return an object of type FTprogramData containing all data read.
     * @throws SerialComException if an I/O error occurs.
     */
    public FTprogramData eeRead(final long handle, int version) throws SerialComException {
        char[] manufacturer = new char[32];
        char[] manufacturerID = new char[16];
        char[] description = new char[64];
        char[] serialNumber = new char[16];
        FTprogramData ftProgramData;

        int[] info = mFTDID2XXJNIBridge.eeRead(handle, version, manufacturer, manufacturerID, description, serialNumber);
        if(info == null) {
            throw new SerialComException("Could not read the contents from EEPROM. Please retry !");
        }

        ftProgramData = new FTprogramData(info, manufacturer, manufacturerID, description, serialNumber);
        return ftProgramData;
    }

    /**
     * <p>Executes FT_EE_ReadEx function of D2XX library.</p>
     * 
     * <p>Read the contents of the EEPROM.</p>
     * 
     * @param handle handle of the device whose contents are to be read.
     * @param version of the FT_PROGRAM_DATA structure defined in FTD2XX.h file.
     * @param manufacturer char array of size 32 bytes to save manufacturer name.
     * @param manufacturerID char array of size 16 bytes to save manufacturer ID.
     * @param description char array of size 64 bytes to save device description.
     * @param serialNumber char array of size 16 bytes to save serial number of device.
     * @return an object of type FTprogramData containing all data read.
     * @throws IllegalArgumentException if manufacturer, manufacturerID, description or serialNumber is
     *          null.
     * @throws SerialComException if an I/O error occurs.
     */
    public FTprogramData eeReadEx(final long handle, int version, char[] manufacturer, char[] manufacturerID, 
            char[] description, char[] serialNumber) throws SerialComException {
        FTprogramData ftProgramData;

        // verify correct objects
        if((manufacturer == null) || (manufacturerID == null) || (description == null) || (serialNumber == null)) {
            throw new IllegalArgumentException("Arguments manufacturer, manufacturerID, description and serialNumber can not be null !");
        }
        if(manufacturer.length != 32) {
            throw new IllegalArgumentException("Argument manufacturer must be of 32 bytes in size !");
        }
        if(manufacturerID.length != 16) {
            throw new IllegalArgumentException("Argument manufacturerID must be of 16 bytes in size !");
        }
        if(description.length != 64) {
            throw new IllegalArgumentException("Argument manufacturer must be of 64 bytes in size !");
        }
        if(serialNumber.length != 16) {
            throw new IllegalArgumentException("Argument serialNumber must be of 16 bytes in size !");
        }

        int[] info = mFTDID2XXJNIBridge.eeRead(handle, version, manufacturer, manufacturerID, description, serialNumber);
        if(info == null) {
            throw new SerialComException("Could not read the contents from EEPROM. Please retry !");
        }

        ftProgramData = new FTprogramData(info, manufacturer, manufacturerID, description, serialNumber);
        return ftProgramData;
    }

    /**
     * <p>Executes FT_EE_Program function of D2XX library.</p>
     * 
     * <p>Programs the EEPROM. When creating array of data values to be written, Manufacturer, 
     * ManufacturerId, Description and  should be set to -1.</p>
     * 
     * @param handle handle of the device whose EEPROM is to be programmed.
     * @param manufacturer manufacturer name string (see app note for maximum suggested size, typically 32).
     * @param manufacturerID manufacturer ID string (see app note for maximum suggested size, typically 16).
     * @param description device description (see app note for maximum suggested size, typically 64).
     * @param serialNumber serial number of device (see app note for maximum suggested size, typically 16).
     * @param values array of integer with all values populated in order as declared in 
     *         FT_PROGRAM_DATA structure in FTD2XX.h file.
     * @return true is data is programmed into EEPROM.
     * @throws IllegalArgumentException if manufacturer, manufacturerID, description or serialNumber is
     *          null.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean eeProgram(final long handle, String manufacturer, String manufacturerID, 
            String description, String serialNumber, int[] values) throws SerialComException {
        if((manufacturer == null) || (manufacturerID == null) || (description == null) || (serialNumber == null)) {
            throw new IllegalArgumentException("Arguments manufacturer, manufacturerID, description and serialNumber can not be null !");
        }
        int ret = mFTDID2XXJNIBridge.eeProgram(handle, manufacturer, manufacturerID, description, serialNumber, values);
        if(ret < 0) {
            throw new SerialComException("Could not program the EEPROM. Please retry !");
        }

        return true;
    }

    /**
     * <p>Executes FT_EE_ProgramEx function of D2XX library.</p>
     * 
     * <p>Programs the EEPROM. When creating array of data values to be written, Manufacturer, 
     * ManufacturerId, Description and  should be set to -1.</p>
     * 
     * @param handle handle of the device whose EEPROM is to be programmed.
     * @param manufacturer manufacturer name string (see app note for maximum suggested size, typically 32).
     * @param manufacturerID manufacturer ID string (see app note for maximum suggested size, typically 16).
     * @param description device description (see app note for maximum suggested size, typically 64).
     * @param serialNumber serial number of device (see app note for maximum suggested size, typically 16).
     * @param values array of integer with all values populated in order as declared in 
     *         FT_PROGRAM_DATA structure in FTD2XX.h file.
     * @return true is data is programmed into EEPROM.
     * @throws IllegalArgumentException if manufacturer, manufacturerID, description or serialNumber is
     *          null.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean eeProgramEx(final long handle, String manufacturer, String manufacturerID, 
            String description, String serialNumber, int[] values) throws SerialComException {
        if((manufacturer == null) || (manufacturerID == null) || (description == null) || (serialNumber == null)) {
            throw new IllegalArgumentException("Arguments manufacturer, manufacturerID, description and serialNumber can not be null !");
        }
        int ret = mFTDID2XXJNIBridge.eeProgramEx(handle, manufacturer, manufacturerID, description, serialNumber, values);
        if(ret < 0) {
            throw new SerialComException("Could not program the EEPROM. Please retry !");
        }

        return true;
    }

    /**
     * <p>Executes FT_EE_UASize function of D2XX library.</p>
     * 
     * <p>Get the available size of the EEPROM user area.</p>
     * 
     * @param handle handle of the device whose EEPROM area is to be calculated.
     * @return area in terms of number of bytes.
     * @throws SerialComException if an I/O error occurs.
     */
    public int eeUAsize(final long handle) throws SerialComException {
        int ret = mFTDID2XXJNIBridge.eeUAsize(handle);
        if(ret < 0) {
            throw new SerialComException("Could not determine the available size. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes FT_EE_UARead function of D2XX library.</p>
     * 
     * <p>Read the contents of the EEPROM user area.</p>
     * 
     * @param handle handle of the device whose EEPROM area is to be read.
     * @param buffer byte buffer to store data.
     * @param length number of bytes to read from EEPROM.
     * @return number of bytes actually read.
     * @throws IllegalArgumentException if buffer is null or length > buffer.length.
     * @throws SerialComException if an I/O error occurs.
     */
    public int eeUAread(final long handle, byte[] buffer, int length) throws SerialComException {
        if(buffer == null) {
            throw new IllegalArgumentException("Argument buffer can not be null !");
        }
        if(length > buffer.length) {
            throw new IllegalArgumentException("Argument length must be less than or equal to length of buffer !");
        }
        int ret = mFTDID2XXJNIBridge.eeUAread(handle, buffer, length);
        if(ret < 0) {
            throw new SerialComException("Could not read the EEPROM. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes FT_EE_UAWrite function of D2XX library.</p>
     * 
     * <p>Write data into the EEPROM user area.</p>
     * 
     * @param handle handle of the device whose EEPROM area is to be written.
     * @param buffer byte buffer to containing data.
     * @param length number of bytes to write from buffer to EEPROM user area.
     * @return true on success.
     * @throws IllegalArgumentException if buffer is null or length > buffer.length.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean eeUAwrite(final long handle, byte[] buffer, int length) throws SerialComException {
        if(buffer == null) {
            throw new IllegalArgumentException("Argument buffer can not be null !");
        }
        if(length > buffer.length) {
            throw new IllegalArgumentException("Argument length must be less than or equal to length of buffer !");
        }
        int ret = mFTDID2XXJNIBridge.eeUAwrite(handle, buffer, length);
        if(ret < 0) {
            throw new SerialComException("Could not write data to the EEPROM. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes FT_EEPROM_Read function of D2XX library.</p>
     * 
     * <p>Read data from the EEPROM, this command will work for all existing FTDI chipset, and must 
     * be used for the FT-X series.</p>
     * 
     * @param handle handle of the device whose contents are to be read.
     * @param deviceType one of the constants FT_DEVICE_XXXX.
     * @param manufacturer char array to save manufacturer name (suggested size 32).
     * @param manufacturerID char array to save manufacturer ID (suggested size 16).
     * @param description char array to save device description (suggested size 64).
     * @param serialNumber char array to save serial number of device (suggested size 16).
     * @return an object of FTeepromData class containing all values read.
     * @throws IllegalArgumentException if deviceType is invalid or length of byte arrays are incorrect.
     * @throws SerialComException if an I/O error occurs.
     */
    public FTeepromData eepromRead(final long handle, int deviceType, char[] manufacturer, 
            char[] manufacturerID, char[] description, char[] serialNumber) throws SerialComException {

        // verify correct device
        if((deviceType != FT_DEVICE_2232C) && (deviceType != FT_DEVICE_2232H) && (deviceType != FT_DEVICE_232R)
                && (deviceType != FT_DEVICE_232H) && (deviceType != FT_DEVICE_4232H) && 
                (deviceType != FT_DEVICE_X_SERIES)) {
            throw new IllegalArgumentException("Argument deviceType has invalid value !");
        }

        // verify correct objects
        if((manufacturer == null) || (manufacturerID == null) || (description == null) || (serialNumber == null)) {
            throw new IllegalArgumentException("Arguments manufacturer, manufacturerID, description and serialNumber can not be null !");
        }
        if(manufacturer.length != 32) {
            throw new IllegalArgumentException("Argument manufacturer must be of 32 bytes in size !");
        }
        if(manufacturerID.length != 16) {
            throw new IllegalArgumentException("Argument manufacturerID must be of 16 bytes in size !");
        }
        if(description.length != 64) {
            throw new IllegalArgumentException("Argument manufacturer must be of 64 bytes in size !");
        }
        if(serialNumber.length != 16) {
            throw new IllegalArgumentException("Argument serialNumber must be of 16 bytes in size !");
        }

        // native layer will make sure that returned array is of correct size.
        int[] dataValues = mFTDID2XXJNIBridge.eepromRead(handle, deviceType, manufacturer, manufacturerID, 
                description, serialNumber);
        if(dataValues == null) {
            throw new SerialComException("Could not read the EEPROM data. Please retry !");
        }

        if(deviceType == FT_DEVICE_2232C) {
            return new FTeeprom2232(dataValues);
        }else if(deviceType == FT_DEVICE_2232H) {
            return new FTeeprom2232H(dataValues);
        }else if(deviceType == FT_DEVICE_232R) {
            return new FTeeprom232R(dataValues);
        }else if(deviceType == FT_DEVICE_232H) {
            return new FTeeprom232H(dataValues);
        }else if(deviceType == FT_DEVICE_4232H) {
            return new FTeeprom4232H(dataValues);
        }else if(deviceType == FT_DEVICE_X_SERIES) {
            return new FTeepromXseries(dataValues);
        }else {
        }

        return null;
    }

    /**
     * <p>Executes FT_EEPROM_Program function of D2XX library.</p>
     * 
     * <p>Write data into the EEPROM, this command will work for all existing FTDI chipset, and must 
     * be used for the FT-X series.</p>
     * 
     * @param handle handle of the device whose EEPROM is to be programmed.
     * @param eepromData an instance of class containing data to be written to EEPROM.
     * @param manufacturer manufacturer name string.
     * @param manufacturerID manufacturer ID string.
     * @param description device description string.
     * @param serialNumber serial number of device string.
     * @return true if data gets flashed into EEPROM successfully.
     * @throws IllegalArgumentException if eepromData is not an instance of class corresponding to 
     *          the device type or length of byte arrays are incorrect.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean eepromProgram(final long handle, FTeepromData eepromData, String manufacturer, 
            String manufacturerID, String description, String serialNumber) throws SerialComException {

        int[] dataToBeWritten = null;
        FTeepromHeader header = (FTeepromHeader) eepromData;
        int devType = header.getDeviceType();

        if(devType == FT_DEVICE_2232C) {
            if(!(eepromData instanceof FTeeprom2232)) {
                throw new IllegalArgumentException("For FT_DEVICE_2232C, argument eepromData must be an instance of FTeeprom2232 class !");
            }
            FTeeprom2232 dev = (FTeeprom2232) eepromData;
            dataToBeWritten = dev.getAllMembers();
        }else if(devType == FT_DEVICE_2232H) {
            if(!(eepromData instanceof FTeeprom2232H)) {
                throw new IllegalArgumentException("For FT_DEVICE_2232H, argument eepromData must be an instance of FTeeprom2232H class !");
            }
            FTeeprom2232H dev = (FTeeprom2232H) eepromData;
            dataToBeWritten = dev.getAllMembers();
        }else if(devType == FT_DEVICE_232R) {
            if(!(eepromData instanceof FTeeprom232R)) {
                throw new IllegalArgumentException("For FT_DEVICE_232R, argument eepromData must be an instance of FTeeprom232R class !");
            }
            FTeeprom232R dev = (FTeeprom232R) eepromData;
            dataToBeWritten = dev.getAllMembers();
        }else if(devType == FT_DEVICE_232H) {
            if(!(eepromData instanceof FTeeprom232H)) {
                throw new IllegalArgumentException("For FT_DEVICE_232H, argument eepromData must be an instance of FTeeprom232H class !");
            }
            FTeeprom232H dev = (FTeeprom232H) eepromData;
            dataToBeWritten = dev.getAllMembers();
        }else if(devType == FT_DEVICE_4232H) {
            if(!(eepromData instanceof FTeeprom4232H)) {
                throw new IllegalArgumentException("For FT_DEVICE_4232H, argument eepromData must be an instance of FTeeprom4232H class !");
            }
            FTeeprom4232H dev = (FTeeprom4232H) eepromData;
            dataToBeWritten = dev.getAllMembers();
        }else if(devType == FT_DEVICE_X_SERIES) {
            if(!(eepromData instanceof FTeepromXseries)) {
                throw new IllegalArgumentException("For FT_DEVICE_X_SERIES, argument eepromData must be an instance of FTeepromXseries class !");
            }
            FTeepromXseries dev = (FTeepromXseries) eepromData;
            dataToBeWritten = dev.getAllMembers();
        }else {
            /* should not happen */
        }

        // make sure that correct array size is passed to native layer.
        int ret = mFTDID2XXJNIBridge.eepromProgram(handle, devType, dataToBeWritten, manufacturer, manufacturerID, 
                description, serialNumber);
        if(ret < 0) {
            throw new SerialComException("Could not program the EEPROM. Please retry !");
        }

        return true;
    }


    /* ********************* Extended API Functions *********************/


    /**
     * <p>Executes FT_SetLatencyTimer function of D2XX library.</p>
     * 
     * <p>Set the latency timer value.</p>
     * 
     * @param handle handle of the device whose timer need to be set.
     * @param value timer value.
     * @return true on success.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean setLatencyTimer(final long handle, int value) throws SerialComException {
        int ret = mFTDID2XXJNIBridge.setLatencyTimer(handle, value);
        if(ret < 0) {
            throw new SerialComException("Could not set the latency timer value. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes FT_GetLatencyTimer function of D2XX library.</p>
     * 
     * <p>Get the current value of the latency timer.</p>
     * 
     * @param handle handle of the device whose timer value is to be read.
     * @return current timer value.
     * @throws SerialComException if an I/O error occurs.
     */
    public int getLatencyTimer(final long handle) throws SerialComException {
        int ret = mFTDID2XXJNIBridge.getLatencyTimer(handle);
        if(ret < 0) {
            throw new SerialComException("Could not determine the latency timer value. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes FT_SetBitMode function of D2XX library.</p>
     * 
     * <p>Enables different chip modes.</p>
     * 
     * @param handle handle of the device whose mode is to be set.
     * @param mask required value for bit mode mask. This sets up which bits are inputs and outputs. 
     *         A bit value of 0 sets the corresponding pin to an input, a bit value of 1 sets the corresponding 
     *         pin to an output. In the case of CBUS Bit Bang, the upper nibble of this value controls which 
     *         pins are inputs and outputs, while the lower nibble controls which of the outputs are high and low.
     * @param mode it should be one of the following : 0x0 for Reset, 0x1 for Asynchronous Bit Bang, 0x2 
     *         for MPSSE, 0x4 for Synchronous Bit Bang, 0x8 for MCU Host Bus Emulation Mode, 0x10 for Fast 
     *         Opto-Isolated Serial Mode, 0x20 for CBUS Bit Bang Mode, 0x40 for Single Channel Synchronous 
     *         245 FIFO Mode.
     * @return true on success.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean setBitMode(final long handle, int mask, int mode) throws SerialComException {
        int ret = mFTDID2XXJNIBridge.setBitMode(handle, mask, mode);
        if(ret < 0) {
            throw new SerialComException("Could not set the given chip mode. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes FT_GetBitMode function of D2XX library.</p>
     * 
     * <p>Gets the instantaneous value of the data bus.</p>
     * 
     * @param handle handle of the device whose mode is to be fetched.
     * @return current instantaneous value of the data bus.
     * @throws SerialComException if an I/O error occurs.
     */
    public int getBitMode(final long handle) throws SerialComException {
        int ret = mFTDID2XXJNIBridge.getBitMode(handle);
        if(ret < 0) {
            throw new SerialComException("Could not get the given chip mode. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes FT_SetUSBParameters function of D2XX library.</p>
     * 
     * <p>Set the USB request transfer size. Check with application note that setting output 
     * transfer size is supported or not. If not supported then outTransferSize must be 0.</p>
     * 
     * @param handle handle of the device whose parameters is to be set.
     * @param inTransferSize Transfer size for USB IN request.
     * @param outTransferSize Transfer size for USB OUT request.
     * @return true on success.
     * @throws IllegalArgumentException if inTransferSize or outTransferSize is negative.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean setUSBParameters(final long handle, int inTransferSize, int outTransferSize) throws SerialComException {
        if((outTransferSize < 0) || (inTransferSize < 0)) {
            throw new IllegalArgumentException("Argument inTransferSize or outTransferSize can not be negative !");
        }
        int ret = mFTDID2XXJNIBridge.setUSBParameters(handle, inTransferSize, outTransferSize);
        if(ret < 0) {
            throw new SerialComException("Could not set the usb parameters. Please retry !");
        }
        return true;
    }


    /* ********************* FT-Win32 API Functions ******************** */


    /**
     * <p>Executes FT_W32_CreateFile function of D2XX library.</p>
     * 
     * <p>Opens the specified device and return a handle which will be used for subsequent accesses. 
     * The device can be specified by its serial number, device description, or location. This function 
     * must be used if overlapped I/O is required.</p>
     * 
     * @param serialNum serial number string if device is to be opened using serial number.
     * @param description description of the device if it is to be opened using description.
     * @param location location of the device if it is to be opened using location.
     * @param dwAttrsAndFlags one of the constant FT_OPEN_BY_SERIAL_NUMBER, FT_OPEN_BY_DESCRIPTION or 
     *         FT_OPEN_BY_LOCATION.
     * @param dwAccess bit mask of GENERIC_READ or GENERIC_WRITE or both.
     * @param overLapped true if device is to be opened for overlapped operations.
     * @return handle of the opened device.
     * @throws IllegalArgumentException if serialNum or description is null or location is negative when 
     *          they must be valid.
     * @throws SerialComException if an I/O error occurs.
     */
    public long w32CreateFile(String serialNum, String description, long location, int dwAttrsAndFlags, 
            int dwAccess, boolean overLapped) throws SerialComException {

        if((dwAttrsAndFlags & FT_OPEN_BY_SERIAL_NUMBER) == FT_OPEN_BY_SERIAL_NUMBER) {
            if(serialNum == null) {
                throw new IllegalArgumentException("Argument serialNum can not be null for FT_OPEN_BY_SERIAL_NUMBER !");
            }
        }
        if((dwAttrsAndFlags & FT_OPEN_BY_DESCRIPTION) == FT_OPEN_BY_DESCRIPTION) {
            if(description == null) {
                throw new IllegalArgumentException("Argument description can not be null for FT_OPEN_BY_DESCRIPTION !");
            }
        }
        if((dwAttrsAndFlags & FT_OPEN_BY_LOCATION) == FT_OPEN_BY_LOCATION) {
            if(location < 0) {
                throw new IllegalArgumentException("Argument location can not be negative for FT_OPEN_BY_LOCATION !");
            }
        }

        long handle = mFTDID2XXJNIBridge.w32CreateFile(serialNum, description, location, dwAttrsAndFlags, 
                dwAccess, overLapped);
        if(handle < 0) {
            throw new SerialComException("Could not open the requested device. Please retry !");
        }

        return handle;
    }

    /**
     * <p>Executes FT_W32_CloseHandle function of D2XX library.</p>
     * 
     * <p>Close the specified device handle.</p>
     * 
     * @param handle of the device that is to be close.
     * @return true on success.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean w32CloseHandle(final long handle) throws SerialComException {
        int ret = mFTDID2XXJNIBridge.w32CloseHandle(handle);
        if(ret < 0) {
            throw new SerialComException("Could not close the requested device. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes FT_W32_ReadFile function of D2XX library.</p>
     * 
     * <p>Read data from the device.</p>
     * 
     * <p>Note that SerialPundit supports only non-overlapped version of FT_W32_ReadFile method. If the application 
     * wishes to do anything in parallel, it should use java thread. By default this method will block. 
     * If non-blocking behavior is required application can set timeouts using w32SetCommTimeouts method.</p>
     * 
     * @param handle handle of the device from which to read data.
     * @param buffer byte buffer where data read will be placed.
     * @param numOfBytesToRead number of bytes to be tried to read.
     * @return number of bytes read.
     * @throws SerialComException if an I/O error occurs.
     * @throws IllegalArgumentException if buffer is null or numOfBytesToRead is negative or zero.
     */
    public int w32ReadFile(long handle, final byte[] buffer, int numOfBytesToRead) throws SerialComException {
        if(buffer == null) {
            throw new IllegalArgumentException("Argument buffer can not be null !");
        }
        if(numOfBytesToRead <= 0) {
            throw new IllegalArgumentException("Argument numOfBytesToRead can not be negative or zero !");
        }

        int ret = mFTDID2XXJNIBridge.w32ReadFile(handle, buffer, numOfBytesToRead);
        if(ret < 0) {
            throw new SerialComException("Could not read the data from the requested device. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes FT_W32_WriteFile function of D2XX library.</p>
     * 
     * <p>Write data from given buffer to the device.</p>
     * 
     * <p>Note that SerialPundit supports only non-overlapped version of FT_W32_WriteFile method. If the application 
     * wishes to do anything in parallel, it should use java thread. By default this method will block. 
     * If non-blocking behavior is required application can set timeouts using w32SetCommTimeouts method.</p>
     * 
     * @param handle handle of the device to which data is to be sent.
     * @param buffer byte buffer that contains the data to be written to the device.
     * @param numOfBytesToWrite number of bytes to write from buffer to the device.
     * @return number of bytes written to the device.
     * @throws SerialComException if an I/O error occurs.
     * @throws IllegalArgumentException if buffer is null or numOfBytesToWrite is negative or zero.
     */
    public int w32WriteFile(long handle, final byte[] buffer, int numOfBytesToWrite) throws SerialComException {
        if(buffer == null) {
            throw new IllegalArgumentException("Argument buffer can not be null !");
        }
        if(numOfBytesToWrite <= 0) {
            throw new IllegalArgumentException("Argument numOfBytesToWrite can not be negative or zero !");
        }

        int ret = mFTDID2XXJNIBridge.w32WriteFile(handle, buffer, numOfBytesToWrite);
        if(ret < 0) {
            throw new SerialComException("Could not send data to the requested device. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes FT_W32_GetOverlappedResult function of D2XX library.</p>
     * 
     * <p>Gets the result of an overlapped operation.</p>
     * 
     * <P>Note that this method is not supported by SerialPundit as SerialPundit implements only non-overlapped version of 
     * FT_W32_WriteFile and FT_W32_ReadFile functions.</p>
     * 
     * @param handle handle of the device whose baud rate need to be set.
     * @param wait Set to TRUE if the function does not return until the operation has been completed.
     * @return false always.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean w32GetOverlappedResult(final long handle, boolean wait) throws SerialComException {
        //		int ret = mFTDID2XXJNIBridge.w32GetOverlappedResult(handle, wait);
        //		if(ret < 0) {
        //			throw new SerialComException("Requested operation could not be executed successfully. Please retry !");
        //		}
        //		return true;
        return false;
    }

    /**
     * <p>Executes FT_W32_EscapeCommFunction function of D2XX library.</p>
     * 
     * <p>Perform an extended function.</p>
     * 
     * @param handle handle of the device.
     * @param function can be one of the following constant : SETRTS, CLRRTS, SETDTR, CLRDTR, SETBREAK
     *          and CLRBREAK defined in this class.
     * @return true if the operation executed successfully.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean w32EscapeCommFunction(final long handle, short function) throws SerialComException {
        int ret = mFTDID2XXJNIBridge.w32EscapeCommFunction(handle, function);
        if(ret < 0) {
            throw new SerialComException("Requested operation could not be executed successfully. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes FT_W32_GetCommModemStatus function of D2XX library.</p>
     * 
     * <p>Gets the current modem control value.</p>
     * 
     * @param handle handle of the device.
     * @return bit mask of the following constant : MS_CTS_ON, MS_DSR_ON, MS_RING_ON and MS_RLSD_ON.
     * @throws SerialComException if an I/O error occurs.
     */
    public int w32GetCommModemStatus(final long handle) throws SerialComException {
        int ret = mFTDID2XXJNIBridge.w32GetCommModemStatus(handle);
        if(ret < 0) {
            throw new SerialComException("Could not get the modem status. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes FT_W32_SetupComm function of D2XX library.</p>
     * 
     * <p>Sets the read and write buffers.</p>
     * 
     * @param handle handle of the device.
     * @return true if buffer size is set as requested.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean w32SetupComm(final long handle, int readBufSize, int writeBufSize) throws SerialComException {
        int ret = mFTDID2XXJNIBridge.w32SetupComm(handle, readBufSize, writeBufSize);
        if(ret < 0) {
            throw new SerialComException("Could not set the buffer size. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes FT_W32_SetCommState function of D2XX library.</p>
     * 
     * <p>Sets the setting given in DCB structure. Create an array of integers of size 28, fill all the 
     * values as if filling C structure but type cast every value to int. Native layer will type cast them 
     * back to data type as expected by C function. The DCBlength, fDummy2, wReserved, wReserved1 are 
     * ignored by native layer. The DCB configuration values must be in the following order : DCBlength, 
     * BaudRate, fBinary, fParity, fOutxCtsFlow, fOutxDsrFlow, fDtrControl, fDsrSensitivity, fTXContinueOnXoff,
     * fOutX, fInX, fErrorChar, fNull, fRtsControl, fAbortOnError, fDummy2, wReserved, XonLim, XoffLim, 
     * ByteSize, Parity, StopBits, XonChar, XoffChar, ErrorChar, EofChar, EvtChar and wReserved1.</p>
     * 
     * @param handle handle of the device.
     * @param dcb configuration values array defined in correct order.
     * @return true if operation executed successfully.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean w32SetCommState(final long handle, int[] dcb) throws SerialComException {
        int ret = mFTDID2XXJNIBridge.w32SetCommState(handle, dcb);
        if(ret < 0) {
            throw new SerialComException("Could not set the given settings values. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes FT_W32_GetCommState function of D2XX library.</p>
     * 
     * <p>Gets the current device state. The returned DCB configuration values are in the following order : 
     * DCBlength, BaudRate, fBinary, fParity, fOutxCtsFlow, fOutxDsrFlow, fDtrControl, fDsrSensitivity, 
     * fTXContinueOnXoff, fOutX, fInX, fErrorChar, fNull, fRtsControl, fAbortOnError, fDummy2, wReserved, 
     * XonLim, XoffLim, ByteSize, Parity, StopBits, XonChar, XoffChar, ErrorChar, EofChar, EvtChar and 
     * wReserved1. To extract char data type values, application must type cast value to appropriate type. 
     * For example to obtain xonchar value use; char XonChar = (char) dcbval[22];</p>
     * 
     * @param handle handle of the device.
     * @return current setting as per DCB structure.
     * @throws SerialComException if an I/O error occurs.
     */
    public int[] w32GetCommState(final long handle) throws SerialComException {
        int[] ret = mFTDID2XXJNIBridge.w32GetCommState(handle);
        if(ret == null) {
            throw new SerialComException("Could not get the current state. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes FT_W32_SetCommTimeouts function of D2XX library.</p>
     * 
     * <p>Sets the timeout parameters for I/O requests.</p>
     * 
     * @param handle handle of the device.
     * @param readIntervalTimeout The maximum time allowed to elapse before the arrival of the next byte 
     *         on the communications line, in milliseconds.
     * @param readTotalTimeoutMultiplier The multiplier used to calculate the total time-out period for 
     *         read operations, in milliseconds.
     * @param readTotalTimeoutConstant A constant used to calculate the total time-out period for read 
     *         operations, in milliseconds. 
     * @param writeTotalTimeoutMultiplier The multiplier used to calculate the total time-out period for 
     *         write operations, in milliseconds.
     * @param writeTotalTimeoutConstant A constant used to calculate the total time-out period for write 
     *         operations, in milliseconds. 
     * @return true if operation executed successfully.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean w32SetCommTimeouts(long handle, int readIntervalTimeout, int readTotalTimeoutMultiplier, 
            int readTotalTimeoutConstant, int writeTotalTimeoutMultiplier, int writeTotalTimeoutConstant) 
                    throws SerialComException {
        int ret = mFTDID2XXJNIBridge.w32SetCommTimeouts(handle, readIntervalTimeout, readTotalTimeoutMultiplier, 
                readTotalTimeoutConstant, writeTotalTimeoutMultiplier, writeTotalTimeoutConstant);
        if(ret < 0) {
            throw new SerialComException("Could not set the given timeouts. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes FT_W32_GetCommTimeouts function of D2XX library.</p>
     * 
     * <p>Gets the timeout parameters for I/O requests.</p>
     * 
     * @param handle handle of the device whose timeout values is to be fetched.
     * @return array of integers with elements in following sequence : readIntervalTimeout, 
     *          readTotalTimeoutMultiplier, readTotalTimeoutConstant, writeTotalTimeoutMultiplier 
     *          and writeTotalTimeoutConstant.
     * @throws SerialComException if an I/O error occurs.
     */
    public int[] w32GetCommTimeouts(long handle) throws SerialComException {
        int[] ret = mFTDID2XXJNIBridge.w32GetCommTimeouts(handle);
        if(ret == null) {
            throw new SerialComException("Could not get the timeout values. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes FT_W32_SetCommBreak function of D2XX library.</p>
     * 
     * <p>Puts the communications line in the BREAK state.</p>
     * 
     * @param handle handle of the device.
     * @return true if operation executed successfully.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean w32SetCommBreak(long handle) throws SerialComException {
        int ret = mFTDID2XXJNIBridge.w32SetCommBreak(handle);
        if(ret < 0) {
            throw new SerialComException("Could not put the communications line in the BREAK state. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes FT_W32_ClearCommBreak function of D2XX library.</p>
     * 
     * <p>Puts the communications line in the non-BREAK state.</p>
     * 
     * @param handle handle of the device.
     * @return true if operation executed successfully.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean w32ClearCommBreak(long handle) throws SerialComException {
        int ret = mFTDID2XXJNIBridge.w32ClearCommBreak(handle);
        if(ret < 0) {
            throw new SerialComException("Could not put the communications line in non-BREAK state. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes FT_W32_SetCommMask function of D2XX library.</p>
     * 
     * <p>Specifies events that the device has to monitor.</p>
     * 
     * @param handle handle of the device.
     * @param flag bit mask of the constants EV_XXXXX in SerialComFTDID2XX class.
     * @return true if operation executed successfully.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean w32SetCommMask(long handle, int flag) throws SerialComException {
        int ret = mFTDID2XXJNIBridge.w32SetCommMask(handle, flag);
        if(ret < 0) {
            throw new SerialComException("Could not set the communication mask. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes FT_W32_GetCommMask function of D2XX library.</p>
     * 
     * <p>Retrieves the events that are currently being monitored by a device.</p>
     * 
     * @param handle handle of the device.
     * @return bit mask of the constants EV_XXXXX in SerialComFTDID2XX class.
     * @throws SerialComException if an I/O error occurs.
     */
    public int w32GetCommMask(long handle) throws SerialComException {
        int ret = mFTDID2XXJNIBridge.w32GetCommMask(handle);
        if(ret < 0) {
            throw new SerialComException("Could not get the communication mask. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes FT_W32_WaitCommEvent function of D2XX library.</p>
     * 
     * <p>Waits for an event to occur.</p>
     * 
     * <p>Note that SerialPundit supports only non-overlapped version of FT_W32_WaitCommEvent method. If the application 
     * wishes to do anything in parallel, it should use java thread. By default this method will block.</p>
     * 
     * @param handle handle of the device.
     * @param event bit mask of the constants EV_XXXXX defined in SerialComFTDID2XX class.
     * @return true when one or more event has happened.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean w32WaitCommEvent(long handle, int event) throws SerialComException {
        int ret = mFTDID2XXJNIBridge.w32WaitCommEvent(handle, event);
        if(ret < 0) {
            throw new SerialComException("Could not wait for event specified. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes FT_W32_PurgeComm function of D2XX library.</p>
     * 
     * <p>Aborts and clear buffers as per flag bits.</p>
     * 
     * @param handle handle of the device.
     * @param event bit mask of the constants PURGE_XXXXX in this class.
     * @return true if operation executed successfully.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean w32PurgeComm(long handle, int event) throws SerialComException {
        int ret = mFTDID2XXJNIBridge.w32PurgeComm(handle, event);
        if(ret < 0) {
            throw new SerialComException("Could not purge the port as specified. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes FT_W32_GetLastError function of D2XX library.</p>
     * 
     * <p>Gets the last error that occurred on the device.</p>
     * 
     * @param handle handle of the device.
     * @return error string if any.
     * @throws SerialComException if an I/O error occurs.
     */
    public String w32GetLastError(long handle) throws SerialComException {
        String ret = mFTDID2XXJNIBridge.w32GetLastError(handle);
        if(ret == null) {
            throw new SerialComException("Could not get the last error. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes FT_W32_ClearCommError function of D2XX library.</p>
     * 
     * <p>Gets information about a communications error and get current status of the device. The returned 
     * array has following sequence of information starting at index 0: bit mask of error constants (CE_XXXX), 
     * fCtsHold, fDsrHold, fRlsdHold, fXoffHold, fXoffSent, fEof, fTxim, fReserved, cbInQue and cbOutQue.</p>
     * 
     * @param handle handle of the device.
     * @return array of integers of size 11 with information about each field.
     * @throws SerialComException if an I/O error occurs.
     */
    public int[] w32ClearCommError(long handle) throws SerialComException {
        int[] ret = mFTDID2XXJNIBridge.w32ClearCommError(handle);
        if(ret == null) {
            throw new SerialComException("Could not get the error and status information. Please retry !");
        }
        return ret;
    }
}
