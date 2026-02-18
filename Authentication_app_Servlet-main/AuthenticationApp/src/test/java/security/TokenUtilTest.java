package security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TokenUtilTest {

	@Test
    void testTokenCycle() {
        String originalUser = "aswin_dev";
        
        // 1. Generate
        String token = TokenUtil.generateToken(originalUser);
        assertNotNull(token, "Token should not be null");
        
        // 2. Validate
        String extractedUser = TokenUtil.validateToken(token);
        assertEquals(originalUser, extractedUser, "The extracted username must match the original");
    }

    @Test
    void testInvalidToken() {
        String result = TokenUtil.validateToken("invalid.token.here");
        assertNull(result, "An invalid token should return null");
    }
    

}
