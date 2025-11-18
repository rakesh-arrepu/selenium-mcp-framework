package framework.pages;

import framework.healing.HealingMetrics;
import framework.locators.LocatorRegistry;
import framework.locators.LocatorRegistry.LocatorStrategy;
import framework.mcp.MCPSeleniumClient;

import java.io.IOException;

/**
 * RegistrationPage - Page object for user registration functionality
 *
 * This class provides methods to interact with the registration page and validate form inputs.
 * It demonstrates comprehensive form handling with self-healing element interactions.
 *
 * Key Features:
 * - Form field validation
 * - Password strength validation
 * - Email format validation
 * - Terms & conditions handling
 * - Registration error handling
 * - Success confirmation verification
 *
 * Example Usage:
 * <pre>
 * RegistrationPage regPage = new RegistrationPage(mcp, registry, metrics);
 * regPage.initializeLocators();
 * regPage.registerNewUser("John", "Doe", "john@example.com", "SecurePass123");
 * regPage.verifyRegistrationSuccess();
 * </pre>
 *
 * @author Selenium-MCP Framework
 * @version 1.0.0
 */
public class RegistrationPage extends BasePage {

    // Element name constants
    public static final String FIRST_NAME_INPUT = "firstNameInput";
    public static final String LAST_NAME_INPUT = "lastNameInput";
    public static final String EMAIL_INPUT = "emailInput";
    public static final String PASSWORD_INPUT = "passwordInput";
    public static final String CONFIRM_PASSWORD_INPUT = "confirmPasswordInput";
    public static final String TERMS_CHECKBOX = "termsCheckbox";
    public static final String REGISTER_BUTTON = "registerButton";
    public static final String SUCCESS_MESSAGE = "registrationSuccessMessage";
    public static final String ERROR_MESSAGE = "registrationErrorMessage";
    public static final String EMAIL_ERROR = "emailError";
    public static final String PASSWORD_ERROR = "passwordError";
    public static final String TERMS_ERROR = "termsError";

    /**
     * Creates a new RegistrationPage instance
     *
     * @param mcp MCP Selenium client
     * @param locatorRegistry Locator registry
     * @param healingMetrics Healing metrics tracker
     */
    public RegistrationPage(MCPSeleniumClient mcp, LocatorRegistry locatorRegistry, HealingMetrics healingMetrics) {
        super(mcp, locatorRegistry, healingMetrics);
    }

    /**
     * Initializes all locators for the registration page
     * Each element has multiple fallback strategies for resilience
     */
    public void initializeLocators() {
        log("Initializing locators for RegistrationPage");

        // First name input
        locatorRegistry.addElement(FIRST_NAME_INPUT,
                new LocatorStrategy("id", "firstName", 1),
                new LocatorStrategy("name", "firstName", 2),
                new LocatorStrategy("css", "input[placeholder*='First']", 3),
                new LocatorStrategy("xpath", "//input[@id='firstName' or @name='firstName']", 4)
        );

        // Last name input
        locatorRegistry.addElement(LAST_NAME_INPUT,
                new LocatorStrategy("id", "lastName", 1),
                new LocatorStrategy("name", "lastName", 2),
                new LocatorStrategy("css", "input[placeholder*='Last']", 3),
                new LocatorStrategy("xpath", "//input[@id='lastName' or @name='lastName']", 4)
        );

        // Email input
        locatorRegistry.addElement(EMAIL_INPUT,
                new LocatorStrategy("id", "email", 1),
                new LocatorStrategy("name", "email", 2),
                new LocatorStrategy("css", "input[type='email']", 3),
                new LocatorStrategy("xpath", "//input[@id='email' or @type='email']", 4)
        );

        // Password input
        locatorRegistry.addElement(PASSWORD_INPUT,
                new LocatorStrategy("id", "password", 1),
                new LocatorStrategy("name", "password", 2),
                new LocatorStrategy("css", "input[type='password'][name='password']", 3),
                new LocatorStrategy("xpath", "//input[@id='password' and @type='password']", 4)
        );

        // Confirm password input
        locatorRegistry.addElement(CONFIRM_PASSWORD_INPUT,
                new LocatorStrategy("id", "confirmPassword", 1),
                new LocatorStrategy("name", "confirmPassword", 2),
                new LocatorStrategy("css", "input[type='password'][name='confirmPassword']", 3),
                new LocatorStrategy("xpath", "//input[@id='confirmPassword' and @type='password']", 4)
        );

        // Terms & conditions checkbox
        locatorRegistry.addElement(TERMS_CHECKBOX,
                new LocatorStrategy("id", "terms", 1),
                new LocatorStrategy("name", "terms", 2),
                new LocatorStrategy("css", "input[type='checkbox']#terms", 3),
                new LocatorStrategy("xpath", "//input[@type='checkbox' and @id='terms']", 4)
        );

        // Register button
        locatorRegistry.addElement(REGISTER_BUTTON,
                new LocatorStrategy("id", "register-btn", 1),
                new LocatorStrategy("css", "button[type='submit']", 2),
                new LocatorStrategy("xpath", "//button[contains(text(), 'Register') or @type='submit']", 3),
                new LocatorStrategy("name", "register", 4)
        );

        // Success message
        locatorRegistry.addElement(SUCCESS_MESSAGE,
                new LocatorStrategy("id", "success-message", 1),
                new LocatorStrategy("css", ".success-message", 2),
                new LocatorStrategy("xpath", "//div[contains(@class, 'success')]", 3),
                new LocatorStrategy("css", "[role='alert'].success", 4)
        );

        // Error message
        locatorRegistry.addElement(ERROR_MESSAGE,
                new LocatorStrategy("id", "error-message", 1),
                new LocatorStrategy("css", ".error-message", 2),
                new LocatorStrategy("xpath", "//div[contains(@class, 'error')]", 3),
                new LocatorStrategy("css", "[role='alert'].error", 4)
        );

        // Email validation error
        locatorRegistry.addElement(EMAIL_ERROR,
                new LocatorStrategy("id", "email-error", 1),
                new LocatorStrategy("css", ".email-error", 2),
                new LocatorStrategy("xpath", "//span[contains(@class, 'email-error')]", 3),
                new LocatorStrategy("css", "[data-field='email'].error", 4)
        );

        // Password validation error
        locatorRegistry.addElement(PASSWORD_ERROR,
                new LocatorStrategy("id", "password-error", 1),
                new LocatorStrategy("css", ".password-error", 2),
                new LocatorStrategy("xpath", "//span[contains(@class, 'password-error')]", 3),
                new LocatorStrategy("css", "[data-field='password'].error", 4)
        );

        // Terms validation error
        locatorRegistry.addElement(TERMS_ERROR,
                new LocatorStrategy("id", "terms-error", 1),
                new LocatorStrategy("css", ".terms-error", 2),
                new LocatorStrategy("xpath", "//span[contains(@class, 'terms-error')]", 3),
                new LocatorStrategy("css", "[data-field='terms'].error", 4)
        );

        log("✓ Locators initialized successfully");
    }

    /**
     * Enters first name
     *
     * @param firstName First name to enter
     * @throws IOException if element interaction fails
     */
    public void enterFirstName(String firstName) throws IOException {
        log("Entering first name: " + firstName);
        element(FIRST_NAME_INPUT).typeText(firstName);
    }

    /**
     * Enters last name
     *
     * @param lastName Last name to enter
     * @throws IOException if element interaction fails
     */
    public void enterLastName(String lastName) throws IOException {
        log("Entering last name: " + lastName);
        element(LAST_NAME_INPUT).typeText(lastName);
    }

    /**
     * Enters email address
     *
     * @param email Email to enter
     * @throws IOException if element interaction fails
     */
    public void enterEmail(String email) throws IOException {
        log("Entering email: " + email);
        element(EMAIL_INPUT).typeText(email);
    }

    /**
     * Enters password
     *
     * @param password Password to enter
     * @throws IOException if element interaction fails
     */
    public void enterPassword(String password) throws IOException {
        log("Entering password: ***");
        element(PASSWORD_INPUT).typeText(password);
    }

    /**
     * Enters confirm password
     *
     * @param confirmPassword Confirm password to enter
     * @throws IOException if element interaction fails
     */
    public void enterConfirmPassword(String confirmPassword) throws IOException {
        log("Entering confirm password: ***");
        element(CONFIRM_PASSWORD_INPUT).typeText(confirmPassword);
    }

    /**
     * Accepts terms and conditions
     *
     * @throws IOException if element interaction fails
     */
    public void acceptTerms() throws IOException {
        log("Accepting terms and conditions");
        element(TERMS_CHECKBOX).click();
    }

    /**
     * Clicks the register button
     *
     * @throws IOException if element interaction fails
     */
    public void clickRegisterButton() throws IOException {
        log("Clicking register button");
        element(REGISTER_BUTTON).click();
    }

    /**
     * Performs complete user registration
     *
     * @param firstName First name
     * @param lastName Last name
     * @param email Email address
     * @param password Password
     * @throws IOException if registration fails
     */
    public void registerNewUser(String firstName, String lastName, String email, String password) throws IOException {
        log("Registering new user: " + email);

        enterFirstName(firstName);
        enterLastName(lastName);
        enterEmail(email);
        enterPassword(password);
        enterConfirmPassword(password);
        acceptTerms();
        clickRegisterButton();

        log("✓ Registration form submitted");
    }

    /**
     * Performs registration with all fields explicitly
     *
     * @param firstName First name
     * @param lastName Last name
     * @param email Email address
     * @param password Password
     * @param confirmPassword Confirm password
     * @param acceptTerms Whether to accept terms
     * @throws IOException if registration fails
     */
    public void registerUser(String firstName, String lastName, String email,
                            String password, String confirmPassword, boolean acceptTerms) throws IOException {
        log("Registering user with custom settings");

        enterFirstName(firstName);
        enterLastName(lastName);
        enterEmail(email);
        enterPassword(password);
        enterConfirmPassword(confirmPassword);

        if (acceptTerms) {
            acceptTerms();
        }

        clickRegisterButton();
        log("✓ Registration form submitted");
    }

    /**
     * Gets the success message text
     *
     * @return Success message text
     * @throws IOException if element not found
     */
    public String getSuccessMessage() throws IOException {
        log("Getting success message");
        String message = element(SUCCESS_MESSAGE).getText();
        log("Success message: " + message);
        return message;
    }

    /**
     * Gets the error message text
     *
     * @return Error message text
     * @throws IOException if element not found
     */
    public String getErrorMessage() throws IOException {
        log("Getting error message");
        String message = element(ERROR_MESSAGE).getText();
        log("Error message: " + message);
        return message;
    }

    /**
     * Gets email validation error
     *
     * @return Email error message
     * @throws IOException if element not found
     */
    public String getEmailError() throws IOException {
        log("Getting email validation error");
        return element(EMAIL_ERROR).getText();
    }

    /**
     * Gets password validation error
     *
     * @return Password error message
     * @throws IOException if element not found
     */
    public String getPasswordError() throws IOException {
        log("Getting password validation error");
        return element(PASSWORD_ERROR).getText();
    }

    /**
     * Gets terms validation error
     *
     * @return Terms error message
     * @throws IOException if element not found
     */
    public String getTermsError() throws IOException {
        log("Getting terms validation error");
        return element(TERMS_ERROR).getText();
    }

    /**
     * Verifies registration was successful
     *
     * @throws IOException if verification fails
     */
    public void verifyRegistrationSuccess() throws IOException {
        log("Verifying registration success");
        try {
            waitForElementVisible(SUCCESS_MESSAGE, 5000);
            String message = getSuccessMessage();
            log("✓ Registration successful - Message: " + message);
        } catch (IOException e) {
            logError("✗ Registration verification failed");
            throw new IOException("Registration success verification failed", e);
        }
    }

    /**
     * Verifies registration failed with error
     *
     * @param expectedError Expected error message
     * @throws IOException if verification fails
     */
    public void verifyRegistrationError(String expectedError) throws IOException {
        log("Verifying registration error: " + expectedError);
        try {
            waitForElementVisible(ERROR_MESSAGE, 5000);
            String actualError = getErrorMessage();

            if (!actualError.contains(expectedError)) {
                logError("✗ Error message mismatch - Expected: '" + expectedError + "', Got: '" + actualError + "'");
                throw new IOException("Error message mismatch");
            }

            log("✓ Registration error verified");
        } catch (IOException e) {
            logError("✗ Registration error verification failed");
            throw new IOException("Registration error verification failed", e);
        }
    }

    /**
     * Verifies email validation error
     *
     * @throws IOException if verification fails
     */
    public void verifyEmailValidationError() throws IOException {
        log("Verifying email validation error");
        waitForElementVisible(EMAIL_ERROR, 3000);
        String error = getEmailError();
        log("✓ Email validation error: " + error);
    }

    /**
     * Verifies password validation error
     *
     * @throws IOException if verification fails
     */
    public void verifyPasswordValidationError() throws IOException {
        log("Verifying password validation error");
        waitForElementVisible(PASSWORD_ERROR, 3000);
        String error = getPasswordError();
        log("✓ Password validation error: " + error);
    }

    /**
     * Verifies terms validation error
     *
     * @throws IOException if verification fails
     */
    public void verifyTermsValidationError() throws IOException {
        log("Verifying terms validation error");
        waitForElementVisible(TERMS_ERROR, 3000);
        String error = getTermsError();
        log("✓ Terms validation error: " + error);
    }

    /**
     * Checks if success message is displayed
     *
     * @return true if success message is visible
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
     * Checks if error message is displayed
     *
     * @return true if error message is visible
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
     * Clears the registration form
     *
     * @throws IOException if clearing fails
     */
    public void clearForm() throws IOException {
        log("Clearing registration form");
        enterFirstName("");
        enterLastName("");
        enterEmail("");
        enterPassword("");
        enterConfirmPassword("");
        log("✓ Form cleared");
    }
}
