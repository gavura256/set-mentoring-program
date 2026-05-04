package com.bookshop.ui.pages;

import com.microsoft.playwright.Page;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BookingModal extends BasePage {

    private static final String QTY_SELECTOR = "#bm-qty";
    private static final String SUBMIT_SELECTOR = "#bookingModal button[type=\"submit\"]";

    public BookingModal(Page page, String baseUrl) {
        super(page, baseUrl);
    }

    public BookingModal fillQuantity(int qty) {
        fill(QTY_SELECTOR, String.valueOf(qty));
        return this;
    }

    public void submit() {
        click(SUBMIT_SELECTOR);
        waitForHidden(SUBMIT_SELECTOR);
    }
}
