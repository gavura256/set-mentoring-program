package com.bookshop.ui.steps;

import com.bookshop.ui.context.TestContext;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RequiredArgsConstructor
public class NavigationSteps {

    private final TestContext ctx;

    @When("the user navigates to the bookings page")
    public void theUserNavigatesToTheBookingsPage() {
        ctx.getNavbar().clickBookings();
        ctx.getPlaywrightManager().getPage().waitForSelector("#bookings-body table");
    }

    @Then("the bookings table should have at least one row")
    public void theBookingsTableShouldHaveRows() {
        long count = ctx.getBookingsPage().getRowCount();
        log.info("Bookings table row count: {}", count);
        assertThat(count)
                .as("Bookings table should have at least one row")
                .isGreaterThanOrEqualTo(1);
    }

    @When("the user clicks the Logout button")
    public void theUserClicksTheLogoutButton() {
        ctx.getNavbar().clickLogout();
    }

    @When("the user clicks the register link")
    public void theUserClicksTheRegisterLink() {
        ctx.getLoginPage().clickRegisterLink();
    }

    @Then("the registration form should be visible")
    public void theRegistrationFormShouldBeVisible() {
        assertThat(ctx.getRegistrationPage().isFormVisible())
                .as("Registration form should be visible after clicking register link")
                .isTrue();
    }
}
