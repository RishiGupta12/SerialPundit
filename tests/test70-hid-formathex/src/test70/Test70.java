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

package test70;

import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.hid.SerialComRawHID;

public class Test70 {

	public static SerialComRawHID scrh = null;
	static SerialComPlatform scp;
	public static String PORT = null;
	public static String PORT1 = null;

	public static void main(String[] args) {
		try {
			scrh = new SerialComRawHID(null, null);
			System.out.println(scrh.formatReportToHexR("hello".getBytes(), " : "));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
