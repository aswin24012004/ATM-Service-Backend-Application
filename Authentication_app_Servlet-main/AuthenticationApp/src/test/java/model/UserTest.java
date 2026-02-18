package model;

//ONLY JUnit 5 imports
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class UserTest {
	private User user;
	
	@BeforeEach
	void setup() {
		user = new User(1,"Aswin","abc@asd.com","123");
	}
	
	@Test
	void testFullConstructorAndGetters() {
        assertAll("Verify all fields in full constructor",
            () -> assertEquals(1, user.getId()),
            () -> assertEquals("Aswin", user.getName()),
            () -> assertEquals("abc@asd.com", user.getEmail()),
            () -> assertEquals("123", user.getPassword())
        );
    }
	
	@Test
    void testConstructorWithoutId() {
        User userNoId = new User("John", "john@gmail.com", "secure456");
        assertAll("Verify constructor used for new registration",
            () -> assertEquals(0, userNoId.getId()), 
            () -> assertEquals("John", userNoId.getName()),
            () -> assertEquals("john@gmail.com", userNoId.getEmail())
        );
    }
	
	@Test
    void testSetters() {
        user.setName("user");
        user.setEmail("user@email.com");
//        System.out.println(user.getName());
//        assertEquals("user33",user.getName(),"Not updated");
        assertEquals("user", user.getName(), "Name should be updated");
       assertEquals("user@email.com", user.getEmail(), "Email should be updated");
    }
//	@Test
//    void testSetters() {
//        // 1. Act
//        user.setName("user");
//        user.setEmail("user@email.com");
//        
//        // 2. Assert
//        // JUnit 5 syntax: assertEquals(Expected, Actual, Message)
//        assertEquals("user", user.getName(), "Name should be updated");
//        assertEquals("user@email.com", user.getEmail(), "Email should be updated");
//    }
}
