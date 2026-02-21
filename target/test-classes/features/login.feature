Feature: User Authentication
  As a tester
  I want to test user login functionality
  So that I can ensure authentication works correctly

  Background:
    Given user navigates to login page
    Then user should see login page

  Scenario: Successful login with valid credentials
    When user enters "testuser@example.com" and password "SecurePass123"
    And user clicks login button
    Then dashboard should be displayed

  Scenario: Login fails with invalid password
    When user enters "testuser@example.com" and password "WrongPassword"
    And user clicks login button
    Then error message should appear "Invalid credentials"

  Scenario: Login fails with non-existent user
    When user enters "nonexistent@example.com" and password "SomePassword123"
    And user clicks login button
    Then error message should appear "User not found"

  Scenario: Multiple login attempts with different credentials
    When user enters "admin@example.com" and password "AdminPass123"
    And user clicks login button
    Then dashboard should be displayed
    When user takes screenshot "successful_login"
