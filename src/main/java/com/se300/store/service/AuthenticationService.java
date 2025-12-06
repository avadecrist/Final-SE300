package com.se300.store.service;

import com.se300.store.model.User;
import com.se300.store.repository.UserRepository;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.Optional;

/**
 * This class is responsible for authenticating users and managing user data.
 *
 * @author  Sergey L. Sundukovskiy
 * @version 1.0
 * @since 2025-11-09
 **/
public class AuthenticationService {

    private final UserRepository userRepository;

    public AuthenticationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Register a new user
     */
    public User registerUser(String email, String password, String name) throws IllegalArgumentException {
        // Check if user already exists
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("User with email " + email + " already exists");
        }
        User user = new User(email, password, name);
        userRepository.save(user);
        return user;
    }

    /**
     * Check if user exists
     */
    public boolean userExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Get all users
     */
    public Collection<User> getAllUsers() {
        return userRepository.findAll().values();
    }

    /**
     * Get user by email
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    /**
     * Update user information
     */
    public User updateUser(String email, String password, String name) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return null;
        }

        User user = userOpt.get();
        if (password != null) {
            user.setPassword(password);
        }
        if (name != null) {
            user.setName(name);
        }

        userRepository.save(user);
        return user;
    }

    /**
     * Delete user by email
     */
    public boolean deleteUser(String email) {
        if (!userRepository.existsByEmail(email)) {
            return false;
        }
        userRepository.delete(email);
        return true;
    }
}
