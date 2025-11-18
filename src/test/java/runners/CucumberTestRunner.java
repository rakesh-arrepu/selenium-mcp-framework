package runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 * CucumberTestRunner - Main test runner for executing Cucumber BDD tests
 *
 * This class configures and runs all Cucumber feature files with specified options.
 * It generates multiple report formats and supports tag-based test execution.
 *
 * Key Features:
 * - Executes all features in src/test/resources/features
 * - Generates HTML, JSON, and pretty console reports
 * - Supports tag-based filtering (@smoke, @regression, etc.)
 * - Pretty formatting for console output
 * - Strict mode to fail on undefined steps
 *
 * Usage:
 * Run all tests:
 *   mvn test
 *
 * Run specific tags:
 *   mvn test -Dcucumber.filter.tags="@smoke"
 *   mvn test -Dcucumber.filter.tags="@login and not @skip"
 *
 * Run specific feature:
 *   mvn test -Dcucumber.features="src/test/resources/features/login.feature"
 *
 * @author Selenium-MCP Framework
 * @version 1.0.0
 */
@RunWith(Cucumber.class)
@CucumberOptions(
        // Feature files location
        features = "src/test/resources/features",

        // Step definitions package
        glue = {"stepdefinitions"},

        // Report plugins
        plugin = {
                "pretty",                                          // Pretty console output
                "html:target/cucumber-reports/cucumber.html",     // HTML report
                "json:target/cucumber-reports/cucumber.json",     // JSON report (for advanced reporting)
                "junit:target/cucumber-reports/cucumber.xml",     // JUnit XML report (for CI/CD)
                "rerun:target/cucumber-reports/rerun.txt",        // Rerun failed scenarios
                "framework.reporting.ExtentTestListener",          // Extent Reports integration
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm" // Allure Reports integration
        },

        // Display more detailed output
        monochrome = true,

        // Fail if there are undefined or pending steps
        strict = true,

        // Dry run - check if all steps have definitions (useful for validation)
        dryRun = false,

        // Tag filters - can be overridden via command line
        // tags = "@smoke"
        tags = ""
)
public class CucumberTestRunner {
    // This class will be empty - configuration is done via annotations
    // JUnit will use the @RunWith annotation to execute Cucumber tests
}
