@checkbox
Feature: Checkbox Interactions
  As a tester
  I want to test checkbox toggle functionality
  So that I can ensure checkbox interactions work correctly

  Background:
    Given user navigates to checkboxes page
    Then checkboxes page should be displayed

  Scenario: Verify checkboxes page displays correct heading
    Then page heading should display "Checkboxes"

  Scenario: Toggle first checkbox on and off
    When user clicks on checkbox 1
    And user clicks on checkbox 1
    Then checkbox 1 should be present

  Scenario: Toggle second checkbox off and on
    When user clicks on checkbox 2
    And user clicks on checkbox 2
    Then checkbox 2 should be present
