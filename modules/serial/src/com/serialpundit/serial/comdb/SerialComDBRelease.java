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

package com.serialpundit.serial.comdb;

import com.serialpundit.core.SerialComException;
import com.serialpundit.serial.internal.SerialComDBReleaseJNIBridge;

/**
 * <p>Provides methods to release COM ports assigned by Windows operating system to dynamically when 
 * a serial device is inserted into system (installation).</p>
 * 
 * <p>In Windows, the system-supplied COM port database arbitrates the use of COM port numbers 
 * by COM ports that are installed on the system. Microsoft Windows provides this component to 
 * facilitate installing COM ports and, in particular, to ensure that each port number is assigned, 
 * at most, to one port. The component consists of the database and a library containing functions 
 * that the installation software calls to access the database. All system-supplied installers for 
 * COM ports use the COM port database to obtain a COM port number. Although not a Plug and Play requirement, 
 * all vendor-supplied installers should also use the COM port database to obtain a COM port number.</p>
 * 
 * @author Rishi Gupta
 */
public final class SerialComDBRelease {

    private final Object lock = new Object();
    private final SerialComDBReleaseJNIBridge mSerialComDBReleaseJNIBridge;

    /**
     * <p>Construct and allocates a new SerialComDBRelease object with given details.</p>
     * 
     * @param mSerialComDBReleaseJNIBridge interface class to native library for calling platform 
     *         specific routines.
     * @throws SerialComException if the object can not be constructed.
     */
    public SerialComDBRelease(SerialComDBReleaseJNIBridge mSerialComDBReleaseJNIBridge) {
        this.mSerialComDBReleaseJNIBridge = mSerialComDBReleaseJNIBridge;
    }

    /**
     * <p>Starts the service on demand. This method gives more control to application to manage the 
     * service as and when needed.</p>
     * 
     * @return true if the service was started successfully and communication was established.
     * @throws SerialComException if an error occurs.
     */
    public boolean startSerialComDBReleaseSerive() throws SerialComException {

        synchronized(lock) {
            int ret = mSerialComDBReleaseJNIBridge.startSerialComDBReleaseSerive();
            if(ret < 0) {
                throw new SerialComException("Could not start the comdb release service. Please retry !");
            }
        }

        return true;
    }

    /**
     * <p>Stops the service on demand. This method gives more control to application to manage the 
     * service as and when needed.</p>
     * 
     * @return true if the service was stopped successfully.
     * @throws SerialComException if an error occurs.
     */
    public boolean stopSerialComDBReleaseSerive() throws SerialComException {

        synchronized(lock) {
            int ret = mSerialComDBReleaseJNIBridge.stopSerialComDBReleaseSerive();
            if(ret < 0) {
                throw new SerialComException("Could not stop the comdb release service. Please retry !");
            }
        }

        return true;
    }

    /**
     * <p>Free the COM port number in database of Windows of operating system so that other 
     * serial devices can use that port number.</p>
     * 
     * @param comPortName COM port to be released for example COM32 etc.
     * @return true on success.
     * @throws SerialComException if given COM port can not be released or an error occurs.
     */
    public boolean releaseComPort(String comPortName) throws SerialComException {

        if(comPortName == null) {
            throw new IllegalArgumentException("Argument comPortName can not be null !");
        }
        String comPortNameVal = comPortName.trim();
        if(comPortNameVal.length() == 0) {
            throw new IllegalArgumentException("Argument comPortName can not be an empty string !");
        }

        synchronized(lock) {
            int ret = mSerialComDBReleaseJNIBridge.releaseComPort(comPortName);
            if(ret < 0) {
                throw new SerialComException("Could not release the given serial port. Please retry !");
            }
        }

        return true;
    }

    /**
     * <p>Free all the COM port number in database of Windows of operating system so that other 
     * serial devices can use that port number. Typically, if there are serial devices that will 
     * be always present in system for example Bluetooth modem on serial interface on computer's 
     * motherboard, than you may not want to release COM port number assigned to this device. Add 
     * this COM port in excludeList and this library will not remove it from database.</p>
     * 
     * @param excludeList list of COM ports that should not be released.
     * @return true on success.
     * @throws SerialComException if COM ports can not be released or an error occurs.
     */
    public boolean releaseAllComPorts(String[] excludeList) throws SerialComException {

        synchronized(lock) {
            int ret = mSerialComDBReleaseJNIBridge.releaseAllComPorts(excludeList);
            if(ret < 0) {
                throw new SerialComException("Could not release the serial ports. Please retry !");
            }
        }

        return true;
    }

    /**
     * <p>Provides an array containing all COM ports which are marked as "(in use)" by Windows operating 
     * system.</p>
     * 
     * @return array of strings containing COM ports marked as in use.
     * @throws SerialComException if an error occurs.
     */
    public String[] getComPortNumbersInUse() throws SerialComException {

        String[] portsInUse = null;

        synchronized(lock) {
            portsInUse = mSerialComDBReleaseJNIBridge.getComPortNumbersInUse();
            if(portsInUse == null) {
                throw new SerialComException("Could not calculate ports in use. Please retry !");
            }
        }

        return portsInUse;
    }

    /**
     * <p>Provides the current size of COM port database.</p>
     * 
     * @return current size of COM port database.
     * @throws SerialComException if an error occurs.
     */
    public int getCurrentComDBDatabaseSize() throws SerialComException {

        int currentSize = 0;

        synchronized(lock) {
            currentSize = mSerialComDBReleaseJNIBridge.getCurrentComDBDatabaseSize();
            if(currentSize < 0) {
                throw new SerialComException("Could not get current size of COMDB database. Please retry !");
            }
        }

        return currentSize;
    }

    /**
     * <p>It resizes database to arbitrate give number of serial ports.</p>
     * 
     * @param newSize number of port numbers to be arbitrated in the database. 
     * @return true on success.
     * @throws SerialComException if COM ports can not be released or an error occurs.
     */
    public boolean resizeComDBDatabase(int newSize) throws SerialComException {

        synchronized(lock) {
            int ret = mSerialComDBReleaseJNIBridge.resizeComDBDatabase(newSize);
            if(ret < 0) {
                throw new SerialComException("Could not resize the database. Please retry !");
            }
        }

        return true;
    }
}
