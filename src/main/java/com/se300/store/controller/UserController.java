package com.se300.store.controller;

import com.se300.store.model.User;
import com.se300.store.service.AuthenticationService;
import com.se300.store.servlet.BaseServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

/**
 * REST API controller for User operations
 * Implements full CRUD operations
 *
 * @author Sergey L. Sundukovskiy, Ph.D.
 * @version 1.0
 */
public class UserController extends BaseServlet {

    //TODO: Implement REST CRUD API for User operations

    private final AuthenticationService authenticationService;

    public UserController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * Handle GET requests
     * - GET /api/v1/users (no parameters) - Get all users
     * - GET /api/v1/users/{email} - Get user by email
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try{
            // Extract email 
            String email = extractResourceId(request);
            // If the email isn't provided return all the users
            if(email == null){
                Collection<User> users = authenticationService.getAllUsers();
                sendJsonResponse(response, users);
            // If the email is provided return the specific user
            }else{
                User user = authenticationService.getUserByEmail(email);
                if(user == null){
                    sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "User not found");
                }else{
                    sendJsonResponse(response, user);
                }
            }
        // Catches any errors that may come about
        }catch(Exception e){
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    /**
     * Handle POST requests - Register new user
     * POST /api/v1/users?email=xxx&password=xxx&name=xxx
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try{
            // Get parameters
            String email = request.getParameter("email");
            String password = request.getParameter("password");
            String name = request.getParameter("name");
            // Validate parameters are provided
            if(email == null || password == null || name == null){
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Missing required parameters: email, password and name are required");
                return;
            }
            // Call service to register the user
            User regUser = authenticationService.registerUser(email, password, name);
            // Send response that the user has been registered successfully
            sendJsonResponse(response, regUser, HttpServletResponse.SC_CREATED); // Expecting 201 Created
        // Catches all exceptions
        }catch(Exception e){
            sendErrorResponse(response, HttpServletResponse.SC_CONFLICT, e.getMessage());
        }
    }

    /**
     * Handle PUT requests - Update user information
     * PUT /api/v1/users/{email}?password=xxx&name=xxx
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try{
            // Extract email from URL
            String email = extractResourceId(request);
            // Validate email is provided
            if(email == null){
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Email is required in URL path");
                return;
            }
            // Get parameters from request
            String password = request.getParameter("password");
            String name = request.getParameter("name");
            // Call service to update the user
            User updatedUser = authenticationService.updateUser(email, password, name);
            // Send response that the user has been updated successfully
            sendJsonResponse(response, updatedUser); // Expecting 200 OK
        // Catches any errors that may come about
        }catch(Exception e){
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        }
    }

    /**
     * Handle DELETE requests - Delete user
     * DELETE /api/v1/users/{email}
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try{
            // Extract email from URL
            String email = extractResourceId(request);
            // Validate email was provided
            if(email == null){
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Email is required in URL path");
                return;
            }
            // Call service to delete the user
            authenticationService.deleteUser(email);
            // Send response that user was deleted successfully
            response.setStatus(HttpServletResponse.SC_NO_CONTENT); // Expecting 204 No Content
        }catch(Exception e){
            // Catches any errors that may come about 
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        }
    }
}