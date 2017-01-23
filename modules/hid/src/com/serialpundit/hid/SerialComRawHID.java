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

import java.io.IOException;
import java.util.HashMap;
import java.util.TreeMap;

import com.serialpundit.core.SerialComException;
import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComSystemProperty;
import com.serialpundit.core.util.SerialComUtil;
import com.serialpundit.hid.internal.HIDdevHandleInfo;
import com.serialpundit.hid.internal.InputReportListenerState;
import com.serialpundit.hid.internal.SerialComHIDJNIBridge;

/* Executes as a worker thread waiting for input reports, reading whenever available and delivering 
 * them to the registered listener. */
final class HIDInputReportReader implements Runnable {

    private final long handle;
    private final long context;
    private final IHIDInputReportListener listener;
    private byte[] inputReportBuffer;
    private final SerialComRawHID scrh;
    private final InputReportListenerState irls;
    private int ret;

    public HIDInputReportReader(InputReportListenerState irls, long handle, final IHIDInputReportListener listener, 
            byte[] inputReportBuffer, long context, SerialComRawHID scrh) {
        this.irls = irls;
        this.handle = handle;
        this.listener = listener;
        this.inputReportBuffer = inputReportBuffer;
        this.context = context;
        this.scrh = scrh;
    }

    public void run() {
        while(true) {
            try {
                ret = 0;
                ret = scrh.readInputReportR(handle, inputReportBuffer, context);
                // deliver input report
                if(ret > 0) {
                    listener.onNewInputReportAvailable(ret, inputReportBuffer);
                }
            } catch (SerialComException e) {
                if(SerialComHID.EXP_UNBLOCK_HIDIO.equals(e.getExceptionMsg())) {
                    // this thread should exit as other thread indicated it to return.
                    return;
                }
                listener.onNewInputReportAvailableError(e);
            } catch (Exception e1) {
                listener.onNewInputReportAvailableError(e1);
            }
            if(irls.shouldInputReportListenerExit()) {
                return;
            }
        }
    }
}

/**
 * <p>Contains APIs to communicate with a HID class device in raw mode. The reports sent/received 
 * will not be parsed. The application must understand meaning and format of each field in report 
 * exchanged.</p>
 *
 * <p>The application using HID protocol is advantageous when deploying the application in a corporate 
 * environment sensitive to the installation of kernel code(driver).</p>
 * 
 * <p>This API for human interface device communication uses kernel mode drivers provided by operating system 
 * by default.</p>
 * 
 * <p>Applications may develop user space drivers using raw HID methods in this class.</p>
 * 
 * <table summary="">
 * <tr><td>
 * <p><strong>1: Device discovery</strong></p>
 * listHIDdevicesWithInfoR<br/>
 * 
 * <p><strong>3 : Information</strong></p>
 * getManufacturerStringR<br/>
 * getProductStringR<br/>
 * getSerialNumberStringR<br/>
 * getIndexedStringR<br/>
 * findDriverServingHIDDeviceR<br/>
 * </td><td>
 * <p><strong>2 : Data/Configuration exchange</strong></p>
 * writeOutputReportR<br/>
 * readInputReportR<br/>
 * readInputReportWithTimeoutR<br/>
 * sendFeatureReportR<br/>
 * getFeatureReportR<br/>
 * createBlockingHIDIOContextR<br/>
 * unblockBlockingHIDIOOperationR<br/>
 * destroyBlockingIOContextR<br/>
 * </td><td>
 * <p><strong>4 : Miscellaneous</strong></p>
 * openHidDeviceR<br/>
 * closeHidDeviceR<br/>
 * flushInputReportQueueR<br/>
 * getReportDescriptorR<br/>
 * getPhysicalDescriptorR<br/>
 * formatReportToHexR<br/>
 * </td></tr>
 * </table>
 * 
 * @author Rishi Gupta
 */
public final class SerialComRawHID extends SerialComHID {

    // used to synchronize access to treemap if caller can modify treemap.
    private final Object lock = new Object();

    // This provides guaranteed log(n) time complexity for the containsKey, get, put and remove operations.
    // It maps opened handle of HID device to its information object.
    private final TreeMap<Long, HIDdevHandleInfo> devInfo;

    // Instead of linearly traversing tree to find context corresponding to listener, we maintain 
    // a hashmap to decrease look up time.
    private final HashMap<IHIDInputReportListener, Long> listenerToHandleMap;

    private static SerialComHIDJNIBridge mHIDJNIBridge;
    private final SerialComSystemProperty mSerialComSystemProperty;
    private static final Object lockA = new Object();

    private SerialComPlatform mSerialComPlatform;
    private static int osType = SerialComPlatform.OS_UNKNOWN;
    private static int cpuArch = SerialComPlatform.ARCH_UNKNOWN;
    private static int abiType = SerialComPlatform.ABI_UNKNOWN;

    /**
     * <p>Construct and allocates a new SerialComRawHID object with given details and load/link 
     * native libraries if required.</p>
     * 
     * @param libDirectory absolute path of directory to be used for native library extraction.
     * @param loadedLibName library name without extension (do not append .so, .dll or .dylib etc.).
     * @throws IOException if libDirectory does not exist, or is not a regular directory or is not 
     * writable, if native libraries are not found or can not be loaded/linked or initialized etc.
     */
    public SerialComRawHID(String libDirectory, String loadedLibName) throws IOException {

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
            if(mHIDJNIBridge == null) {
                mHIDJNIBridge = new SerialComHIDJNIBridge();
                SerialComHIDJNIBridge.loadNativeLibrary(libDirectory, loadedLibName, mSerialComSystemProperty, osType, cpuArch, abiType);
                int ret = mHIDJNIBridge.initNativeLib();
                if(ret < 0) {
                    throw new SerialComException("Failed to initilize the native library. Please retry !");
                }
            }
        }

        devInfo = new TreeMap<Long, HIDdevHandleInfo>();
        listenerToHandleMap = new HashMap<IHIDInputReportListener, Long>();
    }

    /**
     * <P>Find all the device instances claiming to be HID device.</p>
     * 
     * <p>Returns an array of SerialComHIDdevice objects containing information about HID devices 
     * as found by this library. The HID devices found may be USB HID or Bluetooth HID device. 
     * Application can call various  methods on returned SerialComHIDdevice object to get specific 
     * information like vendor id and product id etc.</p>
     * 
     * <p>The information about HID device returned includes, transport, vendor ID, product ID, serial 
     * number, product, manufacturer, USB bus number, USB device number, location ID etc. In situations 
     * where two or more devices with exactly same vendor ID, product ID and serial number are present 
     * into system, information like location can be used to further categories them into unique devices. 
     * Application can also use some custom protocol to identify devices that are of interest to them.</p>
     * 
     * <p>If you know that the HID device is USB device than consider using listUSBHIDdevicesWithInfo 
     * method in SerialComUSBHID class.</p>
     * 
     * @return list of the HID devices with information about them or empty array if no device 
     *          matching given criteria found.
     * @throws SerialComException if an I/O error occurs.
     */
    public SerialComHIDdevice[] listHIDdevicesWithInfoR() throws SerialComException {
        int i = 0;
        int numOfDevices = 0;
        SerialComHIDdevice[] hidDevicesFound = null;

        String[] hidDevicesInfo = mHIDJNIBridge.listHIDdevicesWithInfoR();

        if(hidDevicesInfo != null) {
            if(hidDevicesInfo.length < 3) {
                return new SerialComHIDdevice[] { };
            }
            numOfDevices = hidDevicesInfo.length / 8;
            hidDevicesFound = new SerialComHIDdevice[numOfDevices];
            for(int x=0; x < numOfDevices; x++) {
                hidDevicesFound[x] = new SerialComHIDdevice(hidDevicesInfo[i], hidDevicesInfo[i+1], hidDevicesInfo[i+2], 
                        hidDevicesInfo[i+3], hidDevicesInfo[i+4], hidDevicesInfo[i+5], hidDevicesInfo[i+6],
                        hidDevicesInfo[i+7]);
                i = i + 8;
            }
            return hidDevicesFound;
        }else {
            throw new SerialComException("Could not find HID devices. Please retry !");
        }	
    }

    /**
     * <p>Converts report read from HID device to hexadecimal string. This may be 
     * useful when report is to be passed to next level as hex data or report is to be 
     * feed into external HID report parser tool.</p>
     * 
     * @param report report to be converted into hex string.
     * @param separator separator string to be placed between two consecutive bytes (useful 
     *         when printing values on console).
     * @return constructed hex string if report.length > 0 otherwise empty string.
     * @throws IllegalArgumentException if report is null.
     */
    public String formatReportToHexR(byte[] report, String separator) throws SerialComException {
        return SerialComUtil.byteArrayToHexString(report, separator);
    }

    /**
     * <p>Opens a HID device for communication using its path name. Applications should first list HID devices 
     * to get the path of the desired device using methods like listUSBHIDdevicesWithInfo etc.</p>
     * 
     * <P>Applications can register USB hot plug listener to get notified when the desired USB device 
     * is plugged into system. Once the listener is invoked indicating device is added, application 
     * can find the device node representing this USB-HID device and proceed to open it.</p>
     * 
     * <p>In Linux it may be required to add correct udev rules so as to grant permission to 
     * access to the USB-HID device. Refer this udev rule file for MCP2200 as an example : 
     * https://github.com/RishiGupta12/serial-communication-manager/blob/master/tools-and-utilities/99-scm-mcp2200-hid.rules</p>
     * 
     * <p>In Windows, a unique physical device object (PDO) is created for each Top Level Collection 
     * described by the Report Descriptor and there will be device instance for each Top Level 
     * Collection. This means same USB HID interface may have many HID device instances associated 
     * with it.</p>
     * 
     * <p>Windows supports many top level collection and some of them might be opened in shared mode while 
     * may be available for exclusive access only. Some of the HID devices may be reserved for system use 
     * only and operating system may provide a dedicated framework/driver and API for it. Some devices 
     * need to be switched from keyboard emulation mode to HID mode to make them accessible by application.</p>
     * 
     * @param pathName device node full path for Unix-like OS and device instance for Windows 
     *         (as obtained by listing HID devices).
     * @param shared set to true if the device is to be opened in shared mode otherwise false 
     *         for exclusive access.
     * @return handle of the opened HID device.
     * @throws SerialComException if an IO error occurs.
     * @throws IllegalArgumentException if pathName is null or empty string.
     */
    public long openHidDeviceR(final String pathName, boolean shared) throws SerialComException {
        if(pathName == null) {
            throw new IllegalArgumentException("Argument pathName can not be null !");
        }

        String pathNameVal = pathName.trim();
        if(pathNameVal.length() == 0) {
            throw new IllegalArgumentException("Argument pathName can not be empty string !");
        }

        long handle = mHIDJNIBridge.openHidDeviceR(pathNameVal, shared, osType);
        if(handle < 0) {
            /* JNI should have already thrown exception, this is an extra check to increase reliability of program. */
            throw new SerialComException("Could not open the HID device " + pathNameVal + ". Please retry !");
        }

        // save data info internally for later use
        synchronized(lock) {
            devInfo.put(handle, new HIDdevHandleInfo(null));
        }
        return handle;
    }

    /**
     * <p>Closes a HID device.</p>
     * 
     * @param handle handle of the device to be closed.
     * @return true if device's handle closed successfully.
     * @throws SerialComException if fails to close the device or an IO error occurs.
     * @throws IllegalStateException if application tries to close handle when input report listener still exist.
     */
    public boolean closeHidDeviceR(long handle) throws SerialComException {		

        HIDdevHandleInfo info = devInfo.get(handle);
        if(info == null) {
            throw new SerialComException("Given handle does not represent a HID device opened through SCM !");
        }

        if(info.getInputReportListener() != null) {
            throw new IllegalStateException("Closing device handle without unregistering input report listener is not allowed to prevent inconsistency !");
        }

        int ret = mHIDJNIBridge.closeHidDeviceR(handle);
        if(ret < 0) {
            throw new SerialComException("Could not close the given HID device. Please retry !");
        }

        // remove local data info
        synchronized(lock) {
            devInfo.remove(handle);
        }
        return true;
    }

    /** 
     * <p>Prepares a context that should be passed to readInputReportR, unblockBlockingHIDIOOperationR 
     * and destroyBlockingIOContextR methods.</p>
     * 
     * <p>Application must catch exception thrown by this method. When this method returns and 
     * exception with message SerialComHID.EXP_UNBLOCK_HIDIO is thrown, it indicates that the 
     * blocked read method was explicitly unblocked by another thread (possibly because it is 
     * going to close the device).</p>
     * 
     * @return context that should be passed to readInputReportR, unblockBlockingHIDIOOperationR and 
     *          destroyBlockingIOContextR methods.
     * @throws SerialComException if an I/O error occurs.
     */
    public long createBlockingHIDIOContextR() throws SerialComException {
        long ret = mHIDJNIBridge.createBlockingHIDIOContextR();
        if(ret < 0) {
            throw new SerialComException("Could not create blocking HID I/O context. Please retry !");
        }
        return ret;
    }

    /** 
     * <p>Unblocks any blocked operation if it exist. This causes closing of HID device possible 
     * gracefully and return the worker thread that called blocking read/write to return and proceed 
     * as per application design.</p>
     * 
     * @param context context obtained from call to createBlockingHIDIOContextR method for blocking 
     *         HID I/O operations.
     * @return true if blocked operation was unblocked successfully.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean unblockBlockingHIDIOOperationR(long context) throws SerialComException {
        int ret = mHIDJNIBridge.unblockBlockingHIDIOOperationR(context);
        if(ret < 0) {
            throw new SerialComException("Could not unblock the blocked HID I/O operation. Please retry !");
        }
        return true;
    }

    /** 
     * <p>Destroys the context that was created by a call to createBlockingHIDIOContextR method for 
     * blocking I/O operations uses.</p>
     * 
     * @param context context obtained from call to createBlockingIOContext method for blocking 
     *         HID I/O operations.
     * @return true if the context gets destroyed successfully.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean destroyBlockingIOContextR(long context) throws SerialComException {
        int ret = mHIDJNIBridge.destroyBlockingIOContextR(context);
        if(ret < 0) {
            throw new SerialComException("Could not destroy blocking HID I/O context. Please retry !");
        }
        return true;
    }

    /**
     * <p>Sends the given output report to the HID device. Report ID is used to uniquely identify the  
     * report.</p>
     * 
     * <p>Output report (controls) may be a sink for application data, for example, an LED that indicates 
     * the state of a device. It can represent a command sent from application running on host to USB HID 
     * device for example to toggle a GPIO pin or vibrate the motor mounted on gamepad.</p>
     * 
     * <p>If the HID device uses numbered report, reportID should be set to report number. If the HID 
     * device does not uses numbered reports reportID must be set to -1. The report (report array) should 
     * should contain only report bytes (it should not contain report ID).</p>
     * 
     * @param handle handle of the HID device to which this report will be sent.
     * @param reportId unique identifier for the report type or -1 if device does not use report IDs.
     * @param report report to be sent to the HID device.
     * @return number of bytes sent to the HID device.
     * @throws SerialComException if an I/O error occurs.
     * @throws IllegalArgumentException if report is null or empty array. 
     */
    public int writeOutputReportR(long handle, byte reportId, final byte[] report) throws SerialComException {
        if((report == null) || (report.length == 0)) {
            throw new IllegalArgumentException("Argument report can not be null or of zero length!");
        }

        int ret = mHIDJNIBridge.writeOutputReportR(handle, reportId, report, report.length);
        if(ret < 0) {
            throw new SerialComException("Could not write output report to the HID device. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Reads input report from the given HID device.</p>
     * 
     * <p>The size of {@code reportBuffer} passed must be large enough to hold the expected number of bytes 
     * in input report and one more extra byte if the HID device uses numbered reports. The 1st byte will be 
     * report ID if device uses numbered reports otherwise the report data will begin at the first byte.</p>
     * 
     * <p>If input report is read from device, it returns number of bytes read and places data bytes in 
     * given buffer. If the device uses numbered reports, first byte in reportBuffer array will be report 
     * number. If the device does not uses numbered reports, first byte in reportBuffer will be beginning 
     * of data itself.</p>
     * 
     * <p>HID devices with custom firmware provide valid HID report descriptor to comply with USB 
     * standards and to make sure that class driver of operating system recognizes device and serve it. 
     * However, the data carried in reports may have different meaning and interpretation than what was 
     * described in report descriptor. This is the case mainly when developing custom application HID device 
     * which can not be strictly categorized as a HID device, however, leverages HID specifications and 
     * API to communicate with the host system. Vendors provide a document describing how to interpret a 
     * particular byte in report received from device or how to construct an output report.</p>
     * 
     * @param handle handle of the HID device from whom input report is to be read.
     * @param reportBuffer byte buffer in which input report will be saved.
     * @param context context obtained by a call to createBlockingIOContext method.
     * @return number of bytes read from HID device.
     * @throws SerialComException if an I/O error occurs.
     * @throws IllegalArgumentException if reportBuffer is null or if length is negative.
     */
    public int readInputReportR(long handle, byte[] reportBuffer, long context) throws SerialComException {
        if((reportBuffer == null) || (reportBuffer.length == 0)) {
            throw new IllegalArgumentException("Argument reportBuffer can not be null or of zero length!");
        }

        int ret = mHIDJNIBridge.readInputReportR(handle, reportBuffer, reportBuffer.length, context);
        if(ret < 0) {
            throw new SerialComException("Could not read input report from HID device. Please retry !");
        }

        return ret;
    }

    /**
     * <p>Try to read input report from HID device within the given timeout limit.</p>
     * 
     * <p>The size of {@code reportBuffer} passed must be large enough to hold the expected number of bytes 
     * in input report and one more extra byte if the HID device uses numbered reports. The 1st byte will be 
     * report ID if device uses numbered reports otherwise the report data will begin at the first byte.</p>
     * 
     * <p>If input report is read from HID device, it returns number of bytes read and places data bytes in 
     * given buffer. If there was no data to read it returns 0.</p>
     * 
     * <p>Input report (controls) are sources of data for application running on host processor (USB Host side) 
     * for example X and Y coordinates obtained from touch screen or state of a GPIO pin. It can also be a 
     * response to a command sent previously as output report.</p>
     * 
     * @param handle handle of the HID device from whom input report is to be read.
     * @param reportBuffer byte buffer in which input report will be saved.
     * @param timeoutValue time in milliseconds after which read must return with whatever data is read 
     *         till that time or no data read at all.
     * @return number of bytes read from HID device.
     * @throws SerialComException if an I/O error occurs.
     * @throws IllegalArgumentException if reportBuffer is null or if length is negative.
     */
    public int readInputReportWithTimeoutR(long handle, byte[] reportBuffer, int timeoutValue) 
            throws SerialComException {

        if(reportBuffer == null) {
            throw new IllegalArgumentException("Argumenet reportBuffer can not be null !");
        }

        int ret = mHIDJNIBridge.readInputReportWithTimeoutR(handle, reportBuffer, reportBuffer.length, timeoutValue);
        if(ret < 0) {
            throw new SerialComException("Could not read input report from HID device. Please retry !");
        }

        return ret;
    }

    /**
     * <p>Send a feature report to the HID device. If the HID device uses numbered reports, set reportID 
     * to report number. If the HID device does not uses numbered reports set reportID to -1. The report 
     * byte array should contain only report data bytes.</p>
     * 
     * <p>Typically, feature reports are sent/received for configuring USB device or USB host at application 
     * start-up, or for sending/receiving special event or state information, or for saving any data item that 
     * application wish to write in HID device and read it back may be some time later.</p>
     * 
     * @param handle handle of the HID device to which this feature report will be sent.
     * @param reportId unique identifier for the report type or -1 if not applicable.
     * @param report feature report to be sent to the HID device.
     * @return number of bytes sent to HID device.
     * @throws SerialComException if an I/O error occurs.
     * @throws IllegalArgumentException if report is null or empty array. 
     */
    public int sendFeatureReportR(long handle, byte reportId, final byte[] report) throws SerialComException {
        if(report == null) {
            throw new IllegalArgumentException("Argumenet report can not be null !");
        }
        if(report.length == 0) {
            throw new IllegalArgumentException("Argumenet report can not be of zero length !");
        }

        int ret = mHIDJNIBridge.sendFeatureReportR(handle, reportId, report, report.length);
        if(ret < 0) {
            throw new SerialComException("Could not send feature report to HID device. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Read a feature report to the HID device. If the HID device uses numbered reports, set reportID 
     * to report number. If the HID device does not uses numbered reports set reportID to -1. If the 
     * featured report is read from HID device, data read will be placed in report byte array. This 
     * array will contain feature report (excluding report ID).</p>
     * 
     * <p>Typically, feature reports are sent/received for configuring USB device or USB host at application 
     * start-up, or for sending/receiving special event or state information, or for saving any data item that 
     * application wish to write in HID device and read it back may be some time later.</p>
     * 
     * @param handle handle of the HID device from whom feature report is to be read.
     * @param reportId unique identifier for the report type or -1 if not applicable.
     * @param report byte type buffer where feature report will be saved.
     * @return number of bytes read from HID device.
     * @throws SerialComException if an I/O error occurs.
     * @throws IllegalArgumentException if reportBuffer is null or its length is zero.
     */
    public int getFeatureReportR(long handle, byte reportId, final byte[] report) throws SerialComException {
        if(report == null) {
            throw new IllegalArgumentException("Argumenet report can not be null !");
        }

        int ret = mHIDJNIBridge.getFeatureReportR(handle, reportId, report, report.length);
        if(ret < 0) {
            throw new SerialComException("Could not get feature report from HID device. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Gives the manufacturer of the HID device.</p>
     * 
     * @param handle handle of the HID device whose manufacturer is to be found.
     * @return manufacturer name string.
     * @throws SerialComException if an I/O error occurs.
     */
    public String getManufacturerStringR(long handle) throws SerialComException {
        String ret = mHIDJNIBridge.getManufacturerStringR(handle);
        if(ret == null) {
            throw new SerialComException("Could not get the manufacturer string from the HID device. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Gives the product name of the HID device.</p>
     * 
     * @param handle handle of the HID device whose product name is to be found.
     * @return product name string of the HID device.
     * @throws SerialComException if an I/O error occurs.
     */
    public String getProductStringR(long handle) throws SerialComException {
        String ret = mHIDJNIBridge.getProductStringR(handle);
        if(ret == null) {
            throw new SerialComException("Could not get the product string from the HID device. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Gives the serial number of the HID device.</p>
     * 
     * @param handle handle of the HID device whose serial number is to be found.
     * @return serial number string of the HID device.
     * @throws SerialComException if an I/O error occurs.
     */
    public String getSerialNumberStringR(long handle) throws SerialComException {
        String ret = mHIDJNIBridge.getSerialNumberStringR(handle);
        if(ret == null) {
            throw new SerialComException("Could not get the serial number string from the HID device. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Gives the string at the given index of string descriptor from HID device.</p>
     * 
     * <p>Supported on Windows only as SerialPundit does not use any user space drivers.</p>
     * 
     * @param handle handle of the HID device from whom indexed string is to be read.
     * @return string at given index read from the HID device.
     * @throws SerialComException if an I/O error occurs.
     */
    public String getIndexedStringR(long handle, int index) throws SerialComException {
        if(osType == SerialComPlatform.OS_WINDOWS) {
            String ret = mHIDJNIBridge.getIndexedStringR(handle, index);
            if(ret == null) {
                throw new SerialComException("Could not get the string at given index from the HID device. Please retry !");
            }
            return ret;
        }else {
            throw new SerialComException("Not supported on this operating system !");
        }
    }

    /**
     * <p>Gives the name of the driver who is driving the given HID device.</p>
     * 
     * @param hidDeviceNode device node (port name) for HID device whose driver is to be found.
     * @return name of driver serving given HID device.
     * @throws SerialComException if operation can not be completed successfully.
     * @throws IllegalArgumentException if argument hidDeviceNode is null or is an empty string.
     */
    public String findDriverServingHIDDeviceR(String hidDeviceNode) throws SerialComException {
        if(hidDeviceNode == null) {
            throw new IllegalArgumentException("Argument hidDeviceNode can not be null !");
        }
        if(hidDeviceNode.length() == 0) {
            throw new IllegalArgumentException("Argument hidDeviceNode can not be empty string !");
        }
        if(hidDeviceNode.length() > 256) {
            // Linux may have 256 as maximum length of file name.
            throw new IllegalArgumentException("Argument hidDeviceNode string can not be greater than 256 in length !");
        }

        String driverName = mHIDJNIBridge.findDriverServingHIDDeviceR(hidDeviceNode);
        if(driverName == null) {
            throw new SerialComException("Failed to find driver serving the given HID device. Please retry !");
        }
        return driverName;
    }

    /**
     * <p>Gives the report descriptor as supplied by device itself.</p>
     * 
     * @param handle handle of the device whose report descriptor is to be obtained.
     * @return HID report descriptor as array of bytes otherwise empty array.
     * @throws SerialComException if operation can not be completed successfully.
     */
    public byte[] getReportDescriptorR(long handle) throws SerialComException {
        byte[] reportDescriptorRead = mHIDJNIBridge.getReportDescriptorR(handle);
        if(reportDescriptorRead != null) {
            return reportDescriptorRead;
        }
        return new byte[0];
    }

    /**
     * <p>Gives the physical descriptor for the given HID device.</p>
     * 
     * <p>Physical Descriptors are entirely optional. They add complexity and offer very little in 
     * return for most devices. However, some devices, particularly those with a large number of 
     * identical controls (for example, buttons) will find that Physical Descriptors help different 
     * applications assign functionality to these controls in a more consistent manner.</p>
     * 
     * @param handle handle of the device whose physical descriptor is to be obtained.
     * @return HID physical descriptor as array of bytes otherwise empty array.
     * @throws SerialComException if operation can not be completed successfully.
     */
    public byte[] getPhysicalDescriptorR(long handle) throws SerialComException {
        byte[] physicalDescriptorRead = mHIDJNIBridge.getPhysicalDescriptorR(handle);
        if(physicalDescriptorRead != null) {
            return physicalDescriptorRead;
        }
        return new byte[0];
    }

    /**
     * <p>Deletes all the input reports from input report buffer maintained by operating system.</p>
     * 
     * @param handle handle of the device whose input report queue is to be flushed.
     * @return true on success.
     * @throws SerialComException if operation can not be completed successfully.
     */
    public boolean flushInputReportQueueR(long handle) throws SerialComException {
        int ret = mHIDJNIBridge.flushInputReportQueueR(handle);
        if (ret < 0) {
            throw new SerialComException("Could not flush the input report queue. Please retry !");
        }
        return true;
    }

    /**
     * <p>Registers a listener which will be invoked whenever an input report is available to read.</p>
     * 
     * @param handle of the HID device for which input reports are to be listened.
     * @param listener instance of class which implements IHIDInputReportListener interface.
     * @param inputReportBuffer byte buffer that will contain report read from HID device.
     * @return true on success.
     * @throws SerialComException if the registration fails due to some reason.
     * @throws IllegalArgumentException if input report listener is null.
     * @throws IllegalStateException if input report listener already exist for given handle.
     */
    public boolean registerInputReportListener(long handle, final IHIDInputReportListener listener, byte[] inputReportBuffer) throws SerialComException {

        if(listener == null) {
            throw new IllegalArgumentException("Argument listener can not be null !");
        }

        HIDdevHandleInfo info = devInfo.get(handle);
        if(info != null) {
            if(info.getInputReportListener() != null) {
                throw new IllegalStateException("Input report listener already exist for given handle !");
            }
        }

        InputReportListenerState irls = info.getInputReportListenerStateInstance();
        if(irls == null) {
            irls = new InputReportListenerState();
        }else {
            irls.setInputReportListenerState(false);
        }

        long context = createBlockingHIDIOContextR();
        Thread dataReaderThread = new Thread(new HIDInputReportReader(irls, handle, listener, inputReportBuffer, context, this));

        synchronized(lock) {
            info.setInputReportListenerStateInstance(irls);
            info.setInputReportListener(listener);
            info.setListenerContext(context);
            listenerToHandleMap.put(listener, handle);
            dataReaderThread.start();
        }

        return true;
    }

    /**
     * <p>This unregisters listener and terminates worker thread which was delivering input reports.</p>
     * 
     * @param listener reference to class which implemented IHIDInputReportListener interface to get input reports.
     * @return true on success.
     * @throws SerialComException if un-registration fails due to some reason.
     * @throws IllegalArgumentException if listener is null or given listener is not registered.
     */
    public boolean unregisterInputReportListener(final IHIDInputReportListener listener) throws SerialComException {

        if(listener == null) {
            throw new IllegalArgumentException("Argument listener can not be null !");
        }

        long handle = listenerToHandleMap.get(listener);

        HIDdevHandleInfo info = devInfo.get(handle);
        if(info == null) {
            throw new IllegalArgumentException("Invalid listener passed for unregistration !");
        }

        long context = info.getListenerContext();
        InputReportListenerState irls = info.getInputReportListenerStateInstance();

        synchronized(lock) {
            irls.setInputReportListenerState(true);
            unblockBlockingHIDIOOperationR(context);
            Thread.yield(); // let worker thread's read method get unblocked and thread to exit itself.
            destroyBlockingIOContextR(context);
            info.setInputReportListener(null);
            info.setInputReportListenerStateInstance(null);
            listenerToHandleMap.remove(listener);
        }

        return true;
    }

    /**
     * <p>Read input report from given HID device and handles inconsistencies in reports internally using 
     * facilities provided by operating system. For example some device send/receive reports which are inconsistent 
     * with what was described in their HID report descriptor. For some devices their firmware can not be modified 
     * and this is where this method can be used.</p>
     * 
     * <p>For Windows operating system the methods readInputReportWithTimeoutR and readInputReportR uses ReadFile 
     * function, whereas readPlatformSpecificInputReportR uses HidD_GetInputReport function.</p>
     * 
     * <p>The size of reportBuffer passed must be equal to the number of bytes(fields) in input report (excluding 
     * report ID whether devices uses numbered reports or not).</p>
     *  
     * @param handle handle of the HID device from whom input report is to be read.
     * @param reportId unique identifier for the report type or -1 if device does not use report IDs.
     * @param reportBuffer byte buffer in which input report will be saved.
     * @return true on success.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean readPlatformSpecificInputReportR(long handle, byte reportId, byte[] reportBuffer) throws SerialComException {
        if(reportBuffer == null) {
            throw new IllegalArgumentException("Argumenet reportBuffer can not be null !");
        }

        int ret = mHIDJNIBridge.readPlatformSpecificInputReportR(handle, reportId, reportBuffer, reportBuffer.length);
        if(ret < 0) {
            throw new SerialComException("Could not read input report from HID device. Please retry !");
        }

        return true;
    }

    /**
     * <p>Sends an output report to given HID device and handles inconsistencies in reports internally using 
     * facilities provided by operating system. For example some device send/receive reports which are inconsistent 
     * with what was described in their HID report descriptor. For some devices their firmware can not be modified 
     * and this is where this method can be used.</p>
     * 
     * <p>For Windows operating system the method writeOutputReportR uses WriteFile function, whereas 
     * writePlatformSpecificOutputReportR uses HidD_SetOutputReport function.</p>
     * 
     * @param handle handle of the HID device to whom this output report is to be sent.
     * @param reportId unique identifier for the report type or -1 if device does not use report IDs.
     * @param reportBuffer buffer containing output report to be sent to device.
     * @return true on success.
     * @throws SerialComException
     */
    public boolean writePlatformSpecificOutputReportR(long handle, byte reportId, byte[] reportBuffer) throws SerialComException {
        if(reportBuffer == null) {
            throw new IllegalArgumentException("Argumenet reportBuffer can not be null !");
        }

        int ret = mHIDJNIBridge.writePlatformSpecificOutputReportR(handle, reportId, reportBuffer, reportBuffer.length);
        if(ret < 0) {
            throw new SerialComException("Could not read input report from HID device. Please retry !");
        }

        return true;
    }
}
