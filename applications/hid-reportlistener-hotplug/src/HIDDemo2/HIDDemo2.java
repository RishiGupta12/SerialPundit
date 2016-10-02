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

package HIDDemo2;

import java.util.concurrent.atomic.AtomicBoolean;

import com.serialpundit.core.util.RingArrayBlockingQueue;
import com.serialpundit.usb.ISerialComUSBHotPlugListener;
import com.serialpundit.usb.SerialComUSB;
import com.serialpundit.usb.SerialComUSBHID;
import com.serialpundit.usb.SerialComUSBHIDdevice;
import com.serialpundit.hid.IHIDInputReportListener;
import com.serialpundit.hid.SerialComRawHID;

/** 
 * This class focuses on sending output report to HID device (host-->device) and receiving
 * input reports from HID device (host<--device). The method onNewInputReportAvailable is 
 * called from a dedicated Java worker thread. So if the application is not interested in next
 * input report until it has finished processing current input report, there may not be any need 
 * to create one more thread for data handling. However, if the HID device sends input reports at 
 * a very fast rate than consider using onNewInputReportAvailable() method to just insert report 
 * in a queue and creating separate thread that will consume report from this queue and process it. 
 */
final class InputReportAndHotplugHandler implements Runnable, ISerialComUSBHotPlugListener, IHIDInputReportListener {

    /* ***** Modify these 4 variables as per your HID device/application requirement ****** */
    private final int PRODUCT_VID = 0x04d8;
    private final int PRODUCT_PID = 0x00df;
    // Size of inputReportBuffer must be equal to or greater than the maximum size 
    // of input report of the given HID device, it can send to host computer.
    private byte[] inputReportBuffer = new byte[16];
    private byte[] outputReportBuffer = new byte[16];
    /* ********************************************************************************** */

    private final SerialComUSB scusb;
    private final SerialComRawHID scrh;
    private final SerialComUSBHID scuh;
    private String hidDevNode;
    private final Object lock;
    private int hotplugEvent;
    private long hidDevHandle = -1; // initial value.
    private int eventSerialize = 1; // begin with add event.
    private final AtomicBoolean exitThread;
    private RingArrayBlockingQueue<Integer> mUSBhotplugEventQueue = new RingArrayBlockingQueue<Integer>(512);

    public InputReportAndHotplugHandler(AtomicBoolean exitThread, Object lock) throws Exception {
        this.exitThread = exitThread;
        this.lock = lock;
        scusb = new SerialComUSB(null, null);
        scrh = new SerialComRawHID(null, null);
        scuh = scusb.getUSBHIDTransportInstance();
    }

    // This method must return as soon as possible as operating system may need to do other things 
    // related to this event. Therefore, just enqueue event and return. Run() method will process
    // this hot plug event in separate thread context. This also ensures that hot plug events are 
    // processed in same order as they occurred in a complex multi-threaded environment and with 
    // various use cases.
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

        // First register USB hot plug event listener and then enumerate devices. If the desired 
        // USB HID device is already connected to system insert an add event in queue.
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

        while(true) {
            try {
                // Until next USB hot plug event comes this will block indefinitely.
                hotplugEvent = mUSBhotplugEventQueue.take();

                if(hotplugEvent == SerialComUSB.DEV_ADDED) {
                    if((eventSerialize == 1) && (hidDevHandle == -1)) {

                        // Give some time to operating system so that device nodes gets created 
                        // and given correct permissions.
                        Thread.sleep(500);

                        try {
                            // HID device is not opened, let us get it prepared for communication.
                            SerialComUSBHIDdevice[] usbHidDevicesPresent = scuh.listUSBHIDdevicesWithInfo(PRODUCT_VID);
                            if(usbHidDevicesPresent.length > 0) {
                                for(int x=0; x < usbHidDevicesPresent.length; x++) {
                                    if((usbHidDevicesPresent[x].getVendorID() == PRODUCT_VID) && (usbHidDevicesPresent[x].getProductID() == PRODUCT_PID)) {
                                        hidDevNode = usbHidDevicesPresent[x].getDeviceNode();
                                        hidDevHandle = scrh.openHidDeviceR(hidDevNode, true);
                                        System.out.println("Opened HID device : " + hidDevNode);

                                        scrh.registerInputReportListener(hidDevHandle, this, inputReportBuffer);
                                        System.out.println("Registered input report listener");

                                        // specify remove as next event to execute.
                                        eventSerialize = 0;

                                        outputReportBuffer[0] = (byte) 0x80;
                                        scrh.writeOutputReportR(hidDevHandle, (byte) -1, outputReportBuffer);
                                        break;
                                    }
                                }
                            }
                        } catch (Exception e6) {
                            System.out.println("Open : " + e6.getMessage());
                        }
                    }
                }else if(hotplugEvent == SerialComUSB.DEV_REMOVED) {
                    if((eventSerialize == 0) && (hidDevHandle != -1)) {
                        try {
                            scrh.unregisterInputReportListener(this);
                            scrh.closeHidDeviceR(hidDevHandle);
                            System.out.println("Closed HID device : " + hidDevNode + "\n");
                        } catch (Exception e) {
                            // Closing device handle which no longer exist may throw exception. Just ignore it.
                            // We have to close the device handle to release resources.
                            System.out.println("Closing HID device after removal error : " + e.getMessage());
                        }
                        hidDevHandle = -1; // reset.
                        eventSerialize = 1; // specify add as next event to execute.
                    }
                }else {
                    // do nothing.
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onNewInputReportAvailable(int numBytesInReport, byte[] inputReportBuffer) {
        try {
            // print input report on console.
            System.out.println("numBytesInReport : " + numBytesInReport + ", "
                    + "report data : " + scrh.formatReportToHexR(inputReportBuffer, " "));

            /* { PROCESS INPUT REPORT HERE } */

            // after processing input report if required send output report to HID device.
            Thread.sleep(2500);
            outputReportBuffer[0] = (byte) 0x80;
            if(hidDevHandle != -1) {
                scrh.writeOutputReportR(hidDevHandle, (byte) -1, outputReportBuffer);
                System.out.println("Sent output report : " + scrh.formatReportToHexR(outputReportBuffer, " "));
            }
        } catch (Exception e) {
            // If the HID device has been unplugged from system, writeOutputReportR() probably
            // may throw SerialComException. If the device is unplugged, USB hot plug listener
            // thread will close the HID device's handle and unregister this input report 
            // listener.
            System.out.println("Exception in onNewInputReportAvailable : " + e.getMessage());
        }
    }
    
    @Override
    public void onNewInputReportAvailableError(Exception e) {
        System.out.println("onNewInputReportAvailableError : " + e.getMessage());
    }
}

/** 
 * Entry point to this application.
 */
public final class HIDDemo2 {

    private static final Object lock = new Object();
    private static AtomicBoolean exitThread = new AtomicBoolean(false);

    /* 
     * Entry point to this application. On application exit :
     * 1. unregister input report listener.
     * 2. unregister usb hot plug listener.
     * 3. close hid device handle.
     * 4. notify to come out of wait state (lock.notify())
     * 5. terminate worker thread (existThread.set(true))
     */
    public static void main(String[] args) {
        try {
            Thread t = new Thread( new InputReportAndHotplugHandler(exitThread, lock));
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
