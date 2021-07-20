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

import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.JPanel;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;

import com.serialpundit.core.SerialComException;
import com.serialpundit.serial.SerialComManager;
import com.serialpundit.serial.SerialComManager.BAUDRATE;
import com.serialpundit.serial.SerialComManager.DATABITS;
import com.serialpundit.serial.SerialComManager.FLOWCONTROL;
import com.serialpundit.serial.SerialComManager.PARITY;
import com.serialpundit.serial.SerialComManager.STOPBITS;

public class SerialTerminalApp extends JFrame {

    private static final long serialVersionUID = -3067979562931264507L;

    private static SerialComManager scm;
    private Thread mDataReceiverThread;
    private DataPoller dataPoller;
    private SignalExit exitTrigger;

    private static String[] comPortsFound;
    private long comPortHandle;
    private DATABITS databits;
    private STOPBITS stopbits;
    private PARITY parity;
    private BAUDRATE baudrate;
    private FLOWCONTROL flowcontrol;
    private char xon;
    private char xoff;

    private JFrame frame;
    private JMenuBar menuBar;
    private MenuBar menuBarObj;
    private JPanel centerPanel;

    // receive
    private JPanel rcvDataPanel;
    private JTextField datarcvtextfield;
    private Checkbox hexDisplay;
    private boolean displayInHex;
    private JButton clrDataButton;

    // send data stuff
    private JPanel sendDataPanel;
    private JTextField sendDataTextField;
    private JButton sendDataButton;
    private JTabbedPane tabbedPane;

    // serial port tab
    private JPanel comTopPanel;
    private JPanel comportPanel;
    private JComboBox<String> comportComboBox;
    private JButton comportRefreshButton;
    private JComboBox<String> baudrateComboBox;
    private JPanel parityPanel;
    private JRadioButton parnoneRbutton;
    private JRadioButton paroddRbutton;
    private JRadioButton parevenRbutton;
    private JRadioButton parmarkRbutton;
    private JRadioButton parspaceRbutton;
    private ButtonGroup dbitsButtonGroup;
    private JPanel databitsPanel;
    private JRadioButton dbits8Rbutton;
    private JRadioButton dbits7Rbutton;
    private JRadioButton dbits6Rbutton;
    private JRadioButton dbits5Rbutton;
    private ButtonGroup parityButtonGroup;
    private JRadioButton sbits1Rbutton;
    private JRadioButton sbits2Rbutton;
    private ButtonGroup stopbitsButtonGroup;
    private JPanel flowctrlPanel;
    private JRadioButton fctrlnoneRbutton;
    private JRadioButton fctrlswRbutton;
    private JRadioButton fctrlhwRbutton;
    private ButtonGroup fctrlButtonGroup;
    private JPanel flowctrlCharPanel;
    private JCheckBox swflctrlrcv;
    private JCheckBox swflctrlsnt;
    private JTextField xonCharText;
    private JTextField xoffCharText;
    private JButton openButton;
    private JButton closeButton;
    private JPanel modemLinesStatusPanel;
    private JLabel dsrStatusLabel;
    private JLabel dcdStatusLabel;
    private JLabel ctsStatusLabel;
    private JLabel riStatusLabel;
    private ImageIcon iconRed = new ImageIcon("images/red.png");
    //	private ImageIcon iconGreen = new ImageIcon("images/green.png");
    private ImageIcon iconSmiley = new ImageIcon("images/smiley.png");
    // program status
    private JPanel programStatusPanel;
    private JLabel programStatusLabel;
    protected JTextField programStatusText;

    protected void begin() throws Exception {

        comPortHandle = -1;
        // This can also be done in a separate thread to decrease startup time of this application.
        scm = new SerialComManager();
        comPortsFound = scm.listAvailableComPorts();

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
        frame.setSize(670, 520);
        frame.setResizable(false);
        frame.setTitle("Serial terminal emulator");
        frame.getContentPane().setLayout(new BorderLayout(0,0));

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent){
                triggerAppExit();
            }       
        });

        /* ~~~ MENU BAR ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
        menuBarObj = new MenuBar(this);
        menuBar = menuBarObj.setUpMenuBar();
        frame.setJMenuBar(menuBar);

        /* ~~~ TOP MOST PANEL ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
        frame.getContentPane().add(new JLabel("   "), BorderLayout.NORTH);
        frame.getContentPane().add(new JLabel("   "), BorderLayout.WEST);
        centerPanel = new JPanel();
        centerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        frame.getContentPane().add(centerPanel, BorderLayout.CENTER);
        frame.getContentPane().add(new JLabel("   "), BorderLayout.EAST);
        frame.getContentPane().add(new JLabel("   "), BorderLayout.SOUTH);

        /* ~~~ DATA RECEIVE BLOCK ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
        rcvDataPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 15));
        datarcvtextfield = new JTextField();
        datarcvtextfield.setPreferredSize(new Dimension(520, 40));
        datarcvtextfield.setBackground(Color.WHITE);
        datarcvtextfield.setForeground(Color.BLACK);
        datarcvtextfield.setEditable(false);
        rcvDataPanel.add(datarcvtextfield);
        rcvDataPanel.add(new JLabel("  "));

        clrDataButton = new JButton("Clear data");
        clrDataButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                datarcvtextfield.setText("");
            }          
        });
        rcvDataPanel.add(clrDataButton);
        centerPanel.add(rcvDataPanel);

        hexDisplay = new Checkbox("Display in Hex");
        hexDisplay.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == 1) {
                    displayInHex = true;
                    if(dataPoller != null) {
                        dataPoller.setDisplayInHex(true);
                    }
                }else {
                    displayInHex = false;
                    if(dataPoller != null) {
                        dataPoller.setDisplayInHex(false);
                    }
                }
            }
        });
        centerPanel.add(hexDisplay);

        /* ~~~ SEND DATA BLOCK ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
        sendDataPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 15));
        sendDataTextField = new JTextField();
        sendDataTextField.setBackground(Color.WHITE);
        sendDataTextField.setForeground(Color.BLACK);
        sendDataTextField.setEditable(true);
        sendDataTextField.setPreferredSize(new Dimension(520, 40));
        sendDataPanel.add(sendDataTextField);

        sendDataPanel.add(new JLabel("  "));

        sendDataButton = new JButton("Send data");
        sendDataButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    scm.writeString(comPortHandle, sendDataTextField.getText(), 0);
                    programStatusText.setText("");
                    programStatusText.setText("Data sent to port successfully !");
                } catch (SerialComException e1) {
                    programStatusText.setText("");
                    programStatusText.setText(e1.getExceptionMsg());
                }
            }          
        });
        sendDataPanel.add(sendDataButton);
        centerPanel.add(sendDataPanel);

        /* ~~~~ TABBED CONTENT ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
        tabbedPane = new JTabbedPane();

        // serial port tab		
        comTopPanel = new JPanel(new GridLayout(6, 1, 5, 0));

        comportPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        comportPanel.add(new JLabel("Com port : "));
        comportComboBox = new JComboBox<>(comPortsFound);
        comportComboBox.setPreferredSize(new Dimension(150, 25));
        comportPanel.add(comportComboBox);
        comportRefreshButton = new JButton("Refresh list");
        comportRefreshButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    comPortsFound = scm.listAvailableComPorts();
                    comportComboBox.removeAllItems();
                    for(int x=0; x<comPortsFound.length; x++) {
                        comportComboBox.addItem(comPortsFound[x]);
                    }
                    programStatusText.setText("");
                    programStatusText.setText("List refreshed successfully !");
                } catch (SerialComException e1) {
                    programStatusText.setText("");
                    programStatusText.setText(e1.getExceptionMsg());
                }
            }          
        });
        comportPanel.add(comportRefreshButton);
        comportPanel.add(new JLabel("       Baudrate : "));
        baudrateComboBox = new JComboBox<>(new String[] {"4800", "9600", "115200"});
        baudrateComboBox.setPreferredSize(new Dimension(150, 25));
        comportPanel.add(baudrateComboBox);
        comTopPanel.add(comportPanel);

        parityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        parityPanel.add(new JLabel("Parity : "));
        parnoneRbutton = new JRadioButton("None");
        parnoneRbutton.setSelected(true);
        paroddRbutton = new JRadioButton("Odd");
        parevenRbutton = new JRadioButton("Even");
        parmarkRbutton = new JRadioButton("Mark");
        parspaceRbutton = new JRadioButton("Space");
        parityButtonGroup = new ButtonGroup();
        parityButtonGroup.add(parnoneRbutton);
        parityButtonGroup.add(paroddRbutton);
        parityButtonGroup.add(parevenRbutton);
        parityButtonGroup.add(parmarkRbutton);
        parityButtonGroup.add(parspaceRbutton);
        parityPanel.add(parnoneRbutton);
        parityPanel.add(paroddRbutton);
        parityPanel.add(parevenRbutton);
        parityPanel.add(parmarkRbutton);
        parityPanel.add(parspaceRbutton);
        comTopPanel.add(parityPanel);

        databitsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        databitsPanel.add(new JLabel("Data bits : "));
        dbits8Rbutton = new JRadioButton("8");
        dbits8Rbutton.setSelected(true);
        dbits7Rbutton = new JRadioButton("7");
        dbits6Rbutton = new JRadioButton("6");
        dbits5Rbutton = new JRadioButton("5");
        dbitsButtonGroup = new ButtonGroup();
        dbitsButtonGroup.add(dbits8Rbutton);
        dbitsButtonGroup.add(dbits7Rbutton);
        dbitsButtonGroup.add(dbits6Rbutton);
        dbitsButtonGroup.add(dbits5Rbutton);
        databitsPanel.add(dbits8Rbutton);
        databitsPanel.add(dbits7Rbutton);
        databitsPanel.add(dbits6Rbutton);
        databitsPanel.add(dbits5Rbutton);
        databitsPanel.add(new JLabel("        Stop bits : "));
        sbits1Rbutton = new JRadioButton("1");
        sbits1Rbutton.setSelected(true);
        sbits2Rbutton = new JRadioButton("2");
        stopbitsButtonGroup = new ButtonGroup();
        stopbitsButtonGroup.add(dbits8Rbutton);
        stopbitsButtonGroup.add(dbits7Rbutton);
        databitsPanel.add(sbits1Rbutton);
        databitsPanel.add(sbits2Rbutton);
        comTopPanel.add(databitsPanel);

        flowctrlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        flowctrlPanel.add(new JLabel("Flow control : "));
        fctrlnoneRbutton = new JRadioButton("None");
        fctrlnoneRbutton.setSelected(true);
        fctrlswRbutton = new JRadioButton("Software");
        fctrlhwRbutton = new JRadioButton("Hardware");
        fctrlButtonGroup = new ButtonGroup();
        fctrlButtonGroup.add(fctrlnoneRbutton);
        fctrlButtonGroup.add(fctrlswRbutton);
        fctrlButtonGroup.add(fctrlhwRbutton);
        flowctrlPanel.add(fctrlnoneRbutton);
        flowctrlPanel.add(fctrlswRbutton);
        flowctrlPanel.add(fctrlhwRbutton);
        comTopPanel.add(flowctrlPanel);

        flowctrlCharPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        flowctrlCharPanel.add(new JLabel("Software flow control : "));
        swflctrlrcv = new JCheckBox("Receive");
        flowctrlCharPanel.add(swflctrlrcv);
        flowctrlCharPanel.add(new JLabel("Xon char : "));
        xonCharText = new JTextField();
        xonCharText.setPreferredSize(new Dimension(25, 15));
        xonCharText.setBackground(Color.WHITE);
        xonCharText.setForeground(Color.BLACK);
        xonCharText.setEditable(true);
        flowctrlCharPanel.add(xonCharText);
        flowctrlCharPanel.add(new JLabel("         "));
        swflctrlsnt = new JCheckBox("Transmit");
        flowctrlCharPanel.add(swflctrlsnt);
        flowctrlCharPanel.add(new JLabel("Xoff char : "));
        xoffCharText = new JTextField();
        xoffCharText.setPreferredSize(new Dimension(25, 15));
        xoffCharText.setBackground(Color.WHITE);
        xoffCharText.setForeground(Color.BLACK);
        xoffCharText.setEditable(true);
        flowctrlCharPanel.add(xoffCharText);
        comTopPanel.add(flowctrlCharPanel);

        modemLinesStatusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        modemLinesStatusPanel.add(new JLabel("Modem lines status : "));
        dsrStatusLabel = new JLabel("DSR", iconRed, JLabel.LEFT);
        modemLinesStatusPanel.add(dsrStatusLabel);
        dcdStatusLabel = new JLabel("DCD", iconRed, JLabel.LEFT);
        modemLinesStatusPanel.add(dcdStatusLabel);
        ctsStatusLabel = new JLabel("CTS", iconRed, JLabel.LEFT);
        modemLinesStatusPanel.add(ctsStatusLabel);
        riStatusLabel = new JLabel("RI", iconRed, JLabel.LEFT);
        modemLinesStatusPanel.add(riStatusLabel);

        openButton = new JButton("Open");
        openButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    comPortHandle = scm.openComPort((String)comportComboBox.getSelectedItem(), true, true, true);
                    if(dbitsButtonGroup.getSelection().equals(dbits8Rbutton.getModel())) {
                        databits = DATABITS.DB_8;
                    }else if(dbitsButtonGroup.getSelection().equals(dbits7Rbutton.getModel())) {
                        databits = DATABITS.DB_7;
                    }else if(dbitsButtonGroup.getSelection().equals(dbits6Rbutton.getModel())) {
                        databits = DATABITS.DB_6;
                    }else if(dbitsButtonGroup.getSelection().equals(dbits5Rbutton.getModel())) {
                        databits = DATABITS.DB_5;
                    }else {
                    }
                    if(stopbitsButtonGroup.getSelection().equals(sbits1Rbutton.getModel())) {
                        stopbits = STOPBITS.SB_1;
                    }else {
                        stopbits = STOPBITS.SB_2;
                    }
                    if(parityButtonGroup.getSelection().equals(parnoneRbutton.getModel())) {
                        parity = PARITY.P_NONE;
                    }else if(parityButtonGroup.getSelection().equals(paroddRbutton.getModel())) {
                        parity = PARITY.P_ODD;
                    }else if(parityButtonGroup.getSelection().equals(parevenRbutton.getModel())) {
                        parity = PARITY.P_EVEN;
                    }else if(parityButtonGroup.getSelection().equals(parmarkRbutton.getModel())) {
                        parity = PARITY.P_MARK;
                    }else {
                        parity = PARITY.P_SPACE;
                    }
                    String brate = (String)baudrateComboBox.getSelectedItem();
                    if(brate.equals("4800")) {
                        baudrate = BAUDRATE.B4800;
                    }else if(brate.equals("9600")) {
                        baudrate = BAUDRATE.B9600;
                    }else {
                        baudrate = BAUDRATE.B115200;
                    }
                    scm.configureComPortData(comPortHandle, databits, stopbits, parity, baudrate, 0);
                    if(fctrlButtonGroup.getSelection().equals(fctrlnoneRbutton.getModel())) {
                        flowcontrol = FLOWCONTROL.NONE;
                        xon = 'x';
                        xoff = 'x';
                    }else if(fctrlButtonGroup.getSelection().equals(fctrlswRbutton.getModel())) {
                        flowcontrol = FLOWCONTROL.XON_XOFF;
                        xon = (char) Integer.parseInt(xonCharText.getText());
                        xoff = (char) Integer.parseInt(xoffCharText.getText());
                    }else {
                        flowcontrol = FLOWCONTROL.RTS_CTS;
                        xon = 'x';
                        xoff = 'x';
                    }
                    scm.configureComPortControl(comPortHandle, flowcontrol, xon, xoff, false, false);

                    exitTrigger = new SignalExit(false);
                    dataPoller = new DataPoller(scm, datarcvtextfield, comPortHandle, datarcvtextfield, displayInHex, exitTrigger);
                    mDataReceiverThread = new Thread(dataPoller, "SCM DataPoller");
                    mDataReceiverThread.start();

                    programStatusText.setText("");
                    programStatusText.setText("Port opened and configured. Reading data from port started !");
                } catch (SerialComException e1) {
                    programStatusText.setText("");
                    programStatusText.setText(e1.getExceptionMsg());
                }
            }          
        });

        closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    if(comPortHandle != -1) {
                        if(mDataReceiverThread != null) {
                            exitTrigger.setExitTrigger(true);
                            mDataReceiverThread.interrupt();
                        }
                        scm.closeComPort(comPortHandle);
                        comPortHandle = -1;
                        programStatusText.setText("");
                        programStatusText.setText("Port closed successfully !");
                    }
                } catch (SerialComException e1) {
                    programStatusText.setText("");
                    programStatusText.setText(e1.getExceptionMsg());
                }
            }          
        });

        modemLinesStatusPanel.add(new JLabel("                     "));
        modemLinesStatusPanel.add(openButton);
        modemLinesStatusPanel.add(closeButton);
        comTopPanel.add(modemLinesStatusPanel);

        tabbedPane.addTab("serial port", null, comTopPanel, null);
        tabbedPane.addTab("x/y/z modem", null, null, null);
        centerPanel.add(tabbedPane);

        /* ~~~ PROGRAM STATUS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
        programStatusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
        programStatusLabel = new JLabel("Program status : ", iconSmiley, JLabel.LEFT);
        programStatusPanel.add(programStatusLabel);
        programStatusText = new JTextField("Initializing......");
        programStatusText.setPreferredSize(new Dimension(495, 25));
        programStatusText.setEditable(false);
        programStatusText.setBorder(BorderFactory.createEmptyBorder(0,0,1,0));
        programStatusPanel.add(programStatusText);
        centerPanel.add(programStatusPanel);

        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
        frame.setLocation(x, y);

        // finally show UI On screen
        frame.setVisible(true);
    }

    public void triggerAppExit() {
        if(comPortHandle != -1) {
            if(mDataReceiverThread != null) {
                exitTrigger.setExitTrigger(true);
                mDataReceiverThread.interrupt();
            }
            try {
                scm.closeComPort(comPortHandle);
            } catch (SerialComException e) {
                e.printStackTrace();
            }
        }
        frame.setVisible(false);
        frame.dispose();
        System.exit(0);
    }

    /* Entry point to this application. */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    SerialTerminalApp app = new SerialTerminalApp();
                    app.begin();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(0);
                }
            }
        });
    }
}
