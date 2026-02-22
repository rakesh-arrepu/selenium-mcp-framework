package stepdefinitions;

import framework.healing.HealingMetrics;
import framework.locators.LocatorRegistry;
import framework.mcp.MCPSeleniumClient;
import framework.pages.AddRemoveElementsPage;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * AddRemoveElementsSteps - Cucumber step definitions for add/remove elements feature
 *
 * Uses tagged hooks (@add_remove) to isolate from other features.
 */
public class AddRemoveElementsSteps {

    private MCPSeleniumClient mcp;
    private LocatorRegistry locatorRegistry;
    private HealingMetrics healingMetrics;
    private AddRemoveElementsPage addRemovePage;

    @Before("@add_remove")
    public void setUp() throws IOException {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║      STARTING ADD/REMOVE ELEMENTS TEST SCENARIO               ║");
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
        addRemovePage = new AddRemoveElementsPage(mcp, locatorRegistry, healingMetrics);
        addRemovePage.initializeLocators();

        System.out.println("Setup complete\n");
    }

    @After("@add_remove")
    public void tearDown() {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║      CLEANING UP ADD/REMOVE ELEMENTS TEST SCENARIO            ║");
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

    // ── Navigation ──────────────────────────────────────────────────

    @Given("user navigates to add remove elements page")
    public void userNavigatesToAddRemoveElementsPage() throws IOException {
        addRemovePage.navigateToAddRemovePage();
    }

    @Then("add remove elements page should be displayed")
    public void addRemoveElementsPageShouldBeDisplayed() throws IOException {
        addRemovePage.verifyPageLoaded();
    }

    @Then("the page heading should be {string}")
    public void thePageHeadingShouldBe(String expectedHeading) throws IOException {
        String actual = addRemovePage.getPageHeading();
        assertTrue("Expected heading '" + expectedHeading + "' but got '" + actual + "'",
                actual.contains(expectedHeading));
    }

    // ── Add Element ─────────────────────────────────────────────────

    @When("user clicks Add Element button")
    public void userClicksAddElementButton() throws IOException {
        addRemovePage.clickAddElement();
    }

    @When("user clicks Add Element button {int} times")
    public void userClicksAddElementButtonMultipleTimes(int times) throws IOException {
        for (int i = 0; i < times; i++) {
            addRemovePage.clickAddElement();
        }
    }

    // ── Delete Element ──────────────────────────────────────────────

    @When("user clicks delete button at position {int}")
    public void userClicksDeleteButtonAtPosition(int position) throws IOException {
        addRemovePage.clickDeleteButton(position);
    }

    // ── Verification ────────────────────────────────────────────────

    @Then("there should be {int} delete button(s) on the page")
    public void thereShouldBeDeleteButtonsOnThePage(int expectedCount) throws IOException {
        addRemovePage.verifyDeleteButtonCount(expectedCount);
    }
}
