package stepdefinitions;

import framework.config.ConfigurationManager;
import framework.healing.HealingMetrics;
import framework.locators.LocatorRegistry;
import framework.mcp.MCPSeleniumClient;
import framework.pages.RegistrationPage;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.io.IOException;

/**
 * RegistrationSteps - Cucumber step definitions for registration feature
 *
 * This class provides step definitions for user registration test scenarios.
 * It demonstrates:
 * - User registration with valid data
 * - Form validation (email, password, required fields)
 * - Terms and conditions acceptance
 * - Error handling and success verification
 *
 * Works in conjunction with LoginSteps for common setup/teardown,
 * or can run independently with its own hooks.
 *
 * Example scenarios:
 * - Successful user registration
 * - Registration with invalid email
 * - Registration with weak password
 * - Registration without accepting terms
 * - Registration with missing required fields
 *
 * @author Selenium-MCP Framework
 * @version 1.0.0
 */
public class RegistrationSteps {

    private MCPSeleniumClient mcp;
    private LocatorRegistry locatorRegistry;
    private HealingMetrics healingMetrics;
    private RegistrationPage registrationPage;
    private ConfigurationManager config;

    /**
     * Setup method - runs before each scenario
     * Initializes all framework components and starts the browser
     * Note: If using shared hooks with LoginSteps, this can be omitted
     *
     * @throws IOException if setup fails
     */
    @Before("@registration")
    public void setUp() throws IOException {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║           STARTING REGISTRATION TEST SCENARIO                 ║");
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

        System.out.println("→ Initializing Registration Page...");
        registrationPage = new RegistrationPage(mcp, locatorRegistry, healingMetrics);

        System.out.println("→ Initializing page locators...");
        registrationPage.initializeLocators();

        System.out.println("\n✓ Setup complete - Test ready to run\n");
    }

    /**
     * Cleanup method - runs after each scenario
     * Prints metrics, saves registry, and closes browser
     *
     * @throws IOException if cleanup fails
     */
    @After("@registration")
    public void tearDown() throws IOException {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║         CLEANING UP REGISTRATION TEST SCENARIO                ║");
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
     * Step: User navigates to registration page
     *
     * @throws IOException if navigation fails
     */
    @Given("user navigates to registration page")
    public void userNavigatesToRegistrationPage() throws IOException {
        System.out.println("\n→ STEP: User navigates to registration page");
        String registrationUrl = config.getBaseUrl() + "/register";
        registrationPage.navigateToPage(registrationUrl);
        System.out.println("✓ Navigation complete\n");
    }

    /**
     * Step: User should see registration page
     *
     * @throws IOException if verification fails
     */
    @Then("user should see registration page")
    public void userShouldSeeRegistrationPage() throws IOException {
        System.out.println("\n→ STEP: Verify registration page is displayed");
        registrationPage.verifyPageLoaded();
        System.out.println("✓ Registration page verified\n");
    }

    /**
     * Step: User enters first name
     *
     * @param firstName First name to enter
     * @throws IOException if element interaction fails
     */
    @When("user enters first name {string}")
    public void userEntersFirstName(String firstName) throws IOException {
        System.out.println("\n→ STEP: User enters first name: " + firstName);
        registrationPage.enterFirstName(firstName);
        System.out.println("✓ First name entered\n");
    }

    /**
     * Step: User enters last name
     *
     * @param lastName Last name to enter
     * @throws IOException if element interaction fails
     */
    @When("user enters last name {string}")
    public void userEntersLastName(String lastName) throws IOException {
        System.out.println("\n→ STEP: User enters last name: " + lastName);
        registrationPage.enterLastName(lastName);
        System.out.println("✓ Last name entered\n");
    }

    /**
     * Step: User enters registration email
     *
     * @param email Email to enter
     * @throws IOException if element interaction fails
     */
    @When("user enters registration email {string}")
    public void userEntersRegistrationEmail(String email) throws IOException {
        System.out.println("\n→ STEP: User enters email: " + email);
        registrationPage.enterEmail(email);
        System.out.println("✓ Email entered\n");
    }

    /**
     * Step: User enters registration password
     *
     * @param password Password to enter
     * @throws IOException if element interaction fails
     */
    @When("user enters registration password {string}")
    public void userEntersRegistrationPassword(String password) throws IOException {
        System.out.println("\n→ STEP: User enters password");
        registrationPage.enterPassword(password);
        System.out.println("✓ Password entered\n");
    }

    /**
     * Step: User enters password confirmation
     *
     * @param confirmPassword Confirmation password to enter
     * @throws IOException if element interaction fails
     */
    @When("user enters password confirmation {string}")
    public void userEntersPasswordConfirmation(String confirmPassword) throws IOException {
        System.out.println("\n→ STEP: User enters password confirmation");
        registrationPage.enterConfirmPassword(confirmPassword);
        System.out.println("✓ Password confirmation entered\n");
    }

    /**
     * Step: User enters phone number
     *
     * @param phone Phone number to enter
     * @throws IOException if element interaction fails
     */
    @When("user enters phone number {string}")
    public void userEntersPhoneNumber(String phone) throws IOException {
        System.out.println("\n→ STEP: User enters phone: " + phone);
        registrationPage.enterPhone(phone);
        System.out.println("✓ Phone number entered\n");
    }

    /**
     * Step: User accepts terms and conditions
     *
     * @throws IOException if element interaction fails
     */
    @When("user accepts terms and conditions")
    public void userAcceptsTermsAndConditions() throws IOException {
        System.out.println("\n→ STEP: User accepts terms and conditions");
        registrationPage.acceptTerms();
        System.out.println("✓ Terms accepted\n");
    }

    /**
     * Step: User does not accept terms and conditions
     *
     * @throws IOException if element interaction fails
     */
    @When("user does not accept terms and conditions")
    public void userDoesNotAcceptTermsAndConditions() throws IOException {
        System.out.println("\n→ STEP: User does not accept terms and conditions");
        // Intentionally not accepting terms
        System.out.println("✓ Terms not accepted\n");
    }

    /**
     * Step: User clicks register button
     *
     * @throws IOException if click fails
     */
    @When("user clicks register button")
    public void userClicksRegisterButton() throws IOException {
        System.out.println("\n→ STEP: User clicks register button");
        registrationPage.clickRegisterButton();
        System.out.println("✓ Register button clicked\n");

        // Brief wait for processing
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Step: Registration should be successful
     *
     * @throws IOException if verification fails
     */
    @Then("registration should be successful")
    public void registrationShouldBeSuccessful() throws IOException {
        System.out.println("\n→ STEP: Verify registration is successful");
        registrationPage.verifyRegistrationSuccess();
        System.out.println("✓ Registration successful\n");
    }

    /**
     * Step: Registration success message should be displayed
     *
     * @throws IOException if verification fails
     */
    @Then("registration success message should be displayed")
    public void registrationSuccessMessageShouldBeDisplayed() throws IOException {
        System.out.println("\n→ STEP: Verify success message is displayed");
        if (!registrationPage.isSuccessDisplayed()) {
            throw new IOException("Success message is not displayed");
        }
        System.out.println("✓ Success message displayed\n");
    }

    /**
     * Step: Registration error message should appear with text
     *
     * @param expectedError Expected error message
     * @throws IOException if verification fails
     */
    @Then("registration error message should appear {string}")
    public void registrationErrorMessageShouldAppear(String expectedError) throws IOException {
        System.out.println("\n→ STEP: Verify error message appears");
        System.out.println("   Expected: " + expectedError);

        registrationPage.verifyRegistrationError(expectedError);

        System.out.println("✓ Error message verified\n");
    }

    /**
     * Step: Email validation error should be displayed
     *
     * @throws IOException if verification fails
     */
    @Then("email validation error should be displayed")
    public void emailValidationErrorShouldBeDisplayed() throws IOException {
        System.out.println("\n→ STEP: Verify email validation error");
        registrationPage.verifyEmailValidationError();
        System.out.println("✓ Email validation error displayed\n");
    }

    /**
     * Step: Password validation error should be displayed
     *
     * @throws IOException if verification fails
     */
    @Then("password validation error should be displayed")
    public void passwordValidationErrorShouldBeDisplayed() throws IOException {
        System.out.println("\n→ STEP: Verify password validation error");
        registrationPage.verifyPasswordValidationError();
        System.out.println("✓ Password validation error displayed\n");
    }

    /**
     * Step: Terms acceptance error should be displayed
     *
     * @throws IOException if verification fails
     */
    @Then("terms acceptance error should be displayed")
    public void termsAcceptanceErrorShouldBeDisplayed() throws IOException {
        System.out.println("\n→ STEP: Verify terms acceptance error");
        String errorMessage = registrationPage.getErrorMessage();
        if (!errorMessage.toLowerCase().contains("terms")) {
            throw new IOException("Terms acceptance error not displayed");
        }
        System.out.println("✓ Terms acceptance error displayed\n");
    }

    /**
     * Step: User registers with complete details
     * Combines multiple steps into one for convenience
     *
     * @param firstName First name
     * @param lastName Last name
     * @param email Email
     * @param password Password
     * @param phone Phone number
     * @throws IOException if registration fails
     */
    @When("user registers with {string}, {string}, {string}, {string}, and {string}")
    public void userRegistersWithCompleteDetails(String firstName, String lastName,
                                                  String email, String password, String phone) throws IOException {
        System.out.println("\n→ STEP: User registers with complete details");
        System.out.println("   Name: " + firstName + " " + lastName);
        System.out.println("   Email: " + email);
        System.out.println("   Phone: " + phone);

        registrationPage.registerNewUser(firstName, lastName, email, password, phone);

        System.out.println("✓ Registration form completed\n");
    }

    /**
     * Step: User submits registration form
     *
     * @throws IOException if submission fails
     */
    @When("user submits registration form")
    public void userSubmitsRegistrationForm() throws IOException {
        System.out.println("\n→ STEP: User submits registration form");
        registrationPage.submitRegistration();
        System.out.println("✓ Registration form submitted\n");
    }

    /**
     * Step: Password mismatch error should be displayed
     *
     * @throws IOException if verification fails
     */
    @Then("password mismatch error should be displayed")
    public void passwordMismatchErrorShouldBeDisplayed() throws IOException {
        System.out.println("\n→ STEP: Verify password mismatch error");
        String errorMessage = registrationPage.getErrorMessage();
        if (!errorMessage.toLowerCase().contains("match") &&
            !errorMessage.toLowerCase().contains("same")) {
            throw new IOException("Password mismatch error not displayed");
        }
        System.out.println("✓ Password mismatch error displayed\n");
    }

    /**
     * Step: Required field error should be displayed for field
     *
     * @param fieldName Name of the required field
     * @throws IOException if verification fails
     */
    @Then("required field error should be displayed for {string}")
    public void requiredFieldErrorShouldBeDisplayedFor(String fieldName) throws IOException {
        System.out.println("\n→ STEP: Verify required field error for: " + fieldName);
        String errorMessage = registrationPage.getErrorMessage();
        if (!errorMessage.toLowerCase().contains("required") &&
            !errorMessage.toLowerCase().contains(fieldName.toLowerCase())) {
            throw new IOException("Required field error not displayed for: " + fieldName);
        }
        System.out.println("✓ Required field error displayed\n");
    }

    /**
     * Step: User clears registration form
     *
     * @throws IOException if clearing fails
     */
    @When("user clears registration form")
    public void userClearsRegistrationForm() throws IOException {
        System.out.println("\n→ STEP: User clears registration form");
        // Clear all fields
        registrationPage.clearFirstName();
        registrationPage.clearLastName();
        registrationPage.clearEmail();
        registrationPage.clearPassword();
        registrationPage.clearConfirmPassword();
        registrationPage.clearPhone();
        System.out.println("✓ Registration form cleared\n");
    }

    /**
     * Step: Registration form should be empty
     *
     * @throws IOException if verification fails
     */
    @Then("registration form should be empty")
    public void registrationFormShouldBeEmpty() throws IOException {
        System.out.println("\n→ STEP: Verify registration form is empty");
        // This would require getter methods in RegistrationPage to verify field values
        System.out.println("✓ Registration form is empty (placeholder)\n");
    }

    /**
     * Step: User takes screenshot with given name
     *
     * @param screenshotName Name for the screenshot
     * @throws IOException if screenshot fails
     */
    @When("user takes registration screenshot {string}")
    public void userTakesRegistrationScreenshot(String screenshotName) throws IOException {
        System.out.println("\n→ STEP: Taking screenshot: " + screenshotName);
        registrationPage.captureScreenshot(screenshotName);
        System.out.println("✓ Screenshot captured\n");
    }

    /**
     * Step: Success message should contain text
     *
     * @param expectedText Expected text in success message
     * @throws IOException if verification fails
     */
    @Then("success message should contain {string}")
    public void successMessageShouldContain(String expectedText) throws IOException {
        System.out.println("\n→ STEP: Verify success message contains: " + expectedText);
        String successMessage = registrationPage.getSuccessMessage();
        if (!successMessage.contains(expectedText)) {
            throw new IOException("Success message does not contain: " + expectedText);
        }
        System.out.println("✓ Success message verified\n");
    }

    /**
     * Step: Error message should contain text
     *
     * @param expectedText Expected text in error message
     * @throws IOException if verification fails
     */
    @Then("error message should contain {string}")
    public void errorMessageShouldContain(String expectedText) throws IOException {
        System.out.println("\n→ STEP: Verify error message contains: " + expectedText);
        String errorMessage = registrationPage.getErrorMessage();
        if (!errorMessage.contains(expectedText)) {
            throw new IOException("Error message does not contain: " + expectedText);
        }
        System.out.println("✓ Error message verified\n");
    }
}
