package stepdefinitions;

import framework.healing.HealingMetrics;
import framework.locators.LocatorRegistry;
import framework.mcp.MCPSeleniumClient;
import framework.pages.LoginPage;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.io.IOException;

/**
 * LoginSteps - Cucumber step definitions for login feature
 *
 * This class demonstrates how to write Cucumber step definitions using the framework.
 * It shows:
 * - @Before hook for test setup (browser, registry, page objects)
 * - @After hook for cleanup (metrics, screenshots, browser close)
 * - Step definitions that delegate to page object methods
 * - Proper error handling and logging
 *
 * The steps are designed to be reusable and map directly to Gherkin scenarios.
 *
 * @author Selenium-MCP Framework
 * @version 1.0.0
 */
public class LoginSteps {

    private MCPSeleniumClient mcp;
    private LocatorRegistry locatorRegistry;
    private HealingMetrics healingMetrics;
    private LoginPage loginPage;

    /**
     * Setup method - runs before each scenario
     * Initializes all framework components and starts the browser
     *
     * @throws IOException if setup fails
     */
    @Before
    public void setUp() throws IOException {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║              STARTING TEST SCENARIO                           ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");

        System.out.println("→ Initializing MCP Selenium Client...");
        mcp = new MCPSeleniumClient();

        System.out.println("→ Starting MCP server...");
        mcp.startMCPServer();

        System.out.println("→ Starting browser (Chrome, non-headless)...");
        mcp.startBrowser("chrome", false);

        System.out.println("→ Initializing Locator Registry...");
        locatorRegistry = new LocatorRegistry();

        System.out.println("→ Loading locator registry from JSON...");
        try {
            locatorRegistry.loadRegistry();
        } catch (IOException e) {
            System.out.println("⚠ Could not load existing registry, will create new one");
        }

        System.out.println("→ Initializing Healing Metrics...");
        healingMetrics = new HealingMetrics();

        System.out.println("→ Initializing Login Page...");
        loginPage = new LoginPage(mcp, locatorRegistry, healingMetrics);

        System.out.println("→ Initializing page locators...");
        loginPage.initializeLocators();

        System.out.println("\n✓ Setup complete - Test ready to run\n");
    }

    /**
     * Cleanup method - runs after each scenario
     * Prints metrics, saves registry, and closes browser
     *
     * @throws IOException if cleanup fails
     */
    @After
    public void tearDown() throws IOException {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║              CLEANING UP TEST SCENARIO                        ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");

        System.out.println("→ Printing healing metrics report...");
        healingMetrics.printHealingReport();

        System.out.println("→ Saving locator registry...");
        try {
            locatorRegistry.saveRegistry();
        } catch (IOException e) {
            System.err.println("⚠ Could not save registry: " + e.getMessage());
        }

        System.out.println("→ Closing browser...");
        try {
            mcp.closeBrowser();
        } catch (IOException e) {
            System.err.println("⚠ Could not close browser: " + e.getMessage());
        }

        System.out.println("→ Stopping MCP server...");
        try {
            mcp.stopMCPServer();
        } catch (IOException e) {
            System.err.println("⚠ Could not stop MCP server: " + e.getMessage());
        }

        System.out.println("\n✓ Cleanup complete\n");
    }

    /**
     * Step: User navigates to login page
     *
     * @throws IOException if navigation fails
     */
    @Given("user navigates to login page")
    public void userNavigatesToLoginPage() throws IOException {
        System.out.println("\n→ STEP: User navigates to login page");
        loginPage.navigateToLoginPage();
        System.out.println("✓ Navigation complete\n");
    }

    /**
     * Step: User should see login page
     *
     * @throws IOException if verification fails
     */
    @Then("user should see login page")
    public void userShouldSeeLoginPage() throws IOException {
        System.out.println("\n→ STEP: Verify login page is displayed");
        loginPage.verifyPageLoaded();
        System.out.println("✓ Login page verified\n");
    }

    /**
     * Step: User enters username and password
     *
     * @param username Username to enter
     * @param password Password to enter
     * @throws IOException if element interaction fails
     */
    @When("user enters {string} and password {string}")
    public void userEntersUsernameAndPassword(String username, String password) throws IOException {
        System.out.println("\n→ STEP: User enters username and password");
        System.out.println("   Username: " + username);
        System.out.println("   Password: " + "***" + password.substring(Math.max(0, password.length() - 3)));

        loginPage.enterUsername(username);
        loginPage.enterPassword(password);

        System.out.println("✓ Credentials entered\n");
    }

    /**
     * Step: User clicks login button
     *
     * @throws IOException if click fails
     */
    @When("user clicks login button")
    public void userClicksLoginButton() throws IOException {
        System.out.println("\n→ STEP: User clicks login button");
        loginPage.clickLoginButton();
        System.out.println("✓ Login button clicked\n");

        // Brief wait for page transition
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Step: Dashboard should be displayed (successful login)
     *
     * @throws IOException if verification fails
     */
    @Then("dashboard should be displayed")
    public void dashboardShouldBeDisplayed() throws IOException {
        System.out.println("\n→ STEP: Verify dashboard is displayed");
        loginPage.verifyLoginSuccess();
        System.out.println("✓ Dashboard verified - Login successful\n");
    }

    /**
     * Step: Error message should appear with specific text
     *
     * @param expectedError Expected error message
     * @throws IOException if verification fails
     */
    @Then("error message should appear {string}")
    public void errorMessageShouldAppear(String expectedError) throws IOException {
        System.out.println("\n→ STEP: Verify error message appears");
        System.out.println("   Expected: " + expectedError);

        loginPage.verifyLoginError(expectedError);

        System.out.println("✓ Error message verified\n");
    }

    /**
     * Step: User takes screenshot with given name
     *
     * @param screenshotName Name for the screenshot
     * @throws IOException if screenshot fails
     */
    @When("user takes screenshot {string}")
    public void userTakesScreenshot(String screenshotName) throws IOException {
        System.out.println("\n→ STEP: Taking screenshot: " + screenshotName);
        loginPage.captureLoginPageScreenshot(screenshotName);
        System.out.println("✓ Screenshot captured\n");
    }

    /**
     * Step: User waits for specified seconds
     *
     * @param seconds Number of seconds to wait
     */
    @When("user waits for {int} seconds")
    public void userWaitsForSeconds(int seconds) {
        System.out.println("\n→ STEP: Waiting for " + seconds + " seconds");
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("✓ Wait complete\n");
    }

    /**
     * Step: User navigates to custom URL
     *
     * @param url URL to navigate to
     * @throws IOException if navigation fails
     */
    @Given("user navigates to {string}")
    public void userNavigatesTo(String url) throws IOException {
        System.out.println("\n→ STEP: User navigates to: " + url);
        loginPage.navigateToLoginPage(url);
        System.out.println("✓ Navigation complete\n");
    }

    /**
     * Step: User clears username field
     *
     * @throws IOException if element interaction fails
     */
    @When("user clears username field")
    public void userClearsUsernameField() throws IOException {
        System.out.println("\n→ STEP: Clearing username field");
        loginPage.clearUsername();
        System.out.println("✓ Username field cleared\n");
    }

    /**
     * Step: User clears password field
     *
     * @throws IOException if element interaction fails
     */
    @When("user clears password field")
    public void userClearsPasswordField() throws IOException {
        System.out.println("\n→ STEP: Clearing password field");
        loginPage.clearPassword();
        System.out.println("✓ Password field cleared\n");
    }

    /**
     * Step: Error message is displayed
     *
     * @throws IOException if verification fails
     */
    @Then("error message is displayed")
    public void errorMessageIsDisplayed() throws IOException {
        System.out.println("\n→ STEP: Verify error message is displayed");
        if (!loginPage.isErrorDisplayed()) {
            throw new IOException("Error message is not displayed");
        }
        System.out.println("✓ Error message is displayed\n");
    }

    /**
     * Step: Success message is displayed
     *
     * @throws IOException if verification fails
     */
    @Then("success message is displayed")
    public void successMessageIsDisplayed() throws IOException {
        System.out.println("\n→ STEP: Verify success message is displayed");
        if (!loginPage.isSuccessDisplayed()) {
            throw new IOException("Success message is not displayed");
        }
        System.out.println("✓ Success message is displayed\n");
    }

    /**
     * Step: User prints healing report
     */
    @When("user prints healing report")
    public void userPrintsHealingReport() {
        System.out.println("\n→ STEP: Printing healing report");
        healingMetrics.printHealingReport();
        System.out.println("✓ Report printed\n");
    }
}
