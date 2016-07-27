/*
 * This file is part of SerialPundit project and software.
 * 
 * Copyright (C) 2014-2016, Rishi Gupta. All rights reserved.
 *
 * The SerialPundit software is DUAL licensed. It is made available under the terms of the GNU Affero 
 * General Public License (AGPL) v3.0 for non-commercial use and under the terms of a commercial 
 * license for commercial use of this software. 
 * 
 * The SerialPundit software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tty2comKmApp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import com.serialpundit.serial.SerialComManager;
import com.serialpundit.serial.nullmodem.SerialComNullModem;

public final class MenuBar {

    private final SerialComManager scm;
    private final int osType;
    private final SerialComNullModem scnm;

    public MenuBar(SerialComManager scm, int osType, SerialComNullModem scnm) {
        this.scm = scm;
        this.osType = osType;
        this.scnm = scnm;
    }

    public JMenuBar init() {

        JMenuBar menubar = new JMenuBar();

        /* File menu */
        JMenu file = new JMenu("File");
        file.setMnemonic(KeyEvent.VK_F);

        JMenuItem fileExit = new JMenuItem("Exit");
        fileExit.setMnemonic(KeyEvent.VK_C);
        fileExit.setToolTipText("Exit application");
        fileExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,ActionEvent.CTRL_MASK));
        fileExit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //TODO
            }
        });

        file.add(fileExit);
        menubar.add(file);

        /* Driver menu */
        JMenu drvm = new JMenu("Driver");

        menubar.add(drvm);

        /* Devices menu */
        JMenu devm = new JMenu("Devices");

        JMenuItem delAllDevs = new JMenuItem("Delete all devices");
        delAllDevs.setToolTipText("Deletes all virtual devices created by tty2comKm driver");
        delAllDevs.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    scnm.destroyAllVirtualDevices();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } //TODO call from non-ui thread
            }
        });
        devm.add(delAllDevs);

        menubar.add(devm);

        /* Help menu */
        JMenu hlpm = new JMenu("Help");

        menubar.add(hlpm);

        return menubar;
    }
}












