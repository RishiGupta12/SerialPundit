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

package serialterminal;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public final class MenuBar {

    private final SerialTerminalApp sertermapp;

    public MenuBar(SerialTerminalApp sertermapp) {
        this.sertermapp = sertermapp;
    }

    public JMenuBar setUpMenuBar() {

        JMenuBar menubar = new JMenuBar();

        // file menu
        JMenu file = new JMenu("File");
        file.setMnemonic(KeyEvent.VK_F);

        // exit option
        JMenuItem fileExit = new JMenuItem("Exit");
        fileExit.setMnemonic(KeyEvent.VK_C);
        fileExit.setToolTipText("Exit application");
        fileExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,ActionEvent.CTRL_MASK));
        fileExit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sertermapp.triggerAppExit();
            }
        });
        file.add(fileExit);

        menubar.add(file);

        return menubar;
    }
}
