package com.bookshop.ui.pages;

import com.microsoft.playwright.Page;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RegistrationPage extends BasePage {

    private static final String NAME_SELECTOR     = "#r-name";
    private static final String EMAIL_SELECTOR    = "#r-email";
    private static final String PASSWORD_SELECTOR = "#r-password";

    public RegistrationPage(Page page, String baseUrl) {
        super(page, baseUrl);
    }

    public boolean isFormVisible() {
        return isVisible(NAME_SELECTOR)
                && isVisible(EMAIL_SELECTOR)
                && isVisible(PASSWORD_SELECTOR);
    }

    public void waitForForm() {
        waitForSelector(NAME_SELECTOR);
    }
}
