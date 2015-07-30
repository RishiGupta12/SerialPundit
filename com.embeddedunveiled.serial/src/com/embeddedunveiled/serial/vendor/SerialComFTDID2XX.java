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
import java.io.FileNotFoundException;

import com.embeddedunveiled.serial.SerialComException;
import com.embeddedunveiled.serial.SerialComLoadException;
import com.embeddedunveiled.serial.SerialComUnexpectedException;
import com.embeddedunveiled.serial.internal.SerialComFTDID2XXJNIBridge;
import com.embeddedunveiled.serial.internal.SerialComSystemProperty;

/**
 * <p>FTDI provides two interfaces for their USB-UART ICs; first interface is Virtual COM port and 
 * second is provided via a proprietary DLL (a.k.a. D2XX). The D2XX interface provides special 
 * functions that are not available in standard operating system COM port APIs, such as setting the 
 * device into a different mode or writing data into the device EEPROM.</p>
 * 
 * <p>Using this interface requires FTDI drivers to be installed. For Windows FTDI provides CDM package.
 * For Linux and Mac os x, FTDI drivers are provided but default drivers need to be un-installed.</p>
 * 
 * <p>More information about D2XX is here : http://www.ftdichip.com/Drivers/D2XX.htm </p>
 * 
 * <p>Developers are advised to check with vendor library documentation if a particular function is supported
 * for desired platform or not and also how does a particular API will behave. For FTDI d2xx the API guide is 
 * here : http://www.ftdichip.com/Support/Documents/ProgramGuides/D2XX_Programmer's_Guide(FT_000071).pdf</p>
 */
public final class SerialComFTDID2XX extends SerialComVendorLib {

	private final SerialComFTDID2XXJNIBridge mFTDID2XXJNIBridge;

	/**
	 * <p>Allocates a new SerialComFTDID2XX object and extract and load shared libraries as required.</p>
	 * 
	 * @param libDirectory directory in which native library will be extracted and vendor library will be found
	 * @param vlibName name of vendor library to load and link
	 * @param cpuArch architecture of CPU this library is running on
	 * @param osType operating system this library is running on
	 * @param serialComSystemProperty instance of SerialComSystemProperty to get required java properties
	 * @throws SerialComUnexpectedException if a critical java system property is null in system
	 * @throws SecurityException if any java system property can not be accessed
	 * @throws FileNotFoundException if the vendor library file is not found
	 * @throws SerialComLoadException if any file system related issue occurs
	 * @throws UnsatisfiedLinkError if loading/linking shared library fails
	 * @throws SerialComException if initializing native library fails
	 */
	public SerialComFTDID2XX(File libDirectory, String vlibName, int cpuArch, int osType, SerialComSystemProperty serialComSystemProperty) 
			throws UnsatisfiedLinkError, SerialComLoadException, SerialComUnexpectedException, SecurityException, FileNotFoundException {
		mFTDID2XXJNIBridge = new SerialComFTDID2XXJNIBridge();
		SerialComFTDID2XXJNIBridge.loadNativeLibrary(libDirectory, vlibName, cpuArch, osType, serialComSystemProperty);
	}

	// D2XX Classic Functions
	
	/**
	 * <p>Executes FT_SetVIDPID function of D2XX library.</p>
	 * 
	 * <p>By default, the d2xx driver will support a limited set of VID and PID matched devices 
	 * (VID 0x0403 with PIDs 0x6001, 0x6010, 0x6006 only). In order to use the driver with 
	 * other VID and PID combinations this method should be called so that the driver can 
	 * update its internal device list table.</p>
	 * 
	 * @param vid USB-IF vendor id of the USB device
	 * @param pid product id of the USB device
	 * @return true if requested operation was successful
	 * @throws SerialComException if an I/O error occurs
	 * @throws IllegalArgumentException if vid or pid is negative or invalid number
	 */
	public boolean setVidPid(int vid, int pid) throws SerialComException {
		if((vid < 0) || (vid > 0XFFFF)) {
			throw new IllegalArgumentException("Argument vid can not be negative or greater tha 0xFFFF !");
		}
		if((pid < 0) || (pid > 0XFFFF)) {
			throw new IllegalArgumentException("Argument pid can not be negative or greater tha 0xFFFF !");
		}
		int ret = mFTDID2XXJNIBridge.setVidPid(vid, pid);
		if(ret < 0) { /* extra check */
			throw new SerialComException("Could not set the VID and PID combination. Please retry !");
		}
		return true;
	}
	
	/**
	 * <p>Executes FT_GetVIDPID function of D2XX library.</p>
	 * 
	 * <p>Retrieves the current VID and PID combination from within the internal device 
	 * list table. The sequence of return array is USB VID and USB PID.</p>
	 * 
	 * @return USB vid and pid combination from within the internal device list table
	 * @throws SerialComException if an I/O error occurs
	 */
	public int[] getVidPid() throws SerialComException {
		int[] combination = null;
		combination = mFTDID2XXJNIBridge.getVidPid();
		if(combination == null) { /* extra check */
			throw new SerialComException("Could not get the VID and PID values. Please retry !");
		}
		return combination;
	}
	
	/**
	 * <p>Executes FT_CreateDeviceInfoList function of D2XX library.</p>
	 * 
	 * <p>Returns the number of FTDI devices connected to the system presently.
	 * If any device is removed or added to the system this method should be 
	 * called again so that internal list can be updated by driver.</p>
	 * 
	 * @return number of FTDI devices connected to the system at the time this method is called
	 * @throws SerialComException if an I/O error occurs
	 */
	public int createDeviceInfoList() throws SerialComException {
		int ret = mFTDID2XXJNIBridge.createDeviceInfoList();
		if(ret < 0) { /* extra check */
			throw new SerialComException("Could not create device info list. Please retry !");
		}
		return ret;
	}

	/**
	 * <p>Executes FT_GetDeviceInfoList function of D2XX library.</p>
	 * 
	 * <p>Retrieves information about the connected devices and populate them in FTdevicelistInfoNode 
	 * class objects.</p>
	 * 
	 * @param numOfDevices number of FTDI devices connected to system
	 * @return array of device info list (list of FT_DEVICE_LIST_INFO_NODE structure)
	 * @throws SerialComException if an I/O error occurs
	 * @throws IllegalArgumentException if numOfDevices is negative or zero
	 */
	public FTdevicelistInfoNode[] getDeviceInfoList(final int numOfDevices) throws SerialComException {
		int i = 0;
		int numOfDev = 0;
		FTdevicelistInfoNode[] infoList = null;
		String[] rawDataList = null;
		
		if(numOfDevices <= 0) {
			throw new IllegalArgumentException("getDeviceInfoList(), " + "Argument numOfDevices can not be negative or zero !");
		}
		
		rawDataList = mFTDID2XXJNIBridge.getDeviceInfoList(numOfDevices);
		if(rawDataList != null) {
			numOfDev = rawDataList.length / 7;
			infoList = new FTdevicelistInfoNode[numOfDev];
			for(int x=0; x<numOfDev; x++) {
				infoList[x] = new FTdevicelistInfoNode(rawDataList[i], rawDataList[i+1], rawDataList[i+2], 
						            rawDataList[i+3], rawDataList[i+4], rawDataList[i+5], rawDataList[i+6]);
				i = i + 7;
			}
			return infoList;
		}else {
			return new FTdevicelistInfoNode[] { };
		}	
	}
	
	/**
	 * <p>Executes FT_GetDeviceInfoDetail function of D2XX library.</p>
	 * 
	 * <p>Retrieves information about the device at the given index.</p>
	 * 
	 * @param index in list corresponding to the device for which information is to be obtained
	 * @return an object of type FTdevicelistInfoNode containing details of requested device or null
	 * @throws SerialComException if an I/O error occurs
	 * @throws IllegalArgumentException if index is negative
	 */
	public FTdevicelistInfoNode getDeviceInfoDetail(final int index) throws SerialComException {
		if(index < 0) {
			throw new IllegalArgumentException("getDeviceInfoDetail(), " + "Argument index can not be zero !");
		}
		String[] rawData = mFTDID2XXJNIBridge.getDeviceInfoDetail(index);
		if(rawData != null) {
			return new FTdevicelistInfoNode(rawData[0], rawData[1], rawData[2], rawData[3], rawData[4], rawData[5], rawData[6]);
		}else {
			return null;
		}
	}
	
	//TODO FT_ListDevices, DYNAMICALLY GET VALUES OF CONSTANT TO BE PASSED AS ARG
	
	/**
	 * <p>Executes FT_Open function of D2XX library.</p>
	 * 
	 * <p>Open the device and return a handle which will be used for subsequent accesses.</p>
	 * 
	 * @param index in list corresponding to the device that needs to be opened
	 * @return hand;e of the opened device or -1 if method fails
	 * @throws SerialComException if an I/O error occurs
	 * @throws IllegalArgumentException if index is negative
	 */
	public long open(final int index) throws SerialComException {
		if(index < 0) {
			throw new IllegalArgumentException("open(), " + "Argument index can not be zero !");
		}
		long handle = mFTDID2XXJNIBridge.open(index);
		if(handle < 0) {
			throw new SerialComException("Could not open the requested device. Please retry !");
		}else {
			return handle;
		}
	}
	
	//TODO FT_OpenEx
	
	/**
	 * <p>Executes FT_Close function of D2XX library.</p>
	 * 
	 * <p>Closes an open FT device.</p>
	 * 
	 * @param index in list corresponding to the device that needs to be opened
	 * @return handle of the opened device or -1 if method fails
	 * @throws SerialComException if an I/O error occurs
	 */
	public boolean close(final long handle) throws SerialComException {
		int ret = mFTDID2XXJNIBridge.close(handle);
		if(ret < 0) {
			throw new SerialComException("Could not close the requested device. Please retry !");
		}
		
		return true;
	}
	
	/**
	 * <p>Executes FT_Read function of D2XX library.</p>
	 * 
	 * <p>Read data from the device.</p>
	 * 
	 * @param handle handle of the device from which to read data
	 * @param buffer byte buffer where data read will be placed
	 * @param numOfBytesToRead number of bytes to be tried to read
	 * @return number of bytes read
	 * @throws SerialComException if an I/O error occurs
	 * @throws IllegalArgumentException if buffer is null or numOfBytesToRead is negative or zero
	 */
	public int read(long handle, final byte[] buffer, int numOfBytesToRead) throws SerialComException {
		if(buffer == null) {
			throw new IllegalArgumentException("read(), " + "Argument buffer can not be null !");
		}
		if(numOfBytesToRead <= 0) {
			throw new IllegalArgumentException("read(), " + "Argument numOfBytesToRead can not be negative or zero !");
		}
		int ret = mFTDID2XXJNIBridge.read(handle, buffer, numOfBytesToRead);
		if(ret < 0) {
			throw new SerialComException("Could not read the data from the requested device. Please retry !");
		}
		
		return ret;
	}
	
	/**
	 * <p>Executes FT_Write function of D2XX library.</p>
	 * 
	 * <p>Write data from given buffer to the device.</p>
	 * 
	 * @param handle handle of the device to which data is to be sent
	 * @param buffer byte buffer that contains the data to be written to the device
	 * @param numOfBytesToWrite Number of bytes to write to the device.
	 * @return number of bytes written to the device
	 * @throws SerialComException if an I/O error occurs
	 * @throws IllegalArgumentException if buffer is null or numOfBytesToWrite is negative or zero
	 */
	public int write(long handle, final byte[] buffer, int numOfBytesToWrite) throws SerialComException {
		if(buffer == null) {
			throw new IllegalArgumentException("write(), " + "Argument buffer can not be null !");
		}
		if(numOfBytesToWrite <= 0) {
			throw new IllegalArgumentException("write(), " + "Argument numOfBytesToWrite can not be negative or zero !");
		}
		int ret = mFTDID2XXJNIBridge.write(handle, buffer, numOfBytesToWrite);
		if(ret < 0) {
			throw new SerialComException("Could not send data to the requested device. Please retry !");
		}
		
		return ret;
	}
	
	/**
	 * <p>Executes FT_SetBaudRate function of D2XX library.</p>
	 * 
	 * <p>Sets the baud rate value for the given FT device.</p>
	 * 
	 * @param handle handle of the device whose baud rate need to be set
	 * @param baudRate baud rate value to set
	 * @return true if the operation executed successfully
	 * @throws SerialComException if an I/O error occurs
	 * @throws IllegalArgumentException if baudRate is negative
	 */
	public boolean setBaudRate(final long handle, int baudRate) throws SerialComException {
		if(baudRate < 0) {
			throw new IllegalArgumentException("setBaudRate(), " + "Argument baudRate can not be negative !");
		}
		int ret = mFTDID2XXJNIBridge.setBaudRate(handle, baudRate);
		if(ret < 0) {
			throw new SerialComException("Could not set the desired baud rate value for the requested device. Please retry !");
		}
		
		return true;
	}
	
	// EEPROM Programming Interface Functions

	// Extended API Functions

	// FT-Win32 API Functions

}



















