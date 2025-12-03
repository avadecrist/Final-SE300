package com.se300.store;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Test-only filter that captures any Throwable during request processing
 * and writes the full stack trace into the HTTP response body. This
 * is used only in tests to surface server-side exceptions without
 * modifying production code.
 */
public class TestExceptionLoggingFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // no-op
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } catch (Throwable t) {
            HttpServletResponse http = (HttpServletResponse) response;
            http.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            http.setContentType("text/plain;charset=UTF-8");
            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            http.getWriter().write(sw.toString());
            http.getWriter().flush();
        }
    }

    @Override
    public void destroy() {
        // no-op
    }
}
