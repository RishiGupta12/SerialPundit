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
