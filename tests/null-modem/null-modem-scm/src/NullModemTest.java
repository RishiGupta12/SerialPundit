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

        try {
            scm = new SerialComManager();
            SerialComNullModem scnm = scm.getSerialComNullModemInstance();

            scnm.createStandardLoopBackDevice(-1);
            scnm.createStandardLoopBackDevice(3);

            scnm.createStandardNullModemDevices(-1, -1);
            scnm.createStandardNullModemDevices(-1, 12);
            scnm.createStandardNullModemDevices(7, -1);
            scnm.createStandardNullModemDevices(9, 16);

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

}
