package com.bookshop.ui.steps;

import com.bookshop.ui.context.TestContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RequiredArgsConstructor
public class LoginSteps {

    private final TestContext ctx;

    @Given("the user is on the login page")
    public void theUserIsOnTheLoginPage() {
        ctx.getLoginPage().open();
    }

    @Given("the admin user is logged in")
    public void theAdminUserIsLoggedIn() {
        ctx.getLoginPage().open();
        logIn(ctx.getConfig().getAdminEmail(), ctx.getConfig().getAdminPassword());
        assertThat(ctx.getLoginPage().isLoginSuccessful())
                .as("Admin login should succeed")
                .isTrue();
    }

    @Given("the manager user is logged in")
    public void theManagerUserIsLoggedIn() {
        ctx.getLoginPage().open();
        logIn(ctx.getConfig().getManagerEmail(), ctx.getConfig().getManagerPassword());
        assertThat(ctx.getLoginPage().isLoginSuccessful())
                .as("Manager login should succeed")
                .isTrue();
    }

    @When("the user logs in as admin")
    public void theUserLogsInAsAdmin() {
        logIn(ctx.getConfig().getAdminEmail(), ctx.getConfig().getAdminPassword());
    }

    @When("the user logs in as manager")
    public void theUserLogsInAsManager() {
        logIn(ctx.getConfig().getManagerEmail(), ctx.getConfig().getManagerPassword());
    }

    @Then("the user should be redirected to the products page")
    public void theUserShouldBeRedirectedToTheProductsPage() {
        assertThat(ctx.getLoginPage().isLoginSuccessful())
                .as("User should be redirected away from login page after successful login")
                .isTrue();
    }

    @And("the navbar should be visible")
    public void theNavbarShouldBeVisible() {
        assertThat(ctx.getNavbar().isVisible())
                .as("Navbar should be visible after successful login")
                .isTrue();
    }

    @And("the navbar should show the user name {string}")
    public void theNavbarShouldShowTheUserName(String expectedName) {
        String actual = ctx.getNavbar().getUserName();
        log.info("Navbar user name: '{}'", actual);
        assertThat(actual)
                .as("Navbar should display the logged-in user's name")
                .contains(expectedName);
    }

    @When("the user navigates directly to {string}")
    public void theUserNavigatesDirectlyTo(String hash) {
        ctx.getPlaywrightManager().getPage().navigate(ctx.getConfig().getBaseUrl() + "/#" + hash);
        ctx.getPlaywrightManager().getPage().waitForLoadState(
                com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);
    }

    @Then("the login form should be displayed")
    public void theLoginFormShouldBeDisplayed() {
        assertThat(ctx.getLoginPage().isLoginFormVisible())
                .as("Login form should be displayed")
                .isTrue();
    }

    private void logIn(String email, String password) {
        ctx.getLoginPage()
                .fillEmail(email)
                .fillPassword(password)
                .submitLogin();
    }
}
