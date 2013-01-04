/**
 *
 */
package org.woltage.irssiconnectbot.util;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.woltage.irssiconnectbot.bean.PubkeyBean;

import com.trilead.ssh2.crypto.PEMDecoder;

/**
 * @author seaders
 *
 */
public class KeyUtils {

	public static Object DecodeKey(PubkeyBean pubkey, String password) throws IOException, Exception {
		Object trileadKey = null;
		if(PubkeyDatabase.KEY_TYPE_IMPORTED.equals(pubkey.getType())) {
			// load specific key using pem format
			trileadKey = PEMDecoder.decode(new String(pubkey.getPrivateKey()).toCharArray(), password);

		} else {
			// load using internal generated format
			PrivateKey privKey = PubkeyUtils.decodePrivate(pubkey.getPrivateKey(), pubkey.getType(), password);
			PublicKey pubKey = PubkeyUtils.decodePublic(pubkey.getPublicKey(), pubkey.getType());

			// convert key to trilead format
			trileadKey = PubkeyUtils.convertToTrilead(privKey, pubKey);
		}

		return trileadKey;
	}
}
