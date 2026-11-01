package com.github.enerccio.marginalia.revision;

import com.github.enerccio.marginalia.bound.Migration.MigrationType;
import com.github.enerccio.marginalia.bound.migration.PartRevisionMigration;
import com.github.enerccio.marginalia.domain.collections.RevisionSource;
import com.github.enerccio.marginalia.domain.components.RevisionRow;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.MessageRevision;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.CleanupService.EntityKey;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.MessageRevisionService;
import com.github.enerccio.marginalia.domain.service.TrashService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link PartRevisionMigration}: the first versions of the parts that were written before the history was kept.
 */
class PartRevisionMigrationTest extends MarginaliaTestBase {

    @Autowired
    private MessageRevisionService messageRevisionService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private TrashService trashService;

    @Autowired
    private AutowireCapableBeanFactory beanFactory;

    private Manuscript book;
    private ChatMessage last;

    @BeforeEach
    void createBook() throws Exception {
        login();
        Manuscript manuscript = new Manuscript();
        manuscript.setName(uniqueName("book"));
        book = manuscriptService.save(manuscript);
    }

    /**
     * A part as the version before had it: no versions.
     */
    private ChatMessage legacy(String response, String original, String model, String protocol, String reasoning) throws Exception {
        ChatMessage created = chatMessageService.createPart(book, last, response, null);
        ChatMessage stored = chatMessageService.find(created.getId());
        stored.setOriginalResponse(original);
        stored.setModelUsed(model);
        stored.setProtocolUsed(protocol);
        stored.setResponseReasoning(reasoning);
        chatMessageService.save(stored);
        messageRevisionService.hardDeleteForParts(List.of(stored.getId()));
        last = stored;
        return stored;
    }

    private void migrate() throws Exception {
        PartRevisionMigration migration = new PartRevisionMigration();
        beanFactory.autowireBean(migration);
        assertThat(migration.migrate(6, MigrationType.APP)).isEqualTo(7);
    }

    private List<MessageRevision> history(ChatMessage part) throws Exception {
        List<MessageRevision> versions = new ArrayList<>();
        for (RevisionRow row : messageRevisionService.list(part, true).reversed()) {
            versions.add(messageRevisionService.find(row.id()));
        }
        return versions;
    }

    @Test
    void aGeneratedPartStartsWithTheGeneratedTextTheReasoningAndTheModel() throws Exception {
        ChatMessage part = legacy("The text.", "The text.", "gemma", "chat", "Thinking.");

        migrate();

        List<MessageRevision> versions = history(part);
        assertThat(versions).hasSize(1);
        assertThat(versions.getFirst().getSource()).isEqualTo(RevisionSource.GENERATE);
        assertThat(versions.getFirst().getResponse()).isEqualTo("The text.");
        assertThat(versions.getFirst().getResponseReasoning()).isEqualTo("Thinking.");
        assertThat(versions.getFirst().getSourceDetail()).isEqualTo("gemma · chat");
        assertThat(versions.getFirst().isAgent()).isFalse();
        assertThat(versions.getFirst().getOwner().getId()).isEqualTo(part.getOwner().getId());
        assertThat(versions.getFirst().getWordCount()).isEqualTo(part.getWordCount());
        assertThat(versions.getFirst().getTokenCount()).isEqualTo(part.getTokenCount());
    }

    @Test
    void anEditedGeneratedPartHasTheGeneratedTextAndTheEditedOneAfterIt() throws Exception {
        ChatMessage part = legacy("The text, edited by hand.", "The generated text.", "gemma", null, "Thinking.");

        migrate();

        List<MessageRevision> versions = history(part);
        assertThat(versions).extracting(MessageRevision::getSource).containsExactly(RevisionSource.GENERATE, RevisionSource.HUMAN);
        assertThat(versions).extracting(MessageRevision::getResponse).containsExactly("The generated text.", "The text, edited by hand.");
        assertThat(versions).extracting(MessageRevision::getResponseReasoning).containsExactly("Thinking.", null);
        assertThat(versions.getFirst().getSourceDetail()).isEqualTo("gemma");
        assertThat(versions.getFirst().getWordCount()).isEqualTo(3);
        assertThat(versions.getFirst().getTokenCount()).isZero();
        assertThat(versions.getLast().getWordCount()).isEqualTo(chatMessageService.find(part.getId()).getWordCount());
        assertThat(messageRevisionService.findLatest(part).getResponse()).isEqualTo("The text, edited by hand.");
    }

    @Test
    void aPartOfTheFirstVersionsOfTheAppHasOneGeneratedVersionWhateverWasEdited() throws Exception {
        // OriginalResponseMigration copied the text of every part, so an edited part has the same text in both
        ChatMessage part = legacy("Edited long ago.", "Edited long ago.", "gemma", "chat", null);

        migrate();

        assertThat(history(part)).extracting(MessageRevision::getSource).containsExactly(RevisionSource.GENERATE);
    }

    @Test
    void aTypedPartStartsWithAHumanVersion() throws Exception {
        ChatMessage typed = legacy("Typed.", "Typed.", null, null, null);
        ChatMessage withoutOriginal = legacy("Imported.", null, null, null, "A reasoning that is not kept.");

        migrate();

        assertThat(history(typed)).extracting(MessageRevision::getSource).containsExactly(RevisionSource.HUMAN);
        assertThat(history(typed).getFirst().getSourceDetail()).isNull();
        assertThat(history(withoutOriginal)).extracting(MessageRevision::getResponse).containsExactly("Imported.");
        assertThat(history(withoutOriginal).getFirst().getResponseReasoning()).isNull();
    }

    @Test
    void aPartWithoutTextOrWithVersionsGetsNone() throws Exception {
        ChatMessage empty = legacy("   ", null, null, null, null);
        ChatMessage recorded = legacy("Already recorded.", null, "gemma", null, null);
        MessageRevision existing = messageRevisionService.record(chatMessageService.find(recorded.getId()),
                RevisionSource.HUMAN, "the first one", false);

        migrate();

        assertThat(history(empty)).isEmpty();
        assertThat(history(recorded)).extracting(MessageRevision::getId).containsExactly(existing.getId());
    }

    @Test
    void theMigrationCanBeRepeatedAndAnotherVersionIsNotItsBusiness() throws Exception {
        ChatMessage part = legacy("Edited.", "Generated.", "gemma", "chat", "Thinking.");

        migrate();
        migrate();

        assertThat(history(part)).hasSize(2);
        PartRevisionMigration migration = new PartRevisionMigration();
        beanFactory.autowireBean(migration);
        assertThat(migration.migrate(5, MigrationType.APP)).isEqualTo(5);
        assertThat(migration.migrate(7, MigrationType.APP)).isEqualTo(7);
    }

    @Test
    void thePartIsNotTouched() throws Exception {
        ChatMessage part = legacy("Edited.", "Generated.", "gemma", "chat", "Thinking.");
        ChatMessage before = chatMessageService.find(part.getId());

        migrate();

        ChatMessage after = chatMessageService.find(part.getId());
        assertThat(after.getModification()).isEqualTo(before.getModification());
        assertThat(after.getResponse()).isEqualTo("Edited.");
        assertThat(after.getResponseReasoning()).isEqualTo("Thinking.");
    }

    @Test
    void aDeletedPartHasItsVersionsDeletedAndRestoringThePartBringsThemBack() throws Exception {
        ChatMessage first = legacy("First.", "First.", "gemma", "chat", null);
        ChatMessage second = legacy("Second.", "Second.", "gemma", "chat", null);
        chatMessageService.delete(chatMessageService.find(second.getId()), false);
        messageRevisionService.hardDeleteForParts(List.of(first.getId(), second.getId()));

        migrate();

        assertThat(messageRevisionService.list(first, false)).hasSize(1);
        assertThat(messageRevisionService.list(second, false)).isEmpty();
        assertThat(messageRevisionService.list(second, true)).hasSize(1);
        trashService.restore(List.of(new EntityKey(ChatMessage.class, second.getId())));
        assertThat(messageRevisionService.list(second, false)).hasSize(1);
    }

    @Test
    void theVersionsOfManyPartsNeedNewIdsFromTheDatabaseAndStillAreMade() throws Exception {
        // more rows than an id generator has ids in a block, so it has to update its sequence table (on another connection,
        // which SQLite refuses to a transaction that has read before) at least once during the migration
        List<ChatMessage> parts = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            parts.add(legacy("Edited " + i, "Generated " + i, "gemma", "chat", "Thinking " + i));
        }

        migrate();

        for (int i = 0; i < parts.size(); i++) {
            assertThat(history(parts.get(i))).extracting(MessageRevision::getResponse).containsExactly("Generated " + i, "Edited " + i);
        }
    }
}
