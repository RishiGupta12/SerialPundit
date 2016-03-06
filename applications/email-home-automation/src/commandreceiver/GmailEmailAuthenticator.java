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

import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.IMAPSSLStore;
import java.security.Provider;
import java.security.Security;
import java.util.Properties;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.URLName;

public final class GmailEmailAuthenticator {

	private IMAPStore imapStore;
	private GmailEmailReader emailReader;
	private ProgramStatusPanel pstatusPaneli;
	private PortSettingsPanel psPaneli;

	// vendor specific provider that encapsulates security service.
	public static final class OAuth2Provider extends Provider {
		private static final long serialVersionUID = -1117052426976292694L;
		public OAuth2Provider() {
			super("Google OAuth2 Provider", 1.0, "Provides the XOAUTH2 SASL Mechanism");
			put("SaslClientFactory.XOAUTH2", "commandreceiver.GmailOAuth2SaslClientFactory");
		}
	}

	private final class DoLogin implements Runnable {

		private String userEmaili;
		private String oauthTokeni;
		private EmailUIPanel euiPaneli;

		public DoLogin(String userEmail, String authToken, EmailUIPanel euiPanel) {
			this.userEmaili = userEmail;
			this.oauthTokeni = authToken;
			this.euiPaneli = euiPanel;
			emailReader = new GmailEmailReader(pstatusPaneli, psPaneli);
		}

		@Override
		public void run() {

			Properties props = new Properties();
			props.put("mail.imaps.sasl.enable", "true");
			props.put("mail.imaps.sasl.mechanisms", "XOAUTH2");
			props.put(GmailOAuth2SaslClientFactory.OAUTH_TOKEN_PROP, oauthTokeni);

			Session session = Session.getInstance(props);

			// set this to true for receiving debugging message about IMAP.
			session.setDebug(false);

			final URLName unusedUrlName = null;
			IMAPSSLStore store = new IMAPSSLStore(session, unusedUrlName);
			final String emptyPassword = "";

			try {
				store.connect("imap.gmail.com", 993, userEmaili, emptyPassword);

				imapStore = store;

				// update login status on UI
				euiPaneli.setLoginStatus(true, null);


				// After successful login, start reading inbox and looking for command.
				emailReader.setImapStore(store);
				Thread tt = new Thread(emailReader);
				tt.start();
			} catch (MessagingException e) {
				euiPaneli.setLoginStatus(false,  e);
			}
		}
	}

	private final class DoLogout implements Runnable {

		private EmailUIPanel euiPaneli;

		public DoLogout(EmailUIPanel euiPanel) {
			this.euiPaneli = euiPanel;
		}

		@Override
		public void run() {
			if(emailReader != null) {
				try {
					emailReader.close();
				} catch (MessagingException e) {
					euiPaneli.setLogoutStatus(false, e);
				}
				try {
					if(imapStore != null) {
						imapStore.close();
					}
					imapStore = null;
				} catch (MessagingException e) {
					euiPaneli.setLogoutStatus(false, e);
				}
			}
		}
	}

	// Install authentication custom provider.
	public GmailEmailAuthenticator(ProgramStatusPanel pstatusPanel, PortSettingsPanel psPanel) {
		pstatusPaneli = pstatusPanel;
		psPaneli = psPanel;
		Security.addProvider(new OAuth2Provider());
	}

	// Invoked when user clicks login button.
	public void login(String userEmail, String authToken, EmailUIPanel euiPanel) {
		Thread t = new Thread(new DoLogin(userEmail, authToken, euiPanel));
		t.start();
	}

	// Invoked when user clicks logout button.
	public void logout(EmailUIPanel euiPanel) {
		Thread t = new Thread(new DoLogout(euiPanel));
		t.start();
	}
}
