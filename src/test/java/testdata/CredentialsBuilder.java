package testdata;

import java.util.Random;

/**
 * CredentialsBuilder - Builder pattern for creating Credentials test data
 *
 * This class provides a fluent API for creating login credentials with various scenarios.
 * It supports valid, invalid, and edge case credential generation.
 *
 * Example Usage:
 * <pre>
 * Credentials creds = new CredentialsBuilder()
 *     .withUsername("john.doe@example.com")
 *     .withPassword("SecurePass123")
 *     .build();
 *
 * // Or with invalid data
 * Credentials invalid = new CredentialsBuilder().buildInvalid();
 * </pre>
 *
 * @author Selenium-MCP Framework
 * @version 1.0.0
 */
public class CredentialsBuilder {

    private String username;
    private String password;

    private static final Random random = new Random();

    /**
     * Creates a new CredentialsBuilder instance with default values
     */
    public CredentialsBuilder() {
        // Default values
        this.username = "testuser@example.com";
        this.password = "Password123";
    }

    /**
     * Sets the username
     *
     * @param username Username
     * @return CredentialsBuilder instance
     */
    public CredentialsBuilder withUsername(String username) {
        this.username = username;
        return this;
    }

    /**
     * Sets the password
     *
     * @param password Password
     * @return CredentialsBuilder instance
     */
    public CredentialsBuilder withPassword(String password) {
        this.password = password;
        return this;
    }

    /**
     * Builds a Credentials object with current values
     *
     * @return Credentials object
     */
    public Credentials build() {
        return new Credentials(username, password);
    }

    /**
     * Builds valid admin credentials
     *
     * @return Credentials object for admin
     */
    public Credentials buildAdmin() {
        this.username = "admin@example.com";
        this.password = "AdminPass123";
        return build();
    }

    /**
     * Builds invalid credentials
     *
     * @return Credentials object with invalid data
     */
    public Credentials buildInvalid() {
        this.username = "invalid@example.com";
        this.password = "WrongPassword";
        return build();
    }

    /**
     * Builds credentials with empty username
     *
     * @return Credentials object with empty username
     */
    public Credentials buildWithEmptyUsername() {
        this.username = "";
        return build();
    }

    /**
     * Builds credentials with empty password
     *
     * @return Credentials object with empty password
     */
    public Credentials buildWithEmptyPassword() {
        this.password = "";
        return build();
    }

    /**
     * Builds credentials with both fields empty
     *
     * @return Credentials object with empty fields
     */
    public Credentials buildEmpty() {
        this.username = "";
        this.password = "";
        return build();
    }

    /**
     * Builds credentials with SQL injection attempt
     *
     * @return Credentials object with SQL injection string
     */
    public Credentials buildWithSQLInjection() {
        this.username = "admin' OR '1'='1";
        this.password = "anything";
        return build();
    }

    /**
     * Builds credentials with XSS attempt
     *
     * @return Credentials object with XSS script
     */
    public Credentials buildWithXSS() {
        this.username = "<script>alert('XSS')</script>";
        this.password = "test";
        return build();
    }

    /**
     * Builds credentials with special characters
     *
     * @return Credentials object with special characters
     */
    public Credentials buildWithSpecialCharacters() {
        this.username = "test!@#$%^&*()@example.com";
        this.password = "P@$$w0rd!#";
        return build();
    }

    /**
     * Builds credentials with whitespace
     *
     * @return Credentials object with whitespace
     */
    public Credentials buildWithWhitespace() {
        this.username = " testuser@example.com ";
        this.password = " Password123 ";
        return build();
    }

    /**
     * Builds random valid credentials
     *
     * @return Credentials object with random data
     */
    public Credentials buildRandom() {
        int randomNum = random.nextInt(10000);
        this.username = "user" + randomNum + "@example.com";
        this.password = "Pass" + randomNum + "!";
        return build();
    }

    /**
     * Credentials data class
     */
    public static class Credentials {
        private final String username;
        private final String password;

        public Credentials(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        @Override
        public String toString() {
            return "Credentials{" +
                    "username='" + username + '\'' +
                    ", password='***'" +
                    '}';
        }
    }
}
