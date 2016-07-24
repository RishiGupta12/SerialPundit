/*
 * This file is part of SerialPundit project and software.
 * 
 * Copyright (C) 2014-2016, Rishi Gupta. All rights reserved.
 *
 * The SerialPundit software is DUAL licensed. It is made available under the terms of the GNU Affero 
 * General Public License (AGPL) v3.0 for non-commercial use and under the terms of a commercial 
 * license for commercial use of this software. 
 * 
 * The SerialPundit software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.serialpundit.serial.nullmodem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import com.serialpundit.core.SerialComPlatform;

/**
 * <p>Provides APIs to create and destroy virtual serial ports using the tty2comKm null modem emulation 
 * driver provided by this library. It follows the protocol as defined by tty2comKm driver.</p>
 * 
 * @author Rishi Gupta
 */
public final class SerialComNullModem {

    /**<p> Bit mask bit specifying that the given pin should be left unconnected.</p> 
     * Constant with value 0x0000. </p>*/
    public static final int SCM_CON_NONE = 0x0000;

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

    /**<p> Bit mask bit specifying that a framing error should be emulated.</p> 
     * Constant with value 0x0010. </p>*/
    public static final int ERR_FRAME = 0x0010;

    /**<p> Bit mask bit specifying that a parity error should be emulated.</p> 
     * Constant with value 0x0020. </p>*/
    public static final int ERR_PARITY = 0x0020;

    /**<p> Bit mask bit specifying that a overrun error should be emulated.</p> 
     * Constant with value 0x0040. </p>*/
    public static final int ERR_OVERRUN = 0x0040;

    /**<p> Bit mask bit specifying that a break condition reception should be emulated.</p> 
     * Constant with value 0x0080. </p>*/
    public static final int RCV_BREAK = 0x0080;

    private final int osType;
    private FileOutputStream linuxVadaptOut;
    private FileInputStream linuxVadaptIn;
    private final Object lock = new Object();

    // creation of devices, removal of devices and modification to these maps is synchronized.
    private final TreeMap<Integer, String> loopBackDevList;
    private final TreeMap<Integer, String> nullModemDevList;

    /**
     * <p>Create an instance of SerialComNullModem with given details.</p>
     *  
     * @param osType operating system this library is running on.
     * @throws IOException if any exception occurs while preparing for null modem communication.
     */
    public SerialComNullModem(int osType) throws IOException {

        this.osType = osType;
        if(osType == SerialComPlatform.OS_LINUX) {
            try {
                File f = new File("/proc/scmtty_vadaptkm");
                if(!f.exists()) {
                    throw new FileNotFoundException("The /proc/scmtty_vadaptkm not found. Is driver loaded ???");
                }
                linuxVadaptOut = new FileOutputStream(f);
            } catch(Exception e) {
                throw e;
            }
            try {
                linuxVadaptIn = new FileInputStream(new File("/proc/scmtty_vadaptkm"));
            } catch(IOException e) {
                try {
                    linuxVadaptOut.close();
                } catch (IOException e1) {
                    throw e1;
                }
                throw e;
            }
        }
        loopBackDevList = new TreeMap<Integer, String>();
        nullModemDevList = new TreeMap<Integer, String>();
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
        if(osType == SerialComPlatform.OS_LINUX) {
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
     * <P>After calling this method it is advised to call getLastNullModemDevicePairNodes() method to get operating 
     * system specific device node name.</p>
     * 
     * @param deviceIndex1 -1 or valid device number (0 <= deviceIndex1 =< 65535).
     * @param deviceIndex2 -1 or valid device number (0 <= deviceIndex1 =< 65535).
     * @return Created virtual null modem pair device's node on success.
     * @throws IOException if virtual null modem device pair can not be created,
     *          IllegalArgumentException if deviceIndex1/2 is invalid.
     */
    public String[] createStandardNullModemPair(int deviceIndex1, int deviceIndex2) throws IOException {
        byte[] cmd = null;
        String[] nmdevs = null;
        if(osType == SerialComPlatform.OS_LINUX) {
            if(deviceIndex1 == -1) {
                if(deviceIndex2 == -1) {
                    cmd = "gennm#xxxxx#xxxxx#7-8,x,x,x#4-1,6,x,x#7-8,x,x,x#4-1,6,x,x#y#y".getBytes();
                }else {
                    if((deviceIndex2 < 0) || (deviceIndex2 > 65535)) {
                        throw new IllegalArgumentException("deviceIndex2 should be -1 <= deviceIndex2 =< 65535 !");
                    }
                    String cmdd = "gennm#xxxxx#".concat(String.format("%05d", deviceIndex2));
                    cmdd = cmdd.concat("#7-8,x,x,x#4-1,6,x,x#7-8,x,x,x#4-1,6,x,x#y#y");

                }
            }else {
                if(deviceIndex2 == -1) {
                    String cmdd = "gennm#".concat(String.format("%05d", deviceIndex1));
                    cmdd = cmdd.concat("#xxxxx#7-8,x,x,x#4-1,6,x,x#7-8,x,x,x#4-1,6,x,x#y#y");
                    cmd = cmdd.getBytes();
                }else {
                    if((deviceIndex1 < 0) || (deviceIndex1 > 65535)) {
                        throw new IllegalArgumentException("deviceIndex1 should be -1 <= deviceIndex1 =< 65535 !");
                    }
                    String cmdd = "gennm#".concat(String.format("%05d", deviceIndex1));
                    cmdd = cmdd.concat("#");
                    cmdd = cmdd.concat(String.format("%05d", deviceIndex2));
                    cmdd = cmdd.concat("#7-8,x,x,x#4-1,6,x,x#7-8,x,x,x#4-1,6,x,x#y#y");
                    cmd = cmdd.getBytes();
                }
            }
            synchronized(lock) {
                linuxVadaptOut.write(cmd);
                nmdevs = getLastNullModemDevicePairNodes();
                int idx = Integer.parseInt(nmdevs[0].substring(12), 10);
                StringBuilder sb = new StringBuilder();
                sb.append(nmdevs[0]);
                sb.append(" <=> ");
                sb.append(nmdevs[1]);
                nullModemDevList.put(idx, sb.toString());
            }
        }

        return nmdevs;
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
        if(osType == SerialComPlatform.OS_LINUX) {
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

            if(rtsMap == SCM_CON_NONE) {
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

            if(dtrMap == SCM_CON_NONE) {
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
        }

        return lbdev;
    }

    /**
     * <p>Creates a null modem device pair with given pin mappings.</p>
     * 
     * @param idx1 index of 1st virtual serial port to be created.
     * @param rtsMap1 pin mappings definition (define how RTS pin of idx1 device should be connected to pins of idx2 device).
     * @param dtrMap1 pin mappings definition (define how DTR pin of idx1 device should be connected to pins of idx2 device).
     * @param idx2 index of 2nd virtual serial port to be created.
     * @param rtsMap2 pin mappings definition (define how RTS pin of idx2 device should be connected to pins of idx1 device).
     * @param dtrMap2 pin mappings definition (define how DTR pin of idx2 device should be connected to pins of idx1 device).
     * @return Created virtual null modem pair device's node on success.
     * @throws IOException if the null modem device node pair can not be created, 
     *          IllegalArgumentException if invalid idx1/2 is supplied.
     */
    public String[] createCustomNullModemPair(int idx1, int rtsMap1, int dtrMap1, int idx2, int rtsMap2, int dtrMap2) throws IOException {
        String[] nmdevs = null;
        StringBuilder sb = new StringBuilder();

        if(osType == SerialComPlatform.OS_LINUX) {
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

            if(rtsMap1 == SCM_CON_NONE) {
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

            if(dtrMap1 == SCM_CON_NONE) {
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

            if(rtsMap2 == SCM_CON_NONE) {
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

            if(dtrMap2 == SCM_CON_NONE) {
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

            synchronized(lock) {
                linuxVadaptOut.write(sb.toString().getBytes());
                nmdevs = getLastNullModemDevicePairNodes();
                int idx = Integer.parseInt(nmdevs[0].substring(12), 10);
                StringBuilder sb1 = new StringBuilder();
                sb1.append(nmdevs[0]);
                sb1.append(" <=> ");
                sb1.append(nmdevs[1]);
                nullModemDevList.put(idx, sb1.toString());
            }
        }

        return nmdevs;
    }

    /**
     * <p>Removes all virtual serial devices created by tty2comKm driver.</p>
     * 
     * @return true on success.
     * @throws IOException if the operation can not be completed due to some reason.
     */
    public boolean destroyAllVirtualDevices() throws IOException {
        if(osType == SerialComPlatform.OS_LINUX) {
            synchronized(lock) {
                linuxVadaptOut.write("del#xxxxx#xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx".getBytes());
                loopBackDevList.clear();
                nullModemDevList.clear();
            }
        }
        return true;
    }

    /**
     * <p>Removes the virtual loop back device created using tty2comKm driver.</p>
     * 
     * @param deviceNode virtual serial port to be destroyed.
     * @return true on success.
     * @throws IOException if the given device can not be deleted due to some reason,
     *          IllegalArgumentException if invalid deviceNode is supplied or deviceNode is null.
     */
    public boolean destroyVirtualLoopBackDevice(final String deviceNode) throws IOException {
        if((deviceNode == null) || (deviceNode.length() == 0)) {
            throw new IllegalArgumentException("The deviceNode can not be null or 0 length !");
        }
        if(osType == SerialComPlatform.OS_LINUX) {
            int nodeNum = Integer.parseInt(deviceNode.substring(12), 10);
            synchronized(lock) {
                if(loopBackDevList.containsKey(nodeNum)) {
                    String cmd = "del#".concat(String.format("%05d", nodeNum));
                    cmd = cmd.concat("#xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
                    linuxVadaptOut.write(cmd.getBytes());
                    loopBackDevList.remove(nodeNum);
                    return true;
                }
            }
            throw new IllegalArgumentException("Given device node is not found in our records !");
        }
        return false;
    }

    /**
     * <p>Removes the virtual null modem device pair created using tty2comKm driver. The devNode1 must be 
     * 1st device node as returned by createStandardNullModemPair() or createCustomNullModemPair() methods.</p>
     * 
     * @param devNode1 one of the device node of null modem pair to be destroyed.
     * @param devNode2 one of the device node of null modem pair to be destroyed.
     * @return true on success.
     * @throws IOException if the given device can not be deleted due to some reason, 
     *          IllegalArgumentException if invalid deviceNode is supplied or devNode1/2 is null.
     */
    public boolean destroyVirtualNullModemPair(final String devNode1, final String devNode2) throws IOException {
        if((devNode1 == null) || (devNode1.length() == 0) || (devNode2 == null) || (devNode2.length() == 0)) {
            throw new IllegalArgumentException("The devNode1/2 can not be null or 0 length string !");
        }
        if(devNode1.equals(devNode2)) {
            throw new IllegalArgumentException("The devNode1 can not be equal to devNode2 !");
        }
        if(osType == SerialComPlatform.OS_LINUX) {
            int idx = Integer.parseInt(devNode1.substring(12), 10);
            String pair = nullModemDevList.get(idx);
            if(pair == null) {
                throw new IllegalArgumentException("Given device nodes are not found in our records !");
            }
            if(pair.contains(devNode2)) {
                String cmd = "del#".concat(String.format("%05d", idx));
                cmd = cmd.concat("#xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
                linuxVadaptOut.write(cmd.getBytes());
                nullModemDevList.remove(idx);
                return true;
            }
            throw new IllegalArgumentException("Given devNode1 and devNode2 seems not to be a null modem pair !");
        }

        return false;
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

        if(osType == SerialComPlatform.OS_LINUX) {
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

        if(osType == SerialComPlatform.OS_LINUX) {
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

    /**
     * <p>Returns list of virtual null modem device pairs created by tty2comKm driver and currently present 
     * in system.</p>
     * 
     * @return list of virtual null modem devices created by tty2comKm driver and currently present in system 
     *          or null if no null modem pair is created.
     */
    public String[] listNullModemDevicePairs() {
        int x = 0;
        String[] list = null;
        synchronized(lock) {
            int msize = nullModemDevList.size();
            if(msize == 0) {
                return null;
            }
            list = new String[msize];
            for (Map.Entry<Integer, String> entry : nullModemDevList.entrySet()) {
                list[x] = entry.getValue();
                x++;
            }
        }
        return list;
    }

    /**
     * <p>Emulate the given line error (frame, parity or overrun) on given device.</p>
     * 
     * @param devNode virtual serial port which will receive this error event.
     * @param error one of the constants SerialComNullModem.ERR_XXX.
     * @return true if the given error has been emulated on given virtual serial port otherwise
     *          false.
     * @throws IOException if the operating system specific file is not found, writing to it fails 
     *          or operation can not be completed due to some reason.
     */
    public boolean emulateLineError(final String devNode, int error) throws IOException {
        if((devNode == null) || (devNode.length() == 0)) {
            throw new IllegalArgumentException("The devNode can not be null or 0 length !");
        }
        if(osType == SerialComPlatform.OS_LINUX) {
            // /sys/devices/virtual/tty/tty2com0/scmvtty_errevt/evt
            StringBuilder sb = new StringBuilder();
            sb.append("/sys/devices/virtual/tty/");
            sb.append(devNode.substring(5));
            sb.append("/scmvtty_errevt/evt");
            try (FileOutputStream fout = new FileOutputStream(sb.toString())) {
                if((error & ERR_FRAME) == ERR_FRAME) {
                    fout.write("1".getBytes());
                }else if((error & ERR_PARITY) == ERR_PARITY) {
                    fout.write("2".getBytes());
                }else if((error & ERR_OVERRUN) == ERR_OVERRUN) {
                    fout.write("3".getBytes());
                }else if((error & RCV_BREAK) == RCV_BREAK) {
                    fout.write("6".getBytes());
                }else {
                    return false;
                }
            } catch (IOException e) {
                throw e;
            }
        }

        return true;
    }

    /**
     * <p>Emulate line ringing event on given device node.</p>
     * 
     * @param devNode device node which will observe ringing conditions.
     * @param state true if ringing event should be asserted or false for de-assertion.
     * @return true on success.
     * @throws IOException if the operating system specific file is not found, writing to it fails 
     *          or operation can not be completed due to some reason.
     */
    public boolean emulateLineRingingEvent(final String devNode, boolean state) throws IOException {
        if((devNode == null) || (devNode.length() == 0)) {
            throw new IllegalArgumentException("The devNode can not be null or 0 length !");
        }
        if(osType == SerialComPlatform.OS_LINUX) {
            // /sys/devices/virtual/tty/tty2com0/scmvtty_errevt/evt
            StringBuilder sb = new StringBuilder();
            sb.append("/sys/devices/virtual/tty/");
            sb.append(devNode.substring(5));
            sb.append("/scmvtty_errevt/evt");
            try (FileOutputStream fout = new FileOutputStream(sb.toString())) {
                if(state == true) {
                    fout.write("4".getBytes());
                }else {
                    fout.write("5".getBytes());
                }
            } catch (IOException e) {
                throw e;
            }
        }

        return true;
    }

    /**
     * <p>Releases operating system specific resources acquired to carry out virtual device related
     * operations. Applications must call this method when this class is no longer required.</p>
     * 
     * @return true on success.
     * @throws IOException if the resources can not be released.
     */
    public boolean releaseResources() throws IOException {
        if(osType == SerialComPlatform.OS_LINUX) {
            try {
                linuxVadaptOut.close();
            } catch (IOException e) {
                try {
                    linuxVadaptIn.close();
                } catch (IOException e1) {
                    throw e1;
                }
                throw e;
            }
            try {
                linuxVadaptIn.close();
            } catch (IOException e) {
                throw e;
            }
        }
        return true;
    }
}
