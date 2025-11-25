package com.se300.store.repository.integration;

import com.se300.store.data.DataManager;
import com.se300.store.model.Store;
import com.se300.store.model.User;
import com.se300.store.repository.StoreRepository;
import com.se300.store.repository.UserRepository;
import org.junit.jupiter.api.*;

import java.util.Map;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RepositoryIntegrationTest is designed to perform integration tests
 * for repository classes, ensuring their functionality and verifying
 * operations such as persistence, updates, deletions, and concurrency.
 */
@DisplayName("Repository Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RepositoryIntegrationTest {

    //TODO: Implement Integration Tests for the Smart Store Repositories

    private static DataManager dataManager;
    private static StoreRepository storeRepository;
    private static UserRepository userRepository;

    @BeforeAll
    public static void setUpClass() {
        dataManager = DataManager.getInstance();
        dataManager.clear();

        storeRepository = new StoreRepository(dataManager);
        userRepository = new UserRepository(dataManager);

        dataManager.put("stores", new java.util.HashMap<String, Store>()); // Initialize empty stores map to start
    }

    @Test
    @Order(1)
    @DisplayName("Integration: Save multiple stores and verify persistence")
    public void testSaveMultipleStores() {
        // ARRANGE
        Store store1 = new Store("store-1", "123 Main St", "Store one");
        Store store2 = new Store("store-2", "456 Euclid St", "Store two");

        // ACT
        storeRepository.save(store1);
        storeRepository.save(store2);

        // ASSERT
        Map<String, Store> stores = storeRepository.findAll();
        assertEquals(2, stores.size(), "There should be exactly 2 stores after saving two separate stores");
        assertTrue(storeRepository.existsById("store-1"), "Store with ID 'store-1' should exist");
        assertTrue(storeRepository.existsById("store-2"), "Store with ID 'store-2' should exist");

        assertTrue(storeRepository.findById("store-1").isPresent(), "findById should return store-1");
        assertTrue(storeRepository.findById("store-2").isPresent(), "findById should return store-2");
    }

    @Test
    @Order(2)
    @DisplayName("Integration: Update store and verify changes")
    public void testUpdateStore() {
        // ARRANGE 
        Store updatedStore = new Store("store-1", "789 Oak Ave", "Updated Store Description"); // same id as store1 from test (1)

        // ACT
        storeRepository.save(updatedStore); // should overwrite the existing store
        Store updatedStoreResult = storeRepository.findById("store-1").orElse(null);

        // ASSERT
        assertNotNull(updatedStoreResult, "store-1 should exist after update");
        assertEquals("789 Oak Ave", updatedStoreResult.getAddress(), "Store address should be updated from original address");
        assertEquals("Updated Store Description", updatedStoreResult.getDescription(), "Store description should be updated from original description");
    }

    @Test
    @Order(3)
    @DisplayName("Integration: Delete store and verify removal")
    public void testDeleteStore() {
        // ACT
        storeRepository.delete("store-2"); // delete store-2 from test (1)
        Map<String, Store> allStores = storeRepository.findAll();

        // ASSERT
        assertFalse(storeRepository.existsById("store-2"), "Store with ID 'store-2' should no longer exist after deletion");
        assertTrue(storeRepository.existsById("store-1"), "Store with ID 'store-1' should still exist");

        assertEquals(1, allStores.size(), "There should be exactly 1 store remaining after deleting one");
    }

    @Test
    @Order(4)
    @DisplayName("Integration: Register multiple users and verify")
    public void testRegisterMultipleUsers() {
        // use default test users from UserRepository initialization
        // ACT
        Map<String, User> users = userRepository.findAll();

        // ASSERT
        assertEquals(2, users.size(), "There should be exactly 2 users from default initialization");
        assertTrue(userRepository.existsByEmail("admin@store.com"), "User with email 'admin@store.com' should exist");
        assertTrue(userRepository.existsByEmail("user@store.com"), "User with email 'user@store.com' should exist");

        // RE-ACT
        User newUser = new User("ava@fake.com", "pwd123", "Ava DeCristofaro");
        userRepository.save(newUser);
        Map<String, User> updatedUsers = userRepository.findAll();

        // RE-ASSERT
        assertEquals(3, updatedUsers.size(), "There should be exactly 3 users after adding a new user");
        assertTrue(userRepository.existsByEmail("ava@fake.com"), "User with email 'ava@fake.com' should exist after registration");
    }

    @Test
    @Order(5)
    @DisplayName("Integration: Update user and verify changes")
    public void testUpdateUser() {
        // ARRANGE
        User updatedUser = new User("ava@fake.com", "newpassword", "Ava D.");

        // ACT
        userRepository.save(updatedUser); // should overwrite the existing user with same email
        User updatedUserResult = userRepository.findByEmail("ava@fake.com").orElse(null);
        

        // ASSERT
        assertNotNull(updatedUserResult, "User with email 'ava@fake.com' should exist after update");
        assertEquals("newpassword", updatedUserResult.getPassword(), "User password should be updated");
        assertEquals("Ava D.", updatedUserResult.getName(), "User name should be updated");
    }

    @Test
    @Order(6)
    @DisplayName("Integration: Cross-repository data consistency")
    public void testCrossRepositoryConsistency() {
        // ARRANGE
        Map<String, Store> storesBefore = storeRepository.findAll();
        Map<String, User> usersBefore = userRepository.findAll();

        // from earlier tests: store-1 should exist, store-2 should be deleted
        assertEquals(1, storesBefore.size(), "There should be exactly 1 store at this stage");
        assertTrue(storesBefore.containsKey("store-1"), "store-1 should still be present");

        // from earlier tests: users map should have 3 users including the 2 default and 1 added user
        assertEquals(3, usersBefore.size(), "There should be exactly 3 users at this stage");
        assertTrue(usersBefore.containsKey("admin@store.com"), "Default admin user should exist");
        assertTrue(usersBefore.containsKey("user@store.com"), "Default regular user should exist");
        assertTrue(usersBefore.containsKey("ava@fake.com"), "Registered Ava user should exist");

        // ACT
        Store extraStore = new Store("store-extra", "999 Test Dr", "Extra Store");
        storeRepository.save(extraStore); 

        User extraUser = new User("extra@example.com", "pw-333", "Extra User");
        userRepository.save(extraUser); 

        // RE-ARRANGE
        Map<String, Store> storesAfter = storeRepository.findAll();
        Map<String, User> usersAfter = userRepository.findAll();

        // ASSERT
            // stores
        assertEquals(2, storesAfter.size(), "StoreRepository should now track 2 stores");
        assertTrue(storesAfter.containsKey("store-1"), "Existing store-1 should still be present");
        assertTrue(storesAfter.containsKey("store-extra"), "Newly added store-extra should be present");

            // users
        assertEquals(4, usersAfter.size(), "UserRepository should now track 4 users");
        assertTrue(usersAfter.containsKey("admin@store.com"), "Default admin user should still exist");
        assertTrue(usersAfter.containsKey("user@store.com"), "Default user should still exist");
        assertTrue(usersAfter.containsKey("ava@fake.com"), "Updated user Ava should still exist");
        assertTrue(usersAfter.containsKey("extra@example.com"), "Newly added extra user should now be present");
    }

    @Test
    @Order(7)
    @DisplayName("Integration: Concurrent repository operations")
    public void testConcurrentOperations() {
        // ARRANGE
            // Get current counts so we can assert growth
        int initialStoreCount = storeRepository.findAll().size();
        int initialUserCount = userRepository.findAll().size();

        // Each thread writes 50 NEW, uniquely-keyed entries
        Runnable storeWriter = () -> {
            for (int i = 0; i < 50; i++) {
                Store s = new Store(
                        "concurrent-store-" + i,
                        "Concurrent Address " + i,
                        "Concurrent Store " + i
                );
                storeRepository.save(s);
            }
        };

        Runnable userWriter = () -> {
            for (int i = 0; i < 50; i++) {
                User u = new User(
                        "concurrent-user-" + i + "@example.com",
                        "pwd-" + i,
                        "Concurrent User " + i
                );
                userRepository.save(u);
            }
        };

        Thread t1 = new Thread(storeWriter);
        Thread t2 = new Thread(userWriter);

        // ACT
            // Start both threads concurrently
        t1.start();
        t2.start();

            // Wait for threads to finish
        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Test interrupted during concurrent operations");
        }

        // ASSERT
        Map<String, Store> currentStores = storeRepository.findAll();
        Map<String, User> currentUsers = userRepository.findAll();

        assertEquals(initialStoreCount + 50, currentStores.size(),
            "StoreRepository should have 50 more stores after concurrent writes");
        assertEquals(initialUserCount + 50, currentUsers.size(),
            "UserRepository should have 50 more users after concurrent writes");

            // Ensure all concurrent calls of save() worked
        for (int i = 0; i < 50; i++) {
            assertTrue(currentStores.containsKey("concurrent-store-" + i),
                    "Store map should contain ID: concurrent-store-" + i);
            assertTrue(currentUsers.containsKey("concurrent-user-" + i + "@example.com"),
                    "User map should contain email: concurrent-user-" + i + "@example.com");
        }
    }
}
