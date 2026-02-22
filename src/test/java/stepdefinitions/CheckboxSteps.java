package stepdefinitions;

import framework.healing.HealingMetrics;
import framework.locators.LocatorRegistry;
import framework.mcp.MCPSeleniumClient;
import framework.pages.CheckboxPage;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * CheckboxSteps - Cucumber step definitions for checkbox feature
 *
 * Auto-generated for https://the-internet.herokuapp.com/checkboxes
 * Uses tagged hooks (@checkbox) to isolate from other features.
 */
public class CheckboxSteps {

    private MCPSeleniumClient mcp;
    private LocatorRegistry locatorRegistry;
    private HealingMetrics healingMetrics;
    private CheckboxPage checkboxPage;

    @Before("@checkbox")
    public void setUp() throws IOException {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║         STARTING CHECKBOX TEST SCENARIO                       ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");

        mcp = new MCPSeleniumClient();
        mcp.startMCPServer();
        mcp.startBrowser("chrome", false);

        locatorRegistry = new LocatorRegistry();
        try {
            locatorRegistry.loadRegistry();
        } catch (IOException e) {
            System.out.println("Could not load existing registry, will create new one");
        }

        healingMetrics = new HealingMetrics();
        checkboxPage = new CheckboxPage(mcp, locatorRegistry, healingMetrics);
        checkboxPage.initializeLocators();

        System.out.println("Setup complete\n");
    }

    @After("@checkbox")
    public void tearDown() {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║         CLEANING UP CHECKBOX TEST SCENARIO                    ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");

        if (healingMetrics != null) {
            healingMetrics.printHealingReport();
        }
        if (locatorRegistry != null) {
            try { locatorRegistry.saveRegistry(); } catch (IOException e) {
                System.err.println("Could not save registry: " + e.getMessage());
            }
        }
        if (mcp != null) {
            try { mcp.closeBrowser(); } catch (Exception e) {
                System.err.println("Could not close browser: " + e.getMessage());
            }
            try { mcp.stopMCPServer(); } catch (Exception e) {
                System.err.println("Could not stop MCP server: " + e.getMessage());
            }
        }
        System.out.println("Cleanup complete\n");
    }

    @Given("user navigates to checkboxes page")
    public void userNavigatesToCheckboxesPage() throws IOException {
        checkboxPage.navigateToCheckboxPage();
    }

    @Then("checkboxes page should be displayed")
    public void checkboxesPageShouldBeDisplayed() throws IOException {
        checkboxPage.verifyPageLoaded();
    }

    @Then("page heading should display {string}")
    public void pageHeadingShouldDisplay(String expectedText) throws IOException {
        String actual = checkboxPage.getPageHeading();
        assertTrue("Expected heading '" + expectedText + "' but got '" + actual + "'",
                actual.contains(expectedText));
    }

    @When("user clicks on checkbox {int}")
    public void userClicksOnCheckbox(int checkboxNumber) throws IOException {
        if (checkboxNumber == 1) {
            checkboxPage.clickCheckbox1();
        } else if (checkboxNumber == 2) {
            checkboxPage.clickCheckbox2();
        }
    }

    @Then("checkbox {int} should be present")
    public void checkboxShouldBePresent(int checkboxNumber) throws IOException {
        String elementName = checkboxNumber == 1 ? CheckboxPage.CHECKBOX_1 : CheckboxPage.CHECKBOX_2;
        checkboxPage.assertElementPresent(elementName);
    }
}
