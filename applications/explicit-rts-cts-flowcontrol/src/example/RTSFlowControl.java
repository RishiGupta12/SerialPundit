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

import com.serialpundit.core.util.SerialComUtil;
import com.serialpundit.serial.SerialComManager;
import com.serialpundit.serial.SerialComManager.BAUDRATE;
import com.serialpundit.serial.SerialComManager.DATABITS;
import com.serialpundit.serial.SerialComManager.FLOWCONTROL;
import com.serialpundit.serial.SerialComManager.PARITY;
import com.serialpundit.serial.SerialComManager.STOPBITS;

final class DataHandler implements Runnable {

    /* Modify port as per your system */
    private String PORT = "/dev/ttyUSB0";

    private long handle;
    private SerialComManager scm;
    private long context;
    private byte[] buffer;
    private Object lock;

    public DataHandler (Object lock) {
        this.lock = lock;
    }

    @Override
    public void run() {
        try {
            System.out.println("Application thread started !");

            scm = new SerialComManager();
            handle = scm.openComPort(PORT, true, true, true);
            scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
            scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);
            context = scm.createBlockingIOContext();

            // When a serial port is opened using serialpundit, by default DTR and RTS line will be asserted.
            // This may make serial device think that application running on host computer is ready for 
            // communication. So, reset these lines until application has finished all initialization. Use 
            // a keyword ("start") to sync application and firmware at startup.
            scm.setDTR(handle, false);
            scm.setRTS(handle, false);

            // { Do application specific initialization here } //

            // notify serial device that host application is now ready.
            scm.setDTR(handle, true);
            scm.setRTS(handle, true);

            // when the initialization is completed, notify firmware and start actual communication.
            scm.writeString(handle, "start", 0);

            // block until serial device sends some data.
            buffer = scm.readBytesBlocking(handle, 32, context);

            // print data received from serial device on console.
            System.out.println(SerialComUtil.byteArrayToHexString(buffer, " "));

            // exit application.
            synchronized(lock) {
                lock.notify();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }	
}

/* Entry point to this application */
public final class RTSFlowControl {

    private final static Object lock = new Object();

    public static void main(String[] args) throws InterruptedException {
        Thread t = new Thread(new DataHandler(lock));
        t.start();
        synchronized(lock) {
            lock.wait();
        }
        System.out.println("Application exited !");
    }
}
