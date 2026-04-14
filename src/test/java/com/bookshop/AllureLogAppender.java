package com.bookshop;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

import java.util.ArrayList;
import java.util.List;

public class AllureLogAppender extends AppenderBase<ILoggingEvent> {

    private static final ThreadLocal<List<ILoggingEvent>> EVENTS =
            ThreadLocal.withInitial(ArrayList::new);

    @Override
    protected void append(ILoggingEvent event) {
        EVENTS.get().add(event);
    }

    public static List<ILoggingEvent> getAndClear() {
        List<ILoggingEvent> events = new ArrayList<>(EVENTS.get());
        EVENTS.get().clear();
        return events;
    }

    public static void clear() {
        EVENTS.get().clear();
    }
}
