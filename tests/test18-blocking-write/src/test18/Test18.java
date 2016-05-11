/**
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

package test18;

import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.SerialComManager.BAUDRATE;
import com.embeddedunveiled.serial.SerialComManager.DATABITS;
import com.embeddedunveiled.serial.SerialComManager.FLOWCONTROL;
import com.embeddedunveiled.serial.SerialComManager.PARITY;
import com.embeddedunveiled.serial.SerialComManager.STOPBITS;

class Reader implements Runnable {
    SerialComManager scm = null;
    long handle = 0;
    public Reader(SerialComManager scm, long handle) {
        this.scm = scm;
        this.handle = handle;
    }
    @Override
    public void run() {
        try {
            for(int x=0; x<7; x++) {
                Thread.sleep(5000);
                System.out.println("Read : " + scm.readString(handle, 2*1024));
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}

// Custom baud rate setting and register/unregister listener many times
public class Test18 {
    public static void main(String[] args) throws Exception {

        SerialComManager scm = new SerialComManager();
        long context = 0;
        Thread t = null;

        String PORT = null;
        String PORT1 = null;
        int osType = scm.getOSType();
        if(osType == SerialComManager.OS_LINUX) {
            PORT = "/dev/ttyUSB0";
            PORT1 = "/dev/ttyUSB1";
        }else if(osType == SerialComManager.OS_WINDOWS) {
            PORT = "COM51";
            PORT = "COM52";
        }else if(osType == SerialComManager.OS_MAC_OS_X) {
            PORT = "/dev/cu.usbserial-A70362A3";
            PORT = "/dev/cu.usbserial-A602RDCH";
        }else if(osType == SerialComManager.OS_SOLARIS) {
            PORT = null;
            PORT1 = null;
        }else{
        }

        try {
            long handle = scm.openComPort(PORT, true, true, true);
            scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
            scm.configureComPortControl(handle, FLOWCONTROL.XON_XOFF, 'x', 'Y', false, false);

            long handle1 = scm.openComPort(PORT1, true, true, true);
            scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
            scm.configureComPortControl(handle1, FLOWCONTROL.XON_XOFF, 'x', 'Y', false, false);

            context = scm.createBlockingIOContext();

            byte[] buffer = new byte[2*1024];
            for(int x=0; x<2048; x++) {
                buffer[x] = (byte)65;
            }

            t = new Thread(new Reader(scm, handle1));
            t.start();

            try {
                System.out.println("1 : " + scm.writeBytesBlocking(handle, buffer, context));
                System.out.println("2 : " + scm.writeBytesBlocking(handle, buffer, context));
                System.out.println("3 : " + scm.writeBytesBlocking(handle, buffer, context));
                System.out.println("4 : " + scm.writeBytesBlocking(handle, buffer, context));
                System.out.println("5 : " + scm.writeBytesBlocking(handle, buffer, context));
                System.out.println("6 : " + scm.writeBytesBlocking(handle, buffer, context));
                System.out.println("7 : " + scm.writeBytesBlocking(handle, buffer, context));
                System.out.println("8 : " + scm.writeBytesBlocking(handle, buffer, context));
                Thread.sleep(10000);
            }catch(Exception e) {
                e.printStackTrace();
            }

            // close the port releasing handle
            scm.closeComPort(handle);
            scm.closeComPort(handle1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
