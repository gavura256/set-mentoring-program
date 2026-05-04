package com.bookshop.ui.pages;

import com.microsoft.playwright.Page;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BookingsPage extends BasePage {

    private static final String ROWS_SELECTOR = "#bookings-body tbody tr";

    public BookingsPage(Page page, String baseUrl) {
        super(page, baseUrl);
    }

    public long getRowCount() {
        return all(ROWS_SELECTOR).stream()
                .filter(row -> !row.innerText().contains("No bookings found"))
                .count();
    }
}
