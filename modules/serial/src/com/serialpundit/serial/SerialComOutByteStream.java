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

package com.serialpundit.serial;

import java.io.IOException;
import java.io.OutputStream;

import com.serialpundit.core.SerialComException;
import com.serialpundit.serial.SerialComManager.SMODE;
import com.serialpundit.serial.internal.ISerialIOStream;
import com.serialpundit.serial.internal.SerialComPortHandleInfo;

/**
 * <p>Represents an output stream of bytes that gets sent over to serial port for transmission.</p>
 * 
 * @author Rishi Gupta
 */
public final class SerialComOutByteStream extends OutputStream implements ISerialIOStream {

    private final SerialComManager scm;
    private final SerialComPortHandleInfo portHandleInfo;
    private final long handle;
    private final Object lock;
    private final boolean isBlocking;
    private final long context;
    private boolean isOpened;

    /**
     * <p>Allocates a new SerialComOutByteStream object.</p>
     * 
     * @param scm instance of SerialComManager class with which this stream will associate itself.
     * @param handle handle of the serial port on which to write data bytes.
     * @param streamMode indicates blocking or non-blocking behavior of stream.
     * @throws SerialComException if serial port can not be configured for specified write behavior.
     * @throws com.serialpundit.core.SerialComException 
     */
    public SerialComOutByteStream(SerialComManager scm, SerialComPortHandleInfo portHandleInfo, long handle, 
            SMODE streamMode) throws SerialComException {

        this.scm = scm;
        this.portHandleInfo = portHandleInfo;
        this.handle = handle;
        lock = new Object();

        if(streamMode.getValue() == 1) {
            context = scm.createBlockingIOContext();
            isBlocking = true;
        }else {
            context = 0;
            isBlocking = false;
        }
        isOpened = true;
    }

    /**
     * <p>Writes the specified byte to this output stream (eight low-order bits of the argument data).
     * The 24 high-order bits of data are ignored.</p>
     * 
     * @param data integer to be written to serial port.
     * @throws IOException if write fails or output stream has been closed.
     */
    @Override
    public void write(int data) throws IOException {
        if(isOpened != true) {
            throw new IOException("The byte stream has been closed !");
        }
        try {
            if(isBlocking == true) {
                synchronized(lock) {
                    try {
                        byte[] buffer = new byte[1];
                        buffer[0] = (byte)data;
                        data = scm.writeBytesBlocking(handle, buffer, context);
                    }catch (SerialComException e) {
                        if(SerialComManager.EXP_UNBLOCKIO.equals(e.getExceptionMsg())) {
                            // this exception message occurs when application has closed stream.
                            // release lock so that blocking context can be destroyed.
                            return;
                        }
                        // this is error other than expected, pass it to the application.
                        throw new IOException(e.getExceptionMsg());
                    }
                }
            }else {
                int ret = scm.writeSingleByte(handle, (byte)data);
                if(ret == 0) {
                    throw new IOException("Given data not sent to serial port. Please retry !");
                }
            }
        } catch (SerialComException e) {
            throw new IOException(e.getExceptionMsg());
        }
    }

    /**
     * <p>Writes data.length bytes from the specified byte array to this output stream.</p>
     * 
     * @param data byte type array of data to be written to serial port.
     * @throws IOException if write fails or output stream has been closed.
     * @throws IllegalArgumentException if data is null or an empty array.
     */
    @Override
    public void write(byte[] data) throws IOException {
        if(isOpened != true) {
            throw new IOException("The byte stream has been closed !");
        }
        if((data == null) || (data.length == 0)) {
            throw new IllegalArgumentException("Argument data can not be null or an empty array !");
        }
        try {
            if(isBlocking == true) {
                synchronized(lock) {
                    try {
                        int result = scm.writeBytesBlocking(handle, data, context);
                        if(result == 0) {
                            throw new IOException("Given data not sent to serial port. Please retry !");
                        }
                    }catch (SerialComException e) {
                        if(SerialComManager.EXP_UNBLOCKIO.equals(e.getExceptionMsg())) {
                            return;
                        }
                        throw new IOException(e.getExceptionMsg());
                    }
                }
            }else {
                int ret = scm.writeBytes(handle, data, 0);
                if(ret == 0) {
                    throw new IOException("Given data not sent to serial port. Please retry !");
                }
            }
        } catch (SerialComException e) {
            throw new IOException(e.getExceptionMsg());
        }
    }

    /**
     * <p>Writes len bytes from the specified byte array starting at offset off to this output stream.</p>
     * <p>If b is null, a NullPointerException is thrown.</p>
     * <p>If off is negative, or len is negative, or off+len is greater than the length of the array data, 
     * then an IndexOutOfBoundsException is thrown.<p>
     * 
     * @param data byte type array of data to be written to serial port.
     * @param off offset from where to start sending data.
     * @param len length of data to be written.
     * @throws IOException if write fails or output stream has been closed.
     * @throws IllegalArgumentException if data is not a byte type array.
     * @throws NullPointerException if data is null.
     * @throws IndexOutOfBoundsException If off is negative, or len is negative, or off+len is greater 
     * than the length of the array data.
     */
    @Override
    public void write(byte[] data, final int off, final int len) throws IOException, IndexOutOfBoundsException {
        if(isOpened != true) {
            throw new IOException("The byte stream has been closed !");
        }
        if((data == null) || (data.length == 0)) {
            throw new IllegalArgumentException("Argument data can not be null or an empty array !");
        }
        if((off < 0) || (len < 0) || ((off+len) > data.length)) {
            throw new IndexOutOfBoundsException("Index violation detected in given data array !");
        }

        int i = off;
        byte[] buf = new byte[len];
        for(int x=0; x<len; x++) {
            buf[x] = data[i];
            i++;
        }
        try {
            if(isBlocking == true) {
                synchronized(lock) {
                    try {
                        int result = scm.writeBytesBlocking(handle, buf, context);
                        if(result == 0) {
                            throw new IOException("Given data not sent to serial port. Please retry !");
                        }
                    }catch (SerialComException e) {
                        if(SerialComManager.EXP_UNBLOCKIO.equals(e.getExceptionMsg())) {
                            return;
                        }
                        throw new IOException(e.getExceptionMsg());
                    }
                }
            }else {
                int ret = scm.writeBytes(handle, buf, 0);
                if(ret == 0) {
                    throw new IOException("Given data not sent to serial port. Please retry !");
                }
            }
        } catch (SerialComException e) {
            throw new IOException(e.getExceptionMsg());
        }
    }

    /**
     * <p>SCM always flushes data every time writeBytes() method is called. So do nothing just return.</p>
     * 
     * @throws IOException if write fails or output stream has been closed.
     */
    @Override
    public void flush() throws IOException {
        if(isOpened != true) {
            throw new IOException("The byte stream has been closed !");
        }
    }

    /**
     * <p>This method releases the OutputStream object internally associated with the operating handle.</p>
     * <p>To actually close the port closeComPort() method should be used.</p>
     * 
     * @throws IOException if write fails or output stream has been closed.
     */
    @Override
    public void close() throws IOException {
        if(isOpened != true) {
            throw new IOException("The byte stream has been already closed !");
        }
        if(isBlocking == true) {
            scm.unblockBlockingIOOperation(context);
            // if there was a blocked write operation, it will hold this lock. when it gets unblocked
            // it will release this lock and therefore this close method will acquire this lock.
            // once the lock is acquired it is safe to destroy context.
            synchronized(lock) {
                scm.destroyBlockingIOContext(context);
            }
        }
        isOpened = false;
        portHandleInfo.setSerialComOutByteStream(null);
    }
}
