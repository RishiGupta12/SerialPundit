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

import javax.swing.SwingUtilities;

import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComSystemProperty;
import com.serialpundit.serial.SerialComInByteStream;
import com.serialpundit.serial.SerialComManager;
import com.serialpundit.serial.SerialComManager.BAUDRATE;
import com.serialpundit.serial.SerialComManager.DATABITS;
import com.serialpundit.serial.SerialComManager.FLOWCONTROL;
import com.serialpundit.serial.SerialComManager.PARITY;
import com.serialpundit.serial.SerialComManager.SMODE;
import com.serialpundit.serial.SerialComManager.STOPBITS;
import com.serialpundit.serial.SerialComOutByteStream;

import ui.GraphPlotter;
import ui.ICleanUpListener;

final class CleanUp implements ICleanUpListener {

    private SerialComManager scm;
    private SerialComOutByteStream out;
    private SerialComInByteStream in;
    private long comPortHandle;

    public CleanUp(SerialComManager scm, SerialComInByteStream in, SerialComOutByteStream out, long comPortHandle) {
        this.scm = scm;
        this.in = in;
        this.out = out;
        this.comPortHandle = comPortHandle;
    }

    @Override
    public void onAppExit() {
        try {
            in.close();
            out.close();
            scm.closeComPort(comPortHandle);
            System.out.println("Clean up completed !");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

public final class ByteStreamIntegrationApp {

    private SerialComManager scm;
    private SerialComOutByteStream out;
    private SerialComInByteStream in;
    private String PORT;
    private long comPortHandle;
    private CleanUp cleanup;
    private GraphPlotter plotter;

    protected void begin() {
        try {
            System.out.println("Application started !");

            // instantiate serialpundit.
            scm = new SerialComManager();
            SerialComPlatform scp = new SerialComPlatform(new SerialComSystemProperty());

            int osType = scp.getOSType();
            if(osType == SerialComPlatform.OS_LINUX) {
                PORT = "/dev/ttyUSB0";
            }else if(osType == SerialComPlatform.OS_WINDOWS) {
                PORT = "COM51";
            }else if(osType == SerialComPlatform.OS_MAC_OS_X) {
                PORT = "/dev/cu.usbserial-A70362A3";
            }else{
            }

            // open serial port.
            comPortHandle = scm.openComPort(PORT, true, true, true);
            scm.configureComPortData(comPortHandle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
            scm.configureComPortControl(comPortHandle, FLOWCONTROL.NONE, 'x', 'x', false, false);

            // create input and output byte streams.            
            out = (SerialComOutByteStream) scm.getIOStreamInstance(SerialComManager.OutputStream, comPortHandle, SMODE.BLOCKING);
            in = (SerialComInByteStream) scm.getIOStreamInstance(SerialComManager.InputStream, comPortHandle, SMODE.BLOCKING);

            // prepare class that will be used when application exits.
            cleanup = new CleanUp(scm, in, out, comPortHandle);

            // setup GUI.
            plotter = new GraphPlotter(in, out, cleanup);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* Entry point to this application. */
    public static void main(String[] args) {
        // Setup GUI in event-dispatching thread for thread-safety.
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ByteStreamIntegrationApp app = new ByteStreamIntegrationApp();
                app.begin();
            }
        });
    }
}
