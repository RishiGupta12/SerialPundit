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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import com.embeddedunveiled.serial.SerialComManager;

/**
 * <p>Provides APIs to create and destroy virtual serial ports using the tty2comKm null modem emulation 
 * driver provided by this library. It follows the protocol as defined by tty2comKm driver.</p>
 * 
 * @author Rishi Gupta
 */
public final class SerialComNullModem {

    /**<p> Bit mask bit specifying that the given pin should be connected to CTS pin of other end. 
     * Constant with value 0x0001. </p>*/
    public static final int SCM_CON_CTS = 0x0001;

    /**<p> Bit mask bit specifying that the given pin should be connected to DCD pin of other end. 
     * Constant with value 0x0002. </p>*/
    public static final int SCM_CON_DCD = 0x0002;

    /**<p> Bit mask bit specifying that the given pin should be connected to DSR pin of other end. 
     * Constant with value 0x0004. </p>*/
    public static final int SCM_CON_DSR = 0x0004;

    /**<p> Bit mask bit specifying that the given pin should be connected to RI pin of other end. 
     * Constant with value 0x0008. </p>*/
    public static final int SCM_CON_RI  = 0x0008;

    private final int osType;
    private FileOutputStream linuxVadaptOut;
    private FileInputStream linuxVadaptIn;
    private final Object lock = new Object();

    // creation of devices, removal of devices and modification to this map is synchronized.
    private final TreeMap<Integer, String> loopBackDevList;

    public SerialComNullModem(int osType) {
        this.osType = osType;
        if(osType == SerialComManager.OS_LINUX) {
            try {
                linuxVadaptOut = new FileOutputStream(new File("/proc/scmtty_vadaptkm"));
                linuxVadaptIn = new FileInputStream(new File("/proc/scmtty_vadaptkm"));
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        loopBackDevList = new TreeMap<Integer, String>();
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
     * @return Created virtual loop back device's node on success.
     * @throws IOException if virtual loop back device can not be created,
     *          IllegalArgumentException if deviceIndex is invalid.
     */
    public String createStandardLoopBackDevice(int deviceIndex) throws IOException {
        byte[] cmd = null;
        String lbdev = null;
        if(osType == SerialComManager.OS_LINUX) {
            if(deviceIndex == -1) {
                cmd = "genlb#xxxxx#xxxxx#7-8,x,x,x#4-1,6,x,x#x-x,x,x,x#x-x,x,x,x#y#y".getBytes();
            }else {
                if((deviceIndex < 0) || (deviceIndex > 65535)) {
                    throw new IllegalArgumentException("deviceIndex should be -1 <= deviceIndex =< 65535 !");
                }
                String cmdd = "genlb#".concat(String.format("%05d", deviceIndex));
                cmdd = cmdd.concat("#xxxxx#7-8,x,x,x#4-1,6,x,x#x-x,x,x,x#x-x,x,x,x#y#y");
                cmd = cmdd.getBytes();
            }
        }

        synchronized(lock) {
            linuxVadaptOut.write(cmd);
            lbdev = getLastLoopBackDeviceNode();
            if(deviceIndex == -1) {
                int idx = Integer.parseInt(lbdev.substring(12), 10);
                loopBackDevList.put(idx, lbdev);
            }else {
                loopBackDevList.put(deviceIndex, lbdev);
            }
        }
        return lbdev;
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
     * @param deviceIndex1 -1 or valid device number (0 <= deviceIndex1 =< 65535).
     * @param deviceIndex2 -1 or valid device number (0 <= deviceIndex1 =< 65535).
     * @return true on success.
     * @throws IOException if virtual null modem device pair can not be created,
     *          IllegalArgumentException if deviceIndex1/2 is invalid.
     */
    public boolean createStandardNullModemPair(int deviceIndex1, int deviceIndex2) throws IOException {
        if(osType == SerialComManager.OS_LINUX) {
            if(deviceIndex1 == -1) {
                if(deviceIndex2 == -1) {
                    synchronized(lock) {
                        linuxVadaptOut.write("gennm#xxxxx#xxxxx#7-8,x,x,x#4-1,6,x,x#7-8,x,x,x#4-1,6,x,x#y#y".getBytes());
                    }
                }else {
                    if((deviceIndex2 < 0) || (deviceIndex2 > 65535)) {
                        throw new IllegalArgumentException("deviceIndex2 should be -1 <= deviceIndex2 =< 65535 !");
                    }
                    String cmd = "gennm#xxxxx#".concat(String.format("%05d", deviceIndex2));
                    cmd = cmd.concat("#7-8,x,x,x#4-1,6,x,x#7-8,x,x,x#4-1,6,x,x#y#y");
                    synchronized(lock) {
                        linuxVadaptOut.write(cmd.getBytes());
                    }
                }
            }else {
                if(deviceIndex2 == -1) {
                    String cmd = "gennm#".concat(String.format("%05d", deviceIndex1));
                    cmd = cmd.concat("#xxxxx#7-8,x,x,x#4-1,6,x,x#7-8,x,x,x#4-1,6,x,x#y#y");
                    synchronized(lock) {
                        linuxVadaptOut.write(cmd.getBytes());
                    }
                }else {
                    if((deviceIndex1 < 0) || (deviceIndex1 > 65535)) {
                        throw new IllegalArgumentException("deviceIndex1 should be -1 <= deviceIndex1 =< 65535 !");
                    }
                    String cmd = "gennm#".concat(String.format("%05d", deviceIndex1));
                    cmd = cmd.concat("#");
                    cmd = cmd.concat(String.format("%05d", deviceIndex2));
                    cmd = cmd.concat("#7-8,x,x,x#4-1,6,x,x#7-8,x,x,x#4-1,6,x,x#y#y");
                    synchronized(lock) {
                        linuxVadaptOut.write(cmd.getBytes());
                    }
                }
            }
        }
        return true;
    }

    /**
     * <p>Creates a virtual loop back device with given pin mappings.</p>
     * 
     * <p>To connect RTS pin to CTS pin use rtsMap = SerialComNullModem.SCM_CON_CTS. A pin can be 
     * connected to one or more pins using bit mask. For example to connect RTS pin to CTS and DSR use 
     * rtsMap = SerialComNullModem.SCM_CON_CTS | SerialComNullModem.SCM_CON_DSR.</p>
     * 
     * @param deviceIndex -1 or valid device number (0 <= deviceIndex =< 65535).
     * @param rtsMap Bit mask of SerialComNullModem.SCM_CON_XXX constants as per the desired pin mappings 
     *         or 0 if RTS pin should be left unconnected.
     * @param dtrMap Bit mask of SerialComNullModem.SCM_CON_XXX constants as per the desired pin mappings 
     *         or 0 if DTR pin should be left unconnected.
     * @return Created virtual loop back device's node on success.
     * @throws IOException if the operation can not be completed successfully,
     *          IllegalArgumentException if invalid deviceIndex is supplied.
     */
    public String createCustomLoopBackDevice(int deviceIndex, int rtsMap, int dtrMap) throws IOException {
        String lbdev = null;
        StringBuilder sb = new StringBuilder();
        if(osType == SerialComManager.OS_LINUX) {
            sb.append("genlb#");
            if(deviceIndex < 0) {
                if(deviceIndex != -1) {
                    throw new IllegalArgumentException("Argument deviceIndex should be -1 <= deviceIndex =< 65535 !");
                }
                sb.append("xxxxx#xxxxx#7-");
            }else {
                if(deviceIndex > 65535) {
                    throw new IllegalArgumentException("Argument deviceIndex should be -1 <= deviceIndex =< 65535 !");
                }
                sb.append(String.format("%05d", deviceIndex));
                sb.append("#xxxxx#7-");
            }

            if(rtsMap == 0) {
                sb.append("x,x,x,x#4-");
            }else {
                if((rtsMap & SCM_CON_CTS) == SCM_CON_CTS) {
                    sb.append(8);
                }else {
                    sb.append("x");
                }
                sb.append(",");
                if((rtsMap & SCM_CON_DCD) == SCM_CON_DCD) {
                    sb.append(1);
                }else {
                    sb.append("x");
                }
                sb.append(",");
                if((rtsMap & SCM_CON_DSR) == SCM_CON_DSR) {
                    sb.append(6);
                }else {
                    sb.append("x");
                }
                sb.append(",");
                if((rtsMap & SCM_CON_RI) == SCM_CON_RI) {
                    sb.append(9);
                }else {
                    sb.append("x");
                }
                sb.append("#4-");
            }

            if(dtrMap == 0) {
                sb.append("x,x,x,x");
            }else {
                if((dtrMap & SCM_CON_CTS) == SCM_CON_CTS) {
                    sb.append(8);
                }else {
                    sb.append("x");
                }
                sb.append(",");
                if((dtrMap & SCM_CON_DCD) == SCM_CON_DCD) {
                    sb.append(1);
                }else {
                    sb.append("x");
                }
                sb.append(",");
                if((dtrMap & SCM_CON_DSR) == SCM_CON_DSR) {
                    sb.append(6);
                }else {
                    sb.append("x");
                }
                sb.append(",");
                if((dtrMap & SCM_CON_RI) == SCM_CON_RI) {
                    sb.append(9);
                }else {
                    sb.append("x");
                }
            }

            sb.append("#x-x,x,x,x#x-x,x,x,x#y#y");
        }

        synchronized(lock) {
            linuxVadaptOut.write(sb.toString().getBytes());
            lbdev = getLastLoopBackDeviceNode();
            if(deviceIndex == -1) {
                int idx = Integer.parseInt(lbdev.substring(12), 10);
                loopBackDevList.put(idx, lbdev);
            }else {
                loopBackDevList.put(deviceIndex, lbdev);
            }
        }

        return lbdev;
    }

    /**
     * 
     * @param idx1
     * @param rtsMap1
     * @param dtrMap1
     * @param idx2
     * @param rtsMap2
     * @param dtrMap2
     * @return
     * @throws IOException
     *          IllegalArgumentException if invalid idx1/2 is supplied.
     */
    public boolean createStandardNullModemPair(int idx1, int rtsMap1, int dtrMap1, int idx2, int rtsMap2, int dtrMap2) throws IOException {
        StringBuilder sb = new StringBuilder();
        if(osType == SerialComManager.OS_LINUX) {
            sb.append("gennm#");
            if(idx1 < 0) {
                if(idx1 != -1) {
                    throw new IllegalArgumentException("Argument idx1 should be -1 <= idx1 =< 65535 !");
                }
                sb.append("xxxxx");
            }else {
                if(idx1 > 65535) {
                    throw new IllegalArgumentException("Argument idx1 should be -1 <= idx1 =< 65535 !");
                }
                sb.append(String.format("%05d", idx1));
            }
            sb.append("#");
            if(idx2 < 0) {
                if(idx2 != -1) {
                    throw new IllegalArgumentException("Argument idx2 should be -1 <= idx2 =< 65535 !");
                }
                sb.append("xxxxx");
            }else {
                if(idx2 > 65535) {
                    throw new IllegalArgumentException("Argument idx2 should be -1 <= idx2 =< 65535 !");
                }
                sb.append(String.format("%05d", idx2));
            }
            sb.append("#7-");

            if(rtsMap1 == 0) {
                sb.append("x,x,x,x#4-");
            }else {
                if((rtsMap1 & SCM_CON_CTS) == SCM_CON_CTS) {
                    sb.append(8);
                }else {
                    sb.append("x");
                }
                sb.append(",");
                if((rtsMap1 & SCM_CON_DCD) == SCM_CON_DCD) {
                    sb.append(1);
                }else {
                    sb.append("x");
                }
                sb.append(",");
                if((rtsMap1 & SCM_CON_DSR) == SCM_CON_DSR) {
                    sb.append(6);
                }else {
                    sb.append("x");
                }
                sb.append(",");
                if((rtsMap1 & SCM_CON_RI) == SCM_CON_RI) {
                    sb.append(9);
                }else {
                    sb.append("x");
                }
                sb.append("#4-");
            }

            if(dtrMap1 == 0) {
                sb.append("x,x,x,x#7-");
            }else {
                if((dtrMap1 & SCM_CON_CTS) == SCM_CON_CTS) {
                    sb.append(8);
                }else {
                    sb.append("x");
                }
                sb.append(",");
                if((dtrMap1 & SCM_CON_DCD) == SCM_CON_DCD) {
                    sb.append(1);
                }else {
                    sb.append("x");
                }
                sb.append(",");
                if((dtrMap1 & SCM_CON_DSR) == SCM_CON_DSR) {
                    sb.append(6);
                }else {
                    sb.append("x");
                }
                sb.append(",");
                if((dtrMap1 & SCM_CON_RI) == SCM_CON_RI) {
                    sb.append(9);
                }else {
                    sb.append("x");
                }
                sb.append("#7-");
            }

            if(rtsMap2 == 0) {
                sb.append("x,x,x,x#4-");
            }else {
                if((rtsMap2 & SCM_CON_CTS) == SCM_CON_CTS) {
                    sb.append(8);
                }else {
                    sb.append("x");
                }
                sb.append(",");
                if((rtsMap2 & SCM_CON_DCD) == SCM_CON_DCD) {
                    sb.append(1);
                }else {
                    sb.append("x");
                }
                sb.append(",");
                if((rtsMap2 & SCM_CON_DSR) == SCM_CON_DSR) {
                    sb.append(6);
                }else {
                    sb.append("x");
                }
                sb.append(",");
                if((rtsMap2 & SCM_CON_RI) == SCM_CON_RI) {
                    sb.append(9);
                }else {
                    sb.append("x");
                }
                sb.append("#4-");
            }

            if(dtrMap2 == 0) {
                sb.append("x,x,x,x#y#y");
            }else {
                if((dtrMap2 & SCM_CON_CTS) == SCM_CON_CTS) {
                    sb.append(8);
                }else {
                    sb.append("x");
                }
                sb.append(",");
                if((dtrMap2 & SCM_CON_DCD) == SCM_CON_DCD) {
                    sb.append(1);
                }else {
                    sb.append("x");
                }
                sb.append(",");
                if((dtrMap2 & SCM_CON_DSR) == SCM_CON_DSR) {
                    sb.append(6);
                }else {
                    sb.append("x");
                }
                sb.append(",");
                if((dtrMap2 & SCM_CON_RI) == SCM_CON_RI) {
                    sb.append(9);
                }else {
                    sb.append("x");
                }
                sb.append("#y#y");
            }
        }

        synchronized(lock) {
            linuxVadaptOut.write(sb.toString().getBytes());
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
     * @throws IOException if the operation can not be completed due to some reason,
     *          IllegalArgumentException if invalid atIndex is supplied.
     */
    public boolean destroyVirtualSerialDevice(int atIndex) throws IOException {
        if(osType == SerialComManager.OS_LINUX) {
            if(atIndex < 0) {
                if(atIndex != -1) {
                    throw new IllegalArgumentException("Argument atIndex is negative, it has to be -1 only !");
                }else {
                    synchronized(lock) {
                        linuxVadaptOut.write("del#xxxxx#xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx".getBytes());
                    }
                }
            }
            else {
                if((atIndex > 65535)) {
                    throw new IllegalArgumentException("Argument atIndex can be -1 <= atIndex =< 65535 !");
                }else {
                    String cmd = "del#".concat(String.format("%05d", atIndex));
                    cmd = cmd.concat("#xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
                    synchronized(lock) {
                        linuxVadaptOut.write(cmd.getBytes());
                    }
                }
            }
        }
        return true;
    }

    /**
     * <p>Returns the device node of last created loop back device.</p>
     * 
     * @return Device node on success otherwise null.
     * @throws IOException if the operation can not be completed for some reason.
     */
    public String getLastLoopBackDeviceNode() throws IOException {
        byte data[] = new byte[64];
        byte tmp[] = new byte[5];

        if(osType == SerialComManager.OS_LINUX) {
            linuxVadaptIn.read(data); // 00002#00009-00016
            for(int q=0; q<5; q++) {
                tmp[q] = data[q];
            }
            int nodeNum = Integer.parseInt(new String(tmp), 10);
            StringBuilder sb = new StringBuilder();
            sb.append("/dev/tty2com");
            sb.append(nodeNum);
            return sb.toString();
        }
        return null;
    }

    /**
     * <p>Returns the device nodes of last created null modem pair.</p>
     * 
     * @return Device nodes of null modem pair on success otherwise null.
     * @throws IOException if the operation can not be completed for some reason.
     */
    public String[] getLastNullModemDevicePairNodes() throws IOException {
        byte data[] = new byte[64];
        byte tmp1[] = new byte[5];
        byte tmp2[] = new byte[5];

        if(osType == SerialComManager.OS_LINUX) {
            linuxVadaptIn.read(data);
            for(int q=0; q<5; q++) {
                tmp1[q] = data[q + 6];
            }
            for(int q=0; q<5; q++) {
                tmp2[q] = data[q + 12];
            }
            int nodeNum1 = Integer.parseInt(new String(tmp1), 10);
            int nodeNum2 = Integer.parseInt(new String(tmp2), 10);
            StringBuilder sb = new StringBuilder();
            sb.append("/dev/tty2com");
            sb.append(nodeNum1);
            String[] nodes = new String[2];
            nodes[0] = sb.toString();
            sb.delete(0, sb.length());
            sb.append("/dev/tty2com");
            sb.append(nodeNum2);
            nodes[1] = sb.toString();
            return nodes;
        }
        return null;
    }

    /**
     * <p>Returns list of virtual loop back devices created by tty2comKm driver and currently present in system.</p>
     * 
     * @return list of virtual loop back devices created by tty2comKm driver and currently present in system or 
     *          null if no loop back device is created.
     */
    public String[] listLoopBackDevices() {
        int x = 0;
        String[] list = null;
        synchronized(lock) {
            int msize = loopBackDevList.size();
            if(msize == 0) {
                return null;
            }
            list = new String[msize];
            for (Map.Entry<Integer, String> entry : loopBackDevList.entrySet()) {
                list[x] = entry.getValue();
                x++;
            }
        }
        return list;
    }
}
