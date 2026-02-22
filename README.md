# Selenium-MCP Java Test Framework

> **AI-Powered Test Automation with Self-Healing Capabilities**

A test automation framework that combines **Selenium**, **MCP (Model Context Protocol)**, and **Cucumber BDD** to deliver self-healing locator management and AI-assisted test generation. Java communicates with a Node.js MCP Selenium server via JSON-RPC 2.0 over stdio, providing automatic locator fallback and learning from every test execution.

---

## Key Features

- **Self-Healing Locators**: Automatic fallback through multiple locator strategies, sorted by success rate
- **Intelligent Learning**: Tracks success/failure per locator strategy and optimizes selection over time
- **Zero API Costs**: Uses a local MCP Selenium server (`@executeautomation/mcp-selenium`) — no cloud API keys needed
- **BDD with Cucumber**: Full Gherkin scenario support with tagged execution
- **Comprehensive Metrics**: Detailed per-element healing reports identifying brittle and robust elements
- **Page Object Model**: Clean POM architecture with `BasePage` inheritance and element name constants
- **AI-Assisted Generation**: Works with Claude Code, Cline, and Amazon Q for generating test artifacts

---

## Architecture

```text
Cucumber BDD (.feature files)
  -> Step Definitions (stepdefinitions/)
    -> Page Objects (pages/ extending BasePage)
      -> SelfHealingElement (healing/) -- retry/fallback chain
        -> LocatorRegistry (locators/) -- multi-strategy, JSON-persisted
          -> MCPSeleniumClient (mcp/) -- JSON-RPC 2.0 stdio bridge
            -> npx @executeautomation/mcp-selenium (Node.js process)
              -> Browser (Chrome, Firefox)
```

### Design Patterns

| Pattern | Implementation |
| --- | --- |
| Page Object Model | `BasePage` + page subclasses with element constants |
| Chain of Responsibility | Locator fallback chain in `SelfHealingElement` |
| Strategy | `LocatorStrategy` objects selected dynamically by success rate |
| Registry | `LocatorRegistry` — in-memory + JSON persistence |
| Decorator | `SelfHealingElement` wraps all browser actions with healing |

---

## Quick Start

### Prerequisites

- **Java 21+**
- **Maven 3.6+**
- **Node.js 16+**
- **npm**

### Installation

1. **Clone the repository**:

   ```bash
   git clone <repository-url>
   cd selenium-mcp-framework
   ```

2. **Install MCP Selenium**:

   ```bash
   npm install -g @executeautomation/mcp-selenium
   ```

3. **Build the project**:

   ```bash
   mvn clean install
   ```

4. **Run tests**:

   ```bash
   mvn test
   ```

---

## Project Structure

```text
selenium-mcp-framework/
├── src/
│   ├── main/java/framework/
│   │   ├── mcp/
│   │   │   └── MCPSeleniumClient.java          # JSON-RPC 2.0 client for MCP server
│   │   ├── locators/
│   │   │   └── LocatorRegistry.java            # Element-to-strategies store, JSON persistence
│   │   ├── healing/
│   │   │   ├── SelfHealingElement.java         # Core healing algorithm with retry logic
│   │   │   └── HealingMetrics.java             # Per-element performance tracking and reports
│   │   └── pages/
│   │       ├── BasePage.java                   # POM base class (element(), navigateTo(), assertions)
│   │       ├── LoginPage.java                  # Login page (the-internet.herokuapp.com/login)
│   │       ├── CheckboxPage.java               # Checkboxes page (elements without IDs)
│   │       └── AddRemoveElementsPage.java      # Add/Remove Elements page (dynamic elements)
│   └── test/
│       ├── java/
│       │   ├── runners/
│       │   │   └── RunCucumberTest.java        # Cucumber JUnit runner
│       │   └── stepdefinitions/
│       │       ├── LoginSteps.java             # Login feature step definitions
│       │       ├── CheckboxSteps.java          # Checkbox feature step definitions
│       │       └── AddRemoveElementsSteps.java # Add/Remove feature step definitions
│       └── resources/
│           ├── features/
│           │   ├── login.feature               # Login scenarios (3 scenarios)
│           │   ├── checkbox.feature            # Checkbox scenarios (3 scenarios)
│           │   └── add_remove_elements.feature # Add/Remove scenarios (9 scenarios)
│           └── locators/
│               └── locators.json              # Locator configuration with success stats
├── pom.xml
├── CLAUDE.md
└── README.md
```

---

## Test Coverage

All tests target [the-internet.herokuapp.com](https://the-internet.herokuapp.com). There are **15 scenarios** across 3 feature files:

### Login (`@login` — 3 scenarios)

- Successful login with valid credentials
- Login fails with invalid password
- Login fails with non-existent user

### Checkboxes (`@checkbox` — 3 scenarios)

- Verify checkboxes page displays correct heading
- Toggle first checkbox on and off
- Toggle second checkbox off and on

### Add/Remove Elements (`@add_remove` — 9 scenarios)

- Verify page heading
- Add single and multiple elements
- Delete elements and verify count changes
- No delete buttons present initially
- Add, delete, and add more elements
- Delete all elements one by one
- Delete from middle shifts remaining

---

## How It Works

### Self-Healing Element Discovery

When you interact with an element, the framework:

1. **Retrieves all registered locator strategies** for that element
2. **Sorts strategies by success rate** (higher rate wins; on tie, lower priority number wins)
3. **Tries each strategy** with up to 3 retries and 500ms delay between retries
4. **Records success/failure** counts to the strategy for continuous learning
5. **Persists stats to locators.json** at test teardown for future runs

### Example

```java
// Traditional (brittle):
driver.findElement(By.id("login-btn")).click();

// Self-healing:
element("loginButton").click();
```

Behind the scenes, the framework tries strategies in success-rate order:

1. `css=button.login-button` (95% success rate)
2. `css=.radius` (if #1 fails)
3. `xpath=//button[@type='submit']` (if #2 fails)
4. `css=.fa-sign-in` (if #3 fails)

---

## Key Files

| File | Purpose |
| --- | --- |
| `MCPSeleniumClient.java` | Starts Node.js MCP server as subprocess, sends JSON-RPC requests via stdin, reads responses from stdout. 30s timeout. |
| `LocatorRegistry.java` | Stores element-to-strategies mappings, tracks success/failure counts, loads/saves to JSON. |
| `SelfHealingElement.java` | Core healing algorithm: tries strategies by success rate, retries 3x per strategy with 500ms delay. |
| `HealingMetrics.java` | Aggregates per-element stats, identifies brittle (<80%) and robust (100%) elements, generates reports. |
| `BasePage.java` | POM base with `element()`, `navigateTo()`, `waitForElementVisible()`, `assertElementVisible()`. Base URL: the-internet.herokuapp.com. |

---

## Adding a New Page

1. **Create a page class** extending `BasePage` with element name constants:

```java
public class DashboardPage extends BasePage {

    public static final String WELCOME_MESSAGE = "welcomeMessage";
    public static final String LOGOUT_BUTTON = "logoutButton";

    public DashboardPage(MCPSeleniumClient mcp,
                         LocatorRegistry registry,
                         HealingMetrics metrics) {
        super(mcp, registry, metrics);
    }

    public void initializeLocators() {
        locatorRegistry.addElement(WELCOME_MESSAGE,
            new LocatorStrategy("id", "welcome", 1),
            new LocatorStrategy("css", ".welcome-msg", 2),
            new LocatorStrategy("xpath", "//h1[@class='welcome']", 3)
        );

        locatorRegistry.addElement(LOGOUT_BUTTON,
            new LocatorStrategy("id", "logout-btn", 1),
            new LocatorStrategy("css", "button.logout", 2),
            new LocatorStrategy("xpath", "//button[text()='Logout']", 3)
        );
    }

    public String getWelcomeMessage() throws IOException {
        return element(WELCOME_MESSAGE).getText();
    }

    public void clickLogout() throws IOException {
        element(LOGOUT_BUTTON).click();
    }
}
```

1. **Create step definitions** with `@Before`/`@After` hooks tagged to your feature.

1. **Register locators** in the `@Before` hook: create MCPSeleniumClient, start browser, load registry, initialize page, call `initializeLocators()`.

1. **Write a Gherkin feature** file tagged with your hook tag.

---

## Running Tests

```bash
# Run all tests
mvn test

# Run a specific feature file
mvn test -Dcucumber.options="src/test/resources/features/login.feature"

# Run by tag
mvn test -Dcucumber.options="--tags @smoke"

# Build and generate reports
mvn verify

# Compile only (no tests)
mvn clean test-compile
```

---

## Metrics and Reporting

The framework tracks per-element and per-locator statistics. After each test run, call `printHealingReport()` to see:

```text
=== SELF-HEALING METRICS REPORT ===

OVERALL STATISTICS:
   Total healing attempts: 45
   Successful heals: 42
   Failed heals: 3
   Overall success rate: 93.3%

ELEMENT-BY-ELEMENT BREAKDOWN:

  loginButton
   Success Rate: 100.0% (15/15 attempts)
   Most Reliable Locator: css=button.login-button
   Locator Performance:
      css=button.login-button (100.0% - 15/15)
      css=.radius (0.0% - 0/0)

  usernameInput
   Success Rate: 86.7% (13/15 attempts)
   Most Reliable Locator: id=username
   Locator Performance:
      id=username (92.3% - 12/13)
      css=input[name='username'] (50.0% - 1/2)

ROBUST ELEMENTS (100% success):
   loginButton
   passwordInput

BRITTLE ELEMENTS (<80% success):
   errorMessage - 66.7%
```

---

## Configuration

### Locator Registry (locators.json)

Each element has multiple fallback strategies with priority and tracked success stats:

```json
{
  "loginButton": [
    {
      "strategy": "css",
      "value": "button.login-button",
      "priority": 1,
      "successCount": 1,
      "failureCount": 0,
      "successRate": 100.0
    },
    {
      "strategy": "css",
      "value": ".radius",
      "priority": 2,
      "successCount": 0,
      "failureCount": 0,
      "successRate": 0.0
    }
  ]
}
```

The registry currently tracks **13 elements** with **41 locator strategies** across all page objects.

### Browser Configuration

```java
// Chrome (default)
mcp.startBrowser("chrome", false);

// Chrome headless
mcp.startBrowser("chrome", true);

// Firefox
mcp.startBrowser("firefox", false);
```

---

## Troubleshooting

### MCP server fails to start

Ensure MCP Selenium is installed globally:

```bash
npm install -g @executeautomation/mcp-selenium
```

### Elements not found

Check locator registry and add more strategies:

```java
locatorRegistry.printRegistry();
```

### Low success rates

Review the metrics report and update locators for brittle elements:

```java
healingMetrics.printHealingReport();
List<String> brittleElements = healingMetrics.getBrittleElements(80.0);
```

---

## Dependencies

| Dependency | Version | Purpose |
| --- | --- | --- |
| Selenium WebDriver | 4.15.0 | Browser automation |
| Cucumber (Java + JUnit) | 7.14.0 | BDD framework |
| Jackson (Databind + Core) | 2.15.3 | JSON processing for locators and JSON-RPC |
| JUnit 4 | 4.13.2 | Test runner |

---

## Additional Resources

- [MCP Selenium](https://github.com/executeautomation/mcp-selenium) — The MCP server this framework communicates with
- [Cucumber Documentation](https://cucumber.io/docs/cucumber/) — BDD framework
- [Selenium Documentation](https://www.selenium.dev/documentation/) — Browser automation

---

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

---

## License

This project is licensed under the MIT License.
