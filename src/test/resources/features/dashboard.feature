Feature: Dashboard Operations
  As a logged-in user
  I want to interact with the dashboard
  So that I can access application features

  Background:
    Given user navigates to login page
    And user enters "testuser@example.com" and password "SecurePass123"
    And user clicks login button
    And dashboard should be displayed

  @smoke @dashboard
  Scenario: View welcome message on dashboard
    Then user should see welcome message containing "Welcome"
    When user takes screenshot "dashboard_welcome"

  @regression @dashboard
  Scenario: Verify user profile is displayed
    Then user profile section should be visible
    And user avatar should be displayed

  @smoke @dashboard
  Scenario: Logout from dashboard
    When user clicks logout button
    Then user should see login page
    When user takes screenshot "after_logout"

  @regression @dashboard
  Scenario: Navigate to settings from dashboard
    When user clicks settings link
    Then settings page should be displayed

  @regression @dashboard
  Scenario: Open notifications panel
    When user clicks notifications icon
    Then notifications panel should be displayed

  @regression @dashboard
  Scenario: Click user avatar to open profile menu
    When user clicks user avatar
    Then user menu should be displayed

  @smoke @dashboard
  Scenario: Verify dashboard title is correct
    Then dashboard title should be "Dashboard"

  @regression @dashboard
  Scenario: Verify navigation menu is present
    Then navigation menu should be displayed
    And navigation menu should contain links

  @dashboard
  Scenario: Dashboard elements are properly loaded
    Then welcome message should be visible
    And logout button should be visible
    And user profile should be visible
    And navigation menu should be visible

  @regression @dashboard
  Scenario: Screenshot dashboard on load
    When user takes screenshot "dashboard_loaded"
    Then dashboard page should be fully loaded

  @smoke @dashboard
  Scenario: Multiple dashboard interactions
    When user clicks user profile
    And user waits for 1 seconds
    Then user profile details should be displayed
    When user clicks settings link
    And user waits for 2 seconds
    Then settings page should load successfully
