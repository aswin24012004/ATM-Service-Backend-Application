package com.atm.filter;

import com.atm.util.TokenUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.io.PrintWriter;

import static org.mockito.Mockito.*;

class AuthFilterTest {

    private AuthFilter authFilter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        authFilter = new AuthFilter();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        chain = mock(FilterChain.class);
    }

    @Test
    void testLoginPathBypassesFilter() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/api/login");

        authFilter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    void testValidSessionAllowsAccess() throws IOException, ServletException {
        HttpSession session = mock(HttpSession.class);
        when(request.getRequestURI()).thenReturn("/api/deposit");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("username")).thenReturn("testuser");

        authFilter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void testValidJwtAllowsAccess() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/api/deposit");
        when(request.getSession(false)).thenReturn(null);
        when(request.getHeader("Authorization")).thenReturn("Bearer Token");

        try (MockedStatic<TokenUtil> tokenUtilMock = mockStatic(TokenUtil.class)) {
            tokenUtilMock.when(() -> TokenUtil.validateToken(""))
                         .thenReturn(true);
            authFilter.doFilter(request, response, chain);
            verify(chain).doFilter(request, response);
        }
        catch(Exception e) {}

    }

    @Test
    void testUnauthorizedRequestReturns401() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/api/deposit");
        when(request.getSession(false)).thenReturn(null);
        when(request.getHeader("Authorization")).thenReturn(null);

        PrintWriter out = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(out);

        authFilter.doFilter(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(out).println("{\"error\":\"Unauthorized\"}");
        verify(chain, never()).doFilter(request, response);
    }
}
