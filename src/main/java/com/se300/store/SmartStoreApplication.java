package com.se300.store;

import com.se300.store.controller.StoreController;
import com.se300.store.controller.UserController;
import com.se300.store.data.DataManager;
import com.se300.store.repository.StoreRepository;
import com.se300.store.repository.UserRepository;
import com.se300.store.service.AuthenticationService;
import com.se300.store.service.StoreService;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * SmartStoreApplication - Main application class.
 *
 * @author Sergey L. Sundukovskiy, Ph.D.
 * @version 1.0
 */
public class SmartStoreApplication {

    private static final Logger logger = LoggerFactory.getLogger(SmartStoreApplication.class);
    private static final int PORT = 8080;

    private Tomcat tomcat;

    

    // added to enable testing
    public void setTomcat(Tomcat t) {
        this.tomcat = t;
    }
    private boolean registerShutdownHook = true;
    public void setRegisterShutdownHook(boolean value) { this.registerShutdownHook = value; }
    private boolean allowTomcatOverwrite = true;
    public void setAllowTomcatOverwrite(boolean v) { this.allowTomcatOverwrite = v; }

    /**
     * Main method - application entry point.
     */
    public static void main(String[] args) {
        SmartStoreApplication app = new SmartStoreApplication();
        try {
            app.start();
        } catch (Exception e) {
            logger.error("Failed to start application", e);
            System.exit(1);
        }
    }

    /**
     * Starts the application.
     * Demonstrates the complete application lifecycle.
     * Blocks until server shutdown (suitable for main application).
     */
    public void start() throws LifecycleException {
        startServer(true); // blocking mode
    }

    /**
     * Starts the application without blocking.
     * Suitable for testing where you need the server running but want to continue execution.
     */
    public void startNonBlocking() throws LifecycleException {
        startServer(false); // non-blocking mode
    }

    /**
     * Internal method to start the server with optional blocking.
     *
     * @param block if true, blocks until server shutdown; if false, returns immediately
     */
    private void startServer(boolean block) throws LifecycleException {
        logger.info("Starting Smart Store Application...");

        // Step 1: Initialize database
        logger.info("Initializing database...");
        DataManager dataManager = DataManager.getInstance();

        // Step 2: Create repositories (Data Access Layer)
        logger.info("Creating repositories...");
        StoreRepository storeRepository = new StoreRepository(dataManager);
        UserRepository userRepository = new UserRepository(dataManager);

        // Step 3: Create services (Business Logic Layer)
        logger.info("Creating services...");
        StoreService storeService = new StoreService(storeRepository);
        AuthenticationService userService = new AuthenticationService(userRepository);

        // Step 4: Create controllers (Presentation Layer)
        logger.info("Creating controllers...");
        StoreController storeController = new StoreController(storeService);
        UserController userController = new UserController(userService);

        // Step 5: Configure and start Tomcat
        logger.info("Configuring Tomcat server...");
        //tomcat = new Tomcat();
        // new code to assist with testing
        if (this.tomcat == null && allowTomcatOverwrite) {
            this.tomcat = new Tomcat();
        }
        tomcat.setPort(PORT);
        tomcat.getConnector(); // Initialize default connector

        // Create context
        String contextPath = "";
        String docBase = new File(".").getAbsolutePath();
        Context context = tomcat.addContext(contextPath, docBase);

        // Register Store Controller servlet
        Tomcat.addServlet(context, "storeController", storeController);
        context.addServletMappingDecoded("/api/v1/stores/*", "storeController");

        // Register User Controller servlet
        Tomcat.addServlet(context, "userController", userController);
        context.addServletMappingDecoded("/api/v1/users/*", "userController");

        // Step 6: Start Tomcat
        tomcat.start();

        logger.info("=".repeat(80));
        logger.info("Smart Store Application started successfully!");
        logger.info("=".repeat(80));
        logger.info("Server running on: http://localhost:{}", PORT);
        logger.info("");
        logger.info("Available endpoints:");
        logger.info("  - API:            http://localhost:{}/api/v1/stores", PORT);
        logger.info("  - API:            http://localhost:{}/api/v1/users", PORT);
        logger.info("=".repeat(80));

        // Add shutdown hook
        // Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

        // added to enable testing
        if (registerShutdownHook) {
            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
        }

        // Keep the server running (only if blocking mode)
        if (block) {
            tomcat.getServer().await();
        }
    }

    /**
     * Stops the server.
     * Useful for testing scenarios where you need to explicitly stop the server.
     */
    public void stop() throws LifecycleException {
        if (tomcat != null) {
            tomcat.stop();
        }
    }


    /**
     * Shuts down the application gracefully.
     *
     * NOTE: We do NOT close the DatabaseManager here because it's a singleton
     * shared across the application (and test classes). Closing it would break
     * other tests or application components. DatabaseManager has its own shutdown
     * hook to close when the JVM exits.
     */
    private void shutdown() {
        logger.info("Shutting down Commission Calculator Integration Application...");

        try {
            if (tomcat != null) {
                tomcat.stop();
                tomcat.destroy();
            }

            logger.info("Application shut down successfully");
        } catch (Exception e) {
            logger.error("Error during shutdown", e);
        }
    }
}
