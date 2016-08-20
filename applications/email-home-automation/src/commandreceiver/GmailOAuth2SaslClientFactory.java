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
