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

package test89;

import java.io.File;

import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.SerialComManager.BAUDRATE;
import com.embeddedunveiled.serial.SerialComManager.DATABITS;
import com.embeddedunveiled.serial.SerialComManager.FLOWCONTROL;
import com.embeddedunveiled.serial.SerialComManager.FTPPROTO;
import com.embeddedunveiled.serial.SerialComManager.FTPVAR;
import com.embeddedunveiled.serial.SerialComManager.PARITY;
import com.embeddedunveiled.serial.SerialComManager.STOPBITS;
import com.embeddedunveiled.serial.ftp.ISerialComYmodemProgress;
import com.embeddedunveiled.serial.ftp.SerialComFTPCMDAbort;

class AbortTest implements Runnable {

    SerialComFTPCMDAbort abort = null;

    public AbortTest(SerialComFTPCMDAbort bb) {
        abort = bb;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("=======ABORTING !======");
        abort.abortTransfer();
    }
}

class Send extends Test89 implements Runnable, ISerialComYmodemProgress {
    
    public SerialComFTPCMDAbort transferStatea = new SerialComFTPCMDAbort();

    @Override
    public void run() {
        try {
            long handle1 = scm.openComPort(PORT1, true, true, true);
            scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
            scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);

//            new Thread(new AbortTest(transferStatea)).start();

            File[] f = new File[2];
            f[1] = new File(sndbfilepath);
            f[0] = new File(sndtfilepath);
            boolean statusc = scm.sendFile(handle1, f, FTPPROTO.YMODEM, FTPVAR.CRC, false, this, transferStatea);
            System.out.println("\nsent text status : " + statusc);

            done = true;

//            f[0] = new File(sndbfilepath);
//            boolean statusb = scm.sendFile(handle1, f, FTPPROTO.YMODEM, FTPVAR.CRC, false, this, transferStatea);
//            System.out.println("\nsent binary status : " + statusb);

            scm.closeComPort(handle1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onYmodemSentProgressUpdate(String name, long arg0, int arg1) {
        System.out.println("send : " + name + " : number : " + arg0 + " : percent " + arg1);
    }
    @Override
    public void onYmodemReceiveProgressUpdate(String name, long arg0, int arg1) {	
    }
}

// send file from one thread and receive from other using XMODEM checksum protocol 
public class Test89 implements ISerialComYmodemProgress {

    private static SerialComFTPCMDAbort transferStatec = new SerialComFTPCMDAbort();
    private static Thread mThread = null;
    public static SerialComManager scm = null;
    public static String PORT = null;
    public static String PORT1 = null;
    public static String sndtfilepath = null;
    public static String rcvtfilepath = null;
    public static String sndbfilepath = null;
    public static String rcvbfilepath = null;
    public static boolean done = false;

    public static void main(String[] args) {
        try {
            scm = new SerialComManager();

            int osType = scm.getOSType();
            if(osType == SerialComManager.OS_LINUX) {
                PORT = "/dev/ttyUSB0";
                PORT1 = "/dev/ttyUSB1";
                sndtfilepath = "/home/r/tmp/btsnd.txt";
                rcvtfilepath = "/home/r/tmp/rcvdir";
                sndbfilepath = "/home/r/tmp/bbsnd.jpg";
                rcvbfilepath = "/home/r/tmp/rcvdir";
            }else if(osType == SerialComManager.OS_WINDOWS) {
                PORT = "COM51";
                PORT1 = "COM52";
                sndtfilepath = "D:\\atsnd.txt";
                rcvtfilepath = "D:\\atrcv.txt";
            }else if(osType == SerialComManager.OS_MAC_OS_X) {
                PORT = "/dev/cu.usbserial-A70362A3";
                PORT1 = "/dev/cu.usbserial-A602RDCH";
            }else if(osType == SerialComManager.OS_SOLARIS) {
                PORT = null;
                PORT1 = null;
            }else{
            }

            PORT = "/dev/pts/2";
            PORT1 = "/dev/pts/3";

            long handle = scm.openComPort(PORT, true, true, true);
            scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
            scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);

            mThread = new Thread(new Send());
            mThread.start();

            //			new Thread(new AbortTest(transferStatec)).start();

            // ascii text mode
            boolean status = scm.receiveFile(handle, new File(rcvtfilepath), FTPPROTO.YMODEM, FTPVAR.CRC, false, new Test89(), transferStatec);
            System.out.println("\nreceived status text : " + status);

            while(done == false) { 
                Thread.sleep(10);
            }

//            // binary mode
//            boolean statusa = scm.receiveFile(handle, new File(rcvbfilepath), FTPPROTO.YMODEM, FTPVAR.CRC, false, new Test89(), transferStatec);
//            System.out.println("\nreceived status binary : " + statusa);

            scm.closeComPort(handle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onYmodemReceiveProgressUpdate(String name, long arg0, int arg1) {
        System.out.println("receive : " + name + " : number : " + arg0 + " : percent " + arg1);
    }
    @Override
    public void onYmodemSentProgressUpdate(String name, long arg0, int arg1) {
    }
}
