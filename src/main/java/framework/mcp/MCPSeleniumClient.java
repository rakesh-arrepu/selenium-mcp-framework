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
 * - Automatic MCP server startup/shutdown with protocol handshake
 * - JSON-RPC 2.0 protocol communication over stdio
 * - Configurable timeout handling (default 30 seconds)
 * - Comprehensive logging with [MCP] prefix
 * - Support for all @angiejones/mcp-selenium tools
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
    private static final String MCP_PROTOCOL_VERSION = "2024-11-05";

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
     * Starts the MCP Selenium server as a subprocess and performs protocol handshake.
     * The server runs the npx @angiejones/mcp-selenium command.
     *
     * @throws IOException if server startup or handshake fails
     */
    public void startMCPServer() throws IOException {
        System.out.println("[MCP] Starting MCP Selenium server...");

        try {
            ProcessBuilder pb = new ProcessBuilder("npx", "@angiejones/mcp-selenium");
            pb.redirectErrorStream(false);

            mcpProcess = pb.start();

            processInput = new BufferedWriter(new OutputStreamWriter(mcpProcess.getOutputStream()));
            processOutput = new BufferedReader(new InputStreamReader(mcpProcess.getInputStream()));
            processError = new BufferedReader(new InputStreamReader(mcpProcess.getErrorStream()));

            // Give server time to initialize
            Thread.sleep(3000);

            // Verify the process is still alive
            if (!mcpProcess.isAlive()) {
                String errorOutput = drainErrorStream();
                throw new IOException("MCP server process died on startup. Stderr: " + errorOutput);
            }

            // Perform MCP protocol initialization handshake
            performHandshake();

            System.out.println("[MCP] MCP Selenium server started and initialized successfully");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Server startup interrupted", e);
        } catch (IOException e) {
            System.err.println("[MCP] Failed to start MCP server: " + e.getMessage());
            throw new IOException("Failed to start MCP server. Ensure Node.js and @angiejones/mcp-selenium are installed. " + e.getMessage(), e);
        }
    }

    /**
     * Performs the MCP protocol initialization handshake.
     * Sends initialize request, receives response, then sends initialized notification.
     *
     * @throws IOException if handshake fails
     */
    private void performHandshake() throws IOException {
        System.out.println("[MCP] Performing protocol handshake...");

        // Step 1: Send initialize request
        int initId = requestIdCounter.getAndIncrement();
        ObjectNode initRequest = objectMapper.createObjectNode();
        initRequest.put("jsonrpc", "2.0");
        initRequest.put("id", initId);
        initRequest.put("method", "initialize");

        ObjectNode initParams = objectMapper.createObjectNode();
        initParams.put("protocolVersion", MCP_PROTOCOL_VERSION);
        initParams.set("capabilities", objectMapper.createObjectNode());

        ObjectNode clientInfo = objectMapper.createObjectNode();
        clientInfo.put("name", "selenium-mcp-java-client");
        clientInfo.put("version", "1.0.0");
        initParams.set("clientInfo", clientInfo);

        initRequest.set("params", initParams);

        String initJson = objectMapper.writeValueAsString(initRequest);
        processInput.write(initJson);
        processInput.newLine();
        processInput.flush();

        // Step 2: Read initialize response
        String responseLine = readResponseLine(DEFAULT_TIMEOUT_MS);
        if (responseLine == null) {
            throw new IOException("Timeout waiting for initialize response from MCP server");
        }

        JsonNode initResponse = objectMapper.readTree(responseLine);
        if (initResponse.has("error")) {
            throw new IOException("MCP initialize failed: " + initResponse.get("error"));
        }

        System.out.println("[MCP] Initialize response received");

        // Step 3: Send initialized notification (no id, no response expected)
        ObjectNode notification = objectMapper.createObjectNode();
        notification.put("jsonrpc", "2.0");
        notification.put("method", "notifications/initialized");

        String notifJson = objectMapper.writeValueAsString(notification);
        processInput.write(notifJson);
        processInput.newLine();
        processInput.flush();

        System.out.println("[MCP] Protocol handshake completed");
    }

    /**
     * Reads a single response line from the MCP server stdout with timeout.
     *
     * @param timeoutMs Timeout in milliseconds
     * @return The response line or null if timeout
     * @throws IOException if read fails
     */
    private String readResponseLine(long timeoutMs) throws IOException {
        long startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            if (processOutput.ready()) {
                String line = processOutput.readLine();
                if (line != null && !line.isBlank()) {
                    return line;
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Read interrupted", e);
            }
        }

        return null;
    }

    /**
     * Drains the error stream for diagnostic output.
     *
     * @return Error stream content
     */
    private String drainErrorStream() {
        StringBuilder sb = new StringBuilder();
        try {
            while (processError != null && processError.ready()) {
                String line = processError.readLine();
                if (line != null) {
                    sb.append(line).append("\n");
                }
            }
        } catch (IOException ignored) {
        }
        return sb.toString().trim();
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
        if (mcpProcess == null || !mcpProcess.isAlive()) {
            throw new IOException("[MCP] Server process is not running");
        }

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
        String responseLine = readResponseLine(timeoutMs);

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
     * Extracts the content text from an MCP response and validates it.
     * Throws IOException if the content indicates an error from the MCP tool.
     *
     * @param result The JSON result node
     * @return Extracted content text
     * @throws IOException if the content indicates a tool-level error
     */
    private String extractAndValidateContent(JsonNode result) throws IOException {
        String content = extractContent(result);
        if (content.startsWith("Error ") || content.startsWith("Error:")) {
            throw new IOException("[MCP] " + content);
        }
        return content;
    }

    /**
     * Starts a browser session
     *
     * @param browserName Browser type (chrome, firefox)
     * @param headless Whether to run in headless mode
     * @return Result message from server
     * @throws IOException if browser startup fails
     */
    public String startBrowser(String browserName, boolean headless) throws IOException {
        System.out.println("[MCP] Starting browser: " + browserName + " (headless: " + headless + ")");

        Map<String, Object> params = new HashMap<>();
        params.put("browser", browserName);
        if (headless) {
            Map<String, Object> options = new HashMap<>();
            options.put("headless", true);
            params.put("options", options);
        }

        JsonNode result = sendRequest("start_browser", params);
        return extractAndValidateContent(result);
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

        JsonNode result = sendRequest("navigate", params);
        return extractAndValidateContent(result);
    }

    /**
     * Finds an element on the page
     *
     * @param by Locator strategy (id, css, xpath, name, tag, class)
     * @param value Locator value
     * @param timeout Timeout in milliseconds
     * @return Element result text
     * @throws IOException if element not found or timeout
     */
    public String findElement(String by, String value, long timeout) throws IOException {
        System.out.println("[MCP] Finding element: " + by + "=" + value + " (timeout: " + timeout + "ms)");

        Map<String, Object> params = new HashMap<>();
        params.put("by", by);
        params.put("value", value);
        params.put("timeout", timeout);

        JsonNode result = sendRequest("find_element", params, timeout + 5000);
        return extractAndValidateContent(result);
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

        JsonNode result = sendRequest("click_element", params);
        return extractAndValidateContent(result);
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

        JsonNode result = sendRequest("send_keys", params);
        return extractAndValidateContent(result);
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

        JsonNode result = sendRequest("get_element_text", params);
        // Don't validate for error prefix here — getText() may return any user-visible text
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

        JsonNode result = sendRequest("hover", params);
        return extractAndValidateContent(result);
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
        params.put("by", sourceBy);
        params.put("value", sourceValue);
        params.put("targetBy", targetBy);
        params.put("targetValue", targetValue);

        JsonNode result = sendRequest("drag_and_drop", params);
        return extractAndValidateContent(result);
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

        JsonNode result = sendRequest("double_click", params);
        return extractAndValidateContent(result);
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

        JsonNode result = sendRequest("right_click", params);
        return extractAndValidateContent(result);
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

        JsonNode result = sendRequest("press_key", params);
        return extractAndValidateContent(result);
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

        JsonNode result = sendRequest("take_screenshot", params);
        return extractAndValidateContent(result);
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

        JsonNode result = sendRequest("upload_file", params);
        return extractAndValidateContent(result);
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

        JsonNode result = sendRequest("close_session", params);
        return extractAndValidateContent(result);
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
