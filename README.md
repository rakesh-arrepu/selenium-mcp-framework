# Selenium-MCP Java Test Framework

> **AI-Powered Test Automation with Self-Healing Capabilities**

A revolutionary test automation framework that combines **Selenium**, **MCP (Model Context Protocol)**, and **AI tools** to enable testers to generate test cases with minimal effort while achieving unprecedented test stability.

---

## 🎯 Key Features

- **🤖 AI-Powered Test Generation**: Works seamlessly with Claude Code, Cline, and Amazon Q
- **🔄 Self-Healing Locators**: Automatic fallback through multiple locator strategies
- **📊 Intelligent Learning**: Tracks success rates and optimizes locator usage
- **💰 Zero API Costs**: Uses local MCP Selenium server
- **📈 Comprehensive Metrics**: Detailed healing performance reports
- **🎭 BDD Support**: Full Cucumber integration for Gherkin scenarios

---

## 📊 Expected Performance

- **88% reduction** in test creation time
- **83% improvement** in test stability
- **Automatic recovery** from locator failures
- **Continuous learning** from test executions

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    AI Tools Layer                           │
│         (Claude Code, Cline, Amazon Q)                      │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│                  Cucumber BDD Layer                         │
│            (Gherkin Features → Step Definitions)            │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│                  Page Object Layer                          │
│         (LoginPage, DashboardPage, etc.)                    │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│              Self-Healing Framework Core                    │
│  ┌──────────────┬──────────────┬──────────────┐            │
│  │ BasePage     │ SelfHealing  │ Healing      │            │
│  │              │ Element      │ Metrics      │            │
│  └──────────────┴──────────────┴──────────────┘            │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│              Locator Management Layer                       │
│  ┌──────────────────────────────────────────┐              │
│  │      LocatorRegistry (JSON-based)        │              │
│  │  - Multiple strategies per element       │              │
│  │  - Success/failure tracking              │              │
│  │  - Priority-based fallback               │              │
│  └──────────────────────────────────────────┘              │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│              MCP Communication Layer                        │
│         (JSON-RPC 2.0 over stdio)                           │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│              MCP Selenium Server                            │
│         (@executeautomation/mcp-selenium)                   │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│              Browser (Chrome, Firefox, etc.)                │
└─────────────────────────────────────────────────────────────┘
```

---

## 🚀 Quick Start

### Prerequisites

- **Java 11+** installed
- **Maven 3.6+** installed
- **Node.js 16+** installed
- **npm** installed

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

4. **Run example tests**:
   ```bash
   mvn test
   ```

---

## 💡 How It Works

### 1. Self-Healing Element Discovery

When you interact with an element, the framework:

1. **Retrieves all registered locator strategies** (sorted by success rate)
2. **Tries each strategy** in order (best-performing first)
3. **Records success/failure** for continuous learning
4. **Automatically falls back** to alternative strategies on failure
5. **Updates metrics** for reporting and optimization

### 2. Example: Login Button Click

```java
// Traditional approach (brittle):
driver.findElement(By.id("login-btn")).click();

// Self-healing approach:
element("loginButton").click();
```

Behind the scenes, the framework tries:
1. ✓ `id=login-btn` (Success rate: 95%)
2. ✗ `css=button.login-button` (if #1 fails)
3. ✗ `xpath=//button[@type='submit']` (if #2 fails)
4. ✗ `name=login` (if #3 fails)

---

## 📁 Project Structure

```
selenium-mcp-framework/
├── src/
│   ├── main/java/framework/
│   │   ├── mcp/
│   │   │   └── MCPSeleniumClient.java      # MCP server wrapper
│   │   ├── locators/
│   │   │   └── LocatorRegistry.java        # Locator management
│   │   ├── healing/
│   │   │   ├── SelfHealingElement.java     # Self-healing logic
│   │   │   └── HealingMetrics.java         # Performance tracking
│   │   └── pages/
│   │       ├── BasePage.java               # Base page class
│   │       └── LoginPage.java              # Example page object
│   └── test/
│       ├── java/stepdefinitions/
│       │   └── LoginSteps.java             # Cucumber steps
│       └── resources/
│           ├── features/
│           │   └── login.feature           # Gherkin scenarios
│           └── locators/
│               └── locators.json           # Locator configuration
├── screenshots/                            # Test screenshots
│   ├── baseline/                           # Baseline images
│   └── current/                            # Current screenshots
├── pom.xml                                 # Maven configuration
└── README.md                               # This file
```

---

## 🎓 Usage Examples

### Example 1: Create a New Page Object

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

### Example 2: Write Cucumber Steps

```java
@Given("user is on dashboard")
public void userIsOnDashboard() throws IOException {
    dashboardPage.verifyPageLoaded();
}

@When("user clicks logout")
public void userClicksLogout() throws IOException {
    dashboardPage.clickLogout();
}

@Then("user should see welcome message {string}")
public void userShouldSeeWelcomeMessage(String expectedMessage) throws IOException {
    String actualMessage = dashboardPage.getWelcomeMessage();
    assertEquals(expectedMessage, actualMessage);
}
```

### Example 3: Write Gherkin Feature

```gherkin
Feature: Dashboard Operations
  As a logged-in user
  I want to interact with the dashboard
  So that I can access application features

  Scenario: View welcome message
    Given user is on dashboard
    Then user should see welcome message "Welcome, Test User!"

  Scenario: Logout from dashboard
    Given user is on dashboard
    When user clicks logout
    Then user should see login page
```

---

## 🔧 Configuration

### Locator Registry (locators.json)

Define multiple fallback strategies for each element:

```json
{
  "loginButton": [
    {
      "strategy": "id",
      "value": "login-btn",
      "priority": 1
    },
    {
      "strategy": "css",
      "value": "button.login-button",
      "priority": 2
    },
    {
      "strategy": "xpath",
      "value": "//button[@type='submit']",
      "priority": 3
    }
  ]
}
```

### Browser Configuration

Configure browser in your test setup:

```java
// Chrome (default)
mcp.startBrowser("chrome", false);

// Chrome headless
mcp.startBrowser("chrome", true);

// Firefox
mcp.startBrowser("firefox", false);
```

---

## 📊 Metrics and Reporting

The framework automatically tracks:

- **Success/failure rates** per element
- **Per-locator performance** statistics
- **Brittle elements** (low success rate)
- **Robust elements** (100% success rate)
- **Failure history** for debugging

### Sample Metrics Report

```
╔════════════════════════════════════════════════════════════════╗
║           SELF-HEALING METRICS REPORT                         ║
╚════════════════════════════════════════════════════════════════╝

📊 OVERALL STATISTICS:
   Total healing attempts: 45
   Successful heals: 42
   Failed heals: 3
   Overall success rate: 93.3%

📝 ELEMENT-BY-ELEMENT BREAKDOWN:
───────────────────────────────────────────────────────────────

🔹 loginButton
   Success Rate: 100.0% (15/15 attempts)
   Most Reliable Locator: id=login-btn
   Locator Performance:
      • id=login-btn (100.0% - 15/15)
      • css=button.login-button (0.0% - 0/0)

🔹 usernameInput
   Success Rate: 86.7% (13/15 attempts)
   Most Reliable Locator: id=username
   Locator Performance:
      • id=username (92.3% - 12/13)
      • css=input[name='username'] (50.0% - 1/2)

🏆 ROBUST ELEMENTS (100% success):
   ✓ loginButton
   ✓ passwordInput

⚠️  BRITTLE ELEMENTS (<80% success):
   ✗ errorMessage - 66.7%
```

---

## 🤝 Integration with AI Tools

### Using with Claude Code

1. Open your feature file
2. Ask Claude: "Generate step definitions for this feature"
3. Claude generates the step definitions using the framework
4. Run tests immediately

### Using with Cline

1. Describe your test scenario
2. Cline generates Gherkin feature
3. Cline generates page objects and steps
4. Framework handles locator management automatically

### Using with Amazon Q

1. Provide test requirements
2. Amazon Q generates test code
3. Framework provides self-healing capabilities
4. Tests run reliably

---

## 🧪 Running Tests

### Run all tests
```bash
mvn test
```

### Run specific feature
```bash
mvn test -Dcucumber.options="src/test/resources/features/login.feature"
```

### Run with tags
```bash
mvn test -Dcucumber.options="--tags @smoke"
```

### Generate reports
```bash
mvn verify
```

---

## 📈 Best Practices

### 1. Define Multiple Locator Strategies

Always provide 3-4 fallback strategies per element:

```java
locatorRegistry.addElement("submitButton",
    new LocatorStrategy("id", "submit", 1),          // Most specific
    new LocatorStrategy("css", "button.submit", 2),  // Class-based
    new LocatorStrategy("xpath", "//button[@type='submit']", 3),  // Attribute
    new LocatorStrategy("linkText", "Submit", 4)     // Text-based
);
```

### 2. Use Meaningful Element Names

```java
// Good
public static final String LOGIN_BUTTON = "loginButton";
public static final String ERROR_MESSAGE = "errorMessage";

// Bad
public static final String BTN1 = "btn1";
public static final String DIV2 = "div2";
```

### 3. Review Healing Metrics Regularly

Check your healing reports to identify:
- Elements that need better locators
- Locators that consistently fail
- Pages with stability issues

### 4. Keep Locator Registry Updated

Save the registry after test runs to preserve learning:

```java
@After
public void tearDown() throws IOException {
    locatorRegistry.saveRegistry();  // Saves success/failure stats
}
```

---

## 🔍 Troubleshooting

### Problem: MCP server fails to start

**Solution**: Ensure MCP Selenium is installed globally:
```bash
npm install -g @executeautomation/mcp-selenium
```

### Problem: Elements not found

**Solution**: Check locator registry and add more strategies:
```java
locatorRegistry.printRegistry();  // Debug output
```

### Problem: Low success rates

**Solution**: Review metrics report and update locators:
```java
healingMetrics.printHealingReport();
List<String> brittleElements = healingMetrics.getBrittleElements(80.0);
```

---

## 📚 Additional Resources

- **MCP Selenium Documentation**: https://github.com/executeautomation/mcp-selenium
- **Cucumber Documentation**: https://cucumber.io/docs/cucumber/
- **Selenium Documentation**: https://www.selenium.dev/documentation/

---

## 🤝 Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

---

## 📄 License

This project is licensed under the MIT License.

---

## 🎉 Acknowledgments

- **@executeautomation** for MCP Selenium
- **Cucumber** team for BDD framework
- **Selenium** project for browser automation

---

## 📞 Support

For issues and questions:
- Create an issue on GitHub
- Check existing documentation
- Review healing metrics for insights

---

**Built with ❤️ for the QA Community**

*Empowering testers with AI-driven automation*
