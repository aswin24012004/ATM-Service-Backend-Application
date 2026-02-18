package db;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.Connection;
import org.junit.jupiter.api.Test;


class DBConnectionTest {

	@Test
	void testGetConnection() {
		try(Connection con = DBConnection.getConnection()){
			assertNotNull(con ,"Connection should not be null" );
			assertTrue(con.isValid(2),"Connection should be valid and open");
			assertEquals("MySQL", con.getMetaData().getDatabaseProductName());
		}catch(Exception e) {
			fail("Database connection failed: " + e.getMessage());
		}
	}

}
