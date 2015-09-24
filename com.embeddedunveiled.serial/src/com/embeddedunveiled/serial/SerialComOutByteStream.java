/*
 * Author : Rishi Gupta
 * 
 * This file is part of 'serial communication manager' library.
 *
 * The 'serial communication manager' is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by the Free Software 
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * The 'serial communication manager' is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with serial communication manager. If not, see <http://www.gnu.org/licenses/>.
 */

package com.embeddedunveiled.serial;

import java.io.IOException;
import java.io.OutputStream;
import com.embeddedunveiled.serial.SerialComManager.SMODE;
import com.embeddedunveiled.serial.internal.SerialComPortHandleInfo;

/**
 * <p>Represents an output stream of bytes that gets sent over to serial port for transmission.</p>
 * 
 * @author Rishi Gupta
 */
public final class SerialComOutByteStream extends OutputStream {

	private SerialComManager scm;
	private SerialComPortHandleInfo portHandleInfo;
	private long handle;
	private boolean isOpened;
	/* private boolean isBlocking = false; */

	/**
	 * <p>Allocates a new SerialComOutByteStream object.</p>
	 * 
	 * @param scm instance of SerialComManager class with which this stream will associate itself.
	 * @param handle handle of the serial port on which to write data bytes.
	 * @param streamMode indicates blocking or non-blocking behavior of stream.
	 * @throws SerialComException if serial port can not be configured for specified write behavior.
	 */
	public SerialComOutByteStream(SerialComManager scm, SerialComPortHandleInfo portHandleInfo, long handle, 
			SMODE streamMode) throws SerialComException {
		this.scm = scm;
		this.portHandleInfo = portHandleInfo;
		this.handle = handle;
		isOpened = true;
		/* if(streamMode.getValue() == 1) {
			isBlocking = true;
		} */
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
			scm.writeSingleByte(handle, (byte)data);
		} catch (SerialComException e) {
			throw new IOException(e.getExceptionMsg());
		}
	}

	/**
	 * <p>Writes data.length bytes from the specified byte array to this output stream.</p>
	 * 
	 * @param data byte type array of data to be written to serial port.
	 * @throws IOException if write fails or output stream has been closed.
	 * @throws NullPointerException if data is null.
	 * @throws IllegalArgumentException if data is not a byte type array.
	 */
	@Override
	public void write(byte[] data) throws IOException {
		if(isOpened != true) {
			throw new IOException("The byte stream has been closed !");
		}
		if(data == null) {
			throw new NullPointerException("Argument data can not be null !");
		}
		if(!(data instanceof byte[])) {
			throw new IllegalArgumentException("Argument data is not byte type array !");
		}
		try {
			scm.writeBytes(handle, data, 0);
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
	public void write(byte[] data, int off, int len) throws IOException, IndexOutOfBoundsException {
		if(isOpened != true) {
			throw new IOException("The byte stream has been closed !");
		}
		if(data == null) {
			throw new NullPointerException("Argument data can not be null !");
		}
		if((off < 0) || (len < 0) || ((off+len) > data.length)) {
			throw new IndexOutOfBoundsException("Index violation detected !");
		}
		if(!(data instanceof byte[])) {
			throw new IllegalArgumentException("Argument data is not byte type array !");
		}

		try {
			int x = 0;
			int i = off;
			byte[] buf = new byte[len];
			for(x=0; x<len; x++) {
				buf[x] = data[i];
				i++;
			}
			scm.writeBytes(handle, buf, 0);
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
			throw new IOException("The byte stream has been closed !");
		}
		portHandleInfo.setSerialComOutByteStream(null);
		isOpened = false;
	}
}
