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

package com.serialpundit.serial.internal;

import com.serialpundit.serial.ISerialComDataListener;
import com.serialpundit.serial.ISerialComEventListener;
import com.serialpundit.serial.SerialComInByteStream;
import com.serialpundit.serial.SerialComOutByteStream;

/**
 * <p>Encapsulates the information like port handle, looper object, event listener, 
 * data listener, port name etc associated with a particular port.</p>
 * 
 * @author Rishi Gupta
 */
public final class SerialComPortHandleInfo {

    private long mPortHandle = -1;
    private String mOpenedPortName = null;
    private SerialComLooper mLooper = null;
    private ISerialComEventListener mEventListener = null;
    private ISerialComDataListener mDataListener = null;
    private SerialComInByteStream mSerialComInByteStream = null;
    private SerialComOutByteStream mSerialComOutByteStream = null;

    /**
     * <p>Allocates a new SerialComPortHandleInfo object.</p>
     * 
     * @param portName name of port for which info is to stored.
     * @param handle handle of opened port.
     * @param looper looper object serving this handle.
     * @param dataListener listener to whom data bytes/errors will be delivered for this handle.
     * @param eventListener listener to whom events will be delivered for this handle.
     */
    public SerialComPortHandleInfo(String portName, long handle, SerialComLooper looper, 
            ISerialComDataListener dataListener, ISerialComEventListener eventListener) {
        this.mOpenedPortName = portName;
        this.mPortHandle     = handle;
        this.mLooper = looper;
        this.mEventListener  = eventListener;
        this.mDataListener  = dataListener;
    }


    /** 
     * <p>Get the name of port associated with given handle Callers first find reference to this class 
     * object using given handle and then invoke this method.</p>
     * 
     * @return name of port.
     */	
    public String getOpenedPortName() {
        return mOpenedPortName;
    }

    /** 
     * <p>Set the name of port.</p>
     * 
     * @param portName name of port opened for communication.
     */
    public void setOpenedPortName(String portName) {
        this.mOpenedPortName = portName;
    }

    /** 
     * <p> Check if the corresponding port name exist. </p>
     * 
     * @param portName name of port to find.
     * @throws IllegalArgumentException if portName is null.
     */
    public boolean containsPort(String portName) throws IllegalArgumentException {
        if(portName == null) {
            throw new IllegalArgumentException("containsPort(), " + "Name of the port can not be null");
        }
        if(mOpenedPortName != null) {
            if(portName.equals(mOpenedPortName)) {
                return true;
            }
        }
        return false;
    }

    /** 
     * <p>Returns handle to the opened port. </p>
     * 
     * @return handle of opened port.
     */	
    public long getPortHandle() {
        return mPortHandle;
    }

    /** 
     * <p>Sets the handle of the port opened.</p>
     * @param handle handle of port after opening it successfully.
     */
    public void setPortHandle(long handle) {
        this.mPortHandle = handle;
    }

    /** 
     * <p>Check if the object of this class have this handle. </p>
     * 
     * @return true if object of this class contains given handle false otherwise.
     */
    public boolean containsHandle(long handle) {
        if(handle == mPortHandle) {
            return true;
        }
        return false;
    }

    /** 
     * <p>Looper associated with this port, info and manipulation. </p>
     * 
     * @return looper object for this handle/port.
     */	
    public SerialComLooper getLooper() {
        return mLooper;
    }

    /** <p>Set the looper object that is associated with this handle.</p>
     * 
     * @param looper looper object that will server this port/handle.
     */
    public void setLooper(SerialComLooper looper) {
        this.mLooper = looper;
    }

    /** 
     * <p>Event listener associated with this port, info and manipulation. </p>
     * @return event listener who will get events for this port/handle
     */	
    public ISerialComEventListener getEventListener() {
        return mEventListener;
    }

    /** <p> Set the event listener associated with this handle. </p>
     * @param eventListener event listener who will get events for this port/handle
     */
    public void setEventListener(ISerialComEventListener eventListener) {
        this.mEventListener  = eventListener;
    }

    /** 
     * <p> Check if there is already a registered event listener for this handle. </p> 
     * @param eventListener event listener who will get events for this port/handle
     */
    public boolean containsEventListener(ISerialComEventListener eventListener) {
        if(eventListener == mEventListener) {
            return true;
        }
        return false;
    }

    /** 
     * <p>Data Listener associated with this port, info and manipulation.</p>
     * @return data listener who will get data bytes/errors for this port/handle
     */	
    public ISerialComDataListener getDataListener() {
        return mDataListener;
    }

    /** 
     * <p> Set the data listener for this handle. </p> 
     * @param dataListener listener who will get data bytes/errors for this port/handle
     */
    public void setDataListener(ISerialComDataListener dataListener) {
        this.mDataListener  = dataListener;
    }

    /** 
     * <p> Check if there already exist a data listener for this handle. </p>
     * @param dataListener who will get data bytes/errors for this port/handle
     */
    public boolean containsDataListener(ISerialComDataListener dataListener) {
        if(dataListener == mDataListener) {
            return true;
        }
        return false;
    }

    /** 
     * <p>Return SerialComByteStream object associated with this handle. </p>
     * @return input byte stream object for this port/handle
     */	
    public SerialComInByteStream getSerialComInByteStream() {
        return mSerialComInByteStream;
    }

    /** 
     * <p> Set the SerialComByteStream object associated with this handle. </p>
     * @param serialComInByteStream input byte stream object for this port/handle
     */
    public void setSerialComInByteStream(SerialComInByteStream serialComInByteStream) {
        this.mSerialComInByteStream  = serialComInByteStream;
    }

    /** 
     * <p>Return SerialComByteStream object associated with this handle. </p>
     * @return output byte stream for this port/handle
     */	
    public SerialComOutByteStream getSerialComOutByteStream() {
        return mSerialComOutByteStream;
    }

    /** <p> Set the SerialComByteStream object associated with this handle. </p>
     * @param serialComOutByteStream output byte stream for this port/handle
     */
    public void setSerialComOutByteStream(SerialComOutByteStream serialComOutByteStream) {
        this.mSerialComOutByteStream  = serialComOutByteStream;
    }
}
