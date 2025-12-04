package com.se310.store.security;

import com.se310.store.model.User;
import com.se310.store.service.AuthenticationService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;

/**
 * AuthenticationFilter - Servlet filter that authenticates users via HTTP Basic Authentication
 *
 * This filter intercepts requests to protected endpoints and validates user credentials.
 * If authentication succeeds, the User object is stored in the request attribute "authenticatedUser".
 * If authentication fails, returns HTTP 401 Unauthorized.
 *
 * @author  Sergey L. Sundukovskiy
 * @version 1.0
 * @since   2025-11-11
 */
public class AuthenticationFilter implements Filter {

    private AuthenticationService authenticationService;

    public AuthenticationFilter() {
    }

    public AuthenticationFilter(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * Initialize filter - can be used to set up authentication service
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // AuthenticationService should be injected via constructor
        // or obtained from servlet context
        if (authenticationService == null) {
            ServletContext context = filterConfig.getServletContext();
            authenticationService = (AuthenticationService) context.getAttribute("authenticationService");
        }
    }

    /**
     * Filter method that authenticates requests using HTTP Basic Authentication
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Extract Authorization header
        String authHeader = httpRequest.getHeader("Authorization");

        // Attempt authentication
        Optional<User> userOpt = authenticationService.authenticateBasic(authHeader);

        if (userOpt.isPresent()) {
            // Authentication successful - store user in request attribute
            request.setAttribute("authenticatedUser", userOpt.get());

            // Continue with the filter chain
            chain.doFilter(request, response);
        } else {
            // Authentication failed - return 401 Unauthorized with WWW-Authenticate header
            // This triggers the browser's built-in username/password dialog
            httpResponse.setHeader("WWW-Authenticate", "Basic realm=\"Smart Store Application\"");
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json");
            httpResponse.setCharacterEncoding("UTF-8");
            httpResponse.getWriter().write("{\"error\": \"Unauthorized - Invalid credentials\"}");
        }
    }

    /**
     * Cleanup method called when filter is destroyed
     */
    @Override
    public void destroy() {
        // No cleanup needed
    }
}
