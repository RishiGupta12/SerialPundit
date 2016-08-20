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

package test1;

import com.serialpundit.serial.SerialComManager;

/*
 * Must find :
 * - hw/sw virtual ports
 * - bluetooth dongle and 3G dongle
 * - port server
 * - USB-UART converter
 * - regular ports
 * - ports connected through USB hub/expander
 */
public class Test1 {
	static long a = 0;
	public static void main(String[] args) {
		try {
			SerialComManager scm = new SerialComManager();
			String[] ports = scm.listAvailableComPorts();
			for(String port: ports){
				System.out.println(port + " : " + port.length());
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}
