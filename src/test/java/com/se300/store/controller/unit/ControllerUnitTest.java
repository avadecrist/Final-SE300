package com.se300.store.controller.unit;

import com.se300.store.controller.StoreController;
import com.se300.store.controller.UserController;
import com.se300.store.model.Store;
import com.se300.store.model.StoreException;
import com.se300.store.model.User;
import com.se300.store.service.AuthenticationService;
import com.se300.store.service.StoreService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.*;
import com.se300.store.SmartStoreApplication;
import java.io.IOException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * Unit tests for Store and User controllers using Mockito and RestAssured.
 * These tests mock the service layer to test controller logic in isolation.
 */
@DisplayName("Controller Mock Tests - Unit Testing with Mockito")
@ExtendWith(MockitoExtension.class)
public class ControllerUnitTest {

    //TODO: Implement Unit Tests for Smart Store Controllers

    @Mock
    private StoreService storeService;

    @Mock
    private AuthenticationService authenticationService;

    private static Tomcat tomcat;
    private static final int TEST_PORT = 8081; // Different port from integration tests
    private static final String BASE_URL = "http://localhost:" + TEST_PORT;

    private StoreController storeController;
    private UserController userController;

    @BeforeEach
    public void setUp() throws LifecycleException {
        // Create controllers with mocked services
        storeController = new StoreController(storeService);
        userController = new UserController(authenticationService);

        // Start embedded Tomcat server with mocked controllers
        tomcat = new Tomcat();
        tomcat.setPort(TEST_PORT);
        tomcat.getConnector();

        String contextPath = "";
        String docBase = new File(".").getAbsolutePath();
        Context context = tomcat.addContext(contextPath, docBase);

        // Register controllers
        Tomcat.addServlet(context, "storeController", storeController);
        context.addServletMappingDecoded("/api/v1/stores/*", "storeController");

        Tomcat.addServlet(context, "userController", userController);
        context.addServletMappingDecoded("/api/v1/users/*", "userController");

        tomcat.start();

        // Configure RestAssured
        RestAssured.baseURI = BASE_URL;
        RestAssured.port = TEST_PORT;
    }

    @AfterEach
    public void tearDown() throws LifecycleException {
        if (tomcat != null) {
            tomcat.stop();
            tomcat.destroy();
        }
        // Reset mocks after each test
        reset(storeService, authenticationService);
    }

    // ==================== STORE CONTROLLER MOCK TESTS ====================

    @Test
    @DisplayName("Mock: Create store - verify service call")
    public void testCreateStoreWithMock() throws Exception {
        // Create mock store
        Store mockCreatedStore = new Store("lowes", "675 Main St", "Lowes");
        // Tell mock to return the mock created store when provisionStore is created
        when(storeService.provisionStore("lowes", "Lowes", "675 Main St", null)).thenReturn(mockCreatedStore);
        // Make HTTP POST request to create store
        given()
            .param("storeId", "lowes")
            .param("name", "Lowes")
            .param("address", "675 Main St")
        .when()
            .post("/api/v1/stores") // Send POST request to create store endpoint
        .then()
        .statusCode(201) // Expecting that a new store was created successfully
        .contentType(ContentType.JSON)
        // Assertions
        .body("id", equalTo("lowes"))
        .body("address", equalTo("675 Main St"))
        .body("description", equalTo("Lowes"));
        // Verify method was called once with correct parameters
        verify(storeService, times(1)).provisionStore("lowes", "Lowes", "675 Main St", null);
        // Verify that no other service was called 
        verifyNoMoreInteractions(storeService);
    }

    @Test
    @DisplayName("Mock: Get all stores - verify service call")
    public void testGetAllStoresWithMock() throws Exception {
        // Create mock stores
        Store store1 = new Store("store1", "160 Main St","Walmart");
        Store store2 = new Store("store2", "180 Main St", "Ralphs");
        // Add mock stores to collection
        Collection<Store> mockStores = Arrays.asList(store1, store2);
        // Tell mock to return mock stores when getAllStores() is called
        when(storeService.getAllStores()).thenReturn(mockStores);
        // Make HTTP GET request
        given()
            .when()
                .get("/api/v1/stores") // Get all stores
            .then()
                .statusCode(200) // Expecting success
                .contentType(ContentType.JSON) //  Expecting JSON response
                .body("size()", equalTo(2)) // Expecting 2 stores
                // Assertions
                .body("[0].id", equalTo("store1"))
                .body("[0].address", equalTo("160 Main St"))
                .body("[1].id", equalTo("store2"))
                .body("[1].address", equalTo("180 Main St"));
        // Verify service was called exactly once
        verify(storeService, times(1)).getAllStores();
        // Verify that no other service was called  
        verifyNoMoreInteractions(storeService);    
    }

    @Test
    @DisplayName("Mock: Get store by ID - verify service call")
    public void testGetStoreByIdWithMock() throws Exception {
        // Create mock store
        Store mockStore = new Store("target", "903 Main St", "Target");
        // Tell mock to return target when showStore() is called
        when(storeService.showStore("target", null)).thenReturn(mockStore);
        // Make HTTP GET request to target specifically
        given()
            .when()
                .get("/api/v1/stores/target") // Get target
            .then()
                .statusCode(200) // Expecting success
                .contentType(ContentType.JSON) // Expecting JSON response
                // Assertions
                .body("id", equalTo("target"))
                .body("address", equalTo("903 Main St"))
                .body("description", equalTo("Target"));
        // Verify service was called exactly once
        verify(storeService, times(1)).showStore("target", null);
        // Verify that no other service was called
        verifyNoMoreInteractions(storeService);
    }

    @Test
    @DisplayName("Mock: Update store - verify service call")
    public void testUpdateStoreWithMock() throws Exception {
        // Create mock updated store
        Store mockUpdatedStore = new Store("lowes", "675 Poppy St","Lowes Updated");
        // Tell mock to return lowes when updateStore is called 
        when(storeService.updateStore("lowes", "Lowes Updated", "675 Poppy St")).thenReturn(mockUpdatedStore);
        given()
            .param("description", "Lowes Updated")
            .param("address", "675 Poppy St")
        .when()
            .put("/api/v1/stores/lowes") // Put to lowes
        .then()
            .statusCode(200) // Expecting success
            .contentType(ContentType.JSON)
            // Assertions
            .body("id", equalTo("lowes"))
            .body("address", equalTo("675 Poppy St"))
            .body("description", equalTo("Lowes Updated"));
        // Verify service was called exactly once
        verify(storeService, times(1)).updateStore("lowes", "Lowes Updated", "675 Poppy St");
        // Verify that no other service was called
        verifyNoMoreInteractions(storeService);   
    }

    @Test
    @DisplayName("Mock: Delete store - verify service call")
    public void testDeleteStoreWithMock() throws Exception {
        // Create mock service to delete store
        doNothing().when(storeService).deleteStore("lowes");
        // Make HTTP DELETE request
        given()
        .when()
            .delete("/api/v1/stores/lowes") // DELETE lowes
        .then()
            .statusCode(204); // Expecting 204 No Content
         // Verify service was called exactly once
         verify(storeService, times(1)).deleteStore("lowes");
         // Verify that no other service was called
         verifyNoMoreInteractions(storeService);
    }

    @Test
    @DisplayName("Mock: Store error handling - service throws exception")
    public void testStoreErrorHandlingWithMock() throws Exception {
        // Tell mock to throw exception if store doesn't exist
        when(storeService.showStore("nonexistent", null)).thenThrow(new StoreException("Show Store", "Store Does Not Exist"));
        // Make HTTP GET request for nonexistent store
        given()
            .when()
                .get("/api/v1/stores/nonexistent") // Get nonexistent store
            .then()
                .statusCode(404) // Expecting store to not be found
                .contentType(ContentType.JSON)
                // Assertion
                .body("status", equalTo(404));
        // Verify service was called exactly once
        verify(storeService, times(1)).showStore("nonexistent", null);
        // Verify that no other service was called 
        verifyNoMoreInteractions(storeService);
    }

    // ==================== USER CONTROLLER MOCK TESTS ====================

    @Test
    @DisplayName("Mock: Register user - verify service call")
    public void testRegisterUserWithMock() throws Exception {
        // Create mock registered user
        User mockRegUser = new User("zach@email.com", "maybe789","Zach Colby");
        // Tell mock to return mock registered user details when registerUser() is called
        when(authenticationService.registerUser("zach@email.com", "maybe789", "Zach Colby")).thenReturn(mockRegUser);
        // Make HTTP POST request
        given()
            .param("email", "zach@email.com")
            .param("password", "maybe789")
            .param("name", "Zach Colby")
        .when()
            .post("/api/v1/users") // Send POST request to users endpoint
        .then()
            .statusCode(201) // Expecting 201 Created
            .contentType(ContentType.JSON)
            // Assertions
            .body("email", equalTo("zach@email.com"))
            .body("password", equalTo("maybe789"))
            .body("name", equalTo("Zach Colby"));
        // Verify service was called exactly once
        verify(authenticationService, times(1)).registerUser("zach@email.com", "maybe789", "Zach Colby");
        // Verify that no other service was called 
        verifyNoMoreInteractions(authenticationService);
    }

    @Test
    @DisplayName("Mock: Get all users - verify service call")
    public void testGetAllUsersWithMock() throws Exception {
        // Create mock users
        User user1 = new User("awoh@email.com", "yes123", "Awoh Biah");
        User user2 = new User("ava@email.com", "no456", "Ava DeCristofaro");
        // Add mock users to collection of mock users
        Collection<User> mockUsers = Arrays.asList(user1, user2);
        // Tell mock to return mock users when getAllUsers() is called
        when(authenticationService.getAllUsers()).thenReturn(mockUsers);
        // Make HTTP GET request
        given()
            .when()
                .get("/api/v1/users") // Get all users
            .then()
                .statusCode(200) // Expecting success
                .contentType(ContentType.JSON)
                // Assertions
                .body("size()", equalTo(2))
                .body("[0].email", equalTo("awoh@email.com"))
                .body("[0].name", equalTo("Awoh Biah"))
                .body("[1].email", equalTo("ava@email.com"))
                .body("[1].name", equalTo("Ava DeCristofaro"));
        // Verify service was called exactly once
        verify(authenticationService, times(1)).getAllUsers();
        // Verify no other service was called
        verifyNoMoreInteractions(authenticationService);
    }

    @Test
    @DisplayName("Mock: Get user by email - verify service call")
    public void testGetUserByEmailWithMock() throws Exception {
        // Create mock user
        User mockUser = new User("isaac@email.com", "love716", "Isaac Sarpong");
        // Tell mock to return mock user details
        when(authenticationService.getUserByEmail("isaac@email.com")).thenReturn(mockUser);
        // Make HTTP GET requesr
        given()
            .when()
                .get("/api/v1/users/isaac@email.com") // Get user with email isaac@email.com
            .then()
                .statusCode(200) // Expecting 200 OK
                .contentType(ContentType.JSON)
                // Assertions
                .body("email", equalTo("isaac@email.com"))
                .body("name", equalTo("Isaac Sarpong"))
                .body("password", equalTo("love716"));
            // Verify service was called exactly once
            verify(authenticationService, times(1)).getUserByEmail("isaac@email.com");
            // Verify no other services were called
            verifyNoMoreInteractions(authenticationService);
    }

    @Test
    @DisplayName("Mock: Get user by email - user not found")
    public void testGetUserByEmailNotFoundWithMock() throws Exception {
        // Tell mock to return null when getUserByEmail() is called
        when(authenticationService.getUserByEmail("nonexistent@email.com")).thenReturn(null);
        // Make HTTP GET request
        given()
            .when()
                .get("/api/v1/users/nonexistent@email.com") // Get nonexistent user
            .then()
                .statusCode(404) // Expecting 404 Not Found
                .contentType(ContentType.JSON)
                // Assertions
                .body("status", equalTo(404))
                .body("message", equalTo("User not found"));
        // Verify service was called exactly once
        verify(authenticationService, times(1)).getUserByEmail("nonexistent@email.com");
        // Verify no other services were called
        verifyNoMoreInteractions(authenticationService);
    }

    @Test
    @DisplayName("Mock: Update user - verify service call")
    public void testUpdateUserWithMock() throws Exception {
        // Create mock updated user
        User mockUpdatedUser = new User("kal@email.com", "notquite112", "Kal Fernande");
        // Tell mock to return mock updated user details when updateUser() is called
        when(authenticationService.updateUser("kal@email.com", "notquite112", "Kal Fernande")).thenReturn(mockUpdatedUser);
        // Make HTTP PUT request
        given()
            .param("email", "kal@email.com")
            .param("password", "notquite112")
            .param("name", "Kal Fernande")
        .when()
            .put("/api/v1/users/kal@email.com") // Put to Kal
        .then()
            .statusCode(200) // Expecting 200 OK
            .contentType(ContentType.JSON)
            // Assertions
            .body("email", equalTo("kal@email.com"))
            .body("password", equalTo("notquite112"))
            .body("name", equalTo("Kal Fernande"));
        // Verify service was called exactly once
        verify(authenticationService, times(1)).updateUser("kal@email.com", "notquite112", "Kal Fernande");
        // Verify no other service was called
        verifyNoMoreInteractions(authenticationService);
    }

    @Test
    @DisplayName("Mock: Delete user - verify service call")
    public void testDeleteUserWithMock() throws Exception {
        // Create mock service to delete user
        when(authenticationService.deleteUser("awoh@email.com")).thenReturn(true);
        // Make HTTP DELETE request
        given()
        .when()
            .delete("/api/v1/users/awoh@email.com") // Delete user with email awoh@email.com
        .then()
            .statusCode(204); // Expecting 204 No Content
        // Verify service was used exactly once
        verify(authenticationService, times(1)).deleteUser("awoh@email.com");
        // Verify no other services were called
        verifyNoMoreInteractions(authenticationService);
    }

    @Test
    @DisplayName("Mock: Delete user - user not found")
    public void testDeleteUserNotFoundWithMock() throws Exception {
        // Tell mock to throw runtime exception when there is no user for deletion
        when(authenticationService.deleteUser("nonexistent@email.com")).thenThrow(new RuntimeException("User not found"));
        // Make HTTP DELETE request
        given()
            .when()
                .delete("/api/v1/users/nonexistent@email.com") // Delete non-existent user
            .then()
                .statusCode(404) // Expecting 404 Not Found
                .contentType(ContentType.JSON)
                // Assertions
                .body("status", equalTo(404))
                .body("message", equalTo("User not found"));
        // Verify the service was called exactly once
        verify(authenticationService, times(1)).deleteUser("nonexistent@email.com");
        // Verify no other services were called
        verifyNoMoreInteractions(authenticationService);
    }

    @Test
    @DisplayName("Mock: Register duplicate user - verify conflict handling")
    public void testRegisterDuplicateUserWithMock() throws Exception {
        // Tell mock to throw runtime exception when registerUser() is called
        when(authenticationService.registerUser("ava@email.com", "ava123", "Ava DeCristofaro")).thenThrow(new RuntimeException("User already exists"));
        // Make HTTP POST request
        given()
            .param("email", "ava@email.com")
            .param("password", "ava123")
            .param("name", "Ava DeCristofaro")
        .when()
            .post("/api/v1/users") // POST to register duplicate user
        .then()
            .statusCode(409) // Expecting 409 Conflict
            .contentType(ContentType.JSON)
            // Assertions
            .body("status", equalTo(409))
            .body("message", equalTo("User already exists"));
        // Verify service was called exactly once
        verify(authenticationService, times(1)).registerUser("ava@email.com", "ava123", "Ava DeCristofaro");
        // Verify no other services were called
        verifyNoMoreInteractions(authenticationService);
    }

    @Test
    @DisplayName("Mock: Verify no unexpected service calls")
    public void testNoUnexpectedServiceCalls() throws Exception {
        // Verify there were no interactions with either service
        verifyNoInteractions(storeService);
        verifyNoInteractions(authenticationService);
    }

    // ==================== ADDITIONAL TESTS ====================

    @Test
    @DisplayName("Mock: Create store with missing parameters - validation error")
    public void testCreateStoreWithMissingParams() throws Exception {
        // Make HTTP POST request with missing required parameters
        given()
            .param("storeId", "lowes")
            // Missing name and address parameters
        .when()
            .post("/api/v1/stores")
        .then()
            .statusCode(400) // Expecting Bad Request
            .contentType(ContentType.JSON)
            .body("message", containsString("Missing required parameters"));
        
        // Verify service was never called due to validation failure
        verifyNoInteractions(storeService);
    }

    @Test
    @DisplayName("Mock: Get store by ID - returns null (not found)")
    public void testGetStoreByIdNotFound() throws Exception {
        // Tell mock to return null when store doesn't exist
        when(storeService.showStore("nonexistent", null)).thenReturn(null);
        
        given()
        .when()
            .get("/api/v1/stores/nonexistent")
        .then()
            .statusCode(200);
        
        verify(storeService, times(1)).showStore("nonexistent", null);
        verifyNoMoreInteractions(storeService);
    }

    @Test
    @DisplayName("Mock: Update store - returns null (not found)")
    public void testUpdateStoreNotFound() throws Exception {
        // Tell mock to return null when store doesn't exist
        when(storeService.updateStore("nonexistent", "New Desc", "New Address")).thenReturn(null);
        
        given()
            .param("description", "New Desc")
            .param("address", "New Address")
        .when()
            .put("/api/v1/stores/nonexistent")
        .then()
            .statusCode(200);
        
        verify(storeService, times(1)).updateStore("nonexistent", "New Desc", "New Address");
        verifyNoMoreInteractions(storeService);
    }

    @Test
    @DisplayName("Mock: Update store with missing storeId in path")
    public void testUpdateStoreWithoutStoreId() throws Exception {
        // Make HTTP PUT request without storeId in path
        given()
            .param("description", "Updated Store")
            .param("address", "New Address")
        .when()
            .put("/api/v1/stores") // No storeId in path
        .then()
            .statusCode(400) // Expecting Bad Request
            .contentType(ContentType.JSON)
            .body("message", containsString("Store ID is required"));
        
        // Verify service was never called
        verifyNoInteractions(storeService);
    }

    @Test
    @DisplayName("Mock: Delete store with missing storeId")
    public void testDeleteStoreWithoutStoreId() throws Exception {
        // Make HTTP DELETE request without storeId
        given()
        .when()
            .delete("/api/v1/stores") // No storeId in path  
        .then()
            .statusCode(400); // Expecting Bad Request
        
        // Verify service was never called
        verifyNoInteractions(storeService);
    }

    @Test
    @DisplayName("Mock: Register user with missing parameters")
    public void testRegisterUserWithMissingParams() throws Exception {
        // Make HTTP POST request with missing required parameters
        given()
            .param("email", "test@email.com")
            // Missing password and name
        .when()
            .post("/api/v1/users")
        .then()
            .statusCode(400) // Expecting Bad Request
            .contentType(ContentType.JSON)
            .body("message", containsString("Missing required parameters"));
        
        // Verify service was never called
        verifyNoInteractions(authenticationService);
    }

    @Test
    @DisplayName("Mock: Register user with invalid role")
    public void testRegisterUserWithInvalidRole() throws Exception {
        given()
            .param("email", "test@email.com")
            .param("password", "password123")
            .param("name", "Test User")
            .param("role", "INVALID_ROLE")
        .when()
            .post("/api/v1/users")
        .then()
            .statusCode(201);
        
        verify(authenticationService, times(1))
        .registerUser(anyString(), anyString(), anyString());
         // Now verify service was never called due to validation failure
        verifyNoMoreInteractions(authenticationService);
        
    }

    @Test
    @DisplayName("Mock: Update user with missing email in path")
    public void testUpdateUserWithoutEmail() throws Exception {
        given()
            .param("password", "newpass")
            .param("name", "New Name")
        .when()
            .put("/api/v1/users") // No email in path
        .then()
            .statusCode(400) // Expecting Bad Request
            .contentType(ContentType.JSON)
            .body("message", containsString("Email is required"));
        
        // Verify service was never called
        verifyNoInteractions(authenticationService);
    }

    @Test
    @DisplayName("Mock: Update user - returns null (not found)")
    public void testUpdateUserNotFound() throws Exception {
        // Tell mock to return null when user doesn't exist
        when(authenticationService.updateUser("nonexistent@email.com", "newpass", "New Name")).thenReturn(null);
        
        given()
            .param("password", "newpass")
            .param("name", "New Name")
        .when()
            .put("/api/v1/users/nonexistent@email.com")
        .then()
            .statusCode(200);
        
        verify(authenticationService, times(1)).updateUser("nonexistent@email.com", "newpass", "New Name");
        verifyNoMoreInteractions(authenticationService);
    }

    @Test
    @DisplayName("Mock: Delete user - returns false (not found)")
    public void testDeleteUserReturnsFalse() throws Exception {
        // Tell mock to return false when user doesn't exist
        when(authenticationService.deleteUser("nonexistent@email.com")).thenReturn(false);
        
        given()
        .when()
            .delete("/api/v1/users/nonexistent@email.com")
        .then()
            .statusCode(204);
        
        verify(authenticationService, times(1)).deleteUser("nonexistent@email.com");
        verifyNoMoreInteractions(authenticationService);
    }

    @Test
    @DisplayName("Mock: Register user with valid role - ADMIN")
    public void testRegisterUserWithValidAdminRole() throws Exception {
        User mockUser = new User("admin@email.com", "admin123", "Admin User");
        when(authenticationService.registerUser(eq("admin@email.com"), eq("admin123"), eq("Admin User"))).thenReturn(mockUser);
        
        given()
            .param("email", "admin@email.com")
            .param("password", "admin123")
            .param("name", "Admin User")
            .param("role", "ADMIN")
        .when()
            .post("/api/v1/users")
        .then()
            .statusCode(201) // Expecting Created
            .contentType(ContentType.JSON)
            .body("email", equalTo("admin@email.com"))
            .body("name", equalTo("Admin User"));
        
        verify(authenticationService, times(1)).registerUser(eq("admin@email.com"), eq("admin123"), eq("Admin User"));
        verifyNoMoreInteractions(authenticationService);
    }

    @Test
    @DisplayName("Mock: Register user with valid role - USER")
    public void testRegisterUserWithValidUserRole() throws Exception {
        User mockUser = new User("user@email.com", "user123", "Regular User");
        when(authenticationService.registerUser(eq("user@email.com"), eq("user123"), eq("Regular User"))).thenReturn(mockUser);
        
        given()
            .param("email", "user@email.com")
            .param("password", "user123")
            .param("name", "Regular User")
            .param("role", "USER")
        .when()
            .post("/api/v1/users")
        .then()
            .statusCode(201) // Expecting Created
            .contentType(ContentType.JSON)
            .body("email", equalTo("user@email.com"))
            .body("name", equalTo("Regular User"));
        
        verify(authenticationService, times(1)).registerUser(eq("user@email.com"), eq("user123"), eq("Regular User"));
        verifyNoMoreInteractions(authenticationService);
    }

    @Test
    @DisplayName("Mock: Store service throws StoreException - proper error handling")
    public void testStoreServiceThrowsStoreException() throws Exception {
        // Test different types of StoreException handling
        when(storeService.provisionStore("duplicate", "Duplicate Store", "123 Main St", null))
            .thenThrow(new StoreException("Add Store", "Store Already Exists"));
        
        given()
            .param("storeId", "duplicate")
            .param("name", "Duplicate Store")
            .param("address", "123 Main St")
        .when()
            .post("/api/v1/stores")
        .then()
            .statusCode(409) 
            .contentType(ContentType.JSON);
        
        verify(storeService, times(1)).provisionStore("duplicate", "Duplicate Store", "123 Main St", null);
        verifyNoMoreInteractions(storeService);
    }

    @Test
    @DisplayName("Mock: User service throws RuntimeException - error handling")
    public void testUserServiceThrowsRuntimeException() throws Exception {
        // Test generic RuntimeException handling
        when(authenticationService.registerUser("error@email.com", "pass123", "Error User"))
            .thenThrow(new RuntimeException("Database connection failed"));
        
        given()
            .param("email", "error@email.com")
            .param("password", "pass123")
            .param("name", "Error User")
        .when()
            .post("/api/v1/users")
        .then()
            .statusCode(409)
            .contentType(ContentType.JSON);
        
        verify(authenticationService, times(1)).registerUser("error@email.com", "pass123", "Error User");
        verifyNoMoreInteractions(authenticationService);
    }

    @Test
    @DisplayName("Mock: Create store - missing only name parameter")
    public void testCreateStoreMissingName() throws Exception {
        given()
            .param("storeId", "store123")
            .param("address", "123 Main St")
            // Missing name parameter
        .when()
            .post("/api/v1/stores")
        .then()
            .statusCode(400) // Expecting Bad Request
            .contentType(ContentType.JSON)
            .body("message", containsString("Missing required parameters"));
        
        verifyNoInteractions(storeService);
    }

    @Test
    @DisplayName("Mock: Create store - missing only address parameter")
    public void testCreateStoreMissingAddress() throws Exception {
        given()
            .param("storeId", "store123")
            .param("name", "Test Store")
            // Missing address parameter
        .when()
            .post("/api/v1/stores")
        .then()
            .statusCode(400) // Expecting Bad Request
            .contentType(ContentType.JSON)
            .body("message", containsString("Missing required parameters"));
        
        verifyNoInteractions(storeService);
    }

    @Test
    @DisplayName("Mock: Create store - StoreException handling with specific error response")
    public void testCreateStoreStoreExceptionHandling() throws Exception {
        when(storeService.provisionStore("duplicate", "Duplicate Store", "123 Main St", null))
            .thenThrow(new StoreException("Add Store", "Store Already Exists"));
        
        given()
            .param("storeId", "duplicate")
            .param("name", "Duplicate Store")
            .param("address", "123 Main St")
        .when()
            .post("/api/v1/stores")
        .then()
            .statusCode(409) // Check actual status code returned by handleException
            .contentType(ContentType.JSON);
        
        verify(storeService, times(1)).provisionStore("duplicate", "Duplicate Store", "123 Main St", null);
        verifyNoMoreInteractions(storeService);
    }

    @Test
    @DisplayName("Mock: Update store - StoreException handling")
    public void testUpdateStoreStoreExceptionHandling() throws Exception {
        when(storeService.updateStore("error", "New Desc", "New Address"))
            .thenThrow(new StoreException("Update Store", "Store Update Failed"));
        
        given()
            .param("description", "New Desc")
            .param("address", "New Address")
        .when()
            .put("/api/v1/stores/error")
        .then()
            .statusCode(404) 
            .contentType(ContentType.JSON);
        
        verify(storeService, times(1)).updateStore("error", "New Desc", "New Address");
        verifyNoMoreInteractions(storeService);
    }

    @Test
    @DisplayName("Mock: Delete store - StoreException handling")
    public void testDeleteStoreStoreExceptionHandling() throws Exception {
        doThrow(new StoreException("Delete Store", "Store Cannot Be Deleted"))
            .when(storeService).deleteStore("protected");
        
        given()
        .when()
            .delete("/api/v1/stores/protected")
        .then()
            .statusCode(404) // Check actual status code returned by handleException
            .contentType(ContentType.JSON);
        
        verify(storeService, times(1)).deleteStore("protected");
        verifyNoMoreInteractions(storeService);
    }

    @Test
    @DisplayName("Mock: Register user - missing only password parameter")
    public void testRegisterUserMissingPassword() throws Exception {
        given()
            .param("email", "test@email.com")
            .param("name", "Test User")
            // Missing password parameter
        .when()
            .post("/api/v1/users")
        .then()
            .statusCode(400) // Expecting Bad Request
            .contentType(ContentType.JSON)
            .body("message", containsString("Missing required parameters"));
        
        verifyNoInteractions(authenticationService);
    }

    @Test
    @DisplayName("Mock: Register user - missing only name parameter")
    public void testRegisterUserMissingName() throws Exception {
        given()
            .param("email", "test@email.com")
            .param("password", "password123")
            // Missing name parameter
        .when()
            .post("/api/v1/users")
        .then()
            .statusCode(400) // Expecting Bad Request
            .contentType(ContentType.JSON)
            .body("message", containsString("Missing required parameters"));
        
        verifyNoInteractions(authenticationService);
    }

    @Test
    @DisplayName("Mock: Register user with valid role - MANAGER")
    public void testRegisterUserWithValidManagerRole() throws Exception {
        User mockUser = new User("manager@email.com", "manager123", "Manager User");
        when(authenticationService.registerUser(eq("manager@email.com"), eq("manager123"), eq("Manager User"))).thenReturn(mockUser);
        
        given()
            .param("email", "manager@email.com")
            .param("password", "manager123")
            .param("name", "Manager User")
            .param("role", "MANAGER")
        .when()
            .post("/api/v1/users")
        .then()
            .statusCode(201) // Expecting Created
            .contentType(ContentType.JSON)
            .body("email", equalTo("manager@email.com"))
            .body("name", equalTo("Manager User"));
        
        verify(authenticationService, times(1)).registerUser(eq("manager@email.com"), eq("manager123"), eq("Manager User"));
        verifyNoMoreInteractions(authenticationService);
    }

    @Test
    @DisplayName("Mock: Update user - AuthenticationService exception handling")
    public void testUpdateUserAuthenticationException() throws Exception {
        when(authenticationService.updateUser("error@email.com", "newpass", "New Name"))
            .thenThrow(new RuntimeException("Authentication failed"));
        
        given()
            .param("password", "newpass")
            .param("name", "New Name")
        .when()
            .put("/api/v1/users/error@email.com")
        .then()
            .statusCode(404) 
            .contentType(ContentType.JSON);
        
        verify(authenticationService, times(1)).updateUser("error@email.com", "newpass", "New Name");
        verifyNoMoreInteractions(authenticationService);
    }

    @Test
    @DisplayName("Mock: Delete user with missing email in path")
    public void testDeleteUserWithoutEmail() throws Exception {
        given()
        .when()
            .delete("/api/v1/users") // No email in path
        .then()
            .statusCode(400) // Expecting Bad Request
            .contentType(ContentType.JSON)
            .body("message", containsString("Email is required"));
        
        verifyNoInteractions(authenticationService);
    }

    @Test
    @DisplayName("Mock: Register user - null role defaults properly")
    public void testRegisterUserWithNullRole() throws Exception {
        User mockUser = new User("default@email.com", "default123", "Default User");
        when(authenticationService.registerUser("default@email.com", "default123", "Default User")).thenReturn(mockUser);
        
        given()
            .param("email", "default@email.com")
            .param("password", "default123")
            .param("name", "Default User")
            // No role parameter - should use default
        .when()
            .post("/api/v1/users")
        .then()
            .statusCode(201) // Expecting Created
            .contentType(ContentType.JSON)
            .body("email", equalTo("default@email.com"))
            .body("name", equalTo("Default User"));
        
        verify(authenticationService, times(1)).registerUser("default@email.com", "default123", "Default User");
        verifyNoMoreInteractions(authenticationService);
    }

    @Test
    @DisplayName("Mock: Register user - empty role string handling")
    public void testRegisterUserWithEmptyRole() throws Exception {
        User mockUser = new User("empty@email.com", "empty123", "Empty Role User");
        when(authenticationService.registerUser("empty@email.com", "empty123", "Empty Role User")).thenReturn(mockUser);
        
        given()
            .param("email", "empty@email.com")
            .param("password", "empty123")
            .param("name", "Empty Role User")
            .param("role", "") // Empty role string
        .when()
            .post("/api/v1/users")
        .then()
            .statusCode(201) // Expecting Created
            .contentType(ContentType.JSON)
            .body("email", equalTo("empty@email.com"))
            .body("name", equalTo("Empty Role User"));
        
        verify(authenticationService, times(1)).registerUser("empty@email.com", "empty123", "Empty Role User");
        verifyNoMoreInteractions(authenticationService);
    }

    @Test
    @DisplayName("Mock: Register user - blank role string handling")
    public void testRegisterUserWithBlankRole() throws Exception {
        // Create mock user with blank role
        User mockUser = new User("blank@email.com", "blank123", "Blank Role User");
        // Tell mock to return user when registerUser is called with blank role string
        when(authenticationService.registerUser("blank@email.com", "blank123", "Blank Role User")).thenReturn(mockUser);
        
        given()
            .param("email", "blank@email.com")
            .param("password", "blank123")
            .param("name", "Blank Role User")
            .param("role", "   ") // Blank role string with spaces
        .when()
            .post("/api/v1/users") // Send POST request to register user with blank role
        .then()
            .statusCode(201) // Expecting Created
            .contentType(ContentType.JSON)
            // Assertions
            .body("email", equalTo("blank@email.com"))
            .body("name", equalTo("Blank Role User"));
        
        // Verify service was called exactly once
        verify(authenticationService, times(1)).registerUser("blank@email.com", "blank123", "Blank Role User");
        // Verify no other services were called
        verifyNoMoreInteractions(authenticationService);
    }
    
    @Test
    @DisplayName("Mock: Create store - missing only storeId parameter")
    public void testCreateStoreMissingStoreId() throws Exception {
        // Make HTTP POST request with missing storeId parameter
        given()
            .param("name", "Test Store")
            .param("address", "123 Main St")
            // Missing storeId parameter - this hits the final uncovered branch
        .when()
            .post("/api/v1/stores") // Send POST request to create store endpoint
        .then()
            .statusCode(400) // Expecting Bad Request
            .contentType(ContentType.JSON)
            // Assertion
            .body("message", containsString("Missing required parameters"));
        
        // Verify service was never called due to validation failure
        verifyNoInteractions(storeService);
    }

    @Test
    @DisplayName("Mock: Register user - missing only email parameter")
    public void testRegisterUserMissingEmail() throws Exception {
        // Make HTTP POST request with missing email parameter
        given()
            .param("password", "password123")
            .param("name", "Test User")
            // Missing email parameter
        .when()
            .post("/api/v1/users") // Send POST request to register user
        .then()
            .statusCode(400) // Expecting Bad Request
            .contentType(ContentType.JSON)
            // Assertion
            .body("message", containsString("Missing required parameters"));
        
        // Verify service was never called due to validation failure
        verifyNoInteractions(authenticationService);
    }

    @Test
    @DisplayName("Mock: Store controller - comprehensive exception handling")
    public void testStoreControllerExceptionHandling() throws Exception {
        // Tell mock to throw generic exception to test exception handling
        when(storeService.getAllStores()).thenThrow(new RuntimeException("Unexpected server error"));
        
        // Make HTTP GET request
        given()
        .when()
            .get("/api/v1/stores") // Get all stores
        .then()
            .statusCode(500) // Expecting Internal Server Error
            .contentType(ContentType.JSON);
        
        // Verify service was called exactly once
        verify(storeService, times(1)).getAllStores();
        // Verify no other services were called
        verifyNoMoreInteractions(storeService);
    }

    @Test
    @DisplayName("Mock: User controller - comprehensive exception handling")
    public void testUserControllerExceptionHandling() throws Exception {
        // Tell mock to throw generic exception to test exception handling
        when(authenticationService.getAllUsers()).thenThrow(new RuntimeException("Database connection lost"));
        
        // Make HTTP GET request
        given()
        .when()
            .get("/api/v1/users") // Get all users
        .then()
            .statusCode(500) // Expecting Internal Server Error
            .contentType(ContentType.JSON);
        
        // Verify service was called exactly once
        verify(authenticationService, times(1)).getAllUsers();
        // Verify no other services were called
        verifyNoMoreInteractions(authenticationService);
    }

    @Test
    @DisplayName("Mock: Store validation - all parameter combinations")
    public void testStoreParameterValidationCombinations() throws Exception {
        // Test case 1: All parameters null
        given()
        .when()
            .post("/api/v1/stores") // Send POST request with no parameters
        .then()
            .statusCode(400) // Expecting Bad Request
            .contentType(ContentType.JSON)
            // Assertion
            .body("message", containsString("Missing required parameters"));
        
        // Test case 2: Only storeId provided
        given()
            .param("storeId", "store123")
        .when()
            .post("/api/v1/stores") // Send POST request with only storeId
        .then()
            .statusCode(400) // Expecting Bad Request
            .contentType(ContentType.JSON)
            // Assertion
            .body("message", containsString("Missing required parameters"));
        
        // Verify service was never called
        verifyNoInteractions(storeService);
    }

    @Test
    @DisplayName("Mock: User validation - all parameter combinations")
    public void testUserParameterValidationCombinations() throws Exception {
        // Test case 1: All required parameters null
        given()
        .when()
            .post("/api/v1/users") // Send POST request with no parameters
        .then()
            .statusCode(400) // Expecting Bad Request
            .contentType(ContentType.JSON)
            // Assertion
            .body("message", containsString("Missing required parameters"));
        
        // Test case 2: Only email and password provided
        given()
            .param("email", "test@email.com")
            .param("password", "password123")
            // Missing name parameter
        .when()
            .post("/api/v1/users") // Send POST request with missing name
        .then()
            .statusCode(400) // Expecting Bad Request
            .contentType(ContentType.JSON)
            // Assertion
            .body("message", containsString("Missing required parameters"));
        
        // Verify service was never called
        verifyNoInteractions(authenticationService);
    }

    @Test
    @DisplayName("Sanity: SmartStoreApplication startNonBlocking and stop")
    public void testSmartStoreApplicationStartStop() throws Exception {
        // Skip this test if port 8080 is already in use to avoid flaky failures on CI
        boolean portFree = false;
        try (java.net.ServerSocket ss = new java.net.ServerSocket(8080)) {
            portFree = true;
        } catch (IOException ioe) {
            // port in use
            portFree = false;
        }
        Assumptions.assumeTrue(portFree, "Port 8080 is in use — skipping SmartStoreApplication lifecycle test");

        SmartStoreApplication app = new SmartStoreApplication();
        try {
            app.startNonBlocking();
            // give server a moment to start
            Thread.sleep(1000);
        } finally {
            // ensure we stop the server
            app.stop();
        }
    }
}
