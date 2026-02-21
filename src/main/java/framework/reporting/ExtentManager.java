package framework.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * ExtentManager - Manages Extent Reports lifecycle and configuration
 *
 * This class provides centralized management of Extent Reports including:
 * - Report initialization and configuration
 * - Test management (create, update, finish)
 * - Screenshot attachment
 * - Log management
 * - Report finalization
 *
 * Features:
 * - Singleton pattern for thread-safe report management
 * - HTML reporting with Spark reporter
 * - Test categorization and tagging
 * - Screenshot embedding
 * - System information capture
 * - Custom branding and theming
 *
 * Usage:
 * <pre>
 * ExtentManager.getInstance().createTest("Test Name", "Test Description");
 * ExtentManager.getInstance().logInfo("Test step");
 * ExtentManager.getInstance().logPass("Test passed");
 * ExtentManager.getInstance().attachScreenshot("/path/to/screenshot.png");
 * ExtentManager.getInstance().flush();
 * </pre>
 *
 * Thread Safety:
 * - Uses ThreadLocal for test management in parallel execution
 * - Synchronized methods for report initialization
 *
 * @author Selenium-MCP Framework
 * @version 1.0.0
 * @see ExtentReports
 * @see ExtentSparkReporter
 */
public class ExtentManager {

    /** Singleton instance */
    private static ExtentManager instance;

    /** Extent Reports instance */
    private static ExtentReports extent;

    /** Thread-local test instances for parallel execution */
    private static ThreadLocal<ExtentTest> test = new ThreadLocal<>();

    /** Test instances map for tracking */
    private static Map<String, ExtentTest> testMap = new HashMap<>();

    /** Report file path */
    private static String reportPath;

    /** Screenshots directory */
    private static String screenshotsPath;

    /**
     * Private constructor to enforce singleton pattern
     */
    private ExtentManager() {
        // Private constructor
    }

    /**
     * Gets the singleton instance of ExtentManager
     *
     * @return ExtentManager instance
     */
    public static synchronized ExtentManager getInstance() {
        if (instance == null) {
            instance = new ExtentManager();
        }
        return instance;
    }

    /**
     * Initializes the Extent Reports instance
     * Creates report with timestamp and configures Spark reporter
     *
     * @return ExtentReports instance
     */
    public static synchronized ExtentReports getReporter() {
        if (extent == null) {
            // Create reports directory if it doesn't exist
            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            String reportDir = "test-output/extent-reports";
            new File(reportDir).mkdirs();

            reportPath = reportDir + "/ExtentReport_" + timestamp + ".html";
            screenshotsPath = reportDir + "/screenshots";
            new File(screenshotsPath).mkdirs();

            // Initialize Extent Spark Reporter
            ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);

            // Configure Spark Reporter
            sparkReporter.config().setDocumentTitle("Selenium-MCP Test Automation Report");
            sparkReporter.config().setReportName("Test Execution Results");
            sparkReporter.config().setTheme(Theme.STANDARD);
            sparkReporter.config().setTimeStampFormat("MMM dd, yyyy HH:mm:ss");
            sparkReporter.config().setEncoding("UTF-8");

            // CSS customization
            sparkReporter.config().setCss(
                ".r-img { width: 30%; } " +
                "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }"
            );

            // JavaScript customization
            sparkReporter.config().setJs(
                "document.title = 'Selenium-MCP Test Report';"
            );

            // Initialize ExtentReports
            extent = new ExtentReports();
            extent.attachReporter(sparkReporter);

            // Set system information
            extent.setSystemInfo("Framework", "Selenium-MCP Framework");
            extent.setSystemInfo("Test Type", "Automated Testing");
            extent.setSystemInfo("Environment", System.getProperty("test.environment", "QA"));
            extent.setSystemInfo("Browser", System.getProperty("browser", "Chrome"));
            extent.setSystemInfo("OS", System.getProperty("os.name"));
            extent.setSystemInfo("Java Version", System.getProperty("java.version"));
            extent.setSystemInfo("User", System.getProperty("user.name"));
            extent.setSystemInfo("Execution Time", new SimpleDateFormat("MMM dd, yyyy HH:mm:ss").format(new Date()));

            System.out.println("✓ Extent Reports initialized: " + reportPath);
        }
        return extent;
    }

    /**
     * Creates a new test in the report
     *
     * @param testName Test name
     * @param description Test description
     * @return ExtentTest instance
     */
    public ExtentTest createTest(String testName, String description) {
        ExtentTest extentTest = getReporter().createTest(testName, description);
        test.set(extentTest);
        testMap.put(testName, extentTest);
        return extentTest;
    }

    /**
     * Creates a new test in the report without description
     *
     * @param testName Test name
     * @return ExtentTest instance
     */
    public ExtentTest createTest(String testName) {
        return createTest(testName, "");
    }

    /**
     * Gets the current test instance from ThreadLocal
     *
     * @return ExtentTest instance
     */
    public ExtentTest getTest() {
        return test.get();
    }

    /**
     * Gets a test instance by name
     *
     * @param testName Test name
     * @return ExtentTest instance
     */
    public ExtentTest getTest(String testName) {
        return testMap.get(testName);
    }

    /**
     * Assigns category to current test
     *
     * @param category Category name
     */
    public void assignCategory(String category) {
        if (test.get() != null) {
            test.get().assignCategory(category);
        }
    }

    /**
     * Assigns author to current test
     *
     * @param author Author name
     */
    public void assignAuthor(String author) {
        if (test.get() != null) {
            test.get().assignAuthor(author);
        }
    }

    /**
     * Assigns device to current test
     *
     * @param device Device name
     */
    public void assignDevice(String device) {
        if (test.get() != null) {
            test.get().assignDevice(device);
        }
    }

    /**
     * Logs a PASS status with message
     *
     * @param message Log message
     */
    public void logPass(String message) {
        if (test.get() != null) {
            test.get().log(Status.PASS, MarkupHelper.createLabel(message, ExtentColor.GREEN));
        }
    }

    /**
     * Logs a FAIL status with message
     *
     * @param message Log message
     */
    public void logFail(String message) {
        if (test.get() != null) {
            test.get().log(Status.FAIL, MarkupHelper.createLabel(message, ExtentColor.RED));
        }
    }

    /**
     * Logs a FAIL status with throwable
     *
     * @param throwable Exception/Error
     */
    public void logFail(Throwable throwable) {
        if (test.get() != null) {
            test.get().log(Status.FAIL, throwable);
        }
    }

    /**
     * Logs a SKIP status with message
     *
     * @param message Log message
     */
    public void logSkip(String message) {
        if (test.get() != null) {
            test.get().log(Status.SKIP, MarkupHelper.createLabel(message, ExtentColor.YELLOW));
        }
    }

    /**
     * Logs an INFO status with message
     *
     * @param message Log message
     */
    public void logInfo(String message) {
        if (test.get() != null) {
            test.get().log(Status.INFO, message);
        }
    }

    /**
     * Logs a WARNING status with message
     *
     * @param message Log message
     */
    public void logWarning(String message) {
        if (test.get() != null) {
            test.get().log(Status.WARNING, MarkupHelper.createLabel(message, ExtentColor.ORANGE));
        }
    }

    /**
     * Attaches a screenshot to the current test
     *
     * @param screenshotPath Path to screenshot file
     */
    public void attachScreenshot(String screenshotPath) {
        if (test.get() != null) {
            try {
                test.get().addScreenCaptureFromPath(screenshotPath);
            } catch (Exception e) {
                logWarning("Failed to attach screenshot: " + e.getMessage());
            }
        }
    }

    /**
     * Attaches a screenshot to the current test with custom title
     *
     * @param screenshotPath Path to screenshot file
     * @param title Screenshot title
     */
    public void attachScreenshot(String screenshotPath, String title) {
        if (test.get() != null) {
            try {
                test.get().addScreenCaptureFromPath(screenshotPath, title);
            } catch (Exception e) {
                logWarning("Failed to attach screenshot: " + e.getMessage());
            }
        }
    }

    /**
     * Creates a child test node under current test
     *
     * @param nodeName Node name
     * @return ExtentTest child instance
     */
    public ExtentTest createNode(String nodeName) {
        if (test.get() != null) {
            return test.get().createNode(nodeName);
        }
        return null;
    }

    /**
     * Creates a child test node under current test with description
     *
     * @param nodeName Node name
     * @param description Node description
     * @return ExtentTest child instance
     */
    public ExtentTest createNode(String nodeName, String description) {
        if (test.get() != null) {
            return test.get().createNode(nodeName, description);
        }
        return null;
    }

    /**
     * Flushes the report and writes to file
     * Should be called at the end of test execution
     */
    public void flush() {
        if (extent != null) {
            extent.flush();
            System.out.println("✓ Extent Reports flushed to: " + reportPath);
        }
    }

    /**
     * Removes the current test from ThreadLocal
     * Should be called after test completion
     */
    public void removeTest() {
        test.remove();
    }

    /**
     * Gets the report file path
     *
     * @return Report file path
     */
    public String getReportPath() {
        return reportPath;
    }

    /**
     * Gets the screenshots directory path
     *
     * @return Screenshots directory path
     */
    public String getScreenshotsPath() {
        return screenshotsPath;
    }

    /**
     * Creates a formatted log entry with details
     *
     * @param details Map of detail key-value pairs
     */
    public void logDetails(Map<String, String> details) {
        if (test.get() != null && details != null && !details.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("<table class='table table-sm'>");
            sb.append("<thead><tr><th>Property</th><th>Value</th></tr></thead>");
            sb.append("<tbody>");
            for (Map.Entry<String, String> entry : details.entrySet()) {
                sb.append("<tr><td>").append(entry.getKey()).append("</td>");
                sb.append("<td>").append(entry.getValue()).append("</td></tr>");
            }
            sb.append("</tbody></table>");
            test.get().info(sb.toString());
        }
    }

    /**
     * Sets test status based on boolean result
     *
     * @param passed Whether test passed
     * @param passMessage Message for pass status
     * @param failMessage Message for fail status
     */
    public void setTestStatus(boolean passed, String passMessage, String failMessage) {
        if (passed) {
            logPass(passMessage);
        } else {
            logFail(failMessage);
        }
    }
}
