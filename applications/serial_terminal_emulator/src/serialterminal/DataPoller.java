/*
 * This file is part of SerialPundit.
 * 
 * Copyright (C) 2014-2021, Rishi Gupta. All rights reserved.
 *
 * The SerialPundit is DUAL LICENSED. It is made available under the terms of the GNU Affero 
 * General Public License (AGPL) v3.0 for non-commercial use and under the terms of a commercial 
 * license for commercial use of this software. 
 * 
 * The SerialPundit is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package serialterminal;

import javax.swing.JTextField;

import com.serialpundit.core.SerialComException;
import com.serialpundit.serial.SerialComManager;
import com.serialpundit.core.util.SerialComUtil;

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
