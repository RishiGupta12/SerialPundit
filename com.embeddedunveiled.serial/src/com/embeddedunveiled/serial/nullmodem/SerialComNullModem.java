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

package com.embeddedunveiled.serial.nullmodem;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.embeddedunveiled.serial.SerialComManager;

/**
 * 
 * @author Rishi Gupta
 */
public final class SerialComNullModem {
    
    private final int osType;
    private boolean isOpen;
    
    public SerialComNullModem(int osType) {
        this.osType = osType;
        isOpen = false;
    }
    
    private boolean openVirtualSerialAdaptor() throws IOException {
        if(osType == SerialComManager.OS_LINUX) {
            FileWriter  writer      = new FileWriter("d:\\data\\report.txt");
            PrintWriter printWriter = new PrintWriter(writer);

            printWriter.print(true);
        }
        return true;
    }
    
    /**
     * 
     * @return true on success.
     */
    public boolean createVirtualSerialPort() {
        return true;
    }
    
    /**
     * 
     * @return true on success.
     */
    public boolean createNumberedVirtualSerialPort(int portNumber) {
        return true;
    }
}






