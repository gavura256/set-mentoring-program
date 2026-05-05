package com.bookshop.ui.pages;

import com.microsoft.playwright.Page;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UsersPage extends BasePage {

    private static final String TABLE_SELECTOR = "#users-body table";
    private static final String ROWS_SELECTOR  = TABLE_SELECTOR + " tbody tr";
    private static final String DELETE_BTN     = "button:has-text(\"Delete\")";

    public UsersPage(Page page, String baseUrl) {
        super(page, baseUrl);
    }

    public int getRowCount() {
        return count(ROWS_SELECTOR);
    }

    public boolean hasDeleteButtons() {
        return count(DELETE_BTN) > 0;
    }

    public void waitForLoaded() {
        waitForSelector(TABLE_SELECTOR);
    }
}
