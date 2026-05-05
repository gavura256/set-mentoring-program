package com.bookshop.ui.pages;

import com.microsoft.playwright.Page;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProductDetailPage extends BasePage {

    private static final String TITLE_SELECTOR = "#pd-body h3";
    private static final String PRICE_SELECTOR = "#pd-body .fs-3.fw-bold";

    public ProductDetailPage(Page page, String baseUrl) {
        super(page, baseUrl);
    }

    public String getProductTitle() {
        return innerText(TITLE_SELECTOR);
    }

    public String getPriceText() {
        return innerText(PRICE_SELECTOR);
    }

    public void waitForLoaded() {
        waitForSelector(TITLE_SELECTOR);
    }
}
