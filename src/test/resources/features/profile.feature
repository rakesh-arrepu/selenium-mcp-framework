Feature: User Profile Management
  As a registered user
  I want to manage my profile information
  So that I can keep my account details up to date

  Background:
    Given user navigates to profile page
    Then user should see profile page

  @smoke @profile
  Scenario: View user profile information
    Then profile name should be displayed as "John Doe"
    And profile email should be displayed as "john.doe@example.com"

  @smoke @profile
  Scenario: Successfully update profile information
    When user clicks edit profile button
    And user enters profile first name "Jane"
    And user enters profile last name "Smith"
    And user enters profile email "jane.smith@example.com"
    And user enters profile phone "555-123-4567"
    And user clicks save profile button
    Then profile should be updated successfully
    And profile success message should be displayed

  @smoke @profile
  Scenario: Successfully update profile using combined method
    When user updates profile with "Michael", "Johnson", "michael.johnson@example.com", and "555-234-5678"
    And user saves profile changes
    Then profile should be updated successfully
    And profile success message should contain "updated successfully"

  @regression @profile
  Scenario: Update profile with address
    When user clicks edit profile button
    And user enters profile first name "Sarah"
    And user enters profile last name "Williams"
    And user enters profile email "sarah.williams@example.com"
    And user enters profile phone "555-345-6789"
    And user enters profile address "123 Main Street, Springfield, IL 62701"
    And user clicks save profile button
    Then profile should be updated successfully

  @smoke @profile
  Scenario: Upload profile picture
    When user clicks edit profile button
    And user uploads profile picture "/path/to/profile-picture.jpg"
    And user clicks save profile button
    Then profile should be updated successfully
    And profile picture should be updated

  @regression @profile
  Scenario: Cancel profile changes
    When user clicks edit profile button
    And user enters profile first name "Temporary"
    And user enters profile last name "Name"
    And user clicks cancel profile button
    Then profile changes should not be saved

  @regression @profile
  Scenario: Cancel profile changes using combined method
    When user updates profile with "Old", "Name", "old.name@example.com", and "555-111-2222"
    And user cancels profile changes
    Then profile changes should not be saved

  @regression @profile
  Scenario: Profile update fails with invalid email format
    When user clicks edit profile button
    And user enters profile first name "Robert"
    And user enters profile last name "Brown"
    And user enters profile email "invalid-email"
    And user enters profile phone "555-456-7890"
    And user clicks save profile button
    Then profile error message should appear "Invalid email format"

  @regression @profile
  Scenario: Profile update fails with empty first name
    When user clicks edit profile button
    And user enters profile first name ""
    And user enters profile last name "Davis"
    And user enters profile email "davis@example.com"
    And user enters profile phone "555-567-8901"
    And user clicks save profile button
    Then profile error message should appear "First name is required"

  @regression @profile
  Scenario: Profile update fails with empty last name
    When user clicks edit profile button
    And user enters profile first name "Patricia"
    And user enters profile last name ""
    And user enters profile email "patricia@example.com"
    And user enters profile phone "555-678-9012"
    And user clicks save profile button
    Then profile error message should appear "Last name is required"

  @regression @profile
  Scenario: Profile update fails with invalid phone number
    When user clicks edit profile button
    And user enters profile first name "David"
    And user enters profile last name "Miller"
    And user enters profile email "david.miller@example.com"
    And user enters profile phone "invalid-phone"
    And user clicks save profile button
    Then profile error message should appear "Invalid phone number"

  @regression @profile
  Scenario: Update only first name
    When user clicks edit profile button
    And user enters profile first name "Christopher"
    And user clicks save profile button
    Then profile should be updated successfully

  @regression @profile
  Scenario: Update only email
    When user clicks edit profile button
    And user enters profile email "newemail@example.com"
    And user clicks save profile button
    Then profile should be updated successfully

  @regression @profile
  Scenario: Update only phone number
    When user clicks edit profile button
    And user enters profile phone "555-789-0123"
    And user clicks save profile button
    Then profile should be updated successfully

  @security @profile
  Scenario: Profile update prevents SQL injection in name
    When user clicks edit profile button
    And user enters profile first name "admin' OR '1'='1"
    And user enters profile last name "Test"
    And user clicks save profile button
    Then profile error message should appear "Invalid input"

  @security @profile
  Scenario: Profile update prevents XSS attack in fields
    When user clicks edit profile button
    And user enters profile first name "<script>alert('XSS')</script>"
    And user enters profile last name "Test"
    And user clicks save profile button
    Then profile error message should appear "Invalid input"

  @regression @profile
  Scenario: Profile update with special characters in name
    When user clicks edit profile button
    And user enters profile first name "María"
    And user enters profile last name "O'Brien-García"
    And user enters profile email "maria.obrien@example.com"
    And user enters profile phone "555-890-1234"
    And user clicks save profile button
    Then profile should be updated successfully

  @regression @profile
  Scenario: Profile update with international phone format
    When user clicks edit profile button
    And user enters profile first name "Thomas"
    And user enters profile last name "Anderson"
    And user enters profile email "thomas.anderson@example.com"
    And user enters profile phone "+1-555-901-2345"
    And user clicks save profile button
    Then profile should be updated successfully

  @regression @profile
  Scenario: Profile update with maximum length fields
    When user clicks edit profile button
    And user enters profile first name "VeryLongFirstNameThatExceedsNormalLength"
    And user enters profile last name "VeryLongLastNameThatExceedsNormalLength"
    And user enters profile email "very.long.email.address@example.com"
    And user enters profile phone "555-012-3456"
    And user clicks save profile button
    Then profile error message should appear "Input exceeds maximum length"

  @regression @profile
  Scenario: Profile update with minimum length fields
    When user clicks edit profile button
    And user enters profile first name "A"
    And user enters profile last name "B"
    And user enters profile email "a@b.co"
    And user enters profile phone "555-1234"
    And user clicks save profile button
    Then profile should be updated successfully

  @regression @profile
  Scenario: Navigate to change password from profile
    When user clicks change password link
    Then user should see password reset page

  @smoke @profile
  Scenario: Profile update with screenshot capture
    When user clicks edit profile button
    And user takes profile screenshot "before_profile_update"
    And user enters profile first name "Linda"
    And user enters profile last name "Martinez"
    And user enters profile email "linda.martinez@example.com"
    And user enters profile phone "555-123-9876"
    And user clicks save profile button
    Then profile should be updated successfully
    And user takes profile screenshot "after_profile_update"

  @regression @profile
  Scenario: Multiple profile updates in sequence
    When user updates profile with "First", "Update", "first@example.com", and "555-111-1111"
    And user saves profile changes
    Then profile should be updated successfully
    When user updates profile with "Second", "Update", "second@example.com", and "555-222-2222"
    And user saves profile changes
    Then profile should be updated successfully

  @regression @profile
  Scenario: Profile update with email already in use
    When user clicks edit profile button
    And user enters profile email "alice.anderson@example.com"
    And user clicks save profile button
    Then profile error message should appear "Email already in use"
    And profile error message should contain "another account"

  @regression @profile
  Scenario: Profile update with whitespace in email
    When user clicks edit profile button
    And user enters profile email " testuser@example.com "
    And user clicks save profile button
    Then profile error message should appear "Invalid email format"

  @regression @profile
  Scenario: Profile update preserves existing data when fields are not changed
    When user clicks edit profile button
    And user enters profile phone "555-999-8888"
    And user clicks save profile button
    Then profile should be updated successfully
    And profile name should be displayed as "John Doe"

  @regression @profile
  Scenario: Profile picture upload with invalid file type
    When user clicks edit profile button
    And user uploads profile picture "/path/to/document.pdf"
    And user clicks save profile button
    Then profile error message should appear "Invalid file type"
    And profile error message should contain "image"

  @regression @profile
  Scenario: Profile picture upload with file size exceeding limit
    When user clicks edit profile button
    And user uploads profile picture "/path/to/large-image.jpg"
    And user clicks save profile button
    Then profile error message should appear "File size exceeds limit"

  @smoke @profile
  Scenario: Verify profile changes are reflected immediately
    When user updates profile with "Immediate", "Test", "immediate.test@example.com", and "555-777-7777"
    And user saves profile changes
    Then profile should be updated successfully
    And profile name should be displayed as "Immediate Test"
    And profile email should be displayed as "immediate.test@example.com"
