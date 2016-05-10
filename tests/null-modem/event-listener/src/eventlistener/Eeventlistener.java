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

package eventlistener;

import java.io.IOException;
import java.util.Arrays;
import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.nullmodem.SerialComNullModem;
import com.embeddedunveiled.serial.SerialComManager.BAUDRATE;
import com.embeddedunveiled.serial.SerialComManager.DATABITS;
import com.embeddedunveiled.serial.SerialComManager.FLOWCONTROL;
import com.embeddedunveiled.serial.SerialComManager.PARITY;
import com.embeddedunveiled.serial.SerialComManager.STOPBITS;
import com.embeddedunveiled.serial.ISerialComEventListener;
import com.embeddedunveiled.serial.SerialComLineEvent;

class EventListener implements ISerialComEventListener{
    @Override
    public void onNewSerialEvent(SerialComLineEvent lineEvent) {
        System.out.println("eventCTS : " + lineEvent.getCTS());
        System.out.println("eventDSR : " + lineEvent.getDSR());
    }
}

public class Eeventlistener {

    static SerialComNullModem scnm = null;

    public static void main(String[] args) throws IOException {
        try {
            SerialComManager scm = new SerialComManager();
            scnm = scm.getSerialComNullModemInstance();
            String[] ports = scnm.createStandardNullModemPair(-1, -1);

            // instantiate class which is will implement ISerialComEventListener interface
            EventListener eventListener = new EventListener();

            long handle = scm.openComPort(ports[0], true, true, true);
            scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
            scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);
            System.out.println("regisration status : " + scm.registerLineEventListener(handle, eventListener));

            long handle1 = scm.openComPort(ports[1], true, true, true);
            scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
            scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);

            int[] interrupts = scm.getInterruptCount(handle);
            System.out.println("1 : " + Arrays.toString(interrupts));

            Thread.sleep(100);
            scm.setDTR(handle1, false);
            Thread.sleep(100);
            scm.setRTS(handle1, false);

            interrupts = scm.getInterruptCount(handle);
            System.out.println("2 : " + Arrays.toString(interrupts));

            Thread.sleep(100);
            scm.setDTR(handle1, true);
            Thread.sleep(100);
            scm.setRTS(handle1, true);
            Thread.sleep(100);

            interrupts = scm.getInterruptCount(handle);
            System.out.println("3 : " + Arrays.toString(interrupts));

            scm.setDTR(handle1, false);
            Thread.sleep(100);
            scm.setRTS(handle1, false);
            Thread.sleep(100);

            interrupts = scm.getInterruptCount(handle);
            System.out.println("3 : " + Arrays.toString(interrupts));

            scm.setDTR(handle1, true);
            Thread.sleep(100);
            scm.setRTS(handle1, true);
            Thread.sleep(100);

            interrupts = scm.getInterruptCount(handle);
            System.out.println("4 : " + Arrays.toString(interrupts));

            scm.setDTR(handle1, false);
            Thread.sleep(100);
            scm.setRTS(handle1, false);
            Thread.sleep(100);

            interrupts = scm.getInterruptCount(handle);
            System.out.println("5 : " + Arrays.toString(interrupts));

            scm.setDTR(handle1, true);
            Thread.sleep(100);
            scm.setRTS(handle1, true);
            Thread.sleep(100);

            interrupts = scm.getInterruptCount(handle);
            System.out.println("6 : " + Arrays.toString(interrupts));

            // unregister data listener
            scm.unregisterLineEventListener(handle, eventListener);
            Thread.sleep(100);
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
