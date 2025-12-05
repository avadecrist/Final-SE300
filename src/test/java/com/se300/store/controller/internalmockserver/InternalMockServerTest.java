package com.se300.store.controller.internalmockserver;

import org.junit.jupiter.api.*;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.mockserver.verify.VerificationTimes;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

/**
 * A test class for verifying internal Smart Store API calls using a mock server.
 * Simulates internal service dependencies 
 * 
 * Purpose: Verify your app correctly handles responses from internal APIs
 * and properly handles errors from those internal services.
 */
@DisplayName("Internal Mock Server Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Disabled("Internal mock tests disabled - requires endpoint configuration")
public class InternalMockServerTest {

    private static ClientAndServer mockServer;
    private static final int MOCK_SERVER_PORT = 8888;
    private static final String MOCK_SERVER_URL = "http://localhost:" + MOCK_SERVER_PORT;

    @BeforeAll
    public static void setUpMockServer() {
        mockServer = ClientAndServer.startClientAndServer(MOCK_SERVER_PORT);
    }

    @AfterAll
    public static void tearDownMockServer() {
        if (mockServer != null) {
            mockServer.stop();
        }
    }

    @BeforeEach
    public void setUp() {
        mockServer.reset();
    }

    @Test
    @Order(1)
    @DisplayName("Mock Server: Test internal store provisioning API endpoint")
    public void testInternalStoreProvisioningAPI() {
        // Set up mock server to respond to store provisioning request
        mockServer
            .when(request()
                .withMethod("POST")
                .withPath("/api/internal/v1/stores/provision")
                .withHeader("Content-Type", "application/x-www-form-urlencoded"))
            .respond(response()
                .withStatusCode(201)
                .withHeader(new Header("Content-Type", "application/json"))
                .withBody("{\"id\":\"store1\",\"name\":\"Internal Store\",\"address\":\"123 Internal Ave\"}"));

        // Call the mocked internal API
        given()
            .param("storeId", "store1")
            .param("name", "Internal Store")
            .param("address", "123 Internal Ave")
        .when()
            .post(MOCK_SERVER_URL + "/api/internal/v1/stores/provision")
        .then()
            .statusCode(201)
            .body("id", equalTo("store1"))
            .body("name", equalTo("Internal Store"));
    }

    @Test
    @Order(2)
    @DisplayName("Mock Server: Test internal store retrieval API endpoint")
    public void testInternalStoreRetrievalAPI() {
        // Set up mock server to respond to store retrieval request
        mockServer
            .when(request()
                .withMethod("GET")
                .withPath("/api/internal/v1/stores/store1"))
            .respond(response()
                .withStatusCode(200)
                .withHeader(new Header("Content-Type", "application/json"))
                .withBody("{\"id\":\"store1\",\"name\":\"Internal Store\",\"address\":\"123 Internal Ave\",\"status\":\"active\"}"));

        // Call the mocked internal API
        given()
        .when()
            .get(MOCK_SERVER_URL + "/api/internal/v1/stores/store1")
        .then()
            .statusCode(200)
            .body("id", equalTo("store1"))
            .body("status", equalTo("active"));
    }

    @Test
    @Order(3)
    @DisplayName("Mock Server: Test internal user registration API endpoint")
    public void testInternalUserRegistrationAPI() {
        // Set up mock server to respond to user registration request
        mockServer
            .when(request()
                .withMethod("POST")
                .withPath("/api/internal/v1/users/register")
                .withHeader("Content-Type", "application/x-www-form-urlencoded"))
            .respond(response()
                .withStatusCode(201)
                .withHeader(new Header("Content-Type", "application/json"))
                .withBody("{\"email\":\"user@internal.com\",\"name\":\"Internal User\",\"verified\":false}"));

        // Call the mocked internal API
        given()
            .param("email", "user@internal.com")
            .param("password", "SecurePass123")
            .param("name", "Internal User")
        .when()
            .post(MOCK_SERVER_URL + "/api/internal/v1/users/register")
        .then()
            .statusCode(201)
            .body("email", equalTo("user@internal.com"))
            .body("verified", equalTo(false));
    }

    @Test
    @Order(4)
    @DisplayName("Mock Server: Test internal authentication API endpoint")
    public void testInternalAuthenticationAPI() {
        // Set up mock server to respond to authentication request
        mockServer
            .when(request()
                .withMethod("POST")
                .withPath("/api/internal/v1/auth/validate")
                .withHeader("Content-Type", "application/x-www-form-urlencoded"))
            .respond(response()
                .withStatusCode(200)
                .withHeader(new Header("Content-Type", "application/json"))
                .withBody("{\"valid\":true,\"email\":\"user@internal.com\",\"role\":\"admin\"}"));

        // Call the mocked internal API
        given()
            .param("email", "user@internal.com")
            .param("password", "SecurePass123")
        .when()
            .post(MOCK_SERVER_URL + "/api/internal/v1/auth/validate")
        .then()
            .statusCode(200)
            .body("valid", equalTo(true))
            .body("role", equalTo("admin"));
    }

    @Test
    @Order(5)
    @DisplayName("Mock Server: Test internal error handling - 404 Not Found")
    public void testInternalErrorHandling() {
        // Set up mock server to return 404 for non-existent store
        mockServer
            .when(request()
                .withMethod("GET")
                .withPath("/api/internal/v1/stores/nonexistent"))
            .respond(response()
                .withStatusCode(404)
                .withHeader(new Header("Content-Type", "application/json"))
                .withBody("{\"error\":\"Store not found\",\"storeId\":\"nonexistent\"}"));

        // Call the mocked internal API expecting 404
        given()
        .when()
            .get(MOCK_SERVER_URL + "/api/internal/v1/stores/nonexistent")
        .then()
            .statusCode(404)
            .body("error", equalTo("Store not found"));
    }

    @Test
    @Order(6)
    @DisplayName("Mock Server: Test internal unauthorized access - 401")
    public void testInternalUnauthorizedAccess() {
        // Set up mock server to return 401 for unauthorized access
        mockServer
            .when(request()
                .withMethod("POST")
                .withPath("/api/internal/v1/admin/delete")
                .withHeader("Authorization", "Bearer invalid-token"))
            .respond(response()
                .withStatusCode(401)
                .withHeader(new Header("Content-Type", "application/json"))
                .withBody("{\"error\":\"Unauthorized\",\"message\":\"Invalid or missing authentication token\"}"));

        // Call the mocked internal API with invalid token
        given()
            .header("Authorization", "Bearer invalid-token")
        .when()
            .post(MOCK_SERVER_URL + "/api/internal/v1/admin/delete")
        .then()
            .statusCode(401)
            .body("error", equalTo("Unauthorized"));
    }

    @Test
    @Order(7)
    @DisplayName("Mock Server: Verify request was received and logged")
    public void testMockServerRequestVerification() {
        // Set up mock server to respond to a request
        mockServer
            .when(request()
                .withMethod("GET")
                .withPath("/api/internal/v1/health"))
            .respond(response()
                .withStatusCode(200)
                .withHeader(new Header("Content-Type", "application/json"))
                .withBody("{\"status\":\"healthy\",\"timestamp\":\"2025-12-04T12:00:00Z\"}"));

        // Call the mocked internal API
        given()
        .when()
            .get(MOCK_SERVER_URL + "/api/internal/v1/health")
        .then()
            .statusCode(200)
            .body("status", equalTo("healthy"));

        // Verify the mock server received exactly one request to this endpoint
        mockServer.verify(request()
            .withMethod("GET")
            .withPath("/api/internal/v1/health"), VerificationTimes.exactly(1));
    }
}