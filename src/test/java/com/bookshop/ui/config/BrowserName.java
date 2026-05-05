package com.bookshop.ui.config;

import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;

import java.util.function.Function;

public enum BrowserName {

    CHROMIUM(Playwright::chromium),
    FIREFOX(Playwright::firefox),
    WEBKIT(Playwright::webkit);

    private final Function<Playwright, BrowserType> browserTypeProvider;

    BrowserName(Function<Playwright, BrowserType> browserTypeProvider) {
        this.browserTypeProvider = browserTypeProvider;
    }

    public BrowserType createBrowserType(Playwright playwright) {
        return browserTypeProvider.apply(playwright);
    }

    public void checkCompatible(boolean headless) {
        if (this == WEBKIT && headless && isWindows()) {
            throw new IllegalStateException(
                    "WebKit headless mode is not supported on Windows. " +
                    "Set framework.headless=false or use chromium/firefox.");
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }
}
