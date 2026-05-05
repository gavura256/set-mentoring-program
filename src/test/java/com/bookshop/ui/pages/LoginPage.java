package com.bookshop.ui.pages;

import com.bookshop.ui.config.HashRoute;
import com.microsoft.playwright.Page;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoginPage extends BasePage {

    private static final String EMAIL_SELECTOR    = "#l-email";
    private static final String PASSWORD_SELECTOR = "#l-password";
    private static final String SUBMIT_SELECTOR   = "button[type=\"submit\"]";
    private static final String REGISTER_LINK     = "a[href=\"%s\"]".formatted(HashRoute.REGISTER);

    public LoginPage(Page page, String baseUrl) {
        super(page, baseUrl);
    }

    public boolean isLoginFormVisible() {
        return isVisible(EMAIL_SELECTOR) && isVisible(PASSWORD_SELECTOR);
    }

    public LoginPage fillEmail(String email) {
        fill(EMAIL_SELECTOR, email);
        return this;
    }

    public LoginPage fillPassword(String password) {
        fill(PASSWORD_SELECTOR, password);
        return this;
    }

    public LoginPage submitLogin() {
        click(SUBMIT_SELECTOR);
        return this;
    }

    public boolean isLoginSuccessful() {
        return waitForHidden(PASSWORD_SELECTOR);
    }

    public void clickRegisterLink() {
        click(REGISTER_LINK);
    }

    public void waitForLoginForm() {
        waitForSelector(EMAIL_SELECTOR);
    }
}
