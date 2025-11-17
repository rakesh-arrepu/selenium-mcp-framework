package framework.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MCPSeleniumClient - Wrapper around mcp-selenium Node.js server
 *
 * This client handles all communication with the MCP Selenium server using JSON-RPC 2.0 protocol.
 * It manages the MCP server process lifecycle, sends commands, and processes responses.
 *
 * Key Features:
 * - Automatic MCP server startup/shutdown
 * - JSON-RPC 2.0 protocol communication over stdio
 * - Configurable timeout handling (default 30 seconds)
 * - Comprehensive logging with [MCP] prefix
 * - Support for all mcp-selenium tools
 *
 * Example Usage:
 * <pre>
 * MCPSeleniumClient mcp = new MCPSeleniumClient();
 * mcp.startMCPServer();
 * mcp.startBrowser("chrome", false);
 * mcp.navigate("https://example.com");
 * String elementId = mcp.findElement("id", "username", 5000);
 * mcp.closeBrowser();
 * </pre>
 *
 * @author Selenium-MCP Framework
 * @version 1.0.0
 */
public class MCPSeleniumClient {

    private Process mcpProcess;
    private BufferedWriter processInput;
    private BufferedReader processOutput;
    private BufferedReader processError;
    private final ObjectMapper objectMapper;
    private final AtomicInteger requestIdCounter;
    private static final long DEFAULT_TIMEOUT_MS = 30000; // 30 seconds

    /**
     * Creates a new MCPSeleniumClient instance
     * Initializes JSON mapper and request ID counter
     */
    public MCPSeleniumClient() {
        this.objectMapper = new ObjectMapper();
        this.requestIdCounter = new AtomicInteger(1);
        System.out.println("[MCP] MCPSeleniumClient initialized");
    }

    /**
     * Starts the MCP Selenium server as a subprocess
     * The server runs the npx @executeautomation/mcp-selenium command
     *
     * @throws IOException if server startup fails
     */
    public void startMCPServer() throws IOException {
        System.out.println("[MCP] Starting MCP Selenium server...");

        try {
            ProcessBuilder pb = new ProcessBuilder("npx", "@executeautomation/mcp-selenium");
            pb.redirectErrorStream(false);

            mcpProcess = pb.start();

            processInput = new BufferedWriter(new OutputStreamWriter(mcpProcess.getOutputStream()));
            processOutput = new BufferedReader(new InputStreamReader(mcpProcess.getInputStream()));
            processError = new BufferedReader(new InputStreamReader(mcpProcess.getErrorStream()));

            // Give server time to initialize
            Thread.sleep(2000);

            System.out.println("[MCP] MCP Selenium server started successfully");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Server startup interrupted", e);
        } catch (IOException e) {
            System.err.println("[MCP] Failed to start MCP server: " + e.getMessage());
            throw new IOException("Failed to start MCP server. Ensure Node.js and @executeautomation/mcp-selenium are installed.", e);
        }
    }

    /**
     * Stops the MCP Selenium server and cleans up resources
     *
     * @throws IOException if server shutdown fails
     */
    public void stopMCPServer() throws IOException {
        System.out.println("[MCP] Stopping MCP Selenium server...");

        try {
            if (processInput != null) {
                processInput.close();
            }
            if (processOutput != null) {
                processOutput.close();
            }
            if (processError != null) {
                processError.close();
            }
            if (mcpProcess != null) {
                mcpProcess.destroy();
                mcpProcess.waitFor(5, TimeUnit.SECONDS);
                if (mcpProcess.isAlive()) {
                    mcpProcess.destroyForcibly();
                }
            }
            System.out.println("[MCP] MCP Selenium server stopped successfully");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Server shutdown interrupted", e);
        }
    }

    /**
     * Sends a JSON-RPC 2.0 request to the MCP server
     *
     * @param method The MCP tool method name
     * @param params Parameters for the method
     * @return Response JSON from the server
     * @throws IOException if communication fails or timeout occurs
     */
    private JsonNode sendRequest(String method, Map<String, Object> params) throws IOException {
        return sendRequest(method, params, DEFAULT_TIMEOUT_MS);
    }

    /**
     * Sends a JSON-RPC 2.0 request to the MCP server with custom timeout
     *
     * @param method The MCP tool method name
     * @param params Parameters for the method
     * @param timeoutMs Timeout in milliseconds
     * @return Response JSON from the server
     * @throws IOException if communication fails or timeout occurs
     */
    private JsonNode sendRequest(String method, Map<String, Object> params, long timeoutMs) throws IOException {
        int requestId = requestIdCounter.getAndIncrement();

        ObjectNode request = objectMapper.createObjectNode();
        request.put("jsonrpc", "2.0");
        request.put("id", requestId);
        request.put("method", "tools/call");

        ObjectNode requestParams = objectMapper.createObjectNode();
        requestParams.put("name", method);
        requestParams.set("arguments", objectMapper.valueToTree(params));
        request.set("params", requestParams);

        String requestJson = objectMapper.writeValueAsString(request);
        System.out.println("[MCP] → Request: " + method + " (ID: " + requestId + ")");

        processInput.write(requestJson);
        processInput.newLine();
        processInput.flush();

        // Wait for response with timeout
        long startTime = System.currentTimeMillis();
        String responseLine = null;

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            if (processOutput.ready()) {
                responseLine = processOutput.readLine();
                break;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Request interrupted", e);
            }
        }

        if (responseLine == null) {
            throw new IOException("[MCP] Timeout waiting for response from MCP server (timeout: " + timeoutMs + "ms)");
        }

        JsonNode response = objectMapper.readTree(responseLine);

        if (response.has("error")) {
            JsonNode error = response.get("error");
            String errorMessage = error.has("message") ? error.get("message").asText() : "Unknown error";
            System.err.println("[MCP] ✗ Error: " + errorMessage);
            throw new IOException("[MCP] Server error: " + errorMessage);
        }

        System.out.println("[MCP] ✓ Response received (ID: " + requestId + ")");
        return response.get("result");
    }

    /**
     * Starts a browser session
     *
     * @param browserName Browser type (chrome, firefox, edge, safari)
     * @param headless Whether to run in headless mode
     * @return Result message from server
     * @throws IOException if browser startup fails
     */
    public String startBrowser(String browserName, boolean headless) throws IOException {
        System.out.println("[MCP] Starting browser: " + browserName + " (headless: " + headless + ")");

        Map<String, Object> params = new HashMap<>();
        params.put("browserName", browserName);
        params.put("headless", headless);

        JsonNode result = sendRequest("selenium_start_browser", params);
        return extractContent(result);
    }

    /**
     * Navigates to a URL
     *
     * @param url The URL to navigate to
     * @return Result message from server
     * @throws IOException if navigation fails
     */
    public String navigate(String url) throws IOException {
        System.out.println("[MCP] Navigating to: " + url);

        Map<String, Object> params = new HashMap<>();
        params.put("url", url);

        JsonNode result = sendRequest("selenium_navigate", params);
        return extractContent(result);
    }

    /**
     * Finds an element on the page
     *
     * @param by Locator strategy (id, css, xpath, name, className, tagName, linkText, partialLinkText)
     * @param value Locator value
     * @param timeout Timeout in milliseconds
     * @return Element ID or description
     * @throws IOException if element not found or timeout
     */
    public String findElement(String by, String value, long timeout) throws IOException {
        System.out.println("[MCP] Finding element: " + by + "=" + value + " (timeout: " + timeout + "ms)");

        Map<String, Object> params = new HashMap<>();
        params.put("by", by);
        params.put("value", value);
        params.put("timeout", timeout);

        JsonNode result = sendRequest("selenium_find_element", params, timeout + 5000);
        return extractContent(result);
    }

    /**
     * Clicks an element
     *
     * @param by Locator strategy
     * @param value Locator value
     * @return Result message from server
     * @throws IOException if click fails
     */
    public String clickElement(String by, String value) throws IOException {
        System.out.println("[MCP] Clicking element: " + by + "=" + value);

        Map<String, Object> params = new HashMap<>();
        params.put("by", by);
        params.put("value", value);

        JsonNode result = sendRequest("selenium_click_element", params);
        return extractContent(result);
    }

    /**
     * Sends text to an element (types into input field)
     *
     * @param by Locator strategy
     * @param value Locator value
     * @param text Text to type
     * @return Result message from server
     * @throws IOException if typing fails
     */
    public String sendKeys(String by, String value, String text) throws IOException {
        System.out.println("[MCP] Typing text into element: " + by + "=" + value);

        Map<String, Object> params = new HashMap<>();
        params.put("by", by);
        params.put("value", value);
        params.put("text", text);

        JsonNode result = sendRequest("selenium_send_keys", params);
        return extractContent(result);
    }

    /**
     * Gets text content from an element
     *
     * @param by Locator strategy
     * @param value Locator value
     * @return The element's text content
     * @throws IOException if getting text fails
     */
    public String getElementText(String by, String value) throws IOException {
        System.out.println("[MCP] Getting text from element: " + by + "=" + value);

        Map<String, Object> params = new HashMap<>();
        params.put("by", by);
        params.put("value", value);

        JsonNode result = sendRequest("selenium_get_element_text", params);
        return extractContent(result);
    }

    /**
     * Hovers over an element
     *
     * @param by Locator strategy
     * @param value Locator value
     * @return Result message from server
     * @throws IOException if hover fails
     */
    public String hoverElement(String by, String value) throws IOException {
        System.out.println("[MCP] Hovering over element: " + by + "=" + value);

        Map<String, Object> params = new HashMap<>();
        params.put("by", by);
        params.put("value", value);

        JsonNode result = sendRequest("selenium_hover_element", params);
        return extractContent(result);
    }

    /**
     * Performs drag and drop operation
     *
     * @param sourceBy Source element locator strategy
     * @param sourceValue Source element locator value
     * @param targetBy Target element locator strategy
     * @param targetValue Target element locator value
     * @return Result message from server
     * @throws IOException if drag and drop fails
     */
    public String dragAndDrop(String sourceBy, String sourceValue, String targetBy, String targetValue) throws IOException {
        System.out.println("[MCP] Drag and drop: " + sourceBy + "=" + sourceValue + " → " + targetBy + "=" + targetValue);

        Map<String, Object> params = new HashMap<>();
        params.put("sourceBy", sourceBy);
        params.put("sourceValue", sourceValue);
        params.put("targetBy", targetBy);
        params.put("targetValue", targetValue);

        JsonNode result = sendRequest("selenium_drag_and_drop", params);
        return extractContent(result);
    }

    /**
     * Double-clicks an element
     *
     * @param by Locator strategy
     * @param value Locator value
     * @return Result message from server
     * @throws IOException if double-click fails
     */
    public String doubleClick(String by, String value) throws IOException {
        System.out.println("[MCP] Double-clicking element: " + by + "=" + value);

        Map<String, Object> params = new HashMap<>();
        params.put("by", by);
        params.put("value", value);

        JsonNode result = sendRequest("selenium_double_click", params);
        return extractContent(result);
    }

    /**
     * Right-clicks an element
     *
     * @param by Locator strategy
     * @param value Locator value
     * @return Result message from server
     * @throws IOException if right-click fails
     */
    public String rightClick(String by, String value) throws IOException {
        System.out.println("[MCP] Right-clicking element: " + by + "=" + value);

        Map<String, Object> params = new HashMap<>();
        params.put("by", by);
        params.put("value", value);

        JsonNode result = sendRequest("selenium_right_click", params);
        return extractContent(result);
    }

    /**
     * Presses a keyboard key
     *
     * @param key Key to press (ENTER, TAB, ESCAPE, etc.)
     * @return Result message from server
     * @throws IOException if key press fails
     */
    public String pressKey(String key) throws IOException {
        System.out.println("[MCP] Pressing key: " + key);

        Map<String, Object> params = new HashMap<>();
        params.put("key", key);

        JsonNode result = sendRequest("selenium_press_key", params);
        return extractContent(result);
    }

    /**
     * Takes a screenshot and saves it to a file
     *
     * @param outputPath Path where screenshot should be saved
     * @return Result message from server
     * @throws IOException if screenshot fails
     */
    public String takeScreenshot(String outputPath) throws IOException {
        System.out.println("[MCP] Taking screenshot: " + outputPath);

        Map<String, Object> params = new HashMap<>();
        params.put("outputPath", outputPath);

        JsonNode result = sendRequest("selenium_take_screenshot", params);
        return extractContent(result);
    }

    /**
     * Uploads a file to a file input element
     *
     * @param by Locator strategy
     * @param value Locator value
     * @param filePath Path to file to upload
     * @return Result message from server
     * @throws IOException if upload fails
     */
    public String uploadFile(String by, String value, String filePath) throws IOException {
        System.out.println("[MCP] Uploading file: " + filePath + " to element: " + by + "=" + value);

        Map<String, Object> params = new HashMap<>();
        params.put("by", by);
        params.put("value", value);
        params.put("filePath", filePath);

        JsonNode result = sendRequest("selenium_upload_file", params);
        return extractContent(result);
    }

    /**
     * Closes the browser and ends the session
     *
     * @return Result message from server
     * @throws IOException if browser close fails
     */
    public String closeBrowser() throws IOException {
        System.out.println("[MCP] Closing browser...");

        Map<String, Object> params = new HashMap<>();

        JsonNode result = sendRequest("selenium_close_browser", params);
        return extractContent(result);
    }

    /**
     * Extracts the content text from an MCP response
     * Handles both simple string responses and content array structures
     *
     * @param result The JSON result node
     * @return Extracted content text
     */
    private String extractContent(JsonNode result) {
        if (result == null) {
            return "";
        }

        // Handle content array structure
        if (result.has("content") && result.get("content").isArray()) {
            JsonNode contentArray = result.get("content");
            if (contentArray.size() > 0) {
                JsonNode firstContent = contentArray.get(0);
                if (firstContent.has("text")) {
                    return firstContent.get("text").asText();
                }
            }
        }

        // Handle direct text field
        if (result.has("text")) {
            return result.get("text").asText();
        }

        // Return the whole result as string
        return result.toString();
    }

    /**
     * Checks if the MCP server process is running
     *
     * @return true if server is alive, false otherwise
     */
    public boolean isServerRunning() {
        return mcpProcess != null && mcpProcess.isAlive();
    }
}
