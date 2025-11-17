package framework.locators;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * LocatorRegistry - Central repository for element locators with multiple strategies
 *
 * This registry manages element locators with fallback strategies and tracks success/failure
 * rates to learn which locators are most reliable. It enables self-healing by providing
 * multiple ways to find the same element.
 *
 * Key Features:
 * - Multiple locator strategies per element (id, css, xpath, etc.)
 * - Success/failure tracking per strategy
 * - Automatic sorting by success rate
 * - JSON persistence for learning across test runs
 * - Priority-based fallback chain
 *
 * Example Usage:
 * <pre>
 * LocatorRegistry registry = new LocatorRegistry();
 * registry.loadRegistry();
 * registry.addElement("loginButton",
 *     new LocatorStrategy("id", "login-btn", 1),
 *     new LocatorStrategy("css", "button.login", 2)
 * );
 * ElementLocator locator = registry.getElement("loginButton");
 * List&lt;LocatorStrategy&gt; strategies = locator.getStrategiesBySuccessRate();
 * </pre>
 *
 * @author Selenium-MCP Framework
 * @version 1.0.0
 */
public class LocatorRegistry {

    private final Map<String, ElementLocator> registry;
    private final ObjectMapper objectMapper;
    private static final String DEFAULT_REGISTRY_PATH = "src/test/resources/locators/locators.json";

    /**
     * Locator strategy with priority
     * Represents one way to find an element (e.g., by ID, CSS, XPath)
     */
    public static class LocatorStrategy {
        private final String strategy;
        private final String value;
        private final int priority;

        /**
         * Creates a new locator strategy
         *
         * @param strategy Locator type (id, css, xpath, name, className, tagName, linkText, partialLinkText)
         * @param value Locator value (the actual selector)
         * @param priority Priority (1=primary, 2=fallback, 3=fallback2, etc.)
         */
        public LocatorStrategy(String strategy, String value, int priority) {
            this.strategy = strategy;
            this.value = value;
            this.priority = priority;
        }

        public String getStrategy() {
            return strategy;
        }

        public String getValue() {
            return value;
        }

        public int getPriority() {
            return priority;
        }

        @Override
        public String toString() {
            return strategy + "=" + value + " (priority: " + priority + ")";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LocatorStrategy that = (LocatorStrategy) o;
            return Objects.equals(strategy, that.strategy) && Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(strategy, value);
        }
    }

    /**
     * Element locator with multiple strategies and success tracking
     * Represents all the ways to find a specific element on a page
     */
    public static class ElementLocator {
        private final String elementName;
        private final List<LocatorStrategy> strategies;
        private final Map<LocatorStrategy, Integer> successCount;
        private final Map<LocatorStrategy, Integer> failureCount;

        /**
         * Creates a new element locator
         *
         * @param elementName Name of the element (e.g., "loginButton", "usernameInput")
         * @param strategies List of locator strategies to try
         */
        public ElementLocator(String elementName, List<LocatorStrategy> strategies) {
            this.elementName = elementName;
            this.strategies = new ArrayList<>(strategies);
            this.successCount = new HashMap<>();
            this.failureCount = new HashMap<>();

            // Initialize counters for all strategies
            for (LocatorStrategy strategy : strategies) {
                successCount.put(strategy, 0);
                failureCount.put(strategy, 0);
            }
        }

        public String getElementName() {
            return elementName;
        }

        public List<LocatorStrategy> getStrategies() {
            return new ArrayList<>(strategies);
        }

        /**
         * Gets strategies sorted by success rate (highest first)
         * This is the key to self-healing - try the most successful locators first
         *
         * @return List of strategies sorted by success rate
         */
        public List<LocatorStrategy> getStrategiesBySuccessRate() {
            return strategies.stream()
                    .sorted((s1, s2) -> {
                        double rate1 = getSuccessRate(s1);
                        double rate2 = getSuccessRate(s2);
                        if (rate1 != rate2) {
                            return Double.compare(rate2, rate1); // Higher success rate first
                        }
                        // If equal success rates, use priority
                        return Integer.compare(s1.getPriority(), s2.getPriority());
                    })
                    .collect(Collectors.toList());
        }

        /**
         * Records a successful element find
         *
         * @param strategy The strategy that succeeded
         */
        public void recordSuccess(LocatorStrategy strategy) {
            successCount.merge(strategy, 1, Integer::sum);
        }

        /**
         * Records a failed element find
         *
         * @param strategy The strategy that failed
         */
        public void recordFailure(LocatorStrategy strategy) {
            failureCount.merge(strategy, 1, Integer::sum);
        }

        /**
         * Gets success rate for a strategy (0.0 to 1.0)
         *
         * @param strategy The strategy to check
         * @return Success rate as decimal (e.g., 0.85 = 85%)
         */
        public double getSuccessRate(LocatorStrategy strategy) {
            int successes = successCount.getOrDefault(strategy, 0);
            int failures = failureCount.getOrDefault(strategy, 0);
            int total = successes + failures;

            if (total == 0) {
                // No attempts yet, use priority as tiebreaker
                return 0.5;
            }

            return (double) successes / total;
        }

        /**
         * Gets total attempts for a strategy
         *
         * @param strategy The strategy to check
         * @return Total number of attempts (successes + failures)
         */
        public int getTotalAttempts(LocatorStrategy strategy) {
            return successCount.getOrDefault(strategy, 0) + failureCount.getOrDefault(strategy, 0);
        }

        public int getSuccessCount(LocatorStrategy strategy) {
            return successCount.getOrDefault(strategy, 0);
        }

        public int getFailureCount(LocatorStrategy strategy) {
            return failureCount.getOrDefault(strategy, 0);
        }
    }

    /**
     * Creates a new LocatorRegistry
     * Uses LinkedHashMap to preserve insertion order
     */
    public LocatorRegistry() {
        this.registry = new LinkedHashMap<>();
        this.objectMapper = new ObjectMapper();
        System.out.println("[LocatorRegistry] Initialized");
    }

    /**
     * Adds an element with its locator strategies
     *
     * @param elementName Name of the element
     * @param strategies Variable number of locator strategies
     */
    public void addElement(String elementName, LocatorStrategy... strategies) {
        List<LocatorStrategy> strategyList = Arrays.asList(strategies);
        ElementLocator locator = new ElementLocator(elementName, strategyList);
        registry.put(elementName, locator);
        System.out.println("[LocatorRegistry] Added element: " + elementName + " with " + strategies.length + " strategies");
    }

    /**
     * Gets an element locator by name
     *
     * @param elementName Name of the element
     * @return The ElementLocator or null if not found
     */
    public ElementLocator getElement(String elementName) {
        return registry.get(elementName);
    }

    /**
     * Gets the primary (highest priority) strategy for an element
     *
     * @param elementName Name of the element
     * @return The primary strategy or null if element not found
     */
    public LocatorStrategy getPrimaryStrategy(String elementName) {
        ElementLocator locator = registry.get(elementName);
        if (locator == null || locator.getStrategies().isEmpty()) {
            return null;
        }
        return locator.getStrategies().stream()
                .min(Comparator.comparingInt(LocatorStrategy::getPriority))
                .orElse(null);
    }

    /**
     * Gets all strategies for an element sorted by success rate
     *
     * @param elementName Name of the element
     * @return List of strategies sorted by success rate
     */
    public List<LocatorStrategy> getAllStrategies(String elementName) {
        ElementLocator locator = registry.get(elementName);
        if (locator == null) {
            return Collections.emptyList();
        }
        return locator.getStrategiesBySuccessRate();
    }

    /**
     * Records a successful element find
     *
     * @param elementName Name of the element
     * @param strategy The strategy that succeeded
     */
    public void recordSuccess(String elementName, LocatorStrategy strategy) {
        ElementLocator locator = registry.get(elementName);
        if (locator != null) {
            locator.recordSuccess(strategy);
        }
    }

    /**
     * Records a failed element find
     *
     * @param elementName Name of the element
     * @param strategy The strategy that failed
     */
    public void recordFailure(String elementName, LocatorStrategy strategy) {
        ElementLocator locator = registry.get(elementName);
        if (locator != null) {
            locator.recordFailure(strategy);
        }
    }

    /**
     * Gets success rate for a specific element and strategy
     *
     * @param elementName Name of the element
     * @param strategy The strategy to check
     * @return Success rate as decimal (0.0 to 1.0)
     */
    public double getSuccessRate(String elementName, LocatorStrategy strategy) {
        ElementLocator locator = registry.get(elementName);
        if (locator == null) {
            return 0.0;
        }
        return locator.getSuccessRate(strategy);
    }

    /**
     * Loads the registry from a JSON file
     * Merges with existing registry data
     *
     * @throws IOException if file cannot be read
     */
    public void loadRegistry() throws IOException {
        loadRegistry(DEFAULT_REGISTRY_PATH);
    }

    /**
     * Loads the registry from a JSON file at specified path
     * Merges with existing registry data
     *
     * @param filePath Path to JSON file
     * @throws IOException if file cannot be read
     */
    public void loadRegistry(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("[LocatorRegistry] Registry file not found: " + filePath + " (creating new registry)");
            return;
        }

        System.out.println("[LocatorRegistry] Loading registry from: " + filePath);

        String jsonContent = new String(Files.readAllBytes(Paths.get(filePath)));
        JsonNode root = objectMapper.readTree(jsonContent);

        if (!root.isObject()) {
            throw new IOException("Invalid registry format: root must be an object");
        }

        root.fields().forEachRemaining(entry -> {
            String elementName = entry.getKey();
            JsonNode strategiesNode = entry.getValue();

            if (strategiesNode.isArray()) {
                List<LocatorStrategy> strategies = new ArrayList<>();

                for (JsonNode strategyNode : strategiesNode) {
                    String strategy = strategyNode.get("strategy").asText();
                    String value = strategyNode.get("value").asText();
                    int priority = strategyNode.get("priority").asInt();

                    strategies.add(new LocatorStrategy(strategy, value, priority));
                }

                addElement(elementName, strategies.toArray(new LocatorStrategy[0]));
            }
        });

        System.out.println("[LocatorRegistry] Loaded " + registry.size() + " elements from registry");
    }

    /**
     * Saves the registry to a JSON file
     *
     * @throws IOException if file cannot be written
     */
    public void saveRegistry() throws IOException {
        saveRegistry(DEFAULT_REGISTRY_PATH);
    }

    /**
     * Saves the registry to a JSON file at specified path
     * Includes success/failure statistics for analysis
     *
     * @param filePath Path to JSON file
     * @throws IOException if file cannot be written
     */
    public void saveRegistry(String filePath) throws IOException {
        System.out.println("[LocatorRegistry] Saving registry to: " + filePath);

        ObjectNode root = objectMapper.createObjectNode();

        for (Map.Entry<String, ElementLocator> entry : registry.entrySet()) {
            String elementName = entry.getKey();
            ElementLocator locator = entry.getValue();

            ArrayNode strategiesArray = objectMapper.createArrayNode();

            for (LocatorStrategy strategy : locator.getStrategies()) {
                ObjectNode strategyNode = objectMapper.createObjectNode();
                strategyNode.put("strategy", strategy.getStrategy());
                strategyNode.put("value", strategy.getValue());
                strategyNode.put("priority", strategy.getPriority());

                // Add statistics for analysis
                strategyNode.put("successCount", locator.getSuccessCount(strategy));
                strategyNode.put("failureCount", locator.getFailureCount(strategy));
                strategyNode.put("successRate", String.format("%.2f", locator.getSuccessRate(strategy) * 100) + "%");

                strategiesArray.add(strategyNode);
            }

            root.set(elementName, strategiesArray);
        }

        // Ensure directory exists
        File file = new File(filePath);
        file.getParentFile().mkdirs();

        // Write formatted JSON
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, root);

        System.out.println("[LocatorRegistry] Registry saved successfully");
    }

    /**
     * Prints the registry for debugging
     * Shows all elements with their strategies and success rates
     */
    public void printRegistry() {
        System.out.println("\n========== LOCATOR REGISTRY ==========");
        System.out.println("Total elements: " + registry.size());
        System.out.println("=====================================");

        for (Map.Entry<String, ElementLocator> entry : registry.entrySet()) {
            String elementName = entry.getKey();
            ElementLocator locator = entry.getValue();

            System.out.println("\n" + elementName + ":");

            List<LocatorStrategy> strategies = locator.getStrategiesBySuccessRate();
            for (LocatorStrategy strategy : strategies) {
                int successes = locator.getSuccessCount(strategy);
                int failures = locator.getFailureCount(strategy);
                int total = successes + failures;
                double successRate = locator.getSuccessRate(strategy);

                System.out.printf("  • %s (priority: %d) - Success: %d/%d (%.1f%%)%n",
                        strategy.getStrategy() + "=" + strategy.getValue(),
                        strategy.getPriority(),
                        successes,
                        total,
                        successRate * 100);
            }
        }

        System.out.println("\n=====================================\n");
    }

    /**
     * Gets all registered element names
     *
     * @return Set of element names
     */
    public Set<String> getAllElementNames() {
        return new HashSet<>(registry.keySet());
    }

    /**
     * Checks if an element is registered
     *
     * @param elementName Name of the element
     * @return true if element exists, false otherwise
     */
    public boolean hasElement(String elementName) {
        return registry.containsKey(elementName);
    }

    /**
     * Gets the total number of registered elements
     *
     * @return Number of elements in registry
     */
    public int size() {
        return registry.size();
    }

    /**
     * Clears all elements from the registry
     */
    public void clear() {
        registry.clear();
        System.out.println("[LocatorRegistry] Registry cleared");
    }
}
