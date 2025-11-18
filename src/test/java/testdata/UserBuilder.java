package testdata;

import java.util.Random;

/**
 * UserBuilder - Builder pattern for creating User test data objects
 *
 * This class provides a fluent API for creating user test data with default and random values.
 * It supports various user attributes and validation scenarios.
 *
 * Example Usage:
 * <pre>
 * User user = new UserBuilder()
 *     .withFirstName("John")
 *     .withLastName("Doe")
 *     .withEmail("john.doe@example.com")
 *     .withPassword("SecurePass123")
 *     .build();
 *
 * // Or with random data
 * User randomUser = new UserBuilder().buildRandom();
 * </pre>
 *
 * @author Selenium-MCP Framework
 * @version 1.0.0
 */
public class UserBuilder {

    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String phone;
    private String address;
    private boolean termsAccepted;

    private static final Random random = new Random();
    private static final String[] FIRST_NAMES = {"John", "Jane", "Michael", "Sarah", "David", "Emma", "Robert", "Lisa"};
    private static final String[] LAST_NAMES = {"Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis"};

    /**
     * Creates a new UserBuilder instance with default values
     */
    public UserBuilder() {
        // Default values
        this.firstName = "Test";
        this.lastName = "User";
        this.email = "test.user@example.com";
        this.password = "Password123";
        this.phone = "";
        this.address = "";
        this.termsAccepted = true;
    }

    /**
     * Sets the first name
     *
     * @param firstName First name
     * @return UserBuilder instance
     */
    public UserBuilder withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    /**
     * Sets the last name
     *
     * @param lastName Last name
     * @return UserBuilder instance
     */
    public UserBuilder withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    /**
     * Sets the email
     *
     * @param email Email address
     * @return UserBuilder instance
     */
    public UserBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    /**
     * Sets the password
     *
     * @param password Password
     * @return UserBuilder instance
     */
    public UserBuilder withPassword(String password) {
        this.password = password;
        return this;
    }

    /**
     * Sets the phone number
     *
     * @param phone Phone number
     * @return UserBuilder instance
     */
    public UserBuilder withPhone(String phone) {
        this.phone = phone;
        return this;
    }

    /**
     * Sets the address
     *
     * @param address Address
     * @return UserBuilder instance
     */
    public UserBuilder withAddress(String address) {
        this.address = address;
        return this;
    }

    /**
     * Sets whether terms are accepted
     *
     * @param termsAccepted Terms accepted
     * @return UserBuilder instance
     */
    public UserBuilder withTermsAccepted(boolean termsAccepted) {
        this.termsAccepted = termsAccepted;
        return this;
    }

    /**
     * Builds a User object with current values
     *
     * @return User object
     */
    public User build() {
        return new User(firstName, lastName, email, password, phone, address, termsAccepted);
    }

    /**
     * Builds a User object with random values
     *
     * @return User object with random data
     */
    public User buildRandom() {
        this.firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
        this.lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
        this.email = firstName.toLowerCase() + "." + lastName.toLowerCase() +
                    random.nextInt(1000) + "@example.com";
        this.password = "Pass" + random.nextInt(10000) + "!";
        this.phone = generateRandomPhone();
        this.termsAccepted = true;

        return build();
    }

    /**
     * Builds a User object with invalid email
     *
     * @return User object with invalid email
     */
    public User buildWithInvalidEmail() {
        this.email = "invalid-email";
        return build();
    }

    /**
     * Builds a User object with weak password
     *
     * @return User object with weak password
     */
    public User buildWithWeakPassword() {
        this.password = "123";
        return build();
    }

    /**
     * Builds a User object with empty fields
     *
     * @return User object with empty fields
     */
    public User buildWithEmptyFields() {
        this.firstName = "";
        this.lastName = "";
        this.email = "";
        this.password = "";
        return build();
    }

    /**
     * Generates a random phone number
     *
     * @return Random phone number
     */
    private String generateRandomPhone() {
        return String.format("555-%03d-%04d",
                random.nextInt(1000),
                random.nextInt(10000));
    }

    /**
     * User data class
     */
    public static class User {
        private final String firstName;
        private final String lastName;
        private final String email;
        private final String password;
        private final String phone;
        private final String address;
        private final boolean termsAccepted;

        public User(String firstName, String lastName, String email, String password,
                   String phone, String address, boolean termsAccepted) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.password = password;
            this.phone = phone;
            this.address = address;
            this.termsAccepted = termsAccepted;
        }

        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getEmail() { return email; }
        public String getPassword() { return password; }
        public String getPhone() { return phone; }
        public String getAddress() { return address; }
        public boolean isTermsAccepted() { return termsAccepted; }

        @Override
        public String toString() {
            return "User{" +
                    "firstName='" + firstName + '\'' +
                    ", lastName='" + lastName + '\'' +
                    ", email='" + email + '\'' +
                    ", termsAccepted=" + termsAccepted +
                    '}';
        }
    }
}
