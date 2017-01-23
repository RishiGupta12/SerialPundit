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
import java.awt.GridLayout;
import javax.swing.JPanel;
import javax.swing.JTextField;

public final class ProgramStatusPanel extends JPanel {

	private static final long serialVersionUID = 4669301182777946770L;
	private JTextField portStatus;
	private JTextField extraInfo;

	public void initPanel() {
		this.setLayout(new GridLayout(0, 1, 0, 1));
		this.setBackground(new Color(214, 217, 223));

		portStatus = new JTextField();
		portStatus.setPreferredSize(new Dimension(709, 20));
		portStatus.setBackground(new Color(214, 217, 223));
		portStatus.setText("-> Connect serial device, configure it and click connect. Low now/later panels will be enabled.");
		portStatus.setBorder(javax.swing.BorderFactory.createEmptyBorder());
		portStatus.setEditable(false);
		this.add(portStatus);

		extraInfo = new JTextField();
		extraInfo.setPreferredSize(new Dimension(709, 20));
		extraInfo.setBackground(new Color(214, 217, 223));
		extraInfo.setText("-> Specify log file and click on start button.");
		extraInfo.setBorder(javax.swing.BorderFactory.createEmptyBorder());
		extraInfo.setEditable(false);
		this.add(extraInfo);
	}

	public void setProgStatus(boolean portConnection, boolean loggingIsOn) {
		portStatus.setText("                                                             ");
		if((portConnection == true) && (loggingIsOn == true)) {
			portStatus.setText("-> Port is connected. Logging is in progress.");
		}else if((portConnection == true) && (loggingIsOn == false)) {
			portStatus.setText("-> Port is connected. Logging is off.");
		}else if((portConnection == false) && (loggingIsOn == true)) {
			portStatus.setText("-> Port is not connected. Logging is in progress.");
		}else {
			portStatus.setText("-> Port is not connected. Logging is off.");
		}
	}

	public void setExtraInfo(String extraInfoStr) {
		extraInfo.setText("-> " + extraInfoStr + " !");
	}
}
