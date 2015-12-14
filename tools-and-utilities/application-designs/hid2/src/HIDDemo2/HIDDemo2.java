/*
 * Author : Rishi Gupta
 * 
 * This file is part of 'serial communication manager' library.
 *
 * The 'serial communication manager' is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by the Free Software 
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * The 'serial communication manager' is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with serial communication manager. If not, see <http://www.gnu.org/licenses/>.
 */

package HIDDemo2;

import com.embeddedunveiled.serial.ISerialComUSBHotPlugListener;
import com.embeddedunveiled.serial.SerialComException;
import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.hid.IHIDInputReportListener;
import com.embeddedunveiled.serial.hid.SerialComHID;
import com.embeddedunveiled.serial.hid.SerialComHIDdevice;
import com.embeddedunveiled.serial.hid.SerialComRawHID;
import com.embeddedunveiled.serial.usb.SerialComUSB;
import com.embeddedunveiled.serial.usb.SerialComUSBHID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/*
 * Executes in worker thread context and handles hot plug events. If the device is added, it will 
 * open it and register input report listener for it. If the device is removed it will unregister
 * input report listener, close the device.
 */
class USBhotplugConcreteEventHandler implements Runnable {

	/* ***** Modify these 4 variables as per your HID device/application requirement ****** */
	protected int MY_VID = 0x04d8;
	protected int MY_PID = 0x00df;
	protected byte[] inputReportBuffer = new byte[16];
	protected byte[] outputReportBuffer = new byte[16];
	/* ************************************************************************************ */

	protected SerialComManager scm;
	protected HIDDemo2 demo2;
	protected USBhotplugConcreteEventHandler uhceh;
	protected SerialComRawHID scrh;
	protected SerialComUSBHID scuh;
	protected String hidDevNode;
	protected long hidDevHandle;
	protected boolean hidAlreadyOpened;
	protected int hothandle;
	protected Thread hotPlugHandlerThread = null;
	protected BlockingQueue<Integer> mUSBhotplugEventQueue = new ArrayBlockingQueue<Integer>(1024);

	private int event = 0;
	
	public USBhotplugConcreteEventHandler() {
		uhceh = this;
	}

	@Override
	public void run() {
		while (true) {
			try {
				// until next hot plug event comes this will block
				event = mUSBhotplugEventQueue.take();

				if (event == SerialComUSB.DEV_ADDED) {
					/* USB HID device is added to system */

					/* Give some time to operating system so that device nodes
					 * gets created and is assigned correct permissions etc.*/
					Thread.sleep(500);

					// User inserted HID device into system. If the HID device
					// is not opened, then open it. If it is opened do nothing.
					if (hidAlreadyOpened != true) {
						try {
							// find and get information about our USB HID device
							SerialComHIDdevice[] usbHidDevicesPresent = scuh.listUSBHIDdevicesWithInfo(MY_VID);

							// loop over the list and match our VID and PID.
							for (int a = 0; a < usbHidDevicesPresent.length; a++) {
								if ((usbHidDevicesPresent[a].getVendorID() == MY_VID)
										&& (usbHidDevicesPresent[a].getProductID() == MY_PID)) {
									// get the device node/path assigned by
									// operating system to this HID device
									hidDevNode = usbHidDevicesPresent[a].getDeviceNode();
								}
							}

							// open HID device
							hidDevHandle = scrh.openHidDeviceR(hidDevNode, true);
							hidAlreadyOpened = true;

							// register input report listener.
							scrh.registerInputReportListener(hidDevHandle, demo2, inputReportBuffer);
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						// do nothing
					}

				} else if (event == SerialComUSB.DEV_REMOVED) {
					/* USB HID device is removed from system */

					// If the HID device is removed, the handle becomes invalid,
					// so close this handle. We will open the device again when user plugin 
					// the device in computer again.
					try {
						scrh.unregisterInputReportListener(demo2);
						scrh.closeHidDeviceR(demo2.hidDevHandle);
						hidAlreadyOpened = false;
					} catch (Exception e) {
						e.printStackTrace();
					}

				} else {
					// will not happen but just keep for future
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}

/* Entry point for application */
public final class HIDDemo2 extends USBhotplugConcreteEventHandler implements ISerialComUSBHotPlugListener, IHIDInputReportListener {
	
	public HIDDemo2() {
		super();
	}

	/* Entry point to this application. */
	public static void main(String[] args) {
		try {
			HIDDemo2 demo = new HIDDemo2();
			demo.demo2 = demo;
			demo.demo2.begin();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* This will be called by Java worker thread whenever an input report is sent by 
	 * HID device to computer. */
	@Override
	public void onNewInputReportAvailable(int numBytesInReport, byte[] inputReportBuffer) {
		try {
			System.out.println(scrh.formatReportToHexR(inputReportBuffer, " "));
		} catch (SerialComException e) {
			e.printStackTrace();
		}
	}

	/* This method must return as soon as possible. All the steps that must be
	 * executed should be executed in another thread. Here we notify other
	 * thread that a USB hot plug event has occurred and pass that thread event
	 * value in-directly. */
	@Override
	public void onUSBHotPlugEvent(int event) {
		mUSBhotplugEventQueue.offer(event);
	}

	private void begin() throws Exception {
		// Create instance of SCM library and HID classes.
		scm = new SerialComManager();
		scrh = (SerialComRawHID) scm.getSerialComHIDInstance(SerialComHID.MODE_RAW, null, null);
		scuh = (SerialComUSBHID) scrh.getHIDTransportInstance(SerialComHID.HID_USB);

		if(scm.isUSBDevConnected(MY_VID, MY_PID, null)) {

			// find and get information about our USB HID device
			SerialComHIDdevice[] usbHidDevicesPresent = scuh.listUSBHIDdevicesWithInfo(MY_VID);

			// loop over the list and match our VID and PID.
			for(int a=0; a < usbHidDevicesPresent.length; a++) {
				if((usbHidDevicesPresent[a].getVendorID() == MY_VID) && (usbHidDevicesPresent[a].getProductID() == MY_PID)) {
					// get the device node/path assigned by operating system to 
					// this HID device
					hidDevNode = usbHidDevicesPresent[a].getDeviceNode();
				}
			}

			// open HID device
			hidDevHandle = scrh.openHidDeviceR(hidDevNode, true);
			hidAlreadyOpened = true;

			// register input report listener.
			scrh.registerInputReportListener(hidDevHandle, this, inputReportBuffer);
		}else {
			hidAlreadyOpened = false;
		}

		// before registering hot plug listener start thread
		hotPlugHandlerThread = new Thread(uhceh);
		hotPlugHandlerThread.start();

		// register hot plug listener so that whenever HID device is removed from system, 
		// application gets notification.
		hothandle = scm.registerUSBHotPlugEventListener(this, MY_VID, MY_PID, null);

		/* ************* YOUR LOGIC may be written in this block ********************** */
		// write your application logic here; do rest of things you want to do. 
		// 1. In this example, this applications sends a command to MCP2200 and sleeps. MCP2200 send 
		//    response which is printed by reader thread.
		// 2. On application exit call scm.unregisterUSBHotPlugEventListener(hothandle);
		// 3. On application exit call scrh.unregisterInputReportListener(this);
		// 4. On application exit call scrh.closeHidDeviceR(hidDevHandle);
		while(true) {
			try {
				Thread.sleep(1500);
				if(hidAlreadyOpened == true) {
					outputReportBuffer[0] = (byte) 0x80;
					scrh.writeOutputReportR(hidDevHandle, (byte) -1, outputReportBuffer);
				}
			} catch (Exception e) {
				System.out.println("Did you removed device from system ? Please plug-in again to resume operations !");
			}
		}
		/* **************************************************************************** */
	}
}