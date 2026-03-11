package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.atm.model.Transaction;

import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    private Transaction transaction;

    @BeforeEach
    void setUp() {
        transaction = new Transaction();
    }

    @Test
    void testTransactionDataIntegrity() {
        int id = 101;
        String user = "testUser";
        String type = "WITHDRAWAL";
        double amount = 500.0;
        Date now = new Date();
//        set values
        transaction.setId(id);
        transaction.setUsername(user);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setTimestamp(now);

//         Assert
        assertAll("Verify all Transaction properties",
            () -> assertEquals(id, transaction.getId()),
            () -> assertEquals(user, transaction.getUsername()),
            () -> assertEquals(type, transaction.getType()),
            () -> assertEquals(amount, transaction.getAmount()),
            () -> assertEquals(now, transaction.getTimestamp())
        );
    }

    @Test
    void testAmountPrecision() {
        transaction.setAmount(123.456);
        assertEquals(123.456, transaction.getAmount(), 0.001, "Amount should support decimals");
    }
}
