/*
 * This file is part of SerialPundit.
 * 
 * Copyright (C) 2014-2016, Rishi Gupta. All rights reserved.
 *
 * The SerialPundit is DUAL LICENSED. It is made available under the terms of the GNU Affero 
 * General Public License (AGPL) v3.0 for non-commercial use and under the terms of a commercial 
 * license for commercial use of this software. 
 * 
 * The SerialPundit is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.serialpundit.serial.vendor;

import java.io.File;
import java.io.FileNotFoundException;

import com.serialpundit.core.SerialComSystemProperty;
import com.serialpundit.core.SerialComException;
import com.serialpundit.serial.internal.SerialComCP210xManufacturingJNIBridge;

/**
 * <p>Silicon labs provides libraries to communicate with their USB-UART devices. More information can 
 * be found here : https://www.silabs.com/products/mcu/Pages/USBtoUARTBridgeVCPDrivers.aspx</p>
 * 
 * <ul>
 * <li>The data types used in java layer may be bigger in size than the native layer. For example; if native 
 * function returns 16 bit signed integer, than java method will return 32 bit integer. This is done to make 
 * sure that no data loss occur. SerialPundit takes care of sign and their applicability internally.</li>
 * 
 * <li><p>Developers are requested to check with vendor library documentation if a particular function is supported
 * for desired platform or not and also how does a particular API will behave. Also consider paying attention to 
 * valid values and range when passing arguments to a method.</p></li>
 * 
 * <li>The application note for CP210xManufacturing library is here : 
 * http://www.silabs.com/Support%20Documents/TechnicalDocs/AN721.pdf</li>
 * 
 * <li><p>It seems like CP210xManufacturing library uses user space drivers. So if you encounter any problems 
 * with permissions add the following udev rules file at appropriate location in your system : 
 * <github repository>/tools-and-utilities/99-sp-cp210x.rules</p></li>
 *
 * <li>The udev rules to support various applications designs are here : 
 * <github repository>/tools-and-utilities/99-sp-extra-udev.rules</li>
 *
 * <li><p>Silicon labs softwares can be downloaded from here :
 * http://www.silabs.com/products/Interface/Pages/interface-application-notes.aspx </p></li>
 * 
 * <li>SerialPundit version 1.0.4 is linked to v6.2.0.0 version of CP210xManufacturing library (libcp210xmanufacturing.so, CP210xManufacturing.dll).</li>
 * </ul>
 * 
 * @author Rishi Gupta
 */
public final class SerialComSLabsCP210xManufacturing extends SerialComVendorLib {

    /**<p>Constant representing one of the flag to be used with getProductString() method. </p>*/
    public static final int CP210x_RETURN_SERIAL_NUMBER = 0x00;

    /**<p>Constant representing one of the flag to be used with getProductString() method. </p>*/
    public static final int CP210x_RETURN_DESCRIPTION = 0x01;

    /**<p>Constant representing one of the flag to be used with getProductString() method. </p>*/
    public static final int CP210x_RETURN_FULL_PATH = 0x02;

    /**<p>Constant representing one of the bit in bit mask to be used with setFlushBufferConfig() method. </p>*/
    public static final int FC_OPEN_TX = 0x01;

    /**<p>Constant representing one of the bit in bit mask to be used with setFlushBufferConfig() method. </p>*/
    public static final int FC_OPEN_RX = 0x02;

    /**<p>Constant representing one of the bit in bit mask to be used with setFlushBufferConfig() method. </p>*/
    public static final int FC_CLOSE_TX = 0x04;

    /**<p>Constant representing one of the bit in bit mask to be used with setFlushBufferConfig() method. </p>*/
    public static final int FC_CLOSE_RX = 0x08;

    /**<p>Constant representing one of the bit in bit mask to be used with setFlushBufferConfig() method 
     * mainly for CP2105 devices. </p>*/
    public static final int FC_OPEN_TX_SCI = 0x01;

    /**<p>Constant representing one of the bit in bit mask to be used with setFlushBufferConfig() method 
     * mainly for CP2105 devices. </p>*/
    public static final int FC_OPEN_RX_SCI = 0x02;

    /**<p>Constant representing one of the bit in bit mask to be used with setFlushBufferConfig() method 
     * mainly for CP2105 devices. </p>*/
    public static final int FC_CLOSE_TX_SCI = 0x04;

    /**<p>Constant representing one of the bit in bit mask to be used with setFlushBufferConfig() method 
     * mainly for CP2105 devices. </p>*/
    public static final int FC_CLOSE_RX_SCI = 0x08;

    /**<p>Constant representing one of the bit in bit mask to be used with setFlushBufferConfig() method 
     * mainly for CP2105 devices. </p>*/
    public static final int FC_OPEN_TX_ECI = 0x10;

    /**<p>Constant representing one of the bit in bit mask to be used with setFlushBufferConfig() method 
     * mainly for CP2105 devices. </p>*/
    public static final int FC_OPEN_RX_ECI = 0x20;

    /**<p>Constant representing one of the bit in bit mask to be used with setFlushBufferConfig() method 
     * mainly for CP2105 devices. </p>*/
    public static final int FC_CLOSE_TX_ECI = 0x40;

    /**<p>Constant representing one of the bit in bit mask to be used with setFlushBufferConfig() method 
     * mainly for CP2105 devices. </p>*/
    public static final int FC_CLOSE_RX_ECI = 0x80;

    /**<p>Constant representing one of the bit in bit mask to be used with setFlushBufferConfig() method 
     * mainly for CP2108 devices. </p>*/
    public static final int FC_OPEN_TX_IFC0  = 0x0001;

    /**<p>Constant representing one of the bit in bit mask to be used with setFlushBufferConfig() method 
     * mainly for CP2108 devices. </p>*/
    public static final int FC_OPEN_RX_IFC0  = 0x0002;

    /**<p>Constant representing one of the bit in bit mask to be used with setFlushBufferConfig() method 
     * mainly for CP2108 devices. </p>*/
    public static final int FC_CLOSE_TX_IFC0 = 0x0004;

    /**<p>Constant representing one of the bit in bit mask to be used with setFlushBufferConfig() method 
     * mainly for CP2108 devices. </p>*/
    public static final int FC_CLOSE_RX_IFC0 = 0x0008;

    /**<p>Constant representing one of the bit in bit mask to be used with setFlushBufferConfig() method 
     * mainly for CP2108 devices. </p>*/
    public static final int FC_OPEN_TX_IFC1  = 0x0010;

    /**<p>Constant representing one of the bit in bit mask to be used with setFlushBufferConfig() method 
     * mainly for CP2108 devices. </p>*/
    public static final int FC_OPEN_RX_IFC1  = 0x0020;

    /**<p>Constant representing one of the bit in bit mask to be used with setFlushBufferConfig() method 
     * mainly for CP2108 devices. </p>*/
    public static final int FC_CLOSE_TX_IFC1 = 0x0040;

    /**<p>Constant representing one of the bit in bit mask to be used with setFlushBufferConfig() method 
     * mainly for CP2108 devices. </p>*/
    public static final int FC_CLOSE_RX_IFC1 = 0x0080;

    /**<p>Constant representing one of the bit in bit mask to be used with setFlushBufferConfig() method 
     * mainly for CP2108 devices. </p>*/
    public static final int FC_OPEN_TX_IFC2  = 0x0100;

    /**<p>Constant representing one of the bit in bit mask to be used with setFlushBufferConfig() method 
     * mainly for CP2108 devices. </p>*/
    public static final int FC_OPEN_RX_IFC2  = 0x0200;

    /**<p>Constant representing one of the bit in bit mask to be used with setFlushBufferConfig() method 
     * mainly for CP2108 devices. </p>*/
    public static final int FC_CLOSE_TX_IFC2 = 0x0400;

    /**<p>Constant representing one of the bit in bit mask to be used with setFlushBufferConfig() method 
     * mainly for CP2108 devices. </p>*/
    public static final int FC_CLOSE_RX_IFC2 = 0x0800;

    /**<p>Constant representing one of the bit in bit mask to be used with setFlushBufferConfig() method 
     * mainly for CP2108 devices. </p>*/
    public static final int FC_OPEN_TX_IFC3  = 0x1000;

    /**<p>Constant representing one of the bit in bit mask to be used with setFlushBufferConfig() method 
     * mainly for CP2108 devices. </p>*/
    public static final int FC_OPEN_RX_IFC3  = 0x2000;

    /**<p>Constant representing one of the bit in bit mask to be used with setFlushBufferConfig() method 
     * mainly for CP2108 devices. </p>*/
    public static final int FC_CLOSE_TX_IFC3 = 0x4000;

    /**<p>Constant representing one of the bit in bit mask to be used with setFlushBufferConfig() method 
     * mainly for CP2108 devices. </p>*/
    public static final int FC_CLOSE_RX_IFC3 = 0x8000;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2103/4 devices.</p>*/
    public static final int PORT_RI_ON	= 0x0001;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2103/4 devices.</p>*/
    public static final int PORT_DCD_ON	= 0x0002;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2103/4 devices.</p>*/
    public static final int PORT_DTR_ON	= 0x0004;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2103/4 devices.</p>*/
    public static final int PORT_DSR_ON	= 0x0008;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2103/4 devices.</p>*/
    public static final int PORT_TXD_ON	= 0x0010;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2103/4 devices.</p>*/
    public static final int PORT_RXD_ON	= 0x0020;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2103/4 devices.</p>*/
    public static final int PORT_RTS_ON	= 0x0040;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2103/4 devices.</p>*/
    public static final int PORT_CTS_ON	= 0x0080;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2103/4 devices.</p>*/
    public static final int PORT_GPIO_0_ON = 0x0100;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2103/4 devices.</p>*/
    public static final int PORT_GPIO_1_ON = 0x0200;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2103/4 devices.</p>*/
    public static final int PORT_GPIO_2_ON = 0x0400;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2103/4 devices.</p>*/
    public static final int PORT_GPIO_3_ON = 0x0800;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2103/4 devices.</p>*/
    public static final int PORT_SUSPEND_ON = 0x4000;	// Can't configure latch value

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2103/4 devices.</p>*/
    public static final int PORT_SUSPEND_BAR_ON = 0x8000;	// Can't configure latch value

    /**<p>Constant representing one of the bit locations for EnhancedFxn features to be used mainly for 
     * CP2103/4 devices.</p>*/
    public static final int EF_GPIO_0_TXLED = 0x01;  // Under device control

    /**<p>Constant representing one of the bit locations for EnhancedFxn features to be used mainly for 
     * CP2103/4 devices.</p>*/
    public static final int EF_GPIO_1_RXLED = 0x02;  // Under device control

    /**<p>Constant representing one of the bit locations for EnhancedFxn features to be used mainly for 
     * CP2103/4 devices.</p>*/
    public static final int EF_GPIO_2_RS485 = 0x04;  // Under device control

    /**<p>Constant representing one of the bit locations for EnhancedFxn features to be used mainly for 
     * CP2103/4 devices.</p>*/
    public static final int EF_RS485_INVERT = 0x08;  // RS485 Invert bit

    /**<p>Constant representing one of the bit locations for EnhancedFxn features to be used mainly for 
     * CP2103/4 devices.</p>*/
    public static final int EF_WEAKPULLUP = 0x10;   // Weak Pull-up on

    /**<p>Constant representing one of the bit locations for EnhancedFxn features to be used mainly for 
     * CP2103/4 devices.</p>*/
    public static final int EF_RESERVED_1 = 0x20;  //  Reserved, leave bit 5 cleared

    /**<p>Constant representing one of the bit locations for EnhancedFxn features to be used mainly for 
     * CP2103/4 devices.</p>*/
    public static final int EF_SERIAL_DYNAMIC_SUSPEND  = 0x40;  //  For 8 UART/Modem signals

    /**<p>Constant representing one of the bit locations for EnhancedFxn features to be used mainly for 
     * CP2103/4 devices.</p>*/
    public static final int EF_GPIO_DYNAMIC_SUSPEND = 0x80;	  //  For 4 GPIO signals

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2105 devices.</p>*/
    public static final int PORT_RI_SCI_ON = 0x0001;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2105 devices.</p>*/
    public static final int PORT_DCD_SCI_ON = 0x0002;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2105 devices.</p>*/
    public static final int PORT_DTR_SCI_ON = 0x0004;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2105 devices.</p>*/
    public static final int PORT_DSR_SCI_ON = 0x0008;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2105 devices.</p>*/
    public static final int PORT_TXD_SCI_ON = 0x0010;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2105 devices.</p>*/
    public static final int PORT_RXD_SCI_ON = 0x0020;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2105 devices.</p>*/
    public static final int PORT_RTS_SCI_ON = 0x0040;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2105 devices.</p>*/
    public static final int PORT_CTS_SCI_ON = 0x0080;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2105 devices.</p>*/
    public static final int PORT_GPIO_0_SCI_ON = 0x0002;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2105 devices.</p>*/
    public static final int PORT_GPIO_1_SCI_ON = 0x0004;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2105 devices.</p>*/
    public static final int PORT_GPIO_2_SCI_ON = 0x0008;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2105 devices.</p>*/
    public static final int PORT_SUSPEND_SCI_ON = 0x0001;	//  Can't configure latch value

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2105 devices.</p>*/
    public static final int PORT_RI_ECI_ON = 0x0100;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2105 devices.</p>*/
    public static final int PORT_DCD_ECI_ON = 0x0200;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2105 devices.</p>*/
    public static final int PORT_DTR_ECI_ON = 0x0400;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2105 devices.</p>*/
    public static final int PORT_DSR_ECI_ON = 0x0800;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2105 devices.</p>*/
    public static final int PORT_TXD_ECI_ON = 0x1000;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2105 devices.</p>*/
    public static final int PORT_RXD_ECI_ON = 0x2000;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2105 devices.</p>*/
    public static final int PORT_RTS_ECI_ON = 0x4000;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2105 devices.</p>*/
    public static final int PORT_CTS_ECI_ON = 0x8000;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2105 devices.</p>*/
    public static final int PORT_GPIO_0_ECI_ON = 0x0400;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2105 devices.</p>*/
    public static final int PORT_GPIO_1_ECI_ON = 0x0800;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2105 devices.</p>*/
    public static final int PORT_SUSPEND_ECI_ON = 0x0100;	//  Can't configure latch value

    /**<p>Constant representing one of the bit locations for EnhancedFxn_ECI features to be used mainly 
     * for CP2105 devices.</p>*/
    public static final int EF_GPIO_0_TXLED_ECI = 0x01;	// Under device control

    /**<p>Constant representing one of the bit locations for EnhancedFxn_ECI features to be used mainly 
     * for CP2105 devices.</p>*/
    public static final int EF_GPIO_1_RXLED_ECI = 0x02;	// Under device control

    /**<p>Constant representing one of the bit locations for EnhancedFxn_ECI features to be used mainly 
     * for CP2105 devices.</p>*/
    public static final int EF_GPIO_1_RS485_ECI = 0x04;	// Under device control

    /**<p>Constant representing one of the bit locations for EnhancedFxn_ECI features to be used mainly 
     * for CP2105 devices.</p>*/
    public static final int CP2105_EF_RS485_INVERT = 0x08;	// Under device control

    /**<p>Constant representing one of the bit locations for EnhancedFxn_ECI features to be used mainly 
     * for CP2105 devices.</p>*/
    public static final int EF_INVERT_SUSPEND_ECI = 0x10;	// RS485 Invert bit

    /**<p>Constant representing one of the bit locations for EnhancedFxn_ECI features to be used mainly 
     * for CP2105 devices.</p>*/
    public static final int EF_DYNAMIC_SUSPEND_ECI = 0x40;	// For GPIO signals

    /**<p>Constant representing one of the bit locations for EnhancedFxn_SCI features to be used mainly 
     * for CP2105 devices.</p>*/
    public static final int EF_GPIO_0_TXLED_SCI = 0x01;	// Under device control

    /**<p>Constant representing one of the bit locations for EnhancedFxn_SCI features to be used mainly 
     * for CP2105 devices.</p>*/
    public static final int EF_GPIO_1_RXLED_SCI = 0x02;	// Under device control

    /**<p>Constant representing one of the bit locations for EnhancedFxn_SCI features to be used mainly 
     * for CP2105 devices.</p>*/
    public static final int EF_INVERT_SUSPEND_SCI = 0x10;	// RS485 Invert bit

    /**<p>Constant representing one of the bit locations for EnhancedFxn_SCI features to be used mainly 
     * for CP2105 devices.</p>*/
    public static final int EF_DYNAMIC_SUSPEND_SCI = 0x40;	// For GPIO signals

    /**<p>Constant representing one of the bit locations for EnhancedFxn_Device to be used mainly for 
     * CP2105 devices.</p>*/
    public static final int CP2105_EF_WEAKPULLUP	 = 0x10;	// Weak Pull-up on

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2108 devices.</p>*/
    public static final int PB0_PORT_TX0 = 0X0001;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2108 devices.</p>*/
    public static final int PB0_PORT_RX0 = 0X0002;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2108 devices.</p>*/
    public static final int PB0_PORT_RTS0 = 0x0004;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2108 devices.</p>*/
    public static final int PB0_PORT_CTS0 = 0x0008;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2108 devices.</p>*/
    public static final int PB0_PORT_DTR0 = 0x0010;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2108 devices.</p>*/
    public static final int PB0_PORT_DSR0 = 0x0020;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2108 devices.</p>*/
    public static final int PB0_PORT_DCD0 = 0x0040;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2108 devices.</p>*/
    public static final int PB0_PORT_RI0 = 0X0080;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2108 devices.</p>*/
    public static final int PB0_PORT_TX1 = 0X0100;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2108 devices.</p>*/
    public static final int PB0_PORT_RX1 = 0X0200;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2108 devices.</p>*/
    public static final int PB0_PORT_RTS1 = 0x0400;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2108 devices.</p>*/
    public static final int PB0_PORT_CTS1 = 0x0800;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2108 devices.</p>*/
    public static final int PB0_PORT_DTR1 = 0x1000;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2108 devices.</p>*/
    public static final int PB0_PORT_DSR1 = 0x2000;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2108 devices.</p>*/
    public static final int PB0_PORT_DCD1 = 0x4000;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2108 devices.</p>*/
    public static final int PB0_PORT_RI1 = 0X8000;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2108 devices.</p>*/
    public static final int PB1_PORT_GPIO_0 = 0x0001;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2108 devices.</p>*/
    public static final int PB1_PORT_GPIO_1 = 0x0002;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2108 devices.</p>*/
    public static final int PB1_PORT_GPIO_2 = 0x0004;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2108 devices.</p>*/
    public static final int PB1_PORT_GPIO_3 = 0x0008;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2108 devices.</p>*/
    public static final int PB1_PORT_GPIO_4 = 0x0010;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2108 devices.</p>*/
    public static final int PB1_PORT_GPIO_5 = 0x0020;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2108 devices.</p>*/
    public static final int PB1_PORT_GPIO_6 = 0x0040;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2108 devices.</p>*/
    public static final int PB1_PORT_GPIO_7 = 0x0080;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2108 devices.</p>*/
    public static final int PB1_PORT_GPIO_8 = 0x0100;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2108 devices.</p>*/
    public static final int PB1_PORT_GPIO_9 = 0x0200;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2108 devices.</p>*/
    public static final int PB1_PORT_GPIO_10 = 0x0400;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2108 devices.</p>*/
    public static final int PB1_PORT_GPIO_11 = 0x0800;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2108 devices.</p>*/
    public static final int PB1_PORT_GPIO_12 = 0x1000;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2108 devices.</p>*/
    public static final int PB1_PORT_GPIO_13 = 0x2000;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2108 devices.</p>*/
    public static final int PB1_PORT_GPIO_14 = 0x4000;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2108 devices.</p>*/
    public static final int PB1_PORT_GPIO_15 = 0x8000;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2108 devices.</p>*/
    public static final int PB2_PORT_SUSPEND = 0x0001;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2108 devices.</p>*/
    public static final int PB2_PORT_SUSPEND_BAR = 0x0002;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2108 devices.</p>*/
    public static final int PB2_PORT_DTR2 = 0x0004;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2108 devices.</p>*/
    public static final int PB2_PORT_DSR2 = 0x0008;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2108 devices.</p>*/
    public static final int PB3_PORT_TX2 = 0x0001;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2108 devices.</p>*/
    public static final int PB3_PORT_RX2 = 0x0002;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2108 devices.</p>*/
    public static final int PB3_PORT_RTS2 = 0x0004;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2108 devices.</p>*/
    public static final int PB3_PORT_CTS2 = 0x0008;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2108 devices.</p>*/
    public static final int PB3_PORT_DCD2 = 0x0010;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2108 devices.</p>*/
    public static final int PB3_PORT_RI2 = 0x0020;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2108 devices.</p>*/
    public static final int PB3_PORT_DTR3 = 0x0040;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2108 devices.</p>*/
    public static final int PB3_PORT_DSR3 = 0x0080;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2108 devices.</p>*/
    public static final int PB3_PORT_DCD3 = 0x0100;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2108 devices.</p>*/
    public static final int PB3_PORT_RI3 = 0x0200;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2108 devices.</p>*/
    public static final int PB4_PORT_RTS3 = 0x0001;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2108 devices.</p>*/
    public static final int PB4_PORT_CTS3 = 0x0002;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2108 devices.</p>*/
    public static final int PB4_PORT_TX3 = 0x0004;

    /**<p>Constant representing one of the bit locations for Mode/Latch for Reset and Suspend features to 
     * be used mainly for CP2108 devices.</p>*/
    public static final int PB4_PORT_RX3 = 0x0008;

    /**<p>Constant representing one of the bit locations for EnhancedFxn_IFCx to be used mainly for CP2108 
     * devices.</p>*/
    public static final int EF_IFC_GPIO_TXLED = 0x01;

    /**<p>Constant representing one of the bit locations for EnhancedFxn_IFCx to be used mainly for CP2108 
     * devices.</p>*/
    public static final int EF_IFC_GPIO_RXLED = 0x02;

    /**<p>Constant representing one of the bit locations for EnhancedFxn_IFCx to be used mainly for CP2108 
     * devices.</p>*/
    public static final int EF_IFC_GPIO_RS485 = 0x04;

    /**<p>Constant representing one of the bit locations for EnhancedFxn_IFCx to be used mainly for CP2108 
     * devices.</p>*/
    public static final int EF_IFC_GPIO_RS485_LOGIC = 0x08;

    /**<p>Constant representing one of the bit locations for EnhancedFxn_IFCx to be used mainly for CP2108 
     * devices.</p>*/
    public static final int EF_IFC_GPIO_CLOCK = 0x10;

    /**<p>Constant representing one of the bit locations for EnhancedFxn_IFCx to be used mainly for CP2108 
     * devices.</p>*/
    public static final int EF_IFC_DYNAMIC_SUSPEND  = 0x40;

    /**<p>Constant representing one of the bit locations for EnhancedFxn_Device to be used mainly for CP2108 
     * devices.</p>*/
    public static final int EF_DEVICE_WEAKPULLUP_RESET = 0x10;

    /**<p>Constant representing one of the bit locations for EnhancedFxn_Device to be used mainly for CP2108 
     * devices.</p>*/
    public static final int EF_DEVICE_WEAKPULLUP_SUSPEND = 0x20;

    /**<p>Constant representing one of the bit locations for EnhancedFxn_Device to be used mainly for CP2108 
     * devices.</p>*/
    public static final int EF_DEVICE_DYNAMIC_SUSPEND = 0x40;

    private final SerialComCP210xManufacturingJNIBridge mSerialComCP210xManufacturingJNIBridge;

    /**
     * <p>Allocates a new SerialComSLabsCP210xManufacturing object and extract and load shared libraries as 
     * required.</p>
     * 
     * @param libDirectory directory in which native library will be extracted and vendor library will be found.
     * @param vlibName name of vendor library to load and link.
     * @param cpuArch architecture of CPU this library is running on.
     * @param osType operating system this library is running on.
     * @param serialComSystemProperty instance of SerialComSystemProperty to get required java properties.
     * @throws SerialComException if java system properties can not be is null, if any file system related issue occurs.
     * @throws SecurityException if java system properties can not be  accessed or required files can not be accessed.
     * @throws UnsatisfiedLinkError if loading/linking shared library fails.
     * @throws FileNotFoundException if the vendor library file is not found.
     */
    public SerialComSLabsCP210xManufacturing(File libDirectory, String vlibName, int cpuArch, int osType, 
            SerialComSystemProperty serialComSystemProperty) throws SerialComException {

        mSerialComCP210xManufacturingJNIBridge = new SerialComCP210xManufacturingJNIBridge();
        SerialComCP210xManufacturingJNIBridge.loadNativeLibrary(libDirectory, vlibName, cpuArch, osType, serialComSystemProperty);
    }

    /**
     * <p>Executes CP210x_GetNumDevices function of CP210xManufacturing library.</p>
     * 
     * <p>Returns the number of CP210x devices connected to the host.</p>
     * 
     * @return number of the CP210X devices connected to host system presently.
     * @throws SerialComException if an I/O error occurs.
     */
    public int getNumDevices() throws SerialComException {
        int ret = mSerialComCP210xManufacturingJNIBridge.getNumDevices();
        if(ret < 0) {
            throw new SerialComException("Could not get the number of devices connected to host. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes CP210x_GetProductString function of CP210xManufacturing library.</p>
     * 
     * <p>Returns product description, serial number or full path based on flag passed.</p>
     * 
     * <p>The argument flag can be one of the constant CP210x_RETURN_SERIAL_NUMBER, 
     * CP210x_RETURN_DESCRIPTION or CP210x_RETURN_FULL_PATH.</p>
     * 
     * @param index index of device in list.
     * @param flag indicates which property is to be fetched.
     * @return product description, serial number or full path.
     * @throws SerialComException if an I/O error occurs.
     * @throws IllegalArgumentException if invalid flag is passed.
     */
    public String getProductString(int index, int flag) throws SerialComException {
        String ret = null;
        if((flag == CP210x_RETURN_FULL_PATH) || (flag == CP210x_RETURN_DESCRIPTION) 
                || (flag == CP210x_RETURN_SERIAL_NUMBER)) {
            ret = mSerialComCP210xManufacturingJNIBridge.getProductString(index, flag);
            if(ret == null) {
                throw new SerialComException("Could not get the requested information. Please retry !");
            }
            return ret;
        }

        throw new IllegalArgumentException("Invalid flag passed for requested operation !");
    }

    /**
     * <p>Executes CP210x_Open function of of CP210xManufacturing library.</p>
     * 
     * <p>Open the device and return a handle which will be used for subsequent accesses.</p>
     * 
     * @param index of the device that needs to be opened.
     * @return handle of the opened device or -1 if method fails.
     * @throws SerialComException if an I/O error occurs.
     * @throws IllegalArgumentException if index is negative.
     */
    public long open(final int index) throws SerialComException {
        if(index < 0) {
            throw new IllegalArgumentException("Argument index can not be negative !");
        }
        long handle = mSerialComCP210xManufacturingJNIBridge.open(index);
        if(handle < 0) {
            throw new SerialComException("Could not open the requested device at given index. Please retry !");
        }else {
            return handle;
        }
    }

    /**
     * <p>Executes CP210x_Close function of of CP210xManufacturing library.</p>
     * 
     * <p>Closes an opened cp210x device.</p>
     * 
     * @param handle of the device that is to be close.
     * @return true on success.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean close(final long handle) throws SerialComException {
        int ret = mSerialComCP210xManufacturingJNIBridge.close(handle);
        if(ret < 0) {
            throw new SerialComException("Could not close the requested device. Please retry !");
        }

        return true;
    }

    /**
     * <p>Executes CP210x_GetPartNumber function of CP210xManufacturing library.</p>
     * 
     * <p>Returns the part number associated with the given handle.</p>
     * 
     * @return part number associated with the given handle.
     * @throws SerialComException if an I/O error occurs.
     */
    public String getPartNumber(long handle) throws SerialComException {
        String ret = mSerialComCP210xManufacturingJNIBridge.getPartNumber(handle);
        if(ret == null) {
            throw new SerialComException("Could not get the part number. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes CP210x_SetVid function of of CP210xManufacturing library.</p>
     * 
     * <p>Sets the 2-byte Vendor ID field of the Device Descriptor of a CP210x device.</p>
     * 
     * @param handle of the device.
     * @param vid 16 bit Vendor ID.
     * @return true on success.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean setVid(final long handle, int vid) throws SerialComException {
        int ret = mSerialComCP210xManufacturingJNIBridge.setVid(handle, vid);
        if(ret < 0) {
            throw new SerialComException("Could not set the USB VID. Please retry !");
        }

        return true;
    }

    /**
     * <p>Executes CP210x_SetPid function of of CP210xManufacturing library.</p>
     * 
     * <p>Sets the 2-byte Product ID field of the Device Descriptor of a CP210x device.</p>
     * 
     * @param handle of the device.
     * @param pid 16 bit Product ID.
     * @return true on success.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean setPid(final long handle, int pid) throws SerialComException {
        int ret = mSerialComCP210xManufacturingJNIBridge.setPid(handle, pid);
        if(ret < 0) {
            throw new SerialComException("Could not set the USB PID. Please retry !");
        }

        return true;
    }

    /**
     * <p>Executes CP210x_SetProductString function of of CP210xManufacturing library.</p>
     * 
     * <p>Sets the Product Description String of the String Descriptor of a CP210x device.</p>
     * 
     * @param handle of the device.
     * @param product string that need to be saved in device.
     * @return true on success.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean setProductString(final long handle, String product) throws SerialComException {

        int ret = mSerialComCP210xManufacturingJNIBridge.setProductString(handle, product);
        if(ret < 0) {
            throw new SerialComException("Could not set description for the product. Please retry !");
        }

        return true;
    }

    /**
     * <p>Executes CP210x_SetSerialNumber function of of CP210xManufacturing library.</p>
     * 
     * <p>Sets the Serial Number String of the String Descriptor of a CP210x device.</p>
     * 
     * @param handle of the device.
     * @param serialNumber string that need to be saved in device.
     * @return true on success.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean setSerialNumber(final long handle, String serialNumber) throws SerialComException {

        int ret = mSerialComCP210xManufacturingJNIBridge.setSerialNumber(handle, serialNumber);
        if(ret < 0) {
            throw new SerialComException("Could not set serial number for the product. Please retry !");
        }

        return true;
    }

    /**
     * <p>Executes CP210x_SetInterfaceString function of of CP210xManufacturing library.</p>
     * 
     * <p>Sets the Serial Number String of the String Descriptor of a CP210x device.</p>
     * 
     * @param handle of the device.
     * @param bInterfaceNumber Set to 0 for Enhanced Interface String, or 1 for Standard Interface String 
     *         on the CP2105. 0-3 for the CP2108 which has 4 interfaces.
     * @param interfaceString interface string to be set on device.
     * @return true on success.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean setInterfaceString(long handle, byte bInterfaceNumber, String interfaceString) 
            throws SerialComException {

        int ret = mSerialComCP210xManufacturingJNIBridge.setInterfaceString(handle, bInterfaceNumber, interfaceString);
        if(ret < 0) {
            throw new SerialComException("Could not set interface string. Please retry !");
        }

        return true;
    }

    /**
     * <p>Executes CP210x_SetSelfPower function of of CP210xManufacturing library.</p>
     * 
     * <p>Sets or clears the Self-Powered bit of the Power Attributes field of the Configuration Descriptor of a CP210x device.</p>
     * 
     * @param handle of the device.
     * @param selfPower if true self power bit will be set, if false self power bit will be cleared.
     * @return true on success.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean setSelfPower(final long handle, boolean selfPower) throws SerialComException {

        int ret = mSerialComCP210xManufacturingJNIBridge.setSelfPower(handle, selfPower);
        if(ret < 0) {
            throw new SerialComException("Could not set/clear Self-Powered bit for the product. Please retry !");
        }

        return true;
    }

    /**
     * <p>Executes CP210x_SetMaxPower function of of CP210xManufacturing library.</p>
     * 
     * <p>Sets the Max Power field of the Configuration Descriptor of a CP210x device.</p>
     * 
     * @param handle of the device.
     * @param maxPower 1-byte value representing the maximum power consumption of the CP210x USB device, 
     *         expressed in 2 mA units.
     * @return true on success.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean setMaxPower(final long handle, byte maxPower) throws SerialComException {

        int ret = mSerialComCP210xManufacturingJNIBridge.setMaxPower(handle, maxPower);
        if(ret < 0) {
            throw new SerialComException("Could not set the max power field for the product. Please retry !");
        }

        return true;
    }

    /**
     * <p>Executes CP210x_SetFlushBufferConfig function of of CP210xManufacturing library.</p>
     * 
     * <p>Sets the Flush Buffer configuration of a CP210x device.</p>
     * 
     * <p>The argument flag can be bit mask of constants FC_OPEN_TX, FC_OPEN_RX, FC_CLOSE_TX, FC_CLOSE_RX. 
     * For CP2105 bit mask should be FC_OPEN_TX_SCI, FC_OPEN_RX_SCI, FC_CLOSE_TX_SCI, FC_CLOSE_RX_SCI, or 
     * FC_OPEN_TX_ECI, FC_OPEN_RX_ECI, FC_CLOSE_TX_ECI, FC_CLOSE_RX_ECI. For CP2108 bit mask should be of 
     * FC_OPEN_TX_IFC0, FC_OPEN_RX_IFC0, FC_CLOSE_TX_IFC0, FC_CLOSE_RX_IFC0, FC_OPEN_TX_IFC1, FC_OPEN_RX_IFC1, 
     * FC_CLOSE_TX_IFC1, FC_CLOSE_RX_IFC1, FC_OPEN_TX_IFC2, FC_OPEN_RX_IFC2, FC_CLOSE_TX_IFC2, FC_CLOSE_RX_IFC2, 
     * FC_OPEN_TX_IFC3, FC_OPEN_RX_IFC3, FC_CLOSE_TX_IFC3, FC_CLOSE_RX_IFC3.</p>
     * 
     * @param handle of the device.
     * @param flag bit mask indicating which buffer to flush and upon which event.
     * @return true on success.
     * @throws SerialComException if an I/O error occurs.
     * @throws IllegalArgumentException if invalid flag is passed.
     */
    public boolean setFlushBufferConfig(final long handle, int flag) throws SerialComException {
        if(flag > 0x0F) {
            throw new IllegalArgumentException("Invalid flag passed for the requested operation !");
        }
        int ret = mSerialComCP210xManufacturingJNIBridge.setFlushBufferConfig(handle, flag);
        if(ret < 0) {
            throw new SerialComException("Could not set the flushing configuration for the product. Please retry !");
        }

        return true;
    }

    /**
     * <p>Executes CP210x_SetDeviceMode function of of CP210xManufacturing library.</p>
     * 
     * <p>Sets the operating mode (GPIO or Modem) or each Interface of a CP210x device.</p>
     * 
     * <p>Devices like CP2105 have more than one serial port (interface). Each serial port can be operated 
     * in two modes modem and GPIO respectively. This method can be used to set these modes.</p>
     * 
     * @param handle of the device.
     * @param bDeviceModeECI set to 0 for modem mode for Enhanced interface. Set to 1 for GPIO mode.
     * @param bDeviceModeSCI set to 0 for modem mode for Enhanced interface. Set to 1 for GPIO mode.
     * @return true on success.
     * @throws SerialComException if an I/O error occurs.
     * @throws IllegalArgumentException if invalid flag is passed.
     */
    public boolean setDeviceMode(final long handle, byte bDeviceModeECI, byte bDeviceModeSCI) 
            throws SerialComException {
        int ret = mSerialComCP210xManufacturingJNIBridge.setDeviceMode(handle, bDeviceModeECI, bDeviceModeSCI);
        if(ret < 0) {
            throw new SerialComException("Could not set the device configuration. Please retry !");
        }

        return true;
    }

    /**
     * <p>Executes CP210x_SetDeviceVersion function of of CP210xManufacturing library.</p>
     * 
     * <p>Sets the Device Release Version field of the Device Descriptor of a CP210x device.</p>
     * 
     * @param handle of the device.
     * @param version 2-byte Device Release Version number in Binary-Coded Decimal (BCD) format 
     *         with the upper two nibbles containing the two decimal digits of the major version 
     *         and the lower two nibbles containing the two decimal digits of the minor version.
     * @return true on success.
     * @throws SerialComException if an I/O error occurs.
     * @throws IllegalArgumentException if invalid flag is passed.
     */
    public boolean setDeviceVersion(final long handle, int version) throws SerialComException {
        if(version > 0xFFFF) {
            throw new IllegalArgumentException("Invalid flag passed for the requested operation !");
        }
        int ret = mSerialComCP210xManufacturingJNIBridge.setDeviceVersion(handle, version);
        if(ret < 0) {
            throw new SerialComException("Could not set the device version for the product. Please retry !");
        }

        return true;
    }

    /**
     * <p>Executes CP210x_SetBaudRateConfig function of of CP210xManufacturing library.</p>
     * 
     * <p>Sets the baud rate configuration data of a CP210x device.</p>
     * 
     * @param handle of the device.
     * @param baudGen BaudGen field of BAUD_CONFIG structure defined in CP210XManufacturingDLL.h header file.
     * @param timer0Reload Timer0Reload field of BAUD_CONFIG structure defined in CP210XManufacturingDLL.h header file.
     * @param prescalar Pre-scaler field of BAUD_CONFIG structure defined in CP210XManufacturingDLL.h header file.
     * @param baudrate BaudRate field of BAUD_CONFIG structure defined in CP210XManufacturingDLL.h header file.
     * @return true on success.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean setBaudRateConfig(long handle, int baudGen, int timer0Reload, int prescalar, int baudrate) throws SerialComException {
        int ret = mSerialComCP210xManufacturingJNIBridge.setBaudRateConfig(handle, baudGen, timer0Reload, prescalar, baudrate);
        if(ret < 0) {
            throw new SerialComException("Could not set the baud rate configuration values for the product. Please retry !");
        }

        return true;
    }

    /**
     * <p>Executes CP210x_SetPortConfig function of of CP210xManufacturing library.</p>
     * 
     * <p>Sets the current port pin configuration from the CP210x device.</p>
     * 
     * @param handle of the device.
     * @param mode Mode field of PORT_CONFIG structure defined in CP210XManufacturingDLL.h header file.
     * @param resetLatch Reset_Latch field of PORT_CONFIG structure defined in CP210XManufacturingDLL.h header file.
     * @param suspendLatch Suspend_Latch field of PORT_CONFIG structure defined in CP210XManufacturingDLL.h header file.
     * @param enhancedFxn EnhancedFxn field of PORT_CONFIG structure defined in CP210XManufacturingDLL.h header file.
     * @return true on success.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean setPortConfig(long handle, int mode, int resetLatch, int suspendLatch, int enhancedFxn) throws SerialComException {
        int ret = mSerialComCP210xManufacturingJNIBridge.setPortConfig(handle, mode, resetLatch, suspendLatch, enhancedFxn);
        if(ret < 0) {
            throw new SerialComException("Could not set the port configuration values for the product. Please retry !");
        }

        return true;
    }

    /**
     * <p>Executes CP210x_SetDualPortConfig function of of CP210xManufacturing library.</p>
     * 
     * <p>Sets the port configuration of a CP2105 device.</p>
     * 
     * @param handle of the device.
     * @param mode Mode field of DUAL_PORT_CONFIG structure defined in CP210XManufacturingDLL.h header file.
     * @param resetLatch Reset_Latch field of DUAL_PORT_CONFIG structure defined in CP210XManufacturingDLL.h header file.
     * @param suspendLatch Suspend_Latch field of DUAL_PORT_CONFIG structure defined in CP210XManufacturingDLL.h header file.
     * @param enhancedFxnECI EnhancedFxn_ECI field of DUAL_PORT_CONFIG structure defined in CP210XManufacturingDLL.h header file.
     * @param enhancedFxnSCI EnhancedFxn_SCI field of DUAL_PORT_CONFIG structure defined in CP210XManufacturingDLL.h header file.
     * @param enhancedFxnDevice EnhancedFxn_Device field of DUAL_PORT_CONFIG structure defined in CP210XManufacturingDLL.h header file.
     * @return true on success.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean setDualPortConfig(long handle, int mode, int resetLatch, int suspendLatch, int enhancedFxnECI, 
            int enhancedFxnSCI, int enhancedFxnDevice) throws SerialComException {
        int ret = mSerialComCP210xManufacturingJNIBridge.setDualPortConfig(handle, mode, resetLatch, 
                suspendLatch, enhancedFxnECI, enhancedFxnSCI, enhancedFxnDevice);
        if(ret < 0) {
            throw new SerialComException("Could not set the dual port configuration values for the device. Please retry !");
        }

        return true;
    }

    /**
     * <p>Executes CP210x_SetDualPortConfig function of of CP210xManufacturing library.</p>
     * 
     * <p>Sets the port configuration of a CP2108 device.</p>
     * 
     * <p>The sequence of resetLatch array starting at index 0 is : Mode_PB0, Mode_PB1, Mode_PB2, Mode_PB3,
     * Mode_PB4, LowPower_PB0, LowPower_PB1, LowPower_PB2, LowPower_PB3, LowPower_PB4, Latch_PB0, Latch_PB1,
     * Latch_PB2, Latch_PB3, Latch_PB4.</p>
     * 
     * <p>The sequence of suspendLatch array starting at index 0 is : Mode_PB0, 
     * Mode_PB1, Mode_PB2, Mode_PB3, Mode_PB4, LowPower_PB0, LowPower_PB1, LowPower_PB2, LowPower_PB3, 
     * LowPower_PB4, Latch_PB0, Latch_PB1, Latch_PB2, Latch_PB3, Latch_PB4.</p>
     * 
     * <p>The sequence for config starting at index 0 is : IPDelay_IFC0, IPDelay_IFC1, IPDelay_IFC2, IPDelay_IFC3, 
     * EnhancedFxn_IFC0, EnhancedFxn_IFC1, EnhancedFxn_IFC2, EnhancedFxn_IFC3, EnhancedFxn_Device, ExtClk0Freq, 
     * ExtClk1Freq, ExtClk2Freq, ExtClk3Freq respectively.</p>
     * 
     * @param handle of the device.
     * @param resetLatch array of integers containing info related to QUAD_PORT_STATE structure defined in 
     *         CP210XManufacturingDLL.h header file.
     * @param suspendLatch array of integers containing info related to QUAD_PORT_STATE structure defined 
     *         in CP210XManufacturingDLL.h header file.
     * @param config array of bytes containing info related to QUAD_PORT_CONFIG structure defined in 
     *         CP210XManufacturingDLL.h header file.
     * @return true on success.
     * @throws IllegalArgumentException if length of resetLatch, suspendLatch or config is incorrect.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean setQuadPortConfig(long handle, int[] resetLatch, int[] suspendLatch, byte[] config) throws SerialComException {
        if((resetLatch.length != 15) || (suspendLatch.length != 15)) {
            throw new IllegalArgumentException("Length of array resetLatch and suspendLatch must be 15 !");
        }
        if(config.length != 13) {
            throw new IllegalArgumentException("Length of array config must be 13 !");
        }
        int ret = mSerialComCP210xManufacturingJNIBridge.setQuadPortConfig(handle, resetLatch, suspendLatch, config);
        if(ret < 0) {
            throw new SerialComException("Could not set the dual port configuration values for the device. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes CP210x_SetLockValue function of of CP210xManufacturing library.</p>
     * 
     * <p>Sets the 1-byte lock value of a CP210x device.</p>
     * 
     * @param handle of the device.
     * @return true on success.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean setLockValue(final long handle) throws SerialComException {
        int ret = mSerialComCP210xManufacturingJNIBridge.setLockValue(handle);
        if(ret < 0) {
            throw new SerialComException("Could not set the lock value on the device. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes CP210x_GetDeviceVid function of CP210xManufacturing library.</p>
     * 
     * <p>Returns the 2-byte Vendor ID field of the device descriptor of a CP210x device.</p>
     * 
     * @param handle of the device.
     * @return USB vendor ID of this device.
     * @throws SerialComException if an I/O error occurs.
     */
    public int getDeviceVid(long handle) throws SerialComException {
        int ret = mSerialComCP210xManufacturingJNIBridge.getDeviceVid(handle);
        if(ret < 0) {
            throw new SerialComException("Could not get the device USB VID. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes CP210x_GetDevicePid function of CP210xManufacturing library.</p>
     * 
     * <p>Returns the 2-byte Product ID field of the device descriptor of a CP210x device.</p>
     * 
     * @param handle of the device.
     * @return USB product ID of this device.
     * @throws SerialComException if an I/O error occurs.
     */
    public int getDevicePid(long handle) throws SerialComException {
        int ret = mSerialComCP210xManufacturingJNIBridge.getDevicePid(handle);
        if(ret < 0) {
            throw new SerialComException("Could not get the device USB VID. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes CP210x_GetDeviceManufacturerString function of CP210xManufacturing library.</p>
     * 
     * <p>Returns the manufacturer string of the String Descriptor of a CP210x device.</p>
     * 
     * @param handle of the device.
     * @return manufacturer of the device.
     * @throws SerialComException if an I/O error occurs.
     */
    public String getDeviceManufacturerString(long handle) throws SerialComException {
        String ret = mSerialComCP210xManufacturingJNIBridge.getDeviceManufacturerString(handle);
        if(ret == null) {
            throw new SerialComException("Could not get the manufacturer string. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes CP210x_GetDeviceProductString function of CP210xManufacturing library.</p>
     * 
     * <p>Returns the product description string of the String Descriptor of a CP210x device.</p>
     * 
     * @param handle of the device.
     * @return product description of the device.
     * @throws SerialComException if an I/O error occurs.
     */
    public String getDeviceProductString(long handle) throws SerialComException {
        String ret = mSerialComCP210xManufacturingJNIBridge.getDeviceProductString(handle);
        if(ret == null) {
            throw new SerialComException("Could not get the product description string. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes CP210x_GetDeviceInterfaceString function of CP210xManufacturing library.</p>
     * 
     * <p>Gets the interface string of a CP210x device.</p>
     * 
     * @param handle of the device.
     * @return interface string of the device.
     * @throws SerialComException if an I/O error occurs.
     */
    public String getDeviceInterfaceString(long handle, byte bInterfaceNumber) throws SerialComException {
        String ret = mSerialComCP210xManufacturingJNIBridge.getDeviceInterfaceString(handle, bInterfaceNumber);
        if(ret == null) {
            throw new SerialComException("Could not get the product serial number string. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes CP210x_GetDeviceSerialNumber function of CP210xManufacturing library.</p>
     * 
     * <p>Gets the serial number string of the String Descriptor of a CP210x device.</p>
     * 
     * @param handle of the device.
     * @return serial number of the device.
     * @throws SerialComException if an I/O error occurs.
     */
    public String getDeviceSerialNumber(long handle) throws SerialComException {
        String ret = mSerialComCP210xManufacturingJNIBridge.getDeviceSerialNumber(handle);
        if(ret == null) {
            throw new SerialComException("Could not get the product serial number string. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes CP210x_GetSelfPower function of CP210xManufacturing library.</p>
     * 
     * <p>Returns the state of the Self-Powered bit of the Power Attributes field of the Configuration Descriptor of a CP210x device.</p>
     * 
     * @param handle of the device.
     * @return true if self powered bit is high or false is self power bit is low.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean getSelfPower(long handle) throws SerialComException {
        int ret = mSerialComCP210xManufacturingJNIBridge.getSelfPower(handle);
        if(ret < 0) {
            throw new SerialComException("Could not determine the self powered bit value. Please retry !");
        }
        if(ret == 0) {
            return false;
        }
        return true;
    }

    /**
     * <p>Executes CP210x_GetMaxPower function of CP210xManufacturing library.</p>
     * 
     * <p>Returns the 1-byte Max Power field of the Configuration Descriptor of a CP210x device.</p>
     * 
     * @param handle of the device.
     * @return max power field value.
     * @throws SerialComException if an I/O error occurs.
     */
    public byte getMaxPower(long handle) throws SerialComException {
        int ret = mSerialComCP210xManufacturingJNIBridge.getMaxPower(handle);
        if(ret < 0) {
            throw new SerialComException("Could not determine the max power field value. Please retry !");
        }
        return (byte)ret;
    }

    /**
     * <p>Executes CP210x_GetFlushBufferConfig function of CP210xManufacturing library.</p>
     * 
     * <p>Returns the flush buffer configuration of a CP210x device.</p>
     * 
     * @param handle of the device.
     * @return bit mask of constants FC_OPEN_TX, FC_OPEN_RX, FC_CLOSE_TX, FC_CLOSE_RX.
     * @throws SerialComException if an I/O error occurs.
     */
    public short getFlushBufferConfig(long handle) throws SerialComException {
        int ret = mSerialComCP210xManufacturingJNIBridge.getFlushBufferConfig(handle);
        if(ret < 0) {
            throw new SerialComException("Could not determine flush buffer config. Please retry !");
        }
        return (byte)ret;
    }

    /**
     * <p>Executes CP210x_GetDeviceMode function of CP210xManufacturing library.</p>
     * 
     * <p>Gets the operating modes of interfaces of a CP2105 device.</p>
     * 
     * @param handle of the device.
     * @return DeviceModeECI value at index 0 and DeviceModeSCI value at index 1 in byte array.
     * @throws SerialComException if an I/O error occurs.
     */
    public byte[] getDeviceMode(long handle) throws SerialComException {
        byte[] ret = mSerialComCP210xManufacturingJNIBridge.getDeviceMode(handle);
        if(ret == null) {
            throw new SerialComException("Could not get the device mode. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes CP210x_GetDeviceVersion function of CP210xManufacturing library.</p>
     * 
     * <p>Returns the device version of a CP210x device.</p>
     * 
     * @param handle of the device.
     * @return device version.
     * @throws SerialComException if an I/O error occurs.
     */
    public int getDeviceVersion(long handle) throws SerialComException {
        int ret = mSerialComCP210xManufacturingJNIBridge.getDeviceVersion(handle);
        if(ret < 0) {
            throw new SerialComException("Could not get the device version. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes CP210x_GetBaudRateConfig function of CP210xManufacturing library.</p>
     * 
     * <p>Gets the baud rate configuration data of a CP210x device.</p>
     * 
     * @param handle of the device.
     * @return array of CP210XbaudConfigs objects containing baudrate data.
     * @throws SerialComException if an I/O error occurs.
     */
    public CP210XbaudConfigs[] getBaudRateConfig(long handle) throws SerialComException {

        CP210XbaudConfigs[] configs = null;
        int i = 0;
        int numOfValues = 0;

        int[] ret = mSerialComCP210xManufacturingJNIBridge.getBaudRateConfig(handle);
        if(ret == null) {
            throw new SerialComException("Could not get the baud rate configuration values. Please retry !");
        }

        // no exception occurred however there is no data read from device.
        if(ret.length < 4) {
            return new CP210XbaudConfigs[] { };
        }

        numOfValues = ret.length / 4;
        configs = new CP210XbaudConfigs[numOfValues];
        for(int x = 0; x < numOfValues; x++) {
            configs[x] = new CP210XbaudConfigs(ret[i], ret[i+1], ret[i+2], ret[i+3]);
            i = i + 4;
        }
        return configs;
    }

    /**
     * <p>Executes CP210x_GetPortConfig function of CP210xManufacturing library.</p>
     * 
     * <p>Gets the current port pin configuration from the CP210x device.</p>
     * 
     * @param handle of the device.
     * @return array of integers containing values (starting from index 0) Mode, Reset_Latch, Suspend_Latch, 
     *          EnhancedFxn respectively.
     * @throws SerialComException if an I/O error occurs.
     */
    public int[] getPortConfig(long handle) throws SerialComException {
        int[] ret = mSerialComCP210xManufacturingJNIBridge.getPortConfig(handle);
        if(ret == null) {
            throw new SerialComException("Could not get the port configuration values. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes CP210xGetDualPortConfig function of CP210xManufacturing library.</p>
     * 
     * <p>Gets the current port pin configuration from the CP210x (typically CP2105) device.</p>
     * 
     * @param handle of the device.
     * @return array of integers containing values (starting from index 0) Mode, Reset_Latch, Suspend_Latch, 
     *          EnhancedFxn_ECI, EnhancedFxn_SCI, EnhancedFxn_Device respectively.
     * @throws SerialComException if an I/O error occurs.
     */
    public int[] getDualPortConfig(long handle) throws SerialComException {
        int[] ret = mSerialComCP210xManufacturingJNIBridge.getDualPortConfig(handle);
        if(ret == null) {
            throw new SerialComException("Could not get the dual port configuration values. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes CP210x_GetQuadPortConfig function of CP210xManufacturing library.</p>
     * 
     * <p>Gets the current port pin configuration from the CP210x (typically CP2108) device.</p>
     * 
     * <p>The sequence of member values in returned array is : Reset_Latch.Mode_PB0, Reset_Latch.Mode_PB1, 
     * Reset_Latch.Mode_PB2, Reset_Latch.Mode_PB3, Reset_Latch.Mode_PB4, Reset_Latch.LowPower_PB0,
     * Reset_Latch.LowPower_PB1, Reset_Latch.LowPower_PB2, Reset_Latch.LowPower_PB3, Reset_Latch.LowPower_PB4, 
     * Reset_Latch.Latch_PB0, Reset_Latch.Latch_PB1, Reset_Latch.Latch_PB2, Reset_Latch.Latch_PB3, Reset_Latch.Latch_PB4, 
     * Suspend_Latch.Mode_PB0, Suspend_Latch.Mode_PB1, Suspend_Latch.Mode_PB2, Suspend_Latch.Mode_PB3, 
     * Suspend_Latch.Mode_PB4, Suspend_Latch.LowPower_PB0, Suspend_Latch.LowPower_PB1, Suspend_Latch.LowPower_PB2, 
     * Suspend_Latch.LowPower_PB3, Suspend_Latch.LowPower_PB4, Suspend_Latch.Latch_PB0, Suspend_Latch.Latch_PB1, 
     * Suspend_Latch.Latch_PB2, Suspend_Latch.Latch_PB3, Suspend_Latch.Latch_PB4, IPDelay_IFC0, IPDelay_IFC1, 
     * IPDelay_IFC2, IPDelay_IFC3, EnhancedFxn_IFC0, EnhancedFxn_IFC1, EnhancedFxn_IFC2, EnhancedFxn_IFC3, 
     * EnhancedFxn_Device, ExtClk0Freq, ExtClk1Freq, ExtClk2Freq, ExtClk3Freq respectively. </p>
     * 
     * @param handle of the device.
     * @return array of integers containing values (starting from index 0) mode, resetLatch, suspendLatch, 
     *          enhancedFxn respectively.
     * @throws SerialComException if an I/O error occurs.
     */
    public int[] getQuadPortConfig(long handle) throws SerialComException {
        int[] ret = mSerialComCP210xManufacturingJNIBridge.getQuadPortConfig(handle);
        if(ret == null) {
            throw new SerialComException("Could not get the quad port configuration values. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Executes CP210x_GetLockValue function of CP210xManufacturing library.</p>
     * 
     * <p>Returns the 1-byte Lock Value of a CP210x device.</p>
     * 
     * @param handle of the device.
     * @return lock value of device.
     * @throws SerialComException if an I/O error occurs.
     */
    public byte getLockValue(long handle) throws SerialComException {
        int ret = mSerialComCP210xManufacturingJNIBridge.getLockValue(handle);
        if(ret < 0) {
            throw new SerialComException("Could not determine the lock value. Please retry !");
        }
        return (byte)ret;
    }

    /**
     * <p>Executes CP210x_Reset function of CP210xManufacturing library.</p>
     * 
     * <p>Initiates a reset of the USB interface.</p>
     * 
     * @param handle of the device.
     * @return true on success.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean reset(long handle) throws SerialComException {
        int ret = mSerialComCP210xManufacturingJNIBridge.reset(handle);
        if(ret < 0) {
            throw new SerialComException("Could not reset the device. Please retry !");
        }
        return true;
    }

    /**
     * <p>Executes CP210x_CreateHexFile function of CP210xManufacturing library.</p>
     * 
     * @param handle of the device.
     * @param fileName name of file.
     * @return true on success.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean createHexFile(long handle, String fileName) throws SerialComException {
        int ret = mSerialComCP210xManufacturingJNIBridge.createHexFile(handle, fileName);
        if(ret < 0) {
            throw new SerialComException("Could not perform the requested operation. Please retry !");
        }
        return true;
    }
}
