package com.bookshop.ui.pages;

import com.microsoft.playwright.Page;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Navbar extends BasePage {

    private static final String PRODUCTS_LINK  = ".nav-link[href=\"#/products\"]";
    private static final String BOOKINGS_LINK  = ".nav-link[href=\"#/bookings\"]";
    private static final String USERS_LINK     = ".nav-link[href=\"#/users\"]";
    private static final String LOGOUT_BTN     = "button:has-text(\"Logout\")";
    private static final String BRAND_SELECTOR = ".navbar-brand";
    private static final String USER_NAME      = ".navbar .text-light";

    public Navbar(Page page) {
        super(page, "");
    }

    public boolean isVisible() {
        return isVisible(BRAND_SELECTOR);
    }

    public String getUserName() {
        return innerText(USER_NAME);
    }

    public void clickProducts() {
        click(PRODUCTS_LINK);
    }

    public void clickBookings() {
        click(BOOKINGS_LINK);
    }

    public void clickUsers() {
        click(USERS_LINK);
    }

    public void clickLogout() {
        click(LOGOUT_BTN);
    }
}
