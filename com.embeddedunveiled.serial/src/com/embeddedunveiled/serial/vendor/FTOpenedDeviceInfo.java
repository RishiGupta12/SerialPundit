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

import com.embeddedunveiled.serial.util.SerialComUtil;

/**
 * <p>Represents an opened FT device with information about it.</p>
 * 
 * @author Rishi Gupta
 */
public final class FTOpenedDeviceInfo {

	private String type = null;
	private String id = null;
	private String serialNumber = null;
	private String description = null;

	/**
	 * <p>Construct and allocates a new FTOpenedDeviceInfo object with given details.</p>
	 * 
	 * @param type device type.
	 * @param id device ID.
	 * @param serialNumber serial number of this device.
	 * @param description description of this device.
	 */
	public FTOpenedDeviceInfo(String type, String id, String serialNumber, String description) {
		this.type = type;
		this.id = id;
		this.serialNumber = serialNumber;
		this.description = description;
	}

	/** 
	 * <p>Retrieves the type for this opened FT device.</p>
	 * 
	 * @return type for this FT device info node.
	 * @throws NumberFormatException if the type hex string can not be converted into numerical representation.
	 */
	public long getType() {
		return SerialComUtil.hexStrToLongNumber(type);
	}

	/** 
	 * <p>Retrieves the id for this opened FT device.</p>
	 * 
	 * @return id for this FT device info node.
	 * @throws NumberFormatException if the id hex string can not be converted into numerical representation.
	 */
	public long getId() {
		return SerialComUtil.hexStrToLongNumber(id);
	}

	/** 
	 * <p>Retrieves the serial number string for this opened FT device.</p>
	 * 
	 * @return serial number string for this FT device info node.
	 */
	public String getSerialNumber() {
		return serialNumber;
	}

	/** 
	 * <p>Retrieves the description for this opened FT device.</p>
	 * 
	 * @return description string for this FT device info node.
	 */
	public String getDescription() {
		return description;
	}

	/** 
	 * <p>Prints information about this opened FT device on console.</p>
	 */
	public void dumpDeviceInfo() {
		System.out.println("\nType : " + type + 
				"\nID : 0x" + id + 
				"\nSerialNumber : " + serialNumber + 
				"\nDescription : " + description);
	}
}
