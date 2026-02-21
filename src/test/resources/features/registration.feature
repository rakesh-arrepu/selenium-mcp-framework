Feature: User Registration
  As a new user
  I want to register for an account
  So that I can access the application

  Background:
    Given user navigates to registration page
    Then user should see registration page

  @smoke @registration
  Scenario: Successful registration with valid details
    When user enters first name "Alice"
    And user enters last name "Anderson"
    And user enters registration email "alice.anderson@example.com"
    And user enters registration password "AlicePass123!"
    And user enters password confirmation "AlicePass123!"
    And user enters phone number "555-111-2222"
    And user accepts terms and conditions
    And user clicks register button
    Then registration should be successful
    And registration success message should be displayed

  @smoke @registration
  Scenario: Successful registration using complete details method
    When user registers with "Bob", "Baker", "bob.baker@example.com", "BobSecure456!", and "555-222-3333"
    And user accepts terms and conditions
    And user submits registration form
    Then registration should be successful

  @regression @registration
  Scenario: Registration fails with invalid email format
    When user enters first name "Charlie"
    And user enters last name "Clark"
    And user enters registration email "invalid-email"
    And user enters registration password "CharliePass789!"
    And user enters password confirmation "CharliePass789!"
    And user enters phone number "555-333-4444"
    And user accepts terms and conditions
    And user clicks register button
    Then registration error message should appear "Invalid email format"
    And email validation error should be displayed

  @regression @registration
  Scenario: Registration fails with empty first name
    When user enters first name ""
    And user enters last name "Davis"
    And user enters registration email "empty.name@example.com"
    And user enters registration password "EmptyPass123!"
    And user enters password confirmation "EmptyPass123!"
    And user enters phone number "555-444-5555"
    And user accepts terms and conditions
    And user clicks register button
    Then registration error message should appear "First name is required"
    And required field error should be displayed for "first name"

  @regression @registration
  Scenario: Registration fails with weak password
    When user enters first name "Eve"
    And user enters last name "Evans"
    And user enters registration email "eve.evans@example.com"
    And user enters registration password "123"
    And user enters password confirmation "123"
    And user enters phone number "555-555-6666"
    And user accepts terms and conditions
    And user clicks register button
    Then registration error message should appear "Password is too weak"
    And password validation error should be displayed

  @regression @registration
  Scenario: Registration fails without accepting terms
    When user enters first name "Frank"
    And user enters last name "Foster"
    And user enters registration email "frank.foster@example.com"
    And user enters registration password "FrankPass123!"
    And user enters password confirmation "FrankPass123!"
    And user enters phone number "555-666-7777"
    And user does not accept terms and conditions
    And user clicks register button
    Then registration error message should appear "You must accept the terms and conditions"
    And terms acceptance error should be displayed

  @regression @registration
  Scenario: Registration fails with password mismatch
    When user enters first name "Grace"
    And user enters last name "Green"
    And user enters registration email "grace.green@example.com"
    And user enters registration password "GracePass123!"
    And user enters password confirmation "DifferentPass456!"
    And user enters phone number "555-777-8888"
    And user accepts terms and conditions
    And user clicks register button
    Then password mismatch error should be displayed

  @regression @registration
  Scenario: Registration fails with missing email
    When user enters first name "Henry"
    And user enters last name "Harris"
    And user enters registration email ""
    And user enters registration password "HenryPass123!"
    And user enters password confirmation "HenryPass123!"
    And user enters phone number "555-888-9999"
    And user accepts terms and conditions
    And user clicks register button
    Then required field error should be displayed for "email"

  @regression @registration
  Scenario: Registration fails with missing last name
    When user enters first name "Irene"
    And user enters last name ""
    And user enters registration email "irene.irwin@example.com"
    And user enters registration password "IrenePass123!"
    And user enters password confirmation "IrenePass123!"
    And user enters phone number "555-999-0000"
    And user accepts terms and conditions
    And user clicks register button
    Then required field error should be displayed for "last name"

  @security @registration
  Scenario: Registration prevents SQL injection in email
    When user enters first name "Hacker"
    And user enters last name "Test"
    And user enters registration email "admin' OR '1'='1"
    And user enters registration password "HackerPass123!"
    And user enters password confirmation "HackerPass123!"
    And user enters phone number "555-000-1111"
    And user accepts terms and conditions
    And user clicks register button
    Then registration error message should appear "Invalid email format"

  @security @registration
  Scenario: Registration prevents XSS attack in name fields
    When user enters first name "<script>alert('XSS')</script>"
    And user enters last name "Test"
    And user enters registration email "xss.test@example.com"
    And user enters registration password "XSSPass123!"
    And user enters password confirmation "XSSPass123!"
    And user enters phone number "555-111-0000"
    And user accepts terms and conditions
    And user clicks register button
    Then registration error message should appear "Invalid input"

  @regression @registration
  Scenario: Clear registration form and re-enter data
    When user enters first name "Wrong"
    And user enters last name "Data"
    And user clears registration form
    Then registration form should be empty
    When user enters first name "Correct"
    And user enters last name "Data"
    And user enters registration email "correct.data@example.com"
    And user enters registration password "CorrectPass123!"
    And user enters password confirmation "CorrectPass123!"
    And user enters phone number "555-222-1111"
    And user accepts terms and conditions
    And user clicks register button
    Then registration should be successful

  @regression @registration
  Scenario: Registration with minimum valid password length
    When user enters first name "Kevin"
    And user enters last name "King"
    And user enters registration email "kevin.king@example.com"
    And user enters registration password "Pass123!"
    And user enters password confirmation "Pass123!"
    And user enters phone number "555-333-2222"
    And user accepts terms and conditions
    And user clicks register button
    Then registration should be successful

  @regression @registration
  Scenario: Registration with maximum length fields
    When user enters first name "VeryLongFirstNameThatExceedsTheNormalLengthLimitForTestingPurposes"
    And user enters last name "VeryLongLastNameThatExceedsTheNormalLengthLimitForTestingPurposes"
    And user enters registration email "very.long.email.address.for.testing@example.com"
    And user enters registration password "VeryLongPassword123!WithSpecialChars"
    And user enters password confirmation "VeryLongPassword123!WithSpecialChars"
    And user enters phone number "555-444-3333"
    And user accepts terms and conditions
    And user clicks register button
    Then registration error message should appear "Input exceeds maximum length"

  @smoke @registration
  Scenario: Registration with screenshot capture
    When user enters first name "Laura"
    And user enters last name "Lewis"
    And user enters registration email "laura.lewis@example.com"
    And user enters registration password "LauraPass123!"
    And user enters password confirmation "LauraPass123!"
    And user enters phone number "555-555-4444"
    And user accepts terms and conditions
    And user takes registration screenshot "before_registration"
    And user clicks register button
    Then registration should be successful
    And success message should contain "successfully registered"
    And user takes registration screenshot "after_registration"

  @regression @registration
  Scenario: Registration with special characters in name
    When user enters first name "María"
    And user enters last name "O'Brien-Smith"
    And user enters registration email "maria.obrien@example.com"
    And user enters registration password "MariaPass123!"
    And user enters password confirmation "MariaPass123!"
    And user enters phone number "555-666-5555"
    And user accepts terms and conditions
    And user clicks register button
    Then registration should be successful

  @regression @registration
  Scenario: Registration with international phone format
    When user enters first name "Nathan"
    And user enters last name "Nelson"
    And user enters registration email "nathan.nelson@example.com"
    And user enters registration password "NathanPass123!"
    And user enters password confirmation "NathanPass123!"
    And user enters phone number "+1-555-777-6666"
    And user accepts terms and conditions
    And user clicks register button
    Then registration should be successful

  @regression @registration
  Scenario: Registration fails with duplicate email
    When user registers with "Olivia", "Owen", "alice.anderson@example.com", "OliviaPass123!", and "555-888-7777"
    And user accepts terms and conditions
    And user submits registration form
    Then registration error message should appear "Email already exists"
    And error message should contain "already registered"
