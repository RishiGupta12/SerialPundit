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

/*
 * 1. Start now and stop at some time later automatically.
 * 2. Start some time later and stop manually.
 * 3. Start some time later and stop some time later.
 */
public final class LogLaterPanel extends JPanel {

	private static final long serialVersionUID = -8190710738451360859L;

	private ComToFileLoggerApp logappi;
	private SerialComManager scmi;
	private SerialComToFile sctf;

	private PortSettingsPanel portSettingsPaneli;
	private LogNowPanel logNowPaneli;
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
	private JCheckBox logLaterSelection;

	private SimpleDateFormat sdf;

	public void initPanel(final SerialComManager scm, final PortSettingsPanel portSettingsPanel, final LogNowPanel logNowPanel, 
			final ProgramStatusPanel progStatusPanel, ComToFileLoggerApp logapp, MenuBar mbar) {

		scmi = scm;
		portSettingsPaneli = portSettingsPanel;
		logNowPaneli = logNowPanel;
		progStatusPaneli = progStatusPanel;
		logappi = logapp;
		mbari = mbar;
		sctf = new SerialComToFile();

		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1)));

		innerJPanel = new JPanel();
		innerJPanel.setLayout(new GridLayout(0, 2, 8, 14));
		innerJPanel.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 1));

		// Check box
		logLaterSelection = new JCheckBox("Log later", false);
		logLaterSelection.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ie) {
				int state = ie.getStateChange();
				if (state == ItemEvent.SELECTED) {
					logNowPaneli.setLogNowCheckBoxEnabled(false);
				}else {

				}
			}
		});
		logLaterSelection.setEnabled(false);
		innerJPanel.add(logLaterSelection);
		innerJPanel.add(new JLabel(""));

		// Start time
		innerJPanel.add(new JLabel("Start time :"));
		startValue = new JTextField();
		startValue.setPreferredSize(new Dimension(160, 20));
		innerJPanel.add(startValue);

		innerJPanel.add(new JLabel("Stop time :"));
		stopValue = new JTextField();
		stopValue.setPreferredSize(new Dimension(160, 20));
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

						String startTime = startValue.getText();
						if(startTime != null) {
							if(startTime.length() != "dd/MM/yyyy HH:mm:ss".length()) {
								progStatusPaneli.setExtraInfo("Incorrect start time. Correct is dd/MM/yyyy HH:mm:ss ");
								fileButton.setEnabled(true);
								startButton.setEnabled(true);
								stopButton.setEnabled(false);
								return;
							}
						}

						String stopTime = stopValue.getText();
						if(stopTime != null) {
							if(stopTime.length() != "dd/MM/yyyy HH:mm:ss".length()) {
								progStatusPaneli.setExtraInfo("Incorrect stop time. Correct is dd/MM/yyyy HH:mm:ss ");
								fileButton.setEnabled(true);
								startButton.setEnabled(true);
								stopButton.setEnabled(false);
								return;
							}
						}

						sctf.scheduleLaterLogging(startTime, stopTime, scmi, comPortHandle, param[0], param[1], logFile, logappi);

						loggingIsInProgress = true;
						progStatusPaneli.setProgStatus(true,  true);
						progStatusPaneli.setExtraInfo("Logging scheduled");
						portSettingsPaneli.setConnectButton(false);
						fileButton.setEnabled(false);
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
					try {
						long comPortHandle = portSettingsPaneli.getComPortHandle();
						sctf.unscheduleOrStopLaterLogging(comPortHandle, logappi);
						loggingIsInProgress = false;
						stopValue.setText(sdf.format(new Date()));
						progStatusPaneli.setProgStatus(true,  false);
						progStatusPaneli.setExtraInfo("Logging unscheduled/stopped");
						portSettingsPaneli.setConnectButton(true);
						fileButton.setEnabled(true);
						startButton.setEnabled(true);
						stopButton.setEnabled(false);
					} catch (Exception e1) {
						progStatusPaneli.setExtraInfo(e1.getMessage());
					}
				}
			}
		});
		innerJPanel.add(stopButton);

		this.add(innerJPanel);
	}

	public void setLogLaterCheckBoxEnabled(boolean value) {
		if(value == true) {
			logLaterSelection.setSelected(false);
			logLaterSelection.setEnabled(true);
		}else {
			logLaterSelection.setSelected(false);
			logLaterSelection.setEnabled(false);
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
