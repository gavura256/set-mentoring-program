package com.bookshop.ui.steps;

import com.bookshop.ui.context.TestContext;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RequiredArgsConstructor
public class ProductsSteps {

    private final TestContext ctx;

    @When("the user navigates to the products page")
    public void theUserNavigatesToTheProductsPage() {
        ctx.getNavbar().clickProducts();
        ctx.getPlaywrightManager().getPage().waitForSelector("#products-body table");
    }

    @Then("the products table should have at least one data row")
    public void theProductsTableShouldHaveDataRows() {
        int count = ctx.getProductsPage().getRowCount();
        log.info("Products table row count: {}", count);
        assertThat(count)
                .as("Products table should have at least one data row")
                .isGreaterThanOrEqualTo(1);
    }

    @When("the user clicks the first product row")
    public void theUserClicksTheFirstProductRow() {
        ctx.getProductsPage().clickFirstProductRow();
        ctx.getPlaywrightManager().getPage().waitForSelector("#pd-body h3");
    }

    @Then("the product detail page should show a title and a price in dollar format")
    public void theProductDetailPageShouldShowTitleAndPrice() {
        String title = ctx.getProductDetailPage().getProductTitle();
        String price = ctx.getProductDetailPage().getPriceText();
        log.info("Product detail — title='{}', price='{}'", title, price);
        assertThat(title).as("Product title should not be blank").isNotBlank();
        assertThat(price).as("Price should match $N.NN format").matches("\\$\\d+\\.\\d{2}");
    }
}
