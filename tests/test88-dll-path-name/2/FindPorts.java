/**
 * Author : Rishi Gupta
 * 
 * This file is part of 'serial communication manager' library.
 * Copyright (C) <2014-2016>  <Rishi Gupta>
 *
 * This 'serial communication manager' is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by the Free Software 
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * The 'serial communication manager' is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR 
 * A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with 'serial communication manager'.  If not, see <http://www.gnu.org/licenses/>.
 */
 
import com.embeddedunveiled.serial.SerialComManager;

public class FindPorts {
	public static void main(String[] args) {
	 	try {
			System.out.println("Executing FindPorts application 2");
			SerialComManager scm = new SerialComManager(System.getProperty("user.home"), "lib2");
			String[] ports = scm.listAvailableComPorts();
			for(String port: ports){
				System.out.println(port);
			}
			// give time so that another instance of SCM gets created via another shell script and these
			// 2 apps exist together in system.
			Thread.sleep(1000);
			System.out.println("Exit 2");
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}
