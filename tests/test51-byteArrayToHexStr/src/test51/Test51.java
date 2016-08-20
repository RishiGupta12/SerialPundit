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

package test51;

import com.serialpundit.core.util.SerialComUtil;

public class Test51 {
	public static void main(String[] args) {
		try {
			// output 48:45:4C:4C:4F:20:57:4F:52:4C:44
			String str = SerialComUtil.byteArrayToHexString("HELLO WORLD".getBytes(), ":");
			System.out.println(str);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}
