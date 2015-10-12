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
			SerialComManager scm = new SerialComManager();
			portWatcher pw = new portWatcher();
			
//			scm.registerUSBHotPlugEventListener(pw, SerialComUSB.DEV_ANY, SerialComUSB.DEV_ANY);
			scm.registerUSBHotPlugEventListener(pw, 0x0403, 0x6001, "A7036479");
			Thread.sleep(3000);
			scm.unregisterUSBHotPlugEventListener(pw);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}