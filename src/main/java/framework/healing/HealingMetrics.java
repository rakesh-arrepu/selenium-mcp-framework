package framework.healing;

import framework.locators.LocatorRegistry.LocatorStrategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * HealingMetrics - Track and analyze self-healing performance
 *
 * This class collects comprehensive metrics about element healing attempts, enabling
 * identification of brittle elements, reliable locators, and overall framework health.
 *
 * Key Features:
 * - Per-element success/failure tracking
 * - Per-locator performance statistics
 * - Brittle element detection (elements with low success rates)
 * - Robust element identification (elements with 100% success)
 * - Comprehensive reporting and data export
 * - Failure history for debugging
 *
 * Example Usage:
 * <pre>
 * HealingMetrics metrics = new HealingMetrics();
 * metrics.recordHealingSuccess("loginButton", strategy);
 * metrics.recordHealingFailure("loginButton", attemptedStrategies);
 * metrics.printHealingReport();
 * List&lt;String&gt; brittleElements = metrics.getBrittleElements(80.0);
 * </pre>
 *
 * @author Selenium-MCP Framework
 * @version 1.0.0
 */
public class HealingMetrics {

    private final Map<String, ElementHealingStats> elementStats;
    private static final int MAX_FAILURE_HISTORY = 3;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Performance statistics for a specific locator strategy
     */
    public static class LocatorPerformance {
        private final String strategy;
        private final String value;
        private int attempts;
        private int successes;
        private int failures;

        /**
         * Creates a new locator performance tracker
         *
         * @param strategy Locator type (id, css, xpath, etc.)
         * @param value Locator value (the actual selector)
         */
        public LocatorPerformance(String strategy, String value) {
            this.strategy = strategy;
            this.value = value;
            this.attempts = 0;
            this.successes = 0;
            this.failures = 0;
        }

        /**
         * Records a successful attempt
         */
        public void recordSuccess() {
            attempts++;
            successes++;
        }

        /**
         * Records a failed attempt
         */
        public void recordFailure() {
            attempts++;
            failures++;
        }

        /**
         * Gets success rate as percentage (0-100)
         *
         * @return Success rate percentage
         */
        public double getSuccessRate() {
            if (attempts == 0) return 0.0;
            return (double) successes / attempts * 100.0;
        }

        public String getStrategy() {
            return strategy;
        }

        public String getValue() {
            return value;
        }

        public int getAttempts() {
            return attempts;
        }

        public int getSuccesses() {
            return successes;
        }

        public int getFailures() {
            return failures;
        }

        @Override
        public String toString() {
            return String.format("%s=%s (%.1f%% - %d/%d)",
                    strategy, value, getSuccessRate(), successes, attempts);
        }
    }

    /**
     * Comprehensive healing statistics for a single element
     */
    public static class ElementHealingStats {
        private final String elementName;
        private int totalAttempts;
        private int successfulHeals;
        private int failedHeals;
        private final Map<String, LocatorPerformance> locatorPerformance;
        private LocalDateTime lastAttempt;
        private final List<String> failureHistory;

        /**
         * Creates new element healing statistics
         *
         * @param elementName Name of the element
         */
        public ElementHealingStats(String elementName) {
            this.elementName = elementName;
            this.totalAttempts = 0;
            this.successfulHeals = 0;
            this.failedHeals = 0;
            this.locatorPerformance = new LinkedHashMap<>();
            this.lastAttempt = null;
            this.failureHistory = new ArrayList<>();
        }

        /**
         * Records a successful healing attempt
         *
         * @param strategy The strategy that succeeded
         */
        public void recordSuccess(LocatorStrategy strategy) {
            totalAttempts++;
            successfulHeals++;
            lastAttempt = LocalDateTime.now();

            String key = strategy.getStrategy() + "=" + strategy.getValue();
            LocatorPerformance perf = locatorPerformance.computeIfAbsent(key,
                    k -> new LocatorPerformance(strategy.getStrategy(), strategy.getValue()));
            perf.recordSuccess();
        }

        /**
         * Records a failed healing attempt
         *
         * @param attemptedStrategies All strategies that were tried
         */
        public void recordFailure(List<LocatorStrategy> attemptedStrategies) {
            totalAttempts++;
            failedHeals++;
            lastAttempt = LocalDateTime.now();

            // Record failures for all attempted strategies
            for (LocatorStrategy strategy : attemptedStrategies) {
                String key = strategy.getStrategy() + "=" + strategy.getValue();
                LocatorPerformance perf = locatorPerformance.computeIfAbsent(key,
                        k -> new LocatorPerformance(strategy.getStrategy(), strategy.getValue()));
                perf.recordFailure();
            }

            // Add to failure history
            String failureRecord = LocalDateTime.now().format(DATE_FORMATTER) +
                    " - Tried " + attemptedStrategies.size() + " strategies";
            failureHistory.add(failureRecord);

            // Keep only recent failures
            if (failureHistory.size() > MAX_FAILURE_HISTORY) {
                failureHistory.remove(0);
            }
        }

        /**
         * Gets healing success rate as percentage (0-100)
         *
         * @return Success rate percentage
         */
        public double getHealingSuccessRate() {
            if (totalAttempts == 0) return 100.0;
            return (double) successfulHeals / totalAttempts * 100.0;
        }

        /**
         * Gets the most reliable locator strategy for this element
         *
         * @return Name of the most reliable strategy or "None" if no data
         */
        public String getMostReliableLocator() {
            if (locatorPerformance.isEmpty()) {
                return "None";
            }

            return locatorPerformance.values().stream()
                    .max(Comparator.comparingDouble(LocatorPerformance::getSuccessRate)
                            .thenComparingInt(LocatorPerformance::getSuccesses))
                    .map(perf -> perf.getStrategy() + "=" + perf.getValue())
                    .orElse("None");
        }

        public String getElementName() {
            return elementName;
        }

        public int getTotalAttempts() {
            return totalAttempts;
        }

        public int getSuccessfulHeals() {
            return successfulHeals;
        }

        public int getFailedHeals() {
            return failedHeals;
        }

        public Map<String, LocatorPerformance> getLocatorPerformance() {
            return new LinkedHashMap<>(locatorPerformance);
        }

        public LocalDateTime getLastAttempt() {
            return lastAttempt;
        }

        public List<String> getFailureHistory() {
            return new ArrayList<>(failureHistory);
        }
    }

    /**
     * Creates a new HealingMetrics instance
     */
    public HealingMetrics() {
        this.elementStats = new LinkedHashMap<>();
        System.out.println("[HealingMetrics] Initialized");
    }

    /**
     * Records a successful healing attempt
     *
     * @param elementName Name of the element
     * @param strategy The strategy that succeeded
     */
    public void recordHealingSuccess(String elementName, LocatorStrategy strategy) {
        ElementHealingStats stats = elementStats.computeIfAbsent(elementName, ElementHealingStats::new);
        stats.recordSuccess(strategy);
    }

    /**
     * Records a failed healing attempt
     *
     * @param elementName Name of the element
     * @param attemptedStrategies All strategies that were tried
     */
    public void recordHealingFailure(String elementName, List<LocatorStrategy> attemptedStrategies) {
        ElementHealingStats stats = elementStats.computeIfAbsent(elementName, ElementHealingStats::new);
        stats.recordFailure(attemptedStrategies);
    }

    /**
     * Gets healing statistics for a specific element
     *
     * @param elementName Name of the element
     * @return Element statistics or null if not found
     */
    public ElementHealingStats getElementStats(String elementName) {
        return elementStats.get(elementName);
    }

    /**
     * Gets elements with healing success rate below threshold (brittle elements)
     *
     * @param thresholdPercent Success rate threshold (0-100)
     * @return List of brittle element names
     */
    public List<String> getBrittleElements(double thresholdPercent) {
        return elementStats.values().stream()
                .filter(stats -> stats.getTotalAttempts() > 0)
                .filter(stats -> stats.getHealingSuccessRate() < thresholdPercent)
                .map(ElementHealingStats::getElementName)
                .collect(Collectors.toList());
    }

    /**
     * Gets elements with 100% healing success rate (robust elements)
     *
     * @return List of robust element names
     */
    public List<String> getRobustElements() {
        return elementStats.values().stream()
                .filter(stats -> stats.getTotalAttempts() > 0)
                .filter(stats -> stats.getHealingSuccessRate() == 100.0)
                .map(ElementHealingStats::getElementName)
                .collect(Collectors.toList());
    }

    /**
     * Prints a comprehensive healing report to console
     * Shows all elements with their healing statistics
     */
    public void printHealingReport() {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║           SELF-HEALING METRICS REPORT                         ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        if (elementStats.isEmpty()) {
            System.out.println("\nNo healing attempts recorded yet.\n");
            return;
        }

        int totalAttempts = elementStats.values().stream().mapToInt(ElementHealingStats::getTotalAttempts).sum();
        int totalSuccesses = elementStats.values().stream().mapToInt(ElementHealingStats::getSuccessfulHeals).sum();
        int totalFailures = elementStats.values().stream().mapToInt(ElementHealingStats::getFailedHeals).sum();
        double overallSuccessRate = totalAttempts > 0 ? (double) totalSuccesses / totalAttempts * 100.0 : 0.0;

        System.out.println("\n📊 OVERALL STATISTICS:");
        System.out.println("   Total healing attempts: " + totalAttempts);
        System.out.println("   Successful heals: " + totalSuccesses);
        System.out.println("   Failed heals: " + totalFailures);
        System.out.println("   Overall success rate: " + String.format("%.1f%%", overallSuccessRate));

        System.out.println("\n📝 ELEMENT-BY-ELEMENT BREAKDOWN:");
        System.out.println("───────────────────────────────────────────────────────────────");

        for (ElementHealingStats stats : elementStats.values()) {
            printElementStats(stats);
        }

        // Summary section
        List<String> robustElements = getRobustElements();
        List<String> brittleElements = getBrittleElements(80.0);

        System.out.println("\n🏆 ROBUST ELEMENTS (100% success):");
        if (robustElements.isEmpty()) {
            System.out.println("   None");
        } else {
            robustElements.forEach(name -> System.out.println("   ✓ " + name));
        }

        System.out.println("\n⚠️  BRITTLE ELEMENTS (<80% success):");
        if (brittleElements.isEmpty()) {
            System.out.println("   None");
        } else {
            brittleElements.forEach(name -> {
                ElementHealingStats stats = elementStats.get(name);
                System.out.println("   ✗ " + name + " - " + String.format("%.1f%%", stats.getHealingSuccessRate()));
            });
        }

        System.out.println("\n════════════════════════════════════════════════════════════════\n");
    }

    /**
     * Prints detailed statistics for a single element
     *
     * @param stats Element statistics to print
     */
    private void printElementStats(ElementHealingStats stats) {
        System.out.println("\n🔹 " + stats.getElementName());
        System.out.println("   Success Rate: " + String.format("%.1f%%", stats.getHealingSuccessRate()) +
                " (" + stats.getSuccessfulHeals() + "/" + stats.getTotalAttempts() + " attempts)");

        if (stats.getLastAttempt() != null) {
            System.out.println("   Last Attempt: " + stats.getLastAttempt().format(DATE_FORMATTER));
        }

        System.out.println("   Most Reliable Locator: " + stats.getMostReliableLocator());

        // Locator performance breakdown
        if (!stats.getLocatorPerformance().isEmpty()) {
            System.out.println("   Locator Performance:");
            stats.getLocatorPerformance().values().forEach(perf ->
                    System.out.println("      • " + perf.toString()));
        }

        // Recent failures
        if (!stats.getFailureHistory().isEmpty()) {
            System.out.println("   Recent Failures:");
            stats.getFailureHistory().forEach(failure ->
                    System.out.println("      ✗ " + failure));
        }
    }

    /**
     * Exports metrics as a map for JSON serialization
     *
     * @return Map containing all metrics data
     */
    public Map<String, Object> exportMetricsAsMap() {
        Map<String, Object> export = new LinkedHashMap<>();

        int totalAttempts = elementStats.values().stream().mapToInt(ElementHealingStats::getTotalAttempts).sum();
        int totalSuccesses = elementStats.values().stream().mapToInt(ElementHealingStats::getSuccessfulHeals).sum();
        int totalFailures = elementStats.values().stream().mapToInt(ElementHealingStats::getFailedHeals).sum();
        double overallSuccessRate = totalAttempts > 0 ? (double) totalSuccesses / totalAttempts * 100.0 : 0.0;

        Map<String, Object> overall = new LinkedHashMap<>();
        overall.put("totalAttempts", totalAttempts);
        overall.put("totalSuccesses", totalSuccesses);
        overall.put("totalFailures", totalFailures);
        overall.put("overallSuccessRate", String.format("%.2f%%", overallSuccessRate));

        export.put("overall", overall);

        List<Map<String, Object>> elements = new ArrayList<>();
        for (ElementHealingStats stats : elementStats.values()) {
            Map<String, Object> elementData = new LinkedHashMap<>();
            elementData.put("name", stats.getElementName());
            elementData.put("totalAttempts", stats.getTotalAttempts());
            elementData.put("successfulHeals", stats.getSuccessfulHeals());
            elementData.put("failedHeals", stats.getFailedHeals());
            elementData.put("successRate", String.format("%.2f%%", stats.getHealingSuccessRate()));
            elementData.put("mostReliableLocator", stats.getMostReliableLocator());

            if (stats.getLastAttempt() != null) {
                elementData.put("lastAttempt", stats.getLastAttempt().format(DATE_FORMATTER));
            }

            List<Map<String, Object>> locators = new ArrayList<>();
            for (LocatorPerformance perf : stats.getLocatorPerformance().values()) {
                Map<String, Object> locatorData = new LinkedHashMap<>();
                locatorData.put("strategy", perf.getStrategy());
                locatorData.put("value", perf.getValue());
                locatorData.put("attempts", perf.getAttempts());
                locatorData.put("successes", perf.getSuccesses());
                locatorData.put("failures", perf.getFailures());
                locatorData.put("successRate", String.format("%.2f%%", perf.getSuccessRate()));
                locators.add(locatorData);
            }
            elementData.put("locatorPerformance", locators);

            elements.add(elementData);
        }
        export.put("elements", elements);

        export.put("robustElements", getRobustElements());
        export.put("brittleElements", getBrittleElements(80.0));

        return export;
    }

    /**
     * Gets the total number of elements tracked
     *
     * @return Number of tracked elements
     */
    public int getTrackedElementCount() {
        return elementStats.size();
    }

    /**
     * Clears all metrics
     */
    public void clear() {
        elementStats.clear();
        System.out.println("[HealingMetrics] Metrics cleared");
    }

    /**
     * Saves current metrics to a JSON file
     * Creates the directory if it doesn't exist
     *
     * @param filePath Path to save the metrics file
     * @throws IOException if file write fails
     */
    public void saveMetricsToFile(String filePath) throws IOException {
        // Create directory if it doesn't exist
        File file = new File(filePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        // Export metrics to map
        Map<String, Object> metricsData = exportMetricsAsMap();

        // Add metadata
        metricsData.put("timestamp", LocalDateTime.now().format(DATE_FORMATTER));
        metricsData.put("version", "1.0.0");

        // Write to JSON file with pretty printing
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.writeValue(file, metricsData);

        System.out.println("✓ Healing metrics saved to: " + filePath);
    }

    /**
     * Saves current metrics with automatic timestamped filename
     *
     * @param directory Directory to save metrics
     * @return Path to saved file
     * @throws IOException if file write fails
     */
    public String saveMetricsWithTimestamp(String directory) throws IOException {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        String filePath = directory + "/healing-metrics_" + timestamp + ".json";
        saveMetricsToFile(filePath);
        return filePath;
    }

    /**
     * Loads historical metrics from a JSON file
     *
     * @param filePath Path to the metrics file
     * @return Map containing historical metrics data
     * @throws IOException if file read fails
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> loadMetricsFromFile(String filePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(filePath);

        if (!file.exists()) {
            throw new IOException("Metrics file not found: " + filePath);
        }

        Map<String, Object> metricsData = mapper.readValue(file, Map.class);
        System.out.println("✓ Healing metrics loaded from: " + filePath);

        return metricsData;
    }

    /**
     * Loads all historical metrics files from a directory
     *
     * @param directory Directory containing metrics files
     * @return List of historical metrics maps, sorted by timestamp
     * @throws IOException if directory read fails
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> loadHistoricalMetrics(String directory) throws IOException {
        List<Map<String, Object>> historicalMetrics = new ArrayList<>();
        File dir = new File(directory);

        if (!dir.exists() || !dir.isDirectory()) {
            System.out.println("⚠ Historical metrics directory not found: " + directory);
            return historicalMetrics;
        }

        File[] files = dir.listFiles((d, name) -> name.startsWith("healing-metrics_") && name.endsWith(".json"));

        if (files == null || files.length == 0) {
            System.out.println("⚠ No historical metrics files found in: " + directory);
            return historicalMetrics;
        }

        ObjectMapper mapper = new ObjectMapper();

        for (File file : files) {
            try {
                Map<String, Object> metricsData = mapper.readValue(file, Map.class);
                metricsData.put("fileName", file.getName());
                historicalMetrics.add(metricsData);
            } catch (IOException e) {
                System.err.println("⚠ Failed to load metrics from: " + file.getName() + " - " + e.getMessage());
            }
        }

        // Sort by timestamp (most recent first)
        historicalMetrics.sort((m1, m2) -> {
            String ts1 = (String) m1.getOrDefault("timestamp", "");
            String ts2 = (String) m2.getOrDefault("timestamp", "");
            return ts2.compareTo(ts1); // Descending order
        });

        System.out.println("✓ Loaded " + historicalMetrics.size() + " historical metrics files");

        return historicalMetrics;
    }

    /**
     * Generates a trend report comparing current metrics with historical data
     *
     * @param directory Directory containing historical metrics files
     * @throws IOException if file operations fail
     */
    public void generateTrendReport(String directory) throws IOException {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║           HEALING METRICS TREND REPORT                        ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        // Load historical metrics
        List<Map<String, Object>> historicalMetrics = loadHistoricalMetrics(directory);

        if (historicalMetrics.isEmpty()) {
            System.out.println("\nNo historical data available for trend analysis.\n");
            return;
        }

        // Current metrics
        Map<String, Object> currentMetrics = exportMetricsAsMap();
        Map<String, Object> currentOverall = (Map<String, Object>) currentMetrics.get("overall");

        System.out.println("\n📈 TREND ANALYSIS:");
        System.out.println("   Historical data points: " + historicalMetrics.size());
        System.out.println("   Current timestamp: " + LocalDateTime.now().format(DATE_FORMATTER));

        // Overall success rate trend
        System.out.println("\n📊 OVERALL SUCCESS RATE TREND:");
        System.out.println("───────────────────────────────────────────────────────────────");

        int count = 0;
        for (Map<String, Object> metrics : historicalMetrics) {
            if (count >= 10) break; // Show last 10 data points

            String timestamp = (String) metrics.get("timestamp");
            Map<String, Object> overall = (Map<String, Object>) metrics.get("overall");

            if (overall != null) {
                String successRate = (String) overall.get("overallSuccessRate");
                int totalAttempts = (int) overall.get("totalAttempts");
                System.out.println("   " + timestamp + " - " + successRate +
                        " (" + totalAttempts + " attempts)");
            }
            count++;
        }

        // Current metrics
        System.out.println("   CURRENT - " + currentOverall.get("overallSuccessRate") +
                " (" + currentOverall.get("totalAttempts") + " attempts)");

        // Element-specific trends
        System.out.println("\n🔍 ELEMENT TREND ANALYSIS:");
        System.out.println("───────────────────────────────────────────────────────────────");

        Map<String, List<Double>> elementTrends = new HashMap<>();

        // Collect success rates for each element across history
        for (Map<String, Object> metrics : historicalMetrics) {
            List<Map<String, Object>> elements = (List<Map<String, Object>>) metrics.get("elements");
            if (elements != null) {
                for (Map<String, Object> element : elements) {
                    String name = (String) element.get("name");
                    String successRateStr = (String) element.get("successRate");

                    if (successRateStr != null) {
                        double successRate = Double.parseDouble(successRateStr.replace("%", ""));
                        elementTrends.computeIfAbsent(name, k -> new ArrayList<>()).add(successRate);
                    }
                }
            }
        }

        // Show trends for elements with most data points
        elementTrends.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
                .limit(5)
                .forEach(entry -> {
                    String elementName = entry.getKey();
                    List<Double> rates = entry.getValue();

                    double avgRate = rates.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                    double minRate = rates.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
                    double maxRate = rates.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);

                    System.out.println("\n   🔹 " + elementName);
                    System.out.println("      Data points: " + rates.size());
                    System.out.println("      Average: " + String.format("%.1f%%", avgRate));
                    System.out.println("      Range: " + String.format("%.1f%% - %.1f%%", minRate, maxRate));

                    // Determine trend direction
                    if (rates.size() >= 2) {
                        double recentAvg = rates.subList(0, Math.min(3, rates.size())).stream()
                                .mapToDouble(Double::doubleValue).average().orElse(0.0);
                        double olderAvg = rates.subList(Math.min(3, rates.size()), rates.size()).stream()
                                .mapToDouble(Double::doubleValue).average().orElse(0.0);

                        if (recentAvg > olderAvg + 5) {
                            System.out.println("      Trend: ↗ IMPROVING");
                        } else if (recentAvg < olderAvg - 5) {
                            System.out.println("      Trend: ↘ DEGRADING");
                        } else {
                            System.out.println("      Trend: → STABLE");
                        }
                    }
                });

        // Brittle elements analysis
        System.out.println("\n⚠️  BRITTLE ELEMENTS TRACKING:");
        System.out.println("───────────────────────────────────────────────────────────────");

        Map<String, Integer> brittleCount = new HashMap<>();

        for (Map<String, Object> metrics : historicalMetrics) {
            List<String> brittleElements = (List<String>) metrics.get("brittleElements");
            if (brittleElements != null) {
                for (String element : brittleElements) {
                    brittleCount.merge(element, 1, Integer::sum);
                }
            }
        }

        if (brittleCount.isEmpty()) {
            System.out.println("   No consistently brittle elements detected.");
        } else {
            brittleCount.entrySet().stream()
                    .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                    .forEach(entry -> {
                        double percentage = (double) entry.getValue() / historicalMetrics.size() * 100;
                        System.out.println("   ✗ " + entry.getKey() +
                                " - Brittle in " + entry.getValue() + "/" + historicalMetrics.size() +
                                " runs (" + String.format("%.1f%%", percentage) + ")");
                    });
        }

        System.out.println("\n════════════════════════════════════════════════════════════════\n");
    }

    /**
     * Saves metrics and generates trend report in one operation
     *
     * @param directory Directory to save metrics and load historical data
     * @throws IOException if file operations fail
     */
    public void saveAndGenerateTrends(String directory) throws IOException {
        // Ensure directory exists
        Files.createDirectories(Paths.get(directory));

        // Save current metrics
        String savedFile = saveMetricsWithTimestamp(directory);
        System.out.println("✓ Metrics saved to: " + savedFile);

        // Generate trend report
        generateTrendReport(directory);
    }
}
