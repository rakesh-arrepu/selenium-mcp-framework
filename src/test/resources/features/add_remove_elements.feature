@add_remove
Feature: Add and Remove Elements

  Users can dynamically add Delete buttons by clicking "Add Element"
  and remove them by clicking individual "Delete" buttons.

  Background:
    Given user navigates to add remove elements page
    Then add remove elements page should be displayed

  # ── Positive Test Cases ──────────────────────────────────────────

  Scenario: Verify page heading
    Then the page heading should be "Add/Remove Elements"

  Scenario: Add a single element
    When user clicks Add Element button
    Then there should be 1 delete button on the page

  Scenario: Add multiple elements
    When user clicks Add Element button 3 times
    Then there should be 3 delete buttons on the page

  Scenario: Delete an element reduces count
    When user clicks Add Element button 2 times
    Then there should be 2 delete buttons on the page
    When user clicks delete button at position 1
    Then there should be 1 delete button on the page

  # ── Negative Test Cases ──────────────────────────────────────────

  Scenario: No delete buttons present initially
    Then there should be 0 delete buttons on the page

  Scenario: Add and delete returns to empty state
    When user clicks Add Element button
    Then there should be 1 delete button on the page
    When user clicks delete button at position 1
    Then there should be 0 delete buttons on the page

  # ── Edge Cases ───────────────────────────────────────────────────

  Scenario: Add many elements and verify all present
    When user clicks Add Element button 5 times
    Then there should be 5 delete buttons on the page

  Scenario: Delete all elements one by one from last to first
    When user clicks Add Element button 3 times
    Then there should be 3 delete buttons on the page
    When user clicks delete button at position 3
    Then there should be 2 delete buttons on the page
    When user clicks delete button at position 2
    Then there should be 1 delete button on the page
    When user clicks delete button at position 1
    Then there should be 0 delete buttons on the page

  Scenario: Delete from middle shifts remaining elements
    When user clicks Add Element button 3 times
    Then there should be 3 delete buttons on the page
    When user clicks delete button at position 2
    Then there should be 2 delete buttons on the page

  Scenario: Add delete and add more elements
    When user clicks Add Element button 2 times
    Then there should be 2 delete buttons on the page
    When user clicks delete button at position 1
    Then there should be 1 delete button on the page
    When user clicks Add Element button 3 times
    Then there should be 4 delete buttons on the page
