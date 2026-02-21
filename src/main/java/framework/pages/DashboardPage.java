package framework.pages;

import framework.healing.HealingMetrics;
import framework.locators.LocatorRegistry;
import framework.locators.LocatorRegistry.LocatorStrategy;
import framework.mcp.MCPSeleniumClient;

import java.io.IOException;

/**
 * DashboardPage - Page object for dashboard functionality
 *
 * This class provides methods to interact with the dashboard page after successful login.
 * It demonstrates self-healing element interactions for common dashboard operations.
 *
 * Key Features:
 * - Welcome message verification
 * - User profile interaction
 * - Logout functionality
 * - Dashboard element verification
 * - Navigation to different sections
 *
 * Example Usage:
 * <pre>
 * DashboardPage dashboard = new DashboardPage(mcp, registry, metrics);
 * dashboard.initializeLocators();
 * dashboard.verifyPageLoaded();
 * String welcome = dashboard.getWelcomeMessage();
 * dashboard.clickLogout();
 * </pre>
 *
 * @author Selenium-MCP Framework
 * @version 1.0.0
 */
public class DashboardPage extends BasePage {

    // Element name constants
    public static final String WELCOME_MESSAGE = "welcomeMessage";
    public static final String LOGOUT_BUTTON = "logoutButton";
    public static final String USER_PROFILE = "userProfile";
    public static final String DASHBOARD_TITLE = "dashboardTitle";
    public static final String NAVIGATION_MENU = "navigationMenu";
    public static final String NOTIFICATIONS_ICON = "notificationsIcon";
    public static final String SETTINGS_LINK = "settingsLink";
    public static final String USER_AVATAR = "userAvatar";

    /**
     * Creates a new DashboardPage instance
     *
     * @param mcp MCP Selenium client
     * @param locatorRegistry Locator registry
     * @param healingMetrics Healing metrics tracker
     */
    public DashboardPage(MCPSeleniumClient mcp, LocatorRegistry locatorRegistry, HealingMetrics healingMetrics) {
        super(mcp, locatorRegistry, healingMetrics);
    }

    /**
     * Initializes all locators for the dashboard page
     * Each element has multiple fallback strategies for resilience
     *
     * This method should be called once during setup before using the page
     */
    public void initializeLocators() {
        log("Initializing locators for DashboardPage");

        // Welcome message
        locatorRegistry.addElement(WELCOME_MESSAGE,
                new LocatorStrategy("id", "welcome", 1),
                new LocatorStrategy("css", ".welcome-message", 2),
                new LocatorStrategy("xpath", "//h1[@class='welcome' or @id='welcome']", 3),
                new LocatorStrategy("css", "[data-testid='welcome-message']", 4)
        );

        // Logout button
        locatorRegistry.addElement(LOGOUT_BUTTON,
                new LocatorStrategy("id", "logout-btn", 1),
                new LocatorStrategy("css", "button.logout", 2),
                new LocatorStrategy("xpath", "//button[contains(text(), 'Logout') or contains(text(), 'Log out')]", 3),
                new LocatorStrategy("linkText", "Logout", 4)
        );

        // User profile section
        locatorRegistry.addElement(USER_PROFILE,
                new LocatorStrategy("id", "user-profile", 1),
                new LocatorStrategy("css", ".user-profile", 2),
                new LocatorStrategy("xpath", "//div[contains(@class, 'user-profile')]", 3),
                new LocatorStrategy("css", "[data-testid='user-profile']", 4)
        );

        // Dashboard title
        locatorRegistry.addElement(DASHBOARD_TITLE,
                new LocatorStrategy("id", "dashboard-title", 1),
                new LocatorStrategy("css", "h1.dashboard-title", 2),
                new LocatorStrategy("xpath", "//h1[contains(text(), 'Dashboard')]", 3),
                new LocatorStrategy("tagName", "h1", 4)
        );

        // Navigation menu
        locatorRegistry.addElement(NAVIGATION_MENU,
                new LocatorStrategy("id", "nav-menu", 1),
                new LocatorStrategy("css", "nav.navigation", 2),
                new LocatorStrategy("xpath", "//nav[@role='navigation']", 3),
                new LocatorStrategy("css", ".nav-menu", 4)
        );

        // Notifications icon
        locatorRegistry.addElement(NOTIFICATIONS_ICON,
                new LocatorStrategy("id", "notifications", 1),
                new LocatorStrategy("css", ".notifications-icon", 2),
                new LocatorStrategy("xpath", "//i[contains(@class, 'notification')]", 3),
                new LocatorStrategy("css", "[data-testid='notifications']", 4)
        );

        // Settings link
        locatorRegistry.addElement(SETTINGS_LINK,
                new LocatorStrategy("id", "settings-link", 1),
                new LocatorStrategy("linkText", "Settings", 2),
                new LocatorStrategy("css", "a[href*='settings']", 3),
                new LocatorStrategy("xpath", "//a[contains(text(), 'Settings')]", 4)
        );

        // User avatar
        locatorRegistry.addElement(USER_AVATAR,
                new LocatorStrategy("id", "user-avatar", 1),
                new LocatorStrategy("css", ".user-avatar", 2),
                new LocatorStrategy("xpath", "//img[contains(@class, 'avatar')]", 3),
                new LocatorStrategy("css", "img.profile-pic", 4)
        );

        log("✓ Locators initialized successfully");
    }

    /**
     * Gets the welcome message text
     *
     * @return Welcome message text
     * @throws IOException if element not found
     */
    public String getWelcomeMessage() throws IOException {
        log("Getting welcome message");
        String message = element(WELCOME_MESSAGE).getText();
        log("Welcome message: " + message);
        return message;
    }

    /**
     * Clicks the logout button
     *
     * @throws IOException if element interaction fails
     */
    public void clickLogout() throws IOException {
        log("Clicking logout button");
        element(LOGOUT_BUTTON).click();
        log("✓ Logout button clicked");
    }

    /**
     * Gets the dashboard title text
     *
     * @return Dashboard title text
     * @throws IOException if element not found
     */
    public String getDashboardTitle() throws IOException {
        log("Getting dashboard title");
        String title = element(DASHBOARD_TITLE).getText();
        log("Dashboard title: " + title);
        return title;
    }

    /**
     * Clicks on the user profile section
     *
     * @throws IOException if element interaction fails
     */
    public void clickUserProfile() throws IOException {
        log("Clicking user profile");
        element(USER_PROFILE).click();
        log("✓ User profile clicked");
    }

    /**
     * Clicks on the notifications icon
     *
     * @throws IOException if element interaction fails
     */
    public void clickNotifications() throws IOException {
        log("Clicking notifications icon");
        element(NOTIFICATIONS_ICON).click();
        log("✓ Notifications icon clicked");
    }

    /**
     * Clicks on the settings link
     *
     * @throws IOException if element interaction fails
     */
    public void clickSettings() throws IOException {
        log("Clicking settings link");
        element(SETTINGS_LINK).click();
        log("✓ Settings link clicked");
    }

    /**
     * Clicks on the user avatar
     *
     * @throws IOException if element interaction fails
     */
    public void clickUserAvatar() throws IOException {
        log("Clicking user avatar");
        element(USER_AVATAR).click();
        log("✓ User avatar clicked");
    }

    /**
     * Verifies that the dashboard page is loaded and ready
     * Checks for presence of key dashboard elements
     *
     * @throws IOException if page verification fails
     */
    public void verifyPageLoaded() throws IOException {
        log("Verifying dashboard page loaded");
        try {
            assertElementVisible(WELCOME_MESSAGE);
            assertElementVisible(LOGOUT_BUTTON);
            assertElementVisible(DASHBOARD_TITLE);
            log("✓ Dashboard page loaded successfully");
        } catch (IOException e) {
            logError("✗ Dashboard page verification failed");
            throw new IOException("Dashboard page not loaded properly", e);
        }
    }

    /**
     * Verifies that a user is logged in
     * Checks for presence of user-specific elements
     *
     * @throws IOException if verification fails
     */
    public void verifyUserLoggedIn() throws IOException {
        log("Verifying user is logged in");
        try {
            // Wait for welcome message to appear
            waitForElementVisible(WELCOME_MESSAGE, 5000);

            // Verify user-specific elements are present
            assertElementVisible(USER_PROFILE);
            assertElementVisible(LOGOUT_BUTTON);

            log("✓ User is logged in successfully");
        } catch (IOException e) {
            logError("✗ User login verification failed");
            throw new IOException("User login verification failed", e);
        }
    }

    /**
     * Verifies the welcome message contains expected text
     *
     * @param expectedText Expected text in welcome message
     * @throws IOException if verification fails
     */
    public void verifyWelcomeMessage(String expectedText) throws IOException {
        log("Verifying welcome message contains: " + expectedText);
        String actualMessage = getWelcomeMessage();

        if (!actualMessage.contains(expectedText)) {
            logError("✗ Welcome message mismatch - Expected to contain: '" + expectedText + "', Got: '" + actualMessage + "'");
            throw new IOException("Welcome message mismatch: expected to contain '" + expectedText + "' but got '" + actualMessage + "'");
        }

        log("✓ Welcome message verified");
    }

    /**
     * Checks if user is on dashboard page
     *
     * @return true if on dashboard, false otherwise
     */
    public boolean isOnDashboard() {
        try {
            element(DASHBOARD_TITLE).getText();
            element(WELCOME_MESSAGE).getText();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Checks if navigation menu is displayed
     *
     * @return true if navigation menu is visible, false otherwise
     */
    public boolean isNavigationMenuDisplayed() {
        try {
            element(NAVIGATION_MENU).getText();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Checks if logout button is displayed
     *
     * @return true if logout button is visible, false otherwise
     */
    public boolean isLogoutButtonDisplayed() {
        try {
            element(LOGOUT_BUTTON).getText();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Takes a screenshot of the dashboard page
     *
     * @param screenshotName Name for the screenshot
     * @throws IOException if screenshot capture fails
     */
    public void captureDashboardScreenshot(String screenshotName) throws IOException {
        log("Capturing dashboard screenshot: " + screenshotName);
        takeScreenshot("dashboard_" + screenshotName);
    }

    /**
     * Performs complete logout flow
     * Clicks logout and waits for redirect
     *
     * @throws IOException if logout fails
     */
    public void performLogout() throws IOException {
        log("Performing logout");
        clickLogout();

        // Wait a bit for redirect
        sleep(1000);

        log("✓ Logout completed");
    }
}
