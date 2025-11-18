package framework.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TestDataProvider - Utility class for loading test data from various sources
 *
 * This class provides methods to read test data from CSV, JSON, and other file formats.
 * It supports dynamic data loading and parsing for data-driven testing.
 *
 * Key Features:
 * - CSV file reading
 * - JSON file reading
 * - Excel file reading (placeholder for Apache POI)
 * - Dynamic data parsing
 * - Support for different data formats
 *
 * Example Usage:
 * <pre>
 * TestDataProvider provider = new TestDataProvider();
 * List&lt;Map&lt;String, String&gt;&gt; users = provider.readCSV("valid_users.csv");
 * Map&lt;String, Object&gt; credentials = provider.readJSON("credentials.json");
 * </pre>
 *
 * @author Selenium-MCP Framework
 * @version 1.0.0
 */
public class TestDataProvider {

    private static final String TEST_DATA_DIR = "src/test/resources/testdata/";
    private final ObjectMapper objectMapper;

    /**
     * Creates a new TestDataProvider instance
     */
    public TestDataProvider() {
        this.objectMapper = new ObjectMapper();
        System.out.println("[TestDataProvider] Initialized");
    }

    /**
     * Reads CSV file and returns data as list of maps
     *
     * @param fileName CSV file name
     * @return List of maps where keys are column headers
     * @throws IOException if file reading fails
     */
    public List<Map<String, String>> readCSV(String fileName) throws IOException {
        String filePath = TEST_DATA_DIR + fileName;
        System.out.println("[TestDataProvider] Reading CSV file: " + filePath);

        List<Map<String, String>> data = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            String[] headers = null;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    headers = line.split(",");
                    firstLine = false;
                    continue;
                }

                if (headers != null) {
                    String[] values = line.split(",", -1); // -1 to preserve empty values
                    Map<String, String> row = new HashMap<>();

                    for (int i = 0; i < headers.length && i < values.length; i++) {
                        row.put(headers[i].trim(), values[i].trim());
                    }

                    data.add(row);
                }
            }

            System.out.println("[TestDataProvider] ✓ CSV data loaded: " + data.size() + " rows");
            return data;

        } catch (FileNotFoundException e) {
            System.err.println("[TestDataProvider] ✗ CSV file not found: " + filePath);
            throw new IOException("CSV file not found: " + fileName, e);
        } catch (IOException e) {
            System.err.println("[TestDataProvider] ✗ Error reading CSV file: " + e.getMessage());
            throw new IOException("Error reading CSV file: " + fileName, e);
        }
    }

    /**
     * Reads JSON file and returns data as Map
     *
     * @param fileName JSON file name
     * @return Map containing JSON data
     * @throws IOException if file reading fails
     */
    public Map<String, Object> readJSON(String fileName) throws IOException {
        String filePath = TEST_DATA_DIR + fileName;
        System.out.println("[TestDataProvider] Reading JSON file: " + filePath);

        try {
            File file = new File(filePath);
            Map<String, Object> data = objectMapper.readValue(file, new TypeReference<Map<String, Object>>() {});

            System.out.println("[TestDataProvider] ✓ JSON data loaded: " + data.size() + " keys");
            return data;

        } catch (FileNotFoundException e) {
            System.err.println("[TestDataProvider] ✗ JSON file not found: " + filePath);
            throw new IOException("JSON file not found: " + fileName, e);
        } catch (IOException e) {
            System.err.println("[TestDataProvider] ✗ Error reading JSON file: " + e.getMessage());
            throw new IOException("Error reading JSON file: " + fileName, e);
        }
    }

    /**
     * Reads JSON file and returns data as List
     *
     * @param fileName JSON file name
     * @return List containing JSON array data
     * @throws IOException if file reading fails
     */
    public List<Map<String, Object>> readJSONArray(String fileName) throws IOException {
        String filePath = TEST_DATA_DIR + fileName;
        System.out.println("[TestDataProvider] Reading JSON array file: " + filePath);

        try {
            File file = new File(filePath);
            List<Map<String, Object>> data = objectMapper.readValue(file,
                    new TypeReference<List<Map<String, Object>>>() {});

            System.out.println("[TestDataProvider] ✓ JSON array data loaded: " + data.size() + " items");
            return data;

        } catch (FileNotFoundException e) {
            System.err.println("[TestDataProvider] ✗ JSON file not found: " + filePath);
            throw new IOException("JSON file not found: " + fileName, e);
        } catch (IOException e) {
            System.err.println("[TestDataProvider] ✗ Error reading JSON array file: " + e.getMessage());
            throw new IOException("Error reading JSON array file: " + fileName, e);
        }
    }

    /**
     * Reads a specific row from CSV file
     *
     * @param fileName CSV file name
     * @param rowIndex Row index (0-based)
     * @return Map containing row data
     * @throws IOException if file reading fails or row not found
     */
    public Map<String, String> readCSVRow(String fileName, int rowIndex) throws IOException {
        List<Map<String, String>> data = readCSV(fileName);

        if (rowIndex >= 0 && rowIndex < data.size()) {
            return data.get(rowIndex);
        } else {
            throw new IOException("Row index " + rowIndex + " out of bounds. Available rows: " + data.size());
        }
    }

    /**
     * Gets a specific value from JSON data
     *
     * @param fileName JSON file name
     * @param key Key to retrieve
     * @return Value as Object
     * @throws IOException if file reading fails or key not found
     */
    public Object getJSONValue(String fileName, String key) throws IOException {
        Map<String, Object> data = readJSON(fileName);

        if (data.containsKey(key)) {
            return data.get(key);
        } else {
            throw new IOException("Key '" + key + "' not found in JSON file: " + fileName);
        }
    }

    /**
     * Placeholder for Excel file reading (requires Apache POI)
     *
     * @param fileName Excel file name
     * @return List of maps containing Excel data
     * @throws IOException if file reading fails
     */
    public List<Map<String, String>> readExcel(String fileName) throws IOException {
        System.out.println("[TestDataProvider] Excel reading not yet implemented");
        System.out.println("[TestDataProvider] Please add Apache POI dependency for Excel support");
        throw new UnsupportedOperationException("Excel reading requires Apache POI dependency");
    }

    /**
     * Reads plain text file
     *
     * @param fileName Text file name
     * @return File content as String
     * @throws IOException if file reading fails
     */
    public String readTextFile(String fileName) throws IOException {
        String filePath = TEST_DATA_DIR + fileName;
        System.out.println("[TestDataProvider] Reading text file: " + filePath);

        try {
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }

            System.out.println("[TestDataProvider] ✓ Text file loaded");
            return content.toString();

        } catch (FileNotFoundException e) {
            System.err.println("[TestDataProvider] ✗ Text file not found: " + filePath);
            throw new IOException("Text file not found: " + fileName, e);
        }
    }

    /**
     * Gets the test data directory path
     *
     * @return Test data directory path
     */
    public String getTestDataDir() {
        return TEST_DATA_DIR;
    }

    /**
     * Checks if a test data file exists
     *
     * @param fileName File name to check
     * @return true if file exists, false otherwise
     */
    public boolean fileExists(String fileName) {
        String filePath = TEST_DATA_DIR + fileName;
        File file = new File(filePath);
        return file.exists() && file.isFile();
    }

    /**
     * Lists all files in test data directory
     *
     * @return Array of file names
     */
    public String[] listTestDataFiles() {
        File dir = new File(TEST_DATA_DIR);

        if (dir.exists() && dir.isDirectory()) {
            String[] files = dir.list();
            System.out.println("[TestDataProvider] Found " + (files != null ? files.length : 0) + " files in test data directory");
            return files;
        }

        return new String[0];
    }
}
