package com.pcr.lottery_system.infrastructure;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @param <T> The type of JSON DTO that this handler will read/write.
 * @param <W> The type of wrapper DTO that contains the list of T.
 */
public interface JsonFileHandler<T, W> {

    /**
     * Reads a list of DTOs from the specified JSON file.
     *
     * @param jsonFile The file to read from.
     * @param wrapperClass The Class object for the wrapper DTO type (W).
     * @return A list of DTOs read from the file, or an empty list if the file is empty or malformed.
     * @throws IOException if there's an error reading the file.
     * @throws MismatchedInputException if the JSON structure doesn't match the wrapper DTO.
     */
    List<T> readFromFile(File jsonFile, Class<W> wrapperClass) throws IOException;

    /**
     * Writes a list of DTOs to the specified JSON file, wrapped in the appropriate structure.
     *
     * @param jsonFile The file to write to.
     * @param dataList The list of DTOs to write.
     * @param wrapperClass The Class object for the wrapper DTO type (W).
     * @throws IOException if there's an error writing to the file.
     */
    void writeToFile(File jsonFile, List<T> dataList, Class<W> wrapperClass) throws IOException;

    /**
     * Ensures the specified file exists and is initialized with an empty wrapper structure.
     * @param jsonFile The file to initialize.
     * @param wrapperClass The Class object for the wrapper DTO type (W).
     * @throws IOException if there's an error creating or initializing the file.
     */
    void ensureFileExistsAndInitialized(File jsonFile, Class<W> wrapperClass) throws IOException;
}

