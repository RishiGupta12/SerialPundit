/*
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

package commandreceiver;

import com.embeddedunveiled.serial.SerialComException;
import com.embeddedunveiled.serial.SerialComManager;

public final class SerialCommunicator {

	private final SerialComManager scmi;
	private final PortSettingsPanel psPaneli;
	private final ProgramStatusPanel progStatusPaneli;

	public SerialCommunicator(SerialComManager scm, PortSettingsPanel psPanel, ProgramStatusPanel progStatusPanel) {
		scmi = scm;
		psPaneli = psPanel;
		progStatusPaneli = progStatusPanel;
	}

	public void executeCommand(String command) {

		long comPortHandle = psPaneli.getComPortHandle();
		if(comPortHandle == -1) {
			progStatusPaneli.setExtraInfo("COM port is not opened");
			return;
		}

		switch(command) {
		case "CMD1":
			try {
				scmi.writeString(comPortHandle, "CMD1", 0);
			} catch (SerialComException e) {
				progStatusPaneli.setExtraInfo(e.getMessage());
			}
		case "CMD2":
			try {
				scmi.writeString(comPortHandle, "CMD2", 0);
			} catch (SerialComException e) {
				progStatusPaneli.setExtraInfo(e.getMessage());
			}
		default :
		}
	}
}
