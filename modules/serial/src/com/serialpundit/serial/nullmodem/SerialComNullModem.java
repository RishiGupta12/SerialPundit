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

package com.serialpundit.serial.nullmodem;

import java.io.IOException;

import com.serialpundit.core.SerialComException;
import com.serialpundit.serial.internal.SerialComPortJNIBridge;

/**
 * <p>Provides APIs to create and destroy virtual serial ports using the tty2com null modem emulation 
 * driver provided by serialpundit. It follows the protocol as defined by tty2com driver.</p>
 * 
 * @author Rishi Gupta
 */
public final class SerialComNullModem {

    /**<p> Bit mask bit specifying that the given pin should be left unconnected.</p> 
     * Constant with value 0x0000. </p>*/
    public static final int SP_CON_NONE = 0x0000;

    /**<p> Bit mask bit specifying that the given pin should be connected to CTS pin of other end. 
     * Constant with value 0x0001. </p>*/
    public static final int SP_CON_CTS = 0x0001;

    /**<p> Bit mask bit specifying that the given pin should be connected to DCD pin of other end. 
     * Constant with value 0x0002. </p>*/
    public static final int SP_CON_DCD = 0x0002;

    /**<p> Bit mask bit specifying that the given pin should be connected to DSR pin of other end. 
     * Constant with value 0x0004. </p>*/
    public static final int SP_CON_DSR = 0x0004;

    /**<p> Bit mask bit specifying that the given pin should be connected to RI pin of other end. 
     * Constant with value 0x0008. </p>*/
    public static final int SP_CON_RI  = 0x0008;

    /**<p> Bit mask bit specifying that a framing error should be emulated.</p> 
     * Constant with value 0x0010. </p>*/
    public static final int ERR_FRAME = 0x0010;

    /**<p> Bit mask bit specifying that a parity error should be emulated.</p> 
     * Constant with value 0x0020. </p>*/
    public static final int ERR_PARITY = 0x0020;

    /**<p> Bit mask bit specifying that a overrun error should be emulated.</p> 
     * Constant with value 0x0040. </p>*/
    public static final int ERR_OVERRUN = 0x0040;

    /**<p> Bit mask bit specifying that a break condition reception should be emulated.</p> 
     * Constant with value 0x0080. </p>*/
    public static final int RCV_BREAK = 0x0080;

    /** <p>The value indicating that a virtual serial device has been added into system. </p>*/
    public static final int DEV_ADDED = 0x01;

    /** <p>The value indicating that a virtual serial device has been removed from system. </p>*/
    public static final int DEV_REMOVED  = 0x02;

    private final SerialComPortJNIBridge mComPortJNIBridge;
    private final Object lockA = new Object();
    private final Object lockB = new Object();

    /**
     * <p>Create an instance of SerialComNullModem with given details.</p>
     * 
     * @param mComPortJNIBridge native interface.
     * @throws SerialComException if any exception occurs while preparing for communication using virtual device.
     */
    public SerialComNullModem(SerialComPortJNIBridge mComPortJNIBridge) throws SerialComException {
        this.mComPortJNIBridge = mComPortJNIBridge;
    }

    /**
     * <p>Allocate resources and initialize as required.</p>
     * 
     * @throws SerialComException if driver is not loaded or initialization fails.
     */
    public void initialize() throws SerialComException {
        mComPortJNIBridge.setuptty2com();
    }

    /**
     * <p>Release resources if any was acquired and perform clean up tasks as required. After calling 
     * this method if the null modem facilities is to be used again than call initialize method again.</p>
     * 
     * @throws SerialComException if the task fails.
     */
    public void deinitialize() throws SerialComException {
        mComPortJNIBridge.unsetuptty2com();
    }

    /**
     * <p>Gives the device node names that can be used to create next virtual serial port.</p>
     * 
     * <p>Returned array contains empty string if no more ports are free to be created.</p>
     * 
     * @return device node names (two strings) that can be used for next virtual serial port.
     * @throws IOException if the operation can not be completed for some reason.
     */
    public String[] listNextAvailablePorts() throws SerialComException {
        return mComPortJNIBridge.listNextAvailablePorts();
    }

    /**
     * <p>Gives all existing virtual serial ports which are connected in standard null modem fashion. In returned array, 
     * the port at even index is connected to the port at following odd index. For example :<br>
     * String[] list = scnm.listExistingStandardNullModemPorts();<br>
     * list[0] <-----> list[1]</p>
     * 
     * <p>Empty array is returned if no standard null modem pair is found.</p>
     * 
     * @return existing ports which are connected in standard null modem fashion.
     * @throws IOException if the operation can not be completed for some reason.
     */
    public String[] listExistingStandardNullModemPorts() throws SerialComException {
        return mComPortJNIBridge.listExistingStandardNullModemPorts();
    }

    /**
     * <p>Gives all existing virtual serial ports which are connected in custom null modem fashion. In returned 
     * array, the port at even index is connected to the port at following odd index. For example :<br>
     * String[] list = scnm.listExistingCustomNullModemPorts();<br>
     * list[0] <-----> list[4]</p>
     * 
     * <p>The sequence of information returned is shown below. The RTS and DTR mappings are returned 
     * in string form. The caller has to convert them into int data type and then constant bit mask 
     * SerialComNullModem.SP_CON_XXX can be used.<br>
     * x + 0: 1st port's name/path <br>
     * x + 1: 1st port's RTS mappings <br>
     * x + 2: 1st port's DTR mappings <br>
     * x + 3: 1st port's DTR state when opening port <br>
     * x + 4: 2nd port's name/path <br>
     * x + 5: 2nd port's RTS mappings <br>
     * x + 6: 2nd port's DTR mappings <br>
     * x + 7: 2nd port's DTR state when opening port <br></p>
     * 
     * <p>Empty array is returned if no custom null modem pair is found.</p>
     * 
     * @return existing ports which are connected in custom null modem fashion.
     * @throws IOException if the operation can not be completed for some reason.
     */
    public String[] listExistingCustomNullModemPorts() throws SerialComException {
        return mComPortJNIBridge.listExistingCustomNullModemPorts();
    }

    /**
     * <p>Gives all existing ports which are connected in standard loopback fashion.</p>
     * 
     * @return existing ports which are connected in standard loopback fashion.
     * @throws IOException if the operation can not be completed for some reason.
     */
    public String[] listExistingStandardLoopbackPorts() throws SerialComException {
        return mComPortJNIBridge.listExistingStandardLoopbackPorts();
    }

    /**
     * <p>Gives all existing virtual serial ports which are connected in standard loopback fashion.</p>
     * 
     * <p>The sequence of information returned is shown below. The RTS and DTR mappings are returned 
     * in string form. The caller has to convert them into int data type and then constant bit mask 
     * SerialComNullModem.SP_CON_XXX can be used.<br>
     * x + 0: port's name <br>
     * x + 1: port's RTS mappings <br>
     * x + 2: port's DTR mappings <br>
     * x + 3: port's DTR state when opening port <br></p>
     * 
     * <p>Empty array is returned if no custom loop back device is found.</p>
     * 
     * @return existing ports which are connected in custom null modem fashion.
     * @throws IOException if the operation can not be completed for some reason.
     */
    public String[] listExistingCustomLoopbackPorts() throws SerialComException {
        return mComPortJNIBridge.listExistingCustomLoopbackPorts();
    }

    /**
     * <p>Gives names of all existing virtual serial ports created by driver.</p>
     * 
     * @return names of all existing virtual ports created by driver.
     * @throws IOException if the operation can not be completed for some reason.
     */
    public String[] listAllExistingPorts() throws SerialComException {
        return mComPortJNIBridge.listAllExistingPorts();
    }

    /**
     * <p>Gives all existing virtual serial ports created by null modem driver driver.</p>
     * 
     * <p>The sequence of information returned is shown below. The RTS and DTR mappings are returned 
     * in string form. The caller has to convert them into int data type and then constant bit mask 
     * SerialComNullModem.SP_CON_XXX can be used.<br>
     * x + 0: port's name <br>
     * x + 1: port's RTS mappings <br>
     * x + 2: port's DTR mappings <br>
     * x + 3: port's DTR state when opening port <br></p>
     * 
     * <p>Empty array is returned if no devices are found.</p>
     * 
     * @return names of all existing virtual ports created by driver with their mappings.
     * @throws IOException if the operation can not be completed for some reason.
     */
    public String[] listAllExistingPortsWithInfo() throws SerialComException {
        return mComPortJNIBridge.listAllExistingPortsWithInfo();
    }

    /**
     * <p>Creates two virtual ports/devices connected in standard null modem fashion. If deviceIndex is -1, 
     * the next available index will be used by driver. If deviceIndex is a valid number, the given index 
     * will be used to create device nodes.</p>
     * 
     * <p>For example; createStandardNullModemPair(2, 3) will create /dev/tty2com2 and /dev/tty2com3 device 
     * nodes in Linux or will throw exception if any of the given number is already in use. Similarly the 
     * createStandardNullModemPair(-1, -1) will create /dev/tty2comXX and /dev/tty2comYY where XX/YY are the 
     * next free numbers managed by the driver internally.</p>
     * 
     * @param deviceIndex1 -1 or valid device number (0 <= deviceIndex1 =< 65535).
     * @param deviceIndex2 -1 or valid device number (0 <= deviceIndex2 =< 65535).
     * @return Created virtual null modem pair device's node on success.
     * @throws SerialComException if virtual null modem device pair can not be created,
     *         IllegalArgumentException if deviceIndex1/2 is invalid.
     */
    public String[] createStandardNullModemPair(int deviceIndex1, int deviceIndex2) throws SerialComException {

        String[] result = null;

        if((deviceIndex1 < -1) || (deviceIndex1 > 65535)) {
            throw new IllegalArgumentException("deviceIndex1 should be -1 <= deviceIndex1 =< 65535 !");
        }
        if((deviceIndex2 < -1) || (deviceIndex2 > 65535)) {
            throw new IllegalArgumentException("deviceIndex2 should be -1 <= deviceIndex1 =< 65535 !");
        }
        if((deviceIndex1 != -1) && (deviceIndex2 != -1) && (deviceIndex1 == deviceIndex2)) {
            throw new IllegalArgumentException("Both deviceIndex1 and deviceIndex2 can not be same !");
        }

        synchronized (lockA) {
            result = mComPortJNIBridge.createStandardNullModemPair(deviceIndex1, deviceIndex2);
        }

        return result;
    }

    /**
     * <p>Creates two virtual ports/devices connected in null modem fashion with given signal mappings. 
     * If idxYY is -1, the next available index will be used by driver. If idxYY is a valid number, the 
     * given index will be used to create device nodes.</p>
     * 
     * <p>For example; createCustomNullModemPair(2, 0, 0, 3, 0, 0) will create /dev/tty2com2 and the 
     * /dev/tty2com3 device nodes in Linux or will throw exception if any of the given number is already 
     * in use. Similarly the createCustomNullModemPair(-1, 0, 0, -1, 0, 0) will create /dev/tty2comXX and 
     * /dev/tty2comYY where XX/YY are the next free numbers managed by the driver internally.</p>
     * 
     * <p>To connect RTS pin to CTS pin use rtsMap = SerialComNullModem.SP_CON_CTS. A pin can be 
     * connected to one or more pins using bit mask. For example to connect RTS pin to CTS and DSR use 
     * rtsMap = SerialComNullModem.SP_CON_CTS | SerialComNullModem.SP_CON_DSR.</p>
     * 
     * @param idx1 -1 or valid device number (0 <= idx1 =< 65535).
     * @param rtsMap1 Bit mask of SerialComNullModem.SCM_CON_XXX constants as per the desired pin mappings 
     *        or 0 if RTS pin should be left unconnected.
     * @param dtrMap1 Bit mask of SerialComNullModem.SCM_CON_XXX constants as per the desired pin mappings 
     *        or 0 if DTR pin should be left unconnected.
     * @param setDTRatOpen1 if true DTR will be asserted when serial port is opened otherwise it will not be raised.
     * @param idx2 -1 or valid device number (0 <= idx2 =< 65535).
     * @param rtsMap2 Bit mask of SerialComNullModem.SCM_CON_XXX constants as per the desired pin mappings 
     *        or 0 if RTS pin should be left unconnected.
     * @param dtrMap2 Bit mask of SerialComNullModem.SCM_CON_XXX constants as per the desired pin mappings 
     *        or 0 if DTR pin should be left unconnected.
     * @param setDTRatOpen2 if true DTR will be asserted when serial port is opened otherwise it will not be raised.
     * @return Created virtual null modem pair device's node on success.
     * @throws SerialComException if virtual null modem device pair can not be created,
     *         IllegalArgumentException if idx1/2 is invalid.
     */
    public String[] createCustomNullModemPair(int idx1, int rtsMap1, int dtrMap1, boolean setDTRatOpen1, int idx2, 
            int rtsMap2, int dtrMap2, boolean setDTRatOpen2) throws SerialComException {

        String[] result = null;

        if((idx1 < -1) || (idx1 > 65535)) {
            throw new IllegalArgumentException("idx1 should be -1 <= idx1 =< 65535 !");
        }
        if((idx2 < -1) || (idx2 > 65535)) {
            throw new IllegalArgumentException("idx2 should be -1 <= idx2 =< 65535 !");
        }
        if((idx1 != -1) && (idx2 != -1) && (idx1 == idx2)) {
            throw new IllegalArgumentException("Both device indexs idx1 and idx2 can not be same !");
        }

        synchronized (lockA) {
            result = mComPortJNIBridge.createCustomNullModemPair(idx1, rtsMap1, dtrMap1, setDTRatOpen1, idx2, rtsMap2, dtrMap2, setDTRatOpen2);
        }

        return result; 
    }

    /**
     * <p>Creates a virtual port/device connected in standard loopback fashion. If deviceIndex is -1, 
     * the next available index will be used by driver. If deviceIndex is a valid number, the given index 
     * will be used to create device nodes.</p>
     * 
     * <p>For example; createStandardLoopBackDevice(2) will create /dev/tty2com2 device node in Linux or 
     * will throw exception if that number is already in use. Similarly createStandardLoopBackDevice(-1) 
     * will create /dev/tty2comXX where XX is the next free number managed by the driver internally.</p>
     *
     * <p>Typically loopback mode is used to detect communication link failures.</p>
     * 
     * @param deviceIndex -1 or valid device number (0 <= deviceIndex =< 65535).
     * @return Created virtual loop back device's node on success.
     * @throws SerialComException if virtual loop back device can not be created,
     *         IllegalArgumentException if deviceIndex is invalid.
     */
    public String[] createStandardLoopBackDevice(int deviceIndex) throws SerialComException {

        String[] result = null;

        if((deviceIndex < -1) || (deviceIndex > 65535)) {
            throw new IllegalArgumentException("deviceIndex should be -1 <= deviceIndex =< 65535 !");
        }

        synchronized (lockA) {
            result = mComPortJNIBridge.createStandardLoopBackDevice(deviceIndex);
        }

        return result;
    }

    /**
     * <p>Creates a virtual port/device connected in standard loopback fashion. If deviceIndex is -1, 
     * the next available index will be used by driver. If deviceIndex is a valid number, the given index 
     * will be used to create device nodes.</p>
     * 
     * <p>For example; createCustomLoopBackDevice(2, SerialComNullModem.SP_CON_CTS, SerialComNullModem.SP_CON_DTR) 
     * will create /dev/tty2com2 device node in Linux or will throw exception if that number is already in use. 
     * Similarly createCustomLoopBackDevice(-1, SerialComNullModem.SP_CON_CTS, SerialComNullModem.SP_CON_DTR) 
     * will create /dev/tty2comXX where XX is the next free number managed by the driver internally.</p>
     * 
     * <p>To connect RTS pin to CTS pin use rtsMap = SerialComNullModem.SP_CON_CTS. A pin can be 
     * connected to one or more pins using bit mask. For example to connect RTS pin to CTS and DSR use 
     * rtsMap = SerialComNullModem.SP_CON_CTS | SerialComNullModem.SP_CON_DSR.</p>
     * 
     * @param deviceIndex -1 or valid device number (0 <= deviceIndex =< 65535).
     * @param rtsMap Bit mask of SerialComNullModem.SCM_CON_XXX constants as per the desired pin mappings 
     *        or 0 if RTS pin should be left unconnected.
     * @param dtrMap Bit mask of SerialComNullModem.SCM_CON_XXX constants as per the desired pin mappings 
     *        or 0 if DTR pin should be left unconnected.
     * @param setDTRatOpen if true DTR will be asserted when serial port is opened otherwise it will not be raised.
     * @return Created virtual loop back device's node on success.
     * @throws SerialComException if virtual loop back device can not be created,
     *         IllegalArgumentException if deviceIndex is invalid.
     */
    public String[] createCustomLoopBackDevice(int deviceIndex, int rtsMap, int dtrMap, boolean setDTRatOpen) throws SerialComException {

        String[] result = null;

        if((deviceIndex < -1) || (deviceIndex > 65535)) {
            throw new IllegalArgumentException("deviceIndex should be -1 <= deviceIndex =< 65535 !");
        }

        synchronized (lockA) {
            result = mComPortJNIBridge.createCustomLoopBackDevice(deviceIndex, rtsMap, dtrMap, setDTRatOpen);
        }

        return result;
    }

    /**
     * <p>Removes all virtual serial devices created by tty2com driver.</p>
     * 
     * @return true on success.
     * @throws SerialComException if the operation can not be completed due to some reason.
     */
    public boolean destroyAllCreatedVirtualDevices() throws SerialComException {

        synchronized (lockA) {
            int ret = mComPortJNIBridge.destroyAllCreatedVirtualDevices();
            if(ret < 0) {
                throw new SerialComException("Can not destroy created virtual devices !");
            }
        }

        return true;
    }

    /**
     * <p>Removes all null modem virtual serial devices created by tty2com driver.</p>
     * 
     * @return true on success.
     * @throws SerialComException if the operation can not be completed due to some reason.
     */
    public boolean destroyAllCreatedNullModemPairs() throws SerialComException {

        synchronized (lockA) {
            int ret = mComPortJNIBridge.destroyAllCreatedNullModemPairs();
            if(ret < 0) {
                throw new SerialComException("Can not destroy created null modem pairs/devices !");
            }
        }

        return true;
    }

    /**
     * <p>Removes all loop back virtual serial devices created by tty2com driver.</p>
     * 
     * @return true if device gets deleted.
     * @throws SerialComException if the operation can not be completed due to some reason.
     */
    public boolean destroyAllCreatedLoopbackDevices() throws SerialComException {

        synchronized (lockA) {
            int ret = mComPortJNIBridge.destroyAllCreatedLoopbackDevices();
            if(ret < 0) {
                throw new SerialComException("Can not destroy created loopback devices !");
            }
        }

        return true;
    }

    /**
     * <p>Removes the given virtual serial device created by tty2com driver. If the given device 
     * is one of the device in a null modem pair, the other paired device will be automatically 
     * removed.</p>
     * 
     * @return true if device gets deleted.
     * @throws SerialComException if the operation can not be completed due to some reason, device is null 
     *         or invalid string.
     */
    public boolean destroyGivenVirtualDevice(String device) throws SerialComException {

        if((device == null) || (device.length() == 0)) {
            throw new SerialComException("Invalid virtual device !");
        }
        synchronized (lockA) {
            int ret = mComPortJNIBridge.destroyGivenVirtualDevice(device);
            if(ret < 0) {
                throw new SerialComException("Can not destroy given virtual device !");
            }
        }

        return true;
    }

    /**
     * <p>Returns the device node of last created loop back device.</p>
     * 
     * <p>The sequence of information returned is shown below. The RTS and DTR mappings are returned 
     * in string form. The caller has to convert them into int data type and then constant bit mask 
     * SerialComNullModem.SP_CON_XXX can be used. The x is 0 or multiple of 3.<br>
     * x + 0: port's name/path <br>
     * x + 1: port's RTS mappings <br>
     * x + 2: port's DTR mappings <br></p>
     * 
     * @return Device node on success otherwise null.
     * @throws SerialComException if the operation can not be completed for some reason.
     */
    public String[] getLastLoopBackDeviceNode() throws SerialComException {

        String[] result = null;
        synchronized (lockA) {
            result = mComPortJNIBridge.getLastLoopBackDeviceNode();
        }

        return result;
    }

    /**
     * <p>Returns the device nodes of last created null modem pair.</p>
     * 
     * <p>The sequence of information returned is shown below. The RTS and DTR mappings are returned 
     * in string form. The caller has to convert them into int data type and then constant bit mask 
     * SerialComNullModem.SP_CON_XXX can be used. The x is 0 or multiple of 6.<br>
     * x + 0: 1st port's name/path <br>
     * x + 1: 1st port's RTS mappings <br>
     * x + 2: 1st port's DTR mappings <br>
     * x + 3: 2nd port's name/path <br>
     * x + 4: 2nd port's RTS mappings <br>
     * x + 5: 2nd port's DTR mappings <br></p>
     * 
     * @return Device nodes of null modem pair on success otherwise null.
     * @throws SerialComException if the operation can not be completed for some reason.
     */
    public String[] getLastNullModemPairNodes() throws SerialComException {

        String[] result = null;
        synchronized (lockA) {
            result = mComPortJNIBridge.getLastNullModemPairNodes();
        }

        return result;
    }

    /**
     * <p>Emulates the given line error/event (frame, parity, overrun or break) on given virtual device.</p>
     * 
     * @param devNode virtual serial port which will receive this error event.
     * @param error one of the constants SerialComNullModem.ERR_XXX or SerialComNullModem.RCV_BREAK.
     * @return true if the given error has been emulated on given virtual serial port otherwise false.
     * @throws SerialComException if the operating system specific file is not found, writing to it fails 
     *         or operation can not be completed due to some reason for example if driver is not loaded, 
     *         port has not been opened.
     */
    public boolean emulateSerialEvent(final String devNode, int error) throws SerialComException {

        if((devNode == null) || (devNode.length() == 0)) {
            throw new IllegalArgumentException("The devNode can not be null or empty string !");
        }

        int ret = mComPortJNIBridge.emulateSerialEvent(devNode, error);
        if(ret < 0) {
            throw new SerialComException("Can not emulate specified event on given device !");
        }

        return true;
    }

    /**
     * <p>Emulate line ringing event on given device node.</p>
     * 
     * @param devNode device node which will observe ringing conditions.
     * @param state true if ringing event should be asserted or false for de-assertion.
     * @return true on success.
     * @throws SerialComException if the operating system specific file is not found, writing to it fails 
     *         or operation can not be completed due to some reason for example if driver is not loaded, 
     *         port has not been opened.
     */
    public boolean emulateLineRingingEvent(final String devNode, boolean state) throws SerialComException {

        if((devNode == null) || (devNode.length() == 0)) {
            throw new IllegalArgumentException("The devNode can not be null or empty string !");
        }

        int ret = mComPortJNIBridge.emulateLineRingingEvent(devNode, state);
        if(ret < 0) {
            throw new SerialComException("Can not emulate ringing event on given device !");
        }

        return true;
    }

    /**
     * <p>This registers a listener who will be invoked whenever a virtual serial device has been plugged or 
     * un-plugged in system. This method can be used to write auto discovery applications for example 
     * when a virtual serial device is added to system, application can automatically detect, identify it 
     * and launches an appropriate service.</p>
     *
     * <p>Application must implement Itty2comHotPlugListener interface and override onTTY2COMHotPlugEvent method. 
     * The event value SerialComNullModem.DEV_ADDED indicates virtual serial device has been added to the system. 
     * The event value SerialComNullModem.DEV_REMOVED indicates virtual serial device has been removed from system.</p>
     *
     * <p>This can also be used to detect situations like broken serial cable.</p>
     * 
     * @param hotPlugListener object of class which implements Itty2comHotPlugListener interface.
     * @param deviceNode serial port name or null.
     * @return opaque handle on success that should be passed to unregisterTTY2COMHotPlugEventListener method for 
     *         unregistering this listener.
     * @throws SerialComException if registration fails due to some reason.
     * @throws IllegalArgumentException if hotPlugListener is null.
     */
    public int registerTTY2COMHotPlugEventListener(final Itty2comHotPlugListener hotPlugListener, String deviceNode) 
            throws SerialComException {

        int opaqueHandle = 0;

        if(hotPlugListener == null) {
            throw new IllegalArgumentException("Argument hotPlugListener can not be null !");
        }

        synchronized(lockB) {
            opaqueHandle = mComPortJNIBridge.registerTTY2COMHotPlugEventListener(hotPlugListener, deviceNode);
            if(opaqueHandle < 0) {
                throw new SerialComException("Can't register virtual serial device hotplug listener. Please retry !");
            }
        }

        return opaqueHandle;
    }

    /**
     * <p>This unregisters listener and terminate native thread used for monitoring virtual serial device 
     * insertion or removal.</p>
     * 
     * @param opaqueHandle handle returned by registerTTY2COMHotPlugEventListener method for this listener.
     * @return true on success.
     * @throws SerialComException if un-registration fails due to some reason.
     * @throws IllegalArgumentException if argument opaqueHandle is negative.
     */
    public boolean unregisterTTY2COMHotPlugEventListener(final int opaqueHandle) throws SerialComException {

        if(opaqueHandle < 0) {
            throw new IllegalArgumentException("Argument opaqueHandle can not be negative !");
        }

        synchronized(lockB) {
            int ret = mComPortJNIBridge.unregisterTTY2COMHotPlugEventListener(opaqueHandle);
            if(ret < 0) {
                throw new SerialComException("Can't unregister virtual serial device hotplug listener. Please retry !");
            }
        }

        return true;
    }

    /**
     * <p>Gives operating system specific device stats at the instant this method is called like number of bytes transmitted, 
     * number of bytes received etc.</p>
     * 
     * <p>For Linux the following information is returned in the given order starting at index 0 in returned string array: 
     * number of bytes transmitted, number if bytes received, number of times CTS was raised, number of times DCD was raised, 
     * number of times DSR was raised, number of times BREAK signal was received, number of times RING signal was detected, 
     * number of times framing error has occurred, number of times parity error has occurred, number of times overrun error has 
     * occurred and number of times buffer overrun error has occurred.</p>
     * 
     * @param deviceNode name of the virtual serial port whose stats are to be fetched from driver.
     * @return operating system specific device stats for given serial port.
     * @throws SerialComException if the operation can not be completed for some reason.
     */
    public String[] getStatsForGivenDevice(String deviceNode) throws SerialComException {
        return mComPortJNIBridge.getStatsForGivenDevice(deviceNode);
    }

    /**
     * <p>Set/Unset faulty cable condition. If the state is true, faulty cable condition is emulated by driver i.e. the data 
     * will be sent from sender end but receiver will not receive it. If the state is false, driver will emulate a good cable 
     * condition i.e. all data integrity will be maintained and data will be sent to intended receiver.</p>
     * 
     * @param deviceNode name of the virtual serial port (virtual cable is attached to this port).
     * @param state true for faulty cable otherwise false.
     * @throws SerialComException if the operation can not be completed for some reason.
     */
    public void emulateFaultyCable(String deviceNode, boolean state) throws SerialComException {
        mComPortJNIBridge.emulateFaultyCable(deviceNode, state);
    }
}
