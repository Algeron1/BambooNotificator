package com.rgs.bamboonotifier.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.AppenderBase;
import jakarta.annotation.PostConstruct;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

@Component
public class InMemoryLogAppender extends AppenderBase<ILoggingEvent> {

    private static final int MAX_ENTRIES = 500;

    private final Deque<LogEntry> buffer = new ArrayDeque<>(MAX_ENTRIES + 1);

    @PostConstruct
    public void init() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        setContext(context);
        setName("IN_MEMORY");
        start();
        Logger root = context.getLogger(Logger.ROOT_LOGGER_NAME);
        root.addAppender(this);
    }

    @Override
    protected synchronized void append(ILoggingEvent event) {
        if (event.getLevel().isGreaterOrEqual(Level.INFO)) {
            if (buffer.size() >= MAX_ENTRIES) buffer.pollFirst();
            buffer.addLast(new LogEntry(
                    event.getLevel().toString(),
                    event.getTimeStamp(),
                    shortName(event.getLoggerName()),
                    event.getFormattedMessage(),
                    formatThrowable(event.getThrowableProxy())
            ));
        }
    }

    public synchronized List<LogEntry> getEntries() {
        return new ArrayList<>(buffer);
    }

    private static String shortName(String name) {
        if (name == null) return "";
        int idx = name.lastIndexOf('.');
        return idx >= 0 ? name.substring(idx + 1) : name;
    }

    private static String formatThrowable(IThrowableProxy proxy) {
        if (proxy == null) return null;
        StringBuilder sb = new StringBuilder(proxy.getClassName()).append(": ").append(proxy.getMessage()).append("\n");
        for (StackTraceElementProxy el : proxy.getStackTraceElementProxyArray()) {
            sb.append("  at ").append(el.getSTEAsString()).append("\n");
            if (sb.length() > 2000) { sb.append("  ..."); break; }
        }
        return sb.toString();
    }

    public record LogEntry(String level, long timestamp, String logger, String message, String throwable) {}
}
