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

package serialterminal;

import javax.swing.JTextField;

import com.embeddedunveiled.serial.SerialComException;
import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.util.SerialComUtil;

public final class DataPoller implements Runnable {

    private byte[] dataRead;
    private String dataStr;
    private boolean displayInHex;

    private final SerialComManager scm;
    private final JTextField text;
    private final long comPortHandle;
    private final JTextField status;
    private final SignalExit exitTrigger;

    public DataPoller(SerialComManager scm, JTextField text, long comPortHandle, JTextField status, 
            boolean displayInHex, SignalExit exitTrigger) {
        this.scm = scm;
        this.comPortHandle = comPortHandle;
        this.text = text;
        this.status = status;
        this.displayInHex = displayInHex;
        this.exitTrigger = exitTrigger;
    }

    public void setDisplayInHex(boolean enabled) {
        displayInHex = enabled;
    }

    @Override
    public void run() {
        while(exitTrigger.isExitTriggered() == false) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e1) {
                if(exitTrigger.isExitTriggered() == true) {
                    return;
                }
            }
            try {
                dataRead = scm.readBytes(comPortHandle, 10);
                if(dataRead != null) {
                    text.setText("");
                    if(displayInHex == true) {
                        dataStr = SerialComUtil.byteArrayToHexString(dataRead, " ");
                    }else {
                        dataStr = new String(dataRead);
                    }
                    text.setText(dataStr);
                }
            } catch (SerialComException e) {
                status.setText("");
                status.setText(e.getExceptionMsg());
            } catch (Exception e) {
                status.setText("");
                status.setText(e.getMessage());
            }
        }
    }
}
