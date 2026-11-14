package com.github.enerccio.marginalia.domain.repository.impl;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The saves are remembered by the database that owns the row: the ids of two databases are the same numbers (the contexts of the tests
 * are one JVM with a database each), and a row of one is not the row of the other.
 */
class StaleSavesScopeTest {

    private final Object database = new Object();
    private final Object another = new Object();

    @BeforeEach
    void failing() {
        StaleSaves.setMode(StaleSaves.Mode.FAIL);
        StaleSaves.reset();
    }

    @AfterEach
    void back() {
        StaleSaves.setMode(null);
        StaleSaves.reset();
    }

    private static ChatMessage message(long id, long modification) {
        ChatMessage message = new ChatMessage();
        message.setId(id);
        message.setModification(new Date(modification));
        return message;
    }

    @Test
    void aRowOfAnotherDatabaseWithTheSameIdIsNotTheSameRow() {
        StaleSaves.written(database, message(1877, 2_000));

        // a copy from the past of the row of the other database: the other database never saved it
        assertThatCode(() -> StaleSaves.check(another, message(1877, 1_000))).doesNotThrowAnyException();
    }

    @Test
    void anOlderCopyOfTheRowOfTheSameDatabaseIsStale() {
        StaleSaves.written(database, message(1877, 2_000));

        assertThatThrownBy(() -> StaleSaves.check(database, message(1877, 1_000))).isInstanceOf(StaleSaves.StaleSaveException.class)
                .hasMessageContaining("ChatMessage 1877");
        assertThatCode(() -> StaleSaves.check(database, message(1877, 2_000))).doesNotThrowAnyException();
        assertThatCode(() -> StaleSaves.check(database, message(1878, 1_000))).doesNotThrowAnyException();
    }

    @Test
    void aRemovedRowIsForgottenInItsDatabaseOnly() {
        StaleSaves.written(database, message(1877, 2_000));
        StaleSaves.written(another, message(1877, 2_000));

        StaleSaves.removed(database, message(1877, 2_000));

        assertThatCode(() -> StaleSaves.check(database, message(1877, 1_000))).doesNotThrowAnyException();
        assertThatThrownBy(() -> StaleSaves.check(another, message(1877, 1_000))).isInstanceOf(StaleSaves.StaleSaveException.class);
    }

    @Test
    void theLastSaveIsTheNewestOfAnyDatabase() {
        StaleSaves.written(database, message(1877, 2_000));
        StaleSaves.written(another, message(1877, 3_000));

        assertThat(StaleSaves.lastSave(message(1877, 0))).isEqualTo(new Date(3_000));
        assertThat(StaleSaves.lastSave(message(1, 0))).isNull();
    }
}
