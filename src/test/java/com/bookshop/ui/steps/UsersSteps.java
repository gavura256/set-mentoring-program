package com.bookshop.ui.steps;

import com.bookshop.ui.context.TestContext;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RequiredArgsConstructor
public class UsersSteps {

    private final TestContext ctx;

    @When("the user navigates to the users page")
    public void theUserNavigatesToTheUsersPage() {
        ctx.getNavbar().clickUsers();
        ctx.getUsersPage().waitForLoaded();
    }

    @Then("the users table should have at least one row")
    public void theUsersTableShouldHaveRows() {
        int count = ctx.getUsersPage().getRowCount();
        log.info("Users table row count: {}", count);
        assertThat(count)
                .as("Users table should have at least one row")
                .isGreaterThanOrEqualTo(1);
    }

    @Then("Delete buttons should be visible")
    public void deleteButtonsShouldBeVisible() {
        assertThat(ctx.getUsersPage().hasDeleteButtons())
                .as("Admin-only Delete buttons should be visible")
                .isTrue();
    }

    @Then("Delete buttons should not be visible")
    public void deleteButtonsShouldNotBeVisible() {
        assertThat(ctx.getUsersPage().hasDeleteButtons())
                .as("Admin-only Delete buttons should NOT be visible for manager")
                .isFalse();
    }
}
