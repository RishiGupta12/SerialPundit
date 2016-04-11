import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

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

public final class NullModemTest {

    public static void main(String[] args) {      
        try {
            File f = new File("/proc/scmtty_vadaptkm");
            //            FileOutputStream out = new FileOutputStream(f);
            //            out.write("genlb#xxxxx#xxxxx#7-8,x,x,x#4-1,6,x,x#x-x,x,x,x#x-x,x,x,x#y#x".getBytes(), 0, "genlb#xxxxx#xxxxx#7-8,x,x,x#4-1,6,x,x#x-x,x,x,x#x-x,x,x,x#y#x".length());

            byte[] abc = new byte[128];
            InputStream input = new FileInputStream("/proc/scmtty_vadaptkm");
            input.read(abc);
            System.out.println(new String(abc));
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

}
