package com.se300.store.model.unit;

import com.se300.store.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// For testing the customer class
import java.util.Date;

/**
 * The ModelUnitTest class contains unit tests for various models used in the Smart Store application.
 * It includes tests for creation, basic operations, and validation of models and enums utilized in the system.
 */
@DisplayName("Model Unit Tests")
public class ModelUnitTest {

    @Test
    @DisplayName("Test User model creation and getters/setters")
    public void testUserModel() {
        User u = new User("alice@example.com", "p@ss", "Alice");
        assertEquals("alice@example.com", u.getEmail());
        assertEquals("p@ss", u.getPassword());
        assertEquals("Alice", u.getName());

        // mutate and re-check
        u.setEmail("bob@example.com");
        u.setPassword("newpass");
        u.setName("Bob");
        assertEquals("bob@example.com", u.getEmail());
        assertEquals("newpass", u.getPassword());
        assertEquals("Bob", u.getName());
    }

    @Test
    @DisplayName("Test Product model creation and getters/setters")
    public void testProductModel() {
        Product p = new Product("P1", "Milk", "Dairy milk", "1L", "Dairy", 2.49, Temperature.refrigerated);
        assertEquals("P1", p.getId());
        assertEquals("Milk", p.getName());
        assertEquals("Dairy milk", p.getDescription());
        assertEquals("1L", p.getSize());
        assertEquals("Dairy", p.getCategory());
        assertEquals(2.49, p.getPrice());
        assertEquals(Temperature.refrigerated, p.getTemperature());

        // change some values
        p.setName("Skim Milk");
        p.setPrice(2.29);
        p.setTemperature(Temperature.ambient);
        assertEquals("Skim Milk", p.getName());
        assertEquals(2.29, p.getPrice());
        assertEquals(Temperature.ambient, p.getTemperature());
    }

    @Test
    @DisplayName("Test Customer model creation and getters/setters")
    public void testCustomerModel() {
        Customer c = new Customer("C1", "Jane", "Doe", CustomerType.registered, "jane@example.com", "123 Road");
        assertEquals("C1", c.getId());
        assertEquals("Jane", c.getFirstName());
        assertEquals("Doe", c.getLastName());
        assertEquals(CustomerType.registered, c.getType());
        assertEquals("jane@example.com", c.getEmail());
        assertEquals("123 Road", c.getAccountAddress());

        // set optional fields
        c.setAgeGroup(CustomerAgeGroup.adult);
        assertEquals(CustomerAgeGroup.adult, c.getAgeGroup());

        StoreLocation loc = new StoreLocation("S1", "A1");
        c.setStoreLocation(loc);
        Date now = new Date();
        c.setLastSeen(now);
        assertEquals(loc, c.getStoreLocation());
        assertEquals(now, c.getLastSeen());

        // basket assign/unassign via assignBasket
        Basket b = new Basket("B1");
        c.assignBasket(b);
        assertEquals(b, c.getBasket());
        c.assignBasket(null);
        assertNull(c.getBasket());
    }

    @Test
    @DisplayName("Test Store model creation and basic operations")
    public void testStoreModel() throws StoreException {
        // Create store
        Store s = new Store("S1", "10 Main", "Main store");
        assertEquals("S1", s.getId());
        assertEquals("10 Main", s.getAddress());
        assertEquals("Main store", s.getDescription());

        // Add aisle
        Aisle aisle = s.addAisle("A1", "Produce", "Fresh produce", AisleLocation.floor);
        assertNotNull(aisle);
        assertEquals("A1", aisle.getNumber());
        assertEquals("Produce", aisle.getName());

        // Retrieve aisle via getAisle
        Aisle fetched = s.getAisle("A1");
        assertEquals(aisle, fetched);

        // Add shelf to aisle and then add inventory
        Shelf shelf = aisle.addShelf("SH1", "Top", ShelfLevel.high, "Top shelf", Temperature.ambient);
        assertNotNull(shelf);
        assertEquals("SH1", shelf.getId());
        assertEquals(ShelfLevel.high, shelf.getLevel());
        assertEquals(Temperature.ambient, shelf.getTemperature());

        // Create product and add inventory to shelf
        Product p = new Product("P100", "Chips", "Potato chips", "200g", "Snacks", 1.99, Temperature.ambient);
        Inventory inv = shelf.addInventory("I100", "S1", "A1", "SH1", 20, 5, "P100", InventoryType.standard);
        assertNotNull(inv);
        assertEquals("I100", inv.getId());
        assertEquals(5, inv.getCount());
        assertEquals(20, inv.getCapacity());

        // Ensure store.addInventory enforces uniqueness (call twice -> StoreException)
        // First, add inventory to store's inventory map
        s.addInventory(inv);
        // Adding same inventory again should throw
        StoreException thrown = assertThrows(StoreException.class, () -> s.addInventory(inv));
        assertTrue(thrown.getReason().toLowerCase().contains("already exists"));
    }

    @Test
    @DisplayName("Test Basket model operations")
    public void testBasketModel() throws StoreException {
        // Build minimal environment for basket operations:
        // Store S2 with Aisle A1 and Shelf SH1 containing Inventory I2 for Product P2
        Store store = new Store("S2", "20 Center", "Test store");
        Aisle aisle = store.addAisle("A1", "Grocery", "Grocery aisle", AisleLocation.floor);
        Shelf shelf = aisle.addShelf("SH1", "Middle", ShelfLevel.medium, "Mid shelf", Temperature.ambient);

        // Provision product
        Product prod = new Product("P2", "Soda", "Cola", "355ml", "Beverages", 1.25, Temperature.ambient);

        // Add inventory to shelf
        Inventory inventory = shelf.addInventory("I2", "S2", "A1", "SH1", 10, 4, "P2", InventoryType.standard);
        // note: shelf.addInventory will populate shelf.inventoryMap

        // Create customer, set storeLocation (customer must be registered)
        Customer cust = new Customer("CUST1", "Sam", "Shopper", CustomerType.registered, "sam@example.com", "Addr");
        // important: customer must be visible in store.getCustomer if store.addCustomer is used in flow,
        // but Basket.addProduct uses the customer's StoreLocation and the aisle's shelf inventory.
        cust.setStoreLocation(new StoreLocation("S2", "A1"));
        store.addCustomer(cust);

        // Create basket and associate with customer and store
        Basket basket = new Basket("B2");
        basket.setCustomer(cust);
        basket.setStore(store);
        cust.assignBasket(basket);

        // Add product to basket: will decrement inventory from 4 -> 2 (adding 2)
        basket.addProduct("P2", 2);
        assertEquals(2, inventory.getCount());

        // Remove one product: inventory increments to 3
        basket.removeProduct("P2", 1);
        assertEquals(3, inventory.getCount());

        // Clear basket: should return remaining count to shelf (3 -> 4)
        basket.clearBasket();
        assertEquals(4, inventory.getCount());

        // After clear, basket should not be associated with customer
        assertNull(cust.getBasket());
    }

    @Test
    @DisplayName("Test StoreLocation model")
    public void testStoreLocationModel() {
        StoreLocation loc = new StoreLocation("StoreX", "A5");
        assertEquals("StoreX", loc.getStoreId());
        assertEquals("A5", loc.getAisleId());

        loc.setStoreId("StoreY");
        loc.setAisleId("A6");
        assertEquals("StoreY", loc.getStoreId());
        assertEquals("A6", loc.getAisleId());

        String s = loc.toString();
        assertTrue(s.contains("StoreY") && s.contains("A6"));
    }

    @Test
    @DisplayName("Test Temperature enum")
    public void testTemperatureEnum() {
        // verify values and valueOf
        assertArrayEquals(new Temperature[]{Temperature.frozen, Temperature.refrigerated, Temperature.ambient, Temperature.warm, Temperature.hot},
                Temperature.values());

        assertEquals(Temperature.ambient, Temperature.valueOf("ambient"));
    }

    @Test
    @DisplayName("Test CustomerType enum")
    public void testCustomerTypeEnum() {
        assertEquals(CustomerType.guest, CustomerType.valueOf("guest"));
        assertEquals(CustomerType.registered, CustomerType.valueOf("registered"));
    }

    @Test
    @DisplayName("Test Store Exception")
    public void testStoreException() {
        StoreException ex = new StoreException("DoThing", "Something went wrong");
        assertEquals("DoThing", ex.getAction());
        assertEquals("Something went wrong", ex.getReason());

        ex.setAction("NewAction");
        ex.setReason("NewReason");
        assertEquals("NewAction", ex.getAction());
        assertEquals("NewReason", ex.getReason());
    }
}