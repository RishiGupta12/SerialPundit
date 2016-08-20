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

package commandreceiver;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public final class EmailCommandReceiverApp {

    private JFrame mainFrame;
    private JPanel mainPanel;
    private JPanel secondPanel;
    private PortSettingsPanel psPanel;
    private EmailUIPanel emPanel;
    private ProgramStatusPanel pstatusPanel;

    /* Entry point to this application. */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    EmailCommandReceiverApp app = new EmailCommandReceiverApp();
                    app.begin();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(0);
                }
            }
        });
    }

    protected void begin() throws Exception {

        System.out.println("Application started !");

        mainFrame = new JFrame("Email command sender");
        mainFrame.getContentPane().setLayout(new GridLayout(0, 1, 10, 10));
        mainFrame.setResizable(false);

        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        mainFrame.setLocation(d.width/2 - 512, d.height/2 - 384);

        mainPanel = new JPanel(new FlowLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 5, 5, 0));

        psPanel = new PortSettingsPanel();
        emPanel = new EmailUIPanel();
        pstatusPanel = new ProgramStatusPanel();

        pstatusPanel.initPanel();
        psPanel.initPanel(pstatusPanel);
        emPanel.initPanel(psPanel, pstatusPanel);

        secondPanel = new JPanel(new GridLayout(0, 1, 0, 10));
        secondPanel.add(emPanel);
        secondPanel.add(pstatusPanel);

        mainPanel.add(psPanel);
        mainPanel.add(secondPanel);

        mainFrame.add(mainPanel);

        // What will happen when user clicks on close icon.
        mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        mainFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent){
                emPanel.exit();
                mainFrame.setVisible(false);
                mainFrame.dispose();
                System.exit(0);
            }       
        });

        mainFrame.pack();
        mainFrame.setVisible(true);
    }
}
