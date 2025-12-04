package com.se310.store.dto;

import com.google.gson.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Centralized Gson configuration for JSON serialization/deserialization.
 * This class demonstrates the DRY (Don't Repeat Yourself) principle by providing
 * a single source of truth for Gson configuration across all DTOs.
 *
 * Benefits:
 * - Eliminates code duplication
 * - Ensures consistent JSON formatting across all DTOs
 * - Makes it easy to update serialization behavior in one place
 * - Follows the Single Responsibility Principle
 *
 * @author  Sergey L. Sundukovskiy
 * @version 1.0
 * @since   2025-11-13
 */
public class JsonHelper {

    // Singleton Gson instance with custom type adapters
    // Configured once and shared by all DTOs
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()  // Makes JSON output human-readable
            .serializeNulls()     // Include null fields in JSON output
            // Custom serializer for LocalDate (converts to ISO-8601 format: yyyy-MM-dd)
            .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>)
                    (src, typeOfSrc, context) -> new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE)))
            // Custom deserializer for LocalDate (parses from ISO-8601 format)
            .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>)
                    (json, typeOfT, context) -> LocalDate.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE))
            // Custom serializer for LocalDateTime (converts to ISO-8601 format with time)
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>)
                    (src, typeOfSrc, context) -> new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
            // Custom deserializer for LocalDateTime (parses from ISO-8601 format with time)
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>)
                    (json, typeOfT, context) -> LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .create();

    // Private constructor to prevent instantiation
    private JsonHelper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Get the configured Gson instance.
     * This method provides access to the shared Gson instance for all DTOs.
     *
     * @return The configured Gson instance
     */
    public static Gson getGson() {
        return GSON;
    }

    /**
     * Convert an object to JSON string using the shared Gson instance.
     *
     * @param object The object to serialize
     * @return JSON string representation
     */
    public static String toJson(Object object) {
        return GSON.toJson(object);
    }

    /**
     * Convert a JSON string to an object of the specified type.
     *
     * @param json The JSON string to deserialize
     * @param classOfT The class of the target object
     * @param <T> The type of the target object
     * @return The deserialized object
     */
    public static <T> T fromJson(String json, Class<T> classOfT) {
        return GSON.fromJson(json, classOfT);
    }
}