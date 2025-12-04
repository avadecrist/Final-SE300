package com.se310.store.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * ConfigLoader - Utility class for loading application configuration from properties file.
 *
 * This class demonstrates the separation of configuration from code,
 * following the 12-Factor App principles for configuration management.
 *
 * Benefits:
 * - Externalized configuration: Easy to change without recompiling
 * - Environment-specific configs: Different properties for dev/test/prod
 * - Security: Credentials not hardcoded in source code
 * - Maintainability: Single source of truth for configuration
 *
 * @author  Sergey L. Sundukovskiy
 * @version 1.0
 * @since   2025-11-13
 */
public class ConfigLoader {

    private static final String CONFIG_FILE = "application.properties";
    private static final Properties properties;

    // Load properties file on class initialization
    static {
        properties = new Properties();
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                System.err.println("Unable to find " + CONFIG_FILE);
                throw new RuntimeException("Configuration file not found: " + CONFIG_FILE);
            }
            properties.load(input);
            System.out.println("ConfigLoader: Successfully loaded " + CONFIG_FILE);
        } catch (IOException ex) {
            System.err.println("Error loading configuration file: " + ex.getMessage());
            throw new RuntimeException("Failed to load configuration", ex);
        }
    }

    // Private constructor to prevent instantiation
    private ConfigLoader() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Get property value by key
     *
     * @param key The property key
     * @return The property value, or null if not found
     */
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * Get property value with default fallback
     *
     * @param key The property key
     * @param defaultValue The default value if key not found
     * @return The property value, or defaultValue if not found
     */
    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    // ==================== SERVER CONFIGURATION ====================

    public static int getServerPort() {
        String portString = getProperty("server.port", "8080");
        try {
            return Integer.parseInt(portString);
        } catch (NumberFormatException e) {
            System.err.println("Invalid server.port value: " + portString + ", using default 8080");
            return 8080;
        }
    }

    public static String getApiBaseUrl() {
        return getProperty("api.base.url", "http://localhost:8080/api/v1");
    }

    // ==================== SECURITY CONFIGURATION ====================

    public static String getEncryptionKey() {
        return getProperty("security.encryption.key", "DefaultInsecureKey123!");
    }

    // ==================== DATABASE CONFIGURATION ====================

    public static String getDbDriver() {
        return getProperty("db.driver", "org.h2.Driver");
    }

    public static String getDbUrl() {
        return getProperty("db.url", "jdbc:h2:~/smartstore/storedb;AUTO_SERVER=TRUE");
    }

    public static String getDbUser() {
        return getProperty("db.user", "sa");
    }

    public static String getDbPassword() {
        return getProperty("db.password", "");
    }

    // ==================== ADMIN USER CONFIGURATION ====================

    public static String getAdminEmail() {
        return getProperty("admin.email", "admin@store.com");
    }

    public static String getAdminPassword() {
        return getProperty("admin.password", "admin123");
    }

    public static String getAdminName() {
        return getProperty("admin.name", "Admin User");
    }

    public static String getAdminRole() {
        return getProperty("admin.role", "ADMIN");
    }

    // ==================== REGULAR USER CONFIGURATION ====================

    public static String getUserEmail() {
        return getProperty("user.email", "user@store.com");
    }

    public static String getUserPassword() {
        return getProperty("user.password", "user123");
    }

    public static String getUserName() {
        return getProperty("user.name", "Regular User");
    }

    public static String getUserRole() {
        return getProperty("user.role", "USER");
    }

    /**
     * Get all properties (for debugging)
     *
     * @return Properties object
     */
    public static Properties getAllProperties() {
        return (Properties) properties.clone();
    }
}