# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
mvn clean install                # Build the project
mvn test                         # Run all tests
mvn test -Dcucumber.options="src/test/resources/features/login.feature"  # Run specific feature
mvn test -Dcucumber.options="--tags @smoke"                              # Run by tag
mvn verify                       # Run tests + generate reports
mvn clean test-compile           # Compile only (no tests)
```

**Prerequisites:** Java 21+, Maven 3.6+, Node.js 16+, and `npm install -g @executeautomation/mcp-selenium`.

## Architecture

This is a Selenium test framework where Java communicates with a Node.js MCP Selenium server (`@executeautomation/mcp-selenium`) via **JSON-RPC 2.0 over stdio**. The layered call flow:

```
Cucumber BDD (.feature files)
  → Step Definitions (stepdefinitions/)
    → Page Objects (pages/ extending BasePage)
      → SelfHealingElement (healing/) — retry/fallback chain
        → LocatorRegistry (locators/) — multi-strategy, JSON-persisted
          → MCPSeleniumClient (mcp/) — JSON-RPC 2.0 stdio bridge
            → npx @executeautomation/mcp-selenium (Node.js process)
              → Browser
```

### Core Concepts

- **Self-Healing Locators**: Each element has multiple `LocatorStrategy` entries (id, css, xpath, etc.) with priorities. `SelfHealingElement.findElementWithHealing()` tries strategies sorted by success rate (higher wins; on tie, lower priority number wins). Each attempt retries up to 3 times with 500ms delay. Success/failure counts persist to `locators.json`.

- **MCP Client**: `MCPSeleniumClient` starts the Node.js MCP server as a subprocess, sends JSON-RPC requests via stdin, reads responses from stdout. 30s timeout, 100ms polling interval.

- **Page Object Model**: Pages extend `BasePage`, define element name constants (`public static final String`), implement `initializeLocators()` to register strategies, and use `element("name")` to get a `SelfHealingElement`.

- **Test Lifecycle**: `@Before` hook in step definitions creates MCPSeleniumClient → starts MCP server → starts browser → loads LocatorRegistry from JSON → creates page objects. `@After` prints metrics, saves registry, closes browser, stops server.

## Key Patterns

| Pattern | Implementation |
|---|---|
| Page Object Model | `BasePage` + page subclasses with element constants |
| Chain of Responsibility | Locator fallback chain in `SelfHealingElement` |
| Strategy | `LocatorStrategy` objects selected dynamically by success rate |
| Registry | `LocatorRegistry` — in-memory + JSON persistence |
| Decorator | `SelfHealingElement` wraps all browser actions with healing |

## Key Files

- `src/main/java/framework/mcp/MCPSeleniumClient.java` — JSON-RPC client; all browser commands
- `src/main/java/framework/locators/LocatorRegistry.java` — element→strategies store, JSON serialization
- `src/main/java/framework/healing/SelfHealingElement.java` — core healing algorithm
- `src/main/java/framework/healing/HealingMetrics.java` — per-element success tracking, report generation
- `src/main/java/framework/pages/BasePage.java` — POM base: `element()`, `navigateTo()`, `assertElementVisible()`
- `src/test/resources/locators/locators.json` — locator configuration (read at start, written at end with updated stats)

## Adding New Pages

1. Create class extending `BasePage` with element name constants
2. Implement `initializeLocators()` with 3-4 strategies per element (id, css, xpath, etc.)
3. Add action methods that use `element("name").click()`, `.typeText()`, `.getText()`
4. Register locators in step definition `@Before` hook

## Known Issue

No Cucumber runner class exists yet (`*Runner.java`). The Maven Surefire plugin is configured to find `**/*Runner.java` but none exist, so `mvn test` currently discovers 0 tests. A `RunCucumberTest.java` with `@CucumberOptions` is needed in `src/test/java/`.
