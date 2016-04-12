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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.embeddedunveiled.serial.SerialComManager;

/**
 * <p>Provides APIs to create and destroy virtual serial ports using the tty2comKm driver.</p>
 * 
 * @author Rishi Gupta
 */
public final class SerialComNullModem {

    private final int osType;
    private FileOutputStream linuxVadapt;

    public SerialComNullModem(int osType) {
        this.osType = osType;
        if(osType == SerialComManager.OS_LINUX) {
            try {
                linuxVadapt = new FileOutputStream(new File("/proc/scmtty_vadaptkm"));
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * <p>Creates a standard loop back connected virtual serial port device. If deviceIndex is -1, the 
     * next available index will be used by driver. If deviceIndex is a valid number, the given index 
     * will be used to create device node.</p>
     * 
     * <p>For example; createStandardLoopBackDevice(2) will create /dev/tty2com2 device node in Linux or 
     * will throw exception if that number is already in use. Similarly createStandardLoopBackDevice(-1) 
     * will create /dev/tty2comXX where XX is the next free number managed by the driver internally.</p>
     * 
     * @param deviceIndex -1 or valid device number (0 <= deviceIndex =< 65535).
     * @return true on success.
     * @throws IOException if virtual loop back device can not be created.
     */
    public boolean createStandardLoopBackDevice(int deviceIndex) throws IOException {
        if(osType == SerialComManager.OS_LINUX) {
            if(deviceIndex == -1) {
                linuxVadapt.write("genlb#xxxxx#xxxxx#7-8,x,x,x#4-1,6,x,x#x-x,x,x,x#x-x,x,x,x#y#y".getBytes());
            }else {
                if((deviceIndex < 0) || (deviceIndex > 65535)) {
                    throw new IOException("deviceIndex should be 0 <= deviceIndex =< 65535 !");
                }
                String cmd = "genlb#".concat(String.format("%05d", deviceIndex));
                cmd = cmd.concat("#xxxxx#7-8,x,x,x#4-1,6,x,x#x-x,x,x,x#x-x,x,x,x#y#y");
                linuxVadapt.write(cmd.getBytes());
            }
        }
        return true;
    }

    /**
     * <p>Creates a standard null modem connected virtual serial port device pair. If deviceIndex is -1, the 
     * next available index will be used by driver. If deviceIndex is a valid number, the given index 
     * will be used to create device nodes.</p>
     * 
     * <p>For example; createStandardNullModemDevices(2, 3) will create /dev/tty2com2 and /dev/tty2com3 device 
     * nodes in Linux or will throw exception if any of the given number is already in use. Similarly 
     * createStandardNullModemDevices(-1, -1) will create /dev/tty2comXX and /dev/tty2comYY where XX/YY are the 
     * next free numbers managed by the driver internally.</p>
     * 
     * @param deviceIndex1/2 -1 or valid device number (0 <= deviceIndex1/2 =< 65535).
     * @return true on success.
     * @throws IOException if virtual null modem device pair can not be created.
     */
    public boolean createStandardNullModemDevices(int deviceIndex1, int deviceIndex2) throws IOException {
        if(osType == SerialComManager.OS_LINUX) {
            if(deviceIndex1 == -1) {
                if(deviceIndex2 == -1) {
                    linuxVadapt.write("gennm#xxxxx#xxxxx#7-8,x,x,x#4-1,6,x,x#7-8,x,x,x#4-1,6,x,x#y#y".getBytes());
                }else {
                    String cmd = "gennm#xxxxx#".concat(String.format("%05d", deviceIndex2));
                    cmd = cmd.concat("#7-8,x,x,x#4-1,6,x,x#7-8,x,x,x#4-1,6,x,x#y#y");
                    linuxVadapt.write(cmd.getBytes());
                }
            }else {
                if(deviceIndex2 == -1) {
                    String cmd = "gennm#".concat(String.format("%05d", deviceIndex1));
                    cmd = cmd.concat("#xxxxx#7-8,x,x,x#4-1,6,x,x#7-8,x,x,x#4-1,6,x,x#y#y");
                    linuxVadapt.write(cmd.getBytes());
                }else {
                    String cmd = "gennm#".concat(String.format("%05d", deviceIndex1));
                    cmd = cmd.concat("#");
                    cmd = cmd.concat(String.format("%05d", deviceIndex2));
                    cmd = cmd.concat("#7-8,x,x,x#4-1,6,x,x#7-8,x,x,x#4-1,6,x,x#y#y");
                    linuxVadapt.write(cmd.getBytes());
                }
            }
        }
        return true;
    }

    /**
     * <p>Removes all virtual serial devices created by tty2comKm driver if atIndex is -1 or removes only a 
     * particular device as specified by atIndex argument. Note that if the given index represent to one of 
     * the device nodes in a null modem pair, paired device will automatically be identified and deleted too.</p>
     * 
     * @param atIndex -1 if all devices are to be destroyed or valid device number.
     * @return true on success.
     * @throws IOException if the operation can not be complted due to some reason.
     */
    public boolean destroyVirtualSerialDevice(int atIndex) throws IOException {
        if(osType == SerialComManager.OS_LINUX) {
            if(atIndex < 0) {
                if(atIndex != -1) {
                    throw new IOException("If atIndex is negative, it has to be -1 only !");
                }else {
                    linuxVadapt.write("del#xxxxx#xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx".getBytes());
                }
            }
            else {
                if((atIndex > 65535)) {
                    throw new IOException("The atIndex can be -1 <= atIndex =< 65535 !");
                }else {
                    String cmd = "del#".concat(String.format("%05d", atIndex));
                    cmd = cmd.concat("#xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
                    linuxVadapt.write(cmd.getBytes());
                }
            }

        }
        return true;
    }
}






