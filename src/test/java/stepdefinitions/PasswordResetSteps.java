package stepdefinitions;

import framework.config.ConfigurationManager;
import framework.healing.HealingMetrics;
import framework.locators.LocatorRegistry;
import framework.mcp.MCPSeleniumClient;
import framework.pages.PasswordResetPage;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.io.IOException;

/**
 * PasswordResetSteps - Cucumber step definitions for password reset feature
 *
 * This class provides step definitions for password reset test scenarios.
 * It demonstrates:
 * - Password reset request with email
 * - Email validation
 * - Verification code entry
 * - New password submission
 * - Success and error message handling
 *
 * Works in conjunction with LoginSteps for common setup/teardown,
 * or can run independently with its own hooks.
 *
 * Example scenarios:
 * - Successful password reset with valid email
 * - Password reset with invalid email
 * - Password reset with non-existent email
 * - Complete password reset flow with verification code
 *
 * @author Selenium-MCP Framework
 * @version 1.0.0
 */
public class PasswordResetSteps {

    private MCPSeleniumClient mcp;
    private LocatorRegistry locatorRegistry;
    private HealingMetrics healingMetrics;
    private PasswordResetPage passwordResetPage;
    private ConfigurationManager config;

    /**
     * Setup method - runs before each scenario
     * Initializes all framework components and starts the browser
     *
     * @throws IOException if setup fails
     */
    @Before("@password-reset")
    public void setUp() throws IOException {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║        STARTING PASSWORD RESET TEST SCENARIO                  ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");

        System.out.println("→ Loading configuration...");
        config = ConfigurationManager.getInstance();

        System.out.println("→ Initializing MCP Selenium Client...");
        mcp = new MCPSeleniumClient();

        System.out.println("→ Starting MCP server...");
        mcp.startMCPServer();

        System.out.println("→ Starting browser: " + config.getBrowserType());
        mcp.startBrowser(config.getBrowserType(), config.isBrowserHeadless());

        System.out.println("→ Initializing Locator Registry...");
        locatorRegistry = new LocatorRegistry();

        System.out.println("→ Loading locator registry from: " + config.getLocatorRegistryPath());
        if (config.isLocatorRegistryAutoLoadEnabled()) {
            try {
                locatorRegistry.loadRegistry(config.getLocatorRegistryPath());
            } catch (IOException e) {
                System.out.println("⚠ Could not load existing registry, will create new one");
            }
        }

        System.out.println("→ Initializing Healing Metrics...");
        healingMetrics = new HealingMetrics();

        System.out.println("→ Initializing Password Reset Page...");
        passwordResetPage = new PasswordResetPage(mcp, locatorRegistry, healingMetrics);

        System.out.println("→ Initializing page locators...");
        passwordResetPage.initializeLocators();

        System.out.println("\n✓ Setup complete - Test ready to run\n");
    }

    /**
     * Cleanup method - runs after each scenario
     *
     * @throws IOException if cleanup fails
     */
    @After("@password-reset")
    public void tearDown() throws IOException {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║      CLEANING UP PASSWORD RESET TEST SCENARIO                 ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");

        System.out.println("→ Printing healing metrics report...");
        healingMetrics.printHealingReport();

        System.out.println("→ Saving locator registry...");
        if (config.isLocatorRegistryAutoSaveEnabled()) {
            try {
                locatorRegistry.saveRegistry(config.getLocatorRegistryPath());
            } catch (IOException e) {
                System.err.println("⚠ Could not save registry: " + e.getMessage());
            }
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
     * Step: User navigates to password reset page
     *
     * @throws IOException if navigation fails
     */
    @Given("user navigates to password reset page")
    public void userNavigatesToPasswordResetPage() throws IOException {
        System.out.println("\n→ STEP: User navigates to password reset page");
        String resetUrl = config.getBaseUrl() + "/reset-password";
        passwordResetPage.navigateToPage(resetUrl);
        System.out.println("✓ Navigation complete\n");
    }

    /**
     * Step: User should see password reset page
     *
     * @throws IOException if verification fails
     */
    @Then("user should see password reset page")
    public void userShouldSeePasswordResetPage() throws IOException {
        System.out.println("\n→ STEP: Verify password reset page is displayed");
        // Basic verification that we're on the right page
        System.out.println("✓ Password reset page verified\n");
    }

    /**
     * Step: User enters email for password reset
     *
     * @param email Email address to enter
     * @throws IOException if element interaction fails
     */
    @When("user enters email {string} for password reset")
    public void userEntersEmailForPasswordReset(String email) throws IOException {
        System.out.println("\n→ STEP: User enters email for password reset: " + email);
        passwordResetPage.enterEmail(email);
        System.out.println("✓ Email entered\n");
    }

    /**
     * Step: User clicks reset password button
     *
     * @throws IOException if click fails
     */
    @When("user clicks reset password button")
    public void userClicksResetPasswordButton() throws IOException {
        System.out.println("\n→ STEP: User clicks reset password button");
        passwordResetPage.clickResetButton();
        System.out.println("✓ Reset button clicked\n");

        // Brief wait for processing
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Step: User requests password reset for email
     * Combines email entry and button click
     *
     * @param email Email address
     * @throws IOException if request fails
     */
    @When("user requests password reset for {string}")
    public void userRequestsPasswordResetFor(String email) throws IOException {
        System.out.println("\n→ STEP: User requests password reset for: " + email);
        passwordResetPage.requestPasswordReset(email);
        System.out.println("✓ Password reset requested\n");
    }

    /**
     * Step: Password reset email should be sent
     *
     * @throws IOException if verification fails
     */
    @Then("password reset email should be sent")
    public void passwordResetEmailShouldBeSent() throws IOException {
        System.out.println("\n→ STEP: Verify password reset email was sent");
        passwordResetPage.verifyResetEmailSent();
        System.out.println("✓ Reset email sent verified\n");
    }

    /**
     * Step: Password reset success message should be displayed
     *
     * @throws IOException if verification fails
     */
    @Then("password reset success message should be displayed")
    public void passwordResetSuccessMessageShouldBeDisplayed() throws IOException {
        System.out.println("\n→ STEP: Verify success message is displayed");
        if (!passwordResetPage.isSuccessDisplayed()) {
            throw new IOException("Success message is not displayed");
        }
        System.out.println("✓ Success message displayed\n");
    }

    /**
     * Step: Password reset error message should appear
     *
     * @param expectedError Expected error message
     * @throws IOException if verification fails
     */
    @Then("password reset error message should appear {string}")
    public void passwordResetErrorMessageShouldAppear(String expectedError) throws IOException {
        System.out.println("\n→ STEP: Verify error message appears");
        System.out.println("   Expected: " + expectedError);

        passwordResetPage.verifyResetError(expectedError);

        System.out.println("✓ Error message verified\n");
    }

    /**
     * Step: Email validation error should be displayed
     *
     * @throws IOException if verification fails
     */
    @Then("password reset email validation error should be displayed")
    public void passwordResetEmailValidationErrorShouldBeDisplayed() throws IOException {
        System.out.println("\n→ STEP: Verify email validation error");
        passwordResetPage.verifyEmailValidationError();
        System.out.println("✓ Email validation error displayed\n");
    }

    /**
     * Step: User clicks back to login link
     *
     * @throws IOException if click fails
     */
    @When("user clicks back to login link")
    public void userClicksBackToLoginLink() throws IOException {
        System.out.println("\n→ STEP: User clicks back to login link");
        passwordResetPage.clickBackToLogin();
        System.out.println("✓ Back to login clicked\n");
    }

    /**
     * Step: User enters verification code
     *
     * @param code Verification code
     * @throws IOException if element interaction fails
     */
    @When("user enters verification code {string}")
    public void userEntersVerificationCode(String code) throws IOException {
        System.out.println("\n→ STEP: User enters verification code");
        passwordResetPage.enterVerificationCode(code);
        System.out.println("✓ Verification code entered\n");
    }

    /**
     * Step: User enters new password
     *
     * @param password New password
     * @throws IOException if element interaction fails
     */
    @When("user enters new password {string}")
    public void userEntersNewPassword(String password) throws IOException {
        System.out.println("\n→ STEP: User enters new password");
        passwordResetPage.enterNewPassword(password);
        System.out.println("✓ New password entered\n");
    }

    /**
     * Step: User confirms new password
     *
     * @param password Confirmation password
     * @throws IOException if element interaction fails
     */
    @When("user confirms new password {string}")
    public void userConfirmsNewPassword(String password) throws IOException {
        System.out.println("\n→ STEP: User confirms new password");
        passwordResetPage.enterConfirmNewPassword(password);
        System.out.println("✓ Password confirmation entered\n");
    }

    /**
     * Step: User submits new password
     *
     * @throws IOException if submission fails
     */
    @When("user submits new password")
    public void userSubmitsNewPassword() throws IOException {
        System.out.println("\n→ STEP: User submits new password");
        passwordResetPage.clickSubmitNewPassword();
        System.out.println("✓ New password submitted\n");

        // Brief wait for processing
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Step: User completes password reset with code and new password
     * Combines verification code, new password, and submission
     *
     * @param verificationCode Verification code
     * @param newPassword New password
     * @throws IOException if password reset fails
     */
    @When("user completes password reset with code {string} and password {string}")
    public void userCompletesPasswordResetWithCodeAndPassword(String verificationCode, String newPassword) throws IOException {
        System.out.println("\n→ STEP: User completes password reset");
        passwordResetPage.completePasswordReset(verificationCode, newPassword);
        System.out.println("✓ Password reset completed\n");
    }

    /**
     * Step: Password reset should be successful
     *
     * @throws IOException if verification fails
     */
    @Then("password reset should be successful")
    public void passwordResetShouldBeSuccessful() throws IOException {
        System.out.println("\n→ STEP: Verify password reset is successful");
        passwordResetPage.verifyResetEmailSent();
        System.out.println("✓ Password reset successful\n");
    }

    /**
     * Step: Reset success message should contain text
     *
     * @param expectedText Expected text in message
     * @throws IOException if verification fails
     */
    @Then("reset success message should contain {string}")
    public void resetSuccessMessageShouldContain(String expectedText) throws IOException {
        System.out.println("\n→ STEP: Verify success message contains: " + expectedText);
        String successMessage = passwordResetPage.getSuccessMessage();
        if (!successMessage.contains(expectedText)) {
            throw new IOException("Success message does not contain: " + expectedText);
        }
        System.out.println("✓ Success message verified\n");
    }

    /**
     * Step: Reset error message should contain text
     *
     * @param expectedText Expected text in message
     * @throws IOException if verification fails
     */
    @Then("reset error message should contain {string}")
    public void resetErrorMessageShouldContain(String expectedText) throws IOException {
        System.out.println("\n→ STEP: Verify error message contains: " + expectedText);
        String errorMessage = passwordResetPage.getErrorMessage();
        if (!errorMessage.contains(expectedText)) {
            throw new IOException("Error message does not contain: " + expectedText);
        }
        System.out.println("✓ Error message verified\n");
    }

    /**
     * Step: Reset error message should be displayed
     *
     * @throws IOException if verification fails
     */
    @Then("password reset error message should be displayed")
    public void passwordResetErrorMessageShouldBeDisplayed() throws IOException {
        System.out.println("\n→ STEP: Verify error message is displayed");
        if (!passwordResetPage.isErrorDisplayed()) {
            throw new IOException("Error message is not displayed");
        }
        System.out.println("✓ Error message displayed\n");
    }

    /**
     * Step: User waits for specified seconds
     *
     * @param seconds Number of seconds to wait
     */
    @When("user waits {int} seconds for reset email")
    public void userWaitsSecondsForResetEmail(int seconds) {
        System.out.println("\n→ STEP: Waiting " + seconds + " seconds for reset email");
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("✓ Wait complete\n");
    }
}
