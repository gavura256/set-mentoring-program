package com.bookshop.ui.steps;

import com.bookshop.ui.context.TestContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RequiredArgsConstructor
public class BookingSteps {

    private final TestContext ctx;

    @And("the user notes the available quantity of the first product")
    public void theUserNotesTheAvailableQuantityOfTheFirstProduct() {
        int qty = ctx.getProductsPage().getFirstProductQuantity();
        ctx.setNotedQuantity(qty);
        log.info("Noted product quantity: {}", qty);
    }

    @And("the user books the first product with quantity {int}")
    public void theUserBooksTheFirstProductWithQuantity(int qty) {
        ctx.getProductsPage().clickBookFirstProduct();
        ctx.getBookingModal().fillQuantity(qty).submit();
    }

    @And("the user cancels the most recent booking")
    public void theUserCancelsTheMostRecentBooking() {
        ctx.getBookingsPage().cancelLastBooking();
        ctx.getBookingsPage().waitForActionComplete();
    }

    @Then("the available quantity of the first product should be decreased by {int}")
    public void theAvailableQuantityOfTheFirstProductShouldBeDecreasedBy(int delta) {
        int actual = ctx.getProductsPage().getFirstProductQuantity();
        int expected = ctx.getNotedQuantity() - delta;
        log.info("Expected quantity: {}, actual: {}", expected, actual);
        assertThat(actual)
                .as("Product quantity should decrease by %d after booking", delta)
                .isEqualTo(expected);
    }

    @Then("the available quantity of the first product should be restored to the noted value")
    public void theAvailableQuantityOfTheFirstProductShouldBeRestoredToTheNotedValue() {
        int actual = ctx.getProductsPage().getFirstProductQuantity();
        int expected = ctx.getNotedQuantity();
        log.info("Expected quantity: {}, actual: {}", expected, actual);
        assertThat(actual)
                .as("Product quantity should be restored to noted value after cancellation")
                .isEqualTo(expected);
    }
}
