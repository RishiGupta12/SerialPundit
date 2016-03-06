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

import java.io.IOException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslException;

// Performs SASL authentication as a client.
public final class GmailOAuth2SaslClient implements SaslClient {

	private final String oauthToken;
	private final CallbackHandler callbackHandler;

	private boolean isComplete = false;

	/**
	 * Creates a new instance of the OAuth2SaslClient. This will ordinarily only
	 * be called from OAuth2SaslClientFactory.
	 */
	public GmailOAuth2SaslClient(String oauthToken, CallbackHandler callbackHandler) {
		this.oauthToken = oauthToken;
		this.callbackHandler = callbackHandler;
	}

	public String getMechanismName() {
		return "XOAUTH2";
	}

	public boolean hasInitialResponse() {
		return true;
	}

	public byte[] evaluateChallenge(byte[] challenge) throws SaslException {
		if (isComplete) {
			// Empty final response from server, just ignore it.
			return new byte[] { };
		}

		NameCallback nameCallback = new NameCallback("Enter name");
		Callback[] callbacks = new Callback[] { nameCallback };
		try {
			callbackHandler.handle(callbacks);
		} catch (UnsupportedCallbackException e) {
			throw new SaslException("Unsupported callback: " + e);
		} catch (IOException e) {
			throw new SaslException("Failed to execute callback: " + e);
		}
		String email = nameCallback.getName();

		byte[] response = String.format("user=%s\1auth=Bearer %s\1\1", email, oauthToken).getBytes();
		isComplete = true;
		return response;
	}

	public boolean isComplete() {
		return isComplete;
	}

	public byte[] unwrap(byte[] incoming, int offset, int len) throws SaslException {
		throw new IllegalStateException();
	}

	public byte[] wrap(byte[] outgoing, int offset, int len) throws SaslException {
		throw new IllegalStateException();
	}

	public Object getNegotiatedProperty(String propName) {
		if (!isComplete()) {
			throw new IllegalStateException();
		}
		return null;
	}

	public void dispose() throws SaslException {
	}
}
