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
import com.embeddedunveiled.serial.nullmodem.SerialComNullModem;

public final class NullModemTest {

    public static void main(String[] args) {

        SerialComManager scm = null;
        SerialComNullModem scnm = null;

        try {
            scm = new SerialComManager();
            scnm = scm.getSerialComNullModemInstance();           

            try {
                scnm.createStandardLoopBackDevice(-1);
            }catch (Exception e) {
                e.printStackTrace();
            }

            try {
                scnm.createStandardLoopBackDevice(2);
            }catch (Exception e) {
                e.printStackTrace();
            }

            try {
                scnm.createStandardNullModemPair(-1, -1);
            }catch (Exception e) {
                e.printStackTrace();
            }

            try {
                scnm.createStandardNullModemPair(-1, 2);
            }catch (Exception e) {
                e.printStackTrace();
            }

            try {
                scnm.createStandardNullModemPair(7, -1);
            }catch (Exception e) {
                e.printStackTrace();
            }

            try {
                scnm.createStandardNullModemPair(9, 16);
            }catch (Exception e) {
                e.printStackTrace();
            }

            try {
                System.out.println("last loopback node : " + scnm.getLastLoopBackDeviceNode());
            }catch (Exception e) {
                e.printStackTrace();
            }

            try {
                String[] str = scnm.getLastNullModemDevicePairNodes();
                System.out.println("last null modem node : " + str[0] + "--" + str[1]);
            }catch (Exception e) {
                e.printStackTrace();
            }

            try {
                scnm.destroyVirtualSerialDevice(9);
            }catch (Exception e) {
                e.printStackTrace();
            }

            try {
                scnm.destroyVirtualSerialDevice(-2);
            }catch (Exception e) {
                e.printStackTrace();
            }

            try {
                scnm.destroyVirtualSerialDevice(-1);
            }catch (Exception e) {
                e.printStackTrace();
            }

            try {
                scnm.createCustomLoopBackDevice(25, 0, 0);
            }catch (Exception e) {
                e.printStackTrace();
            }

            try {
                scnm.createCustomLoopBackDevice(-1, SerialComNullModem.SCM_CON_CTS, SerialComNullModem.SCM_CON_DCD | SerialComNullModem.SCM_CON_DSR);
            }catch (Exception e) {
                e.printStackTrace();
            }

            try {
                scnm.createCustomLoopBackDevice(65, 0, SerialComNullModem.SCM_CON_CTS | SerialComNullModem.SCM_CON_DCD | SerialComNullModem.SCM_CON_DSR);
            }catch (Exception e) {
                e.printStackTrace();
            }

            try {
                scnm.createCustomLoopBackDevice(29, SerialComNullModem.SCM_CON_CTS | SerialComNullModem.SCM_CON_DCD | SerialComNullModem.SCM_CON_DSR, 0);
            }catch (Exception e) {
                e.printStackTrace();
            }


            try {
                scnm.destroyVirtualSerialDevice(-1);
            }catch (Exception e) {
                e.printStackTrace();
            }

            try {
                scnm.createStandardNullModemPair(-1, SerialComNullModem.SCM_CON_CTS, SerialComNullModem.SCM_CON_DCD | SerialComNullModem.SCM_CON_DSR, 
                        -1, SerialComNullModem.SCM_CON_CTS, SerialComNullModem.SCM_CON_DCD | SerialComNullModem.SCM_CON_DSR);
            }catch (Exception e) {
                e.printStackTrace();
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
