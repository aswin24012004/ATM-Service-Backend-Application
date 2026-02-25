package com.atm.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EncryptionUtilTest {

    @Test
    void testEncryptAndDecrypt() throws Exception {
        String original = "sensitivedata007";

//         Encrypt
        String encrypted = EncryptionUtil.encrypt(original);
        assertNotNull(encrypted, "Encrypted string should not be null");
        assertNotEquals(original, encrypted, "Ciphertext should differ from plaintext");

//         Decrypt
        String decrypted = EncryptionUtil.decrypt(encrypted);
        assertEquals(original, decrypted, "Decrypted text should match original");
    }

    @Test
    void testDifferentInputsProduceDifferentCiphertext() throws Exception {
        String data1 = "HelloWorld";
        String data2 = "NewString";

        String encrypted_data1 = EncryptionUtil.encrypt(data1);
        String encrypted_data2 = EncryptionUtil.encrypt(data2);

        assertNotEquals(encrypted_data1, encrypted_data2, "Different inputs should produce different ciphertext");
    }

    @Test
    void testDecryptInvalidCiphertextThrowsException() {
        String invalidCiphertext = "NotBase64OrAES";

        assertThrows(Exception.class, () -> {
            EncryptionUtil.decrypt(invalidCiphertext);
        }, "Decrypting invalid ciphertext should throw an exception");
    }
}
