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

package commandreceiver;

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
		portStatus.setPreferredSize(new Dimension(400, 20));
		portStatus.setBackground(new Color(214, 217, 223));
		portStatus.setText("-> Connect serial device, configure it and click connect.");
		portStatus.setBorder(javax.swing.BorderFactory.createEmptyBorder());
		portStatus.setEditable(false);
		this.add(portStatus);

		extraInfo = new JTextField();
		extraInfo.setPreferredSize(new Dimension(400, 20));
		extraInfo.setBackground(new Color(214, 217, 223));
		extraInfo.setText("-> Specify email id and token & click on login button.");
		extraInfo.setBorder(javax.swing.BorderFactory.createEmptyBorder());
		extraInfo.setEditable(false);
		this.add(extraInfo);

		this.add(new JPanel());
	}

	public void setProgStatus(boolean portConnection) {
		portStatus.setText("                                                             ");
		if(portConnection == true) {
			portStatus.setText("-> Port is connected");
		}else if(portConnection == true) {
			portStatus.setText("-> Port is connected");
		}else if(portConnection == false) {
			portStatus.setText("-> Port is not connected");
		}else {
			portStatus.setText("-> Port is not connected");
		}
	}

	public void setExtraInfo(String extraInfoStr) {
		extraInfo.setText("-> " + extraInfoStr + " !");
	}
}
