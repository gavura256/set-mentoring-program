@smoke @booking-stock
Feature: Booking Stock Quantity Smoke Tests
  Verifies that creating and cancelling bookings correctly adjusts product stock.

  Scenario: Booking decreases stock and cancellation restores it
    Given the admin user is logged in
    When the user navigates to the products page
    And the user notes the available quantity of the first product
    And the user books the first product with quantity 1
    And the user navigates to the products page
    Then the available quantity of the first product should be decreased by 1
    When the user navigates to the bookings page
    And the user cancels the most recent booking
    And the user navigates to the products page
    Then the available quantity of the first product should be restored to the noted value
