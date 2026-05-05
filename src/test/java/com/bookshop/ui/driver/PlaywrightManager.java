package com.bookshop.ui.driver;

import com.bookshop.ui.config.BrowserName;
import com.bookshop.ui.config.FrameworkConfig;
import com.microsoft.playwright.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
@Scope("cucumber-glue")
@RequiredArgsConstructor
public class PlaywrightManager {

    private final FrameworkConfig config;

    private Playwright playwright;
    private BrowserContext context;
    private Browser browser;
    @Getter private Page page;
    private boolean tracingStarted;

    public void initialize() {
        if (playwright != null) {
            log.warn("PlaywrightManager.initialize() called more than once — ignoring duplicate call");
            return;
        }
        BrowserName selected = config.getBrowser();
        log.info("Initialising Playwright [browser={}, headless={}]", selected, config.isHeadless());

        selected.checkCompatible(config.isHeadless());

        playwright = Playwright.create();
        BrowserType.LaunchOptions opts = new BrowserType.LaunchOptions()
                .setHeadless(config.isHeadless())
                .setSlowMo(config.getSlowMotionMs());
        browser = selected.createBrowserType(playwright).launch(opts);

        context = browser.newContext(
                new Browser.NewContextOptions()
                        .setViewportSize(1920, 1080)
                        .setLocale("en-US")
        );
        context.setDefaultTimeout(config.getDefaultTimeout().toMillis());

        page = context.newPage();
        log.info("Browser session ready [1920×1080]");
    }

    public void cleanup() {
        log.info("Closing browser session");
        closeSilently(page);
        closeSilently(context);
        closeSilently(browser);
        closeSilently(playwright);
    }

    public void startTrace() {
        context.tracing().start(
                new Tracing.StartOptions().setScreenshots(true).setSnapshots(true)
        );
        tracingStarted = true;
    }

    public void stopTrace(String name) {
        if (!tracingStarted) {
            log.debug("stopTrace called but tracing was never started — skipping");
            return;
        }
        Path dest = Paths.get(config.getTracesDir(), name + ".zip");
        ensureDir(dest.getParent());
        context.tracing().stop(new Tracing.StopOptions().setPath(dest));
        tracingStarted = false;
        log.info("Trace saved → {}", dest);
    }

    public byte[] captureScreenshot() {
        return page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
    }

    public void saveScreenshot(String name) {
        Path dest = Paths.get(config.getScreenshotsDir(), name + ".png");
        ensureDir(dest.getParent());
        page.screenshot(new Page.ScreenshotOptions().setFullPage(true).setPath(dest));
        log.info("Screenshot saved → {}", dest);
    }

    private void closeSilently(AutoCloseable resource) {
        if (resource != null) {
            try { resource.close(); } catch (Exception e) {
                log.warn("Error closing resource: {}", e.getMessage());
            }
        }
    }

    private void ensureDir(Path dir) {
        try { Files.createDirectories(dir); } catch (Exception e) {
            log.warn("Could not create directory {}: {}", dir, e.getMessage());
        }
    }
}
