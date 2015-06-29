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
 * <p>This class helps in consistent error reporting in java layer mapping OS specific error numbers.</p>
 */
public final class SerialComErrorMapper {
	private int osType = 0;
	public static final String ERR_PROP_VM_VENDOR = "The java.vm.vendor system property is null in the system";
	public static final String ERR_PROP_OS_NAME = "The os.name system property is null in the system";
	public static final String ERR_PROP_OS_ARCH = "The os.arch system property is null in the system";
	public static final String ERR_PORT_ALREADY_OPEN = "The requested port is already opened";
	public static final String ERR_SCM_DOES_NOT_INSTANTIATED = "SerialComManager class has to be instantiated first";
	public static final String ERR_SCM_NOT_STORE_PORTINFO = "Could not save info about port locally. Please retry opening port.";
	public static final String ERR_UNABLE_TO_DETECT_OS_TYPE = "Unable to detect Operating System";
	public static final String ERR_PORT_NAME_FOR_PORT_OPENING = "Name of the port to open is not passed";
	public static final String ERR_WRONG_HANDLE = "Wrong port handle passed for the requested operations";
	public static final String ERR_LISTENER_ALREADY_EXIST = "Event listener already exist. Only one listener allowed.";
	public static final String ERR_DATA_LISTENER_ALREADY_EXIST = "Data listener already exist. Only one listener allowed.";
	public static final String ERR_NULL_POINTER_FOR_DBITS = "The dataBits argument can not be null";
	public static final String ERR_NULL_POINTER_FOR_SBITS = "The stopBits argument can not be null";
	public static final String ERR_NULL_POINTER_FOR_PARITY = "The parity argument can not be null";
	public static final String ERR_NULL_POINTER_FOR_BRATE = "The baudRate argument can not be null";
	public static final String ERR_NULL_POINTER_FOR_ENDIAN = "The endianness argument can not be null";
	public static final String ERR_NULL_POINTER_FOR_NUMBYTE = "The numOfBytes argument can not be null";
	public static final String ERR_NULL_POINTER_FOR_LISTENER = "The listener can not be null";
	public static final String ERR_NULL_POINTER_FOR_MONITOR = "The monitor can not be null";
	public static final String ERR_NULL_POINTER_FOR_FILE_SEND = "The fileToSend argument can not be null";
	public static final String ERR_NULL_POINTER_FOR_PROTOCOL = "The ftpProto argument can not be null";
	public static final String ERR_NULL_POINTER_FOR_VARIANT = "The ftpVariant argument can not be null";
	public static final String ERR_NULL_POINTER_FOR_MODE = "The ftpMode argument can not be null";
	public static final String ERR_NULL_POINTER_FOR_FILE_RECEIVE = "The fileToReceive argument can not be null";
	public static final String ERR_NULL_POINTER_FOR_SMODE = "The streamMode argument can not be null";
	public static final String ERR_NULL_POINTER_FOR_FLOWCTRL = "The flowctrl argument can not be null";
	public static final String ERR_WRONG_LISTENER_PASSED = "This listener is not registered";
	public static final String ERR_UNABLE_TO_WRITE = "Unable to copy native library in tmp directory. Probably insufficient permissions.";
	public static final String ERR_PORT_NAME_NULL = "Port name can not be null";
	public static final String ERR_INVALID_COMBINATION_ARG = "Invalid combination of arguments passed";
	public static final String ERR_WIN_OWNERSHIP = "Windows does not allow port sharing. The exclusiveOwnerShip must be true";
	public static final String ERR_CLOSE_WITHOUT_UNREG_DATA = "Closing port without unregistering data listener is not allowed";
	public static final String ERR_CLOSE_WITHOUT_UNREG_EVENT = "Closing port without unregistering event listener is not allowed";
	public static final String ERR_WRITE_NULL_DATA_PASSED = "Null data buffer passed to write operation";
	public static final String ERR_INDEX_VIOLATION = "Index violation detected";
	public static final String ERR_READ_NULL_DATA_PASSED = "Null data buffer passed to read operation";
	public static final String ERR_NOT_A_FILE = "Seems like either file does not exist or not a file";
	public static final String ERR_TIMEOUT_RECEIVER_CONNECT = "Timedout while waiting for file receiver to initiate connection setup";
	public static final String ERR_TIMEOUT_TRANSMITTER_CONNECT = "Timedout while trying to connect to file sender";
	public static final String ERR_TIMEOUT_RECV_FROM_SENDER = "Timedout while trying to receive next data byte from file sender";
	public static final String ERR_MAX_TX_RETRY_REACHED = "Maximum number of retries reached while sending same data block";
	public static final String ERR_MAX_RX_RETRY_REACHED = "Maximum number of retries reached while receiving same data block";
	public static final String ERR_UNKNOWN_OCCURED = "Unknown error occured";
	public static final String ERR_TIMEOUT_ACKNOWLEDGE_BLOCK = "Timedout while waiting for block reception acknowledgement from file receiver";
	public static final String ERR_TIMEOUT_ACKNOWLEDGE_EOT = "Timedout while waiting for EOT reception acknowledgement from file receiver";
	public static final String ERR_CAN_NOT_BE_NULL = "This argument can not be null";
	public static final String ERR_IN_STREAM_ALREADY_EXIST = "Input stream already exist for this handle";
	public static final String ERR_OUT_STREAM_ALREADY_EXIST = "Output stream already exist for this handle";
	public static final String ERR_BYTE_STREAM_IS_CLOSED = "The byte stream has been closed";
	public static final String ERR_ARG_NOT_BYTE_ARRAY = "The argument data is not a byte array";
	public static final String ERR_ARG_CAN_NOT_NEGATIVE = "Argument(s) can not be negative";
	public static final String ERR_DELAY_CAN_NOT_NEG = "Delay can not be negative";
	public static final String ERR_DUR_CAN_NOT_NEG_ZERO = "Duration can not be negative or zero";
	public static final String ERR_CUSTB_CAN_NOT_NEG_ZERO = "Baudrate can not be negative or zero";

	/**
	 * <p>Allocates a new SerialComErrorMapper object.</p>
	 * @param osType operating system type as identified by SCM library
	 */
	public SerialComErrorMapper(int osType) {
		this.osType = osType;
	}

	/**
	 * <p>Error numbers and their meaning is taken from Linux standard just to make it easier for developer. If an error occurs in
	 * POSIX/BSD compatible native library, these error numbers have one-to-one correspondence. However, in case of windows, native
	 * library may map windows error number to their corresponding number in unix given below. This gives application a consistent
	 * behavior of java library throwing exactly same exception/error irrespective of which OS platform the library is running on.</p>
	 * 
	 * <p>For windows if the error can not be mapped exact windows error code is printed.</p>
	 * 
	 * @param errorNumber operating system specific error number (may have been translated already in case of Windows OS)
	 * @return string constructed out of error number
	 * 
	 */
	public String getMappedError(long errorNumber) {
		String exceptionType = null;
		long err = -1 * errorNumber;
		int errorNum = (int)err;
		
		if(osType == SerialComManager.OS_WINDOWS) {
			
		}else {
			switch (errorNum) {
				case 1:
					if((osType == SerialComManager.OS_HP_UX) || (osType == SerialComManager.OS_SOLARIS)) {
						exceptionType = new String("Not super-user");
					}else {
						exceptionType = new String("Operation not permitted");
					}
					break;
				case 2:
					exceptionType = new String("No such file or directory");
					break;
				case 3:
					exceptionType = new String("No such process");
					break;
				case 4:
					exceptionType = new String("Interrupted system call");
					break;
				case 5:
					if((osType == SerialComManager.OS_LINUX) || (osType == SerialComManager.OS_SOLARIS) || (osType == SerialComManager.OS_IBM_AIX)) {
						exceptionType = new String("I/O error");
					}else {
						exceptionType = new String("Input/output error");
					}
					break;
				case 6:
					if((osType == SerialComManager.OS_LINUX) || (osType == SerialComManager.OS_SOLARIS) || (osType == SerialComManager.OS_IBM_AIX) || (osType == SerialComManager.OS_HP_UX)) {
						exceptionType = new String("No such device or address");
					}else {
						exceptionType = new String("Device not configured");
					}
					break;
				case 7:
					if((osType == SerialComManager.OS_LINUX) || (osType == SerialComManager.OS_SOLARIS) || (osType == SerialComManager.OS_IBM_AIX) || (osType == SerialComManager.OS_HP_UX)) {
						exceptionType = new String("Arg list too long");
					}else {
						exceptionType = new String("Argument list too long");
					}
					break;
				case 8:
					exceptionType = new String("Exec format error");
					break;
				case 9:
					if((osType == SerialComManager.OS_LINUX) || (osType == SerialComManager.OS_SOLARIS) || (osType == SerialComManager.OS_IBM_AIX) || (osType == SerialComManager.OS_HP_UX)) {
						exceptionType = new String("Bad file number");
					}else {
						exceptionType = new String("Bad file descriptor");
					}
					break;
				case 10:
					if((osType == SerialComManager.OS_SOLARIS) || (osType == SerialComManager.OS_HP_UX)) {
						exceptionType = new String("No children");
					}else {
						exceptionType = new String("No child processes");
					}
					break;
				case 11:
					if(osType == SerialComManager.OS_LINUX) {
						exceptionType = new String("Try again");
					}else if(osType == SerialComManager.OS_HP_UX) {
						exceptionType = new String("No more processes");
					}else {
						exceptionType = new String("Resource deadlock avoided");
					}
					break;
				case 12:
					if(osType == SerialComManager.OS_LINUX) {
						exceptionType = new String("Out of memory");
					}else if((osType == SerialComManager.OS_SOLARIS) || (osType == SerialComManager.OS_HP_UX)) {
						exceptionType = new String("Not enough core");
					}else if(osType == SerialComManager.OS_IBM_AIX) {
						exceptionType = new String("Not enough space");
					}else {
						exceptionType = new String("Cannot allocate memory");
					}
					break;
				case 13:
					exceptionType = new String("Permission denied");
					break;
				case 14:
					exceptionType = new String("Bad address");
					break;
				case 15:
					exceptionType = new String("Block device required");
					break;
				case 16:
					if(osType == SerialComManager.OS_LINUX) {
						exceptionType = new String("Device or resource busy");
					}else if((osType == SerialComManager.OS_SOLARIS) || (osType == SerialComManager.OS_HP_UX)) {
						exceptionType = new String("Mount device busy");
					}else if(osType == SerialComManager.OS_IBM_AIX) {
						exceptionType = new String("Resource busy");
					}else {
						exceptionType = new String("Device busy");
					}
					break;
				case 17:
					exceptionType = new String("File exists");
					break;
				case 18:
					if(osType == SerialComManager.OS_IBM_AIX) {
						exceptionType = new String("Improper link");
					}else {
						exceptionType = new String("Cross-device link");
					}
					break;
				case 19:
					if((osType == SerialComManager.OS_LINUX) || (osType == SerialComManager.OS_SOLARIS) || (osType == SerialComManager.OS_IBM_AIX) || (osType == SerialComManager.OS_HP_UX)) {
						exceptionType = new String("No such device");
					}else {
						exceptionType = new String("Operation not supported by device");
					}
					break;
				case 20:
					exceptionType = new String("Not a directory");
					break;
				case 21:
					exceptionType = new String("Is a directory");
					break;
				case 22:
					exceptionType = new String("Invalid argument");
					break;
				case 23:
					if((osType == SerialComManager.OS_LINUX) || (osType == SerialComManager.OS_SOLARIS) || (osType == SerialComManager.OS_HP_UX)) {
						exceptionType = new String("File table overflow");
					}else {
						exceptionType = new String("Too many open files in system");
					}
					break;
				case 24:
					exceptionType = new String("Too many open files");
					break;
				case 25:
					if((osType == SerialComManager.OS_LINUX) || (osType == SerialComManager.OS_HP_UX)) {
						exceptionType = new String("Not a typewriter");
					}else if(osType == SerialComManager.OS_IBM_AIX) {
						exceptionType = new String("Inappropriate I/O control operation");
					}else {
						exceptionType = new String("Inappropriate ioctl for device");
					}
					break;
				case 26:
					exceptionType = new String("Text file busy");
					break;
				case 27:
					exceptionType = new String("File too large");
					break;
				case 28:
					exceptionType = new String("No space left on device");
					break;
				case 29:
					if(osType == SerialComManager.OS_IBM_AIX) {
						exceptionType = new String("Invalid seek");
					}else {
						exceptionType = new String("Illegal seek");
					}
					break;
				case 30:
					exceptionType = new String("Read only file system");
					break;
				case 31:
					exceptionType = new String("Too many links");
					break;
				case 32:
					exceptionType = new String("Broken pipe");
					break;
				case 33:
					if(osType == SerialComManager.OS_LINUX) {
						exceptionType = new String("Math argument out of domain of func");
					}else if(osType == SerialComManager.OS_SOLARIS) {
						exceptionType = new String("Math arg out of domain of func");
					}else if(osType == SerialComManager.OS_IBM_AIX) {
						exceptionType = new String("Domain error within math function");
					}else if(osType == SerialComManager.OS_HP_UX) {
						exceptionType = new String("OS error code : 33");
					}else {
						exceptionType = new String("Numerical argument out of domain");
					}
					break;
				case 34:
					if((osType == SerialComManager.OS_LINUX) || (osType == SerialComManager.OS_SOLARIS)) {
						exceptionType = new String("Math result not representable");
					}else if(osType == SerialComManager.OS_HP_UX) {
						exceptionType = new String("OS error code : 34");
					}else {
						exceptionType = new String("Result too large");
					}
					break;
				case 35:
					if(osType == SerialComManager.OS_LINUX) {
						exceptionType = new String("Resource deadlock would occur");
					}else if(osType == SerialComManager.OS_SOLARIS) {
						exceptionType = new String("Resource deadlock would occur");
					}
					break;
				default:
					StringBuilder sBuilder = new StringBuilder("OS error code : ");
					sBuilder.append(errorNum);
					exceptionType = sBuilder.toString();
					break;
			}
		}
		
		return exceptionType;
	}
}


























