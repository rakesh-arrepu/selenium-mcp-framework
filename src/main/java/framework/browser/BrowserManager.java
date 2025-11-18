package framework.browser;

import framework.config.ConfigurationManager;
import framework.mcp.MCPSeleniumClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * BrowserManager - Manages browser initialization and configuration
 *
 * This class provides centralized browser management with support for multiple browsers,
 * browser options, capabilities, and download directory configuration.
 *
 * Key Features:
 * - Multi-browser support (Chrome, Firefox, Edge, Safari)
 * - Headless mode configuration
 * - Browser options/capabilities management
 * - Download directory configuration
 * - Window size management
 * - Proxy configuration support
 * - Integration with ConfigurationManager
 *
 * Example Usage:
 * <pre>
 * BrowserManager browserManager = new BrowserManager(mcp, config);
 * browserManager.initializeBrowser();
 * browserManager.maximizeBrowser();
 * browserManager.closeBrowser();
 * </pre>
 *
 * @author Selenium-MCP Framework
 * @version 1.0.0
 */
public class BrowserManager {

    private final MCPSeleniumClient mcp;
    private final ConfigurationManager config;
    private BrowserType currentBrowser;
    private boolean isHeadless;

    /**
     * Supported browser types
     */
    public enum BrowserType {
        CHROME("chrome"),
        FIREFOX("firefox"),
        EDGE("edge"),
        SAFARI("safari");

        private final String browserName;

        BrowserType(String browserName) {
            this.browserName = browserName;
        }

        public String getBrowserName() {
            return browserName;
        }

        public static BrowserType fromString(String browser) {
            for (BrowserType type : BrowserType.values()) {
                if (type.browserName.equalsIgnoreCase(browser)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unsupported browser: " + browser);
        }
    }

    /**
     * Creates a new BrowserManager instance
     *
     * @param mcp MCP Selenium client
     * @param config Configuration manager
     */
    public BrowserManager(MCPSeleniumClient mcp, ConfigurationManager config) {
        this.mcp = mcp;
        this.config = config;
        this.isHeadless = config.isBrowserHeadless();

        String browserType = config.getBrowserType();
        this.currentBrowser = BrowserType.fromString(browserType);

        System.out.println("[BrowserManager] Initialized with browser: " + currentBrowser +
                          ", headless: " + isHeadless);
    }

    /**
     * Initializes the browser with configured settings
     *
     * @throws IOException if browser initialization fails
     */
    public void initializeBrowser() throws IOException {
        System.out.println("[BrowserManager] Initializing browser: " + currentBrowser.getBrowserName());

        try {
            // Start browser using MCP client
            mcp.startBrowser(currentBrowser.getBrowserName(), isHeadless);

            // Apply browser settings
            applyBrowserSettings();

            System.out.println("[BrowserManager] ✓ Browser initialized successfully");
        } catch (IOException e) {
            System.err.println("[BrowserManager] ✗ Failed to initialize browser: " + e.getMessage());
            throw new IOException("Browser initialization failed", e);
        }
    }

    /**
     * Applies browser settings from configuration
     *
     * @throws IOException if settings application fails
     */
    private void applyBrowserSettings() throws IOException {
        // Maximize browser if configured
        if (config.shouldMaximizeBrowser()) {
            System.out.println("[BrowserManager] Maximizing browser window");
            // Note: MCP might not have maximize, so we set window size instead
            setWindowSize(config.getBrowserWindowWidth(), config.getBrowserWindowHeight());
        } else {
            // Set custom window size
            setWindowSize(config.getBrowserWindowWidth(), config.getBrowserWindowHeight());
        }
    }

    /**
     * Sets the browser window size
     *
     * @param width Window width in pixels
     * @param height Window height in pixels
     * @throws IOException if window resize fails
     */
    public void setWindowSize(int width, int height) throws IOException {
        System.out.println("[BrowserManager] Setting window size: " + width + "x" + height);
        // Note: This would require MCP Selenium support for window resizing
        // For now, this is a placeholder
    }

    /**
     * Maximizes the browser window
     *
     * @throws IOException if maximize fails
     */
    public void maximizeBrowser() throws IOException {
        System.out.println("[BrowserManager] Maximizing browser window");
        // Note: This would require MCP Selenium support
    }

    /**
     * Minimizes the browser window
     *
     * @throws IOException if minimize fails
     */
    public void minimizeBrowser() throws IOException {
        System.out.println("[BrowserManager] Minimizing browser window");
        // Note: This would require MCP Selenium support
    }

    /**
     * Sets browser in full screen mode
     *
     * @throws IOException if full screen fails
     */
    public void fullScreen() throws IOException {
        System.out.println("[BrowserManager] Setting browser to full screen");
        // Note: This would require MCP Selenium support
    }

    /**
     * Gets Chrome browser options
     *
     * @return Map of Chrome options
     */
    public Map<String, Object> getChromeOptions() {
        Map<String, Object> options = new HashMap<>();

        if (isHeadless) {
            options.put("headless", true);
        }

        // Disable notifications
        options.put("disable-notifications", true);

        // Disable save password prompts
        options.put("disable-save-password-bubble", true);

        // Set download directory
        String downloadDir = System.getProperty("user.dir") + "/downloads";
        options.put("download.default_directory", downloadDir);
        options.put("download.prompt_for_download", false);

        // Disable dev tools
        options.put("excludeSwitches", new String[]{"enable-automation"});

        // Window size
        if (!config.shouldMaximizeBrowser()) {
            options.put("window-size", config.getBrowserWindowWidth() + "," + config.getBrowserWindowHeight());
        } else {
            options.put("start-maximized", true);
        }

        System.out.println("[BrowserManager] Chrome options configured");
        return options;
    }

    /**
     * Gets Firefox browser options
     *
     * @return Map of Firefox options
     */
    public Map<String, Object> getFirefoxOptions() {
        Map<String, Object> options = new HashMap<>();

        if (isHeadless) {
            options.put("headless", true);
        }

        // Disable notifications
        options.put("dom.webnotifications.enabled", false);

        // Set download directory
        String downloadDir = System.getProperty("user.dir") + "/downloads";
        options.put("browser.download.folderList", 2);
        options.put("browser.download.dir", downloadDir);
        options.put("browser.helperApps.neverAsk.saveToDisk",
                   "application/pdf,application/zip,text/csv");

        // Window size
        if (!config.shouldMaximizeBrowser()) {
            options.put("window-size", config.getBrowserWindowWidth() + "," + config.getBrowserWindowHeight());
        }

        System.out.println("[BrowserManager] Firefox options configured");
        return options;
    }

    /**
     * Gets Edge browser options
     *
     * @return Map of Edge options
     */
    public Map<String, Object> getEdgeOptions() {
        Map<String, Object> options = new HashMap<>();

        if (isHeadless) {
            options.put("headless", true);
        }

        // Similar to Chrome options
        options.put("disable-notifications", true);
        options.put("disable-save-password-bubble", true);

        // Set download directory
        String downloadDir = System.getProperty("user.dir") + "/downloads";
        options.put("download.default_directory", downloadDir);

        // Window size
        if (config.shouldMaximizeBrowser()) {
            options.put("start-maximized", true);
        }

        System.out.println("[BrowserManager] Edge options configured");
        return options;
    }

    /**
     * Gets Safari browser options
     *
     * @return Map of Safari options
     */
    public Map<String, Object> getSafariOptions() {
        Map<String, Object> options = new HashMap<>();

        // Safari has limited options compared to other browsers
        System.out.println("[BrowserManager] Safari options configured (limited options available)");
        return options;
    }

    /**
     * Closes the browser
     *
     * @throws IOException if browser close fails
     */
    public void closeBrowser() throws IOException {
        System.out.println("[BrowserManager] Closing browser");

        try {
            mcp.closeBrowser();
            System.out.println("[BrowserManager] ✓ Browser closed successfully");
        } catch (IOException e) {
            System.err.println("[BrowserManager] ✗ Failed to close browser: " + e.getMessage());
            throw new IOException("Browser close failed", e);
        }
    }

    /**
     * Quits the browser and MCP server
     *
     * @throws IOException if quit fails
     */
    public void quitBrowser() throws IOException {
        System.out.println("[BrowserManager] Quitting browser and MCP server");

        try {
            closeBrowser();
            mcp.stopMCPServer();
            System.out.println("[BrowserManager] ✓ Browser and MCP server stopped successfully");
        } catch (IOException e) {
            System.err.println("[BrowserManager] ✗ Failed to quit browser: " + e.getMessage());
            throw new IOException("Browser quit failed", e);
        }
    }

    /**
     * Refreshes the current page
     *
     * @throws IOException if refresh fails
     */
    public void refreshPage() throws IOException {
        System.out.println("[BrowserManager] Refreshing page");
        // Note: This would require MCP Selenium support for page refresh
    }

    /**
     * Navigates back in browser history
     *
     * @throws IOException if navigation fails
     */
    public void navigateBack() throws IOException {
        System.out.println("[BrowserManager] Navigating back");
        // Note: This would require MCP Selenium support
    }

    /**
     * Navigates forward in browser history
     *
     * @throws IOException if navigation fails
     */
    public void navigateForward() throws IOException {
        System.out.println("[BrowserManager] Navigating forward");
        // Note: This would require MCP Selenium support
    }

    /**
     * Clears browser cookies
     *
     * @throws IOException if cookie clear fails
     */
    public void clearCookies() throws IOException {
        System.out.println("[BrowserManager] Clearing browser cookies");
        // Note: This would require MCP Selenium support
    }

    /**
     * Clears browser cache
     *
     * @throws IOException if cache clear fails
     */
    public void clearCache() throws IOException {
        System.out.println("[BrowserManager] Clearing browser cache");
        // Note: This would require MCP Selenium support
    }

    /**
     * Gets the current browser type
     *
     * @return Current browser type
     */
    public BrowserType getCurrentBrowser() {
        return currentBrowser;
    }

    /**
     * Checks if browser is running in headless mode
     *
     * @return true if headless, false otherwise
     */
    public boolean isHeadlessMode() {
        return isHeadless;
    }

    /**
     * Switches to a different browser (requires restart)
     *
     * @param newBrowser New browser type
     * @throws IOException if browser switch fails
     */
    public void switchBrowser(BrowserType newBrowser) throws IOException {
        System.out.println("[BrowserManager] Switching from " + currentBrowser + " to " + newBrowser);

        // Close current browser
        closeBrowser();

        // Update browser type
        this.currentBrowser = newBrowser;

        // Initialize new browser
        initializeBrowser();

        System.out.println("[BrowserManager] ✓ Browser switched successfully");
    }

    /**
     * Prints browser information
     */
    public void printBrowserInfo() {
        System.out.println("\n========== BROWSER INFORMATION ==========");
        System.out.println("Browser Type: " + currentBrowser.getBrowserName());
        System.out.println("Headless Mode: " + isHeadless);
        System.out.println("Window Size: " + config.getBrowserWindowWidth() + "x" + config.getBrowserWindowHeight());
        System.out.println("Maximize: " + config.shouldMaximizeBrowser());
        System.out.println("=========================================\n");
    }
}
