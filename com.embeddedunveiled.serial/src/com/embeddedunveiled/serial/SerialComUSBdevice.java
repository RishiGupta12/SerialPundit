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
 * <p>This class represents a USB device with information about it.</p>
 */
public final class SerialComUSBdevice {
	
	private String idVendor = null;
	private String idProduct = null;
	private String serial = null;
	private String product = null;
	private String manufacturer = null;

	public SerialComUSBdevice(String idVendor, String idProduct, String serial, String product, String manufacturer) {
		this.idVendor = idVendor;
		this.idProduct = idProduct;
		this.serial = serial;
		this.product = product;
		this.manufacturer = manufacturer;
	}
	
	/** 
	 * <p>Retrieves the vendor id of the USB device</p>
	 * @return vendor id of the USB device
	 */
	public int getVendorId() {
		if("---".equals(idVendor)) {
			return -1;
		}
		return (int) SerialComUtil.hexStrToLongNumber(idVendor);
	}
	
	/** 
	 * <p>Retrieves the product id of the USB device</p>
	 * @return product id of the USB device
	 */
	public int getProductId() {
		if("---".equals(idProduct)) {
			return -1;
		}
		return (int) SerialComUtil.hexStrToLongNumber(idProduct);
	}

	/** 
	 * <p>Retrieves the serial number string of the USB device</p>
	 * @return serial number string of the USB device
	 */
	public String getSerialNumber() {
		return serial;
	}
	
	/** 
	 * <p>Retrieves the product string of the USB device</p>
	 * @return serial number string of the USB device
	 */
	public String getProductString() {
		return product;
	}
	
	/** 
	 * <p>Retrieves the manufacturer string of the USB device</p>
	 * @return serial number string of the USB device
	 */
	public String getManufacturerString() {
		return manufacturer;
	}
	
	/** 
	 * <p>Prints information about this device on console.</p>
	 */
	public void dumpDeviceInfo() {
		System.out.println("\nVendor id : 0x" + idVendor + 
				            "\nProduct id : 0x" + idProduct + 
				            "\nSerial number : " + serial + 
				            "\nProduct : " + product + 
				            "\nManufacturer : " + manufacturer);
	}
	
}
