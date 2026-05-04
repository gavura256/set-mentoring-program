@smoke @navigation
Feature: Navigation Smoke Tests
  Verifies logout and registration page navigation.

  Scenario: Logout redirects to login page
    Given the admin user is logged in
    When the user clicks the Logout button
    Then the login form should be displayed

  Scenario: Registration page is accessible from login page
    Given the user is on the login page
    When the user clicks the register link
    Then the registration form should be visible
