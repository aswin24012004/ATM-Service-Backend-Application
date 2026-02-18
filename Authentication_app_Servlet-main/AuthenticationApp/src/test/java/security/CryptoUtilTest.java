package security;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Base64;

import org.junit.jupiter.api.Test;

class CryptoUtilTest {

	@Test
	void testEncryptReturnsNonNullValue() throws Exception{
		String realData = "RealDataChecking123";
		String encryptedData = CryptoUtil.encrypt(realData);
		assertNotNull(encryptedData, "The encrypted string should not be null");
	}
	@Test
    void testEncryptConsistency() throws Exception {
        String data = "SecurePassword123";
        
        String firstEncryption = CryptoUtil.encrypt(data);
        String secondEncryption = CryptoUtil.encrypt(data);
        
        assertEquals(firstEncryption, secondEncryption, "Encrypting the same data => same result");
    }

  

    @Test
    void testEncryptionActuallyChangesData() throws Exception {
        String originalData = "BankingProject2026";
        String encryptedData = CryptoUtil.encrypt(originalData);
        
        assertNotEquals(originalData, encryptedData, "The encrypted data should not match the plain text");
    }

}
