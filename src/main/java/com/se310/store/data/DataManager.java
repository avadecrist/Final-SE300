package com.se310.store.data;

import com.se310.store.config.ConfigLoader;
import com.se310.store.model.Store;
import com.se310.store.model.User;
import com.se310.store.model.UserRole;
import com.se310.store.security.PasswordEncryption;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DataManager - Singleton class for managing H2 database connections.
 * Provides centralized database management for the application.
 *
 * This implementation uses H2 embedded database for persistent data storage.
 * The database file is stored in the user's home directory under .smartstore/
 *
 * All database-specific logic (SQLException handling, ResultSet mapping) is encapsulated here.
 * Repositories are database-agnostic and work only with domain objects.
 *
 * @author  Sergey L. Sundukovskiy
 * @version 1.0
 * @since   2025-11-11
 */
public class DataManager {

    //TODO: Implement Singleton pattern with double-checked locking

    private static volatile DataManager instance;
    private Connection connection;

    // H2 database configuration - loaded from application.properties
    private static final String DB_DRIVER = ConfigLoader.getDbDriver();
    private static final String DB_URL = ConfigLoader.getDbUrl();
    private static final String DB_USER = ConfigLoader.getDbUser();
    private static final String DB_PASSWORD = ConfigLoader.getDbPassword();

    // Private constructor to prevent instantiation
    private DataManager() {
        initializeDatabase();
    }

    //TODO: Thread-safe singleton implementation with double-checked locking
    public static DataManager getInstance() { 
        DataManager result = instance;
        if (result == null) {
            synchronized (DataManager.class) {
                result = instance;
                if (result == null) {
                    result = new DataManager();
                    instance = result;
                }
            }
        }
        return result;
    }

    /**
     * Initialize H2 database connection and create tables if they don't exist
     */
    private void initializeDatabase() {
        try {
            // Load H2 driver
            Class.forName(DB_DRIVER);

            // Establish connection
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            // Create tables
            createTables();

            System.out.println("DataManager: H2 Database initialized successfully");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("H2 Driver not found", e);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize H2 database", e);
        }
    }

    /**
     * Create database tables if they don't exist
     */
    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Users table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                "email VARCHAR(255) PRIMARY KEY," +
                "password VARCHAR(255) NOT NULL," +
                "name VARCHAR(255) NOT NULL," +
                "role VARCHAR(50) DEFAULT 'USER'" +
                ")");

            // Stores table
            stmt.execute("CREATE TABLE IF NOT EXISTS stores (" +
                "id VARCHAR(255) PRIMARY KEY," +
                "address VARCHAR(500)," +
                "description VARCHAR(1000)" +
                ")");

            // Products table
            stmt.execute("CREATE TABLE IF NOT EXISTS products (" +
                "id VARCHAR(255) PRIMARY KEY," +
                "name VARCHAR(255) NOT NULL," +
                "description VARCHAR(1000)," +
                "size VARCHAR(100)," +
                "category VARCHAR(100)," +
                "price DOUBLE," +
                "temperature VARCHAR(50)" +
                ")");

            // Customers table
            stmt.execute("CREATE TABLE IF NOT EXISTS customers (" +
                "id VARCHAR(255) PRIMARY KEY," +
                "first_name VARCHAR(255)," +
                "last_name VARCHAR(255)," +
                "customer_type VARCHAR(50)," +
                "email VARCHAR(255)," +
                "account_address VARCHAR(500)," +
                "store_id VARCHAR(255)," +
                "aisle_number VARCHAR(50)," +
                "last_seen TIMESTAMP" +
                ")");

            // Baskets table
            stmt.execute("CREATE TABLE IF NOT EXISTS baskets (" +
                "id VARCHAR(255) PRIMARY KEY," +
                "customer_id VARCHAR(255)," +
                "store_id VARCHAR(255)" +
                ")");

            // Basket items table
            stmt.execute("CREATE TABLE IF NOT EXISTS basket_items (" +
                "basket_id VARCHAR(255)," +
                "product_id VARCHAR(255)," +
                "count INT," +
                "PRIMARY KEY (basket_id, product_id)" +
                ")");

            // Inventory table
            stmt.execute("CREATE TABLE IF NOT EXISTS inventory (" +
                "id VARCHAR(255) PRIMARY KEY," +
                "store_id VARCHAR(255)," +
                "aisle_number VARCHAR(50)," +
                "shelf_id VARCHAR(50)," +
                "capacity INT," +
                "count INT," +
                "product_id VARCHAR(255)," +
                "inventory_type VARCHAR(50)" +
                ")");

            // Devices table
            stmt.execute("CREATE TABLE IF NOT EXISTS devices (" +
                "id VARCHAR(255) PRIMARY KEY," +
                "name VARCHAR(255)," +
                "device_type VARCHAR(100)," +
                "store_id VARCHAR(255)," +
                "aisle_number VARCHAR(50)" +
                ")");

            // Insert default users if not exists
            insertDefaultUsers();
        }
    }

    /**
     * Insert default test users from application.properties.
     * Passwords are encrypted before storage for security.
     */
    private void insertDefaultUsers() throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM users WHERE email = ?";
        String insertSql = "INSERT INTO users (email, password, name, role) VALUES (?, ?, ?, ?)";

        // Check and insert admin user - credentials loaded from properties, password encrypted
        try (PreparedStatement checkStmt = getConnection().prepareStatement(checkSql)) {
            checkStmt.setString(1, ConfigLoader.getAdminEmail());
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                try (PreparedStatement insertStmt = getConnection().prepareStatement(insertSql)) {
                    insertStmt.setString(1, ConfigLoader.getAdminEmail());
                    insertStmt.setString(2, PasswordEncryption.encrypt(ConfigLoader.getAdminPassword()));
                    insertStmt.setString(3, ConfigLoader.getAdminName());
                    insertStmt.setString(4, ConfigLoader.getAdminRole());
                    insertStmt.executeUpdate();
                }
            }
        }

        // Check and insert regular user - credentials loaded from properties, password encrypted
        try (PreparedStatement checkStmt = getConnection().prepareStatement(checkSql)) {
            checkStmt.setString(1, ConfigLoader.getUserEmail());
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                try (PreparedStatement insertStmt = getConnection().prepareStatement(insertSql)) {
                    insertStmt.setString(1, ConfigLoader.getUserEmail());
                    insertStmt.setString(2, PasswordEncryption.encrypt(ConfigLoader.getUserPassword()));
                    insertStmt.setString(3, ConfigLoader.getUserName());
                    insertStmt.setString(4, ConfigLoader.getUserRole());
                    insertStmt.executeUpdate();
                }
            }
        }
    }

    /**
     * Get database connection
     */
    public Connection getConnection() {
        try {
            // Check if connection is still valid and test it
            if (connection == null || connection.isClosed() || !connection.isValid(1)) {
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            }
            return connection;
        } catch (SQLException e) {
            // If we get an error, try to reconnect
            try {
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                return connection;
            } catch (SQLException ex) {
                throw new RuntimeException("Failed to get database connection", ex);
            }
        }
    }

    /**
     * Close database connection
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("DataManager: Database connection closed");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }

    /**
     * Execute a query and return ResultSet
     */
    public ResultSet executeQuery(String sql, Object... params) throws SQLException {
        PreparedStatement stmt = getConnection().prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
        return stmt.executeQuery();
    }

    /**
     * Execute an update/insert/delete statement
     */
    public int executeUpdate(String sql, Object... params) throws SQLException {
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            return stmt.executeUpdate();
        }
    }

    /**
     * Clear all data from all tables (for testing)
     */
    public void clearAllTables() throws SQLException {
        try (Statement stmt = getConnection().createStatement()) {
            // Delete in order to respect foreign key constraints
            stmt.execute("DELETE FROM basket_items");
            stmt.execute("DELETE FROM baskets");
            stmt.execute("DELETE FROM devices");
            stmt.execute("DELETE FROM inventory");
            stmt.execute("DELETE FROM customers");
            stmt.execute("DELETE FROM products");
            stmt.execute("DELETE FROM stores");
            stmt.execute("DELETE FROM users");
            // Re-insert default users
            insertDefaultUsers();
        }
    }

    // ==================== USER OPERATIONS ====================

    /**
     * Find user by email
     */
    public ResultSet findUserByEmail(String email) throws SQLException {
        String sql = "SELECT email, password, name, role FROM users WHERE email = ?";
        return executeQuery(sql, email);
    }

    /**
     * Save or update a user
     */
    public void saveUser(String email, String password, String name, String role) throws SQLException {
        // Try update first
        String updateSql = "UPDATE users SET password = ?, name = ?, role = ? WHERE email = ?";
        int rowsAffected = executeUpdate(updateSql, password, name, role, email);

        // If no rows updated, insert new user
        if (rowsAffected == 0) {
            String insertSql = "INSERT INTO users (email, password, name, role) VALUES (?, ?, ?, ?)";
            executeUpdate(insertSql, email, password, name, role);
        }
    }

    /**
     * Check if user exists by email
     */
    public boolean userExists(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (ResultSet rs = executeQuery(sql, email)) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    /**
     * Delete user by email
     */
    public boolean deleteUser(String email) throws SQLException {
        String sql = "DELETE FROM users WHERE email = ?";
        return executeUpdate(sql, email) > 0;
    }

    /**
     * Get all users
     */
    public ResultSet findAllUsers() throws SQLException {
        String sql = "SELECT email, password, name, role FROM users";
        PreparedStatement stmt = getConnection().prepareStatement(sql);
        return stmt.executeQuery();
    }

    // ==================== USER OPERATIONS (DOMAIN OBJECT BASED) ====================

    /**
     * Find user by email - Returns domain object
     * All SQLException handling is done here
     */
    public Optional<User> getUserByEmail(String email) {
        String sql = "SELECT email, password, name, role FROM users WHERE email = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding user by email: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Get all users - Returns list of domain objects
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT email, password, name, role FROM users";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all users: " + e.getMessage());
        }
        return users;
    }

    /**
     * Save user - Accepts domain object
     */
    public User persistUser(User user) {
        try {
            saveUser(user.getEmail(), user.getPassword(), user.getName(), user.getRole().name());
            return user;
        } catch (SQLException e) {
            System.err.println("Error saving user: " + e.getMessage());
            throw new RuntimeException("Failed to save user", e);
        }
    }

    /**
     * Delete user by email - Returns boolean
     */
    public boolean removeUser(String email) {
        try {
            return deleteUser(email);
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if user exists - Handles SQLException internally
     */
    public boolean doesUserExist(String email) {
        try {
            return userExists(email);
        } catch (SQLException e) {
            System.err.println("Error checking if user exists: " + e.getMessage());
            return false;
        }
    }

    /**
     * Helper method to map ResultSet to User object
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        return new User(
            rs.getString("email"),
            rs.getString("password"),
            rs.getString("name"),
            UserRole.valueOf(rs.getString("role"))
        );
    }

    // ==================== STORE OPERATIONS ====================

    /**
     * Find store by ID
     */
    public ResultSet findStoreById(String storeId) throws SQLException {
        String sql = "SELECT id, address, description FROM stores WHERE id = ?";
        return executeQuery(sql, storeId);
    }

    /**
     * Save or update a store
     */
    public void saveStore(String id, String address, String description) throws SQLException {
        // Try update first
        String updateSql = "UPDATE stores SET address = ?, description = ? WHERE id = ?";
        int rowsAffected = executeUpdate(updateSql, address, description, id);

        // If no rows updated, insert new store
        if (rowsAffected == 0) {
            String insertSql = "INSERT INTO stores (id, address, description) VALUES (?, ?, ?)";
            executeUpdate(insertSql, id, address, description);
        }
    }

    /**
     * Check if store exists by ID
     */
    public boolean storeExists(String storeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM stores WHERE id = ?";
        try (ResultSet rs = executeQuery(sql, storeId)) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    /**
     * Delete store by ID
     */
    public boolean deleteStore(String storeId) throws SQLException {
        String sql = "DELETE FROM stores WHERE id = ?";
        return executeUpdate(sql, storeId) > 0;
    }

    /**
     * Get all stores
     */
    public ResultSet findAllStores() throws SQLException {
        String sql = "SELECT id, address, description FROM stores";
        PreparedStatement stmt = getConnection().prepareStatement(sql);
        return stmt.executeQuery();
    }

    // ==================== STORE OPERATIONS (DOMAIN OBJECT BASED) ====================

    /**
     * Find store by ID - Returns domain object
     * All SQLException handling is done here
     */
    public Optional<Store> getStoreById(String storeId) {
        String sql = "SELECT id, address, description FROM stores WHERE id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, storeId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToStore(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding store by ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Get all stores - Returns list of domain objects
     */
    public List<Store> getAllStores() {
        List<Store> stores = new ArrayList<>();
        String sql = "SELECT id, address, description FROM stores";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                stores.add(mapResultSetToStore(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all stores: " + e.getMessage());
        }
        return stores;
    }

    /**
     * Save store - Accepts domain object
     */
    public Store persistStore(Store store) {
        try {
            saveStore(store.getId(), store.getAddress(), store.getDescription());
            return store;
        } catch (SQLException e) {
            System.err.println("Error saving store: " + e.getMessage());
            throw new RuntimeException("Failed to save store", e);
        }
    }

    /**
     * Delete store by ID - Returns boolean
     */
    public boolean removeStore(String storeId) {
        try {
            return deleteStore(storeId);
        } catch (SQLException e) {
            System.err.println("Error deleting store: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if store exists - Handles SQLException internally
     */
    public boolean doesStoreExist(String storeId) {
        try {
            return storeExists(storeId);
        } catch (SQLException e) {
            System.err.println("Error checking if store exists: " + e.getMessage());
            return false;
        }
    }

    /**
     * Helper method to map ResultSet to Store object
     */
    private Store mapResultSetToStore(ResultSet rs) throws SQLException {
        return new Store(
            rs.getString("id"),
            rs.getString("address"),
            rs.getString("description")
        );
    }

    // ==================== PRODUCT OPERATIONS ====================

    public ResultSet findProductById(String productId) throws SQLException {
        String sql = "SELECT id, name, description, size, category, price, temperature FROM products WHERE id = ?";
        return executeQuery(sql, productId);
    }

    public void saveProduct(String id, String name, String description, String size, String category, double price, String temperature) throws SQLException {
        String updateSql = "UPDATE products SET name = ?, description = ?, size = ?, category = ?, price = ?, temperature = ? WHERE id = ?";
        int rowsAffected = executeUpdate(updateSql, name, description, size, category, price, temperature, id);
        if (rowsAffected == 0) {
            String insertSql = "INSERT INTO products (id, name, description, size, category, price, temperature) VALUES (?, ?, ?, ?, ?, ?, ?)";
            executeUpdate(insertSql, id, name, description, size, category, price, temperature);
        }
    }

    public boolean productExists(String productId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM products WHERE id = ?";
        try (ResultSet rs = executeQuery(sql, productId)) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    public boolean deleteProduct(String productId) throws SQLException {
        String sql = "DELETE FROM products WHERE id = ?";
        return executeUpdate(sql, productId) > 0;
    }

    public ResultSet findAllProducts() throws SQLException {
        String sql = "SELECT id, name, description, size, category, price, temperature FROM products";
        PreparedStatement stmt = getConnection().prepareStatement(sql);
        return stmt.executeQuery();
    }

    // ==================== CUSTOMER OPERATIONS ====================

    public ResultSet findCustomerById(String customerId) throws SQLException {
        String sql = "SELECT id, first_name, last_name, customer_type, email, account_address, store_id, aisle_number, last_seen FROM customers WHERE id = ?";
        return executeQuery(sql, customerId);
    }

    public void saveCustomer(String id, String firstName, String lastName, String customerType, String email, String address, String storeId, String aisleNumber, Timestamp lastSeen) throws SQLException {
        String updateSql = "UPDATE customers SET first_name = ?, last_name = ?, customer_type = ?, email = ?, account_address = ?, store_id = ?, aisle_number = ?, last_seen = ? WHERE id = ?";
        int rowsAffected = executeUpdate(updateSql, firstName, lastName, customerType, email, address, storeId, aisleNumber, lastSeen, id);
        if (rowsAffected == 0) {
            String insertSql = "INSERT INTO customers (id, first_name, last_name, customer_type, email, account_address, store_id, aisle_number, last_seen) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            executeUpdate(insertSql, id, firstName, lastName, customerType, email, address, storeId, aisleNumber, lastSeen);
        }
    }

    public boolean customerExists(String customerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM customers WHERE id = ?";
        try (ResultSet rs = executeQuery(sql, customerId)) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    public boolean deleteCustomer(String customerId) throws SQLException {
        String sql = "DELETE FROM customers WHERE id = ?";
        return executeUpdate(sql, customerId) > 0;
    }

    public ResultSet findAllCustomers() throws SQLException {
        String sql = "SELECT id, first_name, last_name, customer_type, email, account_address, store_id, aisle_number, last_seen FROM customers";
        PreparedStatement stmt = getConnection().prepareStatement(sql);
        return stmt.executeQuery();
    }

    public ResultSet findCustomersByStoreId(String storeId) throws SQLException {
        String sql = "SELECT id, first_name, last_name, customer_type, email, account_address, store_id, aisle_number, last_seen FROM customers WHERE store_id = ?";
        return executeQuery(sql, storeId);
    }

    // ==================== BASKET OPERATIONS ====================

    public ResultSet findBasketById(String basketId) throws SQLException {
        String sql = "SELECT id, customer_id, store_id FROM baskets WHERE id = ?";
        return executeQuery(sql, basketId);
    }

    public void saveBasket(String id, String customerId, String storeId) throws SQLException {
        String updateSql = "UPDATE baskets SET customer_id = ?, store_id = ? WHERE id = ?";
        int rowsAffected = executeUpdate(updateSql, customerId, storeId, id);
        if (rowsAffected == 0) {
            String insertSql = "INSERT INTO baskets (id, customer_id, store_id) VALUES (?, ?, ?)";
            executeUpdate(insertSql, id, customerId, storeId);
        }
    }

    public boolean basketExists(String basketId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM baskets WHERE id = ?";
        try (ResultSet rs = executeQuery(sql, basketId)) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    public boolean deleteBasket(String basketId) throws SQLException {
        // First delete basket items
        executeUpdate("DELETE FROM basket_items WHERE basket_id = ?", basketId);
        // Then delete basket
        String sql = "DELETE FROM baskets WHERE id = ?";
        return executeUpdate(sql, basketId) > 0;
    }

    public ResultSet findAllBaskets() throws SQLException {
        String sql = "SELECT id, customer_id, store_id FROM baskets";
        PreparedStatement stmt = getConnection().prepareStatement(sql);
        return stmt.executeQuery();
    }

    public void addBasketItem(String basketId, String productId, int count) throws SQLException {
        String sql = "INSERT INTO basket_items (basket_id, product_id, count) VALUES (?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE count = count + ?";
        executeUpdate(sql, basketId, productId, count, count);
    }

    public void updateBasketItemCount(String basketId, String productId, int count) throws SQLException {
        if (count <= 0) {
            String sql = "DELETE FROM basket_items WHERE basket_id = ? AND product_id = ?";
            executeUpdate(sql, basketId, productId);
        } else {
            String sql = "UPDATE basket_items SET count = ? WHERE basket_id = ? AND product_id = ?";
            executeUpdate(sql, count, basketId, productId);
        }
    }

    public ResultSet findBasketItems(String basketId) throws SQLException {
        String sql = "SELECT product_id, count FROM basket_items WHERE basket_id = ?";
        return executeQuery(sql, basketId);
    }

    public void clearBasketItems(String basketId) throws SQLException {
        String sql = "DELETE FROM basket_items WHERE basket_id = ?";
        executeUpdate(sql, basketId);
    }

    // ==================== INVENTORY OPERATIONS ====================

    public ResultSet findInventoryById(String inventoryId) throws SQLException {
        String sql = "SELECT id, store_id, aisle_number, shelf_id, capacity, count, product_id, inventory_type FROM inventory WHERE id = ?";
        return executeQuery(sql, inventoryId);
    }

    public void saveInventory(String id, String storeId, String aisleNumber, String shelfId, int capacity, int count, String productId, String inventoryType) throws SQLException {
        String updateSql = "UPDATE inventory SET store_id = ?, aisle_number = ?, shelf_id = ?, capacity = ?, count = ?, product_id = ?, inventory_type = ? WHERE id = ?";
        int rowsAffected = executeUpdate(updateSql, storeId, aisleNumber, shelfId, capacity, count, productId, inventoryType, id);
        if (rowsAffected == 0) {
            String insertSql = "INSERT INTO inventory (id, store_id, aisle_number, shelf_id, capacity, count, product_id, inventory_type) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            executeUpdate(insertSql, id, storeId, aisleNumber, shelfId, capacity, count, productId, inventoryType);
        }
    }

    public boolean inventoryExists(String inventoryId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM inventory WHERE id = ?";
        try (ResultSet rs = executeQuery(sql, inventoryId)) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    public boolean deleteInventory(String inventoryId) throws SQLException {
        String sql = "DELETE FROM inventory WHERE id = ?";
        return executeUpdate(sql, inventoryId) > 0;
    }

    public ResultSet findAllInventory() throws SQLException {
        String sql = "SELECT id, store_id, aisle_number, shelf_id, capacity, count, product_id, inventory_type FROM inventory";
        PreparedStatement stmt = getConnection().prepareStatement(sql);
        return stmt.executeQuery();
    }

    // ==================== DEVICE OPERATIONS ====================

    public ResultSet findDeviceById(String deviceId) throws SQLException {
        String sql = "SELECT id, name, device_type, store_id, aisle_number FROM devices WHERE id = ?";
        return executeQuery(sql, deviceId);
    }

    public void saveDevice(String id, String name, String deviceType, String storeId, String aisleNumber) throws SQLException {
        String updateSql = "UPDATE devices SET name = ?, device_type = ?, store_id = ?, aisle_number = ? WHERE id = ?";
        int rowsAffected = executeUpdate(updateSql, name, deviceType, storeId, aisleNumber, id);
        if (rowsAffected == 0) {
            String insertSql = "INSERT INTO devices (id, name, device_type, store_id, aisle_number) VALUES (?, ?, ?, ?, ?)";
            executeUpdate(insertSql, id, name, deviceType, storeId, aisleNumber);
        }
    }

    public boolean deviceExists(String deviceId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM devices WHERE id = ?";
        try (ResultSet rs = executeQuery(sql, deviceId)) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    public boolean deleteDevice(String deviceId) throws SQLException {
        String sql = "DELETE FROM devices WHERE id = ?";
        return executeUpdate(sql, deviceId) > 0;
    }

    public ResultSet findAllDevices() throws SQLException {
        String sql = "SELECT id, name, device_type, store_id, aisle_number FROM devices";
        PreparedStatement stmt = getConnection().prepareStatement(sql);
        return stmt.executeQuery();
    }
}
