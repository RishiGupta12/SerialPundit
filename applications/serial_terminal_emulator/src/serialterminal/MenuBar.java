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

package serialterminal;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public final class MenuBar {

	private final SerialTerminalApp sertermapp;

	public MenuBar(SerialTerminalApp sertermapp) {
		this.sertermapp = sertermapp;
	}

	public JMenuBar setUpMenuBar() {

		JMenuBar menubar = new JMenuBar();

		// file menu
		JMenu file = new JMenu("File");
		file.setMnemonic(KeyEvent.VK_F);

		// exit option
		JMenuItem fileExit = new JMenuItem("Exit");
		fileExit.setMnemonic(KeyEvent.VK_C);
		fileExit.setToolTipText("Exit application");
		fileExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,ActionEvent.CTRL_MASK));
		fileExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sertermapp.triggerAppExit();
			}
		});
		file.add(fileExit);

		menubar.add(file);

		return menubar;
	}
}
