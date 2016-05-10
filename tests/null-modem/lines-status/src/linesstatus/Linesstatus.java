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

package linesstatus;

import java.io.IOException;

import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.SerialComManager.BAUDRATE;
import com.embeddedunveiled.serial.SerialComManager.DATABITS;
import com.embeddedunveiled.serial.SerialComManager.FLOWCONTROL;
import com.embeddedunveiled.serial.SerialComManager.PARITY;
import com.embeddedunveiled.serial.SerialComManager.STOPBITS;
import com.embeddedunveiled.serial.nullmodem.SerialComNullModem;

public class Linesstatus {

    static SerialComNullModem scnm = null;

    public static void main(String[] args) throws IOException {
        try {
            SerialComManager scm = new SerialComManager();
            scnm = scm.getSerialComNullModemInstance();
            String[] ports = scnm.createStandardNullModemPair(-1, -1);

            long handle = scm.openComPort(ports[0], true, true, true);
            scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
            scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);
            long handle1 = scm.openComPort(ports[1], true, true, true);
            scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
            scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);

            // reset everything and read initial state
            scm.setRTS(handle, false);
            scm.setDTR(handle, false);
            scm.setRTS(handle1, false);
            scm.setDTR(handle1, false);
            int[] state = scm.getLinesStatus(handle);
            System.out.println("CTS state = " + state[0]);
            System.out.println("DSR state = " + state[1]);
            System.out.println("CD state = " +  state[2]);
            System.out.println("RI state = " +  state[3] + "\n");
            state = scm.getLinesStatus(handle1);
            System.out.println("CTS state = " + state[0]);
            System.out.println("DSR state = " + state[1]);
            System.out.println("CD state = " +  state[2]);
            System.out.println("RI state = " +  state[3] + "\n");

            // test set RTS/DTR on 1st handle and check state on other end
            scm.setRTS(handle, true);
            scm.setDTR(handle, true);
            state = scm.getLinesStatus(handle1);
            System.out.println("CTS state = " + state[0]);
            System.out.println("DSR state = " + state[1]);
            System.out.println("CD state = " +  state[2]);
            System.out.println("RI state = " +  state[3] + "\n");

            // test set RTS/DTR on 1st handle and check state on other end
            scm.setRTS(handle1, true);
            scm.setDTR(handle1, true);
            state = scm.getLinesStatus(handle);
            System.out.println("CTS state = " + state[0]);
            System.out.println("DSR state = " + state[1]);
            System.out.println("CD state = " +  state[2]);
            System.out.println("RI state = " +  state[3] + "\n");

            // reset everything again and read state change
            scm.setRTS(handle, false);
            scm.setDTR(handle, false);
            scm.setRTS(handle1, false);
            scm.setDTR(handle1, false);
            state = scm.getLinesStatus(handle);
            System.out.println("CTS state = " + state[0]);
            System.out.println("DSR state = " + state[1]);
            System.out.println("CD state = " +  state[2]);
            System.out.println("RI state = " +  state[3] + "\n");
            state = scm.getLinesStatus(handle1);
            System.out.println("CTS state = " + state[0]);
            System.out.println("DSR state = " + state[1]);
            System.out.println("CD state = " +  state[2]);
            System.out.println("RI state = " +  state[3] + "\n");

            //set everything again and read state change
            scm.setRTS(handle, true);
            scm.setDTR(handle, true);
            scm.setRTS(handle1, true);
            scm.setDTR(handle1, true);
            state = scm.getLinesStatus(handle);
            System.out.println("CTS state = " + state[0]);
            System.out.println("DSR state = " + state[1]);
            System.out.println("CD state = " +  state[2]);
            System.out.println("RI state = " +  state[3] + "\n");
            state = scm.getLinesStatus(handle1);
            System.out.println("CTS state = " + state[0]);
            System.out.println("DSR state = " + state[1]);
            System.out.println("CD state = " +  state[2]);
            System.out.println("RI state = " +  state[3] + "\n");

            // set RTS but unset DTR
            scm.setRTS(handle, true);
            scm.setDTR(handle, false);
            state = scm.getLinesStatus(handle1);
            System.out.println("CTS state = " + state[0]);
            System.out.println("DSR state = " + state[1]);
            System.out.println("CD state = " +  state[2]);
            System.out.println("RI state = " +  state[3] + "\n");
            scm.setRTS(handle1, true);
            scm.setDTR(handle1, false);
            state = scm.getLinesStatus(handle);
            System.out.println("CTS state = " + state[0]);
            System.out.println("DSR state = " + state[1]);
            System.out.println("CD state = " +  state[2]);
            System.out.println("RI state = " +  state[3] + "\n");

            // set DTR but unset RTS
            scm.setRTS(handle, false);
            scm.setDTR(handle, true);
            state = scm.getLinesStatus(handle1);
            System.out.println("CTS state = " + state[0]);
            System.out.println("DSR state = " + state[1]);
            System.out.println("CD state = " +  state[2]);
            System.out.println("RI state = " +  state[3] + "\n");
            scm.setRTS(handle1, false);
            scm.setDTR(handle1, true);
            state = scm.getLinesStatus(handle);
            System.out.println("CTS state = " + state[0]);
            System.out.println("DSR state = " + state[1]);
            System.out.println("CD state = " +  state[2]);
            System.out.println("RI state = " +  state[3] + "\n");

            scm.closeComPort(handle);
            scm.closeComPort(handle1);

            scnm.destroyAllVirtualDevices();
            scnm.releaseResources();
            System.out.println("Done !");
        } catch (Exception e) {
            scnm.destroyAllVirtualDevices();
            scnm.releaseResources();
            System.out.println("Done !");
            e.printStackTrace();
        }
    }
}
