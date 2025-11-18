package framework.pages;

import framework.healing.HealingMetrics;
import framework.locators.LocatorRegistry;
import framework.locators.LocatorRegistry.LocatorStrategy;
import framework.mcp.MCPSeleniumClient;

import java.io.IOException;

/**
 * PasswordResetPage - Page object for password reset functionality
 *
 * This class provides methods to interact with the password reset page.
 * It demonstrates password reset flow with email verification.
 *
 * Key Features:
 * - Email input and validation
 * - Reset request submission
 * - Success/error message handling
 * - Email verification support
 *
 * Example Usage:
 * <pre>
 * PasswordResetPage resetPage = new PasswordResetPage(mcp, registry, metrics);
 * resetPage.initializeLocators();
 * resetPage.requestPasswordReset("user@example.com");
 * resetPage.verifyResetEmailSent();
 * </pre>
 *
 * @author Selenium-MCP Framework
 * @version 1.0.0
 */
public class PasswordResetPage extends BasePage {

    // Element name constants
    public static final String EMAIL_INPUT = "resetEmailInput";
    public static final String RESET_BUTTON = "resetButton";
    public static final String SUCCESS_MESSAGE = "resetSuccessMessage";
    public static final String ERROR_MESSAGE = "resetErrorMessage";
    public static final String EMAIL_ERROR = "resetEmailError";
    public static final String BACK_TO_LOGIN = "backToLoginLink";
    public static final String VERIFICATION_CODE_INPUT = "verificationCodeInput";
    public static final String NEW_PASSWORD_INPUT = "newPasswordInput";
    public static final String CONFIRM_NEW_PASSWORD = "confirmNewPasswordInput";
    public static final String SUBMIT_NEW_PASSWORD = "submitNewPasswordButton";

    /**
     * Creates a new PasswordResetPage instance
     *
     * @param mcp MCP Selenium client
     * @param locatorRegistry Locator registry
     * @param healingMetrics Healing metrics tracker
     */
    public PasswordResetPage(MCPSeleniumClient mcp, LocatorRegistry locatorRegistry, HealingMetrics healingMetrics) {
        super(mcp, locatorRegistry, healingMetrics);
    }

    /**
     * Initializes all locators for the password reset page
     */
    public void initializeLocators() {
        log("Initializing locators for PasswordResetPage");

        // Email input
        locatorRegistry.addElement(EMAIL_INPUT,
                new LocatorStrategy("id", "email", 1),
                new LocatorStrategy("name", "email", 2),
                new LocatorStrategy("css", "input[type='email']", 3),
                new LocatorStrategy("xpath", "//input[@type='email' or @name='email']", 4)
        );

        // Reset button
        locatorRegistry.addElement(RESET_BUTTON,
                new LocatorStrategy("id", "reset-btn", 1),
                new LocatorStrategy("css", "button[type='submit']", 2),
                new LocatorStrategy("xpath", "//button[contains(text(), 'Reset') or @type='submit']", 3),
                new LocatorStrategy("name", "reset", 4)
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

        // Back to login link
        locatorRegistry.addElement(BACK_TO_LOGIN,
                new LocatorStrategy("id", "back-to-login", 1),
                new LocatorStrategy("linkText", "Back to Login", 2),
                new LocatorStrategy("css", "a[href*='login']", 3),
                new LocatorStrategy("xpath", "//a[contains(text(), 'Back') or contains(text(), 'Login')]", 4)
        );

        // Verification code input
        locatorRegistry.addElement(VERIFICATION_CODE_INPUT,
                new LocatorStrategy("id", "verification-code", 1),
                new LocatorStrategy("name", "verificationCode", 2),
                new LocatorStrategy("css", "input[name='verificationCode']", 3),
                new LocatorStrategy("xpath", "//input[@id='verification-code' or @name='verificationCode']", 4)
        );

        // New password input
        locatorRegistry.addElement(NEW_PASSWORD_INPUT,
                new LocatorStrategy("id", "new-password", 1),
                new LocatorStrategy("name", "newPassword", 2),
                new LocatorStrategy("css", "input[type='password'][name='newPassword']", 3),
                new LocatorStrategy("xpath", "//input[@type='password' and @name='newPassword']", 4)
        );

        // Confirm new password
        locatorRegistry.addElement(CONFIRM_NEW_PASSWORD,
                new LocatorStrategy("id", "confirm-new-password", 1),
                new LocatorStrategy("name", "confirmNewPassword", 2),
                new LocatorStrategy("css", "input[type='password'][name='confirmNewPassword']", 3),
                new LocatorStrategy("xpath", "//input[@type='password' and @name='confirmNewPassword']", 4)
        );

        // Submit new password button
        locatorRegistry.addElement(SUBMIT_NEW_PASSWORD,
                new LocatorStrategy("id", "submit-new-password", 1),
                new LocatorStrategy("css", "button[type='submit']", 2),
                new LocatorStrategy("xpath", "//button[contains(text(), 'Submit') or @type='submit']", 3),
                new LocatorStrategy("name", "submit", 4)
        );

        log("✓ Locators initialized successfully");
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
     * Clicks the reset password button
     *
     * @throws IOException if element interaction fails
     */
    public void clickResetButton() throws IOException {
        log("Clicking reset button");
        element(RESET_BUTTON).click();
    }

    /**
     * Requests password reset for given email
     *
     * @param email Email address
     * @throws IOException if request fails
     */
    public void requestPasswordReset(String email) throws IOException {
        log("Requesting password reset for: " + email);
        enterEmail(email);
        clickResetButton();
        log("✓ Password reset requested");
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
     * Verifies that reset email was sent successfully
     *
     * @throws IOException if verification fails
     */
    public void verifyResetEmailSent() throws IOException {
        log("Verifying reset email sent");
        try {
            waitForElementVisible(SUCCESS_MESSAGE, 5000);
            String message = getSuccessMessage();
            log("✓ Reset email sent - Message: " + message);
        } catch (IOException e) {
            logError("✗ Reset email verification failed");
            throw new IOException("Reset email verification failed", e);
        }
    }

    /**
     * Verifies password reset error
     *
     * @param expectedError Expected error message
     * @throws IOException if verification fails
     */
    public void verifyResetError(String expectedError) throws IOException {
        log("Verifying reset error: " + expectedError);
        try {
            waitForElementVisible(ERROR_MESSAGE, 5000);
            String actualError = getErrorMessage();

            if (!actualError.contains(expectedError)) {
                logError("✗ Error message mismatch - Expected: '" + expectedError + "', Got: '" + actualError + "'");
                throw new IOException("Error message mismatch");
            }

            log("✓ Reset error verified");
        } catch (IOException e) {
            logError("✗ Reset error verification failed");
            throw new IOException("Reset error verification failed", e);
        }
    }

    /**
     * Clicks back to login link
     *
     * @throws IOException if element interaction fails
     */
    public void clickBackToLogin() throws IOException {
        log("Clicking back to login link");
        element(BACK_TO_LOGIN).click();
    }

    /**
     * Enters verification code
     *
     * @param code Verification code
     * @throws IOException if element interaction fails
     */
    public void enterVerificationCode(String code) throws IOException {
        log("Entering verification code");
        element(VERIFICATION_CODE_INPUT).typeText(code);
    }

    /**
     * Enters new password
     *
     * @param password New password
     * @throws IOException if element interaction fails
     */
    public void enterNewPassword(String password) throws IOException {
        log("Entering new password: ***");
        element(NEW_PASSWORD_INPUT).typeText(password);
    }

    /**
     * Enters confirm new password
     *
     * @param password Confirm password
     * @throws IOException if element interaction fails
     */
    public void enterConfirmNewPassword(String password) throws IOException {
        log("Entering confirm new password: ***");
        element(CONFIRM_NEW_PASSWORD).typeText(password);
    }

    /**
     * Clicks submit new password button
     *
     * @throws IOException if element interaction fails
     */
    public void clickSubmitNewPassword() throws IOException {
        log("Clicking submit new password button");
        element(SUBMIT_NEW_PASSWORD).click();
    }

    /**
     * Completes password reset with verification code
     *
     * @param verificationCode Verification code from email
     * @param newPassword New password
     * @throws IOException if password reset fails
     */
    public void completePasswordReset(String verificationCode, String newPassword) throws IOException {
        log("Completing password reset");
        enterVerificationCode(verificationCode);
        enterNewPassword(newPassword);
        enterConfirmNewPassword(newPassword);
        clickSubmitNewPassword();
        log("✓ Password reset completed");
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
     * Verifies email validation error is displayed
     *
     * @throws IOException if verification fails
     */
    public void verifyEmailValidationError() throws IOException {
        log("Verifying email validation error");
        waitForElementVisible(EMAIL_ERROR, 3000);
        String error = getEmailError();
        log("✓ Email validation error: " + error);
    }
}
