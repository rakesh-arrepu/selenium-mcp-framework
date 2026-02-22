package framework.healing;

import framework.locators.LocatorRegistry;
import framework.locators.LocatorRegistry.LocatorStrategy;
import framework.mcp.MCPSeleniumClient;

import java.io.IOException;
import java.util.List;

/**
 * SelfHealingElement - Intelligent element interaction with automatic fallback
 *
 * This class wraps element interactions with self-healing capabilities. When a locator
 * fails, it automatically tries alternative strategies in order of historical success rate.
 * All attempts are recorded for continuous learning.
 *
 * Key Features:
 * - Automatic fallback through multiple locator strategies
 * - Success rate-based strategy ordering
 * - Retry mechanism (3 attempts per strategy)
 * - Comprehensive metrics tracking
 * - Learning from past successes/failures
 *
 * Example Usage:
 * <pre>
 * SelfHealingElement loginBtn = new SelfHealingElement("loginButton", mcp, registry, metrics);
 * loginBtn.click(); // Tries strategies in success-rate order
 * String text = loginBtn.getText();
 * loginBtn.typeText("username");
 * </pre>
 *
 * @author Selenium-MCP Framework
 * @version 1.0.0
 */
public class SelfHealingElement {

    private final String elementName;
    private final MCPSeleniumClient mcp;
    private final LocatorRegistry registry;
    private final HealingMetrics metrics;
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long DEFAULT_TIMEOUT_MS = 5000;

    /**
     * Creates a new self-healing element
     *
     * @param elementName Name of the element in the registry
     * @param mcp MCP Selenium client for browser interaction
     * @param registry Locator registry with element strategies
     * @param metrics Metrics tracker for healing performance
     */
    public SelfHealingElement(String elementName, MCPSeleniumClient mcp,
                             LocatorRegistry registry, HealingMetrics metrics) {
        this.elementName = elementName;
        this.mcp = mcp;
        this.registry = registry;
        this.metrics = metrics;
    }

    /**
     * Clicks the element using self-healing logic
     *
     * @throws IOException if element cannot be found or clicked after all attempts
     */
    public void click() throws IOException {
        System.out.println("[SelfHealing] Attempting to click: " + elementName);

        LocatorStrategy successfulStrategy = findElementWithHealing();
        if (successfulStrategy == null) {
            throw new IOException("[SelfHealing] Failed to find element: " + elementName + " after all healing attempts");
        }

        try {
            mcp.clickElement(successfulStrategy.getStrategy(), successfulStrategy.getValue());
            System.out.println("[SelfHealing] ✓ Successfully clicked: " + elementName);
        } catch (IOException e) {
            registry.recordFailure(elementName, successfulStrategy);
            metrics.recordHealingFailure(elementName, List.of(successfulStrategy));
            throw new IOException("[SelfHealing] Failed to click element: " + elementName, e);
        }
    }

    /**
     * Types text into the element using self-healing logic
     *
     * @param text Text to type
     * @throws IOException if element cannot be found or typed into after all attempts
     */
    public void typeText(String text) throws IOException {
        System.out.println("[SelfHealing] Attempting to type into: " + elementName);

        LocatorStrategy successfulStrategy = findElementWithHealing();
        if (successfulStrategy == null) {
            throw new IOException("[SelfHealing] Failed to find element: " + elementName + " after all healing attempts");
        }

        try {
            mcp.sendKeys(successfulStrategy.getStrategy(), successfulStrategy.getValue(), text);
            System.out.println("[SelfHealing] ✓ Successfully typed into: " + elementName);
        } catch (IOException e) {
            registry.recordFailure(elementName, successfulStrategy);
            metrics.recordHealingFailure(elementName, List.of(successfulStrategy));
            throw new IOException("[SelfHealing] Failed to type into element: " + elementName, e);
        }
    }

    /**
     * Gets text content from the element using self-healing logic
     *
     * @return The element's text content
     * @throws IOException if element cannot be found after all attempts
     */
    public String getText() throws IOException {
        System.out.println("[SelfHealing] Attempting to get text from: " + elementName);

        LocatorStrategy successfulStrategy = findElementWithHealing();
        if (successfulStrategy == null) {
            throw new IOException("[SelfHealing] Failed to find element: " + elementName + " after all healing attempts");
        }

        try {
            String text = mcp.getElementText(successfulStrategy.getStrategy(), successfulStrategy.getValue());
            System.out.println("[SelfHealing] ✓ Successfully retrieved text from: " + elementName);
            return text;
        } catch (IOException e) {
            registry.recordFailure(elementName, successfulStrategy);
            metrics.recordHealingFailure(elementName, List.of(successfulStrategy));
            throw new IOException("[SelfHealing] Failed to get text from element: " + elementName, e);
        }
    }

    /**
     * Hovers over the element using self-healing logic
     *
     * @throws IOException if element cannot be found or hovered after all attempts
     */
    public void hover() throws IOException {
        System.out.println("[SelfHealing] Attempting to hover over: " + elementName);

        LocatorStrategy successfulStrategy = findElementWithHealing();
        if (successfulStrategy == null) {
            throw new IOException("[SelfHealing] Failed to find element: " + elementName + " after all healing attempts");
        }

        try {
            mcp.hoverElement(successfulStrategy.getStrategy(), successfulStrategy.getValue());
            System.out.println("[SelfHealing] ✓ Successfully hovered over: " + elementName);
        } catch (IOException e) {
            registry.recordFailure(elementName, successfulStrategy);
            metrics.recordHealingFailure(elementName, List.of(successfulStrategy));
            throw new IOException("[SelfHealing] Failed to hover over element: " + elementName, e);
        }
    }

    /**
     * Double-clicks the element using self-healing logic
     *
     * @throws IOException if element cannot be found or double-clicked after all attempts
     */
    public void doubleClick() throws IOException {
        System.out.println("[SelfHealing] Attempting to double-click: " + elementName);

        LocatorStrategy successfulStrategy = findElementWithHealing();
        if (successfulStrategy == null) {
            throw new IOException("[SelfHealing] Failed to find element: " + elementName + " after all healing attempts");
        }

        try {
            mcp.doubleClick(successfulStrategy.getStrategy(), successfulStrategy.getValue());
            System.out.println("[SelfHealing] ✓ Successfully double-clicked: " + elementName);
        } catch (IOException e) {
            registry.recordFailure(elementName, successfulStrategy);
            metrics.recordHealingFailure(elementName, List.of(successfulStrategy));
            throw new IOException("[SelfHealing] Failed to double-click element: " + elementName, e);
        }
    }

    /**
     * Right-clicks the element using self-healing logic
     *
     * @throws IOException if element cannot be found or right-clicked after all attempts
     */
    public void rightClick() throws IOException {
        System.out.println("[SelfHealing] Attempting to right-click: " + elementName);

        LocatorStrategy successfulStrategy = findElementWithHealing();
        if (successfulStrategy == null) {
            throw new IOException("[SelfHealing] Failed to find element: " + elementName + " after all healing attempts");
        }

        try {
            mcp.rightClick(successfulStrategy.getStrategy(), successfulStrategy.getValue());
            System.out.println("[SelfHealing] ✓ Successfully right-clicked: " + elementName);
        } catch (IOException e) {
            registry.recordFailure(elementName, successfulStrategy);
            metrics.recordHealingFailure(elementName, List.of(successfulStrategy));
            throw new IOException("[SelfHealing] Failed to right-click element: " + elementName, e);
        }
    }

    /**
     * Finds element using fallback chain strategy with self-healing
     *
     * This is the core self-healing logic:
     * 1. Gets all strategies sorted by success rate (best first)
     * 2. Tries each strategy with retry mechanism
     * 3. Records success/failure for learning
     * 4. Returns the successful strategy or null if all fail
     *
     * @return The successful LocatorStrategy or null if all strategies fail
     * @throws IOException if MCP communication fails
     */
    private LocatorStrategy findElementWithHealing() throws IOException {
        LocatorRegistry.ElementLocator locator = registry.getElement(elementName);
        if (locator == null) {
            throw new IOException("[SelfHealing] Element not registered: " + elementName);
        }

        // Get strategies sorted by success rate (highest first)
        List<LocatorStrategy> strategies = locator.getStrategiesBySuccessRate();
        if (strategies.isEmpty()) {
            throw new IOException("[SelfHealing] No strategies defined for element: " + elementName);
        }

        System.out.println("[SelfHealing] Trying " + strategies.size() + " strategies for: " + elementName);

        List<LocatorStrategy> attemptedStrategies = new java.util.ArrayList<>();

        // Try each strategy in order of success rate
        for (LocatorStrategy strategy : strategies) {
            attemptedStrategies.add(strategy);

            System.out.println("[SelfHealing]   → Trying: " + strategy.getStrategy() + "=" + strategy.getValue() +
                    " (success rate: " + "%.1f%%".formatted(locator.getSuccessRate(strategy) * 100) + ")");

            // Retry mechanism for each strategy
            for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
                try {
                    String result = mcp.findElement(strategy.getStrategy(), strategy.getValue(), DEFAULT_TIMEOUT_MS);

                    // Element found successfully
                    System.out.println("[SelfHealing]   ✓ Found with: " + strategy.getStrategy() + "=" + strategy.getValue() +
                            (attempt > 1 ? " (attempt " + attempt + ")" : ""));

                    registry.recordSuccess(elementName, strategy);
                    metrics.recordHealingSuccess(elementName, strategy);

                    return strategy;

                } catch (IOException e) {
                    if (attempt < MAX_RETRY_ATTEMPTS) {
                        System.out.println("[SelfHealing]   ⚠ Attempt " + attempt + " failed, retrying...");
                        try {
                            Thread.sleep(500); // Brief pause before retry
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                    } else {
                        System.out.println("[SelfHealing]   ✗ Failed with: " + strategy.getStrategy() + "=" + strategy.getValue() +
                                " after " + MAX_RETRY_ATTEMPTS + " attempts");
                        registry.recordFailure(elementName, strategy);
                    }
                }
            }
        }

        // All strategies failed
        System.err.println("[SelfHealing] ✗ All strategies failed for element: " + elementName);
        metrics.recordHealingFailure(elementName, attemptedStrategies);

        return null;
    }

    /**
     * Refreshes locators (placeholder for future visual matching integration)
     * This could be enhanced to use screenshot-based locator discovery
     */
    public void refreshLocators() {
        System.out.println("[SelfHealing] Refreshing locators for: " + elementName);
        // Future enhancement: Visual matching to discover new locators
    }

    /**
     * Gets healing statistics for this element
     *
     * @return Healing statistics summary
     */
    public String getHealingStats() {
        HealingMetrics.ElementHealingStats stats = metrics.getElementStats(elementName);
        if (stats == null) {
            return "No healing stats available for: " + elementName;
        }

        return "%s - Success Rate: %.1f%% (%d/%d attempts)".formatted(
                elementName,
                stats.getHealingSuccessRate(),
                stats.getSuccessfulHeals(),
                stats.getTotalAttempts());
    }

    /**
     * Gets the element name
     *
     * @return Element name
     */
    public String getElementName() {
        return elementName;
    }
}
