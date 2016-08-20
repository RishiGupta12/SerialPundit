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

package test89;

import java.io.File;
import java.util.concurrent.Executors;

import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComSystemProperty;
import com.serialpundit.serial.SerialComManager;
import com.serialpundit.serial.SerialComManager.BAUDRATE;
import com.serialpundit.serial.SerialComManager.DATABITS;
import com.serialpundit.serial.SerialComManager.FLOWCONTROL;
import com.serialpundit.serial.SerialComManager.FTPPROTO;
import com.serialpundit.serial.SerialComManager.FTPVAR;
import com.serialpundit.serial.SerialComManager.PARITY;
import com.serialpundit.serial.SerialComManager.STOPBITS;
import com.serialpundit.serial.ftp.ISerialComYmodemProgress;
import com.serialpundit.serial.ftp.SerialComFTPCMDAbort;

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

// send file from one thread and receive from other using XMODEM checksum protocol 
public class Test89 implements ISerialComYmodemProgress {

	public static SerialComManager scm = null;
	public static SerialComPlatform scp = null;
	public static String PORT1 = null;
	public static String PORT2 = null;
	public static String sndtxt1 = null;
	public static String sndtxt2 = null;
	public static String sndtxt3 = null;
	public static String sndtxt4 = null;
	public static String rcvtxt1 = null;
	public static String rcvtxt2 = null;
	public static String rcvtxt3 = null;
	public static String sndjpg1 = null;
	public static String sndjpg2 = null;
	public static String rcvjpg1 = null;
	public static String rcvjpg2 = null;
	public static String rcvdiry1 = null;
	public static String rcvdiry3 = null;
	public static String rcvdiry2 = null;
	public static volatile boolean done = false;
	public static Test89 test89 = new Test89();

	public static void main(String[] args) {
		try {
			scm = new SerialComManager();
			scp = new SerialComPlatform(new SerialComSystemProperty());

			int osType = scp.getOSType();
			if(osType == SerialComPlatform.OS_LINUX) {
				PORT1 = "/dev/ttyUSB0";
				PORT2 = "/dev/ttyUSB1";
				sndtxt1 = "/home/r/ws-host-uart/ftptest/f1.txt";
				sndtxt2 = "/home/r/ws-host-uart/ftptest/f2.txt";
				sndtxt3 = "/home/r/ws-host-uart/ftptest/f3.txt";
				sndtxt4 = "/home/r/ws-host-uart/ftptest/f4.txt";
				sndjpg1 = "/home/r/ws-host-uart/ftptest/f1.jpg";
				sndjpg2 = "/home/r/ws-host-uart/ftptest/f2.jpg";
				rcvdiry1 = "/home/r/ws-host-uart/ftptest/rcvdiry1";
				rcvdiry2 = "/home/r/ws-host-uart/ftptest/rcvdiry2";
				rcvdiry3 = "/home/r/ws-host-uart/ftptest/rcvdiry3";
			}else if(osType == SerialComPlatform.OS_WINDOWS) {
				PORT1 = "COM51";
				PORT2 = "COM52";
			}else if(osType == SerialComPlatform.OS_MAC_OS_X) {
				PORT1 = "/dev/cu.usbserial-A70362A3";
				PORT2 = "/dev/cu.usbserial-A602RDCH";
			}else if(osType == SerialComPlatform.OS_SOLARIS) {
				PORT1 = null;
				PORT2 = null;
			}else{
			}

			Executors.newSingleThreadExecutor().execute(new Runnable() {
				@Override 
				public void run() {
					try {
						long handle1 = scm.openComPort(PORT1, true, true, true);
						scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
						scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
						File[] f = new File[3];
						f[0] = new File(sndtxt1);
						f[1] = new File(sndtxt2);
						f[2] = new File(sndtxt4);
						Thread.sleep(1000);
						boolean status1 = scm.sendFile(handle1, f, FTPPROTO.YMODEM, FTPVAR.CRC, true, test89, null);
						System.out.println("ASCII MODE sent txt status : " + status1);
						done = true;
						scm.closeComPort(handle1);
					}catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			long handle2 = scm.openComPort(PORT2, true, true, true);
			scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
			boolean status2 = scm.receiveFile(handle2, new File(rcvdiry1), FTPPROTO.YMODEM, FTPVAR.CRC, true, test89, null);
			System.out.println("ASCII MODE received status txt : " + status2);
			scm.closeComPort(handle2);

			while(done == false) { 
				Thread.sleep(10);
			}

			System.out.println("\n-------- Test1 done --------\n");

			done = false;

			Executors.newSingleThreadExecutor().execute(new Runnable() {
				@Override 
				public void run() {
					try {
						long handle3 = scm.openComPort(PORT1, true, true, true);
						scm.configureComPortData(handle3, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
						scm.configureComPortControl(handle3, FLOWCONTROL.NONE, 'x', 'x', false, false);
						File[] f = new File[3];
						f[0] = new File(sndtxt1);
						f[1] = new File(sndtxt2);
						f[2] = new File(sndtxt4);
						Thread.sleep(1000);
						boolean status3 = scm.sendFile(handle3, f, FTPPROTO.YMODEM, FTPVAR.CRC, false, test89, null);
						System.out.println("BINARY MODE sent txt status : " + status3);
						done = true;
						scm.closeComPort(handle3);
					}catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			long handle4 = scm.openComPort(PORT2, true, true, true);
			scm.configureComPortData(handle4, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle4, FLOWCONTROL.NONE, 'x', 'x', false, false);
			boolean status4 = scm.receiveFile(handle4, new File(rcvdiry2), FTPPROTO.YMODEM, FTPVAR.CRC, false, test89, null);
			System.out.println("BINARY MODE received status txt : " + status4);
			scm.closeComPort(handle4);

			while(done == false) { 
				Thread.sleep(10);
			}

			System.out.println("\n-------- Test2 done --------\n");

			done = false;

			Executors.newSingleThreadExecutor().execute(new Runnable() {
				@Override 
				public void run() {
					try {
						long handle5 = scm.openComPort(PORT1, true, true, true);
						scm.configureComPortData(handle5, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
						scm.configureComPortControl(handle5, FLOWCONTROL.NONE, 'x', 'x', false, false);
						File[] f = new File[2];
						f[0] = new File(sndjpg1);
						f[1] = new File(sndjpg2);
						Thread.sleep(1000);
						boolean status5 = scm.sendFile(handle5, f, FTPPROTO.YMODEM, FTPVAR.CRC, false, test89, null);
						System.out.println("BINARY MODE sent jpg status : " + status5);
						done = true;
						scm.closeComPort(handle5);
					}catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			long handle6 = scm.openComPort(PORT2, true, true, true);
			scm.configureComPortData(handle6, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle6, FLOWCONTROL.NONE, 'x', 'x', false, false);
			boolean status6 = scm.receiveFile(handle6, new File(rcvdiry3), FTPPROTO.YMODEM, FTPVAR.CRC, false, test89, null);
			System.out.println("BINARY MODE received status jpg : " + status6);
			scm.closeComPort(handle6);

			while(done == false) { 
				Thread.sleep(10);
			}

			System.out.println("\n-------- Test3 done --------\n");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onYmodemReceiveProgressUpdate(String name, long arg0, int arg1) {
		System.out.println("app receive : " + name + " : number : " + arg0 + " : percent " + arg1);
	}
	@Override
	public void onYmodemSentProgressUpdate(String name, long arg0, int arg1) {
		System.out.println("app sent : " + name + " : number : " + arg0 + " : percent " + arg1);
	}
}
