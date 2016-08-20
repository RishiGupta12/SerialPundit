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

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.serialpundit.core.SerialComException;
import com.serialpundit.serial.SerialComManager;
import com.serialpundit.serial.SerialComManager.BAUDRATE;
import com.serialpundit.serial.SerialComManager.DATABITS;
import com.serialpundit.serial.SerialComManager.FLOWCONTROL;
import com.serialpundit.serial.SerialComManager.PARITY;
import com.serialpundit.serial.SerialComManager.STOPBITS;

public final class PortSettingsPanel extends JPanel {

    final class SCMInstantiator implements Runnable {
        @Override
        public void run() {
            try {
                try {
                    // Start after UI has been instantiated.
                    Thread.sleep(100);
                } catch (Exception e) {
                }
                scmi = new SerialComManager();
                communicator = new SerialCommunicator(scmi, psPaneli, progStatusPaneli);
            } catch (Exception e) {
                progStatusPaneli.setExtraInfo(e.getMessage());
            }
        }
    }

    private static final long serialVersionUID = -4519870174920231439L;

    private SerialComManager scmi;
    private ProgramStatusPanel progStatusPaneli;
    private boolean isComPortOpen;
    private SerialCommunicator communicator;
    private PortSettingsPanel psPaneli;

    private String comPortToUse;
    private long comPortHandle = -1;
    private BAUDRATE baudRateToUse = BAUDRATE.B9600;
    private DATABITS dataBitsToUse = DATABITS.DB_8;
    private STOPBITS stopBitsToUse = STOPBITS.SB_1;
    private PARITY   parityToUse   = PARITY.P_NONE;
    private FLOWCONTROL flowctrlToUse = FLOWCONTROL.NONE;

    private String[] comports = { "COMxx" };
    private String[] baudrates = { "4800","9600","14400","19200","28800","38400","57600","115200" };
    private String[] databits = { "5", "6", "7", "8" };
    private String[] stopbits = { "1", "1.5", "2" };
    private String[] parityop = { "None", "Even", "Odd", "Mark", "Space" };
    private String[] flowctrlop = { "None", "RTS/CTS", "Xon/Xoff" };

    // UI variables
    private JPanel innerJPanel;
    private JComboBox<String> portSelection;
    private JComboBox<String> baudrateSelection;
    private JComboBox<String> stopbitsSelection;
    private JComboBox<String> databitsSelection;
    private JComboBox<String> paritySelection;
    private JComboBox<String> flowctrlSelection;
    private JButton saveButton;
    private JButton refreshButton;

    public void initPanel(ProgramStatusPanel progStatusPanel) {

        progStatusPaneli = progStatusPanel;
        psPaneli = this;

        Thread t = new Thread(new SCMInstantiator());
        t.start();

        this.setLayout(new FlowLayout(FlowLayout.LEFT));
        this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1), "Com port settings"));

        innerJPanel = new JPanel();
        innerJPanel.setLayout(new GridLayout(0, 2, 3, 14));
        innerJPanel.setBorder(BorderFactory.createEmptyBorder(8, 5, 5, 0));

        // Com Ports available
        innerJPanel.add(new JLabel("Com port :"));

        portSelection = new JComboBox<>(comports);
        portSelection.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                @SuppressWarnings("unchecked")
                JComboBox<String> combo = (JComboBox<String>) e.getSource();
                comPortToUse = (String) combo.getSelectedItem();
            }
        });
        innerJPanel.add(portSelection);

        // Baud rate
        innerJPanel.add(new JLabel("Baud rate :"));
        baudrateSelection = new JComboBox<>(baudrates);
        baudrateSelection.setSelectedIndex(1);
        baudrateSelection.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                @SuppressWarnings("unchecked")
                JComboBox<String> combo = (JComboBox<String>) e.getSource();
                String baudRate = (String) combo.getSelectedItem();
                if(baudRate.equals("4800")) {
                    baudRateToUse = BAUDRATE.B4800;
                }else if(baudRate.equals("9600")) {
                    baudRateToUse = BAUDRATE.B9600;
                }else if(baudRate.equals("14400")) {
                    baudRateToUse = BAUDRATE.B14400;
                }else if(baudRate.equals("19200")) {
                    baudRateToUse = BAUDRATE.B19200;
                }else if(baudRate.equals("28800")) {
                    baudRateToUse = BAUDRATE.B28800;
                }else if(baudRate.equals("38400")) {
                    baudRateToUse = BAUDRATE.B38400;
                }else if(baudRate.equals("57600")) {
                    baudRateToUse = BAUDRATE.B57600;
                }else if(baudRate.equals("115200")) {
                    baudRateToUse = BAUDRATE.B115200;
                }else {
                    baudRateToUse = BAUDRATE.B9600;
                }
            }
        });
        innerJPanel.add(baudrateSelection);

        // Data bits
        innerJPanel.add(new JLabel("Data bits :"));
        databitsSelection = new JComboBox<>(databits);
        databitsSelection.setSelectedIndex(3);
        databitsSelection.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                @SuppressWarnings("unchecked")
                JComboBox<String> combo = (JComboBox<String>) e.getSource();
                String databit = (String) combo.getSelectedItem();
                if(databit.equals("5")) {
                    dataBitsToUse = DATABITS.DB_5;
                }else if(databit.equals("6")) {
                    dataBitsToUse = DATABITS.DB_6;
                }else if(databit.equals("7")) {
                    dataBitsToUse = DATABITS.DB_7;
                }else if(databit.equals("8")) {
                    dataBitsToUse = DATABITS.DB_8;
                }else {
                    dataBitsToUse = DATABITS.DB_8;
                }
            }
        });
        innerJPanel.add(databitsSelection);

        // Stop bits
        innerJPanel.add(new JLabel("Stop bits :"));
        stopbitsSelection = new JComboBox<>(stopbits);
        stopbitsSelection.setSelectedIndex(0);
        stopbitsSelection.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                @SuppressWarnings("unchecked")
                JComboBox<String> combo = (JComboBox<String>) e.getSource();
                String stopbit = (String) combo.getSelectedItem();
                if(stopbit.equals("1")) {
                    stopBitsToUse = STOPBITS.SB_1;
                }else if(stopbit.equals("1.5")) {
                    stopBitsToUse = STOPBITS.SB_1_5;
                }else if(stopbit.equals("2")) {
                    stopBitsToUse = STOPBITS.SB_2;
                }else {
                    stopBitsToUse = STOPBITS.SB_2;
                }
            }
        });
        innerJPanel.add(stopbitsSelection);

        // Parity
        innerJPanel.add(new JLabel("Parity :"));
        paritySelection = new JComboBox<>(parityop);
        paritySelection.setSelectedIndex(0);
        paritySelection.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                @SuppressWarnings("unchecked")
                JComboBox<String> combo = (JComboBox<String>) e.getSource();
                String parityops = (String) combo.getSelectedItem();
                if(parityops.equals("None")) {
                    parityToUse = PARITY.P_NONE;
                }else if(parityops.equals("Even")) {
                    parityToUse = PARITY.P_EVEN;
                }else if(parityops.equals("Odd")) {
                    parityToUse = PARITY.P_ODD;
                }else if(parityops.equals("Mark")) {
                    parityToUse = PARITY.P_MARK;
                }else {
                    parityToUse = PARITY.P_SPACE;
                }
            }
        });
        innerJPanel.add(paritySelection);

        // Flow control
        innerJPanel.add(new JLabel("Flow control :"));
        flowctrlSelection = new JComboBox<>(flowctrlop);
        flowctrlSelection.setSelectedIndex(0);
        flowctrlSelection.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                @SuppressWarnings("unchecked")
                JComboBox<String> combo = (JComboBox<String>) e.getSource();
                String flow = (String) combo.getSelectedItem();
                if(flow.equals("None")) {
                    flowctrlToUse = FLOWCONTROL.NONE;
                }else if(flow.equals("RTS/CTS")) {
                    flowctrlToUse = FLOWCONTROL.RTS_CTS;
                }else if(flow.equals("Xon/Xoff")) {
                    flowctrlToUse = FLOWCONTROL.XON_XOFF;
                }else {
                    flowctrlToUse = FLOWCONTROL.NONE;
                }
            }
        });
        innerJPanel.add(flowctrlSelection);

        // refresh button
        refreshButton = new JButton("Refresh ports");
        refreshButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    comports = scmi.listAvailableComPorts();
                    portSelection.removeAllItems();
                    for(int x=0; x<comports.length; x++) {
                        portSelection.addItem(comports[x]);
                    }
                } catch (Exception e1) {
                    progStatusPaneli.setExtraInfo(e1.getMessage());
                }
            }
        });
        refreshButton.setEnabled(true);
        innerJPanel.add(refreshButton);

        // Connect/Disconnect button
        saveButton = new JButton("Connect");
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if ("Connect".equals((String) e.getActionCommand())) {
                    try {
                        comPortHandle = scmi.openComPort(comPortToUse, true, true, true);
                        scmi.configureComPortData(comPortHandle, dataBitsToUse, stopBitsToUse, parityToUse, baudRateToUse, 0);
                        scmi.configureComPortControl(comPortHandle, flowctrlToUse, (char)0x11, (char)0x13, false, false);
                        isComPortOpen = true;
                        saveButton.setText("Disconnect");
                        progStatusPaneli.setProgStatus(true);
                        progStatusPaneli.setExtraInfo("------------------------------------------------------------------- ");
                    } catch (SerialComException e1) {
                        progStatusPaneli.setExtraInfo(e1.getExceptionMsg());
                    } catch (Exception e2) {
                        progStatusPaneli.setExtraInfo(e2.getMessage());
                    }
                }else {
                    try {
                        scmi.closeComPort(comPortHandle);
                        saveButton.setText("Connect");
                        isComPortOpen = false;
                        comPortHandle = -1;
                        progStatusPaneli.setProgStatus(false);
                    } catch (Exception e3) {
                        progStatusPaneli.setExtraInfo(e3.getMessage());
                    }
                }
            }
        });
        saveButton.setEnabled(true);
        innerJPanel.add(saveButton);

        this.add(innerJPanel);
    }

    public long getComPortHandle() {
        return comPortHandle;
    }

    public boolean isPortOpened() {
        return isComPortOpen;
    }

    public void setConnectButton(boolean state) {
        saveButton.setEnabled(state);
    }

    public SerialComManager getSCMInstance() {
        return scmi;
    }

    public void executeCommand(String cmd) {
        communicator.executeCommand(cmd);
    }

    public void exit() {
        try {
            scmi.closeComPort(comPortHandle);
        } catch (Exception e) {
        }
    }
}
