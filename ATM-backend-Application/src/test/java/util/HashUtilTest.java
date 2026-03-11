package util;

import org.junit.jupiter.api.Test;

import com.atm.util.HashUtil;

import static org.junit.jupiter.api.Assertions.*;

class HashUtilTest {

    @Test
    void testHashPasswordGeneratesDifferentHash() {
        String password = "mySecret123";
        String hash1 = HashUtil.hashPassword(password);
        String hash2 = HashUtil.hashPassword(password);

        assertNotNull(hash1);
        assertNotNull(hash2);
        assertNotEquals(password, hash1); 
        assertNotEquals(hash1, hash2);    
    }

    @Test
    void testCheckPasswordValid() {
        String password = "securePass";
        String hash = HashUtil.hashPassword(password);

        assertTrue(HashUtil.checkPassword(password, hash));
    }

    @Test
    void testCheckPasswordInvalid() {
        String password = "securePass";
        String hash = HashUtil.hashPassword(password);

        assertFalse(HashUtil.checkPassword("wrongPass", hash));
    }
}
