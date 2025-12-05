package com.se300.store.controller.externalmockserver;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * External Mock Server Tests
 *
 * These tests interact with an external mock API endpoint hosted on Apidog.
 * The external endpoint simulates the Smart Store REST API for integration testing.
 *
 * Purpose: Demonstrate integration testing with external third-party APIs
 * and validate that our application can consume external store services.
 */
@DisplayName("External Mock Server Tests - Apidog Integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Disabled("External tests disabled - requires actual Apidog or mock server endpoint")
public class ExternalMockServerTest {

    // External mock API endpoint - Replace with your actual Apidog or mock server URL
    // Example: https://your-workspace.apidog.io/api/v1
    private static final String EXTERNAL_API_BASE_URL = "http://localhost:9999";
    private static final String STORES_ENDPOINT = "/stores";
    private static final String USERS_ENDPOINT = "/users";

    @BeforeAll
    public static void setUpExternalMockServer() {
        // Configure RestAssured for external API testing
        RestAssured.baseURI = EXTERNAL_API_BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @AfterAll
    public static void tearDown() {
        RestAssured.reset();
    }

    // ==================== STORE OPERATIONS ====================

    @Test
    @Order(1)
    @DisplayName("External API: GET /stores - Retrieve all stores")
    public void testGetAllStores() {
        given()
        .when()
            .get(STORES_ENDPOINT)
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", greaterThanOrEqualTo(0));
    }

    @Test
    @Order(2)
    @DisplayName("External API: GET /stores/{id} - Retrieve store by ID")
    public void testGetStoreById() {
        String storeId = "store1";
        given()
        .when()
            .get(STORES_ENDPOINT + "/" + storeId)
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id", equalTo(storeId));
    }

    @Test
    @Order(3)
    @DisplayName("External API: POST /stores - Create new store")
    public void testCreateStore() {
        given()
            .param("storeId", "external-store-1")
            .param("name", "External Test Store")
            .param("address", "123 External St")
        .when()
            .post(STORES_ENDPOINT)
        .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body("id", equalTo("external-store-1"))
            .body("description", equalTo("External Test Store"));
    }

    @Test
    @Order(4)
    @DisplayName("External API: PUT /stores/{id} - Update store")
    public void testUpdateStore() {
        String storeId = "store1";
        given()
            .param("description", "Updated External Store")
            .param("address", "456 Updated St")
        .when()
            .put(STORES_ENDPOINT + "/" + storeId)
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id", equalTo(storeId))
            .body("description", equalTo("Updated External Store"));
    }

    @Test
    @Order(5)
    @DisplayName("External API: DELETE /stores/{id} - Delete store")
    public void testDeleteStore() {
        String storeId = "store1";
        given()
        .when()
            .delete(STORES_ENDPOINT + "/" + storeId)
        .then()
            .statusCode(204);
    }

    // ==================== USER OPERATIONS ====================

    @Test
    @Order(6)
    @DisplayName("External API: GET /users - Retrieve all users")
    public void testGetAllUsers() {
        given()
        .when()
            .get(USERS_ENDPOINT)
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", greaterThanOrEqualTo(0));
    }

    @Test
    @Order(7)
    @DisplayName("External API: POST /users - Register new user")
    public void testRegisterUser() {
        given()
            .param("email", "external@test.com")
            .param("password", "SecurePassword123")
            .param("name", "External User")
        .when()
            .post(USERS_ENDPOINT)
        .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body("email", equalTo("external@test.com"))
            .body("name", equalTo("External User"));
    }

    @Test
    @Order(8)
    @DisplayName("External API: GET /users/{email} - Retrieve user by email")
    public void testGetUserByEmail() {
        String email = "user@test.com";
        given()
        .when()
            .get(USERS_ENDPOINT + "/" + email)
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("email", equalTo(email));
    }

    @Test
    @Order(9)
    @DisplayName("External API: PUT /users/{email} - Update user")
    public void testUpdateUser() {
        String email = "user@test.com";
        given()
            .param("name", "Updated User Name")
        .when()
            .put(USERS_ENDPOINT + "/" + email)
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("email", equalTo(email))
            .body("name", equalTo("Updated User Name"));
    }

    // ==================== ERROR HANDLING ====================

    @Test
    @Order(10)
    @DisplayName("External API: Handle 404 - Non-existent store")
    public void testGetNonExistentStore() {
        given()
        .when()
            .get(STORES_ENDPOINT + "/nonexistent-store-id")
        .then()
            .statusCode(404);
    }

    @Test
    @Order(11)
    @DisplayName("External API: Handle missing required parameters")
    public void testCreateStoreWithMissingParameters() {
        given()
            .param("storeId", "test-store")
            // Missing 'name' and 'address' parameters
        .when()
            .post(STORES_ENDPOINT)
        .then()
            .statusCode(400);
    }

    // ==================== INTEGRATION WORKFLOW ====================

    @Test
    @Order(12)
    @DisplayName("External API: Complete store lifecycle workflow")
    public void testCompleteStoreLifecycle() {
        String storeId = "lifecycle-test-store";

        // 1. Create store
        given()
            .param("storeId", storeId)
            .param("name", "Lifecycle Test Store")
            .param("address", "999 Lifecycle Ave")
        .when()
            .post(STORES_ENDPOINT)
        .then()
            .statusCode(201)
            .body("id", equalTo(storeId));

        // 2. Retrieve store
        given()
        .when()
            .get(STORES_ENDPOINT + "/" + storeId)
        .then()
            .statusCode(200)
            .body("id", equalTo(storeId));

        // 3. Update store
        given()
            .param("description", "Lifecycle Store Updated")
            .param("address", "999 Updated Lifecycle Ave")
        .when()
            .put(STORES_ENDPOINT + "/" + storeId)
        .then()
            .statusCode(200)
            .body("description", equalTo("Lifecycle Store Updated"));

        // 4. Delete store
        given()
        .when()
            .delete(STORES_ENDPOINT + "/" + storeId)
        .then()
            .statusCode(204);

        // 5. Verify deletion
        given()
        .when()
            .get(STORES_ENDPOINT + "/" + storeId)
        .then()
            .statusCode(404);
    }

    // ==================== PERFORMANCE TEST ====================

    @Test
    @Order(13)
    @DisplayName("External API: Response time validation")
    public void testApiResponseTime() {
        given()
        .when()
            .get(STORES_ENDPOINT)
        .then()
            .statusCode(200)
            .time(lessThan(5000L)); // Response should be under 5 seconds
    }
}