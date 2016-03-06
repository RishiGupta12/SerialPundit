/*
 * Author : Rishi Gupta
 * 
 * This file is part of 'serial communication manager' library.
 * Copyright (C) <2014-2016>  <Rishi Gupta>
 *
 * This 'serial communication manager' is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by the Free Software 
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * The 'serial communication manager' is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR 
 * A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with 'serial communication manager'.  If not, see <http://www.gnu.org/licenses/>.
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
