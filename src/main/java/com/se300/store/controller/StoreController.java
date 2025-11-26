package com.se300.store.controller;

import com.se300.store.model.Store;
import com.se300.store.model.StoreException;
import com.se300.store.service.StoreService;
import com.se300.store.servlet.BaseServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collection;

/**
 * REST API controller for Store operations
 * Implements full CRUD operations
 *
 * @author Sergey L. Sundukovskiy, Ph.D.
 * @version 1.0
 */
public class StoreController extends BaseServlet {

    //TODO: Implement REST CRUD API for Store operations

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    /**
     * Handle GET requests
     * - GET /api/v1/stores (no parameters) - Get all stores
     * - GET /api/v1/stores/{storeId} - Get store by ID
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try{
            // Extract store ID from URL
            String storeId = extractResourceId(request);
            // If store ID isn't provided return all the stores
            if(storeId == null){
                Collection<Store> stores = storeService.getAllStores();
                sendJsonResponse(response, stores);
            // Return the specific store if the ID is provided
            }else{
                Store store = storeService.showStore(storeId, null);
                sendJsonResponse(response, store);
            }
        // Catches business logic errors
        }catch(StoreException e){
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND,e.getMessage());
        // Catches any other errors that may come about
        }catch(Exception e){
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    /**
     * Handle POST requests - Create new store
     * POST /api/v1/stores?storeId=xxx&name=xxx&address=xxx
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try{
            // Get parameters
            String storeId = request.getParameter("storeId");
            String name = request.getParameter("name");
            String address = request.getParameter("address");
            // Validate parameters are provided
            if(storeId == null || name == null || address == null){
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Missing required parameters: storeId, name and address are required.");
                return;
            }
            // Call service to create store
            Store createdStore = storeService.provisionStore(storeId, name, address, null);
            // Send response that store has been created successfully
            sendJsonResponse(response, createdStore, HttpServletResponse.SC_CREATED); // Expecting 201 Created
            // Catches business logic errors
        }catch(StoreException e){
            sendErrorResponse(response, HttpServletResponse.SC_CONFLICT, e.getMessage());
            // Catches any other errors that may come about
        }catch(Exception e){
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    /**
     * Handle PUT requests - Update existing store
     * PUT /api/v1/stores/{storeId}?description=xxx&address=xxx
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try{
            // Extract store ID from URL
            String storeId = extractResourceId(request);
            // Validate store ID is provided 
            if(storeId == null){
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Store ID is required in URL");
                return;
            }
            // Get parameters from request
            String description = request.getParameter("description");
            String address = request.getParameter("address");
            // Call service to update the store
            Store updatedStore = storeService.updateStore(storeId, description, address);
            // Send success response
            sendJsonResponse(response, updatedStore); // Expecting 200 OK
            // Catches business logic errors
        }catch(StoreException e){
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
            // Catches any other errors that may come about
        }catch(Exception e){
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    /**
     * Handle DELETE requests - Delete store
     * DELETE /api/v1/stores/{storeId}
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try{
            // Extract store ID from URL
            String storeId = extractResourceId(request);
            // Validate store ID is provided
            if(storeId == null){
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Store ID is required in URL path");
                return;
            }
            // Call service to delete store 
            storeService.deleteStore(storeId);
            // Send success response
            response.setStatus(HttpServletResponse.SC_NO_CONTENT); // Expecting 204 Not Found
        // Catches business logic errors
        }catch(StoreException e){
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        // Catches any other errors that may come about
        }catch(Exception e){
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }
}