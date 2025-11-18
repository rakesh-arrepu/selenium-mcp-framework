package framework.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * ConfigurationManager - Centralized configuration management for the framework
 *
 * This singleton class loads and manages all framework configuration from application.properties.
 * It supports environment variable overrides and provides type-safe getters for all config values.
 *
 * Key Features:
 * - Singleton pattern for global access
 * - Loads from application.properties
 * - Environment variable overrides
 * - Type-safe getters (String, int, long, boolean)
 * - Multiple environment support (dev, qa, staging, prod)
 * - Default values for missing properties
 *
 * Example Usage:
 * <pre>
 * ConfigurationManager config = ConfigurationManager.getInstance();
 * String baseUrl = config.getBaseUrl();
 * String browser = config.getBrowserType();
 * long timeout = config.getElementFindTimeout();
 * boolean headless = config.isBrowserHeadless();
 * </pre>
 *
 * @author Selenium-MCP Framework
 * @version 1.0.0
 */
public class ConfigurationManager {

    private static ConfigurationManager instance;
    private final Properties properties;
    private static final String CONFIG_FILE = "config/application.properties";
    private final String environment;

    /**
     * Private constructor - Singleton pattern
     * Loads configuration from properties file
     */
    private ConfigurationManager() {
        properties = new Properties();
        loadProperties();

        // Get environment from system property or use default from config
        String envOverride = System.getProperty("env");
        if (envOverride != null && !envOverride.isEmpty()) {
            environment = envOverride;
        } else {
            environment = getProperty("environment", "dev");
        }

        System.out.println("[ConfigurationManager] Initialized with environment: " + environment);
    }

    /**
     * Gets the singleton instance of ConfigurationManager
     *
     * @return ConfigurationManager instance
     */
    public static synchronized ConfigurationManager getInstance() {
        if (instance == null) {
            instance = new ConfigurationManager();
        }
        return instance;
    }

    /**
     * Loads properties from configuration file
     */
    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                System.err.println("[ConfigurationManager] Unable to find " + CONFIG_FILE);
                return;
            }
            properties.load(input);
            System.out.println("[ConfigurationManager] Configuration loaded successfully from " + CONFIG_FILE);
        } catch (IOException ex) {
            System.err.println("[ConfigurationManager] Error loading configuration: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Gets a property value with support for environment variables override
     *
     * @param key Property key
     * @param defaultValue Default value if property not found
     * @return Property value or default
     */
    private String getProperty(String key, String defaultValue) {
        // First check system property (environment variable override)
        String systemValue = System.getProperty(key);
        if (systemValue != null && !systemValue.isEmpty()) {
            return systemValue;
        }

        // Then check environment variable (convert dots to underscores and uppercase)
        String envKey = key.replace('.', '_').toUpperCase();
        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.isEmpty()) {
            return envValue;
        }

        // Finally, return from properties file or default
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Gets a property value as integer
     *
     * @param key Property key
     * @param defaultValue Default value if property not found or invalid
     * @return Property value as integer
     */
    private int getPropertyAsInt(String key, int defaultValue) {
        String value = getProperty(key, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            System.err.println("[ConfigurationManager] Invalid integer value for " + key + ": " + value);
            return defaultValue;
        }
    }

    /**
     * Gets a property value as long
     *
     * @param key Property key
     * @param defaultValue Default value if property not found or invalid
     * @return Property value as long
     */
    private long getPropertyAsLong(String key, long defaultValue) {
        String value = getProperty(key, String.valueOf(defaultValue));
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            System.err.println("[ConfigurationManager] Invalid long value for " + key + ": " + value);
            return defaultValue;
        }
    }

    /**
     * Gets a property value as boolean
     *
     * @param key Property key
     * @param defaultValue Default value if property not found
     * @return Property value as boolean
     */
    private boolean getPropertyAsBoolean(String key, boolean defaultValue) {
        String value = getProperty(key, String.valueOf(defaultValue));
        return Boolean.parseBoolean(value);
    }

    // ================================================================
    // Application URLs
    // ================================================================

    /**
     * Gets the base URL for current environment
     *
     * @return Base URL
     */
    public String getBaseUrl() {
        String envUrl = getProperty("base.url." + environment, null);
        if (envUrl != null && !envUrl.isEmpty()) {
            return envUrl;
        }
        return getProperty("base.url", "https://example.com");
    }

    /**
     * Gets the current environment name
     *
     * @return Environment name (dev, qa, staging, prod)
     */
    public String getEnvironment() {
        return environment;
    }

    // ================================================================
    // Browser Configuration
    // ================================================================

    /**
     * Gets the browser type
     *
     * @return Browser type (chrome, firefox, edge, safari)
     */
    public String getBrowserType() {
        return getProperty("browser.type", "chrome");
    }

    /**
     * Checks if browser should run in headless mode
     *
     * @return true if headless, false otherwise
     */
    public boolean isBrowserHeadless() {
        return getPropertyAsBoolean("browser.headless", false);
    }

    /**
     * Gets browser window width
     *
     * @return Window width in pixels
     */
    public int getBrowserWindowWidth() {
        return getPropertyAsInt("browser.window.width", 1920);
    }

    /**
     * Gets browser window height
     *
     * @return Window height in pixels
     */
    public int getBrowserWindowHeight() {
        return getPropertyAsInt("browser.window.height", 1080);
    }

    /**
     * Checks if browser should start maximized
     *
     * @return true if maximize, false otherwise
     */
    public boolean shouldMaximizeBrowser() {
        return getPropertyAsBoolean("browser.maximize", true);
    }

    // ================================================================
    // Timeout Configuration
    // ================================================================

    /**
     * Gets implicit wait timeout
     *
     * @return Timeout in milliseconds
     */
    public long getImplicitTimeout() {
        return getPropertyAsLong("timeout.implicit", 10000);
    }

    /**
     * Gets explicit wait timeout
     *
     * @return Timeout in milliseconds
     */
    public long getExplicitTimeout() {
        return getPropertyAsLong("timeout.explicit", 30000);
    }

    /**
     * Gets page load timeout
     *
     * @return Timeout in milliseconds
     */
    public long getPageLoadTimeout() {
        return getPropertyAsLong("timeout.pageload", 60000);
    }

    /**
     * Gets script execution timeout
     *
     * @return Timeout in milliseconds
     */
    public long getScriptTimeout() {
        return getPropertyAsLong("timeout.script", 30000);
    }

    /**
     * Gets element find timeout
     *
     * @return Timeout in milliseconds
     */
    public long getElementFindTimeout() {
        return getPropertyAsLong("timeout.element.find", 5000);
    }

    /**
     * Gets element click timeout
     *
     * @return Timeout in milliseconds
     */
    public long getElementClickTimeout() {
        return getPropertyAsLong("timeout.element.click", 3000);
    }

    // ================================================================
    // Self-Healing Configuration
    // ================================================================

    /**
     * Gets maximum retry attempts per locator strategy
     *
     * @return Maximum retry attempts
     */
    public int getHealingMaxRetryAttempts() {
        return getPropertyAsInt("healing.max.retry.attempts", 3);
    }

    /**
     * Gets delay between retry attempts
     *
     * @return Delay in milliseconds
     */
    public long getHealingRetryDelay() {
        return getPropertyAsLong("healing.retry.delay", 500);
    }

    /**
     * Checks if healing metrics collection is enabled
     *
     * @return true if enabled, false otherwise
     */
    public boolean isHealingMetricsEnabled() {
        return getPropertyAsBoolean("healing.metrics.enabled", true);
    }

    /**
     * Checks if healing metrics should be saved to file
     *
     * @return true if should save, false otherwise
     */
    public boolean shouldSaveHealingMetrics() {
        return getPropertyAsBoolean("healing.metrics.save", true);
    }

    /**
     * Gets healing metrics output directory
     *
     * @return Output directory path
     */
    public String getHealingMetricsOutputDir() {
        return getProperty("healing.metrics.output.dir", "target/healing-metrics");
    }

    // ================================================================
    // Screenshot Configuration
    // ================================================================

    /**
     * Checks if screenshots should be taken on failure
     *
     * @return true if enabled, false otherwise
     */
    public boolean isScreenshotOnFailureEnabled() {
        return getPropertyAsBoolean("screenshot.on.failure", true);
    }

    /**
     * Gets screenshot format
     *
     * @return Screenshot format (png, jpg)
     */
    public String getScreenshotFormat() {
        return getProperty("screenshot.format", "png");
    }

    /**
     * Gets screenshot directory
     *
     * @return Screenshot directory path
     */
    public String getScreenshotDir() {
        return getProperty("screenshot.dir", "screenshots/current");
    }

    /**
     * Gets baseline screenshot directory
     *
     * @return Baseline screenshot directory path
     */
    public String getScreenshotBaselineDir() {
        return getProperty("screenshot.baseline.dir", "screenshots/baseline");
    }

    /**
     * Checks if screenshots should be taken for passed tests
     *
     * @return true if enabled, false otherwise
     */
    public boolean isScreenshotOnPassEnabled() {
        return getPropertyAsBoolean("screenshot.on.pass", false);
    }

    // ================================================================
    // Reporting Configuration
    // ================================================================

    /**
     * Gets report output directory
     *
     * @return Report output directory path
     */
    public String getReportOutputDir() {
        return getProperty("report.output.dir", "target/cucumber-reports");
    }

    /**
     * Gets report title
     *
     * @return Report title
     */
    public String getReportTitle() {
        return getProperty("report.title", "Selenium-MCP Test Execution Report");
    }

    // ================================================================
    // Locator Registry Configuration
    // ================================================================

    /**
     * Gets locator registry file path
     *
     * @return Locator registry file path
     */
    public String getLocatorRegistryPath() {
        return getProperty("locator.registry.path", "src/test/resources/locators/locators.json");
    }

    /**
     * Checks if locator registry should auto-save after tests
     *
     * @return true if auto-save enabled, false otherwise
     */
    public boolean isLocatorRegistryAutoSaveEnabled() {
        return getPropertyAsBoolean("locator.registry.autosave", true);
    }

    /**
     * Checks if locator registry should auto-load on startup
     *
     * @return true if auto-load enabled, false otherwise
     */
    public boolean isLocatorRegistryAutoLoadEnabled() {
        return getPropertyAsBoolean("locator.registry.autoload", true);
    }

    // ================================================================
    // MCP Selenium Configuration
    // ================================================================

    /**
     * Gets MCP server startup timeout
     *
     * @return Timeout in milliseconds
     */
    public long getMcpServerStartupTimeout() {
        return getPropertyAsLong("mcp.server.startup.timeout", 5000);
    }

    /**
     * Gets MCP server shutdown timeout
     *
     * @return Timeout in milliseconds
     */
    public long getMcpServerShutdownTimeout() {
        return getPropertyAsLong("mcp.server.shutdown.timeout", 5000);
    }

    /**
     * Gets MCP request timeout
     *
     * @return Timeout in milliseconds
     */
    public long getMcpRequestTimeout() {
        return getPropertyAsLong("mcp.request.timeout", 30000);
    }

    // ================================================================
    // Logging Configuration
    // ================================================================

    /**
     * Gets log level
     *
     * @return Log level (DEBUG, INFO, WARN, ERROR)
     */
    public String getLogLevel() {
        return getProperty("log.level", "INFO");
    }

    /**
     * Checks if console logging is enabled
     *
     * @return true if enabled, false otherwise
     */
    public boolean isConsoleLoggingEnabled() {
        return getPropertyAsBoolean("log.console.enabled", true);
    }

    /**
     * Checks if file logging is enabled
     *
     * @return true if enabled, false otherwise
     */
    public boolean isFileLoggingEnabled() {
        return getPropertyAsBoolean("log.file.enabled", true);
    }

    /**
     * Gets log file path
     *
     * @return Log file path
     */
    public String getLogFilePath() {
        return getProperty("log.file.path", "target/logs/test-execution.log");
    }

    // ================================================================
    // Test Data Configuration
    // ================================================================

    /**
     * Gets test data directory
     *
     * @return Test data directory path
     */
    public String getTestDataDir() {
        return getProperty("testdata.dir", "src/test/resources/testdata");
    }

    /**
     * Gets test data format
     *
     * @return Test data format (csv, json, excel)
     */
    public String getTestDataFormat() {
        return getProperty("testdata.format", "json");
    }

    // ================================================================
    // Advanced Settings
    // ================================================================

    /**
     * Checks if test retry is enabled
     *
     * @return true if enabled, false otherwise
     */
    public boolean isTestRetryEnabled() {
        return getPropertyAsBoolean("test.retry.enabled", false);
    }

    /**
     * Gets number of test retry attempts
     *
     * @return Number of retry attempts
     */
    public int getTestRetryCount() {
        return getPropertyAsInt("test.retry.count", 2);
    }

    /**
     * Gets parallel execution thread count
     *
     * @return Thread count
     */
    public int getParallelThreadCount() {
        return getPropertyAsInt("test.parallel.threads", 4);
    }

    /**
     * Checks if video recording is enabled
     *
     * @return true if enabled, false otherwise
     */
    public boolean isVideoRecordingEnabled() {
        return getPropertyAsBoolean("video.recording.enabled", false);
    }

    /**
     * Gets video output directory
     *
     * @return Video output directory path
     */
    public String getVideoOutputDir() {
        return getProperty("video.output.dir", "target/videos");
    }

    /**
     * Prints all configuration values (for debugging)
     */
    public void printConfiguration() {
        System.out.println("\n========== CONFIGURATION ==========");
        System.out.println("Environment: " + getEnvironment());
        System.out.println("Base URL: " + getBaseUrl());
        System.out.println("Browser: " + getBrowserType());
        System.out.println("Headless: " + isBrowserHeadless());
        System.out.println("Element Find Timeout: " + getElementFindTimeout() + "ms");
        System.out.println("Explicit Timeout: " + getExplicitTimeout() + "ms");
        System.out.println("Healing Max Retries: " + getHealingMaxRetryAttempts());
        System.out.println("Locator Registry Path: " + getLocatorRegistryPath());
        System.out.println("==================================\n");
    }
}
