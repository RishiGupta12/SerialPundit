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

package tty2comKmApp;

import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

import com.serialpundit.core.SerialComException;
import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComSystemProperty;
import com.serialpundit.serial.SerialComManager;
import com.serialpundit.serial.nullmodem.SerialComNullModem;

/*
 * TODO
 * - AFTER EVERY TASK UPDATE UI WITH NEXT VALUES
 */

public final class TaskExecutor {

    private SerialComManager scm;
    private SerialComPlatform scp;
    private int osType;
    private SerialComNullModem scnm;
    private JTextField statusInfo;
    private boolean isDriverLoaded;

    // application wide lock
    private final Object lock = new Object();

    public TaskExecutor(JTextField statusInfo) {
        this.statusInfo = statusInfo;
    }

    public void init() throws Exception {

        scm = new SerialComManager();
        scp = new SerialComPlatform(new SerialComSystemProperty());
        osType = scp.getOSType();
        scnm = scm.getSerialComNullModemInstance();

        isDriverLoaded = false;
        try {
            scnm.initialize();
        } catch (SerialComException e) {
            if(osType == SerialComPlatform.OS_LINUX) {
                if(e.getMessage().contains("No such file")) {
                    isDriverLoaded = false;
                }else {
                    throw new IOException("tty2comKm driver not exist !");
                }
            }
        } catch (Exception e) {
            throw e;
        }

        isDriverLoaded = true;
    }

    public void deinit() {
        try {
            scnm.deinitialize();
        } catch (Exception e) {
            // ignore as of now until we have specific scenarios to be handled.
        }
    }

    public String[] getNextAvailableTTY2COMports() {

        String[] nxtp = null;

        if(isDriverLoaded == false) {
            nxtp = new String[2];
            nxtp[0] = "";
            nxtp[1] = "";
        }
        else {
            try {
                nxtp = scnm.listNextAvailablePorts();
            } catch (SerialComException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return nxtp;
    }

    public String[] listExistingStandardNullModemPairs() {

        int i = 0;
        String[] cookedPairs = null;
        StringBuffer buf = null;

        try {
            String[] esnmp = scnm.listExistingStandardNullModemPorts();
            int num = esnmp.length / 2;
            cookedPairs = new String[num];

            for(int x = 0; x < num; x++) {
                buf = new StringBuffer(esnmp[i]);
                buf.append(" -- ");
                buf.append(esnmp[i + 1]);
                cookedPairs[x] = buf.toString();
                i = i + 2;
            }
        } catch (SerialComException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if((cookedPairs == null) || (cookedPairs.length == 0)) {
            cookedPairs = new String[1];
            cookedPairs[0] = TTY2COMApp.NODEV;
        }

        return cookedPairs;
    }

    public String[] listExistingStandardNullModemPairsList() {

        String[] esnmp = null;

        try {
            esnmp = scnm.listExistingStandardNullModemPorts();
        } catch (SerialComException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if((esnmp == null) || (esnmp.length == 0)) {
            esnmp = new String[1];
            esnmp[0] = TTY2COMApp.NONMDEV;
        }

        return esnmp;
    }

    /* User should select either next value we passed as obtained from driver or valid index */
    public void createStandardNullModemPair(final String devNode1, final String devNode2) {

        int idx1 = 0;
        int idx2 = 0;

        if(osType == SerialComPlatform.OS_LINUX) {
            idx1 = Integer.parseInt(devNode1.trim().substring(12), 10);
            idx2 = Integer.parseInt(devNode2.trim().substring(12), 10);
        }

        try {
            scnm.createStandardNullModemPair(idx1, idx2);
        } catch (SerialComException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void deleteStandardNullModemPair(final String devPairToBeDeleted) {

        String[] in = devPairToBeDeleted.split(" ");
        try {
            scnm.destroyGivenVirtualDevice(in[0].trim());
        } catch (SerialComException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void emulateLineError(String devSelectForErrorEvt, int mask) {

    }

    public void emulateLineRingingEvent(String devSelectForErrorEvt, boolean state) {

    }

    public boolean istty2comDriverLoaded() {
        return false;
    }

    public void loadTTY2COMDriver() {
        String[] command = {"gksu", "modprobe usbserial"};
        try {
            Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void unloadTTY2COMDriver() {
        String[] command = {"gksu", "modprobe usbserial"};
        try {
            Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void destroyAlltty2comDevices() {

    }
}




























