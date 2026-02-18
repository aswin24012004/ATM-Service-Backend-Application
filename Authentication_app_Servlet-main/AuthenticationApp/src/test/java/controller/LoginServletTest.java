package controller;
 //     controller
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;

import jakarta.servlet.http.*;

class LoginServletTest {

    @Test
    void testDoPost_Success() throws Exception {
        // 1. Mock the Request and Response
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        // 2. Set up mock inputs
        when(request.getParameter("name")).thenReturn("user1");
        when(request.getParameter("password")).thenReturn("123");

        // 3.PrintWriter to get the Servlet output
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        // 4. Run  Servlet method
        LoginServlet servlet = new LoginServlet();
        servlet.doPost(request, response);

        // 5. Verify results
        writer.flush();
        String result = stringWriter.toString();
        
        assertTrue(result.contains("success"), "Response should indicate success");
        assertTrue(result.contains("Login Successful"), "Message should be Login Successful");
        verify(response).setHeader(eq("Authorization"), contains("Bearer "));
    }
}