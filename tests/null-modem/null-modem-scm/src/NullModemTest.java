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

import java.util.concurrent.Executors;
import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.nullmodem.SerialComNullModem;
import com.embeddedunveiled.serial.SerialComManager.BAUDRATE;
import com.embeddedunveiled.serial.SerialComManager.DATABITS;
import com.embeddedunveiled.serial.SerialComManager.FLOWCONTROL;
import com.embeddedunveiled.serial.SerialComManager.PARITY;
import com.embeddedunveiled.serial.SerialComManager.STOPBITS;

/* 
 * LOAD module to support large number of devices for this test.
 * 
 * $ insmod ./tty2comKm.ko max_num_vtty_dev=5000 
 */

public final class NullModemTest {

    public static void main(String[] args) throws Exception {

        SerialComManager scm = new SerialComManager();
        final SerialComNullModem scnm = scm.getSerialComNullModemInstance();
        String a = null;

        //        // create loopback, writing to it then read, don't configure terminal, it should not block
        //        String lbp1 = scnm.createStandardLoopBackDevice(-1);
        //        System.out.println("loop back dev : " + lbp1);
        //        long lbp1hand1 = scm.openComPort(lbp1, true, true, false);
        //        scm.writeString(lbp1hand1, "data", 0);
        //        System.out.println("written string data");
        //        Thread.sleep(100);
        //        System.out.println("read string : " + scm.readString(lbp1hand1));
        //        scm.closeComPort(lbp1hand1);

        // num bytes in i/o buffer
        try {
            String[] ports = scnm.createStandardNullModemPair(-1, -1);
            long hand1 = scm.openComPort(ports[0], true, true, false);
            scm.configureComPortData(hand1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_ODD, BAUDRATE.B115200, 0);
            scm.configureComPortControl(hand1, FLOWCONTROL.NONE, 'x', 'x', true, true);
            long hand2 = scm.openComPort(ports[1], true, true, false);
            scm.configureComPortData(hand2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_ODD, BAUDRATE.B115200, 0);
            scm.configureComPortControl(hand2, FLOWCONTROL.NONE, 'x', 'x', true, true);

            int[] b = scm.getByteCountInPortIOBuffer(hand1);
            System.out.println("before: " + b[0] + " : " + b[1]);
            scm.writeString(hand2, "test", 0);
            int[] c = scm.getByteCountInPortIOBuffer(hand1);
            System.out.println("after: " + c[0] + " : " + c[1]);

            scm.closeComPort(hand1);
            scm.closeComPort(hand2);
        }catch (Exception e) {
            scnm.destroyAllVirtualDevices();
            e.printStackTrace();
        }

        try {
            Executors.newSingleThreadExecutor().execute(new Runnable() {
                @Override 
                public void run() {
                    try {
                        for(int x=0; x<1000; x++) {
                            scnm.createStandardNullModemPair(-1, -1);
                            Thread.sleep(10);
                        }
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            Executors.newSingleThreadExecutor().execute(new Runnable() {
                @Override 
                public void run() {
                    try {
                        for(int x=0; x<1000; x++) {
                            scnm.createStandardLoopBackDevice(-1);
                            Thread.sleep(10);
                        }
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            Executors.newSingleThreadExecutor().execute(new Runnable() {
                @Override 
                public void run() {
                    try {
                        for(int x=0; x<1000; x++) {
                            scnm.createStandardNullModemPair(-1, -1);
                            Thread.sleep(10);
                        }
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            try {
                scnm.destroyAllVirtualDevices();
            }catch (Exception e) {
                e.printStackTrace();
            }

            try {
                scnm.createStandardLoopBackDevice(-1);
            }catch (Exception e) {
                e.printStackTrace();
            }

            try {
                a = scnm.createStandardLoopBackDevice(2);
            }catch (Exception e) {
                e.printStackTrace();
            }

            try {
                scnm.createStandardNullModemPair(-1, -1);
            }catch (Exception e) {
                e.printStackTrace();
            }

            try {
                scnm.createStandardNullModemPair(-1, 2);
            }catch (Exception e) {
                e.printStackTrace();
            }

            try {
                scnm.createStandardNullModemPair(7, -1);
            }catch (Exception e) {
                e.printStackTrace();
            }

            try {
                scnm.createStandardNullModemPair(9, 16);
            }catch (Exception e) {
                e.printStackTrace();
            }

            try {
                System.out.println("last loopback node : " + scnm.getLastLoopBackDeviceNode());
            }catch (Exception e) {
                e.printStackTrace();
            }

            try {
                String[] str = scnm.getLastNullModemDevicePairNodes();
                System.out.println("last null modem node : " + str[0] + "--" + str[1]);
            }catch (Exception e) {
                e.printStackTrace();
            }

            try {
                scnm.destroyVirtualLoopBackDevice(a);
            }catch (Exception e) {
                e.printStackTrace();
            }

            try {
                scnm.destroyAllVirtualDevices();
            }catch (Exception e) {
                e.printStackTrace();
            }

            try {
                scnm.createCustomLoopBackDevice(25, 0, 0);
            }catch (Exception e) {
                e.printStackTrace();
            }

            try {
                scnm.createCustomLoopBackDevice(-1, SerialComNullModem.SCM_CON_CTS, SerialComNullModem.SCM_CON_DCD | SerialComNullModem.SCM_CON_DSR);
            }catch (Exception e) {
                e.printStackTrace();
            }

            try {
                scnm.createCustomLoopBackDevice(65, 0, SerialComNullModem.SCM_CON_CTS | SerialComNullModem.SCM_CON_DCD | SerialComNullModem.SCM_CON_DSR);
            }catch (Exception e) {
                e.printStackTrace();
            }

            try {
                scnm.createCustomLoopBackDevice(29, SerialComNullModem.SCM_CON_CTS | SerialComNullModem.SCM_CON_DCD | SerialComNullModem.SCM_CON_DSR, 0);
            }catch (Exception e) {
                e.printStackTrace();
            }


            try {
                scnm.destroyAllVirtualDevices();
            }catch (Exception e) {
                e.printStackTrace();
            }

            try {
                scnm.createCustomNullModemPair(-1, SerialComNullModem.SCM_CON_CTS, SerialComNullModem.SCM_CON_DCD | SerialComNullModem.SCM_CON_DSR, 
                        -1, SerialComNullModem.SCM_CON_CTS, SerialComNullModem.SCM_CON_DCD | SerialComNullModem.SCM_CON_DSR);
            }catch (Exception e) {
                e.printStackTrace();
            }

            try {
                scnm.destroyAllVirtualDevices();
            }catch (Exception e) {
                e.printStackTrace();
            }

            /********* Final clean up (Release operating system specific resources held by null modem class) *********/
            scnm.destroyAllVirtualDevices();
            Thread.sleep(10000);
            scnm.releaseResources();
            System.out.println("Done !");
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
