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
import com.embeddedunveiled.serial.internal.SerialComMCHPSIOJNIBridge;
import com.embeddedunveiled.serial.internal.SerialComRetStatus;

/**
 * <p>Super class for all classes which implements vendor specific API to talk to 
 * their devices using the libraries provided by vendor. These libraries from vendor 
 * may be propriety or not.</p>
 */
public final class SerialComMCHPSimpleIO extends SerialComVendorLib {
	
	/**<p> </p>*/
	public static final int OFF = 0;
	
	/**<p> </p>*/
	public static final int ON = 1;
	
	/**<p> </p>*/
	public static final int TOGGLE = 3;
	
	/**<p> </p>*/
	public static final int BLINKSLOW = 4;
	
	/**<p> </p>*/
	public static final int BLINKFAST = 5;

	private final SerialComMCHPSIOJNIBridge mSerialComMCHPSIOJNIBridge;
	
	/**
	 * <p>Allocates a new SerialComMCHPSimpleIO object.</p>
	 */
	public SerialComMCHPSimpleIO(File libDirectory) {
		mSerialComMCHPSIOJNIBridge = new SerialComMCHPSIOJNIBridge();
		SerialComMCHPSIOJNIBridge.loadNativeLibrary(libDirectory);
	}
	
	/**
	 * <p></p>
	 * @param
	 * @param
	 */
	public void initMCP2200(int vendorID, int productID) throws IllegalArgumentException {
		if((vendorID < 0) || (vendorID > 0XFFFF)) {
			throw new IllegalArgumentException("initMCP2200(), " + "Argument vendorID can not be negative or greater tha 0xFFFF");
		}
		if((productID < 0) || (productID > 0XFFFF)) {
			throw new IllegalArgumentException("initMCP2200(), " + "Argument productID can not be negative or greater tha 0xFFFF");
		}
		SerialComRetStatus retStatus = new SerialComRetStatus(1);
		int ret = mSerialComMCHPSIOJNIBridge.initMCP2200(vendorID, productID, retStatus);
		if(ret < 0) {
			
		}
	}
	
	/**
	 * <p></p>
	 * @return 
	 */
	public boolean isConnected() throws SerialComException {
		return false;
	}
	
	/**
	 * <p></p>
	 * @return 
	 */
	public boolean configureMCP2200(short ioMap, long baudRateParam, int rxLEDMode, int txLEDMode, boolean flow,
			                          boolean uload, boolean sspnd, boolean invert) throws SerialComException {
		return false;
	}
	
	/**
	 * <p></p>
	 * @param 
	 * @return 
	 */
	public boolean setPin(int pin) throws SerialComException {
		return false;
	}
	
	/**
	 * <p></p>
	 * @param 
	 * @return 
	 */
	public boolean clearPin(int pin) throws SerialComException {
		return false;
	}
	
	/**
	 * <p></p>
	 * @param 
	 * @return 
	 */
	public int readPinValue(int pin) throws SerialComException {
		return 0;
	}
	
	/**
	 * <p></p>
	 * @param 
	 * @return 
	 */
	public boolean readPin(int pin) throws SerialComException {
		return false;
	}
	
	/**
	 * <p></p>
	 * @param 
	 * @return 
	 */
	public boolean writePort(int portValue) throws SerialComException {
		return false;
	}
	
	/**
	 * <p></p>
	 * @param 
	 * @return 
	 */
	public boolean readPort(int pin) throws SerialComException {
		return false;
	}
	
	/**
	 * <p></p>
	 * @param 
	 * @return 
	 */
	public int readPortValue(int pin) throws SerialComException {
		return 0;
	}
	
	/**
	 * <p></p>
	 * @param 
	 * @return 
	 */
	public int selectDevice(int uiDeviceNumber) throws SerialComException {
		return 0;
	}
	
	/**
	 * <p></p>
	 * @param 
	 * @return 
	 */
	public int getSelectedDevice() throws SerialComException {
		return 0;
	}
	
	/**
	 * <p></p>
	 * @param 
	 * @return 
	 */
	public int getNumOfDevices() throws SerialComException {
		return 0;
	}
	
	/**
	 * <p></p>
	 * @param 
	 * @return 
	 */
	public void getSelectedDeviceInfo() throws SerialComException {

	}
	
	/**
	 * <p></p>
	 * @param 
	 * @return 
	 */
	public int readEEPROM(int uiEEPAddress) throws SerialComException {
		return 0;
	}
	
	/**
	 * <p></p>
	 * @param 
	 * @return 
	 */
	public int writeEEPROM(int uiEEPAddress, short ucValue) throws SerialComException {
		return 0;
	}
	
	/**
	 * <p></p>
	 * @param 
	 * @return 
	 */
	public boolean fnRxLED(int mode) throws SerialComException {
		return false;
	}
	
	/**
	 * <p></p>
	 * @param 
	 * @return 
	 */
	public boolean fnTxLED(int mode) throws SerialComException {
		return false;
	}
	
	/**
	 * <p></p>
	 * @param 
	 * @return 
	 */
	public boolean hardwareFlowControl(int onOff) throws SerialComException {
		return false;
	}
	
	/**
	 * <p></p>
	 * @param 
	 * @return 
	 */
	public boolean fnULoad(int onOff) throws SerialComException {
		return false;
	}
	
	/**
	 * <p></p>
	 * @param 
	 * @return 
	 */
	public boolean fnSuspend(int onOff) throws SerialComException {
		return false;
	}
	
	/**
	 * <p></p>
	 * @param 
	 * @return 
	 */
	public boolean fnInvertUartPol(int onOff) throws SerialComException {
		return false;
	}
	
	/**
	 * <p></p>
	 * @param 
	 * @return 
	 */
	public boolean fnSetBaudRate(long baudRateParam) throws SerialComException {
		return false;
	}
	
	/**
	 * <p></p>
	 * @param 
	 * @return 
	 */
	public boolean configureIO(short ioMap) throws SerialComException {
		return false;
	}
	
	/**
	 * <p></p>
	 * @param 
	 * @return 
	 */
	public boolean configureIoDefaultOutput(short ioMap, short ucDefValue) throws SerialComException {
		return false;
	}

}
