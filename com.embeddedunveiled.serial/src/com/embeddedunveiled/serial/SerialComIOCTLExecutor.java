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

package com.embeddedunveiled.serial;

/**
 */
public final class SerialComIOCTLExecutor {
	
	private SerialComJNINativeInterface mNativeInterface = null;
	private SerialComErrorMapper mErrMapper = null;

	/**
     * <p>Allocates a new SerialComIOCTLExecutor object.</p>
     * @param mNativeInterface interface used to invoke appropriate native function
     * @param mErrMapper mapper to map error code into meaningful info
     */
	public SerialComIOCTLExecutor(SerialComJNINativeInterface mNativeInterface, SerialComErrorMapper mErrMapper) {
		this.mNativeInterface = mNativeInterface;
		this.mErrMapper = mErrMapper;
	}
	
	/**
     * <p>Executes the requested operation on the specified handle.</p>
     *
     * @param handle handle of the port on which to execute this ioctl operation
     * @param operationCode unique ioctl operation code
     * @return true if operation executed successfully
	 * @throws SerialComException if the operation can not be completed as requested
     */
	public boolean ioctlExecuteOperation(long handle, long operationCode) throws SerialComException {
		int ret = 0;
		ret = mNativeInterface.ioctlExecuteOperation(handle, operationCode);
		if(ret < 0) {
			throw new SerialComException("ioctlExecuteOperation()", mErrMapper.getMappedError(ret));
		}
		return true;
	}

}
