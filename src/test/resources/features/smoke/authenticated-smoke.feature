@smoke @authenticated
Feature: Authenticated Pages Smoke Tests
  Verifies key pages render with data after admin login.
  Each scenario is self-contained and includes its own login step.

  Scenario: Products list renders with data
    Given the admin user is logged in
    When the user navigates to the products page
    Then the products table should have at least one data row

  Scenario: Product detail page loads for any product
    Given the admin user is logged in
    When the user navigates to the products page
    And the user clicks the first product row
    Then the product detail page should show a title and a price in dollar format

  Scenario: Bookings page renders with data
    Given the admin user is logged in
    When the user navigates to the bookings page
    Then the bookings table should have at least one row

  Scenario: Admin sees users list with admin-only delete buttons
    Given the admin user is logged in
    When the user navigates to the users page
    Then the users table should have at least one row
    And Delete buttons should be visible
