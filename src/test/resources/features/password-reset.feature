Feature: Password Reset
  As a user who forgot their password
  I want to reset my password
  So that I can regain access to my account

  Background:
    Given user navigates to password reset page
    Then user should see password reset page

  @smoke @password-reset
  Scenario: Successful password reset request with valid email
    When user enters email "testuser@example.com" for password reset
    And user clicks reset password button
    Then password reset email should be sent
    And password reset success message should be displayed
    And reset success message should contain "check your email"

  @smoke @password-reset
  Scenario: Successful password reset using combined method
    When user requests password reset for "alice.anderson@example.com"
    Then password reset should be successful
    And reset success message should contain "reset link"

  @regression @password-reset
  Scenario: Password reset fails with invalid email format
    When user enters email "invalid-email" for password reset
    And user clicks reset password button
    Then password reset error message should appear "Invalid email format"
    And password reset email validation error should be displayed

  @regression @password-reset
  Scenario: Password reset fails with empty email
    When user enters email "" for password reset
    And user clicks reset password button
    Then password reset error message should be displayed
    And reset error message should contain "required"

  @regression @password-reset
  Scenario: Password reset fails with non-existent email
    When user enters email "nonexistent@example.com" for password reset
    And user clicks reset password button
    Then password reset error message should appear "User not found"

  @smoke @password-reset
  Scenario: Complete password reset flow with verification code
    When user requests password reset for "bob.baker@example.com"
    Then password reset email should be sent
    And user waits 2 seconds for reset email
    When user enters verification code "123456"
    And user enters new password "NewSecurePass123!"
    And user confirms new password "NewSecurePass123!"
    And user submits new password
    Then password reset should be successful

  @smoke @password-reset
  Scenario: Complete password reset using combined method
    When user requests password reset for "charlie.clark@example.com"
    Then password reset email should be sent
    When user completes password reset with code "654321" and password "CharlieNewPass456!"
    Then password reset should be successful

  @regression @password-reset
  Scenario: Password reset fails with mismatched passwords
    When user requests password reset for "dave.davis@example.com"
    Then password reset email should be sent
    When user enters verification code "111111"
    And user enters new password "NewPassword123!"
    And user confirms new password "DifferentPassword456!"
    And user submits new password
    Then password reset error message should appear "Passwords do not match"

  @regression @password-reset
  Scenario: Password reset fails with weak new password
    When user requests password reset for "eve.evans@example.com"
    Then password reset email should be sent
    When user enters verification code "222222"
    And user enters new password "weak"
    And user confirms new password "weak"
    And user submits new password
    Then password reset error message should appear "Password is too weak"

  @regression @password-reset
  Scenario: Password reset fails with invalid verification code
    When user requests password reset for "frank.foster@example.com"
    Then password reset email should be sent
    When user enters verification code "000000"
    And user enters new password "FrankNewPass123!"
    And user confirms new password "FrankNewPass123!"
    And user submits new password
    Then password reset error message should appear "Invalid verification code"

  @regression @password-reset
  Scenario: Password reset fails with expired verification code
    When user requests password reset for "grace.green@example.com"
    Then password reset email should be sent
    When user enters verification code "999999"
    And user enters new password "GraceNewPass123!"
    And user confirms new password "GraceNewPass123!"
    And user submits new password
    Then password reset error message should appear "Verification code expired"

  @security @password-reset
  Scenario: Password reset prevents SQL injection in email
    When user enters email "admin' OR '1'='1" for password reset
    And user clicks reset password button
    Then password reset error message should appear "Invalid email format"

  @security @password-reset
  Scenario: Password reset prevents XSS attack in email
    When user enters email "<script>alert('XSS')</script>" for password reset
    And user clicks reset password button
    Then password reset error message should appear "Invalid email format"

  @regression @password-reset
  Scenario: User returns to login page from password reset
    When user enters email "testuser@example.com" for password reset
    And user clicks back to login link
    Then user should see login page

  @regression @password-reset
  Scenario: Multiple password reset requests for same email
    When user requests password reset for "henry.harris@example.com"
    Then password reset email should be sent
    When user requests password reset for "henry.harris@example.com"
    Then password reset error message should appear "Reset request already pending"

  @regression @password-reset
  Scenario: Password reset with email containing special characters
    When user requests password reset for "user+test@example.com"
    Then password reset email should be sent

  @regression @password-reset
  Scenario: Password reset with case-sensitive email validation
    When user requests password reset for "TestUser@Example.COM"
    Then password reset email should be sent

  @regression @password-reset
  Scenario: Password reset with whitespace in email
    When user enters email " testuser@example.com " for password reset
    And user clicks reset password button
    Then password reset error message should appear "Invalid email format"

  @smoke @password-reset
  Scenario: Password reset with minimum valid password length
    When user requests password reset for "irene.irwin@example.com"
    Then password reset email should be sent
    When user completes password reset with code "333333" and password "Pass123!"
    Then password reset should be successful

  @regression @password-reset
  Scenario: Password reset with maximum password length
    When user requests password reset for "jack.jackson@example.com"
    Then password reset email should be sent
    When user enters verification code "444444"
    And user enters new password "VeryLongPasswordWithSpecialCharactersAndNumbers123456789!@#$%"
    And user confirms new password "VeryLongPasswordWithSpecialCharactersAndNumbers123456789!@#$%"
    And user submits new password
    Then password reset should be successful
