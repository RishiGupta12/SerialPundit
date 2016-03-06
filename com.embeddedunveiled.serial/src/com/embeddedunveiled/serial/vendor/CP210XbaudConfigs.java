/*
 * Author : Rishi Gupta
 * 
 * This file is part of 'serial communication manager' library.
 * Copyright (C) <2014-2016>  <Rishi Gupta>
 *
 * This 'serial communication manager' is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by the Free Software 
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * The 'serial communication manager' is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR 
 * A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with 'serial communication manager'.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.embeddedunveiled.serial.vendor;

/**
 * <p>Represents the C structure 'BAUD_CONFIG' defined in CP210XManufacturingDLL.h header file.</p>
 * 
 * @author Rishi Gupta
 */
public class CP210XbaudConfigs {

	private final int baudGen;
	private final int timer0Reload;
	private final int prescaler;
	private final int baudRate;

	/**
	 * <p>Allocates a new CP210XbaudConfigs object with given details.</p>
	 * 
	 * @param baudGen values of BaudGen member variables for C structure.
	 * @param timer0Reload values of Timer0Reload member variables for C structure.
	 * @param prescaler values of Prescaler member variables for C structure.
	 * @param baudRate values of BaudRate member variables for C structure.
	 */
	public CP210XbaudConfigs(int baudGen, int timer0Reload, int prescaler, int baudRate) {
		this.baudGen = baudGen;
		this.timer0Reload = timer0Reload;
		this.prescaler = prescaler;
		this.baudRate = baudRate;
	}

	/**
	 * <p>Gives value of BaudGen member in the C structure 'BAUD_CONFIG' defined in CP210XManufacturingDLL.h header file.</p>
	 * 
	 * @return value of BaudGen variable.
	 */
	public int getBaudGen() {
		return baudGen;
	}

	/**
	 * <p>Gives value of Timer0Reload member in the C structure 'BAUD_CONFIG' defined in CP210XManufacturingDLL.h header file.</p>
	 * 
	 * @return value of Timer0Reload variable.
	 */
	public int getTimer0Reload() {
		return timer0Reload;
	}

	/**
	 * <p>Gives value of Prescaler member in the C structure 'BAUD_CONFIG' defined in CP210XManufacturingDLL.h header file.</p>
	 * 
	 * @return value of Prescaler variable.
	 */
	public int getPrescaler() {
		return prescaler;
	}

	/**
	 * <p>Gives value of BaudRate member in the C structure 'BAUD_CONFIG' defined in CP210XManufacturingDLL.h header file.</p>
	 * 
	 * @return value of BaudRate variable.
	 */
	public int getBaudRate() {
		return baudRate;
	}

	/** 
	 * <p>Prints information about baudrate on console.</p>
	 */
	public void dumpBaudInfo() {
		System.out.println("BaudGen : " + baudGen + 
				"\nTimer0Reload : " + timer0Reload + 
				"\nPrescaler : " + prescaler + 
				"\nBaudRate : " + baudRate);
	}
}
