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

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.datalogger.ISerialComToFileErrListener;

/*
 * 1. The basic idea is to guide users by enabling/disabling UI widgets at appropriate times.
 *    For example, logging panel should be enabled only after serial port connection has been
 *    established.
 */
public final class ComToFileLoggerApp implements ISerialComToFileErrListener {

	private JFrame mainFrame;
	private JPanel mainPanel;
	private JPanel statusPanel;
	private PortSettingsPanel portSettingsPanel;
	private LogNowPanel logNowPanel;
	private LogLaterPanel logLaterPanel;
	private ProgramStatusPanel progStatusPanel;
	private SerialComManager scm;
	private MenuBar menuBar;
	private JTabbedPane jtp = new JTabbedPane();

	protected void begin() throws Exception {

		scm = new SerialComManager();

		mainFrame = new JFrame("Com to File logger");
		mainFrame.getContentPane().setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		mainFrame.setPreferredSize(new Dimension(717, 419));
		mainFrame.setResizable(false);

		// What will happen when user clicks on close icon.
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent windowEvent){
				triggerAppExit();
			}       
		});

		// Be little stylish.
		JFrame.setDefaultLookAndFeelDecorated(true);
		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (Exception e) {
			JFrame.setDefaultLookAndFeelDecorated(true);
		}

		// When application starts, place window nearly in center of screen.
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		mainFrame.setLocation(d.width/2 - 512, d.height/2 - 384);

		menuBar = new MenuBar();
		progStatusPanel = new ProgramStatusPanel();
		logNowPanel = new LogNowPanel();
		logLaterPanel = new LogLaterPanel();
		portSettingsPanel = new PortSettingsPanel();
		jtp = new JTabbedPane();

		JMenuBar jmenuBar = menuBar.initMenuBar(this);
		progStatusPanel.initPanel();
		logNowPanel.initPanel(scm, portSettingsPanel, logLaterPanel, progStatusPanel, this, menuBar);
		logLaterPanel.initPanel(scm, portSettingsPanel, logNowPanel, progStatusPanel, this, menuBar);
		portSettingsPanel.initPanel(scm, logNowPanel, logLaterPanel, progStatusPanel);

		statusPanel = new JPanel();
		statusPanel.setLayout(new GridLayout(0, 1, 1, 10));
		statusPanel.setBorder(BorderFactory.createEmptyBorder(6, 8, 9, 0));
		statusPanel.add(progStatusPanel);

		mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(0, 2, 10, 10));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 7, 5, 8));
		mainPanel.add(portSettingsPanel);
		jtp.addTab("Log now", logNowPanel);
		jtp.addTab("Log later", logLaterPanel);
		mainPanel.add(jtp);

		mainFrame.setJMenuBar(jmenuBar);
		mainFrame.add(mainPanel);
		mainFrame.add(statusPanel);
		mainFrame.pack();
		mainFrame.setVisible(true);
	}

	/* Entry point to this application. */
	public static void main(String[] args) {
		// Setup GUI in event-dispatching thread for thread-safety.
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				ComToFileLoggerApp app = new ComToFileLoggerApp();
				try {
					app.begin();
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(0);
				}
			}
		});
	}

	/* Whenever any error occurs, library will call this method. This can also be written to a text 
	 * file for later analysis instead of printing on JPanel. */
	@Override
	public void onLogError(Exception e) {
		progStatusPanel.setExtraInfo("-> " + e.getMessage() + " !");
	}

	/* Whenever user wishes to exit application stop logging, close port and release 
	 * any resources if occupied and gracefully exit. */
	public void triggerAppExit() {
		if(logNowPanel.isLoggingOn()) {
			logNowPanel.stopLogging();
		}
		if(logLaterPanel.isLoggingOn()) {
			logLaterPanel.stopLogging();
		}
		if(portSettingsPanel.isPortOpened()) {
			try {
				long comPortHandle = portSettingsPanel.getComPortHandle();
				scm.closeComPort(comPortHandle);
			} catch (Exception e) {
			}
		}
		mainFrame.setVisible(false);
		mainFrame.dispose();
		System.exit(0);
	}
}
