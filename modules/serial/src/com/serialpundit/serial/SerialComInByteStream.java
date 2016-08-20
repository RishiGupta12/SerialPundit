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
import java.io.InputStream;

import com.serialpundit.core.SerialComException;
import com.serialpundit.serial.SerialComManager.SMODE;
import com.serialpundit.serial.internal.ISerialIOStream;
import com.serialpundit.serial.internal.SerialComPortHandleInfo;

/**
 * <p>Represents an input stream of bytes which is received from serial port.</p>
 * 
 * <p>Application design should make sure that the port is not closed if there exist a read method
 * which is blocked (waiting for data byte) on the same port.</p>
 * 
 * <p>Advance applications may fine tune the timing behavior using fineTuneReadBehaviour() API defined 
 * in SerialComManager class.</p>
 * 
 * @author Rishi Gupta
 */
public final class SerialComInByteStream extends InputStream implements ISerialIOStream {

    private final SerialComManager scm;
    private final SerialComPortHandleInfo portHandleInfo;
    private final long handle;
    private final Object lock;
    private final boolean isBlocking;
    private final long context;
    private boolean isOpened;

    /**
     * <p>Construct and allocates a new SerialComInByteStream object with given details.</p>
     * 
     * @param scm instance of SerialComManager class with which this stream will associate itself.
     * @param handle handle of the serial port on which to read data bytes.
     * @param streamMode indicates blocking or non-blocking behavior of stream.
     * @throws SerialComException if the input stream can not be prepared for the specified read behavior.
     */
    public SerialComInByteStream(SerialComManager scm, SerialComPortHandleInfo portHandleInfo, 
            long handle, SMODE streamMode) throws SerialComException {

        this.scm = scm;
        this.portHandleInfo = portHandleInfo;
        this.handle = handle;
        lock = new Object();

        /* For blocking read, create a operating system specific event object that will be used to 
         * wait for data to be available for reading. If a thread is blocked (waiting for data and 
         * another thread closes stream, the serial port will not be closed. To prevent this we explicitly
         * cause an event to happen to bring out the blocked read call so that serial port can be closed. */
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
     * <p>Returns an estimate of the minimum number of bytes that can be read from this input stream
     * without blocking by the next invocation of a method for this input stream.</p>
     * 
     * @return an estimate of the minimum number of bytes available for reading.
     * @throws IOException if an I/O error occurs or if stream has been closed already.
     */
    @Override
    public int available() throws IOException {
        if(isOpened != true) {
            throw new IOException("The byte stream has been closed !");
        }

        int[] numBytesAvailable = new int[2];
        try {
            numBytesAvailable = scm.getByteCountInPortIOBuffer(handle);
        } catch (SerialComException e) {
            throw new IOException(e.getExceptionMsg());
        }
        return numBytesAvailable[0];
    }

    /**
     * <p>This method releases the InputStream object associated with the operating handle.</p>
     * <p>To actually close the port closeComPort() method should be used.</p>
     * 
     * @throws IOException if an I/O error occurs or if stream has been closed already.
     */
    @Override
    public void close() throws IOException {
        if(isOpened != true) {
            throw new IOException("The byte stream has been already closed !");
        }
        if(isBlocking == true) {
            scm.unblockBlockingIOOperation(context);
            // if there was a blocked read operation, it will hold this lock. when it gets unblocked
            // it will release this lock and therefore this close method will acquire this lock.
            // once the lock is acquired it is safe to destroy context.
            synchronized(lock) {
                scm.destroyBlockingIOContext(context);
            }
        }
        isOpened = false;
        portHandleInfo.setSerialComInByteStream(null);
    }

    /**
     * <p>SCM does not support mark and reset of input stream. If required, it can be developed at 
     * application level.</p>
     * 
     */
    @Override
    public void mark(int a) {
    }

    /**
     * <p>SCM does not support mark and reset of input stream. If required, it can be developed at 
     * application level.</p>
     * 
     * @return always returns false.
     */
    @Override
    public boolean markSupported() {
        return false;
    }

    /**
     * <p>Reads the next byte of data from the input stream. The value byte is returned as an int in 
     * the range 0 to 255.</p>
     * 
     * <p>For blocking mode this method returns the next byte of data. For non-blocking mode this 
     * method returns the next byte of data if it is available otherwise -1 if there is no data 
     * at serial port.</p>
     * 
     * @return the next byte of data or -1.
     * @throws IOException if an I/O error occurs or if stream has been closed already.
     */
    @Override
    public int read() throws IOException {
        if(isOpened != true) {
            throw new IOException("The byte stream has been closed !");
        }

        byte[] data;
        try {
            if(isBlocking == true) {
                // blocking I/O
                synchronized(lock) {
                    try {
                        data = scm.readBytesBlocking(handle, 1, context);
                    }catch (SerialComException e) {
                        if(SerialComManager.EXP_UNBLOCKIO.equals(e.getExceptionMsg())) {
                            // this exception message occurs when application has closed stream.
                            // release lock so that blocking context can be destroyed.
                            return -1;
                        }
                        // this is error other than expected, pass it to application.
                        throw new IOException(e.getExceptionMsg());
                    }
                    if(data != null) {
                        return (int)data[0];
                    }else {
                        throw new IOException("Unknown error occured while reading data in blocking mode !");
                    }
                }
            }else {
                // non-blocking I/O
                data = scm.readBytes(handle, 1);
                if(data != null) {
                    return (int)data[0];
                }else {
                    return -1;
                }
            }
        }catch (SerialComException e) {
            throw new IOException(e.getExceptionMsg());
        }
    }

    /**
     * <p>Reads some number of bytes from the input stream and stores them into the buffer array b. 
     * The number of bytes actually read is returned as an integer.  This method blocks until input 
     * data is available or an exception is thrown.</p>
     *
     * <p>If the length of b is zero, then no bytes are read and 0 is returned; otherwise, there is 
     * an attempt to read at least one byte.</p>
     *
     * <p>The first byte read is stored into element b[0], the next one into b[1], and so on. The 
     * number of bytes read is, at most, equal to the length of b. Let k be the number of bytes actually 
     * read; these bytes will be stored in elements b[0] through <code>b[</code><i>k</i><code>-1]</code>, 
     * leaving elements <code>b[</code><i>k</i><code>]</code> through <code>b[b.length-1]</code> unaffected.</p>
     *
     * <p>The read(b) method for class SerialComInByteStream has the same effect as : read(b, 0, b.length).</p>
     *
     * @param  b the buffer into which the data is read.
     * @return the total number of bytes read into the buffer.
     * @throws IOException if an I/O error occurs or if input stream has been closed.
     * @throws NullPointerException if <code>b</code> is <code>null</code>.
     */
    @Override
    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    /**
     * <p>Reads up to len bytes of data from the input stream into an array of bytes. An attempt is made to read
     * as many as len bytes, but a smaller number may be read. The number of bytes actually read is returned as 
     * an integer.</p>
     * 
     * <p>If len is zero, then no bytes are read and 0 is returned; otherwise, there is an attempt to read at 
     * least one byte and stored into b.</p>
     * 
     * <p>The first byte read is stored into element b[off], the next one into b[off+1], and so on. The number 
     * of bytes read is, at most, equal to len. Let k be the number of bytes actually read; these bytes will be 
     * stored in elements b[off] through b[off+k-1], leaving elements b[off+k] through b[off+len-1] unaffected.</p>
     *  
     * <p>In every case, elements b[0] through b[off] and elements b[off+len] through b[b.length-1] are unaffected.</p>
     * 
     * <p>For blocking mode, this method blocks until input data is available or an exception is thrown. For 
     * non-blocking mode it attempts to read data, returns data byte if read. It will return -1 if there is 
     * no data at serial port.</p>
     * 
     * @param b the buffer into which the data is read.
     * @param off the start offset in array b at which the data is written.
     * @param len the maximum number of bytes to read.
     * @return the total number of bytes read into the buffer or 0 if len is zero or -1 if there is no 
     *          data (non-blocking).
     * @throws IOException if an I/O error occurs or if input stream has been closed.
     * @throws NullPointerException if <code>b</code> is <code>null</code>.
     * @throws IllegalArgumentException if data is not a byte type array.
     * @throws IndexOutOfBoundsException if off is negative, len is negative, or len is greater 
     *          than b.length - off.
     */
    @Override
    public int read(byte b[], final int off, final int len) throws IOException {
        if(isOpened != true) {
            throw new IOException("The byte stream has been closed !");
        }
        if(b == null) {
            throw new NullPointerException("Null data buffer passed to read operation !");
        }
        if((off < 0) || (len < 0) || (len > (b.length - off))) {
            throw new IndexOutOfBoundsException("Index violation detected in given byte array !");
        }
        if(len == 0) {
            return 0;
        }

        int i = off;
        byte[] data;
        try {
            if(isBlocking == true) {
                // blocking I/O
                synchronized(lock) {
                    try {
                        data = scm.readBytesBlocking(handle, len, context);
                    }catch (SerialComException e) {
                        if(SerialComManager.EXP_UNBLOCKIO.equals(e.getExceptionMsg())) {
                            // this exception message occurs when application has closed stream.
                            // release lock so that blocking context can be destroyed.
                            return -1;
                        }
                        // this is error other than expected, pass it to application.
                        throw new IOException(e.getExceptionMsg());
                    }
                    if(data != null) {
                        for(int x=0; x < data.length; x++) {
                            b[i] = data[x];
                            i++;
                        }
                        return data.length;
                    }else {
                        throw new IOException("Unknown error occured in native layer !");
                    }
                }
            }else {
                // non-blocking I/O
                data = scm.readBytes(handle, len);
                if(data != null) {
                    for(int x=0; x < data.length; x++) {
                        b[i] = data[x];
                        i++;
                    }
                    return data.length;
                }else {
                    return -1;
                }
            }
        }catch (SerialComException e) {
            throw new IOException(e.getExceptionMsg());
        }
    }

    /**
     * <p>SCM does not support reset. If required, it can be developed at application level.</p>
     */
    @Override
    public synchronized void reset() throws IOException {
    }

    /**
     * <p>SCM does not support skip. If required, it can be developed at application level.</p>
     * 
     * @param number of bytes to skip.
     * @return always returns 0.
     */
    @Override
    public long skip(long number) {
        return 0;
    }
}

