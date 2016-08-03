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
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

public final class MenuBar {

    private final JFrame mainFrame;
    private final TaskExecutor taskExecutor;
    private final JTextField statusInfo;

    public MenuBar(JFrame mainFrame, TaskExecutor taskExecutor, JTextField statusInfo) {
        this.mainFrame = mainFrame;
        this.taskExecutor = taskExecutor;
        this.statusInfo = statusInfo;
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
                taskExecutor.deinit();
                System.exit(0);
            }
        });
        file.add(fileExit);

        JMenuItem ulExit = new JMenuItem("Unload & exit");
        ulExit.setToolTipText("Unload driver & exit application");
        ulExit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                taskExecutor.loadTTY2COMDriver();
                taskExecutor.deinit();
                System.exit(0);
            }
        });
        file.add(ulExit);

        /* Driver menu */
        JMenu drvm = new JMenu("Driver");

        JMenuItem drvStatus = new JMenuItem("Driver status");
        drvStatus.setToolTipText("Info about driver loading");
        drvStatus.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(taskExecutor.istty2comDriverLoaded() == true) {
                    JOptionPane.showMessageDialog(mainFrame, "Driver has been already loaded !", "", JOptionPane.PLAIN_MESSAGE);
                }else {
                    taskExecutor.loadTTY2COMDriver();
                }
            }
        });
        drvm.add(drvStatus);

        /* Devices menu */
        JMenu devm = new JMenu("Devices");

        JMenuItem delAllDevs = new JMenuItem("Delete all devices");
        delAllDevs.setToolTipText("Deletes all virtual devices created by tty2comKm driver");
        delAllDevs.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                taskExecutor.destroyAlltty2comDevices();
            }
        });
        devm.add(delAllDevs);

        /* Help menu */
        JMenu hlpm = new JMenu("Help");
        //TODO

        menubar.add(file);
        menubar.add(drvm);
        menubar.add(devm);
        menubar.add(hlpm);

        return menubar;
    }
}
