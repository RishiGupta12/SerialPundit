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

package comfilelogger;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.datalogger.SerialComToFile;

public final class LogNowPanel extends JPanel {

	private static final long serialVersionUID = -521319114116095982L;

	private ComToFileLoggerApp logappi;
	private SerialComManager scmi;
	private SerialComToFile sctf;

	private PortSettingsPanel portSettingsPaneli;
	private LogLaterPanel logLaterPaneli;
	private ProgramStatusPanel progStatusPaneli;
	private MenuBar mbari;

	private JPanel innerJPanel;
	private JTextField startValue;
	private JTextField stopValue;
	private JFileChooser fileChooser;
	private JButton fileButton;
	private boolean loggingIsInProgress;
	private File logFile;
	private JButton startButton;
	private JButton stopButton;
	private JCheckBox logNowSelection;

	private SimpleDateFormat sdf;

	public void initPanel(final SerialComManager scm, final PortSettingsPanel portSettingsPanel, final LogLaterPanel logLaterPanel, 
			final ProgramStatusPanel progStatusPanel, final ComToFileLoggerApp logapp, MenuBar mbar) {

		scmi = scm;
		portSettingsPaneli = portSettingsPanel;
		logLaterPaneli = logLaterPanel;
		progStatusPaneli = progStatusPanel;
		logappi = logapp;
		mbari = mbar;
		sctf = new SerialComToFile();
		sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1)));

		innerJPanel = new JPanel();
		innerJPanel.setLayout(new GridLayout(0, 2, 8, 14));
		innerJPanel.setBorder(BorderFactory.createEmptyBorder(8, 5, 6, 1));

		// Check box
		logNowSelection = new JCheckBox("Log now", false);
		logNowSelection.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ie) {
				int state = ie.getStateChange();
				if (state == ItemEvent.SELECTED) {
					fileButton.setEnabled(true);
					logLaterPaneli.setLogLaterCheckBoxEnabled(false);
				}else {
					logLaterPaneli.setLogLaterCheckBoxEnabled(true);
					startButton.setEnabled(false);
					fileButton.setEnabled(false);
					stopButton.setEnabled(false);
					startValue.setText("          ");
					stopValue.setText("          ");
				}
			}
		});
		logNowSelection.setEnabled(false);
		innerJPanel.add(logNowSelection);

		innerJPanel.add(new JLabel(""));

		// Start time
		innerJPanel.add(new JLabel("Start time :"));
		startValue = new JTextField();
		startValue.setPreferredSize(new Dimension(160, 20));
		startValue.setBackground(new Color(214, 217, 223));
		startValue.setBorder(javax.swing.BorderFactory.createEmptyBorder());
		innerJPanel.add(startValue);

		innerJPanel.add(new JLabel("Stop time :"));
		stopValue = new JTextField();
		stopValue.setPreferredSize(new Dimension(160, 20));
		stopValue.setBackground(new Color(214, 217, 223));
		stopValue.setBorder(javax.swing.BorderFactory.createEmptyBorder());
		innerJPanel.add(stopValue);

		innerJPanel.add(new JLabel("Log file :"));		
		fileChooser = new JFileChooser();

		fileButton = new JButton("Select");
		fileButton.setEnabled(false);
		fileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int result = fileChooser.showOpenDialog(new JFrame());
				if(result == JFileChooser.APPROVE_OPTION) {
					logFile = fileChooser.getSelectedFile();
					if(logFile != null) {
						startButton.setEnabled(true);
					}
				}else {
					progStatusPaneli.setExtraInfo("Please select log file");
				}
			}
		});
		innerJPanel.add(fileButton);

		// Start button
		startButton = new JButton("Start");
		startButton.setEnabled(false);
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(loggingIsInProgress == false) {
					fileButton.setEnabled(false);
					startButton.setEnabled(false);
					stopButton.setEnabled(true);
					try {
						long comPortHandle = portSettingsPaneli.getComPortHandle();
						int[] param = mbari.getReadParameters();
						sctf.startLogging(scmi, comPortHandle, param[0], param[1], logFile, logappi);
						loggingIsInProgress = true;
						startValue.setText(sdf.format(new Date()));
						progStatusPaneli.setProgStatus(true,  true);
						progStatusPaneli.setExtraInfo("Logging started");
						portSettingsPaneli.setConnectButton(false);
						fileButton.setEnabled(false);
						stopValue.setText("          ");
					} catch (Exception e1) {
						progStatusPaneli.setExtraInfo(e1.getMessage());
						fileButton.setEnabled(true);
						startButton.setEnabled(true);
						stopButton.setEnabled(false);
					}
				}
			}
		});
		innerJPanel.add(startButton);

		// Stop button
		stopButton = new JButton("Stop");
		stopButton.setEnabled(false);
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(loggingIsInProgress == true) {					
					fileButton.setEnabled(true);
					startButton.setEnabled(true);
					stopButton.setEnabled(false);
					try {
						long comPortHandle = portSettingsPaneli.getComPortHandle();
						sctf.stopLogging(comPortHandle, logappi);
						loggingIsInProgress = false;
						stopValue.setText(sdf.format(new Date()));
						progStatusPaneli.setProgStatus(true,  false);
						progStatusPaneli.setExtraInfo("Logging stopped");
						portSettingsPaneli.setConnectButton(true);
						fileButton.setEnabled(true);
					} catch (Exception e1) {
						progStatusPaneli.setExtraInfo(e1.getMessage());
						fileButton.setEnabled(false);
						startButton.setEnabled(false);
						stopButton.setEnabled(true);
					}
				}
			}
		});
		innerJPanel.add(stopButton);

		this.add(innerJPanel);
	}

	public void setLogNowCheckBoxEnabled(boolean value) {
		if(value == true) {
			logNowSelection.setSelected(false);
			logNowSelection.setEnabled(true);
		}else {
			logNowSelection.setSelected(false);
			logNowSelection.setEnabled(false);
		}
	}

	public boolean isLoggingOn() {
		return loggingIsInProgress;
	}

	public void stopLogging() {
		long comPortHandle = portSettingsPaneli.getComPortHandle();
		sctf.stopLogging(comPortHandle, logappi);
	}
}
