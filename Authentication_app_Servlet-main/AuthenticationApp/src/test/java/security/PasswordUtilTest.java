package security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.*;


class PasswordUtilTest {

	
	 @Test
	    @DisplayName("Should successfully hash and verify a password")
	    void testPasswordHashing() {
		 
		 
	        String rawPassword = "BankSecure123";
	        
	      
	        	// 1. Test Hashing
		        String hashed = PasswordUtil.hashedPassword(rawPassword);
		        assertNotNull(hashed);
		        assertNotEquals(rawPassword, hashed);
		        
		        // 2. Test Verification
		        boolean isMatch = PasswordUtil.verifypassword(rawPassword, hashed);
		        assertTrue(isMatch, "Password verification should return true for correct password");
	        }
	    


		@Test
	    @DisplayName("Should fail verification with wrong password")
	    void testWrongPassword() {
	        String hashed = PasswordUtil.hashedPassword("myPass");
	        assertFalse(PasswordUtil.verifypassword("wrongPass", hashed));
	    }
		
}
