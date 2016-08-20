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

package readtimeout;

import com.serialpundit.core.SerialComException;
import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComSystemProperty;
import com.serialpundit.serial.SerialComManager;
import com.serialpundit.serial.SerialComManager.BAUDRATE;
import com.serialpundit.serial.SerialComManager.DATABITS;
import com.serialpundit.serial.SerialComManager.FLOWCONTROL;
import com.serialpundit.serial.SerialComManager.PARITY;
import com.serialpundit.serial.SerialComManager.STOPBITS;

public final class TimeoutReadCallApp {

    public static void main(String[] args) {

        String PORT = null;
        long handle = -1;
        byte[] dataRead;

        SerialComManager scm;
        SerialComPlatform scp = new SerialComPlatform(new SerialComSystemProperty());

        // get serial communication manager instance
        try {
            scm = new SerialComManager();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        int osType = scp.getOSType();
        if(osType == SerialComPlatform.OS_LINUX) {
            PORT = "/dev/ttyUSB0";
        }else if(osType == SerialComPlatform.OS_WINDOWS) {
            PORT = "COM51";
        }else if(osType == SerialComPlatform.OS_MAC_OS_X) {
            PORT = "/dev/cu.usbserial-A70362A3";
        }else{
        }

        try {
            handle = scm.openComPort(PORT, true, true, true);
            scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
            scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);

            scm.writeString(handle, "test ", 0);

            // Tune read method behaviour (500 milliseconds wait timeout value)
            if(osType == SerialComPlatform.OS_WINDOWS) {
                scm.fineTuneReadBehaviour(handle, 0, 0, 0, 0 , 0);
            }else {
                scm.fineTuneReadBehaviour(handle, 0, 5, 0, 0 , 0);
            }

            // This will return only after given timeout (500 milliseconds) has expired
            dataRead = scm.readBytes(handle);
            if(dataRead != null) {
                System.out.println("Data read : " + new String(dataRead));
            }else {
                System.out.println("Timed out without reading data");
            }

            scm.closeComPort(handle);
        } catch (SerialComException e) {
            if(handle == -1) {
                try {
                    scm.closeComPort(handle);
                } catch (SerialComException e1) {
                }
            }
            e.printStackTrace();
        }
    }
}

