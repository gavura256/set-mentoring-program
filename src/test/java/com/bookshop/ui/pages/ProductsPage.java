package com.bookshop.ui.pages;

import com.microsoft.playwright.Page;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProductsPage extends BasePage {

    private static final String ROWS_SELECTOR = "#products-body tbody tr";

    public ProductsPage(Page page, String baseUrl) {
        super(page, baseUrl);
    }

    public int getRowCount() {
        return count(ROWS_SELECTOR);
    }

    public void clickFirstProductRow() {
        clickFirst(ROWS_SELECTOR);
    }
}
