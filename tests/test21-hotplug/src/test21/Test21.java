/**
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


package test21;

import com.embeddedunveiled.serial.ISerialComUSBHotPlugListener;
import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.usb.SerialComUSB;

// event 2 indicates port removal, 1 indicates additional of port
class portWatcher implements ISerialComUSBHotPlugListener {

	@Override
	public void onUSBHotPlugEvent(int arg0) {
		System.out.println("event " + arg0);
	}

}

public class Test21 {
	public static void main(String[] args) {
		try {
			int handle = 0;
			SerialComManager scm = new SerialComManager();
			portWatcher pw = new portWatcher();

			//			handle = scm.registerUSBHotPlugEventListener(pw, SerialComUSB.DEV_ANY, SerialComUSB.DEV_ANY);

			System.out.println("registering");
			//			handle = scm.registerUSBHotPlugEventListener(pw, 0x0403, 0x6001, "A7036479");

//			handle = scm.registerUSBHotPlugEventListener(pw, 0x10C4, 0xEA60, "0001");

			handle = scm.registerUSBHotPlugEventListener(pw, 0x04d8, 0x00df, "0000980371");
			System.out.println("sleeping");
			Thread.sleep(50444400);

			System.out.println("unregsitering");
			scm.unregisterUSBHotPlugEventListener(handle);

			System.out.println("unregistered");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}