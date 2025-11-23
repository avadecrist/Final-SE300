package com.se300.store.repository;

import com.se300.store.data.DataManager;
import com.se300.store.model.Store;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Store Repository represents the store data access layer
 *
 * @author  Sergey L. Sundukovskiy
 * @version 1.0
 * @since   2025-11-06
 */
public class StoreRepository {

    private static final String STORES_KEY = "stores";
    private final DataManager dataManager;

    public StoreRepository(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    /**
     * Find store by ID
     */
    public Optional<Store> findById(String storeId) {
        Map<String, Store> stores = getStoresMap();
        return Optional.ofNullable(stores.get(storeId));
    }

    /**
     * Save or update a store
     */
    public void save(Store store) {
        Map<String, Store> stores = getStoresMap();
        stores.put(store.getId(), store);
        dataManager.put(STORES_KEY, stores);
    }

    /**
     * Check if store exists by ID
     */
    public boolean existsById(String storeId) {
        Map<String, Store> stores = getStoresMap();
        return stores.containsKey(storeId);
    }

    /**
     * Delete store by ID
     */
    public void delete(String storeId) {
        Map<String, Store> stores = getStoresMap();
        stores.remove(storeId);
        dataManager.put(STORES_KEY, stores);
    }

    /**
     * Get all stores
     */
    public Map<String, Store> findAll() {
        return new HashMap<>(getStoresMap());
    }

    /**
     * Helper method to get stores map from DataManager
     */
    @SuppressWarnings("unchecked")
    private Map<String, Store> getStoresMap() {
        return dataManager.get(STORES_KEY);
    }
} 
