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
 * <p>Encapsulates USB related operations and values.</p>
 * 
 * <p>An end product may be based on dedicated USB-UART bridge IC for providing serial over USB or 
 * may use general purpose microcontroller like PIC18F4550 from Microchip technology Inc. and 
 * program appropriate firmware (USB CDC) into ti to provide UART communication over USB port.</p>
 */
public class SerialComUSB {
	
	/** <p>Value indicating all vendors (vendor neutral operation).</p>*/
	public static final int V_ALL =  0x0000;
	
	/** <p>Value indicating vendor - Future technology devices international, Ltd. It manufactures FT232 USB-UART bridge IC.</p>*/
	public static final int V_FTDI =  0x0403;
	
	/** <p>Value indicating vendor - Silicon Laboratories. It manufactures CP2102 USB-UART bridge IC.</p>*/
	public static final int V_SLABS = 0x10C4;
	
	/** <p>Value indicating vendor - Microchip technology Inc. It manufactures MCP2200 USB-UART bridge IC.</p>*/
	public static final int V_MCHIP = 0x04D8;
	
	/** <p>Value indicating vendor - Prolific technology Inc. It manufactures PL2303 USB-UART bridge IC.</p>*/
	public static final int V_PL = 0x067B;
	
	/** <p>Value indicating vendor - Exar corporation. It manufactures XR21V1410 USB-UART bridge IC.</p>*/
	public static final int V_EXAR = 0x04E2;
	
	/** <p>Value indicating vendor - Atmel corporation. It manufactures AT90USxxx and other processors which can be used as USB-UART bridge.</p>*/
	public static final int V_ATML =  0x03EB;
	
	/** <p>Value indicating vendor - MosChip semiconductor. It manufactures MCS7810 USB-UART bridge IC.</p>*/
	public static final int V_MOSCHP = 0x9710;
	
	/** <p>Value indicating vendor - Cypress semiconductor corporation. It manufactures CY7C65213 USB-UART bridge IC.</p>*/
	public static final int V_CYPRS = 0x04B4;
	
	/** <p>Value indicating vendor - Texas instruments, Inc. It manufactures TUSB3410 USB-UART bridge IC.</p>*/
	public static final int V_TI = 0x0451;
	
	/** <p>Value indicating vendor - WinChipHead. It manufactures CH340 USB-UART bridge IC.</p>*/
	public static final int V_WCH = 0x4348;
	
	/** <p>Value indicating vendor - QinHeng electronics. It manufactures HL-340 converter product.</p>*/
	public static final int V_QHE = 0x1A86;
	
	/** <p>Value indicating vendor - NXP semiconductors. It manufactures LPC134x series of microcontrollers.</p>*/
	public static final int V_NXP = 0x1FC9;
	
	/** <p>Value indicating vendor - Renesas electronics (NEC electronics). It manufactures μPD78F0730 microcontroller which can be used as USB-UART converter.</p>*/
	public static final int V_RNSAS = 0x0409;
	
	/** <p>The value indicating that the USB device can have any vendor id and product id. </p>*/
	public static final int DEV_ANY = 0x00;
	
	/** <p>The value indicating that a USB device has been added into system. </p>*/
	public static final int DEV_ADDED = 0x01;
	
	/** <p>The value indicating that a USB device has been removed from system. </p>*/
	public static final int DEV_REMOVED  = 0x02;

	/**
	 * <p>Allocates a new SerialComUSB object.</p>
	 */
	public SerialComUSB() {
	}

}
