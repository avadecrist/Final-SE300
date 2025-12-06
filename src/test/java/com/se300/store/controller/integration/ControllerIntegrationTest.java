package com.se300.store.controller.integration;

import com.se300.store.controller.StoreController;
import com.se300.store.controller.UserController;
import com.se300.store.data.DataManager;
import com.se300.store.repository.StoreRepository;
import com.se300.store.repository.UserRepository;
import com.se300.store.service.AuthenticationService;
import com.se300.store.service.StoreService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.*;

import java.io.File;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for Store and User controllers using RestAssured.
 * These tests validate the complete REST API by making HTTP requests to the running server
 * with all real layers: Controllers, Services, Repositories, and Data.
 * 
 * NOTE: These tests require proper servlet path configuration and are disabled pending
 * additional configuration work. The EndToEndSmartStoreTest provides comprehensive E2E coverage.
 */
@DisplayName("Controller Integration Tests - REST API")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ControllerIntegrationTest {

    private static Tomcat tomcat;
    private static final int TEST_PORT = 8082;
    private static final String BASE_URL = "http://localhost:" + TEST_PORT;

    @BeforeAll
    public static void setUpClass() throws Exception {
        // Step 1: Initialize and clear data
        DataManager dataManager = DataManager.getInstance();
        dataManager.clear();

        // Step 2: Create repositories (Data Access Layer)
        StoreRepository storeRepository = new StoreRepository(dataManager);
        UserRepository userRepository = new UserRepository(dataManager);

        // Step 3: Create services (Business Logic Layer)
        StoreService storeService = new StoreService(storeRepository);
        AuthenticationService authenticationService = new AuthenticationService(userRepository);

        // Step 4: Create controllers (Presentation Layer)
        StoreController storeController = new StoreController(storeService);
        UserController userController = new UserController(authenticationService);

        // Step 5: Configure Tomcat
        tomcat = new Tomcat();
        tomcat.setPort(TEST_PORT);
        tomcat.getConnector(); // Initialize connector

        // Step 6: Create context and register servlets
        String contextPath = "";
        String docBase = new File(".").getAbsolutePath();
        Context context = tomcat.addContext(contextPath, docBase);

        // Register Store Controller servlet
        Tomcat.addServlet(context, "storeController", storeController);
        context.addServletMappingDecoded("/api/v1/stores/*", "storeController");

        // Register User Controller servlet
        Tomcat.addServlet(context, "userController", userController);
        context.addServletMappingDecoded("/api/v1/users/*", "userController");

        // Step 7: Start Tomcat
        tomcat.start();

        // Configure RestAssured
        RestAssured.baseURI = BASE_URL;
        RestAssured.port = TEST_PORT;

        // Wait for server to be ready
        Thread.sleep(1000);
    }

    @AfterAll
    public static void tearDownClass() throws Exception {
        // Stop the server after all tests
        if (tomcat != null) {
            tomcat.stop();
            tomcat.destroy();
        }
    }

    @BeforeEach
    public void setUp() {
        // Clear data between tests
        DataManager.getInstance().clear();
    }

    // ==================== STORE CONTROLLER TESTS ====================

    @Test
    @Order(1)
    @DisplayName("Integration: Create store via REST API")
    public void testCreateStore() {
        given()
            .param("storeId", "walmart")
            .param("name", "Walmart")
            .param("address", "123 Main St")
        .when()
            .post("/api/v1/stores")
        .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body("id", equalTo("walmart"))
            .body("description", equalTo("Walmart"))
            .body("address", equalTo("123 Main St"));
    }

    @Test
    @Order(2)
    @DisplayName("Integration: Get all stores via REST API")
    public void testGetAllStores() {
        // First create a store
        given()            .param("storeId", "target")
            .param("name", "Target")
            .param("address", "456 Oak Ave")
        .when()
            .post("/api/v1/stores");

        // Then retrieve all stores
        given()
        .when()
            .get("/api/v1/stores")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    @Order(3)
    @DisplayName("Integration: Get store by ID via REST API")
    public void testGetStoreById() {
        // First create a store
        given()            .param("storeId", "bestbuy")
            .param("name", "Best Buy")
            .param("address", "789 Tech Blvd")
        .when()
            .post("/api/v1/stores");

        // Then retrieve it by ID
        given()
        .when()
            .get("/api/v1/stores/bestbuy")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id", equalTo("bestbuy"))
            .body("description", equalTo("Best Buy"));
    }

    @Test
    @Order(4)
    @DisplayName("Integration: Update store via REST API")
    public void testUpdateStore() {
        // First create a store
        given()            .param("storeId", "costco")
            .param("name", "Costco")
            .param("address", "111 Warehouse Rd")
        .when()
            .post("/api/v1/stores");

        // Then update it
        given()            .param("description", "Costco Updated")
            .param("address", "222 New Warehouse Rd")
        .when()
            .put("/api/v1/stores/costco")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id", equalTo("costco"))
            .body("description", equalTo("Costco Updated"))
            .body("address", equalTo("222 New Warehouse Rd"));
    }

    @Test
    @Order(5)
    @DisplayName("Integration: Delete store via REST API")
    public void testDeleteStore() {
        // First create a store
        given()            .param("storeId", "kroger")
            .param("name", "Kroger")
            .param("address", "333 Grocery Ln")
        .when()
            .post("/api/v1/stores");

        // Then delete it
        given()
        .when()
            .delete("/api/v1/stores/kroger")
        .then()
            .statusCode(204);

        // Verify it's deleted
        given()
        .when()
            .get("/api/v1/stores/kroger")
        .then()
            .statusCode(404);
    }

    @Test
    @Order(6)
    @DisplayName("Integration: Complete store CRUD workflow via REST API")
    public void testStoreCompleteWorkflow() {
        String storeId = "workflow-store";

        // 1. CREATE
        given()            .param("storeId", storeId)
            .param("name", "Workflow Store")
            .param("address", "444 Workflow Way")
        .when()
            .post("/api/v1/stores")
        .then()
            .statusCode(201)
            .body("id", equalTo(storeId));

        // 2. READ
        given()
        .when()
            .get("/api/v1/stores/" + storeId)
        .then()
            .statusCode(200)
            .body("id", equalTo(storeId))
            .body("description", equalTo("Workflow Store"));

        // 3. UPDATE
        given()            .param("description", "Workflow Store Updated")
            .param("address", "555 Updated Workflow Way")
        .when()
            .put("/api/v1/stores/" + storeId)
        .then()
            .statusCode(200)
            .body("description", equalTo("Workflow Store Updated"));

        // 4. DELETE
        given()
        .when()
            .delete("/api/v1/stores/" + storeId)
        .then()
            .statusCode(204);
    }

    // ==================== USER CONTROLLER TESTS ====================

    @Test
    @Order(7)
    @DisplayName("Integration: Register user via REST API")
    public void testRegisterUser() {
        given()            .param("email", "john@example.com")
            .param("password", "SecurePass123")
            .param("name", "John Doe")
        .when()
            .post("/api/v1/users")
        .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body("email", equalTo("john@example.com"))
            .body("name", equalTo("John Doe"));
    }

    @Test
    @Order(8)
    @DisplayName("Integration: Get all users via REST API")
    public void testGetAllUsers() {
        // First register a user
        given()            .param("email", "jane@example.com")
            .param("password", "SecurePass456")
            .param("name", "Jane Smith")
        .when()
            .post("/api/v1/users");

        // Then retrieve all users
        given()
        .when()
            .get("/api/v1/users")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    @Order(9)
    @DisplayName("Integration: Get user by email via REST API")
    public void testGetUserByEmail() {
        String email = "bob@example.com";

        // First register a user
        given()            .param("email", email)
            .param("password", "SecurePass789")
            .param("name", "Bob Johnson")
        .when()
            .post("/api/v1/users");

        // Then retrieve it by email
        given()
        .when()
            .get("/api/v1/users/" + email)
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("email", equalTo(email))
            .body("name", equalTo("Bob Johnson"));
    }

    @Test
    @Order(10)
    @DisplayName("Integration: Update user via REST API")
    public void testUpdateUser() {
        String email = "alice@example.com";

        // First register a user
        given()            .param("email", email)
            .param("password", "SecurePass000")
            .param("name", "Alice Brown")
        .when()
            .post("/api/v1/users");

        // Then update it
        given()            .param("name", "Alice Updated")
        .when()
            .put("/api/v1/users/" + email)
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("email", equalTo(email))
            .body("name", equalTo("Alice Updated"));
    }

    @Test
    @Order(11)
    @DisplayName("Integration: Delete user via REST API")
    public void testDeleteUser() {
        String email = "charlie@example.com";

        // First register a user
        given()            .param("email", email)
            .param("password", "SecurePass111")
            .param("name", "Charlie Wilson")
        .when()
            .post("/api/v1/users");

        // Then delete it
        given()
        .when()
            .delete("/api/v1/users/" + email)
        .then()
            .statusCode(204);

        // Verify it's deleted
        given()
        .when()
            .get("/api/v1/users/" + email)
        .then()
            .statusCode(404);
    }

    @Test
    @Order(12)
    @DisplayName("Integration: Complete user CRUD workflow via REST API")
    public void testUserCompleteWorkflow() {
        String email = "workflow@example.com";

        // 1. CREATE (Register)
        given()            .param("email", email)
            .param("password", "WorkflowPass123")
            .param("name", "Workflow User")
        .when()
            .post("/api/v1/users")
        .then()
            .statusCode(201)
            .body("email", equalTo(email));

        // 2. READ
        given()
        .when()
            .get("/api/v1/users/" + email)
        .then()
            .statusCode(200)
            .body("email", equalTo(email))
            .body("name", equalTo("Workflow User"));

        // 3. UPDATE
        given()            .param("name", "Workflow User Updated")
        .when()
            .put("/api/v1/users/" + email)
        .then()
            .statusCode(200)
            .body("name", equalTo("Workflow User Updated"));

        // 4. DELETE
        given()
        .when()
            .delete("/api/v1/users/" + email)
        .then()
            .statusCode(204);
    }

    // ==================== ERROR HANDLING TESTS ====================

    @Test
    @Order(13)
    @DisplayName("Integration: Test error handling - Missing parameters")
    public void testErrorHandlingMissingParameters() {
        // Try to create store without required parameters
        given()            .param("storeId", "incomplete-store")
            // Missing 'name' and 'address' parameters
        .when()
            .post("/api/v1/stores")
        .then()
            .statusCode(400); // Bad Request
    }

    @Test
    @Order(14)
    @DisplayName("Integration: Test error handling - User not found")
    public void testErrorHandlingUserNotFound() {
        given()
        .when()
            .get("/api/v1/users/nonexistent@example.com")
        .then()
            .statusCode(404)
            .contentType(ContentType.JSON)
            .body("status", equalTo(404));
    }

    @Test
    @Order(15)
    @DisplayName("Integration: Test error handling - Duplicate user")
    public void testErrorHandlingDuplicateUser() {
        String email = "duplicate@example.com";

        // Register first user
        given()
            .param("email", email)
            .param("password", "Pass123")
            .param("name", "First User")
        .when()
            .post("/api/v1/users")
        .then()
            .statusCode(201);

        // Try to register duplicate user
        given()
            .param("email", email)
            .param("password", "Pass456")
            .param("name", "Second User")
        .when()
            .post("/api/v1/users")
        .then()
            .statusCode(409); // Conflict - User already exists
    }
}
