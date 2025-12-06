package com.se300.store.servlet;

import com.google.gson.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Helper class for JSON serialization and deserialization.
 *
 * @author Sergey L. Sundukovskiy, Ph.D.
 * @version 1.0
 */
public class JsonHelper {

        // static {
        //         System.out.println(">>> USING SERVLET JSONHELPER <<<");
        //     }

    // Singleton Gson instance configured with custom type adapters
    // This ensures consistent JSON formatting across the entire application
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

    /**
     * Convert a Java object to JSON string.
     *
     * @param object The object to serialize
     * @return JSON string representation
     */
    public static String toJson(Object object) {
        return GSON.toJson(object);
    }

    /**
     * Convert a JSON string to a Java object of the specified type.
     *
     * @param json The JSON string to deserialize
     * @param classOfT The class of the target object
     * @param <T> The type of the target object
     * @return The deserialized object
     */
    public static <T> T fromJson(String json, Class<T> classOfT) {
        return GSON.fromJson(json, classOfT);
    }

    /**
     * Get the configured Gson instance.
     * Useful for advanced serialization scenarios.
     *
     * @return The Gson instance
     */
    public static Gson getGson() {
        // System.out.println("GSON = " + GSON);
        // System.out.println("serializeNulls test = " + GSON.toJson(new Object() { String x = null; }));

        return GSON;
    }

    // added to assist 100% coverage
    private JsonHelper() {
        // prevents instantiation
    }



}