package com.se300.store.controller.externalmockserver;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

/**
 * External Mock Server Tests
 *
 * These tests simulate integration with an external third-party API using MockServer.
 * The mock server simulates an external Smart Store REST API for integration testing.
 *
 * Purpose: Demonstrate integration testing with external third-party APIs
 * and validate that our application can consume external store services.
 */
@DisplayName("External Mock Server Tests - Third-Party API Integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ExternalMockServerTest {

    private static ClientAndServer mockServer;
    private static final int MOCK_SERVER_PORT = 9999;
    private static final String EXTERNAL_API_BASE_URL = "http://localhost:" + MOCK_SERVER_PORT;
    private static final String STORES_ENDPOINT = "/stores";
    private static final String USERS_ENDPOINT = "/users";

    @BeforeAll
    public static void setUpExternalMockServer() {
        // Start mock server to simulate external API
        mockServer = ClientAndServer.startClientAndServer(MOCK_SERVER_PORT);
        
        // Configure RestAssured for external API testing
        RestAssured.baseURI = EXTERNAL_API_BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @AfterAll
    public static void tearDown() {
        if (mockServer != null) {
            mockServer.stop();
        }
        RestAssured.reset();
    }
    
    @BeforeEach
    public void setUp() {
        // Reset mock server expectations before each test
        mockServer.reset();
    }

    // ==================== STORE OPERATIONS ====================

    @Test
    @Order(1)
    @DisplayName("External API: GET /stores - Retrieve all stores")
    public void testGetAllStores() {
        // Mock external API response
        mockServer
            .when(request()
                .withMethod("GET")
                .withPath(STORES_ENDPOINT))
            .respond(response()
                .withStatusCode(200)
                .withHeader(new Header("Content-Type", "application/json"))
                .withBody("[{\"id\":\"store1\",\"name\":\"External Store\"}]"));
        
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
        
        // Mock external API response
        mockServer
            .when(request()
                .withMethod("GET")
                .withPath(STORES_ENDPOINT + "/" + storeId))
            .respond(response()
                .withStatusCode(200)
                .withHeader(new Header("Content-Type", "application/json"))
                .withBody("{\"id\":\"store1\",\"name\":\"External Store\"}"));
        
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
        // Mock external API response
        mockServer
            .when(request()
                .withMethod("POST")
                .withPath(STORES_ENDPOINT))
            .respond(response()
                .withStatusCode(201)
                .withHeader(new Header("Content-Type", "application/json"))
                .withBody("{\"id\":\"external-store-1\",\"description\":\"External Test Store\",\"address\":\"123 External St\"}"));
        
        given()
            .contentType("application/x-www-form-urlencoded")
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
        
        // Mock external API response
        mockServer
            .when(request()
                .withMethod("PUT")
                .withPath(STORES_ENDPOINT + "/" + storeId))
            .respond(response()
                .withStatusCode(200)
                .withHeader(new Header("Content-Type", "application/json"))
                .withBody("{\"id\":\"store1\",\"description\":\"Updated External Store\",\"address\":\"456 Updated St\"}"));
        
        given()
            .contentType("application/x-www-form-urlencoded")
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
        
        // Mock external API response
        mockServer
            .when(request()
                .withMethod("DELETE")
                .withPath(STORES_ENDPOINT + "/" + storeId))
            .respond(response()
                .withStatusCode(204));
        
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
        // Mock external API response
        mockServer
            .when(request()
                .withMethod("GET")
                .withPath(USERS_ENDPOINT))
            .respond(response()
                .withStatusCode(200)
                .withHeader(new Header("Content-Type", "application/json"))
                .withBody("[{\"email\":\"user@test.com\",\"name\":\"Test User\"}]"));
        
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
        // Mock external API response
        mockServer
            .when(request()
                .withMethod("POST")
                .withPath(USERS_ENDPOINT))
            .respond(response()
                .withStatusCode(201)
                .withHeader(new Header("Content-Type", "application/json"))
                .withBody("{\"email\":\"external@test.com\",\"name\":\"External User\"}"));
        
        given()
            .contentType("application/x-www-form-urlencoded")
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
        String encodedEmail = "user%40test.com"; // URL encoded @
        
        // Mock external API response - MockServer matches URL-encoded paths
        mockServer
            .when(request()
                .withMethod("GET")
                .withPath(USERS_ENDPOINT + "/" + encodedEmail))
            .respond(response()
                .withStatusCode(200)
                .withHeader(new Header("Content-Type", "application/json"))
                .withBody("{\"email\":\"user@test.com\",\"name\":\"Test User\"}"));
        
        given()
        .when()
            .get(USERS_ENDPOINT + "/" + email) // RestAssured will URL-encode automatically
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
        String encodedEmail = "user%40test.com"; // URL encoded @
        
        // Mock external API response - MockServer matches URL-encoded paths
        mockServer
            .when(request()
                .withMethod("PUT")
                .withPath(USERS_ENDPOINT + "/" + encodedEmail))
            .respond(response()
                .withStatusCode(200)
                .withHeader(new Header("Content-Type", "application/json"))
                .withBody("{\"email\":\"user@test.com\",\"name\":\"Updated User Name\"}"));
        
        given()
            .contentType("application/x-www-form-urlencoded")
            .param("name", "Updated User Name")
        .when()
            .put(USERS_ENDPOINT + "/" + email) // RestAssured will URL-encode automatically
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
        // Mock external API response
        mockServer
            .when(request()
                .withMethod("GET")
                .withPath(STORES_ENDPOINT + "/nonexistent-store-id"))
            .respond(response()
                .withStatusCode(404));
        
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
        // Mock external API response
        mockServer
            .when(request()
                .withMethod("POST")
                .withPath(STORES_ENDPOINT))
            .respond(response()
                .withStatusCode(400));
        
        given()
            .contentType("application/x-www-form-urlencoded")
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

        // Mock 1: Create store
        mockServer
            .when(request()
                .withMethod("POST")
                .withPath(STORES_ENDPOINT))
            .respond(response()
                .withStatusCode(201)
                .withHeader(new Header("Content-Type", "application/json"))
                .withBody("{\"id\":\"lifecycle-test-store\",\"name\":\"Lifecycle Test Store\"}"));

        // 1. Create store
        given()
            .contentType("application/x-www-form-urlencoded")
            .param("storeId", storeId)
            .param("name", "Lifecycle Test Store")
            .param("address", "999 Lifecycle Ave")
        .when()
            .post(STORES_ENDPOINT)
        .then()
            .statusCode(201)
            .body("id", equalTo(storeId));

        // Mock 2: Retrieve store
        mockServer.reset();
        mockServer
            .when(request()
                .withMethod("GET")
                .withPath(STORES_ENDPOINT + "/" + storeId))
            .respond(response()
                .withStatusCode(200)
                .withHeader(new Header("Content-Type", "application/json"))
                .withBody("{\"id\":\"lifecycle-test-store\",\"name\":\"Lifecycle Test Store\"}"));

        // 2. Retrieve store
        given()
        .when()
            .get(STORES_ENDPOINT + "/" + storeId)
        .then()
            .statusCode(200)
            .body("id", equalTo(storeId));

        // Mock 3: Update store
        mockServer.reset();
        mockServer
            .when(request()
                .withMethod("PUT")
                .withPath(STORES_ENDPOINT + "/" + storeId))
            .respond(response()
                .withStatusCode(200)
                .withHeader(new Header("Content-Type", "application/json"))
                .withBody("{\"id\":\"lifecycle-test-store\",\"description\":\"Lifecycle Store Updated\"}"));

        // 3. Update store
        given()
            .contentType("application/x-www-form-urlencoded")
            .param("description", "Lifecycle Store Updated")
            .param("address", "999 Updated Lifecycle Ave")
        .when()
            .put(STORES_ENDPOINT + "/" + storeId)
        .then()
            .statusCode(200)
            .body("description", equalTo("Lifecycle Store Updated"));

        // Mock 4: Delete store
        mockServer.reset();
        mockServer
            .when(request()
                .withMethod("DELETE")
                .withPath(STORES_ENDPOINT + "/" + storeId))
            .respond(response()
                .withStatusCode(204));

        // 4. Delete store
        given()
        .when()
            .delete(STORES_ENDPOINT + "/" + storeId)
        .then()
            .statusCode(204);

        // Mock 5: Verify deletion (404)
        mockServer.reset();
        mockServer
            .when(request()
                .withMethod("GET")
                .withPath(STORES_ENDPOINT + "/" + storeId))
            .respond(response()
                .withStatusCode(404));

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
        // Mock external API response
        mockServer
            .when(request()
                .withMethod("GET")
                .withPath(STORES_ENDPOINT))
            .respond(response()
                .withStatusCode(200)
                .withHeader(new Header("Content-Type", "application/json"))
                .withBody("[]"));
        
        given()
        .when()
            .get(STORES_ENDPOINT)
        .then()
            .statusCode(200)
            .time(lessThan(5000L)); // Response should be under 5 seconds
    }
}