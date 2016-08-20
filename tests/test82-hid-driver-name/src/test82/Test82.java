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

package test82;

import com.serialpundit.core.SerialComException;
import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComSystemProperty;
import com.serialpundit.hid.SerialComRawHID;

public class Test82  {

	public static SerialComRawHID scrh = null;
	public static SerialComPlatform scp = null;
	public static int osType = 0;
	public static String PORT = null;
	public static String PORT1 = null;

	public static void main(String[] args) throws SerialComException {

		try {
			scrh = new SerialComRawHID(null, null);
			scp = new SerialComPlatform(new SerialComSystemProperty());
		} catch (Exception e) {
			e.printStackTrace();
		}

		osType = scp.getOSType();
		if(osType == SerialComPlatform.OS_LINUX) {
			PORT = "/dev/hidraw1";
		}else if(osType == SerialComPlatform.OS_WINDOWS) {
			PORT = "HID\\VID_04D8&PID_00DF&MI_02\\7&33842c3f&0&0000";
		}else if(osType == SerialComPlatform.OS_MAC_OS_X) {
			PORT = null;
		}else if(osType == SerialComPlatform.OS_SOLARIS) {
			PORT = null;
		}else{
		}

		if(osType == SerialComPlatform.OS_LINUX) {
			System.out.println("driver : "+ scrh.findDriverServingHIDDeviceR("/dev/hidraw1"));
		}else if(osType == SerialComPlatform.OS_WINDOWS) {
			// mcp2200
			System.out.println("driver : "+ scrh.findDriverServingHIDDeviceR("HID\\VID_04D8&PID_00DF&MI_02\\7&33842c3f&0&0000"));

			// windows mouse
			System.out.println("driver : "+ scrh.findDriverServingHIDDeviceR("HID\\VID_04CA&PID_0061\\6&35F47D18&0&0000"));
		}else if(osType == SerialComPlatform.OS_MAC_OS_X) {
		}else if(osType == SerialComPlatform.OS_SOLARIS) {
		}else{
		}
	}
}
