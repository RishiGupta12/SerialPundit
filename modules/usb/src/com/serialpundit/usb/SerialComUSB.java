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

import java.io.IOException;

import com.serialpundit.core.SerialComException;
import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComSystemProperty;
import com.serialpundit.usb.ISerialComUSBHotPlugListener;
import com.serialpundit.usb.internal.SerialComUSBJNIBridge;

/**
 * <p>Encapsulates USB related operations and values.</p>
 * 
 * <p>An end product may be based on dedicated USB-UART bridge IC for providing serial over USB or 
 * may use general purpose microcontroller like PIC18F4550 from Microchip technology Inc. and 
 * program appropriate firmware (USB CDC) into it to provide UART communication over USB port.</p>
 *
 * <p>[1] If your USB-UART converter based design is not working, consider not connecting USB connector 
 * shield directly to ground. Further, double check if termination resistors in D+/D- lines are 
 * really required or not.</p>
 * 
 * @author Rishi Gupta
 */
public final class SerialComUSB {

    /** <p>Value indicating all vendors (vendor neutral operation).</p>*/
    public static final int V_ALL = 0x0000;

    /** <p>Value indicating vendor - Future technology devices international, Ltd. It manufactures FT232 
     * USB-UART bridge IC.</p>*/
    public static final int V_FTDI = 0x0403;

    /** <p>Value indicating vendor - Silicon Laboratories. It manufactures CP2102 USB-UART bridge IC.</p>*/
    public static final int V_SLABS = 0x10C4;

    /** <p>Value indicating vendor - Microchip technology Inc. It manufactures MCP2200 USB-UART bridge IC.</p>*/
    public static final int V_MCHIP = 0x04D8;

    /** <p>Value indicating vendor - Prolific technology Inc. It manufactures PL2303 USB-UART bridge IC.</p>*/
    public static final int V_PL = 0x067B;

    /** <p>Value indicating vendor - Exar corporation. It manufactures XR21V1410 USB-UART bridge IC.</p>*/
    public static final int V_EXAR = 0x04E2;

    /** <p>Value indicating vendor - Atmel corporation. It manufactures AT90USxxx and other processors which 
     * can be used as USB-UART bridge.</p>*/
    public static final int V_ATML = 0x03EB;

    /** <p>Value indicating vendor - MosChip semiconductor. It manufactures MCS7810 USB-UART bridge IC.</p>*/
    public static final int V_MOSCHP = 0x9710;

    /** <p>Value indicating vendor - Cypress semiconductor corporation. It manufactures CY7C65213 USB-UART 
     * bridge IC.</p>*/
    public static final int V_CYPRS = 0x04B4;

    /** <p>Value indicating vendor - Texas instruments, Inc. It manufactures TUSB3410 USB-UART bridge IC.</p>*/
    public static final int V_TI = 0x0451;

    /** <p>Value indicating vendor - WinChipHead. It manufactures CH340 USB-UART bridge IC.</p>*/
    public static final int V_WCH = 0x4348;

    /** <p>Value indicating vendor - QinHeng electronics. It manufactures HL-340 converter product.</p>*/
    public static final int V_QHE = 0x1A86;

    /** <p>Value indicating vendor - NXP semiconductors. It manufactures LPC134x series of microcontrollers.</p>*/
    public static final int V_NXP = 0x1FC9;

    /** <p>Value indicating vendor - Renesas electronics (NEC electronics). It manufactures μPD78F0730 
     * microcontroller which can be used as USB-UART converter.</p>*/
    public static final int V_RNSAS = 0x0409;

    /** <p>The value indicating that the USB device can have any vendor id and product id. </p>*/
    public static final int DEV_ANY = 0x00;

    /** <p>The value indicating that a USB device has been added into system. </p>*/
    public static final int DEV_ADDED = 0x01;

    /** <p>The value indicating that a USB device has been removed from system. </p>*/
    public static final int DEV_REMOVED  = 0x02;

    // private stuff
    private SerialComPlatform mSerialComPlatform;
    private final SerialComSystemProperty mSerialComSystemProperty;
    private final Object lockB = new Object();
    private static int osType = SerialComPlatform.OS_UNKNOWN;
    private static int cpuArch = SerialComPlatform.ARCH_UNKNOWN;
    private static int abiType = SerialComPlatform.ABI_UNKNOWN;
    private static final Object lockA = new Object();
    private static SerialComUSBJNIBridge mUSBJNIBridge;

    /**
     * <p>Allocates a new SerialComUSB object and load/link native libraries if required.</p>
     * 
     * @param libDirectory absolute path of directory to be used for native library extraction.
     * @param loadedLibName library name without extension (do not append .so, .dll or .dylib etc.).
     * @throws IOException if native libraries are not found or can not be loaded/linked. If 
     *         appropriate files/directories can not be read or written, If native library can not 
     *         be initialized.
     */
    public SerialComUSB(String libDirectory, String loadedLibName) throws IOException {

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
            if(mUSBJNIBridge == null) {
                mUSBJNIBridge = new SerialComUSBJNIBridge();
                SerialComUSBJNIBridge.loadNativeLibrary(libDirectory, loadedLibName, mSerialComSystemProperty, osType, cpuArch, abiType);
                mUSBJNIBridge.initNativeLib();
            }
        }
    }

    /**
     * <p>Returns an array of SerialComUSBdevice class objects containing information about all the USB devices 
     * found by this library. Application can call various methods on SerialComUSBdevice object to get specific 
     * information like vendor id and product id etc. The GUI applications may display a dialogue box asking 
     * user to connect the end product if the desired product is still not connected to system.</p>
     * 
     * <p>The USB vendor id, USB product id, serial number, product name and manufacturer information is 
     * encapsulated in the object of class SerialComUSBdevice returned.</p>
     * 
     * <p>Some USB-UART chip manufactures may give some unique USB PID(s) to end product manufactures at minimal 
     * or no cost. Applications written for these end products may be interested in finding devices only from the 
     * USB-UART chip manufacturer. For example, an application built for finger print scanner based on FT232 IC 
     * will like to list only those devices whose VID matches VID of FTDI. Then further application may verify 
     * PID by calling methods on the USBDevice object. For this purpose argument vendorFilter may be used.</p>
     * 
     * @param vendorFilter vendor whose devices should be listed (one of the constants SerialComUSB.V_xxxxx or 
     *        any valid USB VID).
     * @return list of the USB devices with information about them or empty array if no device matching given 
     *         criteria found.
     * @throws SerialComException if an I/O error occurs.
     * @throws IllegalArgumentException if vendorFilter is negative or invalid number.
     */
    public SerialComUSBdevice[] listUSBdevicesWithInfo(int vendorFilter) throws SerialComException {
        int i = 0;
        int numOfDevices = 0;
        SerialComUSBdevice[] usbDevicesFound = null;
        if((vendorFilter < 0) || (vendorFilter > 0XFFFF)) {
            throw new IllegalArgumentException("Argument vendorFilter can not be negative or greater than 0xFFFF !");
        }
        String[] usbDevicesInfo = mUSBJNIBridge.listUSBdevicesWithInfo(vendorFilter);

        if(usbDevicesInfo != null) {
            if(usbDevicesInfo.length < 4) {
                return new SerialComUSBdevice[] { };
            }
            numOfDevices = usbDevicesInfo.length / 6;
            usbDevicesFound = new SerialComUSBdevice[numOfDevices];
            for(int x=0; x<numOfDevices; x++) {
                usbDevicesFound[x] = new SerialComUSBdevice(usbDevicesInfo[i], usbDevicesInfo[i+1], usbDevicesInfo[i+2], 
                        usbDevicesInfo[i+3], usbDevicesInfo[i+4], usbDevicesInfo[i+5]);
                i = i + 6;
            }
            return usbDevicesFound;
        }else {
            throw new SerialComException("Could not find USB devices. Please retry !");
        }   
    }

    /**
     * <p>This registers a listener who will be invoked whenever a USB device has been plugged or 
     * un-plugged in system. This method can be used to write auto discovery applications for example 
     * when a hardware USB device is added to system, application can automatically detect, identify 
     * it and launches an appropriate service.</p>
     * 
     * <p>This API can be used for detecting both USB-HID and USB-CDC devices. Essentially this API is 
     * USB interface agnostic; meaning it would invoke listener for matching USB device irresepective of 
     * functionality offered by the USB device.</p> 
     * 
     * <p>Application must implement ISerialComUSBHotPlugListener interface and override onUSBHotPlugEvent method. 
     * The event value SerialComUSB.DEV_ADDED indicates USB device has been added to the system. The event 
     * value SerialComUSB.DEV_REMOVED indicates USB device has been removed from system.</p>
     * 
     * <p>Application can specify the usb device for which callback should be called based on USB VID and 
     * USB PID. If the value of filterVID is specified however the value of filterPID is constant SerialComUSB.DEV_ANY, 
     * then callback will be called for USB device which matches given VID and its PID can have any value. 
     * If the value of filterPID is specified however the value of filterVID is constant SerialComUSB.DEV_ANY, 
     * then callback will be called for USB device which matches given PID and its VID can have any value.</p>
     * 
     * <p>If both filterVID and filterPID are set to SerialComUSB.DEV_ANY, then callback will be called for 
     * every USB device. Further in listener, USBVID will be 0, if SerialComUSB.DEV_ANY was passed to registerUSBHotPlugEventListener 
     * for filterVID argument. USBPID will be 0, if SerialComUSB.DEV_ANY was passed to registerUSBHotPlugEventListener for filterPID 
     * and serialNumber will be empty string if null was passed to registerUSBHotPlugEventListener for serialNumber argument.</p>
     * 
     * <p>If the application do not know the USB attributes like the VID/PID/Serial of the device use listUSBdevicesWithInfo() 
     * along with hot plug listener. For example; call listUSBdevicesWithInfo(SerialComUSB.V_ALL) and create and arraylist 
     * to know what all USB devices are present in system and register this hotplug listener. Now whenever a USB device 
     * is attached to system, listener will be called. From with this listener call listUSBdevicesWithInfo(SerialComUSB.V_ALL) 
     * and create arraylist again. Compare these two list to find information about newly added device and take next 
     * appropriate step<p>
     * 
     * @param hotPlugListener object of class which implements ISerialComUSBHotPlugListener interface.
     * @param filterVID USB vendor ID to match.
     * @param filterPID USB product ID to match.
     * @param serialNumber serial number of USB device (case insensitive, optional, can be null) to match.
     * @return opaque handle on success that should be passed to unregisterUSBHotPlugEventListener method 
     *          for unregistering this listener.
     * @throws SerialComException if registration fails due to some reason.
     * @throws IllegalArgumentException if hotPlugListener is null or if USB VID or USB PID are invalid numbers.
     */
    public int registerUSBHotPlugEventListener(final ISerialComUSBHotPlugListener hotPlugListener, 
            int filterVID, int filterPID, String serialNumber) throws SerialComException {

        int opaqueHandle = 0;

        if(hotPlugListener == null) {
            throw new IllegalArgumentException("Argument hotPlugListener can not be null !");
        }

        if((filterVID < 0) || (filterPID < 0) || (filterVID > 0XFFFF) || (filterPID > 0XFFFF)) {
            throw new IllegalArgumentException("USB VID or PID can not be negative number(s) or greater than 0xFFFF !");
        }

        synchronized(lockB) {
            opaqueHandle = mUSBJNIBridge.registerUSBHotPlugEventListener(hotPlugListener, filterVID, filterPID, serialNumber);
            if(opaqueHandle < 0) {
                throw new SerialComException("Could not register USB device hotplug listener. Please retry !");
            }
        }

        return opaqueHandle;
    }

    /**
     * <p>This unregisters listener and terminate native thread used for monitoring specified USB device 
     * insertion or removal.</p>
     * 
     * @param opaqueHandle handle returned by registerUSBHotPlugEventListener method for this listener.
     * @return true on success.
     * @throws SerialComException if un-registration fails due to some reason.
     * @throws IllegalArgumentException if argument opaqueHandle is negative.
     */
    public boolean unregisterUSBHotPlugEventListener(final int opaqueHandle) throws SerialComException {
        int ret = 0;

        if(opaqueHandle < 0) {
            throw new IllegalArgumentException("Argument opaqueHandle can not be negative !");
        }

        synchronized(lockB) {
            ret = mUSBJNIBridge.unregisterUSBHotPlugEventListener(opaqueHandle);
            if(ret < 0) {
                throw new SerialComException("Could not un-register USB device hotplug listener. Please retry !");
            }
        }

        return true;
    }

    /**
     * <p>Gives COM port (COMxx/ttySx) of a connected USB-UART device (CDC/ACM Interface) assigned by operating 
     * system.</p>
     * 
     * <p>Assume a bar code scanner using FTDI chip FT232R is to be used by application at point of sale.
     * First we need to know whether it is connect to system or not. This can be done using 
     * listUSBdevicesWithInfo() or by using USB hot plug listener depending upon application design.</p>
     * 
     * <p>Once it is known that the device is connected to system, we application need to open it. For this, 
     * application needs to know the COM port number or device node corresponding to the scanner. It is for 
     * this purpose this method can be used.</p>
     * 
     * <p>Another use case of this API is to align application design with true spirit of USB hot plugging 
     * in operating system. When a USB-UART device is connected, OS may assign different COM port number or 
     * device node to the same device depending upon system scenario. Generally we need to write custom udev 
     * rules so that device node will be same. Using this API this limitation can be overcome.
     * 
     * <p>The reason why this method returns array instead of string is that two or more USB-UART converters 
     * connected to system might have exactly same USB attributes. So this will list COM ports assigned to all 
     * of them.<p>
     * 
     * @param usbVidToMatch USB vendor id of the device to match.
     * @param usbPidToMatch USB product id of the device to match.
     * @param serialNumber USB serial number (case insensitive, optional) of device to match or null if not 
     *        to be matched.
     * @return list of COM port(s) (device node) for given USB device or empty array if no COM port is assigned.
     * @throws SerialComException if an I/O error occurs.
     * @throws IllegalArgumentException if usbVidToMatch or usbPidToMatch is negative or or invalid number.
     */
    public String[] findComPortFromUSBAttributes(int usbVidToMatch, int usbPidToMatch, final String serialNumber) throws SerialComException {
        if((usbVidToMatch < 0) || (usbVidToMatch > 0XFFFF)) {
            throw new IllegalArgumentException("Argument usbVidToMatch can not be negative or greater than 0xFFFF !");
        }
        if((usbPidToMatch < 0) || (usbPidToMatch > 0XFFFF)) {
            throw new IllegalArgumentException("Argument usbPidToMatch can not be negative or greater than 0xFFFF !");
        }
        String serialNum = null;
        if(serialNumber != null) {
            serialNum = serialNumber.toLowerCase();
        }

        String[] comPortsInfo = mUSBJNIBridge.findComPortFromUSBAttribute(usbVidToMatch, usbPidToMatch, serialNum);
        if(comPortsInfo == null) {
            throw new SerialComException("Could not find COM port for given device. Please retry !");
        }

        return comPortsInfo;
    }

    /**
     * <p>Gets the USB device firmware revision number as reported by USB device descriptor in its 
     * device descriptor using bcdDevice field.</p>
     * 
     * <p>Application can get this firmware revision number and can self adopt to a particular hardware 
     * device. For example if a particular feature is present in firmware version 1.00 than application 
     * create a button in GUI, however for revision 1.11 it creates a different button in GUI window.</p>
     *
     * <p>Embedded system device vendors sometimes use bcdDevice value to indicate the 'embedded bootloader'
     * version so that the firmware image flash loader program can identify the bootloader in use and use the 
     * appropriate protocol to flash firmware in flash memory. Typically, USB Device Firmware Upgrade (DFU) 
     * which is an official USB device class specification of the USB Implementers Forum is used.</p>
     *
     * <p>On custom hardware the RTS and DTR pins of USB-UART device can be used to control GPIO or boot mode
     * pins to enter a particular boot mode. For example, open the host serial port, make DTR low and RTS high 
     * and then reset microcontroller board. The microcontroller will see levels at its boot pins and will
     * enter into a particular boot mode.</p>
     * 
     * @param usbvid USB vendor ID to match.
     * @param usbpid USB product ID to match.
     * @param serialNumber serial number of USB device (case insensitive, optional) to match.
     * @return firmware number revision string on success.
     * @throws SerialComException if an I/O error occurs.
     */
    public String[] getFirmwareRevisionNumber(int usbvid, int usbpid, String serialNumber) throws SerialComException {

        if((usbvid < 0) || (usbvid > 0XFFFF)) {
            throw new IllegalArgumentException("Argument usbvid can not be negative or greater than 0xFFFF !");
        }
        if((usbpid < 0) || (usbpid > 0XFFFF)) {
            throw new IllegalArgumentException("Argument usbpid can not be negative or greater than 0xFFFF !");
        }

        String serial = null;
        if(serialNumber != null) {
            serial = serialNumber.trim().toLowerCase();
        }

        String[] bcdCodedRevNumber = mUSBJNIBridge.getFirmwareRevisionNumber(usbvid, usbpid, serial);
        if(bcdCodedRevNumber == null) {
            throw new SerialComException("Could not get the bcdDevice field of USB device descriptor. Please retry !");
        }

        String[] fwver = new String[bcdCodedRevNumber.length];
        int firmwareRevisionNum;

        for(int x=0; x < bcdCodedRevNumber.length; x++) {
            firmwareRevisionNum = Integer.parseInt(bcdCodedRevNumber[x]);
            fwver[x] = String.format("%x.%02x", (firmwareRevisionNum & 0xFF00) >> 8, firmwareRevisionNum & 0x00FF);
        }

        return fwver;
    }

    /**
     * <p>Read all the power management related information about a particular USB device. The returned 
     * instance of SerialComUSBPowerInfo class contains information about auto suspend, selective suspend,
     * current power status etc.</p>
     * 
     * 
     * @param comPort serial port name/path (COMxx, /dev/ttyUSBx) which is associated with a particular
     *        USB CDC/ACM interface in the USB device to be analyzed for power management.
     * @return an instance of SerialComUSBPowerInfo class containing operating system and device specific 
     *         information about power management or null if given COM port does not belong to a USB 
     *         device.
     * @throws SerialComException if an I/O error occurs.
     */
    public SerialComUSBPowerInfo getCDCUSBDevPowerInfo(String comPort) throws SerialComException {
        if(comPort == null) {
            throw new IllegalArgumentException("Argument comPort can not be null !");
        }
        String portNameVal = comPort.trim();
        if(portNameVal.length() == 0) {
            throw new IllegalArgumentException("Argument comPort can not be empty string !");
        }

        String[] usbPowerInfo = mUSBJNIBridge.getCDCUSBDevPowerInfo(portNameVal);
        if(usbPowerInfo != null) {
            if(usbPowerInfo.length > 2) {
                return new SerialComUSBPowerInfo(usbPowerInfo[0], usbPowerInfo[1], usbPowerInfo[2], 
                        usbPowerInfo[3], usbPowerInfo[4], usbPowerInfo[5]);
            }
        }else {
            throw new SerialComException("Could not find USB devices. Please retry !");
        }

        return null;
    }

    /**
     * <p>Sets the latency timer value for FTDI devices. When using FTDI USB-UART devices, optimal values 
     * of latency timer and read/write block size may be required to obtain optimal data throughput.</p>
     * 
     * <p>Note that built-in drivers in Linux kernel image may not allow changing timer values as it may have 
     * been hard-coded. Drivers supplied by FTDI at their website should be used if changing latency timer 
     * values is required by application.</p>
     * 
     * @return true on success.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean setLatencyTimer(String comPort, byte timerValue) throws SerialComException {
        int ret = mUSBJNIBridge.setLatencyTimer(comPort, timerValue);
        if(ret < 0) {
            throw new SerialComException("Could not set the latency timer value. Please retry !");
        }
        return true;
    }

    /**
     * <p>Gets the current latency timer value for FTDI devices.</p>
     * 
     * @return current latency timer value.
     * @throws SerialComException if an I/O error occurs.
     */
    public int getLatencyTimer(String comPort) throws SerialComException {
        int value = mUSBJNIBridge.getLatencyTimer(comPort);
        if(value < 0) {
            throw new SerialComException("Could not get the latency timer value. Please retry !");
        }
        return value;
    }

    /**
     * <p>Checks whether a particular USB device identified by vendor id, product id and serial number is 
     * connected to the system or not. The connection information is obtained from the operating system.</p>
     * 
     * @param vendorID USB-IF vendor ID of the USB device to match.
     * @param productID product ID of the USB device to match.
     * @param serialNumber USB device's serial number (case insensitive, optional). If not required it can be null.
     * @return true is device is connected otherwise false.
     * @throws SerialComException if an I/O error occurs.
     * @throws IllegalArgumentException if productID or vendorID is negative or invalid.
     */
    public boolean isUSBDevConnected(int vendorID, int productID, String serialNumber) throws SerialComException {
        if((vendorID < 0) || (vendorID > 0XFFFF)) {
            throw new IllegalArgumentException("Argument vendorID can not be negative or greater than 0xFFFF !");
        }
        if((productID < 0) || (productID > 0XFFFF)) {
            throw new IllegalArgumentException("Argument productID can not be negative or greater than 0xFFFF !");
        }

        int ret = mUSBJNIBridge.isUSBDevConnected(vendorID, productID, serialNumber);
        if(ret < 0) {
            throw new SerialComException("Unknown error occurred !");
        }else if(ret == 1) {
            return true;
        }
        return false;
    }

    /**
     * <p>Returns an instance of class SerialComUSBHID for HID over USB operations.</p>
     * 
     * @return an instance of SerialComUSBHID.
     * @throws SerialComException if operation can not be completed successfully.
     */
    public SerialComUSBHID getUSBHIDTransportInstance() throws SerialComException {
        return new SerialComUSBHID(mUSBJNIBridge);
    }
}
