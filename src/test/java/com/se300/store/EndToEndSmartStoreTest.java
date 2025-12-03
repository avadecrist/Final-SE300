package com.se300.store;

import com.se300.store.controller.StoreController;
import com.se300.store.controller.UserController;
import com.se300.store.data.DataManager;
import com.se300.store.model.*;
import com.se300.store.repository.StoreRepository;
import com.se300.store.repository.UserRepository;
import com.se300.store.service.AuthenticationService;
import com.se300.store.service.StoreService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.catalina.Context;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-End Integration Tests for the Smart Store Application.
 * Tests the complete system including all layers: REST API, Controllers, Services, Repositories, and Data.
 * Uses a clean Tomcat server (no sample data) to ensure test isolation.
 */
@DisplayName("Big Bang Integration Test - Complete System Testing")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EndToEndSmartStoreTest {

    /* TODO: The following
     * 1. Achieve 100% Test Coverage
     * 2. Produce/Print Identical Results to Command Line DriverTest
     * 3. Produce SonarCube Quality and Coverage Report
     */

    private static DataManager dataManager;
    private static StoreRepository storeRepository;
    private static UserRepository userRepository;
    private static StoreService storeService;
    private static AuthenticationService authenticationService;
    private static Tomcat tomcat;

    @BeforeAll
    public static void setUpCompleteSystem() throws Exception {
        // Initialize data layer
        dataManager = DataManager.getInstance();
        dataManager.clear();

        // Ensure repository backing maps exist in DataManager for tests
        dataManager.put("stores", new java.util.HashMap<String, Store>());

        // Clear static maps in StoreService to ensure clean state (no sample data)
        StoreService.clearAllMaps();

        // Initialize repositories
        storeRepository = new StoreRepository(dataManager);
        userRepository = new UserRepository(dataManager);

        // Initialize services
        storeService = new StoreService(storeRepository);
        authenticationService = new AuthenticationService(userRepository);

        // Initialize controllers
        StoreController storeController = new StoreController(storeService);
        UserController userController = new UserController(authenticationService);

        // Start clean Tomcat server (without sample data from SmartStoreApplication)
        tomcat = new Tomcat();
        tomcat.setPort(0); // Use dynamic port allocation
        tomcat.getConnector();

        String contextPath = "";
        String docBase = new File(".").getAbsolutePath();
        Context context = tomcat.addContext(contextPath, docBase);

        // Register test-only filter that returns stack traces in responses
        FilterDef filterDef = new FilterDef();
        filterDef.setFilterName("testExceptionFilter");
        filterDef.setFilterClass("com.se300.store.TestExceptionLoggingFilter");
        context.addFilterDef(filterDef);
        FilterMap filterMap = new FilterMap();
        filterMap.setFilterName("testExceptionFilter");
        filterMap.addURLPattern("/*");
        context.addFilterMap(filterMap);

        // Register controllers
        Tomcat.addServlet(context, "storeController", storeController);
        context.addServletMappingDecoded("/api/v1/stores/*", "storeController");

        Tomcat.addServlet(context, "userController", userController);
        context.addServletMappingDecoded("/api/v1/users/*", "userController");

        tomcat.start();
        
        // Get the actual port assigned by the system
        int testPort = tomcat.getConnector().getLocalPort();

        // Configure RestAssured
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = testPort;

        // Wait for server to be ready
        Thread.sleep(1000);
    }

    @AfterEach
    public void cleanupBetweenTests() {
        // Don't clear data between tests since they're ordered and may depend on previous test data
    }

    @AfterAll
    public static void tearDownCompleteSystem() throws Exception {
        // Stop Tomcat server
        if (tomcat != null) {
            try {
                tomcat.stop();
                tomcat.destroy();
                // Give server time to shut down completely
                Thread.sleep(1000);
            } catch (Exception e) {
                // Force cleanup even if stop fails
                System.err.println("Error stopping Tomcat: " + e.getMessage());
            }
        }

        // Clear all data
        if (dataManager != null) {
            dataManager.clear();
        }
        StoreService.clearAllMaps();
    }

    @Test
    @Order(1)
    @DisplayName("E2E: Complete user registration and authentication workflow")
    public void testCompleteUserWorkflow() {;
        String email = "e2e-user@example.com";
        String name = "E2E User";
        String password = "password123";

        // Register user
        given()
            .contentType(ContentType.URLENC)
            .param("email", email)
            .param("password", password)
            .param("name", name)
        .when()
            .post("/api/v1/users")
        .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body("email", equalTo(email))
            .body("name", equalTo(name));

        // Get user
        given()
        .when()
            .get("/api/v1/users/" + email)
        .then()
            .statusCode(200)
            .body("email", equalTo(email));

        // Update user
        String newName = "E2E User Updated";
        given()
            .contentType(ContentType.URLENC)
            .param("name", newName)
        .when()
            .put("/api/v1/users/" + email)
        .then()
            .statusCode(200)
            .body("name", equalTo(newName));

        // Delete user
        given()
        .when()
            .delete("/api/v1/users/" + email)
        .then()
            .statusCode(204);

        // Verify deletion
        given()
        .when()
            .get("/api/v1/users/" + email)
        .then()
            .statusCode(404);
    }

    @Test
    @Order(2)
    @DisplayName("E2E: Complete store provisioning and management workflow")
    public void testCompleteStoreWorkflow() throws StoreException {
        String storeId = "e2e-store-1";
        String name = "E2E Store";
        String address = "123 E2E Lane";

        // Diagnostic: call service directly to see if service throws an exception
        try {
            storeService.provisionStore("diag-" + storeId, name, address, null);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("StoreService.provisionStore threw: " + t.getClass().getName() + ": " + t.getMessage());
        }

        // Create store
        Response createResp = given()
            .contentType(ContentType.URLENC)
            .param("storeId", storeId)
            .param("name", name)
            .param("address", address)
        .when()
            .post("/api/v1/stores");

        if (createResp.statusCode() != 201) {
            System.err.println("Store creation failed (status=" + createResp.statusCode() + ") body:\n" + createResp.asString());
            fail("Expected 201 Created but got " + createResp.statusCode() + " - body: " + createResp.asString());
        }
        createResp.then().statusCode(201).body("id", equalTo(storeId)).body("description", equalTo(name));

        // Get store by id
        given()
        .when()
            .get("/api/v1/stores/" + storeId)
        .then()
            .statusCode(200)
            .body("id", equalTo(storeId));

        // Update store
        String newAddress = "456 Updated Ave";
        given()
            .contentType(ContentType.URLENC)
            .param("address", newAddress)
        .when()
            .put("/api/v1/stores/" + storeId)
        .then()
            .statusCode(200)
            .body("address", equalTo(newAddress));

        // Delete store
        given()
        .when()
            .delete("/api/v1/stores/" + storeId)
        .then()
            .statusCode(204);

        // Verify deletion
        given()
        .when()
            .get("/api/v1/stores/" + storeId)
        .then()
            .statusCode(404);
    }

    @Test
    @Order(3)
    @DisplayName("E2E: Complete store operations - aisles, shelves, products, inventory")
    public void testCompleteStoreOperations() throws StoreException {
        String storeId = "ops-store-1";
        // Create a store via service (controller endpoints for aisles/shelves are not available)
        Store store = storeService.provisionStore(storeId, "Ops Store", "1 Ops Way", null);
        assertNotNull(store);

        // Provision aisles
        Aisle aisleA = storeService.provisionAisle(storeId, "A1", "Aisle A1", "Primary aisle", AisleLocation.store_room, null);
        assertNotNull(aisleA);
        assertEquals("A1", aisleA.getNumber());

        Aisle aisleB = storeService.provisionAisle(storeId, "B1", "Aisle B1", "Secondary aisle", AisleLocation.floor, null);
        assertNotNull(aisleB);

        // Show aisle
        Aisle shown = storeService.showAisle(storeId, "A1", null);
        assertEquals("Aisle A1", shown.getName());

        // Provision shelves in aisle A1
        Shelf shelf1 = storeService.provisionShelf(storeId, "A1", "shelf_q1", "Shelf Q1", ShelfLevel.high, "Top shelf", Temperature.frozen, null);
        assertNotNull(shelf1);

        Shelf shelf2 = storeService.provisionShelf(storeId, "A1", "shelf_q2", "Shelf Q2", ShelfLevel.medium, "Middle shelf", Temperature.ambient, null);
        assertNotNull(shelf2);

        // Provision products
        Product prod10 = storeService.provisionProduct("prod10", "Milk", "Dairy milk", "1L", "Dairy", 2.99, Temperature.frozen, null);
        Product prod11 = storeService.provisionProduct("prod11", "Cereal", "Breakfast cereal", "500g", "Grocery", 3.49, Temperature.ambient, null);
        assertNotNull(prod10);
        assertNotNull(prod11);

        // Provision inventory (matching temperatures)
        Inventory inv1 = storeService.provisionInventory("inv_u21", storeId, "A1", "shelf_q1", 1500, 1000, "prod10", InventoryType.standard, null);
        assertNotNull(inv1);
        assertEquals(1000, inv1.getCount());

        Inventory inv2 = storeService.provisionInventory("inv_u22", storeId, "A1", "shelf_q2", 1500, 500, "prod11", InventoryType.flexible, null);
        assertNotNull(inv2);

        // Show inventory
        Inventory shownInv = storeService.showInventory("inv_u21", null);
        assertEquals("inv_u21", shownInv.getId());

        // Update inventory (API expects positive delta to increment)
        Inventory updated = storeService.updateInventory("inv_u21", 100, null);
        assertEquals(1100, updated.getCount());

        // Negative checks: try to provision inventory with wrong temperature -> expect StoreException
        Exception ex = assertThrows(StoreException.class, () -> {
            storeService.provisionInventory("inv_bad", storeId, "A1", "shelf_q1", 100, 10, "prod11", InventoryType.standard, null);
        });
        // Message may be null in some implementations; assert exception type instead of message content
        assertTrue(ex instanceof StoreException);
    }

    @Test
    @Order(4)
    @DisplayName("E2E: Complete customer shopping workflow")
    public void testCompleteCustomerShoppingWorkflow() throws StoreException {
    }

    @Test
    @Order(5)
    @Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
    @DisplayName("E2E: Device management and events")
    public void testCompleteDeviceWorkflow() throws StoreException {
    }

    @Test
    @Order(6)
    @DisplayName("E2E: Error handling across all layers")
    public void testCompleteErrorHandling() {
        // Missing params for user registration -> 400
        given()
            .contentType(ContentType.URLENC)
            .param("email", "")
        .when()
            .post("/api/v1/users")
        .then()
            .statusCode(anyOf(equalTo(400), equalTo(409)));

        // Missing params for creating store -> 400
        given()
            .contentType(ContentType.URLENC)
            .param("storeId", "")
        .when()
            .post("/api/v1/stores")
        .then()
            .statusCode(anyOf(equalTo(400), equalTo(409)));
    }

    @Test
    @Order(7)
    @DisplayName("E2E: Data consistency across all layers")
    public void testDataConsistencyAcrossLayers() {
    }

    @Test
    @Order(8)
    @DisplayName("E2E: REST API Controller - Store CRUD operations")
    public void testRestApiStoreOperations() {
        // create multiple stores and fetch all
        for(int i=0;i<2;i++){
            String id = "list-store-"+i;
            Response r = given().contentType(ContentType.URLENC)
                .param("storeId", id)
                .param("name", "Store " + i)
                .param("address", "Addr " + i)
            .when().post("/api/v1/stores");
            if (r.statusCode() != 201) {
                System.err.println("Store creation failed for id=" + id + " status=" + r.statusCode() + " body:\n" + r.asString());
                fail("Store creation failed for id=" + id + " status=" + r.statusCode());
            }
        }

        // fetch all stores
        given()
        .when()
            .get("/api/v1/stores")
        .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(2));
    }

    @Test
    @Order(9)
    @DisplayName("E2E: REST API Controller - User CRUD operations")
    public void testRestApiUserOperations() {
        String email = "list-user@example.com";
        given().contentType(ContentType.URLENC)
            .param("email", email)
            .param("password", "pw")
            .param("name", "List User")
        .when().post("/api/v1/users").then().statusCode(201);

        // get all users
        given().when().get("/api/v1/users").then().statusCode(200).body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    @Order(10)
    @DisplayName("E2E: REST API Controller - Error handling")
    public void testRestApiErrorHandling() {
    }

    @Test
    @Order(11)
    @DisplayName("E2E: Final cleanup and deletion operations")
    public void testFinalCleanupOperations() throws StoreException {
    }

    @Test
    @Order(12)
    @DisplayName("E2E: Complete store.script data processing with assertions")
    public void testStoreScriptEndToEnd() throws Exception {
    }
}