package com.bookshop.ui.pages;

import com.microsoft.playwright.Page;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProductsPage extends BasePage {

    private static final String ROWS_SELECTOR = "#products-body tbody tr";
    private static final String QTY_SELECTOR    = ROWS_SELECTOR + " td:nth-child(5)";
    private static final String ACTIONS_TOGGLE  = ROWS_SELECTOR + " td:last-child .dropdown-toggle";
    private static final String ACTIONS_BOOK    = ROWS_SELECTOR + " td:last-child .dropdown-item";

    public ProductsPage(Page page, String baseUrl) {
        super(page, baseUrl);
    }

    public int getRowCount() {
        return count(ROWS_SELECTOR);
    }

    public void clickFirstProductRow() {
        clickFirst(ROWS_SELECTOR);
    }

    public int getFirstProductQuantity() {
        int qty = Integer.parseInt(firstInnerText(QTY_SELECTOR));
        log.info("First product quantity: {}", qty);
        return qty;
    }

    public void clickBookFirstProduct() {
        clickFirst(ACTIONS_TOGGLE);
        clickFirst(ACTIONS_BOOK);
    }
}
