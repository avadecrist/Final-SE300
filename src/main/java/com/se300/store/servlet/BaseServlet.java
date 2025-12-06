package com.se300.store.servlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Base servlet providing common functionality for all API servlets.
 *
 * @author Sergey L. Sundukovskiy, Ph.D.
 * @version 1.0
 */
public abstract class BaseServlet extends HttpServlet {

    /**
     * Read the request body as a string.
     * Used for parsing JSON payloads from POST/PUT requests.
     *
     * @param request The HTTP request
     * @return The request body as a string
     * @throws IOException If reading fails
     */
    public String readRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder buffer = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }
        return buffer.toString();
    }

    /**
     * Send a JSON response with HTTP 200 OK status.
     *
     * @param response The HTTP response
     * @param object The object to serialize to JSON
     * @throws IOException If writing fails
     */
    protected void sendJsonResponse(HttpServletResponse response, Object object) throws IOException {
        sendJsonResponse(response, object, HttpServletResponse.SC_OK);
    }

    /**
     * Send a JSON response with a specified HTTP status code.
     *
     * @param response The HTTP response
     * @param object The object to serialize to JSON
     * @param statusCode The HTTP status code (200, 201, 400, etc.)
     * @throws IOException If writing fails
     */
    protected void sendJsonResponse(HttpServletResponse response, Object object, int statusCode) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(statusCode);

        PrintWriter out = response.getWriter();
        out.print(JsonHelper.toJson(object));
        out.flush();
    }

    /**
     * Send an error response with a message.
     *
     * @param response The HTTP response
     * @param statusCode The HTTP status code (400, 404, 500, etc.)
     * @param message The error message
     * @throws IOException If writing fails
     */
    protected void sendErrorResponse(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(statusCode);

        // Create a simple error object
        ErrorResponse error = new ErrorResponse(statusCode, message);
        PrintWriter out = response.getWriter();
        out.print(JsonHelper.toJson(error));
        out.flush();
    }

    /**
     * Extract the resource ID from the request path.
     * For example, "/api/v1/deals/DEAL-001" returns "DEAL-001"
     *
     * @param request The HTTP request
     * @return The resource ID, or null if not present
     */
    // changed to public to help with testing
    public String extractResourceId(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            return null;
        }

        // Remove leading slash and extract ID
        String[] parts = pathInfo.substring(1).split("/");
        //return parts.length > 0 ? parts[0] : null;

        // altered the return statement because after split, .length always returns a value greater than 0
        return parts[0];
    }


    /**
     * Simple error response object for consistent error formatting.
     */
    // Changed to public for testing
    public static class ErrorResponse {
        private final int status;
        private final String message;
        private final long timestamp;

        public ErrorResponse(int status, String message) {
            this.status = status;
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }

        public int getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}