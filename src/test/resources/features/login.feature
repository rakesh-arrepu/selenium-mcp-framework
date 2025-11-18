Feature: User Authentication
  As a tester
  I want to test user login functionality
  So that I can ensure authentication works correctly

  Background:
    Given user navigates to login page
    Then user should see login page

  @smoke @regression
  Scenario: Successful login with valid credentials
    When user enters "testuser@example.com" and password "SecurePass123"
    And user clicks login button
    Then dashboard should be displayed

  @regression
  Scenario: Login fails with invalid password
    When user enters "testuser@example.com" and password "WrongPassword"
    And user clicks login button
    Then error message should appear "Invalid credentials"

  @regression
  Scenario: Login fails with non-existent user
    When user enters "nonexistent@example.com" and password "SomePassword123"
    And user clicks login button
    Then error message should appear "User not found"

  @smoke
  Scenario: Multiple login attempts with different credentials
    When user enters "admin@example.com" and password "AdminPass123"
    And user clicks login button
    Then dashboard should be displayed
    When user takes screenshot "successful_login"

  @regression @security
  Scenario: Login fails with empty credentials
    When user enters "" and password ""
    And user clicks login button
    Then error message is displayed

  @regression @security
  Scenario: Login fails with empty username
    When user enters "" and password "SecurePass123"
    And user clicks login button
    Then error message is displayed

  @regression @security
  Scenario: Login fails with empty password
    When user enters "testuser@example.com" and password ""
    And user clicks login button
    Then error message is displayed

  @security
  Scenario: SQL injection attempt is prevented
    When user enters "admin' OR '1'='1" and password "anything"
    And user clicks login button
    Then error message should appear "Invalid credentials"

  @security
  Scenario: XSS attack validation is handled
    When user enters "<script>alert('XSS')</script>" and password "test"
    And user clicks login button
    Then error message should appear "Invalid credentials"

  @regression
  Scenario: Username is case sensitive
    When user enters "TESTUSER@EXAMPLE.COM" and password "SecurePass123"
    And user clicks login button
    Then error message should appear "Invalid credentials"

  @regression
  Scenario: Password is case sensitive
    When user enters "testuser@example.com" and password "securepass123"
    And user clicks login button
    Then error message should appear "Invalid credentials"

  @regression
  Scenario: Login with whitespace in credentials
    When user enters " testuser@example.com " and password " SecurePass123 "
    And user clicks login button
    Then error message should appear "Invalid credentials"

  @smoke
  Scenario: Clear credentials and re-enter
    When user enters "wrong@email.com" and password "WrongPass"
    And user clears username field
    And user clears password field
    And user enters "testuser@example.com" and password "SecurePass123"
    And user clicks login button
    Then dashboard should be displayed
