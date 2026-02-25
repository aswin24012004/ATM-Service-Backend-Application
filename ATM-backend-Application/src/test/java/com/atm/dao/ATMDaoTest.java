package com.atm.dao;

import com.atm.model.ATM;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ATMDaoTest {

    private JdbcTemplate jdbcTemplate;
    private ATMDao atmDao;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(JdbcTemplate.class);
        atmDao = new ATMDao() {
            { this.jdbc = jdbcTemplate; }
        };
    }

    @Test
    void testGetATMById() {
        ATM atm = new ATM();
        atm.setId(1);
        atm.setTotalBalance(5000.0);

        when(jdbcTemplate.queryForObject(anyString(), any(Object[].class), any(ATMDao.ATMRowMapper.class)))
                .thenReturn(atm);

        ATM result = atmDao.getATMById(1);

        assertEquals(1, result.getId());
        assertEquals(5000.0, result.getTotalBalance());
        verify(jdbcTemplate, times(1))
                .queryForObject(eq("SELECT * FROM atm WHERE id=?"), any(Object[].class), any(ATMDao.ATMRowMapper.class));
    }

    @Test
    void testUpdateBalance() {
        atmDao.updateBalance(1, 2000.0);

        verify(jdbcTemplate, times(1))
                .update(eq("UPDATE atm SET total_balance=? WHERE id=?"), eq(2000.0), eq(1));
    }

    @Test
    void testAddFunds() {
        atmDao.addFunds(1, 500.0);

        verify(jdbcTemplate, times(1))
                .update(eq("UPDATE atm SET total_balance = total_balance + ? WHERE id=?"), eq(500.0), eq(1));
    }

    @Test
    void testCheckBalance() {
        atmDao.checkBalance(1, 300.0);

        verify(jdbcTemplate, times(1))
                .update(eq("UPDATE atm SET total_balance = total_balance - ? WHERE id=?"), eq(300.0), eq(1));
    }
}
