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

import java.io.File;

/**
 * <p>This class is an interface between java and native shared library.</p>
 */
public final class SerialComMCHPSIOJNIBridge {

	/**
	 * <p>Allocates a new SerialComMCHPSIOJNIBridge object.</p>
	 */
	public SerialComMCHPSIOJNIBridge() {
	}
	
	/**
	 * <p>Extract native library from jar in a working directory and load it. Also load vendor's library.</p> 
	 * @param directoryPath directory in which native library will be extracted and vendor library will be found
	 */
	public static boolean loadNativeLibrary(File libDirectory) throws UnsatisfiedLinkError {
		return false;
	}
	
	public native int initMCP2200(int vendorID, int productID);

}
