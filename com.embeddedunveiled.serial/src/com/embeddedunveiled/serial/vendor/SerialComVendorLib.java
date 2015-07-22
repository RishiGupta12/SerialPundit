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

package com.embeddedunveiled.serial.vendor;

import java.io.File;

import com.embeddedunveiled.serial.SerialComException;
import com.embeddedunveiled.serial.SerialComLoadException;

/**
 * <p>Super class for all classes which implements vendor specific API to talk to 
 * their devices using the libraries provided by vendor. These libraries from vendor 
 * may be propriety or not.</p>
 */
public class SerialComVendorLib {
	
	/**<p> The value indicating 'SimpleIO-UM.dll' library from Microchip technology Inc. </p>*/
	public static final int VLI_MCHP_SIMPLEIO =  0x01;

	/**
	 * <p>Allocates a new SerialComVendorLib object.</p>
	 */
	public SerialComVendorLib() {
	}
	
	/**
	 * <p>Gives an instance on which vendor specific method calls can be made.</p>
	 * 
	 * @param vendorLibIdentifier one of the constant VLI_xxx_xxx in SerialComVendorLib class
	 * @param libDirectory directory where vendor library is placed
	 * @return true is device is connected otherwise false
	 * @throws SerialComException if an I/O error occurs.
	 * @throws SerialComLoadException if the library can not be extracted or loaded
	 * @throws IllegalArgumentException if productID or vendorID is negative or invalid
	 */
	public SerialComVendorLib getVendorLibInstance(int vendorLibIdentifier, File libDirectory) throws SerialComException, SerialComLoadException {
		SerialComVendorLib vendorLib = null;
		if(vendorLibIdentifier == VLI_MCHP_SIMPLEIO) {
			vendorLib = new SerialComMCHPSimpleIO(libDirectory);
			return vendorLib;
		}
		
		return null;
	}

}
