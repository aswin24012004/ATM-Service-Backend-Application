package controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class RegisterServletTest {

	@Test
    void testDoPost_RegistrationSuccess() throws Exception {
        // 1. Mock Request and Response
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        // 2. Mock input parameters
        when(request.getParameter("name")).thenReturn("Aswin_Test");
        when(request.getParameter("email")).thenReturn("aswin@test.com");
        when(request.getParameter("password")).thenReturn("Pass@123");

        // 3. Mock PrintWriter to check the output message
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        // 4. Execute the Servlet
        RegisterServlet servlet = new RegisterServlet();
        servlet.doPost(request, response);

        // 5. Verify the results
        writer.flush();
        String output = stringWriter.toString().trim();
        
        assertEquals("User Registered Successfully!", output, "Should receive success message");
        // Verify that we didn't hit the 500 error block
        verify(response, never()).setStatus(500);
    }

    @Test
    void testDoGet_ServiceStatus() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        RegisterServlet servlet = new RegisterServlet();
        servlet.doGet(request, response);

        writer.flush();
        assertTrue(stringWriter.toString().contains("Auth Service is online"));
    }

}
