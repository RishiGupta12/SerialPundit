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

import java.util.concurrent.atomic.AtomicBoolean;

import com.serialpundit.core.util.SerialComUtil;
import com.serialpundit.core.util.RingArrayBlockingQueue;
import com.serialpundit.usb.ISerialComUSBHotPlugListener;
import com.serialpundit.usb.SerialComUSB;
import com.serialpundit.serial.SerialComManager;
import com.serialpundit.serial.SerialComManager.BAUDRATE;
import com.serialpundit.serial.SerialComManager.DATABITS;
import com.serialpundit.serial.SerialComManager.FLOWCONTROL;
import com.serialpundit.serial.SerialComManager.PARITY;
import com.serialpundit.serial.SerialComManager.STOPBITS;
import com.serialpundit.serial.ISerialComDataListener;

/**
 * This class focuses on communicating data with serial port.
 */
final class DataCommunicationHandler implements Runnable, ISerialComDataListener {

    private final SerialComManager scm;
    private final PortHandle phandle;
    private final AtomicBoolean isDevConnected;
    private final AtomicBoolean exitDataThread;
    private final Object dataLock;
    private final Object numByteLock;
    private long comPortHandle;
    private byte[] CMD = new byte[] { 0x41, 0x42, 0x43, 0x44, 0x45 };
    private byte[] dataBuffer = new byte[32];
    private int index = 0;
    private int totalNumberOfBytesReadTillNow = 0;
    private int q = 0;

    public DataCommunicationHandler(SerialComManager scm, AtomicBoolean exitDataThread, Object dataLock, 
            AtomicBoolean isDevConnected, PortHandle phandle, Object numByteLock) {
        this.scm = scm;
        this.exitDataThread = exitDataThread;
        this.dataLock = dataLock;
        this.isDevConnected = isDevConnected;
        this.phandle = phandle;
        this.numByteLock = numByteLock;
    }

    // In this run() method we can write our application logic, like process data received 
    // from serial port and display result on screen or transmit it any radio.
    // 1. When this thread should be terminated, other thread will make exitDataThread true.
    // 2. This thread wait on dataLock until other thread notifies it. Typically when a serial 
    // device is added USB thread will notify this thread.
    // 3. When a serial device is removed, inner while loop will be stopped and this thread will
    // wait in outer while loop using dataLock again until serial device is added in system again.
    @Override
    public void run() {
        while(exitDataThread.get() == false) {
            try {
                synchronized(dataLock) {
                    dataLock.wait();
                }

                // Check if this thread is asked to exit.
                if(exitDataThread.get() == true) {
                    return;
                }

                // Serial device is opened and closed by USB Hot plug thread. Handle of serial port
                // is shared between two threads via PortHandle object.
                comPortHandle = phandle.getComPortHandle();

                q = 0; // reset.
                while(isDevConnected.get() == true) {
                    try {
                        // Try to send command to serial device (Host --> Device). There are reasons why this write may fail.
                        // (1) Serial device is unplugged. In this case, isDevConnected will be set to false and this while
                        // loop will be stopped. (2) Any I/O error occurs. In this case we try 10 times to send command to device
                        // and if we fail 10 times, we may treat it as fatal error and exit the application. Strategy to handle
                        // these 2 types of error is completely upto application developer.
                        if(q < 10) {
                            try {
                                scm.writeBytes(comPortHandle, CMD);
                            } catch (Exception e) {
                                System.out.println("Write status error : " + e.getMessage());
                                try {
                                    // Give sufficient time to hot plug thread, so that it can detect and update variables to tell
                                    // that device has been unplugged. This time can be tweaked as per application requirements.
                                    Thread.sleep(200);
                                } catch(Exception e1) {
                                }
                                q++;
                                continue;
                            }
                        }else {
                            System.out.println("Number of tries to send command to serial device = " + q);
                            break;
                        }

                        // Wait till serial device sends 15 or more bytes to host.
                        synchronized(numByteLock) {
                            numByteLock.wait();
                        }

                        // We were waiting for 15 bytes, but before they come serial device
                        // was removed, as a result of which numByteLock.notify was invoked.
                        // If came out if waiting due to device removal, break out of inner while loop.
                        if(isDevConnected.get() == false) {
                            break;
                        }

                        // Print data received from serial port in hexadecimal format.
                        if(totalNumberOfBytesReadTillNow >= 15) {
                            System.out.println("Response read : " + SerialComUtil.byteArrayToHexString(dataBuffer, " "));
                        }
                    } catch (Exception e) {
                        // This e.printStackTrace is kept for application debugging only. It may be removed once
                        System.out.println("Read/Write status error : " + e.getMessage());

                        // Most probably user unplugged device, so let hot plug thread give chance to run and  
                        // update appropriate variables. This also handles the case where user plugs in device 
                        // and very soon unplugs it.
                        try {
                            Thread.sleep(300);
                        } catch (Exception e1) {
                        }
                    } 
                }
                System.out.println("Data exchange thread looping back to wait for device !");
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("Data exchange thread exiting !");
    }

    @Override
    public void onDataListenerError(int errorNum) {
        // Comment/Uncomment for production/debugging.
        System.out.println("onDataListenerError : " + errorNum);
    }

    // This method is invoked by a dedicated native thread for this serial port.
    @Override
    public void onNewSerialDataAvailable(byte[] data) {

        // Buffer data until 15 bytes has been received
        if(totalNumberOfBytesReadTillNow < 15) {
            for(int x=0; x < data.length; x++) {
                dataBuffer[index] = data[x];
                index++;
            }
            totalNumberOfBytesReadTillNow = totalNumberOfBytesReadTillNow + data.length;
        }

        // If 15 (complete frame) or more bytes have been received, process them.
        if(totalNumberOfBytesReadTillNow >= 15) {
            synchronized(numByteLock) {
                numByteLock.notify();
            }

            // reset for next iteration
            index = 0;
            totalNumberOfBytesReadTillNow = 0;
        }
    }
}

/** 
 * This class focuses on listening for USB hot-plug events and taking appropriate actions.
 */
final class ConcreteUSBHotPlugEventHandler implements Runnable, ISerialComUSBHotPlugListener {

    /* MODIFY VID AND PID AS PER YOUR USB-UART DEVICE. This example is for FTDI FT232R. */
    private final int PRODUCT_VID = 0x0403; // FT232
    private final int PRODUCT_PID = 0x6001;
    //	private final int PRODUCT_VID = 0x10C4; // CP2102
    //	private final int PRODUCT_PID = 0xEA60;
    //private final int PRODUCT_VID = 0x04D8; // MCP2200
    //private final int PRODUCT_PID = 0x00DF;
    /* ******************************************************************************** */

    private final SerialComManager scm;
    private final SerialComUSB scusb;
    private String[] comPorts;
    private String openedComPortNode;
    private DataCommunicationHandler dch;
    private Thread dataCommunicatorThread;
    private long comPortHandle = -1; // initial value.
    private PortHandle phandle;
    private int hotplugEvent;
    private int eventSerialize = 1; // begin with add event.
    private AtomicBoolean isDevConnected = new AtomicBoolean(false);
    private final AtomicBoolean exitHotThread;
    private AtomicBoolean exitDataThread = new AtomicBoolean(false);
    private final Object lockApp;
    private Object dataLock = new Object();
    private final Object numByteLock = new Object();
    private static RingArrayBlockingQueue<Integer> mUSBhotplugEventQueue = new RingArrayBlockingQueue<Integer>(512);

    public ConcreteUSBHotPlugEventHandler(AtomicBoolean exitHotThread, Object lockApp) throws Exception {
        this.exitHotThread = exitHotThread;
        this.lockApp = lockApp;
        scm = new SerialComManager();
        scusb = new SerialComUSB(null, null);
    }

    // The onUSBHotPlugEvent() must return as soon as possible, therefore USB hot plug events are handled
    // in a separate worker thread context. This just enqueues event and return to JNI layer.
    @Override
    public void onUSBHotPlugEvent(int event, int usbvid, int usbpid, String serialNumber) {
        try {
            mUSBhotplugEventQueue.offer(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 1. If the device is added/removed after hot plug listener has been registered, it will 
    //    be detected by hot plug listener.
    // 2. If the hot plug listener has not started and device is added into system, it will be
    //    detected by isUSBDevConnected() method.
    // 3. In worst case race conditions if two add or two remove events get enqueued in queue,
    //    they will be handled using eventSerialize variable.
    // 4. If user plugs, unplugs and re-plugs device very fast, hotplug thread may detect all 
    //    these events but data handler thread may not get chance to actually open device. This 
    //    is handled by checking comPortHandle != -1 condition at appropriate places.
    @Override
    public void run() {

        // First register USB hot plug event listener and then enumerate devices. If the desired 
        // USB CDC device is already connected to system insert an add event in queue.
        try {
            scusb.registerUSBHotPlugEventListener(this, PRODUCT_VID, PRODUCT_PID, null);
            if(scusb.isUSBDevConnected(PRODUCT_VID, PRODUCT_PID, null)) {
                mUSBhotplugEventQueue.offer(SerialComUSB.DEV_ADDED);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Keep looping until instructed to terminate this thread.
        System.out.println("Application started !");

        phandle = new PortHandle(comPortHandle);
        dch = new DataCommunicationHandler(scm, exitDataThread, dataLock, isDevConnected, phandle, numByteLock);
        dataCommunicatorThread = new Thread(dch);
        dataCommunicatorThread.start();

        // Keep looping until instructed to exit.
        while(exitHotThread.get() == false) {
            try {
                // Until next USB hot plug event comes, this will block indefinitely.
                hotplugEvent = mUSBhotplugEventQueue.take();

                if(hotplugEvent == SerialComUSB.DEV_ADDED) {
                    if((eventSerialize == 1) && (comPortHandle == -1)) {
                        // Give some time to operating system so that device nodes gets created 
                        // and given correct permissions. Ubuntu 12.04 was faster than Ubuntu 16.04.
                        // May be because of systemd there is more delay.
                        Thread.sleep(1400);

                        comPorts = scusb.findComPortFromUSBAttributes(PRODUCT_VID, PRODUCT_PID, null);
                        if(comPorts.length > 0) {
                            // Open and configure serial port.
                            openedComPortNode = comPorts[0];
                            System.out.println("Found serial port : " + openedComPortNode);
                            try {
                                comPortHandle = scm.openComPort(openedComPortNode, true, true, true);
                                scm.configureComPortData(comPortHandle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
                                scm.configureComPortControl(comPortHandle, FLOWCONTROL.NONE, 'x', 'x', false, false);
                                phandle.setComPortHandle(comPortHandle);

                                // Update global connection state.
                                isDevConnected.set(true);
                                System.out.println("Opened serial port : " + openedComPortNode);

                                // after starting data communicator thread, register data listener.
                                scm.registerDataListener(comPortHandle, dch);

                                // specify remove as next event to execute.
                                eventSerialize = 0;

                                // Make data handler thread run and communicate with the serial device added.
                                synchronized(dataLock) {
                                    dataLock.notify();
                                }
                            } catch (Exception e6) {
                                System.out.println("Open/Configure : " + e6.getMessage());
                            }
                        }
                    }
                }else if(hotplugEvent == SerialComUSB.DEV_REMOVED) {
                    // If the device is unplugged from system, close handle.
                    if((eventSerialize == 0) && (comPortHandle != -1)) {
                        try {
                            isDevConnected.set(false);
                            synchronized(numByteLock) {
                                numByteLock.notify();
                            }

                            scm.unregisterDataListener(comPortHandle, dch);

                            scm.closeComPort(comPortHandle);
                            System.out.println("Closed serial port : " + openedComPortNode + "\n");
                        } catch (Exception e) {
                            // Closing port which no longer exist may throw exception. Just ignore it.
                            // We have to close the port to release resources.
                            System.out.println("Closing port after device removal error : " + e.getMessage());
                        }
                        // specify add as next event to execute.
                        comPortHandle = -1; // reset.
                        eventSerialize = 1; // specify add as next event to execute.
                    }
                }else {
                    // do nothing.
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Knowingly give chance to other thread to run so that it can react to this event also as fast as possible.
            Thread.yield();
        }
    }
}

/** 
 * Entry point to this application.
 */
public final class ComListenerDataReadApplication {

    private static final Object lockApp = new Object();
    private static AtomicBoolean exitHotThread = new AtomicBoolean(false);

    public static void main(String[] args) {
        try {			
            Thread t = new Thread( new ConcreteUSBHotPlugEventHandler(exitHotThread, lockApp));
            t.start();

            synchronized (lockApp) {
                lockApp.wait();
            }
            // Unregister USB hot plug listener when application exits.
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
