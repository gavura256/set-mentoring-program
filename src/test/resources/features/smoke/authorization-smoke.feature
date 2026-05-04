@smoke @authorization
Feature: Authorization Smoke Tests
  Verifies role-based access control and auth guard behavior in the UI.

  Scenario: Manager sees users list but no admin-only delete buttons
    Given the manager user is logged in
    When the user navigates to the users page
    Then the users table should have at least one row
    And Delete buttons should not be visible

  Scenario Outline: Unauthenticated user is redirected to login
    When the user navigates directly to "<path>"
    Then the login form should be displayed

    Examples:
      | path      |
      | products  |
      | bookings  |
      | users     |
