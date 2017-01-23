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

import java.util.Map;
import java.util.TreeMap;

import com.serialpundit.core.SerialComException;
import com.serialpundit.serial.ISerialComDataListener;
import com.serialpundit.serial.ISerialComEventListener;

/**
 * <p>Represents Proactor in our IO design pattern.</p>
 * 
 * <p>An application may register data listener only, event listener only or both listeners. 
 * A single dedicated looper handles both the listeners. We first check if a looper exist for 
 * given handle or not. If it does not exist we create and start looper thread which loops over 
 * data or event as specified by application. If it exist, we start data or event looper thread 
 * as specified by application.</p>
 * 
 * <p>An application can have multiple handles for the same port (if there is no exclusive owner 
 * of port).</p>
 * 1. A handle can have only one looper for delivering data and line events.<br/>
 * 2. A looper can have none or only one data looper at any instant of time.<br/>
 * 3. A looper can have none or only one event looper at any instant of time.<br/>
 * 
 * @author Rishi Gupta
 */
public final class SerialComCompletionDispatcher {

    private SerialComPortJNIBridge mComPortJNIBridge = null;
    private TreeMap<Long, SerialComPortHandleInfo> mPortHandleInfo = null;

    /**
     * <p>Allocates a new SerialComCompletionDispatcher object.</p>
     * 
     * @param mComPortJNIBridge interface used to invoke appropriate native function
     * @param portHandleInfo reference to portHandleInfo object to get/set information about handle/port
     */
    public SerialComCompletionDispatcher(SerialComPortJNIBridge mComPortJNIBridge, TreeMap<Long, SerialComPortHandleInfo> portHandleInfo) {
        this.mComPortJNIBridge = mComPortJNIBridge;
        this.mPortHandleInfo = portHandleInfo;
    }

    /**
     * <p>This method creates data looper thread and initialize subsystem for data event passing. </p>
     * 
     * @param handle handle of the opened port for which data looper need to be set up.
     * @param mHandleInfo Reference to SerialComPortHandleInfo object associated with given handle.
     * @param dataListener listener for which looper has to be set up.
     * @return true on success.
     * @throws SerialComException if not able to complete requested operation.
     */
    public boolean setUpDataLooper(long handle, SerialComPortHandleInfo mHandleInfo, ISerialComDataListener dataListener) throws SerialComException {

        int ret = 0;
        SerialComLooper looper = mHandleInfo.getLooper();

        // Create looper for this handle and listener, if it does not exist.
        if(looper == null) {
            looper = new SerialComLooper(mComPortJNIBridge);
            mHandleInfo.setLooper(looper);
        }

        // set up queue and start thread first, then set up native thread
        looper.startDataLooper(handle, dataListener, mHandleInfo.getOpenedPortName());
        mHandleInfo.setDataListener(dataListener);

        try {
            ret = mComPortJNIBridge.setUpDataLooperThread(handle, looper);
            if(ret < 0) {
                looper.stopDataLooper();
                mHandleInfo.setDataListener(null);
                if(mHandleInfo.getEventListener() == null) {
                    mHandleInfo.setLooper(null);
                }
                throw new SerialComException("Could not create native data worker thread. Please retry !");
            }
        }catch (SerialComException e) {
            looper.stopDataLooper();
            mHandleInfo.setDataListener(null);
            if(mHandleInfo.getEventListener() == null) {
                mHandleInfo.setLooper(null);
            }
            throw new SerialComException(e.getExceptionMsg());
        }

        return true;
    }

    /**
     * <p>Check if we have handler corresponding to this listener and take actions accordingly.</p>
     * 
     * @param handle handle of the serial port for which this data listener was registered.
     * @param handleInfo global information object about this handle.
     * @param dataListener listener for which looper has to be removed.
     * @return true on success.
     * @throws SerialComException if not able to complete requested operation.
     */
    public boolean destroyDataLooper(long handle, SerialComPortHandleInfo handleInfo, ISerialComDataListener dataListener) throws SerialComException {

        // We got valid handle so destroy native threads for this listener.
        int ret = mComPortJNIBridge.destroyDataLooperThread(handle);
        if(ret < 0) {
            throw new SerialComException("Could not unregister data listener (termination of native thread failed.). Please retry !");
        }

        // Destroy data looper thread.
        handleInfo.getLooper().stopDataLooper();

        // Remove data listener from information object about this handle.
        handleInfo.setDataListener(null);

        // If neither data nor event listener exist, looper object should be destroyed.
        if((handleInfo.getEventListener() == null) && (handleInfo.getDataListener() == null)) {
            handleInfo.setLooper(null);
        }

        return true;
    }

    /**
     * <p>This method creates event looper thread and initialize subsystem for line event passing. </p>
     * 
     * @param handle handle of the opened port for which event looper need to be set up.
     * @param mHandleInfo Reference to SerialComPortHandleInfo object associated with given handle.
     * @param eventListener listener for which looper has to be set up.
     * @return true on success.
     * @throws SerialComException if an error occurs. 
     */
    public boolean setUpEventLooper(long handle, SerialComPortHandleInfo mHandleInfo, ISerialComEventListener eventListener) throws SerialComException {

        int ret = 0;
        SerialComLooper looper = mHandleInfo.getLooper();

        // Create looper for this handle and listener, if it does not exist.
        if(looper == null) {
            looper = new SerialComLooper(mComPortJNIBridge);
            mHandleInfo.setLooper(looper);
        }

        looper.startEventLooper(handle, eventListener, mHandleInfo.getOpenedPortName());
        mHandleInfo.setEventListener(eventListener);

        try {
            ret = mComPortJNIBridge.setUpEventLooperThread(handle, looper);
            if(ret < 0) {
                looper.stopEventLooper();
                mHandleInfo.setEventListener(null);
                if(mHandleInfo.getDataListener() == null) {
                    mHandleInfo.setLooper(null);
                }
                throw new SerialComException("Could not create native event worker thread. Please retry !");
            }
        }catch (SerialComException e) {
            looper.stopEventLooper();
            mHandleInfo.setEventListener(null);
            if(mHandleInfo.getDataListener() == null) {
                mHandleInfo.setLooper(null);
            }
            throw new SerialComException(e.getExceptionMsg());
        }

        return true;
    }

    /**
     * <p>Check if we have handler corresponding to this listener and take actions accordingly.</p>
     * 
     * @param handle handle of the serial port for which this event listener was registered.
     * @param handleInfo global information object about this handle.
     * @param eventListener listener for which looper has to be removed.
     * @return true on success.
     * @throws SerialComException if not able to complete requested operation.
     */
    public boolean destroyEventLooper(long handle, SerialComPortHandleInfo handleInfo, ISerialComEventListener eventListener) throws SerialComException {

        // We got valid handle so destroy native threads for this listener.
        int ret = mComPortJNIBridge.destroyEventLooperThread(handle);
        if(ret < 0) {
            throw new SerialComException("Could not unregister event listener (termination of native thread failed.). Please retry !");
        }

        // Destroy event looper thread.
        handleInfo.getLooper().stopEventLooper();

        // Remove event listener from information object about this handle.
        handleInfo.setEventListener(null);

        // If neither data nor event listener exist, looper object should be destroyed.
        if((handleInfo.getEventListener() == null) && (handleInfo.getDataListener() == null)) {
            handleInfo.setLooper(null);
        }

        return true;
    }

    /**
     * <p>Check if we have handler corresponding to this listener and take actions accordingly.</p>
     * 
     * @param listener listener for which event has to be paused.
     * @return true on success.
     * @throws SerialComException if not able to complete requested operation.
     */
    public boolean pauseListeningEvents(ISerialComEventListener listener) throws SerialComException {

        long handle = -1;
        SerialComLooper looper = null;
        SerialComPortHandleInfo handleInfo = null;

        for (Map.Entry<Long, SerialComPortHandleInfo> entry : mPortHandleInfo.entrySet()) {
            handleInfo = entry.getValue();
            if(handleInfo != null) {
                if(handleInfo.getEventListener() ==  listener) {
                    handle = handleInfo.getPortHandle();
                    looper = handleInfo.getLooper();
                    break;
                }
            }
        }

        if(handle != -1) {
            // We got a valid handle, so pause native threads for this listener first.
            int ret = mComPortJNIBridge.pauseListeningEvents(handle);
            if(ret < 0) {
                throw new SerialComException("Could not pause event thread !");
            }

            looper.pause(); // now pause corresponding looper thread.
        }else {
            throw new SerialComException("This event listener is not registered with SerialPundit !");
        }

        return true;
    }

    /**
     * <p>Check if we have handler corresponding to this listener and take actions accordingly.</p>
     * 
     * @param listener for which events sending should be resumed.
     * @return true on success.
     * @throws SerialComException if not able to complete requested operation.
     */
    public boolean resumeListeningEvents(ISerialComEventListener listener) throws SerialComException {

        long handle = -1;
        SerialComLooper looper = null;
        SerialComPortHandleInfo handleInfo = null;

        for (Map.Entry<Long, SerialComPortHandleInfo> entry : mPortHandleInfo.entrySet()) {
            handleInfo = entry.getValue();
            if(handleInfo != null) {
                if(handleInfo.getEventListener() ==  listener) {
                    handle = handleInfo.getPortHandle();
                    looper = handleInfo.getLooper();
                    break;
                }
            }
        }

        if(handle != -1) {
            // We got valid handle, so resume Java looper first.
            looper.resume();

            // now resume native subsystem.
            int ret = mComPortJNIBridge.resumeListeningEvents(handle);
            if(ret < 0) {
                throw new SerialComException("Could not resume event thread !");
            }
        }else {
            throw new SerialComException("This event listener is not registered with SerialPundit !");
        }

        return true;
    }
}
