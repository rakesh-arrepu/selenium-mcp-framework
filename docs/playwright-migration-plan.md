# Selenium-MCP to Playwright Java Migration Plan

## Context

The current Selenium-MCP framework at `selenium-mcp-framework/` communicates with browsers through a **Node.js MCP Selenium server via JSON-RPC 2.0 over stdio**. This migration creates a **brand-new Playwright Java project** in a separate repository, preserving the self-healing locator system while eliminating the MCP intermediary layer. **No files in the existing repo are modified or deleted.**

---

## Source to Target Project Mapping

```text
SOURCE (leave untouched):                  TARGET (new project):
selenium-mcp-framework/                    playwright-self-healing-framework/

src/main/java/framework/                   src/main/java/framework/
  mcp/MCPSeleniumClient.java        ->       playwright/PlaywrightEngine.java      [REPLACED]
                                             playwright/PlaywrightConfig.java      [NEW]
  locators/LocatorRegistry.java      ->       locators/LocatorRegistry.java         [COPIED + ENHANCED]
  healing/SelfHealingElement.java    ->       healing/SelfHealingElement.java       [REWRITTEN for Playwright]
  healing/HealingMetrics.java        ->       healing/HealingMetrics.java           [COPIED AS-IS]
  pages/BasePage.java                ->       pages/BasePage.java                   [REWRITTEN for Playwright]
  pages/LoginPage.java              ->       pages/LoginPage.java                  [MIGRATED - constructor change]
  pages/CheckboxPage.java           ->       pages/CheckboxPage.java               [MIGRATED - constructor change]
  pages/AddRemoveElementsPage.java  ->       pages/AddRemoveElementsPage.java      [MIGRATED - Playwright calls]

src/test/java/                              src/test/java/
  runners/RunCucumberTest.java       ->       runners/RunCucumberTest.java          [COPIED AS-IS]
  stepdefinitions/LoginSteps.java    ->       stepdefinitions/LoginSteps.java       [MIGRATED - Playwright hooks]
  stepdefinitions/CheckboxSteps.java ->       stepdefinitions/CheckboxSteps.java    [MIGRATED - Playwright hooks]
  stepdefinitions/AddRemoveSteps.java->       stepdefinitions/AddRemoveSteps.java   [MIGRATED - Playwright hooks]

src/test/resources/                         src/test/resources/
  features/login.feature             ->       features/login.feature                [COPIED AS-IS]
  features/checkbox.feature          ->       features/checkbox.feature             [COPIED AS-IS]
  features/add_remove_elements.feature->      features/add_remove_elements.feature  [COPIED AS-IS]
  locators/locators.json             ->       locators/locators.json                [COPIED AS-IS]

pom.xml                              ->     pom.xml                                [NEW - Playwright deps]
```

---

## Architecture: Before and After

```text
SELENIUM-MCP (existing repo):              PLAYWRIGHT (new repo):

Cucumber BDD (.feature files)              Cucumber BDD (.feature files)        [IDENTICAL]
  -> Step Definitions                        -> Step Definitions                 [NEW hooks]
    -> Page Objects (BasePage)                 -> Page Objects (BasePage)         [PlaywrightEngine]
      -> SelfHealingElement                      -> SelfHealingElement           [Locator.waitFor probe]
        -> LocatorRegistry                         -> LocatorRegistry            [SAME JSON format]
          -> MCPSeleniumClient                       -> PlaywrightEngine
            -> Node.js MCP server (JSON-RPC)           -> Playwright (direct CDP/WebSocket)
              -> Browser                                 -> Browser
```

---

## Implementation Steps

### Phase 1: New Project Scaffolding

#### Step 1 -- Create new project directory and initialize

```bash
mkdir -p ~/code/playwright-self-healing-framework
cd ~/code/playwright-self-healing-framework
git init
```

Create the directory structure:

```text
src/main/java/framework/playwright/
src/main/java/framework/locators/
src/main/java/framework/healing/
src/main/java/framework/pages/
src/test/java/runners/
src/test/java/stepdefinitions/
src/test/resources/features/
src/test/resources/locators/
```

#### Step 2 -- Create pom.xml with Playwright dependencies

```xml
<groupId>com.qatesting</groupId>
<artifactId>playwright-self-healing-framework</artifactId>
<version>1.0.0</version>
```

Dependencies:

- `com.microsoft.playwright:playwright:1.50.0` (replaces `selenium-java`)
- `io.cucumber:cucumber-java:7.14.0` (same)
- `io.cucumber:cucumber-junit:7.14.0` (same)
- `com.fasterxml.jackson.core:jackson-databind:2.15.3` (same)
- `com.fasterxml.jackson.core:jackson-core:2.15.3` (same)
- `com.fasterxml.jackson.core:jackson-annotations:2.15.3` (same)
- `junit:junit:4.13.2` (same)

Plugins:

- `maven-compiler-plugin` (release 21)
- `maven-surefire-plugin` (includes `**/*Test.java`, `**/*Runner.java`)
- `exec-maven-plugin` -- auto-install Playwright browsers during `generate-test-resources` phase:

  ```xml
  <mainClass>com.microsoft.playwright.CLI</mainClass>
  <arguments><argument>install</argument><argument>--with-deps</argument></arguments>
  ```

#### Step 3 -- Install Playwright browsers

```bash
mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install --with-deps"
```

Validate with `mvn clean compile`.

---

### Phase 2: Core Engine (replaces MCPSeleniumClient)

#### Step 4 -- Create PlaywrightEngine.java (~150 lines)

Path: `src/main/java/framework/playwright/PlaywrightEngine.java`

This replaces the 620-line MCPSeleniumClient. Key responsibilities:

| Method | Replaces (MCP) | Implementation |
| --- | --- | --- |
| `initialize()` | `startMCPServer()` + `startBrowser()` | Creates Playwright, launches Browser, creates BrowserContext (with optional video dir), creates Page |
| `shutdown()` | `closeBrowser()` + `stopMCPServer()` | Closes Page, Context, Browser, Playwright |
| `resolveLocator(LocatorStrategy)` | *(new -- the critical bridge)* | Converts LocatorStrategy into Playwright Locator (see mapping below) |
| `navigate(url)` | `mcp.navigate(url)` | `page.navigate(url)` |
| `takeScreenshot(path)` | `mcp.takeScreenshot(path)` | `page.screenshot(options)` |
| `pressKey(key)` | `mcp.pressKey(key)` | `page.keyboard().press(key)` |
| `startTracing(name)` | *(new capability)* | `context.tracing().start(options)` |
| `stopTracing(path)` | *(new capability)* | `context.tracing().stop(options)` |
| `getPage()` | *(new -- for dynamic elements)* | Returns the Page for direct locator access |

#### Strategy-to-Locator Mapping (inside resolveLocator)

| LocatorStrategy type | Playwright Locator |
| --- | --- |
| `id` | `page.locator("id=" + value)` |
| `css` | `page.locator(value)` |
| `xpath` | `page.locator("xpath=" + value)` |
| `name` | `page.locator("[name='" + value + "']")` |
| `className` | `page.locator("." + value)` |
| `tagName` | `page.locator(value)` |
| `linkText` | `page.getByText(value, exact=true)` |
| `partialLinkText` | `page.getByText(value)` |
| `role` *(new)* | `page.getByRole(AriaRole.valueOf(value))` |
| `label` *(new)* | `page.getByLabel(value)` |
| `placeholder` *(new)* | `page.getByPlaceholder(value)` |
| `testId` *(new)* | `page.getByTestId(value)` |
| `text` *(new)* | `page.getByText(value)` |

Browser type mapping in `initialize()`:

- `"chromium"` or `"chrome"` -> `playwright.chromium().launch()`
- `"firefox"` -> `playwright.firefox().launch()`
- `"webkit"` or `"safari"` -> `playwright.webkit().launch()`

#### Step 5 -- Create PlaywrightConfig.java (~30 lines)

Path: `src/main/java/framework/playwright/PlaywrightConfig.java`

System property-based configuration, enabling:

```bash
mvn test -Dbrowser=firefox -Dheadless=true -Dtracing=true -Dvideo=false
```

Properties: `browser` (default: "chromium"), `headless` (default: false), `tracing` (default: true), `video` (default: false), `timeout` (default: 30000).

---

### Phase 3: Locator and Healing Layer

#### Step 6 -- Copy and enhance LocatorRegistry.java

Copy from source project. Only enhancement:

- Add `VALID_STRATEGIES` constant documenting all supported types (original 8 + 5 new Playwright-native types)
- No structural changes -- the LocatorStrategy, ElementLocator, success/failure tracking, JSON load/save, and sorting by success rate are all **identical**
- The existing `locators.json` is 100% format-compatible

#### Step 7 -- Rewrite SelfHealingElement.java for Playwright Locator

This is the most significant change. The healing algorithm transforms:

Before (MCP):

```text
findElementWithHealing():
  for each strategy (sorted by success rate):
    for attempt 1..3:
      mcp.findElement(strategy, value, 5000ms)  <- JSON-RPC round-trip
      on success: record, return LocatorStrategy
      on failure: sleep(500ms), retry
  all failed: return null

click():
  strategy = findElementWithHealing()
  mcp.clickElement(strategy.type, strategy.value)  <- separate JSON-RPC call
```

After (Playwright):

```text
findElementWithHealing():
  for each strategy (sorted by success rate):
    for attempt 1..3:
      locator = engine.resolveLocator(strategy)
      locator.waitFor(timeout=3000ms)  <- DOM probe via Playwright
      on success: record, return Locator object
      on TimeoutError: sleep(300ms), retry
  all failed: return null

click():
  locator = findElementWithHealing()
  locator.click()  <- Playwright auto-waits for actionability
```

Key differences:

- Returns a `Locator` object (not `LocatorStrategy`) -- the caller acts on it directly
- Probe timeout reduced from 5000ms to 3000ms (Playwright is faster than MCP round-trips)
- Retry delay reduced from 500ms to 300ms
- `MAX_RETRY_ATTEMPTS` stays at 3

Action method transformations:

| Method | MCP call | Playwright call |
| --- | --- | --- |
| `click()` | `mcp.clickElement(strategy, value)` | `locator.click()` |
| `typeText(text)` | `mcp.sendKeys(strategy, value, text)` | `locator.fill(text)` |
| `getText()` | `mcp.getElementText(strategy, value)` | `locator.textContent()` |
| `hover()` | `mcp.hoverElement(strategy, value)` | `locator.hover()` |
| `doubleClick()` | `mcp.doubleClick(strategy, value)` | `locator.dblclick()` |
| `rightClick()` | `mcp.rightClick(strategy, value)` | `locator.click(button=RIGHT)` |

New methods (not in MCP version):

- `isVisible()` -- probes with healing, returns boolean
- `waitForVisible(timeout)` -- probes + `locator.waitFor(VISIBLE, timeout)`
- `clear()` -- `locator.clear()`
- `check()` / `uncheck()` -- for checkboxes

Exception strategy: Catch `PlaywrightException`, wrap in `IOException` -> zero signature changes in page objects and step definitions.

#### Step 8 -- Copy HealingMetrics.java as-is

No modifications needed. Has zero dependency on MCPSeleniumClient -- only depends on `LocatorRegistry.LocatorStrategy`.

---

### Phase 4: Page Objects

#### Step 9 -- Rewrite BasePage.java for PlaywrightEngine

Constructor change: `BasePage(PlaywrightEngine engine, LocatorRegistry registry, HealingMetrics metrics)`

Method migrations:

| Method | MCP version | Playwright version |
| --- | --- | --- |
| `element(name)` | `new SelfHealingElement(name, mcp, ...)` | `new SelfHealingElement(name, engine, ...)` |
| `navigateTo(url)` | `mcp.navigate(url)` | `engine.navigate(url)` |
| `navigateToHomePage()` | calls `navigateTo(BASE_URL)` | same (delegates) |
| `waitForElementVisible(name, timeout)` | retry loop calling `elem.getText()` | `element(name).waitForVisible(timeout)` |
| `assertElementVisible(name)` | `element(name).getText()` | `element(name).isVisible()` |
| `assertElementText(name, expected)` | same logic | same logic (uses `getText()`) |
| `assertElementTextContains(name, sub)` | same logic | same logic |
| `takeScreenshot(name)` | `mcp.takeScreenshot(path)` | `engine.takeScreenshot(path)` |
| `pressKey(key)` | `mcp.pressKey(key)` | `engine.pressKey(key)` |
| `sleep(ms)` | `Thread.sleep(ms)` | same |

New: `getEngine()` (replaces `getMcp()`), `getPage()` -> `engine.getPage()`

`BASE_URL` stays `"https://the-internet.herokuapp.com"`.

#### Step 10 -- Migrate LoginPage.java

Copy from source, change only:

- Constructor param: `MCPSeleniumClient mcp` -> `PlaywrightEngine engine`
- Super call: `super(engine, locatorRegistry, healingMetrics)`
- Import: remove MCP import, add PlaywrightEngine import

Everything else is **identical**: `initializeLocators()` (all 23 strategies use id/css/xpath/name -- all supported), all action methods (`enterUsername`, `clickLoginButton`, `loginWith`, etc.), all verification methods.

#### Step 11 -- Migrate CheckboxPage.java

Same pattern as LoginPage -- constructor param change only. Positional CSS selectors (`:first-of-type`, `:nth-of-type`) work in Playwright.

#### Step 12 -- Migrate AddRemoveElementsPage.java

Constructor param change plus **direct call conversion**:

| Method | MCP version | Playwright version |
| --- | --- | --- |
| `clickDeleteButton(pos)` | `mcp.clickElement("xpath", "//div[@id='elements']/button[" + pos + "]")` | `engine.getPage().locator("xpath=//div[@id='elements']/button[" + pos + "]").click()` |
| `verifyDeleteButtonCount(n)` | Try find N, then try find N+1 expecting failure | `engine.getPage().locator("//div[@id='elements']/button").count()` -- cleaner |

---

### Phase 5: Step Definitions and Runner

#### Steps 13-15 -- Migrate all 3 step definition files

Same transformation pattern for LoginSteps, CheckboxSteps, AddRemoveElementsSteps:

`@Before` hook transformation:

```text
BEFORE (MCP):                              AFTER (Playwright):
mcp = new MCPSeleniumClient();             engine = new PlaywrightEngine("chromium", false);
mcp.startMCPServer();                      engine.initialize();
mcp.startBrowser("chrome", false);         engine.startTracing("scenario-name");
registry = new LocatorRegistry();          registry = new LocatorRegistry();
registry.loadRegistry();                   registry.loadRegistry();
metrics = new HealingMetrics();            metrics = new HealingMetrics();
page = new XxxPage(mcp, registry, ...);   page = new XxxPage(engine, registry, ...);
page.initializeLocators();                 page.initializeLocators();
```

`@After` hook transformation:

```text
BEFORE (MCP):                              AFTER (Playwright):
metrics.printHealingReport();              metrics.printHealingReport();
registry.saveRegistry();                   registry.saveRegistry();
mcp.closeBrowser();                        engine.stopTracing("target/traces/xxx.zip");
mcp.stopMCPServer();                       engine.shutdown();
```

Field change in each: `MCPSeleniumClient mcp` -> `PlaywrightEngine engine`

In `LoginSteps.userNavigatesTo(url)`: change `mcp.navigate(url)` -> `engine.navigate(url)`

#### Step 16 -- Copy RunCucumberTest.java as-is

No changes -- Cucumber runner config is framework-agnostic.

---

### Phase 6: Resources (direct copies)

#### Step 17 -- Copy all 3 feature files as-is

- `login.feature` (3 scenarios)
- `checkbox.feature` (3 scenarios)
- `add_remove_elements.feature` (9 scenarios)

Zero modifications. All Gherkin step text matches the migrated step definitions.

#### Step 18 -- Copy locators.json as-is

The JSON format is identical. All strategy types (`id`, `css`, `xpath`, `name`) are supported by `PlaywrightEngine.resolveLocator()`. Success/failure counters will rebuild naturally over test runs with Playwright timing characteristics.

---

### Phase 7: Documentation

#### Step 19 -- Create CLAUDE.md for the new project

Update to reflect:

- Playwright architecture (no MCP layer, no Node.js dependency)
- New prerequisites: Java 21+, Maven 3.6+ (no Node.js needed)
- New build commands (same `mvn test` etc.)
- Updated architecture diagram
- Updated key files section

#### Step 20 -- Create README.md for the new project

Cover: architecture, prerequisites, setup, project structure, self-healing explanation, running tests, tracing/video features, cross-browser support.

#### Step 21 -- Create .gitignore

```text
target/
*.class
*.jar
.DS_Store
*.iml
.idea/
.project
.classpath
.settings/
```

---

### Phase 8: Verification

| Step | Command | Validates |
| --- | --- | --- |
| 22 | `mvn clean compile` | All code compiles with Playwright deps |
| 23 | `mvn test -Dcucumber.options="--tags @login"` | 3 login scenarios pass |
| 24 | `mvn test -Dcucumber.options="--tags @checkbox"` | 3 checkbox scenarios pass |
| 25 | `mvn test -Dcucumber.options="--tags @add_remove"` | 9 add/remove scenarios pass |
| 26 | `mvn test` | All 15 scenarios pass |
| 27 | Check `target/traces/*.zip` exist | Playwright tracing works |
| 28 | Check `locators.json` has updated stats | Self-healing persistence works |
| 29 | `mvn test -Dbrowser=firefox -Dheadless=true` | Cross-browser + headless |
| 30 | `mvn test -Dbrowser=webkit` | WebKit support (new capability) |

---

## New Project File Inventory

```text
playwright-self-healing-framework/
|-- pom.xml                                           [NEW - Playwright deps]
|-- CLAUDE.md                                         [NEW]
|-- README.md                                         [NEW]
|-- .gitignore                                        [NEW]
+-- src/
    |-- main/java/framework/
    |   |-- playwright/
    |   |   |-- PlaywrightEngine.java                 [NEW ~150 lines]
    |   |   +-- PlaywrightConfig.java                 [NEW ~30 lines]
    |   |-- locators/
    |   |   +-- LocatorRegistry.java                  [COPIED + VALID_STRATEGIES constant]
    |   |-- healing/
    |   |   |-- SelfHealingElement.java               [REWRITTEN ~310 lines - Locator-based]
    |   |   +-- HealingMetrics.java                   [COPIED AS-IS ~476 lines]
    |   +-- pages/
    |       |-- BasePage.java                         [REWRITTEN ~317 lines]
    |       |-- LoginPage.java                        [MIGRATED - constructor change]
    |       |-- CheckboxPage.java                     [MIGRATED - constructor change]
    |       +-- AddRemoveElementsPage.java            [MIGRATED - Playwright direct calls]
    +-- test/
        |-- java/
        |   |-- runners/
        |   |   +-- RunCucumberTest.java              [COPIED AS-IS]
        |   +-- stepdefinitions/
        |       |-- LoginSteps.java                   [MIGRATED - Playwright hooks]
        |       |-- CheckboxSteps.java                [MIGRATED - Playwright hooks]
        |       +-- AddRemoveElementsSteps.java       [MIGRATED - Playwright hooks]
        +-- resources/
            |-- features/
            |   |-- login.feature                     [COPIED AS-IS]
            |   |-- checkbox.feature                  [COPIED AS-IS]
            |   +-- add_remove_elements.feature       [COPIED AS-IS]
            +-- locators/
                +-- locators.json                     [COPIED AS-IS]
```

---

## Key Design Decisions

### 1. Self-Healing Probe: Locator.waitFor() as DOM probe

Playwright's auto-waiting only works within a single locator. Our healing needs to **try multiple locators** to determine which works BEFORE committing to an action. `waitFor()` is the lightest-weight probe -- it checks element existence without performing an action.

### 2. Exception wrapping: PlaywrightException to IOException

All existing page objects and step definitions declare `throws IOException`. Wrapping preserves 100% signature compatibility -- zero changes needed in method declarations.

### 3. fill() for text input (replaces sendKeys())

`fill()` clears first, then sets value. Every current usage fills empty fields or overwrites existing content. Current `clearUsername()` types empty string -- `fill("")` achieves the same.

### 4. Dynamic elements: Direct page.locator() + count()

Dynamic delete buttons don't benefit from persisted healing. `locator.count()` is cleaner than the MCP "try N+1, expect failure" pattern.

### 5. Tracing enabled by default

Playwright traces capture DOM snapshots, screenshots, network, and console at every action -- far superior to screenshot-only debugging. Disable via `-Dtracing=false`.

---

## Risks and Mitigations

| Risk | Mitigation |
| --- | --- |
| `fill()` clears field (vs `sendKeys()` appends) | No tests append text. Add `type()` for future append needs |
| Checkbox `click()` toggles vs `check()` is idempotent | Tests use toggle semantics. Add `check()`/`uncheck()` for future use |
| `textContent()` returns empty for input elements | Use `isVisible()` for visibility checks instead of `getText()` |
| Browser name "chrome" to "chromium" | `PlaywrightEngine` switch maps both names |
| locators.json success rate stats don't carry over perfectly | Stats rebuild naturally -- Playwright is faster so rates will be higher |

---

## Prerequisites for New Project

- **Java 21+**
- **Maven 3.6+**
- **No Node.js required** (Playwright bundles its own browsers)
