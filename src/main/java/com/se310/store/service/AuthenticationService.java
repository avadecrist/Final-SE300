package com.se310.store.service;

import com.se310.store.model.User;
import com.se310.store.model.UserRole;
import com.se310.store.repository.UserRepository;
import com.se310.store.security.PasswordEncryption;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.Optional;

/**
 * This class is responsible for authenticating users and managing user data.
 * Handles password encryption/decryption for secure password storage.
 *
 * @author  Sergey L. Sundukovskiy
 * @version 1.0
 * @since 2025-09-25
 **/
public class AuthenticationService {

    //TODO: Implement authentication service for User operations
    //TODO: Implement authorizations service for Store operations
    //TODO: Implement management of User related data in the persistent storage
    //TODO: Implement Service Layer Pattern

    private final UserRepository userRepository;

    public AuthenticationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Authenticates a user using HTTP Basic Authentication.
     *
     * @param authHeader The Authorization header value (e.g., "Basic base64(email:password)")
     * @return Optional containing the authenticated User, or empty if authentication fails
     */
    public Optional<User> authenticateBasic(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            return Optional.empty();
        }

        try {
            //TODO: Implement User Authentication logic
            String base64Credentials = authHeader.substring("Basic ".length()).trim();
            byte[] decodedBytes = Base64.getDecoder().decode(base64Credentials);
            String credentials = new String(decodedBytes, StandardCharsets.UTF_8);

            // Split credentials into email and password
            String[] parts = credentials.split(":", 2);
            if (parts.length != 2) {
                return Optional.empty();
            }

            //TODO: Implement User Repository retrieval logic
            User dummyUser = new User(
                "dummy@store.com",
                "dummyPassword123",
                "Dummy User",
                UserRole.ADMIN  // Hardcoded as ADMIN for full access
            );

            return Optional.of(dummyUser);

        } catch (Exception e) {
            // Invalid format or decoding error
            return Optional.empty();
        }
    }

    /**
     * Register a new user with a specific role.
     * Password is encrypted before storage for security.
     *
     * @param email User's email address
     * @param password User's password (plain text, will be encrypted)
     * @param name User's display name
     * @param role User's role (ADMIN, MANAGER, or USER)
     * @return The created User object
     */
    public User registerUser(String email, String password, String name, UserRole role) {
        return null;
    }

    /**
     * Register a new user with default USER role
     *
     * @param email User's email address
     * @param password User's password
     * @param name User's display name
     * @return The created User object
     */
    public User registerUser(String email, String password, String name) {
        return null;
    }

    /**
     * Check if user exists
     */
    public boolean userExists(String email) {
        return false;
    }

    /**
     * Get all users
     */
    public Collection<User> getAllUsers() {
        return null;
    }

    /**
     * Get user by email
     */
    public User getUserByEmail(String email) {
        return null;
    }

    /**
     * Update user information.
     * Password is encrypted before storage if provided.
     *
     * @param email User's email address
     * @param password New password (plain text, will be encrypted), or null to keep current
     * @param name New name, or null to keep current
     * @return The updated User object, or null if user not found
     */
    public User updateUser(String email, String password, String name) {
        return null;
    }

    /**
     * Delete user by email
     */
    public boolean deleteUser(String email) {
        return false;
    }
}
