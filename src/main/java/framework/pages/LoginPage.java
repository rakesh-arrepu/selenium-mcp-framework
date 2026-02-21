package framework.pages;

import framework.config.ConfigurationManager;
import framework.healing.HealingMetrics;
import framework.locators.LocatorRegistry;
import framework.locators.LocatorRegistry.LocatorStrategy;
import framework.mcp.MCPSeleniumClient;

import java.io.IOException;

/**
 * LoginPage - Example page object implementation for login functionality
 *
 * This class demonstrates how to create page objects using the self-healing framework.
 * It shows best practices for:
 * - Defining element name constants
 * - Initializing multiple locator strategies
 * - Implementing page actions using element() method
 * - Creating convenience methods
 * - Verification methods
 *
 * Example Usage:
 * <pre>
 * LoginPage loginPage = new LoginPage(mcp, registry, metrics);
 * loginPage.initializeLocators();
 * loginPage.navigateToLoginPage();
 * loginPage.loginWith("user@example.com", "password123");
 * loginPage.verifyLoginSuccess();
 * </pre>
 *
 * @author Selenium-MCP Framework
 * @version 1.0.0
 */
public class LoginPage extends BasePage {

    // Element name constants
    public static final String USERNAME_INPUT = "usernameInput";
    public static final String PASSWORD_INPUT = "passwordInput";
    public static final String LOGIN_BUTTON = "loginButton";
    public static final String ERROR_MESSAGE = "errorMessage";
    public static final String SUCCESS_MESSAGE = "successMessage";

    // Configuration manager for dynamic URL
    private final ConfigurationManager config = ConfigurationManager.getInstance();

    /**
     * Creates a new LoginPage instance
     *
     * @param mcp MCP Selenium client
     * @param locatorRegistry Locator registry
     * @param healingMetrics Healing metrics tracker
     */
    public LoginPage(MCPSeleniumClient mcp, LocatorRegistry locatorRegistry, HealingMetrics healingMetrics) {
        super(mcp, locatorRegistry, healingMetrics);
    }

    /**
     * Initializes all locators for the login page
     * Each element has 4 fallback strategies for maximum resilience
     *
     * This method should be called once during setup before using the page
     */
    public void initializeLocators() {
        log("Initializing locators for LoginPage");

        // Username input field - Multiple strategies for finding it
        locatorRegistry.addElement(USERNAME_INPUT,
                new LocatorStrategy("id", "username", 1),
                new LocatorStrategy("name", "username", 2),
                new LocatorStrategy("css", "input[type='text'][name='username']", 3),
                new LocatorStrategy("xpath", "//input[@id='username' or @name='username']", 4)
        );

        // Password input field
        locatorRegistry.addElement(PASSWORD_INPUT,
                new LocatorStrategy("id", "password", 1),
                new LocatorStrategy("name", "password", 2),
                new LocatorStrategy("css", "input[type='password'][name='password']", 3),
                new LocatorStrategy("xpath", "//input[@id='password' or @name='password']", 4)
        );

        // Login button
        locatorRegistry.addElement(LOGIN_BUTTON,
                new LocatorStrategy("id", "login-btn", 1),
                new LocatorStrategy("css", "button.login-button", 2),
                new LocatorStrategy("xpath", "//button[@type='submit' and contains(text(), 'Login')]", 3),
                new LocatorStrategy("name", "login", 4)
        );

        // Error message (displayed on login failure)
        locatorRegistry.addElement(ERROR_MESSAGE,
                new LocatorStrategy("id", "error-message", 1),
                new LocatorStrategy("css", ".error-message", 2),
                new LocatorStrategy("xpath", "//div[contains(@class, 'error')]", 3),
                new LocatorStrategy("css", "[role='alert'].error", 4)
        );

        // Success message (displayed on successful login)
        locatorRegistry.addElement(SUCCESS_MESSAGE,
                new LocatorStrategy("id", "success-message", 1),
                new LocatorStrategy("css", ".success-message", 2),
                new LocatorStrategy("xpath", "//div[contains(@class, 'success')]", 3),
                new LocatorStrategy("css", "[role='alert'].success", 4)
        );

        log("✓ Locators initialized successfully");
    }

    /**
     * Navigates to the login page
     *
     * @throws IOException if navigation fails
     */
    public void navigateToLoginPage() throws IOException {
        log("Navigating to login page");
        String loginUrl = config.getBaseUrl() + "/login";
        navigateTo(loginUrl);
    }

    /**
     * Navigates to a custom login URL
     *
     * @param url Custom login page URL
     * @throws IOException if navigation fails
     */
    public void navigateToLoginPage(String url) throws IOException {
        log("Navigating to custom login page: " + url);
        navigateTo(url);
    }

    /**
     * Enters username into the username field
     *
     * @param username Username to enter
     * @throws IOException if element interaction fails
     */
    public void enterUsername(String username) throws IOException {
        log("Entering username: " + username);
        element(USERNAME_INPUT).typeText(username);
    }

    /**
     * Enters password into the password field
     *
     * @param password Password to enter
     * @throws IOException if element interaction fails
     */
    public void enterPassword(String password) throws IOException {
        log("Entering password: " + "***" + password.substring(Math.max(0, password.length() - 3)));
        element(PASSWORD_INPUT).typeText(password);
    }

    /**
     * Clicks the login button
     *
     * @throws IOException if element interaction fails
     */
    public void clickLoginButton() throws IOException {
        log("Clicking login button");
        element(LOGIN_BUTTON).click();
    }

    /**
     * Performs complete login with username and password
     * This is a convenience method that combines multiple actions
     *
     * @param username Username to login with
     * @param password Password to login with
     * @throws IOException if any element interaction fails
     */
    public void loginWith(String username, String password) throws IOException {
        log("Performing login with username: " + username);
        enterUsername(username);
        enterPassword(password);
        clickLoginButton();
        log("✓ Login attempt completed");
    }

    /**
     * Gets the error message text
     *
     * @return Error message text
     * @throws IOException if element not found
     */
    public String getErrorMessage() throws IOException {
        log("Getting error message");
        String errorText = element(ERROR_MESSAGE).getText();
        log("Error message: " + errorText);
        return errorText;
    }

    /**
     * Gets the success message text
     *
     * @return Success message text
     * @throws IOException if element not found
     */
    public String getSuccessMessage() throws IOException {
        log("Getting success message");
        String successText = element(SUCCESS_MESSAGE).getText();
        log("Success message: " + successText);
        return successText;
    }

    /**
     * Verifies that login was successful
     * Checks for presence of success message or dashboard elements
     *
     * @throws IOException if verification fails
     */
    public void verifyLoginSuccess() throws IOException {
        log("Verifying login success");
        try {
            // Wait for success indicator
            waitForElementVisible(SUCCESS_MESSAGE, 5000);
            String message = getSuccessMessage();
            log("✓ Login successful - Message: " + message);
        } catch (IOException e) {
            logError("✗ Login verification failed");
            throw new IOException("Login success verification failed", e);
        }
    }

    /**
     * Verifies that login failed with expected error message
     *
     * @param expectedError Expected error message (or substring)
     * @throws IOException if verification fails or message doesn't match
     */
    public void verifyLoginError(String expectedError) throws IOException {
        log("Verifying login error: " + expectedError);
        try {
            waitForElementVisible(ERROR_MESSAGE, 5000);
            String actualError = getErrorMessage();

            if (!actualError.contains(expectedError)) {
                logError("✗ Error message mismatch - Expected: '" + expectedError + "', Got: '" + actualError + "'");
                throw new IOException("Error message mismatch: expected '" + expectedError + "' but got '" + actualError + "'");
            }

            log("✓ Login error verified - Message: " + actualError);
        } catch (IOException e) {
            logError("✗ Login error verification failed");
            throw new IOException("Login error verification failed", e);
        }
    }

    /**
     * Verifies that the login page is loaded and ready
     * Checks for presence of username, password fields and login button
     *
     * @throws IOException if page verification fails
     */
    public void verifyPageLoaded() throws IOException {
        log("Verifying login page loaded");
        try {
            assertElementVisible(USERNAME_INPUT);
            assertElementVisible(PASSWORD_INPUT);
            assertElementVisible(LOGIN_BUTTON);
            log("✓ Login page loaded successfully");
        } catch (IOException e) {
            logError("✗ Login page verification failed");
            throw new IOException("Login page not loaded properly", e);
        }
    }

    /**
     * Clears the username field
     *
     * @throws IOException if element interaction fails
     */
    public void clearUsername() throws IOException {
        log("Clearing username field");
        // Type empty string to clear (or use keyboard shortcuts)
        element(USERNAME_INPUT).typeText("");
    }

    /**
     * Clears the password field
     *
     * @throws IOException if element interaction fails
     */
    public void clearPassword() throws IOException {
        log("Clearing password field");
        element(PASSWORD_INPUT).typeText("");
    }

    /**
     * Checks if error message is displayed
     *
     * @return true if error message is visible, false otherwise
     */
    public boolean isErrorDisplayed() {
        try {
            element(ERROR_MESSAGE).getText();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Checks if success message is displayed
     *
     * @return true if success message is visible, false otherwise
     */
    public boolean isSuccessDisplayed() {
        try {
            element(SUCCESS_MESSAGE).getText();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Takes a screenshot of the login page
     *
     * @param screenshotName Name for the screenshot
     * @throws IOException if screenshot capture fails
     */
    public void captureLoginPageScreenshot(String screenshotName) throws IOException {
        log("Capturing login page screenshot: " + screenshotName);
        takeScreenshot("login_" + screenshotName);
    }
}
