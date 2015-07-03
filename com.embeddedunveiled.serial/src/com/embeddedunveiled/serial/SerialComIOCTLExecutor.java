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
 * <p>This class helps in implementing device specific IOCTL calls.</p>
 * 
 * <p>It should be noted that the USB-UART bridge generally have one time programmable memory 
 * and therefore configuration/customization settings must be done thoughtfully either programmatically 
 * or through vendor provided utility.</p>
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
		long ret = 0;
		ret = mNativeInterface.ioctlExecuteOperation(handle, operationCode);
		if(ret < 0) {
			throw new SerialComException("ioctlExecuteOperation()", mErrMapper.getMappedError(ret));
		}
		return true;
	}
	
	/**
     * <p>Executes the requested operation on the specified handle passing the given value to operation.</p>
     * <p>This may be used to toggle GPIO pins present on some state-of-art USB to UART bridges. For example 
     * for Silicon labs CP210X series using 'CP210x VCP Linux 3.0 Driver Kit' the GPIO 0 pin can be toggled 
     * as shown below: </p>
     * <p>Turn on  : ioctlSetValue(handle, 0x8001, 0x00010001) </p>
     * <p>Turn off : ioctlSetValue(handle, 0x8001, 0x00000001) </p>
     * 
     * <p>Modern USB-UART bridge generally have user-configurable GPIO pins for status and control information. 
     * Each of these GPIO pins may be used as inputs, open-drain outputs, or push-pull outputs. Care must be 
     * taken to correctly interface these GPIO pins for required amount of current.</p>
     * 
     * <p>Further GPIO pins may have multiplexed functionality. For example a particular GPIO Pin may be configured 
     * as GPIO to control external peripheral or may be configured as RTS modem line. It is advised to consult 
     * datasheet.</p>
     *
     * @param handle handle of the port on which to execute this ioctl operation
     * @param operationCode unique ioctl operation code
     * @return true if operation executed successfully
	 * @throws SerialComException if the operation can not be completed as requested
     */
	public boolean ioctlSetValue(long handle, long operationCode, long value) throws SerialComException {
		long ret = 0;
		ret = mNativeInterface.ioctlSetValue(handle, operationCode, value);
		if(ret < 0) {
			throw new SerialComException("ioctlSetValue()", mErrMapper.getMappedError(ret));
		}
		return true;
	}

	/**
     * <p>Executes the requested operation on the specified handle. This operation returns a numerical value. 
     * This method can be used to read a register in chip. For example to get the status of GPIOs pins on 
     * CP210X series from Silicon labs using 'CP210x VCP Linux 3.0 Driver Kit' the following call can be made:</p>
     * 
     * <p>long value = ioctlSetValue(handle, 0x8000) </p>
     * 
     * @param handle handle of the port on which to execute this ioctl operation
     * @param operationCode unique ioctl operation code
     * @return value requested
	 * @throws SerialComException if the operation can not be completed as requested
     */
	public long ioctlGetValue(long handle, long operationCode) throws SerialComException {
		long ret = 0;
		ret = mNativeInterface.ioctlGetValue(handle, operationCode);
		if(ret < 0) {
			throw new SerialComException("ioctlGetValue()", mErrMapper.getMappedError(ret));
		}
		return ret;
	}
}
