package com.atm.util;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class EncryptionUtilTest {
    private MockedStatic<ConfigUtil> configMock;
    @BeforeEach
    void setUp() {
        configMock = mockStatic(ConfigUtil.class);
        configMock.when(() -> ConfigUtil.get("SECRET"))
                  .thenReturn("12345678901234567890123456789012"); // 32 chars
    }
    @AfterEach
    void tearDown() {
        configMock.close();
    }
    @Test
    void testEncryptAndDecrypt() throws Exception {
        String original = "SensitiveData123";
        String encrypted = EncryptionUtil.encrypt(original);
        assertNotNull(encrypted);
        assertNotEquals(original, encrypted);
        String decrypted = EncryptionUtil.decrypt(encrypted);
        assertEquals(original, decrypted);
    }
    @Test
    void testDifferentInputsProduceDifferentCiphertext() throws Exception {
        String encrypted1 = EncryptionUtil.encrypt("HelloWorld");
        String encrypted2 = EncryptionUtil.encrypt("NewString");
        assertNotEquals(encrypted1, encrypted2);
    }
    @Test
    void testDecryptInvalidCiphertextThrowsException() {
        assertThrows(Exception.class, () -> EncryptionUtil.decrypt("NotBase64OrAES"));
    }
    @Test
    void testKeyLengthValidation() {
        configMock.when(() -> ConfigUtil.get("SECRET")).thenReturn("shortkey");
        assertThrows(IllegalArgumentException.class, () -> EncryptionUtil.encrypt("data"));
    }
}
