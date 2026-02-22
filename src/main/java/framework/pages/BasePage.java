package framework.pages;

import framework.healing.HealingMetrics;
import framework.healing.SelfHealingElement;
import framework.locators.LocatorRegistry;
import framework.mcp.MCPSeleniumClient;

import java.io.IOException;

/**
 * BasePage - Base class for Page Object Model with MCP Selenium integration
 *
 * This is the foundation for all page objects in the framework. It provides access to
 * self-healing elements, browser actions, assertions, and metrics. All page classes
 * should extend this base class.
 *
 * Key Features:
 * - Self-healing element access via element() method
 * - Wait utilities for element visibility
 * - Assertion methods for verification
 * - Screenshot capture
 * - Navigation support
 * - Access to healing metrics
 *
 * Example Usage:
 * <pre>
 * public class LoginPage extends BasePage {
 *     public LoginPage(MCPSeleniumClient mcp, LocatorRegistry registry, HealingMetrics metrics) {
 *         super(mcp, registry, metrics);
 *     }
 *
 *     public void clickLoginButton() throws IOException {
 *         element("loginButton").click();
 *     }
 * }
 * </pre>
 *
 * @author Selenium-MCP Framework
 * @version 1.0.0
 */
public class BasePage {

    protected static final String BASE_URL = "https://the-internet.herokuapp.com";

    protected final MCPSeleniumClient mcp;
    protected final LocatorRegistry locatorRegistry;
    protected final HealingMetrics healingMetrics;
    protected static final long DEFAULT_WAIT_TIME = 10000; // 10 seconds

    /**
     * Creates a new BasePage instance
     *
     * @param mcp MCP Selenium client for browser interactions
     * @param locatorRegistry Registry containing element locators
     * @param healingMetrics Metrics tracker for healing performance
     */
    public BasePage(MCPSeleniumClient mcp, LocatorRegistry locatorRegistry, HealingMetrics healingMetrics) {
        this.mcp = mcp;
        this.locatorRegistry = locatorRegistry;
        this.healingMetrics = healingMetrics;
        System.out.println("[BasePage] " + this.getClass().getSimpleName() + " initialized");
    }

    /**
     * Gets a self-healing element by name
     * This is the primary method for accessing elements in page objects
     *
     * @param elementName Name of the element in the registry
     * @return Self-healing element wrapper
     */
    protected SelfHealingElement element(String elementName) {
        return new SelfHealingElement(elementName, mcp, locatorRegistry, healingMetrics);
    }

    /**
     * Waits for an element to become visible
     * Uses retry mechanism with configurable timeout
     *
     * @param elementName Name of the element
     * @throws IOException if element doesn't become visible within timeout
     */
    protected void waitForElementVisible(String elementName) throws IOException {
        waitForElementVisible(elementName, DEFAULT_WAIT_TIME);
    }

    /**
     * Waits for an element to become visible with custom timeout
     *
     * @param elementName Name of the element
     * @param timeoutMs Timeout in milliseconds
     * @throws IOException if element doesn't become visible within timeout
     */
    protected void waitForElementVisible(String elementName, long timeoutMs) throws IOException {
        System.out.println("[BasePage] Waiting for element to be visible: " + elementName + " (timeout: " + timeoutMs + "ms)");

        long startTime = System.currentTimeMillis();
        long elapsedTime = 0;
        IOException lastException = null;

        while (elapsedTime < timeoutMs) {
            try {
                // Try to find the element
                SelfHealingElement elem = element(elementName);
                elem.getText(); // If this succeeds, element is visible

                System.out.println("[BasePage] ✓ Element visible: " + elementName);
                return;

            } catch (IOException e) {
                lastException = e;
                try {
                    Thread.sleep(500); // Wait 500ms before retry
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Wait interrupted", ie);
                }
            }

            elapsedTime = System.currentTimeMillis() - startTime;
        }

        throw new IOException("[BasePage] Timeout waiting for element: " + elementName +
                " (waited " + timeoutMs + "ms)", lastException);
    }

    /**
     * Asserts that an element is visible on the page
     *
     * @param elementName Name of the element
     * @throws IOException if element is not visible
     */
    protected void assertElementVisible(String elementName) throws IOException {
        System.out.println("[BasePage] Asserting element is visible: " + elementName);

        try {
            element(elementName).getText();
            System.out.println("[BasePage] ✓ Assertion passed: " + elementName + " is visible");
        } catch (IOException e) {
            System.err.println("[BasePage] ✗ Assertion failed: " + elementName + " is not visible");
            throw new IOException("[BasePage] Element not visible: " + elementName, e);
        }
    }

    /**
     * Asserts that an element contains expected text
     *
     * @param elementName Name of the element
     * @param expectedText Expected text content
     * @throws IOException if text doesn't match or element not found
     */
    protected void assertElementText(String elementName, String expectedText) throws IOException {
        System.out.println("[BasePage] Asserting element text: " + elementName + " = '" + expectedText + "'");

        String actualText = element(elementName).getText();

        if (!actualText.trim().equals(expectedText.trim())) {
            System.err.println("[BasePage] ✗ Assertion failed: Expected '" + expectedText + "' but got '" + actualText + "'");
            throw new IOException("[BasePage] Text mismatch for " + elementName +
                    ": expected '" + expectedText + "' but got '" + actualText + "'");
        }

        System.out.println("[BasePage] ✓ Assertion passed: Text matches");
    }

    /**
     * Asserts that an element's text contains expected substring
     *
     * @param elementName Name of the element
     * @param expectedSubstring Expected substring
     * @throws IOException if text doesn't contain substring or element not found
     */
    protected void assertElementTextContains(String elementName, String expectedSubstring) throws IOException {
        System.out.println("[BasePage] Asserting element text contains: " + elementName + " contains '" + expectedSubstring + "'");

        String actualText = element(elementName).getText();

        if (!actualText.contains(expectedSubstring)) {
            System.err.println("[BasePage] ✗ Assertion failed: Text '" + actualText + "' doesn't contain '" + expectedSubstring + "'");
            throw new IOException("[BasePage] Text doesn't contain expected substring for " + elementName +
                    ": expected to contain '" + expectedSubstring + "' but got '" + actualText + "'");
        }

        System.out.println("[BasePage] ✓ Assertion passed: Text contains expected substring");
    }

    /**
     * Navigates to a URL
     *
     * @param url URL to navigate to
     * @throws IOException if navigation fails
     */
    protected void navigateTo(String url) throws IOException {
        System.out.println("[BasePage] Navigating to: " + url);
        mcp.navigate(url);
        System.out.println("[BasePage] ✓ Navigation complete");
    }

    /**
     * Navigates to the application homepage (BASE_URL)
     * Page objects can call this, then click the relevant link to reach their page.
     *
     * @throws IOException if navigation fails
     */
    protected void navigateToHomePage() throws IOException {
        System.out.println("[BasePage] Navigating to homepage: " + BASE_URL);
        navigateTo(BASE_URL);
    }

    /**
     * Takes a screenshot and saves it with the given name
     *
     * @param screenshotName Name for the screenshot file (without extension)
     * @throws IOException if screenshot capture fails
     */
    protected void takeScreenshot(String screenshotName) throws IOException {
        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = screenshotName + "_" + timestamp + ".png";
        String filepath = "screenshots/current/" + filename;

        System.out.println("[BasePage] Taking screenshot: " + filepath);
        mcp.takeScreenshot(filepath);
        System.out.println("[BasePage] ✓ Screenshot saved: " + filepath);
    }

    /**
     * Presses a keyboard key
     *
     * @param key Key to press (ENTER, TAB, ESCAPE, etc.)
     * @throws IOException if key press fails
     */
    protected void pressKey(String key) throws IOException {
        System.out.println("[BasePage] Pressing key: " + key);
        mcp.pressKey(key);
    }

    /**
     * Waits for a specified duration
     *
     * @param milliseconds Time to wait in milliseconds
     */
    protected void sleep(long milliseconds) {
        try {
            System.out.println("[BasePage] Sleeping for " + milliseconds + "ms");
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("[BasePage] Sleep interrupted");
        }
    }

    /**
     * Gets the healing metrics instance
     * Useful for generating reports in page objects
     *
     * @return Healing metrics tracker
     */
    protected HealingMetrics getHealingMetrics() {
        return healingMetrics;
    }

    /**
     * Gets the locator registry instance
     * Useful for debugging or dynamic locator management
     *
     * @return Locator registry
     */
    protected LocatorRegistry getLocatorRegistry() {
        return locatorRegistry;
    }

    /**
     * Gets the MCP client instance
     * Useful for advanced browser operations
     *
     * @return MCP Selenium client
     */
    protected MCPSeleniumClient getMcp() {
        return mcp;
    }

    /**
     * Prints a healing report to console
     * Shows success rates and brittle elements
     */
    protected void printHealingReport() {
        healingMetrics.printHealingReport();
    }

    /**
     * Gets the page name (class simple name)
     *
     * @return Page name
     */
    public String getPageName() {
        return this.getClass().getSimpleName();
    }

    /**
     * Logs a message with [PageName] prefix
     *
     * @param message Message to log
     */
    protected void log(String message) {
        System.out.println("[" + getPageName() + "] " + message);
    }

    /**
     * Logs an error message with [PageName] prefix
     *
     * @param message Error message to log
     */
    protected void logError(String message) {
        System.err.println("[" + getPageName() + "] " + message);
    }
}
