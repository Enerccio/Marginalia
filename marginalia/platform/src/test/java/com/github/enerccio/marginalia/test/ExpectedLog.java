package com.github.enerccio.marginalia.test;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.AppenderBase;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Captures what a logger writes while a test exercises a failure path on purpose, so expected errors are asserted
 * instead of printed (and mistaken for test failures).
 * <pre>
 * try (ExpectedLog log = ExpectedLog.capture(ExtensionServiceImpl.class)) {
 *     ...
 *     assertThat(log.errors()).anyMatch(m -> m.contains("broken"));
 * }
 * </pre>
 */
public final class ExpectedLog extends AppenderBase<ILoggingEvent> implements AutoCloseable {

    public record Entry(Level level, String message, Throwable throwable) {
    }

    private final Logger logger;
    private final boolean additivity;
    private final List<Entry> entries = new CopyOnWriteArrayList<>();

    private ExpectedLog(Logger logger) {
        this.logger = logger;
        this.additivity = logger.isAdditive();
        setContext(logger.getLoggerContext());
        start();
        logger.addAppender(this);
        // don't pass captured events to the console appender of the root logger
        logger.setAdditive(false);
    }

    public static ExpectedLog capture(String loggerName) {
        return new ExpectedLog((Logger) LoggerFactory.getLogger(loggerName));
    }

    public static ExpectedLog capture(Class<?> loggerClass) {
        return new ExpectedLog((Logger) LoggerFactory.getLogger(loggerClass));
    }

    @Override
    protected void append(ILoggingEvent event) {
        Throwable throwable = event.getThrowableProxy() instanceof ThrowableProxy proxy ? proxy.getThrowable() : null;
        entries.add(new Entry(event.getLevel(), event.getFormattedMessage(), throwable));
    }

    public List<Entry> entries() {
        return List.copyOf(entries);
    }

    public List<String> atLeast(Level level) {
        return entries.stream().filter(e -> e.level().isGreaterOrEqual(level)).map(Entry::message).toList();
    }

    public List<String> errors() {
        return atLeast(Level.ERROR);
    }

    public List<String> warnings() {
        return atLeast(Level.WARN);
    }

    @Override
    public void close() {
        logger.detachAppender(this);
        logger.setAdditive(additivity);
        stop();
    }
}
