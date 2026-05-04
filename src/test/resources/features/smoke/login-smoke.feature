@smoke @login
Feature: Login Smoke Tests
  Verifies both admin and manager can authenticate and land on the products page.

  Scenario: Admin login succeeds and lands on products
    Given the user is on the login page
    When the user logs in as admin
    Then the user should be redirected to the products page
    And the navbar should be visible
    And the navbar should show the user name "Admin User"

  Scenario: Manager login succeeds and lands on products
    Given the user is on the login page
    When the user logs in as manager
    Then the user should be redirected to the products page
    And the navbar should be visible
    And the navbar should show the user name "Store Manager"
