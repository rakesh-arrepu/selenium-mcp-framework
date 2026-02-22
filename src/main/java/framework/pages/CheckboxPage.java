package framework.pages;

import framework.healing.HealingMetrics;
import framework.locators.LocatorRegistry;
import framework.locators.LocatorRegistry.LocatorStrategy;
import framework.mcp.MCPSeleniumClient;

import java.io.IOException;

/**
 * CheckboxPage - Page object for the-internet.herokuapp.com/checkboxes
 *
 * Demonstrates self-healing locators for elements WITHOUT IDs —
 * uses CSS nth-of-type, XPath position, and form-scoped selectors as fallbacks.
 */
public class CheckboxPage extends BasePage {

    public static final String PAGE_HEADING = "pageHeading";
    public static final String CHECKBOX_1 = "checkbox1";
    public static final String CHECKBOX_2 = "checkbox2";
    public static final String HOMEPAGE_CHECKBOX_LINK = "homepageCheckboxLink";

    public CheckboxPage(MCPSeleniumClient mcp, LocatorRegistry locatorRegistry, HealingMetrics healingMetrics) {
        super(mcp, locatorRegistry, healingMetrics);
    }

    /**
     * Initializes locators for the checkboxes page.
     * Neither checkbox has an ID, so we use positional CSS/XPath strategies.
     */
    public void initializeLocators() {
        log("Initializing locators for CheckboxPage");

        locatorRegistry.addElement(PAGE_HEADING,
                new LocatorStrategy("css", "h3", 1),
                new LocatorStrategy("xpath", "//h3", 2),
                new LocatorStrategy("css", ".example h3", 3)
        );

        // Checkbox 1 — no ID, first input inside form#checkboxes
        locatorRegistry.addElement(CHECKBOX_1,
                new LocatorStrategy("css", "#checkboxes input:first-of-type", 1),
                new LocatorStrategy("xpath", "(//form[@id='checkboxes']/input)[1]", 2),
                new LocatorStrategy("css", "form#checkboxes input:nth-of-type(1)", 3),
                new LocatorStrategy("xpath", "(//input[@type='checkbox'])[1]", 4)
        );

        // Checkbox 2 — no ID, second input inside form#checkboxes
        locatorRegistry.addElement(CHECKBOX_2,
                new LocatorStrategy("css", "#checkboxes input:last-of-type", 1),
                new LocatorStrategy("xpath", "(//form[@id='checkboxes']/input)[2]", 2),
                new LocatorStrategy("css", "form#checkboxes input:nth-of-type(2)", 3),
                new LocatorStrategy("xpath", "(//input[@type='checkbox'])[2]", 4)
        );

        // Homepage link to reach checkboxes page
        locatorRegistry.addElement(HOMEPAGE_CHECKBOX_LINK,
                new LocatorStrategy("css", "a[href='/checkboxes']", 1),
                new LocatorStrategy("xpath", "//a[text()='Checkboxes']", 2),
                new LocatorStrategy("xpath", "//a[contains(@href,'checkboxes')]", 3)
        );

        log("Locators initialized successfully");
    }

    public void navigateToCheckboxPage() throws IOException {
        log("Navigating to checkboxes page via homepage");
        navigateToHomePage();
        element(HOMEPAGE_CHECKBOX_LINK).click();
        log("✓ Reached checkboxes page via homepage link");
    }

    public String getPageHeading() throws IOException {
        return element(PAGE_HEADING).getText();
    }

    public void clickCheckbox1() throws IOException {
        log("Clicking checkbox 1");
        element(CHECKBOX_1).click();
    }

    public void clickCheckbox2() throws IOException {
        log("Clicking checkbox 2");
        element(CHECKBOX_2).click();
    }

    /**
     * Asserts that a named element is present and findable on the page.
     */
    public void assertElementPresent(String elementName) throws IOException {
        assertElementVisible(elementName);
    }

    public void verifyPageLoaded() throws IOException {
        log("Verifying checkboxes page loaded");
        assertElementVisible(PAGE_HEADING);
        assertElementVisible(CHECKBOX_1);
        assertElementVisible(CHECKBOX_2);
        log("Checkboxes page loaded successfully");
    }
}
