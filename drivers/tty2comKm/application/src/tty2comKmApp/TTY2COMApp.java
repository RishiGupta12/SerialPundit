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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;

import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComSystemProperty;
import com.serialpundit.serial.SerialComManager;
import com.serialpundit.serial.nullmodem.SerialComNullModem;

public final class TTY2COMApp extends JFrame {

    private SerialComManager scm;
    private SerialComPlatform scp;
    private int osType;
    private SerialComNullModem scnm;
    private JFrame mainFrame;
    private JPanel mainPanel;
    private JTabbedPane tabbedPane;
    private NullModemPanel nmPanel;
    private LoopbackPanel lbPanel;
    private CustomPanel ctPanel;
    private JPanel statusPanel;
    private JTextField statusInfo;

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
        scnm = scm.getSerialComNullModemInstance();

        JFrame.setDefaultLookAndFeelDecorated(true);
        try{
            // Set cross-platform Java L&F (also called "Metal")
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
            UIManager.put("swing.boldMetal", Boolean.FALSE);
        }catch (Exception e) {
            System.out.println("UIManager.setLookAndFeel : " + e.getMessage());
        }

        // init main frame and place it little above the center of desktop screen
        mainFrame = new JFrame();
        mainFrame.setSize(550, 600);
        mainFrame.setResizable(false);
        mainFrame.setTitle("tty2comKm null modem emulator");
        mainFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
                try {
                    scnm.releaseResources();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                System.exit(0);
            }        
        });
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - mainFrame.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - mainFrame.getHeight()) / 2);
        y = y - 100;
        mainFrame.setLocation(x, y);
        mainFrame.getContentPane().setLayout(new GridBagLayout());

        MenuBar menuBar = new MenuBar(scm, osType, scnm);
        JMenuBar jmenuBar = menuBar.init();

        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(0, 1, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
        mainPanel.setPreferredSize(new Dimension(120, 500));

        statusPanel = new JPanel();
        statusPanel.setPreferredSize(new Dimension(100, 30));
        statusPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
        statusInfo = new JTextField(47);
        statusInfo.setOpaque(false);
        statusInfo.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        statusPanel.add(statusInfo);

        nmPanel = new NullModemPanel(scm, osType, scnm, statusInfo);
        nmPanel.init();

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Null modem standard", null, nmPanel, null);
        tabbedPane.addTab("Loop back standard", null, lbPanel, null);
        tabbedPane.addTab("Custom pinout", null, ctPanel, null);;
        mainPanel.add(tabbedPane);

        mainFrame.setJMenuBar(jmenuBar);

        GridBagConstraints gbcm = new GridBagConstraints();
        gbcm.fill = GridBagConstraints.HORIZONTAL;
        gbcm.weightx = 0.5;
        gbcm.gridx = 0;
        gbcm.gridy = 0;

        GridBagConstraints gbcs = new GridBagConstraints();
        gbcs.fill = GridBagConstraints.HORIZONTAL;
        gbcs.weightx = 0.0;
        gbcs.gridx = 0;
        gbcs.gridy = 1;

        mainFrame.add(mainPanel, gbcm);
        mainFrame.add(statusPanel, gbcs);

        //        frame.pack();
        mainFrame.setVisible(true);
    }
}
