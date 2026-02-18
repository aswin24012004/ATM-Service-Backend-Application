package dao;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import model.User;

class UserDAOTest {

	@Test
    void testSaveAndFindByName() {
        // 1. Create a  test user
        String uniqueName = "TestUser_" + System.currentTimeMillis();
        User testUser = new User(uniqueName, "test@example.com", "secret123");

        // 2.  Save
        assertDoesNotThrow(() -> UserDAO.save(testUser), 
            "Save operation should not throw an exception");

        // 3.  Find and Verify
        User foundUser = UserDAO.findByName(uniqueName);
        
        assertNotNull(foundUser, "User should be found in the database");
        assertEquals(uniqueName, foundUser.getName());
        assertEquals("test@example.com", foundUser.getEmail());
    }
//	@Test
//	void testFindUnknownUser() {
//		String name = "abc";
//		User finduser = UserDAO.findByName(name);
//		assertNull(finduser, "The DAO should return null for an unknown username");
//	}
	@Test
    void testFindNonExistentUser() {
        User user = UserDAO.findByName("NonExistentUserXYZ");
        assertNull(user, "Searching for a non-existent user should return null");
    }

}
