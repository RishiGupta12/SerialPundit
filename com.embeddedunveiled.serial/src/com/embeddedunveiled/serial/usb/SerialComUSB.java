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

package com.embeddedunveiled.serial.usb;

import com.embeddedunveiled.serial.SerialComException;
import com.embeddedunveiled.serial.internal.SerialComPortJNIBridge;

/**
 * <p>Encapsulates USB related operations and values.</p>
 * 
 * <p>An end product may be based on dedicated USB-UART bridge IC for providing serial over USB or 
 * may use general purpose microcontroller like PIC18F4550 from Microchip technology Inc. and 
 * program appropriate firmware (USB CDC) into it to provide UART communication over USB port.</p>
 * 
 * @author Rishi Gupta
 */
public final class SerialComUSB {

	/** <p>Value indicating all vendors (vendor neutral operation).</p>*/
	public static final int V_ALL = 0x0000;

	/** <p>Value indicating vendor - Future technology devices international, Ltd. It manufactures FT232 
	 * USB-UART bridge IC.</p>*/
	public static final int V_FTDI = 0x0403;

	/** <p>Value indicating vendor - Silicon Laboratories. It manufactures CP2102 USB-UART bridge IC.</p>*/
	public static final int V_SLABS = 0x10C4;

	/** <p>Value indicating vendor - Microchip technology Inc. It manufactures MCP2200 USB-UART bridge IC.</p>*/
	public static final int V_MCHIP = 0x04D8;

	/** <p>Value indicating vendor - Prolific technology Inc. It manufactures PL2303 USB-UART bridge IC.</p>*/
	public static final int V_PL = 0x067B;

	/** <p>Value indicating vendor - Exar corporation. It manufactures XR21V1410 USB-UART bridge IC.</p>*/
	public static final int V_EXAR = 0x04E2;

	/** <p>Value indicating vendor - Atmel corporation. It manufactures AT90USxxx and other processors which 
	 * can be used as USB-UART bridge.</p>*/
	public static final int V_ATML = 0x03EB;

	/** <p>Value indicating vendor - MosChip semiconductor. It manufactures MCS7810 USB-UART bridge IC.</p>*/
	public static final int V_MOSCHP = 0x9710;

	/** <p>Value indicating vendor - Cypress semiconductor corporation. It manufactures CY7C65213 USB-UART 
	 * bridge IC.</p>*/
	public static final int V_CYPRS = 0x04B4;

	/** <p>Value indicating vendor - Texas instruments, Inc. It manufactures TUSB3410 USB-UART bridge IC.</p>*/
	public static final int V_TI = 0x0451;

	/** <p>Value indicating vendor - WinChipHead. It manufactures CH340 USB-UART bridge IC.</p>*/
	public static final int V_WCH = 0x4348;

	/** <p>Value indicating vendor - QinHeng electronics. It manufactures HL-340 converter product.</p>*/
	public static final int V_QHE = 0x1A86;

	/** <p>Value indicating vendor - NXP semiconductors. It manufactures LPC134x series of microcontrollers.</p>*/
	public static final int V_NXP = 0x1FC9;

	/** <p>Value indicating vendor - Renesas electronics (NEC electronics). It manufactures μPD78F0730 
	 * microcontroller which can be used as USB-UART converter.</p>*/
	public static final int V_RNSAS = 0x0409;

	/** <p>The value indicating that the USB device can have any vendor id and product id. </p>*/
	public static final int DEV_ANY = 0x00;

	/** <p>The value indicating that a USB device has been added into system. </p>*/
	public static final int DEV_ADDED = 0x01;

	/** <p>The value indicating that a USB device has been removed from system. </p>*/
	public static final int DEV_REMOVED  = 0x02;

	SerialComPortJNIBridge mComPortJNIBridge;

	/**
	 * <p>Allocates a new SerialComUSB object.</p>
	 * @param mComPortJNIBridge interface to native library for serial port communication.
	 */
	public SerialComUSB(SerialComPortJNIBridge mComPortJNIBridge) {
		this.mComPortJNIBridge = mComPortJNIBridge;
	}

	/**
	 * <p>Read all the power management related information about a particular USB device. The returned 
	 * instance of SerialComUSBPowerInfo class contains information about auto suspend, selective suspend,
	 * current power status etc.</p>
	 * 
	 * 
	 * @param comPort serial port name/path (COMxx, /dev/ttyUSBx) which is associated with a particular
	 *         USB CDC/ACM interface in the USB device to be analyzed for power management.
	 * @return an instance of SerialComUSBPowerInfo class containing operating system and device specific 
	 *          information about power management or null if given COM port does not belong to a USB 
	 *          device.
	 * @throws SerialComException if an I/O error occurs.
	 */
	public SerialComUSBPowerInfo getCDCUSBDevPowerInfo(String comPort) throws SerialComException {
		if(comPort == null) {
			throw new IllegalArgumentException("Argument comPort can not be null !");
		}
		String portNameVal = comPort.trim();
		if(portNameVal.length() == 0) {
			throw new IllegalArgumentException("Argument comPort can not be empty string !");
		}

		String[] usbPowerInfo = mComPortJNIBridge.getCDCUSBDevPowerInfo(portNameVal);
		if(usbPowerInfo != null) {
			if(usbPowerInfo.length > 2) {
				return new SerialComUSBPowerInfo(usbPowerInfo[0], usbPowerInfo[1], usbPowerInfo[2], 
						usbPowerInfo[3], usbPowerInfo[4], usbPowerInfo[5]);
			}
		}else {
			throw new SerialComException("Could not find USB devices. Please retry !");
		}

		return null;
	}

	/**
	 * <p>Causes re-scan for USB devices. It is equivalent to clicking the "Scan for hardware changes" 
	 * button  in the Device Manager. Only USB hardware is checked for new devices. This can be of use 
	 * when trying to recover devices programmatically.</p>
	 * 
	 * <p>This is applicable to Windows operating system only.</p>
	 * 
	 * @return true on success.
	 * @throws SerialComException if an I/O error occurs.
	 */
	public boolean rescanUSBDevicesHW() throws SerialComException {
		int ret = mComPortJNIBridge.rescanUSBDevicesHW();
		if(ret < 0) {
			throw new SerialComException("Could not cause re-scanning for hardware change. Please retry !");
		}
		return true;
	}

	/**
	 * <p>Sets the latency timer value for FTDI devices. When using FTDI USB-UART devices, optimal values 
	 * of latency timer and read/write block size may be required to obtain optimal data throughput.</p>
	 * 
	 * <p>Note that built-in drivers in Linux kernel image may not allow changing timer values as it may have 
	 * been hard-coded. Drivers supplied by FTDI at their website should be used if changing latency timer 
	 * values is required by application.</p>
	 * 
	 * @return true on success.
	 * @throws SerialComException if an I/O error occurs.
	 */
	public boolean setLatencyTimer(String comPort, byte timerValue) throws SerialComException {
		int ret = mComPortJNIBridge.setLatencyTimer(comPort, timerValue);
		if(ret < 0) {
			throw new SerialComException("Could not set the latency timer value. Please retry !");
		}
		return true;
	}

	/**
	 * <p>Gets the current latency timer value for FTDI devices.</p>
	 * 
	 * @return current latency timer value.
	 * @throws SerialComException if an I/O error occurs.
	 */
	public int getLatencyTimer(String comPort) throws SerialComException {
		int value = mComPortJNIBridge.getLatencyTimer(comPort);
		if(value < 0) {
			throw new SerialComException("Could not get the latency timer value. Please retry !");
		}
		return value;
	}
}
