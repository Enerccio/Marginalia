package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;
import com.github.enerccio.marginalia.domain.service.manual.AdminDataSource;
import com.github.enerccio.marginalia.domain.service.manual.AdminHelpToolsImpl;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The log of an extension as the help assistant's model gets it, and how the tool guards what it asks for.
 */
class HelpAdminDataTest {

    @Test
    void theModelGetsTheEndOfTheLogWithoutSecrets() {
        StringBuilder log = new StringBuilder();
        for (int i = 1; i <= 100; i++) {
            log.append("2026-10-16 12:00:").append(i % 60).append(" INFO line ").append(i).append('\n');
        }
        log.append("2026-10-16 12:30:00 ERROR request with Authorization: Bearer abcdef1234567890xyz failed\n");

        String text = HelpAdminData.tail("Side query", log.toString(), 5);

        assertThat(text).startsWith("Last 5 of 101 lines of the log of Side query, newest last:")
                .contains("line 97").contains("line 100").doesNotContain("line 96")
                .contains("Bearer [removed]").doesNotContain("abcdef1234567890xyz");
    }

    @Test
    void aLongLogIsCutFromTheFront() {
        String log = ("x".repeat(300) + "\n").repeat(100);

        String text = HelpAdminData.tail("ext", log, 100);

        assertThat(text).contains("[...]");
        assertThat(text.length()).isLessThan(HelpAdminData.MAX_LOG_CHARS + 300);
    }

    @Test
    void anEmptyLogSaysSo() {
        assertThat(HelpAdminData.tail("ext", "", 10)).contains("is empty");
        assertThat(HelpAdminData.tail("ext", null, 10)).contains("is empty");
        assertThat(HelpAdminData.tail("ext", "\n  \n", 10)).contains("is empty");
    }

    /** A source that records what it was asked for. */
    private static final class Source implements AdminDataSource {
        final List<String> asked = new ArrayList<>();

        @Override
        public String read(String topic) {
            asked.add(topic);
            return "data of " + topic;
        }

        @Override
        public String readExtensionLog(String extension, int lines) {
            asked.add(extension + "|" + lines);
            if (extension.equals("nope")) {
                throw new IllegalArgumentException("There is no extension \"nope\".");
            }
            return "log of " + extension;
        }
    }

    private static final class Result implements FunctionContinuation<String> {
        String completed;
        String failed;

        @Override
        public void complete(String result) {
            completed = result;
        }

        @Override
        public void fail(String message) {
            failed = message;
        }

        @Override
        public void fail(Throwable error) {
            failed = String.valueOf(error);
        }

        @Override
        public boolean isCancelled() {
            return false;
        }
    }

    @Test
    void theToolPassesOnOnlyKnownTopicsIgnoringCase() {
        Source source = new Source();
        AdminHelpToolsImpl tools = new AdminHelpToolsImpl(source);
        Result ok = new Result();
        Result unknown = new Result();

        tools.getAdminData(ok, " Extensions ");
        tools.getAdminData(unknown, "passwords");

        assertThat(ok.completed).isEqualTo("data of extensions");
        assertThat(unknown.failed).contains("There is no topic").contains("extensions, users, backups, cleanup, application");
        assertThat(source.asked).containsExactly("extensions");
    }

    @Test
    void theLogToolDefaultsAndClampsTheNumberOfLines() {
        Source source = new Source();
        AdminHelpToolsImpl tools = new AdminHelpToolsImpl(source);

        tools.readExtensionLog(new Result(), "sidequery", 0);
        tools.readExtensionLog(new Result(), "sidequery", 100000);
        tools.readExtensionLog(new Result(), "sidequery", 7);

        assertThat(source.asked).containsExactly("sidequery|40", "sidequery|200", "sidequery|7");
    }

    @Test
    void theLogToolTellsTheModelWhenThereIsNoSuchExtensionOrNoName() {
        AdminHelpToolsImpl tools = new AdminHelpToolsImpl(new Source());
        Result missing = new Result();
        Result blank = new Result();

        tools.readExtensionLog(missing, "nope", 10);
        tools.readExtensionLog(blank, "  ", 10);

        assertThat(missing.failed).contains("There is no extension");
        assertThat(blank.failed).contains("Name the extension");
    }
}
