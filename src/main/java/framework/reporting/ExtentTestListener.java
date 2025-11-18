package framework.reporting;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.Markup;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.*;

import java.util.HashMap;
import java.util.Map;

/**
 * ExtentTestListener - Cucumber plugin for Extent Reports integration
 *
 * This listener automatically integrates Extent Reports with Cucumber test execution.
 * It hooks into Cucumber's event system to:
 * - Create test entries for each scenario
 * - Log test steps with status
 * - Capture step failures and skips
 * - Add tags and metadata
 * - Generate comprehensive HTML reports
 *
 * Features:
 * - Automatic test creation from Cucumber scenarios
 * - Step-level logging with status (Pass/Fail/Skip)
 * - Tag-based categorization
 * - Error stack trace capture
 * - Thread-safe parallel execution support
 *
 * Usage:
 * Add to cucumber.properties:
 * <pre>
 * cucumber.plugin=framework.reporting.ExtentTestListener
 * </pre>
 *
 * Or in @CucumberOptions:
 * <pre>
 * @CucumberOptions(
 *     plugin = {"framework.reporting.ExtentTestListener"}
 * )
 * </pre>
 *
 * Thread Safety:
 * - Implements ConcurrentEventListener for parallel execution
 * - Uses ThreadLocal for scenario management
 *
 * @author Selenium-MCP Framework
 * @version 1.0.0
 * @see ConcurrentEventListener
 * @see ExtentManager
 */
public class ExtentTestListener implements ConcurrentEventListener {

    /** Thread-local storage for current scenario test */
    private static ThreadLocal<ExtentTest> scenarioTest = new ThreadLocal<>();

    /** Thread-local storage for current step test */
    private static ThreadLocal<ExtentTest> stepTest = new ThreadLocal<>();

    /** Map to store scenario tests by thread ID */
    private static Map<Long, ExtentTest> scenarioMap = new HashMap<>();

    /**
     * Sets the event publisher for registering event handlers
     *
     * @param publisher Event publisher
     */
    @Override
    public void setEventPublisher(EventPublisher publisher) {
        // Register event handlers
        publisher.registerHandlerFor(TestRunStarted.class, this::handleTestRunStarted);
        publisher.registerHandlerFor(TestRunFinished.class, this::handleTestRunFinished);
        publisher.registerHandlerFor(TestCaseStarted.class, this::handleTestCaseStarted);
        publisher.registerHandlerFor(TestCaseFinished.class, this::handleTestCaseFinished);
        publisher.registerHandlerFor(TestStepStarted.class, this::handleTestStepStarted);
        publisher.registerHandlerFor(TestStepFinished.class, this::handleTestStepFinished);
    }

    /**
     * Handles test run started event
     * Initializes Extent Reports
     *
     * @param event TestRunStarted event
     */
    private void handleTestRunStarted(TestRunStarted event) {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║           EXTENT REPORTS - TEST RUN STARTED                   ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");

        // Initialize Extent Reports
        ExtentManager.getReporter();
    }

    /**
     * Handles test run finished event
     * Flushes Extent Reports to file
     *
     * @param event TestRunFinished event
     */
    private void handleTestRunFinished(TestRunFinished event) {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║           EXTENT REPORTS - TEST RUN FINISHED                  ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");

        // Flush reports
        ExtentManager.getInstance().flush();

        // Cleanup
        scenarioTest.remove();
        stepTest.remove();
    }

    /**
     * Handles test case started event
     * Creates a new test in Extent Reports
     *
     * @param event TestCaseStarted event
     */
    private void handleTestCaseStarted(TestCaseStarted event) {
        TestCase testCase = event.getTestCase();
        String scenarioName = testCase.getName();
        String featureName = testCase.getUri().toString();

        // Extract feature file name
        if (featureName.contains("/")) {
            featureName = featureName.substring(featureName.lastIndexOf("/") + 1);
        }
        if (featureName.endsWith(".feature")) {
            featureName = featureName.substring(0, featureName.length() - 8);
        }

        // Create test with feature name as description
        ExtentTest test = ExtentManager.getInstance().createTest(scenarioName, "Feature: " + featureName);
        scenarioTest.set(test);
        scenarioMap.put(Thread.currentThread().getId(), test);

        // Add tags as categories
        for (String tag : testCase.getTags()) {
            test.assignCategory(tag.replace("@", ""));
        }

        // Log scenario start
        test.info("Scenario started: " + scenarioName);
        test.info("Feature file: " + featureName);
    }

    /**
     * Handles test case finished event
     * Sets final status of test
     *
     * @param event TestCaseFinished event
     */
    private void handleTestCaseFinished(TestCaseFinished event) {
        ExtentTest test = scenarioTest.get();
        if (test == null) {
            return;
        }

        Status status = mapStatus(event.getResult().getStatus());
        String duration = formatDuration(event.getResult().getDuration().toMillis());

        // Log test completion
        if (status == Status.PASS) {
            Markup markup = MarkupHelper.createLabel(
                "Scenario PASSED - Duration: " + duration,
                ExtentColor.GREEN
            );
            test.log(status, markup);
        } else if (status == Status.FAIL) {
            Throwable error = event.getResult().getError();
            if (error != null) {
                test.log(status, error);
            }
            Markup markup = MarkupHelper.createLabel(
                "Scenario FAILED - Duration: " + duration,
                ExtentColor.RED
            );
            test.log(status, markup);
        } else if (status == Status.SKIP) {
            Markup markup = MarkupHelper.createLabel(
                "Scenario SKIPPED - Duration: " + duration,
                ExtentColor.YELLOW
            );
            test.log(status, markup);
        }

        test.info("Total duration: " + duration);

        // Cleanup
        scenarioMap.remove(Thread.currentThread().getId());
    }

    /**
     * Handles test step started event
     * Logs step start in report
     *
     * @param event TestStepStarted event
     */
    private void handleTestStepStarted(TestStepStarted event) {
        ExtentTest test = scenarioTest.get();
        if (test == null) {
            return;
        }

        TestStep testStep = event.getTestStep();
        if (testStep instanceof PickleStepTestStep) {
            PickleStepTestStep pickleStep = (PickleStepTestStep) testStep;
            String stepText = pickleStep.getStep().getText();
            String keyword = pickleStep.getStep().getKeyword();

            // Create a node for the step
            ExtentTest stepNode = test.createNode(keyword + stepText);
            stepTest.set(stepNode);
        }
    }

    /**
     * Handles test step finished event
     * Logs step status and details
     *
     * @param event TestStepFinished event
     */
    private void handleTestStepFinished(TestStepFinished event) {
        ExtentTest step = stepTest.get();
        if (step == null) {
            return;
        }

        TestStep testStep = event.getTestStep();
        if (testStep instanceof PickleStepTestStep) {
            Status status = mapStatus(event.getResult().getStatus());
            String duration = formatDuration(event.getResult().getDuration().toMillis());

            // Log step status
            if (status == Status.PASS) {
                step.log(status, MarkupHelper.createLabel("Step PASSED - " + duration, ExtentColor.GREEN));
            } else if (status == Status.FAIL) {
                Throwable error = event.getResult().getError();
                if (error != null) {
                    step.log(status, error);
                } else {
                    step.log(status, MarkupHelper.createLabel("Step FAILED - " + duration, ExtentColor.RED));
                }
            } else if (status == Status.SKIP) {
                step.log(status, MarkupHelper.createLabel("Step SKIPPED - " + duration, ExtentColor.YELLOW));
            } else {
                step.log(Status.INFO, "Step completed - " + duration);
            }
        }

        // Cleanup
        stepTest.remove();
    }

    /**
     * Maps Cucumber status to Extent Reports status
     *
     * @param cucumberStatus Cucumber status
     * @return Extent Reports status
     */
    private Status mapStatus(io.cucumber.plugin.event.Status cucumberStatus) {
        switch (cucumberStatus) {
            case PASSED:
                return Status.PASS;
            case FAILED:
                return Status.FAIL;
            case SKIPPED:
                return Status.SKIP;
            case PENDING:
                return Status.WARNING;
            case AMBIGUOUS:
                return Status.WARNING;
            case UNDEFINED:
                return Status.WARNING;
            default:
                return Status.INFO;
        }
    }

    /**
     * Formats duration in milliseconds to human-readable format
     *
     * @param durationMs Duration in milliseconds
     * @return Formatted duration string
     */
    private String formatDuration(long durationMs) {
        if (durationMs < 1000) {
            return durationMs + "ms";
        } else if (durationMs < 60000) {
            return String.format("%.2fs", durationMs / 1000.0);
        } else {
            long minutes = durationMs / 60000;
            long seconds = (durationMs % 60000) / 1000;
            return minutes + "m " + seconds + "s";
        }
    }

    /**
     * Gets the current scenario test from ThreadLocal
     *
     * @return Current scenario ExtentTest
     */
    public static ExtentTest getCurrentScenario() {
        return scenarioTest.get();
    }

    /**
     * Gets the current step test from ThreadLocal
     *
     * @return Current step ExtentTest
     */
    public static ExtentTest getCurrentStep() {
        return stepTest.get();
    }
}
