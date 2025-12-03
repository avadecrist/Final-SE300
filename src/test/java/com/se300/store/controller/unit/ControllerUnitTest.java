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
}
