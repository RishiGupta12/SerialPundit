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

package example;

import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComSystemProperty;
import com.serialpundit.serial.SerialComManager;
import com.serialpundit.serial.SerialComManager.BAUDRATE;
import com.serialpundit.serial.SerialComManager.DATABITS;
import com.serialpundit.serial.SerialComManager.FLOWCONTROL;
import com.serialpundit.serial.SerialComManager.PARITY;
import com.serialpundit.serial.SerialComManager.STOPBITS;

/* 
 * This example demonstrates how to read data from serial port and buffer it locally until a 
 * particular number of data bytes has been received from serial port.
 * 
 * There are many different versions of read methods provided by serialpundit and developer 
 * can use the method that is best fit for application requirement. Other variant of read are :
 * 
 * readBytes(long handle)
 * readBytes(long handle, byte[] buffer, int offset, int length, long context)
 * readBytes(long handle, int byteCount)
 * readBytesBlocking(long handle, int byteCount, long context)
 * readBytesDirect(long handle, java.nio.ByteBuffer buffer, int offset, int length)
 * readSingleByte(long handle)
 * readString(long handle)
 * readString(long handle, int byteCount)
 * 
 * This design may be used for "send command and read response" type applications.
 */
public final class ComPollDataRead {

    public static void main(String[] args) {
        try {
            // get serial communication manager instance
            SerialComManager scm = new SerialComManager();
            SerialComPlatform scp = new SerialComPlatform(new SerialComSystemProperty());

            String PORT = null;
            int osType = scp.getOSType();
            if(osType == SerialComPlatform.OS_LINUX) {
                PORT = "/dev/ttyUSB0";
            }else if(osType == SerialComPlatform.OS_WINDOWS) {
                PORT = "COM51";
            }else if(osType == SerialComPlatform.OS_MAC_OS_X) {
                PORT = "/dev/cu.usbserial-A70362A3";
            }else{
            }

            long handle = scm.openComPort(PORT, true, true, true);
            scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
            scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);

            scm.writeString(handle, "test", 0);

            // This is the final buffer in which all data read will be placed
            byte[] dataBuffer = new byte[100];
            byte[] data = null;
            int x = 0;
            int index = 0;
            int totalNumberOfBytesReadTillNow = 0;
            // Keep buffering data until 10 or more than 10 bytes are received.
            while (totalNumberOfBytesReadTillNow <= 10) {
                data = scm.readBytes(handle);
                if(data != null) {
                    for(x=0; x<data.length; x++) {
                        dataBuffer[index] = data[x];
                        index++;
                    }
                    totalNumberOfBytesReadTillNow = totalNumberOfBytesReadTillNow + data.length;
                }
                Thread.sleep(10);
            }

            String readData = new String(dataBuffer);
            System.out.println("Data received is : " + readData);

            scm.closeComPort(handle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
