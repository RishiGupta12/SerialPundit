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

package com.embeddedunveiled.serial.vendor;

import java.io.File;
import java.io.FileNotFoundException;

import com.embeddedunveiled.serial.SerialComException;
import com.embeddedunveiled.serial.SerialComLoadException;
import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.SerialComUnexpectedException;
import com.embeddedunveiled.serial.internal.SerialComSystemProperty;

/**
 * <p>Super class for all classes which implements vendor specific API to talk to 
 * their devices using the libraries provided by vendor. These libraries from vendor 
 * may be propriety or not.</p>
 */
public class SerialComVendorLib {

	/**<p>The value indicating proprietary D2XX software interface from Future Technology Devices 
	 * International Ltd.</p>*/
	public static final int VLIB_FTDI_D2XX = 0x01;

	/**<p>The value indicating 'SimpleIO' library from Microchip Technology Inc.</p>*/
	public static final int VLIB_MCHP_SIMPLEIO = 0x02;

	/**<p>The value indicating 'CP210xRuntime' library from Silicon Laboratories, Inc.</p>*/
	public static final int VLIB_SLABS_CP210XRUNTIME = 0x03;

	/**<p>The value indicating 'CP210xManufacturing' library from Silicon Laboratories, Inc.</p>*/
	public static final int VLIB_SLABS_CP210XMANUFACTURING = 0x04;

	/**<p>The value indicating 'USBXpress' library from Silicon Laboratories, Inc.</p>*/
	public static final int VLIB_SLABS_USBXPRESS = 0x05;

	/**
	 * <p>Allocates a new SerialComVendorLib object.</p>
	 */
	public SerialComVendorLib() {
	}

	/**
	 * <p>Gives an instance of the class which implements API defined by vendor in their propriety library.</p>
	 * 
	 * @param vendorLibIdentifier one of the constant VLIB_XXXX_XXXX in SerialComVendorLib class.
	 * @param libDirectory directory where vendor library is placed.
	 * @param vlibName full name of the vendor library (for ex. libftd2xx.so.1.1.12).
	 * @param cpuArch architecture of CPU this library is running on.
	 * @param osType operating system this library is running on.
	 * @param serialComSystemProperty instance of SerialComSystemProperty to get required java properties.
	 * @return object of class on which vendor specific API calls can be made otherwise null.
	 * @throws SerialComUnexpectedException if a critical java system property is null in system.
	 * @throws SecurityException if any java system property can not be accessed.
	 * @throws FileNotFoundException if the vendor library file is not found.
	 * @throws UnsatisfiedLinkError if loading/linking shared library fails.
	 * @throws SerialComException if an I/O error occurs.
	 * @throws SerialComLoadException if the library can not be found, extracted or loaded 
	 *                                 if the mentioned library is not supported by vendor for operating 
	 *                                 system and cpu architecture combination.
	 */
	public SerialComVendorLib getVendorLibInstance(int vendorLibIdentifier, File libDirectory, String vlibName, 
			int cpuArch, int osType, SerialComSystemProperty serialComSystemProperty) throws UnsatisfiedLinkError, 
			SerialComLoadException, SerialComUnexpectedException, SecurityException, FileNotFoundException {
		SerialComVendorLib vendorLib = null;
		if(vendorLibIdentifier == VLIB_FTDI_D2XX) {
			if(!((cpuArch == SerialComManager.ARCH_AMD64) || (cpuArch == SerialComManager.ARCH_X86))) {
				throw new SerialComLoadException("FTDI D2XX library is not supported for this CPU architecture !");
			}
			if(!((osType == SerialComManager.OS_WINDOWS) || (osType == SerialComManager.OS_LINUX) || (osType == SerialComManager.OS_MAC_OS_X))) {
				throw new SerialComLoadException("FTDI D2XX library is not supported for this operating system !");
			}
			vendorLib = new SerialComFTDID2XX(libDirectory, vlibName, cpuArch, osType, serialComSystemProperty);
			return vendorLib;
		}else if(vendorLibIdentifier == VLIB_MCHP_SIMPLEIO) {
			if(!((cpuArch == SerialComManager.ARCH_AMD64) || (cpuArch == SerialComManager.ARCH_X86))) {
				throw new SerialComLoadException("Microchip SimpleIO library is not supported for this CPU architecture !");
			}
			if(osType != SerialComManager.OS_WINDOWS) {
				throw new SerialComLoadException("Microchip SimpleIO library is not supported for this operating system !");
			}
			vendorLib = new SerialComMCHPSimpleIO(libDirectory, vlibName, cpuArch, osType, serialComSystemProperty);
			return vendorLib;
		}else if(vendorLibIdentifier == VLIB_SLABS_CP210XRUNTIME) {
			if(!((cpuArch == SerialComManager.ARCH_AMD64) || (cpuArch == SerialComManager.ARCH_X86))) {
				throw new SerialComLoadException("Silicon labs cp210x runtime dll library is not supported for this CPU architecture !");
			}
			if(osType != SerialComManager.OS_WINDOWS) {
				throw new SerialComLoadException("Silicon labs cp210x runtime dll library is not supported for this operating system !");
			}
			vendorLib = new SerialComSLabsCP210xRuntime(libDirectory, vlibName, cpuArch, osType, serialComSystemProperty);
			return vendorLib;
		}else if(vendorLibIdentifier == VLIB_SLABS_CP210XMANUFACTURING) {
			if(!((cpuArch == SerialComManager.ARCH_AMD64) || (cpuArch == SerialComManager.ARCH_X86))) {
				throw new SerialComLoadException("Silicon labs cp210x manufacturing dll library is not supported for this CPU architecture !");
			}
			if(osType != SerialComManager.OS_WINDOWS) {
				throw new SerialComLoadException("Silicon labs cp210x manufacturing dll library is not supported for this operating system !");
			}
			vendorLib = new SerialComSLabsCP210xManufacturing(libDirectory, vlibName, cpuArch, osType, serialComSystemProperty);
			return vendorLib;
		}else if(vendorLibIdentifier == VLIB_SLABS_USBXPRESS) {
			if(!((cpuArch == SerialComManager.ARCH_AMD64) || (cpuArch == SerialComManager.ARCH_X86))) {
				throw new SerialComLoadException("Silicon labs usbxpress library is not supported for this CPU architecture !");
			}
			if(osType != SerialComManager.OS_WINDOWS) {
				throw new SerialComLoadException("Silicon labs usbxpress library is not supported for this operating system !");
			}
			vendorLib = new SerialComSLabsCP210xManufacturing(libDirectory, vlibName, cpuArch, osType, serialComSystemProperty);
			return vendorLib;
		}else {
		}

		return null;
	}
}
