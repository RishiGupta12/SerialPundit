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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Ellipse2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import com.serialpundit.core.SerialComException;
import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.serial.SerialComManager;
import com.serialpundit.serial.nullmodem.SerialComNullModem;

public final class NullModemPanel extends JPanel {

    private final SerialComManager scm;
    private final int osType;
    private final SerialComNullModem scnm;
    private final JTextField statusInfo;

    // Create
    private JPanel cPanel;
    private JComboBox<String> createDevSelection1;
    private JComboBox<String> createDevSelection2;
    private String suggestedDevNode1;
    private String suggestedDevNode2;
    private String[] nextAvailableDevName1 = { suggestedDevNode1 }; //TODO make OS specific
    private String[] nextAvailableDevName2 = { suggestedDevNode2 };
    private JButton createDevButton;
    String[] nextDev;

    // Destroy
    private JPanel lPanel;
    private PinMappings pm;
    private JComboBox<String> destroyDevSelection;
    private String[] existingDevices = {" Select device pair to be deleted  "};
    private String devPairToBeDeleted;
    private JButton deleteDevButton;
    private JComboBox<String> listDevSelection;
    private String[] existingDevicesList = { " tty2comXX  <--->  tty2comYY  " };
    private JButton refreshDevListButton;

    // Event
    private JPanel evtPanel;
    private JButton emulateEventButton;
    private JComboBox<String> exstDevSelection;
    private String[] exstDevices = { "" };
    private String devSelectForErrorEvt;
    private ArrayList<String> extDlist = new ArrayList<String>();
    private int errEvtMask = 0;
    private boolean setRingingState = false;
    private final String[] defaultList = { " ---------------------- " };

    public NullModemPanel(SerialComManager scm, int osType, SerialComNullModem scnm, JTextField statusInfo) {
        this.scm = scm;
        this.osType = osType;
        this.scnm = scnm;
        this.statusInfo = statusInfo;
    }

    public void init() {

        if(osType == SerialComPlatform.OS_LINUX) {
            //TODO
        }

        this.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 20));
        //        this.setLayout (new BoxLayout (this, BoxLayout.Y_AXIS));

        /* Device pair to be created --------------------------------------------------------------- */

        cPanel = new JPanel();
        cPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        cPanel.setBorder(BorderFactory.createTitledBorder("Create"));

        try {
            nextDev = scnm.getNextAvailableComPorts();
        } catch (IOException e) {
            statusInfo.setText(e.getMessage());
        }
        nextAvailableDevName1[0] = nextDev[0];
        nextAvailableDevName2[0] = nextDev[1];

        createDevSelection1 = new JComboBox<String>(nextAvailableDevName1);
        createDevSelection1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                @SuppressWarnings("unchecked")
                JComboBox<String> combo = (JComboBox<String>) e.getSource();
                devPairToBeDeleted = (String) combo.getSelectedItem();
            }
        });
        createDevSelection1.setEditable(true);
        cPanel.add(createDevSelection1);

        cPanel.add(new JLabel(" <----> ", null, JLabel.LEFT));

        createDevSelection2 = new JComboBox<String>(nextAvailableDevName2);
        createDevSelection2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                @SuppressWarnings("unchecked")
                JComboBox<String> combo = (JComboBox<String>) e.getSource();
                devPairToBeDeleted = (String) combo.getSelectedItem();
            }
        });
        createDevSelection2.setEditable(true);
        cPanel.add(createDevSelection2);

        createDevButton = new JButton("Create devices");
        createDevButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if ("Create devices".equals((String) e.getActionCommand())) {
                }
                else {
                }
            }
        });
        createDevButton.setEnabled(true);
        cPanel.add(createDevButton);

        // make horizontal size of all group same (internal border of all will be aligned)
        Dimension d = cPanel.getPreferredSize();

        /* Listing/Destroy existing devices --------------------------------------------------------------- */

        lPanel = new JPanel();
        lPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        lPanel.setBorder(BorderFactory.createTitledBorder("List"));

        pm = new PinMappings();
        pm.setPreferredSize(new Dimension((int)d.getWidth() - 10, 150));
        lPanel.add(pm);

        JPanel lstPanel = new JPanel();
        lstPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        lstPanel.setPreferredSize(new Dimension((int)d.getWidth() - 10, (int)d.getHeight() - 30));

        listDevSelection = new JComboBox<String>(existingDevicesList);
        listDevSelection.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                @SuppressWarnings("unchecked")
                JComboBox<String> combo = (JComboBox<String>) e.getSource();
                devPairToBeDeleted = (String) combo.getSelectedItem();//TODO SHOW PIN MAPPING
            }
        });
        lstPanel.add(listDevSelection);

        refreshDevListButton = new JButton("Refresh");
        refreshDevListButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //TODO
            }
        });
        lstPanel.add(refreshDevListButton);

        deleteDevButton = new JButton("Delete devices");
        deleteDevButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if ("Delete devices".equals((String) e.getActionCommand())) {
                }
                else {
                }
            }
        });
        deleteDevButton.setEnabled(true);
        lstPanel.add(deleteDevButton);

        lPanel.add(pm);
        lPanel.add(lstPanel);
        lPanel.setPreferredSize(new Dimension((int)d.getWidth(), 210));

        /* Event emulation --------------------------------------------------------------- */

        evtPanel = new JPanel();
        evtPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        evtPanel.setBorder(BorderFactory.createTitledBorder("Events"));

        JRadioButton frameButton = new JRadioButton("Frame ");
        frameButton.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent ie) {
                if(ie.getStateChange() == ItemEvent.SELECTED) {
                    try {
                        if((devSelectForErrorEvt == null) || (devSelectForErrorEvt.length() == 0) || (devSelectForErrorEvt.equals(defaultList[0]))) {
                            statusInfo.setText("Please select correct port first !");
                            return;
                        }
                        scnm.emulateLineError(devSelectForErrorEvt, SerialComNullModem.ERR_FRAME);
                    } catch (Exception e) {
                        statusInfo.setText(e.getMessage());
                    }
                }
            }
        });

        JRadioButton parityButton = new JRadioButton("Parity ");
        parityButton.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent ie) {
                if(ie.getStateChange() == ItemEvent.SELECTED) {
                    try {
                        if((devSelectForErrorEvt == null) || (devSelectForErrorEvt.length() == 0) || (devSelectForErrorEvt.equals(defaultList[0]))) {
                            statusInfo.setText("Please select correct port first !");
                            return;
                        }
                        scnm.emulateLineError(devSelectForErrorEvt, SerialComNullModem.ERR_PARITY);
                    } catch (Exception e) {
                        statusInfo.setText(e.getMessage());
                    }
                }
            }
        });

        JRadioButton overrunButton = new JRadioButton("Overrun ");
        overrunButton.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent ie) {
                if(ie.getStateChange() == ItemEvent.SELECTED) {
                    try {
                        if((devSelectForErrorEvt == null) || (devSelectForErrorEvt.length() == 0) || (devSelectForErrorEvt.equals(defaultList[0]))) {
                            statusInfo.setText("Please select correct port first !");
                            return;
                        }
                        scnm.emulateLineError(devSelectForErrorEvt, SerialComNullModem.ERR_OVERRUN);
                    } catch (Exception e) {
                        statusInfo.setText(e.getMessage());
                    }
                }
            }
        });

        JRadioButton ringButton = new JRadioButton("Ring ");
        ringButton.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent ie) {
                try {
                    if((devSelectForErrorEvt == null) || (devSelectForErrorEvt.length() == 0) || (devSelectForErrorEvt.equals(defaultList[0]))) {
                        statusInfo.setText("Please select correct port first !");
                        return;
                    }
                    if(ie.getStateChange() == ItemEvent.SELECTED) {
                        scnm.emulateLineRingingEvent(devSelectForErrorEvt, true); //TODO CALL FROM NON-UI THREAD
                    }else {
                        scnm.emulateLineRingingEvent(devSelectForErrorEvt, false);
                    }
                } catch (Exception e) {
                    statusInfo.setText(e.getMessage());
                }
            }
        });

        JRadioButton breakButton = new JRadioButton("Break                    ");
        breakButton.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent ie) {
                if((devSelectForErrorEvt == null) || (devSelectForErrorEvt.length() == 0) || (devSelectForErrorEvt.equals(defaultList[0]))) {
                    statusInfo.setText("Please select correct port first !");
                    return;
                }
                if(ie.getStateChange() == ItemEvent.SELECTED) {
                    try {
                        scnm.emulateLineError(devSelectForErrorEvt, SerialComNullModem.RCV_BREAK);
                    } catch (Exception e) {
                        statusInfo.setText(e.getMessage());
                    }
                }
            }
        });

        // at any instant any one event should be specified by user
        evtPanel.add(frameButton);
        evtPanel.add(parityButton);
        evtPanel.add(overrunButton);
        evtPanel.add(ringButton);
        evtPanel.add(breakButton);

        String[] extdevl = null;
        try {
            extdevl = scm.listAvailableComPorts();
        } catch (Exception e) {
            statusInfo.setText(e.getMessage());
        }

        if(osType == SerialComPlatform.OS_LINUX) {
            for(int x=0; x < extdevl.length; x++) {
                if(extdevl[x].contains("tty2com")) {
                    extDlist.add(" " + extdevl[x] + " ");
                }
            }
        }
        if(extDlist.size() == 0) {
            existingDevicesList = defaultList;
        }else {
            existingDevicesList = extDlist.toArray(new String[extDlist.size()]);
        }

        exstDevSelection = new JComboBox<String>(existingDevicesList);
        exstDevSelection.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                @SuppressWarnings("unchecked")
                JComboBox<String> combo = (JComboBox<String>) e.getSource();
                devSelectForErrorEvt = (String) combo.getSelectedItem();
                devSelectForErrorEvt = devSelectForErrorEvt.trim();
            }
        });
        evtPanel.add(exstDevSelection);

        Dimension de = evtPanel.getPreferredSize();
        evtPanel.setPreferredSize(new Dimension((int)d.getWidth(), (int)de.getHeight() + 30));

        this.add(cPanel);
        this.add(lPanel);
        this.add(evtPanel);
    }
}

class PinMappings extends JPanel {

    private static final long serialVersionUID = 2570934611544922289L;

    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.DARK_GRAY);

        // TXD --> RXD, RXD <-- TXD
        g2d.drawString("TxD", 45, 20);
        g2d.drawString("RxD", 45, 40);
        Ellipse2D.Double tx1 = new Ellipse2D.Double(80, 13, 5, 5);
        g2d.fill(tx1);
        Ellipse2D.Double rx1 = new Ellipse2D.Double(80, 33, 5, 5);
        g2d.fill(rx1);
        g2d.drawLine(82, 16, 95, 16);
        g2d.drawLine(82, 36, 95, 36);
        g2d.drawLine(95, 16, 130, 36);
        g2d.drawLine(95, 36, 130, 16);
        g2d.drawLine(130, 16, 143, 16);
        g2d.drawLine(130, 36, 143, 36);
        Ellipse2D.Double tx2 = new Ellipse2D.Double(140, 13, 5, 5);
        g2d.fill(tx2);
        Ellipse2D.Double rx2 = new Ellipse2D.Double(140, 33, 5, 5);
        g2d.fill(rx2);
        g2d.drawString("TxD", 160, 20);
        g2d.drawString("RxD", 160, 40);

        // RTS --> CTS, CTS <-- RTS
        g2d.drawString("RTS", 45, 64);
        g2d.drawString("CTS", 45, 84);
        Ellipse2D.Double rts1 = new Ellipse2D.Double(80, 58, 5, 5);
        g2d.fill(rts1);
        Ellipse2D.Double cts1 = new Ellipse2D.Double(80, 78, 5, 5);
        g2d.fill(cts1);
        g2d.drawLine(82, 61, 95, 61);
        g2d.drawLine(82, 81, 95, 81);
        g2d.drawLine(95, 61, 130, 81);
        g2d.drawLine(95, 81, 130, 61);
        g2d.drawLine(130, 61, 143, 61);
        g2d.drawLine(130, 81, 143, 81);
        Ellipse2D.Double rts2 = new Ellipse2D.Double(140, 58, 5, 5);
        g2d.fill(rts2);
        Ellipse2D.Double cts2 = new Ellipse2D.Double(140, 78, 5, 5);
        g2d.fill(cts2);
        g2d.drawString("RTS", 160, 64);
        g2d.drawString("CTS", 160, 84);

        // DTR --> DCD+DSR, DTR <-- DCD+DSR
        g2d.drawString("DTR", 45, 105);
        g2d.drawString("DCD", 45, 125);
        g2d.drawString("DSR", 45, 145);
        Ellipse2D.Double dtr1 = new Ellipse2D.Double(80, 99, 5, 5);
        g2d.fill(dtr1);
        Ellipse2D.Double dcd1 = new Ellipse2D.Double(80, 119, 5, 5);
        g2d.fill(dcd1);
        Ellipse2D.Double dsr1 = new Ellipse2D.Double(80, 139, 5, 5);
        g2d.fill(dsr1);
        g2d.drawLine(82, 102, 95, 102);
        g2d.drawLine(82, 122, 95, 122);
        g2d.drawLine(82, 142, 95, 142);
        g2d.drawLine(95, 102, 130, 122);
        g2d.drawLine(95, 122, 130, 102);
        g2d.drawLine(130, 122, 130, 142);
        g2d.drawLine(95, 122, 95, 142);
        g2d.drawLine(130, 102, 143, 102);
        g2d.drawLine(130, 122, 143, 122);
        g2d.drawLine(130, 142, 143, 142);
        g2d.drawString("DTR", 160, 105);
        g2d.drawString("DCD", 160, 125);
        g2d.drawString("DSR", 160, 145);
        Ellipse2D.Double dtr2 = new Ellipse2D.Double(140, 99, 5, 5);
        g2d.fill(dtr2);
        Ellipse2D.Double dcd2 = new Ellipse2D.Double(140, 119, 5, 5);
        g2d.fill(dcd2);
        Ellipse2D.Double dsr2 = new Ellipse2D.Double(140, 139, 5, 5);
        g2d.fill(dsr2);
    }
}
