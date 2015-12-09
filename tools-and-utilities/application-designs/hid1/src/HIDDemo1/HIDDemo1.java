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

package HIDDemo1;

import com.embeddedunveiled.serial.ISerialComUSBHotPlugListener;
import com.embeddedunveiled.serial.SerialComException;
import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.hid.SerialComHID;
import com.embeddedunveiled.serial.hid.SerialComHIDdevice;
import com.embeddedunveiled.serial.hid.SerialComRawHID;
import com.embeddedunveiled.serial.usb.SerialComUSB;
import com.embeddedunveiled.serial.usb.SerialComUSBHID;

/* This class listens for device hot plug events */
final class HotPlugDeviceWatcher extends HIDApplication1 implements ISerialComUSBHotPlugListener {

	@Override
	public void onUSBHotPlugEvent(int event) {

		if(event == SerialComUSB.DEV_ADDED) {
			/* USB HID device is added to system */

			// User inserted HID device into system. If the HID device is not opened, then 
			// open it. If it is opened do nothing.
			if(hidAlreadyOpened != true) {
				try {
					// find and get information about our USB HID device
					SerialComHIDdevice[] usbHidDevicesPresent = scuh.listUSBHIDdevicesWithInfo(MY_VID);

					// loop over the list and match our VID and PID.
					for(int a=0; a < usbHidDevicesPresent.length; a++) {
						if((usbHidDevicesPresent[a].getVendorID() == MY_VID) && (usbHidDevicesPresent[a].getProductID() == MY_PID)) {
							// get the device node/path assigned by operating system to this HID device
							hidDevNode = usbHidDevicesPresent[a].getDeviceNode();
						}
					}

					// open HID device
					Thread.sleep(100); // give some time so that device node gets correct permissions
					hidDevHandle = scrh.openHidDeviceR(hidDevNode, true);
					hidAlreadyOpened = true;

					// Create and start thread that will read data from HID device
					dataReaderThread = new Thread(new HIDDataReader());
					dataReaderThread.start();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else {
				// do nothing
			}

		}else if(event == SerialComUSB.DEV_REMOVED) {
			/* USB HID device is removed from system */

			// If the HID device is removed, the handle become invalid, so close this handle.
			// We will open the device again when user plugin the device in computer again.
			try {
				scrh.closeHidDeviceR(hidDevHandle);
				hidAlreadyOpened = false;
				deviceRemoved = true;
			} catch (Exception e) {
				e.printStackTrace();
			}

		}else {
			// will not happen but just keep for future
		}
	}
}

/* This class listens for data from USB HID device */
final class HIDDataReader extends HIDApplication1 implements Runnable {
	@Override
	public void run() {
		try {
			// create context for blocking HID I/O operations
			context = scrh.createBlockingHIDIOContextR();

			// keep reading data until this thread is stopped
			while(true) {
				try {
					// read data from HID device, it will block if there  is no data
					ret = 0;
					ret = scrh.readInputReportR(hidDevHandle, inputReportBuffer, context);
				} catch (Exception e) {
					if(SerialComHID.EXP_UNBLOCK_HIDIO.equals(((SerialComException) e).getExceptionMsg())) {
						// this thread should exit as other thread told it to return
						return;
					}
				}

				// user removed HID device from system, eit this thread.
				// it will get created automatically when user insert device
				// in system again.
				if(deviceRemoved == true) {
					return;
				}

				// print data if read from HID device actually, readInputReportR method may return 
				// pre-maturally if user removed device from system while readInputReportR was 
				// blocked to read it.
				if(ret > 0) {
					System.out.println(scrh.formatReportToHexR(inputReportBuffer, " "));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class HIDApplication1 {

	/* ********************************************************* */
	// Replace MY_VID value and MY_PID value with your USB-IF VID and PID.
	protected static int MY_VID = 0x04d8;
	protected static int MY_PID = 0x00df;
	/* ********************************************************* */

	protected static SerialComManager scm;
	protected static SerialComRawHID scrh;
	protected static SerialComUSBHID scuh;
	protected static int hothandle;
	protected static long hidDevHandle;
	protected static HotPlugDeviceWatcher hpdw;
	protected static boolean hidAlreadyOpened;
	protected static String hidDevNode;
	protected static long context = 0;
	protected static int ret = 0;
	protected static Thread dataReaderThread = null;
	protected static boolean deviceRemoved = false;

	/* ************ */
	// Set buffer size as per your HID device, this example id for MCP2200.
	protected static byte[] inputReportBuffer = new byte[16];
	protected static byte[] outputReportBuffer = new byte[16];
	/* ************ */

	public void begin() {
		try {
			// Create instance of SCM library.
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

				// Create listener object and register it so that whenever HID device is removed from system, 
				// application gets notification.
				hpdw = new HotPlugDeviceWatcher();
				hothandle = scm.registerUSBHotPlugEventListener(hpdw, MY_VID, MY_PID, null);

				// Create and start thread that will read data from HID device
				dataReaderThread = new Thread(new HIDDataReader());
				dataReaderThread.start();
			}else {
				// Create listener object and register it. Whenever our HID device will be connected, 
				// listener will be called. HID device is then opened from listener.
				hidAlreadyOpened = false;
				hpdw = new HotPlugDeviceWatcher();
				hothandle = scm.registerUSBHotPlugEventListener(hpdw, MY_VID, MY_PID, null);
			}

			/* ************* YOUR LOGIC may be written in this block ********************** */
			// write your application logic here; do rest of things you want to do. 
			// 1. In this example, this applications sends a command to MCP2200 and sleeps. MCP2200 send 
			//    response which is printed by reader thread.
			// 2. On application exit call scm.unregisterUSBHotPlugEventListener(hothandle);
			// 3. On application exit call scrh.unblockBlockingHIDIOOperationR(context) and then call
			//    scrh.destroyBlockingIOContextR(context).
			// 4. On application exit call scrh.closeHidDeviceR(hidDevHandle);
			try {
				while(true) {
					Thread.sleep(1500);
					if(hidAlreadyOpened == true) {
						outputReportBuffer[0] = (byte) 0x80;
						ret = scrh.writeOutputReportR(hidDevHandle, (byte) -1, outputReportBuffer);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			/* **************************************************************************** */

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

/* Entry point for application */
public final class HIDDemo1 {

	private static HIDApplication1 hidapp;

	public static void main(String[] args) {
		try {
			hidapp = new HIDApplication1();
			hidapp.begin();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
