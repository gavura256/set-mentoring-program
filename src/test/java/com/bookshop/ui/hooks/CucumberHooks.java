package com.bookshop.ui.hooks;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.bookshop.AllureLogAppender;
import com.bookshop.ui.context.TestContext;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.qameta.allure.Allure;
import io.qameta.allure.model.Attachment;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StepResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class CucumberHooks {

    private static final String UNSAFE_CHARS    = "[^a-zA-Z0-9_\\-]";
    private static final int    MAX_NAME_LENGTH = 100;
    private final TestContext ctx;

    @Before
    public void setUp(Scenario scenario) {
        log.info("▶ Starting scenario: [{}]", scenario.getName());
        ctx.getPlaywrightManager().initialize();
        ctx.getPlaywrightManager().startTrace();
    }

    @After
    public void tearDown(Scenario scenario) {
        String safeName = scenario.getName().replaceAll(UNSAFE_CHARS, "_");
        if (safeName.length() > MAX_NAME_LENGTH) {
            safeName = safeName.substring(0, MAX_NAME_LENGTH);
        }

        ctx.getPlaywrightManager().stopTrace(safeName);

        if (scenario.isFailed()) {
            attachScreenshotToFailedStep(scenario);
            attachTrace(safeName);
            ctx.getPlaywrightManager().saveScreenshot(safeName + "_final");
        }

        ctx.getPlaywrightManager().cleanup();
        log.info("■ Finished scenario: [{}] – {}", scenario.getName(), scenario.getStatus());

        attachScenarioLogs();
    }

    private void attachScenarioLogs() {
        List<ILoggingEvent> events = AllureLogAppender.getAndClear();
        if (events.isEmpty()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (ILoggingEvent event : events) {
            sb.append(String.format("%s %-5s %s - %s%n",
                    Instant.ofEpochMilli(event.getTimeStamp()),
                    event.getLevel(),
                    event.getLoggerName(),
                    event.getFormattedMessage()));
        }
        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        Allure.addAttachment("Logs", "text/plain", new ByteArrayInputStream(bytes), ".txt");
    }

    private void attachScreenshotToFailedStep(Scenario scenario) {
        byte[] screenshot = ctx.getPlaywrightManager().captureScreenshot();
        if (screenshot == null || screenshot.length == 0) return;

        String source = UUID.randomUUID() + ".png";
        String resultsDir = System.getProperty("allure.results.directory", "target/allure-results");
        Path dir = Paths.get(resultsDir);
        try {
            Files.createDirectories(dir);
            Files.write(dir.resolve(source), screenshot);
        } catch (IOException e) {
            log.warn("Failed to write failure screenshot to {}", dir, e);
            return;
        }

        Attachment attachment = new Attachment()
                .setName("failure-screenshot")
                .setType("image/png")
                .setSource(source);

        Allure.getLifecycle().updateTestCase(scenario.getId(), tc ->
                findFailedStep(tc.getSteps())
                        .ifPresent(step -> step.getAttachments().add(attachment)));
    }

    private void attachTrace(String safeName) {
        Path traceFile = Paths.get(ctx.getConfig().getTracesDir(), safeName + ".zip");
        if (!Files.exists(traceFile)) {
            log.debug("Trace file not found, skipping Allure attachment: {}", traceFile);
            return;
        }
        try (InputStream is = Files.newInputStream(traceFile)) {
            Allure.addAttachment("playwright-trace", "application/zip", is, "zip");
        } catch (IOException e) {
            log.warn("Failed to attach playwright trace to Allure: {}", traceFile, e);
        }
    }

    private static Optional<StepResult> findFailedStep(List<StepResult> steps) {
        return steps.stream()
                .filter(step -> step.getStatus() == Status.FAILED)
                .findFirst();
    }
}
