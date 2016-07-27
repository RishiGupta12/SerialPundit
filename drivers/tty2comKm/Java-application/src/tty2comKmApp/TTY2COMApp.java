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

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;

import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComSystemProperty;
import com.serialpundit.serial.SerialComManager;

public final class TTY2COMApp extends JFrame {

    private SerialComManager scm;
    private SerialComPlatform scp;
    private int osType;
    private JFrame frame;
    private JPanel mainPanel;
    private JTabbedPane tabbedPane;
    private NullModemPanel nmPanel;
    private LoopbackPanel lbPanel;
    private CustomPanel ctPanel;

    /* Entry point to this application. */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    TTY2COMApp app = new TTY2COMApp();
                    app.begin();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(0);
                }
            }
        });
    }

    protected void begin() throws Exception {

        scm = new SerialComManager();
        scp = new SerialComPlatform(new SerialComSystemProperty());
        osType = scp.getOSType();

        JFrame.setDefaultLookAndFeelDecorated(true);
        try{
            // Set cross-platform Java L&F (also called "Metal")
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
            UIManager.put("swing.boldMetal", Boolean.FALSE);
        }catch (Exception e) {
            System.out.println("UIManager.setLookAndFeel : " + e.getMessage());
        }

        frame = new JFrame();
        frame.setSize(540, 490);
        frame.setResizable(false);
        frame.setTitle("tty2comKm null modem emulator");

        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(0, 1, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        frame.getContentPane().add(mainPanel);

        // place windows little above the center of desktop screen
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
        y = y - 100;
        frame.setLocation(x, y);

        nmPanel = new NullModemPanel(scm, osType);
        nmPanel.init();

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Null modem standard", null, nmPanel, null);
        tabbedPane.addTab("Loop back standard", null, lbPanel, null);
        tabbedPane.addTab("Custom pinout", null, ctPanel, null);;
        mainPanel.add(tabbedPane);

        frame.add(mainPanel);
        //        frame.pack();
        frame.setVisible(true);
    }
}
