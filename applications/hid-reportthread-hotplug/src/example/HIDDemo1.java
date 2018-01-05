/*
 * This file is part of SerialPundit.
 * 
 * Copyright (C) 2014-2018, Rishi Gupta. All rights reserved.
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

import com.serialpundit.core.SerialComException;
import com.serialpundit.core.util.RingArrayBlockingQueue;
import com.serialpundit.usb.ISerialComUSBHotPlugListener;
import com.serialpundit.usb.SerialComUSB;
import com.serialpundit.usb.SerialComUSBHID;
import com.serialpundit.usb.SerialComUSBHIDdevice;
import com.serialpundit.hid.SerialComHID;
import com.serialpundit.hid.SerialComRawHID;

/** 
 * This class send output report to HID device and receive input report from HID device.
 * It executes as a worker thread.
 */
final class HIDReportHandler implements Runnable {

    /* ***** Modify these variables as per your HID device/application requirement ****** */
    private byte[] inputReportBuffer  = new byte[16];
    private byte[] outputReportBuffer = new byte[16];
    /* ************************************************************************************ */

    private final SerialComRawHID scrh;
    private AtomicBoolean isDevConnected;
    private long blockingContext;
    private BlockingContext blockingContextObj;
    private long hidDevHandle;
    private HIDDevHandle devHandle;
    private int ret = 0;
    private AtomicBoolean exitDataThread;
    private Object dataLock;
    private int q = 0;

    public HIDReportHandler(SerialComRawHID scrh, AtomicBoolean isDevConnected, BlockingContext blockingContextObj, 
            AtomicBoolean exitDataThread, Object dataLock, HIDDevHandle devHandle) {
        this.scrh = scrh;
        this.isDevConnected = isDevConnected;
        this.blockingContextObj = blockingContextObj;
        this.exitDataThread = exitDataThread;
        this.dataLock = dataLock;
        this.devHandle = devHandle;
    }

    @Override
    public void run() {

        while(exitDataThread.get() == false) {
            try {
                synchronized(dataLock) {
                    // Wait until device is plugged into system and this thread is informed.
                    dataLock.wait();
                }

                // Check if this thread is asked to exit.
                if(exitDataThread.get() == true) {
                    return;
                }

                // Probably HID device is plugged into system.
                blockingContext = blockingContextObj.getBlockingContext();
                hidDevHandle = devHandle.getHIDDevHandle();

                try {
                    // { You can write your application logic here } //

                    // Till the time HID device is present in system, do application specific job.
                    // This example send command to MCP2200 and then wait for response (input report).
                    // Upon reception it will print input report on console.
                    q = 0; // reset.
                    while(isDevConnected.get() == true) {

                        // Try to send command to HID device (Host --> Device). There are reasons why this write may fail.
                        // (1) HID device is unplugged. In this case, isDevConnected will be set to false and this while
                        // loop will be stopped. (2) Any I/O error occurs. In this case we try 10 times to send command to device
                        // and if we fail 10 times, we may treat it as fatal error and exit the application. Strategy to handle
                        // these 2 types of error is completely upto application developer.
                        if(q < 10) {
                            try {
                                outputReportBuffer[0] = (byte) 0x80;
                                scrh.writeOutputReportR(hidDevHandle, (byte) -1, outputReportBuffer);
                            } catch (Exception e) {
                                System.out.println("writeOutputReportR status error : " + e.getMessage());
                                try {
                                    // Give sufficient time to hot plug thread, so that it can detect and update variables to tell
                                    // that device has been unplugged. This time can be tweaked as per application requirements.
                                    // Because we loop back from this catch block, while loop condition will check again for 
                                    // device presence.
                                    Thread.sleep(200);
                                } catch(Exception e1) {
                                }
                                q++;
                                continue;
                            }
                        }else {
                            System.out.println("Number of tries to send command to HID device = " + q);
                            break;
                        }

                        try {
                            ret = 0;
                            ret = scrh.readInputReportR(hidDevHandle, inputReportBuffer, blockingContext);
                        } catch (SerialComException e2) {
                            if (SerialComHID.EXP_UNBLOCK_HIDIO.equals(e2.getExceptionMsg())) {
                                // Another thread unblocked I/O operation. Check if this thread should exit.
                                if(isDevConnected.get() == false) {
                                    break;
                                }
                            }
                        } catch (Exception e2) {
                            System.out.println("readInputReportR status error : " + e2.getMessage());
                        }

                        // If input report was actually read from HID device print it on console.
                        // Checking ret value helps in checking against occurrence of SerialComHID.EXP_UNBLOCK_HIDIO
                        // exception.
                        if (ret > 0) {
                            System.out.println("Input report : " + scrh.formatReportToHexR(inputReportBuffer, " "));
                        }

                        // Rate limit console messages for this example application.
                        Thread.sleep(2500);
                    }
                    System.out.println("HIDReportHandler thread looping back to wait for HID device !");
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("HIDReportHandler thread exited after an exception occured !\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("HIDReportHandler thread exited !");
    }
}

/** 
 * This class look for USB hot plug events and take appropriate actions as per add or remove event.
 * It executes as a worker thread.
 */
final class USBhotplugConcreteEventHandler implements Runnable, ISerialComUSBHotPlugListener {

    /* ***** Modify these variables as per your HID device/application requirement ****** */
    private final int PRODUCT_VID = 0x04d8; // MCP2200
    private final int PRODUCT_PID = 0x00df;
    /* ********************************************************************************** */

    private final SerialComUSB scusb;
    private final SerialComRawHID scrh;
    private final SerialComUSBHID scuh;
    private Thread dataHandlerThread;
    private HIDReportHandler dataHandler;
    private long blockingContext = 0;
    private int hotPlugEvent;
    private String hidDevNode;
    private long hidDevHandle = -1; // initial value.
    private HIDDevHandle devHandle;
    private int eventSerialize = 1; // begin with add event.
    private RingArrayBlockingQueue<Integer> mUSBhotplugEventQueue = new RingArrayBlockingQueue<Integer>(1024);
    private AtomicBoolean isDevConnected = new AtomicBoolean(false);
    private AtomicBoolean exitHotPlugThread;
    private AtomicBoolean exitDataThread = new AtomicBoolean(false);
    private Object dataLock = new Object();
    private SerialComUSBHIDdevice[] usbHidDevicesPresent;
    private BlockingContext blockingContextObj = new BlockingContext(blockingContext);

    public USBhotplugConcreteEventHandler(AtomicBoolean exitHotPlugThread) throws Exception {
        scusb = new SerialComUSB(null, null);
        scrh = new SerialComRawHID(null, null);
        scuh = scusb.getUSBHIDTransportInstance();
        this.exitHotPlugThread = exitHotPlugThread;
    }

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
    // 4. If user plugs, unplugs and re-plugs device very fast, hotplug thread may detect all these
    //    events but data handler thread may not get chance to actually open device. This is
    //    handled by checking hidDevHandle != -1 condition at appropriate places.
    @Override
    public void run() {

        // First register USB hot plug event listener and then enumerate devices. If the desired USB
        // HID device is already connected to system insert an add event in queue.
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

        devHandle = new HIDDevHandle(hidDevHandle);
        dataHandler = new HIDReportHandler(scrh, isDevConnected, blockingContextObj, exitDataThread, dataLock, devHandle);
        dataHandlerThread = new Thread(dataHandler);
        dataHandlerThread.start();

        while (exitHotPlugThread.get() == false) {
            try {
                // Until there is a pending USB hot plug event in the queue, this will block indefinitely.
                hotPlugEvent = mUSBhotplugEventQueue.take();

                if(hotPlugEvent == SerialComUSB.DEV_ADDED) {
                    if((eventSerialize == 1) && (hidDevHandle == -1)) {

                        /* Give some time to operating system so that device nodes
                         * gets created and is assigned correct permissions etc.*/
                        Thread.sleep(500);						

                        // find the device node assigned to our device and prepare it for communication.
                        try {
                            usbHidDevicesPresent = scuh.listUSBHIDdevicesWithInfo(PRODUCT_VID);
                            if(usbHidDevicesPresent.length > 0) {
                                for(int x=0; x < usbHidDevicesPresent.length; x++) {
                                    if((usbHidDevicesPresent[x].getVendorID() == PRODUCT_VID) && (usbHidDevicesPresent[x].getProductID() == PRODUCT_PID)) {

                                        hidDevNode = usbHidDevicesPresent[x].getDeviceNode();
                                        hidDevHandle = scrh.openHidDeviceR(hidDevNode, true);
                                        devHandle.setHIDDevHandle(hidDevHandle);

                                        // Create blocking context and start report handler.
                                        blockingContext = scrh.createBlockingHIDIOContextR();
                                        blockingContextObj.setBlockingContext(blockingContext);

                                        // first set isDevConnected to true and then notify other thread.
                                        isDevConnected.set(true);

                                        // specify remove as next event to execute.
                                        eventSerialize = 0;

                                        synchronized(dataLock) {
                                            // Make report handler thread run and communicate with device added.
                                            dataLock.notify();
                                        }

                                        System.out.println("Opened HID device node : " + hidDevNode);
                                        break;
                                    }
                                }
                            }
                        } catch (Exception e6) {
                            System.out.println("Open : " + e6.getMessage());
                        }
                    }
                }if (hotPlugEvent == SerialComUSB.DEV_REMOVED) {
                    if((eventSerialize == 0) && (hidDevHandle != -1)) {
                        try {
                            // Update state of global variable to notify all users of this variable.
                            // This will cause while loop in data handler thread to break.
                            isDevConnected.set(false);

                            // We may add some delay here to give other threads to terminate or some synchronization 
                            // may be considered here. Then only proceed to close the HID device actually. In this 
                            // example sending/receiving reports is done inside try-catch block and isDevConnected
                            // is used to terminate other threads, so no delay required.

                            // This will cause the blocked read in data handler thread to get unblocked and return.
                            scrh.unblockBlockingHIDIOOperationR(blockingContext);
                            scrh.destroyBlockingIOContextR(blockingContext);
                            scrh.closeHidDeviceR(hidDevHandle);
                            System.out.println("Closed HID device : " + hidDevNode);
                        } catch (Exception e) {
                            // Closing device handle which no longer exist may throw exception. Just ignore it.
                            // We have to close the device handle to release resources.
                            System.out.println("Closing HID device after removal error : " + e.getMessage());
                        }
                        hidDevHandle = -1; // reset.
                        eventSerialize = 1; // specify add as next event to execute.
                    }
                }else {
                    // do nothing
                }

                // Give explicit chance to other threads to run, so they can react to this event also as soon as possible.
                Thread.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

/** 
 * Entry point to this application.
 */
public final class HIDDemo1 {

    private static final Object lock = new Object();
    private static AtomicBoolean exitHotPlugThread = new AtomicBoolean(false);

    /* 
     * When the application exits pay attention to cleaning up :
     * 1. destroy blocking context.
     * 2. close device handle.
     * 3. terminate data handler thread.
     * 4. unregister usb hot plug listener.
     * 5. terminate usb hot plug event listener thread.
     */
    public static void main(String[] args) {
        try {
            Thread t = new Thread( new USBhotplugConcreteEventHandler(exitHotPlugThread));
            t.start();

            // wait forever.
            synchronized(lock) {
                lock.wait();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
