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

import javax.security.auth.callback.CallbackHandler;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslClientFactory;
import java.util.Map;

public final class GmailOAuth2SaslClientFactory implements SaslClientFactory {

	public static final String OAUTH_TOKEN_PROP = "mail.imaps.sasl.mechanisms.oauth2.oauthToken";

	public SaslClient createSaslClient(String[] mechanisms, String authorizationId, String protocol, 
			String serverName, Map<String, ?> props, CallbackHandler callbackHandler) {

		for (int x=0; x < mechanisms.length; x++) {
			if ("XOAUTH2".equalsIgnoreCase(mechanisms[x])) {
				return new GmailOAuth2SaslClient((String) props.get(OAUTH_TOKEN_PROP), callbackHandler);
			}
		}

		return null;
	}

	public String[] getMechanismNames(Map<String, ?> props) {
		return new String[] {"XOAUTH2"};
	}
}
