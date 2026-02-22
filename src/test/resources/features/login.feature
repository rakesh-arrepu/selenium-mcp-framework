@login
Feature: User Authentication
  As a tester
  I want to test user login functionality
  So that I can ensure authentication works correctly

  Background:
    Given user navigates to login page

  Scenario: Successful login with valid credentials
    When user enters "tomsmith" and password "SuperSecretPassword!"
    And user clicks login button
    Then dashboard should be displayed

  Scenario: Login fails with invalid password
    When user enters "tomsmith" and password "WrongPassword"
    And user clicks login button
    Then error message should appear "Your password is invalid!"

  Scenario: Login fails with non-existent user
    When user enters "nonexistent" and password "SomePassword123"
    And user clicks login button
    Then error message should appear "Your username is invalid!"
