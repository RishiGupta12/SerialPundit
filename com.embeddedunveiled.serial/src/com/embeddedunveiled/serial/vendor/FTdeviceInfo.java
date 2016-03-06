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

import com.embeddedunveiled.serial.SerialComUtil;

/**
 * <p>Represents a device returned by calling listDevices() method in 
 * SerialComFTDID2XX class.</p>
 * 
 * @author Rishi Gupta
 */
public final class FTdeviceInfo {

	private String locId = null;
	private String serialNumber = null;
	private String description = null;

	/**
	 * <p>Construct and allocates a new FTdeviceInfo object with given details.</p>
	 * 
	 * @param locId location ID of the device.
	 * @param serialNumber serial number of this device.
	 * @param description description of this device.
	 */
	public FTdeviceInfo(String locId, String serialNumber, String description) {
		this.locId = locId;
		this.serialNumber = serialNumber;
		this.description = description;
	}

	/** 
	 * <p>Retrieves the locId for this FT device.</p>
	 * 
	 * @return locId for this FT device.
	 * @throws NumberFormatException if the locId hex string can not be converted into numerical 
	 *          representation.
	 */
	public long getLocId() {
		return SerialComUtil.hexStrToLongNumber(locId);
	}

	/** 
	 * <p>Retrieves the serial number string for this FT device.</p>
	 * 
	 * @return serial number string for this FT device.
	 */
	public String getSerialNumber() {
		return serialNumber;
	}

	/** 
	 * <p>Retrieves the description for this FT device.</p>
	 * 
	 * @return description string for this FT device.
	 */
	public String getDescription() {
		return description;
	}

	/** 
	 * <p>Prints information about this FT device on console.</p>
	 */
	public void dumpDeviceInfo() {
		System.out.println("LocID : 0x" + locId + 
				"\nSerialNumber : " + serialNumber + 
				"\nDescription : " + description);
	}
}
