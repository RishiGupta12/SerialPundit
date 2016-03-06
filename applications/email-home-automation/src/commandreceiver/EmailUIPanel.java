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
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public final class EmailUIPanel extends JPanel {

	private static final long serialVersionUID = 6541052212141777773L;
	private PortSettingsPanel psPaneli;
	private ProgramStatusPanel progStatusPaneli;
	private EmailUIPanel euiPanel;
	private JLabel emailLabel;
	private JTextField emailText;
	private JLabel oauthLabel;
	private JTextField oauthtokenText;
	private JButton loginButton;
	private String userEmail;
	private String authToken;
	private GmailEmailAuthenticator authenticator;
	private boolean loggedin;

	public void initPanel(PortSettingsPanel psPanel, ProgramStatusPanel progStatusPanel) {

		psPaneli = psPanel;
		progStatusPaneli = progStatusPanel;
		euiPanel = this;

		authenticator = new GmailEmailAuthenticator(progStatusPaneli, psPaneli);

		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1), "Email settings"));

		JPanel innerJPanel = new JPanel();
		innerJPanel.setLayout(new GridLayout(0, 2, 0, 14));
		innerJPanel.setBorder(BorderFactory.createEmptyBorder(8, 5, 5, 0));

		emailLabel = new JLabel("Email id :");
		emailText = new JTextField();
		emailText.setPreferredSize(new Dimension(250, 20));

		oauthLabel = new JLabel("O auth token :");
		oauthtokenText = new JTextField();
		oauthtokenText.setPreferredSize(new Dimension(250, 20));

		// SINGLE USER LOGIN handling
		loginButton = new JButton();
		loginButton.setPreferredSize(new Dimension(100, 20));
		loginButton.setText("Login");
		loginButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if((loggedin == false) && ("Login".equals((String) e.getActionCommand()))) {

					// get auth token
					String token = oauthtokenText.getText();
					if((token != null) && (token.length() != 0)) {
						authToken = token.trim();
					}else {
						progStatusPaneli.setExtraInfo("Invalid authentication token");
						return;
					}

					// get email id
					String email = emailText.getText();
					if((email != null) && (email.length() != 0)) {
						userEmail = email.trim();
					}else {
						progStatusPaneli.setExtraInfo("Invalid email id");
						return;
					}

					// login using information
					authenticator.login(userEmail, authToken, euiPanel);
				}else if((loggedin == true) && ("Log out".equals((String) e.getActionCommand()))) {
					authenticator.logout(euiPanel);
				}else {
				}
			}
		});
		loginButton.setEnabled(true);

		innerJPanel.add(emailLabel);
		innerJPanel.add(emailText);
		innerJPanel.add(oauthLabel);
		innerJPanel.add(oauthtokenText);
		innerJPanel.add(new JPanel());
		innerJPanel.add(loginButton);

		this.add(innerJPanel);
	}

	public void setLoginStatus(boolean state, Exception e) {
		if(state == true) {
			loginButton.setText("Log out");
			progStatusPaneli.setExtraInfo("Logged in successfully");
			loggedin = true;
		}else {
			loggedin = false;
			progStatusPaneli.setExtraInfo(e.getMessage());
		}
	}

	public void setLogoutStatus(boolean state, Exception e) {
		if(state == true) {
			loginButton.setText("Login");
			progStatusPaneli.setExtraInfo("Logged out successfully");
			loggedin = false;
		}else {
			loggedin = true;
			progStatusPaneli.setExtraInfo(e.getMessage());
		}
	}

	public void exit() {
		authenticator.logout(euiPanel);
		psPaneli.exit();
	}
}
