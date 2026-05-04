package com.bookshop.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BasePage {

    protected final Page page;
    private final String baseUrl;

    public void open() {
        log.info("Navigating to {}", baseUrl);
        page.navigate(baseUrl);
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        log.info("Page opened – url={}", baseUrl);
    }

    protected void click(String selector) {
        log.info("Clicking: {}", selector);
        page.locator(selector).click();
    }

    protected void clickFirst(String selector) {
        log.info("Clicking first: {}", selector);
        page.locator(selector).first().click();
    }

    protected void fill(String selector, String value) {
        log.info("Filling {} with ***", selector);
        page.locator(selector).fill(value);
    }

    protected String innerText(String selector) {
        String text = page.locator(selector).innerText();
        log.info("Text from {}: '{}'", selector, text);
        return text;
    }

    protected String firstInnerText(String selector) {
        String text = page.locator(selector).first().innerText();
        log.info("First text from {}: '{}'", selector, text);
        return text;
    }

    protected int count(String selector) {
        int count = page.locator(selector).count();
        log.info("Count({}): {}", selector, count);
        return count;
    }

    protected boolean isVisible(String selector) {
        boolean visible = page.locator(selector).isVisible();
        log.info("isVisible({}): {}", selector, visible);
        return visible;
    }

    protected List<Locator> all(String selector) {
        List<Locator> list = page.locator(selector).all();
        log.info("all({}): {} elements", selector, list.size());
        return list;
    }

    protected boolean waitForHidden(String selector) {
        try {
            page.locator(selector)
                    .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
            log.info("waitForHidden({}): true", selector);
            return true;
        } catch (Exception e) {
            log.debug("waitForHidden({}) timed out", selector, e);
        }
        return !page.locator(selector).isVisible();
    }
}
