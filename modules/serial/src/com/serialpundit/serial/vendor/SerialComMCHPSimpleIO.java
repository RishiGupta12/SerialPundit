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
import com.serialpundit.serial.internal.SerialComMCHPSIOJNIBridge;

/**
 * <p>Microchip Technology Inc provides a shared library known as 'SimpleIO' for communicating with their 
 * USB-UART devices. More information can be found here : 
 * http://www.microchip.com/wwwproducts/devices.aspx?dDocName=en546923</p>
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
 * <li>SerialPundit version 1.0.4 is linked to 1.4.0 version of SimpleIO library (SimpleIO-UM.dll).</li>
 * </ul>
 * 
 * @author Rishi Gupta
 */
public final class SerialComMCHPSimpleIO extends SerialComVendorLib {

    /**<p>Constant representing off state.</p>*/
    public static final int OFF = 0;

    /**<p>Constant representing on state.</p>*/
    public static final int ON = 1;

    /**<p>Constant representing toggling in operational state.</p>*/
    public static final int TOGGLE = 3;

    /**<p>Constant representing slow rate of blinking LED.</p>*/
    public static final int BLINKSLOW = 4;

    /**<p>Constant representing fast rate of blinking LED.</p>*/
    public static final int BLINKFAST = 5;

    private final SerialComMCHPSIOJNIBridge mSerialComMCHPSIOJNIBridge;

    /**
     * <p>Allocates a new SerialComMCHPSimpleIO object and extract and load shared libraries as required.</p>
     * 
     * @param libDirectory directory in which native library will be extracted and vendor library will be found.
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
    public SerialComMCHPSimpleIO(File libDirectory, String vlibName, int cpuArch, int osType, 
            SerialComSystemProperty serialComSystemProperty) throws SerialComException {

        mSerialComMCHPSIOJNIBridge = new SerialComMCHPSIOJNIBridge();
        SerialComMCHPSIOJNIBridge.loadNativeLibrary(libDirectory, vlibName, cpuArch, osType, serialComSystemProperty);
    }

    /**
     * <p>Executes InitMCP2200 function of 'SimpleIO-xxxx' library.</p>
     * <p>Configures the Simple IO class for a specific Vendor and product ID.</p>
     * 
     * @param vendorID USB vendor ID as Assigned by USB IF (www.usb.org).
     * @param productID USB product ID of this device belongs to.
     * @return true if the requested operation executed successfully.
     * @throws SerialComException if an I/O error occurs.
     * @throws IllegalArgumentException if vendorID or productID is negative or invalid number.
     */
    public boolean initMCP2200(int vendorID, int productID) throws SerialComException {
        if((vendorID < 0) || (vendorID > 0XFFFF)) {
            throw new IllegalArgumentException("initMCP2200(), " + "Argument vendorID can not be negative or greater tha 0xFFFF !");
        }
        if((productID < 0) || (productID > 0XFFFF)) {
            throw new IllegalArgumentException("initMCP2200(), " + "Argument productID can not be negative or greater tha 0xFFFF !");
        }

        int ret = mSerialComMCHPSIOJNIBridge.initMCP2200(vendorID, productID);
        if(ret < 0) {
            throw new SerialComException("Could not configure the for specific device. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes IsConnected function of 'SimpleIO-xxxx' library.</p>
     * <p>Checks with the OS to see if the current VID/PID device is connected.</p>
     * 
     * @return true if the device is connected to host otherwise false.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean isConnected() throws SerialComException {
        int ret = mSerialComMCHPSIOJNIBridge.isConnected();
        if(ret < 0) {
            throw new SerialComException("Could not determine whether device is connected or not. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes ConfigureMCP2200 function of 'SimpleIO-xxxx' library.</p>
     * <p>Configures the device. Sets the default GPIO designation, baud rate, TX/RX Led modes, flow control.</p>
     * 
     * <p>The argument rxLEDMode and txLEDMode can be one of these constants OFF, ON, TOGGLE, BLINKSLOW, BLINKFAST 
     * defined in SerialComMCHPSimpleIO class.</p>
     * 
     * @param ioMap A byte which represents the input/output state of the pins (each bit may be either a 1 for 
     *         input, and 0 for output).
     * @param baudRateParam the default communication baud rate.
     * @param rxLEDMode defines the behavior of the RX Led.
     * @param txLEDMode defines the behavior of the TX Led.
     * @param flow establishes the default flow control method (false - no HW flow control, true - RTS/CTS flow 
     *         control).
     * @param uload enables/disables the GP1 pin as a USB configuration status indicator.
     * @param sspnd enables/disables the GP0 pin as a USB suspend status pin.
     * @param invert enables/disables the UART lines states: (1) Normal – Tx/Rx idle high; CTS/RTS active low, 
     *         (2) Inverted – Tx/Rx idle low; CTS/RTS active high.
     * @return true if the device is configured as given.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean configureMCP2200(byte ioMap, long baudRateParam, int rxLEDMode, int txLEDMode, boolean flow,
            boolean uload, boolean sspnd, boolean invert) throws SerialComException {
        int ret = mSerialComMCHPSIOJNIBridge.configureMCP2200(ioMap, baudRateParam, rxLEDMode, txLEDMode, 
                flow, uload, sspnd, invert);
        if(ret < 0) {
            throw new SerialComException("Could not configure the device using given parameters. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes SetPin function of 'SimpleIO-xxxx' library.</p>
     * <p>Sets the specified pin to logic '1'.</p>
     * 
     * @param pinNumber number of the pin which need to be set to logic 1.
     * @return true if the requested operation executed successfully.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean setPin(int pinNumber) throws SerialComException {
        int ret = mSerialComMCHPSIOJNIBridge.setPin(pinNumber);
        if(ret < 0) {
            throw new SerialComException("Could not set the given pin to logic 1. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes ClearPin function of 'SimpleIO-xxxx' library.</p>
     * <p>Sets the specified pin to logic '0'.</p>
     * 
     * @param pinNumber number of the pin which need to be set to logic 0.
     * @return true if the requested operation executed successfully.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean clearPin(int pinNumber) throws SerialComException {
        int ret = mSerialComMCHPSIOJNIBridge.clearPin(pinNumber);
        if(ret < 0) {
            throw new SerialComException("Could not set the given pin to logic 1. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes ReadPinValue function of 'SimpleIO-xxxx' library.</p>
     * <p>Reads the specified pin's value/state.</p>
     * 
     * @param pinNumber number of the pin whose value is to be read.
     * @return value which will be 0 or 1 depending upon pin's state.
     * @throws SerialComException if an I/O error occurs.
     */
    public int readPinValue(int pinNumber) throws SerialComException {
        int ret = mSerialComMCHPSIOJNIBridge.readPinValue(pinNumber);
        if(ret < 0) {
            throw new SerialComException("Could not read the given pin's value. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes ReadPin function of 'SimpleIO-xxxx' library.</p>
     * <p>Reads the specified pin's value/state.</p>
     * 
     * @param pinNumber number of the pin whose value is to be read.
     * @return value which will be 0 or 1 depending upon pin's state.
     * @throws SerialComException if an I/O error occurs.
     */
    public int readPin(int pinNumber) throws SerialComException {
        int ret = mSerialComMCHPSIOJNIBridge.readPin(pinNumber);
        if(ret < 0) {
            throw new SerialComException("Could not read the given pin's value. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes WritePort function of 'SimpleIO-xxxx' library.</p>
     * <p>Writes the given value to the GPIO port.</p>
     * 
     * @param portValue byte value to set on the port.
     * @return true if the requested operation executed successfully.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean writePort(int portValue) throws SerialComException {
        int ret = mSerialComMCHPSIOJNIBridge.writePort(portValue);
        if(ret < 0) {
            throw new SerialComException("Could not write the given value to port. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes ReadPort function of 'SimpleIO-xxxx' library.</p>
     * <p>Reads the GPIO port as digital input.</p>
     * 
     * @return port value read.
     * @throws SerialComException if an I/O error occurs.
     */
    public int readPort() throws SerialComException {
        int ret = mSerialComMCHPSIOJNIBridge.readPort();
        if(ret < 0) {
            throw new SerialComException("Could not read the port value. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes ReadPortValue function of 'SimpleIO-xxxx' library.</p>
     * <p>Reads the GPIO port as digital input.</p>
     * 
     * @return port value read.
     * @throws SerialComException if an I/O error occurs.
     */
    public int readPortValue() throws SerialComException {
        int ret = mSerialComMCHPSIOJNIBridge.readPortValue();
        if(ret < 0) {
            throw new SerialComException("Could not read the port value. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes SelectDevice function of 'SimpleIO-xxxx' library.</p>
     * <p>Selects one of the active devices in the system.</p>
     * 
     * @param uiDeviceNumber the ID of the device to select (can have a value between 0 to (number 
     *         of devices - 1).
     * @return true if the requested device gets selected successfully.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean selectDevice(int uiDeviceNumber) throws SerialComException {
        int ret = mSerialComMCHPSIOJNIBridge.selectDevice(uiDeviceNumber);
        if(ret < 0) {
            throw new SerialComException("Could not select the given device. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes GetSelectedDevice function of 'SimpleIO-xxxx' library.</p>
     * <p>Gets the currently selected device from one of the active devices in the system.</p>
     * 
     * @return ID of the current selected device.
     * @throws SerialComException if an I/O error occurs.
     */
    public int getSelectedDevice() throws SerialComException {
        int ret = mSerialComMCHPSIOJNIBridge.getSelectedDevice();
        if(ret < 0) {
            throw new SerialComException("Could not get the currently selected device. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes GetNoOfDevices function of 'SimpleIO-xxxx' library.</p>
     * <p>Find the number of available devices present in the system.</p>
     * 
     * @return the number of HID devices with the given (as parameters of InitMCP2200() function) VID/PID.
     * @throws SerialComException if an I/O error occurs.
     */
    public int getNumOfDevices() throws SerialComException {
        int ret = mSerialComMCHPSIOJNIBridge.getNumOfDevices();
        if(ret < 0) {
            throw new SerialComException("Could not find the number of devices currently present in system. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes GetDeviceInfo function of 'SimpleIO-xxxx' library.</p>
     * <p>Find the pathname for one of the connected devices.</p>
     * 
     * @param uiDeviceNumber device number about whom information is to be fetched.
     * @return a string containing the pathname of the given device id.
     * @throws SerialComException if an I/O error occurs.
     */
    public String getDeviceInfo(int uiDeviceNumber) throws SerialComException {
        String ret = mSerialComMCHPSIOJNIBridge.getDeviceInfo(uiDeviceNumber);
        if(ret == null) {
            throw new SerialComException("Could not find the path name of the requested device. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes GetSelectedDeviceInfo function of 'SimpleIO-xxxx' library.</p>
     * <p>Find the pathname for currently selected device.</p>
     * 
     * @return a string containing the pathname of the selected device.
     * @throws SerialComException if an I/O error occurs.
     */
    public String getSelectedDeviceInfo() throws SerialComException {
        String ret = mSerialComMCHPSIOJNIBridge.getSelectedDeviceInfo();
        if(ret == null) {
            throw new SerialComException("Could not find the path name of the selected device. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes ReadEEPROM function of 'SimpleIO-xxxx' library.</p>
     * <p>Reads a byte from the EEPROM at the given address.</p>
     * 
     * @param uiEEPAddress EEPROM address from where to read value.
     * @return value at given address.
     * @throws SerialComException if an I/O error occurs.
     */
    public int readEEPROM(int uiEEPAddress) throws SerialComException {
        int ret = mSerialComMCHPSIOJNIBridge.readEEPROM(uiEEPAddress);
        if(ret < 0) {
            throw new SerialComException("Could not read the value from given EEPROM address. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes WriteEEPROM function of 'SimpleIO-xxxx' library.</p>
     * <p>Reads a byte value to the given EEPROM address.</p>
     * 
     * @param uiEEPAddress EEPROM address to write at.
     * @param ucValue value to write at given address.
     * @return true of value gets written successfully at given address.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean writeEEPROM(int uiEEPAddress, short ucValue) throws SerialComException {
        int ret = mSerialComMCHPSIOJNIBridge.writeEEPROM(uiEEPAddress, ucValue);
        if(ret < 0) {
            throw new SerialComException("Could not write the value at given EEPROM address. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes fnRxLED function of 'SimpleIO-xxxx' library.</p>
     * <p>Configures the Rx LED mode. Rx LED configuration will be stored in NVRAM.</p>
     * 
     * <p>The argument mode can be one of these constants OFF, ON, TOGGLE, BLINKSLOW, BLINKFAST 
     * defined in SerialComMCHPSimpleIO class.</p>
     * 
     * @param mode mode as described above.
     * @return true if the mode is set successfully.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean fnRxLED(int mode) throws SerialComException {
        int ret = mSerialComMCHPSIOJNIBridge.fnRxLED(mode);
        if(ret < 0) {
            throw new SerialComException("Could not set the operating mode for RX LED. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes fnTxLED function of 'SimpleIO-xxxx' library.</p>
     * <p>Configures the Tx LED mode. Tx LED configuration will be stored in NVRAM.</p>
     * 
     * <p>The argument mode can be one of these constants OFF, ON, TOGGLE, BLINKSLOW, BLINKFAST 
     * defined in SerialComMCHPSimpleIO class.</p>
     * 
     * @param mode mode as described above.
     * @return true if the mode is set successfully.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean fnTxLED(int mode) throws SerialComException {
        int ret = mSerialComMCHPSIOJNIBridge.fnTxLED(mode);
        if(ret < 0) {
            throw new SerialComException("Could not set the operating mode for TX LED. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes fnHardwareFlowControl function of 'SimpleIO-xxxx' library.</p>
     * <p>Sets the flow control to HW flow control (RTS/CTS) or No flow control.</p>
     * 
     * @param onOff if 1 hardware flow control will be set. if 0 no flow control will be set.
     * @return true if the flow control gets successfully.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean hardwareFlowControl(int onOff) throws SerialComException {
        int ret = mSerialComMCHPSIOJNIBridge.hardwareFlowControl(onOff);
        if(ret < 0) {
            throw new SerialComException("Could not set the flow control as requested. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes fnULoad function of 'SimpleIO-xxxx' library.</p>
     * <p>Configures the GP1 pin of the MCP2200 to show the status of the USB configuration.</p>
     * 
     * @param onOff if 1 GP1 will reflect the USB configuration status, if 0 GP1 will not reflect the USB configuration status (can be used as GPIO).
     * @return true if GP1 gets configured successfully.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean fnULoad(int onOff) throws SerialComException {
        int ret = mSerialComMCHPSIOJNIBridge.fnULoad(onOff);
        if(ret < 0) {
            throw new SerialComException("Could not configure the GP1 as requested. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes fnSuspend function of 'SimpleIO-xxxx' library.</p>
     * <p>Configures the MCP2200 to invert the UART polarity or not.</p>
     * 
     * @param onOff if 1 GP0 will reflect the USB Suspend/Resume states, if 0 GP0 will not reflect 
     *         the USB Suspend/Resume states (can be used as GPIO).
     * @return true if GP0 gets configured successfully.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean fnSuspend(int onOff) throws SerialComException {
        int ret = mSerialComMCHPSIOJNIBridge.fnSuspend(onOff);
        if(ret < 0) {
            throw new SerialComException("Could not configure the GP0 as requested. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes fnInvertUartPol function of 'SimpleIO-xxxx' library.</p>
     * <p>Configures the MCP2200 to invert the UART polarity or not.</p>
     * 
     * @param onOff if 1 invert the UART polarity, if 0 leave the polarity as default.
     * @return true if polarity gets configured successfully.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean fnInvertUartPol(int onOff) throws SerialComException {
        int ret = mSerialComMCHPSIOJNIBridge.fnInvertUartPol(onOff);
        if(ret < 0) {
            throw new SerialComException("Could not configure the UART polarity as requested. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes fnSetBaudRate function of 'SimpleIO-xxxx' library.</p>
     * <p>Sets the desired baudrate and it will store it into device's NVRAM.</p>
     * 
     * @param baudRateParam baud rate value to set.
     * @return true if baud rate gets set successfully.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean fnSetBaudRate(long baudRateParam) throws SerialComException {
        int ret = mSerialComMCHPSIOJNIBridge.fnSetBaudRate(baudRateParam);
        if(ret < 0) {
            throw new SerialComException("Could not set the baud rate on device as requested. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes ConfigureIO function of 'SimpleIO-xxxx' library.</p>
     * <p>Configures the GPIO pins for Digital Input, Digital Output.</p>
     * 
     * @param ioMap a byte which represents a bitmap of the GPIO configuration. A bit set to '1' 
     *         will be a digital input, a bit set to '0' will be a digital output.
     * @return true if GPIO gets configured successfully.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean configureIO(short ioMap) throws SerialComException {
        int ret = mSerialComMCHPSIOJNIBridge.configureIO(ioMap);
        if(ret < 0) {
            throw new SerialComException("Could not configure the GPIO as requested. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes ConfigureIoDefaultOutput function of 'SimpleIO-xxxx' library.</p>
     * <p>Configures the IO pins for Digital Input, Digital Output and also the default output latch value.</p>
     * 
     * @param ioMap a byte which represents a bitmap of the GPIO configuration. A bit set to '1' 
     *         will be a digital input, a bit set to '0' will be a digital output.
     * @param ucDefValue the default value that will be loaded to the output latch (effect only on the pins 
     *         configured as outputs).
     * @return true if GPIO gets configured successfully.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean configureIoDefaultOutput(short ioMap, short ucDefValue) throws SerialComException {
        int ret = mSerialComMCHPSIOJNIBridge.configureIoDefaultOutput(ioMap, ucDefValue);
        if(ret < 0) {
            throw new SerialComException("Could not configure the GPIO as requested. Please retry !");
        }
        return true;
    }
}
