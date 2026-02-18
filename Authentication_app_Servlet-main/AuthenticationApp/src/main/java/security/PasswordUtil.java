package security;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {
	public static String hashedPassword(String password) {
		return BCrypt.hashpw(password, BCrypt.gensalt(12));
	}
	
	public static  boolean verifypassword(String password, String hashed) {
		return BCrypt.checkpw(password, hashed);
	}
}
