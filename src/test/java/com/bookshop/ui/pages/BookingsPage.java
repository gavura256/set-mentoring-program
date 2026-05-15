package com.bookshop.ui.pages;

import com.microsoft.playwright.Page;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BookingsPage extends BasePage {

    private static final String TABLE_SELECTOR = "#bookings-body table";
    private static final String ROWS_SELECTOR = TABLE_SELECTOR + " tbody tr";
    private static final String STATUS_SELECTOR = "select.status-select";
    private static final String CANCELLED_VALUE = "CANCELLED";

    public BookingsPage(Page page, String baseUrl) {
        super(page, baseUrl);
    }

    public long getRowCount() {
        return countExcludingText(ROWS_SELECTOR, "No bookings found");
    }

    public void cancelLastBooking() {
        selectOptionOnLast(ROWS_SELECTOR, STATUS_SELECTOR, CANCELLED_VALUE);
    }

    public void waitForLoaded() {
        waitForSelector(TABLE_SELECTOR);
    }

    public void waitForActionComplete() {
        waitForNetworkIdle();
    }
}
