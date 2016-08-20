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

package factorytest;

import com.serialpundit.usb.SerialComUSB;
import com.serialpundit.usb.ISerialComUSBHotPlugListener;
import com.serialpundit.serial.SerialComManager;
import com.serialpundit.serial.comdb.SerialComDBRelease;

/*
 * This application suggest a design through which testing can be automated or may reduce the overhead 
 * on tester for testing products which requires test to be run after the DUT has been plugged into system.
 * 
 * 1. As soon as DUT with given USB VID/PID is added/removed in system, tests cases are made to execute.
 * When a particular number of devices for example 1000 has been tested, application will exit itself 
 * automatically.
 * 
 * 2. Further, if testing device is like USB-UART cable, it also demonstrates releasing COM port numbers 
 * assigned by Windows operating system automatically when the DUT testing has finished.
 */

class HotPlugEventWatcher implements ISerialComUSBHotPlugListener {

    private int deviceTested = 0;
    final Object obj = new Object();

    @Override
    public void onUSBHotPlugEvent(int event, int usbvid, int usbpid, String serialNumber) {

        if(event == SerialComUSB.DEV_ADDED) {
            System.out.println("DUT added, running automated tests for this device !");

            // If 1000 devices has been tested, unregister hotplug listener otherwise wait for next DUT unit (device under test).
            if(deviceTested == 1000) {
                synchronized (obj) {
                    obj.notify();
                }
            }
            deviceTested++;

        }else if(event == SerialComUSB.DEV_REMOVED) {
            System.out.println("DUT removed, running tests, if any, to be run after device removal !");
        }else {
        }
    }
}

public class AutomatedFactoryTest extends HotPlugEventWatcher {

    public static void main(String[] args) {
        try {
            // CHANGE PRODUCT_VID and PRODUCT_PID to match your device VID/PID.
            int PRODUCT_VID = 0x0403;
            int PRODUCT_PID = 0x6001;

            SerialComUSB scusb = new SerialComUSB(null, null);
            SerialComManager scm = new SerialComManager();
            HotPlugEventWatcher hpew = new HotPlugEventWatcher();

            /*
             * Uncomment following coding lines if :
             * 1. Your operating system is Windows and
             * 2. You need to free COM port number assigned by Windows from Windows database
             * 
             * SerialComDBRelease scdbr = scm.getSerialComDBReleaseInstance(null, null);
             * scdbr.startSerialComDBReleaseSerive();
             */

            int handle = scusb.registerUSBHotPlugEventListener(hpew, PRODUCT_VID, PRODUCT_PID, null);

            System.out.println("Testing session started !");

            // wait till 1000 devices has been tested.
            synchronized (hpew.obj) {
                hpew.obj.wait();
            }

            /*
             * Uncomment following coding lines if scm.getSerialComDBReleaseInstance(null, null); was used.
             * scdbr.stopSerialComDBReleaseSerive();
             */

            scusb.unregisterUSBHotPlugEventListener(handle);
            System.out.println("Testing completed !");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
