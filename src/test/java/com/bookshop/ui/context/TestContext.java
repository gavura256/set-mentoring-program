package com.bookshop.ui.context;

import com.bookshop.ui.config.FrameworkConfig;
import com.bookshop.ui.driver.PlaywrightManager;
import com.bookshop.ui.pages.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("cucumber-glue")
@RequiredArgsConstructor
public class TestContext {

    @Getter private final FrameworkConfig config;
    @Getter private final PlaywrightManager playwrightManager;

    private LoginPage loginPage;
    private RegistrationPage registrationPage;
    private ProductsPage productsPage;
    private ProductDetailPage productDetailPage;
    private BookingsPage bookingsPage;
    private UsersPage usersPage;
    private Navbar navbar;

    public LoginPage getLoginPage() {
        if (loginPage == null) loginPage = new LoginPage(playwrightManager.getPage(), config.getBaseUrl());
        return loginPage;
    }

    public RegistrationPage getRegistrationPage() {
        if (registrationPage == null) registrationPage = new RegistrationPage(playwrightManager.getPage(), config.getBaseUrl());
        return registrationPage;
    }

    public ProductsPage getProductsPage() {
        if (productsPage == null) productsPage = new ProductsPage(playwrightManager.getPage(), config.getBaseUrl());
        return productsPage;
    }

    public ProductDetailPage getProductDetailPage() {
        if (productDetailPage == null) productDetailPage = new ProductDetailPage(playwrightManager.getPage(), config.getBaseUrl());
        return productDetailPage;
    }

    public BookingsPage getBookingsPage() {
        if (bookingsPage == null) bookingsPage = new BookingsPage(playwrightManager.getPage(), config.getBaseUrl());
        return bookingsPage;
    }

    public UsersPage getUsersPage() {
        if (usersPage == null) usersPage = new UsersPage(playwrightManager.getPage(), config.getBaseUrl());
        return usersPage;
    }

    public Navbar getNavbar() {
        if (navbar == null) navbar = new Navbar(playwrightManager.getPage());
        return navbar;
    }
}
