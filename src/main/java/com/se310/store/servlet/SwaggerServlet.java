package com.se310.store.servlet;

import com.se310.store.model.User;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * SwaggerServlet - Serves OpenAPI documentation
 *
 * This servlet serves the OpenAPI specification file (openapi.yaml) which documents
 * all REST API endpoints. The specification can be used with Swagger UI or other
 * OpenAPI tools to visualize and test the API.
 *
 * Endpoints:
 * - GET /api/docs/openapi.yaml - Returns the OpenAPI specification
 * - GET /api/docs or /api/docs/ - Returns HTML page with link to Swagger UI
 *
 * @author  Sergey L. Sundukovskiy
 * @version 1.0
 * @since   2025-11-11
 */
public class SwaggerServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String requestURI = request.getRequestURI();
        String pathInfo = request.getPathInfo();

        // Debug logging
        System.out.println("SwaggerServlet - RequestURI: " + requestURI + ", PathInfo: " + pathInfo);

        // Handle /api-docs endpoint - serve raw YAML for Swagger UI
        if (requestURI.endsWith("/api-docs") || (pathInfo != null && pathInfo.equals("/api-docs"))) {
            serveRawOpenAPISpec(response);
            return;
        }

        // Handle /api-docs/endpoints - serve HTML display of endpoints
        if (requestURI.contains("/api-docs/endpoints") || (pathInfo != null && pathInfo.equals("/api-docs/endpoints"))) {
            serveOpenAPISpec(response);
            return;
        }

        // Handle /swagger-ui/ endpoint - serve the UI
        if (requestURI.contains("/swagger-ui") || pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
            serveSwaggerUI(request, response);
            return;
        }

        // Not found
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String requestURI = request.getRequestURI();

        // Handle logout request
        if (requestURI.contains("/swagger-ui/logout") || requestURI.contains("/api-docs/logout")) {
            handleLogout(response);
            return;
        }

        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    /**
     * Handle logout by returning 401 to force re-authentication
     */
    private void handleLogout(HttpServletResponse response) throws IOException {
        // Return 401 with WWW-Authenticate header to trigger browser's login dialog
        response.setHeader("WWW-Authenticate", "Basic realm=\"Smart Store Application\"");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"message\": \"Logged out successfully. Please refresh to login as a different user.\"}");
    }

    /**
     * Serves the Swagger UI HTML page with authentication info and logout capability.
     * Uses CDN-hosted Swagger UI for simplicity.
     */
    private void serveSwaggerUI(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Extract authenticated user info
        User authenticatedUser = (User) request.getAttribute("authenticatedUser");
        String userEmail = authenticatedUser != null ? authenticatedUser.getEmail() : "Unknown";
        String userName = authenticatedUser != null ? authenticatedUser.getName() : "Unknown User";
        String userRole = authenticatedUser != null ? authenticatedUser.getRole().name() : "UNKNOWN";

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html lang=\"en\">");
        out.println("<head>");
        out.println("    <meta charset=\"UTF-8\">");
        out.println("    <title>Smart Store API - Swagger UI</title>");
        out.println("    <link rel=\"stylesheet\" href=\"https://unpkg.com/swagger-ui-dist@5.10.3/swagger-ui.css\">");
        out.println("    <style>");
        out.println("        body {");
        out.println("            margin: 0;");
        out.println("            padding: 0;");
        out.println("            font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, sans-serif;");
        out.println("        }");
        out.println("        .topbar { display: none; }");
        out.println("        .auth-banner {");
        out.println("            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);");
        out.println("            color: white;");
        out.println("            padding: 15px 30px;");
        out.println("            display: flex;");
        out.println("            justify-content: space-between;");
        out.println("            align-items: center;");
        out.println("            box-shadow: 0 2px 10px rgba(0,0,0,0.1);");
        out.println("        }");
        out.println("        .auth-info {");
        out.println("            display: flex;");
        out.println("            flex-direction: column;");
        out.println("        }");
        out.println("        .auth-info-title {");
        out.println("            font-size: 14px;");
        out.println("            opacity: 0.9;");
        out.println("            margin-bottom: 5px;");
        out.println("        }");
        out.println("        .auth-info-details {");
        out.println("            font-size: 16px;");
        out.println("            font-weight: 600;");
        out.println("        }");
        out.println("        .auth-info-role {");
        out.println("            display: inline-block;");
        out.println("            background: rgba(255,255,255,0.2);");
        out.println("            padding: 3px 10px;");
        out.println("            border-radius: 12px;");
        out.println("            font-size: 12px;");
        out.println("            margin-left: 10px;");
        out.println("            font-weight: 500;");
        out.println("        }");
        out.println("        .logout-btn {");
        out.println("            background: rgba(255,255,255,0.2);");
        out.println("            border: 2px solid white;");
        out.println("            color: white;");
        out.println("            padding: 10px 20px;");
        out.println("            border-radius: 6px;");
        out.println("            cursor: pointer;");
        out.println("            font-size: 14px;");
        out.println("            font-weight: 600;");
        out.println("            transition: all 0.3s ease;");
        out.println("        }");
        out.println("        .logout-btn:hover {");
        out.println("            background: white;");
        out.println("            color: #667eea;");
        out.println("        }");
        out.println("    </style>");
        out.println("</head>");
        out.println("<body>");
        out.println("    <div class=\"auth-banner\">");
        out.println("        <div class=\"auth-info\">");
        out.println("            <div class=\"auth-info-title\">Authenticated as:</div>");
        out.println("            <div class=\"auth-info-details\">");
        out.println("                " + userName + " (" + userEmail + ")");
        out.println("                <span class=\"auth-info-role\">" + userRole + "</span>");
        out.println("            </div>");
        out.println("        </div>");
        out.println("        <button class=\"logout-btn\" onclick=\"openInNewWindow()\">Switch User (New Window)</button>");
        out.println("    </div>");
        out.println("    <div id=\"swagger-ui\"></div>");
        out.println("    <script src=\"https://unpkg.com/swagger-ui-dist@5.10.3/swagger-ui-bundle.js\"></script>");
        out.println("    <script src=\"https://unpkg.com/swagger-ui-dist@5.10.3/swagger-ui-standalone-preset.js\"></script>");
        out.println("    <script>");
        out.println("        window.onload = function() {");
        out.println("            const ui = SwaggerUIBundle({");
        out.println("                url: '/api-docs',");
        out.println("                dom_id: '#swagger-ui',");
        out.println("                deepLinking: true,");
        out.println("                presets: [");
        out.println("                    SwaggerUIBundle.presets.apis,");
        out.println("                    SwaggerUIStandalonePreset");
        out.println("                ],");
        out.println("                plugins: [");
        out.println("                    SwaggerUIBundle.plugins.DownloadUrl");
        out.println("                ],");
        out.println("                layout: \"StandaloneLayout\"");
        out.println("            });");
        out.println("            window.ui = ui;");
        out.println("        };");
        out.println();
        out.println("        function openInNewWindow() {");
        out.println("            const url = window.location.protocol + '//' + window.location.host + '/swagger-ui/';");
        out.println("            const message = 'To switch users, please:\\n\\n' +");
        out.println("                '1. Open a new Incognito/Private window:\\n' +");
        out.println("                '   • Chrome: Ctrl+Shift+N (Windows) or Cmd+Shift+N (Mac)\\n' +");
        out.println("                '   • Firefox: Ctrl+Shift+P (Windows) or Cmd+Shift+P (Mac)\\n' +");
        out.println("                '   • Edge: Ctrl+Shift+N (Windows) or Cmd+Shift+N (Mac)\\n\\n' +");
        out.println("                '2. Navigate to: ' + url + '\\n\\n' +");
        out.println("                '3. Login with different credentials\\n\\n' +");
        out.println("                'Click OK to copy the URL to clipboard.';");
        out.println("            ");
        out.println("            if (confirm(message)) {");
        out.println("                // Copy URL to clipboard");
        out.println("                navigator.clipboard.writeText(url).then(function() {");
        out.println("                    alert('URL copied to clipboard!\\n\\nNow open an Incognito window and paste the URL.');");
        out.println("                }).catch(function(err) {");
        out.println("                    alert('Could not copy URL. Please manually copy:\\n\\n' + url);");
        out.println("                });");
        out.println("            }");
        out.println("        }");
        out.println("    </script>");
        out.println("</body>");
        out.println("</html>");
    }

    /**
     * Serves the raw OpenAPI specification in YAML format for Swagger UI consumption.
     */
    private void serveRawOpenAPISpec(HttpServletResponse response) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("api/openapi.yaml")) {
            if (is == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"OpenAPI specification not found\"}");
                return;
            }

            // Serve as YAML (Swagger UI can consume YAML directly)
            response.setContentType("application/x-yaml; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");

            // Copy YAML content to response
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                response.getOutputStream().write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Failed to load OpenAPI specification: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Serves the OpenAPI specification from openapi.yaml file.
     * Parses and displays all available API endpoints with their methods, descriptions, and parameters.
     */
    private void serveOpenAPISpec(HttpServletResponse response) throws IOException {
        // Try to load and serve the openapi.yaml file
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("api/openapi.yaml")) {
            if (is == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"OpenAPI specification not found\"}");
                return;
            }

            // Read the YAML content
            String yamlContent = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            // Parse and extract endpoints information
            response.setContentType("text/html; charset=UTF-8");
            PrintWriter out = response.getWriter();

            out.println("<!DOCTYPE html>");
            out.println("<html lang='en'>");
            out.println("<head>");
            out.println("    <meta charset='UTF-8'>");
            out.println("    <meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            out.println("    <title>Smart Store API - Endpoints</title>");
            out.println("    <style>");
            out.println("        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; margin: 0; padding: 20px; background: #f5f7fa; }");
            out.println("        .container { max-width: 1200px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }");
            out.println("        h1 { color: #2c3e50; border-bottom: 3px solid #3498db; padding-bottom: 10px; }");
            out.println("        h2 { color: #34495e; margin-top: 30px; }");
            out.println("        .endpoint { margin: 15px 0; padding: 15px; background: #f8f9fa; border-left: 4px solid #3498db; border-radius: 4px; }");
            out.println("        .method { display: inline-block; padding: 4px 12px; border-radius: 4px; font-weight: bold; font-size: 12px; margin-right: 10px; }");
            out.println("        .get { background: #61affe; color: white; }");
            out.println("        .post { background: #49cc90; color: white; }");
            out.println("        .put { background: #fca130; color: white; }");
            out.println("        .delete { background: #f93e3e; color: white; }");
            out.println("        .path { font-family: 'Courier New', monospace; font-size: 14px; color: #2c3e50; font-weight: bold; }");
            out.println("        .description { margin-top: 8px; color: #7f8c8d; font-size: 14px; }");
            out.println("        .summary { margin-top: 5px; color: #34495e; font-weight: 500; }");
            out.println("        .tag-section { margin-bottom: 30px; }");
            out.println("        .tag-header { background: #3498db; color: white; padding: 10px 15px; border-radius: 4px; margin-top: 20px; }");
            out.println("        .info-box { background: #e8f4f8; padding: 15px; border-radius: 4px; margin-bottom: 20px; border-left: 4px solid #3498db; }");
            out.println("        .button { display: inline-block; padding: 10px 20px; background: #3498db; color: white; text-decoration: none; border-radius: 4px; margin-top: 10px; }");
            out.println("        .button:hover { background: #2980b9; }");
            out.println("        code { background: #f4f4f4; padding: 2px 6px; border-radius: 3px; font-family: 'Courier New', monospace; }");
            out.println("    </style>");
            out.println("</head>");
            out.println("<body>");
            out.println("    <div class='container'>");
            out.println("        <h1>Smart Store API - Endpoints</h1>");

            // Parse API info
            String title = extractValue(yamlContent, "title:");
            String version = extractValue(yamlContent, "version:");
            String description = extractMultilineValue(yamlContent, "description:");

            out.println("        <div class='info-box'>");
            out.println("            <h3>" + title + "</h3>");
            out.println("            <p><strong>Version:</strong> " + version + "</p>");
            out.println("            <p><strong>Base URL:</strong> <code>http://localhost:8080/api/v1</code></p>");
            out.println("        </div>");

            out.println("        <a href='/swagger-ui/' class='button'>View Interactive API Documentation (Swagger UI)</a>");

            out.println("        <h2>Available Endpoints</h2>");

            // Parse and display endpoints by extracting from YAML
            displayEndpointsFromYaml(out, yamlContent);

            out.println("    </div>");
            out.println("</body>");
            out.println("</html>");

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Failed to parse OpenAPI specification: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Extract a simple value from YAML content
     */
    private String extractValue(String yaml, String key) {
        int keyIndex = yaml.indexOf(key);
        if (keyIndex == -1) return "";

        int lineStart = keyIndex + key.length();
        int lineEnd = yaml.indexOf('\n', lineStart);
        if (lineEnd == -1) lineEnd = yaml.length();

        return yaml.substring(lineStart, lineEnd).trim();
    }

    /**
     * Extract multiline value from YAML (for description fields with |)
     */
    private String extractMultilineValue(String yaml, String key) {
        int keyIndex = yaml.indexOf(key);
        if (keyIndex == -1) return "";

        // Skip the | character if present
        int lineStart = keyIndex + key.length();
        if (yaml.charAt(lineStart) == '|') {
            lineStart++;
        }

        // Get first line only for simplicity
        int lineEnd = yaml.indexOf('\n', lineStart);
        if (lineEnd == -1) lineEnd = yaml.length();

        return yaml.substring(lineStart, lineEnd).trim();
    }

    /**
     * Parse and display endpoints from YAML content
     */
    private void displayEndpointsFromYaml(PrintWriter out, String yaml) {
        // Find the paths section
        int pathsIndex = yaml.indexOf("paths:");
        if (pathsIndex == -1) {
            out.println("<p>No paths found in OpenAPI specification</p>");
            return;
        }

        // Extract paths section (from "paths:" to the next major section "components:")
        int componentsIndex = yaml.indexOf("components:", pathsIndex);
        if (componentsIndex == -1) componentsIndex = yaml.length();

        String pathsSection = yaml.substring(pathsIndex, componentsIndex);

        // Parse each path (lines starting with "  /" at the beginning)
        String[] lines = pathsSection.split("\n");
        String currentPath = null;
        String currentMethod = null;
        String currentSummary = null;
        String currentDescription = null;
        String currentTag = null;

        for (String line : lines) {
            // Check if this is a path definition (starts with "  /")
            if (line.matches("^  /[^:]*:$")) {
                // New path found
                currentPath = line.trim().replace(":", "");
            }
            // Check if this is an HTTP method
            else if (currentPath != null && line.matches("^    (get|post|put|delete|patch):$")) {
                // Save previous endpoint if exists
                if (currentMethod != null) {
                    printEndpoint(out, currentPath, currentMethod, currentSummary, currentDescription, currentTag);
                }

                currentMethod = line.trim().replace(":", "");
                currentSummary = null;
                currentDescription = null;
                currentTag = null;
            }
            // Extract tags
            else if (line.contains("tags:") && line.contains("[")) {
                int start = line.indexOf("[");
                int end = line.indexOf("]");
                if (start != -1 && end != -1) {
                    currentTag = line.substring(start + 1, end).trim().replace("\"", "").replace("'", "");
                }
            }
            // Extract summary
            else if (line.contains("summary:")) {
                currentSummary = extractValue(line, "summary:");
            }
            // Extract description
            else if (line.contains("description:")) {
                currentDescription = extractValue(line, "description:");
            }
        }

        // Print last endpoint
        if (currentMethod != null && currentPath != null) {
            printEndpoint(out, currentPath, currentMethod, currentSummary, currentDescription, currentTag);
        }
    }

    /**
     * Print a single endpoint in HTML format
     */
    private void printEndpoint(PrintWriter out, String path, String method, String summary, String description, String tag) {
        out.println("        <div class='endpoint'>");
        out.println("            <div>");
        out.println("                <span class='method " + method.toLowerCase() + "'>" + method.toUpperCase() + "</span>");
        out.println("                <span class='path'>/api/v1" + path + "</span>");
        if (tag != null && !tag.isEmpty()) {
            out.println("                <span style='float: right; color: #95a5a6; font-size: 12px;'>[" + tag + "]</span>");
        }
        out.println("            </div>");
        if (summary != null && !summary.isEmpty()) {
            out.println("            <div class='summary'>" + summary + "</div>");
        }
        if (description != null && !description.isEmpty()) {
            out.println("            <div class='description'>" + description + "</div>");
        }
        out.println("        </div>");
    }
}
