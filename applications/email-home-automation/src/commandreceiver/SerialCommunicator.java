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

package commandreceiver;

import com.serialpundit.core.SerialComException;
import com.serialpundit.serial.SerialComManager;

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

        if(command.equals("CMD1")) {
            try {
                scmi.writeString(comPortHandle, "CMD1", 0);
            } catch (SerialComException e) {
                progStatusPaneli.setExtraInfo(e.getMessage());
            }
        }else if(command.equals("CMD2")) {
            try {
                scmi.writeString(comPortHandle, "CMD2", 0);
            } catch (SerialComException e) {
                progStatusPaneli.setExtraInfo(e.getMessage());
            }
        }else {
        }
    }
}
