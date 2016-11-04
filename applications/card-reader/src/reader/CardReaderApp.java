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

package reader;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import com.serialpundit.core.SerialComException;
import com.serialpundit.core.util.SerialComUtil;
import com.serialpundit.serial.ISerialComDataListener;
import com.serialpundit.serial.SerialComManager;
import com.serialpundit.serial.SerialComManager.BAUDRATE;
import com.serialpundit.serial.SerialComManager.DATABITS;
import com.serialpundit.serial.SerialComManager.FLOWCONTROL;
import com.serialpundit.serial.SerialComManager.PARITY;
import com.serialpundit.serial.SerialComManager.STOPBITS;

// Do I/O operations like talking to network/database etc in worker thread and update the result on GUI
final class ProcessData extends SwingWorker<Void, Void> {

	private final byte[] cardid;
	private final JTextField statusInfo;
	private final SerialPortReader spr;
	private boolean cardFound;

	public ProcessData(byte[] cardid, JTextField statusInfo, SerialPortReader spr) {
		this.cardid = cardid;
		this.statusInfo = statusInfo;
		this.spr = spr;
		cardFound = false;
	}

	@Override
	protected Void doInBackground() throws Exception {

		String hexID = SerialComUtil.byteArrayToHexString(cardid, null);
		System.out.println("Card ID : " + hexID);

		// 1. Establish connection with database server or use existing connection.
		// 2. Check if this card is valid and have been assigned to an employee or not. If found set cardFound=true otherwise cardFound=false.
		return null;
	}

	// Update results on UI, this is invoked from UI thread.
	@Override
	protected void done() {
		if(cardFound != true) {
			statusInfo.setText("Not found, please enter name !");
		}else {
			statusInfo.setText("Found, opening gate !");
		}
		spr.enableGettingDataFromComPort();
	}
}

/* This read data from serial port and when complete data has been received send it for processing. */
final class SerialPortReader implements ISerialComDataListener {

	// modify this to your serial port name
	private final String COMPORT = "COM11";

	private final SerialComManager scm;
	private final long comPortHandle;
	private final JTextField statusInfo;

	private int index = 0;
	private int totalNumberOfBytesReadTillNow = 0;
	private byte[] dataBuffer = new byte[12];
	private boolean enableGettingDataFromComPort = true;

	public SerialPortReader(JTextField statusInfo) throws IOException {
		scm = new SerialComManager();
		comPortHandle = scm.openComPort(COMPORT, true, true, true);
		scm.configureComPortData(comPortHandle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
		scm.configureComPortControl(comPortHandle, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.clearPortIOBuffers(comPortHandle, true, true);
		this.statusInfo = statusInfo;
	}

	@Override
	public void onNewSerialDataAvailable(byte[] data) {

		/* Discard data that is not needed. */
		if(enableGettingDataFromComPort == true) {

			// Buffer data until 10 bytes has been received
			if(totalNumberOfBytesReadTillNow < 10) {
				for(int x=0; x < data.length; x++) {
					dataBuffer[index] = data[x];
					index++;
				}
				totalNumberOfBytesReadTillNow = totalNumberOfBytesReadTillNow + data.length;
			}

			// If 10 (complete frame) or more bytes have been received, process them.
			if(totalNumberOfBytesReadTillNow >= 10) {
				enableGettingDataFromComPort = false;
				ProcessData task = new ProcessData(dataBuffer, statusInfo, this);
				task.execute();
			}
		}
	}

	// reset for next operation
	public void enableGettingDataFromComPort() {
		enableGettingDataFromComPort = true;
		index = 0;
		totalNumberOfBytesReadTillNow = 0;
		try {
			scm.clearPortIOBuffers(comPortHandle, true, true);
		} catch (SerialComException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDataListenerError(int arg0) {
		System.out.println("Err SerialPortRedaer : " + arg0);
	}
}

/* This defines, prepares and show GUI part of application. */
public final class CardReaderApp extends JFrame {

	private static final long serialVersionUID = -1162210327547987496L;
	private JTextField cardID;
	private JTextField nameEntered;
	private JTextField statusInfo;

	protected void begin() throws Exception {

		JFrame mainFrame = new JFrame();
		mainFrame.getContentPane().setLayout(new BoxLayout(mainFrame.getContentPane(), BoxLayout.Y_AXIS));
		mainFrame.setSize(250, 150);
		mainFrame.setResizable(false);
		mainFrame.setTitle("demo app");
		mainFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent windowEvent) {
				exitApplication();
			}        
		});

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(0, 1, 10, 10));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		JPanel jp1 = new JPanel(new GridLayout(2, 2, 5, 5));
		JPanel jp2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));

		jp1.add(new JLabel("Card ID :"));
		cardID = new JTextField(11);
		cardID.setEditable(false);
		cardID.setBorder(javax.swing.BorderFactory.createEmptyBorder());
		jp1.add(cardID);

		jp1.add(new JLabel("Name :"));
		nameEntered = new JTextField(11);
		nameEntered.setBorder(javax.swing.BorderFactory.createEmptyBorder());
		jp1.add(nameEntered);

		statusInfo = new JTextField(18);
		statusInfo.setHorizontalAlignment(SwingConstants.LEFT);
		statusInfo.setEditable(false);
		statusInfo.setBorder(javax.swing.BorderFactory.createEmptyBorder());
		jp2.add(statusInfo);

		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (int) ((dimension.getWidth() - mainFrame.getWidth()) / 2);
		int y = (int) ((dimension.getHeight() - mainFrame.getHeight()) / 2);
		mainFrame.setLocation(x, y);

		mainPanel.add(jp1);
		mainPanel.add(jp2);
		mainFrame.add(mainPanel);
		mainFrame.setVisible(true);

		statusInfo.setText("Not found, please enter name !");
	}

	/* This method is the central place where all cleanup task like unregistering, terminating thread etc should happen. */
	public void exitApplication() {
		try {
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* Entry point to this application. */
	public static void main(String[] args) {

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					CardReaderApp app = new CardReaderApp();
					app.begin();
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(0);
				}
			}
		});
	}
}
