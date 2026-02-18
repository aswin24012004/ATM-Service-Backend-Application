package security;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtil {
	private static final String KEY = "1234567890123456";
	
	public static String encrypt(String data) throws Exception{
		Cipher ci = Cipher.getInstance("AES");
		SecretKeySpec keysp = new SecretKeySpec(KEY.getBytes(),"AES");
		ci.init(Cipher.ENCRYPT_MODE, keysp);
		return Base64.getEncoder().encodeToString(ci.doFinal(data.getBytes()));
				}
}
