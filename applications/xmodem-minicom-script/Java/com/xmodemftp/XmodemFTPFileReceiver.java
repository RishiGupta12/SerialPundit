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

package com.xmodemftp;

import java.io.File;

import com.serialpundit.serial.SerialComManager;
import com.serialpundit.serial.SerialComManager.BAUDRATE;
import com.serialpundit.serial.SerialComManager.DATABITS;
import com.serialpundit.serial.SerialComManager.FLOWCONTROL;
import com.serialpundit.serial.SerialComManager.FTPPROTO;
import com.serialpundit.serial.SerialComManager.FTPVAR;
import com.serialpundit.serial.SerialComManager.PARITY;
import com.serialpundit.serial.SerialComManager.STOPBITS;

public final class XmodemFTPFileReceiver {
    
    public static void main(String[] args) {
        try {
            System.out.println("Receiver application started !");
            SerialComManager scm = new SerialComManager();
            long handle = scm.openComPort(args[0], true, true, true);
            scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
            scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);
            boolean status = scm.receiveFile(handle, new File(args[1]), FTPPROTO.XMODEM, FTPVAR.CHKSUM, true, null, null);
            System.out.println("File received status : " + status);
            scm.closeComPort(handle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

