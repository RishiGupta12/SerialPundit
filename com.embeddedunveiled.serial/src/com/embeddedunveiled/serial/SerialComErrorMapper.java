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
 * <p>This class gives consistent error in java layer mapping OS specific error numbers.</p>
 */

public final class SerialComErrorMapper {
	
	public static final String ERR_UNABLE_TO_DETECT_OS_TYPE = "Unable to detect Operating System";
	public static final String ERR_NULL_POINTER_FOR_PORT_OPENING = "Name of the port to open is not passed";
	public static final String ERR_WRONG_HANDLE = "Wrong port handle passed for the requested operations";
	public static final String ERR_LISTENER_ALREADY_EXIST = "Event listener already exist. Only one listener allowed";
	public static final String ERR_DATA_LISTENER_ALREADY_EXIST = "Data listener already exist. Only one listener allowed";
	public static final String ERR_NULL_POINTER_FOR_LISTENER = "The listener can not be null";
	public static final String ERR_WRONG_LISTENER_PASSED = "This listener is not registered";
	public static final String ERR_UNABLE_TO_WRITE = "Unable to copy native library in tmp directory. Probably insufficient permissions";
	public static final String ERR_PORT_NAME_NULL = "Port name can not be null";
	public static final String ERR_INVALID_DATA_LENGTH = "Number of bytes should be positive value";
	public static final String ERR_WIN_OWNERSHIP = "Windows does not allow port sharing. The exclusiveOwnerShip must be true";
	public static final String ERR_CLOSE_WITHOUT_UNREG_DATA = "Closing port without unregistering data listener is not allowed";
	public static final String ERR_CLOSE_WITHOUT_UNREG_EVENT = "Closing port without unregistering event listener is not allowed";
	public static final String ERR_WRITE_NULL_DATA_PASSED = "Null data buffer passed to write operation";
	public static final String ERR_WRITE_INDEX_VIOLATION = "Index violation detected";
	public static final String ERR_NOT_A_FILE = "Seems like either file does not exist or not a file";
	public static final String ERR_TIMEOUT_RECEIVER_CONNECT = "Timedout while waiting for receiver to initiate connection setup";
	public static final String ERR_MAX_RETRY_REACHED = "Maximum number of retries reached while sending same data block";
	public static final String ERR_KNOWN_ERROR_OCCURED = "Unknown error occured";
	public static final String ERR_TIMEOUT_ACKNOWLEDGE_BLOCK = "Timedout while waiting for block reception acknowledgement from receiver";
	public static final String ERR_TIMEOUT_ACKNOWLEDGE_EOT = "Timedout while waiting for EOT reception acknowledgement from receiver";
	
	public SerialComErrorMapper() {
	}
	
	/**
	 * <p>Error numbers and their meaning is taken from Linux standard just to make it easier for developer. If an error occurs in
	 * POSIX/BSD compatible native library, these error numbers have one-to-one correspondence. However, in case of windows, native
	 * library may map windows error number to their corresponding number given below. This gives user a consistent behaviour
	 * of java library throwing exactly same exception/error irrespective of which OS platform the library is running on.</p>
	 */
	public String getMappedError(long errorNumber) {
		String exceptionType = null;
		switch ((int) errorNumber) {
			case -1:
				exceptionType = new String("Operation not permitted !");
				break;
			case -2:
				exceptionType = new String("No such file or directory !");
				break;
			case -4:
				exceptionType = new String("I/O error !");
				break;
			case -5:
				exceptionType = new String("I/O error !");
				break;
			case -6:
				exceptionType = new String("No such device or address !");
				break;
			case -11:
				exceptionType = new String("Try again !");
				break;
			case -12:
				exceptionType = new String("Out of memory !");
				break;
			case -13:
				exceptionType = new String("Permission denied !");
				break;
			case -14:
				exceptionType = new String("Bad address !");
				break;
			case -16:
				exceptionType = new String("Device or resource busy !");
				break;
			case -19:
				exceptionType = new String("No such device !");
				break;
			case -22:
				exceptionType = new String("Invalid argument !");
				break;
			case -24:
				exceptionType = new String("Too many open files !");
				break;
			case -25:
				exceptionType = new String("Not a typewriter !");
				break;
			case -26:
				exceptionType = new String("Text file busy !");
				break;
			case -27:
				exceptionType = new String("File too large !");
				break;
			case -28:
				exceptionType = new String("No space left on device !");
				break;
			case -30:
				exceptionType = new String("Read-only file system !");
				break;
			case -31:
				exceptionType = new String("Too many links !");
				break;
			case -32:
				exceptionType = new String("Broken pipe !");
				break;
			case -35:
				exceptionType = new String("Resource deadlock would occur !");
				break;
			case -36:
				exceptionType = new String("File name too long !");
				break;
			case -38:
				exceptionType = new String("Function not implemented !");
				break;
			case -40:
				exceptionType = new String("Too many symbolic links encountered !");
				break;
			case -42:
				exceptionType = new String("No message of desired type !");
				break;
			case -43:
				exceptionType = new String("Identifier removed !");
				break;
			case -49:
				exceptionType = new String("Protocol driver not attached !");
				break;
			case -53:
				exceptionType = new String("Invalid request descriptor !");
				break;
			case -59:
				exceptionType = new String("Bad font file format !");
				break;
			case -60:
				exceptionType = new String("Device not a stream !");
				break;
			case -61:
				exceptionType = new String("No data available !");
				break;
			case -62:
				exceptionType = new String("Timer expired !");
				break;
			case -63:
				exceptionType = new String("Out of streams resources !");
				break;
			case -64:
				exceptionType = new String("Machine is not on the network !");
				break;
			case -77:
				exceptionType = new String("File descriptor in bad state !");
				break;
			case -79:
				exceptionType = new String("Can not access a needed shared library !");
				break;
			case -80:
				exceptionType = new String("Accessing a corrupted shared library !");
				break;
			case -82:
				exceptionType = new String("Attempting to link in too many shared libraries !");
				break;
			case -83:
				exceptionType = new String("Cannot exec a shared library directly !");
				break;
			case -84:
				exceptionType = new String("Illegal byte sequence !");
				break;
			case -85:
				exceptionType = new String("Interrupted system call should be restarted !");
				break;
			case -86:
				exceptionType = new String("Streams pipe error !");
				break;
			case -87:
				exceptionType = new String("Too many users !");
				break;
			case -130:
				exceptionType = new String("Owner died !");
				break;
			case -131:
				exceptionType = new String("State not recoverable !");
				break;
			case -133:
				exceptionType = new String("Memory page has hardware error !");
				break;
			case -239:
				exceptionType = new String("There are too many outstanding asynchronous I/O requests !");
				break;
			case -240:
				// In some cases, we may deliberately send this value is we want known error to occur.
				exceptionType = new String("Unknown exception occured !");
				break;
			case -241:
				exceptionType = new String("Exclusive ownership is not supported for Solaris as of now. !");
				break;
			case -242:
				exceptionType = new String("Enable parity in configureComPortData() for parity/frame error checking to work. !");
				break;
			default:
				// This means some we were not able to figure out.
				exceptionType = new String("Unknown exception occured !");
				break;
		}
		return exceptionType;
	}
}
