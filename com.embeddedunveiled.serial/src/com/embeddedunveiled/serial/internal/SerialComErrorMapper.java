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

package com.embeddedunveiled.serial.internal;

import com.embeddedunveiled.serial.SerialComManager;

/**
 * <p>This class helps in consistent error reporting in java layer mapping OS specific error numbers.</p>
 * <p>Errors or exceptions are detected and reported as specific as possible to accelerate debugging.</p>
 * 
 * @author Rishi Gupta
 */
public final class SerialComErrorMapper {
	
	private int osType = 0;

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


























