/*
 * Author : Rishi Gupta
 * Email  : gupt21@gmail.com
 * 
 * This file is part of 'serial communication manager' library.
 *
 * 'serial communication manager' is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * 'serial communication manager' is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with serial communication manager. If not, see <http://www.gnu.org/licenses/>.
 */

package com.embeddedunveiled.serial;

import java.util.List;

/**
 * This class represents Proactor in our IO design pattern.
 * 
 * An application may register data listener only, event listener only or both listeners. A single dedicated looper handles both the listeners.
 * We first check if a looper exist for given handle or not. If it does not exist we create and start looper thread which loops over data or event 
 * as specified by application. If it exist, we start data or event looper thread as specified by application.
 * 
 * An application can have multiple handles for the same port (if there is no exclusive owner).
 * A handle can have only one looper for delivering data and line events.
 * A looper can have none or only one data looper at any instant of time.
 * A looper can have none or only one event looper at any instant of time.
 */
public final class SerialComCompletionDispatcher {
	
	private SerialComJNINativeInterface mNativeInterface = null;
	private SerialComErrorMapper mErrMapper = null;
	private List<SerialComPortHandleInfo> mPortHandleInfo = null;
	
	public SerialComCompletionDispatcher(SerialComJNINativeInterface nativeInterface, SerialComErrorMapper errMapper, List<SerialComPortHandleInfo> portHandleInfo) {
		this.mNativeInterface = nativeInterface;
		this.mErrMapper = errMapper;
		this.mPortHandleInfo = portHandleInfo;
	}
	
	/**
	 * 
	 * @param listener
	 * @param mHandleInfo Reference to SerialComPortHandleInfo object associated with given handle
	 * @return
	 * @throws SerialComException
	 */
	
	public boolean setUpDataLooper(long handle, SerialComPortHandleInfo mHandleInfo, ISerialComDataListener dataListener) throws SerialComException {
		
		SerialComLooper looper = mHandleInfo.getLooper();
		
		// Create looper for this handle and listener, if it does not exist.
		if (looper == null) {
			looper = new SerialComLooper(mNativeInterface, mErrMapper);
			mHandleInfo.setLooper(looper);
		}
		
		looper.startDataLooper(handle, dataListener, mHandleInfo.getOpenedPortName());
		mHandleInfo.setDataListener(dataListener);
		
		int ret = mNativeInterface.setUpDataLooperThread(handle, looper);
		if (ret < 0) {
			throw new SerialComException("setUpDataLooper()", mErrMapper.getMappedError(ret));
		}
		
		return true;
	}
	
	/**
	 * Check if we have handler corresponding to this listener and take actions accordingly.
	 * 
	 * @param dataListener
	 * @return
	 * @throws SerialComException
	 */
	public boolean destroyDataLooper(ISerialComDataListener dataListener) throws SerialComException {
		long handle = -1;
		SerialComPortHandleInfo mHandleInfo = null;
		
		for(SerialComPortHandleInfo mInfo: mPortHandleInfo){
			if(mInfo.getDataListener() ==  dataListener) {
				handle = mInfo.getPortHandle();
				mHandleInfo = mInfo;
				break;
			}
		}
		if(handle == -1) {
			throw new SerialComException("destroyDataLooper()", SerialComErrorMapper.ERR_WRONG_LISTENER_PASSED);
		}
		
		// We got valid handle so destroy native threads for this listener.
		int ret = mNativeInterface.destroyDataLooperThread(handle);
		if(ret < 0) {
			throw new SerialComException("destroyDataLooper()", mErrMapper.getMappedError(ret));
		}
		
		// Remove data listener from information object about this handle.
		mHandleInfo.setDataListener(null);
		
		return true;
	}
	
	/**
	 * 
	 * @param listener
	 * @param mHandleInfo Reference to SerialComPortHandleInfo object associated with given handle
	 * @return
	 */
	
	public boolean setUpEventLooper(long handle, SerialComPortHandleInfo mHandleInfo, ISerialComEventListener eventListener) throws SerialComException {
		
		SerialComLooper looper = mHandleInfo.getLooper();
		
		// Create looper for this handle and listener, if it does not exist.
		if(looper == null) {
			looper = new SerialComLooper(mNativeInterface, mErrMapper);
			mHandleInfo.setLooper(looper);
		}
		
		looper.startEventLooper(handle, eventListener, mHandleInfo.getOpenedPortName());
		mHandleInfo.setEventListener(eventListener);
		
		int ret = mNativeInterface.setUpEventLooperThread(handle, looper);
		if (ret < 0) {
			throw new SerialComException("setUpEventLooper()", mErrMapper.getMappedError(ret));
		}
		
		return true;
	}
	
	/**
	 * Check if we have handler corresponding to this listener and take actions accordingly.
	 * 
	 * @param eventListener
	 * @return
	 * @throws SerialComException
	 */
	public boolean destroyEventLooper(ISerialComEventListener eventListener) throws SerialComException {
		long handle = -1;
		SerialComPortHandleInfo mHandleInfo = null;
		
		for(SerialComPortHandleInfo mInfo: mPortHandleInfo){
			if(mInfo.getEventListener() ==  eventListener) {
				handle = mInfo.getPortHandle();
				mHandleInfo = mInfo;
				break;
			}
		}
		if(handle == -1) {
			throw new SerialComException(" destroyEventLooper()", SerialComErrorMapper.ERR_WRONG_LISTENER_PASSED);
		}
		
		// We got valid handle so destroy native threads for this listener.
		int ret = mNativeInterface.destroyEventLooperThread(handle);
		if(ret < 0) {
			throw new SerialComException("destroyDataLooper()", mErrMapper.getMappedError(ret));
		}
		
		// Remove event listener from information object about this handle.
		mHandleInfo.setEventListener(null);
		
		return true;
	}
	
	/**
	 * Check if we have handler corresponding to this listener and take actions accordingly.
	 * 
	 * @param listener
	 * @return
	 * @throws SerialComException
	 */
	public boolean pauseListeningEvents(ISerialComEventListener listener) throws SerialComException {
		long handle = -1;
		SerialComLooper looper = null;
		for(SerialComPortHandleInfo mInfo: mPortHandleInfo){
			if(mInfo.getEventListener() ==  listener) {
				handle = mInfo.getPortHandle();
				looper = mInfo.getLooper();
				break;
			}
		}
		
		if(handle != -1) {
			// We got a valid handle, so pause native threads for this listener first.
			int ret = mNativeInterface.pauseListeningEvents(handle);
			if (ret > 0) {
				// now pause corresponding looper thread.
				looper.pause();
				return true;
			} else {
				throw new SerialComException("pauseListeningEvents()", mErrMapper.getMappedError(ret));
			}
		} else {
			throw new SerialComException("pauseListeningEvents()", SerialComErrorMapper.ERR_WRONG_LISTENER_PASSED);
		}
	}
	
	/**
	 * Check if we have handler corresponding to this listener and take actions accordingly.
	 * 
	 * @param listener
	 * @return
	 * @throws SerialComException
	 */
	public boolean resumeListeningEvents(ISerialComEventListener listener) throws SerialComException {
		long handle = -1;
		SerialComLooper looper = null;
		for(SerialComPortHandleInfo mInfo: mPortHandleInfo){
			if(mInfo.getEventListener() ==  listener) {
				handle = mInfo.getPortHandle();
				looper = mInfo.getLooper();
				break;
			}
		}
		
		if(handle != -1) {
			// We got valid handle, so resume looper first.
			looper.resume();
			
			// now resume native subsystem.
			int ret = mNativeInterface.resumeListeningEvents(handle);
			if (ret > 0) {
				return true;
			} else {
				throw new SerialComException("resumeListeningEvents()", mErrMapper.getMappedError(ret));
			}
		} else {
			throw new SerialComException("resumeListeningEvents()", SerialComErrorMapper.ERR_WRONG_LISTENER_PASSED);
		}
	}
	
}
