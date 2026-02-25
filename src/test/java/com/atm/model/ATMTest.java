package com.atm.model;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ATMTest {

    private ATM atm;

    @BeforeEach
    void setUp() {
        atm = new ATM();
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
        assertEquals(0, atm.getId());
        assertEquals(0.0, atm.getTotalBalance());
    }
}