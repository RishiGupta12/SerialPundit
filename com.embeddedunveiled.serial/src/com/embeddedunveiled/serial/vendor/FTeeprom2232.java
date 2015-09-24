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

/**
 * <p>Represents the C structure 'ft_eeprom_2232' declared in ftd2xx.h header file.</p>
 * 
 * @author Rishi Gupta
 */
public final class FTeeprom2232 extends FTeepromHeader {

	/* same array is shared between super class and sub class, they extract values 
	 * from index applicable to them. */
	private final int[] data2232;

	/**
	 * <p>Construct and allocates a new FTeeprom2232 object with given details.</p>
	 * 
	 * <p>The sequence of values must be defined in following order: deviceType, VendorId, ProductId, 
	 * SerNumEnable, MaxPower, SelfPowered, RemoteWakeup, PullDownEnable, AIsHighCurrent, BIsHighCurrent, 
	 * AIsFifo, AIsFifoTar, AIsFastSer, BIsFifo, BIsFifoTar, BIsFastSer, ADriverType, BDriverType 
	 * respectively.</p>
	 * 
	 * <p>Values should be initialized when instantiating this object for writing purpose. For reading 
	 * purpose there is no need to initialize.</p>
	 * 
	 * <p>If the class is instantiated for writing purpose, the size of data array must be 18 and each 
	 * member (value at index) must be initialized to a valid value. Although all members are of 'int 
	 * data type' but deviceType must be one of the constants FT_DEVICE_XXXX, VendorId and ProductId must 
	 * use lower 16 bit only, rest all the members should be 8 bit value (upper 24 bits must be all 0).</p>
	 * 
	 * @param data array containing values of member variables for C structure.
	 * @throws IllegalArgumentException if data is null or its size is not equal to 18.
	 */
	public FTeeprom2232(int[] data) {
		super(data);
		if(data == null) {
			throw new IllegalArgumentException("Argument data can not be null !");
		}
		if(data.length != 18) {
			throw new IllegalArgumentException("Argument data must be of length 18 !");
		}
		this.data2232 = data;
	}

	/**
	 * <p>Retrieves the value of AIsHighCurrent variable (drive options) in ft_eeprom_2232 structure.</p>
	 * 
	 * @return non-zero if interface is high current.
	 */
	public int getAIsHighCurrent() {
		return data2232[8];
	}

	/**
	 * <p>Retrieves the value of BIsHighCurrent variable (drive options) in ft_eeprom_2232 structure.</p>
	 * 
	 * @return non-zero if interface is high current.
	 */
	public int getBIsHighCurrent() {
		return data2232[9];
	}

	/**
	 * <p>Retrieves the value of AIsFifo variable (hardware options) in ft_eeprom_2232 structure.</p>
	 * 
	 * @return non-zero if interface is 245 FIFO.
	 */
	public int getAIsFifo() {
		return data2232[10];
	}

	/**
	 * <p>Retrieves the value of AIsFifoTar variable (hardware options) in ft_eeprom_2232 structure.</p>
	 * 
	 * @return non-zero if interface is 245 FIFO CPU target.
	 */
	public int getAIsFifoTar() {
		return data2232[11];
	}

	/**
	 * <p>Retrieves the value of AIsFifo variable (hardware options) in ft_eeprom_2232 structure.</p>
	 * 
	 * @return non-zero if interface is Fast serial.
	 */
	public int getAIsFastSer() {
		return data2232[12];
	}

	/**
	 * <p>Retrieves the value of BIsFifo variable (hardware options) in ft_eeprom_2232 structure.</p>
	 * 
	 * @return non-zero if interface is 245 FIFO.
	 */
	public int getBIsFifo() {
		return data2232[13];
	}

	/**
	 * <p>Retrieves the value of BIsFifoTar variable (hardware options) in ft_eeprom_2232 structure.</p>
	 * 
	 * @return non-zero if interface is 245 FIFO CPU target.
	 */
	public int getBIsFifoTar() {
		return data2232[14];
	}

	/**
	 * <p>Retrieves the value of AIsFifo variable (hardware options) in ft_eeprom_2232 structure.</p>
	 * 
	 * @return non-zero if interface is Fast serial.
	 */
	public int getBIsFastSer() {
		return data2232[15];
	}

	/**
	 * <p>Retrieves the value of ADriverType variable (driver options) in ft_eeprom_2232 structure.</p>
	 * 
	 * @return driver type.
	 */
	public int getADriverType() {
		return data2232[16];
	}

	/**
	 * <p>Retrieves the value of BDriverType variable (driver options) in ft_eeprom_2232 structure.</p>
	 * 
	 * @return driver type.
	 */
	public int getBDriverType() {
		return data2232[17];
	}

	/**
	 * <p>Retrieves all the value defined for ft_eeprom_header and ft_eeprom_2232 structures.</p>
	 * 
	 * @return array of member's values.
	 */
	public int[] getAllMembers() {
		return data2232;
	}
}
