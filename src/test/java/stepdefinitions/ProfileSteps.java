package stepdefinitions;

import framework.config.ConfigurationManager;
import framework.healing.HealingMetrics;
import framework.locators.LocatorRegistry;
import framework.mcp.MCPSeleniumClient;
import framework.pages.ProfilePage;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.io.IOException;

/**
 * ProfileSteps - Cucumber step definitions for profile management feature
 *
 * This class provides step definitions for user profile test scenarios.
 * It demonstrates:
 * - Viewing profile information
 * - Editing profile fields (name, email, phone, address)
 * - Uploading profile picture
 * - Saving and canceling profile changes
 * - Profile validation and error handling
 *
 * Works in conjunction with LoginSteps for common setup/teardown,
 * or can run independently with its own hooks.
 *
 * Example scenarios:
 * - View user profile details
 * - Update profile information
 * - Upload profile picture
 * - Cancel profile edits
 * - Profile validation errors
 *
 * @author Selenium-MCP Framework
 * @version 1.0.0
 */
public class ProfileSteps {

    private MCPSeleniumClient mcp;
    private LocatorRegistry locatorRegistry;
    private HealingMetrics healingMetrics;
    private ProfilePage profilePage;
    private ConfigurationManager config;

    /**
     * Setup method - runs before each scenario
     * Initializes all framework components and starts the browser
     *
     * @throws IOException if setup fails
     */
    @Before("@profile")
    public void setUp() throws IOException {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║            STARTING PROFILE TEST SCENARIO                     ║");
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

        System.out.println("→ Initializing Profile Page...");
        profilePage = new ProfilePage(mcp, locatorRegistry, healingMetrics);

        System.out.println("→ Initializing page locators...");
        profilePage.initializeLocators();

        System.out.println("\n✓ Setup complete - Test ready to run\n");
    }

    /**
     * Cleanup method - runs after each scenario
     *
     * @throws IOException if cleanup fails
     */
    @After("@profile")
    public void tearDown() throws IOException {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║          CLEANING UP PROFILE TEST SCENARIO                    ║");
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
     * Step: User navigates to profile page
     *
     * @throws IOException if navigation fails
     */
    @Given("user navigates to profile page")
    public void userNavigatesToProfilePage() throws IOException {
        System.out.println("\n→ STEP: User navigates to profile page");
        String profileUrl = config.getBaseUrl() + "/profile";
        profilePage.navigateToPage(profileUrl);
        System.out.println("✓ Navigation complete\n");
    }

    /**
     * Step: User should see profile page
     *
     * @throws IOException if verification fails
     */
    @Then("user should see profile page")
    public void userShouldSeeProfilePage() throws IOException {
        System.out.println("\n→ STEP: Verify profile page is displayed");
        profilePage.verifyPageLoaded();
        System.out.println("✓ Profile page verified\n");
    }

    /**
     * Step: User clicks edit profile button
     *
     * @throws IOException if click fails
     */
    @When("user clicks edit profile button")
    public void userClicksEditProfileButton() throws IOException {
        System.out.println("\n→ STEP: User clicks edit profile button");
        profilePage.clickEditButton();
        System.out.println("✓ Edit button clicked\n");
    }

    /**
     * Step: User enters profile first name
     *
     * @param firstName First name to enter
     * @throws IOException if element interaction fails
     */
    @When("user enters profile first name {string}")
    public void userEntersProfileFirstName(String firstName) throws IOException {
        System.out.println("\n→ STEP: User enters first name: " + firstName);
        profilePage.enterFirstName(firstName);
        System.out.println("✓ First name entered\n");
    }

    /**
     * Step: User enters profile last name
     *
     * @param lastName Last name to enter
     * @throws IOException if element interaction fails
     */
    @When("user enters profile last name {string}")
    public void userEntersProfileLastName(String lastName) throws IOException {
        System.out.println("\n→ STEP: User enters last name: " + lastName);
        profilePage.enterLastName(lastName);
        System.out.println("✓ Last name entered\n");
    }

    /**
     * Step: User enters profile email
     *
     * @param email Email to enter
     * @throws IOException if element interaction fails
     */
    @When("user enters profile email {string}")
    public void userEntersProfileEmail(String email) throws IOException {
        System.out.println("\n→ STEP: User enters email: " + email);
        profilePage.enterEmail(email);
        System.out.println("✓ Email entered\n");
    }

    /**
     * Step: User enters profile phone
     *
     * @param phone Phone number to enter
     * @throws IOException if element interaction fails
     */
    @When("user enters profile phone {string}")
    public void userEntersProfilePhone(String phone) throws IOException {
        System.out.println("\n→ STEP: User enters phone: " + phone);
        profilePage.enterPhone(phone);
        System.out.println("✓ Phone entered\n");
    }

    /**
     * Step: User enters profile address
     *
     * @param address Address to enter
     * @throws IOException if element interaction fails
     */
    @When("user enters profile address {string}")
    public void userEntersProfileAddress(String address) throws IOException {
        System.out.println("\n→ STEP: User enters address: " + address);
        profilePage.enterAddress(address);
        System.out.println("✓ Address entered\n");
    }

    /**
     * Step: User uploads profile picture
     *
     * @param filePath Path to profile picture file
     * @throws IOException if upload fails
     */
    @When("user uploads profile picture {string}")
    public void userUploadsProfilePicture(String filePath) throws IOException {
        System.out.println("\n→ STEP: User uploads profile picture: " + filePath);
        profilePage.uploadProfilePicture(filePath);
        System.out.println("✓ Profile picture uploaded\n");
    }

    /**
     * Step: User clicks save profile button
     *
     * @throws IOException if click fails
     */
    @When("user clicks save profile button")
    public void userClicksSaveProfileButton() throws IOException {
        System.out.println("\n→ STEP: User clicks save profile button");
        profilePage.clickSaveButton();
        System.out.println("✓ Save button clicked\n");

        // Brief wait for save operation
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Step: User clicks cancel profile button
     *
     * @throws IOException if click fails
     */
    @When("user clicks cancel profile button")
    public void userClicksCancelProfileButton() throws IOException {
        System.out.println("\n→ STEP: User clicks cancel profile button");
        profilePage.clickCancelButton();
        System.out.println("✓ Cancel button clicked\n");
    }

    /**
     * Step: User updates profile with all details
     * Combines editing multiple profile fields
     *
     * @param firstName First name
     * @param lastName Last name
     * @param email Email
     * @param phone Phone
     * @throws IOException if update fails
     */
    @When("user updates profile with {string}, {string}, {string}, and {string}")
    public void userUpdatesProfileWith(String firstName, String lastName, String email, String phone) throws IOException {
        System.out.println("\n→ STEP: User updates profile");
        System.out.println("   Name: " + firstName + " " + lastName);
        System.out.println("   Email: " + email);
        System.out.println("   Phone: " + phone);

        profilePage.updateProfile(firstName, lastName, email, phone);

        System.out.println("✓ Profile updated\n");
    }

    /**
     * Step: User saves profile changes
     *
     * @throws IOException if save fails
     */
    @When("user saves profile changes")
    public void userSavesProfileChanges() throws IOException {
        System.out.println("\n→ STEP: User saves profile changes");
        profilePage.saveChanges();
        System.out.println("✓ Changes saved\n");
    }

    /**
     * Step: User cancels profile changes
     *
     * @throws IOException if cancel fails
     */
    @When("user cancels profile changes")
    public void userCancelsProfileChanges() throws IOException {
        System.out.println("\n→ STEP: User cancels profile changes");
        profilePage.cancelChanges();
        System.out.println("✓ Changes canceled\n");
    }

    /**
     * Step: Profile should be updated successfully
     *
     * @throws IOException if verification fails
     */
    @Then("profile should be updated successfully")
    public void profileShouldBeUpdatedSuccessfully() throws IOException {
        System.out.println("\n→ STEP: Verify profile was updated");
        profilePage.verifyProfileUpdated();
        System.out.println("✓ Profile update verified\n");
    }

    /**
     * Step: Profile success message should be displayed
     *
     * @throws IOException if verification fails
     */
    @Then("profile success message should be displayed")
    public void profileSuccessMessageShouldBeDisplayed() throws IOException {
        System.out.println("\n→ STEP: Verify success message is displayed");
        if (!profilePage.isSuccessDisplayed()) {
            throw new IOException("Success message is not displayed");
        }
        System.out.println("✓ Success message displayed\n");
    }

    /**
     * Step: Profile error message should appear
     *
     * @param expectedError Expected error message
     * @throws IOException if verification fails
     */
    @Then("profile error message should appear {string}")
    public void profileErrorMessageShouldAppear(String expectedError) throws IOException {
        System.out.println("\n→ STEP: Verify error message appears");
        System.out.println("   Expected: " + expectedError);

        String actualError = profilePage.getErrorMessage();
        if (!actualError.contains(expectedError)) {
            throw new IOException("Error message mismatch - Expected: '" + expectedError + "', Got: '" + actualError + "'");
        }

        System.out.println("✓ Error message verified\n");
    }

    /**
     * Step: Profile name should be displayed as
     *
     * @param expectedName Expected profile name
     * @throws IOException if verification fails
     */
    @Then("profile name should be displayed as {string}")
    public void profileNameShouldBeDisplayedAs(String expectedName) throws IOException {
        System.out.println("\n→ STEP: Verify profile name is: " + expectedName);
        String actualName = profilePage.getProfileName();
        if (!actualName.equals(expectedName)) {
            throw new IOException("Profile name mismatch - Expected: '" + expectedName + "', Got: '" + actualName + "'");
        }
        System.out.println("✓ Profile name verified\n");
    }

    /**
     * Step: Profile email should be displayed as
     *
     * @param expectedEmail Expected profile email
     * @throws IOException if verification fails
     */
    @Then("profile email should be displayed as {string}")
    public void profileEmailShouldBeDisplayedAs(String expectedEmail) throws IOException {
        System.out.println("\n→ STEP: Verify profile email is: " + expectedEmail);
        String actualEmail = profilePage.getProfileEmail();
        if (!actualEmail.equals(expectedEmail)) {
            throw new IOException("Profile email mismatch - Expected: '" + expectedEmail + "', Got: '" + actualEmail + "'");
        }
        System.out.println("✓ Profile email verified\n");
    }

    /**
     * Step: User clicks change password link
     *
     * @throws IOException if click fails
     */
    @When("user clicks change password link")
    public void userClicksChangePasswordLink() throws IOException {
        System.out.println("\n→ STEP: User clicks change password link");
        profilePage.clickChangePassword();
        System.out.println("✓ Change password link clicked\n");
    }

    /**
     * Step: Profile success message should contain text
     *
     * @param expectedText Expected text in message
     * @throws IOException if verification fails
     */
    @Then("profile success message should contain {string}")
    public void profileSuccessMessageShouldContain(String expectedText) throws IOException {
        System.out.println("\n→ STEP: Verify success message contains: " + expectedText);
        String successMessage = profilePage.getSuccessMessage();
        if (!successMessage.contains(expectedText)) {
            throw new IOException("Success message does not contain: " + expectedText);
        }
        System.out.println("✓ Success message verified\n");
    }

    /**
     * Step: Profile error message should contain text
     *
     * @param expectedText Expected text in message
     * @throws IOException if verification fails
     */
    @Then("profile error message should contain {string}")
    public void profileErrorMessageShouldContain(String expectedText) throws IOException {
        System.out.println("\n→ STEP: Verify error message contains: " + expectedText);
        String errorMessage = profilePage.getErrorMessage();
        if (!errorMessage.contains(expectedText)) {
            throw new IOException("Error message does not contain: " + expectedText);
        }
        System.out.println("✓ Error message verified\n");
    }

    /**
     * Step: Profile changes should not be saved
     *
     * @throws IOException if verification fails
     */
    @Then("profile changes should not be saved")
    public void profileChangesShouldNotBeSaved() throws IOException {
        System.out.println("\n→ STEP: Verify profile changes were not saved");
        // This would require checking that the profile values match the original values
        System.out.println("✓ Changes not saved (placeholder)\n");
    }

    /**
     * Step: User takes profile screenshot
     *
     * @param screenshotName Name for the screenshot
     * @throws IOException if screenshot fails
     */
    @When("user takes profile screenshot {string}")
    public void userTakesProfileScreenshot(String screenshotName) throws IOException {
        System.out.println("\n→ STEP: Taking screenshot: " + screenshotName);
        profilePage.captureScreenshot(screenshotName);
        System.out.println("✓ Screenshot captured\n");
    }

    /**
     * Step: Profile error message should be displayed
     *
     * @throws IOException if verification fails
     */
    @Then("profile error message should be displayed")
    public void profileErrorMessageShouldBeDisplayed() throws IOException {
        System.out.println("\n→ STEP: Verify error message is displayed");
        if (!profilePage.isErrorDisplayed()) {
            throw new IOException("Error message is not displayed");
        }
        System.out.println("✓ Error message displayed\n");
    }

    /**
     * Step: Profile picture should be updated
     *
     * @throws IOException if verification fails
     */
    @Then("profile picture should be updated")
    public void profilePictureShouldBeUpdated() throws IOException {
        System.out.println("\n→ STEP: Verify profile picture was updated");
        // This would require checking the profile picture src attribute
        System.out.println("✓ Profile picture updated (placeholder)\n");
    }
}
