package framework.pages;

import framework.healing.HealingMetrics;
import framework.locators.LocatorRegistry;
import framework.locators.LocatorRegistry.LocatorStrategy;
import framework.mcp.MCPSeleniumClient;

import java.io.IOException;

/**
 * AddRemoveElementsPage - Page object for the-internet.herokuapp.com/add_remove_elements/
 *
 * Handles the Add/Remove Elements page where clicking "Add Element" creates
 * Delete buttons and clicking "Delete" removes them. Uses direct MCP calls
 * for dynamic delete buttons (positions shift on add/remove) and self-healing
 * locators for static elements (Add button, heading, homepage link).
 */
public class AddRemoveElementsPage extends BasePage {

    public static final String HOMEPAGE_ADD_REMOVE_LINK = "homepageAddRemoveLink";
    public static final String ADD_BUTTON = "addElementButton";
    public static final String PAGE_HEADING = "addRemovePageHeading";

    public AddRemoveElementsPage(MCPSeleniumClient mcp, LocatorRegistry locatorRegistry, HealingMetrics healingMetrics) {
        super(mcp, locatorRegistry, healingMetrics);
    }

    public void initializeLocators() {
        log("Initializing locators for AddRemoveElementsPage");

        locatorRegistry.addElement(HOMEPAGE_ADD_REMOVE_LINK,
                new LocatorStrategy("css", "a[href='/add_remove_elements/']", 1),
                new LocatorStrategy("xpath", "//a[text()='Add/Remove Elements']", 2),
                new LocatorStrategy("xpath", "//a[contains(@href,'add_remove_elements')]", 3)
        );

        locatorRegistry.addElement(ADD_BUTTON,
                new LocatorStrategy("xpath", "//button[text()='Add Element']", 1),
                new LocatorStrategy("css", "div.example > button", 2),
                new LocatorStrategy("xpath", "//div[@class='example']/button[text()='Add Element']", 3)
        );

        locatorRegistry.addElement(PAGE_HEADING,
                new LocatorStrategy("css", "div.example h3", 1),
                new LocatorStrategy("xpath", "//h3[text()='Add/Remove Elements']", 2),
                new LocatorStrategy("xpath", "//div[@class='example']/h3", 3)
        );

        log("Locators initialized successfully");
    }

    public void navigateToAddRemovePage() throws IOException {
        log("Navigating to Add/Remove Elements page via homepage");
        navigateToHomePage();
        element(HOMEPAGE_ADD_REMOVE_LINK).click();
        log("Reached Add/Remove Elements page");
    }

    public void clickAddElement() throws IOException {
        element(ADD_BUTTON).click();
        log("Clicked Add Element button");
    }

    /**
     * Clicks the delete button at the given position (1-based).
     * Uses direct MCP calls since delete buttons are dynamic.
     */
    public void clickDeleteButton(int position) throws IOException {
        mcp.clickElement("xpath", "//div[@id='elements']/button[" + position + "]");
        log("Clicked Delete button at position " + position);
    }

    /**
     * Verifies the exact number of delete buttons on the page.
     * Checks that button[expectedCount] exists (if > 0) and button[expectedCount+1] does not.
     */
    public void verifyDeleteButtonCount(int expectedCount) throws IOException {
        if (expectedCount > 0) {
            mcp.findElement("xpath",
                    "//div[@id='elements']/button[" + expectedCount + "]", 5000);
        }

        try {
            mcp.findElement("xpath",
                    "//div[@id='elements']/button[" + (expectedCount + 1) + "]", 2000);
            throw new AssertionError(
                    "Expected " + expectedCount + " delete button(s) but found more");
        } catch (IOException e) {
            // Expected — no button beyond the expected count
        }

        log("Verified " + expectedCount + " delete button(s) on the page");
    }

    public String getPageHeading() throws IOException {
        return element(PAGE_HEADING).getText();
    }

    public void verifyPageLoaded() throws IOException {
        log("Verifying Add/Remove Elements page loaded");
        assertElementVisible(PAGE_HEADING);
        assertElementVisible(ADD_BUTTON);
        log("Add/Remove Elements page loaded successfully");
    }
}
