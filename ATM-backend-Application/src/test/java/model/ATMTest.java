package model;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.atm.model.ATM;

import static org.junit.jupiter.api.Assertions.*;

class ATMTest {

    private ATM atm;

    
    @BeforeEach
    void setUp() {
        atm = new ATM(1, 100.0);  
    }


    @Test
    void testSetAndGetId() {
        int idValue = 1;
        atm.setId(idValue);
        assertEquals(idValue, atm.getId(), "The ID is should match in the value..");
    }

    @Test
    void testSetAndGetTotalBalance() {
        double balanceValue = 50000.0;
        atm.setTotalBalance(balanceValue);
        assertEquals(balanceValue, atm.getTotalBalance(), "The balance should be match in the value.");
    }

    @Test
    void testInitialValues() {
        assertEquals(1, atm.getId());
        assertEquals(100.0, atm.getTotalBalance());
    }
}