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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import com.embeddedunveiled.serial.datalogger.SerialComToFile;

public final class MenuBar {

	private ComToFileLoggerApp logappi;
	private int readingStyle;
	private String frequency;
	private JTextField freq;
	private JCheckBoxMenuItem periodic;
	private JCheckBoxMenuItem blocking;
	private JCheckBoxMenuItem dlisten;

	public JMenuBar initMenuBar(ComToFileLoggerApp logapp) {

		logappi = logapp;
		frequency = "500";
		readingStyle = SerialComToFile.NONBLOCKING_PERIODIC;
		JMenuBar menubar = new JMenuBar();

		/* File menu */
		JMenu file = new JMenu("File");
		file.setMnemonic(KeyEvent.VK_F);

		JMenuItem fileExit = new JMenuItem("Exit");
		fileExit.setMnemonic(KeyEvent.VK_C);
		fileExit.setToolTipText("Exit application");
		fileExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,ActionEvent.CTRL_MASK));
		fileExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				logappi.triggerAppExit();
			}
		});

		file.add(fileExit);
		menubar.add(file);

		/* Preference menu */
		JMenu pref = new JMenu("Prefrences");
		JMenu submenurs = new JMenu("Read style");

		periodic = new JCheckBoxMenuItem("Non-blocking periodic");
		periodic.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ie) {
				int state = ie.getStateChange();
				if (state == ItemEvent.SELECTED) {
					readingStyle = SerialComToFile.NONBLOCKING_PERIODIC;
					freq.setEnabled(true);
					blocking.setSelected(false);
					blocking.setEnabled(false);
					dlisten.setSelected(false);
					dlisten.setEnabled(false);
				}else {
					freq.setEnabled(false);
					blocking.setEnabled(true);
					dlisten.setEnabled(true);
				}
			}
		});

		blocking = new JCheckBoxMenuItem("Blocking read");
		blocking.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ie) {
				if (ItemEvent.SELECTED == ie.getStateChange()) {
					readingStyle = SerialComToFile.BLOCKING;
					periodic.setSelected(false);
					periodic.setEnabled(false);
					freq.setEnabled(false);
					dlisten.setSelected(false);
					dlisten.setEnabled(false);
				}else {
					periodic.setEnabled(true);
					dlisten.setEnabled(true);
				}
			}
		});

		dlisten = new JCheckBoxMenuItem("Data listener based");
		dlisten.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ie) {
				if (ItemEvent.SELECTED == ie.getStateChange()) {
					readingStyle = SerialComToFile.DATALISTENER;
					periodic.setSelected(false);
					periodic.setEnabled(false);
					freq.setEnabled(false);
					dlisten.setSelected(false);
					dlisten.setEnabled(false);
				}else {
					blocking.setEnabled(true);
					periodic.setEnabled(true);
				}
			}
		});

		freq = new JTextField("Interval (millisseconds)");
		freq.setEnabled(false);
		freq.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String val = freq.getText();
				if((val != null) && (val.length() != 0)) {
					if(val.equalsIgnoreCase("Interval (millisseconds)")) {
						frequency = "500";
					}else {
						frequency = val;
					}
				}else {
					frequency = "500";
				}
			}
		});

		submenurs.add(periodic);
		submenurs.add(blocking);
		submenurs.add(dlisten);
		submenurs.add(freq);

		pref.add(submenurs);
		menubar.add(pref);

		return menubar;
	}

	public int[] getReadParameters() {
		int[] style = new int[2];
		style[0] = readingStyle;
		style[1] = Integer.parseInt(frequency);
		return style;
	}
}
