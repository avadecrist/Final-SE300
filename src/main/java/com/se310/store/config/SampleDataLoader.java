package com.se310.store.config;

import com.se310.store.model.*;
import com.se310.store.service.AuthenticationService;
import com.se310.store.service.StoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SampleDataLoader - Comprehensive data loading utility for populating the Smart Store application.
 *
 * This class provides methods to load sample data for:
 * - Users (admin, staff, customers)
 * - Stores (multiple locations)
 * - Aisles (organized by location)
 * - Shelves (with temperature zones)
 * - Products (various categories)
 * - Inventory (stocked items)
 * - Customers (registered and guests)
 * - Baskets (shopping carts)
 * - Devices (IoT sensors and appliances)
 *
 * Usage:
 * - Application startup: Load production-like sample data
 * - Testing: Load minimal or comprehensive test data
 * - Demos: Load realistic demonstration data
 *
 * @author Sergey L. Sundukovskiy
 * @version 1.0
 */
public class SampleDataLoader {

    private static final Logger logger = LoggerFactory.getLogger(SampleDataLoader.class);

    private final StoreService storeService;
    private final AuthenticationService authenticationService;

    /**
     * Constructor
     */
    public SampleDataLoader(StoreService storeService, AuthenticationService authenticationService) {
        this.storeService = storeService;
        this.authenticationService = authenticationService;
    }

    /**
     * Load ALL sample data (comprehensive dataset)
     * Suitable for: Application startup, demos, full integration testing
     */
    public void loadAllSampleData() {
        logger.info("=".repeat(80));
        logger.info("Loading comprehensive sample data...");
        logger.info("=".repeat(80));

        try {
            // Load in dependency order
            loadUsers();
            loadStores();
            loadAisles();
            loadShelves();
            loadProducts();
            loadInventory();
            loadCustomers();
            loadBaskets();
            loadDevices();

            logger.info("=".repeat(80));
            logger.info("Sample data loaded successfully!");
            logger.info("=".repeat(80));
        } catch (Exception e) {
            logger.error("Error loading sample data: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to load sample data", e);
        }
    }

    /**
     * Load minimal sample data (basic dataset)
     * Suitable for: Unit testing, quick startup, minimal environment
     */
    public void loadMinimalSampleData() {
        logger.info("Loading minimal sample data...");

        try {
            // Load only essential data
            loadBasicUsers();
            loadBasicStore();
            loadBasicProducts();

            logger.info("Minimal sample data loaded successfully");
        } catch (Exception e) {
            logger.error("Error loading minimal sample data: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to load minimal sample data", e);
        }
    }

    // ==================== USER DATA ====================

    /**
     * Load comprehensive user dataset
     */
    public void loadUsers() {
        logger.info("Loading users...");

        try {
            // Administrator - credentials loaded from application.properties
            authenticationService.registerUser(
                ConfigLoader.getAdminEmail(),
                ConfigLoader.getAdminPassword(),
                ConfigLoader.getAdminName(),
                UserRole.valueOf(ConfigLoader.getAdminRole())
            );

            // Store Manager
            authenticationService.registerUser(
                "manager@store.com",
                "manager123",
                "Store Manager",
                UserRole.MANAGER
            );

            // Staff members
            authenticationService.registerUser(
                "cashier@store.com",
                "cashier123",
                "John Cashier",
                UserRole.USER
            );

            authenticationService.registerUser(
                "stocker@store.com",
                "stocker123",
                "Jane Stocker",
                UserRole.USER
            );

            // Customer accounts
            authenticationService.registerUser(
                "customer1@example.com",
                "customer123",
                "Alice User",
                UserRole.USER
            );

            authenticationService.registerUser(
                "customer2@example.com",
                "customer123",
                "Bob User",
                UserRole.USER
            );

            logger.info("Loaded 6 users (1 admin, 1 manager, 4 regular users)");
        } catch (Exception e) {
            logger.warn("Some users may already exist: {}", e.getMessage());
        }
    }

    /**
     * Load basic users (minimal)
     * All credentials loaded from application.properties
     */
    private void loadBasicUsers() {
        logger.info("Loading basic users...");

        try {
            // Admin user - from application.properties
            authenticationService.registerUser(
                ConfigLoader.getAdminEmail(),
                ConfigLoader.getAdminPassword(),
                ConfigLoader.getAdminName(),
                UserRole.valueOf(ConfigLoader.getAdminRole())
            );

            // Regular user - from application.properties
            authenticationService.registerUser(
                ConfigLoader.getUserEmail(),
                ConfigLoader.getUserPassword(),
                ConfigLoader.getUserName(),
                UserRole.valueOf(ConfigLoader.getUserRole())
            );

            logger.info("Loaded 2 basic users");
        } catch (Exception e) {
            logger.warn("Basic users may already exist: {}", e.getMessage());
        }
    }

    // ==================== STORE DATA ====================

    /**
     * Load multiple stores
     */
    public void loadStores() {
        logger.info("Loading stores...");

        try {
            // Downtown flagship store
            storeService.provisionStore(
                "store1",
                "Downtown Flagship Store",
                "123 Main Street, Downtown, CA 90001",
                ""
            );

            // Suburban location
            storeService.provisionStore(
                "store2",
                "Suburban Shopping Center",
                "456 Oak Avenue, Suburbia, CA 90002",
                ""
            );

            // Express store
            storeService.provisionStore(
                "store3",
                "Express Mini-Mart",
                "789 Quick Lane, Metro, CA 90003",
                ""
            );

            logger.info("Loaded 3 stores");
        } catch (Exception e) {
            logger.warn("Some stores may already exist: {}", e.getMessage());
        }
    }

    /**
     * Load basic store (minimal)
     */
    private void loadBasicStore() {
        logger.info("Loading basic store...");

        try {
            storeService.provisionStore(
                "store1",
                "Test Store",
                "123 Test Street",
                ""
            );

            logger.info("Loaded 1 basic store");
        } catch (Exception e) {
            logger.warn("Basic store may already exist: {}", e.getMessage());
        }
    }

    // ==================== AISLE DATA ====================

    /**
     * Load aisles for all stores
     */
    public void loadAisles() {
        logger.info("Loading aisles...");

        try {
            // Store 1 - Downtown (comprehensive layout)
            storeService.provisionAisle("store1", "A1", "Fresh Produce", "Fruits and vegetables", AisleLocation.floor, "");
            storeService.provisionAisle("store1", "A2", "Dairy & Eggs", "Refrigerated dairy products", AisleLocation.floor, "");
            storeService.provisionAisle("store1", "A3", "Frozen Foods", "Frozen meals and ice cream", AisleLocation.floor, "");
            storeService.provisionAisle("store1", "A4", "Bakery", "Fresh baked goods", AisleLocation.floor, "");
            storeService.provisionAisle("store1", "A5", "Beverages", "Drinks and refreshments", AisleLocation.floor, "");
            storeService.provisionAisle("store1", "A6", "Snacks & Candy", "Chips and confections", AisleLocation.floor, "");

            // Store 2 - Suburban (medium layout)
            storeService.provisionAisle("store2", "B1", "Groceries", "General groceries", AisleLocation.floor, "");
            storeService.provisionAisle("store2", "B2", "Refrigerated", "Cold items", AisleLocation.floor, "");
            storeService.provisionAisle("store2", "B3", "Pantry", "Dry goods", AisleLocation.store_room, "");

            // Store 3 - Express (minimal layout)
            storeService.provisionAisle("store3", "C1", "Quick Grab", "Essentials", AisleLocation.floor, "");

            logger.info("Loaded 10 aisles across 3 stores");
        } catch (Exception e) {
            logger.warn("Error loading aisles: {}", e.getMessage());
        }
    }

    // ==================== SHELF DATA ====================

    /**
     * Load shelves in aisles
     */
    public void loadShelves() {
        logger.info("Loading shelves...");

        try {
            // Store 1, Aisle A1 - Fresh Produce
            storeService.provisionShelf("store1", "A1", "A1-S1", "Top Shelf", ShelfLevel.high, "Premium fruits", Temperature.refrigerated, "");
            storeService.provisionShelf("store1", "A1", "A1-S2", "Middle Shelf", ShelfLevel.medium, "Regular produce", Temperature.refrigerated, "");
            storeService.provisionShelf("store1", "A1", "A1-S3", "Bottom Shelf", ShelfLevel.low, "Bulk items", Temperature.ambient, "");

            // Store 1, Aisle A2 - Dairy (only one shelf per level allowed per aisle)
            storeService.provisionShelf("store1", "A2", "A2-S1", "Milk & Cream", ShelfLevel.high, "Dairy products", Temperature.refrigerated, "");
            storeService.provisionShelf("store1", "A2", "A2-S2", "Cheese Section", ShelfLevel.medium, "Cheese varieties", Temperature.refrigerated, "");

            // Store 1, Aisle A3 - Frozen
            storeService.provisionShelf("store1", "A3", "A3-S1", "Frozen Meals", ShelfLevel.high, "Ready meals", Temperature.frozen, "");
            storeService.provisionShelf("store1", "A3", "A3-S2", "Ice Cream", ShelfLevel.medium, "Frozen desserts", Temperature.frozen, "");

            // Store 1, Aisle A4 - Bakery
            storeService.provisionShelf("store1", "A4", "A4-S1", "Fresh Bread", ShelfLevel.medium, "Daily bread", Temperature.ambient, "");

            // Store 1, Aisle A5 - Beverages
            storeService.provisionShelf("store1", "A5", "A5-S1", "Soft Drinks", ShelfLevel.low, "Carbonated drinks", Temperature.refrigerated, "");
            storeService.provisionShelf("store1", "A5", "A5-S2", "Juices", ShelfLevel.high, "Fruit juices", Temperature.refrigerated, "");

            // Store 1, Aisle A6 - Snacks
            storeService.provisionShelf("store1", "A6", "A6-S1", "Chips & Crisps", ShelfLevel.high, "Savory snacks", Temperature.ambient, "");
            storeService.provisionShelf("store1", "A6", "A6-S2", "Candy", ShelfLevel.low, "Confections", Temperature.ambient, "");

            // Store 2 - Suburban
            storeService.provisionShelf("store2", "B1", "B1-S1", "General Shelf", ShelfLevel.medium, "Various items", Temperature.ambient, "");
            storeService.provisionShelf("store2", "B2", "B2-S1", "Cold Shelf", ShelfLevel.high, "Refrigerated items", Temperature.refrigerated, "");

            logger.info("Loaded 14 shelves");
        } catch (Exception e) {
            logger.warn("Error loading shelves: {}", e.getMessage());
        }
    }

    // ==================== PRODUCT DATA ====================

    /**
     * Load comprehensive product catalog
     */
    public void loadProducts() {
        logger.info("Loading products...");

        try {
            // Dairy products
            storeService.provisionProduct("PROD001", "Whole Milk", "Fresh whole milk", "1 gallon", "Dairy", 3.99, Temperature.refrigerated, "");
            storeService.provisionProduct("PROD002", "Cheddar Cheese", "Aged cheddar", "16 oz", "Dairy", 5.49, Temperature.refrigerated, "");
            storeService.provisionProduct("PROD003", "Greek Yogurt", "Plain yogurt", "32 oz", "Dairy", 4.99, Temperature.refrigerated, "");

            // Frozen foods
            storeService.provisionProduct("PROD004", "Frozen Pizza", "Pepperoni pizza", "Large", "Frozen", 7.99, Temperature.frozen, "");
            storeService.provisionProduct("PROD005", "Ice Cream", "Vanilla ice cream", "1.5 quart", "Frozen", 6.49, Temperature.frozen, "");
            storeService.provisionProduct("PROD006", "Frozen Vegetables", "Mixed vegetables", "16 oz", "Frozen", 2.99, Temperature.frozen, "");

            // Produce
            storeService.provisionProduct("PROD007", "Apples", "Fresh red apples", "per lb", "Produce", 1.99, Temperature.refrigerated, "");
            storeService.provisionProduct("PROD008", "Bananas", "Fresh bananas", "per lb", "Produce", 0.59, Temperature.ambient, "");
            storeService.provisionProduct("PROD009", "Lettuce", "Romaine lettuce", "Head", "Produce", 2.49, Temperature.refrigerated, "");

            // Bakery
            storeService.provisionProduct("PROD010", "Whole Wheat Bread", "Fresh baked", "24 oz loaf", "Bakery", 3.49, Temperature.ambient, "");
            storeService.provisionProduct("PROD011", "Bagels", "Plain bagels", "6 pack", "Bakery", 4.29, Temperature.ambient, "");

            // Beverages
            storeService.provisionProduct("PROD012", "Orange Juice", "100% pure", "64 oz", "Beverages", 4.99, Temperature.refrigerated, "");
            storeService.provisionProduct("PROD013", "Cola", "Carbonated soda", "2 liter", "Beverages", 2.49, Temperature.refrigerated, "");
            storeService.provisionProduct("PROD014", "Bottled Water", "Spring water", "24 pack", "Beverages", 5.99, Temperature.ambient, "");

            // Snacks
            storeService.provisionProduct("PROD015", "Potato Chips", "Classic flavor", "10 oz", "Snacks", 3.99, Temperature.ambient, "");
            storeService.provisionProduct("PROD016", "Chocolate Bar", "Milk chocolate", "1.5 oz", "Candy", 1.49, Temperature.ambient, "");
            storeService.provisionProduct("PROD017", "Pretzels", "Salted pretzels", "16 oz", "Snacks", 3.29, Temperature.ambient, "");

            logger.info("Loaded 17 products");
        } catch (Exception e) {
            logger.warn("Error loading products: {}", e.getMessage());
        }
    }

    /**
     * Load basic products (minimal)
     */
    private void loadBasicProducts() {
        logger.info("Loading basic products...");

        try {
            storeService.provisionProduct("PROD001", "Test Product 1", "Test item", "1 unit", "Test", 9.99, Temperature.ambient, "");
            storeService.provisionProduct("PROD002", "Test Product 2", "Test item", "1 unit", "Test", 14.99, Temperature.refrigerated, "");

            logger.info("Loaded 2 basic products");
        } catch (Exception e) {
            logger.warn("Error loading basic products: {}", e.getMessage());
        }
    }

    // ==================== INVENTORY DATA ====================

    /**
     * Load inventory records
     */
    public void loadInventory() {
        logger.info("Loading inventory...");

        try {
            // Store 1 - Dairy aisle inventory
            storeService.provisionInventory("INV001", "store1", "A2", "A2-S1", 100, 85, "PROD001", InventoryType.standard, "");
            storeService.provisionInventory("INV002", "store1", "A2", "A2-S2", 50, 42, "PROD002", InventoryType.standard, "");
            storeService.provisionInventory("INV003", "store1", "A2", "A2-S1", 60, 55, "PROD003", InventoryType.standard, "");

            // Store 1 - Frozen aisle inventory
            storeService.provisionInventory("INV004", "store1", "A3", "A3-S1", 40, 28, "PROD004", InventoryType.standard, "");
            storeService.provisionInventory("INV005", "store1", "A3", "A3-S2", 80, 65, "PROD005", InventoryType.standard, "");
            storeService.provisionInventory("INV006", "store1", "A3", "A3-S1", 100, 90, "PROD006", InventoryType.standard, "");

            // Store 1 - Produce inventory
            storeService.provisionInventory("INV007", "store1", "A1", "A1-S2", 200, 150, "PROD007", InventoryType.standard, "");
            storeService.provisionInventory("INV008", "store1", "A1", "A1-S3", 300, 275, "PROD008", InventoryType.standard, "");

            // Store 1 - Bakery inventory
            storeService.provisionInventory("INV009", "store1", "A4", "A4-S1", 50, 35, "PROD010", InventoryType.standard, "");

            // Store 1 - Beverages inventory
            storeService.provisionInventory("INV010", "store1", "A5", "A5-S2", 100, 78, "PROD012", InventoryType.standard, "");
            storeService.provisionInventory("INV011", "store1", "A5", "A5-S1", 150, 120, "PROD013", InventoryType.standard, "");

            // Store 1 - Snacks inventory
            storeService.provisionInventory("INV012", "store1", "A6", "A6-S1", 80, 65, "PROD015", InventoryType.standard, "");
            storeService.provisionInventory("INV013", "store1", "A6", "A6-S2", 200, 180, "PROD016", InventoryType.standard, "");

            logger.info("Loaded 13 inventory records");
        } catch (Exception e) {
            logger.warn("Error loading inventory: {}", e.getMessage());
        }
    }

    // ==================== CUSTOMER DATA ====================

    /**
     * Load customer data
     */
    public void loadCustomers() {
        logger.info("Loading customers...");

        try {
            // Registered customers
            storeService.provisionCustomer(
                "CUST001",
                "Alice",
                "Johnson",
                CustomerType.registered,
                "alice.johnson@example.com",
                "123 Elm Street, City, CA 90001",
                ""
            );

            storeService.provisionCustomer(
                "CUST002",
                "Bob",
                "Smith",
                CustomerType.registered,
                "bob.smith@example.com",
                "456 Maple Avenue, City, CA 90002",
                ""
            );

            storeService.provisionCustomer(
                "CUST003",
                "Carol",
                "Williams",
                CustomerType.registered,
                "carol.williams@example.com",
                "789 Oak Boulevard, City, CA 90003",
                ""
            );

            // Guest customers
            storeService.provisionCustomer(
                "GUEST001",
                "Guest",
                "User1",
                CustomerType.guest,
                "",
                "",
                ""
            );

            logger.info("Loaded 4 customers (3 registered, 1 guest)");
        } catch (Exception e) {
            logger.warn("Error loading customers: {}", e.getMessage());
        }
    }

    // ==================== BASKET DATA ====================

    /**
     * Load shopping baskets
     */
    public void loadBaskets() {
        logger.info("Loading baskets...");

        try {
            // Create baskets
            storeService.provisionBasket("BASKET001", "");
            storeService.provisionBasket("BASKET002", "");
            storeService.provisionBasket("BASKET003", "");

            // Update customers to be in store and assign baskets
            storeService.updateCustomer("CUST001", "store1", "A2", "");
            storeService.assignCustomerBasket("CUST001", "BASKET001", "");

            storeService.updateCustomer("CUST002", "store1", "A5", "");
            storeService.assignCustomerBasket("CUST002", "BASKET002", "");

            // Add items to BASKET001 (CUST001)
            // Customer is in A2 (Dairy), add dairy products
            storeService.addBasketProduct("BASKET001", "PROD001", 2, ""); // 2x Milk (in A2)
            storeService.addBasketProduct("BASKET001", "PROD002", 1, ""); // 1x Cheese (in A2)

            // Move customer to A1 (Produce) to add apples
            storeService.updateCustomer("CUST001", "store1", "A1", "");
            storeService.addBasketProduct("BASKET001", "PROD007", 5, ""); // 5 lbs Apples (in A1)

            // Add items to BASKET002 (CUST002)
            // Customer is already in A5 (Beverages), add beverage products
            storeService.addBasketProduct("BASKET002", "PROD012", 1, ""); // 1x Orange Juice (in A5)
            storeService.addBasketProduct("BASKET002", "PROD013", 2, ""); // 2x Cola (in A5)

            logger.info("Loaded 3 baskets with items for 2 customers");
        } catch (Exception e) {
            logger.warn("Error loading baskets: {}", e.getMessage(), e);
        }
    }

    // ==================== DEVICE DATA ====================

    /**
     * Load IoT devices (sensors and appliances)
     */
    public void loadDevices() {
        logger.info("Loading IoT devices...");

        try {
            // Store 1 devices
            // Sensors
            storeService.provisionDevice("DEV001", "Entrance Camera", "camera", "store1", "A1", "");
            storeService.provisionDevice("DEV002", "Aisle Microphone", "microphone", "store1", "A2", "");
            storeService.provisionDevice("DEV003", "Entry Turnstile", "turnstile", "store1", "A1", "");

            // Appliances
            storeService.provisionDevice("DEV004", "Cleaning Robot", "robot", "store1", "A6", "");
            storeService.provisionDevice("DEV005", "Announcement Speaker", "speaker", "store1", "A5", "");

            // Store 2 devices
            storeService.provisionDevice("DEV006", "Security Camera", "camera", "store2", "B1", "");
            storeService.provisionDevice("DEV007", "PA System", "speaker", "store2", "B2", "");

            logger.info("Loaded 7 IoT devices (3 sensors, 4 appliances)");
        } catch (Exception e) {
            logger.warn("Error loading devices: {}", e.getMessage(), e);
        }
    }

    // ==================== SUMMARY ====================

    /**
     * Print summary of loaded data
     */
    public void printDataSummary() {
        logger.info("");
        logger.info("=".repeat(80));
        logger.info("SAMPLE DATA SUMMARY");
        logger.info("=".repeat(80));
        logger.info("Users:      6 (admin, manager, staff, customers)");
        logger.info("Stores:     3 (downtown, suburban, express)");
        logger.info("Aisles:     10 across all stores");
        logger.info("Shelves:    14 with various temperature zones");
        logger.info("Products:   17 across multiple categories");
        logger.info("Inventory:  13 stocked items");
        logger.info("Customers:  4 (3 registered, 1 guest)");
        logger.info("Baskets:    3 (2 with items)");
        logger.info("Devices:    7 IoT devices");
        logger.info("=".repeat(80));
        logger.info("Available Test Credentials:");
        logger.info("  Admin:    {} / {}", ConfigLoader.getAdminEmail(), ConfigLoader.getAdminPassword());
        logger.info("  Manager:  manager@store.com / manager123");
        logger.info("  Customer: customer1@example.com / customer123");
        logger.info("=".repeat(80));
    }
}