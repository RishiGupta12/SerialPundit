/**
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

package com.embeddedunveiled.serial.internal;

import java.util.List;
import com.embeddedunveiled.serial.ISerialComDataListener;
import com.embeddedunveiled.serial.ISerialComEventListener;
import com.embeddedunveiled.serial.SerialComException;

/**
 * Represents Proactor in our IO design pattern.
 * 
 * <p>An application may register data listener only, event listener only or both listeners. A single dedicated looper handles both the listeners.
 * We first check if a looper exist for given handle or not. If it does not exist we create and start looper thread which loops over data or event 
 * as specified by application. If it exist, we start data or event looper thread as specified by application.</p>
 * 
 * <p>An application can have multiple handles for the same port (if there is no exclusive owner of port).</p>
 * <p>A handle can have only one looper for delivering data and line events.</p>
 * <p>A looper can have none or only one data looper at any instant of time.</p>
 * <p>A looper can have none or only one event looper at any instant of time.</p>
 */
public final class SerialComCompletionDispatcher {

	private SerialComPortJNIBridge mComPortJNIBridge = null;
	private SerialComErrorMapper mErrMapper = null;
	private List<SerialComPortHandleInfo> mPortHandleInfo = null;

	/**
	 * <p>Allocates a new SerialComCompletionDispatcher object.</p>
	 * 
	 * @param mComPortJNIBridge interface used to invoke appropriate native function
	 * @param errMapper reference to errMapper object to get and map error information
	 * @param portHandleInfo reference to portHandleInfo object to get/set information about handle/port
	 */
	public SerialComCompletionDispatcher(SerialComPortJNIBridge mComPortJNIBridge, SerialComErrorMapper errMapper, List<SerialComPortHandleInfo> portHandleInfo) {
		this.mComPortJNIBridge = mComPortJNIBridge;
		this.mErrMapper = errMapper;
		this.mPortHandleInfo = portHandleInfo;
	}

	/**
	 * <p>This method creates data looper thread and initialize subsystem for data event passing. </p>
	 * 
	 * @param handle handle of the opened port for which data looper need to be set up
	 * @param mHandleInfo Reference to SerialComPortHandleInfo object associated with given handle
	 * @param dataListener listener for which looper has to be set up
	 * @return true on success
	 * @throws SerialComException if not able to complete requested operation
	 */

	public boolean setUpDataLooper(long handle, SerialComPortHandleInfo mHandleInfo, ISerialComDataListener dataListener) throws SerialComException {

		SerialComLooper looper = mHandleInfo.getLooper();

		// Create looper for this handle and listener, if it does not exist.
		if(looper == null) {
			looper = new SerialComLooper(mComPortJNIBridge, mErrMapper);
			mHandleInfo.setLooper(looper);
		}

		// set up queue and start thread first, then set up native thread
		looper.startDataLooper(handle, dataListener, mHandleInfo.getOpenedPortName());
		mHandleInfo.setDataListener(dataListener);

		int ret = mComPortJNIBridge.setUpDataLooperThread(handle, looper);
		if(ret < 0) {
			looper.stopDataLooper();
			mHandleInfo.setDataListener(null);
			if(mHandleInfo.getEventListener() == null) {
				mHandleInfo.setLooper(null);
			}
			throw new SerialComException("setUpDataLooper()", mErrMapper.getMappedError(ret));
		}

		return true;
	}

	/**
	 * <p>Check if we have handler corresponding to this listener and take actions accordingly.</p>
	 * 
	 * @param dataListener listener for which looper has to be removed
	 * @return true on success
	 * @throws SerialComException if not able to complete requested operation
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
			throw new SerialComException("destroyDataLooper()", "This listener is not registered");
		}

		// We got valid handle so destroy native threads for this listener.
		int ret = mComPortJNIBridge.destroyDataLooperThread(handle);
		if(ret < 0) {
			throw new SerialComException("destroyDataLooper()", mErrMapper.getMappedError(ret));
		}

		// Destroy data looper thread.
		mHandleInfo.getLooper().stopDataLooper();
		
		// Remove data listener from information object about this handle.
		mHandleInfo.setDataListener(null);

		// If neither data nor event listener exist, looper object should be destroyed.
		if((mHandleInfo.getEventListener() == null) && (mHandleInfo.getDataListener() == null)) {
			mHandleInfo.setLooper(null);
		}

		return true;
	}

	/**
	 * <p>This method creates event looper thread and initialize subsystem for line event passing. </p>
	 * 
	 * @param handle handle of the opened port for which event looper need to be set up
	 * @param mHandleInfo Reference to SerialComPortHandleInfo object associated with given handle
	 * @param eventListener listener for which looper has to be set up
	 * @return true on success
	 * @throws SerialComException if error occurs
	 */

	public boolean setUpEventLooper(long handle, SerialComPortHandleInfo mHandleInfo, ISerialComEventListener eventListener) throws SerialComException {

		SerialComLooper looper = mHandleInfo.getLooper();

		// Create looper for this handle and listener, if it does not exist.
		if(looper == null) {
			looper = new SerialComLooper(mComPortJNIBridge, mErrMapper);
			mHandleInfo.setLooper(looper);
		}

		looper.startEventLooper(handle, eventListener, mHandleInfo.getOpenedPortName());
		mHandleInfo.setEventListener(eventListener);

		int ret = mComPortJNIBridge.setUpEventLooperThread(handle, looper);
		if(ret < 0) {
			looper.stopEventLooper();
			mHandleInfo.setEventListener(null);
			if(mHandleInfo.getDataListener() == null) {
				mHandleInfo.setLooper(null);
			}
			throw new SerialComException("setUpEventLooper()", mErrMapper.getMappedError(ret));
		}

		return true;
	}

	/**
	 * <p>Check if we have handler corresponding to this listener and take actions accordingly.</p>
	 * 
	 * @param eventListener listener for which looper has to be removed
	 * @return true on success
	 * @throws SerialComException if not able to complete requested operation
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
			throw new SerialComException("destroyEventLooper()", "This listener is not registered");
		}

		// We got valid handle so destroy native threads for this listener.
		int ret = mComPortJNIBridge.destroyEventLooperThread(handle);
		if(ret < 0) {
			throw new SerialComException("destroyDataLooper()", mErrMapper.getMappedError(ret));
		}
		
		// Destroy event looper thread.
		mHandleInfo.getLooper().stopEventLooper();

		// Remove event listener from information object about this handle.
		mHandleInfo.setEventListener(null);

		// If neither data nor event listener exist, looper object should be destroyed.
		if((mHandleInfo.getEventListener() == null) && (mHandleInfo.getDataListener() == null)) {
			mHandleInfo.setLooper(null);
		}

		return true;
	}

	/**
	 * <p>Check if we have handler corresponding to this listener and take actions accordingly.</p>
	 * 
	 * @param listener listener for which event has to be paused
	 * @return true on success
	 * @throws SerialComException if not able to complete requested operation
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
			int ret = mComPortJNIBridge.pauseListeningEvents(handle);
			if(ret > 0) {
				looper.pause(); // now pause corresponding looper thread.
				return true;
			}else {
				throw new SerialComException("pauseListeningEvents()", mErrMapper.getMappedError(ret));
			}
		}else {
			throw new SerialComException("pauseListeningEvents()", "This listener is not registered");
		}
	}

	/**
	 * <p>Check if we have handler corresponding to this listener and take actions accordingly.</p>
	 * 
	 * @param listener for which events sending should be resumed
	 * @return true on success
	 * @throws SerialComException if not able to complete requested operation
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
			int ret = mComPortJNIBridge.resumeListeningEvents(handle);
			if(ret > 0) {
				return true;
			}else {
				throw new SerialComException("resumeListeningEvents()", mErrMapper.getMappedError(ret));
			}
		}else {
			throw new SerialComException("resumeListeningEvents()", "This listener is not registered");
		}
	}
}
