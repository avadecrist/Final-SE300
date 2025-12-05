package com.se300.store.repository.unit;

import com.se300.store.data.DataManager;
import com.se300.store.model.Store;
import com.se300.store.model.User;
import com.se300.store.repository.StoreRepository;
import com.se300.store.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the Repository classes including StoreRepository and UserRepository.
 * This test class uses JUnit 5 and Mockito frameworks to verify the expected behavior
 * of the repository operations with mocked dependencies.
 */
@DisplayName("Repository Unit Tests")
@ExtendWith(MockitoExtension.class)
public class RepositoryUnitTest {

    //TODO: Implement Unit Tests for the Smart Store Repositories

    @Mock
    private DataManager dataManager;

    private StoreRepository storeRepository;
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        // Initialize repositories with mocked DataManager
        storeRepository = new StoreRepository(dataManager);
        userRepository = new UserRepository(dataManager);
    }

    @Test
    @DisplayName("Test StoreRepository save with mocked DataManager")
    public void testStoreRepositorySave() {
        //ARRANGE
        Map<String, Store> stores = new HashMap<>();
        when(dataManager.get("stores")).thenReturn(stores);

        Store store = new Store("store-1", "123 Main St", "Test Store");

        //ACT
        storeRepository.save(store);

        //ASSERT
        assertEquals(1, stores.size(), "Store map size should be 1 after saving a store");
        assertTrue(stores.containsKey("store-1"), "Store map should contain the saved store ID");
        assertSame(store, stores.get("store-1"), "The saved store should match the original store object");
        verify(dataManager).put(eq("stores"), eq(stores));
    }

    @Test
    @DisplayName("Test StoreRepository findById with mocked DataManager")
    public void testStoreRepositoryFindById() {
        // ARRANGE
        Map<String, Store> stores = new HashMap<>();
        Store store = new Store("store-1", "123 Main St", "Test Store");
        stores.put("store-1", store);

        when(dataManager.get("stores")).thenReturn(stores);

        // ACT
        Optional<Store> existingResult = storeRepository.findById("store-1");
        Optional<Store> nonExistingResult = storeRepository.findById("missing-store");

        // ASSERT
            // Existing store
        assertTrue(existingResult.isPresent(), "Expected store to be present for existing ID");
        assertSame(store, existingResult.get(), "Returned store should match the stored instance");

            // Non-existing store
        assertTrue(nonExistingResult.isEmpty(), "Expected Optional to be empty for a non-existent store ID");

        verify(dataManager, atLeastOnce()).get("stores");
    }

    @Test
    @DisplayName("Test StoreRepository existsById with mocked DataManager")
    public void testStoreRepositoryExistsById() {
        // ARRANGE
        Map<String, Store> stores = new HashMap<>();
        Store store = new Store("store-1", "123 Main St", "Test Store");
        stores.put("store-1", store);

        when(dataManager.get("stores")).thenReturn(stores);

        // ACT
        boolean doesExist = storeRepository.existsById("store-1");
        boolean doesNotExist = storeRepository.existsById("missing-store");

        // ASSERT
        assertTrue(doesExist, "Should return true when ID is present in the stores map");
        assertFalse(doesNotExist, "Should return false when ID is not present in the stores map");

        verify(dataManager, atLeastOnce()).get("stores");
    }

    @Test
    @DisplayName("Test StoreRepository delete with mocked DataManager")
    public void testStoreRepositoryDelete() {
        // ARRANGE
        Map<String, Store> stores = new HashMap<>();
        Store store1 = new Store("store-1", "123 Main St", "I will be deleted");
        Store store2 = new Store("store-2", "456 Elm St", "I will stay");
        stores.put("store-1", store1);
        stores.put("store-2", store2);

        when(dataManager.get("stores")).thenReturn(stores);

        // ACT
            // Delete existing store
        storeRepository.delete("store-1");

        // ASSERT
            // Case 1: existing store removed
        assertFalse(stores.containsKey("store-1"), "Store should be removed from the map after delete()");
        assertTrue(stores.containsKey("store-2"), "Store should still contain the other store after delete()");
        assertEquals(1, stores.size(), "Stores map should have one store left after deleting one store");

        //RE-ACT
            // Delete non-existing store
        storeRepository.delete("missing-store");

        //RE-ASSERT
            // Case 2: deleting non-existing store should change nothing
        assertEquals(1, stores.size(), "Deleting a non-existent store should not modify the stores map");

        verify(dataManager, atLeastOnce()).put(eq("stores"), eq(stores)); // verify that dataManager.put was called for both delete operations
        verify(dataManager, atLeastOnce()).get("stores"); // verify that stores map was retrieved from DataManager when delete was called
    }

    @Test
    @DisplayName("Test StoreRepository findAll with mocked DataManager")
    public void testStoreRepositoryFindAll() {
        // ARRANGE
        Map<String, Store> stores = new HashMap<>();
        Store store1 = new Store("store-1", "123 Main St", "Store One");
        Store store2 = new Store("store-2", "456 Elm St", "Store Two");
        stores.put("store-1", store1);
        stores.put("store-2", store2);

        when(dataManager.get("stores")).thenReturn(stores);

        // ACT
        Map<String, Store> result = storeRepository.findAll();

        // ASSERT
            // confirm correct size and keys
        assertEquals(2, result.size(), "findAll should return all stores from the stores map");
        assertTrue(result.containsKey("store-1"), "Result map should contain store1 by ID");
        assertTrue(result.containsKey("store-2"), "Result map should contain store2 by ID");

            // values should be the same instances as stored in the original map
        assertSame(store1, result.get("store-1"), "Result should contain the same instance of store1");
        assertSame(store2, result.get("store-2"), "Result should contain the same instance of store2");

            // ensure method returns a copy rather than actual stores map
        assertNotSame(stores, result, "findAll should return a new map, not the original stores map");

        verify(dataManager).get("stores");
    }

    @Test
    @DisplayName("Test UserRepository save with mocked DataManager")
    public void testUserRepositorySave() {
        //ARRANGE
        Map<String, User> users = new HashMap<>();
        when(dataManager.get("users")).thenReturn(users);

        User user = new User("user@example.com", "password-123", "Test User");

        //ACT
        userRepository.save(user);

        //ASSERT
        assertEquals(1, users.size(), "User map size should be 1 after saving a user");
        assertTrue(users.containsKey("user@example.com"), "User map should contain the saved user email");
        assertSame(user, users.get("user@example.com"), "The saved user should match the original user object");
        verify(dataManager).put(eq("users"), eq(users));
    }

    @Test
    @DisplayName("Test UserRepository findByEmail with mocked DataManager")
    public void testUserRepositoryFindByEmail() {
        // ARRANGE
        Map<String, User> users = new HashMap<>();
        User user = new User("user@example.com", "password-123", "Test User");
        users.put("user@example.com", user);

        when(dataManager.get("users")).thenReturn(users);

        // ACT
            // checking for an existing email
        Optional<User> existingResult = userRepository.findByEmail("user@example.com");
            // checking for a non-existent email
        Optional<User> nonExistingResult = userRepository.findByEmail("missing-email@example.com");

        // ASSERT
        assertTrue(existingResult.isPresent(), "Expected user to be present for existing email");
        assertSame(user, existingResult.get(), "Returned user should be the same instance stored in the users map");

        assertTrue(nonExistingResult.isEmpty(), "Expected Optional to be empty for a non-existent email");
        verify(dataManager, times(2)).get("users"); //called twice: for user@example.com AND missing-email@example.com
    }

    @Test
    @DisplayName("Test UserRepository existsByEmail with mocked DataManager")
    public void testUserRepositoryExistsByEmail() {
        // ARRANGE
        Map<String, User> users = new HashMap<>();
        User user = new User("user@example.com", "password-123", "Test User");
        users.put("user@example.com", user);

        when(dataManager.get("users")).thenReturn(users);

        // ACT
        boolean doesExist = userRepository.existsByEmail("user@example.com");
        boolean doesNotExist = userRepository.existsByEmail("missing@example.com");

        // ASSERT
        assertTrue(doesExist, "existsByEmail should return true when email is in the users map");
        assertFalse(doesNotExist, "existsByEmail should return false when email is not in the users map");

        verify(dataManager, atLeastOnce()).get("users"); //called AT LEAST once: for user@example.com and missing-email@example.com
    }

    @Test
    @DisplayName("Test UserRepository delete with mocked DataManager")
    public void testUserRepositoryDelete() {
        // ARRANGE
        Map<String, User> users = new HashMap<>();
        User user1 = new User("user1@example.com", "password-123", "I will be deleted");
        User user2 = new User("user2@example.com", "password-456", "I will stay");
        users.put("user1@example.com", user1);
        users.put("user2@example.com", user2);

        when(dataManager.get("users")).thenReturn(users);

        // ACT
            // Delete existing user
        userRepository.delete("user1@example.com");

        // ASSERT
            // Case 1: existing user removed
        assertFalse(users.containsKey("user1@example.com"), "User should be removed from the map after delete()");
        assertEquals(1, users.size(), "Users map should have one user left after deleting one user");

        //RE-ACT
            // Delete non-existing user
        userRepository.delete("missing@example.com");

        //RE-ASSERT
            // Case 2: deleting non-existing user should change nothing
        assertEquals(1, users.size(), "Deleting a non-existent user should not modify the users map");

        verify(dataManager, atLeastOnce()).put(eq("users"), eq(users)); // verify that dataManager.put was called for each delete operation
        verify(dataManager, atLeastOnce()).get("users"); // verify that users map was retrieved from DataManager when delete is called
    }

    @Test
    @DisplayName("Test UserRepository findAll with mocked DataManager")
    public void testUserRepositoryFindAll() {
        // ARRANGE
        Map<String, User> users = new HashMap<>();
        User user1 = new User("user1@example.com", "password-1", "User One");
        User user2 = new User("user2@example.com", "password-2", "User Two");
        users.put(user1.getEmail(), user1);
        users.put(user2.getEmail(), user2);

        when(dataManager.get("users")).thenReturn(users);

        // ACT
        Map<String, User> result = userRepository.findAll();

        // ASSERT
            // confirm correct size and keys
        assertEquals(2, result.size(), "findAll should return all users from the users map");
        assertTrue(result.containsKey("user1@example.com"), "Result map should contain user1 by email");
        assertTrue(result.containsKey("user2@example.com"), "Result map should contain user2 by email");

            // values should be the same instances as stored in the original map
        assertSame(user1, result.get("user1@example.com"), "Result should contain the same instance of user1");
        assertSame(user2, result.get("user2@example.com"), "Result should contain the same instance of user2");

            // ensure method returns a copy rather than actual users map
        assertNotSame(users, result, "findAll should return a new map, not the original users map");

        verify(dataManager).get("users");
    }

    @Test
    @DisplayName("Test Repository operations with null DataManager response")
    public void testRepositoryWithNullDataManager() {
        // ARRANGE
        when(dataManager.get("stores")).thenReturn(null);
        when(dataManager.get("users")).thenReturn(null);

        // ACT + ASSERT for StoreRepository - should initialize empty map instead of throwing exception
        Map<String, Store> storeResult = storeRepository.findAll();
        
        // ASSERT for StoreRepository behavior
        assertNotNull(storeResult,
                "StoreRepository.findAll() should not return null even with null DataManager response");
        assertTrue(storeResult.isEmpty(),
                "StoreRepository.findAll() should return an empty map with null DataManager response");

        // ACT for UserRepository
        Map<String, User> userResult = userRepository.findAll(); // should handle null and return empty map

        // ASSERT for UserRepository behavior
        assertNotNull(userResult,
                "UserRepository.findAll() should not return null even with null DataManager response");
        assertTrue(userResult.isEmpty(),
                "UserRepository.findAll() should return an empty map with null DataManager response");

        verify(dataManager, atLeastOnce()).get("stores");
        verify(dataManager, atLeastOnce()).get("users");

        verify(dataManager, atLeastOnce()).put(eq("users"), any(Map.class)); // should at least put an empty map when initializing users map
        verify(dataManager, atLeastOnce()).put(eq("stores"), any(Map.class)); // should also put an empty map for stores
    }
}