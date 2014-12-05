/**
 * Author : Rishi Gupta
 * Email  : gupt21@gmail.com
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

/**
 * This class contain information about The association that exist among port name, its corresponding handle,
 * looper, data and event listener.
 */
public final class SerialComPortHandleInfo {

	private String mOpenedPortName = null;
	private long mPortHandle = -1;
	private SerialComLooper mLooper = null;
	private ISerialComEventListener mEventListener = null;
	private ISerialComDataListener mDataListener = null;
	
	public SerialComPortHandleInfo(String portName, long handle, SerialComLooper looper, ISerialComDataListener dataListener, ISerialComEventListener eventListener) {
		this.mOpenedPortName = portName;
		this.mPortHandle     = handle;
		this.mLooper = looper;
		this.mEventListener  = eventListener;
		this.mDataListener  = dataListener;
	}


	/* Port name, info and manipulation. */	
	public String getOpenedPortName() {
		return mOpenedPortName;
	}

	public void setOpenedPortName(String portName) {
		this.mOpenedPortName = portName;
	}

	public boolean containsPort(String portName) throws SerialComException {
		if(portName == null) {
			throw new SerialComException("containsPort()", SerialComErrorMapper.ERR_PORT_NAME_NULL);
		}
		if(mOpenedPortName != null) {
			if(portName.equals(mOpenedPortName)) {
				return true;
			}
		}
		return false;
	}

	/* Port file descriptor, info and manipulation. */	
	public long getPortHandle() {
		return mPortHandle;
	}

	public void setPortHandle(long handle) {
		this.mPortHandle = handle;
	}

	public boolean containsHandle(long handle) {
		if(handle == mPortHandle) {
			return true;
		}
		return false;
	}

	/* Looper associated with this port, info and manipulation. */	
	public SerialComLooper getLooper() {
		return mLooper;
	}

	public void setLooper(SerialComLooper looper) {
		this.mLooper = looper;
	}

	/* Event listener associated with this port, info and manipulation. */	
	public ISerialComEventListener getEventListener() {
		return mEventListener;
	}

	public void setEventListener(ISerialComEventListener eventListener) {
		this.mEventListener  = eventListener;
	}

	public boolean containsEventListener(ISerialComEventListener eventListener) {
		if(eventListener == mEventListener) {
			return true;
		}
		return false;
	}
	
	/* Data Listener associated with this port, info and manipulation. */	
	public ISerialComDataListener getDataListener() {
		return mDataListener;
	}

	public void setDataListener(ISerialComDataListener dataListener) {
		this.mDataListener  = dataListener;
	}
	
	public boolean containsDataListener(ISerialComDataListener dataListener) {
		if(dataListener == mDataListener) {
			return true;
		}
		return false;
	}

}

