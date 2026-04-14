package com.bookshop;

import ch.qos.logback.classic.spi.ILoggingEvent;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

public class AllureLogExtension implements BeforeEachCallback, AfterEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) {
        AllureLogAppender.clear();
    }

    @Override
    public void afterEach(ExtensionContext context) {
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
}
