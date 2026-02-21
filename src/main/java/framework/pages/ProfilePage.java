package framework.pages;

import framework.healing.HealingMetrics;
import framework.locators.LocatorRegistry;
import framework.locators.LocatorRegistry.LocatorStrategy;
import framework.mcp.MCPSeleniumClient;

import java.io.IOException;

/**
 * ProfilePage - Page object for user profile management
 *
 * This class provides methods to interact with the user profile page.
 * It demonstrates profile editing, picture upload, and data validation.
 *
 * Key Features:
 * - View profile information
 * - Edit profile fields
 * - Upload profile picture
 * - Save/cancel changes
 * - Profile validation
 *
 * Example Usage:
 * <pre>
 * ProfilePage profilePage = new ProfilePage(mcp, registry, metrics);
 * profilePage.initializeLocators();
 * profilePage.updateProfile("John", "Doe", "john.doe@example.com", "555-1234");
 * profilePage.saveChanges();
 * </pre>
 *
 * @author Selenium-MCP Framework
 * @version 1.0.0
 */
public class ProfilePage extends BasePage {

    // Element name constants
    public static final String FIRST_NAME_INPUT = "profileFirstName";
    public static final String LAST_NAME_INPUT = "profileLastName";
    public static final String EMAIL_INPUT = "profileEmail";
    public static final String PHONE_INPUT = "profilePhone";
    public static final String ADDRESS_INPUT = "profileAddress";
    public static final String PROFILE_PICTURE = "profilePicture";
    public static final String UPLOAD_PICTURE_BUTTON = "uploadPictureButton";
    public static final String SAVE_BUTTON = "saveProfileButton";
    public static final String CANCEL_BUTTON = "cancelProfileButton";
    public static final String EDIT_BUTTON = "editProfileButton";
    public static final String SUCCESS_MESSAGE = "profileSuccessMessage";
    public static final String ERROR_MESSAGE = "profileErrorMessage";
    public static final String PROFILE_NAME_DISPLAY = "profileNameDisplay";
    public static final String PROFILE_EMAIL_DISPLAY = "profileEmailDisplay";
    public static final String CHANGE_PASSWORD_LINK = "changePasswordLink";

    /**
     * Creates a new ProfilePage instance
     *
     * @param mcp MCP Selenium client
     * @param locatorRegistry Locator registry
     * @param healingMetrics Healing metrics tracker
     */
    public ProfilePage(MCPSeleniumClient mcp, LocatorRegistry locatorRegistry, HealingMetrics healingMetrics) {
        super(mcp, locatorRegistry, healingMetrics);
    }

    /**
     * Initializes all locators for the profile page
     */
    public void initializeLocators() {
        log("Initializing locators for ProfilePage");

        // First name input
        locatorRegistry.addElement(FIRST_NAME_INPUT,
                new LocatorStrategy("id", "firstName", 1),
                new LocatorStrategy("name", "firstName", 2),
                new LocatorStrategy("css", "input[name='firstName']", 3),
                new LocatorStrategy("xpath", "//input[@id='firstName' or @name='firstName']", 4)
        );

        // Last name input
        locatorRegistry.addElement(LAST_NAME_INPUT,
                new LocatorStrategy("id", "lastName", 1),
                new LocatorStrategy("name", "lastName", 2),
                new LocatorStrategy("css", "input[name='lastName']", 3),
                new LocatorStrategy("xpath", "//input[@id='lastName' or @name='lastName']", 4)
        );

        // Email input
        locatorRegistry.addElement(EMAIL_INPUT,
                new LocatorStrategy("id", "email", 1),
                new LocatorStrategy("name", "email", 2),
                new LocatorStrategy("css", "input[type='email']", 3),
                new LocatorStrategy("xpath", "//input[@type='email' or @name='email']", 4)
        );

        // Phone input
        locatorRegistry.addElement(PHONE_INPUT,
                new LocatorStrategy("id", "phone", 1),
                new LocatorStrategy("name", "phone", 2),
                new LocatorStrategy("css", "input[type='tel']", 3),
                new LocatorStrategy("xpath", "//input[@type='tel' or @name='phone']", 4)
        );

        // Address input
        locatorRegistry.addElement(ADDRESS_INPUT,
                new LocatorStrategy("id", "address", 1),
                new LocatorStrategy("name", "address", 2),
                new LocatorStrategy("css", "textarea[name='address']", 3),
                new LocatorStrategy("xpath", "//textarea[@name='address' or @id='address']", 4)
        );

        // Profile picture
        locatorRegistry.addElement(PROFILE_PICTURE,
                new LocatorStrategy("id", "profile-picture", 1),
                new LocatorStrategy("css", ".profile-picture", 2),
                new LocatorStrategy("xpath", "//img[contains(@class, 'profile-picture')]", 3),
                new LocatorStrategy("css", "img.avatar", 4)
        );

        // Upload picture button
        locatorRegistry.addElement(UPLOAD_PICTURE_BUTTON,
                new LocatorStrategy("id", "upload-picture", 1),
                new LocatorStrategy("css", "input[type='file']", 2),
                new LocatorStrategy("xpath", "//input[@type='file']", 3),
                new LocatorStrategy("name", "profilePicture", 4)
        );

        // Save button
        locatorRegistry.addElement(SAVE_BUTTON,
                new LocatorStrategy("id", "save-profile", 1),
                new LocatorStrategy("css", "button.save-profile", 2),
                new LocatorStrategy("xpath", "//button[contains(text(), 'Save')]", 3),
                new LocatorStrategy("name", "save", 4)
        );

        // Cancel button
        locatorRegistry.addElement(CANCEL_BUTTON,
                new LocatorStrategy("id", "cancel-profile", 1),
                new LocatorStrategy("css", "button.cancel-profile", 2),
                new LocatorStrategy("xpath", "//button[contains(text(), 'Cancel')]", 3),
                new LocatorStrategy("name", "cancel", 4)
        );

        // Edit button
        locatorRegistry.addElement(EDIT_BUTTON,
                new LocatorStrategy("id", "edit-profile", 1),
                new LocatorStrategy("css", "button.edit-profile", 2),
                new LocatorStrategy("xpath", "//button[contains(text(), 'Edit')]", 3),
                new LocatorStrategy("name", "edit", 4)
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

        // Profile name display
        locatorRegistry.addElement(PROFILE_NAME_DISPLAY,
                new LocatorStrategy("id", "profile-name", 1),
                new LocatorStrategy("css", ".profile-name", 2),
                new LocatorStrategy("xpath", "//div[contains(@class, 'profile-name')]", 3),
                new LocatorStrategy("css", "h2.user-name", 4)
        );

        // Profile email display
        locatorRegistry.addElement(PROFILE_EMAIL_DISPLAY,
                new LocatorStrategy("id", "profile-email", 1),
                new LocatorStrategy("css", ".profile-email", 2),
                new LocatorStrategy("xpath", "//div[contains(@class, 'profile-email')]", 3),
                new LocatorStrategy("css", "span.user-email", 4)
        );

        // Change password link
        locatorRegistry.addElement(CHANGE_PASSWORD_LINK,
                new LocatorStrategy("id", "change-password", 1),
                new LocatorStrategy("linkText", "Change Password", 2),
                new LocatorStrategy("css", "a[href*='password']", 3),
                new LocatorStrategy("xpath", "//a[contains(text(), 'Change Password')]", 4)
        );

        log("✓ Locators initialized successfully");
    }

    /**
     * Clicks the edit profile button
     *
     * @throws IOException if element interaction fails
     */
    public void clickEditButton() throws IOException {
        log("Clicking edit profile button");
        element(EDIT_BUTTON).click();
    }

    /**
     * Enters first name
     *
     * @param firstName First name
     * @throws IOException if element interaction fails
     */
    public void enterFirstName(String firstName) throws IOException {
        log("Entering first name: " + firstName);
        element(FIRST_NAME_INPUT).typeText(firstName);
    }

    /**
     * Enters last name
     *
     * @param lastName Last name
     * @throws IOException if element interaction fails
     */
    public void enterLastName(String lastName) throws IOException {
        log("Entering last name: " + lastName);
        element(LAST_NAME_INPUT).typeText(lastName);
    }

    /**
     * Enters email
     *
     * @param email Email address
     * @throws IOException if element interaction fails
     */
    public void enterEmail(String email) throws IOException {
        log("Entering email: " + email);
        element(EMAIL_INPUT).typeText(email);
    }

    /**
     * Enters phone number
     *
     * @param phone Phone number
     * @throws IOException if element interaction fails
     */
    public void enterPhone(String phone) throws IOException {
        log("Entering phone: " + phone);
        element(PHONE_INPUT).typeText(phone);
    }

    /**
     * Enters address
     *
     * @param address Address
     * @throws IOException if element interaction fails
     */
    public void enterAddress(String address) throws IOException {
        log("Entering address: " + address);
        element(ADDRESS_INPUT).typeText(address);
    }

    /**
     * Uploads profile picture
     *
     * @param filePath Path to profile picture file
     * @throws IOException if upload fails
     */
    public void uploadProfilePicture(String filePath) throws IOException {
        log("Uploading profile picture: " + filePath);
        element(UPLOAD_PICTURE_BUTTON).uploadFile(filePath);
        log("✓ Profile picture uploaded");
    }

    /**
     * Clicks save button
     *
     * @throws IOException if element interaction fails
     */
    public void clickSaveButton() throws IOException {
        log("Clicking save button");
        element(SAVE_BUTTON).click();
    }

    /**
     * Clicks cancel button
     *
     * @throws IOException if element interaction fails
     */
    public void clickCancelButton() throws IOException {
        log("Clicking cancel button");
        element(CANCEL_BUTTON).click();
    }

    /**
     * Updates profile with all fields
     *
     * @param firstName First name
     * @param lastName Last name
     * @param email Email
     * @param phone Phone
     * @throws IOException if update fails
     */
    public void updateProfile(String firstName, String lastName, String email, String phone) throws IOException {
        log("Updating profile");
        clickEditButton();
        enterFirstName(firstName);
        enterLastName(lastName);
        enterEmail(email);
        enterPhone(phone);
        log("✓ Profile fields updated");
    }

    /**
     * Saves profile changes
     *
     * @throws IOException if save fails
     */
    public void saveChanges() throws IOException {
        log("Saving profile changes");
        clickSaveButton();
        log("✓ Profile changes saved");
    }

    /**
     * Cancels profile changes
     *
     * @throws IOException if cancel fails
     */
    public void cancelChanges() throws IOException {
        log("Canceling profile changes");
        clickCancelButton();
        log("✓ Profile changes canceled");
    }

    /**
     * Gets success message text
     *
     * @return Success message
     * @throws IOException if element not found
     */
    public String getSuccessMessage() throws IOException {
        log("Getting success message");
        String message = element(SUCCESS_MESSAGE).getText();
        log("Success message: " + message);
        return message;
    }

    /**
     * Gets error message text
     *
     * @return Error message
     * @throws IOException if element not found
     */
    public String getErrorMessage() throws IOException {
        log("Getting error message");
        String message = element(ERROR_MESSAGE).getText();
        log("Error message: " + message);
        return message;
    }

    /**
     * Gets profile name display
     *
     * @return Profile name
     * @throws IOException if element not found
     */
    public String getProfileName() throws IOException {
        log("Getting profile name");
        return element(PROFILE_NAME_DISPLAY).getText();
    }

    /**
     * Gets profile email display
     *
     * @return Profile email
     * @throws IOException if element not found
     */
    public String getProfileEmail() throws IOException {
        log("Getting profile email");
        return element(PROFILE_EMAIL_DISPLAY).getText();
    }

    /**
     * Verifies profile updated successfully
     *
     * @throws IOException if verification fails
     */
    public void verifyProfileUpdated() throws IOException {
        log("Verifying profile updated");
        try {
            waitForElementVisible(SUCCESS_MESSAGE, 5000);
            String message = getSuccessMessage();
            log("✓ Profile updated - Message: " + message);
        } catch (IOException e) {
            logError("✗ Profile update verification failed");
            throw new IOException("Profile update verification failed", e);
        }
    }

    /**
     * Clicks change password link
     *
     * @throws IOException if element interaction fails
     */
    public void clickChangePassword() throws IOException {
        log("Clicking change password link");
        element(CHANGE_PASSWORD_LINK).click();
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
     * Verifies profile page is loaded
     *
     * @throws IOException if verification fails
     */
    public void verifyPageLoaded() throws IOException {
        log("Verifying profile page loaded");
        try {
            assertElementVisible(PROFILE_NAME_DISPLAY);
            assertElementVisible(PROFILE_EMAIL_DISPLAY);
            assertElementVisible(EDIT_BUTTON);
            log("✓ Profile page loaded successfully");
        } catch (IOException e) {
            logError("✗ Profile page verification failed");
            throw new IOException("Profile page not loaded properly", e);
        }
    }
}
