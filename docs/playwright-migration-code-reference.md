# Playwright Migration: Code Reference and Examples

This companion document provides production-ready Java code examples for every component in the Playwright migration. Each example includes the exact import statements, method signatures, and Playwright API calls needed.

**Playwright Java API Reference**: [playwright.dev/java/docs/api/class-playwright](https://playwright.dev/java/docs/api/class-playwright)

---

## Table of Contents

1. [pom.xml -- Complete Maven Configuration](#1-pomxml----complete-maven-configuration)
2. [PlaywrightEngine.java -- Complete Implementation](#2-playwrightenginejava----complete-implementation)
3. [PlaywrightConfig.java -- System Property Configuration](#3-playwrightconfigjava----system-property-configuration)
4. [LocatorRegistry.java -- Enhancement Diff](#4-locatorregistryjava----enhancement-diff)
5. [SelfHealingElement.java -- Complete Rewrite](#5-selfhealingelementjava----complete-rewrite)
6. [BasePage.java -- Complete Rewrite](#6-basepagejava----complete-rewrite)
7. [LoginPage.java -- Migration Diff](#7-loginpagejava----migration-diff)
8. [CheckboxPage.java -- Migration Diff](#8-checkboxpagejava----migration-diff)
9. [AddRemoveElementsPage.java -- Migration with Direct Calls](#9-addremoveelementspagejava----migration-with-direct-calls)
10. [Step Definition Hooks -- Migration Pattern](#10-step-definition-hooks----migration-pattern)
11. [Playwright API Quick Reference](#11-playwright-api-quick-reference)
12. [Actionability Checks Reference](#12-actionability-checks-reference)
13. [Locator Strategy Mapping Reference](#13-locator-strategy-mapping-reference)

---

## 1. pom.xml -- Complete Maven Configuration

**Reference**: [playwright.dev/java/docs/intro](https://playwright.dev/java/docs/intro)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.qatesting</groupId>
    <artifactId>playwright-self-healing-framework</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <name>Playwright Self-Healing Test Framework</name>
    <description>Self-healing test automation framework using Playwright Java with Cucumber BDD</description>

    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <maven.compiler.release>21</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <playwright.version>1.50.0</playwright.version>
        <cucumber.version>7.14.0</cucumber.version>
        <jackson.version>2.15.3</jackson.version>
        <junit.version>4.13.2</junit.version>
    </properties>

    <dependencies>
        <!-- Playwright (replaces selenium-java) -->
        <dependency>
            <groupId>com.microsoft.playwright</groupId>
            <artifactId>playwright</artifactId>
            <version>${playwright.version}</version>
        </dependency>

        <!-- Cucumber for BDD -->
        <dependency>
            <groupId>io.cucumber</groupId>
            <artifactId>cucumber-java</artifactId>
            <version>${cucumber.version}</version>
        </dependency>
        <dependency>
            <groupId>io.cucumber</groupId>
            <artifactId>cucumber-junit</artifactId>
            <version>${cucumber.version}</version>
            <scope>test</scope>
        </dependency>

        <!-- Jackson for JSON processing (locators.json) -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>${jackson.version}</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-core</artifactId>
            <version>${jackson.version}</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-annotations</artifactId>
            <version>${jackson.version}</version>
        </dependency>

        <!-- JUnit for testing -->
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <sourceDirectory>src/main/java</sourceDirectory>
        <testSourceDirectory>src/test/java</testSourceDirectory>
        <resources>
            <resource>
                <directory>src/test/resources</directory>
            </resource>
        </resources>

        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <release>21</release>
                    <encoding>UTF-8</encoding>
                </configuration>
            </plugin>

            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.0.0</version>
                <configuration>
                    <includes>
                        <include>**/*Test.java</include>
                        <include>**/Test*.java</include>
                        <include>**/*Runner.java</include>
                    </includes>
                </configuration>
            </plugin>

            <!-- Auto-install Playwright browsers during build -->
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>exec-maven-plugin</artifactId>
                <version>3.1.0</version>
                <executions>
                    <execution>
                        <id>install-playwright-browsers</id>
                        <phase>generate-test-resources</phase>
                        <goals>
                            <goal>java</goal>
                        </goals>
                        <configuration>
                            <mainClass>com.microsoft.playwright.CLI</mainClass>
                            <arguments>
                                <argument>install</argument>
                                <argument>--with-deps</argument>
                            </arguments>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## 2. PlaywrightEngine.java -- Complete Implementation

**Replaces**: `MCPSeleniumClient.java` (620 lines -> ~160 lines)

**References**:

- [Playwright.create()](https://playwright.dev/java/docs/api/class-playwright#playwright-create)
- [BrowserType.launch()](https://playwright.dev/java/docs/api/class-browsertype#browser-type-launch)
- [Browser.newContext()](https://playwright.dev/java/docs/api/class-browser#browser-new-context)
- [Tracing](https://playwright.dev/java/docs/api/class-tracing)
- [Page.screenshot()](https://playwright.dev/java/docs/api/class-page#page-screenshot)

```java
package framework.playwright;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import framework.locators.LocatorRegistry.LocatorStrategy;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Manages Playwright lifecycle and provides the bridge between
 * LocatorStrategy objects and Playwright Locator instances.
 *
 * Replaces MCPSeleniumClient by communicating directly with
 * browsers via CDP/WebSocket instead of JSON-RPC over stdio.
 */
public class PlaywrightEngine {

    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;
    private boolean tracingActive = false;

    private final String browserType;
    private final boolean headless;

    public PlaywrightEngine() {
        this(PlaywrightConfig.getBrowserType(), PlaywrightConfig.isHeadless());
    }

    public PlaywrightEngine(String browserType, boolean headless) {
        this.browserType = browserType;
        this.headless = headless;
    }

    /**
     * Creates Playwright instance, launches browser, creates context and page.
     * Replaces: MCPSeleniumClient.startMCPServer() + startBrowser()
     *
     * Reference: https://playwright.dev/java/docs/api/class-playwright
     */
    public void initialize() {
        System.out.println("[PlaywrightEngine] Initializing...");
        playwright = Playwright.create();

        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(headless);

        // Map browser name to Playwright browser type
        // Reference: https://playwright.dev/java/docs/browsers
        browser = switch (browserType.toLowerCase()) {
            case "firefox" -> playwright.firefox().launch(launchOptions);
            case "webkit", "safari" -> playwright.webkit().launch(launchOptions);
            default -> playwright.chromium().launch(launchOptions); // "chromium" or "chrome"
        };

        // Create context with optional video recording
        // Reference: https://playwright.dev/java/docs/videos
        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions();
        if (PlaywrightConfig.isVideoEnabled()) {
            contextOptions.setRecordVideoDir(Paths.get("target/videos/"));
        }

        context = browser.newContext(contextOptions);
        context.setDefaultTimeout(PlaywrightConfig.getDefaultTimeout());

        page = context.newPage();
        System.out.println("[PlaywrightEngine] Initialized: " + browserType
                + " (headless=" + headless + ")");
    }

    /**
     * Converts a LocatorStrategy into a Playwright Locator.
     * This is the critical bridge between the self-healing registry
     * and Playwright's native API.
     *
     * Reference: https://playwright.dev/java/docs/locators
     */
    public Locator resolveLocator(LocatorStrategy strategy) {
        String type = strategy.getStrategy();
        String value = strategy.getValue();

        return switch (type) {
            // --- Selenium-compatible strategies ---
            case "id" -> page.locator("id=" + value);
            case "css" -> page.locator(value);
            case "xpath" -> page.locator("xpath=" + value);
            case "name" -> page.locator("[name='" + value + "']");
            case "className" -> page.locator("." + value);
            case "tagName" -> page.locator(value);

            // linkText: exact text match
            // Reference: https://playwright.dev/java/docs/api/class-page#page-get-by-text
            case "linkText" -> page.getByText(value,
                    new Page.GetByTextOptions().setExact(true));

            // partialLinkText: substring match
            case "partialLinkText" -> page.getByText(value);

            // --- Playwright-native strategies (new) ---
            // Reference: https://playwright.dev/java/docs/api/class-page#page-get-by-role
            case "role" -> page.getByRole(AriaRole.valueOf(value.toUpperCase()));

            // Reference: https://playwright.dev/java/docs/api/class-page#page-get-by-label
            case "label" -> page.getByLabel(value);

            // Reference: https://playwright.dev/java/docs/api/class-page#page-get-by-placeholder
            case "placeholder" -> page.getByPlaceholder(value);

            // Reference: https://playwright.dev/java/docs/api/class-page#page-get-by-test-id
            case "testId" -> page.getByTestId(value);

            // Reference: https://playwright.dev/java/docs/api/class-page#page-get-by-text
            case "text" -> page.getByText(value);

            default -> throw new IllegalArgumentException(
                    "Unknown locator strategy: " + type);
        };
    }

    /**
     * Navigates to a URL.
     * Replaces: mcp.navigate(url)
     *
     * Reference: https://playwright.dev/java/docs/api/class-page#page-navigate
     */
    public void navigate(String url) {
        page.navigate(url);
        System.out.println("[PlaywrightEngine] Navigated to: " + url);
    }

    /**
     * Takes a full-page screenshot.
     * Replaces: mcp.takeScreenshot(outputPath)
     *
     * Reference: https://playwright.dev/java/docs/api/class-page#page-screenshot
     */
    public void takeScreenshot(String outputPath) {
        page.screenshot(new Page.ScreenshotOptions()
                .setPath(Paths.get(outputPath))
                .setFullPage(true));
        System.out.println("[PlaywrightEngine] Screenshot saved: " + outputPath);
    }

    /**
     * Presses a keyboard key.
     * Replaces: mcp.pressKey(key)
     *
     * Reference: https://playwright.dev/java/docs/api/class-keyboard#keyboard-press
     */
    public void pressKey(String key) {
        page.keyboard().press(key);
    }

    /**
     * Starts Playwright tracing with screenshots, DOM snapshots, and sources.
     * NEW capability -- not available in the MCP version.
     *
     * Reference: https://playwright.dev/java/docs/trace-viewer-intro
     */
    public void startTracing(String traceName) {
        if (PlaywrightConfig.isTracingEnabled()) {
            context.tracing().start(new Tracing.StartOptions()
                    .setScreenshots(true)
                    .setSnapshots(true)
                    .setSources(true)
                    .setName(traceName));
            tracingActive = true;
            System.out.println("[PlaywrightEngine] Tracing started: " + traceName);
        }
    }

    /**
     * Stops tracing and saves to a zip file.
     * View traces: npx playwright show-trace target/traces/trace.zip
     *
     * Reference: https://playwright.dev/java/docs/api/class-tracing#tracing-stop
     */
    public void stopTracing(String outputPath) {
        if (tracingActive) {
            context.tracing().stop(new Tracing.StopOptions()
                    .setPath(Paths.get(outputPath)));
            tracingActive = false;
            System.out.println("[PlaywrightEngine] Trace saved: " + outputPath);
        }
    }

    /**
     * Returns the Playwright Page for direct access.
     * Used by AddRemoveElementsPage for dynamic element patterns.
     */
    public Page getPage() {
        return page;
    }

    /** Returns the BrowserContext for advanced scenarios. */
    public BrowserContext getContext() {
        return context;
    }

    /**
     * Shuts down everything in correct order.
     * Replaces: mcp.closeBrowser() + mcp.stopMCPServer()
     */
    public void shutdown() {
        System.out.println("[PlaywrightEngine] Shutting down...");
        if (tracingActive) {
            stopTracing("target/traces/final-trace.zip");
        }
        if (page != null) { page.close(); page = null; }
        if (context != null) { context.close(); context = null; }
        if (browser != null) { browser.close(); browser = null; }
        if (playwright != null) { playwright.close(); playwright = null; }
        System.out.println("[PlaywrightEngine] Shutdown complete.");
    }

    public boolean isInitialized() {
        return playwright != null && browser != null
                && browser.isConnected() && page != null && !page.isClosed();
    }
}
```

---

## 3. PlaywrightConfig.java -- System Property Configuration

```java
package framework.playwright;

/**
 * System property-based configuration for Playwright.
 *
 * Usage: mvn test -Dbrowser=firefox -Dheadless=true -Dtracing=true
 */
public final class PlaywrightConfig {

    private PlaywrightConfig() {} // utility class

    /** Browser type: "chromium" (default), "firefox", "webkit" */
    public static String getBrowserType() {
        return System.getProperty("browser", "chromium");
    }

    /** Headless mode: false (default) for debugging, true for CI */
    public static boolean isHeadless() {
        return Boolean.parseBoolean(System.getProperty("headless", "false"));
    }

    /** Enable Playwright tracing: true (default) */
    public static boolean isTracingEnabled() {
        return Boolean.parseBoolean(System.getProperty("tracing", "true"));
    }

    /** Enable video recording: false (default) */
    public static boolean isVideoEnabled() {
        return Boolean.parseBoolean(System.getProperty("video", "false"));
    }

    /** Default action timeout in ms: 30000 (default) */
    public static double getDefaultTimeout() {
        return Double.parseDouble(System.getProperty("timeout", "30000"));
    }
}
```

---

## 4. LocatorRegistry.java -- Enhancement Diff

Only add the `VALID_STRATEGIES` constant. No other changes -- the class is copied as-is from the Selenium project.

```java
// Add inside the LocatorRegistry class, before the LocatorStrategy inner class:

/**
 * All supported locator strategy types.
 * Original Selenium types + Playwright-native additions.
 */
public static final Set<String> VALID_STRATEGIES = Set.of(
        // Selenium-compatible
        "id", "css", "xpath", "name", "className", "tagName",
        "linkText", "partialLinkText",
        // Playwright-native (new)
        "role", "label", "placeholder", "testId", "text"
);
```

Add import at top:

```java
import java.util.Set;
```

Everything else stays identical: `LocatorStrategy`, `ElementLocator`, `getStrategiesBySuccessRate()`, `loadRegistry()`, `saveRegistry()`, success/failure tracking.

---

## 5. SelfHealingElement.java -- Complete Rewrite

**Key change**: `findElementWithHealing()` returns a Playwright `Locator` object instead of a `LocatorStrategy`. Uses `Locator.waitFor()` as the DOM probe.

**References**:

- [Locator.waitFor()](https://playwright.dev/java/docs/api/class-locator#locator-wait-for)
- [Locator.click()](https://playwright.dev/java/docs/api/class-locator#locator-click)
- [Locator.fill()](https://playwright.dev/java/docs/api/class-locator#locator-fill)
- [Locator.textContent()](https://playwright.dev/java/docs/api/class-locator#locator-text-content)
- [Locator.hover()](https://playwright.dev/java/docs/api/class-locator#locator-hover)
- [Locator.dblclick()](https://playwright.dev/java/docs/api/class-locator#locator-dblclick)
- [Actionability checks](https://playwright.dev/java/docs/actionability)

```java
package framework.healing;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.MouseButton;
import com.microsoft.playwright.options.WaitForSelectorState;
import framework.locators.LocatorRegistry;
import framework.locators.LocatorRegistry.LocatorStrategy;
import framework.playwright.PlaywrightEngine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Self-healing element that tries multiple locator strategies sorted by
 * success rate. Uses Playwright's Locator.waitFor() as the DOM probe,
 * then returns the Locator for the caller to perform actions on.
 *
 * Playwright's auto-waiting handles actionability checks (visible, stable,
 * enabled, receives events) AFTER the locator is resolved.
 */
public class SelfHealingElement {

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long PROBE_TIMEOUT_MS = 3000;  // Reduced from 5000 (MCP was slower)
    private static final long RETRY_DELAY_MS = 300;     // Reduced from 500

    private final String elementName;
    private final PlaywrightEngine engine;
    private final LocatorRegistry registry;
    private final HealingMetrics metrics;

    public SelfHealingElement(String elementName, PlaywrightEngine engine,
                              LocatorRegistry registry, HealingMetrics metrics) {
        this.elementName = elementName;
        this.engine = engine;
        this.registry = registry;
        this.metrics = metrics;
    }

    // ──────────────────────────────────────────────────
    // Action Methods
    // ──────────────────────────────────────────────────

    /**
     * Clicks the element using self-healing locator resolution.
     *
     * Playwright auto-waits for: visible, stable, receives events, enabled.
     * Reference: https://playwright.dev/java/docs/actionability
     */
    public void click() throws IOException {
        System.out.println("[SelfHealing] Attempting to click: " + elementName);
        Locator locator = findElementWithHealing();
        if (locator == null) {
            throw new IOException("All strategies failed for element: " + elementName);
        }
        try {
            locator.click();
            System.out.println("[SelfHealing] Successfully clicked: " + elementName);
        } catch (Exception e) {
            throw new IOException("Failed to click: " + elementName, e);
        }
    }

    /**
     * Types text into the element. Uses Playwright fill() which clears first.
     *
     * Playwright auto-waits for: visible, enabled, editable.
     * Reference: https://playwright.dev/java/docs/api/class-locator#locator-fill
     */
    public void typeText(String text) throws IOException {
        System.out.println("[SelfHealing] Attempting to type into: " + elementName);
        Locator locator = findElementWithHealing();
        if (locator == null) {
            throw new IOException("All strategies failed for element: " + elementName);
        }
        try {
            locator.fill(text);
            System.out.println("[SelfHealing] Successfully typed into: " + elementName);
        } catch (Exception e) {
            throw new IOException("Failed to type into: " + elementName, e);
        }
    }

    /**
     * Gets the text content of the element.
     *
     * Reference: https://playwright.dev/java/docs/api/class-locator#locator-text-content
     */
    public String getText() throws IOException {
        System.out.println("[SelfHealing] Attempting to get text: " + elementName);
        Locator locator = findElementWithHealing();
        if (locator == null) {
            throw new IOException("All strategies failed for element: " + elementName);
        }
        try {
            String text = locator.textContent();
            System.out.println("[SelfHealing] Got text from: " + elementName);
            return text;
        } catch (Exception e) {
            throw new IOException("Failed to get text: " + elementName, e);
        }
    }

    /**
     * Hovers over the element.
     *
     * Playwright auto-waits for: visible, stable, receives events.
     * Reference: https://playwright.dev/java/docs/api/class-locator#locator-hover
     */
    public void hover() throws IOException {
        System.out.println("[SelfHealing] Attempting to hover: " + elementName);
        Locator locator = findElementWithHealing();
        if (locator == null) {
            throw new IOException("All strategies failed for element: " + elementName);
        }
        try {
            locator.hover();
            System.out.println("[SelfHealing] Successfully hovered: " + elementName);
        } catch (Exception e) {
            throw new IOException("Failed to hover: " + elementName, e);
        }
    }

    /**
     * Double-clicks the element.
     *
     * Reference: https://playwright.dev/java/docs/api/class-locator#locator-dblclick
     */
    public void doubleClick() throws IOException {
        System.out.println("[SelfHealing] Attempting to double-click: " + elementName);
        Locator locator = findElementWithHealing();
        if (locator == null) {
            throw new IOException("All strategies failed for element: " + elementName);
        }
        try {
            locator.dblclick();
            System.out.println("[SelfHealing] Successfully double-clicked: " + elementName);
        } catch (Exception e) {
            throw new IOException("Failed to double-click: " + elementName, e);
        }
    }

    /**
     * Right-clicks the element.
     *
     * Reference: https://playwright.dev/java/docs/api/class-locator#locator-click
     */
    public void rightClick() throws IOException {
        System.out.println("[SelfHealing] Attempting to right-click: " + elementName);
        Locator locator = findElementWithHealing();
        if (locator == null) {
            throw new IOException("All strategies failed for element: " + elementName);
        }
        try {
            locator.click(new Locator.ClickOptions().setButton(MouseButton.RIGHT));
            System.out.println("[SelfHealing] Successfully right-clicked: " + elementName);
        } catch (Exception e) {
            throw new IOException("Failed to right-click: " + elementName, e);
        }
    }

    // ──────────────────────────────────────────────────
    // New methods (not in MCP version)
    // ──────────────────────────────────────────────────

    /**
     * Checks if the element is visible using the healing chain.
     * Returns false instead of throwing if no strategy works.
     */
    public boolean isVisible() {
        try {
            Locator locator = findElementWithHealing();
            return locator != null && locator.isVisible();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Waits for the element to become visible with a custom timeout.
     *
     * Reference: https://playwright.dev/java/docs/api/class-locator#locator-wait-for
     */
    public void waitForVisible(long timeoutMs) throws IOException {
        Locator locator = findElementWithHealing();
        if (locator == null) {
            throw new IOException("All strategies failed for element: " + elementName);
        }
        try {
            locator.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(timeoutMs));
        } catch (TimeoutError e) {
            throw new IOException("Timeout waiting for visible: " + elementName, e);
        }
    }

    /**
     * Clears the element's text content.
     *
     * Reference: https://playwright.dev/java/docs/api/class-locator#locator-clear
     */
    public void clear() throws IOException {
        Locator locator = findElementWithHealing();
        if (locator == null) {
            throw new IOException("All strategies failed for element: " + elementName);
        }
        try {
            locator.clear();
        } catch (Exception e) {
            throw new IOException("Failed to clear: " + elementName, e);
        }
    }

    /**
     * Checks a checkbox (idempotent -- does nothing if already checked).
     *
     * Reference: https://playwright.dev/java/docs/api/class-locator#locator-check
     */
    public void check() throws IOException {
        Locator locator = findElementWithHealing();
        if (locator == null) {
            throw new IOException("All strategies failed for element: " + elementName);
        }
        try {
            locator.check();
        } catch (Exception e) {
            throw new IOException("Failed to check: " + elementName, e);
        }
    }

    /**
     * Unchecks a checkbox (idempotent -- does nothing if already unchecked).
     *
     * Reference: https://playwright.dev/java/docs/api/class-locator#locator-uncheck
     */
    public void uncheck() throws IOException {
        Locator locator = findElementWithHealing();
        if (locator == null) {
            throw new IOException("All strategies failed for element: " + elementName);
        }
        try {
            locator.uncheck();
        } catch (Exception e) {
            throw new IOException("Failed to uncheck: " + elementName, e);
        }
    }

    // ──────────────────────────────────────────────────
    // Core Healing Algorithm
    // ──────────────────────────────────────────────────

    /**
     * Probes the DOM using each locator strategy in success-rate order.
     *
     * Uses Locator.waitFor() as the probe mechanism:
     * - waitFor() checks that the element exists and matches the selector
     * - On success, returns the Locator for the caller to perform actions
     * - On TimeoutError, moves to the next strategy
     *
     * This preserves the self-healing chain while leveraging Playwright's
     * auto-waiting for the actual action (click, fill, etc.).
     *
     * Reference: https://playwright.dev/java/docs/api/class-locator#locator-wait-for
     */
    private Locator findElementWithHealing() {
        LocatorRegistry.ElementLocator elementLocator = registry.getElement(elementName);
        if (elementLocator == null) {
            System.out.println("[SelfHealing] Element not registered: " + elementName);
            return null;
        }

        List<LocatorStrategy> strategies = elementLocator.getStrategiesBySuccessRate();
        if (strategies.isEmpty()) {
            System.out.println("[SelfHealing] No strategies for element: " + elementName);
            return null;
        }

        List<LocatorStrategy> attemptedStrategies = new ArrayList<>();

        for (LocatorStrategy strategy : strategies) {
            attemptedStrategies.add(strategy);

            for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
                try {
                    System.out.printf("[SelfHealing]   -> Trying: %s=%s (success rate: %.1f%%)%n",
                            strategy.getStrategy(), strategy.getValue(),
                            registry.getSuccessRate(elementName, strategy) * 100);

                    // Convert strategy to Playwright Locator
                    Locator locator = engine.resolveLocator(strategy);

                    // Probe: waitFor checks the element exists in the DOM
                    locator.waitFor(new Locator.WaitForOptions()
                            .setTimeout(PROBE_TIMEOUT_MS));

                    // Success -- record and return the Locator
                    System.out.printf("[SelfHealing]   Found with: %s=%s%n",
                            strategy.getStrategy(), strategy.getValue());
                    registry.recordSuccess(elementName, strategy);
                    metrics.recordHealingSuccess(elementName, strategy);
                    return locator;

                } catch (TimeoutError e) {
                    if (attempt < MAX_RETRY_ATTEMPTS) {
                        System.out.printf("[SelfHealing]   Attempt %d failed, retrying...%n",
                                attempt);
                        try {
                            Thread.sleep(RETRY_DELAY_MS);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                    } else {
                        System.out.printf("[SelfHealing]   Failed with: %s after %d attempts%n",
                                strategy.getStrategy(), MAX_RETRY_ATTEMPTS);
                        registry.recordFailure(elementName, strategy);
                    }
                } catch (Exception e) {
                    // Non-timeout exceptions (invalid selector, etc.)
                    System.out.printf("[SelfHealing]   Error with %s: %s%n",
                            strategy.getStrategy(), e.getMessage());
                    registry.recordFailure(elementName, strategy);
                    break; // Skip remaining retries for this strategy
                }
            }
        }

        // All strategies exhausted
        System.out.println("[SelfHealing] All strategies failed for: " + elementName);
        metrics.recordHealingFailure(elementName, attemptedStrategies);
        return null;
    }

    // ──────────────────────────────────────────────────
    // Utility
    // ──────────────────────────────────────────────────

    public String getElementName() {
        return elementName;
    }

    public String getHealingStats() {
        HealingMetrics.ElementHealingStats stats = metrics.getElementStats(elementName);
        if (stats == null) return "No stats for: " + elementName;
        return String.format("%s: %.1f%% success (%d/%d attempts)",
                elementName, stats.getHealingSuccessRate(),
                stats.getSuccessfulHeals(), stats.getTotalAttempts());
    }
}
```

---

## 6. BasePage.java -- Complete Rewrite

```java
package framework.pages;

import framework.healing.HealingMetrics;
import framework.healing.SelfHealingElement;
import framework.locators.LocatorRegistry;
import framework.playwright.PlaywrightEngine;

import com.microsoft.playwright.Page;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Base class for all page objects. Provides self-healing element
 * access, navigation, assertions, and screenshot capabilities.
 */
public abstract class BasePage {

    protected static final String BASE_URL = "https://the-internet.herokuapp.com";

    protected final PlaywrightEngine engine;
    protected final LocatorRegistry locatorRegistry;
    protected final HealingMetrics healingMetrics;

    public BasePage(PlaywrightEngine engine, LocatorRegistry locatorRegistry,
                    HealingMetrics healingMetrics) {
        this.engine = engine;
        this.locatorRegistry = locatorRegistry;
        this.healingMetrics = healingMetrics;
        System.out.println("[BasePage] " + this.getClass().getSimpleName() + " initialized");
    }

    /** Subclasses must register their element locator strategies. */
    public abstract void initializeLocators();

    /**
     * Creates a SelfHealingElement for the named element.
     * The element's locators must be registered via initializeLocators().
     */
    protected SelfHealingElement element(String elementName) {
        return new SelfHealingElement(elementName, engine, locatorRegistry, healingMetrics);
    }

    /**
     * Navigates to a URL.
     * Reference: https://playwright.dev/java/docs/api/class-page#page-navigate
     */
    protected void navigateTo(String url) throws IOException {
        try {
            engine.navigate(url);
            log("Navigated to: " + url);
        } catch (Exception e) {
            throw new IOException("Failed to navigate to: " + url, e);
        }
    }

    protected void navigateToHomePage() throws IOException {
        navigateTo(BASE_URL);
    }

    /**
     * Waits for an element to become visible.
     * Default timeout: 10 seconds.
     */
    protected void waitForElementVisible(String elementName) throws IOException {
        waitForElementVisible(elementName, 10000);
    }

    /**
     * Waits for an element to become visible with custom timeout.
     * Uses the self-healing chain to find the element first.
     */
    protected void waitForElementVisible(String elementName, long timeoutMs)
            throws IOException {
        try {
            element(elementName).waitForVisible(timeoutMs);
            log("Element visible: " + elementName);
        } catch (IOException e) {
            logError("Timeout waiting for element: " + elementName);
            throw e;
        }
    }

    /**
     * Asserts that an element is visible on the page.
     * Uses isVisible() which returns boolean without throwing.
     */
    protected void assertElementVisible(String elementName) throws IOException {
        if (!element(elementName).isVisible()) {
            throw new IOException("Element not visible: " + elementName);
        }
        log("Asserted visible: " + elementName);
    }

    /** Asserts element text matches exactly (after trimming). */
    protected void assertElementText(String elementName, String expectedText)
            throws IOException {
        String actual = element(elementName).getText();
        if (actual == null || !actual.trim().equals(expectedText.trim())) {
            throw new IOException("Expected text '" + expectedText
                    + "' but got '" + actual + "' for element: " + elementName);
        }
        log("Asserted text match for: " + elementName);
    }

    /** Asserts element text contains a substring. */
    protected void assertElementTextContains(String elementName, String expectedSubstring)
            throws IOException {
        String actual = element(elementName).getText();
        if (actual == null || !actual.contains(expectedSubstring)) {
            throw new IOException("Expected text containing '" + expectedSubstring
                    + "' but got '" + actual + "' for element: " + elementName);
        }
        log("Asserted text contains for: " + elementName);
    }

    /**
     * Takes a screenshot.
     * Reference: https://playwright.dev/java/docs/api/class-page#page-screenshot
     */
    protected void takeScreenshot(String screenshotName) throws IOException {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filePath = "screenshots/current/" + screenshotName + "_" + timestamp + ".png";
        try {
            engine.takeScreenshot(filePath);
            log("Screenshot saved: " + filePath);
        } catch (Exception e) {
            throw new IOException("Failed to take screenshot: " + screenshotName, e);
        }
    }

    /**
     * Presses a keyboard key.
     * Reference: https://playwright.dev/java/docs/api/class-keyboard#keyboard-press
     */
    protected void pressKey(String key) throws IOException {
        try {
            engine.pressKey(key);
        } catch (Exception e) {
            throw new IOException("Failed to press key: " + key, e);
        }
    }

    protected void sleep(long milliseconds) {
        try {
            log("Sleeping for " + milliseconds + "ms");
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** Returns the PlaywrightEngine (replaces getMcp()). */
    protected PlaywrightEngine getEngine() { return engine; }

    /** Returns the Playwright Page for direct access. */
    protected Page getPage() { return engine.getPage(); }

    protected LocatorRegistry getLocatorRegistry() { return locatorRegistry; }

    protected HealingMetrics getHealingMetrics() { return healingMetrics; }

    protected void printHealingReport() { healingMetrics.printHealingReport(); }

    protected String getPageName() { return this.getClass().getSimpleName(); }

    protected void log(String message) {
        System.out.println("[" + getPageName() + "] " + message);
    }

    protected void logError(String message) {
        System.err.println("[" + getPageName() + "] ERROR: " + message);
    }
}
```

---

## 7. LoginPage.java -- Migration Diff

Only 3 lines change. The rest of the file is identical.

```java
// BEFORE (Selenium-MCP):
import framework.mcp.MCPSeleniumClient;
// ...
public LoginPage(MCPSeleniumClient mcp, LocatorRegistry locatorRegistry, HealingMetrics healingMetrics) {
    super(mcp, locatorRegistry, healingMetrics);
}

// AFTER (Playwright):
import framework.playwright.PlaywrightEngine;
// ...
public LoginPage(PlaywrightEngine engine, LocatorRegistry locatorRegistry, HealingMetrics healingMetrics) {
    super(engine, locatorRegistry, healingMetrics);
}
```

All `initializeLocators()` strategies, all action methods, all verification methods remain **identical**.

---

## 8. CheckboxPage.java -- Migration Diff

Same 3-line change as LoginPage.

```java
// BEFORE:
import framework.mcp.MCPSeleniumClient;
public CheckboxPage(MCPSeleniumClient mcp, LocatorRegistry locatorRegistry, HealingMetrics healingMetrics) {
    super(mcp, locatorRegistry, healingMetrics);
}

// AFTER:
import framework.playwright.PlaywrightEngine;
public CheckboxPage(PlaywrightEngine engine, LocatorRegistry locatorRegistry, HealingMetrics healingMetrics) {
    super(engine, locatorRegistry, healingMetrics);
}
```

---

## 9. AddRemoveElementsPage.java -- Migration with Direct Calls

Constructor change plus two methods that bypass self-healing for dynamic elements:

```java
import framework.playwright.PlaywrightEngine;
import com.microsoft.playwright.Locator;

public class AddRemoveElementsPage extends BasePage {
    // ... element constants unchanged ...

    public AddRemoveElementsPage(PlaywrightEngine engine,
                                  LocatorRegistry locatorRegistry,
                                  HealingMetrics healingMetrics) {
        super(engine, locatorRegistry, healingMetrics);
    }

    // initializeLocators() -- unchanged

    /**
     * Clicks delete button at a specific position.
     * Uses direct Playwright locator (not self-healing) because
     * delete button positions shift dynamically.
     *
     * BEFORE: mcp.clickElement("xpath", "//div[@id='elements']/button[" + position + "]")
     * AFTER:  engine.getPage().locator(...).click()
     */
    public void clickDeleteButton(int position) throws IOException {
        try {
            getPage().locator("xpath=//div[@id='elements']/button[" + position + "]").click();
            log("Clicked delete button at position " + position);
        } catch (Exception e) {
            throw new IOException("Failed to click delete button at position " + position, e);
        }
    }

    /**
     * Verifies the exact number of delete buttons on the page.
     *
     * BEFORE: Try find button[N], then try find button[N+1] expecting failure
     * AFTER:  Use locator.count() -- cleaner and more reliable
     *
     * Reference: https://playwright.dev/java/docs/api/class-locator#locator-count
     */
    public void verifyDeleteButtonCount(int expectedCount) throws IOException {
        try {
            Locator deleteButtons = getPage()
                    .locator("xpath=//div[@id='elements']/button");
            int actualCount = deleteButtons.count();

            if (actualCount != expectedCount) {
                throw new AssertionError("Expected " + expectedCount
                        + " delete button(s) but found " + actualCount);
            }
            log("Verified " + expectedCount + " delete button(s) on the page");
        } catch (AssertionError e) {
            throw e;
        } catch (Exception e) {
            // If the #elements div doesn't exist yet (0 buttons expected)
            if (expectedCount == 0) {
                log("Verified 0 delete buttons on the page");
                return;
            }
            throw new IOException("Failed to verify delete button count", e);
        }
    }
}
```

---

## 10. Step Definition Hooks -- Migration Pattern

Example for `LoginSteps.java`. CheckboxSteps and AddRemoveElementsSteps follow the same pattern.

```java
package stepdefinitions;

import framework.healing.HealingMetrics;
import framework.locators.LocatorRegistry;
import framework.pages.LoginPage;
import framework.playwright.PlaywrightEngine;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.io.IOException;

public class LoginSteps {

    // BEFORE: private MCPSeleniumClient mcp;
    private PlaywrightEngine engine;
    private LocatorRegistry locatorRegistry;
    private HealingMetrics healingMetrics;
    private LoginPage loginPage;

    @Before("@login")
    public void setUp() throws IOException {
        System.out.println("=== Setting up Login Test ===");

        // BEFORE: mcp = new MCPSeleniumClient(); mcp.startMCPServer(); mcp.startBrowser("chrome", false);
        engine = new PlaywrightEngine("chromium", false);
        engine.initialize();
        engine.startTracing("login-scenario");

        locatorRegistry = new LocatorRegistry();
        try {
            locatorRegistry.loadRegistry();
        } catch (IOException e) {
            System.out.println("No existing registry found, starting fresh");
        }

        healingMetrics = new HealingMetrics();

        // BEFORE: loginPage = new LoginPage(mcp, locatorRegistry, healingMetrics);
        loginPage = new LoginPage(engine, locatorRegistry, healingMetrics);
        loginPage.initializeLocators();

        System.out.println("=== Login Test Setup Complete ===");
    }

    @After("@login")
    public void tearDown() {
        System.out.println("=== Tearing down Login Test ===");

        if (healingMetrics != null) {
            healingMetrics.printHealingReport();
        }

        if (locatorRegistry != null) {
            try {
                locatorRegistry.saveRegistry();
            } catch (IOException e) {
                System.err.println("Failed to save registry: " + e.getMessage());
            }
        }

        if (engine != null) {
            // NEW: Save Playwright trace for debugging
            engine.stopTracing("target/traces/login-trace.zip");
            engine.shutdown();
        }

        // REMOVED: mcp.closeBrowser(); mcp.stopMCPServer();

        System.out.println("=== Login Test Teardown Complete ===");
    }

    // All step methods remain identical -- they delegate to loginPage.*()

    @Given("user navigates to login page")
    public void userNavigatesToLoginPage() throws IOException {
        loginPage.navigateToLoginPage();
    }

    @Given("user navigates to {string}")
    public void userNavigatesTo(String url) throws IOException {
        // BEFORE: mcp.navigate(url);
        engine.navigate(url);
    }

    @When("user enters {string} and password {string}")
    public void userEntersUsernameAndPassword(String username, String password)
            throws IOException {
        loginPage.enterUsername(username);
        loginPage.enterPassword(password);
    }

    @When("user clicks login button")
    public void userClicksLoginButton() throws IOException {
        loginPage.clickLoginButton();
        Thread.sleep(1000);  // Same wait as original
    }

    @Then("dashboard should be displayed")
    public void dashboardShouldBeDisplayed() throws IOException {
        loginPage.verifyLoginSuccess();
    }

    @Then("error message should appear {string}")
    public void errorMessageShouldAppear(String expectedError) throws IOException {
        loginPage.verifyLoginError(expectedError);
    }

    // ... remaining step methods unchanged ...
}
```

---

## 11. Playwright API Quick Reference

Mapping from Selenium/MCP operations to Playwright Java API.

**References**:

- [Page API](https://playwright.dev/java/docs/api/class-page)
- [Locator API](https://playwright.dev/java/docs/api/class-locator)
- [BrowserContext API](https://playwright.dev/java/docs/api/class-browsercontext)

| Operation | Selenium/MCP | Playwright Java |
| --- | --- | --- |
| Navigate | `mcp.navigate(url)` | `page.navigate(url)` |
| Click | `mcp.clickElement(by, val)` | `page.locator(selector).click()` |
| Type text | `mcp.sendKeys(by, val, text)` | `page.locator(selector).fill(text)` |
| Get text | `mcp.getElementText(by, val)` | `page.locator(selector).textContent()` |
| Hover | `mcp.hoverElement(by, val)` | `page.locator(selector).hover()` |
| Double-click | `mcp.doubleClick(by, val)` | `page.locator(selector).dblclick()` |
| Right-click | `mcp.rightClick(by, val)` | `page.locator(selector).click(button=RIGHT)` |
| Drag and drop | `mcp.dragAndDrop(...)` | `page.locator(source).dragTo(page.locator(target))` |
| Press key | `mcp.pressKey(key)` | `page.keyboard().press(key)` |
| Screenshot | `mcp.takeScreenshot(path)` | `page.screenshot(options)` |
| Find element | `mcp.findElement(by, val, timeout)` | `page.locator(selector).waitFor(timeout)` |
| Element count | N/A | `page.locator(selector).count()` |
| Is visible | N/A | `page.locator(selector).isVisible()` |
| Check checkbox | N/A | `page.locator(selector).check()` |
| Uncheck checkbox | N/A | `page.locator(selector).uncheck()` |
| Get attribute | N/A | `page.locator(selector).getAttribute(name)` |
| Get input value | N/A | `page.locator(selector).inputValue()` |
| Wait for URL | N/A | `page.waitForURL(pattern)` |

---

## 12. Actionability Checks Reference

Playwright automatically performs these checks before actions. No explicit waits needed.

**Reference**: [playwright.dev/java/docs/actionability](https://playwright.dev/java/docs/actionability)

| Action | Visible | Stable | Receives Events | Enabled | Editable |
| --- | --- | --- | --- | --- | --- |
| `click()` | Yes | Yes | Yes | Yes | - |
| `dblclick()` | Yes | Yes | Yes | Yes | - |
| `hover()` | Yes | Yes | Yes | - | - |
| `fill()` | Yes | - | - | Yes | Yes |
| `clear()` | Yes | - | - | Yes | Yes |
| `check()` | Yes | Yes | Yes | Yes | - |
| `uncheck()` | Yes | Yes | Yes | Yes | - |
| `textContent()` | - | - | - | - | - |
| `isVisible()` | - | - | - | - | - |
| `count()` | - | - | - | - | - |
| `waitFor()` | - | - | - | - | - |

---

## 13. Locator Strategy Mapping Reference

Complete mapping from locators.json strategy types to Playwright selectors.

**Reference**: [playwright.dev/java/docs/locators](https://playwright.dev/java/docs/locators)

### Existing strategies (from locators.json)

| Element | Strategy | Value (locators.json) | Playwright Locator |
| --- | --- | --- | --- |
| usernameInput | id | `username` | `page.locator("id=username")` |
| usernameInput | name | `username` | `page.locator("[name='username']")` |
| usernameInput | css | `input[type='text'][name='username']` | `page.locator("input[type='text'][name='username']")` |
| usernameInput | xpath | `//input[@id='username' or @name='username']` | `page.locator("xpath=//input[@id='username' or @name='username']")` |
| loginButton | css | `button[type='submit']` | `page.locator("button[type='submit']")` |
| loginButton | css | `button.radius` | `page.locator("button.radius")` |
| loginButton | xpath | `//button[@type='submit']` | `page.locator("xpath=//button[@type='submit']")` |
| checkbox1 | css | `#checkboxes input:first-of-type` | `page.locator("#checkboxes input:first-of-type")` |
| checkbox1 | xpath | `(//form[@id='checkboxes']/input)[1]` | `page.locator("xpath=(//form[@id='checkboxes']/input)[1]")` |
| homepageLoginLink | css | `a[href='/login']` | `page.locator("a[href='/login']")` |
| homepageLoginLink | xpath | `//a[text()='Form Authentication']` | `page.locator("xpath=//a[text()='Form Authentication']")` |

### New Playwright-native strategies (can be added to locators.json)

| Strategy Type | Example Value | Playwright Locator | When to Use |
| --- | --- | --- | --- |
| `role` | `BUTTON` | `page.getByRole(AriaRole.BUTTON)` | Most resilient -- survives DOM restructuring |
| `label` | `Username` | `page.getByLabel("Username")` | Form inputs with visible labels |
| `placeholder` | `Enter username` | `page.getByPlaceholder("Enter username")` | Inputs with placeholder text |
| `testId` | `login-btn` | `page.getByTestId("login-btn")` | Elements with data-testid attributes |
| `text` | `Log in` | `page.getByText("Log in")` | Elements with visible text |

### Locator priority recommendation (most to least resilient)

1. `role` -- ARIA role-based, survives CSS/class changes
2. `testId` -- Explicit test IDs, developers control stability
3. `label` / `placeholder` -- User-visible text, stable if UX unchanged
4. `id` -- Unique IDs, very stable when present
5. `css` -- Class/attribute-based, moderate stability
6. `xpath` -- Full path queries, least stable but most flexible

---

## Official Documentation Links

- [Playwright Java -- Getting Started](https://playwright.dev/java/docs/intro)
- [Playwright Java -- API Reference](https://playwright.dev/java/docs/api/class-playwright)
- [Playwright -- Locators Guide](https://playwright.dev/java/docs/locators)
- [Playwright -- Actionability Checks](https://playwright.dev/java/docs/actionability)
- [Playwright -- Auto-waiting](https://playwright.dev/java/docs/actionability)
- [Playwright -- Trace Viewer](https://playwright.dev/java/docs/trace-viewer-intro)
- [Playwright -- Page Object Model](https://playwright.dev/java/docs/pom)
- [Playwright -- Screenshots](https://playwright.dev/java/docs/screenshots)
- [Playwright -- Videos](https://playwright.dev/java/docs/videos)
- [Playwright -- Browsers](https://playwright.dev/java/docs/browsers)
- [Playwright -- Best Practices](https://playwright.dev/docs/best-practices)
- [Cucumber -- Java Documentation](https://cucumber.io/docs/cucumber/)
