package com.se300.store.service.integration;

import com.se300.store.data.DataManager;
import com.se300.store.model.*;
import com.se300.store.repository.StoreRepository;
import com.se300.store.repository.UserRepository;
import com.se300.store.service.AuthenticationService;
import com.se300.store.service.StoreService;
import org.junit.jupiter.api.*;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class contains integration tests for verifying the correct functionality
 * of various service workflows in the Smart Store system.
 */
@DisplayName("Service Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceIntegrationTest {

    private static StoreService storeService;
    private static AuthenticationService authenticationService;
    private static DataManager dataManager;

    @BeforeAll
    public static void setUpClass() {
        dataManager = DataManager.getInstance();
        dataManager.clear();

        StoreRepository storeRepository = new StoreRepository(dataManager);
        UserRepository userRepository = new UserRepository(dataManager);

        storeService = new StoreService(storeRepository);
        authenticationService = new AuthenticationService(userRepository);

        // Initializes an empty stores map to start
        dataManager.put("stores", new java.util.HashMap<String, Store>());
    }

    @Test
    @Order(1)
    @DisplayName("Integration: Complete Store workflow - provision, show, update, delete")
    public void testCompleteStoreWorkflow() throws StoreException {

        // provision
        Store s = storeService.provisionStore("S1", "Main Store", "123 Main St", null);

        assertNotNull(s);
        assertEquals("S1", s.getId());
        assertEquals("123 Main St", s.getAddress());
        assertEquals("Main Store", s.getDescription());

        // getAllStores should contain five, due to how the database saves stores
        Collection<Store> stores = storeService.getAllStores();
        assertEquals(1, stores.size());

        // show store
        Store shown = storeService.showStore("S1", null);
        assertEquals("S1", shown.getId());

        // update store
        Store updated = storeService.updateStore("S1", "Updated Description", "456 Updated Road");
        assertEquals("Updated Description", updated.getDescription());
        assertEquals("456 Updated Road", updated.getAddress());

        // delete store
        storeService.deleteStore("S1");

        // after deletion store list empty and showStore throws
        assertTrue(storeService.getAllStores().isEmpty());
        assertThrows(StoreException.class, () -> storeService.showStore("S1", null));
    }

    @Test
    @Order(2)
    @DisplayName("Integration: Store with Aisles and Shelves")
    public void testStoreWithAislesAndShelves() throws StoreException {
        // provision store
        storeService.provisionStore("S2", "Produce Store", "99 Market St", null);

        // provision aisle
        Aisle a = storeService.provisionAisle("S2", "A1", "Produce", "Fresh produce", AisleLocation.floor, null);
        assertNotNull(a);
        assertEquals("A1", a.getNumber());
        assertEquals("Produce", a.getName());

        // provision shelf
        Shelf sh = storeService.provisionShelf(
                "S2", "A1", "SH1", "Top Shelf",
                ShelfLevel.high, "Top shelf", Temperature.ambient, null
        );
        assertNotNull(sh);
        assertEquals("SH1", sh.getId());
        assertEquals(ShelfLevel.high, sh.getLevel());
        assertEquals(Temperature.ambient, sh.getTemperature());

        // show shelf
        Shelf shown = storeService.showShelf("S2", "A1", "SH1", null);
        assertEquals("SH1", shown.getId());
    }

    @Test
    @Order(3)
    @DisplayName("Integration: Product and Inventory workflow")
    public void testProductAndInventoryWorkflow() throws StoreException {
        // provision store, aisle, shelf
        storeService.provisionStore("S3", "Grocery", "200 Center Ave", null);
        storeService.provisionAisle("S3", "A1", "Dry Goods", "Pantry items", AisleLocation.floor, null);
        storeService.provisionShelf("S3", "A1", "SH1", "Middle Shelf", ShelfLevel.medium, "Middle shelf", Temperature.ambient, null);

        // product
        Product p = storeService.provisionProduct(
                "P1", "Cereal", "Corn flakes",
                "500g", "Grocery", 3.99,
                Temperature.ambient, null
        );
        assertNotNull(p);
        assertEquals("P1", p.getId());

        // inventory
        Inventory inv = storeService.provisionInventory(
                "I1", "S3", "A1", "SH1",
                50, 20, "P1", InventoryType.standard, null
        );
        assertNotNull(inv);
        assertEquals(20, inv.getCount());
        assertEquals(50, inv.getCapacity());

        // show
        Inventory shown = storeService.showInventory("I1", null);
        assertEquals("I1", shown.getId());

        // update
        Inventory updated = storeService.updateInventory("I1", 5, null);
        assertEquals(25, updated.getCount());
    }

    @Test
    @Order(4)
    @DisplayName("Integration: Customer and Basket workflow")
    public void testCustomerAndBasketWorkflow() throws StoreException {
        // store etc.
        storeService.provisionStore("S4", "SuperMart", "10 Plaza", null);
        storeService.provisionAisle("S4", "A1", "Food", "Food aisle", AisleLocation.floor, null);
        storeService.provisionShelf("S4", "A1", "SH1", "Shelf1", ShelfLevel.medium, "desc", Temperature.ambient, null);

        storeService.provisionProduct("P2", "Snack", "Chips", "200g", "Snacks", 1.99, Temperature.ambient, null);
        storeService.provisionInventory("I2", "S4", "A1", "SH1", 30, 10, "P2", InventoryType.standard, null);

        // customer
        storeService.provisionCustomer("C1", "Alice", "Smith", CustomerType.registered, "alice@example.com", "123 Lane", null);
        Customer updatedCustomer = storeService.updateCustomer("C1", "S4", "A1", null);
        assertNotNull(updatedCustomer);
        assertEquals("C1", updatedCustomer.getId());

        // basket
        storeService.provisionBasket("B1", null);
        Basket assigned = storeService.assignCustomerBasket("C1", "B1", null);
        assertNotNull(assigned);

        // add product
        storeService.addBasketProduct("B1", "P2", 2, null);
        Inventory afterAdd = storeService.showInventory("I2", null);
        assertEquals(8, afterAdd.getCount());

        // remove
        storeService.removeBasketProduct("B1", "P2", 1, null);
        Inventory afterRemove = storeService.showInventory("I2", null);
        assertEquals(9, afterRemove.getCount());

        // clear
        storeService.clearBasket("B1", null);
        Inventory afterClear = storeService.showInventory("I2", null);
        assertEquals(10, afterClear.getCount());
    }

    @Test
    @Order(5)
    @DisplayName("Integration: Authentication service full workflow")
    public void testAuthenticationWorkflow() {
        // register
        User u = authenticationService.registerUser("ava@gmail.com", "password123", "User One");
        System.out.println (u.getEmail());
        assertNotNull(u);
        assertEquals("ava@gmail.com", u.getEmail());

        // exists
        assertTrue(authenticationService.userExists("ava@gmail.com"));

        // fetch
        User fetched = authenticationService.getUserByEmail("ava@gmail.com");
        assertNotNull(fetched);
        assertEquals("User One", fetched.getName());

        // update
        User updated = authenticationService.updateUser("ava@gmail.com", "newPass", "User Uno");
        assertNotNull(updated);
        assertEquals("User Uno", updated.getName());
        assertEquals("newPass", updated.getPassword());

        // delete
        boolean deleted = authenticationService.deleteUser("ava@gmail.com");
        assertTrue(deleted);
        assertFalse(authenticationService.userExists("ava@gmail.com"));
    }

    @Test
    @Order(6)
    @DisplayName("Integration: Device provisioning and events")
    public void testDeviceProvisioningAndEvents() throws StoreException {
        storeService.provisionStore("S5", "DeviceStore", "1 Device Rd", null);
        storeService.provisionAisle("S5", "A1", "Tech", "Tech aisle", AisleLocation.floor, null);

        Device sensor = storeService.provisionDevice("D1", "TempSens", "microphone", "S5", "A1", null);
        assertNotNull(sensor);

        storeService.raiseEvent("D1", "temperature high", null);

        Device appliance = storeService.provisionDevice("A1", "Speaker", "speaker", "S5", "A1", null);
        assertNotNull(appliance);

        storeService.issueCommand("A1", "volume up", null);
    }

    @Test
    @Order(7)
    @DisplayName("Integration: Error handling across services")
    public void testErrorHandling() {
        assertThrows(StoreException.class, () -> storeService.showStore("NOPE", null));

        assertThrows(StoreException.class, () ->
                storeService.provisionInventory("IX", "MISSING_STORE", "A1", "SH1",
                        10, 1, "P999", InventoryType.standard, null)
        );

        assertThrows(StoreException.class, () -> storeService.getCustomerBasket("no-such-cust", null));
    }
}

