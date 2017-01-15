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

package springIntegration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.serialpundit.core.SerialComException;
import com.serialpundit.serial.SerialComManager;

@Component
public class MessagePrinter {

    private final MessageService service;
    private SerialComManager scm;

    @Autowired
    public MessagePrinter(MessageService service) {
        this.service = service;
        try {
            scm = new SerialComManager();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printMessage() {

        System.out.println(this.service.getMessage());

        try {
            String[] ports = scm.listAvailableComPorts();
            for(int x=0; x<ports.length; x++) {
                System.out.println(ports[x]);   
            }
        } catch (SerialComException e) {
            e.printStackTrace();
        }
    }
}
